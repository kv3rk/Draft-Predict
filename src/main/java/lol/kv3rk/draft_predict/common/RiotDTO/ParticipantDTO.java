package lol.kv3rk.draft_predict.common.RiotDTO;

public record ParticipantDTO(

        String championName,
        String teamPosition,
        boolean win,
        int teamId

) {
}
