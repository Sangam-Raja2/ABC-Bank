package com.sangam.abcbank.common.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CommonUser {
    private Long id;
    private String username;
    private String name;
    private String email;
    private Set<String> roles;

}
