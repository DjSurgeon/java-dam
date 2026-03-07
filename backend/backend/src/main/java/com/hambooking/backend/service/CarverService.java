package com.hambooking.backend.service;

import com.hambooking.backend.dto.carver.CarverDTO;
import com.hambooking.backend.exception.InvalidCredentialsException;
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

        // 1. Verificar que el usuario existe
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (carverRepository.existsByUser(user)) {
            throw new BusinessRuleException("Este usuario ya tiene perfil de cortador");
        }

        // 3. Construir el perfil de cortador
        Carver carver = new Carver();
        carver.setUser(user);
        carver.setSpecialty(request.getSpecialty());
        carver.setExperienceYears(request.getExperienceYears() != null ? request.getExperienceYears() : 0);
        carver.setMaxHamsPerDay(request.getMaxHamsPerDay());
        carver.setIsActive(true);

        // 4. Guardar en BD
        Carver saved = carverRepository.save(carver);

        return toDTO(saved);
    }

    // ─────────────────────────────────────────
    // ACTUALIZAR CORTADOR
    // ─────────────────────────────────────────
    @Transactional
    public CarverDTO updateCarver(Long id, CarverDTO request) {

        // 1. Verificar que el cortador existe
        Carver carver = carverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cortador no encontrado"));

        // 2. Actualizar solo los campos que vienen en la petición
        if (request.getSpecialty() != null) {
            carver.setSpecialty(request.getSpecialty());
        }
        if (request.getExperienceYears() != null) {
            carver.setExperienceYears(request.getExperienceYears());
        }
        if (request.getMaxHamsPerDay() != null) {
            carver.setMaxHamsPerDay(request.getMaxHamsPerDay());
        }

        // 3. Guardar cambios
        Carver updated = carverRepository.save(carver);

        return toDTO(updated);
    }

    // ─────────────────────────────────────────
    // DESACTIVAR CORTADOR
    // ─────────────────────────────────────────
    @Transactional
    public void deactivateCarver(Long id) {

        // 1. Verificar que el cortador existe
        Carver carver = carverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cortador no encontrado"));


        // 2. Regla de negocio crítica — no puede quedar cero cortadores activos
        List<Carver> activosActuales = carverRepository.findByIsActiveTrue();
        if (activosActuales.size() <= 1) {
            throw new BusinessRuleException("No se puede desactivar el último cortador activo");
        }

        // 3. Soft delete — nunca borramos, solo desactivamos
        carver.setIsActive(false);
        carverRepository.save(carver);
    }

    // ─────────────────────────────────────────
    // LISTAR CORTADORES ACTIVOS
    // ─────────────────────────────────────────
    public List<CarverDTO> listActiveCarvers() {
        return carverRepository.findByIsActiveTrue()
                .stream()
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