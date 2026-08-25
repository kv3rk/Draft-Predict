package lol.kv3rk.draft_predict.ServerApplication.RankedSoloQ.RankedDbRequests.DTO;

public interface BestDuo {

    String getChampion1();

    String getChampion2();

    int getPickRate();

    double getWinRate();
}
