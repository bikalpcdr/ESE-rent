package com.eserent.controller;

import com.eserent.entity.Room;
import com.eserent.service.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

/**
 * Controller for home page and general navigation.
 */
@Controller
public class HomeController {

    @Autowired
    private RoomService roomService;

    @GetMapping(value = {"", "/", "/home"})
    public String home(Model model) {
        List<Room> featuredRooms = roomService.getAvailableRooms();
        // For the homepage, we'll just show some available rooms
        model.addAttribute("featuredRooms", featuredRooms.subList(0, Math.min(6, featuredRooms.size())));
        return "home";
    }

    @GetMapping("/rooms")
    public String roomsList(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) Room.RoomType roomType,
            Model model) {
        
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
        
        return "admin/room-list";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        if (authentication != null) {
            for (GrantedAuthority authority : authentication.getAuthorities()) {
                String role = authority.getAuthority();

                switch (role) {
                    case "ROLE_ADMIN":
                        return "admin/admin-dashboard";
                    case "ROLE_LANDLORD":
                        return "landlord/landlord-dashboard";
                    case "ROLE_CUSTOMER":
                        return "customer/customer-dashboard";
                }
            }
        }
        return "access-denied";
    }


    @GetMapping("/about")
    public String about() {
        return "about";
    }

    @GetMapping("/contact")
    public String contact() {
        return "contact";
    }
}