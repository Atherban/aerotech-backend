package com.aerotech.ced_ops_backend.user.service;

import com.aerotech.ced_ops_backend.auth.repository.RefreshTokenRepository;
import com.aerotech.ced_ops_backend.common.exception.BadRequestException;
import com.aerotech.ced_ops_backend.common.exception.ResourceNotFoundException;
import com.aerotech.ced_ops_backend.common.enums.NotificationType;
import com.aerotech.ced_ops_backend.common.pagination.PageableResolver;
import com.aerotech.ced_ops_backend.common.pagination.SpecificationBuilder;
import com.aerotech.ced_ops_backend.common.response.PageResponse;
import com.aerotech.ced_ops_backend.notification.service.NotificationChannel;
import com.aerotech.ced_ops_backend.role.entity.Role;
import com.aerotech.ced_ops_backend.role.service.RoleService;
import com.aerotech.ced_ops_backend.user.dto.*;
import com.aerotech.ced_ops_backend.user.entity.User;
import com.aerotech.ced_ops_backend.user.mapper.UserMapper;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserService {

    private static final Map<String, String> SORT_COLUMNS = Map.of(
            "id", "id",
            "employeeId", "employeeId",
            "firstName", "firstName",
            "lastName", "lastName",
            "role", "role.name",
            "active", "active",
            "createdAt", "createdAt"
    );

    private static final String DEFAULT_SORT = "id";

    private final UserRepository userRepository;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;
    private final RefreshTokenRepository refreshTokenRepository;
    private final NotificationChannel notificationChannel;

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.existsByEmployeeId(request.getEmployeeId())) {
            throw new BadRequestException("Employee ID already exists");
        }

        if (userRepository.existsByMobileNumber(request.getMobileNumber())) {
            throw new BadRequestException("Mobile number already exists");
        }

        Role role = roleService.getRoleByName(request.getRole());

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

        notificationChannel.notify(
                NotificationType.USER_CREATED,
                user.getId(),
                "Welcome to CED Ops",
                "Your account " + user.getEmployeeId() + " has been created with the role " + role.getName() + ".",
                "USER",
                String.valueOf(user.getId()),
                null
        );

        notificationChannel.notify(
                NotificationType.WELCOME,
                user.getId(),
                "Account Activated",
                "Your CED Ops account is now active. Please keep your credentials safe.",
                "USER",
                String.valueOf(user.getId()),
                null
        );

        return mapper.toResponse(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {

        return mapper.toResponseList(
                userRepository.findAllByOrderByIdAsc()
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(UserFilterRequest filter) {

        Specification<User> spec = SpecificationBuilder.<User>builder()
                .keyword(filter.getKeyword(),
                        "employeeId", "firstName", "lastName", "mobileNumber")
                .equals("role.name", filter.getRole())
                .equals("active", filter.getActive())
                .build();

        Pageable pageable = PageableResolver.resolve(filter, SORT_COLUMNS, DEFAULT_SORT);

        Page<User> page = userRepository.findAll(spec, pageable);

        return PageResponse.from(page.map(mapper::toResponse));
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

        Role role = roleService.getRoleByName(request.getRole());

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

        notificationChannel.notify(
                NotificationType.PASSWORD_CHANGED,
                user.getId(),
                "Password Changed",
                "Your password was changed successfully.",
                "USER",
                String.valueOf(user.getId()),
                null
        );
    }

}