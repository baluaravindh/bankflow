package com.balu.bankflow.service;

import com.balu.bankflow.entity.RefreshToken;
import com.balu.bankflow.entity.User;
import com.balu.bankflow.exception.ResourceNotFoundException;
import com.balu.bankflow.repository.RefreshTokenRepository;
import com.balu.bankflow.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {

    // @Value jwt.refresh-expiration
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    // Inject: RefreshTokenRepository
    private final RefreshTokenRepository refreshTokenRepository;


    // METHOD: createRefreshToken(User user)
    // WHO: called after login/register
    // WHAT to do:
    // WHAT to return: RefreshToken
    @Transactional
    public RefreshToken createRefreshToken(User user) {

        //   Step 1: Delete existing token for this user if exists
        if (refreshTokenRepository.findByUser(user).isPresent()) {
            refreshTokenRepository.deleteByUser(user);
        }

        //   Step 2: Build RefreshToken entity
        //           token = UUID.randomUUID().toString()
        //           expiryDate = LocalDateTime.now()
        //                        + refreshExpiration milliseconds
        //           user = user passed in
        RefreshToken refreshToken = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expiryDate(LocalDateTime.now().plusSeconds(refreshExpiration / 1000))
                .user(user)
                .build();

        //   Step 3: Save and return
        refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }


    // METHOD: validateRefreshToken(String token)
    // WHO: called during token refresh
    // WHAT to do:
    // WHAT to return: RefreshToken
    @Transactional
    public RefreshToken validateRefreshToken(String token) {

        //   Step 1: Find by token → throw if not found
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Token not found"));

        //   Step 2: Check expiry → delete and throw if expired
        if (refreshToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            refreshTokenRepository.delete(refreshToken);
            throw new RuntimeException("Refresh token expired, Please login again.");
        }

        //   Step 3: Return valid token
        return refreshToken;
    }

    // METHOD: deleteByUser(User user)
    // WHO: called during login
    // WHAT to do: delete token by user
    // WHAT to return: void
    @Transactional
    public void deleteByUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
