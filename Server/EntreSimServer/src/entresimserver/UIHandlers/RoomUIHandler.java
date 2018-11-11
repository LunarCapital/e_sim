/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.UIHandlers;

import entreObj.Decision;
import entreObj.Environment;
import entreObj.PlayerDetails;
import entreObj.WinData;
import entresimserver.FXMLServerController;
import entresimserver.Objects.Data.ConditionalData;
import entresimserver.Objects.Data.Switch;
import entresimserver.Objects.Handlers.ConditionalReader;
import entresimserver.Objects.Handlers.DataReader;
import entresimserver.Objects.Handlers.DecisionHandler;
import entresimserver.Objects.Handlers.SwitchReader;
import entresimserver.Runnables.Broadcaster;
import entresimserver.Runnables.ConnectionHandler;
import entresimserver.Runnables.GameController;
import entresimserver.Runnables.ServerInStream;
import entresimserver.Runnables.ServerOutStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.VBox;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author dylanleong
 */
public class RoomUIHandler {

    private final FXMLServerController main_UI;
    public Broadcaster server_broadcaster;
    public ConnectionHandler connection_handler;
    public GameController game_controller;
    public TreeSet<Decision> decisions;
    public ArrayList<Switch> switches;
    public ArrayList<ConditionalData> conditional_data;
    private boolean room_created;

    public RoomUIHandler(FXMLServerController main_UI) {
        room_created = false;
        this.main_UI = main_UI;
    }

    public void createNewRoom(TextField txtServerName, Button btnRoom, Button btnStart, Button btnResume, Button btnGraph, ChoiceBox<Integer> cbRounds, Stage stage) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText("Cannot create server.");
        alert.initModality(Modality.APPLICATION_MODAL);

        if (!room_created) { //create room

            if (txtServerName.getText().trim().equals("")) {
                alert.setContentText("Please enter a server name in the textbox to the right of the create button.");
                alert.showAndWait();
            } else { //Broadcast Address and create room                
                String path = null;
                if ((path = getFilePath(stage)) == null) {
                    return;
                }
                createRoom(btnRoom, btnStart, btnResume, cbRounds, txtServerName, path);
            }
        } else { //room already created, cancel room
            cancelRoom(btnRoom, btnStart, btnResume, btnGraph, cbRounds);
        }
    }

    public void loadSavedRoom(AnchorPane rootPane, VBox vboxPlayers, Button btnRoom, Button btnStart, Button btnResume, Button btnGraph, ChoiceBox<Integer> cbRounds) {
        FileChooser file_chooser = new FileChooser();
        file_chooser.setTitle("Open Save File");
        //find out what filter we use later
        File selected_file = file_chooser.showOpenDialog(rootPane.getScene().getWindow());
        if (selected_file != null) {
            try {
                FileInputStream fis = new FileInputStream(selected_file);
                ArrayList<Object> save = new ArrayList<Object>();
                ObjectInputStream input = new ObjectInputStream(fis);

                save = (ArrayList<Object>) input.readObject();

                for (Object each : save) {
                    System.out.println(each);
                }

                //Open connectionHandler, GameController, etc
                createRoom(save, btnRoom, btnStart, btnResume, cbRounds);

                for (PlayerDetails each_player : ((Environment) save.get(0)).players) {
                    addPlayerToList(each_player.name, vboxPlayers);
                }

            } catch (FileNotFoundException ex) {
                Logger.getLogger(FXMLServerController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(FXMLServerController.class.getName()).log(Level.SEVERE, null, ex);
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(FXMLServerController.class.getName()).log(Level.SEVERE, null, ex);
            }

        }
    }

    public void startRoom(Button btnStart, Button btnGraph) {
        if (!game_controller.loaded) { //new game
            if (game_controller.checkReady()) {
                btnStart.setDisable(true);
                connection_handler.terminate();
                //game_controller.sendDecisions();
                Thread t = new Thread(game_controller, "Game Controller Server-Side"); //start game
                t.setDaemon(true);
                t.start();
            } else {
                main_UI.writeTxtLog("Some players are still choosing names.");
            }
        } else { //loaded game
            if (game_controller.checkReady()) {
                btnStart.setDisable(true);
                connection_handler.terminate();
                //game_controller.sendDecisions();
                Thread t = new Thread(game_controller, "Game Controller Server-Side");
                t.setDaemon(true);
                t.start();
            } else {
                main_UI.writeTxtLog("You have chosen to wait for more students to show up.  Click the start button either when they turn up or if you change your mind.");
            }
        }
        btnGraph.setDisable(false);
    }

    public boolean displayGraph() {
        if (game_controller != null) {
            if (game_controller.env != null) {
                game_controller.calcResourcesAndShowGraph();
                return true;
            } else {
                return false;
            }
        } else {
            return false;
        }
    }

    public void addPlayerToList(String name, VBox vboxPlayers) {
        Label add_player = new Label(name);

        vboxPlayers.getChildren().add(add_player);
    }

    /**
     * Create a room. Use when starting a new game.
     *
     * @param btnRoom
     * @param btnStart
     * @param btnResume
     * @param cbRounds
     * @param txtServerName
     * @param server_broadcaster
     * @param connection_handler
     * @param game_controller
     * @param decisions
     */
    private void createRoom(Button btnRoom, Button btnStart, Button btnResume, ChoiceBox<Integer> cbRounds, TextField txtServerName, String path) {
        try {

            room_created = true;
            btnRoom.setText("Cancel Room");
            btnStart.setDisable(false);
            btnResume.setDisable(true);
            cbRounds.setDisable(true);

            main_UI.writeTxtLog("Broadcasting server to network...");

            decisions = new TreeSet<Decision>(); //Read Decisions
            DataReader dr = new DataReader(decisions);
            dr.readData(path, "wireless_decisions");

            switches = new ArrayList<Switch>(); //Read switches
            SwitchReader sr = new SwitchReader(switches);
            sr.readSwitches(path, "conditionals_switch");

            conditional_data = new ArrayList<ConditionalData>(); //read conditional data
            ConditionalReader cr = new ConditionalReader(conditional_data, decisions);
            cr.readConditionals(path, "conditionals_changes");

            ArrayList<ServerInStream> instreams = new ArrayList<ServerInStream>();
            ArrayList<ServerOutStream> outstreams = new ArrayList<ServerOutStream>();

            //Init Game Controller (But don't start it)
            game_controller = new GameController(outstreams, instreams, main_UI, decisions, switches, conditional_data, txtServerName.getText(), cbRounds.getSelectionModel().getSelectedItem());
            //don't start it yet

            main_UI.writeTxtLog("Our IP is " + InetAddress.getLocalHost().getHostAddress());

            //Start broadcasting
            server_broadcaster = new Broadcaster(txtServerName.getText());
            Thread t1 = new Thread(server_broadcaster, "Server_Broadcaster");
            t1.setDaemon(true);
            t1.start();

            //Accept Connections
            connection_handler = new ConnectionHandler(instreams, outstreams, game_controller, main_UI);
            Thread t2 = new Thread(connection_handler, "Connection_Handler");
            t2.setDaemon(true);
            t2.start();

        } catch (UnknownHostException | SocketException ex) {
            Logger.getLogger(FXMLServerController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Create a room. Use when loading a game.
     *
     * @param loaded_data
     * @param btnRoom
     * @param btnStart
     * @param btnResume
     * @param cbRounds
     * @param server_broadcaster
     * @param connection_handler
     * @param game_controller
     * @param decisions
     */
    private void createRoom(ArrayList<Object> loaded_data, Button btnRoom, Button btnStart, Button btnResume, ChoiceBox<Integer> cbRounds) {
        try {
            room_created = true;
            btnRoom.setText("Cancel Room");
            btnStart.setDisable(false);
            btnResume.setDisable(true);
            cbRounds.setDisable(true);

            Environment env = (Environment) loaded_data.get(0);
            DecisionHandler decision_handler = (DecisionHandler) loaded_data.get(1);
            decisions = (TreeSet<Decision>) loaded_data.get(2);
            switches = (ArrayList<Switch>) loaded_data.get(3);
            conditional_data = (ArrayList<ConditionalData>) loaded_data.get(4);
            TreeSet<WinData> windata = (TreeSet<WinData>) loaded_data.get(5);
            int rounds_max = (int) loaded_data.get(6);
            int current_round = (int) loaded_data.get(7);
            String room_name = (String) loaded_data.get(8);

            ArrayList<ServerInStream> instreams = new ArrayList<ServerInStream>();
            ArrayList<ServerOutStream> outstreams = new ArrayList<ServerOutStream>();

            //Init Game Controller (but do not start yet)
            game_controller = new GameController(outstreams, instreams, main_UI, decisions, switches, conditional_data, room_name, env, decision_handler, windata, rounds_max, current_round);

            main_UI.writeTxtLog("Our IP is " + InetAddress.getLocalHost().getHostAddress());

            //Start Broadcasting
            server_broadcaster = new Broadcaster(room_name);
            Thread t1 = new Thread(server_broadcaster, "Server_Broadcaster");
            t1.setDaemon(true);
            t1.start();

            //Open up the connection handler
            connection_handler = new ConnectionHandler(instreams, outstreams, game_controller, main_UI);
            Thread t2 = new Thread(connection_handler, "Connection_Handler");
            t2.setDaemon(true);
            t2.start();

        } catch (UnknownHostException ex) {
            Logger.getLogger(FXMLServerController.class
                    .getName()).log(Level.SEVERE, null, ex);

        } catch (SocketException ex) {
            Logger.getLogger(FXMLServerController.class
                    .getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void cancelRoom(Button btnRoom, Button btnStart, Button btnResume, Button btnGraph, ChoiceBox<Integer> cbRounds) {
        room_created = false;
        btnRoom.setText("Create Room");
        btnStart.setDisable(true);
        btnResume.setDisable(false);
        btnGraph.setDisable(true);
        cbRounds.setDisable(false);

        main_UI.writeTxtLog("Cancelling Room creation...");

        main_UI.writeTxtLog("Stopping broadcasting...");
        server_broadcaster.terminate();
        main_UI.writeTxtLog("Broadcasting stopped.");

        main_UI.writeTxtLog("Stopping connection handler...");
        connection_handler.terminate();
        main_UI.writeTxtLog("Connection handler stopped.");

        game_controller.terminate();
        //cancel game_handler, all open sockets, etc

        //RESET ALL VARIABLES IN ROOM_UI AND SERVERCONTROLLER
        main_UI.clearCanvas();
    }

    private String getFilePath(Stage stage) {
        String path = null;

        while (true) {
            DirectoryChooser directory_chooser = new DirectoryChooser();
            directory_chooser.setTitle("Please navigate to the folder containing decision files.");
            File selectedFolder = directory_chooser.showDialog(stage);

            if (selectedFolder != null) {
                path = selectedFolder.getAbsolutePath() + "/";
                System.out.println("Absolute: " + path);

                File decisions_path = new File(path + "wireless_decisions");
                File switch_path = new File(path + "conditionals_switch");
                File conditionals_path = new File(path + "conditionals_changes");

                if (!decisions_path.exists() || !switch_path.exists() || !conditionals_path.exists()) {
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setContentText("Please select a folder that contains the following three files:\n\nwireless_decisions\nconditionals_switch\nconditionals_changes");
                    alert.initModality(Modality.APPLICATION_MODAL);
                    alert.showAndWait();

                } else {
                    break;
                }
            } else {
                path = null;
                break;
            }
        }

        return path;
    }

}
