package com.akhil.stocks_portfolio.entity;

import com.akhil.stocks_portfolio.dto.Exchange;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Builder
@Entity
@Table(name = "stocks")
public class Stock {
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

    private final String isin;
    private final String stockName;

    @Enumerated(EnumType.STRING)
    private final Exchange exchange;

    private double closingPrice;

}