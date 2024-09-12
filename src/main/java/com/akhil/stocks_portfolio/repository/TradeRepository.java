package com.akhil.stocks_portfolio.repository;

import com.akhil.stocks_portfolio.dto.Exchange;
import com.akhil.stocks_portfolio.entity.Trade;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TradeRepository extends JpaRepository<Trade, Long> {
    List<Trade> findAllByUserName(String userName);
    List<Trade> findAllByUserNameAndStockName(String userName, String stockName);
    List<Trade> findAllByUserNameAndIsin(String userName, String isin);

    @Query("SELECT t FROM Trade t WHERE t.userName = :userName AND t.orderDate BETWEEN :startDate AND :endDate")
    List<Trade> findAllTradesByUsernameAndDateRange(@Param("userName") String userName, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
