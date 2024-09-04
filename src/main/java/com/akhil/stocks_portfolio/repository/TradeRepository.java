package com.akhil.stocks_portfolio.repository;

import com.akhil.stocks_portfolio.dto.Exchange;
import com.akhil.stocks_portfolio.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findAllByUserName(String userName);
    List<Trade> findAllByUserNameAndStockName(String userName, String stockName);
}
