package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO;

public record OracleElexirTeamInfo(
        String gameId,
        String league,
        String teamName,
        String side,
        String firstPick,
        String result
) {}
