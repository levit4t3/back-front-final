package com.kato.ipn.backfrontfinal.config;

import com.kato.ipn.backfrontfinal.model.Role;
import com.kato.ipn.backfrontfinal.model.Room;
import com.kato.ipn.backfrontfinal.repository.RoleRepository;
import com.kato.ipn.backfrontfinal.repository.RoomRepository;
import com.kato.ipn.backfrontfinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private RoomRepository roomRepository;

    @Override
    public void run(String... args) {
        if (roleRepository.findByName("ROLE_ADMIN").isEmpty()) {
            roleRepository.save(new Role("ROLE_ADMIN"));
        }
        if (roleRepository.findByName("ROLE_USUARIO").isEmpty()) {
            roleRepository.save(new Role("ROLE_USUARIO"));
        }

        try {
            userService.registerUser("admin", "admin123", "admin@hotel.com", "ROLE_ADMIN");
            System.out.println("==> Usuario admin creado: admin / admin123");
        } catch (Exception ignored) {
            // El usuario ya existe
        }

        try {
            userService.registerUser("usuario", "usuario123", "usuario@hotel.com", "ROLE_USUARIO");
            System.out.println("==> Usuario demo creado: usuario / usuario123");
        } catch (Exception ignored) {
            // El usuario ya existe
        }

        if (roomRepository.count() == 0) {
            addRoom("101", "SIMPLE", 1, "500.00", "Habitación simple con cama individual y baño privado");
            addRoom("102", "SIMPLE", 1, "500.00", "Habitación simple con vista al jardín");
            addRoom("201", "DOBLE", 2, "800.00", "Habitación doble con cama matrimonial");
            addRoom("202", "DOBLE", 2, "850.00", "Habitación doble con vista a la piscina");
            addRoom("301", "SUITE", 4, "1500.00", "Suite ejecutiva con sala de estar y jacuzzi");
            System.out.println("==> Habitaciones de ejemplo creadas");
        }
    }

    private void addRoom(String number, String type, int capacity, String price, String desc) {
        Room room = new Room();
        room.setRoomNumber(number);
        room.setType(type);
        room.setCapacity(capacity);
        room.setPricePerNight(new BigDecimal(price));
        room.setStatus("DISPONIBLE");
        room.setDescription(desc);
        roomRepository.save(room);
    }
}
