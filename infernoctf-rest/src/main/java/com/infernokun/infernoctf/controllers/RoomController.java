package com.infernokun.infernoctf.controllers;

import com.infernokun.infernoctf.models.entities.Room;
import com.infernokun.infernoctf.services.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
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
    public ResponseEntity<Room> createRoom(@RequestBody Room room) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roomService.saveRoom(room));
    }

    @PostMapping("many")
    public ResponseEntity<List<Room>> createRooms(@RequestBody List<Room> rooms) {
        return ResponseEntity.status(HttpStatus.CREATED).body(this.roomService.saveRooms(rooms));
    }
}
