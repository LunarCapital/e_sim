/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver;

import entreObj.Decision;
import entreObj.Environment;
import entreObj.PlayerDetails;
import entresimserver.UIHandlers.RoomUIHandler;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Optional;
import java.util.Random;
import java.util.ResourceBundle;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.collections.FXCollections;
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
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
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

/**
 *
 * @author dylanleong
 */
public class FXMLServerController implements Initializable {

    @FXML
    private AnchorPane rootPane;
    @FXML
    private Button btnRoom;
    @FXML
    private Button btnStart;
    @FXML
    private TextArea txtLog;
    @FXML
    private TextField txtServerName;
    @FXML
    private TextArea txtProposal;
    @FXML
    private Button btnReject;
    @FXML
    private Button btnAccept;
    @FXML
    private TextField txtProposalName;
    @FXML
    private ChoiceBox<Integer> cbRounds;
    @FXML
    private Label lblNotify;
    @FXML
    private Button btnResume;
    @FXML
    private VBox vboxPlayers;
    @FXML
    private AnchorPane paneCanvas;
    @FXML
    private AnchorPane paneSide;
    @FXML
    private Button btnGraph;
    Label lblRound;

    public Environment env;
    public ArrayList<PlayerDetails> players;
    public HashMap<Shape, Integer> shape_to_id;
    public HashMap<Integer, Shape> id_to_shape;
    public HashMap<Shape, Label> shape_to_label;
    double orgSceneX, orgSceneY, orgLayoutX, orgLayoutY;
    private ArrayList<Line> lines;

    private RoomUIHandler room_ui_handler;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        lines = new ArrayList<>();
        txtLog.setEditable(false);
        cbRounds.setItems(FXCollections.observableArrayList(
                8, 9, 10, 11, 12, 13, 14, 15)
        );
        cbRounds.getSelectionModel().selectFirst();

        room_ui_handler = new RoomUIHandler(this);
    }

    @FXML
    private void handleBtnRoom(ActionEvent event) {
        Stage stage = (Stage) rootPane.getScene().getWindow();
        room_ui_handler.createNewRoom(txtServerName, btnRoom, btnStart, btnResume, btnGraph, cbRounds, stage);
    }

    @FXML
    private void handleBtnOpen(ActionEvent event) {
        room_ui_handler.loadSavedRoom(rootPane, vboxPlayers, btnRoom, btnStart, btnResume, btnGraph, cbRounds);
    }

    @FXML
    private void handleBtnStart(ActionEvent event) {
        room_ui_handler.startRoom(btnStart, btnGraph);
    }

    @FXML
    private void handleBtnGraph(ActionEvent event) {
        if (!room_ui_handler.displayGraph()) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("The Resource Graph could not be displayed at this time.");
            alert.initModality(Modality.WINDOW_MODAL);
            alert.initOwner(txtLog.getScene().getWindow());
            alert.setContentText("When the game has started, the resource graph will become available for viewing.");
            alert.showAndWait();
        }
    }

    public void clearCanvas() {
        paneCanvas.getChildren().clear();
    }

    public void setupCanvas(Environment env) {
        this.env = env;
        this.players = env.players;
        shape_to_id = new HashMap<>();
        id_to_shape = new HashMap<>();
        shape_to_label = new HashMap<>();

        //Draw on canvas
        for (PlayerDetails each_player : players) {
            Shape player_shape;
            Random rand = new Random();
            float rand_red = (float) (rand.nextFloat() / 2f + 0.5);
            each_player.red = rand_red;
            float rand_green = (float) (rand.nextFloat() / 2f + 0.5);
            each_player.green = rand_green;
            float rand_blue = (float) (rand.nextFloat() / 2f + 0.5);
            each_player.blue = rand_blue;
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
            String strDisplay = "" + each_player.name + "\n";
            strDisplay += "" + each_player.finance + " " + each_player.explore + " " + each_player.exploit;
            lblDisplay.relocate(x - (30 / 2), y - (30 / 2));
            lblDisplay.setText(strDisplay);
            shape_to_label.put(player_shape, lblDisplay);
            lblDisplay.setMouseTransparent(true);

            shape_to_id.put(player_shape, each_player.id);
            id_to_shape.put(each_player.id, player_shape);
            paneCanvas.getChildren().addAll(player_shape, lblDisplay);
        }
    }

    public void drawStats(Shape shape) {
        PlayerDetails check = env.int_to_player.get(shape_to_id.get(shape));
        if (check == null) {
            return;
        }

        //Stage stage = new Stage();
        //graphics_handler.drawGraph(scnSide, check);
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle(check.name + "'s Resources");
        bc.setMaxSize(paneSide.getWidth() * 1 / 10, paneSide.getHeight());
        xAxis.setLabel("Resource Type");
        yAxis.setLabel("Resource Units");

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Resources");
        series.getData().add(new XYChart.Data<>("Finance", check.finance));
        series.getData().add(new XYChart.Data<>("Exploratory", check.explore));
        series.getData().add(new XYChart.Data<>("Exploitative", check.exploit));
        series.getData().add(new XYChart.Data<>("Income", check.f_change));

        System.out.println("Finance is " + check.finance);

        bc.getData().add(series);
        bc.relocate(0, 0);
        paneSide.getChildren().add(bc);
    }

    public void clearStats() {
        //scnSide = new SubScene(null, scnSide.getWidth(), scnSide.getHeight());
        paneSide.getChildren().clear();
    }

    public void updateRound() {
        if (lblRound != null) {
            paneCanvas.getChildren().remove(lblRound);
        }
        lblRound = new Label("Round " + (room_ui_handler.game_controller.current_round + 1));
        lblRound.setFont(new Font("Arial", 24));

        paneCanvas.getChildren().add(lblRound);
    }

    public void redrawNetwork() {
        paneCanvas.getChildren().removeAll(lines);
        lines = new ArrayList<>();

        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getDead()) {
                Shape shape = id_to_shape.get(players.get(i).id);
                shape.setFill(Color.BLACK);
                shape_to_label.get(shape).setText(players.get(i).name + " is dead.");
                shape_to_label.get(shape).setTextFill(Color.DARKGRAY);
                continue;
            }
            String strDisplay = "" + players.get(i).name + "\n";
            strDisplay += "" + players.get(i).finance + " " + players.get(i).explore + " " + players.get(i).exploit;
            shape_to_label.get(id_to_shape.get(players.get(i).id)).setText(strDisplay);
            for (int j = i; j < players.size(); j++) {
                if (players.get(j).getDead()) {
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

                    //System.out.println("From (" + line.getStartX() + ", " + line.getStartY() + ") to (" + line.getEndX() + ", " + line.getEndY() + ").");
                    lines.add(line);
                    paneCanvas.getChildren().add(line);
                }
            }
        }
    }

    /**
     * Shows the winning graph. Is called at the end of the game, or whenever
     * the "View Resource Graph" button is pressed while a game is open.
     *
     * @param bc is a barchart object with relevant windata
     */
    public void winGraph(BarChart bc) {
        Stage graphstage = new Stage();
        graphstage.setTitle("Win Graph. As the tutor, you can see all your student's data.");

        Scene scene = new Scene(bc, 800, 600);

        graphstage.setScene(scene);
        graphstage.initModality(Modality.APPLICATION_MODAL);
        graphstage.showAndWait();
    }

    /**
     * Saves the log to a text file.
     */
    public void saveLog(String name) {
        try {
            File file = new File("../../../Log/log_" + name + ".txt"); //
            //File file = new File("./Log/log.txt");
            file.getParentFile().mkdirs();
            file.createNewFile();

            PrintWriter out = new PrintWriter(file);
            out.print(txtLog.getText());
            out.flush();
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(FXMLServerController.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Asks the tutor if students who did not turn up to class should be
     * 'killed'. The state of death in this game means not being able to take
     * action.
     *
     * @return true if the tutor said yes or false if the tutor said no.
     */
    public boolean killMissingPlayers() {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Not all players have joined.");
        alert.setHeaderText("If you click ok, the players who have not joined will be treated as dead.");
        alert.setContentText("Are you ok with this?");

        ButtonType buttonTypeYes = new ButtonType("Yes");
        ButtonType buttonTypeNo = new ButtonType("No", ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeYes, buttonTypeNo);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == buttonTypeYes) {
            return true;
        } else {
            return false;
        }
    }

    public void writeTxtLog(String text) {
        if (!text.trim().equals("")) {
            LocalDateTime ldt = LocalDateTime.now();

            String day = ldt.toLocalDate().toString();
            String time = ldt.getHour() + ":" + ldt.getMinute() + ":" + ldt.getSecond();
            String timestamp = "[" + day + " " + time + "]";

            if (room_ui_handler != null) {
                if (room_ui_handler.game_controller != null) {
                    txtLog.appendText(timestamp + " " + "[Round " + (room_ui_handler.game_controller.current_round + 1) + "] " + text + "\n");
                }
            }
        } else {
            txtLog.appendText("\n");
        }
    }

    public void writeTxtProposal(String name, String description) {
        txtProposalName.setText(name);
        txtProposal.setText(description);
    }

    public void notifyProposal() {
        lblNotify.setText("Proposal Received");
        btnReject.setDisable(false);
        btnAccept.setDisable(false);
    }

    private void resetProposalPanel() {
        txtProposalName.setText("");
        txtProposal.setText("");

        lblNotify.setText("");

        btnReject.setDisable(true);
        btnAccept.setDisable(true);
    }

    @FXML
    private void handleBtnReject(ActionEvent event) {
        resetProposalPanel();

        TextInputDialog dialog = new TextInputDialog("");
        dialog.setTitle("Reject");
        dialog.setHeaderText("Would you like to enter an optional short comment?");
        dialog.setContentText("Comment here: ");
        dialog.initModality(Modality.APPLICATION_MODAL);

        Optional<String> result = dialog.showAndWait();
        String comment = "";
        if (result.isPresent()) {
            comment = result.get();
        }

        room_ui_handler.game_controller.customReject(comment);
    }

    @FXML
    private void handleBtnAccept(ActionEvent event) {
        try {
            //custom_ui_handler.acceptProposal(room_ui_handler, txtFinance, txtExplore, txtExploit, txtChange, txtProposal, txtProposalName);
            //OPEN NEW WINDOW
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLProposal.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);

            stage.setOnCloseRequest((close_event) -> System.exit(1)); //Temporary

            FXMLProposalController controller = loader.<FXMLProposalController>getController();

            ArrayList<Decision> decision_checker = new ArrayList<>();
            //PASS TITLE AND DESCRIPTION OVER
            controller.init(txtProposalName.getText(), txtProposal.getText(), decision_checker, room_ui_handler.game_controller.findFreeDecisionID(), room_ui_handler.decisions);
            stage.showAndWait();

            //RECEIVE EITHER COMPLETED DECISION OR CANCELLATION
            boolean accepted;
            String dialogtitle;
            if (decision_checker.isEmpty()) {
                accepted = false;
                dialogtitle = "Reject";
            } else {
                accepted = true;
                dialogtitle = "Accept";
            }

            TextInputDialog dialog = new TextInputDialog("");
            dialog.setTitle(dialogtitle);
            dialog.setHeaderText("Would you like to enter an optional short comment?");
            dialog.setContentText("Comment here: ");
            dialog.initModality(Modality.APPLICATION_MODAL);

            Optional<String> result = dialog.showAndWait();
            String comment = "";
            if (result.isPresent()) {
                comment = result.get();
            }

            if (accepted) {
                room_ui_handler.game_controller.customAccept(comment, decision_checker.get(0));
            } else {
                room_ui_handler.game_controller.customReject(comment);
            }
        } catch (IOException ex) {
            Logger.getLogger(FXMLServerController.class.getName()).log(Level.SEVERE, null, ex);
        }
        resetProposalPanel();
    }

    public EventHandler<MouseEvent> onMousePressedHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            orgSceneX = event.getSceneX();
            orgSceneY = event.getSceneY();
            orgLayoutX = ((Shape) (event.getSource())).getLayoutX();
            orgLayoutY = ((Shape) (event.getSource())).getLayoutY();
        }

    };

    public EventHandler<MouseEvent> onMouseDraggedHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            double offsetX = event.getSceneX() - orgSceneX;
            double offsetY = event.getSceneY() - orgSceneY;
            double newLayoutX = orgLayoutX + offsetX;
            double newLayoutY = orgLayoutY + offsetY;

            if (newLayoutX > paneCanvas.getWidth() - 30) {
                newLayoutX = paneCanvas.getWidth() - 30;
            } else if (newLayoutX < 30) {
                newLayoutX = 30;
            }

            if (newLayoutY > paneCanvas.getHeight() - 30) {
                newLayoutY = paneCanvas.getHeight() - 30;
            } else if (newLayoutY < 30) {
                newLayoutY = 30;
            }

            ((Shape) (event.getSource())).setLayoutX(newLayoutX);
            ((Shape) (event.getSource())).setLayoutY(newLayoutY);

            Label attLabel = shape_to_label.get(((Shape) event.getSource()));
            attLabel.setLayoutX(newLayoutX - (30 / 2));
            attLabel.setLayoutY(newLayoutY - (30 / 2));

            redrawNetwork();
        }
    };

    public EventHandler<MouseEvent> onMouseEnteredHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            Platform.runLater(() -> {
                drawStats((Shape) event.getSource());
            });

        }
    };

    public EventHandler<MouseEvent> onMouseExitedHandler
            = new EventHandler<MouseEvent>() {

        @Override
        public void handle(MouseEvent event) {
            Platform.runLater(() -> {
                clearStats();
            });
        }
    };

}
