package com.akhil.stocks_portfolio.dto;

import com.akhil.stocks_portfolio.enums.Exchange;
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
public class StockNameExchangeDto {
    private final String stockName;

    @Enumerated(EnumType.STRING)
    private final Exchange exchange;
}
