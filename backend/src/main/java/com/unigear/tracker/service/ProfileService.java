package com.unigear.tracker.service;

import com.unigear.tracker.dto.ProfileDto;
import com.unigear.tracker.dto.UpdateProfileDto;
import com.unigear.tracker.entity.User;
import com.unigear.tracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {
    
    @Autowired
    private UserRepository userRepository;
    
    public ProfileDto getProfile(String email) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        return ProfileDto.fromUser(user);
    }
    
    @Transactional
    public ProfileDto updateProfile(String email, UpdateProfileDto dto) {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new RuntimeException("User not found"));
        
        user.setName(dto.getName());
        if (dto.getPicture() != null && !dto.getPicture().isEmpty()) {
            user.setPicture(dto.getPicture());
        }
        
        User updatedUser = userRepository.save(user);
        return ProfileDto.fromUser(updatedUser);
    }
}
