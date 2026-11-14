package com.library.service;

import com.library.config.JwtUtils;
import com.library.dto.AuthDtos.*;
import com.library.model.User;
import com.library.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    public AuthResponse register(RegisterRequest req) {
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Email already registered: " + req.getEmail());
        }

        String role = (req.getRole() != null && !req.getRole().isEmpty()) ? req.getRole() : "ROLE_STUDENT";

        User user = User.builder()
                .name(req.getName())
                .email(req.getEmail())
                .password(passwordEncoder.encode(req.getPassword()))
                .studentId(req.getStudentId())
                .role(role)
                .status("ACTIVE")
                .build();

        User saved = userRepository.save(user);
        String token = jwtUtils.generateToken(saved.getEmail(), saved.getRole());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(saved.getId())
                .name(saved.getName())
                .email(saved.getEmail())
                .studentId(saved.getStudentId())
                .role(saved.getRole())
                .build();
    }

    public AuthResponse login(LoginRequest req) {
        User user = userRepository.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + req.getEmail()));

        if (!passwordEncoder.matches(req.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password credentials.");
        }

        String token = jwtUtils.generateToken(user.getEmail(), user.getRole());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .studentId(user.getStudentId())
                .role(user.getRole())
                .build();
    }
}

