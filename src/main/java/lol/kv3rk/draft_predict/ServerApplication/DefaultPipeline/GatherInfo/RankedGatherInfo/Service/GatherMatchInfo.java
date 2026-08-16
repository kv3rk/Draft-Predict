package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Service;

import lol.kv3rk.draft_predict.common.RiotDTO.InfoDTO;
import lol.kv3rk.draft_predict.common.RiotDTO.MatchDTO;
import lol.kv3rk.draft_predict.common.RiotDTO.TimeLine.InfoTimeLineDTO;
import lol.kv3rk.draft_predict.common.RiotDTO.TimeLine.TimelineDTO;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

@Slf4j
@Service
public class GatherMatchInfo {

    private final GatherMatchIDs gatherMatchIDs;
    private final WebClient euWebClient;
    private final WebClient americasWebClient;
    private final WebClient asiaWebClient;
    private final SaveMatchInfo saveMatchInfo;

    @Value("${api.key}")
    private String api_key;

    @Value("${api.request.delay}")
    private long request_delay;

    public GatherMatchInfo(
            GatherMatchIDs gatherMatchIDs,
            @Qualifier("euWebClient") WebClient euWebClient,
            @Qualifier("americasWebClient") WebClient americasWebClient,
            @Qualifier("asiaWebClient") WebClient asiaWebClient,
            SaveMatchInfo saveMatchInfo
    ) {
        this.gatherMatchIDs = gatherMatchIDs;
        this.euWebClient = euWebClient;
        this.americasWebClient = americasWebClient;
        this.asiaWebClient = asiaWebClient;
        this.saveMatchInfo = saveMatchInfo;
    }

    public void getEUWMatchInfo() throws InterruptedException {

        List<String> allMatchesIDsFromServer = List.copyOf(gatherMatchIDs.getSetOfEUWMatchesIDs());

        getMatchInfo(euWebClient,
                allMatchesIDsFromServer,
                RiotServerName.EUW.name());

    }

    public void getNAMatchInfo() throws InterruptedException {

        List<String> allMatchesIDsFromServer = List.copyOf(gatherMatchIDs.getSetOfNAMatchesIDs());

        getMatchInfo(americasWebClient,
                allMatchesIDsFromServer,
                RiotServerName.NA.name());

    }

    public void getKRMatchInfo() throws InterruptedException {

        List<String> allMatchesIDsFromServer = List.copyOf(gatherMatchIDs.getSetOfKRMatchesIDs());

        getMatchInfo(asiaWebClient,
                allMatchesIDsFromServer,
                RiotServerName.KR.name());

    }

    public void getEUNEMatchInfo() throws InterruptedException {

        List<String> allMatchesIDsFromServer = List.copyOf(gatherMatchIDs.getSetOfEUNEMatchesIDs());

        getMatchInfo(euWebClient,
                allMatchesIDsFromServer,
                RiotServerName.EUNE.name());

    }


    private void getMatchInfo(WebClient webClient,
                              List<String> allMatchesIDsFromServer,
                              String server) throws InterruptedException {

        log.info(allMatchesIDsFromServer.toString());

        Thread.sleep(10000);

        allMatchesIDsFromServer.forEach(matchID -> {

                    try {

                        MatchDTO matchInfo = formatMatchInfoResponse(webClient, matchID);
                        TimelineDTO timeLineMatchInfo = formatMatchTimeLineInfoResponse(
                                webClient, matchID
                        );
                        if (!(matchInfo.info().gameVersion().isEmpty() ||
                                timeLineMatchInfo.info().frames().isEmpty())) {

                            saveMatchInfo.saveMatchInfo(matchInfo, server, matchID, timeLineMatchInfo);
                        }

                    } catch (InterruptedException e) {

                        Thread.currentThread().interrupt();
                        throw new RuntimeException(e);
                    }

                }
        );

    }

    private MatchDTO formatMatchInfoResponse(WebClient regionalWebClient,
                                             String matchID) throws InterruptedException {

        ResponseEntity<MatchDTO> response = regionalWebClient
                .get()
                .uri(
                        "/lol/match/v5/matches",
                        (URIBuilder) -> {
                            ;
                            return URI.create(URIBuilder
                                    .pathSegment(matchID)
                                    .queryParam("api_key", api_key)
                                    .toUriString()
                            );

                        }
                )
                .retrieve()
                .toEntity(MatchDTO.class)
                .block();

        if (response == null) {

            Thread.sleep(request_delay);
            return new MatchDTO(new InfoDTO("", 0, List.of(), List.of()));
        }

        if (!response.getStatusCode().is2xxSuccessful()) {

            Thread.sleep(request_delay);
            return new MatchDTO(new InfoDTO("", 0, List.of(), List.of()));
        }

        MatchDTO matchInfo = response.getBody();

        Thread.sleep(request_delay);

        return matchInfo;

    }

    private TimelineDTO formatMatchTimeLineInfoResponse(WebClient regionalWebClient,
                                                        String matchID) throws InterruptedException {


        ResponseEntity<TimelineDTO> response = regionalWebClient
                .get()
                .uri(
                        "/lol/match/v5/matches",
                        (uriBuilder) -> {

                            return URI.create(uriBuilder
                                    .pathSegment(matchID)
                                    .pathSegment("timeline")
                                    .queryParam("api_key", api_key)
                                    .toUriString());
                        }
                )
                .retrieve()
                .toEntity(TimelineDTO.class)
                .block();

        if (response == null) {

            Thread.sleep(request_delay);
            return new TimelineDTO(new InfoTimeLineDTO(List.of()));
        }

        if (!response.getStatusCode().is2xxSuccessful()){

            Thread.sleep(request_delay);
            return new TimelineDTO(new InfoTimeLineDTO(List.of()));
        }


        TimelineDTO timelineDTO = response.getBody();

        Thread.sleep(request_delay);

        return timelineDTO;
    }

}
