package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.enums.Exchange;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Stock;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface StockService {
    void uploadStockCSV(MultipartFile file, Exchange exchange);
    ResponseEntity<Response<Stock>> getStockData(String isin, Exchange exchange);
    ResponseEntity<Response<List<Stock>>> getStockData(String stockName);
}
