package com.eserent.controller;

import com.eserent.entity.Booking;
import com.eserent.entity.Payment;
import com.eserent.entity.Room;
import com.eserent.entity.User;
import com.eserent.service.BookingService;
import com.eserent.service.PaymentService;
import com.eserent.service.RoomService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Controller for customer dashboard and operations.
 * Handles room booking, payment, and booking management.
 */
@RequiredArgsConstructor
@Controller
@RequestMapping("/customer")
public class CustomerController {

    private final RoomService roomService;

    private final BookingService bookingService;

    private final PaymentService paymentService;

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        User customer = (User) authentication.getPrincipal();
        List<Booking> bookings = bookingService.getBookingsByCustomer(customer);
        
        // Count bookings by status
        long pendingBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.PENDING)
                .count();
        
        long confirmedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.CONFIRMED)
                .count();
        
        long completedBookings = bookings.stream()
                .filter(b -> b.getStatus() == Booking.BookingStatus.COMPLETED)
                .count();
        
        model.addAttribute("pendingBookings", pendingBookings);
        model.addAttribute("confirmedBookings", confirmedBookings);
        model.addAttribute("completedBookings", completedBookings);
        model.addAttribute("recentBookings", bookings.subList(0, Math.min(5, bookings.size())));
        
        return "customer/customer-dashboard";
    }

    @GetMapping("/bookings")
    public String bookingsList(Authentication authentication, Model model) {
        User customer = (User) authentication.getPrincipal();
        List<Booking> bookings = bookingService.getBookingsByCustomer(customer);
        model.addAttribute("bookings", bookings);
        return "customer/bookings";
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

    @PostMapping("/rooms/{id}/book")
    public String bookRoom(@PathVariable Long id,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkInDate,
                          @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOutDate,
                          @RequestParam(required = false, defaultValue = "1") int numberOfGuests,
                          @RequestParam(required = false) String specialRequests,
                          Authentication authentication,
                          RedirectAttributes redirectAttributes) {
        
        User customer = (User) authentication.getPrincipal();
        Optional<Room> roomOpt = roomService.getRoomById(id);
        
        if (roomOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room not found");
            return "redirect:/rooms";
        }
        
        Room room = roomOpt.get();
        
        // Validate dates
        if (checkInDate.isBefore(LocalDate.now())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Check-in date cannot be in the past");
            return "redirect:/customer/rooms/" + id;
        }
        
        if (checkOutDate.isBefore(checkInDate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Check-out date must be after check-in date");
            return "redirect:/customer/rooms/" + id;
        }
        
        // Check room availability
        if (!roomService.isRoomAvailableForDates(room, checkInDate, checkOutDate)) {
            redirectAttributes.addFlashAttribute("errorMessage", "Room is not available for the selected dates");
            return "redirect:/customer/rooms/" + id;
        }
        
        // Check capacity
        if (numberOfGuests > room.getCapacity()) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Number of guests exceeds room capacity of " + room.getCapacity());
            return "redirect:/customer/rooms/" + id;
        }
        
        // Create booking
        Booking booking = new Booking();
        booking.setRoom(room);
        booking.setCustomer(customer);
        booking.setCheckInDate(checkInDate);
        booking.setCheckOutDate(checkOutDate);
        booking.setNumberOfGuests(numberOfGuests);
        booking.setSpecialRequests(specialRequests);
        booking.setTotalPrice(roomService.calculateTotalPrice(room, checkInDate, checkOutDate));
        
        try {
            Booking savedBooking = bookingService.createBooking(booking);
            redirectAttributes.addFlashAttribute("successMessage", "Booking created successfully");
            return "redirect:/customer/bookings/" + savedBooking.getId() + "/payment";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error creating booking: " + e.getMessage());
            return "redirect:/customer/rooms/" + id;
        }
    }

    @GetMapping("/bookings/{id}")
    public String bookingDetails(@PathVariable Long id,
                                Authentication authentication,
                                Model model,
                                RedirectAttributes redirectAttributes) {
        
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/customer/bookings";
        }
        
        Booking booking = bookingOpt.get();
        User customer = (User) authentication.getPrincipal();
        
        // Check if the customer owns this booking
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to view this booking");
            return "redirect:/customer/bookings";
        }
        
        model.addAttribute("booking", booking);
        
        // Get payment information
        Optional<Payment> paymentOpt = paymentService.getPaymentByBooking(booking);
        paymentOpt.ifPresent(payment -> model.addAttribute("payment", payment));
        
        return "customer/booking-details";
    }

    @GetMapping("/bookings/{id}/payment")
    public String showPaymentForm(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/customer/bookings";
        }
        
        Booking booking = bookingOpt.get();
        User customer = (User) authentication.getPrincipal();
        
        // Check if the customer owns this booking
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to make payment for this booking");
            return "redirect:/customer/bookings";
        }
        
        // Check if booking is already paid
        Optional<Payment> paymentOpt = paymentService.getPaymentByBooking(booking);
        if (paymentOpt.isPresent() && paymentOpt.get().getStatus() == Payment.PaymentStatus.COMPLETED) {
            redirectAttributes.addFlashAttribute("infoMessage", "Payment has already been completed for this booking");
            return "redirect:/customer/bookings/" + id;
        }
        
        model.addAttribute("booking", booking);
        model.addAttribute("paymentMethods", Payment.PaymentMethod.values());
        
        return "customer/payment-form";
    }

    @PostMapping("/bookings/{id}/payment")
    public String processPayment(@PathVariable Long id,
                                @RequestParam Payment.PaymentMethod paymentMethod,
                                @RequestParam String cardNumber,
                                @RequestParam String cardHolderName,
                                @RequestParam String expiryDate,
                                @RequestParam String cvv,
                                Authentication authentication,
                                RedirectAttributes redirectAttributes) {
        
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/customer/bookings";
        }
        
        Booking booking = bookingOpt.get();
        User customer = (User) authentication.getPrincipal();
        
        // Check if the customer owns this booking
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to make payment for this booking");
            return "redirect:/customer/bookings";
        }
        
        // Simple validation for card details (in a real app, this would be much more secure)
        if (cardNumber == null || cardNumber.trim().length() < 13) {
            redirectAttributes.addFlashAttribute("errorMessage", "Invalid card number");
            return "redirect:/customer/bookings/" + id + "/payment";
        }
        
        // In a real application, we would tokenize the card details
        // Here we just create a simple token for demonstration
        String cardToken = "tok_" + cardNumber.substring(cardNumber.length() - 4);
        
        try {
            paymentService.processPayment(id, paymentMethod, cardToken);
            redirectAttributes.addFlashAttribute("successMessage", "Payment successful! Your booking is now confirmed.");
            return "redirect:/customer/bookings/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Payment failed: " + e.getMessage());
            return "redirect:/customer/bookings/" + id + "/payment";
        }
    }

    @PostMapping("/bookings/{id}/cancel")
    public String cancelBooking(@PathVariable Long id,
                               Authentication authentication,
                               RedirectAttributes redirectAttributes) {
        
        Optional<Booking> bookingOpt = bookingService.getBookingById(id);
        if (bookingOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Booking not found");
            return "redirect:/customer/bookings";
        }
        
        Booking booking = bookingOpt.get();
        User customer = (User) authentication.getPrincipal();
        
        // Check if the customer owns this booking
        if (!booking.getCustomer().getId().equals(customer.getId())) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have permission to cancel this booking");
            return "redirect:/customer/bookings";
        }
        
        // Check if the booking can be cancelled
        if (!bookingService.canCancelBooking(booking)) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                    "Booking cannot be cancelled as it's too close to check-in date");
            return "redirect:/customer/bookings/" + id;
        }
        
        try {
            bookingService.updateBookingStatus(id, Booking.BookingStatus.CANCELLED);
            
            // If payment was made, process refund
            Optional<Payment> paymentOpt = paymentService.getPaymentByBooking(booking);
            if (paymentOpt.isPresent() && paymentOpt.get().getStatus() == Payment.PaymentStatus.COMPLETED) {
                paymentService.refundPayment(paymentOpt.get().getId());
                redirectAttributes.addFlashAttribute("successMessage", 
                        "Booking cancelled successfully. Your payment will be refunded.");
            } else {
                redirectAttributes.addFlashAttribute("successMessage", "Booking cancelled successfully");
            }
            
            return "redirect:/customer/bookings";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Error cancelling booking: " + e.getMessage());
            return "redirect:/customer/bookings/" + id;
        }
    }
}
