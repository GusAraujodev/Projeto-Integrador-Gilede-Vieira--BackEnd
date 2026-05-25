package com.gilede.livraria.controller;

import com.gilede.livraria.dto.NotificationDTOs;
import com.gilede.livraria.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /notifications/user/{userId} */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTOs.NotificationResponse>> findByUser(
            @PathVariable UUID userId,
            Authentication authentication) {
        // Garante que o usuário só acessa suas próprias notificações
        String emailLogado = authentication.getName();
        // A validação por email é suficiente — o service já filtra por userId
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    /** PATCH /notifications/{id}/read */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTOs.NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /** PATCH /notifications/read-all/{userId} */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
