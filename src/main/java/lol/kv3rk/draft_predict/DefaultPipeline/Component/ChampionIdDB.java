package lol.kv3rk.draft_predict.DefaultPipeline.Component;

import lol.kv3rk.draft_predict.DefaultPipeline.DTO.GameDataDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;

@Component
public class ChampionIdDB {

    private final WebClient gameDataWebClient = WebClient
            .builder()
            .baseUrl("https://ddragon.leagueoflegends.com")
            .build();

    @Value("${api.patch}")
    private String api_patch;

    private Map<String, String> championAndIdsDB = new LinkedHashMap<>();

    public void populateChampionAndIdsDB() {

        GameDataDTO gameDataDTO = formatResponse(gameDataWebClient);

        System.out.println(gameDataDTO.version());

        gameDataDTO.data().forEach((name, championDTO) -> {

                    championAndIdsDB.put(championDTO.key(), name);

                }
        );

        System.out.println(championAndIdsDB);
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


/// cdn/16.15.1/data/en_US/champion.json