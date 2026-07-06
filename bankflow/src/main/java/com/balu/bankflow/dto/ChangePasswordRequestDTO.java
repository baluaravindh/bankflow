package com.balu.bankflow.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ChangePasswordRequestDTO {

    @NotBlank(message = "Current password is required.")
    private String currentPassword;

    @NotBlank(message = "New password is required.")
    @Size(min = 6, message = "Password should be atleast 6 characters.")
    private String newPassword;

    @NotBlank(message = "Confirm new password is required.")
    private String confirmNewPassword;
}
