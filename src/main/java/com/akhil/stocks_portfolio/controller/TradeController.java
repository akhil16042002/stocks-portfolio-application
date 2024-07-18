package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.dto.CreateTradeRequestDto;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.service.TradeService;
import com.akhil.stocks_portfolio.entity.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@Controller
public class TradeController {

    @Autowired
    TradeService tradeService;

    @PostMapping(value = "/createTrade")
    public ResponseEntity<Response<Trade>> onCreateTrade(@RequestBody CreateTradeRequestDto createTradeRequest) {
        return tradeService.createTrade(createTradeRequest);
    }
}
