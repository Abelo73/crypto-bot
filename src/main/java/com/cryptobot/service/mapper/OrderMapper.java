package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.repository.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "symbol", source = "symbol")
    Order toDomain(OrderEntity entity);

    @Mapping(target = "userId", source = "userId")
    OrderEntity toEntity(Order order);
}
