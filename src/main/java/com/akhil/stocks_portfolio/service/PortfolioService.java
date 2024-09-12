package com.akhil.stocks_portfolio.service;

import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Portfolio;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

public interface PortfolioService {
    ResponseEntity<Response<Portfolio>> getPortfolioDetails(String userName);
    ResponseEntity<Response<Portfolio>> getPortfolioDetailsByDate(String userName, LocalDate startDate, LocalDate endDate);
}
