package com.loopers.application.brand;

import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@RequiredArgsConstructor
@Service
public class BrandAppService {

    private final BrandRepository brandRepository;

    public BrandResult getBrand(Long brandId) {
        Optional<Brand> brandOptional = brandRepository.findById(brandId);
        return brandOptional.map(BrandResult::from).orElse(null);
    }
}
