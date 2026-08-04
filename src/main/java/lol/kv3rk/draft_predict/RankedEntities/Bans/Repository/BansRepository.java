package lol.kv3rk.draft_predict.RankedEntities.Bans.Repository;

import lol.kv3rk.draft_predict.ClientApplication.DTO.MostBannedChampions;
import lol.kv3rk.draft_predict.RankedEntities.Bans.Entity.BansEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface BansRepository extends JpaRepository<BansEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_matches as(
                    	select
                    		count(match_id) * 2 as total
                    	from matches m
                    )
                    select
                    	b.champion as champion,
                    	count(b.champion) * 100 / tm.total as ban_rate
                    from bans b
                    cross join total_matches as tm
                    group by champion, tm.total
                    order by ban_rate desc
                    limit 5;
                    """
    )
    List<MostBannedChampions> getMostBannedChampions();
}
