package com.pawlink.sitter;

import com.pawlink.appointment.SlotUnavailableException;
import com.pawlink.common.exception.ResourceNotFoundException;
import com.pawlink.sitter.dto.BookingRequest;
import com.pawlink.sitter.dto.BookingResponse;
import com.pawlink.sitter.dto.CheckinResponse;
import com.pawlink.payment.Payment;
import com.pawlink.payment.PaymentDAO;
import com.pawlink.payment.PaymentFactory;
import com.pawlink.payment.PaymentFailedException;
import com.pawlink.sitter.dto.SitterDTO;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Service managing Sitter Bookings, Escrow Payment linkage, and Care Check-ins.
 * Enforces strict Domain Invariant: only verified sitters may be booked.
 */
@Service
public class SittingBookingService {

    private final SitterDAO sitterDAO;
    private final SittingBookingDAO sittingBookingDAO;
    private final PaymentDAO paymentDAO;

    public SittingBookingService(SitterDAO sitterDAO, SittingBookingDAO sittingBookingDAO, PaymentDAO paymentDAO) {
        this.sitterDAO = sitterDAO;
        this.sittingBookingDAO = sittingBookingDAO;
        this.paymentDAO = paymentDAO;
    }

    /**
     * Books a pet sitting session.
     * Transactional: Booking creation and Payment escrow hold succeed or fail together.
     *
     * @param req booking parameters
     * @return created SittingBooking entity with payment details
     * @throws UnverifiedSitterException if sitter is not verified (Domain Invariant)
     * @throws SlotUnavailableException  if sitter has an overlapping booking
     */
    @Transactional(rollbackFor = Exception.class)
    public SittingBooking bookSitter(BookingRequest req)
            throws UnverifiedSitterException, SlotUnavailableException, PaymentFailedException {
        if (req == null) {
            throw new IllegalArgumentException("Booking request cannot be null");
        }
        if (req.getSitterId() == null) {
            throw new IllegalArgumentException("Sitter ID is required");
        }
        if (req.getPetId() == null) {
            throw new IllegalArgumentException("Pet ID is required");
        }
        if (req.getOwnerId() == null) {
            throw new IllegalArgumentException("Owner ID is required");
        }
        if (req.getStartTime() == null || req.getEndTime() == null) {
            throw new IllegalArgumentException("Start time and end time are required");
        }
        if (!req.getStartTime().isBefore(req.getEndTime())) {
            throw new IllegalArgumentException("Start time must be before end time");
        }

        // 1. Domain Invariant: Only verified sitters can be booked
        Sitter sitter = sitterDAO.findById(req.getSitterId())
                .orElseThrow(() -> new ResourceNotFoundException("Sitter with ID " + req.getSitterId() + " not found"));

        if (!sitter.isVerified()) {
            throw new UnverifiedSitterException(
                    "Sitter '" + sitter.getName() + "' (ID: " + req.getSitterId() +
                    ") is not verified. Only verified sitters can be booked."
            );
        }

        // 2. Conflict detection: Check interval collision
        if (sittingBookingDAO.hasConflict(req.getSitterId(), req.getStartTime(), req.getEndTime())) {
            throw new SlotUnavailableException(
                    "Sitter '" + sitter.getName() + "' is already booked during this time window."
            );
        }

        // 3. Compute booking fee (minimum 1 hour, rounded up)
        Duration duration = Duration.between(req.getStartTime(), req.getEndTime());
        long totalMinutes = Math.max(duration.toMinutes(), 60);
        long hours = (long) Math.ceil(totalMinutes / 60.0);
        BigDecimal hourlyRate = sitter.getHourlyRate() != null ? sitter.getHourlyRate() : BigDecimal.valueOf(25.00);
        BigDecimal totalAmount = hourlyRate.multiply(BigDecimal.valueOf(hours));

        // 4. P7 Polymorphic Payment Processing: Instantiate via Factory & Process
        Payment payment = PaymentFactory.createPayment(req.getPaymentMethod(), totalAmount);
        boolean processed = payment.process();
        if (!processed) {
            throw new PaymentFailedException("Payment processing failed for channel: " + payment.getMethod());
        }

        // 5. Insert booking into database
        SittingBooking booking = new SittingBooking(
                null,
                req.getPetId(),
                req.getSitterId(),
                req.getOwnerId(),
                req.getStartTime(),
                req.getEndTime(),
                "CONFIRMED"
        );
        Long bookingId = sittingBookingDAO.insertBooking(booking);
        booking.setId(bookingId);

        // 6. Save payment via PaymentDAO (Strict Transactional Integrity)
        paymentDAO.insert(payment, bookingId);

        // 7. Return populated entity
        return sittingBookingDAO.findById(bookingId).orElse(booking);
    }

    /**
     * Adds a timestamped check-in note to a sitting booking.
     */
    @Transactional(rollbackFor = Exception.class)
    public CheckIn addCheckin(Long bookingId, String note) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (note == null || note.trim().isEmpty()) {
            throw new IllegalArgumentException("Check-in note cannot be blank");
        }

        // Validate booking exists
        sittingBookingDAO.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + bookingId + " not found"));

        CheckIn checkIn = new CheckIn(null, bookingId, LocalDateTime.now(), note.trim());
        Long id = sittingBookingDAO.insertCheckin(checkIn);
        checkIn.setId(id);
        return checkIn;
    }

    public List<SitterDTO> getSitters() {
        return sitterDAO.getSitters().stream()
                .map(SitterDTO::from)
                .toList();
    }

    public SitterDTO getSitterById(Long sitterId) {
        return sitterDAO.findById(sitterId)
                .map(SitterDTO::from)
                .orElseThrow(() -> new ResourceNotFoundException("Sitter with ID " + sitterId + " not found"));
    }

    public List<BookingResponse> getBookingsByOwner(Long ownerId) {
        return sittingBookingDAO.findByOwnerId(ownerId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<BookingResponse> getBookingsBySitter(Long sitterId) {
        return sittingBookingDAO.findBySitterId(sitterId).stream()
                .map(BookingResponse::from)
                .toList();
    }

    public List<BookingResponse> getAllBookings() {
        return sittingBookingDAO.findAll().stream()
                .map(BookingResponse::from)
                .toList();
    }

    public BookingResponse getBookingById(Long id) {
        return sittingBookingDAO.findById(id)
                .map(BookingResponse::from)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + id + " not found"));
    }

    public List<CheckinResponse> getCheckins(Long bookingId) {
        return sittingBookingDAO.findCheckinsByBookingId(bookingId).stream()
                .map(CheckinResponse::from)
                .toList();
    }

    public void updateVerification(Long sitterId, boolean verified) {
        sitterDAO.findById(sitterId)
                .orElseThrow(() -> new ResourceNotFoundException("Sitter with ID " + sitterId + " not found"));
        sitterDAO.updateVerification(sitterId, verified);
    }

    /**
     * Updates booking status (e.g. CANCELLED or COMPLETED) and updates escrow payment status.
     * When CANCELLED -> Escrow payment is REFUNDED.
     * When COMPLETED -> Escrow payment is RELEASED.
     */
    @Transactional(rollbackFor = Exception.class)
    public SittingBooking updateBookingStatus(Long bookingId, String status) {
        if (bookingId == null) {
            throw new IllegalArgumentException("Booking ID is required");
        }
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Status cannot be blank");
        }

        SittingBooking booking = sittingBookingDAO.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking with ID " + bookingId + " not found"));

        String normalizedStatus = status.trim().toUpperCase();
        sittingBookingDAO.updateStatus(bookingId, normalizedStatus);
        booking.setStatus(normalizedStatus);

        if ("COMPLETED".equalsIgnoreCase(normalizedStatus)) {
            LocalDateTime now = LocalDateTime.now();
            if (booking.getEndTime() != null && now.isBefore(booking.getEndTime())) {
                LocalDateTime newEndTime = (booking.getStartTime() != null && now.isBefore(booking.getStartTime()))
                        ? booking.getStartTime()
                        : now;
                sittingBookingDAO.updateEndTime(bookingId, newEndTime);
                booking.setEndTime(newEndTime);
            }
            paymentDAO.updateStatusByBookingId(bookingId, "RELEASED");
            booking.setPaymentStatus("RELEASED");
        } else if ("CANCELLED".equalsIgnoreCase(normalizedStatus)) {
            paymentDAO.updateStatusByBookingId(bookingId, "REFUNDED");
            booking.setPaymentStatus("REFUNDED");
        }

        return sittingBookingDAO.findById(bookingId).orElse(booking);
    }
}

