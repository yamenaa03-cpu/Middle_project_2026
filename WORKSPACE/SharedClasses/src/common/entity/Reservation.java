package common.entity;

import java.io.Serializable;
import java.time.LocalDate;

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
    private LocalDate reservationDate;  // date of reservation
    private int numberOfGuests;
    private int confirmationCode;
    private int customerId;             // FK to Customer
    private LocalDate createdAt;        // date reservation was created

    public Reservation(int reservationId,
                       LocalDate reservationDate,
                       int numberOfGuests,
                       int confirmationCode,
                       int customerId,
                       LocalDate createdAt) {

        this.reservationId = reservationId;
        this.reservationDate = reservationDate;
        this.numberOfGuests = numberOfGuests;
        this.confirmationCode = confirmationCode;
        this.customerId = customerId;
        this.createdAt = createdAt;
    }

    public int getReservationId() { return reservationId; }
    public LocalDate getReservationDate() { return reservationDate; }
    public int getNumberOfGuests() { return numberOfGuests; }
    public int getConfirmationCode() { return confirmationCode; }
    public int getCustomerId() { return customerId; }
    public LocalDate getCreatedAt() { return createdAt; }

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

