package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.DTO.TopPerformingChampions;
import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppService;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO.BestDuo;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO.BestTrio;
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
    }

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
        model.addAttribute("topPerformingChampions", clientAppService.getTopPerformingChampions("pick_rate"));
        return "stats-pages/win-pick";
    }

    @GetMapping("/ban-rates")
    public String getBanRatesPage(Model model) {
        log.info("Entered [/draft-predict/ban-rates] endpoint");
        addCommonAttributes(model);
        model.addAttribute("mostBannedChampions", clientAppService.getMostBannedChampions());
        return "stats-pages/ban-rates";
    }

    @GetMapping("/best-duo")
    public String getBestDuoPage(Model model) {
        log.info("Entered [/draft-predict/best-duo] endpoint");
        addCommonAttributes(model);
        model.addAttribute("bestDuoChampions", clientAppService.getBestDuoChampions("MIDDLE", "JUNGLE"));
        return "stats-pages/best-duo";
    }

    @GetMapping("/best-trio")
    public String getBestTrioPage(Model model) {
        log.info("Entered [/draft-predict/best-trio] endpoint");
        addCommonAttributes(model);
        model.addAttribute("bestTrioChampions", clientAppService.getBestTrioChampions("TOP", "MIDDLE", "JUNGLE"));
        return "stats-pages/best-trio";
    }

    @GetMapping("/find/best-duo")
    @ResponseBody
    public List<BestDuo> findBestDuo(@RequestParam String role1, @RequestParam String role2) {
        log.info("Entered [/draft-predict/find/best-duo] endpoint");
        return clientAppService.getBestDuoChampions(role1, role2);
    }

    @GetMapping("/find/win-pick")
    @ResponseBody
    public List<TopPerformingChampions> findWinPick(@RequestParam String orderParameter) {
        log.info("Entered [/draft-predict/find/win-pick] endpoint");
        return clientAppService.getTopPerformingChampions(orderParameter);
    }

    @GetMapping("/find/best-trio")
    @ResponseBody
    public List<BestTrio> findBestTrio(@RequestParam String role1,
                                       @RequestParam String role2,
                                       @RequestParam String role3) {
        log.info("Entered [/draft-predict//find/best-trio] endpoint");
        return clientAppService.getBestTrioChampions(role1, role2, role3);
    }

}
