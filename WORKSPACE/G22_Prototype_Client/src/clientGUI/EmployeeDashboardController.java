package clientGUI;

import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

import java.net.URL;
import java.util.ResourceBundle;

public class EmployeeDashboardController implements Initializable {

    // ================== TOP BAR ==================
    @FXML private Button btnRefreshAll;

    // ================== WAITING LIST TAB ==================
    @FXML private Button btnRefreshWaitingList;
    @FXML private ChoiceBox<?> waitingStatusFilter;

    @FXML private TableView<?> waitingTable;
    @FXML private TableColumn<?, ?> colWaitId;
    @FXML private TableColumn<?, ?> colWaitCustomer;
    @FXML private TableColumn<?, ?> colWaitGuests;
    @FXML private TableColumn<?, ?> colWaitTime;
    @FXML private TableColumn<?, ?> colWaitStatus;
    @FXML private TableColumn<?, ?> colWaitNotes;

    // ================== RESERVATIONS TAB ==================
    @FXML private TextField reservationSearchField;
    @FXML private Button btnSearchReservation;
    @FXML private ChoiceBox<?> reservationStatusFilter;
    @FXML private Button btnRefreshReservations;

    @FXML private TableView<?> reservationTable;
    @FXML private TableColumn<?, ?> colResId;
    @FXML private TableColumn<?, ?> colResDateTime;
    @FXML private TableColumn<?, ?> colResGuests;
    @FXML private TableColumn<?, ?> colResConf;
    @FXML private TableColumn<?, ?> colResCustomerId;
    @FXML private TableColumn<?, ?> colResTableId;
    @FXML private TableColumn<?, ?> colResStatus;
    @FXML private TableColumn<?, ?> colResCreated;

    // ================== DINERS NOW TAB ==================
    @FXML private Button btnRefreshDinersNow;
    @FXML private Label lblDinersNowStatus;

    @FXML private TableView<?> dinersNowTable;
    @FXML private TableColumn<?, ?> colDinerResId;
    @FXML private TableColumn<?, ?> colDinerCustomer;
    @FXML private TableColumn<?, ?> colDinerGuests;
    @FXML private TableColumn<?, ?> colDinerTableId;
    @FXML private TableColumn<?, ?> colDinerStatus;
    @FXML private TableColumn<?, ?> colDinerSince;

    // ================== TABLES TAB ==================
    @FXML private Button btnRefreshTables;
    @FXML private TextField tableCapacityField;
    @FXML private Button btnUpdateTableCapacity;

    @FXML private TableView<?> tablesTable;
    @FXML private TableColumn<?, ?> colTableId;
    @FXML private TableColumn<?, ?> colCapacity;

    // ================== WORKING HOURS TAB ==================
    @FXML private Button btnLoadHours;
    @FXML private Button btnSaveHours;

    @FXML private TableView<?> hoursTable;
    @FXML private TableColumn<?, ?> colDay;
    @FXML private TableColumn<?, ?> colOpen;
    @FXML private TableColumn<?, ?> colClose;
    @FXML private TableColumn<?, ?> colClosed;

    // ================== MEMBERS TAB ==================
    @FXML private TextField memberSearchField;
    @FXML private Button btnSearchMember;
    @FXML private Button btnRefreshMembers;

    @FXML private TableView<?> membersTable;
    @FXML private TableColumn<?, ?> colCustId;
    @FXML private TableColumn<?, ?> colCustName;
    @FXML private TableColumn<?, ?> colCustPhone;
    @FXML private TableColumn<?, ?> colCustEmail;
    @FXML private TableColumn<?, ?> colCustSubscribed;
    @FXML private TableColumn<?, ?> colCustCode;

    // ================== CUSTOMER OPERATIONS (ON BEHALF) TAB ==================
    @FXML private RadioButton rbFindByCode;
    @FXML private RadioButton rbFindByPhone;
    @FXML private RadioButton rbFindByEmail;

    @FXML private TextField tfFindCustomerValue;
    @FXML private Button btnFindCustomer;
    @FXML private Button btnClearCustomerSearch;
    @FXML private Label lblFindCustomerStatus;

    @FXML private TableView<?> customerSearchResultsTable;
    @FXML private TableColumn<?, ?> colFoundCustId;
    @FXML private TableColumn<?, ?> colFoundCustName;
    @FXML private TableColumn<?, ?> colFoundCustPhone;
    @FXML private TableColumn<?, ?> colFoundCustEmail;
    @FXML private TableColumn<?, ?> colFoundCustCode;

    @FXML private Button btnSelectCustomerFromResults;
    @FXML private Button btnUnselectCustomer;
    @FXML private Label lblSelectedCustomer;
    @FXML private Label lblOnBehalfGlobalStatus;

    // hidden label holds selected customer id
    @FXML private Label lblSelectedCustomerIdHidden;

    // ----- Make Reservation section -----
    @FXML private DatePicker dpMakeResDate;
    @FXML private ComboBox<?> cbMakeResHour;
    @FXML private ComboBox<?> cbMakeResMinute;
    @FXML private Spinner<?> spMakeResGuests;

    @FXML private CheckBox cbMakeResGuestMode;
    @FXML private TextField tfMakeResGuestName;
    @FXML private TextField tfMakeResGuestPhone;
    @FXML private TextField tfMakeResGuestEmail;

    @FXML private Button btnMakeReservationOnBehalf;
    @FXML private Label lblMakeReservationStatus;

    // ----- Join Waitlist section -----
    @FXML private Spinner<?> spWaitlistGuests;

    @FXML private CheckBox cbWaitlistGuestMode;
    @FXML private TextField tfWaitlistGuestName;
    @FXML private TextField tfWaitlistGuestPhone;
    @FXML private TextField tfWaitlistGuestEmail;

    @FXML private Button btnJoinWaitlistOnBehalf;
    @FXML private Label lblJoinWaitlistStatus;

    // ----- Cancel Reservation section -----
    @FXML private Button btnLoadCancelableReservations;
    @FXML private Label lblCancelLoadStatus;

    @FXML private TableView<?> cancelReservationsTable;
    @FXML private TableColumn<?, ?> colCancelResId;
    @FXML private TableColumn<?, ?> colCancelDateTime;
    @FXML private TableColumn<?, ?> colCancelGuests;
    @FXML private TableColumn<?, ?> colCancelStatus;
    @FXML private TableColumn<?, ?> colCancelConf;

    @FXML private Button btnCancelSelectedReservation;
    @FXML private Label lblCancelStatus;

    // ----- Receive Table section -----
    @FXML private Button btnLoadReceivableReservations;
    @FXML private Label lblReceiveLoadStatus;

    @FXML private TableView<?> receiveReservationsTable;
    @FXML private TableColumn<?, ?> colReceiveResId;
    @FXML private TableColumn<?, ?> colReceiveDateTime;
    @FXML private TableColumn<?, ?> colReceiveGuests;
    @FXML private TableColumn<?, ?> colReceiveStatus;
    @FXML private TableColumn<?, ?> colReceiveConf;

    @FXML private Button btnReceiveTableForSelected;
    @FXML private Label lblReceiveStatus;

    // ----- Checkout section -----
    @FXML private Button btnLoadCheckoutReservations;
    @FXML private Label lblCheckoutLoadStatus;

    @FXML private TableView<?> checkoutReservationsTable;
    @FXML private TableColumn<?, ?> colCheckoutResId;
    @FXML private TableColumn<?, ?> colCheckoutDateTime;
    @FXML private TableColumn<?, ?> colCheckoutGuests;
    @FXML private TableColumn<?, ?> colCheckoutStatus;
    @FXML private TableColumn<?, ?> colCheckoutConf;

    @FXML private Button btnLoadBillForSelected;
    @FXML private Button btnPayLoadedBill;
    @FXML private Label lblCheckoutStatus;

    @FXML private TextArea taBillDetails;

    // internal toggle group (created in code, not in FXML)
    private final ToggleGroup findCustomerToggleGroup = new ToggleGroup();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // radio buttons group
        rbFindByCode.setToggleGroup(findCustomerToggleGroup);
        rbFindByPhone.setToggleGroup(findCustomerToggleGroup);
        rbFindByEmail.setToggleGroup(findCustomerToggleGroup);
        rbFindByCode.setSelected(true);

        // Hook up button actions (so FXML doesn't need onAction="")
        wireActions();

        // Optional: initial UI state
        // disableOperationsUntilCustomerSelected();
    }

    private void wireActions() {
        if (btnRefreshAll != null) btnRefreshAll.setOnAction(e -> onRefreshAll());

        if (btnRefreshWaitingList != null) btnRefreshWaitingList.setOnAction(e -> onRefreshWaitingList());
        if (btnSearchReservation != null) btnSearchReservation.setOnAction(e -> onSearchReservation());
        if (btnRefreshReservations != null) btnRefreshReservations.setOnAction(e -> onRefreshReservations());

        if (btnRefreshDinersNow != null) btnRefreshDinersNow.setOnAction(e -> onRefreshDinersNow());

        if (btnRefreshTables != null) btnRefreshTables.setOnAction(e -> onRefreshTables());
        if (btnUpdateTableCapacity != null) btnUpdateTableCapacity.setOnAction(e -> onUpdateTableCapacity());

        if (btnLoadHours != null) btnLoadHours.setOnAction(e -> onLoadHours());
        if (btnSaveHours != null) btnSaveHours.setOnAction(e -> onSaveHours());

        if (btnSearchMember != null) btnSearchMember.setOnAction(e -> onSearchMember());
        if (btnRefreshMembers != null) btnRefreshMembers.setOnAction(e -> onRefreshMembers());

        if (btnFindCustomer != null) btnFindCustomer.setOnAction(e -> onFindCustomer());
        if (btnClearCustomerSearch != null) btnClearCustomerSearch.setOnAction(e -> onClearCustomerSearch());
        if (btnSelectCustomerFromResults != null) btnSelectCustomerFromResults.setOnAction(e -> onSelectCustomerFromResults());
        if (btnUnselectCustomer != null) btnUnselectCustomer.setOnAction(e -> onUnselectCustomer());

        if (btnMakeReservationOnBehalf != null) btnMakeReservationOnBehalf.setOnAction(e -> onMakeReservationOnBehalf());
        if (btnJoinWaitlistOnBehalf != null) btnJoinWaitlistOnBehalf.setOnAction(e -> onJoinWaitlistOnBehalf());

        if (btnLoadCancelableReservations != null) btnLoadCancelableReservations.setOnAction(e -> onLoadCancelableReservations());
        if (btnCancelSelectedReservation != null) btnCancelSelectedReservation.setOnAction(e -> onCancelSelectedReservation());

        if (btnLoadReceivableReservations != null) btnLoadReceivableReservations.setOnAction(e -> onLoadReceivableReservations());
        if (btnReceiveTableForSelected != null) btnReceiveTableForSelected.setOnAction(e -> onReceiveTableForSelected());

        if (btnLoadCheckoutReservations != null) btnLoadCheckoutReservations.setOnAction(e -> onLoadCheckoutReservations());
        if (btnLoadBillForSelected != null) btnLoadBillForSelected.setOnAction(e -> onLoadBillForSelected());
        if (btnPayLoadedBill != null) btnPayLoadedBill.setOnAction(e -> onPayLoadedBill());
    }

    // ================== STUB METHODS (EMPTY) ==================

    // Top bar
    private void onRefreshAll() { }

    // Waiting list
    private void onRefreshWaitingList() { }

    // Reservations
    private void onSearchReservation() { }
    private void onRefreshReservations() { }

    // Diners Now
    private void onRefreshDinersNow() { }

    // Tables
    private void onRefreshTables() { }
    private void onUpdateTableCapacity() { }

    // Working Hours
    private void onLoadHours() { }
    private void onSaveHours() { }

    // Members
    private void onSearchMember() { }
    private void onRefreshMembers() { }

    // Customer lookup / selection
    private void onFindCustomer() { }
    private void onClearCustomerSearch() { }
    private void onSelectCustomerFromResults() { }
    private void onUnselectCustomer() { }

    // Operations on behalf
    private void onMakeReservationOnBehalf() { }
    private void onJoinWaitlistOnBehalf() { }

    private void onLoadCancelableReservations() { }
    private void onCancelSelectedReservation() { }

    private void onLoadReceivableReservations() { }
    private void onReceiveTableForSelected() { }

    private void onLoadCheckoutReservations() { }
    private void onLoadBillForSelected() { }
    private void onPayLoadedBill() { }

    // Helper stubs you may want later
    private int getSelectedCustomerId() { return 0; }
    private void setSelectedCustomerId(int customerId) { }
    private void clearSelectedCustomer() { }
    private void updateSelectedCustomerLabel(String text) { }
}
