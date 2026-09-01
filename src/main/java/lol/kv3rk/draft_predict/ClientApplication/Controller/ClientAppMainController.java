package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppSoloqService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/lol")
@Slf4j
public class ClientAppMainController {

    private final ClientAppSoloqService clientAppSoloqService;

    public ClientAppMainController(ClientAppSoloqService clientAppSoloqService) {
        this.clientAppSoloqService = clientAppSoloqService;
    }

    private void addCommonAttributes(Model model) {
        model.addAttribute("amountOfMatches", clientAppSoloqService.countMatches());
        model.addAttribute("actualPatch", clientAppSoloqService.actualPatch());
        model.addAttribute("servers", String.join(", ", clientAppSoloqService.getRiotServerName()));
        model.addAttribute("tiers", String.join(", ", clientAppSoloqService.getTierParameters()));
        model.addAttribute("timeUpdated", clientAppSoloqService.lastTimeUpdate());
        model.addAttribute("actualSeason", clientAppSoloqService.getActualSeason());
    }

    //============== Page Endpoints ==============

    @GetMapping("/main")
    public String getMainPage(Model model) {
        log.info("Entered [/lol/main] endpoint");
        addCommonAttributes(model);
        return "main-page";
    }

    @GetMapping("/ranked-soloq")
    public String getRankedSoloqPage() {

        log.info("Entered [/lol/ranked-soloq] endpoint");
        return "redirect:/ranked-soloq/main";
    }

    @GetMapping("/draft-predict")
    public String getDraftPredictPage() {

        log.info("Entered [/lol/draft-predict] endpoint");
        return "redirect:/draft-predict/main";
    }

    @GetMapping("/pro-scene")
    public String getProScenePage() {

        log.info("Entered [/lol/pro-scene] endpoint");
        return "redirect:/pro-scene/main";
    }

}
