package com.cryptobot.repository.entity;

import com.cryptobot.domain.model.CopyTradingStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Entity for persisting copy-trading relationships in PostgreSQL
 */
@Entity
@Table(name = "copy_relations", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "lead_user_id", "follower_user_id" })
})
@Getter
@Setter
public class CopyRelationEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "lead_user_id", nullable = false)
    private Long leadUserId;

    @Column(name = "follower_user_id", nullable = false)
    private Long followerUserId;

    @Column(name = "scale_factor", nullable = false, precision = 10, scale = 4)
    private BigDecimal scaleFactor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CopyTradingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
        if (scaleFactor == null)
            scaleFactor = BigDecimal.ONE;
        if (status == null)
            status = CopyTradingStatus.ACTIVE;
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
