package io.reactivestax.domain;

import io.reactivestax.validations.enums.LockedStatus;
import io.reactivestax.validations.enums.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String currentOtp;
    LocalDateTime createdAt;
    LocalDateTime updatedAt;
    LockedStatus lockedStatus;
    @Enumerated(EnumType.STRING)
    Status status;
    long attempts;
    String customerId;
    String notificationType;
    String window1;
    String window2;

}
