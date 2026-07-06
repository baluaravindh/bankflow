package com.balu.bankflow.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
// extends OncePerRequestFilter
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // Step 1: Get Authorization header
        String authHeader = request.getHeader("Authorization");

        // Step 2: Check starts with "Bearer "
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // Step 3: Extract token
            String token = authHeader.substring(7);

            // Step 4: Validate token
            if (jwtUtil.isValid(token)) {

                // Step 5: Extract email and role
                String email = jwtUtil.extractEmail(token);
                String role = jwtUtil.extractRole(token);

                // Step 6: Set authentication in SecurityContext
                UsernamePasswordAuthenticationToken authentication
                        = new UsernamePasswordAuthenticationToken(email,
                        null,
                        List.of(new SimpleGrantedAuthority("ROLE_" + role)));
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }
        // Step 7: Continue filter chain
        filterChain.doFilter(request, response);
    }
}
