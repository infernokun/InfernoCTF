package com.infernokun.services;

import com.infernokun.models.entities.CTFEntity;
import com.infernokun.models.entities.Flag;
import com.infernokun.repositories.CTFEntityRepository;
import com.infernokun.repositories.FlagRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CTFEntityService {
    private final CTFEntityRepository ctfEntityRepository;
    private final FlagRepository flagRepository;

    public CTFEntityService(CTFEntityRepository ctfEntityRepository, FlagRepository flagRepository) {
        this.ctfEntityRepository = ctfEntityRepository;
        this.flagRepository = flagRepository;
    }

    public Optional<CTFEntity> saveCTFEntity(CTFEntity ctfEntity) {
        return Optional.of(this.ctfEntityRepository.save(ctfEntity));
    }

    public Optional<List<Flag>> saveFlags(List<Flag> flags) {
        return Optional.of(this.flagRepository.saveAll(flags));
    }

    public Optional<List<CTFEntity>> findAllCTFEntities() {
        return Optional.of(this.ctfEntityRepository.findAll());
    }

    public Optional<CTFEntity> findCTFEntityById(String id) {
        return this.ctfEntityRepository.findById((id));
    }

    public void deleteCTFEntity(String id) {
        this.ctfEntityRepository.deleteById(id);
    }

    public Optional<List<CTFEntity>> savaAll(List<CTFEntity> ctfEntities) {
        return Optional.of(this.ctfEntityRepository.saveAll(ctfEntities));
    }

    public Optional<List<CTFEntity>> findCTFEntityByRoomId(String roomId) {
        return this.ctfEntityRepository.findByRoomId(roomId);
    }
}
