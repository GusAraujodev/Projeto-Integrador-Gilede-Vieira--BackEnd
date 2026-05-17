package com.gilede.livraria.mapper;

import com.gilede.livraria.dto.OrderDTOs;
import com.gilede.livraria.model.Order;
import com.gilede.livraria.model.OrderItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class OrderMapper {

        private final BookMapper bookMapper;

        public OrderDTOs.OrderResponse toResponse(Order order) {
                List<OrderDTOs.CartItemResponse> items = order.getItems() == null
                                ? List.of()
                                : order.getItems().stream().map(this::toCartItemResponse).toList();

                return new OrderDTOs.OrderResponse(
                                order.getId().toString(),
                                order.getUserId() != null ? order.getUserId().toString() : null,
                                items,
                                order.getCustomerName(),
                                order.getCustomerEmail(),
                                order.getCustomerPhone(),
                                new OrderDTOs.AddressResponse(
                                                order.getAddressStreet(),
                                                order.getAddressNumber(),
                                                order.getAddressComplement(),
                                                order.getAddressNeighborhood(),
                                                order.getAddressCity(),
                                                order.getAddressState(),
                                                order.getAddressZipCode()),
                                order.getPaymentMethod().name().toLowerCase(),
                                order.getTotal(),
                                order.getDiscount(),
                                order.getCouponCode(),
                                order.getStatus().name().toLowerCase(),
                                order.getCreatedAt().toString(),
                                order.getUpdatedAt() != null ? order.getUpdatedAt().toString() : null);
        }

        private OrderDTOs.CartItemResponse toCartItemResponse(OrderItem item) {
                return new OrderDTOs.CartItemResponse(
                                bookMapper.toResponse(item.getBook()),
                                item.getQuantity());
        }
}
