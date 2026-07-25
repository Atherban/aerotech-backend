package com.aerotech.ced_ops_backend.security.service;

import com.aerotech.ced_ops_backend.security.model.CustomUserDetails;
import com.aerotech.ced_ops_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String employeeId) throws UsernameNotFoundException {
        return userRepository
                .findByEmployeeId(employeeId)
                .map(user -> new CustomUserDetails(
                        user.getEmployeeId(),
                        user.getPassword(),
                        user.getActive(),
                        user.getRole().getName()
                ))
                .orElseThrow(()->
                        new UsernameNotFoundException("User not found")
                );
    }
}
