package com.example.ecommercemgmz.auth;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.user.AppUser;
import com.example.ecommercemgmz.user.AppUserRepository;
import com.example.ecommercemgmz.user.UserRole;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private static final List<String> COMMON_PASSWORDS = List.of(
            "password", "password123", "password1", "12345678", "123456789", "qwerty123",
            "qwertyuiop", "11111111", "12341234", "abcd1234", "iloveyou1", "aaaaaaaa",
            "letmein123", "admin1234", "welcome123", "monkey123", "dragon123", "00000000",
            "abcdefgh", "1q2w3e4r5t"
    );
    private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(30);

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final EmailSender emailSender;

    @Value("${app.mail.public-base-url:http://localhost:3000}")
    private String publicBaseUrl;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validatePassword(request.password());
        String email = request.email().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new ApiException(HttpStatus.CONFLICT, "Email is already registered");
        }
        AppUser user = userRepository.save(new AppUser(
                request.name(),
                email,
                passwordEncoder.encode(request.password()),
                UserRole.CUSTOMER
        ));
        return AuthResponse.from(jwtService.generate(user), user);
    }

    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must be at least 8 characters");
        }
        if (!password.matches(".*[A-Z].*") || !password.matches(".*[a-z].*") || !password.matches(".*[0-9].*")) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password must contain uppercase, lowercase, and a number");
        }
        if (COMMON_PASSWORDS.contains(password.toLowerCase())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "Password is too common. Please choose a stronger password");
        }
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        return AuthResponse.from(jwtService.generate(user), user);
    }

    /**
     * Always returns a neutral message so callers cannot discover which emails are registered.
     */
    @Transactional
    public String forgotPassword(String email) {
        userRepository.findByEmail(email.toLowerCase()).ifPresent(user -> {
            String token = generateToken();
            PasswordResetToken record = new PasswordResetToken(user, hashToken(token), Instant.now().plus(RESET_TOKEN_TTL));
            resetTokenRepository.invalidateAllForUser(user.getId(), Instant.now());
            resetTokenRepository.save(record);
            emailSender.send(
                    user.getEmail(),
                    "MGMZ Store password reset",
                    "Reset your password within 30 minutes using this link: "
                            + publicBaseUrl + "/reset-password?token=" + token
            );
        });
        return "If the email is registered, a password reset link has been sent";
    }

    @Transactional
    public String resetPassword(ResetPasswordRequest request) {
        validatePassword(request.password());
        PasswordResetToken record = resetTokenRepository.findByTokenHash(hashToken(request.token()))
                .orElseThrow(() -> new ApiException(HttpStatus.BAD_REQUEST, "Invalid or expired reset link"));
        if (!record.isUsable()) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "This reset link has expired or was already used");
        }
        AppUser user = record.getUser();
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        userRepository.save(user);
        record.markUsed();
        resetTokenRepository.save(record);
        return "Password updated. You can now login with your new password";
    }

    private String generateToken() {
        byte[] bytes = new byte[32];
        new SecureRandom().nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    private String hashToken(String token) {
        return MessageDigestSupport.sha256Hex(token);
    }
}
