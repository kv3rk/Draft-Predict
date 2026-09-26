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

        Set<OracleElexirMatchInfo> matchInfo = saveMatchData(oracleElexirList);
        Set<OracleElexirTeamInfo> teamInfo = saveTeamData(oracleElexirList, matchInfo);
        saveBanData(oracleElexirList, teamInfo);
        savePickData(oracleElexirList, teamInfo);

        log.info("Data saved successfully");

        removeProMatchFiles.removeAllProSceneFiles(csvFileParser.getProSceneFileMetadata());

    }

    private Set<OracleElexirMatchInfo> saveMatchData(List<OracleElexir> oracleElexirList) {
        Set<OracleElexirMatchInfo> matchInfo = new LinkedHashSet<>(
                oracleElexirList.stream()
                        .map(e -> new OracleElexirMatchInfo(
                                e.getGameId(), e.getLeague(), e.getDate(), e.getPatch()))
                        .filter(dto -> proSceneLeagues.contains(dto.league()))
                        .filter(dto -> dto.date().equals(LocalDate.now().minusDays(1)))
                        .collect(Collectors.toList())
        );

        List<ProMatchEntity> entities = matchInfo.stream()
                .map(dto -> ProMatchEntity.builder()
                        .gameId(dto.gameId())
                        .league(dto.league())
                        .date(dto.date())
                        .patch(dto.patch())
                        .build())
                .collect(Collectors.toList());

        proMatchRepository.saveAll(entities);
        log.info("Saved {} matches", entities.size());

        return matchInfo;
    }

    private Set<OracleElexirTeamInfo> saveTeamData(List<OracleElexir> oracleElexirList,
                                                   Set<OracleElexirMatchInfo> matchInfo) {
        Set<String> gameIds = new HashSet<>(matchInfo.stream()
                .map(OracleElexirMatchInfo::gameId).collect(Collectors.toList()));

        Set<OracleElexirTeamInfo> teamInfo = new LinkedHashSet<>(
                oracleElexirList.stream()
                        .map(e -> new OracleElexirTeamInfo(
                                e.getGameId(), e.getTeamName(), e.getSide(),
                                e.getFirstPick(), e.getResult()))
                        .filter(dto -> gameIds.contains(dto.gameId()))
                        .collect(Collectors.toList())
        );

        List<ProTeamEntity> entities = teamInfo.stream()
                .map(dto -> {
                    ProMatchEntity match = proMatchRepository.findById(dto.gameId())
                            .orElseThrow(() -> new RuntimeException("Match not found: " + dto.gameId()));

                    return ProTeamEntity.builder()
                            .matchId(match)
                            .league(match.getLeague()) // или dto.league() если есть в DTO
                            .teamName(dto.teamName())
                            .side(dto.side())
                            .firstPick("1".equals(dto.firstPick())) // или Boolean.parseBoolean
                            .result("1".equals(dto.result()))
                            .build();
                })
                .collect(Collectors.toList());

        proTeamRepository.saveAll(entities);
        log.info("Saved {} teams", entities.size());

        return teamInfo;
    }

    private void saveBanData(List<OracleElexir> oracleElexirList,
                             Set<OracleElexirTeamInfo> teamInfo) {
        Set<String> teamNames = teamInfo.stream()
                .map(OracleElexirTeamInfo::teamName)
                .collect(Collectors.toSet());

        List<ProBanEntity> entities = oracleElexirList.stream()
                .filter(e -> teamNames.contains(e.getTeamName()))
                .filter(e -> "team".equals(e.getPosition())) // только team строки имеют баны? Проверь логику
                .map(e -> {
                    ProTeamEntity team = proTeamRepository
                            .findByMatchIdAndTeamName(
                                    proMatchRepository.findById(e.getGameId()).orElseThrow(),
                                    e.getTeamName())
                            .orElseThrow(() -> new RuntimeException("Team not found"));

                    return ProBanEntity.builder()
                            .teamId(team)
                            .ban1(e.getBan1())
                            .ban2(e.getBan2())
                            .ban3(e.getBan3())
                            .ban4(e.getBan4())
                            .ban5(e.getBan5())
                            .build();
                })
                .collect(Collectors.toList());

        proBanRepository.saveAll(entities);
        log.info("Saved {} bans", entities.size());
    }

    private void savePickData(List<OracleElexir> oracleElexirList,
                              Set<OracleElexirTeamInfo> teamInfo) {
        collectTeamPicks(oracleElexirList);

        Set<String> teamNames = teamInfo.stream()
                .map(OracleElexirTeamInfo::teamName)
                .collect(Collectors.toSet());

        List<ProPickEntity> entities = oracleElexirList.stream()
                .filter(e -> !"team".equals(e.getPosition()))
                .filter(e -> teamNames.contains(e.getTeamName()))
                .map(e -> {
                    String key = e.getGameId() + "|" + e.getTeamName();
                    List<String> picks = teamPicksMap.getOrDefault(key, List.of());
                    int pickOrder = picks.indexOf(e.getChampion()) + 1;

                    ProTeamEntity team = proTeamRepository
                            .findByMatchIdAndTeamName(
                                    proMatchRepository.findById(e.getGameId()).orElseThrow(),
                                    e.getTeamName())
                            .orElseThrow(() -> new RuntimeException("Team not found"));

                    return ProPickEntity.builder()
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
                })
                .collect(Collectors.toList());

        proPickRepository.saveAll(entities);
        log.info("Saved {} picks", entities.size());
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