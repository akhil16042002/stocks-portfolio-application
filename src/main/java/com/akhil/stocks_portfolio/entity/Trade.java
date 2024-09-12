package com.akhil.stocks_portfolio.entity;

import com.akhil.stocks_portfolio.enums.Broker;
import com.akhil.stocks_portfolio.enums.Exchange;
import com.akhil.stocks_portfolio.enums.TradeType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Entity
@Table(name = "trades")
public class Trade {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private final long id;

    @Version
    private final int version;

    @Column(name = "created_at", updatable = false)
    @CreationTimestamp
    private final LocalDateTime createdAt;

    @Column(name = "updated_at")
    @UpdateTimestamp
    private final LocalDateTime updatedAt;

    private final LocalDateTime orderDate;
    private final String userName;
    private final String isin;
    private final String stockName;

    @Enumerated(EnumType.STRING)
    private final Broker broker;

    @Enumerated(EnumType.STRING)
    private final Exchange exchange;

    @Enumerated(EnumType.STRING)
    private final TradeType tradeType;

    private final int quantity;
    private final double price;

}
