package lol.kv3rk.draft_predict.TestApiEndpoints.Service;

import lol.kv3rk.draft_predict.TestApiEndpoints.DTO.FindSummonerDTO;
import lol.kv3rk.draft_predict.TestApiEndpoints.DTO.RiotDTO.AccountDTO;
import lol.kv3rk.draft_predict.common.WebClient.PlatformRoutingValues;
import lol.kv3rk.draft_predict.common.WebClient.RegionalRoutingValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Service
public class EndpointService {

    private final RegionalRoutingValues regionalRoutingValues;
    private final PlatformRoutingValues platformRoutingValues;

    @Value("${api.key}")
    private String api_key;

    @Autowired
    public EndpointService(
            RegionalRoutingValues regionalRoutingValues,
            PlatformRoutingValues platformRoutingValues
    ) {
        this.regionalRoutingValues = regionalRoutingValues;
        this.platformRoutingValues = platformRoutingValues;
    }

    public void definePUUID(FindSummonerDTO findSummonerDTO) {

        AccountDTO response = regionalRoutingValues.euWebClient(WebClient.builder())
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

        definePlayerStats(puuid);

    }

    public void definePlayerStats(String puuid) {

        List response = regionalRoutingValues.euWebClient(WebClient.builder())
                .get()
                .uri("/lol/match/v5/matches/by-puuid/"
                        + puuid +
                        "/ids?start=0&count=3&api_key="
                        + api_key)
                .retrieve()
                .bodyToMono(List.class)
                .block();

        System.out.println(response.toString());

        System.out.println(puuid);

    }


}
