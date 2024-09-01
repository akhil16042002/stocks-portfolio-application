package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.CreateTradeRequestDto;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.dto.TradeType;
import com.akhil.stocks_portfolio.entity.Stock;
import com.akhil.stocks_portfolio.entity.Trade;
import com.akhil.stocks_portfolio.entity.User;
import com.akhil.stocks_portfolio.repository.StockRepository;
import com.akhil.stocks_portfolio.repository.TradeRepository;
import com.akhil.stocks_portfolio.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class TradeServiceImpl implements TradeService{

    @Autowired
    TradeRepository tradeRepository;

    @Autowired
    StockRepository stockRepository;

    @Autowired
    UserRepository userRepository;

    @Override
    public ResponseEntity<Response<Trade>> createTrade(CreateTradeRequestDto createTradeRequest) {
        try {
            log.info("Creating new trade for {}", createTradeRequest.getUserName());
            
            Optional<User> optionalUser = userRepository.findByUserName(createTradeRequest.getUserName());
            if (optionalUser.isEmpty()) {
                return Response.failed(HttpStatus.BAD_REQUEST, "User name " + createTradeRequest.getUserName() + " does not exist");
            }
            User user = optionalUser.get();
            Optional<Stock> optionalStock = stockRepository.findByStockNameAndExchange(createTradeRequest.getStockName(), createTradeRequest.getExchange());
            if (optionalStock.isEmpty()) {
                return Response.failed(HttpStatus.BAD_REQUEST, "Stock " + createTradeRequest.getStockName() + " is not present in " + createTradeRequest.getExchange());
            }
            Stock stock = optionalStock.get();
            Trade trade;
            if (createTradeRequest.getTradeType().equals(TradeType.BUY)) {
                trade = placeTrade(createTradeRequest, user, stock);
            }
            else if (createTradeRequest.getTradeType().equals(TradeType.SELL)) {
                if (canPlaceSellTrade(createTradeRequest, user, stock)) {
                    trade = placeTrade(createTradeRequest, user, stock);
                }
                else {
                    return Response.failed(HttpStatus.BAD_REQUEST, "User " + user.getUserName() + " has insufficient quantity of " + stock.getStockName() + " - " + stock.getExchange() + " shares to sell");
                }
            }
            else {
                return Response.failed(HttpStatus.BAD_REQUEST, "Invalid trade type: " + createTradeRequest.getTradeType());
            }
            return Response.success(HttpStatus.OK, trade);
        } catch (Exception e) {
            log.error("Error while creating trade: {}", createTradeRequest, e);
            throw new RuntimeException("Error occurred while creating trade: " + createTradeRequest, e);
        }
    }

    private Trade placeTrade(CreateTradeRequestDto createTradeRequest, User user, Stock stock) {
        Trade trade = Trade.builder()
                .userName(user.getUserName())
                .isin(stock.getIsin())
                .stockName(stock.getStockName())
                .broker(createTradeRequest.getBroker())
                .exchange(stock.getExchange())
                .tradeType(createTradeRequest.getTradeType())
                .quantity(createTradeRequest.getQuantity())
                .price(createTradeRequest.getPrice())
                .build();
        return tradeRepository.save(trade);
    }

    private boolean canPlaceSellTrade(CreateTradeRequestDto createTradeRequest, User user, Stock stock) {
        List<Trade> trades = tradeRepository.findAllByUserNameAndStockNameAndExchange(user.getUserName(), stock.getStockName(), stock.getExchange());
        int buyQuantity = 0;
        int sellQuantity = 0;
        for (Trade trade : trades) {
            if (trade.getTradeType().equals(TradeType.BUY)) {
                buyQuantity += trade.getQuantity();
            }
            else {
                sellQuantity += trade.getQuantity();
            }
        }
        return buyQuantity >= sellQuantity + createTradeRequest.getQuantity();
    }

}