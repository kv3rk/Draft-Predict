package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ClientApplication.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampionsDTO;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Repository.ParticipantsRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClientAppService {

    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final BansRepository bansRepository;

    public ClientAppService(MatchesRepository matchesRepository,
                            ParticipantsRepository participantsRepository,
                            BansRepository bansRepository) {

        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
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

    public List<MostBannedChampions> getMostBannedChampions() {

        return bansRepository.getMostBannedChampions();
    }
}
