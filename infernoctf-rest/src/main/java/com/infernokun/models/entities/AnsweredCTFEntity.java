package com.infernokun.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "answered_ctf_entity")
public class AnsweredCTFEntity {
    @Id
    @GeneratedValue(generator = "UUID")
    @GenericGenerator(
            name = "UUID",
            strategy = "org.hibernate.id.UUIDGenerator"
    )
    private String id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "ctf_entity_id")
    private CTFEntity ctfEntity;
    private List<String> answers = new ArrayList<>();
    private List<LocalDateTime> times = new ArrayList<>();
    private int attempts;
    private boolean correct = false;
}