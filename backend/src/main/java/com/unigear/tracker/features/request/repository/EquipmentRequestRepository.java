package com.unigear.tracker.features.request.repository;

import com.unigear.tracker.features.request.dto.AdminEquipmentRequestView;
import com.unigear.tracker.features.request.entity.EquipmentRequest;
import com.unigear.tracker.features.user.entity.User;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EquipmentRequestRepository extends JpaRepository<EquipmentRequest, Long> {
    List<EquipmentRequest> findByUserOrderByCreatedAtDesc(User user);
    List<EquipmentRequest> findByUserAndStatusOrderByCreatedAtDesc(User user, EquipmentRequest.RequestStatus status);
    List<EquipmentRequest> findAllByOrderByCreatedAtDesc();
    List<EquipmentRequest> findByStatusInOrderByCreatedAtDesc(List<EquipmentRequest.RequestStatus> statuses);

    @Query(value = """
            select
                er.id as id,
                er.user_id as userId,
                u.name as requesterName,
                u.email as requesterEmail,
                er.equipment_name as equipmentName,
                er.category as category,
                er.description as description,
                er.quantity as quantity,
                er.borrow_date as borrowDate,
                er.return_date as returnDate,
                er.student_name as studentName,
                er.school_id_number as schoolIdNumber,
                er.year_level as yearLevel,
                er.course as course,
                er.status as status,
                er.notes as notes,
                er.returned_on_time as returnedOnTime,
                er.actual_returned_at as actualReturnedAt,
                er.event_approval_pdf_filename as eventApprovalPdf,
                er.created_at as createdAt,
                er.updated_at as updatedAt
            from equipment_requests er
            join users u on u.id = er.user_id
            where u.email = :email
            order by er.created_at desc
            """, nativeQuery = true)
    List<AdminEquipmentRequestView> findUserRequestViewsByEmailOrderByCreatedAtDesc(String email);

    @Query(value = """
            select
                er.id as id,
                er.user_id as userId,
                u.name as requesterName,
                u.email as requesterEmail,
                er.equipment_name as equipmentName,
                er.category as category,
                er.description as description,
                er.quantity as quantity,
                er.borrow_date as borrowDate,
                er.return_date as returnDate,
                er.student_name as studentName,
                er.school_id_number as schoolIdNumber,
                er.year_level as yearLevel,
                er.course as course,
                er.status as status,
                er.notes as notes,
                er.returned_on_time as returnedOnTime,
                er.actual_returned_at as actualReturnedAt,
                er.event_approval_pdf_filename as eventApprovalPdf,
                er.created_at as createdAt,
                er.updated_at as updatedAt
            from equipment_requests er
            join users u on u.id = er.user_id
            order by er.created_at desc
            """, nativeQuery = true)
    List<AdminEquipmentRequestView> findAllAdminRequestViews();

    @Query(value = """
            select
                er.id as id,
                er.user_id as userId,
                u.name as requesterName,
                u.email as requesterEmail,
                er.equipment_name as equipmentName,
                er.category as category,
                er.description as description,
                er.quantity as quantity,
                er.borrow_date as borrowDate,
                er.return_date as returnDate,
                er.student_name as studentName,
                er.school_id_number as schoolIdNumber,
                er.year_level as yearLevel,
                er.course as course,
                er.status as status,
                er.notes as notes,
                er.returned_on_time as returnedOnTime,
                er.actual_returned_at as actualReturnedAt,
                er.event_approval_pdf_filename as eventApprovalPdf,
                er.created_at as createdAt,
                er.updated_at as updatedAt
            from equipment_requests er
            join users u on u.id = er.user_id
            where er.status in ('APPROVED', 'COMPLETED')
            order by er.created_at desc
            """, nativeQuery = true)
    List<AdminEquipmentRequestView> findBorrowedAdminRequestViews();

    boolean existsByEquipmentNameIgnoreCaseAndStatusIn(String equipmentName, List<EquipmentRequest.RequestStatus> statuses);
}
