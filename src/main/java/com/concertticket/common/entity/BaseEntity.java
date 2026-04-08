package com.concertticket.common.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass  // 자식 테이블에 컬럼 병합; base_entity 테이블은 생성되지 않음
@EntityListeners(AuditingEntityListener.class)  // Auditing 리스너 등록; @EnableJpaAuditing이 Application에 있어야 동작
@Getter
public abstract class BaseEntity {  // abstract: 직접 인스턴스화 방지

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)  // updatable=false: UPDATE 시 created_at 변경 방지
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}