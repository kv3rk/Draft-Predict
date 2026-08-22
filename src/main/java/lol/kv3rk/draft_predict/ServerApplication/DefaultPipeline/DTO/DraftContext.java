package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DTO;

import java.util.Map;
import java.util.Set;

public record DraftContext(
        Set<String> excludedChampions
) {
}
