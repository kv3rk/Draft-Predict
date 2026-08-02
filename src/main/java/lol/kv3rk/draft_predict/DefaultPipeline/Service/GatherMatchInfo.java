package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.common.RiotDTO.*;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.List;

@Service
public class GatherMatchInfo {

    private final GatherMatchIDs gatherMatchIDs;
    private final WebClient euWebClient;
    private final WebClient americasWebClient;
    private final WebClient asiaWebClient;
    private final ChampionIdDB championIdDB;

    @Value("${api.key}")
    private String api_key;

    @Value("${api.request.delay}")
    private long request_delay;

    public GatherMatchInfo(
            GatherMatchIDs gatherMatchIDs,
            @Qualifier("euWebClient") WebClient euWebClient,
            @Qualifier("americasWebClient") WebClient americasWebClient,
            @Qualifier("asiaWebClient") WebClient asiaWebClient,
            ChampionIdDB championIdDB
    ) {
        this.gatherMatchIDs = gatherMatchIDs;
        this.euWebClient = euWebClient;
        this.americasWebClient = americasWebClient;
        this.asiaWebClient = asiaWebClient;
        this.championIdDB = championIdDB;
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


        System.out.println(allMatchesIDsFromServer.toString());

//        Thread.sleep(120000);

//        allMatchesIDsFromServer.forEach(matchID -> {
//
//                    formatResponse(euWebClient, matchID);
//
//                }
//        );

        for (int i = 0; i < 5; i++) {

            formatResponse(webClient, allMatchesIDsFromServer.get(i), server);

        }

    }

    private void formatResponse(WebClient regionalWebClient,
                                String matchID,
                                String server) throws InterruptedException {

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

        System.out.println("--------------------------------------");
        String gameVersion = extractGameVersion(matchInfo.info());
        System.out.println("Game version: " + gameVersion.substring(0, 5));
        System.out.println("Match server: " + server);

        List<ParticipantDTO> participantDTOList = extractParticipantDTOList(matchInfo.info());
        participantDTOList.forEach(participantDTO -> {

                    String championName = participantDTO.championName();
                    String lane = participantDTO.teamPosition();
                    boolean win = participantDTO.win();

                    System.out.println(championName + " - " + lane + ": " + win);

                }

        );

        List<TeamDTO> teamDTOList = extractTeamDTOList(matchInfo.info());
        teamDTOList.forEach(teamDTO -> {

                    List<BanDTO> banDTOList = extractBanDTOList(teamDTO);
                    banDTOList.forEach(banDTO -> {

                                int championId = banDTO.championId();
                                String championName = championIdDB.mapChampionIdToName(championId);

                                System.out.print(championName + " ");

                            }

                    );
                    System.out.println();
                }

        );
        System.out.println("--------------------------------------");

        Thread.sleep(request_delay);


    }

    private String extractGameVersion(InfoDTO infoDTO) {

        return infoDTO.gameVersion();

    }

    private List<ParticipantDTO> extractParticipantDTOList(InfoDTO infoDTO) {

        return infoDTO.participants();

    }

    private List<TeamDTO> extractTeamDTOList(InfoDTO infoDTO) {

        return infoDTO.teams();

    }

    private List<BanDTO> extractBanDTOList(TeamDTO teamDTO) {

        return teamDTO.bans();
    }


}
