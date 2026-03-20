package com.unigear.tracker.service;

import com.unigear.tracker.dto.CreateRequestDto;
import com.unigear.tracker.dto.EquipmentRequestDto;
import com.unigear.tracker.entity.EquipmentRequest;
import com.unigear.tracker.entity.User;
import com.unigear.tracker.repository.EquipmentRequestRepository;
import com.unigear.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {
    
    @Autowired
    private EquipmentRequestRepository requestRepository;
    
    @Autowired
    private UserRepository userRepository;
    
    @Transactional
    public EquipmentRequestDto createRequest(String userEmail, CreateRequestDto dto) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        EquipmentRequest request = new EquipmentRequest();
        request.setUser(user);
        request.setEquipmentName(dto.getEquipmentName());
        request.setCategory(dto.getCategory());
        request.setDescription(dto.getDescription());
        request.setQuantity(dto.getQuantity());
        request.setStatus(EquipmentRequest.RequestStatus.PENDING);
        
        EquipmentRequest savedRequest = requestRepository.save(request);
        return EquipmentRequestDto.fromEntity(savedRequest);
    }
    
    public List<EquipmentRequestDto> getUserRequests(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return requestRepository.findByUserOrderByCreatedAtDesc(user)
            .stream()
            .map(EquipmentRequestDto::fromEntity)
            .collect(Collectors.toList());
    }
    
    public EquipmentRequestDto getRequestById(Long id, String userEmail) {
        EquipmentRequest request = requestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (!request.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to request");
        }
        
        return EquipmentRequestDto.fromEntity(request);
    }
    
    @Transactional
    public void deleteRequest(Long id, String userEmail) {
        EquipmentRequest request = requestRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Request not found"));
        
        if (!request.getUser().getEmail().equals(userEmail)) {
            throw new RuntimeException("Unauthorized access to request");
        }
        
        if (request.getStatus() != EquipmentRequest.RequestStatus.PENDING) {
            throw new RuntimeException("Can only delete pending requests");
        }
        
        requestRepository.delete(request);
    }
}
