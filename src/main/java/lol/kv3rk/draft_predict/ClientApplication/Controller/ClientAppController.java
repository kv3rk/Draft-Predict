package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppService;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/draft-predict")
@Slf4j
public class ClientAppController {

    private final ClientAppService clientAppService;

    @Autowired
    public ClientAppController(
            ClientAppService clientAppService
    ) {
        this.clientAppService = clientAppService;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("amountOfMatches", clientAppService.countMatches());
        model.addAttribute("actualPatch", clientAppService.actualPatch());
        model.addAttribute("servers", String.join(", ", clientAppService.getRiotServerName()));
        model.addAttribute("tiers", String.join(", ", clientAppService.getTierParameters()));
        model.addAttribute("timeUpdated", clientAppService.lastTimeUpdate());
        model.addAttribute("patchList", clientAppService.getPatchList());
    }

    //-------------- Page Endpoints --------------

    @GetMapping("/main")
    public String getMainPage(Model model) {
        log.info("Entered [/draft-predict/main] endpoint");
        addCommonAttributes(model);
        return "main-page/main-page";
    }

    @GetMapping("/win-pick")
    public String getWinPickPage(Model model) {
        log.info("Entered [/draft-predict/win-pick] endpoint");
        addCommonAttributes(model);
        model.addAttribute("topPerformingChampions", clientAppService.getTopPerformingChampions("pick_rate",
                "%"));
        return "stats-pages/win-pick";
    }

    @GetMapping("/ban-rates")
    public String getBanRatesPage(Model model) {
        log.info("Entered [/draft-predict/ban-rates] endpoint");
        addCommonAttributes(model);
        model.addAttribute("mostBannedChampions", clientAppService.getMostBannedChampions(
                "%"
        ));
        return "stats-pages/ban-rates";
    }

    @GetMapping("/best-duo")
    public String getBestDuoPage(Model model) {
        log.info("Entered [/draft-predict/best-duo] endpoint");
        addCommonAttributes(model);
        model.addAttribute("bestDuoChampions", clientAppService.getBestDuoChampions(
                "MIDDLE", "JUNGLE", "%"));
        return "stats-pages/best-duo";
    }

    @GetMapping("/best-trio")
    public String getBestTrioPage(Model model) {
        log.info("Entered [/draft-predict/best-trio] endpoint");
        addCommonAttributes(model);
        model.addAttribute("bestTrioChampions", clientAppService.getBestTrioChampions(
                "TOP", "MIDDLE", "JUNGLE", "%"));
        return "stats-pages/best-trio";
    }

    @GetMapping("/champ-flex")
    public String getChampionFlexibility(Model model) {
        log.info("Entered [/draft-predict/champ-flex] endpoint");
        addCommonAttributes(model);
        model.addAttribute("championFlexibility", clientAppService.getChampionFlexibility(
                "Aatrox", "%"));
        return "stats-pages/champ-flex";
    }

    @GetMapping("/draft-presence")
    public String getChampionDraftPresence(Model model) {
        log.info("Entered [/draft-predict/draft-presence] endpoint");
        addCommonAttributes(model);
        model.addAttribute("championDraftPresence", clientAppService.getChampionDraftPresence(
                "Aatrox", "%"));
        return "stats-pages/draft-presence";
    }

    @GetMapping("/counter-pick")
    public String getCounterPick(Model model) {
        log.info("Entered [/draft-predict/counter-pick] endpoint");
        addCommonAttributes(model);
        model.addAttribute("championCounterPick", clientAppService.getCounterPick(
                "Aatrox", "Ahri", "MIDDLE", "%"
        ));
        return "stats-pages/counter-pick";
    }

    //-------------- Info Endpoints --------------

    @GetMapping("/find/best-duo")
    @ResponseBody
    public List<BestDuo> findBestDuo(@RequestParam String role1,
                                     @RequestParam String role2,
                                     @RequestParam String patch) {
        log.info("Entered [/draft-predict/find/best-duo] endpoint");
        return clientAppService.getBestDuoChampions(role1, role2, patch);
    }

    @GetMapping("/find/win-pick")
    @ResponseBody
    public List<TopPerformingChampions> findWinPick(@RequestParam String orderParameter,
                                                    @RequestParam String patch) {
        log.info("Entered [/draft-predict/find/win-pick] endpoint");
        return clientAppService.getTopPerformingChampions(orderParameter, patch);
    }

    @GetMapping("/find/best-trio")
    @ResponseBody
    public List<BestTrio> findBestTrio(@RequestParam String role1,
                                       @RequestParam String role2,
                                       @RequestParam String role3,
                                       @RequestParam String patch) {
        log.info("Entered [/draft-predict//find/best-trio] endpoint");
        return clientAppService.getBestTrioChampions(role1, role2, role3, patch);
    }

    @GetMapping("/find/champ-flex")
    @ResponseBody
    public ChampionFlexibility findChampFlex(@RequestParam String name,
                                             @RequestParam String patch) {
        log.info("Entered [/draft-predict/find/champ-flex] endpoint");
        return clientAppService.getChampionFlexibility(name, patch);
    }

    @GetMapping("/find/draft-presence")
    @ResponseBody
    public ChampionPresence findDraftPresence(@RequestParam String name,
                                              @RequestParam String patch) {
        log.info("Entered [/draft-predict/find/draft-presence] endpoint");
        return clientAppService.getChampionDraftPresence(name, patch);
    }

    @GetMapping("/get/champion-list")
    @ResponseBody
    public List<Champion> getChampionList() {
        log.info("Entered [/draft-predict/get/champion-list] endpoint");
        return clientAppService.getChampionList();
    }

    @GetMapping("/get/counter-pick")
    @ResponseBody
    public CounterPick findCounterPick(@RequestParam String champion1,
                                       @RequestParam String champion2,
                                       @RequestParam String lane,
                                       @RequestParam String patch) {
        log.info("Entered [/draft-predict/get/counter-pick] endpoint");
        return clientAppService.getCounterPick(champion1, champion2, lane, patch);
    }

    @GetMapping("/get/patch-list")
    @ResponseBody
    public List<String> getPatchList() {
        log.info("Entered [/draft-predict/get/patch-list] endpoint");
        return clientAppService.getPatchList();
    }

    @GetMapping("/find/ban-rates")
    @ResponseBody
    public List<MostBannedChampions> findBanRates(@RequestParam String patch) {
        log.info("Entered [/draft-predict/find/ban-rates] endpoint");
        return clientAppService.getMostBannedChampions(patch);
    }
}
