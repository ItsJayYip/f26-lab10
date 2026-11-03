# RES-4471: two customers holding the same room for the same hour

| | |
|---|---|
| Status | Open, unassigned |
| Severity | S2 (customer facing, no data loss) |
| Opened | 2026-10-21 by H. Ahmad, campus operations |
| Component | Campus room reservations |
| Occurrences | 3 in the last 14 days |

## What happened

On 2026-10-21 two people showed up at the same room for the same hour. Both had
a confirmation on their phone. The reservations system had issued both, and
neither had been cancelled. The front desk moved one party to another room and
comped the booking.

This is the third time in two weeks. Each was a different room and hour:

- 2026-10-08, 10:00 slot
- 2026-10-15, 16:00 slot
- 2026-10-21, 15:00 slot

All three happened in the first minute after a slot opened up on the schedule
page.

A room can have at most one CONFIRMED booking covering any given minute. The
system is supposed to reject a second request for those minutes and offer the
waitlist instead. We can see the bad state in the data after the fact, but we
cannot make it happen on demand.

## Log excerpt, 2026-10-21

```
2026-10-21T14:58:44.106Z INFO booking.confirmed booking=bk-8317 room=WEH-5409 start=780 end=840
2026-10-21T15:02:11.870Z INFO booking.rejected room=GHC-4301 start=600 end=660 reason=slot already held
2026-10-21T15:07:52.318Z INFO booking.confirmed booking=bk-8412 room=WEH-5409 start=900 end=960
2026-10-21T15:07:52.319Z INFO booking.confirmed booking=bk-8413 room=WEH-5409 start=900 end=960
2026-10-21T15:09:03.442Z INFO booking.confirmed booking=bk-8414 room=GHC-4301 start=540 end=600
```

The two confirmations for the same room and range are one millisecond apart.
Nothing was logged between them. The 10-08 and 10-15 excerpts look the same.

## What we have already checked

- The suite was green before the first report and still is. We ran it 50 more
  times with no failures.
- We tried to reproduce it. Two of us booked the same slot from two laptops on
  a count of three, about 30 times. Each attempt gave one confirmation and one
  rejection.
- The logs show no exceptions or failed requests around any of the three
  incidents.
- It isn't a double submission. Each time, the two bookings had different
  accounts, sessions, and ids.
- It isn't a cancellation. Neither booking in any of the three pairs had been
  cancelled or waitlisted. Both were issued as confirmed.
- It isn't the reporting dashboard. The two confirmed bookings are in the
  booking data itself.

## What we need

An explanation of how the system can issue two confirmations for the same room
and the same minutes, a way to make it happen on demand so we can tell when it
is fixed, and a fix. We already have one proposed fix. We don't want to ship
another one we can't test.
