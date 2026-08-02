package lol.kv3rk.draft_predict.DefaultPipeline.DTO;

import java.util.Map;

public record GameDataDTO(
        String version,
        Map<String, ChampionDTO> data
) {
}
