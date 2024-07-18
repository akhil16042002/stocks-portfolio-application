package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.Exchange;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.dto.StockNameExchangeDto;
import com.akhil.stocks_portfolio.dto.TradeType;
import com.akhil.stocks_portfolio.entity.*;
import com.akhil.stocks_portfolio.repository.StockRepository;
import com.akhil.stocks_portfolio.repository.TradeRepository;
import com.akhil.stocks_portfolio.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;

import java.util.*;

@Service
@Slf4j
public class PortfolioServiceImpl implements PortfolioService{

    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public ResponseEntity<Response<Portfolio>> getPortfolioDetails(String userName) {
        try {
            log.info("Fetching trade details for {}", userName);

            Optional<User> optionalUser = userRepository.findByUserName(userName);
            if (optionalUser.isEmpty()) {
                return Response.failed(HttpStatus.BAD_REQUEST, "User name " + userName + " does not exist");
            }
            User user = optionalUser.get();
            List<Trade> tradeList = tradeRepository.findAllByUserName(userName);
            Map<StockNameExchangeDto, List<Trade>> stockToTradeMap = new HashMap<>();
            for (Trade trade : tradeList) {
                StockNameExchangeDto key = StockNameExchangeDto.builder()
                        .stockName(trade.getStockName())
                        .exchange(trade.getExchange())
                        .build();
                if (stockToTradeMap.containsKey(key)) {
                    stockToTradeMap.get(key).add(trade);
                }
                else {
                    List<Trade> trades = new ArrayList<>();
                    trades.add(trade);
                    stockToTradeMap.put(key, trades);
                }
            }
            List<Holding> holdings = new ArrayList<>();
            for (Map.Entry<StockNameExchangeDto, List<Trade>> entry : stockToTradeMap.entrySet()) {
                StockNameExchangeDto key = entry.getKey();
                List<Trade> trades = entry.getValue();
                Optional<Stock> optionalStock = stockRepository.findByStockNameAndExchange(
                        key.getStockName(),
                        key.getExchange());
                if (optionalStock.isEmpty()) {
                    return Response.failed(HttpStatus.BAD_REQUEST, "Stock " + key.getStockName() + " is not present in " + key.getExchange());
                }
                Stock stock = optionalStock.get();
                Holding holding = calculateHolding(stock, trades);
                if (!ObjectUtils.isEmpty(holding)) {
                    holdings.add(holding);
                }
            }
            if (holdings.isEmpty()) {
                log.info("You have no holdings present in your portfolio");
                return Response.failed(HttpStatus.BAD_REQUEST, "You have no holdings present in your portfolio");
            }
            Portfolio portfolio = calculatePortfolio(user, holdings);
            log.info("Successfully fetched portfolio details for: {} with portfolio: {}", userName, portfolio);
            return Response.success(HttpStatus.OK, portfolio);
        } catch (Exception e) {
            log.error("Error while fetching trade details for: {}", userName, e);
            throw new RuntimeException("Error occurred while fetching trade details for : " + userName, e);
        }
    }

    private Portfolio calculatePortfolio(User user, List<Holding> holdings) {
        double totalInvestment = 0;
        double totalHolding = 0;
        for (Holding holding : holdings) {
            totalInvestment += holding.getCurrentInvestment();
            totalHolding += holding.getCurrentHolding();
        }
        double totalGainOrLoss = totalHolding - totalInvestment;
        double totalGainOrLossPercentage = totalGainOrLoss / totalInvestment * 100;

        return Portfolio.builder()
                .user(user)
                .holdings(holdings)
                .totalInvestment(totalInvestment)
                .totalHolding(totalHolding)
                .totalGainOrLoss(totalGainOrLoss)
                .totalGainOrLossPercentage(totalGainOrLossPercentage)
                .build();
    }

    private Holding calculateHolding(Stock stock, List<Trade> trades) {
        trades.sort(Comparator.comparing(Trade::getCreatedAt));
        double avgBuyPrice = 0;
        double totalBuyAmount = 0;
        double currentInvestment = 0;
        int buyQuantity = 0;
        int sellQuantity = 0;
        for (Trade trade : trades) {
            if (trade.getTradeType().equals(TradeType.BUY)) {
                buyQuantity += trade.getQuantity();
                totalBuyAmount += trade.getPrice() * trade.getQuantity();
                currentInvestment += trade.getPrice() * trade.getQuantity();
            }
            else {
                sellQuantity += trade.getQuantity();
                currentInvestment -= trade.getPrice() * trade.getQuantity();
            }
            if (buyQuantity - sellQuantity == 0) {
                buyQuantity = 0;
                sellQuantity = 0;
                totalBuyAmount = 0;
            }
        }
        int quantity = buyQuantity - sellQuantity;
        if (quantity == 0) {
            return null;
        }
        avgBuyPrice = totalBuyAmount / buyQuantity;
        double currentHolding = stock.getClosingPrice() * quantity;
        double gainOrLoss = currentHolding - currentInvestment;
        double gainOrLossPercentage = gainOrLoss / currentInvestment * 100;

        return Holding.builder()
                .isin(stock.getIsin())
                .stockName(stock.getStockName())
                .exchange(stock.getExchange())
                .quantity(quantity)
                .avgBuyPrice(avgBuyPrice)
                .currentPrice(stock.getClosingPrice())
                .currentInvestment(currentInvestment)
                .currentHolding(currentHolding)
                .gainOrLoss(gainOrLoss)
                .gainOrLossPercentage(gainOrLossPercentage)
                .build();
    }
}
