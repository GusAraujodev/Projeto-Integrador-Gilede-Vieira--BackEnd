package com.gilede.livraria.controller;

import com.gilede.livraria.dto.NotificationDTOs;
import com.gilede.livraria.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /** GET /notifications/user/{userId} */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationDTOs.NotificationResponse>> findByUser(
            @PathVariable UUID userId) {
        return ResponseEntity.ok(notificationService.findByUserId(userId));
    }

    /** PATCH /notifications/{id}/read */
    @PatchMapping("/{id}/read")
    public ResponseEntity<NotificationDTOs.NotificationResponse> markAsRead(@PathVariable UUID id) {
        return ResponseEntity.ok(notificationService.markAsRead(id));
    }

    /** PATCH /notifications/read-all/{userId} */
    @PatchMapping("/read-all/{userId}")
    public ResponseEntity<Void> markAllAsRead(@PathVariable UUID userId) {
        notificationService.markAllAsRead(userId);
        return ResponseEntity.noContent().build();
    }
}
