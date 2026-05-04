package com.unigear.tracker.service;

import com.unigear.tracker.dto.CreateEquipmentDto;
import com.unigear.tracker.dto.EquipmentDto;
import com.unigear.tracker.entity.Equipment;
import com.unigear.tracker.entity.User;
import com.unigear.tracker.repository.EquipmentRepository;
import com.unigear.tracker.repository.UserRepository;
import com.unigear.tracker.pattern.factory.EquipmentStatusFactory;
import com.unigear.tracker.pattern.strategy.SearchStrategy;
import com.unigear.tracker.pattern.strategy.SortStrategy;
import com.unigear.tracker.pattern.strategy.StrategyFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class EquipmentService {

    @Autowired
    private EquipmentRepository equipmentRepository;

    @Autowired
    private UserRepository userRepository;

    public List<EquipmentDto> getAllEquipment(String category, String search, 
            String searchStrategy, String sortStrategy) {
        List<Equipment> equipment;

        if (search != null && !search.isBlank()) {
            // Use search strategy (default: CONTAINS)
            String strategyType = (searchStrategy == null || searchStrategy.isBlank()) 
                ? "CONTAINS" 
                : searchStrategy.toUpperCase();
            try {
                SearchStrategy strategy = StrategyFactory.createSearchStrategy(strategyType);
                List<Equipment> allEquipment = equipmentRepository.findAll();
                equipment = strategy.search(allEquipment, search);
            } catch (IllegalArgumentException e) {
                // Fall back to basic search if strategy not found
                equipment = equipmentRepository.findByNameContainingIgnoreCaseOrCategoryContainingIgnoreCaseOrderByNameAsc(search, search);
            }
        } else if (category != null && !category.isBlank() && !"all".equalsIgnoreCase(category)) {
            equipment = equipmentRepository.findByCategoryIgnoreCaseOrderByNameAsc(category);
        } else {
            equipment = equipmentRepository.findAllByOrderByNameAsc();
        }

        // Apply sort strategy (default: BY_NAME)
        String sortStrategyType = (sortStrategy == null || sortStrategy.isBlank()) 
            ? "BY_NAME" 
            : sortStrategy.toUpperCase();
        try {
            SortStrategy sorter = StrategyFactory.createSortStrategy(sortStrategyType);
            equipment = sorter.sort(equipment);
        } catch (IllegalArgumentException e) {
            // Fall back to name sorting if strategy not found
            equipment.sort(Comparator.comparing(Equipment::getName, String.CASE_INSENSITIVE_ORDER));
        }

        return equipment.stream()
                .map(EquipmentDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EquipmentDto> getAllEquipment(String category, String search) {
        return getAllEquipment(category, search, "CONTAINS", "BY_NAME");
    }

    public EquipmentDto getEquipmentById(Long id) {
        Equipment equipment = equipmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Equipment not found"));

        return EquipmentDto.fromEntity(equipment);
    }

    @Transactional
    public EquipmentDto createEquipment(String userEmail, CreateEquipmentDto dto) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can add equipment");
        }

        if (equipmentRepository.findByNameIgnoreCase(dto.getName()).isPresent()) {
            throw new RuntimeException("Equipment with this name already exists");
        }

        if (dto.getAvailableQuantity() > dto.getTotalQuantity()) {
            throw new RuntimeException("Available quantity cannot exceed total quantity");
        }

        Equipment equipment = new Equipment();
        equipment.setName(dto.getName().trim());
        equipment.setCategory(dto.getCategory().trim());
        equipment.setLocation(dto.getLocation().trim());
        equipment.setDescription(dto.getDescription().trim());
        equipment.setSpecifications(String.join("\n", dto.getSpecifications()));
        equipment.setTotalQuantity(dto.getTotalQuantity());
        equipment.setAvailableQuantity(dto.getAvailableQuantity());

        // Use factory to determine status based on availability
        Equipment.EquipmentStatus status = EquipmentStatusFactory.createStatusFromQuantity(
            dto.getAvailableQuantity()
        );
        equipment.setStatus(status);

        Equipment saved = equipmentRepository.save(equipment);
        return EquipmentDto.fromEntity(saved);
    }
    
    /**
     * Update equipment status based on current availability
     * Uses EquipmentStatusFactory for consistent status determination
     */
    @Transactional
    public void updateEquipmentStatus(Equipment equipment) {
        Equipment.EquipmentStatus newStatus = EquipmentStatusFactory.createStatusFromQuantity(
            equipment.getAvailableQuantity()
        );
        equipment.setStatus(newStatus);
        equipmentRepository.save(equipment);
    }
    
    /**
     * Search equipment using specified strategy
     * @param query Search query
     * @param strategyType Strategy name (EXACT_MATCH, CONTAINS, etc)
     * @return Matching equipment DTOs
     */
    public List<EquipmentDto> searchWithStrategy(String query, String strategyType) {
        String strategy = (strategyType == null || strategyType.isBlank()) ? "CONTAINS" : strategyType.toUpperCase();
        try {
            SearchStrategy searchStrategy = StrategyFactory.createSearchStrategy(strategy);
            List<Equipment> allEquipment = equipmentRepository.findAll();
            List<Equipment> results = searchStrategy.search(allEquipment, query);
            return results.stream()
                    .map(EquipmentDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid search strategy: " + strategy);
        }
    }
    
    /**
     * Sort equipment using specified strategy
     * @param equipment List to sort
     * @param strategyType Strategy name (BY_NAME, BY_CATEGORY, BY_AVAILABILITY)
     * @return Sorted equipment DTOs
     */
    public List<EquipmentDto> sortWithStrategy(List<Equipment> equipment, String strategyType) {
        String strategy = (strategyType == null || strategyType.isBlank()) ? "BY_NAME" : strategyType.toUpperCase();
        try {
            SortStrategy sortStrategy = StrategyFactory.createSortStrategy(strategy);
            List<Equipment> sorted = sortStrategy.sort(equipment);
            return sorted.stream()
                    .map(EquipmentDto::fromEntity)
                    .collect(Collectors.toList());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("Invalid sort strategy: " + strategy);
        }
    }
}
