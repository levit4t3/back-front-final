package com.kato.ipn.backfrontfinal.repository;

import com.kato.ipn.backfrontfinal.model.Reservation;
import com.kato.ipn.backfrontfinal.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    List<Reservation> findByUserOrderByCreatedAtDesc(User user);
    List<Reservation> findAllByOrderByCreatedAtDesc();

    @Query("SELECT r FROM Reservation r WHERE r.room.id = :roomId AND r.status != 'CANCELADA' AND r.checkInDate < :checkOut AND r.checkOutDate > :checkIn")
    List<Reservation> findConflictingReservations(
        @Param("roomId") Long roomId,
        @Param("checkIn") LocalDate checkIn,
        @Param("checkOut") LocalDate checkOut
    );
}
