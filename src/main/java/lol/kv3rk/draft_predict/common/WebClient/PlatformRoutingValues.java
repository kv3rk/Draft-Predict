package lol.kv3rk.draft_predict.common.WebClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class PlatformRoutingValues {

    @Bean
    public WebClient euw1WebClient(WebClient.Builder builder){
        return builder
                .baseUrl("https://euw1.api.riotgames.com")
                .build();
    }

    @Bean
    public WebClient naWebClient(WebClient.Builder builder){
        return builder
                .baseUrl("https://na1.api.riotgames.com")
                .build();
    }

    @Bean
    public WebClient krWebClient(WebClient.Builder builder){
        return builder
                .baseUrl("https://kr.api.riotgames.com")
                .build();
    }

    @Bean
    public WebClient euneWebClient(WebClient.Builder builder){
        return builder
                .baseUrl("https://eun1.api.riotgames.com")
                .build();
    }
}
