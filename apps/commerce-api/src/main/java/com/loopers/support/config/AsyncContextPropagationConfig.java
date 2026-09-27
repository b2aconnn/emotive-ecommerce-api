package com.loopers.support.config;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import com.loopers.support.resolver.UserContextHolder;

@Configuration
@EnableAsync
public class AsyncContextPropagationConfig implements AsyncConfigurer {

    private static final int CORE_POOL_SIZE = 10;
    private static final int MAX_POOL_SIZE = 20;
    private static final int QUEUE_CAPACITY = 500;

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(CORE_POOL_SIZE);
        executor.setMaxPoolSize(MAX_POOL_SIZE);
        executor.setQueueCapacity(QUEUE_CAPACITY);
        executor.setThreadNamePrefix("async-task-");
        executor.setTaskDecorator(new UserContextTaskDecorator());
        executor.initialize();
        return executor;
    }

    private static class UserContextTaskDecorator implements TaskDecorator {
        @Override
        public Runnable decorate(Runnable runnable) {
            String parentUserId = UserContextHolder.getUserId();

            return () -> {
                try {
                    if (parentUserId != null) {
                        UserContextHolder.setUserId(parentUserId);
                    }
                    runnable.run();
                } finally {
                    UserContextHolder.clear();
                }
            };
        }
    }
}
