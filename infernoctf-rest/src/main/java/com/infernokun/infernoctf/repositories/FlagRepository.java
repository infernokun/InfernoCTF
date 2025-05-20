package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.entities.Flag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FlagRepository extends JpaRepository<Flag, String> {
}
