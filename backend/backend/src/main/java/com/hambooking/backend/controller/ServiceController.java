package com.hambooking.backend.controller;

import com.hambooking.backend.dto.service.ServiceResponseDTO;
import com.hambooking.backend.service.ServiceService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/services")
public class ServiceController {

    private final ServiceService serviceService;

    public ServiceController(ServiceService serviceService) {
        this.serviceService = serviceService;
    }

    // ─────────────────────────────────────────
    // GET /api/services
    // ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ServiceResponseDTO>> listActiveServices() {
        return ResponseEntity.ok(serviceService.listActiveServices());
    }
}