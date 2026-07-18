package dev.pawan.rupixo.payment.mapper;

import dev.pawan.rupixo.payment.dto.response.OrderResponse;
import dev.pawan.rupixo.payment.entity.OrderRecord;
import org.mapstruct.Mapper;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface OrderMapper {

    OrderResponse toResponse(OrderRecord orderRecord);
}
