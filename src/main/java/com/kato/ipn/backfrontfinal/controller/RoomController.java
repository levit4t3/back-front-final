package com.kato.ipn.backfrontfinal.controller;

import com.kato.ipn.backfrontfinal.model.Room;
import com.kato.ipn.backfrontfinal.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/rooms")
public class RoomController {

    @Autowired
    private RoomService roomService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("rooms", roomService.findAll());
        return "rooms/list";
    }

    @GetMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String newForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("editing", false);
        return "rooms/form";
    }

    @PostMapping("/new")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(@ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            roomService.save(room);
            redirectAttributes.addFlashAttribute("success", "Habitación creada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/rooms/new";
        }
        return "redirect:/rooms";
    }

    @GetMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String editForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return roomService.findById(id).map(room -> {
            model.addAttribute("room", room);
            model.addAttribute("editing", true);
            return "rooms/form";
        }).orElseGet(() -> {
            redirectAttributes.addFlashAttribute("error", "Habitación no encontrada.");
            return "redirect:/rooms";
        });
    }

    @PostMapping("/{id}/edit")
    @PreAuthorize("hasRole('ADMIN')")
    public String update(@PathVariable Long id, @ModelAttribute Room room, RedirectAttributes redirectAttributes) {
        try {
            room.setId(id);
            roomService.save(room);
            redirectAttributes.addFlashAttribute("success", "Habitación actualizada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/rooms";
    }

    @PostMapping("/{id}/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomService.deleteById(id);
            redirectAttributes.addFlashAttribute("success", "Habitación eliminada exitosamente.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "No se puede eliminar la habitación (puede tener reservaciones).");
        }
        return "redirect:/rooms";
    }
}
