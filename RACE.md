# RACE.md

One section per milestone. Fill each one in order as you go.

The prediction in milestone 1 is written BEFORE you run your race test. A TA
may ask about it. Commit it on its own so the order is visible.

Keep it short and specific. Point at methods, call sites, and the invariant.

---

## Milestone 1: from the ticket to a test that fails every time

### Suspect list

Everything your review turned up that touches shared state, with a one-line
verdict for each. Add or remove rows as needed.

| # | What you found (method, field, or class) | Verdict, in one line |
|---|---|---|
| 1 | `BookingService.book`: `store.hasOverlap(...)` then `store.insert(...)` | **Cause:** the check and write are separately atomic, so another booking can interleave between them. |
| 2 | `InMemoryBookingStore.bookings` and its public methods | Safe per method: the private list never escapes and every interface method synchronizes on the same store. |
| 3 | `InMemoryBookingStore.cancelAndPromote` | Safe for its invariant: cancellation, full-slot conflict recheck, and FIFO promotion occur under one store lock. |
| 4 | `BookingService.sequence` / `nextId()` | Safe: `AtomicLong.incrementAndGet()` gives concurrent requests distinct ids. |
| 5 | `BookingService.catalog` | Safe: construction copies it, and later operations only read it. |
| 6 | `BookingStats` counters | Racy by design, but irrelevant: approximate counters make no booking decision and cannot create store rows. |
| 7 | `BookingLog` / standard output | Concurrent lines may be ordered unpredictably, but each completed `println` neither decides nor stores bookings. |

**Which one explains the ticket, and how you know.** Tie it to the invariant
the ticket names.

`BookingService.book` explains the ticket. The named invariant is at most one
`CONFIRMED` booking per room per minute, but `hasOverlap` and `insert` are two
separate critical sections. Two callers can both observe no confirmed overlap
and then both insert a confirmed booking. That produces exactly the durable
bad state and paired confirmation log lines in the ticket; approximate stats,
id generation, and logging cannot do that.

### Predicted interleaving (write this before you run your race test)

Two threads, one row per step, in the form from lecture. Describe what each
thread does at that step and leave the other column empty. Add or remove rows
as your prediction needs.

| Step | Thread A | Thread B |
|---|---|---|
| t1 | `hasOverlap(WEH-5409, 540, 600)` returns false, then pauses before `book` receives the result. | |
| t2 | | `hasOverlap(WEH-5409, 540, 600)` returns false. |
| t3 | | Creates `bk-1` as `CONFIRMED`, inserts it, and logs `booking.confirmed`. |
| t4 | Resumes with its already-computed false result. | |
| t5 | Creates `bk-2` as `CONFIRMED`, inserts it, and logs `booking.confirmed`. | |
| | **End state:** | Two overlapping `CONFIRMED` bookings for the same room and minutes; the invariant assertion expects one and fails with actual count two. |

If the run turned out different from your prediction, add one line on what
you missed. Leave the table as you first wrote it.

### Your race test

**How you aimed `PausingStore`, and the result.** Method name and call index, why
that pair pins the schedule you predicted, and the output of two separate
runs (assertion failure and log lines).

I aimed `PausingStore` at `hasOverlap`, call 1. The wrapper delegates first, so
Thread A computes `false` against the empty store and then parks before
`BookingService.book` receives that answer. Thread B is not gated: it checks
the still-empty store, inserts, and finishes. Releasing A then lets it act on
its already-computed `false` and insert too. The run matched the prediction.

Run 1:

```text
2026-11-05T23:18:58.786Z INFO booking.confirmed booking=bk-1 room=WEH-5409 start=540 end=600
2026-11-05T23:18:58.794Z INFO booking.confirmed booking=bk-2 room=WEH-5409 start=540 end=600
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[ERROR] RaceTest.concurrentRequestsCannotBothConfirmTheSameSlot:51 a room may have at most one confirmed booking covering any minute ==> expected: <1> but was: <2>
[INFO] BUILD FAILURE
```

Run 2:

```text
2026-11-05T23:19:07.686Z INFO booking.confirmed booking=bk-1 room=WEH-5409 start=540 end=600
2026-11-05T23:19:07.687Z INFO booking.confirmed booking=bk-2 room=WEH-5409 start=540 end=600
[ERROR] Tests run: 1, Failures: 1, Errors: 0, Skipped: 0
[ERROR] RaceTest.concurrentRequestsCannotBothConfirmTheSameSlot:51 a room may have at most one confirmed booking covering any minute ==> expected: <1> but was: <2>
[INFO] BUILD FAILURE
```

---

## Milestone 2: the patch a teammate sent

**Your milestone-1 test on the patched code.** Green or red. Paste the result.

**The re-aimed run.** Method name, call index, the pasted result, and one line
on why that aim tests the patch rather than sidestepping it.

**Your verdict.** Accept or reject, and what your milestone-1 result does and
does not show about the patch.

---

## Milestone 3: the fix you would ship

**Which option, and what you changed.** Name the lock or the atomic operation
and the two operations it now covers. Paste the diff of your change and the
passing run of your milestone-1 test and the full suite.

**Is your milestone-1 test still testing anything?** After your fix, describe
what that test now exercises. Did `PausingStore` pause a call during the green run? How do
you know?

**What runs one at a time now that ran in parallel before.** Name concretely which callers
queue behind which. "Nothing, it is just correct now" does not count as an
answer.

**What you left unfixed.** For anything your review flagged that you chose not
to fix, one line each on why.
