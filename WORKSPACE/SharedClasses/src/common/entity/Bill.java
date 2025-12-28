package common.entity;

import java.io.Serializable;

public class Bill implements Serializable {
    private static final long serialVersionUID = 1L;

    private int billId;
    private int reservationId;
    private double amountBeforeDiscount;
    private double finalAmount;
    private boolean paid;

    public Bill(int billId, int reservationId, double amountBeforeDiscount, double finalAmount, boolean paid) {
        this.billId = billId;
        this.reservationId = reservationId;
        this.amountBeforeDiscount = amountBeforeDiscount;
        this.finalAmount = finalAmount;
        this.paid = paid;
    }

    public int getBillId() { return billId; }
    public int getReservationId() { return reservationId; }
    public double getAmountBeforeDiscount() { return amountBeforeDiscount; }
    public double getFinalAmount() { return finalAmount; }
    public boolean isPaid() { return paid; }

    public void setPaid(boolean paid) { this.paid = paid; }
}
