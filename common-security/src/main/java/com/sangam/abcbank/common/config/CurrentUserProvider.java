package com.sangam.abcbank.common.config;

import com.sangam.abcbank.common.dto.CommonUser;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public final class CurrentUserProvider {

    private CurrentUserProvider() {}

    public static CommonUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof CommonUser)) {
            throw new IllegalStateException("No authenticated CommonUser found in security context");
        }
        return (CommonUser) authentication.getPrincipal();
    }
}
