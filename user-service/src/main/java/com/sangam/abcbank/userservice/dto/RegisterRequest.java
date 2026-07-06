package com.sangam.abcbank.userservice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.Set;

@Data
public class RegisterRequest {

    @NotBlank(message = "username is required")
    @Size(min = 3, max = 50)
    private String username;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "password must be at least 6 characters")
    private String password;

    @NotBlank(message = "email is required")
    @Email(message = "email must be valid")
    private String email;

    private String fullName;

    /**
     * Optional. If omitted, ROLE_USER is assigned by default.
     * Only an existing ADMIN can grant ROLE_ADMIN via the role-management API.
     */
    private Set<String> roles;
}
