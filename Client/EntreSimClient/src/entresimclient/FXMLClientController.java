/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient;

import entresimclient.Objects.Server;
import entresimclient.Runnables.Searcher;

import java.io.IOException;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;

import java.util.ResourceBundle;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

/**
 *
 * @author dylanleong
 */
public class FXMLClientController implements Initializable {

    @FXML
    private TableView<Server> listServers;
    @FXML
    private TextField txtManual;
    @FXML
    private Button btnSearch;
    @FXML
    private TableColumn<Server, String> serverNameCol;
    @FXML
    private TableColumn<Server, String> serverAddressCol;
    @FXML
    private Label labelSearch;
    @FXML
    private Button btnManual;
    @FXML
    private Button btnConnect;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
    }

    @FXML
    private void handlebtnManual(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText("Please input a server.");
        alert.initModality(Modality.APPLICATION_MODAL);

        String address = txtManual.getText();

        if (address.trim().equals("")) { //if no name inputted
            alert.setContentText("This button is for manual joins only.  To join a listed server, use the button at the bottom left.\n"
                    + "To manual join, please enter an address in the textbox to the left of this join button.");
            alert.showAndWait();
        } else {
            joinServer(address);
        }
    }

    @FXML
    private void handlebtnConnect(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText("Please select a server.");
        alert.initModality(Modality.APPLICATION_MODAL);

        Server server = listServers.getSelectionModel().getSelectedItem();

        if (server != null) {
            String address = server.getAddress();
            joinServer(address);
        } else {
            alert.setContentText("Please select a server first, then click the connect button.");
            alert.showAndWait();
        }

    }

    @FXML
    private void handlebtnSearch(ActionEvent event) {
        try {
            Callable<ObservableList<Server>> searcher = new Searcher();
            ExecutorService searcher_service = Executors.newSingleThreadExecutor();
            Future<ObservableList<Server>> searcher_future = searcher_service.submit(searcher);
            ObservableList<Server> address_list = searcher_future.get();

            if (address_list.isEmpty()) {
                labelSearch.setText("No Servers Found.");
            } else {
                labelSearch.setText("Searching finished!");
            }

            listServers.setItems(address_list);
            serverNameCol.setCellValueFactory(new PropertyValueFactory("serverName"));
            serverAddressCol.setCellValueFactory(new PropertyValueFactory("ipAddress"));
        } catch (InterruptedException | ExecutionException ex) {
            Logger.getLogger(FXMLClientController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * When called, attempts to join server and open the game window. I know
     * it's horrible to look at but bear with me. It's all try/catch and error
     * alerts.
     *
     * @param ip is the IP Address of the server
     */
    private void joinServer(String ip) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText("Could not connect to server.");
        alert.initModality(Modality.APPLICATION_MODAL);

        Socket client = null;
        boolean success = false;
        try { //Attempt to connect socket
            client = new Socket(); //In the future, add a heartbeat
            client.connect(new InetSocketAddress(ip, 6067), 20000);
            if (client.isClosed()) {
                alert.setContentText("The game has already started, so you cannot join this room.");
                alert.showAndWait();
            } else {
                success = true;

                try { //Open new window
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLGame.fxml"));
                    Stage stage = new Stage();
                    stage.initModality(Modality.APPLICATION_MODAL);
                    stage.setScene(new Scene((Pane) loader.load()));
                    stage.setResizable(false);
                    
                    stage.setOnCloseRequest((event) -> System.exit(1)); //Temporary
                    
                    FXMLGameController controller = loader.<FXMLGameController>getController();
                    controller.init(client); //pass socket to new FXML

                    stage.show();
                    controller.chooseName();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLClientController.class.getName()).log(Level.SEVERE, "Failed to open client window.", ex);
                }
            }
        } catch (UnknownHostException ex) {
            alert.setContentText("Please input a valid IP address and try again.\n"
                    + "A valid IP address will be four numbers separated by a full stop.");
            alert.showAndWait();
        } catch (ConnectException ex) {
            alert.setContentText("Either the server does not exist, has been closed, or the game has already started.");
            alert.showAndWait();
        } catch (SocketTimeoutException ex) {
            alert.setContentText("Took too long to connect to server.  Please doublecheck if you are connected to a network and that the server is still open.");
            alert.showAndWait();
        } catch (IOException ex) {
            Logger.getLogger(FXMLClientController.class.getName()).log(Level.SEVERE, "Failed to connect.", ex);
       // } catch (InterruptedException ex) {
         //   Logger.getLogger(FXMLClientController.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (client != null && !success) {
                try {
                    client.close();
                } catch (IOException ex) {
                    Logger.getLogger(FXMLClientController.class.getName()).log(Level.SEVERE, "Failed to close socket.", ex);
                }
            }
        }//finally
    }

}
