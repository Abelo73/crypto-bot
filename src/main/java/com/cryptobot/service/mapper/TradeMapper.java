package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.Trade;
import com.cryptobot.repository.entity.TradeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TradeMapper {
    Trade toDomain(TradeEntity entity);

    TradeEntity toEntity(Trade domain);
}
