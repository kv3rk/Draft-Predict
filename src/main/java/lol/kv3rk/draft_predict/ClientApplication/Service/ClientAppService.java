package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ClientApplication.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotRequestParameters;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;

@Service
@Slf4j
public class ClientAppService {

    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final BansRepository bansRepository;
    private final RiotRequestParameters riotRequestParameters;

    public ClientAppService(MatchesRepository matchesRepository,
                            ParticipantsRepository participantsRepository,
                            BansRepository bansRepository,
                            RiotRequestParameters riotRequestParameters) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
        this.riotRequestParameters = riotRequestParameters;
    }


    public long countMatches() {

        long actualAmountMatches = matchesRepository.countMatches().orElse(0L);

        return actualAmountMatches;
    }

    public double actualPatch() {

        double actualPatch = matchesRepository.actualPatch().orElse(0.0);

        return actualPatch;
    }

    public List<TopPerformingChampions> getTopPerformingChampionsByPickRate() {

        return participantsRepository.getTopPerformingChampionsByPickRate();
    }

    public List<MostBannedChampions> getMostBannedChampions() {

        return bansRepository.getMostBannedChampions();
    }

    public List<String> getTierParameters() {

        return riotRequestParameters.tierParameters();
    }

    public List<String> getRiotServerName() {

        List<String> riotServerName = Arrays.stream(RiotServerName.values())
                .map(Enum::name)
                .toList();

        return riotServerName;
    }

    public String lastTimeUpdate() {

        String lastTimeUpdate = matchesRepository.getDateOfLastMatch().map(
                LocalDate::toString
        ).orElse("none");

        return lastTimeUpdate;
    }
}
