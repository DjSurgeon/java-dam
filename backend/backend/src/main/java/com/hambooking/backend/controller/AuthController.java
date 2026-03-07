package com.hambooking.backend.controller;

import com.hambooking.backend.dto.auth.LoginRequestDTO;
import com.hambooking.backend.dto.auth.LoginResponseDTO;
import com.hambooking.backend.dto.auth.RegisterRequestDTO;
import com.hambooking.backend.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    // Inyección por constructor — igual que en el Service
    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    // ─────────────────────────────────────────
    // POST /api/auth/login
    // ─────────────────────────────────────────
    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response); // HTTP 200
    }

    // ─────────────────────────────────────────
    // POST /api/auth/register
    // ─────────────────────────────────────────
    @PostMapping("/register")
    public ResponseEntity<LoginResponseDTO> register(@Valid @RequestBody RegisterRequestDTO request) {
        LoginResponseDTO response = authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // HTTP 201
    }
}