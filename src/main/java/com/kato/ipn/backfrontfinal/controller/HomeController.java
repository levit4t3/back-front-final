package com.kato.ipn.backfrontfinal.controller;

import com.kato.ipn.backfrontfinal.model.User;
import com.kato.ipn.backfrontfinal.service.ReservationService;
import com.kato.ipn.backfrontfinal.service.RoomService;
import com.kato.ipn.backfrontfinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @Autowired
    private RoomService roomService;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private UserService userService;

    @GetMapping({"/", "/dashboard"})
    public String dashboard(Authentication authentication, Model model) {
        boolean isAdmin = authentication.getAuthorities()
            .contains(new SimpleGrantedAuthority("ROLE_ADMIN"));

        model.addAttribute("totalRooms", roomService.findAll().size());
        model.addAttribute("availableRooms", roomService.findAvailable().size());

        if (isAdmin) {
            var allReservations = reservationService.findAll();
            model.addAttribute("totalReservations", allReservations.size());
            model.addAttribute("recentReservations", allReservations.stream().limit(5).toList());
            model.addAttribute("totalUsers", userService.findAll().size());
        } else {
            User user = userService.findByUsername(authentication.getName());
            var userReservations = reservationService.findByUser(user);
            model.addAttribute("totalReservations", userReservations.size());
            model.addAttribute("recentReservations", userReservations.stream().limit(5).toList());
        }

        return "home/dashboard";
    }
}
