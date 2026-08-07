package com.example.ecommercemgmz.cart;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class AnonymousCartCookieSupport {
    public static final String COOKIE_NAME = "mgmz_anonymous_cart";
    private static final int MAX_AGE_SECONDS = 604800; // 7 days

    public String resolveToken(String headerToken, HttpServletResponse response) {
        String token = headerToken;
        if (token == null || token.isBlank()) {
            token = UUID.randomUUID().toString();
        }
        setCookie(response, token);
        return token;
    }

    public void setCookie(HttpServletResponse response, String token) {
        String cookie = COOKIE_NAME + "=" + token
                + "; HttpOnly"
                + "; Path=/"
                + "; Max-Age=" + MAX_AGE_SECONDS
                + "; SameSite=Lax";
        response.addHeader("Set-Cookie", cookie);
    }

    public void clearCookie(HttpServletResponse response) {
        String cookie = COOKIE_NAME + "="
                + "; HttpOnly"
                + "; Path=/"
                + "; Max-Age=0"
                + "; SameSite=Lax";
        response.addHeader("Set-Cookie", cookie);
    }
}
