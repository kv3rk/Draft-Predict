package lol.kv3rk.draft_predict.common.RiotParametersDB;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RiotRequestParameters {

    public List<String> tierParameters() {

        List<String> tierParameters = List.of(
                "MASTER", "GRANDMASTER", "CHALLENGER"
        );

        return tierParameters;
    }

    public List<String> divisionParameters() {

        List<String> divisionParameters = List.of(
                "I", "II", "III", "IV"
        );

        return divisionParameters;
    }

}
