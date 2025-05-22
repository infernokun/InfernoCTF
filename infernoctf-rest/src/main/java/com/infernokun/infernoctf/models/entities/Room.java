package com.infernokun.infernoctf.models.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.infernokun.infernoctf.models.constants.InfernoCTFConstants;
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
public class Room extends StoredObject {
    private String name;
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creators")
    private User creator;
    @ElementCollection
    @JsonIgnore
    @Builder.Default
    private List<String> facilitators = new ArrayList<>();
    @Builder.Default
    private String surroundTag = InfernoCTFConstants.DEFAULT_SURROUND_TAG;
}
