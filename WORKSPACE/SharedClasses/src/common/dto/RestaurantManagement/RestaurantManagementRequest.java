package common.dto.RestaurantManagement;

import java.io.Serializable;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import common.enums.RestaurantManagementOperation;

public class RestaurantManagementRequest implements Serializable {
	private static final long serialVersionUID = 1L;

	private RestaurantManagementOperation operation;

	// Table fields
	private int tableNumber;
	private int seats;

	// Hours fields
	private DayOfWeek dayOfWeek;
	private LocalTime openTime;
	private LocalTime closeTime;
	private boolean closed;

	// Date override fields
	private int overrideId;
	private LocalDate overrideDate;
	private String reason;

	// Getters
	public RestaurantManagementOperation getOperation() { return operation; }
	public int getTableNumber() { return tableNumber; }
	public int getSeats() { return seats; }
	public DayOfWeek getDayOfWeek() { return dayOfWeek; }
	public LocalTime getOpenTime() { return openTime; }
	public LocalTime getCloseTime() { return closeTime; }
	public boolean isClosed() { return closed; }
	public int getOverrideId() { return overrideId; }
	public LocalDate getOverrideDate() { return overrideDate; }
	public String getReason() { return reason; }

	// ======================== TABLE FACTORIES ========================

	public static RestaurantManagementRequest createGetAllTablesRequest() {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.GET_ALL_TABLES;
		return req;
	}

	public static RestaurantManagementRequest createAddTableRequest(int seats) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.ADD_TABLE;
		req.seats = seats;
		return req;
	}

	public static RestaurantManagementRequest createUpdateTablerequest(int tableNumber, int newSeats) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.UPDATE_TABLE;
		req.tableNumber = tableNumber;
		req.seats = newSeats;
		return req;
	}

	public static RestaurantManagementRequest createDeleteTableRequest(int tableNumber) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.DELETE_TABLE;
		req.tableNumber = tableNumber;
		return req;
	}

	// ======================== HOURS FACTORIES ========================

	public static RestaurantManagementRequest createGetOpeningHoursRequest() {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.GET_OPENING_HOURS;
		return req;
	}

	public static RestaurantManagementRequest createUpdateOpeningHoursRequest(DayOfWeek day, LocalTime open, LocalTime close, boolean closed) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.UPDATE_OPENING_HOURS;
		req.dayOfWeek = day;
		req.openTime = open;
		req.closeTime = close;
		req.closed = closed;
		return req;
	}

	// ======================== DATE OVERRIDE FACTORIES ========================

	public static RestaurantManagementRequest createGetDateOverridesRequest() {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.GET_DATE_OVERRIDES;
		return req;
	}

	public static RestaurantManagementRequest createAddDateOverrideRequest(LocalDate date, LocalTime open, LocalTime close, boolean closed, String reason) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.ADD_DATE_OVERRIDE;
		req.overrideDate = date;
		req.openTime = open;
		req.closeTime = close;
		req.closed = closed;
		req.reason = reason;
		return req;
	}

	public static RestaurantManagementRequest createUpdateDateOverrideRequest(int id, LocalDate date, LocalTime open, LocalTime close, boolean closed, String reason) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.UPDATE_DATE_OVERRIDE;
		req.overrideId = id;
		req.overrideDate = date;
		req.openTime = open;
		req.closeTime = close;
		req.closed = closed;
		req.reason = reason;
		return req;
	}

	public static RestaurantManagementRequest createDeleteDateOverrideRequest(int id) {
		RestaurantManagementRequest req = new RestaurantManagementRequest();
		req.operation = RestaurantManagementOperation.DELETE_DATE_OVERRIDE;
		req.overrideId = id;
		return req;
	}
}
