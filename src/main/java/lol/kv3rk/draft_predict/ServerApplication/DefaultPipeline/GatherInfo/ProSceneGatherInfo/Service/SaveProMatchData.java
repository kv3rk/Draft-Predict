package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import jakarta.annotation.PostConstruct;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO.*;
import lol.kv3rk.draft_predict.common.RiotParameters.ProSceneLeagues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SaveProMatchData {

    private final CsvFileParser csvFileParser;
    private List<String> proSceneLeagues;
    private final ProSceneLeagues proSceneLeaguesBean;
    private Map<String, List<String>> teamPicksMap = new HashMap<>();

    public SaveProMatchData(CsvFileParser csvFileParser,
                            ProSceneLeagues proSceneLeaguesBean) {
        this.csvFileParser = csvFileParser;
        this.proSceneLeaguesBean = proSceneLeaguesBean;
    }

    @PostConstruct
    private void initBean() {
        proSceneLeagues = proSceneLeaguesBean.proSceneLeagues();
    }

    public void saveData() throws IOException {

        List<OracleElexir> oracleElexirList = csvFileParser.readCSVFile();

        Set<OracleElexirMatchInfo> matchInfo = saveMatchData(oracleElexirList);

        Set<OracleElexirTeamInfo> teamInfo = saveTeamData(oracleElexirList, matchInfo);

        saveBanData(oracleElexirList, teamInfo);

        savePickData(oracleElexirList, teamInfo);

    }

    private Set<OracleElexirMatchInfo> saveMatchData(List<OracleElexir> oracleElexirList) {

        Set<OracleElexirMatchInfo> matchInfo = new LinkedHashSet<>(
                oracleElexirList
                        .stream()
                        .map(OracleElexir -> {
                            return new OracleElexirMatchInfo(
                                    OracleElexir.getGameId(),
                                    OracleElexir.getLeague(),
                                    OracleElexir.getDate(),
                                    OracleElexir.getPatch()
                            );
                        })
                        .filter(OracleElexir -> proSceneLeagues.contains(OracleElexir.league()))
                        .filter(OracleElexir -> OracleElexir.date().equals(LocalDate.now().minusDays(1)))
                        .toList()
        );

        matchInfo.forEach(dto -> System.out.println(dto.gameId() + " | " + dto.league() + " | " + dto.date() + " | " + dto.patch()));
        System.out.println(matchInfo.size());

        return matchInfo;

    }

    private Set<OracleElexirTeamInfo> saveTeamData(List<OracleElexir> oracleElexirList, Set<OracleElexirMatchInfo> matchInfo) {

        Set<String> gameId = new HashSet<>(matchInfo.stream().map(OracleElexirMatchInfo::gameId).toList());

        Set<OracleElexirTeamInfo> teamInfo = new LinkedHashSet<>(
                oracleElexirList
                        .stream()
                        .map(OracleElexir -> {
                            return new OracleElexirTeamInfo(
                                    OracleElexir.getGameId(),
                                    OracleElexir.getTeamName(),
                                    OracleElexir.getSide(),
                                    OracleElexir.getFirstPick(),
                                    OracleElexir.getResult()
                            );
                        })
                        .filter(OracleElexir -> gameId.contains(OracleElexir.gameId()))
                        .toList()
        );

        teamInfo.forEach(dto -> System.out.println(dto.gameId() + " | " + dto.teamName() + " | " + dto.side() + " | " + dto.firstPick() + " | " + dto.result()));
        System.out.println(teamInfo.size());

        return teamInfo;

    }

    private void saveBanData(List<OracleElexir> oracleElexirList, Set<OracleElexirTeamInfo> teamInfo) {

        Set<String> teamName = new HashSet<>(teamInfo.stream().map(OracleElexirTeamInfo::teamName).toList());

        Set<OracleElexirBanInfo> banInfo = new LinkedHashSet<>(
                oracleElexirList
                        .stream()
                        .map(OracleElexir -> {
                            return new OracleElexirBanInfo(
                                    OracleElexir.getTeamName(),
                                    OracleElexir.getBan1(),
                                    OracleElexir.getBan2(),
                                    OracleElexir.getBan3(),
                                    OracleElexir.getBan4(),
                                    OracleElexir.getBan5()
                            );
                        })
                        .filter(OracleElexir -> teamName.contains(OracleElexir.teamName()))
                        .toList()
        );

        banInfo.forEach(dto -> System.out.println(dto.teamName() + " | " + dto.ban1() + " | " + dto.ban2() + " | " + dto.ban3() + " | " + dto.ban4() + " | " + dto.ban5()));
        System.out.println(banInfo.size());

    }

    private void savePickData(List<OracleElexir> oracleElexirList, Set<OracleElexirTeamInfo> teamInfo) {

        collectTeamPicks(oracleElexirList);

        Set<String> teamName = new HashSet<>(teamInfo.stream().map(OracleElexirTeamInfo::teamName).toList());

        Set<OracleElexirPickInfo> pickInfo = new LinkedHashSet<>(
                oracleElexirList
                        .stream()
                        .filter(e -> !"team".equals(e.getPosition()))
                        .map(e -> {
                            String key = e.getGameId() + "|" + e.getTeamName();
                            List<String> picks = teamPicksMap.getOrDefault(key, List.of());
                            int pickOrder = picks.indexOf(e.getChampion()) + 1;

                            return new OracleElexirPickInfo(
                                    e.getTeamName(),
                                    e.getChampion(),
                                    e.getPosition(),
                                    e.getPlayerName(),
                                    e.getPlayerId(),
                                    e.getGoldDiffAt15(),
                                    e.getXpDiffAt15(),
                                    e.getCsDiffAt15(),
                                    pickOrder > 0 ? pickOrder : null
                            );
                        })
                        .filter(e -> teamName.contains(e.teamName()))
                        .toList()
        );

        pickInfo.forEach(dto -> System.out.println(
                dto.teamName() + " | " +
                        dto.champion() + " | " +
                        dto.position() + " | " +
                        dto.playerName() + " | " +
                        dto.playerId() + " | " +
                        dto.goldDiffAt15() + " | " +
                        dto.xpDiffAt15() + " | " +
                        dto.csDiffAt15() + " | " +
                        dto.pickOrder()
        ));
        System.out.println(pickInfo.size());
    }

    private void collectTeamPicks(List<OracleElexir> oracleElexirList) {
        teamPicksMap = oracleElexirList.stream()
                .filter(e -> "team".equals(e.getPosition()))
                .collect(Collectors.toMap(
                        e -> e.getGameId() + "|" + e.getTeamName(),
                        e -> List.of(e.getPick1(), e.getPick2(), e.getPick3(), e.getPick4(), e.getPick5()),
                        (existing, replacement) -> existing
                ));
    }
}
