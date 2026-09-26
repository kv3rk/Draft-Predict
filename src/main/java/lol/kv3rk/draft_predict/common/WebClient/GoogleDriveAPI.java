package lol.kv3rk.draft_predict.common.WebClient;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Instant;
import java.util.Map;

@Configuration
public class GoogleDriveAPI {

    @Value("${api.google_drive.refresh_token}")
    private String refreshToken;

    @Value("${api.google_drive.client_id}")
    private String clientId;

    @Value("${api.google_drive.client_secret}")
    private String clientSecret;

    private volatile String cachedAccessToken;
    private volatile Instant tokenExpiry;

    @Bean
    public WebClient proSceneFilesMetadata(WebClient.Builder builder) {
        return builder
                .baseUrl("https://www.googleapis.com/drive/v3/files")
                .build();
    }

    @Bean
    public WebClient proSceneCopyFile(WebClient.Builder builder) {
        return builder
                .baseUrl("https://www.googleapis.com/drive/v3/files")
                .filter(oauth2Filter())
                .build();
    }

    @Bean
    public WebClient proSceneGetAndDeleteFile(WebClient.Builder builder) {
        return builder
                .baseUrl("https://www.googleapis.com/drive/v3/files")
                .filter(oauth2Filter())
                .build();
    }

    private ExchangeFilterFunction oauth2Filter() {
        return ExchangeFilterFunction.ofRequestProcessor(clientRequest ->
                getValidAccessToken()
                        .map(token -> ClientRequest.from(clientRequest)
                                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                                .build())
        );
    }

    private Mono<String> getValidAccessToken() {
        if (cachedAccessToken != null && tokenExpiry != null && Instant.now().isBefore(tokenExpiry)) {
            return Mono.just(cachedAccessToken);
        }
        return refreshAccessToken();
    }

    private Mono<String> refreshAccessToken() {
        WebClient tokenClient = WebClient.builder()
                .baseUrl("https://oauth2.googleapis.com")
                .build();

        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("refresh_token", refreshToken);
        formData.add("grant_type", "refresh_token");

        return tokenClient.post()
                .uri("/token")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .bodyValue(formData)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    String accessToken = (String) response.get("access_token");
                    Integer expiresIn = (Integer) response.get("expires_in");

                    cachedAccessToken = accessToken;
                    tokenExpiry = Instant.now().plusSeconds(expiresIn - 300);

                    return accessToken;
                });
    }
}