package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampionsDTO;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Repository.ParticipantsRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientAppService {

    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;

    public ClientAppService(MatchesRepository matchesRepository,
                            ParticipantsRepository participantsRepository) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
    }

    public long countMatches() {

        return matchesRepository.countMatches();
    }

    public double actualPatch() {

        return matchesRepository.actualPatch();
    }

    public List<TopPerformingChampionsDTO> getTopPerformingChampions() {

        return participantsRepository.getTopPerformingChampions();
    }
}
