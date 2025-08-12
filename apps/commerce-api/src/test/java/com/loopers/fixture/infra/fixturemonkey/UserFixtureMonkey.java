package com.loopers.fixture.infra.fixturemonkey;

import com.loopers.domain.user.User;
import com.loopers.domain.user.UserRepository;
import com.loopers.fixture.user.UserFixture;
import com.navercorp.fixturemonkey.FixtureMonkey;
import com.navercorp.fixturemonkey.api.introspector.FieldReflectionArbitraryIntrospector;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;

@Component
public class UserFixtureMonkey implements UserFixture {
    @Autowired
    private UserRepository userRepository;

    @Override
    public List<User> create(int count) {
        return FixtureMonkeyFactory.create().giveMeBuilder(User.class)
                .set("id", 0L)
                .sampleList(count);
    }

    @Override
    public User create() {
        return Objects.requireNonNull(create(1)).get(0);
    }

    @Override
    public User save() {
        return userRepository.save(create());
    }

    @Override
    public List<User> save(int count) {
        List<User> users = create(count);
        return userRepository.saveAll(users);
    }
}
