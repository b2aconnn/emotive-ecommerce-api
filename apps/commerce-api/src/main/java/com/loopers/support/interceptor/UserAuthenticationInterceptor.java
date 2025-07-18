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

    private final UserRepository userRepository;

    public UserAuthenticationInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String userIdHeader = request.getHeader("X-USER-ID");

        if (validateUserIdHeader(response, userIdHeader)) return false;
        if (rejectIfUserNotFound(response, userIdHeader)) return false;

        return true;
    }

    private boolean rejectIfUserNotFound(HttpServletResponse response, String userIdHeader) throws IOException {
        boolean userExists = userRepository.existsByUserId(userIdHeader);
        if (!userExists) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Invalid X-USER-ID");
            return true;
        }
        return false;
    }

    private static boolean validateUserIdHeader(HttpServletResponse response, String userIdHeader) throws IOException {
        if (userIdHeader == null || userIdHeader.isBlank()) {
            response.sendError(HttpStatus.BAD_REQUEST.value(), "Missing X-USER-ID header");
            return true;
        }
        return false;
    }
}
