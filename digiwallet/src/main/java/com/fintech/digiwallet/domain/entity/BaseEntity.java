package com.fintech.digiwallet.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;




@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)

public abstract class BaseEntity {


    @Id
    @GeneratedValue
    @JdbcTypeCode(SqlTypes.UUID)
    @Column(updatable = false, nullable = false)
    private UUID id;


    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;
}