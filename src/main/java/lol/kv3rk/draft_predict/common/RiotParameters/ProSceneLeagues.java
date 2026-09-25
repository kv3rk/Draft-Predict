package lol.kv3rk.draft_predict.common.RiotParameters;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class ProSceneLeagues {

    public List<String> proSceneLeagues(){
        return new ArrayList<>(List.of(
                "LEC",
                "LCS",
                "LPL",
                "LCK",
                "LCKC",
                "MSI",
                "EWC",
                "WSCI",
                "DCGI",
                "WLDs",
                "KeSPA Cup",
                "ASIAD"
        ));
    }
}
