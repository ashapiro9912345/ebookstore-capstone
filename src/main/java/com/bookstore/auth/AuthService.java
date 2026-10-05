package com.bookstore.auth;

import com.bookstore.auth.dto.LoginRequest;
import com.bookstore.security.JwtService;
import com.bookstore.user.User;
import com.bookstore.user.UserRepository;
import com.bookstore.user.dto.AuthResponse;
import com.bookstore.user.dto.UserResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

/**
 * Authentication service.
 *
 * Validates credentials via Spring Security's {@link AuthenticationManager}
 * (which uses BCrypt password matching), then issues a JWT on success.
 *
 * On invalid credentials, {@link BadCredentialsException} propagates and is
 * mapped to HTTP 401 by the GlobalExceptionHandler.
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserRepository userRepository;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtService jwtService,
                       UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    public AuthResponse login(LoginRequest request) {
        // Authenticate — throws BadCredentialsException on failure (-> 401)
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(), request.getPassword()));

        // Credentials are valid; load the user and issue a token
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        String token = jwtService.generateToken(user.getEmail());

        UserResponse userResponse = new UserResponse(
                user.getId(), user.getFullName(), user.getEmail(), user.getCreatedAt());

        return new AuthResponse(token, userResponse);
    }
}
