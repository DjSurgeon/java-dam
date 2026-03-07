package com.hambooking.backend.service;

import com.hambooking.backend.dto.auth.LoginRequestDTO;
import com.hambooking.backend.dto.auth.LoginResponseDTO;
import com.hambooking.backend.dto.auth.RegisterRequestDTO;
import com.hambooking.backend.exception.InvalidCredentialsException;
import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.model.enums.Role;
import com.hambooking.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    // Inyección por constructor — la forma correcta en Spring
    public AuthService(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ─────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────
    public LoginResponseDTO login(LoginRequestDTO request) {

        // 1. Buscar usuario por email
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new InvalidCredentialsException("Email o contraseña incorrectos"));

        // 2. Verificar contraseña contra el hash en BD
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Email o contraseña incorrectos");
        }

        // 3. Verificar que el usuario está activo
        if (!user.getIsActive()) {
            throw new InvalidCredentialsException("Esta cuenta está desactivada");
        }

        // 4. Construir y devolver respuesta
        return new LoginResponseDTO(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole()
        );
    }

    // ─────────────────────────────────────────
    // REGISTRO
    // ─────────────────────────────────────────
    @Transactional
    public LoginResponseDTO register(RegisterRequestDTO request) {

        // 1. Verificar que el email no está en uso
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new InvalidCredentialsException("Este email ya está registrado");
        }

        // 2. Construir el nuevo usuario
        User newUser = new User();
        newUser.setFirstName(request.getFirstName());
        newUser.setLastName(request.getLastName());
        newUser.setEmail(request.getEmail());
        newUser.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        newUser.setPhone(request.getPhone());
        newUser.setRole(Role.CLIENT);
        newUser.setIsActive(true);

        // 3. Guardar en BD
        User savedUser = userRepository.save(newUser);

        // 4. Devolver respuesta como si fuera un login
        return new LoginResponseDTO(
                savedUser.getId(),
                savedUser.getFirstName(),
                savedUser.getLastName(),
                savedUser.getEmail(),
                savedUser.getRole()
        );
    }
}