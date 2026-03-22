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

    // GET /api/reservations  (admin: todas)
    @GetMapping
    public ResponseEntity<List<ReservationResponseDTO>> listAllReservations() {
        return ResponseEntity.ok(reservationService.listAllReservations());
    }

    // GET /api/reservations/client/{clientId}
    @GetMapping("/client/{clientId}")
    public ResponseEntity<List<ReservationResponseDTO>> listReservationsByClient(
            @PathVariable Long clientId) {
        return ResponseEntity.ok(reservationService.listReservationsByClient(clientId));
    }

    // POST /api/reservations
    @PostMapping
    public ResponseEntity<ReservationResponseDTO> createReservation(
            @Valid @RequestBody CreateReservationDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(reservationService.createReservation(request));
    }

    // PUT /api/reservations/{id}
    @PutMapping("/{id}")
    public ResponseEntity<ReservationResponseDTO> updateReservation(
            @PathVariable Long id,
            @Valid @RequestBody UpdateReservationDTO request) {
        return ResponseEntity.ok(reservationService.updateReservation(id, request));
    }

    // PATCH /api/reservations/{id}/confirm  ← NUEVO
    @PatchMapping("/{id}/confirm")
    public ResponseEntity<ReservationResponseDTO> confirmReservation(@PathVariable Long id) {
        return ResponseEntity.ok(reservationService.confirmReservation(id));
    }

    // PATCH /api/reservations/{id}/cancel
    @PatchMapping("/{id}/cancel")
    public ResponseEntity<Void> cancelReservation(@PathVariable Long id) {
        reservationService.cancelReservation(id);
        return ResponseEntity.noContent().build();
    }
}