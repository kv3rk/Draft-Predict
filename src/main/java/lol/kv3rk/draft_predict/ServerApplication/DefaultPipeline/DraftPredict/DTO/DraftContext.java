package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.DTO;

import java.util.Set;

public record DraftContext(
        Set<String> excludedChampions
) {
}
