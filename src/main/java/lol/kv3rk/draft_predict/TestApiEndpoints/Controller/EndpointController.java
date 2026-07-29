package lol.kv3rk.draft_predict.TestApiEndpoints.Controller;

import lol.kv3rk.draft_predict.TestApiEndpoints.DTO.FindSummonerDTO;
import lol.kv3rk.draft_predict.TestApiEndpoints.Service.EndpointService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/draft-predict")
public class EndpointController {

    private final EndpointService endpointService;

    @Autowired
    public EndpointController(
            EndpointService endpointService
    ) {
        this.endpointService = endpointService;
    }

    @GetMapping("/search/page")
    public String searchPage(Model model) {

        model.addAttribute("FundSummonerDTO", new FindSummonerDTO("", ""));

        return "search-page/search-page";
    }

    @PostMapping("/find/summoner")
    public String findSummoner(@ModelAttribute FindSummonerDTO findSummonerDTO) {

        endpointService.definePUUID(findSummonerDTO);

        return "redirect:/draft-predict/summoner/page";
    }

    @GetMapping("/summoner/page")
    public String summonerPage() {

        return "summoner-page/summoner-page";
    }


}
