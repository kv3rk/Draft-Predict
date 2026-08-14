package lol.kv3rk.draft_predict.RankedSoloQ.Service;

import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.Repository.RankedRequests;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RankedSoloQService {
    private final RankedRequests rankedRequests;

    public RankedSoloQService(RankedRequests rankedRequests) {
        this.rankedRequests = rankedRequests;
    }
}
