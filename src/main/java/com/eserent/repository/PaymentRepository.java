package com.eserent.repository;

import com.eserent.entity.Booking;
import com.eserent.entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for Payment entity.
 * Provides data access operations for payments.
 */
@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    
    Optional<Payment> findByBooking(Booking booking);
    
    List<Payment> findByStatus(Payment.PaymentStatus status);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.customer.id = :customerId")
    List<Payment> findByCustomerId(Long customerId);
    
    @Query("SELECT p FROM Payment p WHERE p.booking.room.landlord.id = :landlordId")
    List<Payment> findByLandlordId(Long landlordId);
    
    Optional<Payment> findByTransactionId(String transactionId);
}
