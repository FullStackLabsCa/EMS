package io.reactivestax.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.reactivestax.validations.enums.LockedStatus;
import io.reactivestax.validations.enums.NotificationType;
import io.reactivestax.validations.enums.Status;
import io.reactivestax.validations.enums.WindowLockStatus;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OtpDTO {

//    @NotNull(message = "id cannot be null")
    long id;
    @JsonInclude(JsonInclude.Include.NON_NULL)
//    @NotNull(message = "otp cannot be null")
    @Pattern(regexp = "[A-Za-z0-9]{16} ", message = "Phone number should only contain digits")
    String currentOtp;


    LocalDateTime created_at;
    LocalDateTime updated_at;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    LockedStatus lockedStatus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    WindowLockStatus windowOneLockedStatus;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    WindowLockStatus windowTwoLockedStatus;
    NotificationType notificationType;

//    @NotNull(message = "Status should not be null")
    Status status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    @NotNull(message = "The message id should not be null")
    String customerId;
}
