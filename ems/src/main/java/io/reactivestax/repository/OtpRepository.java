package io.reactivestax.repository;

import io.reactivestax.domain.Otp;
import io.reactivestax.validations.enums.LockedStatus;
import io.reactivestax.validations.enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface OtpRepository extends JpaRepository<Otp, Long> {

    @Query("SELECT o FROM Otp o WHERE o.customerId = ?1")
    List<Otp> getAllOtpForCustomer(String id);


    @Modifying
    @Transactional
    @Query("UPDATE Otp o SET o.status = :status WHERE o.customerId = :customerId")
    int updateOtpStatus(@Param("status") Status status, @Param("customerId") String customerId);

    @Modifying
    @Transactional
    @Query("UPDATE Otp o SET o.lockedStatus = :lockedStatus WHERE o.customerId = :customerId")
    void updateLockedStatus(LockedStatus lockedStatus, Long customerId);
}