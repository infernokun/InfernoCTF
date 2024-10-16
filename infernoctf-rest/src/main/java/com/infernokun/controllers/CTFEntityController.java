package com.infernokun.controllers;

import com.infernokun.models.entities.CTFEntity;
import com.infernokun.models.entities.Flag;
import com.infernokun.services.CTFEntityService;
import com.infernokun.services.FlagService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("ctf-entity")
public class CTFEntityController {
    private final Logger LOGGER =  LoggerFactory.getLogger(CTFEntityController.class);
    private final CTFEntityService ctfEntityService;
    private final FlagService flagService;

    @Autowired
    private EntityManager entityManager;

    public CTFEntityController(CTFEntityService ctfEntityService, FlagService flagService) {
        this.ctfEntityService = ctfEntityService;
        this.flagService = flagService;
    }

    @GetMapping
    public ResponseEntity<List<CTFEntity>> getAllCTFEntities() {
        Optional<List<CTFEntity>> ctfEntities = ctfEntityService.findAllCTFEntities();
        return ctfEntities.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<List<CTFEntity>> getByRoomId(@PathVariable String roomId) {
        Optional<List<CTFEntity>> ctfEntities = this.ctfEntityService.findCTFEntityByRoomId(roomId);
        return ctfEntities.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CTFEntity> getCTFEntityById(@PathVariable String id) {
        Optional<CTFEntity> ctfEntity = ctfEntityService.findCTFEntityById(id);
        return ctfEntity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<CTFEntity> createCTFEntity(@RequestBody CTFEntity ctfEntity) {
        // Save the CTFEntity first to generate an ID
        Optional<CTFEntity> savedCTFEntityOptional = ctfEntityService.saveCTFEntity(ctfEntity);

        // If the CTFEntity wasn't saved successfully, return not found
        if (savedCTFEntityOptional.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        CTFEntity savedCTFEntity = savedCTFEntityOptional.get();

        // Set the saved CTFEntity in the flags and save them
        for (Flag flag : savedCTFEntity.getFlags()) {
            if (flag.getId() == null) {
                flag.setId(UUID.randomUUID().toString());
            }
            // Set the CTFEntity reference
            flag.setCtfEntity(savedCTFEntity);

            // Save the flag now that it has a persistent CTFEntity reference
            this.flagService.saveFlag(flag);
        }

        // Return the saved CTFEntity in the response
        return ResponseEntity.ok(savedCTFEntity);
    }


    @PostMapping("/many")
    public ResponseEntity<List<CTFEntity>> createCTFEntities(@RequestBody List<CTFEntity> ctfEntities) {
        ctfEntities.forEach(entity ->
                entity.getFlags().forEach(flag -> {
                    if (flag.getId() == null) {
                        flag.setId(UUID.randomUUID().toString());
                    }
                    if (flag.getCtfEntity() == null) {
                        flag.setCtfEntity(entity);
                    }
                })
        );
        Optional<List<CTFEntity>> savedEntity = ctfEntityService.savaAll(ctfEntities);
        return savedEntity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<CTFEntity> updateCTFEntity(@PathVariable UUID id, @RequestBody CTFEntity ctfEntity) {
        Optional<CTFEntity> updatedEntity = ctfEntityService.saveCTFEntity(ctfEntity);
        return updatedEntity.map(ResponseEntity::ok).orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCTFEntity(@PathVariable String id) {
        Optional<CTFEntity> entityOptional = ctfEntityService.findCTFEntityById(id);
        if (entityOptional.isPresent()) {
            ctfEntityService.deleteCTFEntity(id);
            return ResponseEntity.noContent().build();
        } else {
            return ResponseEntity.notFound().build();
        }
    }
}
