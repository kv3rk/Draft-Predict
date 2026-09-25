package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO;

public record OracleElexirPickInfo(
        String teamName,
        String champion,
        String position,
        String playerName,
        String playerId,
        String goldDiffAt15,
        String xpDiffAt15,
        String csDiffAt15,
        Integer pickOrder
) {
}
