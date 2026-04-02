package com.hambooking.backend.controller;

import com.hambooking.backend.dto.user.UserResponseDTO;
import com.hambooking.backend.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // GET /api/users
    @GetMapping
    public ResponseEntity<List<UserResponseDTO>> listAllUsers() {
        return ResponseEntity.ok(userService.listAllUsers());
    }

    // GET /api/users/{id}
    @GetMapping("/{id}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    // PATCH /api/users/{id}/activate
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateUser(@PathVariable Long id) {
        userService.setUserActive(id, true);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/users/{id}/deactivate
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateUser(@PathVariable Long id) {
        userService.setUserActive(id, false);
        return ResponseEntity.noContent().build();
    }

    // PUT /api/users/{id}/password  ← NUEVO
    @PutMapping("/{id}/password")
    public ResponseEntity<Void> changePassword(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        userService.changePassword(id,
                body.get("currentPassword"),
                body.get("newPassword"));
        return ResponseEntity.noContent().build();
    }
}