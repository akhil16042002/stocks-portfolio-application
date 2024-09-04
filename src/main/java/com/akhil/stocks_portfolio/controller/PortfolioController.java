package com.akhil.stocks_portfolio.controller;

import com.akhil.stocks_portfolio.dto.Response;
import com.akhil.stocks_portfolio.entity.Portfolio;
import com.akhil.stocks_portfolio.service.PortfolioService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PortfolioController {

    @Autowired
    PortfolioService portfolioService;

    @GetMapping(value = "/portfolio_details/{userName}")
    public ResponseEntity<Response<Portfolio>> onPortfolioDetails(@PathVariable String userName) {
        return portfolioService.getPortfolioDetails(userName);
    }
}
