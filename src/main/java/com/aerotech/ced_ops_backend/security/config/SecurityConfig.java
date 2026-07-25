package com.aerotech.ced_ops_backend.security.config;

import com.aerotech.ced_ops_backend.auth.filter.JwtAuthenticationFilter;
import com.aerotech.ced_ops_backend.security.handler.JwtAuthenticationEntryPoint;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final JwtAuthenticationEntryPoint authenticationEntryPoint;

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

        http

                // Disable CSRF since we're using JWT
                .csrf(csrf -> csrf.disable())

                // Enable CORS (important when React Native connects later)
                .cors(Customizer.withDefaults())

                // Stateless Session
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Custom Authentication Provider
                .authenticationProvider(authenticationProvider)

                // Exception Handler
                .exceptionHandling(exception ->
                        exception.authenticationEntryPoint(authenticationEntryPoint)
                )

                // Authorization
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public Endpoints
                         */
                        .requestMatchers(
                                "/api/auth/**",

                                "/swagger",
                                "/swagger/**",

                                "/swagger-ui/**",
                                "/swagger-ui.html",

                                "/api-docs",
                                "/api-docs/**",

                                "/v3/api-docs",
                                "/v3/api-docs/**",

                                "/webjars/**",

                                "/error"
                        )
                        .permitAll()

                        /*
                         * Super Admin
                         */
                        .requestMatchers(HttpMethod.POST, "/api/users")
                        .hasRole("SUPER_ADMIN")

                        /*
                         * Admin + Super Admin
                         */
                        .requestMatchers(HttpMethod.GET, "/api/users/**")
                        .hasAnyRole("SUPER_ADMIN", "ADMIN")

                        /*
                         * Everything Else
                         */
                        .anyRequest()
                        .authenticated()
                )

                .addFilterBefore(
                        jwtAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                );

        return http.build();
    }
}