package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.CopyRelation;
import com.cryptobot.repository.entity.CopyRelationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

/**
 * Mapper for CopyRelation domain models and JPA entities
 */
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CopyRelationMapper {
    CopyRelation toDomain(CopyRelationEntity entity);

    CopyRelationEntity toEntity(CopyRelation domain);
}
