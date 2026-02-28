package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.Carver;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CarverRepository extends JpaRepository<Carver, Long> {

    Optional<Carver> findByUser_Id(Long userId);

    List<Carver> findByIsActiveTrue();

    List<Carver> findBySpecialtyContainingIgnoreCase(String specialty);
}