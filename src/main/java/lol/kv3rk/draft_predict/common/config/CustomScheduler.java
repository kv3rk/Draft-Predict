package lol.kv3rk.draft_predict.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;

@EnableScheduling
@Configuration
public class CustomScheduler {

    @Bean
    public ThreadPoolTaskScheduler getCustomScheduler() {

        ThreadPoolTaskScheduler customScheduler = new ThreadPoolTaskScheduler();
        customScheduler.setPoolSize(2);
        customScheduler.setThreadNamePrefix("Scheduler-");
        customScheduler.initialize();

        return customScheduler;

    }

}
