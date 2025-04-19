package com.eserent.service;

import com.eserent.entity.Booking;
import com.eserent.entity.Room;
import com.eserent.entity.User;
import com.eserent.repository.BookingRepository;
import com.eserent.repository.RoomRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Service for room-related logic.
 * Handles room creation, listing, search, and availability management.
 */
@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;

    private final BookingRepository bookingRepository;

    public List<Room> getAllRooms() {
        return roomRepository.findAll();
    }

    public List<Room> getAvailableRooms() {
        return roomRepository.findByAvailableTrue();
    }

    public List<Room> getRoomsByLandlord(User landlord) {
        return roomRepository.findByLandlord(landlord);
    }

    public Optional<Room> getRoomById(Long id) {
        return roomRepository.findById(id);
    }

    public void createRoom(Room room) {
        roomRepository.save(room);
    }

    public void updateRoom(Room room) {
        roomRepository.save(room);
    }

    public void deleteRoom(Long id) {
        roomRepository.deleteById(id);
    }

    public List<Room> searchRooms(String title, BigDecimal minPrice, BigDecimal maxPrice,
                                  Integer capacity, Room.RoomType roomType) {
        return roomRepository.searchRooms(title, minPrice, maxPrice, capacity, roomType);
    }

    public boolean isRoomAvailableForDates(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        if (!room.isAvailable()) {
            return false;
        }

        // Check for overlapping bookings
        List<Booking> overlappingBookings = bookingRepository.findOverlappingBookings(
                room, checkInDate, checkOutDate);

        return overlappingBookings.isEmpty();
    }

    public List<LocalDate> getUnavailableDates(Room room) {
        // In a real application, this would calculate unavailable dates based on bookings
        // For simplicity, this implementation is omitted
        return List.of();
    }

    public BigDecimal calculateTotalPrice(Room room, LocalDate checkInDate, LocalDate checkOutDate) {
        long nights = checkOutDate.toEpochDay() - checkInDate.toEpochDay();
        return room.getPricePerNight().multiply(BigDecimal.valueOf(nights));
    }
}
