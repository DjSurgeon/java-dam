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

    // GET /api/carvers  (admin: todos)
    @GetMapping
    public ResponseEntity<List<CarverDTO>> listAllCarvers() {
        return ResponseEntity.ok(carverService.listAllCarvers());
    }

    // GET /api/carvers/active
    @GetMapping("/active")
    public ResponseEntity<List<CarverDTO>> listActiveCarvers() {
        return ResponseEntity.ok(carverService.listActiveCarvers());
    }

    // POST /api/carvers
    @PostMapping
    public ResponseEntity<CarverDTO> createCarver(@Valid @RequestBody CarverDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carverService.createCarver(request));
    }

    // PUT /api/carvers/{id}
    @PutMapping("/{id}")
    public ResponseEntity<CarverDTO> updateCarver(@PathVariable Long id,
                                                  @Valid @RequestBody CarverDTO request) {
        return ResponseEntity.ok(carverService.updateCarver(id, request));
    }

    // PATCH /api/carvers/{id}/activate  ← NUEVO
    @PatchMapping("/{id}/activate")
    public ResponseEntity<Void> activateCarver(@PathVariable Long id) {
        carverService.setCarverActive(id, true);
        return ResponseEntity.noContent().build();
    }

    // PATCH /api/carvers/{id}/deactivate
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<Void> deactivateCarver(@PathVariable Long id) {
        carverService.deactivateCarver(id);
        return ResponseEntity.noContent().build();
    }
}