package lol.kv3rk.draft_predict.DefaultPipeline.Service;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.TestComponent;

import java.util.Set;

import static org.assertj.core.api.Assertions.*;

@TestComponent
public class TestGatherMatchInfo {


    @Test
    public void testOptionalSetElement() {

        Set<String> set = Set.of("Element1", "Element2");

        String e1 = set.stream().findFirst().get();

        assertThat(e1).isEqualTo("Element1");

    }
}
