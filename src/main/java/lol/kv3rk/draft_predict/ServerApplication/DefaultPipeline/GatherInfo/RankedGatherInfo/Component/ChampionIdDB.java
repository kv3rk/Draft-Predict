package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Component;

import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.DTO.GameDataDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
@Slf4j
public class ChampionIdDB {

    private final WebClient gameDataWebClient = WebClient
            .builder()
            .baseUrl("https://ddragon.leagueoflegends.com")
            .build();

    private final GatherActualPatch gatherActualPatch;

    private String api_patch;

    private Map<String, String> championAndIdsDB = new LinkedHashMap<>();

    public ChampionIdDB(GatherActualPatch gatherActualPatch) {
        this.gatherActualPatch = gatherActualPatch;
    }

    public void populateChampionAndIdsDB() throws InterruptedException {

        api_patch = gatherActualPatch.getActualPatch();

        Thread.sleep(10000);

        GameDataDTO gameDataDTO = formatResponse(gameDataWebClient);

        log.info(gameDataDTO.version());

        gameDataDTO.data().forEach((name, championDTO) -> {

                    championAndIdsDB.put(championDTO.key(), name);

                }
        );

        log.info(championAndIdsDB.toString());
    }

    public String mapChampionIdToName(int championId) {

        String name = championAndIdsDB.get(String.valueOf(championId));

        return name;

    }

    private GameDataDTO formatResponse(WebClient webClient) {

        GameDataDTO gameDataDTO = webClient
                .get()
                .uri(
                        "/cdn",
                        (URIBuilder) -> {
                            ;
                            return URI.create(URIBuilder
                                    .pathSegment(api_patch)
                                    .pathSegment("data")
                                    .pathSegment("en_US")
                                    .pathSegment("champion.json")
                                    .toUriString()
                            );

                        }
                )
                .retrieve()
                .toEntity(GameDataDTO.class)
                .block()
                .getBody();

        return gameDataDTO;

    }
}