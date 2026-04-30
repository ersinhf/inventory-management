package com.smartinventory.materialrequest.repository;

import com.smartinventory.materialrequest.entity.MaterialRequest;
import com.smartinventory.materialrequest.enums.MaterialRequestStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MaterialRequestRepository extends JpaRepository<MaterialRequest, Long> {

    @Query("""
            SELECT mr FROM MaterialRequest mr
            WHERE (:status IS NULL OR mr.status = :status)
              AND (:requesterId IS NULL OR mr.requestedBy.id = :requesterId)
            ORDER BY mr.createdAt DESC
            """)
    List<MaterialRequest> filter(
            @Param("status") MaterialRequestStatus status,
            @Param("requesterId") Long requesterId);

    long countByStatus(MaterialRequestStatus status);
}
