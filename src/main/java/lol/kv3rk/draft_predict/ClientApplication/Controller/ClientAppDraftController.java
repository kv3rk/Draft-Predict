package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.DTO.Draft.MatchSetupInfoDTO;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppDraftService;
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
        model.addAttribute("patchList", clientAppDraftService.getPatchList());
        model.addAttribute("championList", clientAppDraftService.getChampionList());
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

    // ================= EARLY PHASE DRAFT ENDPOINTS =================
    @PostMapping("/blue-side-ban/recommendations/early-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getBlueSideBanRecommendationsEarlyPhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/blue-side-ban/recommendations/early-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getBlueSideBanRecommendationsEarlyPhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/red-side-ban/recommendations/early-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getRedSideBanRecommendationsEarlyPhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/red-side-ban/recommendations/early-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getRedSideBanRecommendationsEarlyPhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/blue-side-pick/recommendations/early-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getBlueSidePickRecommendationsEarlyPhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/blue-side-pick/recommendations/early-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getBlueSidePickRecommendationsEarlyPhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/red-side-pick/recommendations/early-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getRedSidePickRecommendationsEarlyPhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/red-side-pick/recommendations/early-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getRedSidePickRecommendationsEarlyPhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }


    // ================= LATE PHASE DRAFT ENDPOINTS =================
    @PostMapping("/blue-side-ban/recommendations/late-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getBlueSideBanRecommendationsLatePhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/blue-side-ban/recommendations/late-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getBlueSideBanRecommendationsLatePhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/red-side-ban/recommendations/late-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getRedSideBanRecommendationsLatePhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/red-side-ban/recommendations/late-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getRedSideBanRecommendationsLatePhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/blue-side-pick/recommendations/late-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getBlueSidePickRecommendationsLatePhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/blue-side-pick/recommendations/late-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getBlueSidePickRecommendationsLatePhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }

    @PostMapping("/red-side-pick/recommendations/late-phase-draft")
    @ResponseBody
    public ResponseEntity<List<String>> getRedSidePickRecommendationsLatePhaseDraft(
            @RequestBody Map<String, List<String>> request) {
        log.info("Entered [/draft-predict/red-side-pick/recommendations/late-phase-draft] endpoint");
        List<String> blueSideBans = request.getOrDefault("blueSideBans", List.of());
        List<String> redSideBans = request.getOrDefault("redSideBans", List.of());
        List<String> blueSidePicks = request.getOrDefault("blueSidePicks", List.of());
        List<String> redSidePicks = request.getOrDefault("redSidePicks", List.of());
        List<String> recommendations = clientAppDraftService.getRedSidePickRecommendationsLatePhaseDraft(
                blueSideBans, redSideBans, blueSidePicks, redSidePicks);
        return ResponseEntity.ok(recommendations);
    }
}