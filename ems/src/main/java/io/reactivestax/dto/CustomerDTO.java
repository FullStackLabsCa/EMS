package io.reactivestax.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CustomerDTO {
    int id;

    @Pattern(regexp = "\\d{10}", message = "Phone number should only contain digits")
    private List<String> phoneNumber;

    @Email(message = "email must be a valid email address")
    private List<String> email;
}
