package com.eserent.controller;

import com.eserent.entity.Booking;
import com.eserent.entity.Room;
import com.eserent.entity.User;
import com.eserent.service.BookingService;
import com.eserent.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.List;
import java.util.Optional;

/**
 * Controller for landlord dashboard and operations.
 * Handles room management, booking approvals, and landlord statistics.
 */
@Controller
@RequiredArgsConstructor
@RequestMapping("/landlord")
public class LandlordController {

    private final RoomService roomService;

    private final BookingService bookingService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User landlord = (User) authentication.getPrincipal();
        List<Room> rooms = roomService.getRoomsByLandlord(landlord);
        List<Booking> bookings = bookingService.getBookingsByLandlord(landlord);

        // Count pending, confirmed, and completed bookings
        long pendingBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING)
                .count();

        long confirmedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .count();

        long completedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                .count();

        model.addAttribute("rooms", rooms);
        model.addAttribute("roomCount", rooms.size());
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("recentBookings", bookings.subList(0, Math.min(5, bookings.size())));

        return "landlord/landlord-dashboard";
    }

    @GetMapping("/rooms")
    public String roomsList(Authentication authentication, Model model) {
        User landlord = (User) authentication.getPrincipal();
        List<Room> rooms = roomService.getRoomsByLandlord(landlord);
        model.addAttribute("rooms", rooms);
        return "landlord/rooms";
    }

    @GetMapping("/rooms/{id}")
    public String roomDetails(@PathVariable Long id, Model model) {
        Optional<Room> roomOpt = roomService.getRoomById(id);
        System.out.println("Inside roomDetails method for ID: " + id);
        if (roomOpt.isEmpty()) {
            return "redirect:/403.html";
        }

        Room room = roomOpt.get();
        model.addAttribute("room", room);
        return "customer/room-details";
    }

    @GetMapping("/rooms/add")
    public String showAddRoomForm(Model model) {
        model.addAttribute("room", new Room());
        model.addAttribute("roomTypes", Room.RoomType.values());
        return "landlord/room-form";
    }

    @PostMapping("/rooms/add")
    public String addRoom(@Valid @ModelAttribute("room") Room room,
                         BindingResult bindingResult,
                         Authentication authentication,
                         RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "landlord/room-form";
        }

        User landlord = (User) authentication.getPrincipal();
        room.setLandlord(landlord);

        roomService.createRoom(room);
        redirectAttributes.addFlashAttribute("successMessage", "Room added successfully!");
        return "redirect:/landlord/rooms";
    }

    @GetMapping("/rooms/edit/{id}")
    public String showEditRoomForm(@PathVariable Long id,
                                  Authentication authentication,
                                  Model model,
                                  RedirectAttributes redirectAttributes) {

        Optional<Room> roomOpt = roomService.getRoomById(id);
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
            return "redirect:/landlord/rooms";
        }

        Room room = roomOpt.get();
        User landlord = (User) authentication.getPrincipal();

        // Check if the landlord owns this room
        if (!room.getLandlord().getId().equals(landlord.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this room");
            return "redirect:/landlord/rooms";
        }

        model.addAttribute("room", room);
        model.addAttribute("roomTypes", Room.RoomType.values());
        return "landlord/room-form";
    }

    @PostMapping("/rooms/edit/{id}")
    public String updateRoom(@PathVariable Long id,
                            @Valid @ModelAttribute("room") Room room,
                            BindingResult bindingResult,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "landlord/room-form";
        }

        Optional<Room> existingRoomOpt = roomService.getRoomById(id);
        if (existingRoomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
            return "redirect:/landlord/rooms";
        }

        Room existingRoom = existingRoomOpt.get();
        User landlord = (User) authentication.getPrincipal();

        // Check if the landlord owns this room
        if (!existingRoom.getLandlord().getId().equals(landlord.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to edit this room");
            return "redirect:/landlord/rooms";
        }

        // Update room properties while preserving the landlord and bookings
        existingRoom.setTitle(room.getTitle());
        existingRoom.setDescription(room.getDescription());
        existingRoom.setPricePerNight(room.getPricePerNight());
        existingRoom.setAddress(room.getAddress());
        existingRoom.setSize(room.getSize());
        existingRoom.setCapacity(room.getCapacity());
        existingRoom.setAvailable(room.isAvailable());
        existingRoom.setAmenities(room.getAmenities());
        existingRoom.setRoomType(room.getRoomType());

        roomService.updateRoom(existingRoom);
        redirectAttributes.addFlashAttribute("successMessage", "Room updated successfully!");
        return "redirect:/landlord/rooms";
    }

    @PostMapping("/rooms/delete/{id}")
    public String deleteRoom(@PathVariable Long id,
                            Authentication authentication,
                            RedirectAttributes redirectAttributes) {

        Optional<Room> roomOpt = roomService.getRoomById(id);
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
            return "redirect:/landlord/rooms";
        }

        Room room = roomOpt.get();
        User landlord = (User) authentication.getPrincipal();

        // Check if the landlord owns this room
        if (!room.getLandlord().getId().equals(landlord.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to delete this room");
            return "redirect:/landlord/rooms";
        }

        // Check if there are any active bookings
        List<Booking> activeBookings = bookingService.getBookingsByRoom(room).stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED ||
                            b.getStatus() == Booking.BookingStatus.PENDING)
                .collect(java.util.stream.Collectors.toList());

        if (!activeBookings.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "Cannot delete room with active bookings. Cancel all bookings first.");
            return "redirect:/landlord/rooms";
        }

        roomService.deleteRoom(id);
        redirectAttributes.addFlashAttribute("successMessage", "Room deleted successfully!");
        return "redirect:/landlord/rooms";
    }

    @GetMapping("/bookings")
    public String bookingsList(Authentication authentication, Model model) {
        User landlord = (User) authentication.getPrincipal();
        List<Booking> bookings = bookingService.getBookingsByLandlord(landlord);
        model.addAttribute("bookings", bookings);
        return "landlord/booking-details";
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(@PathVariable Long id,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/landlord/bookings";
        }

        Booking booking = bookingOpt.get();
        User landlord = (User) authentication.getPrincipal();

        // Check if the landlord owns the room for this booking
        if (!booking.getRoom().getLandlord().getId().equals(landlord.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to view this booking");
            return "redirect:/landlord/bookings";
        }

        model.addAttribute("booking", booking);
        return "landlord/booking-details";
    }

    @PostMapping("/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Long id,
                                     @RequestParam Booking.BookingStatus status,
                                     Authentication authentication,
                                     RedirectAttributes redirectAttributes) {

        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/landlord/bookings";
        }

        Booking booking = bookingOpt.get();
        User landlord = (User) authentication.getPrincipal();

        // Check if the landlord owns the room for this booking
        if (!booking.getRoom().getLandlord().getId().equals(landlord.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to update this booking");
            return "redirect:/landlord/bookings";
        }

        try {
            bookingService.updateBookingStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Booking status updated successfully!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error updating booking status: " + e.getMessage());
        }

        return "redirect:/landlord/bookings/" + id;
    }
}
