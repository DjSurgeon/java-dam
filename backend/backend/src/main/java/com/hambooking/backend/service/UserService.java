package com.hambooking.backend.service;

import com.hambooking.backend.dto.user.UserResponseDTO;
import com.hambooking.backend.exception.BusinessRuleException;
import com.hambooking.backend.exception.ResourceNotFoundException;
import com.hambooking.backend.model.entity.User;
import com.hambooking.backend.repository.UserRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                       BCryptPasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> listAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toDTO).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        return userRepository.findById(id).map(this::toDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    @Transactional
    public void setUserActive(Long id, boolean active) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        user.setIsActive(active);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long id, String currentPassword, String newPassword) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash()))
            throw new BusinessRuleException("La contrase\u00f1a actual no es correcta");

        user.setPasswordHash(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    private UserResponseDTO toDTO(User u) {
        return new UserResponseDTO(u.getId(), u.getDni(), u.getFirstName(),
                u.getLastName(), u.getEmail(), u.getPhone(), u.getRole(), u.getIsActive());
    }
}