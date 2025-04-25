package com.eserent.controller;

import com.eserent.entity.Room;
import com.eserent.entity.User;
import com.eserent.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

/**
 * Controller for home page and general navigation.
 */
@Controller
public class HomeController {

    @Autowired
    private RoomService roomService;

    @GetMapping(value = {"", "/", "/home"})
    public String home(Model model) {
        List<Room> featuredRooms = roomService.getFeaturedRooms(6);
        model.addAttribute("featuredRooms", featuredRooms);
        return "home";
    }

    @GetMapping("/rooms")
    public String roomsList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Room.RoomType roomType,
            Authentication authentication,
            Model model) {
        
        // Check if user is authenticated
        boolean isAuthenticated = (authentication != null && authentication.isAuthenticated());
        model.addAttribute("isAuthenticated", isAuthenticated);
        
        List<Room> rooms;
        if (title != null || minPrice != null || maxPrice != null || capacity != null || roomType != null) {
            rooms = roomService.searchRooms(title, minPrice, maxPrice, capacity, roomType);
            model.addAttribute("searchPerformed", true);
        } else {
            rooms = roomService.getAvailableRooms();
            model.addAttribute("searchPerformed", false);
        }
        
        model.addAttribute("rooms", rooms);
        model.addAttribute("roomTypes", Room.RoomType.values());
        model.addAttribute("title", title);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("capacity", capacity);
        model.addAttribute("roomType", roomType);
        
        return "rooms/room-list";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication == null) {
            return "redirect:/login";
        }

        User user = (User) authentication.getPrincipal();
        switch (user.getRole()) {
            case ROLE_SUPER_ADMIN:
                return "redirect:/super-admin/dashboard";
            case ROLE_LANDLORD:
                return "redirect:/landlord/dashboard";
            case ROLE_CUSTOMER:
                return "redirect:/customer/dashboard";
            default:
                return "redirect:/";
        }
    }


    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
    
    @GetMapping("/rooms/{id}")
    public String roomDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Room> roomOpt = roomService.getRoomById(id);
        
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
            return "redirect:/rooms";
        }
        
        Room room = roomOpt.get();
        model.addAttribute("room", room);
        return "customer/room-details";
    }
}