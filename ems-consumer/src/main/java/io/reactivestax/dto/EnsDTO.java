package io.reactivestax.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.reactivestax.validations.NotificationType;
import io.reactivestax.validations.groups.CallGroup;
import io.reactivestax.validations.groups.EmailGroup;
import io.reactivestax.validations.groups.SmsGroup;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.Length;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class EnsDTO {
    private Long id;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Pattern(regexp = "\\d{10}", message = "Phone number should only contain digits")
    @NotNull(groups = {CallGroup.class, SmsGroup.class}, message = "Phone number cannot be null")
    @NotBlank(groups = {CallGroup.class, SmsGroup.class}, message = "Phone number cannot be blank")
    private String phoneNumber;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    @Email(message = "email must be a valid email address")
    @NotNull(groups = EmailGroup.class, message = "Email cannot be null")
    @NotBlank(groups = EmailGroup.class, message = "Email cannot be blank")
    private String email;

    @NotBlank(message = "message body should not be blank" )
    @NotNull(message = "message body should not be null" )
    private String message;

//    @NotBlank(message = "notification type should not be blank" )
//    @NotNull(message = "notification type should not be null" )
    NotificationType notificationType;

}