package com.hambooking.backend.controller;

import com.hambooking.backend.service.AvailabilityService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/availability")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    public AvailabilityController(AvailabilityService availabilityService) {
        this.availabilityService = availabilityService;
    }

    // ─────────────────────────────────────────
    // GET /api/availability?carverId=&date=&serviceId=
    // ─────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<LocalTime>> getAvailableSlots(
            @RequestParam Long carverId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam Long serviceId) {

        List<LocalTime> slots = availabilityService.getAvailableSlots(carverId, date, serviceId);
        return ResponseEntity.ok(slots); // 200
    }
}