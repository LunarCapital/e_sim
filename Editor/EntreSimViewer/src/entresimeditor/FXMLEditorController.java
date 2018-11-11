/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor;

import entreObj.Decision;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entresimeditor.Objects.Data.Effect;
import entresimeditor.Objects.Data.EffectSet;
import entresimeditor.editorservices.EditorDecisionServicer;
import java.io.BufferedReader;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Control;
import javafx.scene.control.TableCell;
import javafx.scene.text.Text;

/**
 * A window that handles the creation of a new decision in response to a
 * student's proposal. It's not too complicated but there's lots and lots of
 * information to add.
 *
 * Based off the description given by the student, the tutor has the options to
 * set effects for the applicant, recipient, and both.
 *
 * These effects can change finance, exploratory&exploitative knowledge, and
 * income. Each effect has a value and a delay (ie finance decreases by 5 with a
 * delay of 2 turns).
 *
 * An entire set of effects has a probability attached to it that can also be
 * modified. If it is necessary to have effects with different probabilities,
 * they can be placed in different "sets".
 *
 * @author dylanleong
 */
public class FXMLEditorController implements Initializable {

    @FXML
    private TextArea txtProposal;
    @FXML
    private TextField txtProposalName;
    @FXML
    private RadioButton radioIndiv;
    @FXML
    private ToggleGroup EffectType;
    @FXML
    private RadioButton radioJoint;
    @FXML
    private ComboBox<String> cmbAffected;
    @FXML
    private TextField txtProbability;
    @FXML
    private ComboBox<Integer> cmbSets;
    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private TextField txtValue;
    @FXML
    private TextField txtDelay;
    @FXML
    private Button btnAdd;
    @FXML
    private Button btnNewSet;
    @FXML
    private Button btnDeleteSet;
    @FXML
    private TableView<Effect> tableEffects;
    @FXML
    private TableColumn<Effect, String> clmCategory;
    @FXML
    private TableColumn<Effect, Integer> clmValue;
    @FXML
    private TableColumn<Effect, Integer> clmDelay;
    @FXML
    private TableColumn<Effect, Integer> clmLinked;
    @FXML
    private Button btnRemove;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnDone;
    @FXML
    private Button btnSetProbability;
    @FXML
    private CheckBox checkLinked;

    ArrayList<Decision> decision_checker;
    TreeSet<Decision> decisions;
    ArrayList<Integer> must;
    ArrayList<Integer> not;
    int[] min_resources;
    boolean[] applicant_roles;
    boolean[] recipient_roles;
    boolean jointventure;
    int id;

    ObservableList<Integer> applicant_set;
    ObservableList<Integer> recipient_set;
    ObservableList<Integer> both_set;

    HashMap<Integer, EffectSet> applicant_map;
    HashMap<Integer, EffectSet> recipient_map;
    HashMap<Integer, EffectSet> both_map;

    int applicant_incrementing;
    int recipient_incrementing;
    int both_incrementing;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        must = new ArrayList<Integer>();
        not = new ArrayList<Integer>();

        min_resources = new int[3];
        min_resources[0] = 0; //finance
        min_resources[1] = 0; //explore
        min_resources[2] = 0; //exploit

        applicant_roles = new boolean[3];
        applicant_roles[0] = true; //R&D
        applicant_roles[1] = true; //MANUFACTURER
        applicant_roles[2] = true; //FINANCIAL BACKER

        recipient_roles = new boolean[3];
        recipient_roles[0] = true; //R&D
        recipient_roles[1] = true; //MANUFACTURER
        recipient_roles[2] = true; //FINANCIAL BACKER

        applicant_set = FXCollections.observableArrayList();
        recipient_set = FXCollections.observableArrayList();
        both_set = FXCollections.observableArrayList();

        applicant_map = new HashMap<Integer, EffectSet>();
        recipient_map = new HashMap<Integer, EffectSet>();
        both_map = new HashMap<Integer, EffectSet>();

        applicant_incrementing = 0;
        recipient_incrementing = 0;
        both_incrementing = 0;

        clmCategory.setCellValueFactory(
                new PropertyValueFactory<>("category"));
        clmValue.setCellValueFactory(
                new PropertyValueFactory<>("value"));
        clmDelay.setCellValueFactory(
                new PropertyValueFactory<>("delay"));
        clmLinked.setCellValueFactory(
                new PropertyValueFactory<>("linked"));

        final ToggleGroup group = new ToggleGroup();
        radioIndiv.setToggleGroup(group);
        radioJoint.setToggleGroup(group);

        cmbAffected.setItems(FXCollections.observableArrayList(
                "Applicant")
        );

        cmbCategory.setItems(FXCollections.observableArrayList(
                "Finance", "Exploratory Knowledge", "Exploitative Knowledge", "Income", "Exploratory Knowledge per Turn", "Exploitative Knowledge per Turn")
        );

        clmCategory.setCellFactory(tc -> { //
            TableCell<Effect, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(clmCategory.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });
    }

    public void initAdd(ArrayList<Decision> decision_checker, int id, TreeSet<Decision> decisions) {
        this.decision_checker = decision_checker;
        this.id = id;
        this.decisions = decisions;
        this.jointventure = false;
    }

    public void initEdit(ArrayList<Decision> decision_checker, int id, TreeSet<Decision> decisions) {
        this.decision_checker = decision_checker;
        this.id = id;
        this.decisions = decisions;

        EditorDecisionServicer editor_reader = new EditorDecisionServicer();
        Decision decision_to_be_edited = editor_reader.getDecision(decisions, id);

        //POPULATE UI WITH ALL OF THE DECISIONS DETAILS
        txtProposalName.setText(decision_to_be_edited.title);

        String truncated_description = truncateDescription(decision_to_be_edited.description);
        txtProposal.setText(truncated_description);

        if (decision_to_be_edited.type.equals("Indiv")) {
            radioIndiv.fire();
        } else {
            radioJoint.fire();
        }

        //EFFECTS
        //for each string in each effect arraylist, create an effect set
        //fill effect set with effects
        if (decision_to_be_edited.type.equals("Indiv")) {
            editor_reader.fillEffects(decision_to_be_edited.applicant_effects, applicant_set, applicant_map);
            applicant_incrementing = applicant_set.size();
            cmbAffected.getSelectionModel().selectFirst();
            cmbSets.setItems(applicant_set); //Sets cmbSets so we don't see an empty screen
        } else {
            editor_reader.fillEffects(decision_to_be_edited.applicant_effects, applicant_set, applicant_map);
            applicant_incrementing = applicant_set.size();
            editor_reader.fillEffects(decision_to_be_edited.recipient_effects, recipient_set, recipient_map);
            recipient_incrementing = recipient_set.size();
            editor_reader.fillEffects(decision_to_be_edited.both_effects, both_set, both_map);
            both_incrementing = both_set.size();
            cmbAffected.getSelectionModel().selectFirst();
            cmbSets.setItems(applicant_set); //Sets cmbSets so we don't see an empty screen
        }

        //PREREQS
        boolean join = editor_reader.fillPrereqs(decision_to_be_edited.prereqs, must, not, min_resources, applicant_roles, recipient_roles);
        if (join) {
            Alert alert = new Alert(AlertType.INFORMATION);
            alert.setHeaderText("This is the joint venture decision.");
            alert.setContentText("This decision is the joint venture decision.  Note that it has a specific set of requirements that cannot be edited by this program yet.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            this.jointventure = true;
        }

    }

    @FXML
    private void handleRadioIndiv(ActionEvent event) {
        cmbAffected.setItems(FXCollections.observableArrayList(
                "Applicant")
        );
        cmbAffected.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleRadioJoint(ActionEvent event) {
        cmbAffected.setItems(FXCollections.observableArrayList(
                "Applicant", "Recipient", "Both")
        );
        cmbAffected.getSelectionModel().selectFirst();
    }

    @FXML
    private void handleCmbAffected(ActionEvent event) {
        String select_affected = cmbAffected.getSelectionModel().getSelectedItem();

        if (select_affected == null) {
            btnNewSet.setDisable(true);
            return;
        }

        if (select_affected.equals("Applicant")) { //Switch Effect Set Array to "Applicant"
            cmbSets.setItems(applicant_set);
        } else if (select_affected.equals("Recipient")) { //Switch Effect Set Array to "Recipient"
            cmbSets.setItems(recipient_set);
        } else if (select_affected.equals("Both")) { //Switch Effect Set Array to "Both"
            cmbSets.setItems(both_set);
        }

        if (cmbSets.getItems().size() > 0) {
            cmbSets.getSelectionModel().selectFirst();
        } else {
            tableEffects.setItems(null);
        }
        btnNewSet.setDisable(false);
    }

    @FXML
    private void handleCmbSets(ActionEvent event) {
        //Change tableview accordingly
        String affected = cmbAffected.getSelectionModel().getSelectedItem();

        if (affected == null || cmbSets.getSelectionModel().getSelectedItem() == null) {
            txtProbability.setDisable(true);
            btnSetProbability.setDisable(true);
            cmbCategory.setDisable(true);
            txtValue.setDisable(true);
            txtDelay.setDisable(true);
            btnAdd.setDisable(true);
            btnDeleteSet.setDisable(true);
            return;
        }

        int set_index = cmbSets.getSelectionModel().getSelectedItem();
        if (affected.equals("Applicant")) {
            if (applicant_map.containsKey(set_index)) {
                tableEffects.setItems(applicant_map.get(set_index).effect_list);
                txtProbability.setText("" + applicant_map.get(set_index).probability);
            }
        } else if (affected.equals("Recipient")) {
            if (recipient_map.containsKey(set_index)) {
                tableEffects.setItems(recipient_map.get(set_index).effect_list);
                txtProbability.setText("" + recipient_map.get(set_index).probability);
            }
        } else if (affected.equals(
                "Both")) {
            if (both_map.containsKey(set_index)) {
                tableEffects.setItems(both_map.get(set_index).effect_list);
                txtProbability.setText("" + both_map.get(set_index).probability);
            }
        }

        txtProbability.setDisable(false);
        btnSetProbability.setDisable(false);
        cmbCategory.setDisable(false);
        txtValue.setDisable(false);
        txtDelay.setDisable(false);
        btnAdd.setDisable(false);
        btnDeleteSet.setDisable(false);
    }

    @FXML
    private void handleAdd(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText("Unable to add effect.");

        int value;
        int delay;

        if (cmbCategory.getSelectionModel().getSelectedItem() == null) { //Cateogyr not selected
            alert.setContentText("Please select a category before attempting to add an effect.");
            alert.showAndWait();
            return;
        }
        if (txtValue.getText().trim().equals("")) { //Value empty
            alert.setContentText("Please ensure the value textbox contains a number.");
            alert.showAndWait();
            return;
        }
        if (txtDelay.getText().trim().equals("")) {//Delay empty
            alert.setContentText("Please ensure the delay textbox contains a number.");
            alert.showAndWait();
            return;
        }
        try {
            value = Integer.parseInt(txtValue.getText());
        } catch (NumberFormatException nfe) { //Value not a whole number
            alert.setContentText("Please ensure the value textbox contains a single integer.");
            alert.showAndWait();
            return;
        }
        try {
            delay = Integer.parseInt(txtDelay.getText());
        } catch (NumberFormatException nfe) { //Delay not a whole number
            alert.setContentText("Please ensure the delay textbox contains a single integer.");
            alert.showAndWait();
            return;
        }
        if ((delay = Integer.parseInt(txtDelay.getText())) < 0) {
            alert.setContentText("Please ensure the delay textbox contains an integer above or equal to zero.");
            alert.showAndWait();
            return;
        }

        boolean linked = checkLinked.isSelected();

        tableEffects.getItems().add(new Effect(cmbCategory.getSelectionModel().getSelectedItem(), value, delay, linked));
    }

    @FXML
    private void handleBtnNewSet(ActionEvent event) {
        String affected = cmbAffected.getSelectionModel().getSelectedItem();
        if (affected.equals("Applicant")) {
            cmbSets.getItems().add(applicant_incrementing);
            applicant_map.put(applicant_incrementing, new EffectSet());

            applicant_incrementing++;
        } else if (affected.equals("Recipient")) {
            cmbSets.getItems().add(recipient_incrementing);
            recipient_map.put(recipient_incrementing, new EffectSet());

            recipient_incrementing++;
        } else if (affected.equals("Both")) {
            cmbSets.getItems().add(both_incrementing);
            both_map.put(both_incrementing, new EffectSet());

            both_incrementing++;
        }

        txtProbability.setText("1.0");
        cmbSets.getSelectionModel().selectLast();
    }

    @FXML
    private void handleBtnDeleteSet(ActionEvent event) {
        ObservableList<Integer> current_set = cmbSets.getItems();
        int set_index = cmbSets.getSelectionModel().getSelectedItem();
        String affected = cmbAffected.getSelectionModel().getSelectedItem();

        if (affected == null) {
            return;
        }

        HashMap<Integer, EffectSet> selected_map = new HashMap<Integer, EffectSet>();
        if (affected.equals("Applicant")) {
            selected_map = applicant_map;
        } else if (affected.equals("Recipient")) {
            selected_map = recipient_map;
        } else if (affected.equals("Both")) {
            selected_map = both_map;
        }

        for (Effect each_effect : selected_map.get(set_index).effect_list) {
            if (each_effect.equals("Joint Venture Effect") || each_effect.equals("Joint Grant Effect")) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Cannot delete this set.");
                alert.setContentText("This set contains the Joint Venture Effect or Joint Grant Effect effect and cannot be removed.");
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
                return;
            }
        }

        if (selected_map.containsKey(set_index)) {
            selected_map.remove(set_index);
        }

        current_set.remove(new Integer(set_index));
        cmbSets.getSelectionModel().selectLast();
    }

    @FXML
    private void handleBtnCancel(ActionEvent event) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cancel");
        alert.setContentText("Are you sure you want to cancel this custom decision?");
        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        } else {
            return;
        }
    }

    @FXML
    private void handleBtnDone(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText("Unable to add decision.");

        if (jointventure) {
            alert.setContentText("You cannot make changes to the joint venture decision as of yet.");
            alert.showAndWait();
            return;
        }

        //ID
        //Obtainable from init()
        //title
        if (txtProposalName.getText().trim().equals("")) {
            alert.setContentText("Please make sure the decision has a name.");
            alert.showAndWait();
            return;
        }
        String title = txtProposalName.getText();

        //description
        if (txtProposal.getText().trim().equals("")) {
            alert.setContentText("Please make sure the decision has a description.");
            alert.showAndWait();
            return;
        }

        txtProposal.getText().replace('\"', '\'');
        StringBuilder description = new StringBuilder("\"" + txtProposal.getText());

        //type
        String type;
        if (radioIndiv.isSelected()) {
            type = "Indiv";
        } else {
            type = "Joint";
        }

        //prereqs
        ArrayList<String> prereqs = new ArrayList<>();
        prereqs.addAll(rolePrereqs()); //Adds role prereqs
        prereqs.addAll(minResourcePrereqs());
        prereqs.addAll(mustPrereqs());
        prereqs.addAll(notPrereqs());
        if (prereqs.isEmpty()) {
            prereqs.add("none");
        }

        //applicant_effects
        Collection<EffectSet> applicant_values = applicant_map.values();
        ArrayList<String> applicant_effects = effectParser(applicant_values);
        System.out.println("Applicant: " + applicant_effects);

        //recipient_effects
        ArrayList<String> recipient_effects = new ArrayList<>();
        if (radioIndiv.isSelected()) {
            recipient_effects.add("");
        } else {
            Collection<EffectSet> recipient_values = recipient_map.values();
            recipient_effects = effectParser(recipient_values);
            System.out.println("Recipient: " + recipient_effects);
        }

        //both_effects
        ArrayList<String> both_effects = new ArrayList<>();
        if (radioIndiv.isSelected()) {
            both_effects.add("");
        } else {
            Collection<EffectSet> both_values = both_map.values();
            both_effects = effectParser(both_values);
            System.out.println("Both: " + both_effects);
        }

        description.append("\"\n");

        //Make a decision and add to list
        Decision new_decision = new Decision(id, txtProposalName.getText(), description.toString(), type, prereqs, applicant_effects, recipient_effects, both_effects);
        decision_checker.add(new_decision);

        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBtnRemove(ActionEvent event) {
        Effect remove_effect = tableEffects.getSelectionModel().getSelectedItem();
        if (remove_effect == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not remove Effect.");
            alert.setContentText("Please select an effect in the table before attempting to remove it.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        } else {
            if (remove_effect.getCategory().equals("Joint Venture Effect") || remove_effect.getCategory().equals("Joint Grant Effect")) {
                Alert alert = new Alert(AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Could not remove Effect.");
                alert.setContentText("You cannot remove the Joint Venture Effect and Joint Grant Effect effects.");
                alert.initModality(Modality.APPLICATION_MODAL);
                alert.showAndWait();
                return;
            }

            tableEffects.getItems().remove(remove_effect);
        }
    }

    @FXML
    private void handlSetProbability(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.setHeaderText("Unable to set probability.");
        alert.initModality(Modality.APPLICATION_MODAL);

        double new_probability;

        if (txtProbability.getText().trim().equals("")) { //Text box empty
            alert.setContentText("Please ensure the probability text box is not empty and contains a number between 0 and 1.");
            alert.showAndWait();
            return;
        }
        try {
            new_probability = Double.parseDouble(txtProbability.getText());
        } catch (NumberFormatException nfe) { //Text box does not contain double
            alert.setContentText("Currently the probability text box does not contain a number.  Please ensure it does not have letters, symbols, or spaces.");
            alert.showAndWait();
            return;
        }
        if (new_probability > 1 || new_probability < 0) { //Text box contains double but not between 0 and 1
            alert.setContentText("Please ensure the probability is between 0 and 1.");
            alert.showAndWait();
            return;
        }

        //No problems, go ahead and change the probability of the effect set.
        String selected = cmbAffected.getSelectionModel().getSelectedItem();

        if (selected == null) { //Should genuinely never happen, but just in case it  does
            alert.setContentText("No affected player is selected.");
            alert.showAndWait();
            return;
        }

        if (selected.equals("Applicant")) {
            applicant_map.get(cmbSets.getSelectionModel().getSelectedItem()).probability = new_probability;
        } else if (selected.equals("Recipient")) {
            recipient_map.get(cmbSets.getSelectionModel().getSelectedItem()).probability = new_probability;
        } else if (selected.equals("Both")) {
            both_map.get(cmbSets.getSelectionModel().getSelectedItem()).probability = new_probability;
        }
    }

    /**
     * Converts all effect sets to String format.
     *
     * @param role_values a collection of effect sets
     * @return An ArrayList of strings, where each string corresponds to one
     * set.
     */
    private ArrayList<String> effectParser(Collection<EffectSet> role_values) {
        ArrayList<String> role_effects = new ArrayList<>();
        for (EffectSet each_set : role_values) {
            String effect_str = "P " + each_set.probability;
            if (each_set.effect_list.isEmpty()) {
                continue; //Don't bother adding anything from an empty set
            }
            for (Effect each_effect : each_set.effect_list) {
                String category;
                switch (each_effect.getCategory()) {
                    case "Finance":
                        category = "finance";
                        break;
                    case "Exploratory Knowledge":
                        category = "explore";
                        break;
                    case "Exploitative Knowledge":
                        category = "exploit";
                        break;
                    case "Income":
                        category = "f_change";
                        break;
                    case "Exploratory Knowledge per Turn":
                        category = "explore_change";
                        break;
                    case "Exploitative Knowledge per Turn":
                        category = "exploit_change";
                        break;
                    case "Joint Venture Effect":
                        category = "join";
                        break;
                    case "Joint Grant Effect":
                        category = "jointgrant";
                        break;
                    default:
                        category = "finance"; //default, should never be used.
                        break;
                }
                if (!(category.equals("join") || category.equals("jointgrant"))) {
                    effect_str += " " + category + " " + each_effect.getValue() + " " + each_effect.getDelay() + " " + String.valueOf(each_effect.getLinked()).toLowerCase();
                } else {
                    effect_str = category;
                }
                
            }
            //should have format P Double CATEGORY1 VALUE1 DELAY1 FALSE1 CATEGORY2 VALUE2 DELAY2 FALSE2...
            role_effects.add(effect_str);
        }
        if (role_effects.isEmpty()) {
            role_effects.add("finance 0 0 false");
        }
        return role_effects;
    }

    @FXML
    private void handleBtnRole(ActionEvent event) {
        ArrayList<ArrayList<String>> share = new ArrayList<ArrayList<String>>();
        //Prerequisites are placed into the above share in this order:  ROLE RESTRICTIONS, MINIMUM RESOURCE REQS, MUST, NOT
        boolean individual = radioIndiv.isSelected();
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLPrerequisiteSetter.fxml"));
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene((Pane) loader.load()));
            stage.setResizable(false);

            FXMLPrerequisiteSetterController controller = loader.<FXMLPrerequisiteSetterController>getController();
            controller.init(must, not, min_resources, applicant_roles, recipient_roles, share, individual, decisions);

            stage.setOnCloseRequest(evt -> {
                evt.consume();

                Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
                alert.initModality(Modality.APPLICATION_MODAL);
                if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
                    stage.close();
                }
            });

            stage.showAndWait();
            if (!share.isEmpty()) { //IF PREREQS ALLOCATED (aka window not cancelled)
                ArrayList<String> role_reqs = share.get(0);

                applicant_roles[0] = role_reqs.contains("AR");
                applicant_roles[1] = role_reqs.contains("AM");
                applicant_roles[2] = role_reqs.contains("AF");

                if (!individual) {
                    recipient_roles[0] = role_reqs.contains("RR");
                    recipient_roles[1] = role_reqs.contains("RM");
                    recipient_roles[2] = role_reqs.contains("RF");
                }

                ArrayList<String> min_resource_reqs = share.get(1);
                min_resources[0] = Integer.valueOf(min_resource_reqs.get(0));
                min_resources[1] = Integer.valueOf(min_resource_reqs.get(1));
                min_resources[2] = Integer.valueOf(min_resource_reqs.get(2));

                ArrayList<String> must_reqs = share.get(2);
                must = new ArrayList<Integer>(); //RESET THE LIST so we don't double-add, or add previously 'cancelled' decisions
                for (String each : must_reqs) {
                    must.add(Integer.valueOf(each));
                }

                ArrayList<String> not_reqs = share.get(3);
                not = new ArrayList<Integer>(); //RESET EXISTING LIST
                for (String each : not_reqs) {
                    not.add(Integer.valueOf(each));
                }

            }
        } catch (IOException ioe) {

        }
    }

    /**
     * Checks the applicant and recipient role arrays, returns an arraylist of
     * strings corresponding to their respective prereqs. Super messy boolean
     * logic, apologies to anybody who has to go through this (this includes
     * myself).
     *
     * @return An arraylist containing role prereqs for an applicant and a
     * recipient if joint decision.
     */
    private ArrayList<String> rolePrereqs() {
        ArrayList<String> role_prereqs = new ArrayList<String>();

        //a_role = applicant must be this role
        //r_role = recipient must be this role
        //a_n_role = applicant must NOT be this role
        //r_n_role = recipient must NOT be this role
        //APPLICANT FIRST
        if (applicant_roles[0] && applicant_roles[1] && applicant_roles[2]) { //applicant can be any role
            //no prereq to add
        } else if (applicant_roles[0] && applicant_roles[1] && !applicant_roles[2]) { //applicant CANNOT be F
            role_prereqs.add("a_n_role F");
        } else if (applicant_roles[0] && !applicant_roles[1] && applicant_roles[2]) { //applicant CANNOT be M
            role_prereqs.add("a_n_role M");
        } else if (!applicant_roles[0] && applicant_roles[1] && applicant_roles[2]) { //applicant CANNOT be R
            role_prereqs.add("a_n_role R");
        } else if (applicant_roles[0] && !applicant_roles[1] && !applicant_roles[2]) { //applicant MUST be R
            role_prereqs.add("a_role R");
        } else if (!applicant_roles[0] && applicant_roles[1] && !applicant_roles[2]) { //applicant MUST be M
            role_prereqs.add("a_role M");
        } else if (!applicant_roles[0] && !applicant_roles[1] && applicant_roles[2]) { //applicant MUST be F
            role_prereqs.add("a_role F");
        }

        //ONLY DO RECIPIENT IF DECISION IS JOINT
        if (!radioIndiv.isSelected()) { //JOINT
            if (recipient_roles[0] && recipient_roles[1] && recipient_roles[2]) { //applicant can be any role
                //no prereq to add
            } else if (recipient_roles[0] && recipient_roles[1] && !recipient_roles[2]) { //applicant CANNOT be F
                role_prereqs.add("r_n_role F");
            } else if (recipient_roles[0] && !recipient_roles[1] && recipient_roles[2]) { //applicant CANNOT be M
                role_prereqs.add("r_n_role M");
            } else if (!recipient_roles[0] && recipient_roles[1] && recipient_roles[2]) { //applicant CANNOT be R
                role_prereqs.add("r_n_role R");
            } else if (recipient_roles[0] && !recipient_roles[1] && !recipient_roles[2]) { //applicant MUST be R
                role_prereqs.add("r_role R");
            } else if (!recipient_roles[0] && recipient_roles[1] && !recipient_roles[2]) { //applicant MUST be M
                role_prereqs.add("r_role M");
            } else if (!recipient_roles[0] && !recipient_roles[1] && recipient_roles[2]) { //applicant MUST be F
                role_prereqs.add("r_role F");
            }
        }

        return role_prereqs;
    }

    /**
     * Checks the min resource array and adds prerequisites accordingly.
     *
     * @return an ArrayList<String> that contains the prereqs in the correct
     * format.
     */
    private ArrayList<String> minResourcePrereqs() {
        ArrayList<String> min_resource_prereqs = new ArrayList<String>();

        if (min_resources[0] > 0) { //finance
            min_resource_prereqs.add("finance " + min_resources[0]);
        }

        if (min_resources[1] > 0) { //explore
            min_resource_prereqs.add("explore " + min_resources[1]);
        }

        if (min_resources[1] > 0) { //exploit
            min_resource_prereqs.add("exploit " + min_resources[2]);
        }

        return min_resource_prereqs;
    }

    private ArrayList<String> mustPrereqs() {
        ArrayList<String> must_prereqs = new ArrayList<String>();

        for (Integer each : must) {
            must_prereqs.add("D " + each);
        }
        return must_prereqs;
    }

    private ArrayList<String> notPrereqs() {
        ArrayList<String> not_prereqs = new ArrayList<String>();

        for (Integer each : not) {
            not_prereqs.add("N " + each);
        }
        return not_prereqs;
    }

    private String truncateDescription(String description) {
        BufferedReader br = new BufferedReader(new StringReader(description));
        StringBuilder sb = new StringBuilder("");
        String line = "";

        try {
            while ((line = br.readLine()) != null) {
                if (line.equals("Applicant Effects: ")) {
                    break;
                }
                line = line.replace("\"", "");
                sb.append(line);

            } //endwhile
        } catch (IOException ex) {
            Logger.getLogger(FXMLEditorController.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sb.toString();
    }

}
