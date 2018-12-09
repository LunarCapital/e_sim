/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient;

import entreObj.Decision;
import entreObj.EData;
import entreObj.Environment;
import entreObj.PlayerDetails;
import entreObj.WinData;
import entresimclient.Objects.GraphicsHandler;
import entresimclient.Objects.PrereqChecker;
import entresimclient.Runnables.ClientInStream;
import entresimclient.Runnables.ClientOutStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;

import java.util.ResourceBundle;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import javafx.event.ActionEvent;
import javafx.event.EventHandler;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextInputDialog;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.scene.text.Font;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;
import javafx.util.Duration;

/**
 * FXML Controller class
 *
 * @author dylanleong
 */
public class FXMLGameController implements Initializable {

    @FXML
    private ComboBox<String> cmbPlayers;
    @FXML
    private Button btnPartner;
    @FXML
    private Button btnLock;
    @FXML
    private Button btnJoint;
    @FXML
    private TextArea txtInfo;
    @FXML
    private Label lblFinance;
    @FXML
    private Label lblExplore;
    @FXML
    private Label lblExploit;
    @FXML
    private Label lblChange;
    @FXML
    private Label lblName;
    @FXML
    private Label lblRole;
    @FXML
    private Button btnRequest;
    @FXML
    private Button btnDisplayFinance;
    @FXML
    private Button btnDisplayExplore;
    @FXML
    private Button btnDisplayExploit;
    @FXML
    private Button btnDetails;
    @FXML
    private VBox vboxIndiv;
    @FXML
    private VBox vboxJoint;
    @FXML
    private Button btnInfo;
    @FXML
    private Label lblNotify;
    @FXML
    private Label lblFinanceTitle;
    @FXML
    private Label lblExploreTitle;
    @FXML
    private Label lblExploitTitle;
    @FXML
    private Label lblIncomeTitle;
    @FXML
    private AnchorPane paneCanvas;
    @FXML
    private AnchorPane paneComparator;
    @FXML
    private Label lblExploreChangeTitle;
    @FXML
    private Label lblExploitChangeTitle;
    @FXML
    private Label lblExploreChange;
    @FXML
    private Label lblExploitChange;
    @FXML
    private AnchorPane paneLegend;

    private Socket socket;
    private GraphicsHandler graphics_handler;
    private volatile CyclicBarrier recv_latch;
    private ArrayList<String> proposal_description;
    private ArrayList<Button> indiv_choices;
    private ArrayList<Button> joint_choices;
    private TreeSet<Decision> decisions;
    private ClientOutStream outstream;
    private ClientInStream instream;
    private int indiv_choice_id;
    private int joint_choice_id;
    private boolean last_choice_is_indiv;
    private int round_num;
    private boolean[] show; //In order, finance, exploratory, exploitative, finance/turn
    private Environment env;
    private String name;
    private String initial_values;
    private PrereqChecker prereq_checker;
    private FXMLGameLogController log_controller;
    Timeline display_flasher;

    private HashMap<Shape, Integer> shape_to_id;
    private HashMap<Integer, Shape> id_to_shape;
    private HashMap<Shape, Label> shape_to_label;
    private double orgSceneX, orgSceneY, orgLayoutX, orgLayoutY;
    private double r;
    private Label lblRound;
    private ArrayList<Line> lines;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        txtInfo.setEditable(false);
        graphics_handler = new GraphicsHandler();
        lines = new ArrayList<>();
        proposal_description = new ArrayList<String>();
        indiv_choices = new ArrayList<Button>();
        joint_choices = new ArrayList<Button>();
        prereq_checker = new PrereqChecker(decisions);
        indiv_choice_id = -1;
        joint_choice_id = -1;
        last_choice_is_indiv = true;
        round_num = 0;
        show = new boolean[3];
        r = 30;
        disableAll();
        btnInfo.setDisable(true);

        drawLegend();
    }

    public void init(Socket s) {
        socket = s;
        recv_latch = new CyclicBarrier(2);
        outstream = new ClientOutStream(socket);
        instream = new ClientInStream(socket, this, recv_latch);

        Thread t1 = new Thread(outstream, "My Outstream");
        Thread t2 = new Thread(instream, "My Instream");
        t1.setDaemon(true);
        t2.setDaemon(true);
        t1.start();
        t2.start();

        //FLASH HIDE/SHOW BUTTONS
        display_flasher = new Timeline(
                new KeyFrame(Duration.seconds(0.2), e -> {
                    btnDisplayFinance.setStyle("-fx-background-color: red");
                    btnDisplayExplore.setStyle("-fx-background-color: red");
                    btnDisplayExploit.setStyle("-fx-background-color: red");
                }),
                new KeyFrame(Duration.seconds(0.4), e -> {
                    btnDisplayFinance.setStyle("-fx-background-color: green");
                    btnDisplayExplore.setStyle("-fx-background-color: green");
                    btnDisplayExploit.setStyle("-fx-background-color: green");
                })
        );
        display_flasher.setCycleCount(Animation.INDEFINITE);

        openLogWindow();
    }

    private void openLogWindow() {
        try { //Open up proposal window, everything should be empty
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/entresimclient/FXMLGameLog.fxml"));
            Stage stage = new Stage();
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);
            log_controller = loader.<FXMLGameLogController>getController();

            stage.setOnCloseRequest(evt -> {
                evt.consume();
            });
            stage.show();

        } catch (IOException ex) {
            Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    public void chooseName() {
        while (!instream.getNameApproved().equals("YES")) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle("Name Input Dialog");
            dialog.setContentText("Please enter a name.");
            Stage dialog_stage = (Stage) dialog.getDialogPane().getScene().getWindow();
            dialog_stage.setAlwaysOnTop(true);

            Optional<String> result = dialog.showAndWait();
            if (result.isPresent()) {
                outstream.setData(new EData("NAME", result.get()));
                name = result.get();

                try {
                    recv_latch.await();
                } catch (InterruptedException ex) {
                    Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
                } catch (BrokenBarrierException ex) {
                    Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
                }
                recv_latch.reset();
            }

            Alert alert = new Alert(AlertType.INFORMATION);
            //alert.initModality(Modality.APPLICATION_MODAL);
            Stage alert_stage = (Stage) alert.getDialogPane().getScene().getWindow();
            alert_stage.setAlwaysOnTop(true);

            if (instream.getNameApproved().equals("NO")) {
                alert.setTitle("Name already taken or invalid.");
                alert.setContentText("Either your name is already taken, is more than 20 characters, or is empty.");
                alert.showAndWait();
            } else if (instream.getNameApproved().equals("NO_LOAD")) {
                alert.setTitle("Name rejected.");
                alert.setContentText("The tutor is loading a game from a previous session.  Please ensure that your name is the same as the last time you played.  If you can't remember, the tutor can show you a list of names.  If you are still having trouble, one of your classmates may have taken your name.");
                alert.showAndWait();
            } else {
                //txtLog.appendText("Name Approved!\nPlease wait for the game to start.\n");
                writeText("Name Approved!\nPlease wait for the game to start.\n");
            }
        }
        System.out.println("Name approved: " + name);
        lblName.setText(name);

        try {
            PrintStream error_out = new PrintStream(new FileOutputStream("../../../" + name + "_client_error_log.txt"));
            System.setErr(error_out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void populateChoiceButtons(TreeSet<Decision> decisions_package) {
        if (decisions == null) {
            decisions = decisions_package;
        } else {
            decisions.addAll(decisions_package);
        }

        for (Decision d : decisions_package) {
            Button b = new Button(d.title);
            if (d.type.equals("Indiv")) { //Individual Button
                b.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent a) {
                        txtInfo.setText(d.description);
                        indiv_choice_id = d.id;
                        last_choice_is_indiv = true;
                        enableOtherIndivButtons(b);
                        btnLock.setDisable(false);
                    }
                });

                indiv_choices.add(b);
                if (prereq_checker.checkRoleRestricted(d.prereqs, env, name)) { //hide decisions not available to a certain role
                    vboxIndiv.getChildren().add(b);
                }

            } else { //Joint Button
                b.setOnAction(new EventHandler<ActionEvent>() {
                    public void handle(ActionEvent a) {
                        txtInfo.setText(d.description);
                        joint_choice_id = d.id;
                        last_choice_is_indiv = false;
                        enableOtherJointButtons(b);
                        btnJoint.setDisable(false);
                        btnPartner.setDisable(false);
                    }
                });

                joint_choices.add(b);
                vboxJoint.getChildren().add(b);
            }
        }
    }

    public void prepareRound(int round_num, Environment e) {
        env = e;
        this.round_num = round_num;
        ObservableList<String> available_players = FXCollections.observableArrayList();
        boolean dead = false;

        for (PlayerDetails p : env.players) {
            if (p.name.equals(this.name)) { //UPDATE MY INFO PANEL        
                lblFinance.setText("" + p.finance);
                lblExplore.setText("" + p.explore);
                lblExploit.setText("" + p.exploit);
                lblChange.setText("" + p.f_change);
                lblExploreChange.setText("" + p.explore_change);
                lblExploitChange.setText("" + p.exploit_change);

                if (p.finance <= 0) {
                    dead = true;
                }

                String role = p.getRole();
                if (role.equals("R")) {
                    lblRole.setText("R&D");
                } else if (role.equals("M")) {
                    lblRole.setText("Manufacturer");
                } else if (role.equals("F")) {
                    lblRole.setText("Financial Backer");
                }
                show[0] = p.show_finance;
                show[1] = p.show_explore;
                show[2] = p.show_exploit;
            } else { //Update everybody elses information if they want to display it
                if (p.finance > 0) { //don't record dead players
                    //Put all info on a label                    
                    available_players.add(p.name);
                }
            }
        }

        cmbPlayers.setItems(available_players);

        btnInfo.setDisable(false);
        if (dead) {
            //Do nothing
            selfComment("Bankrupt!", "", "You have no financial resources and are therefore unable to participate.");
            lblNotify.setTextFill(Color.web("#FF0000"));
            lblNotify.setText("You have no finance and have died!");

            updateRound();
            redrawNetwork();
        } else if (round_num > 0) {
            lblNotify.setTextFill(Color.web("#58FA58"));
            lblNotify.setText("You can make one joint and one individual decision!");
            enableAll();

            updateRound();
            redrawNetwork();
        } else { //Zero Round
            PlayerDetails me = env.getPlayer(env.name_to_int.get(name));
            initial_values = "My initial resource values are: " + "Finance: " + me.finance + ", Exploratory: " + me.explore + ", Exploitative: " + me.exploit + ".";
            indiv_choice_id = 0;
            enableShowBtns();

            //Draw Network
            setupCanvas();

            display_flasher.play();
        }
    }

    public void jointRequested(String joint_request) {
        StringTokenizer st = new StringTokenizer(joint_request);
        int decision_id = Integer.parseInt(st.nextToken());
        String requester_name = st.nextToken();

        Decision decision = null;
        String description = "";
        for (Decision d : decisions) {
            if (d.id == decision_id) {
                decision = d;
                description = d.description;
                break;
            }
        }

        PrereqChecker prereq_checker = new PrereqChecker(decisions);

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("A partner has attempted to make a joint decision with you.");
        alert.setHeaderText("From Player: " + requester_name + ", to you: " + name);
        alert.initModality(Modality.NONE);
        Stage alert_stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert_stage.setAlwaysOnTop(true);
        Window alert_window = alert.getDialogPane().getScene().getWindow();

        if (!prereq_checker.checkNegativeJointKnowledge(decision, env, name)) {
            alert.setAlertType(AlertType.INFORMATION);
            alert.setContentText("However, because the joint decision costs more knowledge than you currently have, you will automatically decline after this alert is closed.");
            alert.showAndWait();
            System.out.println("Auto declined due to lack of resources");
            outstream.setData(new EData("JOINT_ACK", "NO " + requester_name));
            writeText(requester_name + " attempted to make joint decision '" + decision.title + "' with you, but you lacked the resources to accept.");
        } else {
            alert.setContentText("Description: " + description + "\nWould you like to accept?");

            ButtonType buttonTypeYes = new ButtonType("Yes");
            ButtonType buttonTypeNo = new ButtonType("No");
            ButtonType buttonTypeDetails = new ButtonType("View More Details");
            alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo, buttonTypeDetails);

            final Button buttonDetails = (Button) alert.getDialogPane().lookupButton(buttonTypeDetails);
            buttonDetails.addEventFilter(ActionEvent.ACTION, event -> {
                Decision d = null;
                for (Decision each_decision : decisions) {
                    if (each_decision.id == decision_id) {
                        d = each_decision;
                        break;
                    }
                }

                if (d != null) { //bring up a new window quick displaying details about decision d
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDecisionQuickView.fxml"));
                        Stage stage = new Stage();
                        stage.initModality(Modality.WINDOW_MODAL);
                        stage.initOwner(alert_window);
                        stage.setScene(new Scene((Pane) loader.load()));
                        stage.setResizable(false);

                        FXMLDecisionQuickViewController controller = loader.<FXMLDecisionQuickViewController>getController();
                        controller.init(decisions, d.id);
                        stage.showAndWait();
                    } catch (IOException ioe) {
                        System.out.println(ioe.getMessage());
                    }
                }
                event.consume();
            });

            Optional<ButtonType> result = alert.showAndWait();
            if (result.get() == buttonTypeYes) {
                System.out.println("Pressed yes");
                outstream.setData(new EData("JOINT_ACK", "YES " + requester_name + " " + decision_id));
                writeText(requester_name + " attempted to make joint decision '" + decision.title + "' with you, and you accepted.");
            } else if (result.get() == buttonTypeDetails) {
                //Should carry out the above eventfilter without closing the alert
            } else {
                System.out.println("Pressed no/cancel");
                outstream.setData(new EData("JOINT_ACK", "NO " + requester_name));
                writeText(requester_name + " attempted to make joint decision '" + decision.title + "' with you, but you declined.");
            }
            System.out.println(result.get().toString());
        }
    }

    public void pairRequested(String name) {
        cmbPlayers.getItems().remove(name);

        System.out.println("CALLING DIALOG BOX");
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("You've been offered a partnership.");
        alert.setHeaderText("From Player: " + name + ", to you: " + this.name);
        alert.setContentText("Do you accept?");
        alert.initModality(Modality.NONE);
        Stage alert_stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert_stage.setAlwaysOnTop(true);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            outstream.setData(new EData("PAIR_ACK", "YES " + name));
            writeText(name + " attempted to form a partnership with you, and you accepted.");
        } else {
            outstream.setData(new EData("PAIR_ACK", "NO " + name));
            writeText(name + " attempted to form a partnership with you, but you declined.");
        }
    }

    public void writeText(String text) {
        //txtLog.appendText(text + "\n");
        log_controller.writeTxtLog(text, round_num);
    }

    public void resetJoint() {
        btnJoint.setDisable(false);
        lblNotify.setTextFill(Color.web("#58FA58"));
        lblNotify.setText("You can make one joint and one individual decision!");
    }

    public void dialogComment(String comment) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText("Comment received from server.");
        alert.setContentText(comment);
        alert.initModality(Modality.APPLICATION_MODAL);
        Stage alert_stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert_stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    public void selfComment(String title, String header, String comment) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(comment);
        alert.initModality(Modality.APPLICATION_MODAL);
        Stage alert_stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert_stage.setAlwaysOnTop(true);

        alert.showAndWait();
    }

    public void updateConditionalDecisions(ArrayList<Decision> updated_decisions) {
        for (Decision each_decision : updated_decisions) {
            //FIND DECIISON IN TREESET, REMOVE?
            //ADD UPDATED DECISION TO TREESET?

            Decision old_decision = null;
            for (Decision each_existing_decision : decisions) {
                if (each_existing_decision.id == each_decision.id) {
                    old_decision = each_existing_decision;
                }
            }
            decisions.remove(old_decision);
            decisions.add(each_decision);
        }
    }

    public void endGame(TreeSet<WinData> windata) {
        Environment end_env = instream.env;
        PlayerDetails p = end_env.int_to_player.get(end_env.name_to_int.get(name));

        lblFinance.setText("" + p.finance);
        lblExplore.setText("" + p.explore);
        lblExploit.setText("" + p.exploit);
        lblChange.setText("" + p.f_change);
        lblExploreChange.setText("" + p.explore_change);
        lblExploitChange.setText("" + p.exploit_change);

        int our_id = env.name_to_int.get(name);
        graphics_handler.winGraph(windata, our_id, txtInfo.getScene().getWindow());

        try {
            PrintStream out = new PrintStream(new FileOutputStream("../../../" + name + "_client_error_log.txt"));
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
        }

        try {
            File client_log = new File("../../../" + name + "_client_game_log.txt");
            client_log.getParentFile().mkdirs();
            client_log.createNewFile();
            PrintWriter out = new PrintWriter(client_log);
            out.print(log_controller.getTxtLog());
            out.flush();
            out.close();
        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        log_controller.close();
        System.exit(0);
    }

    @FXML
    private void handleBtnPartner(ActionEvent event) {
        joint_choice_id = -2;
        btnPartner.setDisable(true);
        btnJoint.setDisable(false);
        enableOtherJointButtons(null);
    }

    @FXML
    private void handleBtnLock(ActionEvent event) {
        if (round_num == 0) { //Don't check prereqs on round zero
            outstream.setData(new EData("SHOW", show));
            outstream.setData(new EData("DECISION", "" + indiv_choice_id));
            disableAll();
            btnLock.setText("Lock Individual Decision and End Turn");
            display_flasher.stop();
            btnDisplayFinance.setStyle("");
            btnDisplayExplore.setStyle("");
            btnDisplayExploit.setStyle("");
        } else {
            Decision d = null;
            for (Decision each : decisions) {
                if (each.id == indiv_choice_id) {
                    d = each;
                    break;
                }
            }

            prereq_checker = new PrereqChecker(decisions);
            String reason = "";
            if (!(reason = prereq_checker.checkNegativeKnowledge(d, env, name)).equals("")) {
                dialogComment(reason);
            } else if ((reason = prereq_checker.checkPrerequisites(d.prereqs, env, name, "")).equals("")) {
                outstream.setData(new EData("SHOW", show));
                outstream.setData(new EData("DECISION", "" + indiv_choice_id));
                lblNotify.setTextFill(Color.web("#FF0000"));
                lblNotify.setText("You have ended your turn and must wait for the next round.");
                disableAll();
                writeText("You locked in the decision '" + d.title + "'");
            } else {
                dialogComment("The prequisites to make this decision are not met.\n" + reason);
            }
        }
    }

    @FXML
    private void handleBtnJoint(ActionEvent event) {
        String selected = cmbPlayers.getSelectionModel().getSelectedItem();
        System.out.println("Selected is " + selected);
        if (joint_choice_id == -1) {
            selfComment("Notice", "", "Please select a joint decision before pressing the golden joint button.");
            return;
        } else if (selected == null) {
            selfComment("Notice", "", "Please select another player before making a joint decision.");
            return;
        } else if (joint_choice_id == -2) { //Special partnership case, does NOT use up 'joint slot'
            if (env.network[env.name_to_int.get(name)][env.name_to_int.get(selected)] == -1) { //not already partners
                cmbPlayers.getItems().remove(selected);
                outstream.setData(new EData("PAIR_REQUEST", selected));
                writeText("You made a partnership request to the player '" + selected + "'");
                return;
            } else {
                selfComment("Regarding your partnership request", "", "You are already a partner with this person!");
                return;
            }
        }

        if (env.network[env.name_to_int.get(name)][env.name_to_int.get(selected)] == -1) { //check if in partnership
            selfComment("Unable to make joint decision", "", "Please make sure the player you select is actually in a partnership with you before trying to make a joint decision.");
            return;
        }

        Decision d = null;
        for (Decision each : decisions) {
            if (each.id == joint_choice_id) {
                d = each;
                break;
            }
        }

        String reason = "";
        if (!(reason = prereq_checker.checkNegativeKnowledge(d, env, name)).equals("")) {
            dialogComment(reason);
        } else if ((reason = prereq_checker.checkPrerequisites(d.prereqs, env, name, selected)).equals("")) {
            writeText("Sending joint decision request '" + d.title + "' to " + selected);
            outstream.setData(new EData("JOINT_REQUEST", selected + " " + joint_choice_id));
            btnJoint.setDisable(true);
            lblNotify.setTextFill(Color.web("#ffae22"));
            lblNotify.setText("You can make an individual decision!");
        } else {
            dialogComment("The prequisites to make this decision are not met.\n" + reason);
        }
    }

    @FXML
    private void handleBtnDisplayFinance(ActionEvent event) {
        if (show[0]) {
            show[0] = false;
            btnDisplayFinance.setText("Show");
            lblFinanceTitle.setText("Finance [HIDDEN]");
        } else {
            show[0] = true;
            btnDisplayFinance.setText("Hide");
            lblFinanceTitle.setText("Finance [SHOWN]");
        }
    }

    @FXML
    private void handleBtnDisplayExplore(ActionEvent event) {
        if (show[1]) {
            show[1] = false;
            btnDisplayExplore.setText("Show");
            lblExploreTitle.setText("Explore [HIDDEN]");
        } else {
            show[1] = true;
            btnDisplayExplore.setText("Hide");
            lblExploreTitle.setText("Explore [SHOWN]");
        }
    }

    @FXML
    private void handleBtnDisplayExploit(ActionEvent event) {
        if (show[2]) {
            show[2] = false;
            btnDisplayExploit.setText("Show");
            lblExploitTitle.setText("Exploit [HIDDEN]");
        } else {
            show[2] = true;
            btnDisplayExploit.setText("Hide");
            lblExploitTitle.setText("Exploit [SHOWN]");
        }
    }

    @FXML
    private void handleBtnRequest(ActionEvent event) {
        String proposal = "";
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDescriptionWriter.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);

            stage.setOnCloseRequest(evt -> {
                evt.consume();

                Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    stage.close();
                }
            });

            FXMLDescriptionWriterController controller = loader.<FXMLDescriptionWriterController>getController();
            controller.init(proposal_description);

            stage.showAndWait();
            if (!proposal_description.isEmpty()) { //description entered
                proposal = proposal_description.get(0);
                proposal_description.clear();
            } else { //propsoal cancelled
                return; //cancel propsoal
            }

        } catch (IOException ex) {
            Logger.getLogger(FXMLGameController.class.getName()).log(Level.SEVERE, null, ex);
            return; //don't continue 
        }

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Name Input");
        dialog.setHeaderText("");
        dialog.setContentText("Please enter a name for your custom decision proposal.");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Optional<String> result = dialog.showAndWait();
        if (result.isPresent()) {
            String[] s = new String[2];
            s[0] = result.get();
            s[1] = proposal;
            outstream.setData(new EData("PROPOSAL", s));
            btnRequest.setDisable(true);
        }
        proposal_description = new ArrayList<String>();
    }

    private void disableAll() {
        //vboxIndiv.setDisable(true);
        //vboxJoint.setDisable(true);
        cmbPlayers.setDisable(true);
        btnPartner.setDisable(true);
        btnLock.setDisable(true);
        btnJoint.setDisable(true);
        //btnDetails.setDisable(true);

        btnDisplayFinance.setDisable(true);
        btnDisplayExplore.setDisable(true);
        btnDisplayExploit.setDisable(true);

        btnRequest.setDisable(true);
    }

    private void enableAll() {
        vboxIndiv.setDisable(false);
        vboxJoint.setDisable(false);
        cmbPlayers.setDisable(false);
        btnPartner.setDisable(false);
        btnDetails.setDisable(false);

        btnDisplayFinance.setDisable(false);
        btnDisplayExplore.setDisable(false);
        btnDisplayExploit.setDisable(false);

        btnRequest.setDisable(false);

        enableOtherIndivButtons(null);
        enableOtherJointButtons(null);
    }

    private void enableShowBtns() {
        btnDisplayFinance.setDisable(false);
        btnDisplayExplore.setDisable(false);
        btnDisplayExploit.setDisable(false);
        btnLock.setDisable(false);
        btnDetails.setDisable(false);

        btnDisplayFinance.setText((show[0]) ? "Hide" : "Show");
        btnDisplayExplore.setText((show[1]) ? "Hide" : "Show");
        btnDisplayExploit.setText((show[2]) ? "Hide" : "Show");
    }

    private void enableOtherIndivButtons(Button except) {
        for (Button each : indiv_choices) {
            if (each != except) {
                each.setDisable(false);
            } else {
                each.setDisable(true);
            }
        }
    }

    private void enableOtherJointButtons(Button except) {
        for (Button each : joint_choices) {
            if (each != except) {
                each.setDisable(false);
            } else {
                each.setDisable(true);
            }
        }
    }

    @FXML
    private void handleBtnDetails(ActionEvent event) {
        int decision_id = (last_choice_is_indiv) ? indiv_choice_id : joint_choice_id;

        Decision d = null;
        for (Decision each : decisions) {
            if (each.id == decision_id) {
                d = each;
                break;
            }
        }

        if (d != null) { //bring up a new window quick displaying details about decision d
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDecisionQuickView.fxml"));
                Stage stage = new Stage();
                //stage.initModality(Modality.APPLICATION_MODAL);
                stage.setAlwaysOnTop(true);
                stage.setScene(new Scene((Pane) loader.load()));
                stage.setResizable(false);

                FXMLDecisionQuickViewController controller = loader.<FXMLDecisionQuickViewController>getController();
                controller.init(decisions, d.id);
                stage.showAndWait();
            } catch (IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText("Please select a decision to view from the list first.");
            alert.showAndWait();
        }
    }

    @FXML
    private void handleBtnInfo(ActionEvent event) {
        Alert alert = new Alert(AlertType.INFORMATION);
        alert.setTitle("Information");
        alert.setHeaderText(null);
        alert.setContentText(null);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.getDialogPane().setPrefSize(720, 640);

        //I solemnly promise to myself i will do it in a better way next time
        String info_blurb
                = "You are part of a network of innovators interested in wireless LED, which means using LED lights to embed sensors, which are wirelessly connected to each other, in order to collect various types of information (environmental conditions, track items, presence/absence, etc.).\n"
                + "\n"
                + "Both hardware and software components are present within this product system, which allows for collection data from up to 1,000 different sensors. It has a low cost of implementation and maintenance and has multiple applications. For example: in health services (monitoring patients and treatments), in hospitality (personnel tracking to optimise workflow practices; equipment location), or in logistics and warehousing (inventory).\n"
                + "\n"
                + "Your allocated role is '" + lblRole.getText() + "' and your initial financial and knowledge resources are shown below.\n"
                + "\n"
                + "Financial Resources: " + lblFinance.getText() + "\n"
                + "Exploratory Knowledge: " + lblExplore.getText() + "\n"
                + "Exploitative Knowledge: " + lblExploit.getText() + "\n"
                + "\n"
                + "The network includes " + (env.players.size() - 1) + " other members, with whom you have the possibility to support the idea ([either through funding, providing technical expertise, manufacturing the gadget and commercialise it - depending on the role].\n"
                + "The roles and resources of other network participants are also shown, depending on their willingness to disclose or otherwise their position. The structure of the network will be displayed in the left pane and each time when a new connection/alliance occurs, the network will be updated.\n"
                + "\n"
                + "The simulation/game will run in [8-12]? rounds. In each round you are allowed to make two decisions to enable you to reach out collaborators, access other networks, fund the project or obtain competitive advantage. These decisions are of two types, individual and joint (with others), and some of the possibilities are enumerated in the list on the right hand side of the screen. You need to select and lock-in the decision, which will use some of the resources you have. Some decisions may also have uncertain outcomes, such as the success in obtaining funds, and the chances of success are displayed up-front for you to make the decisions fully informed.\n"
                + "\n"
                + "At the end of each round, after all participants have made their decisions, you will be informed of your current resources and your position in the network, so you can adjust your decisions for the following round(s).\n"
                + "\n"
                + "The environmental conditions and the resources of others will change as well, and it is expected that all network members will adapt their decisions (use certain resources, strengthen connections with other participants, commercialise, etc.), based on their own interests.\n"
                + "\n"
                + "At the end of the game, you will be informed of your performance in relation to the network.\n"
                + "\n"
                + "To log into the game you'll need to choose a unique ID and if the game is paused or you encounter wifi access issues and/or log out you need to use the same ID to reconnect to your network.\n"
                + "\n"
                + "Now, are you ready to start?";

        info_blurb += "\n" + "\n" + initial_values; //Show initial resource values here

        info_blurb += "\n" + "\n" + "Switches on:\n"; //Show switches here
        PlayerDetails me = env.getPlayer(env.name_to_int.get(name));
        for (String each_switch : me.switch_map.keySet()) {
            if (me.switch_map.get(each_switch)) { //if switch set to true
                info_blurb += each_switch + "\n";
            }
        }

        TextArea txtInfo = new TextArea(info_blurb);
        txtInfo.setEditable(false);
        txtInfo.setWrapText(true);
        txtInfo.setPrefSize(Double.MAX_VALUE, Double.MAX_VALUE);

        /* GridPane infoPane = new GridPane();
        infoPane.setMaxWidth(Double.MAX_VALUE);
        infoPane.setMaxHeight(Double.MAX_VALUE);
        infoPane.add(txtInfo, 0, 0);*/
        alert.getDialogPane().setContent(txtInfo);

        Stage alert_stage = (Stage) alert.getDialogPane().getScene().getWindow();
        alert_stage.setAlwaysOnTop(true);
        alert.showAndWait();

    }

    public void setupCanvas() {
        shape_to_id = new HashMap<>();
        id_to_shape = new HashMap<>();
        shape_to_label = new HashMap<>();

        //Draw on canvas
        for (PlayerDetails each_player : env.players) {
            Shape player_shape;
            Color randomColor = new Color(each_player.red, each_player.green, each_player.blue, 1);

            double x = ((paneCanvas.getWidth() - 2 * 30) * Math.random()) + 30;
            double y = ((paneCanvas.getHeight() - 2 * 30) * Math.random()) + 30;

            switch (each_player.getRole()) {
                case "R":
                    player_shape = new Circle(30 / 2, randomColor);
                    break;
                case "M":
                    player_shape = new Rectangle(30, 30, randomColor);
                    break;
                case "F":
                    player_shape = new Polygon(0.0, 0.0, 30, 0, 30 / 2, 30);
                    player_shape.setFill(randomColor);
                    break;
                default:
                    player_shape = new Circle(30, Color.BLUE);
                    break;
            }
            player_shape.setOnMousePressed(onMousePressedHandler);
            player_shape.setOnMouseDragged(onMouseDraggedHandler);
            player_shape.setOnMouseEntered(onMouseEnteredHandler);
            player_shape.setOnMouseExited(onMouseExitedHandler);

            player_shape.relocate(x - (30 / 2), y - (30 / 2));

            Label lblDisplay = new Label("");
            if (!each_player.name.equals(name)) {
                String strDisplay = "" + each_player.name + "\n";
                String player_finance = (each_player.show_finance) ? "" + each_player.finance : "?";
                String player_explore = (each_player.show_explore) ? "" + each_player.explore : "?";
                String player_exploit = (each_player.show_exploit) ? "" + each_player.exploit : "?";
                strDisplay += "" + player_finance + " " + player_explore + " " + player_exploit;
                lblDisplay.setText(strDisplay);
            } else {
                lblDisplay.setText("YOU");
            }

            lblDisplay.relocate(x - (30 / 2), y - (30 / 2));
            shape_to_label.put(player_shape, lblDisplay);
            lblDisplay.setMouseTransparent(true);

            shape_to_id.put(player_shape, each_player.id);
            id_to_shape.put(each_player.id, player_shape);
            paneCanvas.getChildren().addAll(player_shape, lblDisplay);
        }
    }

    public void updateRound() {
        if (lblRound != null) {
            paneCanvas.getChildren().remove(lblRound);
        }
        lblRound = new Label("Round " + round_num);
        lblRound.setFont(new Font("Arial", 24));

        paneCanvas.getChildren().add(lblRound);
    }

    public void redrawNetwork() {
        paneCanvas.getChildren().removeAll(lines);
        lines = new ArrayList<>();

        for (int i = 0; i < env.players.size(); i++) {
            if (env.players.get(i).getDead()) {
                Shape shape = id_to_shape.get(env.players.get(i).id);
                shape.setFill(Color.BLACK);
                shape_to_label.get(shape).setText(env.players.get(i).name + " is dead.");
                shape_to_label.get(shape).setTextFill(Color.DARKGRAY);
                continue;
            }
            String strDisplay;
            if (!(env.players.get(i).name).equals(name)) {
                strDisplay = "" + (env.players.get(i).name) + "\n";
                String player_finance = ((env.players.get(i)).show_finance) ? "" + (env.players.get(i)).finance : "?";
                String player_explore = ((env.players.get(i)).show_explore) ? "" + (env.players.get(i)).explore : "?";
                String player_exploit = ((env.players.get(i)).show_exploit) ? "" + (env.players.get(i)).exploit : "?";
                strDisplay += "" + player_finance + " " + player_explore + " " + player_exploit;
            } else {
                strDisplay = "YOU";
            }
            shape_to_label.get(id_to_shape.get(env.players.get(i).id)).setText(strDisplay);

            for (int j = i; j < env.players.size(); j++) {
                if (env.players.get(j).getDead()) {
                    continue;
                }
                Line line = new Line();

                if (i != j && env.network[i][j] != -1) {

                    PlayerDetails player_one = env.int_to_player.get(i);
                    PlayerDetails player_two = env.int_to_player.get(j);
                    Shape shape_one = id_to_shape.get(player_one.id);
                    Shape shape_two = id_to_shape.get(player_two.id);

                    if (player_one.getRole().equals("R")) {
                        line.setStartX(shape_one.getLayoutX());
                        line.setStartY(shape_one.getLayoutY());
                    } else {
                        line.setStartX(shape_one.getLayoutX() + 30 / 2);
                        line.setStartY(shape_one.getLayoutY() + 30 / 2);
                    }

                    if (player_two.getRole().equals("R")) {
                        line.setEndX(shape_two.getLayoutX());
                        line.setEndY(shape_two.getLayoutY());
                    } else {
                        line.setEndX(shape_two.getLayoutX() + 30 / 2);
                        line.setEndY(shape_two.getLayoutY() + 30 / 2);
                    }
                    line.setStrokeWidth(30 / 10);
                    line.setStroke(Color.BLACK);

                    lines.add(line);
                    paneCanvas.getChildren().add(line);
                }
            }
        }
    }

    public void drawStats(Shape shape) {
        PlayerDetails check = env.int_to_player.get(shape_to_id.get(shape));
        PlayerDetails you = env.int_to_player.get(env.name_to_int.get(name));
        if (check == null || you == check) {
            return;
        }

        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Resource Comparator (Others may hide resources)");
        bc.setMaxHeight(paneComparator.getPrefHeight());
        xAxis.setLabel("Type");
        yAxis.setLabel("Units");

        XYChart.Series seriesYou = new XYChart.Series();
        seriesYou.setName("YOU");
        seriesYou.getData().add(new XYChart.Data("Your $", you.finance));
        seriesYou.getData().add(new XYChart.Data("Your Explr", you.explore));
        seriesYou.getData().add(new XYChart.Data("Your Explt", you.exploit));

        XYChart.Series seriesCheck = new XYChart.Series();
        seriesCheck.setName(check.name);
        if (check.show_finance) {
            seriesCheck.getData().add(new XYChart.Data("Their $", check.finance));
        }
        if (check.show_explore) {
            seriesCheck.getData().add(new XYChart.Data("Their Explr", check.explore));
        }
        if (check.show_exploit) {
            seriesCheck.getData().add(new XYChart.Data("Their Explt", check.exploit));
        }

        bc.getData().add(seriesYou);
        bc.getData().add(seriesCheck);
        bc.relocate(0, 0);
        paneComparator.getChildren().add(bc);
    }

    private void drawLegend() {
        Shape circle = new Circle(r / 2, Color.BLACK);
        Shape rectangle = new Rectangle(r, r, Color.BLACK);
        Shape triangle = new Polygon(0.0, 0.0, r, 0, r / 2, r);
        triangle.setFill(Color.BLACK);

        Label lblCircle = new Label("= R&D");
        Label lblRectangle = new Label("= Manufacturer");
        Label lblTriangle = new Label("= Financial Backer");

        circle.relocate(10, 15);
        rectangle.relocate(173, 15);
        triangle.relocate(336, 15);

        lblCircle.relocate(40, 20);
        lblRectangle.relocate(203, 20);
        lblTriangle.relocate(366, 20);

        paneLegend.getChildren().addAll(circle, rectangle, triangle, lblCircle, lblRectangle, lblTriangle);
    }

    EventHandler<MouseEvent> onMousePressedHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            orgSceneX = event.getSceneX();
            orgSceneY = event.getSceneY();
            orgLayoutX = ((Shape) (event.getSource())).getLayoutX();
            orgLayoutY = ((Shape) (event.getSource())).getLayoutY();
        }

    };

    EventHandler<MouseEvent> onMouseDraggedHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            double newLayoutX = orgLayoutX + offsetX;
            double newLayoutY = orgLayoutY + offsetY;

            if (newLayoutX > paneCanvas.getWidth() - r) {
                newLayoutX = paneCanvas.getWidth() - r;
            } else if (newLayoutX < r) {
                newLayoutX = r;
            }

            if (newLayoutY > paneCanvas.getHeight() - r) {
                newLayoutY = paneCanvas.getHeight() - r;
            } else if (newLayoutY < r) {
                newLayoutY = r;
            }

            ((Shape) (event.getSource())).setLayoutX(newLayoutX);
            ((Shape) (event.getSource())).setLayoutY(newLayoutY);

            Label attLabel = shape_to_label.get(((Shape) event.getSource()));
            attLabel.setLayoutX(newLayoutX - (30 / 2));
            attLabel.setLayoutY(newLayoutY - (30 / 2));

            redrawNetwork();
        }
    };

    EventHandler<MouseEvent> onMouseEnteredHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            drawStats((Shape) event.getSource());
        }
    };

    EventHandler<MouseEvent> onMouseExitedHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            paneComparator.getChildren().clear();
        }
    };

}
