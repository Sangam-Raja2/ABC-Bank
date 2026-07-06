package com.sangam.abcbank.userservice.controller;

import com.sangam.abcbank.userservice.dto.JwtResponse;
import com.sangam.abcbank.userservice.dto.LoginRequest;
import com.sangam.abcbank.userservice.dto.RegisterRequest;
import com.sangam.abcbank.userservice.dto.UserResponse;
import com.sangam.abcbank.userservice.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse response = userService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(userService.login(request));
    }
}
