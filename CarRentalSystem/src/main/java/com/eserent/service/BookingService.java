package com.eserent.service;

import com.eserent.entity.Booking;
import com.eserent.entity.Payment;
import com.eserent.entity.Room;
import com.eserent.entity.User;
import com.eserent.repository.BookingRepository;
import com.eserent.repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for booking-related logic.
 * Handles booking creation, management, and status updates.
 */
@Service
public class BookingService {

    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private RoomService roomService;

    public List<Booking> getAllBookings() {
        return bookingRepository.findAll();
    }

    public Optional<Booking> getBookingById(Long id) {
        return bookingRepository.findById(id);
    }

    public List<Booking> getBookingsByCustomer(User customer) {
        return bookingRepository.findByCustomer(customer);
    }

    public List<Booking> getBookingsByRoom(Room room) {
        return bookingRepository.findByRoom(room);
    }

    public List<Booking> getBookingsByLandlord(User landlord) {
        return bookingRepository.findByLandlord(landlord);
    }

    @Transactional
    public Booking createBooking(Booking booking) {
        // Check if the room is available for the requested dates
        boolean isAvailable = roomService.isRoomAvailableForDates(
                booking.getRoom(), booking.getCheckInDate(), booking.getCheckOutDate());
        
        if (!isAvailable) {
            throw new IllegalStateException("Room is not available for the selected dates");
        }

        // Calculate total price
        booking.setTotalPrice(roomService.calculateTotalPrice(
                booking.getRoom(), booking.getCheckInDate(), booking.getCheckOutDate()));
        
        // Save booking
        Booking savedBooking = bookingRepository.save(booking);
        
        // Create pending payment
        Payment payment = new Payment();
        payment.setBooking(savedBooking);
        payment.setAmount(savedBooking.getTotalPrice());
        payment.setStatus(Payment.PaymentStatus.PENDING);
        paymentRepository.save(payment);
        
        return savedBooking;
    }

    @Transactional
    public Booking updateBookingStatus(Long bookingId, Booking.BookingStatus status) {
        Optional<Booking> optionalBooking = bookingRepository.findById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new IllegalArgumentException("Booking not found with id: " + bookingId);
        }
        
        Booking booking = optionalBooking.get();
        booking.setStatus(status);
        
        // Update payment status if booking is cancelled
        if (status == Booking.BookingStatus.CANCELLED) {
            Optional<Payment> payment = paymentRepository.findByBooking(booking);
            payment.ifPresent(p -> {
                if (p.getStatus() == Payment.PaymentStatus.COMPLETED) {
                    p.setStatus(Payment.PaymentStatus.REFUNDED);
                    paymentRepository.save(p);
                }
            });
        }
        
        return bookingRepository.save(booking);
    }

    @Transactional
    public void deleteBooking(Long id) {
        bookingRepository.deleteById(id);
    }

    @Scheduled(cron = "0 0 0 * * ?") // Run daily at midnight
    @Transactional
    public void updateCompletedBookings() {
        List<Booking> completedBookings = bookingRepository.findCompletedBookings();
        for (Booking booking : completedBookings) {
            booking.setStatus(Booking.BookingStatus.COMPLETED);
            bookingRepository.save(booking);
        }
    }

    public boolean canCancelBooking(Booking booking) {
        // Check if cancellation is possible (e.g., not too close to check-in date)
        return LocalDate.now().plusDays(2).isBefore(booking.getCheckInDate());
    }
}
