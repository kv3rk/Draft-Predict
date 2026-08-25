package lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.Repository;

import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqDbRequests.DTO.CounterPick;
import lol.kv3rk.draft_predict.ServerApplication.SoloqRanked.SoloqEntities.Participants.Entity.ParticipantsEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;
@Repository
public interface CounterPickRequests extends JpaRepository<ParticipantsEntity, UUID> {

    @Query(
            nativeQuery = true,
            value = """
                    with total_position_matches as (
                    select
                    	p.champion,
                    	count(p.champion) as total
                    from
                    	participants p
                    join matches m on
                    	m.match_id = p.match_id
                    where
                    	m.patch like :patch
                    	and p.position = :lane
                    group by
                    	p.champion
                    ),
                    avg_position_matches as(
                    	select
                    	round(avg(tpm.total), 0) as total_avg
                    from
                    	total_position_matches tpm
                    )
                    select
                    	p.champion as champion1,
                    	p2.champion as champion2,
                    	round(avg(p.gold - p2.gold), 1) as gold,
                    	round(avg(p.xp - p2.xp), 1) as xp,
                    	round(avg(p.farm - p2.farm), 1) as farm
                    from
                    	participants p
                    join participants p2
                        on
                    	p.match_id = p2.match_id
                    	and p.position = p2.position
                    join matches m
                        on
                    	m.match_id = p.match_id
                    join total_position_matches tpm on
                    	tpm.champion = p2.champion
                    cross join avg_position_matches apm
                    where
                    	p.xp > 1
                    	and p2.xp > 1
                    	and p.champion = :champion1
                    	and p.position = :lane
                    	and m.patch like :patch
                    	and apm.total_avg < tpm.total
                    	and p.champion <> p2.champion
                    group by
                    	p.champion,
                    	p2.champion
                    having
                    	round(avg(p.gold - p2.gold), 1) <= 0
                        and round(avg(p.xp - p2.xp), 1) <= 0
                        and round(avg(p.farm - p2.farm), 1) <= 0
                    order by
                    	gold asc,
                    	xp asc,
                    	farm asc;
                    """
    )
    List<CounterPick> getWorstMatchups(
            @Param("champion1") String champion1,
            @Param("lane") String lane,
            @Param("patch") String patch
    );

    @Query(
            nativeQuery = true,
            value = """
                    with total_position_matches as (
                    select
                    	p.champion,
                    	count(p.champion) as total
                    from
                    	participants p
                    join matches m on
                    	m.match_id = p.match_id
                    where
                    	m.patch like :patch
                    	and p.position = :lane
                    group by
                    	p.champion
                    ),
                    avg_position_matches as(
                    	select
                    	round(avg(tpm.total), 0) as total_avg
                    from
                    	total_position_matches tpm
                    )
                    select
                    	p.champion as champion1,
                    	p2.champion as champion2,
                    	round(avg(p.gold - p2.gold), 1) as gold,
                    	round(avg(p.xp - p2.xp), 1) as xp,
                    	round(avg(p.farm - p2.farm), 1) as farm
                    from
                    	participants p
                    join participants p2
                        on
                    	p.match_id = p2.match_id
                    	and p.position = p2.position
                    join matches m
                        on
                    	m.match_id = p.match_id
                    join total_position_matches tpm on
                    	tpm.champion = p2.champion
                    cross join avg_position_matches apm
                    where
                    	p.xp > 1
                    	and p2.xp > 1
                    	and p.champion = :champion1
                    	and p.position = :lane
                    	and m.patch like :patch
                    	and apm.total_avg < tpm.total
                    	and p.champion <> p2.champion
                    group by
                    	p.champion,
                    	p2.champion
                    having
                    	round(avg(p.gold - p2.gold), 1) >= 0
                        and round(avg(p.xp - p2.xp), 1) >= 0
                        and round(avg(p.farm - p2.farm), 1) >= 0
                    order by
                    	gold desc,
                    	xp desc,
                    	farm desc;
                    """
    )
    List<CounterPick> getBestMatchups(
            @Param("champion1") String champion1,
            @Param("lane") String lane,
            @Param("patch") String patch
    );
}
