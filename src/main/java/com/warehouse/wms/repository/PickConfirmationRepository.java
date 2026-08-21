package com.warehouse.wms.repository;

import com.warehouse.wms.entity.PickConfirmation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Repository
public interface PickConfirmationRepository extends JpaRepository<PickConfirmation, Long> {

    Optional<PickConfirmation> findByConfirmationNumber(String confirmationNumber);

    List<PickConfirmation> findByPickTaskNumber(String pickTaskNumber);

    List<PickConfirmation> findBySoNumber(String soNumber);

    List<PickConfirmation> findByStatus(String status);

    @Modifying
    @Transactional
    @Query("UPDATE PickConfirmation pc SET pc.status = :status WHERE pc.confirmationNumber = :confirmationNumber")
    void updateStatus(@Param("confirmationNumber") String confirmationNumber, @Param("status") String status);
}