package com.example.ecommercemgmz.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Fixed-window per-IP rate limiter for public auth endpoints.
 * ponytail: in-memory counters; swap for Redis/Bucket4j when this runs on more than one instance.
 */
@Component
@Slf4j
public class AuthRateLimitFilter extends OncePerRequestFilter {
    private static final String X_FORWARDED_FOR = "X-Forwarded-For";

    private final int loginLimit;
    private final int registerLimit;
    private final int forgotLimit;
    private final int windowSeconds;

    private final ConcurrentHashMap<String, WindowCounter> counters = new ConcurrentHashMap<>();

    public AuthRateLimitFilter(
            @Value("${app.rate-limit.login:5}") int loginLimit,
            @Value("${app.rate-limit.register:3}") int registerLimit,
            @Value("${app.rate-limit.forgot-password:3}") int forgotLimit,
            @Value("${app.rate-limit.window-seconds:60}") int windowSeconds) {
        this.loginLimit = loginLimit;
        this.registerLimit = registerLimit;
        this.forgotLimit = forgotLimit;
        this.windowSeconds = windowSeconds;
    }

    private record Limit(int max) {
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String path = request.getRequestURI();
        Limit limit = limitFor(path, request.getMethod());
        if (limit == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String key = clientAddress(request) + "|" + path;
        WindowCounter counter = counters.compute(key, (ignored, existing) -> {
            long now = System.currentTimeMillis();
            if (existing == null || now - existing.windowStart >= windowSeconds * 1000L) {
                return new WindowCounter(now, new AtomicInteger(1));
            }
            existing.count.incrementAndGet();
            return existing;
        });

        if (counter.count.get() > limit.max()) {
            log.warn("Rate limit exceeded for client {} on {}", clientAddress(request), path);
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setHeader("Retry-After", String.valueOf(windowSeconds));
            response.getWriter().write(
                    "{\"status\":429,\"error\":\"Too Many Requests\",\"message\":\"Too many attempts. Please try again later\"}");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private Limit limitFor(String path, String method) {
        if ("/api/auth/login".equals(path) && "POST".equals(method)) {
            return new Limit(loginLimit);
        }
        if ("/api/auth/register".equals(path) && "POST".equals(method)) {
            return new Limit(registerLimit);
        }
        if ("/api/auth/forgot-password".equals(path) && "POST".equals(method)) {
            return new Limit(forgotLimit);
        }
        return null;
    }

    private String clientAddress(HttpServletRequest request) {
        String forwarded = request.getHeader(X_FORWARDED_FOR);
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        String remote = request.getRemoteAddr();
        return remote == null ? "unknown" : remote;
    }

    private static final class WindowCounter {
        private final long windowStart;
        private final AtomicInteger count;

        private WindowCounter(long windowStart, AtomicInteger count) {
            this.windowStart = windowStart;
            this.count = count;
        }
    }
}
