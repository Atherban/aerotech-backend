package com.aerotech.ced_ops_backend.auth.service;

import com.aerotech.ced_ops_backend.auth.entity.RefreshToken;
import com.aerotech.ced_ops_backend.auth.repository.RefreshTokenRepository;
import com.aerotech.ced_ops_backend.common.exception.UnauthorizedException;
import com.aerotech.ced_ops_backend.security.jwt.JwtService;
import com.aerotech.ced_ops_backend.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneId;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class RefreshTokenService {

    private final RefreshTokenRepository repository;
    private final JwtService jwtService;

    public RefreshToken create(User user) {

        repository.deleteByUser(user);
        String token = jwtService.generateRefreshToken(user.getEmployeeId());
        RefreshToken refreshToken = RefreshToken.builder()
                .token(token)
                .expiryDate(
                        jwtService.getExpiration(token)
                                .toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDateTime()
                )
                .user(user)
                .build();

        log.info("Refresh token created for user: employeeId={}", user.getEmployeeId());
        return repository.save(refreshToken);
    }

    public RefreshToken verify(String token) {
        RefreshToken refreshToken = repository.findByToken(token)
                .orElseThrow(() ->
                        new UnauthorizedException("Invalid Refresh Token"));

        if (!jwtService.isTokenValid(token)) {
            repository.delete(refreshToken);
            log.warn("Expired refresh token used: userId={}", refreshToken.getUser().getId());
            throw new UnauthorizedException("Refresh Token Expired");
        }

        return refreshToken;
    }

    public void delete(User user) {
        repository.deleteByUser(user);
        log.info("Refresh tokens deleted for user: employeeId={}", user.getEmployeeId());
    }
}
