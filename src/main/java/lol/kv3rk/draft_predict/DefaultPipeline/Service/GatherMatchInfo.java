package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import jakarta.transaction.Transactional;
import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Entity.BansEntity;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Entity.MatchesEntity;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Entity.ParticipantsEntity;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.common.RiotDTO.*;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
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

//        Thread.sleep(120000);

//        allMatchesIDsFromServer.forEach(matchID -> {
//
//                    formatResponse(euWebClient, matchID);
//
//                }
//        );

        for (int i = 0; i < 5; i++) {

            MatchDTO matchInfo = formatResponse(webClient, allMatchesIDsFromServer.get(i));
            saveMatchInfo.saveMatchInfo(matchInfo, server, allMatchesIDsFromServer.get(i));

        }

    }

    private MatchDTO formatResponse(WebClient regionalWebClient,
                                    String matchID) throws InterruptedException {

        MatchDTO matchInfo = regionalWebClient
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
                .block()
                .getBody();


        Thread.sleep(request_delay);

        return matchInfo;

    }

}
