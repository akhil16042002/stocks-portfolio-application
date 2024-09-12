package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.Exchange;
import com.akhil.stocks_portfolio.dto.Response;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
            return buildPortfolio(user, tradeList);
        } catch (Exception e) {
            log.error("Error while fetching trade details for user: {}", userName, e);
            throw new RuntimeException("Error occurred while fetching trade details for : " + userName, e);
        }
    }

    @Override
    public ResponseEntity<Response<Portfolio>> getPortfolioDetailsByDate(String userName, LocalDate startDate, LocalDate endDate) {
        try {
            log.info("Fetching trades for user : {} start date : {} end date {}", userName, startDate, endDate);
            Optional<User> optionalUser = userRepository.findByUserName(userName);
            if (optionalUser.isEmpty()) {
                return Response.failed(HttpStatus.BAD_REQUEST, "User name " + userName + " does not exist");
            }
            User user = optionalUser.get();
            LocalDateTime startDateTime = startDate.atStartOfDay();
            LocalDateTime endDateTime = endDate.atTime(LocalTime.MAX);
            List<Trade> tradeList = tradeRepository.findAllTradesByUsernameAndDateRange(userName, startDateTime, endDateTime);
            return buildPortfolio(user, tradeList);
        } catch (Exception e) {
            log.error("Error occurred while fetching trades for user : {} start date : {} end date {}", userName, startDate, endDate, e);
            throw new RuntimeException("Error occurred while fetching trades for user : " + userName + " start date : " + startDate + " end date : " + endDate);
        }
    }

    private ResponseEntity<Response<Portfolio>> buildPortfolio(User user, List<Trade> tradeList) {
        Map<String, List<Trade>> stockToTradeMap = new HashMap<>();
        for (Trade trade : tradeList) {
            String key = trade.getIsin();
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
        for (Map.Entry<String, List<Trade>> entry : stockToTradeMap.entrySet()) {
            String isin = entry.getKey();
            List<Trade> trades = entry.getValue();
            List<Stock> stockList = stockRepository.findAllByIsin(isin);
            if (stockList.isEmpty()) {
                return Response.failed(HttpStatus.BAD_REQUEST, "ISIN " + isin + " is not present");
            }
            Holding holding = calculateHolding(stockList, trades);
            if (!ObjectUtils.isEmpty(holding)) {
                holdings.add(holding);
            }
        }
        if (holdings.isEmpty()) {
            log.info("You have no holdings present in your portfolio");
            return Response.failed(HttpStatus.BAD_REQUEST, "You have no holdings present in your portfolio");
        }
        Portfolio portfolio = calculatePortfolio(user, holdings);
        log.info("Successfully fetched portfolio details for user: {} with portfolio: {}", user.getUserName(), portfolio);
        return Response.success(HttpStatus.OK, portfolio);
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

    private Holding calculateHolding(List<Stock> stockList, List<Trade> trades) {
        trades.sort(Comparator.comparing(Trade::getOrderDate));
        double avgBuyPrice = 0;
        double totalBuyAmount = 0;
        double currentInvestment = 0;
        int buyQuantity = 0;
        int sellQuantity = 0;
        int nseQuantity = 0;
        int bseQuantity = 0;
        double closingPrice = 0;
        String stockName = null;
        for (Trade trade : trades) {
            if (trade.getTradeType().equals(TradeType.BUY)) {
                buyQuantity += trade.getQuantity();
                totalBuyAmount += trade.getPrice() * trade.getQuantity();
                currentInvestment += trade.getPrice() * trade.getQuantity();
                if(trade.getExchange().equals(Exchange.NSE)) {
                    nseQuantity += trade.getQuantity();
                }
                else {
                    bseQuantity += trade.getQuantity();
                }
            }
            else {
                sellQuantity += trade.getQuantity();
                currentInvestment -= trade.getPrice() * trade.getQuantity();
                if(trade.getExchange().equals(Exchange.NSE)) {
                    nseQuantity -= trade.getQuantity();
                }
                else {
                    bseQuantity -= trade.getQuantity();
                }
            }
            if (buyQuantity - sellQuantity == 0) {
                buyQuantity = 0;
                sellQuantity = 0;
                totalBuyAmount = 0;
                currentInvestment = 0;
            }
        }
        int quantity = buyQuantity - sellQuantity;
        if (quantity == 0) {
            return null;
        }
        if (stockList.size() == 1) {
            closingPrice = stockList.getFirst().getClosingPrice();
        }
        else {
            for (Stock stock : stockList) {
                if (stock.getExchange().equals(Exchange.NSE) && nseQuantity >= bseQuantity) {
                    closingPrice = stock.getClosingPrice();
                    stockName = stock.getStockName();
                }
                else if (stock.getExchange().equals(Exchange.BSE) && bseQuantity > nseQuantity) {
                    closingPrice = stock.getClosingPrice();
                    stockName = stock.getStockName();
                }
            }
        }
        avgBuyPrice = totalBuyAmount / buyQuantity;
        double currentHolding = closingPrice * quantity;
        double gainOrLoss = currentHolding - currentInvestment;
        double gainOrLossPercentage = gainOrLoss / currentInvestment * 100;

        return Holding.builder()
                .isin(stockList.getFirst().getIsin())
                .stockName(stockName)
                .quantity(quantity)
                .avgBuyPrice(avgBuyPrice)
                .currentPrice(closingPrice)
                .currentInvestment(currentInvestment)
                .currentHolding(currentHolding)
                .gainOrLoss(gainOrLoss)
                .gainOrLossPercentage(gainOrLossPercentage)
                .build();
    }
}
