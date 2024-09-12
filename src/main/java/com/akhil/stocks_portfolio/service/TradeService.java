package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.enums.Broker;
import com.akhil.stocks_portfolio.dto.CreateTradeRequestDto;
import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Trade;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

public interface TradeService {
    ResponseEntity<Response<Trade>> createTrade(CreateTradeRequestDto createTradeRequest);
    ResponseEntity<Response<String>> uploadKiteFile(MultipartFile file, Broker broker, String userName);
}
