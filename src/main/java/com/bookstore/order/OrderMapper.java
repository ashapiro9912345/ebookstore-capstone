package com.bookstore.order;

import com.bookstore.order.dto.OrderItemResponse;
import com.bookstore.order.dto.OrderResponse;
import com.bookstore.order.dto.ShippingAddressSnapshot;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;

/**
 * Hand-written mapper from Order/OrderItem entities to response DTOs.
 *
 * The shipping address is built from the order's snapshot columns (DM-03),
 * never from the live Address entity, so historical orders remain accurate.
 */
@Component
public class OrderMapper {

    public OrderItemResponse toItemResponse(OrderItem item) {
        BigDecimal lineTotal = item.getUnitPrice()
                .multiply(BigDecimal.valueOf(item.getQuantity()));

        return new OrderItemResponse(
                item.getBook().getId(),
                item.getBook().getTitle(),
                item.getBook().getAuthor(),
                item.getQuantity(),
                item.getUnitPrice(),
                lineTotal);
    }

    public ShippingAddressSnapshot toAddressSnapshot(Order order) {
        return new ShippingAddressSnapshot(
                order.getSnapRecipient(),
                order.getSnapStreetLine1(),
                order.getSnapStreetLine2(),
                order.getSnapCity(),
                order.getSnapState(),
                order.getSnapPostalCode(),
                order.getSnapCountry());
    }

    public OrderResponse toResponse(Order order) {
        List<OrderItemResponse> items = order.getItems()
                .stream()
                .map(this::toItemResponse)
                .toList();

        return new OrderResponse(
                order.getId(),
                order.getStatus(),
                order.getTotalAmount(),
                order.getPaymentMethod(),
                order.getSimulatedPaymentReference(),
                toAddressSnapshot(order),
                items,
                order.getCreatedAt(),
                order.getUpdatedAt());
    }
}
