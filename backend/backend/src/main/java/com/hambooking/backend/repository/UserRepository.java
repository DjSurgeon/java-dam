package com.hambooking.backend.repository;

import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.model.enums.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    Optional<User> findByDni(String dni);

    List<User> findByRole(Role role);

    List<User> findByIsActiveTrue();
}