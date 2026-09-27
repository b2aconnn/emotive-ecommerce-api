package com.loopers.application.brand;

import java.util.List;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.loopers.application.brand.dto.BrandResult;
import com.loopers.application.brand.dto.BrandsCondition;
import com.loopers.application.brand.dto.BrandsResult;
import com.loopers.domain.brand.Brand;
import com.loopers.domain.brand.BrandRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class BrandService {

    private final BrandRepository brandRepository;

    public List<BrandsResult> getAll(BrandsCondition condition) {
        List<Brand> brands = brandRepository.findAll(condition.searchKeyword(), condition.offset(), condition.size());

        return brands.stream().map(BrandsResult::from).toList();
    }

    public BrandResult getBrand(Long brandId) {
        Brand brand = brandRepository
                .findById(brandId)
                .orElseThrow(() -> new EntityNotFoundException("[brandId = " + brandId + "] 브랜드를 찾을 수 없습니다."));

        return BrandResult.from(brand);
    }
}
