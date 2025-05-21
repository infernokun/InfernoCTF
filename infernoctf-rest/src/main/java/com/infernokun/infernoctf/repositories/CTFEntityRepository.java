package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.entities.CTFEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CTFEntityRepository extends JpaRepository<CTFEntity, String> {
    List<CTFEntity> findByRoomId(String roomId);
}
