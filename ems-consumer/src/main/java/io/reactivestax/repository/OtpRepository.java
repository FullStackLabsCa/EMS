package io.reactivestax.repository;

import io.reactivestax.domain.Otp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface OtpRepository extends JpaRepository<Otp, Long> {

    @Query("SELECT o FROM Otp o WHERE o.customerId = ?1")
    List<Otp> getAllOtpForCustomer(String id);
}