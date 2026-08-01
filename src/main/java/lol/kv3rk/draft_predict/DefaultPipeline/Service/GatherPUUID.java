package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import lol.kv3rk.draft_predict.common.RiotDTO.LeagueEntryDTO;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotParameters;
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
public class GatherPUUID {

    private final WebClient euw1WebClient;
    private final WebClient naWebClient;
    private final WebClient krWebClient;
    private final WebClient euneWebClient;
    private final RiotParameters riotParameters;

    @Value("${api.key}")
    private String api_key;

    public GatherPUUID(
            @Qualifier("euw1WebClient") WebClient euw1WebClient,
            @Qualifier("naWebClient") WebClient naWebClient,
            @Qualifier("krWebClient") WebClient krWebClient,
            @Qualifier("euneWebClient") WebClient euneWebClient,
            RiotParameters riotParameters
    ) {
        this.euw1WebClient = euw1WebClient;
        this.naWebClient = naWebClient;
        this.krWebClient = krWebClient;
        this.euneWebClient = euneWebClient;
        this.riotParameters = riotParameters;
    }


    public Set<String> getSetOfEUWPlayers() {

        return formatResponse(euw1WebClient);

    }
//
//
//    public void getSetOfNAPlayers() {
//
//        formatResponse(naWebClient);
//
//    }
//
//    public void getSetOfKRPlayers() {
//
//        formatResponse(krWebClient);
//
//    }
//
//    public void getSetOfEUNEPlayers() {
//
//        formatResponse(euneWebClient);
//
//    }


    private Set<String> formatResponse(WebClient platformWebClient) {

        List<String> tierParameters = riotParameters.tierParameters();
        List<String> divisionParameters = riotParameters.divisionParameters();
        int countPages = 1;

        Set<String> allPlayersPuuidFromServer = new LinkedHashSet<>();


        for (String tier : tierParameters) {

            division:
            for (String division : divisionParameters) {

                do {

                    int finalCountPages = countPages;
                    Set<LeagueEntryDTO> response = platformWebClient
                            .get()
                            .uri(
                                    "/lol/league-exp/v4/entries/RANKED_SOLO_5x5",
                                    (UriBuilder) -> {
                                        ;
                                        return URI.create(UriBuilder
                                                .pathSegment(tier)
                                                .pathSegment(division)
                                                .queryParam("page", String.valueOf(finalCountPages))
                                                .queryParam("api_key", api_key)
                                                .toUriString());
                                    }
                            )
                            .retrieve()
                            .toEntity(new ParameterizedTypeReference<Set<LeagueEntryDTO>>() {
                            })
                            .block().getBody();

                    if (response.isEmpty()) {

                        countPages = 1;

                        break division;

                    }

                    countPages++;

                    Set<String> puuids = getPUUIDForPlayers(response);

                    allPlayersPuuidFromServer.addAll(puuids);

                } while (true);


            }
        }

        return allPlayersPuuidFromServer;

    }


    private Set<String> getPUUIDForPlayers(Set<LeagueEntryDTO> setOfPlayers) {

        Set<String> puuids = new LinkedHashSet<>();

        setOfPlayers.forEach(leagueEntryDTO -> {

            String puuid = leagueEntryDTO.puuid();
            puuids.add(puuid);

        });

        return puuids;
    }


}
