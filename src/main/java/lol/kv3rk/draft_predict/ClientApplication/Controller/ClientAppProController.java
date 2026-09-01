package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/pro-scene")
@Slf4j
public class ClientAppProController {

    //============== Page Endpoints ==============
    @GetMapping("/main")
    public String getMainPage() {
        log.info("Entered [/pro-scene/main] endpoint");
        return "pro-scene-stats/pro-scene-page";
    }
}
