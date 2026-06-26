package com.kato.ipn.backfrontfinal.service;

import com.kato.ipn.backfrontfinal.model.Room;
import com.kato.ipn.backfrontfinal.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RoomService {

    @Autowired
    private RoomRepository roomRepository;

    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    public List<Room> findAvailable() {
        return roomRepository.findByStatus("DISPONIBLE");
    }

    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    public Room save(Room room) {
        if (room.getId() == null && roomRepository.existsByRoomNumber(room.getRoomNumber())) {
            throw new RuntimeException("Ya existe una habitación con el número: " + room.getRoomNumber());
        }
        return roomRepository.save(room);
    }

    public void deleteById(Long id) {
        roomRepository.deleteById(id);
    }
}
