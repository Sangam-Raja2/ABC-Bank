package com.sangam.abcbank.userservice.dto;

import com.sangam.abcbank.dto.CommonUser;
import com.sangam.abcbank.userservice.model.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtResponse {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private CommonUser user;
    private Set<Role> roles;
    private long expiresInMs;
}
