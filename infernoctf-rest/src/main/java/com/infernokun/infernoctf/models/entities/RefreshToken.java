package com.infernokun.infernoctf.models.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Stores only the SHA-256 hash of the grant; the token itself is returned once, at issue time.
 *
 * <p>The column keeps the name {@code token}: {@code ddl-auto: update} never drops columns, so
 * renaming it would leave a NOT NULL column that nothing populates and break every insert.
 */
@Data
@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "token", length = 1000, nullable = false, unique = true)
    private String tokenHash;

    private Instant creationDate;
    private Instant expirationDate;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
