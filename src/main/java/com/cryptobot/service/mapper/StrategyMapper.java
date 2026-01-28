package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.Strategy;
import com.cryptobot.repository.entity.StrategyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for Strategy domain models and JPA entities
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StrategyMapper {
    Strategy toDomain(StrategyEntity entity);

    StrategyEntity toEntity(Strategy domain);
}
