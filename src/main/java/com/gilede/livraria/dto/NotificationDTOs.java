package com.gilede.livraria.dto;

public class NotificationDTOs {

    public record NotificationResponse(
            String id,
            String userId,
            String orderId,
            String message,
            String type,
            String status,
            Boolean read,
            String createdAt) {
    }
}
