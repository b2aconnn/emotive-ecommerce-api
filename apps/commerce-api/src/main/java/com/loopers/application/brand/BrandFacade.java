package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BrandFacade {

    private final BrandRepository brandRepository;

    public BrandInfo getBrand(Long brandId) {
        Optional<Brand> brandOptional = brandRepository.findById(brandId);
        return brandOptional.map(BrandInfo::from).orElse(null);
    }
}
