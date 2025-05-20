package com.infernokun.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flags")
public class Flag {
    @Id
    @UuidGenerator
    private String id;
    private String flag;
    private Boolean surroundWithTag;
    private Boolean caseSensitive;
    private Double weight;
    @ManyToOne
    @JoinColumn(name = "ctf_entity_id")
    @JsonBackReference
    private CTFEntity ctfEntity;
}
