package io.reactivestax.domain;

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
    LocalDateTime created_at;
    LocalDateTime updated_at;
    Boolean locked;
    @Enumerated(EnumType.STRING)
    Status status;
    long attempts;
    String customerId;
    private String notificationType;
}
