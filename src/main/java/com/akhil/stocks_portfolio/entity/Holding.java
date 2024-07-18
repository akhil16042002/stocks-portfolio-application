package com.akhil.stocks_portfolio.entity;

import com.akhil.stocks_portfolio.dto.Exchange;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class Holding {
    private final String isin;
    private final String stockName;

    @Enumerated(EnumType.STRING)
    private final Exchange exchange;

    private final int quantity;
    private final double avgBuyPrice;
    private final double currentPrice;
    private final double currentInvestment;
    private final double currentHolding;
    private final double gainOrLoss;
    private final double gainOrLossPercentage;
}
