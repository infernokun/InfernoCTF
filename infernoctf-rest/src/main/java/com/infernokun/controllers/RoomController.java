package com.infernokun.controllers;

import com.infernokun.exceptions.RoomNotFoundException;
import com.infernokun.models.entities.Room;
import com.infernokun.services.RoomService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping("room")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public ResponseEntity<Room> getRoomByName(@RequestParam("name") String roomName) {
        return ResponseEntity.status(HttpStatus.OK).body(this.roomService.findByRoomName(roomName));
    }

    @GetMapping("all")
    public ResponseEntity<List<Room>> getAllRooms() {
        return ResponseEntity.status(HttpStatus.OK).body(this.roomService.findAllRooms());
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
