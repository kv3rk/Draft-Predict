package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppSoloqService;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.*;
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
@RequestMapping("/ranked-soloq")
@Slf4j
public class ClientAppSoloqController {

    private final ClientAppSoloqService clientAppSoloqService;

    @Autowired
    public ClientAppSoloqController(
            ClientAppSoloqService clientAppSoloqService
    ) {
        this.clientAppSoloqService = clientAppSoloqService;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("amountOfMatches", clientAppSoloqService.countMatches());
        model.addAttribute("actualPatch", clientAppSoloqService.actualPatch());
        model.addAttribute("servers", String.join(", ", clientAppSoloqService.getRiotServerName()));
        model.addAttribute("tiers", String.join(", ", clientAppSoloqService.getTierParameters()));
        model.addAttribute("timeUpdated", clientAppSoloqService.lastTimeUpdate());
        model.addAttribute("patchList", clientAppSoloqService.getPatchList());
        model.addAttribute("championList", clientAppSoloqService.getChampionList());
    }

    //-------------- Page Endpoints --------------

    @GetMapping("/main")
    public String getMainPage(Model model) {
        log.info("Entered [/ranked-soloq/main] endpoint");
        addCommonAttributes(model);
        return "ranked-soloq-page/ranked-soloq-page";
    }

    @GetMapping("/win-pick")
    public String getWinPickPage(Model model) {
        log.info("Entered [/ranked-soloq/win-pick] endpoint");
        addCommonAttributes(model);
        model.addAttribute("topPerformingChampions", clientAppSoloqService.getTopPerformingChampions("pick_rate",
                "%"));
        return "ranked-soloq-page/stats-pages/win-pick";
    }

    @GetMapping("/ban-rates")
    public String getBanRatesPage(Model model) {
        log.info("Entered [/ranked-soloq/ban-rates] endpoint");
        addCommonAttributes(model);
        model.addAttribute("mostBannedChampions", clientAppSoloqService.getMostBannedChampions(
                "%"
        ));
        return "ranked-soloq-page/stats-pages/ban-rates";
    }

    @GetMapping("/best-duo")
    public String getBestDuoPage(Model model) {
        log.info("Entered [/ranked-soloq/best-duo] endpoint");
        addCommonAttributes(model);
        model.addAttribute("bestDuoChampions", clientAppSoloqService.getBestDuoChampions(
                "TOP", "JUNGLE", "%", "Aatrox"));
        return "ranked-soloq-page/stats-pages/best-duo";
    }

    @GetMapping("/best-trio")
    public String getBestTrioPage(Model model) {
        log.info("Entered [/ranked-soloq/best-trio] endpoint");
        addCommonAttributes(model);
        model.addAttribute("bestTrioChampions", clientAppSoloqService.getBestTrioChampions(
                "TOP", "JUNGLE", "MIDDLE", "%",
                "Ambessa", "LeeSin"));
        return "ranked-soloq-page/stats-pages/best-trio";
    }

    @GetMapping("/champ-flex")
    public String getChampionFlexibility(Model model) {
        log.info("Entered [/ranked-soloq/champ-flex] endpoint");
        addCommonAttributes(model);
        model.addAttribute("championFlexibility", clientAppSoloqService.getChampionFlexibility(
                "Aatrox"));
        return "ranked-soloq-page/stats-pages/champ-flex";
    }

    @GetMapping("/draft-presence")
    public String getChampionDraftPresence(Model model) {
        log.info("Entered [/ranked-soloq/draft-presence] endpoint");
        addCommonAttributes(model);
        model.addAttribute("championDraftPresence", clientAppSoloqService.getChampionDraftPresence(
                "%"));
        return "ranked-soloq-page/stats-pages/draft-presence";
    }

    @GetMapping("/counter-pick")
    public String getCounterPick(Model model) {
        log.info("Entered [/ranked-soloq/counter-pick] endpoint");
        addCommonAttributes(model);
        model.addAttribute("championCounterPick", clientAppSoloqService.getBestMatchUps(
                "Aatrox", "TOP", "%"
        ));
        return "ranked-soloq-page/stats-pages/counter-pick";
    }


    //-------------- Info Endpoints --------------


    @GetMapping("/find/best-duo")
    @ResponseBody
    public List<BestDuo> findBestDuo(@RequestParam String role1,
                                     @RequestParam String role2,
                                     @RequestParam String patch,
                                     @RequestParam String champion) {
        log.info("Entered [/ranked-soloq/find/best-duo] endpoint");
        return clientAppSoloqService.getBestDuoChampions(role1, role2, patch, champion);
    }

    @GetMapping("/find/win-pick")
    @ResponseBody
    public List<TopPerformingChampions> findWinPick(@RequestParam String orderParameter,
                                                    @RequestParam String patch) {
        log.info("Entered [/ranked-soloq/find/win-pick] endpoint");
        return clientAppSoloqService.getTopPerformingChampions(orderParameter, patch);
    }

    @GetMapping("/find/best-trio")
    @ResponseBody
    public List<BestTrio> findBestTrio(@RequestParam String role1,
                                       @RequestParam String role2,
                                       @RequestParam String role3,
                                       @RequestParam String patch,
                                       @RequestParam String champion1,
                                       @RequestParam String champion2) {
        log.info("Entered [/ranked-soloq/find/best-trio] endpoint");
        return clientAppSoloqService.getBestTrioChampions(role1, role2, role3, patch,
                champion1, champion2);
    }

    @GetMapping("/find/champ-flex")
    @ResponseBody
    public ChampionFlexibility findChampFlex(@RequestParam String name,
                                             @RequestParam String patch) {
        log.info("Entered [/ranked-soloq/find/champ-flex] endpoint");
        return clientAppSoloqService.getChampionFlexibility(name);
    }

    @GetMapping("/find/draft-presence")
    @ResponseBody
    public List<ChampionPresence> findDraftPresence(@RequestParam String patch) {
        log.info("Entered [/ranked-soloq/find/draft-presence] endpoint");
        return clientAppSoloqService.getChampionDraftPresence(patch);
    }

    @GetMapping("/get/champion-list")
    @ResponseBody
    public List<Champion> getChampionList() {
        log.info("Entered [/ranked-soloq/get/champion-list] endpoint");
        return clientAppSoloqService.getChampionList();
    }

    @GetMapping("/get/counter-pick")
    @ResponseBody
    public List<CounterPick> findCounterPick(@RequestParam String champion1,
                                             @RequestParam String lane,
                                             @RequestParam String patch) {
        log.info("Entered [/ranked-soloq/get/counter-pick] endpoint");
        return clientAppSoloqService.getBestMatchUps(champion1, lane, patch);
    }

    @GetMapping("/get/patch-list")
    @ResponseBody
    public List<String> getPatchList() {
        log.info("Entered [/ranked-soloq/get/patch-list] endpoint");
        return clientAppSoloqService.getPatchList();
    }

    @GetMapping("/find/ban-rates")
    @ResponseBody
    public List<MostBannedChampions> findBanRates(@RequestParam String patch) {
        log.info("Entered [/ranked-soloq/find/ban-rates] endpoint");
        return clientAppSoloqService.getMostBannedChampions(patch);
    }
}
