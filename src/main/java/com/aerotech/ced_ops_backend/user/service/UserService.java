package com.aerotech.ced_ops_backend.user.service;

import com.aerotech.ced_ops_backend.auth.repository.RefreshTokenRepository;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.role.entity.Role;
import com.aerotech.ced_ops_backend.role.service.RoleService;
import com.aerotech.ced_ops_backend.user.dto.*;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.mapper.UserMapper;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final RefreshTokenRepository refreshTokenRepository;

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException("Mobile number already exists");
        }

        Role role = roleService.getRolebyName(request.getRole());

        User user = User.builder()
                .employeeId(request.getEmployeeId())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .mobileNumber(request.getMobileNumber())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(role)
                .active(true)
                .build();

        userRepository.save(user);

        log.info("User {} created", user.getEmployeeId());

        return mapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return mapper.toResponseList(
                userRepository.findAllByOrderByIdAsc()
        );
    }

    @Transactional(readOnly = true)
    public UserResponse getUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return mapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUser(String employeeId) {

        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        return mapper.toResponse(user);
    }

    public UserResponse updateUser(Long id, UpdateUserRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!user.getMobileNumber().equals(request.getMobileNumber())
                && userRepository.existsByMobileNumber(request.getMobileNumber())) {

            throw new BadRequestException("Mobile number already exists");
        }

        Role role = roleService.getRolebyName(request.getRole());

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setMobileNumber(request.getMobileNumber());
        user.setRole(role);

        userRepository.save(user);

        log.info("User {} updated", user.getEmployeeId());

        return mapper.toResponse(user);
    }

    public void deleteUser(Long id) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        refreshTokenRepository.deleteByUser(user);

        userRepository.delete(user);

        log.info("User {} deleted", user.getEmployeeId());
    }

    public void updateStatus(Long id, UpdateStatusRequest request) {

        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        user.setActive(request.getActive());

        userRepository.save(user);

        log.info("User {} status changed to {}", user.getEmployeeId(), request.getActive());
    }

    public void changePassword(String employeeId,
                               ChangePasswordRequest request) {

        User user = userRepository.findByEmployeeId(employeeId)
                .orElseThrow(() ->
                        new ResourceNotFoundException("User not found"));

        if (!passwordEncoder.matches(
                request.getOldPassword(),
                user.getPassword())) {

            throw new BadRequestException("Old password is incorrect");
        }

        user.setPassword(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        log.info("Password changed for {}", employeeId);
    }

}