package lol.kv3rk.draft_predict.ClientApplication.Controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/draft-predict")
@Slf4j
public class ClientAppDraftController {

    @GetMapping("/main")
    public String getMainPage() {
        log.info("Entered [/draft-predict/main] endpoint");
        return "draft-predict/draft-predict";
    }
}
