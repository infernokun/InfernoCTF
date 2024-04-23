package com.infernokun.models.entities;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;


@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "flags")
public class Flag {
    @Id
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
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
