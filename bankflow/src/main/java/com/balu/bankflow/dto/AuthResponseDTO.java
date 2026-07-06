package com.balu.bankflow.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponseDTO {

    private Long id;
    private String fullName;
    private String email;
    private String phone;
    private String role;
    private String token;
    private String tokenType;
    private String refreshToken;
}
