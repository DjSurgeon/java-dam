package com.hambooking.backend.controller;

import com.hambooking.backend.dto.notification.NotificationResponseDTO;
import com.hambooking.backend.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // GET /api/notifications  (admin: todas)
    @GetMapping
    public ResponseEntity<List<NotificationResponseDTO>> listAllNotifications() {
        return ResponseEntity.ok(notificationService.listAllNotifications());
    }

    // GET /api/notifications/user/{userId}  (cliente: las suyas)
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<NotificationResponseDTO>> listByUser(
            @PathVariable Long userId) {
        return ResponseEntity.ok(notificationService.listByUser(userId));
    }
}