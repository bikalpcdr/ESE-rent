package com.eserent.service;

import com.eserent.entity.Booking;
import com.eserent.entity.Payment;
import com.eserent.repository.PaymentRepository;
import com.eserent.util.PaymentGatewayUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for payment-related logic.
 * Handles payment processing, refunds, and status updates.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    private final BookingService bookingService;

    private final PaymentGatewayUtil paymentGatewayUtil;

    public List<Payment> getAllPayments() {
        return paymentRepository.findAll();
    }

    public Optional<Payment> getPaymentById(Long id) {
        return paymentRepository.findById(id);
    }

    public Optional<Payment> getPaymentByBooking(Booking booking) {
        return paymentRepository.findByBooking(booking);
    }

    public List<Payment> getPaymentsByCustomerId(Long customerId) {
        return paymentRepository.findByCustomerId(customerId);
    }

    public List<Payment> getPaymentsByLandlordId(Long landlordId) {
        return paymentRepository.findByLandlordId(landlordId);
    }

    @Transactional
    public void processPayment(Long bookingId, Payment.PaymentMethod paymentMethod, String cardToken) {
        Optional<Booking> optionalBooking = bookingService.getBookingById(bookingId);
        if (optionalBooking.isEmpty()) {
            throw new IllegalArgumentException("Booking not found with id: " + bookingId);
        }
        
        Booking booking = optionalBooking.get();
        Optional<Payment> optionalPayment = paymentRepository.findByBooking(booking);
        
        if (optionalPayment.isEmpty()) {
            throw new IllegalStateException("Payment record not found for booking");
        }
        
        Payment payment = optionalPayment.get();
        
        // Process payment through payment gateway
        boolean paymentSuccess = paymentGatewayUtil.processPayment(
                payment.getAmount(), paymentMethod, cardToken);
        
        if (paymentSuccess) {
            payment.setStatus(Payment.PaymentStatus.COMPLETED);
            payment.setPaymentMethod(paymentMethod);
            payment.setTransactionId(generateTransactionId());
            
            // Update booking status
            bookingService.updateBookingStatus(bookingId, Booking.BookingStatus.CONFIRMED);

            paymentRepository.save(payment);
        } else {
            payment.setStatus(Payment.PaymentStatus.FAILED);
            paymentRepository.save(payment);
            throw new RuntimeException("Payment processing failed");
        }
    }

    @Transactional
    public void refundPayment(Long paymentId) {
        Optional<Payment> optionalPayment = paymentRepository.findById(paymentId);
        if (optionalPayment.isEmpty()) {
            throw new IllegalArgumentException("Payment not found with id: " + paymentId);
        }
        
        Payment payment = optionalPayment.get();
        
        if (payment.getStatus() != Payment.PaymentStatus.COMPLETED) {
            throw new IllegalStateException("Only completed payments can be refunded");
        }
        
        // Process refund through payment gateway
        boolean refundSuccess = paymentGatewayUtil.processRefund(payment.getTransactionId());
        
        if (refundSuccess) {
            payment.setStatus(Payment.PaymentStatus.REFUNDED);
            
            // Update booking status
            bookingService.updateBookingStatus(payment.getBooking().getId(), Booking.BookingStatus.CANCELLED);

            paymentRepository.save(payment);
        } else {
            throw new RuntimeException("Refund processing failed");
        }
    }

    private String generateTransactionId() {
        return "TXN-" + UUID.randomUUID().toString().replace("-", "").substring(0, 10).toUpperCase();
    }
}
