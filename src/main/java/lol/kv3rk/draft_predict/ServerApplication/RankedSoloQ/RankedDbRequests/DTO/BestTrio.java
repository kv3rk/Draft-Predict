package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO;

public interface BestTrio {

    String getChampion1();

    String getChampion2();

    String getChampion3();

    int getPickRate();

    double getWinRate();
}
