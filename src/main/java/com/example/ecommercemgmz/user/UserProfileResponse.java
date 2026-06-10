package com.example.ecommercemgmz.user;

public record UserProfileResponse(
        Long id,
        String name,
        String email,
        String phone,
        UserRole role
) {
    public static UserProfileResponse from(AppUser user) {
        return new UserProfileResponse(user.getId(), user.getName(), user.getEmail(), user.getPhone(), user.getRole());
    }
}
