package com.loopers.support.config;

import com.loopers.support.resolver.UserContextHolder;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskDecorator;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncContextPropagationConfig implements AsyncConfigurer {

    @Override
    public Executor getAsyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(500);
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

