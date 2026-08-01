package lol.kv3rk.draft_predict.common.DateAndTime;

import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

@Component
public class CustomLocalDateAndTime {

    public long startTime() {

        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();

        long yesterdayStartTime = yesterday.toEpochSecond(ZoneOffset.UTC);

        return yesterdayStartTime;

    }

    public long endTime() {

        LocalDateTime today = LocalDate.now().atStartOfDay();

        long todayEndTime = today.toEpochSecond(ZoneOffset.UTC);

        return todayEndTime;

    }

}
