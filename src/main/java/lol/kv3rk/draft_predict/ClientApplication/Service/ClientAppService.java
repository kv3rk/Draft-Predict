package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ClientApplication.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampionsDTO;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotRequestParameters;
import lol.kv3rk.draft_predict.common.RiotParametersDB.RiotServerName;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

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

        Optional<Long> amountMatches = matchesRepository.countMatches();

        AtomicLong actualAmountMatches = new AtomicLong();

        amountMatches.ifPresentOrElse(
                actualAmountMatches::set,
                () -> {
                    log.warn("Cannot count amount of matches");
                }
        );

        return actualAmountMatches.get();
    }

    public double actualPatch() {

        Optional<Double> patch = matchesRepository.actualPatch();

        AtomicReference<Double> actualPatch = new AtomicReference<>((double) 0);

        patch.ifPresentOrElse(
                actualPatch::set,
                () -> {
                    log.warn("Actual patch doesnt exists in db");
                }
        );

        return actualPatch.get();
    }

    public List<TopPerformingChampionsDTO> getTopPerformingChampions() {

        return participantsRepository.getTopPerformingChampions();
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
}
