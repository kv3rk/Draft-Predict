package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.Service;

import jakarta.transaction.Transactional;
import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.Repository.SystemRankedRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RankedSoloQService {
    private final SystemRankedRequests systemRankedRequests;

    public RankedSoloQService(SystemRankedRequests systemRankedRequests) {
        this.systemRankedRequests = systemRankedRequests;
    }

    @Transactional
    public void refreshMaterializedViewRankedFlexibility(){

        systemRankedRequests.refreshFlexAvg();
        systemRankedRequests.refreshFlexStats();
        systemRankedRequests.refreshFlexAgg();
    }
}
