package com.example.ecommercemgmz.auth;

import com.example.ecommercemgmz.user.AppUser;
import com.example.ecommercemgmz.user.UserRole;

public record AuthResponse(
        String token,
        Long userId,
        String name,
        String email,
        UserRole role
) {
    public static AuthResponse from(String token, AppUser user) {
        return new AuthResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole());
    }
}
