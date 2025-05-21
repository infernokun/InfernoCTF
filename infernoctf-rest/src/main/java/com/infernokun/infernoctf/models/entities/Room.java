package com.infernokun.infernoctf.models.entities;

import com.infernokun.infernoctf.models.constants.InfernoCTFConstants;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Room extends StoredObject {
    private String name;
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "creators")
    private User creator;
    @ElementCollection
    private List<String> facilitators = new ArrayList<>();
    private String surroundTag = InfernoCTFConstants.DEFAULT_SURROUND_TAG;
}
