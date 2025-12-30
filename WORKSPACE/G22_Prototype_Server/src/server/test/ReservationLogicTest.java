package server.test;

import java.time.LocalDateTime;
import dbController.DBController;
import controllers.ReservationController;

public class ReservationLogicTest {

    public static void main(String[] args) throws Exception {

        DBController db =
            new DBController("bistrodb", "root", "Aa123456");

        ReservationController rc =
            new ReservationController(db);

        System.out.println("=== TEST 1: valid reservation ===");
        System.out.println(
            rc.createReservation(
                14,
                LocalDateTime.now().plusHours(2).withMinute(0),
                4
            ).getMessage()
        );

        System.out.println("=== TEST 2: invalid time (not 30 min) ===");
        System.out.println(
            rc.createReservation(
                14,
                LocalDateTime.now().plusHours(2).withMinute(10),
                4
            ).getMessage()
        );

        System.out.println("=== TEST 3: outside opening hours ===");
        System.out.println(
            rc.createReservation(
                14,
                LocalDateTime.of(2026,1,5,3,0),
                4
            ).getMessage()
        );
    }
}
