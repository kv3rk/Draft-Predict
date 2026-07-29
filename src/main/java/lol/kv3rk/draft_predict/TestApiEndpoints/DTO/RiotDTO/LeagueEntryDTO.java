package lol.kv3rk.draft_predict.TestApiEndpoints.DTO.RiotDTO;

public record LeagueEntryDTO(

        String leagueId,
        String puuid,
        String queueType,

        String tier,
        String rank,

        int leaguePoints,
        int wins,
        int losses,

        boolean hotStreak,
        int veteran,
        int freshBlood,
        boolean inactive,

        MiniSeriesDTO miniSeries

        ) {
}
