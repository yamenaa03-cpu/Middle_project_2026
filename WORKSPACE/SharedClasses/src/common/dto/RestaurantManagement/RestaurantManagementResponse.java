package common.dto.RestaurantManagement;

import java.io.Serializable;
import java.util.List;
import common.entity.Table;
import common.entity.DateOverride;
import common.entity.OpeningHours;

public class RestaurantManagementResponse implements Serializable {
	private static final long serialVersionUID = 1L;

	private boolean success;
	private String message;
	private List<Table> tables;
	private List<OpeningHours> openingHours;
	private List<DateOverride> dateOverrides;
	private int newTableNumber;

	// Getters
	public boolean isSuccess() { return success; }
	public String getMessage() { return message; }
	public List<Table> getTables() { return tables; }
	public List<OpeningHours> getOpeningHours() { return openingHours; }
	public List<DateOverride> getDateOverrides() { return dateOverrides; }
	public int getNewTableNumber() { return newTableNumber; }

	// ======================== TABLE RESPONSES ========================

	public static RestaurantManagementResponse tablesLoaded(List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Tables loaded.";
		resp.tables = tables;
		return resp;
	}

	public static RestaurantManagementResponse tableAdded(int newTableNumber, List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Table added successfully.";
		resp.newTableNumber = newTableNumber;
		resp.tables = tables;
		return resp;
	}

	public static RestaurantManagementResponse tableUpdated(List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Table updated.";
		resp.tables = tables;
		return resp;
	}

	public static RestaurantManagementResponse tableDeleted(List<Table> tables) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Table deleted.";
		resp.tables = tables;
		return resp;
	}

	// ======================== HOURS RESPONSES ========================

	public static RestaurantManagementResponse hoursLoaded(List<OpeningHours> hours) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Opening hours loaded.";
		resp.openingHours = hours;
		return resp;
	}

	public static RestaurantManagementResponse hoursUpdated(List<OpeningHours> hours) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Hours updated.";
		resp.openingHours = hours;
		return resp;
	}

	// ======================== OVERRIDE RESPONSES ========================

	public static RestaurantManagementResponse overridesLoaded(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Date overrides loaded.";
		resp.dateOverrides = overrides;
		return resp;
	}

	public static RestaurantManagementResponse overrideAdded(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Override added.";
		resp.dateOverrides = overrides;
		return resp;
	}

	public static RestaurantManagementResponse overrideUpdated(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Override updated.";
		resp.dateOverrides = overrides;
		return resp;
	}

	public static RestaurantManagementResponse overrideDeleted(List<DateOverride> overrides) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = true;
		resp.message = "Override deleted.";
		resp.dateOverrides = overrides;
		return resp;
	}

	// ======================== ERROR RESPONSE ========================

	public static RestaurantManagementResponse fail(String message) {
		RestaurantManagementResponse resp = new RestaurantManagementResponse();
		resp.success = false;
		resp.message = message;
		return resp;
	}
}
