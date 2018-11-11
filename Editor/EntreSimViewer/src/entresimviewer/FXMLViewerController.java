/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer;

import entreObj.Decision;
import entresimeditor.FXMLConditionalEditorController;
import entresimeditor.FXMLEditorController;
import entresimviewer.Objects.Data.ConditionalData;
import entresimviewer.Objects.Data.Switch;
import entresimviewer.Objects.Readers.ConditionalReader;
import entresimviewer.Objects.Readers.DataReader;
import entresimviewer.Objects.Readers.SwitchReader;
import entresimviewer.Objects.View.ListSwitch;
import entresimviewer.Objects.View.ListDecision;
import entresimviewer.Objects.View.ViewEffect;
import entresimviewer.viewerservices.DecisionServicer;
import entresimviewer.viewerservices.FileSaver;
import entresimviewer.viewerservices.SwitchConditionalServicer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.ResourceBundle;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.ListView;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author dylanleong
 */
public class FXMLViewerController implements Initializable {

    @FXML
    private TextField txtTitle;
    @FXML
    private TextArea txtDescription;
    @FXML
    private TextField txtType;
    @FXML
    private ListView<String> listPrereqs;
    @FXML
    private TableView<ViewEffect> tableEffects;
    @FXML
    private TableColumn<ViewEffect, String> colProbability;
    @FXML
    private TableColumn<ViewEffect, String> colAffected;
    @FXML
    private TableColumn<ViewEffect, String> colEffects;
    @FXML
    private ListView<ListDecision> listDecisions;

    @FXML
    private ListView<ListSwitch> listSwitches;
    @FXML
    private ComboBox<ListDecision> cmbConditional;
    @FXML
    private TextField txtSwitchName;
    @FXML
    private TextField txtSwitchDecision;
    @FXML
    private TableView<ViewEffect> tableEffectsOriginal;
    @FXML
    private TableColumn<ViewEffect, String> colProbabilityOriginal;
    @FXML
    private TableColumn<ViewEffect, String> colAffectedOriginal;
    @FXML
    private TableColumn<ViewEffect, String> colEffectsOriginal;
    @FXML
    private TableView<ViewEffect> tableEffectsSwitched;
    @FXML
    private TableColumn<ViewEffect, String> colProbabilitySwitched;
    @FXML
    private TableColumn<ViewEffect, String> colAffectedSwitched;
    @FXML
    private TableColumn<ViewEffect, String> colEffectsSwitched;

    TreeSet<Decision> decisions;
    ArrayList<Switch> switches;
    ArrayList<ConditionalData> conditional_data;
    String save_path;

    @Override
    public void initialize(URL url, ResourceBundle rb) {

    }

    public boolean startUpSuccess(Stage stage) {
        //Reroute error text to a log file
        try {
            PrintStream out = new PrintStream(new FileOutputStream("../../../error_log.txt"));
            System.setErr(out);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(FXMLViewerController.class.getName()).log(Level.SEVERE, null, ex);
        }

        //Get File Path
        String path = null;
        if ((path = getFilePath(stage)) == null) {
            return false;
        }
        save_path = path;

        //Load three files
        decisions = new TreeSet<Decision>(); //Read Decisions
        DataReader dr = new DataReader(decisions);
        dr.readData(path, "wireless_decisions");

        switches = new ArrayList<Switch>(); //Read switches
        SwitchReader sr = new SwitchReader(switches);
        sr.readSwitches(path, "conditionals_switch");

        conditional_data = new ArrayList<ConditionalData>(); //read conditional data
        ConditionalReader cr = new ConditionalReader(conditional_data, decisions);
        cr.readConditionals(path, "conditionals_changes");

        refreshDecisionsList();
        refreshSwitchesList();

        return true;
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

    @FXML
    private void handleListDecisionsSelect(MouseEvent event) {
        ListDecision selected = listDecisions.getSelectionModel().getSelectedItem();

        if (selected != null) {
            displayDecision(decisions, selected.id());
        }
    }

    @FXML
    private void handleListConditionalsSelect(MouseEvent event) {
        ListSwitch selected = listSwitches.getSelectionModel().getSelectedItem();

        if (selected != null) {
            displaySwitches(switches, conditional_data, selected.name().get());
        }
    }

    /**
     * Refreshes base GUI with all details of selected decision
     *
     * @param d_set set of decisions
     * @param decision_id id of decision we want to dipslay
     */
    public void displayDecision(TreeSet<Decision> d_set, int decision_id) {
        DecisionServicer decision_servicer = new DecisionServicer();

        Decision display_decision = decision_servicer.getDecision(d_set, decision_id);

        txtTitle.setText(display_decision.title);
        txtType.setText(display_decision.type);
        txtDescription.setText(display_decision.description);

        ArrayList<String> prereqs_readable = decision_servicer.convertPrereqsReadable(display_decision.prereqs, d_set);
        ObservableList<String> prereqs_list = FXCollections.observableArrayList(prereqs_readable);
        listPrereqs.setItems(prereqs_list);

        ArrayList<ViewEffect> effects_readable = new ArrayList<ViewEffect>();
        //Display effects
        if (display_decision.type.equals("Indiv")) { //INDIVIDUAL
            effects_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.applicant_effects, "Applicant"));
        } else { //JOINT
            effects_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.applicant_effects, "Applicant"));
            effects_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.recipient_effects, "Recipient"));
            effects_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.both_effects, "Both"));
        }

        ObservableList<ViewEffect> effects_list = FXCollections.observableArrayList(effects_readable);
        tableEffects.setItems(effects_list);
        colProbability.setCellValueFactory(new PropertyValueFactory("probability"));
        colAffected.setCellValueFactory(new PropertyValueFactory("affected"));
        colEffects.setCellValueFactory(new PropertyValueFactory("effects"));

        //Wrap text for long effects
        colEffects.setCellFactory(tc -> { //
            TableCell<ViewEffect, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colEffects.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    /**
     * Refreshes base UI with all the details of a selected switch. Mainly
     * called when the user selects a conditional to view with the conditional
     * list. Table effects are only viewed when the user selects an switched
     * decision via the combo box.
     *
     * @param switches list of switches
     * @param c_list arraylist of conditional data
     * @param conditional_name of selected switch to view
     */
    public void displaySwitches(ArrayList<Switch> switches, ArrayList<ConditionalData> c_list, String switch_name) {
        SwitchConditionalServicer switch_conditional_servicer = new SwitchConditionalServicer();
        DecisionServicer decision_servicer = new DecisionServicer();

        Switch switch_to_read = switch_conditional_servicer.getSwitch(switches, switch_name);
        Decision switch_decision = decision_servicer.getDecision(decisions, switch_to_read.getId());

        if (switch_decision == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.setContentText("The decision required to switch this conditional on is missing or has been deleted.  You can assign it a new switch decision by using the edit button below the conditional list.");
            alert.showAndWait();
            return;
        }

        txtSwitchName.setText(switch_name);
        txtSwitchDecision.setText(switch_decision.title);

        ArrayList<ListDecision> array_switched_decisions = new ArrayList<ListDecision>();
        for (ConditionalData each : conditional_data) {
            if (each.name.equals(switch_to_read.getName())) {
                Decision switched_decision = decision_servicer.getDecision(decisions, each.decision_id);
                ListDecision list_switched_decision = new ListDecision(switched_decision.title, switched_decision.id);
                if (!array_switched_decisions.contains(list_switched_decision)) {
                    array_switched_decisions.add(list_switched_decision);
                }
            }
        }
        ObservableList<ListDecision> switched_decisions = FXCollections.observableArrayList(array_switched_decisions);

        cmbConditional.setItems(switched_decisions);
    }

    @FXML
    private void handleCmbConditional(ActionEvent event) {
        ListDecision selected_decision = cmbConditional.getSelectionModel().getSelectedItem();

        if (selected_decision == null) {
            return;
        }

        DecisionServicer decision_servicer = new DecisionServicer();
        SwitchConditionalServicer switch_conditional_servicer = new SwitchConditionalServicer();
        Decision display_decision = decision_servicer.getDecision(decisions, selected_decision.id());

        ArrayList<ViewEffect> effects_original_readable = new ArrayList<ViewEffect>();
        //Display effects
        if (display_decision.type.equals("Indiv")) { //INDIVIDUAL
            effects_original_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.applicant_effects, "Applicant"));
        } else { //JOINT
            effects_original_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.applicant_effects, "Applicant"));
            effects_original_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.recipient_effects, "Recipient"));
            effects_original_readable.addAll(decision_servicer.convertEffectsReadable(display_decision.both_effects, "Both"));
        }

        ObservableList<ViewEffect> effects_original_list = FXCollections.observableArrayList(effects_original_readable);
        tableEffectsOriginal.setItems(effects_original_list);
        colProbabilityOriginal.setCellValueFactory(new PropertyValueFactory("probability"));
        colAffectedOriginal.setCellValueFactory(new PropertyValueFactory("affected"));
        colEffectsOriginal.setCellValueFactory(new PropertyValueFactory("effects"));

        //Wrap text for long effects
        colEffectsOriginal.setCellFactory(tc -> { //
            TableCell<ViewEffect, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colEffectsOriginal.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        ArrayList<ViewEffect> effects_switched_readable = new ArrayList<ViewEffect>();
        //Display effects
        if (display_decision.type.equals("Indiv")) { //INDIVIDUAL
            ArrayList<String> switched_applicant_effects = switch_conditional_servicer.changeConditional(display_decision.id, conditional_data, display_decision.applicant_effects, "applicant");
            effects_switched_readable.addAll(decision_servicer.convertEffectsReadable(switched_applicant_effects, "Applicant"));
        } else { //JOINT
            ArrayList<String> switched_applicant_effects = switch_conditional_servicer.changeConditional(display_decision.id, conditional_data, display_decision.applicant_effects, "applicant");
            System.out.println(switched_applicant_effects);
            ArrayList<String> switched_recipient_effects = switch_conditional_servicer.changeConditional(display_decision.id, conditional_data, display_decision.recipient_effects, "recipient");
            ArrayList<String> switched_both_effects = switch_conditional_servicer.changeConditional(display_decision.id, conditional_data, display_decision.both_effects, "both");
            effects_switched_readable.addAll(decision_servicer.convertEffectsReadable(switched_applicant_effects, "Applicant"));
            effects_switched_readable.addAll(decision_servicer.convertEffectsReadable(switched_recipient_effects, "Recipient"));
            effects_switched_readable.addAll(decision_servicer.convertEffectsReadable(switched_both_effects, "Both"));
        }

        ObservableList<ViewEffect> effects_switched_list = FXCollections.observableArrayList(effects_switched_readable);
        tableEffectsSwitched.setItems(effects_switched_list);
        colProbabilitySwitched.setCellValueFactory(new PropertyValueFactory("probability"));
        colAffectedSwitched.setCellValueFactory(new PropertyValueFactory("affected"));
        colEffectsSwitched.setCellValueFactory(new PropertyValueFactory("effects"));

        //Wrap text for long effects
        colEffectsSwitched.setCellFactory(tc -> { //
            TableCell<ViewEffect, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(colEffectsSwitched.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    /**
     * Refreshes the decisions list. Call every time we change the decisions
     * set, whether through addition, editing, or deletion of existing
     * decisions.
     */
    public void refreshDecisionsList() {
        ArrayList<ListDecision> array_list_decisions = new ArrayList<ListDecision>();
        for (Decision each : decisions) {
            array_list_decisions.add(new ListDecision(each.title, each.id));
        }

        ObservableList<ListDecision> list_decisions = FXCollections.observableArrayList(array_list_decisions);
        listDecisions.setItems(list_decisions);
    }

    /**
     * Refreshes the conditionals list.
     */
    public void refreshSwitchesList() {
        ArrayList<ListSwitch> array_list_switches = new ArrayList<ListSwitch>();
        for (Switch each : switches) {
            array_list_switches.add(new ListSwitch(each.getName()));
        }

        ObservableList<ListSwitch> list_switches = FXCollections.observableArrayList(array_list_switches);
        listSwitches.setItems(list_switches);
    }

    /**
     * Closes window AND saves decisions.
     *
     * @param event
     */
    @FXML
    private void handleBtnClose(ActionEvent event) {
        try {
            FileSaver fs = new FileSaver();

            File decisions_file = new File(save_path + "wireless_decisions");
            decisions_file.getParentFile().mkdirs();
            decisions_file.createNewFile();
            PrintWriter out = new PrintWriter(decisions_file);
            out.print(fs.decisionsToText(decisions));
            out.flush();
            out.close();

            File switches_file = new File(save_path + "conditionals_switch");
            switches_file.getParentFile().mkdirs();
            switches_file.createNewFile();
            out = new PrintWriter(switches_file);
            out.print(fs.switchesToText(switches));
            out.flush();
            out.close();

            File changes_file = new File(save_path + "conditionals_changes");
            changes_file.getParentFile().mkdirs();
            changes_file.createNewFile();
            out = new PrintWriter(changes_file);
            out.print(fs.conditionalsToText(conditional_data, decisions));
            out.flush();
            out.close();

        } catch (IOException ex) {
            System.out.println(ex.getMessage());
        }

        Stage stage = (Stage) txtTitle.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBtnAdd(ActionEvent event) {
        try { //Open up proposal window, everything should be empty
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/entresimeditor/FXMLEditor.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);
            FXMLEditorController controller = loader.<FXMLEditorController>getController();

            DecisionServicer decision_servicer = new DecisionServicer();
            ArrayList<Decision> decision_checker = new ArrayList<Decision>();
            int free_id = decision_servicer.findFreeDecisionID(decisions);

            controller.initAdd(decision_checker, free_id, decisions);

            stage.setOnCloseRequest(evt -> {
                evt.consume();

                Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    stage.close();
                }
            });
            stage.showAndWait();

            //WAIT FOR USER TO FINISH 
            if (!decision_checker.isEmpty()) {
                decisions.add(decision_checker.get(0));
                refreshDecisionsList();
            }

        } catch (IOException ex) {
            Logger.getLogger(FXMLViewerController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void handleBtnEdit(ActionEvent event) {
        ListDecision selected = listDecisions.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a decision to edit from the list.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            return;
        }

        try { //Open up proposal window, everything should be empty
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/entresimeditor/FXMLEditor.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);
            FXMLEditorController controller = loader.<FXMLEditorController>getController();

            ArrayList<Decision> decision_checker = new ArrayList<Decision>();

            controller.initEdit(decision_checker, selected.id(), decisions);

            stage.setOnCloseRequest(evt -> {
                evt.consume();

                Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    stage.close();
                }
            });
            stage.showAndWait();

            //WAIT FOR USER TO FINISH 
            if (!decision_checker.isEmpty()) {
                DecisionServicer decision_servicer = new DecisionServicer();
                Decision to_be_replaced = decision_servicer.getDecision(decisions, selected.id());

                System.out.println(decisions.contains(to_be_replaced));
                decisions.remove(to_be_replaced);

                decisions.add(decision_checker.get(0)); //and add the new one
                refreshDecisionsList(); //refresh list
                displayDecision(decisions, selected.id()); //update display
            }

        } catch (IOException ex) {
            Logger.getLogger(FXMLViewerController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void handleBtnDelete(ActionEvent event) {
        //Ask user if they're sure, then delete decision AND all references to decision
        ListDecision selected = listDecisions.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a decision to delete.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            return;
        } else {
            DecisionServicer decision_servicer = new DecisionServicer();
            if (decision_servicer.isDeletionForbidden(decisions, selected.id())) { //CHECK IF JOIN OR JOINGRANT
                Alert deletion_alert = new Alert(Alert.AlertType.ERROR);
                deletion_alert.setContentText("Cannot delete this decision, as it is either the joint grant or joint venture decision.");
                deletion_alert.initModality(Modality.APPLICATION_MODAL);
                deletion_alert.showAndWait();
                return;
            }

            Alert alert = new Alert(Alert.AlertType.NONE, "Are you sure you want to delete this decision?", ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.APPLICATION_MODAL);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                decision_servicer.deleteDecision(decisions, selected.id());//Delete decision
                refreshDecisionsList(); //refresh list
            }
        }
    }

    @FXML
    private void handleBtnConditionalAdd(ActionEvent event) {
        try { //Open up proposal window, everything should be empty
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/entresimeditor/FXMLConditionalEditor.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);
            FXMLConditionalEditorController controller = loader.<FXMLConditionalEditorController>getController();

            ArrayList<Switch> switch_checker = new ArrayList<Switch>();
            ArrayList<ConditionalData> conditional_checker = new ArrayList<ConditionalData>();

            controller.initAdd(switch_checker, conditional_checker, decisions);

            stage.setOnCloseRequest(evt -> {
                evt.consume();

                Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    stage.close();
                }
            });
            stage.showAndWait();

            if (!switch_checker.isEmpty()) {
                switches.add(switch_checker.get(0));
                conditional_data.addAll(conditional_checker);
                refreshSwitchesList();
            }

        } catch (IOException ex) {
            Logger.getLogger(FXMLViewerController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void handleBtnConditionalEdit(ActionEvent event) {
        ListSwitch selected = listSwitches.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a switch to edit from the list.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            return;
        }

        try { //Open up proposal window, everything should be empty
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/entresimeditor/FXMLConditionalEditor.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);
            FXMLConditionalEditorController controller = loader.<FXMLConditionalEditorController>getController();

            SwitchConditionalServicer sc_servicer = new SwitchConditionalServicer();
            ArrayList<Switch> switch_checker = new ArrayList<Switch>();
            ArrayList<ConditionalData> conditional_checker = new ArrayList<ConditionalData>();
            Switch selected_switch = sc_servicer.getSwitch(switches, selected.name().get());

            controller.initEdit(switch_checker, conditional_checker, decisions, selected_switch, conditional_data);

            stage.setOnCloseRequest(evt -> {
                evt.consume();

                Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    stage.close();
                }
            });
            stage.showAndWait();

            if (!switch_checker.isEmpty()) {
                ArrayList<ConditionalData> old_conditionals = new ArrayList<ConditionalData>();
                for (ConditionalData each_conditional : conditional_data) {
                    if (each_conditional.name.equals(selected_switch.getName())) {
                        old_conditionals.add(each_conditional);
                    }
                }

                switches.remove(selected_switch);
                switches.add(switch_checker.get(0));

                conditional_data.removeAll(old_conditionals);
                conditional_data.addAll(conditional_checker);

                refreshSwitchesList(); //refresh list
                displaySwitches(switches, conditional_data, switch_checker.get(0).getName()); //update display

            }

        } catch (IOException ex) {
            Logger.getLogger(FXMLViewerController.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println(ex.getMessage());
        }
    }

    @FXML
    private void handleBtnConditionalDelete(ActionEvent event) {
        ListSwitch selected = listSwitches.getSelectionModel().getSelectedItem();

        if (selected == null) {
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setContentText("Please select a switch to delete.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            return;
        } else {
            SwitchConditionalServicer sc_servicer = new SwitchConditionalServicer();

            Alert alert = new Alert(Alert.AlertType.NONE, "Are you sure you want to delete this switch along with all of its conditionals?", ButtonType.YES, ButtonType.NO);
            alert.initModality(Modality.APPLICATION_MODAL);
            if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                sc_servicer.deleteSwitch(switches, conditional_data, selected.name().get());//Delete decision
                refreshSwitchesList(); //refresh list
            }
        }
    }

}
