package com.infernokun.services;

import com.infernokun.models.entities.Room;
import com.infernokun.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RoomService {
    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public List<Room> findAllRooms() {
        return this.roomRepository.findAll();
    }

    public List<Room> saveRooms(List<Room> rooms) {
        return this.roomRepository.saveAll(rooms);
    }
}
