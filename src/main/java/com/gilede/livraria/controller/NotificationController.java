package com.gilede.livraria.controller;

import com.gilede.livraria.dto.NotificationDTOs;
import com.gilede.livraria.repository.UserRepository;
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
    private final UserRepository userRepository;

    /** GET /notifications/user/{userId} */
    @PreAuthorize("isAuthenticated()")
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> findByUser(
        @PathVariable UUID userId,
        Authentication authentication) {

        String emailLogado = authentication.getName();

    // Verifica se o ID passado na URL pertence ao e-mail contido no token
    boolean pertenceAoUsuario = userRepository.findByEmail(emailLogado)
        .map(u -> u.getId().equals(userId))
        .orElse(false);

    // Permite o acesso irrestrito se for administrador
    boolean isAdmin = authentication.getAuthorities().stream()
        .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

    if (!pertenceAoUsuario && !isAdmin) {
        return ResponseEntity.status(org.springframework.http.HttpStatus.FORBIDDEN)
            .body("Acesso negado: você não possui permissão para ler estas notificações.");
    }

    return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    /** PATCH /notifications/{id}/read */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTOs.NotificationResponse> markAsRead(
            Authentication authentication,
            @PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(authentication, id));
    }

    /** PATCH /notifications/read-all/{userId} */
    @PreAuthorize("isAuthenticated()")
    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(
            Authentication authentication,
            @PathVariable UUID userId) {
        notificationService.markAllAsRead(authentication, userId);
        return ResponseEntity.noContent().build();
    }
}
