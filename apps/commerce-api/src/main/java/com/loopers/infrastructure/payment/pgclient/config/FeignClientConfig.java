package com.loopers.infrastructure.payment.pgclient.config;

import feign.Request;
import feign.RequestInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class FeignClientConfig {

    @Value("${pg.auth.client-id}")
    private String pgClientId;

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(1000, 3000); // 연결/응답 타임아웃 (ms)
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-USER-ID", pgClientId);
        };
    }
}
