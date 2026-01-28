package com.cryptobot.service.mapper;

import com.cryptobot.domain.model.Order;
import com.cryptobot.domain.vo.Symbol;
import com.cryptobot.repository.entity.OrderEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderMapper {

    @Mapping(target = "symbol", source = "symbol", qualifiedByName = "toSymbol")
    Order toDomain(OrderEntity entity);

    @Mapping(target = "symbol", source = "symbol.value")
    OrderEntity toEntity(Order domain);

    @Named("toSymbol")
    default Symbol toSymbol(String value) {
        return value != null ? new Symbol(value) : null;
    }
}
