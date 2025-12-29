package common.entity;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Represents a single Order entity stored in the database.
 * Contains all fields taken from the Order table and used by both client and server.
 * @author Yamen abu Ahmad
 * @version 1.0
 * 
 * */

public class Reservation implements Serializable {

    private static final long serialVersionUID = 1L;

    // Reservation fields
    private int reservationId;          // PK
    private LocalDateTime reservationDate;  // date of reservation
    private int numberOfGuests;
    private int confirmationCode;
    private int customerId;             // FK to Customer
    private LocalDateTime createdAt;        // date reservation was created
    Integer tableId;

    public Reservation(int reservationId,
    		LocalDateTime reservationDate,
                       int numberOfGuests,
                       int confirmationCode,
                       int customerId,
                       LocalDateTime createdAt) {

        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.numberOfGuests = numberOfGuests;
        this.confirmationCode = confirmationCode;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public int getReservationId() { return reservationId; }
    public LocalDateTime getReservationDateTime() { return reservationDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public int getConfirmationCode() { return confirmationCode; }
    public int getCustomerId() { return customerId; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    @Override
    public String toString() {
        return "Reservation #" + reservationId +
               " | date=" + reservationDate +
               " | guests=" + numberOfGuests +
               " | confirmationCode=" + confirmationCode +
               " | customerId=" + customerId +
               " | createdAt=" + createdAt;
    }
}

