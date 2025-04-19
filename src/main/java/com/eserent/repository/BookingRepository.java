package com.eserent.repository;

import com.eserent.entity.Booking;
import com.eserent.entity.Room;
import com.eserent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository for Booking entity.
 * Provides data access operations for bookings.
 */
@Repository
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    List<Booking> findByCustomer(User customer);
    
    List<Booking> findByRoom(Room room);
    
    List<Booking> findByRoomAndStatus(Room room, Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE b.room.landlord = :landlord")
    List<Booking> findByLandlord(User landlord);
    
    List<Booking> findByStatus(Booking.BookingStatus status);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "b.room = :room AND b.status <> 'CANCELLED' AND " +
           "((b.checkInDate <= :checkOutDate) AND (b.checkOutDate >= :checkInDate))")
    List<Booking> findOverlappingBookings(Room room, LocalDate checkInDate, LocalDate checkOutDate);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "b.checkOutDate < CURRENT_DATE AND b.status = 'CONFIRMED'")
    List<Booking> findCompletedBookings();
}
