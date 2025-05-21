package com.infernokun.infernoctf.models.entities;

import com.infernokun.infernoctf.interfaces.JsonDate;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Version;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.UuidGenerator;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@MappedSuperclass
public abstract class StoredObject {
    @Id
    @UuidGenerator
    private String id;
    private String createdBy = "SYSTEM";
    private String updatedBy = "SYSTEM";
    @JsonDate
    private LocalDateTime createdAt = LocalDateTime.now();
    @JsonDate
    private LocalDateTime updatedAt = LocalDateTime.now();
    @Version
    private Integer versioning;
}