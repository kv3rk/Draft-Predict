package lol.kv3rk.draft_predict.TestApiEndpoints.Service;

import lol.kv3rk.draft_predict.TestApiEndpoints.DTO.FindSummonerDTO;
import lol.kv3rk.draft_predict.TestApiEndpoints.DTO.RiotDTO.AccountDTO;
import lol.kv3rk.draft_predict.common.WebClient.CustomWebClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
public class EndpointService {

    private final CustomWebClient customWebClient;

    @Value("${api.key}")
    private String api_key;

    public EndpointService(
            CustomWebClient customWebClient
    ) {
        this.customWebClient = customWebClient;
    }

    public String definePUUID(FindSummonerDTO findSummonerDTO) {

        AccountDTO response = customWebClient.euWebClient(WebClient.builder())
                .get()
                .uri("/riot/account/v1/accounts/by-riot-id/" +
                        findSummonerDTO.name() + "/"
                        + findSummonerDTO.tagLine() +
                        "?api_key=" +
                        api_key
                )
                .retrieve()
                .bodyToMono(AccountDTO.class)
                .block();

        String puuid = response.puuid();

        return puuid;

    }




}
