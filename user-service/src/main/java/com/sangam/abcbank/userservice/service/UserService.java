package com.sangam.abcbank.userservice.service;

import com.sangam.abcbank.userservice.config.JwtUtil;
import com.sangam.abcbank.userservice.dto.*;
import com.sangam.abcbank.userservice.exception.DuplicateResourceException;
import com.sangam.abcbank.userservice.exception.ResourceNotFoundException;
import com.sangam.abcbank.userservice.model.Role;
import com.sangam.abcbank.userservice.model.User;
import com.sangam.abcbank.userservice.repository.RoleRepository;
import com.sangam.abcbank.userservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateResourceException("Username already taken: " + request.getUsername());
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateResourceException("Email already registered: " + request.getEmail());
        }

        Set<Role> roles = new HashSet<>();
        if (request.getRoles() == null || request.getRoles().isEmpty()) {
            roles.add(getOrCreateRole("ROLE_USER"));
        } else {
            for (String r : request.getRoles()) {
                String roleName = r.toUpperCase().startsWith("ROLE_") ? r.toUpperCase() : "ROLE_" + r.toUpperCase();
                roles.add(getOrCreateRole(roleName));
            }
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .fullName(request.getFullName())
                .enabled(true)
                .roles(roles)
                .build();

        User saved = userRepository.save(user);
        return toResponse(saved);
    }

    public JwtResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + request.getUsername()));

        Set<String> roleNames = user.getRoles().stream().map(Role::getName).collect(Collectors.toSet());
        String token = jwtUtil.generateToken(user.getUsername(), roleNames);

        return JwtResponse.builder()
                .token(token)
                .username(user.getUsername())
                .roles(roleNames)
                .expiresInMs(jwtUtil.getExpirationMs())
                .build();
    }

    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::toResponse).collect(Collectors.toList());
    }

    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return toResponse(user);
    }

    public UserResponse getUserByUsername(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + username));
        return toResponse(user);
    }

    @Transactional
    public UserResponse assignRoles(Long userId, AssignRoleRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        Set<Role> roles = new HashSet<>();
        for (String r : request.getRoles()) {
            String roleName = r.toUpperCase().startsWith("ROLE_") ? r.toUpperCase() : "ROLE_" + r.toUpperCase();
            roles.add(getOrCreateRole(roleName));
        }
        user.setRoles(roles);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public void setEnabled(Long userId, boolean enabled) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
        user.setEnabled(enabled);
        userRepository.save(user);
    }

    private Role getOrCreateRole(String name) {
        return roleRepository.findByName(name).orElseGet(() -> roleRepository.save(new Role(name)));
    }

    private UserResponse toResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .fullName(user.getFullName())
                .enabled(user.isEnabled())
                .roles(user.getRoles().stream().map(Role::getName).collect(Collectors.toSet()))
                .createdAt(user.getCreatedAt())
                .build();
    }
}
