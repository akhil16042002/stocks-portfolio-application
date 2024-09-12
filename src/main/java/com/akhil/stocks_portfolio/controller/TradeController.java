package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.enums.Broker;
import com.akhil.stocks_portfolio.dto.CreateTradeRequestDto;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.service.TradeService;
import com.akhil.stocks_portfolio.entity.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
public class TradeController {

    @Autowired
    TradeService tradeService;

    @PostMapping(value = "/create_trade")
    public ResponseEntity<Response<Trade>> onCreateTrade(@RequestBody CreateTradeRequestDto createTradeRequest) {
        return tradeService.createTrade(createTradeRequest);
    }

    @PostMapping(value = "/upload_kite_csv", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    public ResponseEntity<Response<String>> onUploadKiteFile(@RequestParam("file") MultipartFile file, @RequestHeader Broker broker, @RequestHeader String userName) {
        return tradeService.uploadKiteFile(file, broker, userName);
    }
}
