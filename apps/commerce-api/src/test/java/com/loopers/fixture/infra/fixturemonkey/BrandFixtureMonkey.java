package com.loopers.fixture.infra.fixturemonkey;

import java.util.List;
import java.util.Objects;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import com.loopers.fixture.infra.brand.BrandFixture;

@Component
public class BrandFixtureMonkey implements BrandFixture {
    @Autowired
    private BrandRepository brandRepository;

    private List<Brand> create(int count) {
        return FixtureMonkeyFactory.create()
                .giveMeBuilder(Brand.class)
                .set("id", 0L)
                .sampleList(count);
    }

    private Brand create() {
        return Objects.requireNonNull(create(1)).get(0);
    }

    @Override
    public Brand save() {
        Brand brand = create();
        return brandRepository.save(brand);
    }

    @Override
    public Brand save(String name) {
        Brand brand = Brand.create(
                new BrandCreateCommand(name, "http://example.com/" + name + ".png", name + " description"));
        return brandRepository.save(brand);
    }
}
