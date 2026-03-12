package com.unigear.tracker.repository;

import com.unigear.tracker.entity.EquipmentRequest;
import com.unigear.tracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {
    List<EquipmentRequest> findByUserOrderByCreatedAtDesc(User user);
    List<EquipmentRequest> findByUserAndStatusOrderByCreatedAtDesc(User user, EquipmentRequest.RequestStatus status);
}
