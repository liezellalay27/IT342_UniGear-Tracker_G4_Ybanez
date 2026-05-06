package com.unigear.tracker.features.equipment.repository;

import com.unigear.tracker.features.equipment.entity.Equipment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EquipmentRepository extends JpaRepository<Equipment, Long> {

    List<Equipment> findAllByOrderByNameAsc();

    List<Equipment> findByCategoryIgnoreCaseOrderByNameAsc(String category);

    List<Equipment> findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrderByNameAsc(String name, String category);

    Optional<Equipment> findByNameIgnoreCase(String name);
}
