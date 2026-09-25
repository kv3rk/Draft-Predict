package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO;

import java.time.LocalDate;

public record OracleElexirMatchInfo(
        String gameId,
        String league,
        LocalDate date,
        String patch
) {
}
