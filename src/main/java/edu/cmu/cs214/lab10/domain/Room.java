package edu.cmu.cs214.lab10.domain;

import java.util.Objects;

/**
 * A bookable room in the campus catalog.
 *
 * <p>Rooms are immutable value objects. The catalog is fixed at startup: rooms
 * are not created, renamed, or resized while the service is running.
 *
 * @param id      stable identifier, for example {@code WEH-5409}
 * @param name    display name shown to people booking the room
 * @param seats   how many people the room holds
 */
public record Room(String id, String name, int seats) {

    public Room {
        Objects.requireNonNull(id, "id");
        Objects.requireNonNull(name, "name");
        if (id.isBlank()) {
            throw new IllegalArgumentException("room id must not be blank");
        }
        if (seats <= 0) {
            throw new IllegalArgumentException("seats must be positive, got " + seats);
        }
    }
}
