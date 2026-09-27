package com.loopers.infrastructure.payment.pgclient.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import feign.Request;
import feign.RequestInterceptor;

@Configuration
public class FeignClientConfig {

    private static final int CONNECT_TIMEOUT_MILLIS = 1000;
    private static final int READ_TIMEOUT_MILLIS = 3000;

    @Value("${pg.auth.client-id}")
    private String pgClientId;

    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(CONNECT_TIMEOUT_MILLIS, READ_TIMEOUT_MILLIS);
    }

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {
            requestTemplate.header("X-USER-ID", pgClientId);
        };
    }
}
