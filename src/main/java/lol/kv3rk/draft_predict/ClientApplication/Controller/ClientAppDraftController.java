package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.DTO.Draft.MatchSetupInfoDTO;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppDraftService;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppSoloqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/draft-predict")
@Slf4j
public class ClientAppDraftController {

    private final ClientAppDraftService clientAppDraftService;

    public ClientAppDraftController(ClientAppDraftService clientAppDraftService) {
        this.clientAppDraftService = clientAppDraftService;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute(
                "patchList", clientAppDraftService.getPatchList());
        model.addAttribute(
                "championList", clientAppDraftService.getChampionList());
    }

    //-------------- Page Endpoints --------------

    @GetMapping("/main")
    public String getMainPage(Model model) {
        log.info("Entered [/draft-predict/main] endpoint");
        addCommonAttributes(model);
        return "draft-predict/draft-predict";
    }


    //-------------- Info Endpoints --------------

    @PostMapping("/setup/match/info")
    @ResponseBody
    public ResponseEntity<Map<String, String>> setupMatchInfo(@RequestBody MatchSetupInfoDTO matchSetupInfoDTO) {
        log.info("Entered [/draft-predict/setup/match/info] endpoint");
        log.info("Patch: {}, FirstPickSide: {}", matchSetupInfoDTO.patch(), matchSetupInfoDTO.firstPickSide());

        Map<String, String> response = new HashMap<>();
        response.put("status", "success");
        response.put("patch", matchSetupInfoDTO.patch());
        response.put("firstPickSide", matchSetupInfoDTO.firstPickSide());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/ban/recommendations")
    @ResponseBody
    public ResponseEntity<List<String>> getBanRecommendations(
            @RequestBody Map<String, List<String>> request) {

        log.info("Entered [/draft-predict/ban/recommendations] endpoint");

        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());

        List<String> recommendations = clientAppDraftService.getBanRecommendations(blueSideBans, redSideBans);

        return ResponseEntity.ok(recommendations);
    }
}
