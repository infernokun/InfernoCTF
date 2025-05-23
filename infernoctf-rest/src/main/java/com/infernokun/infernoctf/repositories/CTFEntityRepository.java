package com.infernokun.infernoctf.repositories;

import com.infernokun.infernoctf.models.entities.CTFEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CTFEntityRepository extends JpaRepository<CTFEntity, String> {

    @Query("SELECT DISTINCT c FROM CTFEntity c LEFT JOIN FETCH c.flags")
    List<CTFEntity> findAllWithFlags();

    List<CTFEntity> findByRoomId(String roomId);
}