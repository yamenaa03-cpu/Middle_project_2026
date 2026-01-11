package common.entity;

import java.io.Serializable;

/**
 * Represents billing information for a reservation, including amounts and
 * payment status.
 */
public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int billId;
    private int reservationId;
    private double amountBeforeDiscount;
    private double finalAmount;
    private boolean paid;

    /**
     * Construct a Bill record.
     *
     * @param billId unique identifier of the bill
     * @param reservationId reservation id this bill is for
     * @param amountBeforeDiscount amount prior to discounts
     * @param finalAmount final amount to be paid
     * @param paid whether the bill is paid
     */
    public Bill(int billId, int reservationId, double amountBeforeDiscount, double finalAmount, boolean paid) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.amountBeforeDiscount = amountBeforeDiscount;
        this.finalAmount = finalAmount;
        this.paid = paid;
    }

    /**
     * Returns the bill identifier.
     *
     * @return bill id
     */
    public int getBillId() { return billId; }

    /**
     * Returns the reservation id associated with this bill.
     *
     * @return reservation id
     */
    public int getReservationId() { return reservationId; }

    /**
     * Returns the amount before discounts are applied.
     *
     * @return amount before discount
     */
    public double getAmountBeforeDiscount() { return amountBeforeDiscount; }

    /**
     * Returns the final payable amount after discounts.
     *
     * @return final amount
     */
    public double getFinalAmount() { return finalAmount; }

    /**
     * Returns whether the bill has been paid.
     *
     * @return true if paid, false otherwise
     */
    public boolean isPaid() { return paid; }

    /**
     * Mark the bill as paid or unpaid.
     *
     * @param paid new paid status
     */
    public void setPaid(boolean paid) { this.paid = paid; }
}
