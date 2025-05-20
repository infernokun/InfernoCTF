package com.infernokun.repositories;

import com.infernokun.models.entities.Flag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlagRepository extends JpaRepository<Flag, String> {
}
