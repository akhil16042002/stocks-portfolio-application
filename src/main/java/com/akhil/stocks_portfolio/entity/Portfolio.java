package com.akhil.stocks_portfolio.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
public class Portfolio {
    private final User user;
    private final List<Holding> holdings;
    private final double totalInvestment;
    private final double totalHolding;
    private final double totalGainOrLoss;
    private final double totalGainOrLossPercentage;
}
