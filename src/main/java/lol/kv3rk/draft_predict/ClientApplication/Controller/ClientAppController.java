package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppService;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedDbRequests.DTO.BestDuo;
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
        model.addAttribute("topPerformingChampions", clientAppService.getTopPerformingChampionsByPickRate());
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

    @GetMapping("/find/best-duo")
    @ResponseBody
    public List<BestDuo> findBestDuo(@RequestParam String role1, @RequestParam String role2) {
        log.info("Entered [/draft-predict/find/best-duo] endpoint");
        return clientAppService.getBestDuoChampions(role1, role2);
    }

}
