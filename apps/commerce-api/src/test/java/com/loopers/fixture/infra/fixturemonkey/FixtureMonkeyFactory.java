package com.loopers.fixture.infra.fixturemonkey;

import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;

public class FixtureMonkeyFactory {
    public static FixtureMonkey create() {
        return FixtureMonkey.builder()
                .objectIntrospector(FieldReflectionArbitraryIntrospector.INSTANCE)
                .defaultNotNull(true)
                .build();
    }
}
