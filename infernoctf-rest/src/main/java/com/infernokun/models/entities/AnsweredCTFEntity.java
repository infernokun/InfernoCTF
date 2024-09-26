package com.infernokun.models.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UuidGenerator;

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
    @UuidGenerator
    private String id;
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
    @ManyToOne
    @JoinColumn(name = "ctf_entity_id")
    private CTFEntity ctfEntity;
    @Builder.Default
    private List<String> answers = new ArrayList<>();
    @Builder.Default
    private List<LocalDateTime> times = new ArrayList<>();
    private int attempts;
    @Builder.Default
    private boolean correct = false;
}