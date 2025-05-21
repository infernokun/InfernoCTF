package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.ApiResponse;
import com.infernokun.infernoctf.models.entities.CTFEntity;
import com.infernokun.infernoctf.models.entities.Room;
import com.infernokun.infernoctf.services.RoomService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/room")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(this.roomService.findAllRooms());
    }

    @GetMapping("/byName")
    public ResponseEntity<Room> getRoomByName(@RequestParam("name") String roomName) {
        if (roomName == null || roomName.isEmpty()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        Room room = this.roomService.findByRoomName(roomName);
        if (room == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.status(HttpStatus.OK).body(room);
    }

    @PostMapping
    public ResponseEntity<ApiResponse<Room>> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(ApiResponse.<Room>builder()
                        .code(HttpStatus.OK.value())
                        .message("Lab stopped successfully.")
                        .data(this.roomService.saveRoom(room))
                        .build());
    }

    @PostMapping("many")
    public ResponseEntity<List<Room>> createRooms(@RequestBody List<Room> rooms) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roomService.saveRooms(rooms));
    }
}
