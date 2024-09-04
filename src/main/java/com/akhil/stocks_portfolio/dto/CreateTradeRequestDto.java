package com.akhil.stocks_portfolio.dto;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class CreateTradeRequestDto {
    private final String userName;
    private final String stockName;

    @Enumerated(EnumType.STRING)
    private final Broker broker;

    @Enumerated(EnumType.STRING)
    private final Exchange exchange;

    @Enumerated(EnumType.STRING)
    private final TradeType tradeType;

    private final int quantity;
    private final double price;
    private final LocalDateTime orderDate;
}
