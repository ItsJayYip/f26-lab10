package edu.cmu.cs214.lab10.service;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import edu.cmu.cs214.lab10.domain.Booking;
import edu.cmu.cs214.lab10.domain.BookingResult;
import edu.cmu.cs214.lab10.domain.BookingStatus;
import edu.cmu.cs214.lab10.domain.Room;
import edu.cmu.cs214.lab10.store.InMemoryBookingStore;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/** The booking API, one caller at a time. */
class BookingServiceTest {

    private static final String WEAN = "WEH-5409";
    private static final String GATES = "GHC-4301";
    private static final List<Room> ROOMS = List.of(
            new Room(WEAN, "Wean 5409", 12),
            new Room(GATES, "Gates 4301", 30));

    private InMemoryBookingStore store;
    private BookingService service;

    @BeforeEach
    void setUp() {
        store = new InMemoryBookingStore();
        service = new BookingService(store, ROOMS);
    }

    @Test
    void confirmsABookingInAFreeSlot() {
        BookingResult result = service.book(WEAN, 540, 600);

        assertTrue(result.isConfirmed());
        Booking booking = result.booking().orElseThrow();
        assertEquals(WEAN, booking.roomId());
        assertEquals(BookingStatus.CONFIRMED, booking.status());
        assertEquals(1, service.stats().confirmed());
    }

    @Test
    void overlappingBookingIsRejected() {
        service.book(WEAN, 540, 600);

        BookingResult second = service.book(WEAN, 570, 630);

        assertTrue(second.isRejected());
        assertEquals("slot already held", second.reason());
        assertEquals(1, confirmedCount(WEAN, 540, 630));
    }

    @Test
    void adjacentBookingsBothConfirm() {
        assertTrue(service.book(WEAN, 540, 600).isConfirmed());
        assertTrue(service.book(WEAN, 600, 660).isConfirmed());
        assertEquals(2, confirmedCount(WEAN, 540, 660));
    }

    @Test
    void theSameSlotInAnotherRoomIsFine() {
        assertTrue(service.book(WEAN, 540, 600).isConfirmed());
        assertTrue(service.book(GATES, 540, 600).isConfirmed());
        assertEquals(1, confirmedCount(WEAN, 540, 600));
        assertEquals(1, confirmedCount(GATES, 540, 600));
    }

    @Test
    void rejectsUnknownRoomsAndImpossibleSlots() {
        assertAll(
                () -> assertEquals("unknown room", service.book("PH-100", 540, 600).reason()),
                () -> assertEquals("empty or reversed slot", service.book(WEAN, 600, 540).reason()),
                () -> assertEquals("empty or reversed slot", service.book(WEAN, 600, 600).reason()),
                () -> assertEquals(3, service.stats().rejected()));
    }

    @Test
    void cancellingPromotesTheWaitlistedBooking() {
        Booking held = service.book(WEAN, 540, 600).booking().orElseThrow();
        Booking waiting = service.joinWaitlist(WEAN, 540, 600).booking().orElseThrow();

        Optional<Booking> promoted = service.cancelBooking(held.id());

        assertTrue(promoted.isPresent());
        assertEquals(waiting.id(), promoted.get().id());
        assertEquals(BookingStatus.CONFIRMED, promoted.get().status());
        assertEquals(1, confirmedCount(WEAN, 540, 600));
    }

    @Test
    void listBookingsReturnsASnapshotTheCallerOwns() {
        service.book(WEAN, 540, 600).booking().orElseThrow();
        List<Booking> snapshot = service.listBookings(WEAN);

        service.book(WEAN, 600, 660);

        assertEquals(1, snapshot.size(), "the snapshot did not grow when the store did");
        assertEquals(2, service.listBookings(WEAN).size());
        assertThrows(UnsupportedOperationException.class,
                () -> snapshot.add(new Booking("bk-9999", WEAN, 720, 780, BookingStatus.CONFIRMED)),
                "the caller cannot write through the snapshot into the store");
        assertFalse(new ArrayList<>(snapshot).isEmpty());
    }

    private int confirmedCount(String roomId, int startMinute, int endMinute) {
        int count = 0;
        for (Booking booking : store.bookingsForRoom(roomId)) {
            if (booking.status() == BookingStatus.CONFIRMED && booking.overlaps(startMinute, endMinute)) {
                count++;
            }
        }
        return count;
    }
}
