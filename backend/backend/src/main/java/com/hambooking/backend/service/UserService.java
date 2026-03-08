package com.hambooking.backend.service;

import com.hambooking.backend.dto.user.UserResponseDTO;
import com.hambooking.backend.exception.ResourceNotFoundException;
import com.hambooking.backend.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserResponseDTO getUserById(Long id) {
        return userRepository.findById(id)
                .map(u -> new UserResponseDTO(
                        u.getId(),
                        u.getDni(),
                        u.getFirstName(),
                        u.getLastName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getRole(),
                        u.getIsActive()
                ))
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }
}