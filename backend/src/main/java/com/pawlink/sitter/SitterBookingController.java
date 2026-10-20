package com.pawlink.sitter;

import com.pawlink.appointment.SlotUnavailableException;
import com.pawlink.payment.PaymentFailedException;
import com.pawlink.sitter.dto.BookingRequest;
import com.pawlink.sitter.dto.BookingResponse;
import com.pawlink.sitter.dto.CheckinRequest;
import com.pawlink.sitter.dto.CheckinResponse;
import com.pawlink.sitter.dto.SitterDTO;
import com.pawlink.sitter.dto.VerificationUpdateRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Sitter Bookings, Profiles, and Check-ins.
 * Endpoints:
 * - GET   /api/sitters
 * - GET   /api/sitters/{id}
 * - PATCH /api/sitters/{id}/verify
 * - POST  /api/sitter-bookings
 * - GET   /api/sitter-bookings?ownerId={id} or ?sitterId={id}
 * - GET   /api/sitter-bookings/{id}
 * - POST  /api/sitter-bookings/{id}/checkins
 * - GET   /api/sitter-bookings/{id}/checkins
 */
@RestController
@RequestMapping("/api")
public class SitterBookingController {

    private final SittingBookingService bookingService;

    public SitterBookingController(SittingBookingService bookingService) {
        this.bookingService = bookingService;
    }

    /**
     * GET /api/sitters: List all sitters with verification and pricing details.
     */
    @GetMapping("/sitters")
    public ResponseEntity<List<SitterDTO>> getSitters() {
        return ResponseEntity.ok(bookingService.getSitters());
    }

    /**
     * GET /api/sitters/{id}: Get detailed sitter profile.
     */
    @GetMapping("/sitters/{id}")
    public ResponseEntity<SitterDTO> getSitterById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.getSitterById(id));
    }

    /**
     * PATCH /api/sitters/{id}/verify: Admin endpoint to approve/unapprove sitters.
     */
    @PatchMapping("/sitters/{id}/verify")
    public ResponseEntity<Void> updateVerification(
            @PathVariable("id") Long id,
            @RequestBody(required = false) VerificationUpdateRequest req,
            @RequestParam(name = "verified", required = false) Boolean verifiedParam
    ) {
        boolean verified = verifiedParam != null ? verifiedParam : (req != null && req.isVerified());
        bookingService.updateVerification(id, verified);
        return ResponseEntity.noContent().build();
    }

    /**
     * POST /api/sitter-bookings: Books a pet sitter and places funds in escrow hold.
     * Enforces strict Domain Invariant: Only verified sitters can be booked.
     */
    @PostMapping("/sitter-bookings")
    public ResponseEntity<BookingResponse> bookSitter(
            @RequestBody BookingRequest request,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) throws UnverifiedSitterException, SlotUnavailableException, PaymentFailedException {
        if (request.getOwnerId() == null && xUserId != null) {
            request.setOwnerId(xUserId);
        }
        SittingBooking created = bookingService.bookSitter(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(BookingResponse.from(created));
    }

    /**
     * GET /api/sitter-bookings: Filter bookings by ownerId or sitterId.
     */
    @GetMapping("/sitter-bookings")
    public ResponseEntity<List<BookingResponse>> getBookings(
            @RequestParam(name = "ownerId", required = false) Long ownerId,
            @RequestParam(name = "sitterId", required = false) Long sitterId,
            @RequestHeader(name = "X-User-Id", required = false) Long xUserId
    ) {
        if (ownerId != null) {
            return ResponseEntity.ok(bookingService.getBookingsByOwner(ownerId));
        } else if (sitterId != null) {
            return ResponseEntity.ok(bookingService.getBookingsBySitter(sitterId));
        } else if (xUserId != null) {
            // Default to owner view if X-User-Id provided
            return ResponseEntity.ok(bookingService.getBookingsByOwner(xUserId));
        }
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    /**
     * GET /api/sitter-bookings/{id}: Get booking details by ID.
     */
    @GetMapping("/sitter-bookings/{id}")
    public ResponseEntity<BookingResponse> getBookingById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    /**
     * POST /api/sitter-bookings/{id}/checkins: Adds a check-in note to a booking.
     */
    @PostMapping("/sitter-bookings/{id}/checkins")
    public ResponseEntity<CheckinResponse> addCheckin(
            @PathVariable("id") Long id,
            @RequestBody CheckinRequest request
    ) {
        String note = request != null ? request.getNote() : null;
        CheckIn checkIn = bookingService.addCheckin(id, note);
        return ResponseEntity.status(HttpStatus.CREATED).body(CheckinResponse.from(checkIn));
    }

    /**
     * GET /api/sitter-bookings/{id}/checkins: Get check-in timeline for a booking.
     */
    @GetMapping("/sitter-bookings/{id}/checkins")
    public ResponseEntity<List<CheckinResponse>> getCheckins(@PathVariable("id") Long id) {
        return ResponseEntity.ok(bookingService.getCheckins(id));
    }

    /**
     * PATCH /api/sitter-bookings/{id}/status: Update status of a booking (e.g. CANCELLED, COMPLETED).
     */
    @PatchMapping("/sitter-bookings/{id}/status")
    public ResponseEntity<BookingResponse> updateBookingStatus(
            @PathVariable("id") Long id,
            @RequestBody(required = false) java.util.Map<String, String> body,
            @RequestParam(name = "status", required = false) String statusParam
    ) {
        String status = statusParam != null ? statusParam : (body != null ? body.get("status") : null);
        if (status == null || status.trim().isEmpty()) {
            throw new IllegalArgumentException("Booking status is required (e.g. CANCELLED or COMPLETED)");
        }
        SittingBooking updated = bookingService.updateBookingStatus(id, status.trim().toUpperCase());
        return ResponseEntity.ok(BookingResponse.from(updated));
    }

    /**
     * POST /api/sitter-bookings/{id}/cancel: Cancel booking and refund escrow payment.
     */
    @PostMapping("/sitter-bookings/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable("id") Long id) {
        SittingBooking updated = bookingService.updateBookingStatus(id, "CANCELLED");
        return ResponseEntity.ok(BookingResponse.from(updated));
    }

    /**
     * POST /api/sitter-bookings/{id}/complete: End session and release escrow payment to sitter.
     */
    @PostMapping("/sitter-bookings/{id}/complete")
    public ResponseEntity<BookingResponse> completeBooking(@PathVariable("id") Long id) {
        SittingBooking updated = bookingService.updateBookingStatus(id, "COMPLETED");
        return ResponseEntity.ok(BookingResponse.from(updated));
    }
}

