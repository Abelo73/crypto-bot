package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.ApiKey;
import com.cryptobot.repository.entity.ApiKeyEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ApiKeyMapper {
    ApiKey toDomain(ApiKeyEntity entity);

    ApiKeyEntity toEntity(ApiKey domain);
}
