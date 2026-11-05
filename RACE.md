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
| 1 | `BookingService.book`: `hasOverlap` then `insert` | **Cause:** another booking can run between the check and insert. |
| 2 | `InMemoryBookingStore.bookings` | Safe per method: every store method uses the same lock. |
| 3 | `cancelAndPromote` | Safe: cancellation and promotion use one store lock. |
| 4 | `sequence` / `nextId()` | Safe: `AtomicLong` gives each request a different id. |
| 5 | `catalog` | Safe: it is copied once and only read afterward. |
| 6 | `BookingStats` | Racy by design, but the approximate counts do not affect bookings. |
| 7 | `BookingLog` | Log order may vary, but logging does not affect bookings. |

**Which one explains the ticket, and how you know.** Tie it to the invariant
the ticket names.

`BookingService.book` explains the ticket. Two callers can both see no overlap
before either inserts. Both then insert a `CONFIRMED` booking, breaking the
rule that a room can have at most one confirmed booking for any minute.

### Predicted interleaving (write this before you run your race test)

Two threads, one row per step, in the form from lecture. Describe what each
thread does at that step and leave the other column empty. Add or remove rows
as your prediction needs.

| Step | Thread A | Thread B |
|---|---|---|
| t1 | `hasOverlap` returns false, then pauses. | |
| t2 | | `hasOverlap` returns false. |
| t3 | | Inserts confirmed `bk-1`. |
| t4 | Resumes and inserts confirmed `bk-2`. | |
| | **End state:** | Two confirmed bookings hold the same room and time. |

If the run turned out different from your prediction, add one line on what
you missed. Leave the table as you first wrote it.

### Your race test

**How you aimed `PausingStore`, and the result.** Method name and call index, why
that pair pins the schedule you predicted, and the output of two separate
runs (assertion failure and log lines).

I aimed `PausingStore` at `hasOverlap`, call 1. Thread A gets `false` and
pauses, so Thread B can also get `false` and insert. Releasing A lets it insert
the second confirmed booking. Both runs matched the prediction.

Run 1:

```text
2026-11-05T23:18:58.786Z INFO booking.confirmed booking=bk-1 room=WEH-5409 start=540 end=600
2026-11-05T23:18:58.794Z INFO booking.confirmed booking=bk-2 room=WEH-5409 start=540 end=600
[ERROR] RaceTest.concurrentRequestsCannotBothConfirmTheSameSlot:51 a room may have at most one confirmed booking covering any minute ==> expected: <1> but was: <2>
```

Run 2:

```text
2026-11-05T23:19:07.686Z INFO booking.confirmed booking=bk-1 room=WEH-5409 start=540 end=600
2026-11-05T23:19:07.687Z INFO booking.confirmed booking=bk-2 room=WEH-5409 start=540 end=600
[ERROR] RaceTest.concurrentRequestsCannotBothConfirmTheSameSlot:51 a room may have at most one confirmed booking covering any minute ==> expected: <1> but was: <2>
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
