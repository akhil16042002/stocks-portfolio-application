package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.CreateTradeRequestDto;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Trade;
import org.springframework.http.ResponseEntity;

public interface TradeService {
    ResponseEntity<Response<Trade>> createTrade(CreateTradeRequestDto createTradeRequest);
}
