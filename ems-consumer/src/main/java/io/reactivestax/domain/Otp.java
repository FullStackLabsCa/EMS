package io.reactivestax.domain;

import io.reactivestax.validations.Status;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Otp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    long id;

    String currentOtp;
    long timestamp;
    Status status;
    long attempts;
    String customerId;
}
