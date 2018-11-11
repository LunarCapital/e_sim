/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Objects;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dylanleong
 */
public class Server {
    
    private StringProperty ipAddress;
    public void setAddress(String value) {ipAddressProperty().set(value);}
    public String getAddress() {return ipAddressProperty().get();}
    public StringProperty ipAddressProperty() {
        if (ipAddress == null) ipAddress = new SimpleStringProperty(this, "ipAddress");
        return ipAddress;
    }
    
    private StringProperty serverName;
    public void setServerName(String value) {serverNameProperty().set(value);}
    public String getServerName() {return serverNameProperty().get();}
    public StringProperty serverNameProperty() {
        if (serverName == null) serverName = new SimpleStringProperty(this, "serverName");
        return serverName;
    }
    
}
