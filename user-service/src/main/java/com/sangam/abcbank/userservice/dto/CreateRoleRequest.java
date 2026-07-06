package com.sangam.abcbank.userservice.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class CreateRoleRequest {

    @NotBlank(message = "role name is required")
    private String name;
}
