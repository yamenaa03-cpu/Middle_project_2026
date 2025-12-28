package common.entity;

import java.io.Serializable;

public class Table implements Serializable {
    private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int seats;

    public Table(int tableNumber, int seats) {
        this.tableNumber = tableNumber;
        this.seats = seats;
    }

    public int getTableNumber() { return tableNumber; }
    public int getSeats() { return seats; }

    public void setSeats(int seats) { this.seats = seats; }
}
