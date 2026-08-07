package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;

import java.time.*;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

@TestComponent
public class TestSaveMatchInfo {

    @Test
    public void testEpochTimeToLocalDate() {

        Long epochTime = 1785087169577L;

        long seconds = epochTime / 1_000_000_000;
        long nanos = epochTime % 1_000_000_000;

        LocalDate date = LocalDate.from(
                LocalDateTime.ofEpochSecond(
                        Instant.ofEpochMilli(epochTime).getEpochSecond(),
                        0,
                        ZoneOffset.UTC
                )
        );

        LocalTime time = LocalTime.ofNanoOfDay(epochTime);

        int timezone = TimeZone.getDefault().getOffset(epochTime);

//        LocalDate dateFromTime = LocalDate.from(
//                time
//        );
//
//
//        LocalDate testDate = LocalDate.now();

        System.out.println(time);
        System.out.println(timezone);
        System.out.println(epochTime);
        System.out.println(date);

//        assertThat(dateFromTime).isEqualTo(testDate);
    }
}
