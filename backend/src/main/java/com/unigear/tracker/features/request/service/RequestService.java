package com.unigear.tracker.features.request.service;

import com.unigear.tracker.features.request.dto.CreateRequestDto;
import com.unigear.tracker.features.request.dto.AdminEquipmentRequestView;
import com.unigear.tracker.features.request.dto.EquipmentRequestDto;
import com.unigear.tracker.features.admin.dto.AdminUserDto;
import com.unigear.tracker.features.equipment.entity.Equipment;
import com.unigear.tracker.features.request.entity.EquipmentRequest;
import com.unigear.tracker.features.user.entity.User;
import com.unigear.tracker.features.equipment.repository.EquipmentRepository;
import com.unigear.tracker.features.request.repository.EquipmentRequestRepository;
import com.unigear.tracker.features.user.repository.UserRepository;
import com.unigear.tracker.pattern.factory.RequestValidatorFactory;
import com.unigear.tracker.pattern.observer.EventPublisher;
import com.unigear.tracker.pattern.observer.SystemEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RequestService {
    
    @Autowired
    private EquipmentRequestRepository requestRepository;
    
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EquipmentRepository equipmentRepository;
    
    @Autowired
    private EventPublisher eventPublisher;
    
    /**
     * Centralized validation using RequestValidatorFactory
     * Validates all request components (dates, student info, PDF)
     */
    private void validateRequestWithFactory(
            java.time.LocalDate borrowDate,
            java.time.LocalDate returnDate,
            String studentName,
            String schoolIdNumber,
            String yearLevel,
            String course,
            MultipartFile pdfFile) throws RuntimeException {
        try {
            // Create validators using factory
            List<com.unigear.tracker.pattern.factory.interfaces.RequestValidator> validators =
                RequestValidatorFactory.createRequestValidators(
                    borrowDate, returnDate, studentName, schoolIdNumber,
                    yearLevel, course, pdfFile
                );
            // Execute all validators
            RequestValidatorFactory.validateAll(validators);
        } catch (Exception e) {
            throw new RuntimeException("Validation failed: " + e.getMessage());
        }
    }
    
    private byte[] extractPdfContent(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.equals("application/pdf")) {
            throw new RuntimeException("Only PDF files are allowed for event approval");
        }

        // Validate file size (max 10MB)
        long maxFileSize = 10 * 1024 * 1024; // 10MB
        if (file.getSize() > maxFileSize) {
            throw new RuntimeException("PDF file cannot exceed 10MB");
        }

        return file.getBytes();
    }

    @SuppressWarnings("unused")
    private void validateStudentInfo(String studentName, String schoolIdNumber, String yearLevel, String course) {
        if (studentName == null || studentName.isBlank()) {
            throw new RuntimeException("Student name is required");
        }
        if (schoolIdNumber == null || schoolIdNumber.isBlank()) {
            throw new RuntimeException("School ID number is required");
        }
        if (!schoolIdNumber.matches("^\\d{2}-\\d{4}-\\d{3}$")) {
            throw new RuntimeException("School ID must follow format 17-0635-488");
        }
        if (yearLevel == null || yearLevel.isBlank()) {
            throw new RuntimeException("Year is required");
        }
        if (course == null || course.isBlank()) {
            throw new RuntimeException("Course is required");
        }
    }
    
    @Transactional
    public EquipmentRequestDto createRequest(
            String userEmail,
            String equipmentName,
            String category,
            String description,
            Integer quantity,
            String borrowDateStr,
            String returnDateStr,
            String studentName,
            String schoolIdNumber,
            String yearLevel,
            String course,
            MultipartFile eventApprovalPdf) {
        
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Parse dates
        java.time.LocalDate borrowDate = java.time.LocalDate.parse(borrowDateStr);
        java.time.LocalDate returnDate = java.time.LocalDate.parse(returnDateStr);

        // Use factory-based validation - single method call validates everything
        validateRequestWithFactory(
            borrowDate,
            returnDate,
            studentName,
            schoolIdNumber,
            yearLevel,
            course,
            eventApprovalPdf
        );

        Equipment equipment = equipmentRepository.findByNameIgnoreCase(equipmentName)
            .orElseThrow(() -> new RuntimeException("Equipment not found in catalog"));

        if (equipment.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Selected equipment is not currently available");
        }

        if (quantity > equipment.getAvailableQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available units");
        }
        
        // Extract PDF content if provided
        byte[] pdfContent = null;
        String pdfFilename = null;
        try {
            pdfContent = extractPdfContent(eventApprovalPdf);
            if (pdfContent != null && eventApprovalPdf != null) {
                // Store original filename
                pdfFilename = eventApprovalPdf.getOriginalFilename() != null 
                    ? eventApprovalPdf.getOriginalFilename() 
                    : "event_approval.pdf";
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to process PDF file: " + e.getMessage());
        }
        
        EquipmentRequest request = new EquipmentRequest();
        request.setUser(user);
        request.setEquipmentName(equipment.getName());
        request.setCategory(equipment.getCategory());
        request.setDescription(description);
        request.setQuantity(quantity);
        request.setBorrowDate(borrowDate);
        request.setReturnDate(returnDate);
        request.setStudentName(studentName.trim());
        request.setSchoolIdNumber(schoolIdNumber.trim());
        request.setYearLevel(yearLevel.trim());
        request.setCourse(course.trim());
        request.setStatus(EquipmentRequest.RequestStatus.PENDING);
        request.setEventApprovalPdf(pdfContent);
        request.setEventApprovalPdfFilename(pdfFilename);
        
        EquipmentRequest savedRequest = requestRepository.save(request);
        // Publish creation event for notifications
        SystemEvent creationEvent = SystemEvent.builder()
            .eventType(SystemEvent.EventType.REQUEST_CREATED)
            .source("RequestService")
            .targetId(savedRequest.getId())
            .targetType("REQUEST")
            .actor(user.getEmail())
            .data(user.getEmail())
            .description("Request " + savedRequest.getId() + " created by " + user.getEmail())
            .build();
        creationEvent.addMetadata("equipmentName", savedRequest.getEquipmentName());
        creationEvent.addMetadata("quantity", savedRequest.getQuantity());
        eventPublisher.publish(creationEvent);

        return EquipmentRequestDto.fromEntity(savedRequest);
    }
    
    @Transactional
    public EquipmentRequestDto createRequest(String userEmail, CreateRequestDto dto) {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Use factory-based validation
        validateRequestWithFactory(
            dto.getBorrowDate(),
            dto.getReturnDate(),
            dto.getStudentName(),
            dto.getSchoolIdNumber(),
            dto.getYearLevel(),
            dto.getCourse(),
            null  // No PDF in DTO version
        );

        Equipment equipment = equipmentRepository.findByNameIgnoreCase(dto.getEquipmentName())
            .orElseThrow(() -> new RuntimeException("Equipment not found in catalog"));

        if (equipment.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Selected equipment is not currently available");
        }

        if (dto.getQuantity() > equipment.getAvailableQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available units");
        }
        
        EquipmentRequest request = new EquipmentRequest();
        request.setUser(user);
        request.setEquipmentName(equipment.getName());
        request.setCategory(equipment.getCategory());
        request.setDescription(dto.getDescription());
        request.setQuantity(dto.getQuantity());
        request.setBorrowDate(dto.getBorrowDate());
        request.setReturnDate(dto.getReturnDate());
        request.setStudentName(dto.getStudentName().trim());
        request.setSchoolIdNumber(dto.getSchoolIdNumber().trim());
        request.setYearLevel(dto.getYearLevel().trim());
        request.setCourse(dto.getCourse().trim());
        request.setStatus(EquipmentRequest.RequestStatus.PENDING);
        
        EquipmentRequest savedRequest = requestRepository.save(request);
        SystemEvent creationEvent = SystemEvent.builder()
            .eventType(SystemEvent.EventType.REQUEST_CREATED)
            .source("RequestService")
            .targetId(savedRequest.getId())
            .targetType("REQUEST")
            .actor(user.getEmail())
            .data(user.getEmail())
            .description("Request " + savedRequest.getId() + " created by " + user.getEmail())
            .build();
        creationEvent.addMetadata("equipmentName", savedRequest.getEquipmentName());
        creationEvent.addMetadata("quantity", savedRequest.getQuantity());
        eventPublisher.publish(creationEvent);

        return EquipmentRequestDto.fromEntity(savedRequest);
    }

    public EquipmentRequestDto createRequest(String userEmail, CreateRequestDto dto, MultipartFile eventApprovalPdf) throws IOException {
        User user = userRepository.findByEmail(userEmail)
            .orElseThrow(() -> new RuntimeException("User not found"));

        // Use factory-based validation
        validateRequestWithFactory(
            dto.getBorrowDate(),
            dto.getReturnDate(),
            dto.getStudentName(),
            dto.getSchoolIdNumber(),
            dto.getYearLevel(),
            dto.getCourse(),
            eventApprovalPdf  // Pass PDF for validation
        );

        Equipment equipment = equipmentRepository.findByNameIgnoreCase(dto.getEquipmentName())
            .orElseThrow(() -> new RuntimeException("Equipment not found in catalog"));

        if (equipment.getAvailableQuantity() <= 0) {
            throw new RuntimeException("Selected equipment is not currently available");
        }

        if (dto.getQuantity() > equipment.getAvailableQuantity()) {
            throw new RuntimeException("Requested quantity exceeds available units");
        }
        
        EquipmentRequest request = new EquipmentRequest();
        request.setUser(user);
        request.setEquipmentName(equipment.getName());
        request.setCategory(equipment.getCategory());
        request.setDescription(dto.getDescription());
        request.setQuantity(dto.getQuantity());
        request.setBorrowDate(dto.getBorrowDate());
        request.setReturnDate(dto.getReturnDate());
        request.setStudentName(dto.getStudentName().trim());
        request.setSchoolIdNumber(dto.getSchoolIdNumber().trim());
        request.setYearLevel(dto.getYearLevel().trim());
        request.setCourse(dto.getCourse().trim());
        request.setStatus(EquipmentRequest.RequestStatus.PENDING);
        
        // Extract and store PDF if provided
        if (eventApprovalPdf != null && !eventApprovalPdf.isEmpty()) {
            byte[] pdfContent = extractPdfContent(eventApprovalPdf);
            request.setEventApprovalPdf(pdfContent);
            request.setEventApprovalPdfFilename(
                    eventApprovalPdf.getOriginalFilename() != null
                            ? eventApprovalPdf.getOriginalFilename()
                            : "event_approval.pdf"
            );
        }
        
        EquipmentRequest savedRequest = requestRepository.save(request);
        SystemEvent creationEvent = SystemEvent.builder()
            .eventType(SystemEvent.EventType.REQUEST_CREATED)
            .source("RequestService")
            .targetId(savedRequest.getId())
            .targetType("REQUEST")
            .actor(user.getEmail())
            .data(user.getEmail())
            .description("Request " + savedRequest.getId() + " created by " + user.getEmail())
            .build();
        creationEvent.addMetadata("equipmentName", savedRequest.getEquipmentName());
        creationEvent.addMetadata("quantity", savedRequest.getQuantity());
        eventPublisher.publish(creationEvent);

        return EquipmentRequestDto.fromEntity(savedRequest);
    }
    
    public List<EquipmentRequestDto> getUserRequests(String userEmail) {
        ensureAuthenticatedUserExists(userEmail);

        return requestRepository.findUserRequestViewsByEmailOrderByCreatedAtDesc(userEmail)
            .stream()
            .map(this::fromAdminRequestView)
            .collect(Collectors.toList());
    }

    private void ensureAuthenticatedUserExists(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
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

    public List<AdminUserDto> getAllUsersForAdmin(String adminEmail) {
        ensureAdmin(adminEmail);
        return userRepository.findAll()
                .stream()
                .map(AdminUserDto::fromEntity)
                .collect(Collectors.toList());
    }

    public List<EquipmentRequestDto> getAllRequestsForAdmin(String adminEmail) {
        ensureAdmin(adminEmail);
        return requestRepository.findAllAdminRequestViews()
                .stream()
            .map(this::fromAdminRequestView)
                .collect(Collectors.toList());
    }

    public List<EquipmentRequestDto> getBorrowedRequestsForAdmin(String adminEmail) {
        ensureAdmin(adminEmail);
        return requestRepository.findBorrowedAdminRequestViews()
                .stream()
            .map(this::fromAdminRequestView)
                .collect(Collectors.toList());
    }

    private EquipmentRequestDto fromAdminRequestView(AdminEquipmentRequestView view) {
        return new EquipmentRequestDto(
            view.getId(),
            view.getUserId(),
            view.getRequesterName(),
            view.getRequesterEmail(),
            view.getEquipmentName(),
            view.getCategory(),
            view.getDescription(),
            view.getQuantity(),
            view.getBorrowDate(),
            view.getReturnDate(),
            view.getStudentName(),
            view.getSchoolIdNumber(),
            view.getYearLevel(),
            view.getCourse(),
            view.getStatus(),
            view.getNotes(),
            view.getReturnedOnTime(),
            view.getActualReturnedAt(),
            view.getEventApprovalPdf(),
            view.getCreatedAt(),
            view.getUpdatedAt()
        );
        }

    public byte[] getPdfContent(Long requestId, String userEmail) throws IOException {
        EquipmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        User currentUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isOwner = request.getUser().getEmail().equals(userEmail);
        boolean isAdmin = currentUser.getRole() == User.Role.ADMIN;

        if (!isOwner && !isAdmin) {
            throw new RuntimeException("Unauthorized access to request");
        }

        if (request.getEventApprovalPdf() == null || request.getEventApprovalPdf().length == 0) {
            throw new RuntimeException("No PDF file attached to this request");
        }

        return request.getEventApprovalPdf();
    }

    @Transactional
    public EquipmentRequestDto updateRequestStatusForAdmin(String adminEmail, Long requestId, String statusText, String notes, Boolean returnedOnTime) {
        ensureAdmin(adminEmail);

        EquipmentRequest request = requestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Request not found"));

        EquipmentRequest.RequestStatus nextStatus;
        try {
            nextStatus = EquipmentRequest.RequestStatus.valueOf(statusText.toUpperCase());
        } catch (Exception ex) {
            throw new RuntimeException("Invalid status. Use APPROVED, REJECTED, or COMPLETED");
        }

        if (request.getStatus() == EquipmentRequest.RequestStatus.PENDING) {
            if (nextStatus != EquipmentRequest.RequestStatus.APPROVED
                    && nextStatus != EquipmentRequest.RequestStatus.REJECTED) {
                throw new RuntimeException("Pending requests can only be APPROVED or REJECTED");
            }
        } else if (request.getStatus() == EquipmentRequest.RequestStatus.APPROVED) {
            if (nextStatus != EquipmentRequest.RequestStatus.COMPLETED) {
                throw new RuntimeException("Approved requests can only be marked COMPLETED");
            }
        } else {
            throw new RuntimeException("Only pending or approved requests can be updated");
        }

        if (nextStatus == EquipmentRequest.RequestStatus.APPROVED) {
            Equipment equipment = equipmentRepository.findByNameIgnoreCase(request.getEquipmentName())
                    .orElseThrow(() -> new RuntimeException("Equipment not found in catalog"));

            if (equipment.getAvailableQuantity() < request.getQuantity()) {
                throw new RuntimeException("Insufficient available quantity to approve this request");
            }

            equipment.setAvailableQuantity(equipment.getAvailableQuantity() - request.getQuantity());
            if (equipment.getAvailableQuantity() == 0) {
                equipment.setStatus(Equipment.EquipmentStatus.IN_USE);
            }
            equipmentRepository.save(equipment);
        }

        if (nextStatus == EquipmentRequest.RequestStatus.COMPLETED) {
            if (returnedOnTime == null) {
                throw new RuntimeException("Please set whether the item was returned on time");
            }

            Equipment equipment = equipmentRepository.findByNameIgnoreCase(request.getEquipmentName())
                    .orElseThrow(() -> new RuntimeException("Equipment not found in catalog"));

            equipment.setAvailableQuantity(equipment.getAvailableQuantity() + request.getQuantity());
            if (equipment.getStatus() != Equipment.EquipmentStatus.MAINTENANCE && equipment.getAvailableQuantity() > 0) {
                equipment.setStatus(Equipment.EquipmentStatus.AVAILABLE);
            }
            equipmentRepository.save(equipment);

            request.setReturnedOnTime(returnedOnTime);
            request.setActualReturnedAt(LocalDateTime.now());
        }

        request.setStatus(nextStatus);
        if (notes != null && !notes.isBlank()) {
            request.setNotes(notes.trim());
        }

        EquipmentRequest updated = requestRepository.save(request);
        
        // Publish events based on status change
        if (nextStatus == EquipmentRequest.RequestStatus.APPROVED) {
            SystemEvent approvalEvent = SystemEvent.builder()
                .eventType(SystemEvent.EventType.REQUEST_APPROVED)
                .source("RequestService")
                .targetId(requestId)
                .targetType("REQUEST")
                .actor(adminEmail)
                .data(request.getUser().getEmail())
                .description("Request " + requestId + " approved by admin")
                .build();
            approvalEvent.addMetadata("equipmentName", updated.getEquipmentName());
            approvalEvent.addMetadata("quantity", updated.getQuantity());
            approvalEvent.addMetadata("studentName", updated.getStudentName());
            approvalEvent.addMetadata("schoolIdNumber", updated.getSchoolIdNumber());
            approvalEvent.addMetadata("borrowDate", updated.getBorrowDate());
            approvalEvent.addMetadata("returnDate", updated.getReturnDate());
            approvalEvent.addMetadata("yearLevel", updated.getYearLevel());
            approvalEvent.addMetadata("course", updated.getCourse());
            approvalEvent.addMetadata("notes", updated.getNotes() != null ? updated.getNotes() : "");
            eventPublisher.publish(approvalEvent);
        } else if (nextStatus == EquipmentRequest.RequestStatus.REJECTED) {
            SystemEvent rejectionEvent = SystemEvent.builder()
                .eventType(SystemEvent.EventType.REQUEST_REJECTED)
                .source("RequestService")
                .targetId(requestId)
                .targetType("REQUEST")
                .actor(adminEmail)
                .data(request.getUser().getEmail())
                .description("Request " + requestId + " rejected")
                .build();
            rejectionEvent.addMetadata("rejectionReason", notes != null ? notes : "No reason provided");
            eventPublisher.publish(rejectionEvent);
        }
        
        return EquipmentRequestDto.fromEntity(updated);
    }

    private void ensureAdmin(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (user.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Admin access only");
        }
    }
}
