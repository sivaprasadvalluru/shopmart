package com.shopmart.service;

import com.shopmart.dto.AuthResponse;
import com.shopmart.dto.LoginRequest;
import com.shopmart.dto.RegisterRequest;
import com.shopmart.exception.EmailAlreadyExistsException;
import com.shopmart.model.entity.User;
import com.shopmart.model.enums.Role;
import com.shopmart.repository.UserRepository;
import com.shopmart.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;

    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("An account with email " + request.email() + " already exists");
        }

        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.CUSTOMER)
                .build();
        userRepository.save(user);

        Authentication authentication = authenticate(request.email(), request.password());
        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    public AuthResponse login(LoginRequest request) {
        Authentication authentication = authenticate(request.email(), request.password());
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found: " + request.email()));
        String token = jwtTokenProvider.generateToken(authentication);
        return new AuthResponse(token, user.getEmail(), user.getRole());
    }

    private Authentication authenticate(String email, String password) {
        return authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, password));
    }
}
