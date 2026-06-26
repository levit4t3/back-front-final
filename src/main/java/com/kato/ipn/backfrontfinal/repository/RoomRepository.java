package com.kato.ipn.backfrontfinal.repository;

import com.kato.ipn.backfrontfinal.model.Room;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByStatus(String status);
    boolean existsByRoomNumber(String roomNumber);
}
