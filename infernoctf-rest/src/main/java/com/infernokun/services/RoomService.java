package com.infernokun.services;

import com.infernokun.exceptions.RoomNotFoundException;
import com.infernokun.models.entities.Room;
import com.infernokun.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room findByRoomName(String roomName) throws RoomNotFoundException {
        return roomRepository.findByName(roomName)
                .orElseThrow(() -> new RoomNotFoundException("Room with name '" + roomName + "' not found."));
    }

    public List<Room> findAllRooms() {
        return this.roomRepository.findAll();
    }

    public Room saveRoom(Room room) { return this.roomRepository.save(room); }

    public List<Room> saveRooms(List<Room> rooms) {
        return this.roomRepository.saveAll(rooms);
    }
}
