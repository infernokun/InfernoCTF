package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.ResourceNotFoundException;
import com.infernokun.infernoctf.models.entities.CTFEntity;
import com.infernokun.infernoctf.models.entities.Flag;
import com.infernokun.infernoctf.repositories.CTFEntityRepository;
import com.infernokun.infernoctf.repositories.FlagRepository;
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

    public CTFEntity saveCTFEntity(CTFEntity ctfEntity) {
        return ctfEntityRepository.save(ctfEntity);
    }

    public Optional<List<Flag>> saveFlags(List<Flag> flags) {
        return Optional.of(flagRepository.saveAll(flags));
    }

    public List<CTFEntity> findAllCTFEntities() {
        return ctfEntityRepository.findAll();
    }

    public CTFEntity findCTFEntityById(String id) {
        return ctfEntityRepository.findById((id)).orElseThrow(() -> new ResourceNotFoundException("No entity by id " + id + " found!"));
    }


    public boolean deleteCTFEntity(String id) {
        if (!ctfEntityRepository.existsById(id)) {
            return false;
        }

        ctfEntityRepository.deleteById(id);
        return true;
    }

    public Optional<List<CTFEntity>> savaAll(List<CTFEntity> ctfEntities) {
        return Optional.of(ctfEntityRepository.saveAll(ctfEntities));
    }

    public List<CTFEntity> findCTFEntitiesByRoomId(String roomId) {
        return ctfEntityRepository.findByRoomId(roomId);
    }
}
