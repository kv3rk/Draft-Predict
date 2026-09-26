package lol.kv3rk.draft_predict.ServerApplication.GeneralInfo.Repository;

import lol.kv3rk.draft_predict.ServerApplication.GeneralInfo.Entity.GeneralInfoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

public interface GeneralInfoRepository extends JpaRepository<GeneralInfoEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = "SELECT parsed_lines FROM general_info LIMIT 1"
    )
    int getParsedLines();

    @Modifying
    @Transactional
    @Query(
            nativeQuery = true,
            value = "UPDATE general_info SET parsed_lines = parsed_lines + :addedLines"
    )
    void updateParsedLines(int addedLines);
}