package common.dto.Reservation;

import java.time.LocalDateTime;

public class ReservationBasicInfo {
    public final String fullName;
    public final LocalDateTime dateTime;
    public final int guests;
    public final int confirmationCode;

    public ReservationBasicInfo(String fullName, LocalDateTime dateTime, int guests, int confirmationCode) {
        this.fullName = fullName;
        this.dateTime = dateTime;
        this.guests = guests;
        this.confirmationCode = confirmationCode;
    }
}