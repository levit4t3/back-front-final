package com.kato.ipn.backfrontfinal.controller;

import com.kato.ipn.backfrontfinal.model.Reservation;
import com.kato.ipn.backfrontfinal.model.Room;
import com.kato.ipn.backfrontfinal.model.User;
import com.kato.ipn.backfrontfinal.service.ReservationService;
import com.kato.ipn.backfrontfinal.service.RoomService;
import com.kato.ipn.backfrontfinal.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/reservations")
public class ReservationController {

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private UserService userService;

    private boolean isAdmin(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) return false;
        return auth.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
    }

    @GetMapping
    public String list(Authentication authentication, Model model) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        boolean admin = isAdmin(authentication);
        model.addAttribute("isAdmin", admin);
        if (admin) {
            model.addAttribute("reservations", reservationService.findAll());
        } else {
            User user = userService.findByUsername(authentication.getName());
            model.addAttribute("reservations", reservationService.findByUser(user));
        }
        return "reservations/list";
    }

    @GetMapping("/new")
    public String newForm(Model model, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        model.addAttribute("reservation", new Reservation());
        if (isAdmin(authentication)) {
            model.addAttribute("rooms", roomService.findAll());
        } else {
            model.addAttribute("rooms", roomService.findAvailable());
        }
        return "reservations/form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Reservation reservation,
                         @RequestParam Long roomId,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            User user = userService.findByUsername(authentication.getName());
            Room room = roomService.findById(roomId)
                .orElseThrow(() -> new RuntimeException("Habitación no encontrada"));
            reservation.setRoom(room);
            reservationService.create(reservation, user);
            redirectAttributes.addFlashAttribute("success", "Reservación creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/reservations/new";
        }
        return "redirect:/reservations";
    }

    @GetMapping("/{id}")
    public String view(@PathVariable Long id, Model model, Authentication authentication,
                       RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/login";
        }
        return reservationService.findById(id).map(reservation -> {
            if (!isAdmin(authentication) && !reservation.getUser().getUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
                return "redirect:/reservations";
            }
            model.addAttribute("reservation", reservation);
            return "reservations/view";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Reservación no encontrada.");
            return "redirect:/reservations";
        });
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model, Authentication authentication,
                           RedirectAttributes redirectAttributes) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return "redirect:/auth/login";
        }
        return reservationService.findById(id).map(reservation -> {
            if (!isAdmin(authentication) && !reservation.getUser().getUsername().equals(authentication.getName())) {
                redirectAttributes.addFlashAttribute("error", "Acceso denegado.");
                return "redirect:/reservations";
            }
            if ("CANCELADA".equals(reservation.getStatus())) {
                redirectAttributes.addFlashAttribute("error", "No se puede editar una reservación cancelada.");
                return "redirect:/reservations";
            }
            model.addAttribute("reservation", reservation);
            model.addAttribute("rooms", roomService.findAll());
            return "reservations/edit";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Reservación no encontrada.");
            return "redirect:/reservations";
        });
    }

    @PostMapping("/{id}/edit")
    public String update(@PathVariable Long id, @ModelAttribute Reservation reservation,
                         Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            Reservation existing = reservationService.findById(id)
                .orElseThrow(() -> new RuntimeException("Reservación no encontrada"));
            if (!isAdmin(authentication) && !existing.getUser().getUsername().equals(authentication.getName())) {
                throw new RuntimeException("Acceso denegado.");
            }
            reservationService.update(id, reservation);
            redirectAttributes.addFlashAttribute("success", "Reservación actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/cancel")
    public String cancel(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/login";
            }
            if (isAdmin(authentication)) {
                reservationService.cancelAdmin(id);
            } else {
                reservationService.cancel(id, authentication.getName());
            }
            redirectAttributes.addFlashAttribute("success", "Reservación cancelada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }

    @PostMapping("/{id}/confirm")
    public String confirm(@PathVariable Long id, Authentication authentication, RedirectAttributes redirectAttributes) {
        try {
            if (authentication == null || !authentication.isAuthenticated()) {
                return "redirect:/auth/login";
            }
            if (!isAdmin(authentication)) {
                throw new RuntimeException("Solo los administradores pueden confirmar reservaciones.");
            }
            reservationService.confirm(id);
            redirectAttributes.addFlashAttribute("success", "Reservación confirmada.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/reservations";
    }
}
