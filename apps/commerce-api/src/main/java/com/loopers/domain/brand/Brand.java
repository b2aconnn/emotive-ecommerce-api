package com.loopers.domain.brand;

import com.loopers.domain.BaseEntity;
import com.loopers.domain.brand.dto.command.BrandCreateCommand;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import static lombok.AccessLevel.PROTECTED;

@Getter
@NoArgsConstructor(access = PROTECTED)
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
        return new Brand(
                createCommand.name(),
                createCommand.logoUrl(),
                createCommand.description());
    }
}
