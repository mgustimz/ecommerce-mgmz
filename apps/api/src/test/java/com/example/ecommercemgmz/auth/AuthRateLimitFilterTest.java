package com.example.ecommercemgmz.auth;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.ServletException;
import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

class AuthRateLimitFilterTest {

    @Test
    void allowsRequestsBelowLimitThenReturns429() throws ServletException, IOException {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(2, 3, 3, 60);

        assertThat(statusOf(filter, "POST", "/api/auth/login")).isEqualTo(200);
        assertThat(statusOf(filter, "POST", "/api/auth/login")).isEqualTo(200);
        assertThat(statusOf(filter, "POST", "/api/auth/login")).isEqualTo(429);
    }

    @Test
    void differentPathsHaveSeparateBuckets() throws ServletException, IOException {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(1, 1, 1, 60);

        assertThat(statusOf(filter, "POST", "/api/auth/login")).isEqualTo(200);
        assertThat(statusOf(filter, "POST", "/api/auth/login")).isEqualTo(429);
        assertThat(statusOf(filter, "POST", "/api/auth/register")).isEqualTo(200);
    }

    @Test
    void nonAuthPathsAreNotLimited() throws ServletException, IOException {
        AuthRateLimitFilter filter = new AuthRateLimitFilter(0, 0, 0, 60);

        assertThat(statusOf(filter, "GET", "/api/products")).isEqualTo(200);
    }

    private int statusOf(AuthRateLimitFilter filter, String method, String path) throws ServletException, IOException {
        MockHttpServletRequest request = new MockHttpServletRequest(method, path);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain chain = new MockFilterChain();
        filter.doFilter(request, response, chain);
        return response.getStatus();
    }
}
