package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Portfolio;
import com.akhil.stocks_portfolio.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
public class PortfolioController {

    @Autowired
    PortfolioService portfolioService;

    @GetMapping(value = "/portfolio_details/{userName}")
    public ResponseEntity<Response<Portfolio>> onPortfolioDetails(@PathVariable String userName) {
        return portfolioService.getPortfolioDetails(userName);
    }

    @GetMapping(value = "/portfolio_details_by_date/{userName}")
    public ResponseEntity<Response<Portfolio>> onPortfolioDetails(
            @PathVariable String userName,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        return portfolioService.getPortfolioDetailsByDate(userName, startDate, endDate);
    }

}
