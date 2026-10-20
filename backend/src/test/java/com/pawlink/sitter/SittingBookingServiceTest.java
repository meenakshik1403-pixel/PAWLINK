package com.pawlink.sitter;

import com.pawlink.appointment.SlotUnavailableException;
import com.pawlink.sitter.dto.BookingRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SittingBookingServiceTest {

    @Mock
    private SitterDAO sitterDAO;

    @Mock
    private SittingBookingDAO sittingBookingDAO;

    @InjectMocks
    private SittingBookingService sittingBookingService;

    private Sitter verifiedSitter;
    private Sitter unverifiedSitter;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    @BeforeEach
    void setUp() {
        startTime = LocalDateTime.now().plusDays(1).withHour(10).withMinute(0);
        endTime = LocalDateTime.now().plusDays(1).withHour(14).withMinute(0);

        verifiedSitter = new Sitter(10L, "Emily Watson", "emily@pawlink.com", "555-0101", BigDecimal.valueOf(25.00), true);
        unverifiedSitter = new Sitter(20L, "David Clark", "david@pawlink.com", "555-0102", BigDecimal.valueOf(20.00), false);
    }

    @Test
    @DisplayName("Booking verified sitter succeeds and initiates escrow payment hold")
    void bookSitter_whenVerifiedSitter_shouldSucceed() throws Exception {
        BookingRequest req = new BookingRequest(1L, 10L, 5L, startTime, endTime, "CARD");

        when(sitterDAO.findById(10L)).thenReturn(Optional.of(verifiedSitter));
        when(sittingBookingDAO.hasConflict(eq(10L), eq(startTime), eq(endTime))).thenReturn(false);
        when(sittingBookingDAO.insertBooking(any(SittingBooking.class))).thenReturn(100L);
        when(sittingBookingDAO.insertPayment(eq(100L), any(BigDecimal.class), eq("CARD"), eq("HELD"))).thenReturn(50L);

        SittingBooking mockBooking = new SittingBooking(100L, 1L, 10L, 5L, startTime, endTime, "CONFIRMED");
        mockBooking.setTotalAmount(BigDecimal.valueOf(100.00));
        mockBooking.setPaymentStatus("HELD");
        when(sittingBookingDAO.findById(100L)).thenReturn(Optional.of(mockBooking));

        SittingBooking result = sittingBookingService.bookSitter(req);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals("CONFIRMED", result.getStatus());
        assertEquals("HELD", result.getPaymentStatus());

        verify(sittingBookingDAO).insertBooking(any(SittingBooking.class));
        verify(sittingBookingDAO).insertPayment(eq(100L), any(BigDecimal.class), eq("CARD"), eq("HELD"));
    }

    @Test
    @DisplayName("Domain Invariant: Booking unverified sitter throws UnverifiedSitterException")
    void bookSitter_whenUnverifiedSitter_shouldThrowUnverifiedSitterException() {
        BookingRequest req = new BookingRequest(1L, 20L, 5L, startTime, endTime, "UPI");

        when(sitterDAO.findById(20L)).thenReturn(Optional.of(unverifiedSitter));

        UnverifiedSitterException ex = assertThrows(UnverifiedSitterException.class, () ->
                sittingBookingService.bookSitter(req));

        assertTrue(ex.getMessage().contains("not verified"));
        verify(sittingBookingDAO, never()).insertBooking(any());
        verify(sittingBookingDAO, never()).insertPayment(any(), any(), any(), any());
    }

    @Test
    @DisplayName("Booking overlapping interval throws SlotUnavailableException")
    void bookSitter_whenConflict_shouldThrowSlotUnavailableException() {
        BookingRequest req = new BookingRequest(1L, 10L, 5L, startTime, endTime, "WALLET");

        when(sitterDAO.findById(10L)).thenReturn(Optional.of(verifiedSitter));
        when(sittingBookingDAO.hasConflict(eq(10L), eq(startTime), eq(endTime))).thenReturn(true);

        SlotUnavailableException ex = assertThrows(SlotUnavailableException.class, () ->
                sittingBookingService.bookSitter(req));

        assertTrue(ex.getMessage().contains("already booked"));
        verify(sittingBookingDAO, never()).insertBooking(any());
    }

    @Test
    @DisplayName("Adding check-in note to existing booking succeeds")
    void addCheckin_whenValid_shouldInsertAndReturnCheckin() {
        SittingBooking mockBooking = new SittingBooking(100L, 1L, 10L, 5L, startTime, endTime, "CONFIRMED");
        when(sittingBookingDAO.findById(100L)).thenReturn(Optional.of(mockBooking));
        when(sittingBookingDAO.insertCheckin(any(CheckIn.class))).thenReturn(1L);

        CheckIn checkIn = sittingBookingService.addCheckin(100L, "Gave Buddy lunch and walked 20 mins.");

        assertNotNull(checkIn);
        assertEquals(1L, checkIn.getId());
        assertEquals(100L, checkIn.getBookingId());
        assertEquals("Gave Buddy lunch and walked 20 mins.", checkIn.getNote());
        verify(sittingBookingDAO).insertCheckin(any(CheckIn.class));
    }

    @Test
    @DisplayName("Adding blank check-in note throws IllegalArgumentException")
    void addCheckin_whenBlankNote_shouldThrowIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () ->
                sittingBookingService.addCheckin(100L, "   "));
        verify(sittingBookingDAO, never()).insertCheckin(any());
    }
}

