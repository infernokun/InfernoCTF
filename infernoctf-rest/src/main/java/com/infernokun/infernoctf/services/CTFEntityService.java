package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.ResourceNotFoundException;
import com.infernokun.infernoctf.models.entities.CTFEntity;
import com.infernokun.infernoctf.models.entities.Flag;
import com.infernokun.infernoctf.repositories.CTFEntityRepository;
import com.infernokun.infernoctf.repositories.FlagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class CTFEntityService {

    private final CTFEntityRepository ctfEntityRepository;
    private final FlagRepository flagRepository;

    /**
     * Creates a new CTF entity with its associated flags
     */
    @Transactional
    public CTFEntity createCTFEntity(CTFEntity ctfEntity) {
        log.info("Creating CTF entity: {}", ctfEntity.getQuestion());

        validateCTFEntity(ctfEntity);

        // Save the CTF entity first to generate an ID
        CTFEntity savedEntity = ctfEntityRepository.save(ctfEntity);

        // Process and save flags if they exist
        if (!CollectionUtils.isEmpty(savedEntity.getFlags())) {
            processAndSaveFlags(savedEntity);
        }

        log.info("Successfully created CTF entity with id: {}", savedEntity.getId());
        return savedEntity;
    }

    /**
     * Creates multiple CTF entities in batch
     */
    @Transactional
    public List<CTFEntity> createCTFEntities(List<CTFEntity> ctfEntities) {
        log.info("Creating {} CTF entities", ctfEntities.size());

        if (CollectionUtils.isEmpty(ctfEntities)) {
            throw new IllegalArgumentException("CTF entities list cannot be empty");
        }

        // Save all entities
        List<CTFEntity> savedEntities = ctfEntityRepository.saveAll(ctfEntities);

        log.info("Successfully created {} CTF entities", savedEntities.size());
        return savedEntities;
    }

    /**
     * Updates an existing CTF entity
     */
    @Transactional
    public CTFEntity updateCTFEntity(String id, CTFEntity updatedEntity) {
        log.info("Updating CTF entity with id: {}", id);

        CTFEntity existingEntity = findCTFEntityById(id);

        // Update fields
        updateEntityFields(existingEntity, updatedEntity);

        CTFEntity savedEntity = ctfEntityRepository.save(existingEntity);

        log.info("Successfully updated CTF entity with id: {}", id);
        return savedEntity;
    }

    /**
     * Retrieves all CTF entities
     */
    public List<CTFEntity> findAllCTFEntities() {
        log.info("Fetching all CTF entities");
        return ctfEntityRepository.findAll();
    }

    /**
     * Retrieves a CTF entity by ID
     */
    public CTFEntity findCTFEntityById(String id) {
        log.info("Fetching CTF entity with id: {}", id);

        return ctfEntityRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("CTF entity not found with id: {}", id);
                    return new ResourceNotFoundException("CTF entity not found with id: " + id);
                });
    }

    /**
     * Retrieves CTF entities by room ID
     */
    public List<CTFEntity> findCTFEntitiesByRoomId(String roomId) {
        log.info("Fetching CTF entities for room: {}", roomId);

        if (roomId == null || roomId.trim().isEmpty()) {
            throw new IllegalArgumentException("Room ID cannot be null or empty");
        }

        return ctfEntityRepository.findByRoomId(roomId);
    }

    /**
     * Deletes a CTF entity by ID
     */
    @Transactional
    public void deleteCTFEntity(String id) {
        log.info("Deleting CTF entity with id: {}", id);

        if (!ctfEntityRepository.existsById(id)) {
            throw new ResourceNotFoundException("CTF entity not found with id: " + id);
        }

        ctfEntityRepository.deleteById(id);
        log.info("Successfully deleted CTF entity with id: {}", id);
    }

    /**
     * Checks if a CTF entity exists by ID
     */
    public boolean existsById(String id) {
        return ctfEntityRepository.existsById(id);
    }

    /**
     * Saves flags for a CTF entity
     */
    @Transactional
    public List<Flag> saveFlags(List<Flag> flags) {
        if (CollectionUtils.isEmpty(flags)) {
            return List.of();
        }

        return flagRepository.saveAll(flags);
    }

    // Private helper methods

    private void validateCTFEntity(CTFEntity ctfEntity) {
        if (ctfEntity == null) {
            throw new IllegalArgumentException("CTF entity cannot be null");
        }

        if (ctfEntity.getQuestion() == null || ctfEntity.getQuestion().trim().isEmpty()) {
            throw new IllegalArgumentException("CTF entity name cannot be null or empty");
        }
    }

    private void processAndSaveFlags(CTFEntity ctfEntity) {
        List<Flag> flags = ctfEntity.getFlags();

        flags.forEach(flag -> {
            flag.setCtfEntity(ctfEntity);
            flagRepository.save(flag);
        });

        log.info("Processed and saved {} flags for CTF entity: {}", flags.size(), ctfEntity.getId());
    }

    private void updateEntityFields(CTFEntity existingEntity, CTFEntity updatedEntity) {

        // Handle flags update if needed
        if (!CollectionUtils.isEmpty(updatedEntity.getFlags())) {
            // You might want to implement a more sophisticated flag update logic here
            existingEntity.setFlags(updatedEntity.getFlags());
        }
    }
}