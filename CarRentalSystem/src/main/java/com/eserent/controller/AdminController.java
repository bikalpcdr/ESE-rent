package com.eserent.controller;

import com.eserent.entity.Booking;
import com.eserent.entity.Room;
import com.eserent.entity.User;
import com.eserent.service.BookingService;
import com.eserent.service.PaymentService;
import com.eserent.service.RoomService;
import com.eserent.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

/**
 * Controller for admin dashboard and operations.
 * Handles user management, system statistics, and overall monitoring.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserService userService;

    @Autowired
    private RoomService roomService;

    @Autowired
    private BookingService bookingService;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Get counts for dashboard statistics
        long totalUsers = userService.getAllUsers().size();
        long customerCount = userService.getUsersByRole(User.Role.ROLE_CUSTOMER).size();
        long landlordCount = userService.getUsersByRole(User.Role.ROLE_LANDLORD).size();
        
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
        model.addAttribute("customerCount", customerCount);
        model.addAttribute("landlordCount", landlordCount);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("totalBookings", allBookings.size());
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("recentBookings", allBookings.subList(0, Math.min(5, allBookings.size())));
        
        return "admin-dashboard";
    }

    @GetMapping("/users")
    public String usersList(Model model) {
        List<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "admin/users";
    }

    @GetMapping("/users/{id}")
    public String userDetails(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }
        
        User user = userOpt.get();
        model.addAttribute("user", user);
        
        // Get additional data based on user role
        if (user.getRole() == User.Role.ROLE_LANDLORD) {
            List<Room> rooms = roomService.getRoomsByLandlord(user);
            model.addAttribute("rooms", rooms);
        } else if (user.getRole() == User.Role.ROLE_CUSTOMER) {
            List<Booking> bookings = bookingService.getBookingsByCustomer(user);
            model.addAttribute("bookings", bookings);
        }
        
        return "admin/user-details";
    }

    @PostMapping("/users/{id}/disable")
    public String disableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }
        
        User user = userOpt.get();
        user.setEnabled(false);
        userService.updateUser(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "User disabled successfully");
        return "redirect:/admin/users";
    }

    @PostMapping("/users/{id}/enable")
    public String enableUser(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Optional<User> userOpt = userService.getUserById(id);
        
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found");
            return "redirect:/admin/users";
        }
        
        User user = userOpt.get();
        user.setEnabled(true);
        userService.updateUser(user);
        
        redirectAttributes.addFlashAttribute("successMessage", "User enabled successfully");
        return "redirect:/admin/users";
    }

    @GetMapping("/rooms")
    public String roomsList(Model model) {
        List<Room> rooms = roomService.getAllRooms();
        model.addAttribute("rooms", rooms);
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
        return "booking-details";
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
}
