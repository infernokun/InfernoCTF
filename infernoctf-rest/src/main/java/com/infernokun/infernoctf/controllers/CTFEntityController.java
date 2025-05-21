package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.entities.CTFEntity;
import com.infernokun.infernoctf.models.entities.Flag;
import com.infernokun.infernoctf.services.CTFEntityService;
import com.infernokun.infernoctf.services.FlagService;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/ctf-entity")
public class CTFEntityController {
    private final CTFEntityService ctfEntityService;
    private final FlagService flagService;

    @Autowired
    private EntityManager entityManager;

    public CTFEntityController(CTFEntityService ctfEntityService, FlagService flagService) {
        this.ctfEntityService = ctfEntityService;
        this.flagService = flagService;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CTFEntity>>> getAllCTFEntities() {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<List<CTFEntity>>builder()
                        .code(HttpStatus.OK.value())
                        .message("All entities obtained!")
                        .data(ctfEntityService.findAllCTFEntities())
                        .build());
    }

    @GetMapping("/room/{roomId}")
    public ResponseEntity<ApiResponse<List<CTFEntity>>> getByRoomId(@PathVariable String roomId) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<List<CTFEntity>>builder()
                        .code(HttpStatus.OK.value())
                        .message("Obtained room by id!")
                        .data(ctfEntityService.findCTFEntitiesByRoomId(roomId))
                        .build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CTFEntity>> getCTFEntityById(@PathVariable String id) {
        CTFEntity ctfEntity = ctfEntityService.findCTFEntityById(id);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<CTFEntity>builder()
                        .code(HttpStatus.OK.value())
                        .message("Obtained CTFEntity by id!")
                        .data(ctfEntity)
                        .build());
    }

    @PostMapping
    public ResponseEntity<ApiResponse<CTFEntity>> createCTFEntity(@RequestBody CTFEntity ctfEntity) {
        // Save the CTFEntity first to generate an ID
        CTFEntity savedCTFEntity = ctfEntityService.saveCTFEntity(ctfEntity);

        // Set the saved CTFEntity in the flags and save them
        for (Flag flag : savedCTFEntity.getFlags()) {
            // Set the CTFEntity reference
            flag.setCtfEntity(savedCTFEntity);

            // Save the flag now that it has a persistent CTFEntity reference
            this.flagService.saveFlag(flag);
        }

        // Return the saved CTFEntity in the response
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<CTFEntity>builder()
                        .code(HttpStatus.OK.value())
                        .message("Created new CTFEntity!")
                        .data(savedCTFEntity)
                        .build());
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
    public ResponseEntity<ApiResponse<CTFEntity>> updateCTFEntity(@PathVariable String id) {
        CTFEntity ctfEntity = ctfEntityService.findCTFEntityById(id);
        CTFEntity updatedEntity = ctfEntityService.saveCTFEntity(ctfEntity);
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<CTFEntity>builder()
                        .code(HttpStatus.OK.value())
                        .message("Updated the entity")
                        .data(updatedEntity)
                        .build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Boolean>> deleteCTFEntity(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Boolean>builder()
                        .code(HttpStatus.OK.value())
                        .message("Entity item deleted successfully.")
                        .data(ctfEntityService.deleteCTFEntity(id))
                        .build());
    }
}
