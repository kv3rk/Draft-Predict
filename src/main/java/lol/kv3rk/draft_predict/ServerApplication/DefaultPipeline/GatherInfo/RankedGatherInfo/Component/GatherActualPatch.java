package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.RankedGatherInfo.Component;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;

@Component
public class GatherActualPatch {

    @Value("${api.patch}")
    private String api_patch;

    private final WebClient actualPatchWebClient = WebClient
            .builder()
            .baseUrl("https://ddragon.leagueoflegends.com")
            .build();

    public String getActualPatch() {

        List<String> allPatches = formatResponse(actualPatchWebClient);

        String actualPatch = api_patch;

        if (!allPatches.isEmpty()) {

            actualPatch = allPatches.getFirst();

        }

        return actualPatch;

    }

    public List<String> formatResponse(WebClient webClient) {

        ResponseEntity<List<String>> response = webClient
                .get()
                .uri(
                        "/api/versions.json"
                )
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<String>>() {
                })
                .block();

        if (response == null) {

            return List.of();
        }

        if (!response.getStatusCode().is2xxSuccessful()) {

            return List.of();
        }

        List<String> allPatches = response.getBody();

        return allPatches;
    }
}
