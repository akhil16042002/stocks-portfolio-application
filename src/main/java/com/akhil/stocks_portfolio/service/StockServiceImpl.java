package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.enums.Exchange;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Stock;
import com.akhil.stocks_portfolio.repository.StockRepository;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Service
@Slf4j
public class StockServiceImpl implements StockService {

    @Autowired
    StockRepository stockRepository;

    @Transactional
    @Override
    public void uploadStockCSV(MultipartFile file, Exchange exchange) {
        try {
            CSVParser parser = CSVParser.parse(file.getInputStream(), StandardCharsets.UTF_8, CSVFormat.DEFAULT.withFirstRecordAsHeader());
            List<CSVRecord> records = parser.getRecords();
            for (var record : records) {
                try {
                    String isin = record.get("ISIN");
                    String stockName = record.get("FinInstrmNm").trim();
                    double closingPrice = Double.parseDouble(record.get(exchange == Exchange.NSE ? "ClsPric" : "LastPric"));

                    log.info("Saving record: ISIN: {}, Name: {}", isin, stockName);

                    Optional<Stock> existingStock = stockRepository.findByIsinAndExchange(isin, exchange);
                    Stock stock;
                    if (existingStock.isPresent()) {
                        stock = existingStock.get();
                        stock.setClosingPrice(closingPrice);
                    } else {
                        stock = Stock.builder()
                                .isin(isin)
                                .stockName(stockName)
                                .exchange(exchange)
                                .closingPrice(closingPrice)
                                .build();
                    }
                    stockRepository.save(stock);
                    log.info("Successfully saved record: ISIN: {}, Name: {}", isin, stockName);
                } catch (Exception e) {
                    log.error("Error while saving record: ISIN: {}, Name: {}", record.get(exchange == Exchange.NSE ? "ISIN" : "ISIN_CODE"), record.get(exchange == Exchange.NSE ? "TckrSymb" : "SC_NAME").trim(), e);
                }
            }
        } catch (IOException e) {
            log.error("Error while parsing CSV file", e);
            throw new RuntimeException("Failed to store CSV data: " + e.getMessage());
        }

    }

    @Override
    public ResponseEntity<Response<Stock>> getStockData(String isin, Exchange exchange) {
        log.info("Fetching stock data for ISIN: {} and Exchange: {}", isin, exchange);
        Optional<Stock> optionalStock = stockRepository.findByIsinAndExchange(isin, exchange);
        if (optionalStock.isPresent()) {
            Stock stock = optionalStock.get();
            log.info("Successfully fetched stock data for ISIN: {} and Exchange: {}", isin, exchange);
            return Response.success(HttpStatus.OK, stock);
        }
        return Response.failed(HttpStatus.BAD_REQUEST, "Stock not found for ISIN: " + isin + " and Exchange: " + exchange);
    }

    @Override
    public ResponseEntity<Response<List<Stock>>> getStockData(String stockName) {
        log.info("Fetching stock data for : {}", stockName);
        List<Stock> stockList = stockRepository.findAllStocksByName(stockName);
        log.info("Successfully fetched stock data for : {}", stockName);
        return Response.success(HttpStatus.OK, stockList);
    }

}