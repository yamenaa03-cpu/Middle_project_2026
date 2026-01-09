package common.dto.Reservation;

import java.io.Serializable;

public class PayBillResult implements Serializable {

    private static final long serialVersionUID = 1L;

    private final boolean success;
    private final String message;
    private final Double finalAmount; 
    private int freedCapacity;

    private PayBillResult(boolean success, String message, Double finalAmount, int freedCapacity) {
        this.success = success;
        this.message = message;
        this.finalAmount = finalAmount;
        this.freedCapacity = freedCapacity;
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public Double getFinalAmount() {
        return finalAmount;
    }
    
    public int getFreedCapacity() {
        return freedCapacity;
    }

    // ===== Factory methods =====

    public static PayBillResult ok(double amount, int freedCapacity) {
        return new PayBillResult(true, "PAID", amount, freedCapacity);
    }

    public static PayBillResult fail(String msg) {
        return new PayBillResult(false, msg, null, 0);
    }
}
