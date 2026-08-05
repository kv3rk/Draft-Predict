package lol.kv3rk.draft_predict.common.RiotDTO;

import java.util.List;

public record InfoDTO(

        String gameVersion,
        long gameCreation,
        List<ParticipantDTO> participants,
        List<TeamDTO> teams

) {
}
