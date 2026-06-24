package com.example.ecommercemgmz.auth;

import com.example.ecommercemgmz.common.ApiException;
import com.example.ecommercemgmz.user.AppUser;
import com.example.ecommercemgmz.user.UserRole;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

@Service
public class JwtService {
    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final String secret;
    private final long expirationMinutes;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiration-minutes}") long expirationMinutes) {
        this.objectMapper = objectMapper;
        this.secret = secret;
        this.expirationMinutes = expirationMinutes;
    }

    public String generate(AppUser user) {
        try {
            Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
            Map<String, Object> payload = Map.of(
                    "sub", user.getEmail(),
                    "uid", user.getId(),
                    "role", user.getRole().name(),
                    "exp", Instant.now().plusSeconds(expirationMinutes * 60).getEpochSecond()
            );
            String unsignedToken = encode(header) + "." + encode(payload);
            return unsignedToken + "." + sign(unsignedToken);
        } catch (JsonProcessingException | InvalidKeyException | NoSuchAlgorithmException exception) {
            throw new ApiException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to generate token");
        }
    }

    public AuthenticatedUser parse(String token) {
        try {
            String[] parts = token.split("\\.");
            if (parts.length != 3) {
                throw new IllegalArgumentException("Invalid token");
            }
            String unsignedToken = parts[0] + "." + parts[1];
            if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
                throw new IllegalArgumentException("Invalid token signature");
            }

            Map<String, Object> payload = objectMapper.readValue(BASE64_URL_DECODER.decode(parts[1]), new TypeReference<>() {
            });
            long expiresAt = ((Number) payload.get("exp")).longValue();
            if (Instant.now().getEpochSecond() > expiresAt) {
                throw new IllegalArgumentException("Token expired");
            }
            Long userId = ((Number) payload.get("uid")).longValue();
            String email = (String) payload.get("sub");
            UserRole role = UserRole.valueOf((String) payload.get("role"));
            return new AuthenticatedUser(userId, email, role);
        } catch (IOException | InvalidKeyException | NoSuchAlgorithmException | IllegalArgumentException | ClassCastException | NullPointerException exception) {
            throw new ApiException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }
    }

    private String encode(Object value) throws JsonProcessingException {
        return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
    }

    private String sign(String value) throws NoSuchAlgorithmException, InvalidKeyException {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return BASE64_URL_ENCODER.encodeToString(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
    }

    private boolean constantTimeEquals(String first, String second) {
        return MessageDigestSupport.equals(first.getBytes(StandardCharsets.UTF_8), second.getBytes(StandardCharsets.UTF_8));
    }
}
