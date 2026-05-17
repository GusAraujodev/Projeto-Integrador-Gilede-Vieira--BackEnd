package com.gilede.livraria.service;

import com.gilede.livraria.dto.NotificationDTOs;
import com.gilede.livraria.repository.NotificationRepository;
import com.gilede.livraria.model.Notification;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional(readOnly = true)
    public List<NotificationDTOs.NotificationResponse> findByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationDTOs.NotificationResponse markAsRead(UUID id) {
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada: " + id));
        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void markAllAsRead(UUID userId) {
        notificationRepository.markAllAsReadByUserId(userId);
    }

    private NotificationDTOs.NotificationResponse toResponse(Notification n) {
        return new NotificationDTOs.NotificationResponse(
                n.getId().toString(),
                n.getUserId().toString(),
                n.getOrderId().toString(),
                n.getMessage(),
                n.getType(),
                n.getStatus().name().toLowerCase(),
                n.getRead(),
                n.getCreatedAt().toString());
    }
}
