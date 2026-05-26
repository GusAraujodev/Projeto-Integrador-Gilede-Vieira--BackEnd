package com.gilede.livraria.service;

import com.gilede.livraria.dto.NotificationDTOs;
import com.gilede.livraria.repository.NotificationRepository;
import com.gilede.livraria.repository.UserRepository;
import com.gilede.livraria.model.Notification;
import com.gilede.livraria.model.User;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<NotificationDTOs.NotificationResponse> findByUserId(UUID userId) {
        return notificationRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public NotificationDTOs.NotificationResponse markAsRead(Authentication authentication, UUID id) {
        User authenticatedUser = resolveAuthenticatedUser(authentication);
        Notification n = notificationRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Notificação não encontrada: " + id));

        verifyOwnership(authenticatedUser, n);

        n.setRead(true);
        return toResponse(notificationRepository.save(n));
    }

    @Transactional
    public void markAllAsRead(Authentication authentication, UUID userId) {
        User authenticatedUser = resolveAuthenticatedUser(authentication);
        if (!authenticatedUser.getId().equals(userId)) {
            throw new AccessDeniedException("Acesso negado: você não pode alterar notificações de terceiros.");
        }

        notificationRepository.markAllAsReadByUserId(userId);
    }

    private User resolveAuthenticatedUser(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório para alterar notificações");
        }

        String email = authentication.getName();
        if (email == null || email.isBlank() || "anonymousUser".equalsIgnoreCase(email)) {
            throw new AccessDeniedException("Usuário autenticado é obrigatório para alterar notificações");
        }

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new AccessDeniedException("Usuário autenticado não encontrado"));
    }

    private void verifyOwnership(User authenticatedUser, Notification notification) {
        if (!authenticatedUser.getId().equals(notification.getUserId())) {
            throw new AccessDeniedException("Acesso negado: você não pode alterar notificações de terceiros.");
        }
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
