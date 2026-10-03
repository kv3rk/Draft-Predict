package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppProSceneService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pro-scene")
@Slf4j
public class ClientAppProController {

    private final ClientAppProSceneService clientAppProSceneService;

    public ClientAppProController(ClientAppProSceneService clientAppProSceneService) {
        this.clientAppProSceneService = clientAppProSceneService;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("amountOfProMatches", clientAppProSceneService.getAmountOfMatches());
    }

    //============== Page Endpoints ==============
    @GetMapping("/main")
    public String getMainPage(Model model) {
        log.info("Entered [/pro-scene/main] endpoint");
        addCommonAttributes(model);
        return "pro-scene-stats/pro-scene-page";
    }

    @GetMapping("/pick-rate")
    public String getProChampionPickRate(Model model) {
        log.info("Entered [/pro-scene/pick-rate] endpoint");
        addCommonAttributes(model);
        model.addAttribute("proPickRateChampions", clientAppProSceneService.getProChampionPickRate());
        return "pro-scene-stats/stats-pages/pro-pick";
    }
}
