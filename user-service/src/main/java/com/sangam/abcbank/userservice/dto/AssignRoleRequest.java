package com.sangam.abcbank.userservice.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.Set;

@Data
public class AssignRoleRequest {

    @NotEmpty(message = "at least one role must be provided")
    private Set<String> roles;
}
