package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import lol.kv3rk.draft_predict.common.DateAndTime.CustomLocalDateAndTime;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
public class GatherMatchIDs {

    @Value("${api.key}")
    private String api_key;

    @Value("${api.request.delay}")
    private long request_delay;

    @Value("${api.queue.id}")
    private int queue_id;

    private final WebClient euWebClient;
    private final WebClient americasWebClient;
    private final WebClient asiaWebClient;
    private final GatherPUUID gatherPUUID;
    private final CustomLocalDateAndTime customLocalDateAndTime;

    public GatherMatchIDs(
            @Qualifier("euWebClient") WebClient euWebClient,
            @Qualifier("americasWebClient") WebClient americasWebClient,
            @Qualifier("asiaWebClient") WebClient asiaWebClient,
            GatherPUUID gatherPUUID,
            CustomLocalDateAndTime customLocalDateAndTime
    ) {
        this.euWebClient = euWebClient;
        this.americasWebClient = americasWebClient;
        this.asiaWebClient = asiaWebClient;
        this.gatherPUUID = gatherPUUID;
        this.customLocalDateAndTime = customLocalDateAndTime;
    }

    public Set<String> getSetOfEUWMatchesIDs() throws InterruptedException {

        List<String> allPlayersPuuidFromServer = List.copyOf(gatherPUUID.getSetOfEUWPlayers());

        return getMatchIDs(euWebClient, allPlayersPuuidFromServer);

    }

    public Set<String> getSetOfNAMatchesIDs() throws InterruptedException {

        List<String> allPlayersPuuidFromServer = List.copyOf(gatherPUUID.getSetOfNAPlayers());

        return getMatchIDs(americasWebClient, allPlayersPuuidFromServer);

    }

    public Set<String> getSetOfKRMatchesIDs() throws InterruptedException {

        List<String> allPlayersPuuidFromServer = List.copyOf(gatherPUUID.getSetOfKRPlayers());

        return getMatchIDs(asiaWebClient, allPlayersPuuidFromServer);

    }

    public Set<String> getSetOfEUNEMatchesIDs() throws InterruptedException {

        List<String> allPlayersPuuidFromServer = List.copyOf(gatherPUUID.getSetOfEUNEPlayers());

        return getMatchIDs(euWebClient, allPlayersPuuidFromServer);

    }


    private Set<String> getMatchIDs(WebClient webClient,
                                    List<String> allPlayersPuuidFromServer) throws InterruptedException {


//        Thread.sleep(120000);

        Set<String> allMatchesIDs = new LinkedHashSet<>();

//        allPlayersPuuidFromServer.forEach(puuid -> {
//
//            try {
//
//                List<String> matchesIDs = formatResponse(euWebClient, puuid);
//
//                allMatchesIDs.addAll(matchesIDs);
//
//            } catch (InterruptedException e) {
//
//                throw new RuntimeException(e);
//
//            }
//
//        });

        for (int i = 0; i < 10; i++) {

            try {

                List<String> matchesIDs = formatResponse(webClient, allPlayersPuuidFromServer.get(i));

                allMatchesIDs.addAll(matchesIDs);

            } catch (InterruptedException e) {

                throw new RuntimeException(e);

            }

        }

        return allMatchesIDs;

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
                                    .queryParam("queue", queue_id)
                                    .queryParam("type", "ranked")
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

        log.info("{}: {}", puuid, matchesIDs);

        Thread.sleep(request_delay);

        return matchesIDs;


    }
}
