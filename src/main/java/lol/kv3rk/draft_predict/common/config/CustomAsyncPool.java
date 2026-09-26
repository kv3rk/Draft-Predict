package lol.kv3rk.draft_predict.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@EnableAsync
@Configuration
public class CustomAsyncPool {

    @Bean(name = "getCustomAsyncPool")
    public Executor getCustomAsyncPool() {

        ThreadPoolTaskExecutor customAsyncPool = new ThreadPoolTaskExecutor();
        customAsyncPool.setCorePoolSize(2);
        customAsyncPool.setMaxPoolSize(4);
        customAsyncPool.setQueueCapacity(8);
        customAsyncPool.setThreadNamePrefix("CustomAsyncPool-");
        customAsyncPool.initialize();

        return customAsyncPool;
    }
}
