package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.*;
import com.akhil.stocks_portfolio.entity.Stock;
import com.akhil.stocks_portfolio.entity.Trade;
import com.akhil.stocks_portfolio.entity.User;
import com.akhil.stocks_portfolio.repository.StockRepository;
import com.akhil.stocks_portfolio.repository.TradeRepository;
import com.akhil.stocks_portfolio.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
                trade = placeTrade(user, stock, createTradeRequest);
            }
            else if (createTradeRequest.getTradeType().equals(TradeType.SELL)) {
                if (canPlaceSellTrade(user, stock, createTradeRequest)) {
                    trade = placeTrade(user, stock, createTradeRequest);
                }
                else {
                    return Response.failed(HttpStatus.BAD_REQUEST, "User " + user.getUserName() + " has insufficient quantity of " + stock.getStockName() + " - " + stock.getExchange() + " shares to sell");
                }
            }
            else {
                return Response.failed(HttpStatus.BAD_REQUEST, "Invalid trade type: " + createTradeRequest.getTradeType());
            }
            log.info("Successfully created trade: {}", createTradeRequest);
            return Response.success(HttpStatus.OK, trade);
        } catch (Exception e) {
            log.error("Error while creating trade: {}", createTradeRequest, e);
            throw new RuntimeException("Error occurred while creating trade: " + createTradeRequest, e);
        }
    }

    @Override
    public ResponseEntity<Response<String>> uploadKiteFile(MultipartFile file, Broker broker, String userName) {
        try {
            log.info("Uploading new trades for {}", userName);
            if (userRepository.findByUserName(userName).isEmpty()) {
                return Response.failed(HttpStatus.BAD_REQUEST, "User name " + userName + " does not exist");
            }
            CSVParser parser = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<CSVRecord> records = parser.getRecords();
            List<Trade> tradeList = new ArrayList<>();
            for (var record : records) {
                try {
                    String isin = record.get("isin");
                    Exchange exchange = Exchange.valueOf(record.get("exchange"));
                    TradeType tradeType = Objects.equals(record.get("trade_type"), "buy") ? TradeType.BUY : TradeType.SELL;
                    int quantity = (int) Double.parseDouble(record.get("quantity"));
                    double price = Double.parseDouble(record.get("price"));
                    String orderDateString = record.get("order_execution_time");
                    LocalDateTime orderDate = LocalDateTime.parse(orderDateString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);

                    Optional<Stock> optionalStock = stockRepository.findByIsinAndExchange(isin, exchange);
                    if (optionalStock.isEmpty()) {
                        return Response.failed(HttpStatus.BAD_REQUEST, "Stock with ISIN" + isin + " is not present in " + exchange);
                    }
                    Stock stock = optionalStock.get();
                    tradeList.add(storeTrade(userName, isin, stock.getStockName(), broker, exchange, tradeType, quantity, price, orderDate));
                    log.info("stored trade for user: {} stock name: {} quantity: {} price: {} broker: {} exchange: {}", userName, stock.getStockName(), quantity, price, broker, exchange);
                } catch (Exception e) {
                    log.error("Error while creating trade: ", e);
                    throw new RuntimeException("Error occurred while creating trade: ", e);
                }
            }
            tradeRepository.saveAll(tradeList);
            return Response.success(HttpStatus.OK, "Successfully created all trades for " + userName);
        } catch (IOException e) {
            log.error("Error while parsing CSV file", e);
            throw new RuntimeException("Failed to store CSV data: " + e.getMessage());
        }
    }

    private Trade storeTrade(String userName, String isin, String stockName, Broker broker, Exchange exchange, TradeType tradeType, int quantity, double price, LocalDateTime orderDate) {
        return Trade.builder()
                .userName(userName)
                .isin(isin)
                .stockName(stockName)
                .broker(broker)
                .exchange(exchange)
                .tradeType(tradeType)
                .quantity(quantity)
                .price(price)
                .orderDate(orderDate)
                .build();
    }

    private Trade placeTrade(User user, Stock stock, CreateTradeRequestDto createTradeRequest) {
        Trade trade = Trade.builder()
                .userName(user.getUserName())
                .isin(stock.getIsin())
                .stockName(stock.getStockName())
                .broker(createTradeRequest.getBroker())
                .exchange(stock.getExchange())
                .tradeType(createTradeRequest.getTradeType())
                .quantity(createTradeRequest.getQuantity())
                .price(createTradeRequest.getPrice())
                .orderDate(createTradeRequest.getOrderDate())
                .build();
        return tradeRepository.save(trade);
    }

    private boolean canPlaceSellTrade(User user, Stock stock, CreateTradeRequestDto createTradeRequest) {
        List<Trade> trades = tradeRepository.findAllByUserNameAndIsin(user.getUserName(), stock.getIsin());
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