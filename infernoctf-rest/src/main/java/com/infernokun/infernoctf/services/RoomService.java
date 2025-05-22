package com.infernokun.infernoctf.services;

import com.infernokun.infernoctf.exceptions.RoomNotFoundException;
import com.infernokun.infernoctf.models.entities.Room;
import com.infernokun.infernoctf.repositories.RoomRepository;
import org.springframework.stereotype.Service;

import java.util.List;

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

    public Room findByRoomId(String id) {
        return roomRepository.findById(id).orElseThrow(
                () -> new RoomNotFoundException("Room with id '" + id + "' not found."));
    }
    public List<Room> findAllRooms() {
        return roomRepository.findAll();
    }

    public Room saveRoom(Room room) { return roomRepository.save(room); }

    public List<Room> saveRooms(List<Room> rooms) {
        return roomRepository.saveAll(rooms);
    }

    public void deleteRoom(String id) {
        roomRepository.deleteById(id);
    }
}
