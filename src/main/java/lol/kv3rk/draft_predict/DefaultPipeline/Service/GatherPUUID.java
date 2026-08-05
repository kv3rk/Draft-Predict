package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import lol.kv3rk.draft_predict.common.RiotDTO.LeagueEntryDTO;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotRequestParameters;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Component
public class GatherPUUID {

    private final WebClient euw1WebClient;
    private final WebClient naWebClient;
    private final WebClient krWebClient;
    private final WebClient euneWebClient;
    private final RiotRequestParameters riotRequestParameters;

    @Value("${api.key}")
    private String api_key;

    @Value("${api.request.delay}")
    private long request_delay;

    public GatherPUUID(
            @Qualifier("euw1WebClient") WebClient euw1WebClient,
            @Qualifier("naWebClient") WebClient naWebClient,
            @Qualifier("krWebClient") WebClient krWebClient,
            @Qualifier("euneWebClient") WebClient euneWebClient,
            RiotRequestParameters riotRequestParameters
    ) {
        this.euw1WebClient = euw1WebClient;
        this.naWebClient = naWebClient;
        this.krWebClient = krWebClient;
        this.euneWebClient = euneWebClient;
        this.riotRequestParameters = riotRequestParameters;
    }


    public Set<String> getSetOfEUWPlayers() throws InterruptedException {

        log.info("{} server", RiotServerName.EUW.name());

        return formatResponse(euw1WebClient);

    }

    public Set<String> getSetOfNAPlayers() throws InterruptedException {

        log.info("{} server", RiotServerName.NA.name());

        return formatResponse(naWebClient);

    }

    public Set<String> getSetOfKRPlayers() throws InterruptedException {

        log.info("{} server", RiotServerName.KR.name());

        return formatResponse(krWebClient);

    }

    public Set<String> getSetOfEUNEPlayers() throws InterruptedException {

        log.info("{} server", RiotServerName.EUNE.name());

        return formatResponse(euneWebClient);

    }


    private Set<String> formatResponse(WebClient platformWebClient) throws InterruptedException {

        List<String> tierParameters = riotRequestParameters.tierParameters();
        List<String> divisionParameters = riotRequestParameters.divisionParameters();
        int countPages = 1;

        Set<String> allPlayersPuuidFromServer = new LinkedHashSet<>();

        for (String tier : tierParameters) {

            division:
            for (String division : divisionParameters) {

                log.info("New division entry: {}", division);

                do {

                    int finalCountPages = countPages;

                    ResponseEntity<Set<LeagueEntryDTO>> response = platformWebClient
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
                            .block();

                    if (response == null) {

                        Thread.sleep(request_delay);
                        countPages++;
                        continue;
                    }

                    if (!response.getStatusCode().is2xxSuccessful()) {

                        Thread.sleep(request_delay);
                        countPages++;
                        continue;
                    }

                    if (response.getBody().isEmpty()) {

                        Thread.sleep(request_delay);
                        countPages = 1;
                        break division;
                    }

                    if (tier.equals("MASTER") && finalCountPages <= 40) {

                        Thread.sleep(request_delay);
                        countPages++;
                        continue;
                    }

                    Set<LeagueEntryDTO> setOfPuuid = response.getBody();

                    countPages++;

                    Set<String> puuids = getPUUIDForPlayers(setOfPuuid);

                    allPlayersPuuidFromServer.addAll(puuids);

                    log.info("{} {} {} {}", tier, division, finalCountPages, puuids);

                    Thread.sleep(request_delay);

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
