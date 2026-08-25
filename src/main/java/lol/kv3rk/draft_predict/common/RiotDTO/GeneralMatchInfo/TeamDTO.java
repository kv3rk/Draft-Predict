package lol.kv3rk.draft_predict.common.RiotDTO.GeneralMatchInfo;

import java.util.List;

public record TeamDTO(

        List<BanDTO> bans,
        int teamId

) {
}
