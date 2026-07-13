package com.recruitment.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message = "Email is required.")
    @Email(message = "Invalid email format.")
    @Size(max = 255, message = "Email must not exceed 255 characters.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(
            min = 8,
            max = 100,
            message = "Password must be between 8 and 100 characters."
    )
    private String password;

    @NotBlank(message = "Full name is required.")
    @Size(
            max = 150,
            message = "Full name must not exceed 150 characters."
    )
    private String fullName;

    @Size(
            max = 20,
            message = "Phone number must not exceed 20 characters."
    )
    private String phone;

}