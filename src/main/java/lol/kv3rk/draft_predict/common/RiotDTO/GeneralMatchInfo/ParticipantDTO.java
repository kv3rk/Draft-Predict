package lol.kv3rk.draft_predict.common.RiotDTO.GeneralMatchInfo;

public record ParticipantDTO(

        String championName,
        String teamPosition,
        boolean win,
        int teamId

) {
}
