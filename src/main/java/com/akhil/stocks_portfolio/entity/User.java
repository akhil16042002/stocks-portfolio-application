package com.akhil.stocks_portfolio.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor(force = true)
@Entity
@Table(name = "users", uniqueConstraints = {@UniqueConstraint(columnNames = "userName")})
public class User {
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

    @Column(unique = true)
    private final String userName;
}