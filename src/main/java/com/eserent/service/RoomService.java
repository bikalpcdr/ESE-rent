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

    /**
     * Get featured rooms for the home page
     * This selects rooms based on criteria like being available, having images, 
     * and having complete information
     * @param limit Maximum number of rooms to return
     * @return List of featured rooms
     */
    public List<Room> getFeaturedRooms(int limit) {
        // Get all available rooms
        List<Room> availableRooms = getAvailableRooms();
        
        // Filter rooms with images and good descriptions (quality criteria)
        List<Room> featuredRooms = availableRooms.stream()
            .filter(room -> !room.getImageUrls().isEmpty())                // Must have images
            .filter(room -> room.getDescription() != null 
                         && room.getDescription().length() > 20)           // Must have decent description
            .filter(room -> room.getAmenities() != null 
                         && !room.getAmenities().isEmpty())                // Must have amenities listed
            .sorted((r1, r2) -> r2.getId().compareTo(r1.getId()))         // Sort by newest first (higher ID)
            .limit(limit)
            .toList();
        
        // If we don't have enough rooms after filtering, just return available rooms
        if (featuredRooms.size() < limit) {
            return availableRooms.stream()
                    .limit(limit)
                    .toList();
        }
        
        return featuredRooms;
    }
}
