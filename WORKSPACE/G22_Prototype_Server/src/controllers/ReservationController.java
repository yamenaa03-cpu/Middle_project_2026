package controllers;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

import common.entity.Reservation;
import dbController.DBController;

public class ReservationController {

    private final DBController db;

    public ReservationController(DBController db) {
        this.db = db;
    }

    public List<Reservation> getAllReservations() throws SQLException {
        return db.getAllReservations();
    }

    public boolean updateReservation(int reservationId, LocalDate newDate, int newGuests)
            throws SQLException {

    	if (reservationId <= 0) return false;
        if (newDate == null) return false;
        if (newGuests <= 0) return false;

        return db.updateReservationFields(reservationId, newDate, newGuests);
    }
    
    public int createReservation(int customerId, LocalDate date, int guests) throws SQLException {
        if (customerId <= 0) return -1;
        if (date == null) return -1;
        if (guests <= 0) return -1;

        return db.insertReservation(customerId, date, guests);
    }
}
