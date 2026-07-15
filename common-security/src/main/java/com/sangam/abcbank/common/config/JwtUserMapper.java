package com.sangam.abcbank.common.config;

import com.sangam.abcbank.common.dto.CommonUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Set;

/**
 * Single place responsible for turning a validated JWT into a CommonUser.
 * Any future consumer (filter, event listener, batch job, etc.) that needs a
 * CommonUser out of a token calls this instead of re-implementing extraction.
 */
@Component
@RequiredArgsConstructor
public class JwtUserMapper {

    private final JwtUtil jwtUtil;

    public CommonUser toCommonUser(String token) {
        String username = jwtUtil.extractUsername(token);
        String name = jwtUtil.extractName(token);
        String email = jwtUtil.extractEmail(token);
        Set<String> roles = new HashSet<>(jwtUtil.extractRoles(token));

        return CommonUser.builder()
                .username(username)
                .name(name)
                .email(email)
                .roles(roles)
                .build();
    }
}
