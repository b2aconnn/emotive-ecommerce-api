package com.loopers.domain.brand;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "brand")
@Entity
public class Brand extends BaseEntity {

    private String name;

    private String logoUrl;

    private String description;

    private Brand(String name, String logoUrl, String description) {
        this.name = name;
        this.logoUrl = logoUrl;
        this.description = description;
    }

    public static Brand create(BrandCreateCommand createCommand) {
        return new Brand(createCommand.name(), createCommand.logoUrl(), createCommand.description());
    }
}
