package com.balu.bankflow.service;

import com.balu.bankflow.dto.AuthResponseDTO;
import com.balu.bankflow.dto.ChangePasswordRequestDTO;
import com.balu.bankflow.dto.LoginRequestDTO;
import com.balu.bankflow.dto.RegisterRequestDTO;
import com.balu.bankflow.entity.RefreshToken;
import com.balu.bankflow.entity.User;
import com.balu.bankflow.exception.DuplicateUserFoundException;
import com.balu.bankflow.exception.InvalidCredentialsException;
import com.balu.bankflow.exception.ResourceNotFoundException;
import com.balu.bankflow.repository.RefreshTokenRepository;
import com.balu.bankflow.repository.UserRepository;
import com.balu.bankflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {

    // Inject: UserRepository, PasswordEncoder, JwtUtil, RefreshTokenService, EmailService
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final RefreshTokenService refreshTokenService;
    private final EmailService emailService;

    // METHOD: register(RegisterRequestDTO dto)
    // WHO: anyone (public endpoint)
    // WHAT validate:
    //   - check email already exists → throw DuplicateUserFoundException
    // WHAT to do:
    // WHAT to return: AuthResponseDTO
    @Transactional
    public AuthResponseDTO register(RegisterRequestDTO dto) {

        log.info("Registration attempt for email: {}", dto.getEmail());
        //   - check email already exists → throw DuplicateUserFoundException
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new DuplicateUserFoundException("User already exists");
        }

        //   Step 1: Encode password
        //   Step 2: Build and save User entity
        User user = User.builder()
                .fullName(dto.getFullName())
                .email(dto.getEmail())
                .password(passwordEncoder.encode(dto.getPassword()))
                .phone(dto.getPhone())
                .role(dto.getRole())
                .build();

        User savedUser = userRepository.save(user);

        //   Step 3: Generate JWT token
        String accessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        //   Step 4: Create refresh token
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(savedUser);

        //   Step 5: Send welcome email (async)
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFullName());

        //   Step 6: Return AuthResponseDTO
        log.info("User registered successfully: {}", savedUser.getEmail());
        return mapToDto(savedUser, accessToken, refreshToken.getToken());
    }


    // METHOD: login(LoginRequestDTO dto)
    // WHO: anyone (public endpoint)
    // WHAT validate:
    //   - find user by email → throw ResourceNotFoundException
    //   - verify password → throw InvalidCredentialsException
    //   - check isActive → throw RuntimeException
    // WHAT to do:
    // WHAT to return: AuthResponseDTO
    public AuthResponseDTO login(LoginRequestDTO dto) {

        log.info("Login attempt for email: {}", dto.getEmail());
        //   - find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   - verify password → throw InvalidCredentialsException
        if (!passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid password");
        }

        //   - check isActive → throw RuntimeException
        if (!user.isActive()) {
            throw new RuntimeException("User has been disabled");
        }

        //   Step 1: Delete old refresh token
        refreshTokenService.deleteByUser(user);

        //   Step 2: Generate new JWT token
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        //   Step 3: Create new refresh token
        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        //   Step 4: Return AuthResponseDTO
        log.info("Logged in successfully with email: {}", dto.getEmail());
        return mapToDto(user, newAccessToken, newRefreshToken.getToken());
    }

    // METHOD: refreshToken(String token)
    // WHO: authenticated user
    // WHAT to do:
    // WHAT to return: AuthResponseDTO
    @Transactional
    public AuthResponseDTO refreshToken(String token) {

        //   Step 1: Validate refresh token
        RefreshToken refreshToken = refreshTokenService.validateRefreshToken(token);

        //   Step 2: Get user from refresh token
        User user = refreshToken.getUser();

        //   Step 3: Generate new JWT token
        String newAccessToken = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        RefreshToken newRefreshToken = refreshTokenService.createRefreshToken(user);

        //   Step 4: Return AuthResponseDTO
        return mapToDto(user, newAccessToken, newRefreshToken.getToken());
    }

    // METHOD: changePassword(String email, ChangePasswordRequestDTO dto)
    // WHO: authenticated user (their own account)
    // WHAT validate:
    //   - find user by email → throw ResourceNotFoundException
    //   - verify current password matches → throw InvalidCredentialsException
    //   - confirm newPassword equals confirmNewPassword → throw RuntimeException
    // WHAT to do:
    // WHAT to return: String ("Password changed successfully")
    public String changePassword(String email, ChangePasswordRequestDTO dto) {

        //   - find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   - verify current password matches → throw InvalidCredentialsException
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Your current password is incorrect.");
        }

        //   - confirm newPassword equals confirmNewPassword → throw RuntimeException
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new RuntimeException("Your new password and confirm new password doesn't match.");
        }

        //   Step 1: Encode new password
        //   Step 2: Set new password on user
        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));

        //   Step 3: Save user
        User saveduser = userRepository.save(user);

        //   Step 4: log.info password changed
        log.info("Password changed successfully: {}", saveduser.getEmail());
        return "Password changed successfully";
    }

    // METHOD: logout(String email)
    // WHO: authenticated user
    // WHAT to do:
    // WHAT to return: String ("Logged out successfully")
    public String logout(String email) {

        //   Step 1: Find user by email → throw ResourceNotFoundException
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        //   Step 2: Delete refresh token by user
        refreshTokenService.deleteByUser(user);

        //   Step 3: log.info logged out successfully
        log.info("User logged out successfully: {}", user.getEmail());
        return "User logged out successfully";
    }

    // PRIVATE METHOD: mapToDto(User user, String token, String refreshToken)
    // WHAT to return: AuthResponseDTO
    private AuthResponseDTO mapToDto(User user, String accessToken, String token) {
        return AuthResponseDTO.builder()
                .id(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .role(user.getRole().name())
                .token(accessToken)
                .tokenType("Bearer ")
                .refreshToken(token)
                .build();
    }
}
