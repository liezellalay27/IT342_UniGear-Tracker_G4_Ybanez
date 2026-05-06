package com.unigear.tracker.features.request.repository;

import com.unigear.tracker.features.request.entity.EquipmentRequest;
import com.unigear.tracker.features.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {
    List<EquipmentRequest> findByUserOrderByCreatedAtDesc(User user);
    List<EquipmentRequest> findByUserAndStatusOrderByCreatedAtDesc(User user, EquipmentRequest.RequestStatus status);
    List<EquipmentRequest> findAllByOrderByCreatedAtDesc();
    List<EquipmentRequest> findByStatusInOrderByCreatedAtDesc(List<EquipmentRequest.RequestStatus> statuses);
}
