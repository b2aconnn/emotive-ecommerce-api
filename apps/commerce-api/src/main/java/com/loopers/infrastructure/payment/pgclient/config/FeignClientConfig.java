package com.loopers.infrastructure.payment.pgclient.config;

import com.loopers.support.resolver.UserContextHolder;
import feign.Request;
import feign.RequestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(1000, 3000); // 연결/응답 타임아웃 (ms)
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            String userId = UserContextHolder.getUserId();
            if (userId != null) {
                requestTemplate.header("X-USER-ID", userId);
            }
        };
    }
}
