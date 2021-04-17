/* Generates a new 4-digit room ID for the host */
package com.game.rememberwhen;

import java.security.SecureRandom;

public class Room {
    private final int roomId; // Final Because want to keep it immutable

    // Public constructor generating random pin for roomId
    public Room() {
        SecureRandom randomPin = new SecureRandom();
        roomId = randomPin.nextInt(8999) + 1000;
    }

    // OverRidden Constructor to be used for existing Room initialization
    // isNew == false prevents the roomId being increased at each initialization
    public Room(int roomId, boolean isNew) {
        if (!isNew) {
            this.roomId = roomId;
        } else {
            roomId = roomId >= 9999 ? 1000 : roomId + 1;
            this.roomId = roomId;
        }
    }

    // Getter Method for RoomId
    public int getRoomId() {
        return this.roomId;
    }
}
