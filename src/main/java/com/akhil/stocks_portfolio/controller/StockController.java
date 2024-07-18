package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.dto.Exchange;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Stock;
import com.akhil.stocks_portfolio.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@Controller
public class StockController {

    @Autowired
    StockService stockService;

    @PostMapping(value = "/upload_stock_CSV")
    public ResponseEntity<Response<String>> onUploadNSEFile(@RequestBody MultipartFile file, @RequestHeader Exchange exchange) {
        try {
            stockService.uploadStockCSV(file, exchange);
            return Response.success(HttpStatus.OK,"Successfully uploaded");
        } catch (RuntimeException e) {
            return Response.failed(HttpStatus.UNPROCESSABLE_ENTITY, "Failed to process CSV file");
        }
    }

    @GetMapping(value = "/get_stock_data/{isin}")
    public ResponseEntity<Response<Stock>> onStockData(@PathVariable String isin, @RequestHeader Exchange exchange) {
        return stockService.getStockData(isin, exchange);
    }
}
