package com.gilede.livraria.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;

public class OrderDTOs {

    public record OrderResponse(
            String id,
            String userId,
            List<CartItemResponse> items,
            String customerName,
            String customerEmail,
            String customerPhone,
            AddressResponse address,
            String paymentMethod,
            BigDecimal total,
            BigDecimal discount,
            String couponCode,
            String status,
            String createdAt,
            String updatedAt) {
    }

    public record CartItemResponse(
            BookDTOs.BookResponse book,
            Integer quantity) {
    }

    public record AddressResponse(
            String street,
            String number,
            String complement,
            String neighborhood,
            String city,
            String state,
            String zipCode) {
    }

    /** POST /orders */
    public record CreateOrderRequest(
            String userId, // opcional (guest checkout)
            @NotEmpty List<@Valid CartItemRequest> items,
            @NotBlank String customerName,
            @NotBlank @Email String customerEmail,
            @NotBlank String customerPhone,
            @NotNull @Valid AddressRequest address,
            @NotBlank String paymentMethod,
            BigDecimal shippingCost,
            BigDecimal discount,
            String couponCode) {
    }

    public record CartItemRequest(
            @NotBlank String bookId,
            @NotNull Integer quantity) {
    }

    public record AddressRequest(
            @NotBlank String street,
            @NotBlank String number,
            String complement,
            @NotBlank String neighborhood,
            @NotBlank String city,
            @NotBlank String state,
            @NotBlank String zipCode) {
    }

    /** PATCH /orders/:id/status */
    public record UpdateStatusRequest(
            @NotBlank String status) {
    }
}
