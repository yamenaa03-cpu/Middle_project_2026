package server;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

public class ClientStatusInfo {

    private final StringProperty id;
    private final StringProperty host;
    private final StringProperty ip;
    private final StringProperty status;

    public ClientStatusInfo(String id, String host, String ip, String status) {
        this.id = new SimpleStringProperty(id);
        this.host = new SimpleStringProperty(host);
        this.ip = new SimpleStringProperty(ip);
        this.status = new SimpleStringProperty(status);
    }

    public String getId() { return id.get(); }
    public StringProperty idProperty() { return id; }

    public String getHost() { return host.get(); }
    public StringProperty hostProperty() { return host; }

    public String getIp() { return ip.get(); }
    public StringProperty ipProperty() { return ip; }

    public String getStatus() { return status.get(); }
    public StringProperty statusProperty() { return status; }

    public void setStatus(String value) { status.set(value); }
}
