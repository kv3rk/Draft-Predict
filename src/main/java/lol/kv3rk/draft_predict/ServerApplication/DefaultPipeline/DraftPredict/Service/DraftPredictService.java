package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.DraftPredict.Service;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.RankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Participants.Repository.ParticipantsRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class DraftPredictService {
    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final BansRepository bansRepository;
    private final RankedRequests rankedRequests;

    public DraftPredictService(MatchesRepository matchesRepository,
                               ParticipantsRepository participantsRepository,
                               BansRepository bansRepository,
                               RankedRequests rankedRequests) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
        this.rankedRequests = rankedRequests;
    }

    public List<String> redSideFirstBan(){

        return List.of();
    }

    public List<String> blueSideFirstBan(){



        return List.of();
    }

    public List<String> redSideSecondBan(){

        return List.of();
    }

    public List<String> blueSideSecondBan(){

        return List.of();
    }

    public List<String> redSideThirdBan(){

        return List.of();
    }

    public List<String> blueSideThirdBan(){

        return List.of();
    }

    public List<String> redSideFirstPick(){

        return List.of();
    }

    public List<String> blueSideFirstPick(){

        return List.of();
    }
    public List<String> redSideSecondPick(){

        return List.of();
    }

    public List<String> blueSideSecondPick(){

        return List.of();
    }

}
