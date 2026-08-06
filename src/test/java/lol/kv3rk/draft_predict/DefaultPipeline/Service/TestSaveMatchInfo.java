package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

@TestComponent
public class TestSaveMatchInfo {

    @Test
    public void testEpochTimeToLocalDate() {

        long epochTime = 1786004357;

        LocalDate dateFromTime = LocalDate.from(
                LocalDateTime.ofEpochSecond(epochTime, 0, ZoneOffset.UTC)
        );


        LocalDate testDate = LocalDate.now();

        assertThat(dateFromTime).isEqualTo(testDate);
    }
}
