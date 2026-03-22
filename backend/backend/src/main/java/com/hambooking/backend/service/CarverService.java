package com.hambooking.backend.service;

import com.hambooking.backend.dto.carver.CarverDTO;
import com.hambooking.backend.exception.BusinessRuleException;
import com.hambooking.backend.exception.ResourceNotFoundException;
import com.hambooking.backend.model.entity.Carver;
import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.repository.CarverRepository;
import com.hambooking.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CarverService {

    private final CarverRepository carverRepository;
    private final UserRepository userRepository;

    public CarverService(CarverRepository carverRepository, UserRepository userRepository) {
        this.carverRepository = carverRepository;
        this.userRepository = userRepository;
    }

    // ─────────────────────────────────────────
    // CREAR CORTADOR
    // ─────────────────────────────────────────
    @Transactional
    public CarverDTO createCarver(CarverDTO request) {

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (carverRepository.existsByUser(user))
            throw new BusinessRuleException("Este usuario ya tiene perfil de cortador");

        Carver carver = new Carver();
        carver.setUser(user);
        carver.setSpecialty(request.getSpecialty());
        carver.setExperienceYears(
                request.getExperienceYears() != null ? request.getExperienceYears() : 0);
        carver.setMaxHamsPerDay(request.getMaxHamsPerDay());
        carver.setIsActive(true);

        return toDTO(carverRepository.save(carver));
    }

    // ─────────────────────────────────────────
    // ACTUALIZAR CORTADOR
    // ─────────────────────────────────────────
    @Transactional
    public CarverDTO updateCarver(Long id, CarverDTO request) {

        Carver carver = carverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cortador no encontrado"));

        if (request.getSpecialty() != null)
            carver.setSpecialty(request.getSpecialty());
        if (request.getExperienceYears() != null)
            carver.setExperienceYears(request.getExperienceYears());
        if (request.getMaxHamsPerDay() != null)
            carver.setMaxHamsPerDay(request.getMaxHamsPerDay());

        return toDTO(carverRepository.save(carver));
    }

    // ─────────────────────────────────────────
    // ACTIVAR / DESACTIVAR CORTADOR  ← NUEVO
    // ─────────────────────────────────────────
    @Transactional
    public void setCarverActive(Long id, boolean active) {

        Carver carver = carverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cortador no encontrado"));

        // Si se va a desactivar, verificar que no sea el último activo
        if (!active) {
            long activosActuales = carverRepository.findByIsActiveTrue().stream()
                    .filter(c -> !c.getId().equals(id))
                    .count();
            if (activosActuales < 1)
                throw new BusinessRuleException(
                        "No se puede desactivar el último cortador activo");
        }

        carver.setIsActive(active);
        carverRepository.save(carver);
    }

    // ─────────────────────────────────────────
    // DESACTIVAR CORTADOR (mantener por compatibilidad)
    // ─────────────────────────────────────────
    @Transactional
    public void deactivateCarver(Long id) {
        setCarverActive(id, false);
    }

    // ─────────────────────────────────────────
    // LISTAR TODOS LOS CORTADORES
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CarverDTO> listAllCarvers() {
        return carverRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // LISTAR CORTADORES ACTIVOS
    // ─────────────────────────────────────────
    @Transactional(readOnly = true)
    public List<CarverDTO> listActiveCarvers() {
        return carverRepository.findByIsActiveTrue().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────
    // MÉTODO PRIVADO — Convertir entidad a DTO
    // ─────────────────────────────────────────
    private CarverDTO toDTO(Carver carver) {
        return new CarverDTO(
                carver.getId(),
                carver.getUser().getId(),
                carver.getSpecialty(),
                carver.getExperienceYears(),
                carver.getMaxHamsPerDay(),
                carver.getIsActive()
        );
    }
}