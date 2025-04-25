package com.eserent.controller;

import com.eserent.entity.User;
import com.eserent.entity.Booking;
import com.eserent.entity.Room;
import com.eserent.service.UserService;
import com.eserent.service.BookingService;
import com.eserent.service.RoomService;
import com.eserent.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Controller for super admin operations.
 * Handles user management, system settings, and other administrator functions.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping({"/super-admin", "/admin"}) // Support both path prefixes
@PreAuthorize("hasRole('ROLE_SUPER_ADMIN')")
public class SuperAdminController {

    private final UserService userService;
    private final RoomService roomService;
    private final BookingService bookingService;
    private final PaymentService paymentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get counts for different user types
        long totalUsers = userService.countAllUsers();
        long adminCount = userService.countUsersByRole(User.Role.ROLE_SUPER_ADMIN);
        long landlordCount = userService.countUsersByRole(User.Role.ROLE_LANDLORD);
        long customerCount = userService.countUsersByRole(User.Role.ROLE_CUSTOMER);

        // Get counts for dashboard statistics
        long totalRooms = roomService.getAllRooms().size();
        long availableRooms = roomService.getAvailableRooms().size();
        
        List<Booking> allBookings = bookingService.getAllBookings();
        long pendingBookings = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING)
                .count();
        long confirmedBookings = allBookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .count();

        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("adminCount", adminCount);
        model.addAttribute("landlordCount", landlordCount);
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("recentBookings", allBookings.subList(0, Math.min(5, allBookings.size())));

        return "admin/admin-dashboard";
    }

    // Admin routes from previous AdminController
    @GetMapping("/rooms")
    public String roomsList(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String available,
            Model model) {
        
        List<Room> rooms = roomService.getAllRooms();
        
        // Apply filters if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            rooms = rooms.stream()
                    .filter(room -> 
                        (room.getTitle() != null && room.getTitle().toLowerCase().contains(searchLower)) ||
                        (room.getAddress() != null && room.getAddress().toLowerCase().contains(searchLower)) ||
                        (room.getDescription() != null && room.getDescription().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }
        
        if (roomType != null && !roomType.isEmpty()) {
            try {
                Room.RoomType roomTypeEnum = Room.RoomType.valueOf(roomType);
                rooms = rooms.stream()
                        .filter(room -> room.getRoomType() == roomTypeEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid room type parameter, ignore filter
            }
        }
        
        if (available != null && !available.isEmpty()) {
            boolean isAvailable = "true".equalsIgnoreCase(available);
            rooms = rooms.stream()
                    .filter(room -> room.isAvailable() == isAvailable)
                    .collect(Collectors.toList());
        }
        
        model.addAttribute("rooms", rooms);
        model.addAttribute("search", search);
        model.addAttribute("roomType", roomType);
        model.addAttribute("available", available);
        return "admin/rooms";
    }

    @GetMapping("/bookings")
    public String bookingsList(Model model) {
        List<Booking> bookings = bookingService.getAllBookings();
        model.addAttribute("bookings", bookings);
        return "admin/bookings";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/admin/bookings";
        }
        
        model.addAttribute("booking", bookingOpt.get());
        return "admin/booking-details";
    }

    @PostMapping("/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Long id,
                                     @RequestParam Booking.BookingStatus status,
                                     RedirectAttributes redirectAttributes) {
        
        try {
            bookingService.updateBookingStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Booking status updated successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating booking status: " + e.getMessage());
        }
        
        return "redirect:/admin/bookings/" + id;
    }

    // Original SuperAdmin routes
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            Model model) {
        
        List<User> users = userService.getAllUsers();
        
        // Apply filters if provided
        if (search != null && !search.isEmpty()) {
            String searchLower = search.toLowerCase();
            users = users.stream()
                    .filter(user -> 
                        (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchLower)) ||
                        (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) || 
                        (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchLower)))
                    .collect(Collectors.toList());
        }
        
        if (role != null && !role.isEmpty()) {
            try {
                User.Role roleEnum = User.Role.valueOf(role);
                users = users.stream()
                        .filter(user -> user.getRole() == roleEnum)
                        .collect(Collectors.toList());
            } catch (IllegalArgumentException e) {
                // Invalid role parameter, ignore filter
            }
        }
        
        if (status != null && !status.isEmpty()) {
            boolean isEnabled = "true".equalsIgnoreCase(status);
            users = users.stream()
                    .filter(user -> user.isEnabled() == isEnabled)
                    .collect(Collectors.toList());
        }
        
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/add")
    public String showAddUserForm(Model model, @RequestParam(required = false) String role) {
        User user = new User();
        
        // Pre-select the role if provided in the URL parameter
        if (role != null && !role.isEmpty()) {
            try {
                user.setRole(User.Role.valueOf(role));
            } catch (IllegalArgumentException e) {
                // If the role is invalid, just continue without pre-selecting
            }
        }
        
        model.addAttribute("user", user);
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isAdd", true);
        return "admin/user-form";
    }

    @PostMapping("/users/add")
    public String addUser(@ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            userService.createUser(user);
            
            // If the user is a landlord, redirect to dashboard with success message
            if (user.getRole() == User.Role.ROLE_LANDLORD) {
                redirectAttributes.addFlashAttribute("successMessage", "Landlord added successfully!");
                return "redirect:/admin/dashboard";
            }
            
            // Otherwise redirect to users list
            redirectAttributes.addFlashAttribute("successMessage", "User added successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error adding user: " + e.getMessage());
            return "redirect:/admin/users/add";
        }
    }

    @GetMapping("/users/edit/{id}")
    public String showEditUserForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }

        model.addAttribute("user", userOpt.get());
        model.addAttribute("roles", User.Role.values());
        model.addAttribute("isAdd", false);
        return "admin/user-form";
    }

    @PostMapping("/users/edit/{id}")
    public String updateUser(@PathVariable Long id, @ModelAttribute User user, RedirectAttributes redirectAttributes) {
        try {
            // Check if user exists
            if (!userService.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/admin/users";
            }

            user.setId(id); // Ensure ID is set correctly
            userService.updateUser(user);
            redirectAttributes.addFlashAttribute("successMessage", "User updated successfully!");
            return "redirect:/admin/users";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user: " + e.getMessage());
            return "redirect:/admin/users/edit/" + id;
        }
    }

    @PostMapping("/users/delete/{id}")
    public String deleteUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if user exists
            if (!userService.existsById(id)) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/admin/users";
            }

            // Check if it's the super admin (protect the super admin from deletion)
            User user = userService.getUserById(id).get();
            if (user.getRole() == User.Role.ROLE_SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "Super Admin cannot be deleted");
                return "redirect:/admin/users";
            }

            userService.deleteUser(id);
            redirectAttributes.addFlashAttribute("successMessage", "User deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting user: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/toggle-status")
    public String toggleUserStatus(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if user exists
            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "User not found");
                return "redirect:/admin/users";
            }

            User user = userOpt.get();
            // Check if it's the super admin (protect the super admin)
            if (user.getRole() == User.Role.ROLE_SUPER_ADMIN) {
                redirectAttributes.addFlashAttribute("errorMessage", "Super Admin status cannot be changed");
                return "redirect:/admin/users";
            }

            // Toggle the enabled status
            user.setEnabled(!user.isEnabled());
            userService.updateUser(user);
            
            String status = user.isEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("successMessage", "User " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating user status: " + e.getMessage());
        }
        return "redirect:/admin/users";
    }

    @PostMapping("/rooms/{id}/toggle-availability")
    public String toggleRoomAvailability(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if room exists
            Optional<Room> roomOpt = roomService.getRoomById(id);
            if (roomOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
                return "redirect:/admin/rooms";
            }

            Room room = roomOpt.get();
            // Toggle the availability status
            room.setAvailable(!room.isAvailable());
            roomService.updateRoom(room);
            
            String status = room.isAvailable() ? "available" : "unavailable";
            redirectAttributes.addFlashAttribute("successMessage", "Room marked as " + status + " successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating room availability: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }

    @GetMapping("/rooms/{id}")
    public String viewRoomDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Room> roomOpt = roomService.getRoomById(id);
        
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
            return "redirect:/admin/rooms";
        }
        
        Room room = roomOpt.get();
        model.addAttribute("room", room);
        
        // Get bookings for this room
        List<Booking> roomBookings = bookingService.getBookingsByRoom(room);
        model.addAttribute("bookings", roomBookings);
        
        // Check if room has active bookings
        List<Booking> activeBookings = roomBookings.stream()
            .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED || 
                   b.getStatus() == Booking.BookingStatus.PENDING)
            .collect(Collectors.toList());
        
        return "admin/room-details";
    }
    
    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            // Check if room exists
            Optional<Room> roomOpt = roomService.getRoomById(id);
            if (roomOpt.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
                return "redirect:/admin/rooms";
            }

            // Check if room has active bookings
            Room room = roomOpt.get();
            List<Booking> activeBookings = bookingService.getBookingsByRoom(room).stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED || 
                       b.getStatus() == Booking.BookingStatus.PENDING)
                .collect(Collectors.toList());
            
            if (!activeBookings.isEmpty()) {
                redirectAttributes.addFlashAttribute("errorMessage", "Cannot delete room with active bookings. Please cancel the bookings first or mark the room as unavailable.");
                return "redirect:/admin/rooms";
            }

            roomService.deleteRoom(id);
            redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error deleting room: " + e.getMessage());
        }
        return "redirect:/admin/rooms";
    }
}