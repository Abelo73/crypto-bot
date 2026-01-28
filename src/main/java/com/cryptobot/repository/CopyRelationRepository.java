package com.cryptobot.repository;

import com.cryptobot.domain.model.CopyTradingStatus;
import com.cryptobot.repository.entity.CopyRelationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for managing copy-trading relationships
 */
@Repository
public interface CopyRelationRepository extends JpaRepository<CopyRelationEntity, Long> {
    List<CopyRelationEntity> findByLeadUserIdAndStatus(Long leadUserId, CopyTradingStatus status);

    List<CopyRelationEntity> findByFollowerUserId(Long followerUserId);
}
