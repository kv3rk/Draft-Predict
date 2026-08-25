package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Service;

import jakarta.transaction.Transactional;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository.SystemRankedRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Comparator;
import java.util.List;

@Service
@Slf4j
public class SoloQDbRequestsService {

    private final SystemRankedRequests systemRankedRequests;

    public SoloQDbRequestsService(SystemRankedRequests systemRankedRequests) {
        this.systemRankedRequests = systemRankedRequests;
    }

    @Transactional
    public void refreshMaterializedViewRankedFlexibility() {
        systemRankedRequests.refreshFlexAvg();
        systemRankedRequests.refreshFlexStats();
        systemRankedRequests.refreshFlexAgg();
    }

    @Transactional
    public List<Integer> getSeasonList() {
        return systemRankedRequests.getPatchList().stream()
                .map(patch -> patch.split("\\.")[0])
                .distinct()
                .map(Integer::parseInt)
                .sorted(Comparator.reverseOrder())
                .toList();
    }

}