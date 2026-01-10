package controllers;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import common.dto.Notification.CustomerContactInfo;
import common.dto.Reservation.ReservationBasicInfo;
import common.entity.Bill;
import common.enums.NotificationType;
import dbController.DBController;
import server.ServerUI;

public class NotificationController {

    private final ServerUI ui;
    private final DBController db;
    
    public NotificationController(ServerUI ui, DBController db) {
        this.ui = ui;
        this.db = db;
    }

 // 1) Reservation confirmation
    public boolean sendReservationConfirmation(int reservationId) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);
        
        ReservationBasicInfo info = db.getReservationBasicInfo(reservationId); 
        
        String msg = buildMessage(NotificationType.RESERVATION_CONFIRMATION,
        		info.fullName, info.dateTime, info.guests, info.confirmationCode, null, null);

        return send(contact, msg);
    }


    // 2) Reminder
    public boolean sendReservationReminder(int reservationId) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

        ReservationBasicInfo info = db.getReservationBasicInfo(reservationId); 
        if (info == null) return false;

        String msg = buildMessage(NotificationType.RESERVATION_REMINDER,
                info.fullName, info.dateTime, info.guests, info.confirmationCode, null, null);

        return send(contact, msg);
    }


    // 3) Table available 
    public boolean sendTableAvailable(int reservationId) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

        ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);
        if (info == null) return false;

        String msg = buildMessage(NotificationType.TABLE_AVAILABLE,
                info.fullName, null, null, info.confirmationCode, null, null);

        return send(contact, msg);
    }


    // 4) Table received 
    public boolean sendTableReceived(int reservationId) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

        String name = (contact != null ? contact.getFullName() : "Customer");

        String msg = buildMessage(NotificationType.TABLE_RECEIVED,
                name, null, null, null, null, null);

        return send(contact, msg);
    }


    // 5) Bill sent 
    public boolean sendBillSent(int reservationId, Bill bill) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

        ReservationBasicInfo info = db.getReservationBasicInfo(reservationId);
        if (info == null) return false;

        String msg = buildMessage(NotificationType.BILL_SENT,
                info.fullName, null, null, info.confirmationCode,
                bill.getAmountBeforeDiscount(), bill.getFinalAmount());

        return send(contact, msg);
    }


    // 6) Payment success 
    public boolean sendPaymentSuccess(int reservationId) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

        String name = (contact != null ? contact.getFullName() : "Customer");

        String msg = buildMessage(NotificationType.PAYMENT_SUCCESS,
                name, null, null, null, null, null);

        return send(contact, msg);
    }


    // 7) Reservation canceled
    public boolean sendReservationCanceled(int reservationId) throws SQLException {

        CustomerContactInfo contact = db.getContactInfoByReservationId(reservationId);

        String name = (contact != null ? contact.getFullName() : "Customer");

        String msg = buildMessage(NotificationType.RESERVATION_CANCELED,
                name, null, null, null, null, null);

        return send(contact, msg);
    }

    
    /**
     * Sends message to contact info (simulation).
     * - If email exists -> "simulate email"
     * - If phone exists -> "simulate SMS"
     * - If both missing -> log and return false
     */
    public boolean send(CustomerContactInfo contact, String message) {

        if (message == null || message.isBlank()) {
            ui.display("❌ Notification NOT sent: empty message.");
            return false;
        }

        if (contact == null) {
            ui.display("❌ Notification NOT sent: contact info is null.");
            return false;
        }

        boolean hasEmail = contact.getEmail() != null && !contact.getEmail().isBlank();
        boolean hasPhone = contact.getPhone() != null && !contact.getPhone().isBlank();

        if (!hasEmail && !hasPhone) {
            ui.display("❌ Notification NOT sent: no email/phone for customerId=" + contact.getCustomerId());
            return false;
        }

        boolean sentSomething = false;

        if (hasEmail) {
            // SIMULATE email
            ui.display("📧 [EMAIL] To: " + contact.getEmail());
            ui.display(message);
            sentSomething = true;
        }

        if (hasPhone) {
            // SIMULATE SMS
            ui.display("📱 [SMS] To: " + contact.getPhone());
            ui.display(message);
            sentSomething = true;
        }

        return sentSomething;
    }
    
    private static final DateTimeFormatter FORMATTER =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public static String buildMessage(
            NotificationType type,
            String name,
            LocalDateTime dateTime,
            Integer guests,
            Integer confirmationCode,
            Double amountBefore,
            Double finalAmount
    ) {

        if (name == null || name.isBlank()) {
            name = "Customer";
        }

        return switch (type) {

            case RESERVATION_CONFIRMATION -> """
                    Hello %s,

                    Your reservation has been successfully created.

                    Reservation details:
                    • Date & Time: %s
                    • Number of Guests: %d
                    • Confirmation Code: %d

                    Please keep this code safe.

                    Restaurant Management
                    """.formatted(
                            name,
                            format(dateTime),
                            guests,
                            confirmationCode
                    );

            case RESERVATION_REMINDER -> """
                    Hello %s,

                    This is a friendly reminder that your reservation is scheduled in 2 hours.

                    • Date & Time: %s
                    • Number of Guests: %d

                    We look forward to welcoming you.

                    Restaurant Management
                    """.formatted(
                            name,
                            format(dateTime),
                            guests
                    );

            case TABLE_AVAILABLE -> """
                    Hello %s,

                    A table is now available for you.

                    Please arrive as soon as possible and present your confirmation code:
                    %d

                    Restaurant Management
                    """.formatted(
                            name,
                            confirmationCode
                    );

            case TABLE_RECEIVED -> """
                    Hello %s,

                    Your table has been successfully received.
                    We wish you a pleasant dining experience.

                    Restaurant Management
                    """.formatted(name);

            case BILL_SENT -> """
                    Hello %s,

                    Your bill is ready.

                    • Amount before discount: %.2f
                    • Final amount to pay: %.2f

                    Please proceed to payment using your confirmation code.

                    Restaurant Management
                    """.formatted(
                            name,
                            amountBefore,
                            finalAmount
                    );

            case PAYMENT_SUCCESS -> """
                    Hello %s,

                    Your payment has been successfully completed.

                    Thank you for visiting us.
                    We hope to see you again soon.

                    Restaurant Management
                    """.formatted(name);

            case RESERVATION_CANCELED -> """
                    Hello %s,

                    Your reservation has been canceled.

                    If you wish to make a new reservation,
                    you are welcome to do so at any time.

                    Restaurant Management
                    """.formatted(name);
        };
    }

    private static String format(LocalDateTime dt) {
        return dt == null ? "N/A" : dt.format(FORMATTER);
    }
}
