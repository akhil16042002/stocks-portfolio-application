package com.akhil.stocks_portfolio.repository;

import com.akhil.stocks_portfolio.dto.Exchange;
import com.akhil.stocks_portfolio.entity.Stock;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StockRepository extends JpaRepository<Stock, Long> {
    Optional<Stock> findByIsinAndExchange(String isin, Exchange exchange);
    Optional<Stock> findByStockNameAndExchange(String stockName, Exchange exchange);
}
