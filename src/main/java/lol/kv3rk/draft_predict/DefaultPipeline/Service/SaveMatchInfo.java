package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import jakarta.transaction.Transactional;
import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Bans.Entity.BansEntity;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Matches.Entity.MatchesEntity;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Participants.Entity.ParticipantsEntity;
import lol.kv3rk.draft_predict.RankedSoloQ.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.common.RiotDTO.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Slf4j
@Service
public class SaveMatchInfo {

    private final ChampionIdDB championIdDB;
    private final MatchesRepository matchesRepository;
    private final ParticipantsRepository participantsRepository;
    private final BansRepository bansRepository;

    public SaveMatchInfo(ChampionIdDB championIdDB,
                         MatchesRepository matchesRepository,
                         ParticipantsRepository participantsRepository,
                         BansRepository bansRepository) {

        this.championIdDB = championIdDB;
        this.matchesRepository = matchesRepository;
        this.participantsRepository = participantsRepository;
        this.bansRepository = bansRepository;
    }

    @Transactional
    public void saveMatchInfo(MatchDTO matchInfo, String server, String matchID) {

        log.info("--------------------------------------");

        //Save match general info
        saveMatchGeneralInfo(matchInfo, server, matchID);

        //Save champions pick/win rate stats
        saveParticipantDTO(matchInfo, matchID);

        //Save champions ban rate stats
        saveTeamDTO(matchInfo, matchID);
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    protected void saveMatchGeneralInfo(MatchDTO matchInfo, String server, String matchID) {

        String fullPatch = extractGameVersion(matchInfo.info());
        int secondDot = fullPatch.indexOf('.', fullPatch.indexOf('.') + 1);
        String gameVersion = fullPatch.substring(0, secondDot);
        Long epochTime = extractGameCreation(matchInfo.info());
        LocalDate matchDate = LocalDate.from(
                LocalDateTime.ofEpochSecond(
                        Instant.ofEpochMilli(epochTime).getEpochSecond(),
                        0,
                        ZoneOffset.UTC
                )
        );
        log.info("Game version: {}", gameVersion);
        log.info("Match server: {}", server);
        log.info("Match id: {}", matchID);

        MatchesEntity newMatch = MatchesEntity.builder()
                .matchId(matchID).patch(gameVersion).server(server).matchDate(matchDate)
                .build();
        matchesRepository.save(newMatch);

    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    protected void saveParticipantDTO(MatchDTO matchInfo, String matchID) {

        List<ParticipantDTO> participantDTOList = extractParticipantDTOList(matchInfo.info());
        participantDTOList.forEach(participantDTO -> {

                    String championName = participantDTO.championName();
                    String lane = participantDTO.teamPosition();
                    boolean win = participantDTO.win();
                    int teamId = participantDTO.teamId();

                    log.info("{} - {}: {}. Team {}", championName, lane, win, teamId);
                    ParticipantsEntity newParticipant = ParticipantsEntity.builder()
                            .matchId(matchesRepository.findByMatchId(matchID))
                            .champion(championName)
                            .position(lane)
                            .teamId(teamId)
                            .win(win)
                            .build();
                    participantsRepository.save(newParticipant);

                }

        );
    }

    @Transactional(value = Transactional.TxType.REQUIRES_NEW)
    protected void saveTeamDTO(MatchDTO matchInfo, String matchID) {

        List<TeamDTO> teamDTOList = extractTeamDTOList(matchInfo.info());
        teamDTOList.forEach(teamDTO -> {

                    List<BanDTO> banDTOList = extractBanDTOList(teamDTO);
                    banDTOList.forEach(banDTO -> {

                                int championId = banDTO.championId();
                                String championName = championIdDB.mapChampionIdToName(championId);
                                int teamId = teamDTO.teamId();

                                log.info("{}. Team {}", championName, teamId);
                                BansEntity newBans = BansEntity.builder()
                                        .matchId(matchesRepository.findByMatchId(matchID))
                                        .champion(championName)
                                        .teamId(teamId)
                                        .build();
                                bansRepository.save(newBans);
                            }

                    );
                }
        );

    }

    private String extractGameVersion(InfoDTO infoDTO) {

        return infoDTO.gameVersion();

    }

    private long extractGameCreation(InfoDTO infoDTO) {

        return infoDTO.gameCreation();

    }

    private List<ParticipantDTO> extractParticipantDTOList(InfoDTO infoDTO) {

        return infoDTO.participants();

    }

    private List<TeamDTO> extractTeamDTOList(InfoDTO infoDTO) {

        return infoDTO.teams();

    }

    private List<BanDTO> extractBanDTOList(TeamDTO teamDTO) {

        return teamDTO.bans();
    }
}
