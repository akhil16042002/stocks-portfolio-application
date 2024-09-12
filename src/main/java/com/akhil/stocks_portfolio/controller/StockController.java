package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.enums.Exchange;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Stock;
import com.akhil.stocks_portfolio.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
public class StockController {

    @Autowired
    StockService stockService;

    @PostMapping(value = "/upload_stock_csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Response<String>> onUploadNSEFile(@RequestParam("file") MultipartFile file, @RequestHeader Exchange exchange) {
        try {
            stockService.uploadStockCSV(file, exchange);
            return Response.success(HttpStatus.OK,"Successfully uploaded");
        } catch (RuntimeException e) {
            return Response.failed(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to process CSV file");
        }
    }

    @GetMapping(value = "/stock_data_by_isin")
    public ResponseEntity<Response<Stock>> onStockData(@RequestHeader String isin, @RequestHeader Exchange exchange) {
        return stockService.getStockData(isin, exchange);
    }

    @GetMapping(value = "/stock_data_by_name")
    public ResponseEntity<Response<List<Stock>>> onStockData(@RequestHeader String stockName) {
        return stockService.getStockData(stockName);
    }
}
