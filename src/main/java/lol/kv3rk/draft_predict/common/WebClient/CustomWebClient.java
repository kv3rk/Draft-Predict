package lol.kv3rk.draft_predict.common.WebClient;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class CustomWebClient {

    @Bean
    public WebClient euWebClient(WebClient.Builder builder){

        return builder
                .baseUrl("https://europe.api.riotgames.com")
                .build();
    }

    @Bean
    public WebClient asiaWebClient(WebClient.Builder builder){

        return builder
                .baseUrl("https://asia.api.riotgames.com")
                .build();
    }

    @Bean
    public WebClient americasWebClient(WebClient.Builder builder){

        return builder
                .baseUrl("https://americas.api.riotgames.com")
                .build();
    }

    @Bean
    public WebClient seaWebClient(WebClient.Builder builder){

        return builder
                .baseUrl("https://sea.api.riotgames.com")
                .build();
    }
}
