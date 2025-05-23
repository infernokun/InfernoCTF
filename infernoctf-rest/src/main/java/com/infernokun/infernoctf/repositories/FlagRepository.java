package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.entities.Flag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FlagRepository extends JpaRepository<Flag, String> {
    List<Flag> getFlagsByCtfEntityId(String ctfEntityId);
}
