package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.Balance;
import com.cryptobot.repository.entity.BalanceEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BalanceMapper {
    Balance toDomain(BalanceEntity entity);

    BalanceEntity toEntity(Balance domain);
}
