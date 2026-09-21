package com.loopers.domain.brand;


import java.util.List;
import java.util.Optional;

public interface BrandRepository {
    Brand save(Brand brand);

    List<Brand> findAll(String searchKeyword, Integer offset, Integer size);

    Optional<Brand> findById(Long id);
}
