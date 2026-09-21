package com.loopers.support.interceptor;

import com.loopers.domain.user.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;

@Component
public class UserAuthenticationInterceptor implements HandlerInterceptor {

    private static final String SIGNUP_PATH = "/api/users";

    private final UserRepository userRepository;

    public UserAuthenticationInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        if (isSignupRequest(request)) {
            return true;
        }

        String userIdHeader = request.getHeader("X-USER-ID");

        if (userIdHeader == null || userIdHeader.isBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing X-USER-ID header");
            return false;
        }

        Long userId = parseUserId(userIdHeader);
        if (userId == null) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Invalid X-USER-ID");
            return false;
        }

        if (!userRepository.existsById(userId)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid X-USER-ID");
            return false;
        }

        return true;
    }

    private Long parseUserId(String userIdHeader) {
        try {
            return Long.valueOf(userIdHeader);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private boolean isSignupRequest(HttpServletRequest request) {
        return "POST".equalsIgnoreCase(request.getMethod()) && SIGNUP_PATH.equals(request.getRequestURI());
    }
}
