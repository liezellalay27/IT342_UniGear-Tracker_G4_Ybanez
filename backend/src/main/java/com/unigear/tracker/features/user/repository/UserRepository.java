package com.unigear.tracker.repository;

import com.unigear.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    // Find user by email
    Optional<User> findByEmail(String email);
    
    // Check if email already exists
    boolean existsByEmail(String email);

    // Check if an admin user already exists
    boolean existsByRole(User.Role role);
}
