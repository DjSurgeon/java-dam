package com.hambooking.backend.controller;

import com.hambooking.backend.dto.reservation.CreateReservationDTO;
import com.hambooking.backend.dto.reservation.ReservationResponseDTO;
import com.hambooking.backend.dto.reservation.UpdateReservationDTO;
import com.hambooking.backend.service.ReservationService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    public ReservationController(ReservationService reservationService) {
        this.reservationService = reservationService;
    }

    // ─────────────────────────────────────────
    // POST /api/reservations
    // ─────────────────────────────────────────
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationDTO request) {
        ReservationResponseDTO response = reservationService.createReservation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response); // 201
    }

    // ─────────────────────────────────────────
    // PUT /api/reservations/{id}
    // ─────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationDTO request) {
        ReservationResponseDTO response = reservationService.updateReservation(id, request);
        return ResponseEntity.ok(response); // 200
    }

    // ─────────────────────────────────────────
    // PATCH /api/reservations/{id}/cancel
    // ─────────────────────────────────────────
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build(); // 204
    }

    // ─────────────────────────────────────────
    // GET /api/reservations/client/{clientId}
    // ─────────────────────────────────────────
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReservationResponseDTO>> listReservationsByClient(
            @PathVariable Long clientId) {
        List<ReservationResponseDTO> reservations = reservationService
                .listReservationsByClient(clientId);
        return ResponseEntity.ok(reservations); // 200
    }
}