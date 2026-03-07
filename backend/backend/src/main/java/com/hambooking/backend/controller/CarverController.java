package com.hambooking.backend.controller;

import com.hambooking.backend.dto.carver.CarverDTO;
import com.hambooking.backend.service.CarverService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/carvers")
public class CarverController {

    private final CarverService carverService;

    public CarverController(CarverService carverService) {
        this.carverService = carverService;
    }

    // ─────────────────────────────────────────
    // POST /api/carvers
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<CarverDTO> createCarver(@Valid @RequestBody CarverDTO request) {
        CarverDTO response = carverService.createCarver(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // ─────────────────────────────────────────
    // PUT /api/carvers/{id}
    // ─────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<CarverDTO> updateCarver(@PathVariable Long id,
                                                  @Valid @RequestBody CarverDTO request) {
        CarverDTO response = carverService.updateCarver(id, request);
        return ResponseEntity.ok(response); // 200
    }

    // ─────────────────────────────────────────
    // PATCH /api/carvers/{id}/deactivate
    // ─────────────────────────────────────────
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCarver(@PathVariable Long id) {
        carverService.deactivateCarver(id);
        return ResponseEntity.noContent().build(); // 204
    }

    // ─────────────────────────────────────────
    // GET /api/carvers/active
    // ─────────────────────────────────────────
    @GetMapping("/active")
    public ResponseEntity<List<CarverDTO>> listActiveCarvers() {
        List<CarverDTO> carvers = carverService.listActiveCarvers();
        return ResponseEntity.ok(carvers); // 200
    }
}