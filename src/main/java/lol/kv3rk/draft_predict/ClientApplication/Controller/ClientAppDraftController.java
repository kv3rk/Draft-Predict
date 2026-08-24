package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.DTO.Draft.MatchSetupInfoDTO;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppDraftService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
        model.addAttribute("patchList", clientAppDraftService.getPatchList());
        model.addAttribute("championList", clientAppDraftService.getChampionList());
        model.addAttribute("seasonList", clientAppDraftService.getSeasonList());
        model.addAttribute("actualPatch", clientAppDraftService.getPatchList().getLast());
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
        log.info("Season: {}, Patch: {}, FirstPickSide: {}",
                matchSetupInfoDTO.season(), matchSetupInfoDTO.patch(), matchSetupInfoDTO.firstPickSide());
        // Service already returns complete response with firstPickSide included
        Map<String, String> response = clientAppDraftService.setupMatchInfo(matchSetupInfoDTO);
        return ResponseEntity.ok(response);
    }

    // ================= DRAFT RECOMMENDATION ENDPOINTS =================
    @PostMapping("/blue-side-ban/recommendations")
    @ResponseBody
    public ResponseEntity<List<String>> getBlueSideBanRecommendations(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/blue-side-ban/recommendations] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());

        List<String> recommendations = clientAppDraftService.getBlueSideBanRecommendations(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/red-side-ban/recommendations")
    @ResponseBody
    public ResponseEntity<List<String>> getRedSideBanRecommendations(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/red-side-ban/recommendations] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());

        List<String> recommendations = clientAppDraftService.getRedSideBanRecommendations(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/blue-side-pick/recommendations")
    @ResponseBody
    public ResponseEntity<List<String>> getBlueSidePickRecommendations(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/blue-side-pick/recommendations] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());

        List<String> recommendations = clientAppDraftService.getBlueSidePickRecommendations(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/red-side-pick/recommendations")
    @ResponseBody
    public ResponseEntity<List<String>> getRedSidePickRecommendations(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/red-side-pick/recommendations] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());

        List<String> recommendations = clientAppDraftService.getRedSidePickRecommendations(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }
}