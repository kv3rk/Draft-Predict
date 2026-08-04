package lol.kv3rk.draft_predict.ClientApplication.DTO;

public record TopPerformingChampionsDTO(
        String champion,
        double pickRate,
        double winRate
) {
}