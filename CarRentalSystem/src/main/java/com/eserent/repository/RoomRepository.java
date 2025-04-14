package com.eserent.repository;

import com.eserent.entity.Room;
import com.eserent.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * Repository for Room entity.
 * Provides data access operations for rooms.
 */
@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    List<Room> findByLandlord(User landlord);
    
    List<Room> findByAvailableTrue();
    
    List<Room> findByRoomType(Room.RoomType roomType);
    
    List<Room> findByPricePerNightLessThanEqual(BigDecimal maxPrice);
    
    List<Room> findByCapacityGreaterThanEqual(int capacity);
    
    @Query("SELECT r FROM Room r WHERE " +
           "(:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
           "(:minPrice IS NULL OR r.pricePerNight >= :minPrice) AND " +
           "(:maxPrice IS NULL OR r.pricePerNight <= :maxPrice) AND " +
           "(:capacity IS NULL OR r.capacity >= :capacity) AND " +
           "(:roomType IS NULL OR r.roomType = :roomType) AND " +
           "r.available = true")
    List<Room> searchRooms(String title, BigDecimal minPrice, BigDecimal maxPrice, 
                           Integer capacity, Room.RoomType roomType);
}
