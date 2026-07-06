package com.sangam.abcbank.userservice.controller;

import com.sangam.abcbank.userservice.dto.CreateRoleRequest;
import com.sangam.abcbank.userservice.exception.DuplicateResourceException;
import com.sangam.abcbank.userservice.model.Role;
import com.sangam.abcbank.userservice.repository.RoleRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RoleController {

    private final RoleRepository roleRepository;

    @GetMapping
    public ResponseEntity<List<Role>> getAllRoles() {
        return ResponseEntity.ok(roleRepository.findAll());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Role> createRole(@Valid @RequestBody CreateRoleRequest request) {
        String roleName = request.getName().toUpperCase().startsWith("ROLE_")
                ? request.getName().toUpperCase()
                : "ROLE_" + request.getName().toUpperCase();

        if (roleRepository.existsByName(roleName)) {
            throw new DuplicateResourceException("Role already exists: " + roleName);
        }
        Role saved = roleRepository.save(new Role(roleName));
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
