package io.reactivestax.dto;

import io.reactivestax.validations.Status;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.validator.constraints.Length;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpDTO {

//    @NotNull(message = "id cannot be null")
    long id;

//    @NotNull(message = "otp cannot be null")
    @Pattern(regexp = "[A-Za-z0-9]{16} ", message = "Phone number should only contain digits")
    String currentOtp;


    long timestamp;

//    @NotNull(message = "Status should not be null")
    Status status;

    @Length(max=5, message = "The length should be less than or equal to 5")
    long attempts;

    @NotNull(message = "The message id should not be null")
    String customerId;
}
