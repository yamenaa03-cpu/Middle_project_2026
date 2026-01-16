package clientGUI;

import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import client.Client;
import common.dto.Reservation.ReservationRequest;
import common.dto.Reservation.ReservationResponse;
import common.dto.RestaurantManagement.RestaurantManagementRequest;
import common.dto.RestaurantManagement.RestaurantManagementResponse;
import common.dto.UserAccount.UserAccountRequest;
import common.dto.UserAccount.UserAccountResponse;
import common.entity.Customer;
import common.entity.Reservation;
import common.entity.Table;
import common.entity.OpeningHours; // <-- rename if your class differs
import common.enums.ReservationOperation;
import common.enums.RestaurantManagementOperation;
import common.enums.UserAccountOperation;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.VBox;

/**
 * EmployeeDashboardController
 * - Sends requests to server via OCSF Client
 * - Receives responses via handleServerMessage(...)
 */
public class EmployeeDashboardController {

    // ====== CLIENT ======
    private Client client;

    public void setClient(Client client) {
        this.client = client;
        
        refreshAll();
    }

    // ====== TOP ======
    @FXML private Button btnRefreshAll;

    // ====== TABS ROOT ======
    @FXML private TabPane dashboardTabs;

    // ================= WAITING LIST =================
    @FXML private Button btnRefreshWaitingList;
    @FXML private ChoiceBox<String> waitingStatusFilter;

    @FXML private TableView<Reservation> waitingTable;
    @FXML private TableColumn<Reservation, String> colWaitId;
    @FXML private TableColumn<Reservation, String> colWaitCustomer;
    @FXML private TableColumn<Reservation, String> colWaitGuests;
    @FXML private TableColumn<Reservation, String> colWaitTime;
    @FXML private TableColumn<Reservation, String> colWaitStatus;
    @FXML private TableColumn<Reservation, String> colWaitNotes;

    // ================= RESERVATIONS =================
    @FXML private TextField reservationSearchField;
    @FXML private Button btnSearchReservation;
    @FXML private ChoiceBox<String> reservationStatusFilter;
    @FXML private Button btnRefreshReservations;

    @FXML private TableView<Reservation> reservationTable;
    @FXML private TableColumn<Reservation, String> colResId;
    @FXML private TableColumn<Reservation, String> colResDateTime;
    @FXML private TableColumn<Reservation, String> colResGuests;
    @FXML private TableColumn<Reservation, String> colResConf;
    @FXML private TableColumn<Reservation, String> colResCustomerId;
    @FXML private TableColumn<Reservation, String> colResTableId;
    @FXML private TableColumn<Reservation, String> colResStatus;
    @FXML private TableColumn<Reservation, String> colResCreated;

    // ================= DINERS NOW =================
    @FXML private Button btnRefreshDinersNow;
    @FXML private Label lblDinersNowStatus;

    @FXML private TableView<Customer> dinersNowTable; // server returns List<Customer>
    @FXML private TableColumn<Customer, String> colDinerResId;
    @FXML private TableColumn<Customer, String> colDinerCustomer;
    @FXML private TableColumn<Customer, String> colDinerGuests;
    @FXML private TableColumn<Customer, String> colDinerTableId;
    @FXML private TableColumn<Customer, String> colDinerStatus;
    @FXML private TableColumn<Customer, String> colDinerSince;

    // ================= TABLES =================
    @FXML private Button btnRefreshTables;
    @FXML private Label lblSelectedTableId;
    @FXML private Label lblTablesMsg;

    @FXML private TextField tableCapacityField;
    @FXML private Button btnAddTable;
    @FXML private Button btnUpdateTableCapacity;
    @FXML private Button btnDeleteTable;

    @FXML private FlowPane tablesGraphPane;
    @FXML private TableView<Table> tablesTable;
    @FXML private TableColumn<Table, String> colTableId;
    @FXML private TableColumn<Table, String> colCapacity;

    private Table selectedTable = null;

    // ================= WORKING HOURS =================
    @FXML private Button btnLoadHours;
    @FXML private Button btnSaveHours;

    @FXML private TableView<OpeningHours> hoursTable; // rename if needed
    @FXML private TableColumn<OpeningHours, String> colDay;
    @FXML private TableColumn<OpeningHours, String> colOpen;
    @FXML private TableColumn<OpeningHours, String> colClose;
    @FXML private TableColumn<OpeningHours, String> colClosed;

    // ================= MEMBERS =================
    @FXML private TextField memberSearchField;
    @FXML private Button btnSearchMember;
    @FXML private Button btnRefreshMembers;

    @FXML private TableView<Customer> membersTable;
    @FXML private TableColumn<Customer, String> colCustId;
    @FXML private TableColumn<Customer, String> colCustName;
    @FXML private TableColumn<Customer, String> colCustPhone;
    @FXML private TableColumn<Customer, String> colCustEmail;
    @FXML private TableColumn<Customer, String> colCustSubscribed;
    @FXML private TableColumn<Customer, String> colCustCode;

    // ================= ON BEHALF - FIND CUSTOMER =================
    @FXML private RadioButton rbFindByCode;
    @FXML private RadioButton rbFindByPhone;
    @FXML private RadioButton rbFindByEmail;

    @FXML private TextField tfFindCustomerValue;
    @FXML private Button btnFindCustomer;
    @FXML private Button btnClearCustomerSearch;
    @FXML private Label lblFindCustomerStatus;

    @FXML private TableView<Customer> customerSearchResultsTable;
    @FXML private TableColumn<Customer, String> colFoundCustId;
    @FXML private TableColumn<Customer, String> colFoundCustName;
    @FXML private TableColumn<Customer, String> colFoundCustPhone;
    @FXML private TableColumn<Customer, String> colFoundCustEmail;
    @FXML private TableColumn<Customer, String> colFoundCustCode;

    @FXML private Button btnSelectCustomerFromResults;
    @FXML private Button btnUnselectCustomer;
    @FXML private Label lblSelectedCustomer;
    @FXML private Label lblOnBehalfGlobalStatus;
    @FXML private Label lblSelectedCustomerIdHidden;

    private Customer selectedCustomer = null;

    // ================= ON BEHALF - MAKE RESERVATION =================
    @FXML private DatePicker dpMakeResDate;
    @FXML private ComboBox<String> cbMakeResHour;
    @FXML private ComboBox<String> cbMakeResMinute;
    @FXML private Spinner<Integer> spMakeResGuests;

    @FXML private CheckBox cbMakeResGuestMode;
    @FXML private TextField tfMakeResGuestName;
    @FXML private TextField tfMakeResGuestPhone;
    @FXML private TextField tfMakeResGuestEmail;

    @FXML private Button btnMakeReservationOnBehalf;
    @FXML private Label lblMakeReservationStatus;

    // ================= ON BEHALF - JOIN WAITLIST =================
    @FXML private Spinner<Integer> spWaitlistGuests;
    @FXML private CheckBox cbWaitlistGuestMode;
    @FXML private TextField tfWaitlistGuestName;
    @FXML private TextField tfWaitlistGuestPhone;
    @FXML private TextField tfWaitlistGuestEmail;
    @FXML private Button btnJoinWaitlistOnBehalf;
    @FXML private Label lblJoinWaitlistStatus;

    // ================= ON BEHALF - CANCEL =================
    @FXML private Button btnLoadCancelableReservations;
    @FXML private Label lblCancelLoadStatus;
    @FXML private TableView<Reservation> cancelReservationsTable;
    @FXML private TableColumn<Reservation, String> colCancelResId;
    @FXML private TableColumn<Reservation, String> colCancelDateTime;
    @FXML private TableColumn<Reservation, String> colCancelGuests;
    @FXML private TableColumn<Reservation, String> colCancelStatus;
    @FXML private TableColumn<Reservation, String> colCancelConf;
    @FXML private Button btnCancelSelectedReservation;
    @FXML private Label lblCancelStatus;

    // ================= ON BEHALF - RECEIVE =================
    @FXML private Button btnLoadReceivableReservations;
    @FXML private Label lblReceiveLoadStatus;
    @FXML private TableView<Reservation> receiveReservationsTable;
    @FXML private TableColumn<Reservation, String> colReceiveResId;
    @FXML private TableColumn<Reservation, String> colReceiveDateTime;
    @FXML private TableColumn<Reservation, String> colReceiveGuests;
    @FXML private TableColumn<Reservation, String> colReceiveStatus;
    @FXML private TableColumn<Reservation, String> colReceiveConf;
    @FXML private Button btnReceiveTableForSelected;
    @FXML private Label lblReceiveStatus;

    // ================= ON BEHALF - CHECKOUT =================
    @FXML private Button btnLoadCheckoutReservations;
    @FXML private Label lblCheckoutLoadStatus;
    @FXML private TableView<Reservation> checkoutReservationsTable;
    @FXML private TableColumn<Reservation, String> colCheckoutResId;
    @FXML private TableColumn<Reservation, String> colCheckoutDateTime;
    @FXML private TableColumn<Reservation, String> colCheckoutGuests;
    @FXML private TableColumn<Reservation, String> colCheckoutStatus;
    @FXML private TableColumn<Reservation, String> colCheckoutConf;

    @FXML private Button btnLoadBillForSelected;
    @FXML private Button btnPayLoadedBill;
    @FXML private Label lblCheckoutStatus;
    @FXML private TextArea taBillDetails;

    private Integer loadedBillId = null;
    private DayOfWeek dayOfWeek;
    private LocalTime openTime;
    private LocalTime closeTime;
    private boolean closed;

    public DayOfWeek getDayOfWeek() { return dayOfWeek; }
    public LocalTime getOpenTime() { return openTime; }
    public LocalTime getCloseTime() { return closeTime; }
    public boolean isClosed() { return closed; }

    // ================== DATA LISTS ==================
    private final ObservableList<Reservation> waitingList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> reservationsList = FXCollections.observableArrayList();
    private final ObservableList<Customer> dinersList = FXCollections.observableArrayList();
    private final ObservableList<Table> tablesList = FXCollections.observableArrayList();
    private final ObservableList<Customer> membersList = FXCollections.observableArrayList();
    private final ObservableList<Customer> foundCustomersList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> cancelableList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> receivableList = FXCollections.observableArrayList();
    private final ObservableList<Reservation> checkoutList = FXCollections.observableArrayList();

    private static final DateTimeFormatter DT_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @FXML
    private void initialize() {
        // ---- Attach handlers (because your FXML buttons mostly have fx:id only) ----
        btnRefreshAll.setOnAction(e -> refreshAll());

        btnRefreshWaitingList.setOnAction(e -> refreshWaitingList());
        btnRefreshReservations.setOnAction(e -> refreshReservations());
        btnSearchReservation.setOnAction(e -> searchReservationsLocal()); // client-side filter example
        btnRefreshDinersNow.setOnAction(e -> refreshDinersNow());

        btnRefreshTables.setOnAction(e -> refreshTables());
        btnAddTable.setOnAction(e -> addTable());
        btnUpdateTableCapacity.setOnAction(e -> updateSelectedTable());
        btnDeleteTable.setOnAction(e -> deleteSelectedTable());

        btnLoadHours.setOnAction(e -> loadHours());
        btnSaveHours.setOnAction(e -> saveHours());

        btnRefreshMembers.setOnAction(e -> refreshMembers());
        btnSearchMember.setOnAction(e -> searchMembersLocal()); // client-side filter example

        btnFindCustomer.setOnAction(e -> findCustomer());
        btnClearCustomerSearch.setOnAction(e -> clearCustomerSearch());
        btnSelectCustomerFromResults.setOnAction(e -> useSelectedCustomer());
        btnUnselectCustomer.setOnAction(e -> unselectCustomer());

        btnMakeReservationOnBehalf.setOnAction(e -> makeReservationOnBehalf());
        btnJoinWaitlistOnBehalf.setOnAction(e -> joinWaitlistOnBehalf());

        btnLoadCancelableReservations.setOnAction(e -> loadCancelableReservations());
        btnCancelSelectedReservation.setOnAction(e -> cancelSelectedReservation());

        btnLoadReceivableReservations.setOnAction(e -> loadReceivableReservations());
        btnReceiveTableForSelected.setOnAction(e -> receiveTableForSelected());

        btnLoadCheckoutReservations.setOnAction(e -> loadCheckoutReservations());
        btnLoadBillForSelected.setOnAction(e -> loadBillForSelected());
        btnPayLoadedBill.setOnAction(e -> payLoadedBill());

        // ---- Toggle group for find customer ----
        ToggleGroup tg = new ToggleGroup();
        rbFindByCode.setToggleGroup(tg);
        rbFindByPhone.setToggleGroup(tg);
        rbFindByEmail.setToggleGroup(tg);
        rbFindByCode.setSelected(true);

        // ---- Set table items ----
        waitingTable.setItems(waitingList);
        reservationTable.setItems(reservationsList);
        dinersNowTable.setItems(dinersList);
        tablesTable.setItems(tablesList);
        membersTable.setItems(membersList);
        customerSearchResultsTable.setItems(foundCustomersList);
        cancelReservationsTable.setItems(cancelableList);
        receiveReservationsTable.setItems(receivableList);
        checkoutReservationsTable.setItems(checkoutList);

        // ---- Filters ----
        waitingStatusFilter.setItems(FXCollections.observableArrayList("All", "WAITING", "NOTIFIED", "CANCELLED"));
        waitingStatusFilter.setValue("All");
        waitingStatusFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyWaitingFilter());

        reservationStatusFilter.setItems(FXCollections.observableArrayList("All", "ACTIVE", "WAITING", "SEATED", "CANCELLED", "COMPLETED"));
        reservationStatusFilter.setValue("All");
        reservationStatusFilter.getSelectionModel().selectedItemProperty().addListener((o, a, b) -> applyReservationsFilter());

        // ---- Spinners & time combos ----
        spMakeResGuests.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));
        spWaitlistGuests.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 20, 2));

        cbMakeResHour.setItems(FXCollections.observableArrayList(
                "08","09","10","11","12","13","14","15","16","17","18","19","20","21","22","23"));
        cbMakeResMinute.setItems(FXCollections.observableArrayList("00","15","30","45"));
        cbMakeResHour.setValue("19");
        cbMakeResMinute.setValue("00");

        // ---- Columns mapping (adjust getter names if yours differ) ----
        setupReservationColumns(waitingTable, colWaitId, colWaitGuests, colWaitStatus, colWaitTime);
        colWaitCustomer.setCellValueFactory(c -> new SimpleStringProperty(safeCustomerDisplay(c.getValue())));
        colWaitNotes.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getNotes", "")));

        setupReservationColumns(reservationTable, colResId, colResGuests, colResStatus, colResDateTime);
        colResConf.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getConfirmationCode", "")));
        colResCustomerId.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getCustomerId", "")));
        colResTableId.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getTableNumber", "")));
        colResCreated.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getCreatedAt", "")));

        // diners columns (server returns Customer list, so we read extra fields safely)
        colDinerCustomer.setCellValueFactory(c -> new SimpleStringProperty(
                safeStr(c.getValue(), "getFullName", "Unknown")));
        colDinerResId.setCellValueFactory(c -> new SimpleStringProperty(
                safeStr(c.getValue(), "getCurrentReservationId", "")));
        colDinerGuests.setCellValueFactory(c -> new SimpleStringProperty(
                safeStr(c.getValue(), "getCurrentGuests", "")));
        colDinerTableId.setCellValueFactory(c -> new SimpleStringProperty(
                safeStr(c.getValue(), "getCurrentTableId", "")));
        colDinerStatus.setCellValueFactory(c -> new SimpleStringProperty(
                safeStr(c.getValue(), "getCurrentStatus", "")));
        colDinerSince.setCellValueFactory(c -> new SimpleStringProperty(
                safeStr(c.getValue(), "getDiningSince", "")));

        // tables columns
        colTableId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTableNumber())));
        colCapacity.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getSeats())));

        // click on raw table to select
        tablesTable.setRowFactory(tv -> {
            TableRow<Table> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty() && e.getButton() == MouseButton.PRIMARY) {
                    selectTable(row.getItem());
                }
            });
            return row;
        });

        // members columns
        colCustId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCustomerId())));
        colCustName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colCustPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colCustEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colCustSubscribed.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().isSubscriber())));
        colCustCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubscriptionCode()));

        // found customers columns
        colFoundCustId.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getCustomerId())));
        colFoundCustName.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getFullName()));
        colFoundCustPhone.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getPhone()));
        colFoundCustEmail.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEmail()));
        colFoundCustCode.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getSubscriptionCode()));

        // cancel/receive/checkout reservation tables
        setupSmallReservationTable(cancelReservationsTable, colCancelResId, colCancelDateTime, colCancelGuests, colCancelStatus, colCancelConf);
        setupSmallReservationTable(receiveReservationsTable, colReceiveResId, colReceiveDateTime, colReceiveGuests, colReceiveStatus, colReceiveConf);
        setupSmallReservationTable(checkoutReservationsTable, colCheckoutResId, colCheckoutDateTime, colCheckoutGuests, colCheckoutStatus, colCheckoutConf);

        // hours columns (adjust getter names if needed)
        colDay.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getDayOfWeek", "")));
        colOpen.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getOpenTime", "")));
        colClose.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getCloseTime", "")));
        colClosed.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "isClosed", "")));

        // init bill area
        taBillDetails.setText("");
        btnPayLoadedBill.setDisable(true);

        // optionally auto load everything once
    }

    // ---------- Helpers for reservation columns ----------
    private void setupReservationColumns(TableView<Reservation> tv,
                                         TableColumn<Reservation, String> colId,
                                         TableColumn<Reservation, String> colGuests,
                                         TableColumn<Reservation, String> colStatus,
                                         TableColumn<Reservation, String> colDtOrTime) {

        colId.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getReservationId", "")));
        colGuests.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getNumberOfGuests", "")));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getStatus", "")));

        // this column is used sometimes as DateTime or RequestTime
        colDtOrTime.setCellValueFactory(c -> {
            String dt = safeStr(c.getValue(), "getReservationDateTime", "");
            if (dt == null || dt.isBlank()) dt = safeStr(c.getValue(), "getCreatedAt", "");
            return new SimpleStringProperty(dt);
        });
    }

    private void setupSmallReservationTable(TableView<Reservation> tv,
                                            TableColumn<Reservation, String> colId,
                                            TableColumn<Reservation, String> colDateTime,
                                            TableColumn<Reservation, String> colGuests,
                                            TableColumn<Reservation, String> colStatus,
                                            TableColumn<Reservation, String> colConf) {
        colId.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getReservationId", "")));
        colDateTime.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getReservationDateTime", "")));
        colGuests.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getNumberOfGuests", "")));
        colStatus.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getStatus", "")));
        colConf.setCellValueFactory(c -> new SimpleStringProperty(safeStr(c.getValue(), "getConfirmationCode", "")));
    }



    // ====== REFRESH ALL ======
    private void refreshAll() {
        refreshWaitingList();
        refreshReservations();
        refreshDinersNow();
        refreshTables();
        refreshMembers();
        loadHours();
    }

    // ====== WAITLIST ======
    private void refreshWaitingList() {
        // If you already have a factory for GET_WAITLIST use it.
        // Otherwise add it (example at bottom).
    		client.WaitingListRequest();
    }

    private void applyWaitingFilter() {
        // simplest: re-request from server (server-side filtering can be added later)
        refreshWaitingList();
    }

    // ====== RESERVATIONS ======
    private void refreshReservations() {
    	client.getAllReservationsRequest();
    }

    private void applyReservationsFilter() {
        refreshReservations();
    }

    private void searchReservationsLocal() {
        // optional: local filter (you can also implement server-side search later)
        String q = reservationSearchField.getText();
        if (q == null || q.isBlank()) return;

        // basic local filter on already loaded list
        ObservableList<Reservation> filtered = reservationsList.filtered(r ->
                safeStr(r, "getReservationId", "").contains(q) ||
                safeStr(r, "getConfirmationCode", "").contains(q) ||
                safeStr(r, "getCustomerId", "").contains(q)
        );
        reservationTable.setItems(filtered);
    }

    // ====== DINERS NOW ======
    private void refreshDinersNow() {
        lblDinersNowStatus.setText("Loading...");
        client.getCurrentDinersRequest();
    }

    // ====== TABLES ======
    private void refreshTables() {
    		client.getAllTabelsRequest();
    }

    private void addTable() {
        int cap = parsePositiveInt(tableCapacityField.getText());
        if (cap <= 0) {
            lblTablesMsg.setText("Enter valid capacity.");
            return;
        }
        client.AddTableRequest(cap);
    }

    private void updateSelectedTable() {
        if (selectedTable == null) {
            lblTablesMsg.setText("Select a table first.");
            return;
        }
        int cap = parsePositiveInt(tableCapacityField.getText());
        if (cap <= 0) {
            lblTablesMsg.setText("Enter valid capacity.");
            return;
        }

        client.UpdateTableRequest(selectedTable.getTableNumber(),cap);
    }

    private void deleteSelectedTable() {
        if (selectedTable == null) {
            lblTablesMsg.setText("Select a table first.");
            return;
        }

        client.DeleteTableRequest(selectedTable.getTableNumber());
    }

    private void selectTable(Table t) {
        selectedTable = t;
        lblSelectedTableId.setText(String.valueOf(t.getTableNumber()));
        btnUpdateTableCapacity.setDisable(false);
        btnDeleteTable.setDisable(false);
        tableCapacityField.setText(String.valueOf(t.getSeats()));
    }

    private void renderTablesGraph(List<Table> tables) {
        tablesGraphPane.getChildren().clear();

        for (Table t : tables) {
            VBox card = new VBox(6);
            card.setStyle("""
                -fx-background-color: white;
                -fx-background-radius: 14;
                -fx-padding: 10;
                -fx-border-color: #e6e6e6;
                -fx-border-radius: 14;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.08), 10, 0.2, 0, 3);
            """);

            Label title = new Label("Table #" + t.getTableNumber());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 14;");

            Label cap = new Label("Capacity: " + t.getSeats());
            cap.setStyle("-fx-text-fill: #444444;");

            card.getChildren().addAll(title, cap);

            card.setOnMouseClicked(e -> selectTable(t));
            tablesGraphPane.getChildren().add(card);
        }
    }

    // ====== HOURS ======
    private void loadHours() {
       client.LoadOpeningHoursRequest();
    }

    private void saveHours() {
        if (hoursTable.getItems() == null) return;

        for (OpeningHours h : hoursTable.getItems()) {

            DayOfWeek day = parseDayOfWeekFlexible(safeStr(h, "getDayOfWeek", ""));
            boolean closed = Boolean.parseBoolean(safeStr(h, "isClosed", "false"));

            LocalTime open  = parseLocalTimeFlexible(safeStr(h, "getOpenTime", ""));
            LocalTime close = parseLocalTimeFlexible(safeStr(h, "getCloseTime", ""));

            if (day == null) {
                System.out.println("Invalid dayOfWeek in row: " + safeStr(h, "getDayOfWeek", ""));
                continue;
            }

            // if closed, avoid null times
            if (closed) {
                open = LocalTime.MIDNIGHT;
                close = LocalTime.MIDNIGHT;
            }

            client.updateOpeningHours(day, open, close, closed);
        }
    }
    private DayOfWeek parseDayOfWeekFlexible(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        // if numeric "1".."7"
        if (s.matches("\\d+")) {
            int v = Integer.parseInt(s);
            if (v >= 1 && v <= 7) return DayOfWeek.of(v);
        }

        // common forms: "Monday", "MONDAY", "Mon"
        String up = s.toUpperCase();

        // convert "MON" -> "MONDAY"
        if (up.length() == 3) {
            switch (up) {
                case "MON": return DayOfWeek.MONDAY;
                case "TUE": return DayOfWeek.TUESDAY;
                case "WED": return DayOfWeek.WEDNESDAY;
                case "THU": return DayOfWeek.THURSDAY;
                case "FRI": return DayOfWeek.FRIDAY;
                case "SAT": return DayOfWeek.SATURDAY;
                case "SUN": return DayOfWeek.SUNDAY;
            }
        }

        // exact enum names: "MONDAY"...
        try {
            return DayOfWeek.valueOf(up);
        } catch (Exception ignored) {
            return null;
        }
    }
    private LocalTime parseLocalTimeFlexible(String s) {
        if (s == null) return null;
        s = s.trim();
        if (s.isEmpty()) return null;

        try {
            return LocalTime.parse(s); 
        } catch (Exception ignored) {}

        try {
            return LocalTime.parse(s, DateTimeFormatter.ofPattern("H:mm"));
        } catch (Exception ignored) {}

        try {
            if (s.length() >= 5) return LocalTime.parse(s.substring(0, 5)); // 08:30
        } catch (Exception ignored) {}

        return null;
    }



    // ====== MEMBERS ======
    private void refreshMembers() {
    		client.GetAllSubscribersRequest();
    }

    private void searchMembersLocal() {
        String q = memberSearchField.getText();
        if (q == null || q.isBlank()) return;

        ObservableList<Customer> filtered = membersList.filtered(c ->
                String.valueOf(c.getCustomerId()).contains(q) ||
                safe(c.getFullName()).toLowerCase().contains(q.toLowerCase()) ||
                safe(c.getEmail()).toLowerCase().contains(q.toLowerCase())
        );
        membersTable.setItems(filtered);
    }

    // ====== FIND CUSTOMER (ON BEHALF) ======
    private void findCustomer() {
        String value = tfFindCustomerValue.getText();
        if (value == null || value.isBlank()) {
            lblFindCustomerStatus.setText("Enter a value.");
            return;
        }

        lblFindCustomerStatus.setText("Searching...");


        if (rbFindByCode.isSelected()) {
            client.lookupBySubscriptionCodeRequest(value);
        } else if (rbFindByPhone.isSelected()) {
            client.lookupByPhoneRequest(value);
        } else {
            client.lookupByEmailRequest(value);
        }

    }

    private void clearCustomerSearch() {
        foundCustomersList.clear();
        tfFindCustomerValue.clear();
        lblFindCustomerStatus.setText("");
    }

    private void useSelectedCustomer() {
        Customer c = customerSearchResultsTable.getSelectionModel().getSelectedItem();
        if (c == null) {
            lblOnBehalfGlobalStatus.setText("Select a customer from results.");
            return;
        }
        selectedCustomer = c;
        lblSelectedCustomer.setText(c.getFullName() + " (ID " + c.getCustomerId() + ")");
        lblSelectedCustomerIdHidden.setText(String.valueOf(c.getCustomerId()));
        lblOnBehalfGlobalStatus.setText("Customer selected.");
    }

    private void unselectCustomer() {
        selectedCustomer = null;
        lblSelectedCustomer.setText("None");
        lblSelectedCustomerIdHidden.setText("0");
        lblOnBehalfGlobalStatus.setText("");
    }

    // ====== ON BEHALF: MAKE RESERVATION ======
    private void makeReservationOnBehalf() {
        if (cbMakeResGuestMode.isSelected()) {
            lblMakeReservationStatus.setText("Guest mode: TODO (needs CREATE_GUEST_RESERVATION request).");
            return;
        }

        if (selectedCustomer == null) {
            lblMakeReservationStatus.setText("Select a customer first.");
            return;
        }

        LocalDate date = dpMakeResDate.getValue();
        if (date == null) {
            lblMakeReservationStatus.setText("Pick a date.");
            return;
        }

        int guests = spMakeResGuests.getValue();
        LocalTime time = LocalTime.of(Integer.parseInt(cbMakeResHour.getValue()),
                                      Integer.parseInt(cbMakeResMinute.getValue()));
        LocalDateTime dt = LocalDateTime.of(date, time);

        client.createCreateReservationOnBehalfRequest(
                selectedCustomer.getCustomerId(), dt, guests);

        lblMakeReservationStatus.setText("Sending...");
    }

    // ====== ON BEHALF: JOIN WAITLIST ======
    private void joinWaitlistOnBehalf() {
        if (cbWaitlistGuestMode.isSelected()) {
            lblJoinWaitlistStatus.setText("Guest mode: TODO (needs JOIN_WAITLIST guest request).");
            return;
        }

        if (selectedCustomer == null) {
            lblJoinWaitlistStatus.setText("Select a customer first.");
            return;
        }

        int guests = spWaitlistGuests.getValue();
        client.createJoinWaitlistOnBehalfRequest(
                selectedCustomer.getCustomerId(), guests);

        lblJoinWaitlistStatus.setText("Sending...");
    }

    // ====== ON BEHALF: CANCEL ======
    private void loadCancelableReservations() {
        if (selectedCustomer == null) {
            lblCancelLoadStatus.setText("Select a customer first.");
            return;
        }
        client.createGetCancellableReservationsOnBehalfRequest(
                selectedCustomer.getCustomerId());
        lblCancelLoadStatus.setText("Loading...");
    }

    private void cancelSelectedReservation() {
        Reservation r = cancelReservationsTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null || r == null) {
            lblCancelStatus.setText("Select customer + reservation.");
            return;
        }
        client.createCancelReservationOnBehalfRequest(
                selectedCustomer.getCustomerId(), r.getReservationId());
        lblCancelStatus.setText("Sending...");
    }

    // ====== ON BEHALF: RECEIVE TABLE ======
    private void loadReceivableReservations() {
        if (selectedCustomer == null) {
            lblReceiveLoadStatus.setText("Select a customer first.");
            return;
        }
        client.createGetReceivableReservationsOnBehalfRequest(
                selectedCustomer.getCustomerId());
        lblReceiveLoadStatus.setText("Loading...");
    }

    private void receiveTableForSelected() {
        Reservation r = receiveReservationsTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null || r == null) {
            lblReceiveStatus.setText("Select customer + reservation.");
            return;
        }
        client.createReceiveTableOnBehalfRequest(
                selectedCustomer.getCustomerId(), r.getReservationId());
        lblReceiveStatus.setText("Sending...");
    }

    // ====== ON BEHALF: CHECKOUT ======
    private void loadCheckoutReservations() {
        if (selectedCustomer == null) {
            lblCheckoutLoadStatus.setText("Select a customer first.");
            return;
        }
        client.createGetPayableReservationsOnBehalfRequest(
                selectedCustomer.getCustomerId());
        lblCheckoutLoadStatus.setText("Loading...");
    }

    private void loadBillForSelected() {
        Reservation r = checkoutReservationsTable.getSelectionModel().getSelectedItem();
        if (selectedCustomer == null || r == null) {
            lblCheckoutStatus.setText("Select customer + reservation.");
            return;
        }
        client.createGetBillForPayingOnBehalfRequest(
                selectedCustomer.getCustomerId(), r.getReservationId());
        lblCheckoutStatus.setText("Loading bill...");
    }

    private void payLoadedBill() {
        if (selectedCustomer == null) {
            lblCheckoutStatus.setText("Select customer first.");
            return;
        }
        if (loadedBillId == null) {
            lblCheckoutStatus.setText("Load a bill first.");
            return;
        }

        client.createPayBillOnBehalfRequest(
                selectedCustomer.getCustomerId(), loadedBillId);

        lblCheckoutStatus.setText("Paying...");
    }

    // ================== SERVER MESSAGE ENTRY ==================
    public void handleServerMessage(Object msg) {
        // Always jump to FX thread
        Platform.runLater(() -> {
            try {
                if (msg instanceof ReservationResponse) {
                    handleReservationResponse((ReservationResponse) msg);
                } else if (msg instanceof UserAccountResponse) {
                    handleUserAccountResponse((UserAccountResponse) msg);
                } else if (msg instanceof RestaurantManagementResponse) {
                    handleRestaurantManagementResponse((RestaurantManagementResponse) msg);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    private void handleReservationResponse(ReservationResponse resp) {
        ReservationOperation op = resp.getOperation();

        if (!resp.isSuccess()) {
            // show in a relevant place
            if (op == ReservationOperation.GET_WAITLIST) {
                // maybe show status somewhere if you add a label later
            }
            return;
        }

        switch (op) {
            case GET_WAITLIST:
                waitingList.setAll(resp.getReservations());
                applyWaitingFilter();
                break;

            case GET_ACTIVE_RESERVATIONS:
                reservationsList.setAll(resp.getReservations());
                reservationTable.setItems(reservationsList);
                applyReservationsFilter();
                break;

            case GET_CUSTOMER_RESERVATIONS_FOR_CANCELLATION:
                cancelableList.setAll(resp.getReservations());
                lblCancelLoadStatus.setText(resp.getMessage());
                break;

            case GET_CUSTOMER_RESERVATIONS_FOR_RECEIVING:
                receivableList.setAll(resp.getReservations());
                lblReceiveLoadStatus.setText(resp.getMessage());
                break;

            case GET_CUSTOMER_RESERVATIONS_FOR_CHECKOUT:
                checkoutList.setAll(resp.getReservations());
                lblCheckoutLoadStatus.setText(resp.getMessage());
                break;

            case GET_BILL_FOR_PAYING:
                // adapt based on your ReservationResponse structure
                loadedBillId = resp.getBill().getBillId(); // <-- if you have it
                taBillDetails.setText(resp.getBill().toString()); // <-- if you have it
                btnPayLoadedBill.setDisable(false);
                lblCheckoutStatus.setText("Bill loaded.");
                break;

            case PAY_BILL:
                lblCheckoutStatus.setText(resp.getMessage());
                loadedBillId = null;
                btnPayLoadedBill.setDisable(true);
                taBillDetails.clear();
                // refresh checkout list
                loadCheckoutReservations();
                break;

            case CREATE_RESERVATION:
                lblMakeReservationStatus.setText(resp.getMessage());
                refreshReservations();
                break;

            case JOIN_WAITLIST:
                lblJoinWaitlistStatus.setText(resp.getMessage());
                refreshWaitingList();
                break;

            case CANCEL_RESERVATION:
                lblCancelStatus.setText(resp.getMessage());
                loadCancelableReservations();
                refreshReservations();
                break;

            case RECEIVE_TABLE:
                lblReceiveStatus.setText(resp.getMessage());
                loadReceivableReservations();
                refreshReservations();
                refreshDinersNow();
                break;

            default:
                // ignore or log
                break;
        }
    }

    private void handleUserAccountResponse(UserAccountResponse resp) {
        UserAccountOperation op = resp.getOperation();

        if (!resp.isSuccess()) {
            if (op == UserAccountOperation.GET_CURRENT_DINERS) {
                lblDinersNowStatus.setText(resp.getMessage());
            } else if (op == UserAccountOperation.GET_ALL_SUBSCRIBERS) {
                // can show message somewhere if needed
            } else {
                lblFindCustomerStatus.setText(resp.getMessage());
            }
            return;
        }

        switch (op) {
            case GET_CURRENT_DINERS:
                dinersList.setAll(resp.getCustomers());
                lblDinersNowStatus.setText("Loaded: " + dinersList.size());
                break;

            case GET_ALL_SUBSCRIBERS:
                membersList.setAll(resp.getCustomers());
                membersTable.setItems(membersList);
                break;

            case LOOKUP_CUSTOMER_BY_SUBSCRIPTION_CODE:
            case LOOKUP_CUSTOMER_BY_PHONE:
            case LOOKUP_CUSTOMER_BY_EMAIL:
                foundCustomersList.clear();
                if (resp.getCustomer() != null) {
                    foundCustomersList.add(resp.getCustomer());
                    lblFindCustomerStatus.setText("Customer found.");
                } else {
                    lblFindCustomerStatus.setText("No customer.");
                }
                break;

            default:
                break;
        }
    }

    private void handleRestaurantManagementResponse(RestaurantManagementResponse resp) {
        if (!resp.isSuccess()) {
            lblTablesMsg.setText(resp.getMessage());
            return;
        }

        RestaurantManagementOperation op = resp.getOperation();
        switch (op) {
            case GET_ALL_TABLES:
            case ADD_TABLE:
            case UPDATE_TABLE:
            case DELETE_TABLE:
                tablesList.setAll(resp.getTables());
                renderTablesGraph(resp.getTables());
                lblTablesMsg.setText(resp.getMessage());
                break;

            case GET_OPENING_HOURS:
            case UPDATE_OPENING_HOURS:
                hoursTable.getItems().setAll(resp.getOpeningHours());
                break;

            default:
                break;
        }
    }

    // ================== small utilities ==================
    private int parsePositiveInt(String s) {
        try {
            int x = Integer.parseInt(s.trim());
            return x > 0 ? x : -1;
        } catch (Exception e) {
            return -1;
        }
    }

    private String safe(String s) { return s == null ? "" : s; }

    private void showInfo(String title, String msg) {
        Alert a = new Alert(Alert.AlertType.INFORMATION);
        a.setTitle(title);
        a.setHeaderText(null);
        a.setContentText(msg);
        a.showAndWait();
    }

    private String safeCustomerDisplay(Reservation r) {
        // Try to display name if exists, otherwise customerId
        String name = safeStr(r, "getCustomerName", "");
        if (name != null && !name.isBlank()) return name;
        String cid = safeStr(r, "getCustomerId", "");
        return (cid == null || cid.isBlank()) ? "Guest" : ("Customer #" + cid);
    }

    /**
     * safeStr(obj,"getX","fallback") uses reflection so controller doesn't crash
     * if your entity uses slightly different getter names.
     */
    private String safeStr(Object obj, String getter, String fallback) {
        if (obj == null) return fallback;
        try {
            Object val = obj.getClass().getMethod(getter).invoke(obj);
            return val == null ? fallback : String.valueOf(val);
        } catch (Exception e) {
            return fallback;
        }
    }
}
