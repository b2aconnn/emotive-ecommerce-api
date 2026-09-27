package com.loopers.support.filter;

import java.io.IOException;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.stereotype.Component;

import com.loopers.support.resolver.UserContextHolder;

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
