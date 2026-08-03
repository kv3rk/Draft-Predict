package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;

import java.math.BigDecimal;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@TestComponent
public class TestGatherMatchInfo {


    @Test
    public void testOptionalSetElement() {

        Set<String> set = Set.of("Element1", "Element2");

        String e1 = set.stream().findFirst().get();

        assertThat(e1).isEqualTo("Element1");

    }

    @Test
    public void testSubStringConvertToDouble() {

        String str = "16.15.1".substring(0, 5);

        BigDecimal value2 = new BigDecimal(str);

        double value = Double.parseDouble(str);

        assertThat(value).isEqualTo(16.15);
        assertThat(value2).isEqualTo(BigDecimal.valueOf(16.15));
    }
}
