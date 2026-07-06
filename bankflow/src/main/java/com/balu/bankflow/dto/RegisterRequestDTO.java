package com.balu.bankflow.dto;

import com.balu.bankflow.entity.User;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class RegisterRequestDTO {

    @NotBlank(message = "Full name is required.")
    @Size(min = 2, message = "Name should be atleast 2 characters.")
    private String fullName;

    @NotBlank(message = "Email is required.")
    @Email(message = "Please enter the valid email address.")
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(min = 6, message = "Password should be atleast 6 characters.")
    private String password;

    @Pattern(regexp = "^[0-9]{10}$", message = "Please enter a valid 10 digits phone number.")
    private String phone;

    @NotNull(message = "Role is required.")
    private User.Role role;
}
