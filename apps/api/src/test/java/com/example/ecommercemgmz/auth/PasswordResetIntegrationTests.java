package com.example.ecommercemgmz.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.ecommercemgmz.user.AppUser;
import com.example.ecommercemgmz.user.AppUserRepository;
import com.example.ecommercemgmz.user.UserRole;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PasswordResetIntegrationTests {
    private final MockMvc mockMvc;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;
    private final AppUserRepository userRepository;
    private final PasswordResetTokenRepository resetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    PasswordResetIntegrationTests(MockMvc mockMvc,
                                  com.fasterxml.jackson.databind.ObjectMapper objectMapper,
                                  AppUserRepository userRepository,
                                  PasswordResetTokenRepository resetTokenRepository,
                                  PasswordEncoder passwordEncoder) {
        this.mockMvc = mockMvc;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.resetTokenRepository = resetTokenRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Test
    void forgotPasswordAlwaysReturnsNeutralMessage() throws Exception {
        // Unknown email behaves identically to a known one (no user enumeration).
        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"nobody-" + UUID.randomUUID() + "@test.local\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());
    }

    @Test
    void forgotPasswordForKnownEmailCreatesResetToken() throws Exception {
        String email = uniqueEmail();
        userRepository.save(new AppUser("Customer", email, "old-hash", UserRole.CUSTOMER));

        mockMvc.perform(post("/api/auth/forgot-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"" + email + "\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());

        PasswordResetToken created = resetTokenRepository.findAll().get(0);
        assertThat(created.getUser().getEmail()).isEqualTo(email);
        assertThat(created.isUsable()).isTrue();
        assertThat(created.getUsedAt()).isNull();
    }

    @Test
    void resetPasswordUpdatesUserPasswordAndInvalidatesToken() throws Exception {
        String email = uniqueEmail();
        String rawToken = "raw-" + UUID.randomUUID();
        AppUser user = userRepository.save(new AppUser("Customer", email, "old-hash", UserRole.CUSTOMER));
        resetTokenRepository.save(new PasswordResetToken(
                user,
                MessageDigestSupport.sha256Hex(rawToken),
                Instant.now().plusSeconds(600)
        ));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"" + rawToken + "\",\"password\":\"N3wStrong!Pass\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").isNotEmpty());

        PasswordResetToken used = resetTokenRepository.findAll().get(0);
        assertThat(used.isUsable()).isFalse();
        assertThat(used.getUsedAt()).isNotNull();
        // Password hash was stored encoded, proving resetPassword persisted it.
        assertThat(passwordEncoder.matches("N3wStrong!Pass", userRepository.findByEmail(email).orElseThrow().getPasswordHash()))
                .isTrue();
    }

    @Test
    void resetPasswordRejectsUnknownToken() throws Exception {
        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"token\":\"not-a-real-token\",\"password\":\"N3wStrong!Pass\"}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void resetPasswordRejectsUsedToken() throws Exception {
        String email = uniqueEmail();
        String rawToken = "raw-" + UUID.randomUUID();
        AppUser user = userRepository.save(new AppUser("Customer", email, "old-hash", UserRole.CUSTOMER));
        resetTokenRepository.save(new PasswordResetToken(
                user,
                MessageDigestSupport.sha256Hex(rawToken),
                Instant.now().plusSeconds(600)
        ));

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "token", rawToken,
                                "password", "N3wStrong!Pass"))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/reset-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(java.util.Map.of(
                                "token", rawToken,
                                "password", "N3wStrong!Pass2"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registerRejectsWeakPasswords() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + uniqueEmail() + "\",\"password\":\"alllowercase1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password must contain uppercase, lowercase, and a number"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"Test\",\"email\":\"" + uniqueEmail() + "\",\"password\":\"Password123\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Password is too common. Please choose a stronger password"));
    }

    private String uniqueEmail() {
        return "reset-" + UUID.randomUUID() + "@test.local";
    }
}
