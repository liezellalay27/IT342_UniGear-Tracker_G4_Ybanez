package com.unigear.tracker.dto;

import com.unigear.tracker.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProfileDto {
    private Long id;
    private String name;
    private String email;
    private String picture;
    private LocalDateTime createdAt;
    
    public static ProfileDto fromUser(User user) {
        return new ProfileDto(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPicture(),
            user.getCreatedAt()
        );
    }
}
