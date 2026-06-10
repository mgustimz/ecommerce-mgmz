package com.example.ecommercemgmz.user;

import com.example.ecommercemgmz.common.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
    private final AppUserRepository userRepository;

    public UserService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public UserProfileResponse findProfile(Long userId) {
        return UserProfileResponse.from(findEntity(userId));
    }

    @Transactional
    public UserProfileResponse updateProfile(Long userId, UpdateProfileRequest request) {
        AppUser user = findEntity(userId);
        user.setName(request.name());
        user.setPhone(request.phone());
        return UserProfileResponse.from(user);
    }

    private AppUser findEntity(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ApiException(HttpStatus.NOT_FOUND, "User not found"));
    }
}
