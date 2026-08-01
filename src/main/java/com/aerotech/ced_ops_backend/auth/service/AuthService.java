package com.aerotech.ced_ops_backend.auth.service;

import com.aerotech.ced_ops_backend.auth.dto.AuthResponse;
import com.aerotech.ced_ops_backend.auth.dto.LoginRequest;
import com.aerotech.ced_ops_backend.auth.dto.RefreshTokenRequest;
import com.aerotech.ced_ops_backend.auth.entity.RefreshToken;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.security.jwt.JwtService;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.aerotech.ced_ops_backend.user.dto.UserResponse;
import com.aerotech.ced_ops_backend.user.mapper.UserMapper;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;

    private final UserRepository userRepository;

    private final JwtService jwtService;

    private final RefreshTokenService refreshTokenService;

    private final UserMapper mapper;

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(

                new UsernamePasswordAuthenticationToken(

                        request.getEmployeeId(),

                        request.getPassword()

                )

        );

        User user = userRepository.findByEmployeeId(
                        request.getEmployeeId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        String accessToken = jwtService.generateAccessToken(user);

        RefreshToken refreshToken =
                refreshTokenService.create(user);

        log.info("User logged in: employeeId={}, role={}", user.getEmployeeId(), user.getRole().getName());

        return AuthResponse.builder()

                .accessToken(accessToken)

                .refreshToken(refreshToken.getToken())

                .tokenType("Bearer")

                .employeeId(user.getEmployeeId())

                .fullName(
                        user.getFirstName() + " " + user.getLastName()
                )

                .role(user.getRole().getName())

                .build();

    }

    public AuthResponse refreshToken(
            RefreshTokenRequest request
    ) {

        RefreshToken refreshToken =
                refreshTokenService.verify(
                        request.getRefreshToken()
                );

        User user = refreshToken.getUser();

        String accessToken =
                jwtService.generateAccessToken(user);

        log.info("Token refreshed for user: employeeId={}", user.getEmployeeId());

        return AuthResponse.builder()

                .accessToken(accessToken)

                .refreshToken(refreshToken.getToken())

                .tokenType("Bearer")

                .employeeId(user.getEmployeeId())

                .fullName(
                        user.getFirstName() + " " + user.getLastName()
                )

                .role(user.getRole().getName())

                .build();

    }

    public void logout(String employeeId) {

        User user = userRepository
                .findByEmployeeId(employeeId)
                .orElseThrow();

        refreshTokenService.delete(user);

        log.info("User logged out: employeeId={}", employeeId);

    }

    @Transactional(readOnly = true)
    public UserResponse me(String employeeId) {

        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return mapper.toResponse(user);

    }

    public boolean validateToken(String token) {

        return jwtService.isTokenValid(token);

    }

}