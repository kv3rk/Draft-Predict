package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lol.kv3rk.draft_predict.ClientApplication.Service.ClientAppService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/main")
    public String getMainPage(Model model){

        log.info("Entered [/draft-predict/main] endpoint");

        return "main-page/main-page";
    }


}
