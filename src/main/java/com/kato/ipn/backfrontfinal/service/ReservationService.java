package com.kato.ipn.backfrontfinal.service;

import com.kato.ipn.backfrontfinal.model.Reservation;
import com.kato.ipn.backfrontfinal.model.Room;
import com.kato.ipn.backfrontfinal.model.User;
import com.kato.ipn.backfrontfinal.repository.ReservationRepository;
import com.kato.ipn.backfrontfinal.repository.RoomRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ReservationService {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RoomRepository roomRepository;

    public List<Reservation> findAll() {
        return reservationRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Reservation> findByUser(User user) {
        return reservationRepository.findByUserOrderByCreatedAtDesc(user);
    }

    public Optional<Reservation> findById(Long id) {
        return reservationRepository.findById(id);
    }

    public Reservation create(Reservation reservation, User user) {
        if (!reservation.getCheckOutDate().isAfter(reservation.getCheckInDate())) {
            throw new RuntimeException("La fecha de salida debe ser posterior a la fecha de entrada");
        }

        List<Reservation> conflicts = reservationRepository.findConflictingReservations(
            reservation.getRoom().getId(),
            reservation.getCheckInDate(),
            reservation.getCheckOutDate()
        );
        if (!conflicts.isEmpty()) {
            throw new RuntimeException("La habitación no está disponible en las fechas seleccionadas");
        }

        Room room = roomRepository.findById(reservation.getRoom().getId())
            .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));

        long nights = ChronoUnit.DAYS.between(reservation.getCheckInDate(), reservation.getCheckOutDate());
        reservation.setTotalPrice(room.getPricePerNight().multiply(BigDecimal.valueOf(nights)));
        reservation.setUser(user);
        reservation.setStatus("PENDIENTE");

        return reservationRepository.save(reservation);
    }

    public Reservation update(Long id, Reservation data) {
        Reservation existing = reservationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reservación no encontrada"));

        if (!data.getCheckOutDate().isAfter(data.getCheckInDate())) {
            throw new RuntimeException("La fecha de salida debe ser posterior a la fecha de entrada");
        }

        existing.setCheckInDate(data.getCheckInDate());
        existing.setCheckOutDate(data.getCheckOutDate());
        existing.setGuestName(data.getGuestName());
        existing.setGuestCount(data.getGuestCount());
        existing.setNotes(data.getNotes());

        long nights = ChronoUnit.DAYS.between(existing.getCheckInDate(), existing.getCheckOutDate());
        existing.setTotalPrice(existing.getRoom().getPricePerNight().multiply(BigDecimal.valueOf(nights)));

        return reservationRepository.save(existing);
    }

    public void cancel(Long id, String username) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reservación no encontrada"));

        if (!reservation.getUser().getUsername().equals(username)) {
            throw new RuntimeException("No tiene permiso para cancelar esta reservación");
        }
        if ("CANCELADA".equals(reservation.getStatus())) {
            throw new RuntimeException("La reservación ya está cancelada");
        }

        reservation.setStatus("CANCELADA");
        reservationRepository.save(reservation);
    }

    public void cancelAdmin(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reservación no encontrada"));
        reservation.setStatus("CANCELADA");
        reservationRepository.save(reservation);
    }

    public void confirm(Long id) {
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Reservación no encontrada"));
        if ("CANCELADA".equals(reservation.getStatus())) {
            throw new RuntimeException("No se puede confirmar una reservación cancelada");
        }
        reservation.setStatus("CONFIRMADA");
        reservationRepository.save(reservation);
    }
}
