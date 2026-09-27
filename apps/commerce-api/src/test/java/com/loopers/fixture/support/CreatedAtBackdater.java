package com.loopers.fixture.support;

import java.time.ZonedDateTime;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@code BaseEntity.createdAt}은 {@code @PrePersist}에서 항상 현재 시각으로 채워지고
 * {@code updatable = false}라 JPA로는 바꿀 수 없다. 기간 경계([start, end) 반개구간) 검증처럼
 * 과거 시각의 데이터가 필요한 테스트를 위해 네이티브 쿼리로만 소급 조정한다.
 */
@Component
public class CreatedAtBackdater {

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional
    public void backdate(String tableName, Long id, ZonedDateTime createdAt) {
        entityManager
                .createNativeQuery("UPDATE `" + tableName + "` SET created_at = :createdAt WHERE id = :id")
                .setParameter("createdAt", createdAt)
                .setParameter("id", id)
                .executeUpdate();
    }
}
