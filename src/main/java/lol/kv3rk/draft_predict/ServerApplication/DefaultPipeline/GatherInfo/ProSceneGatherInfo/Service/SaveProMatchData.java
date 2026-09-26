package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.Service;

import jakarta.annotation.PostConstruct;
import lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO.*;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Ban.Entity.ProBanEntity;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Entity.ProMatchEntity;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Match.Repository.ProMatchRepository;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Pick.Entity.ProPickEntity;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Pick.Repository.ProPickRepository;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Ban.Repository.ProBanRepository;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Team.Entity.ProTeamEntity;
import lol.kv3rk.draft_predict.ServerApplication.ProSceneCompetitive.ProSceneEntities.Team.Repository.ProTeamRepository;
import lol.kv3rk.draft_predict.common.RiotParameters.ProSceneLeagues;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SaveProMatchData {

    private final CsvFileParser csvFileParser;
    private final ProMatchRepository proMatchRepository;
    private final ProTeamRepository proTeamRepository;
    private final ProBanRepository proBanRepository;
    private final ProPickRepository proPickRepository;
    private final ProSceneLeagues proSceneLeaguesBean;
    private final RemoveProMatchFiles removeProMatchFiles;

    private List<String> proSceneLeagues;
    private Map<String, List<String>> teamPicksMap = new HashMap<>();

    public SaveProMatchData(CsvFileParser csvFileParser,
                            ProMatchRepository proMatchRepository,
                            ProTeamRepository proTeamRepository,
                            ProBanRepository proBanRepository,
                            ProPickRepository proPickRepository,
                            ProSceneLeagues proSceneLeaguesBean,
                            RemoveProMatchFiles removeProMatchFiles) {
        this.csvFileParser = csvFileParser;
        this.proMatchRepository = proMatchRepository;
        this.proTeamRepository = proTeamRepository;
        this.proBanRepository = proBanRepository;
        this.proPickRepository = proPickRepository;
        this.proSceneLeaguesBean = proSceneLeaguesBean;
        this.removeProMatchFiles = removeProMatchFiles;
    }

    @PostConstruct
    private void initBean() {
        proSceneLeagues = proSceneLeaguesBean.proSceneLeagues();
    }

    @Transactional
    public void saveData() throws IOException {
        List<OracleElexir> oracleElexirList = csvFileParser.readCSVFile();

        saveMatchData(oracleElexirList);
        saveTeamData(oracleElexirList);
        saveBanData(oracleElexirList);
        savePickData(oracleElexirList);

        log.info("Data saved successfully");
        removeProMatchFiles.removeAllProSceneFiles(csvFileParser.getProSceneFileMetadata());
    }

    private void saveMatchData(List<OracleElexir> oracleElexirList) {
        Set<OracleElexirMatchInfo> matchInfo = oracleElexirList.stream()
                .map(e -> new OracleElexirMatchInfo(
                        e.getGameId(), e.getLeague(), e.getDate(), e.getPatch()))
                .filter(dto -> proSceneLeagues.contains(dto.league()))
                .filter(dto -> dto.date().equals(LocalDate.now().minusDays(1)))
                .collect(Collectors.toSet());

        matchInfo.forEach(dto -> {
            ProMatchEntity entity = ProMatchEntity.builder()
                    .gameId(dto.gameId())
                    .league(dto.league())
                    .date(dto.date())
                    .patch(dto.patch())
                    .build();
            proMatchRepository.save(entity);
        });

        log.info("Saved {} matches", matchInfo.size());
    }

    private void saveTeamData(List<OracleElexir> oracleElexirList) {

        Set<OracleElexirTeamInfo> teamInfo = oracleElexirList.stream()
                .filter(e -> !"team".equals(e.getPosition()))  // только игроки, не team
                .map(e -> new OracleElexirTeamInfo(
                        e.getGameId(), e.getLeague(), e.getTeamName(),
                        e.getSide(), e.getFirstPick(), e.getResult()))
                .filter(dto -> proSceneLeagues.contains(dto.league()))
                .collect(Collectors.toSet());

        teamInfo.forEach(dto -> {
            ProMatchEntity match = proMatchRepository.findByGameId(dto.gameId())
                    .orElseThrow(() -> new RuntimeException("Match not found: " + dto.gameId()));

            ProTeamEntity entity = ProTeamEntity.builder()
                    .gameId(match)
                    .league(dto.league())
                    .teamName(dto.teamName())
                    .side(dto.side())
                    .firstPick("1".equals(dto.firstPick()))
                    .result("1".equals(dto.result()))
                    .build();
            proTeamRepository.save(entity);
        });

        log.info("Saved {} teams", teamInfo.size());
    }

    private void saveBanData(List<OracleElexir> oracleElexirList) {

        oracleElexirList.stream()
                .filter(e -> "team".equals(e.getPosition()))
                .filter(e -> proSceneLeagues.contains(e.getLeague()))
                .forEach(e -> {
                    ProMatchEntity match = proMatchRepository.findByGameId(e.getGameId())
                            .orElseThrow(() -> new RuntimeException("Match not found: " + e.getGameId()));

                    ProTeamEntity team = proTeamRepository
                            .findByGameIdAndTeamName(match, e.getTeamName())
                            .orElseThrow(() -> new RuntimeException("Team not found: " + e.getTeamName()));

                    ProBanEntity entity = ProBanEntity.builder()
                            .teamId(team)
                            .ban1(e.getBan1())
                            .ban2(e.getBan2())
                            .ban3(e.getBan3())
                            .ban4(e.getBan4())
                            .ban5(e.getBan5())
                            .build();
                    proBanRepository.save(entity);
                });

        log.info("Saved bans");
    }

    private void savePickData(List<OracleElexir> oracleElexirList) {
        collectTeamPicks(oracleElexirList);

        oracleElexirList.stream()
                .filter(e -> !"team".equals(e.getPosition()))
                .filter(e -> proSceneLeagues.contains(e.getLeague()))
                .forEach(e -> {
                    String key = e.getGameId() + "|" + e.getTeamName();
                    List<String> picks = teamPicksMap.getOrDefault(key, List.of());
                    int pickOrder = picks.indexOf(e.getChampion()) + 1;

                    ProMatchEntity match = proMatchRepository.findByGameId(e.getGameId())
                            .orElseThrow(() -> new RuntimeException("Match not found: " + e.getGameId()));

                    ProTeamEntity team = proTeamRepository
                            .findByGameIdAndTeamName(match, e.getTeamName())
                            .orElseThrow(() -> new RuntimeException("Team not found: " + e.getTeamName()));

                    ProPickEntity entity = ProPickEntity.builder()
                            .teamId(team)
                            .champion(e.getChampion())
                            .position(e.getPosition())
                            .playerName(e.getPlayerName())
                            .playerId(e.getPlayerId())
                            .pickOrder(pickOrder > 0 ? pickOrder : 0)
                            .goldDiffAt15(parseIntSafe(e.getGoldDiffAt15()))
                            .xpDiffAt15(parseIntSafe(e.getXpDiffAt15()))
                            .csDiffAt15(parseIntSafe(e.getCsDiffAt15()))
                            .build();
                    proPickRepository.save(entity);
                });

        log.info("Saved picks");
    }

    private void collectTeamPicks(List<OracleElexir> oracleElexirList) {
        teamPicksMap = oracleElexirList.stream()
                .filter(e -> "team".equals(e.getPosition()))
                .collect(Collectors.toMap(
                        e -> e.getGameId() + "|" + e.getTeamName(),
                        e -> List.of(e.getPick1(), e.getPick2(), e.getPick3(),
                                e.getPick4(), e.getPick5()),
                        (existing, replacement) -> existing
                ));
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.isBlank()) return 0;
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}