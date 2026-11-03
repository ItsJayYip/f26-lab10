package edu.cmu.cs214.lab10.harness;

import edu.cmu.cs214.lab10.domain.Booking;
import edu.cmu.cs214.lab10.domain.BookingStatus;
import edu.cmu.cs214.lab10.domain.Room;
import edu.cmu.cs214.lab10.service.BookingService;
import edu.cmu.cs214.lab10.store.InMemoryBookingStore;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Runs the same two-customer booking round many times and reports how many
 * rounds ended in a state the service is supposed to make impossible.
 *
 * <p>Each round gets a fresh store and a fresh service, then starts two threads
 * that line up on a barrier and book the same room and the same slot at the
 * same moment. After both threads finish, the round checks the room: exactly
 * one booking for that slot should be CONFIRMED.
 *
 * <p>This is a main class, not a test. It is not in the suite and CI never runs
 * it. Results vary run to run and machine to machine, which is the point: a
 * report like this tells you something is wrong without telling you when it
 * will happen next.
 *
 * <p>Run it from the project root:
 *
 * <pre>
 * mvn -q compile
 * java -cp target/classes edu.cmu.cs214.lab10.harness.StressRunner
 * java -cp target/classes edu.cmu.cs214.lab10.harness.StressRunner 2000
 * </pre>
 */
public final class StressRunner {

    private static final int DEFAULT_ROUNDS = 100;
    private static final int PRIMING_BOOKINGS = 20;
    private static final String ROOM_ID = "WEH-5409";
    private static final int START_MINUTE = 540;
    private static final int END_MINUTE = 600;
    private static final long BOUND_MILLIS = 2_000L;

    private static final List<Room> ROOMS = List.of(
            new Room("WEH-5409", "Wean 5409", 12),
            new Room("GHC-4301", "Gates 4301", 30));

    private StressRunner() {
    }

    public static void main(String[] args) throws InterruptedException {
        int rounds = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_ROUNDS;
        PrintStream console = System.out;
        List<String> failures = new ArrayList<>();

        // The service logs a line per booking. Silence it for the run so the
        // report below is readable, then put standard output back.
        System.setOut(new PrintStream(OutputStream.nullOutputStream()));
        try {
            prime();
            for (int round = 1; round <= rounds; round++) {
                int confirmed = runOneRound();
                if (confirmed != 1) {
                    failures.add("round " + round + ": " + confirmed + " confirmed bookings for "
                            + ROOM_ID + " " + START_MINUTE + ".." + END_MINUTE);
                }
            }
        } finally {
            System.setOut(console);
        }

        console.println("rounds: " + rounds);
        console.println("rounds ending in a bad state: " + failures.size());
        for (String failure : failures) {
            console.println("  " + failure);
        }
        if (failures.isEmpty()) {
            console.println("clean run. That is not evidence of anything; run it again.");
        }
    }

    /**
     * Books a few times on one thread before the rounds start, and throws the
     * results away.
     *
     * <p>The JVM runs a code path interpreted the first several times it sees
     * it, and only then compiles it. Interpreted, everything the service does
     * takes orders of magnitude longer, which is not the service anyone runs in
     * production. Priming the path first means the rounds below measure the
     * booking path as it behaves once the process is up and busy.
     */
    private static void prime() {
        for (int i = 0; i < PRIMING_BOOKINGS; i++) {
            BookingService service = new BookingService(new InMemoryBookingStore(), ROOMS);
            service.book(ROOM_ID, START_MINUTE, END_MINUTE);
            service.book(ROOM_ID, START_MINUTE, END_MINUTE);
        }
    }

    /** One round. Returns how many CONFIRMED bookings the slot ended up with. */
    private static int runOneRound() throws InterruptedException {
        InMemoryBookingStore store = new InMemoryBookingStore();
        BookingService service = new BookingService(store, ROOMS);
        CyclicBarrier lineUp = new CyclicBarrier(2);

        Runnable customer = () -> {
            try {
                lineUp.await(BOUND_MILLIS, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return;
            } catch (BrokenBarrierException | TimeoutException e) {
                return;
            }
            service.book(ROOM_ID, START_MINUTE, END_MINUTE);
        };

        Thread first = new Thread(customer, "customer-1");
        Thread second = new Thread(customer, "customer-2");
        first.start();
        second.start();
        first.join(BOUND_MILLIS);
        second.join(BOUND_MILLIS);

        int confirmed = 0;
        for (Booking booking : store.bookingsForRoom(ROOM_ID)) {
            if (booking.status() == BookingStatus.CONFIRMED
                    && booking.overlaps(START_MINUTE, END_MINUTE)) {
                confirmed++;
            }
        }
        return confirmed;
    }
}
