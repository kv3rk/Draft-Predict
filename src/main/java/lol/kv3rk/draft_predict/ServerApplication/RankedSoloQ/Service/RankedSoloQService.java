package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.Service;

import jakarta.transaction.Transactional;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.SystemRankedRequests;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class RankedSoloQService {

    private final SystemRankedRequests systemRankedRequests;
    private final MatchesRepository matchesRepository;

    public RankedSoloQService(SystemRankedRequests systemRankedRequests,
                              MatchesRepository matchesRepository) {
        this.systemRankedRequests = systemRankedRequests;
        this.matchesRepository = matchesRepository;
    }

    @Transactional
    public void refreshMaterializedViewRankedFlexibility() {
        systemRankedRequests.refreshFlexAvg();
        systemRankedRequests.refreshFlexStats();
        systemRankedRequests.refreshFlexAgg();
    }

    // Get list of unique seasons extracted from patch list (e.g. "16.12" -> "16")
    @Transactional
    public List<String> getSeasonList() {
        return matchesRepository.getPatchList()
                .stream()
                .map(patch -> patch.split("\\.")[0])
                .distinct()
                .toList();
    }

    // Get list of all patches from database
    @Transactional
    public List<String> getPatchList() {
        return matchesRepository.getPatchList();
    }
}