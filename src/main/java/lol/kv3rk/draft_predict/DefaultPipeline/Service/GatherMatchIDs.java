package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import lol.kv3rk.draft_predict.common.DateAndTime.CustomLocalDateAndTime;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Component
public class GatherMatchIDs {

    @Value("${api.key}")
    private String api_key;

    @Value("${api.request.delay}")
    private long request_delay;

    private final WebClient euWebClient;
    private final GatherPUUID gatherPUUID;
    private final CustomLocalDateAndTime customLocalDateAndTime;

    public GatherMatchIDs(
            @Qualifier("euWebClient") WebClient euWebClient,
            GatherPUUID gatherPUUID,
            CustomLocalDateAndTime customLocalDateAndTime
    ) {
        this.euWebClient = euWebClient;
        this.gatherPUUID = gatherPUUID;
        this.customLocalDateAndTime = customLocalDateAndTime;
    }

    public void getMatchIDs() throws InterruptedException {

        Set<String> allPlayersPuuidFromServer = gatherPUUID.getSetOfEUWPlayers();

        Thread.sleep(120000);

        Set<String> allMatchesIDs = new LinkedHashSet<>();

        allPlayersPuuidFromServer.forEach(puuid -> {

            try {

                List<String> matchesIDs = formatResponse(euWebClient, puuid);

                allMatchesIDs.addAll(matchesIDs);

            } catch (InterruptedException e) {

                throw new RuntimeException(e);

            }

        });

        System.out.println(allMatchesIDs.toString());

    }

    private List<String> formatResponse(WebClient regionalWebClient, String puuid) throws InterruptedException {

        List<String> matchesIDs = regionalWebClient
                .get()
                .uri(
                        "/lol/match/v5/matches/by-puuid",
                        (URIBuilder) -> {
                            ;
                            return URI.create(URIBuilder
                                    .pathSegment(puuid)
                                    .pathSegment("ids")
                                    .queryParam("startTime", customLocalDateAndTime.startTime())
                                    .queryParam("endTime", customLocalDateAndTime.endTime())
                                    .queryParam("api_key", api_key)
                                    .toUriString()
                            );

                        }
                )
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<String>>() {
                })
                .block()
                .getBody();


        System.out.println(puuid + ": " + matchesIDs);

        Thread.sleep(request_delay);

        return matchesIDs;


    }
}
