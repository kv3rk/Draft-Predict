package lol.kv3rk.draft_predict.ServerApplication.DefaultPipeline.GatherInfo.ProSceneGatherInfo.DTO.OracleElexirDTO;

import com.opencsv.bean.CsvBindByPosition;
import com.opencsv.bean.CsvDate;

import java.time.LocalDate;

public class OracleElexir {

    @CsvBindByPosition(position = 0)   // gameid
    private String gameId;

    @CsvBindByPosition(position = 3)   // league
    private String league;

    @CsvBindByPosition(position = 7)   // date
    @CsvDate("yyyy-MM-dd HH:mm:ss")
    private LocalDate date;

    @CsvBindByPosition(position = 9)   // patch
    private String patch;

    @CsvBindByPosition(position = 11)  // side
    private String side;

    @CsvBindByPosition(position = 12)  // position
    private String position;

    @CsvBindByPosition(position = 13)  // playername
    private String playerName;

    @CsvBindByPosition(position = 14)  // playerid
    private String playerId;

    @CsvBindByPosition(position = 15)  // teamname
    private String teamName;

    @CsvBindByPosition(position = 17)  // firstpick
    private String firstPick;

    @CsvBindByPosition(position = 18)  // champion
    private String champion;

    @CsvBindByPosition(position = 19)  // ban1
    private String ban1;
    @CsvBindByPosition(position = 20)  // ban2
    private String ban2;
    @CsvBindByPosition(position = 21)  // ban3
    private String ban3;
    @CsvBindByPosition(position = 22)  // ban4
    private String ban4;
    @CsvBindByPosition(position = 23)  // ban5
    private String ban5;
    @CsvBindByPosition(position = 24)  // pick1
    private String pick1;
    @CsvBindByPosition(position = 25)  // pick2
    private String pick2;
    @CsvBindByPosition(position = 26)  // pick3
    private String pick3;
    @CsvBindByPosition(position = 27)  // pick4
    private String pick4;
    @CsvBindByPosition(position = 28)  // pick5
    private String pick5;

    @CsvBindByPosition(position = 30)  // result
    private String result;

    @CsvBindByPosition(position = 126)  // result
    private String goldDiffAt15;
    @CsvBindByPosition(position = 127)  // result
    private String xpDiffAt15;
    @CsvBindByPosition(position = 128)  // result
    private String csDiffAt15;


    // getters/setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    public String getLeague() { return league; }
    public void setLeague(String league) { this.league = league; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public String getPatch() { return patch; }
    public void setPatch(String patch) { this.patch = patch; }
    public String getSide() { return side; }
    public void setSide(String side) { this.side = side; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPlayerName() { return playerName; }
    public void setPlayerName(String playerName) { this.playerName = playerName; }
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    public String getTeamName() { return teamName; }
    public void setTeamName(String teamName) { this.teamName = teamName; }
    public String getFirstPick() { return firstPick; }
    public void setFirstPick(String firstPick) { this.firstPick = firstPick; }

    public String getChampion() {
        return champion;
    }

    public void setChampion(String champion) {
        this.champion = champion;
    }

    public String getBan1() {
        return ban1;
    }

    public void setBan1(String ban1) {
        this.ban1 = ban1;
    }

    public String getBan2() {
        return ban2;
    }

    public void setBan2(String ban2) {
        this.ban2 = ban2;
    }

    public String getBan3() {
        return ban3;
    }

    public void setBan3(String ban3) {
        this.ban3 = ban3;
    }

    public String getBan4() {
        return ban4;
    }

    public void setBan4(String ban4) {
        this.ban4 = ban4;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getPick5() {
        return pick5;
    }

    public void setPick5(String pick5) {
        this.pick5 = pick5;
    }

    public String getPick4() {
        return pick4;
    }

    public void setPick4(String pick4) {
        this.pick4 = pick4;
    }

    public String getPick3() {
        return pick3;
    }

    public void setPick3(String pick3) {
        this.pick3 = pick3;
    }

    public String getPick2() {
        return pick2;
    }

    public void setPick2(String pick2) {
        this.pick2 = pick2;
    }

    public String getPick1() {
        return pick1;
    }

    public void setPick1(String pick1) {
        this.pick1 = pick1;
    }

    public String getBan5() {
        return ban5;
    }

    public void setBan5(String ban5) {
        this.ban5 = ban5;
    }

    public String getGoldDiffAt15() {
        return goldDiffAt15;
    }

    public void setGoldDiffAt15(String goldDiffAt15) {
        this.goldDiffAt15 = goldDiffAt15;
    }

    public String getXpDiffAt15() {
        return xpDiffAt15;
    }

    public void setXpDiffAt15(String xpDiffAt15) {
        this.xpDiffAt15 = xpDiffAt15;
    }

    public String getCsDiffAt15() {
        return csDiffAt15;
    }

    public void setCsDiffAt15(String csDiffAt15) {
        this.csDiffAt15 = csDiffAt15;
    }
}