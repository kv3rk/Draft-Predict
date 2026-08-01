package lol.kv3rk.draft_predict.common.DateAndTime;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@TestComponent
public class TestDateAndTime {

    @Test
    public void assertionOfEpochStartTime() {

        LocalDateTime yesterday = LocalDate.now().minusDays(1).atStartOfDay();

        long yesterdayStartTime = yesterday.toEpochSecond(ZoneOffset.UTC);

        assertThat(yesterdayStartTime).isEqualTo(1785456000);

    }

    @Test
    public void assertionOfEpochEndTime() {

        LocalDateTime today = LocalDate.now().atStartOfDay();

        long todayEndTime = today.toEpochSecond(ZoneOffset.UTC);

        assertThat(todayEndTime).isEqualTo(1785542400);

    }

}
