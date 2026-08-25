package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.SoloqGatherInfo.DTO;

import java.util.Map;

public record GameDataDTO(
        String version,
        Map<String, ChampionDTO> data
) {
}
