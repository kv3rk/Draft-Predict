package lol.kv3rk.draft_predict.ClientApplication.Service;

import lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO.Champion;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class ClientAppDraftService {

    private final ClientAppSoloqService clientAppSoloqService;

    public ClientAppDraftService(ClientAppSoloqService clientAppSoloqService) {
        this.clientAppSoloqService = clientAppSoloqService;
    }

    public List<Champion> getChampionList() {

        return clientAppSoloqService.getChampionList();
    }

    public List<String> getPatchList() {

        return clientAppSoloqService.getPatchList();
    }
}
