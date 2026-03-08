package com.hambooking.backend.service;

import com.hambooking.backend.dto.service.ServiceResponseDTO;
import com.hambooking.backend.repository.ServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ServiceService {

    private final ServiceRepository serviceRepository;

    public ServiceService(ServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    @Transactional(readOnly = true)
    public List<ServiceResponseDTO> listActiveServices() {
        return serviceRepository.findByIsActiveTrue()
                .stream()
                .map(s -> new ServiceResponseDTO(
                        s.getId(),
                        s.getName(),
                        s.getDescription(),
                        s.getDurationMinutes(),
                        s.getBasePrice(),
                        s.getIsActive()
                ))
                .collect(Collectors.toList());
    }
}