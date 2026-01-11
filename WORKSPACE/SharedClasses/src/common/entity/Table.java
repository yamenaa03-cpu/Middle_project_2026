package common.entity;

import java.io.Serializable;

/**
 * Represents a physical table in the restaurant with an identifying number
 * and seating capacity.
 */
public class Table implements Serializable {
    private static final long serialVersionUID = 1L;

    private int tableNumber;
    private int seats;

    /**
     * Create a new Table instance.
     *
     * @param tableNumber unique number identifying the table
     * @param seats number of seats available at the table
     */
    public Table(int tableNumber, int seats) {
        this.tableNumber = tableNumber;
        this.seats = seats;
    }

    /**
     * Returns the table number.
     *
     * @return the table number
     */
    public int getTableNumber() { return tableNumber; }

    /**
     * Returns the number of seats at the table.
     *
     * @return number of seats
     */
    public int getSeats() { return seats; }

    /**
     * Update the seating capacity for this table.
     *
     * @param seats new number of seats
     */
    public void setSeats(int seats) { this.seats = seats; }
}
