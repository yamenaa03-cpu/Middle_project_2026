package clientGUI;

import java.time.Year;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import client.Client;
import common.dto.Report.ReportRequest;
import common.dto.Report.ReportResponse;
import common.entity.SubscriberReportEntry;
import common.entity.TimeReportEntry;
import common.enums.ReportOperation;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;

/**
 * ManagerStatisticsController displays reports and analytics for restaurant managers.
 * 
 * <p>This controller provides a comprehensive statistics view with charts showing
 * subscriber activity and timing data for a selected month/year period.
 * 
 * <p>Available Reports:
 * <ul>
 *   <li><b>Subscriber Report</b> - Bar chart showing top subscribers by:
 *       <ul>
 *         <li>Total reservations</li>
 *         <li>Completed reservations</li>
 *         <li>Cancelled reservations</li>
 *         <li>Waitlist entries</li>
 *       </ul>
 *   </li>
 *   <li><b>Time Report</b> - Line chart showing:
 *       <ul>
 *         <li>Arrival delay (minutes early/late)</li>
 *         <li>Session duration (dining time)</li>
 *       </ul>
 *   </li>
 * </ul>
 * 
 * <p>Features:
 * <ul>
 *   <li>Month/Year selection for historical data</li>
 *   <li>Load individual or both reports</li>
 *   <li>Top 10 subscribers displayed to avoid chart overcrowding</li>
 *   <li>Sorted time report for readability</li>
 * </ul>
 * 
 * @author G22 Team
 * @version 1.0
 * @see ClientController
 * @see ReportResponse
 * @see SubscriberReportEntry
 * @see TimeReportEntry
 */
public class ManagerStatisticsController {

    // ==========================================================
    // SERVER CONNECTION AND CONTROLLERS
    // ==========================================================
    
    /** Client instance for server communication */
    private Client client;
    
    /** Reference to main client controller */
    private ClientController mainController;

    // ==========================================================
    // FXML UI COMPONENTS - FILTERS
    // ==========================================================
    
    /** Month selection dropdown (1-12 with names) */
    @FXML private ChoiceBox<String> monthChoice;
    
    /** Year selection dropdown (current year to 5 years back) */
    @FXML private ChoiceBox<Integer> yearChoice;

    /** Status label for loading/error messages */
    @FXML private Label statusLabel;

    // ==========================================================
    // FXML UI COMPONENTS - CHARTS
    // ==========================================================
    
    /** Bar chart for subscriber activity report */
    @FXML private BarChart<String, Number> subscriberBarChart;
    
    /** Line chart for timing/delay report */
    @FXML private LineChart<String, Number> timeLineChart;

    // ==========================================================
    // SETTERS
    // ==========================================================
    
    /**
     * Sets the client instance for server communication.
     * 
     * @param client the Client instance
     */
    public void setClient(Client client) {
        this.client = client;
    }

    /**
     * Sets the main controller reference.
     * 
     * @param c the ClientController instance
     */
    public void setMainController(ClientController c) {
        this.mainController = c;
    }

    // ==========================================================
    // INITIALIZATION
    // ==========================================================
    
    /**
     * Initializes the controller after FXML loading.
     * Sets up month/year dropdowns and chart configurations.
     */
    @FXML
    public void initialize() {
        monthChoice.getItems().addAll(
                "1 - January", "2 - February", "3 - March", "4 - April",
                "5 - May", "6 - June", "7 - July", "8 - August",
                "9 - September", "10 - October", "11 - November", "12 - December"
        );
        monthChoice.getSelectionModel().select(Year.now().atMonth(1).getMonthValue() - 1);
        monthChoice.getSelectionModel().select(java.time.LocalDate.now().getMonthValue() - 1);

        int currentYear = Year.now().getValue();
        for (int y = currentYear; y >= currentYear - 5; y--) {
            yearChoice.getItems().add(y);
        }
        yearChoice.getSelectionModel().selectFirst();

        subscriberBarChart.setAnimated(false);
        timeLineChart.setAnimated(false);

        statusLabel.setText("Select month/year then load a report.");
    }

    // ==========================================================
    // REPORT LOADING ACTIONS
    // ==========================================================
    
    /**
     * Loads the subscriber activity report from server.
     * Shows bar chart with top subscribers by reservation counts.
     */
    @FXML
    public void onLoadSubscriberReport() {
        int year = getSelectedYear();
        int month = getSelectedMonth();

        if (!ensureClientConnected()) return;

        statusLabel.setText("Loading subscriber report...");
        try {
            client.sendToServer(ReportRequest.createSubscriberReportRequest(year, month));
        } catch (Exception e) {
            printCatch("onLoadSubscriberReport", e);
            statusLabel.setText("Failed to send subscriber report request: " + e.getMessage());
        }
    }

    /**
     * Loads the time/delay report from server.
     * Shows line chart with arrival delays and session durations.
     */
    @FXML
    public void onLoadTimeReport() {
        int year = getSelectedYear();
        int month = getSelectedMonth();

        if (!ensureClientConnected()) return;

        statusLabel.setText("Loading time report...");
        try {
            client.sendToServer(ReportRequest.createTimeReportRequest(year, month));
        } catch (Exception e) {
            printCatch("onLoadTimeReport", e);
            statusLabel.setText("Failed to send time report request: " + e.getMessage());
        }
    }

    /**
     * Loads both subscriber and time reports.
     */
    @FXML
    public void onLoadBoth() {
        onLoadSubscriberReport();
        onLoadTimeReport();
    }

    /**
     * Closes the statistics window.
     */
    @FXML
    public void onClose() {
        Stage stage = (Stage) ((Node) statusLabel).getScene().getWindow();
        stage.close();
    }

    // ==========================================================
    // SERVER RESPONSE HANDLING
    // ==========================================================
    
    /**
     * Handles report response from server.
     * Called by ClientController when ReportResponse arrives.
     * 
     * @param resp the report response from server
     */
    public void onReportResponse(ReportResponse resp) {
        Platform.runLater(() -> {
            if (resp == null) return;

            statusLabel.setText(resp.getMessage());

            if (!resp.isSuccess()) {
                return;
            }

            if (resp.getOperation() == ReportOperation.GET_SUBSCRIBER_REPORT) {
                renderSubscriberReport(resp.getSubscriberReportEntries());
            } else if (resp.getOperation() == ReportOperation.GET_TIME_REPORT) {
                renderTimeReport(resp.getTimeReportEntries());
            }
        });
    }

    // ==========================================================
    // CHART RENDERING
    // ==========================================================
    
    /**
     * Renders the subscriber bar chart with report data.
     * Shows top 10 subscribers by total reservations.
     * 
     * @param entries list of subscriber report entries
     */
    private void renderSubscriberReport(List<SubscriberReportEntry> entries) {
        subscriberBarChart.getData().clear();

        if (entries == null || entries.isEmpty()) {
            return;
        }

        List<SubscriberReportEntry> top = entries.stream()
                .sorted(Comparator.comparingInt(SubscriberReportEntry::getTotalReservations).reversed())
                .limit(10)
                .collect(Collectors.toList());

        XYChart.Series<String, Number> total = new XYChart.Series<>();
        total.setName("Total");

        XYChart.Series<String, Number> completed = new XYChart.Series<>();
        completed.setName("Completed");

        XYChart.Series<String, Number> cancelled = new XYChart.Series<>();
        cancelled.setName("Cancelled");

        XYChart.Series<String, Number> waitlist = new XYChart.Series<>();
        waitlist.setName("Waitlist");

        for (SubscriberReportEntry e : top) {
            String label = e.getCustomerName() != null && !e.getCustomerName().isBlank()
                    ? e.getCustomerName()
                    : ("Customer " + e.getCustomerId());

            total.getData().add(new XYChart.Data<>(label, e.getTotalReservations()));
            completed.getData().add(new XYChart.Data<>(label, e.getCompletedReservations()));
            cancelled.getData().add(new XYChart.Data<>(label, e.getCancelledReservations()));
            waitlist.getData().add(new XYChart.Data<>(label, e.getWaitlistEntries()));
        }

        subscriberBarChart.getData().addAll(total, completed, cancelled, waitlist);
    }

    /**
     * Renders the time line chart with report data.
     * Shows arrival delays and session durations sorted by time.
     * 
     * @param entries list of time report entries
     */
    private void renderTimeReport(List<TimeReportEntry> entries) {
        timeLineChart.getData().clear();

        if (entries == null || entries.isEmpty()) {
            return;
        }

        List<TimeReportEntry> sorted = entries.stream()
                .sorted(Comparator.comparing(e -> e.getScheduledTime() == null ? java.time.LocalDateTime.MIN : e.getScheduledTime()))
                .collect(Collectors.toList());

        XYChart.Series<String, Number> delaySeries = new XYChart.Series<>();
        delaySeries.setName("Arrival delay (min)");

        XYChart.Series<String, Number> sessionSeries = new XYChart.Series<>();
        sessionSeries.setName("Session duration (min)");

        for (TimeReportEntry e : sorted) {
            String x = "Res#" + e.getReservationId();

            long delay = e.getArrivalDelayMinutes();
            long duration = e.getSessionDurationMinutes();

            delaySeries.getData().add(new XYChart.Data<>(x, delay));
            sessionSeries.getData().add(new XYChart.Data<>(x, duration));
        }

        timeLineChart.getData().addAll(delaySeries, sessionSeries);
    }

    // ==========================================================
    // HELPER METHODS
    // ==========================================================
    
    /**
     * Extracts the month number from the selection string.
     * 
     * @return the selected month (1-12)
     */
    private int getSelectedMonth() {
        String s = monthChoice.getValue();
        if (s == null || !s.contains(" - ")) return 1;
        try {
            return Integer.parseInt(s.split(" - ")[0].trim());
        } catch (Exception e) {
            return 1;
        }
    }

    /**
     * Gets the selected year value.
     * 
     * @return the selected year
     */
    private int getSelectedYear() {
        Integer y = yearChoice.getValue();
        return (y == null) ? Year.now().getValue() : y;
    }

    /**
     * Checks if client is connected to server.
     * 
     * @return true if connected, false otherwise
     */
    private boolean ensureClientConnected() {
        if (client == null || !client.isConnected()) {
            statusLabel.setText("Not connected to server.");
            return false;
        }
        return true;
    }

    /**
     * Logs exception details for debugging.
     * 
     * @param where the method name where exception occurred
     * @param e the exception
     */
    private void printCatch(String where, Exception e) {
        System.out.println("EXCEPTION in " + where + ": " + e);
        e.printStackTrace();
    }
}
