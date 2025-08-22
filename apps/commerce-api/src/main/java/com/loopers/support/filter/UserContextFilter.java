package com.loopers.support.filter;

import com.loopers.support.resolver.UserContextHolder;
import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class UserContextFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String userId = httpServletRequest.getHeader("X-USER-ID");

        if (userId != null) {
            UserContextHolder.setUserId(userId);
        }

        try {
            chain.doFilter(request, response);
        } finally {
            UserContextHolder.clear();
        }
    }
}
