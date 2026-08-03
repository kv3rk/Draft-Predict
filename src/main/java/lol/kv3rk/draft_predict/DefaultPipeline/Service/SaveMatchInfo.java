package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import jakarta.transaction.Transactional;
import lol.kv3rk.draft_predict.DefaultPipeline.Component.ChampionIdDB;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Entity.BansEntity;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Repository.BansRepository;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Entity.MatchesEntity;
import lol.kv3rk.draft_predict.RankedEntities.Matches.Repository.MatchesRepository;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Entity.ParticipantsEntity;
import lol.kv3rk.draft_predict.RankedEntities.Participants.Repository.ParticipantsRepository;
import lol.kv3rk.draft_predict.common.RiotDTO.*;
import org.springframework.stereotype.Service;

import java.util.List;

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

        System.out.println("--------------------------------------");
        double gameVersion = Double.parseDouble(extractGameVersion(matchInfo.info()).substring(0, 5));
        System.out.println("Game version: " + gameVersion);
        System.out.println("Match server: " + server);
        System.out.println("Match id: " + matchID);

        MatchesEntity newMatch = MatchesEntity.builder()
                .matchId(matchID).patch(gameVersion).server(server)
                .build();
        matchesRepository.save(newMatch);


        List<ParticipantDTO> participantDTOList = extractParticipantDTOList(matchInfo.info());
        participantDTOList.forEach(participantDTO -> {

                    String championName = participantDTO.championName();
                    String lane = participantDTO.teamPosition();
                    boolean win = participantDTO.win();
                    int teamId = participantDTO.teamId();

                    System.out.println(championName + " - " + lane + ": " + win + ". Team: " + teamId);
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

        List<TeamDTO> teamDTOList = extractTeamDTOList(matchInfo.info());
        teamDTOList.forEach(teamDTO -> {

                    List<BanDTO> banDTOList = extractBanDTOList(teamDTO);
                    banDTOList.forEach(banDTO -> {

                                int championId = banDTO.championId();
                                String championName = championIdDB.mapChampionIdToName(championId);
                                int teamId = teamDTO.teamId();

                                System.out.print(championName + ". Team: " + teamId);
                                BansEntity newBans = BansEntity.builder()
                                        .matchId(matchesRepository.findByMatchId(matchID))
                                        .champion(championName)
                                        .teamId(teamId)
                                        .build();
                                bansRepository.save(newBans);
                            }

                    );
                    System.out.println();
                }

        );
        System.out.println("--------------------------------------");

    }

    private String extractGameVersion(InfoDTO infoDTO) {

        return infoDTO.gameVersion();

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
