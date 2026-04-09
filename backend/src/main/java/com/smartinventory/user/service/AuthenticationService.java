package com.smartinventory.user.service;

import com.smartinventory.config.JwtService;
import com.smartinventory.common.exception.ResourceNotFoundException;
import com.smartinventory.user.dto.AuthResponse;
import com.smartinventory.user.dto.LoginRequest;
import com.smartinventory.user.dto.RegisterRequest;
import com.smartinventory.user.entity.Role;
import com.smartinventory.user.entity.User;
import com.smartinventory.user.enums.RoleName;
import com.smartinventory.user.repository.RoleRepository;
import com.smartinventory.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final UserService userService;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("Bu e-posta adresi zaten kayıtlı: " + request.getEmail());
        }

        RoleName roleName = RoleName.valueOf(request.getRoleName());
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new ResourceNotFoundException("Role", "name", roleName));

        User user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .department(request.getDepartment())
                .role(role)
                .build();

        User savedUser = userRepository.save(user);
        String token = jwtService.generateToken(
                Map.of("role", role.getName().name()), savedUser);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userService.mapToResponse(savedUser))
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Geçersiz kimlik bilgileri"));

        String token = jwtService.generateToken(
                Map.of("role", user.getRole().getName().name()), user);

        return AuthResponse.builder()
                .accessToken(token)
                .tokenType("Bearer")
                .user(userService.mapToResponse(user))
                .build();
    }
}
