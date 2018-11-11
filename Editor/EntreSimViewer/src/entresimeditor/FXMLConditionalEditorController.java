/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor;

import entreObj.Decision;
import entresimeditor.Objects.Data.Effect;
import entresimeditor.Objects.Data.EditorListConditional;
import entresimeditor.Objects.Data.EffectSet;
import entresimeditor.editorservices.EditorDecisionServicer;
import entresimeditor.editorservices.EditorSwitchConditionalServicer;
import entresimviewer.Objects.Data.ConditionalData;
import entresimviewer.Objects.Data.Switch;
import entresimviewer.Objects.View.ListDecision;
import entresimviewer.Objects.View.ViewEffect;
import java.net.URL;
import java.util.ArrayList;
import java.util.Optional;
import java.util.ResourceBundle;
import java.util.TreeSet;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class The FXML for the conditional editor UI. Provides full
 * functionality to
 *
 * @author dylanleong
 */
public class FXMLConditionalEditorController implements Initializable {

    @FXML
    private TextField txtSwitchName;
    @FXML
    private ComboBox<ListDecision> cmbSwitchDecision;

    @FXML
    private ComboBox<EditorListConditional> cmbConditionals;
    @FXML
    private Button btnAddConditional;
    @FXML
    private Button btnDeleteConditional;

    @FXML
    private ComboBox<ListDecision> cmbDecisionToBeChanged;

    @FXML
    private ComboBox<String> cmbAffected;

    @FXML
    private RadioButton radioAdd;
    @FXML
    private RadioButton radioReplace;

    @FXML
    private Label lblTableTitle;
    @FXML
    private TableView<ViewEffect> tableOriginalEffects;
    @FXML
    private TableColumn<ViewEffect, String> colProbability;
    @FXML
    private TableColumn<ViewEffect, String> colAffected;
    @FXML
    private TableColumn<ViewEffect, String> colEffects;

    @FXML
    private TextField txtProbability;
    @FXML
    private Button btnSetProbability;

    @FXML
    private ComboBox<String> cmbCategory;
    @FXML
    private TextField txtValue;
    @FXML
    private TextField txtDelay;
    @FXML
    private CheckBox checkLinked;
    @FXML
    private Button btnAddEffect;

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
    private Button btnRemoveEffect;

    @FXML
    private Button btnDone;
    @FXML
    private Button btnCancel;

    //GLOBAL VARIABLES
    int count;
    ArrayList<Switch> switch_checker;
    ArrayList<ConditionalData> conditional_checker;
    TreeSet<Decision> decisions;
    String replaceable_name;
    //GLOBAL VARIABLES

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {

        //going to need massive groups of enables and disables
        final ToggleGroup conditional_type_group = new ToggleGroup();
        radioAdd.setToggleGroup(conditional_type_group);
        radioReplace.setToggleGroup(conditional_type_group);

        colProbability.setCellValueFactory(new PropertyValueFactory<>("probability"));
        colAffected.setCellValueFactory(new PropertyValueFactory<>("affected"));
        colEffects.setCellValueFactory(new PropertyValueFactory<>("effects"));

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

        cmbCategory.setItems(FXCollections.observableArrayList(
                "Finance", "Exploratory Knowledge", "Exploitative Knowledge", "Income", "Exploratory Knowledge per Turn", "Exploitative Knowledge per Turn")
        );

        clmCategory.setCellValueFactory(new PropertyValueFactory<>("category"));
        clmValue.setCellValueFactory(new PropertyValueFactory<>("value"));
        clmDelay.setCellValueFactory(new PropertyValueFactory<>("delay"));
        clmLinked.setCellValueFactory(new PropertyValueFactory<>("linked"));

        clmCategory.setCellFactory(tc -> { //
            TableCell<Effect, String> cell = new TableCell<>();
            Text text = new Text();
            cell.setGraphic(text);
            cell.setPrefHeight(Control.USE_COMPUTED_SIZE);
            text.wrappingWidthProperty().bind(clmCategory.widthProperty());
            text.textProperty().bind(cell.itemProperty());
            return cell;
        });

        count = 0;
    }

    public void initUniversal(ArrayList<Switch> switch_checker, ArrayList<ConditionalData> conditional_checker, TreeSet<Decision> d) {
        this.switch_checker = switch_checker;
        this.conditional_checker = conditional_checker;
        decisions = d;

        ArrayList<ListDecision> array_switch_decisions = new ArrayList<ListDecision>();
        for (Decision each_decision : decisions) {
            array_switch_decisions.add(new ListDecision(each_decision.title, each_decision.id));
        }
        cmbSwitchDecision.setItems(FXCollections.observableArrayList(array_switch_decisions));

        ArrayList<ListDecision> array_decision_to_be_changed = new ArrayList<ListDecision>(array_switch_decisions);
        cmbDecisionToBeChanged.setItems(FXCollections.observableArrayList(array_decision_to_be_changed));
    }

    public void initAdd(ArrayList<Switch> switch_checker, ArrayList<ConditionalData> conditional_checker, TreeSet<Decision> d) {
        initUniversal(switch_checker, conditional_checker, d);

        ArrayList<EditorListConditional> array_list_conditionals = new ArrayList<EditorListConditional>();
        cmbConditionals.setItems(FXCollections.observableArrayList(array_list_conditionals));
        replaceable_name = "";
    }

    public void initEdit(ArrayList<Switch> switch_checker, ArrayList<ConditionalData> conditional_checker, TreeSet<Decision> d, Switch switch_to_be_editted, ArrayList<ConditionalData> conditional_data) {
        initUniversal(switch_checker, conditional_checker, d);
        //Set name of Switch txtbox
        txtSwitchName.setText(switch_to_be_editted.getName());
        //Set switch decision
        EditorDecisionServicer decision_servicer = new EditorDecisionServicer();
        Decision switch_decision = decision_servicer.getDecision(decisions, switch_to_be_editted.getId());
        ListDecision list_switch_decision = new ListDecision(switch_decision.title, switch_decision.id);
        cmbSwitchDecision.getSelectionModel().select(list_switch_decision);

        //Iterate through conditional data and fill dropdown box (WE NEED A CONDITIONAL OBJECT THAT HOLDS ALL DATA)
        EditorSwitchConditionalServicer editor_switch_conditional_servicer = new EditorSwitchConditionalServicer();
        ArrayList<EditorListConditional> array_list_conditionals = editor_switch_conditional_servicer.fillListConditionalArray(decisions, conditional_data, switch_to_be_editted);
        count = array_list_conditionals.size();
        ObservableList<EditorListConditional> list_conditionals = FXCollections.observableArrayList(array_list_conditionals);
        cmbConditionals.setItems(list_conditionals);
        replaceable_name = switch_to_be_editted.getName();
    }

    @FXML
    private void handleCmbConditionals(ActionEvent event) {
        //Select the listed decision if the conditional has one already
        if (cmbConditionals.getSelectionModel().getSelectedItem() != null) {
            ListDecision decision_to_be_changed = cmbConditionals.getSelectionModel().getSelectedItem().decision_to_be_changed;
            if (decision_to_be_changed != null) {
                cmbDecisionToBeChanged.getSelectionModel().select(decision_to_be_changed);
                handleCmbDecisionToBeChanged(null); //also trigger the next combobox's event
            } else {
                cmbDecisionToBeChanged.getSelectionModel().clearSelection();
                handleCmbDecisionToBeChanged(null);
            }
        } else {
            cmbDecisionToBeChanged.getSelectionModel().clearSelection();
            handleCmbDecisionToBeChanged(null);
        }
    }

    @FXML
    private void BtnAddConditional(ActionEvent event) {
        cmbConditionals.getItems().add(new EditorListConditional(count));
        count++;

        cmbConditionals.getSelectionModel().selectLast();
        handleCmbConditionals(null);
    }

    @FXML
    private void handleBtnDeleteConditional(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);

        if (cmbConditionals.getItems().isEmpty()) { //empty conditional array
            alert.setContentText("There are no conditionals to remove.");
            alert.showAndWait();
            return;
        } else if (cmbConditionals.getSelectionModel().getSelectedItem() == null) { //no conditional selected
            alert.setContentText("Please select the conditional to remove from the dropdown menu to the left before attempting to remove it via the 'Delete Conditional' button.");
            alert.showAndWait();
            return;
        }

        EditorListConditional to_be_removed = cmbConditionals.getSelectionModel().getSelectedItem();
        cmbConditionals.getItems().remove(to_be_removed);
        cmbConditionals.getSelectionModel().selectLast();
        handleCmbConditionals(null);
    }

    @FXML
    private void handleCmbDecisionToBeChanged(ActionEvent event) {
        ListDecision selected_list_decision = cmbDecisionToBeChanged.getSelectionModel().getSelectedItem();
        if (selected_list_decision != null) { //apply type
            EditorDecisionServicer decision_servicer = new EditorDecisionServicer();
            Decision changed_decision = decision_servicer.getDecision(decisions, selected_list_decision.id());
            String type = changed_decision.type;

            if (cmbConditionals.getSelectionModel().getSelectedItem() != null) {
                cmbConditionals.getSelectionModel().getSelectedItem().type = type;
            }

            if (type.equals("Indiv")) {
                cmbAffected.setItems(FXCollections.observableArrayList(
                        "Applicant")
                );
                if (cmbConditionals.getSelectionModel().getSelectedItem().decision_to_be_changed != selected_list_decision) { //RESET IF WE CHANGED DECISION
                    int applicant_effects = changed_decision.applicant_effects.size();
                    cmbConditionals.getSelectionModel().getSelectedItem().resetEffectSets(applicant_effects, 0, 0);
                }
            } else {
                cmbAffected.setItems(FXCollections.observableArrayList(
                        "Applicant", "Recipient", "Both")
                );
                if (cmbConditionals.getSelectionModel().getSelectedItem().decision_to_be_changed != selected_list_decision) { //RESET IF WE CHANGED DECISION
                    int applicant_effects = changed_decision.applicant_effects.size();
                    int recipient_effects = changed_decision.recipient_effects.size();
                    int both_effects = changed_decision.both_effects.size();
                    cmbConditionals.getSelectionModel().getSelectedItem().resetEffectSets(applicant_effects, recipient_effects, both_effects);
                }
            }
            cmbAffected.getSelectionModel().selectFirst();
            handleCmbAffected(null);

            cmbConditionals.getSelectionModel().getSelectedItem().decision_to_be_changed = selected_list_decision;
        } else {
            cmbAffected.getSelectionModel().clearSelection();
            handleCmbAffected(null);
        }
    }

    @FXML
    private void handleCmbAffected(ActionEvent event) {
        //Show respective effects in effect table
        ListDecision selected_list_decision = cmbDecisionToBeChanged.getSelectionModel().getSelectedItem();
        if (selected_list_decision != null) {
            String type = cmbAffected.getSelectionModel().getSelectedItem();
            if (type != null) {
                if (type.equals("Applicant") || type.equals("Recipient") || type.equals("Both")) {
                    EditorDecisionServicer decision_servicer = new EditorDecisionServicer();
                    EditorSwitchConditionalServicer editor_switch_conditional_servicer = new EditorSwitchConditionalServicer();
                    Decision selected_decision = decision_servicer.getDecision(decisions, selected_list_decision.id());
                    EditorListConditional current_conditional = cmbConditionals.getSelectionModel().getSelectedItem();

                    ArrayList<String> decision_effects;
                    if (type.equals("Applicant")) {
                        decision_effects = selected_decision.applicant_effects;
                        if (current_conditional.applicant_add_changes) {
                            radioAdd.fire();
                        } else {
                            radioReplace.fire();
                        }
                    } else if (type.equals("Recipient")) {
                        decision_effects = selected_decision.recipient_effects;
                        if (current_conditional.recipient_add_changes) {
                            radioAdd.fire();
                        } else {
                            radioReplace.fire();
                        }
                    } else {
                        decision_effects = selected_decision.both_effects;
                        if (current_conditional.both_add_changes) {
                            radioAdd.fire();
                        } else {
                            radioReplace.fire();
                        }
                    }

                    ArrayList<ViewEffect> effects_readable = editor_switch_conditional_servicer.convertEffectsReadable(decision_effects, type);
                    tableOriginalEffects.setItems(FXCollections.observableArrayList(effects_readable));

                    //Also change effects table accordingly.
                    if (radioAdd.isSelected()) {
                        handleRadioAdd(null);
                    } else {
                        handleRadioReplace(null);
                    }

                } else {
                    //CLEAR ORIGINAL EFFECTS AND EFFECTS TABLE
                    tableOriginalEffects.setItems(FXCollections.observableArrayList(new ArrayList<ViewEffect>()));
                    tableEffects.setItems(FXCollections.observableArrayList(new ArrayList<Effect>()));
                }
            } else {
                //CLEAR ORIGINAL EFFECTS AND EFFECTS TABLE
                tableOriginalEffects.setItems(FXCollections.observableArrayList(new ArrayList<ViewEffect>()));
                tableEffects.setItems(FXCollections.observableArrayList(new ArrayList<Effect>()));
            }
        } else {
            //CLEAR ORIGINAL EFFECTS AND EFFECTS TABLE
            tableOriginalEffects.setItems(FXCollections.observableArrayList(new ArrayList<ViewEffect>()));
            tableEffects.setItems(FXCollections.observableArrayList(new ArrayList<Effect>()));
        }

    }

    @FXML
    private void handleRadioAdd(ActionEvent event) {
        //Display the EffectSet of key -1 in the current EditorListConditional HashMap
        if (cmbConditionals.getSelectionModel().getSelectedItem() != null && cmbDecisionToBeChanged.getSelectionModel().getSelectedItem() != null && cmbAffected.getSelectionModel().getSelectedItem() != null) { //as long as we have a conditional selected
            String type = cmbAffected.getSelectionModel().getSelectedItem();
            EditorListConditional current_conditional = cmbConditionals.getSelectionModel().getSelectedItem();
            switch (type) {
                case "Applicant":
                    tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(new Integer(-1)).effect_list);
                    txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(new Integer(-1)).probability);
                    current_conditional.applicant_add_changes = true;
                    break;
                case "Recipient":
                    tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(new Integer(-1)).effect_list);
                    txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(new Integer(-1)).probability);
                    current_conditional.recipient_add_changes = true;
                    break;
                case "Both":
                    tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(new Integer(-1)).effect_list);
                    txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(new Integer(-1)).probability);
                    current_conditional.both_add_changes = true;
                    break;
                default:
                    break;
            } //endswitch
        } //endif
    }

    @FXML
    private void handleRadioReplace(ActionEvent event) {
        //Display the EffectSet of the current table selection, or if nothing's selected, show nothing.
        if (cmbConditionals.getSelectionModel().getSelectedItem() != null && cmbDecisionToBeChanged.getSelectionModel().getSelectedItem() != null && cmbAffected.getSelectionModel().getSelectedItem() != null) { //as long as we have a conditional selected
            int table_selected_index = tableOriginalEffects.getSelectionModel().getSelectedIndex();
            String type = cmbAffected.getSelectionModel().getSelectedItem();
            if (table_selected_index != -1) { //show effectset of current table selection
                switch (type) {
                    case "Applicant":
                        tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(new Integer(table_selected_index)).effect_list);
                        txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(new Integer(table_selected_index)).probability);
                        break;
                    case "Recipient":
                        tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(new Integer(table_selected_index)).effect_list);
                        txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(new Integer(table_selected_index)).probability);
                        break;
                    case "Both":
                        tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(new Integer(table_selected_index)).effect_list);
                        txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(new Integer(table_selected_index)).probability);
                        break;
                    default:
                        break;
                }
            } else { //show nothing
                tableEffects.setItems(FXCollections.observableArrayList(new ArrayList<Effect>()));
                txtProbability.setText("1.0");
            }
            EditorListConditional current_conditional = cmbConditionals.getSelectionModel().getSelectedItem();
            switch (type) {
                case "Applicant":
                    current_conditional.applicant_add_changes = false;
                    break;
                case "Recipient":
                    current_conditional.recipient_add_changes = false;
                    break;
                case "Both":
                    current_conditional.both_add_changes = false;
                    break;
                default:
                    break;
            }
        }
    }

    @FXML
    private void handleTableOriginalEffectsClicked(MouseEvent event) {
        //In 'add' mode.  
        //Clicking table items does nothing.
        //User can add one 'set' of effects with a shared probability via the controls at the bottom of the UI.
        //Use the EditorListConditional's corresponding hashmap, with the -1 Integer as a key.
        //In 'Replace' mode.
        //Each table item is 'replacable' and has a mapped effectset (much like the editorcontroller's effectset dropdown menu)
        //Use the EditorListConditional's corresponding hashmap, with the respective Integer from the current table effect selection as a key
        if (radioReplace.isSelected()) {
            if (cmbConditionals.getSelectionModel().getSelectedItem() != null && cmbDecisionToBeChanged.getSelectionModel().getSelectedItem() != null && cmbAffected.getSelectionModel().getSelectedItem() != null) { //as long as we have a conditional selected
                int table_selected_index = tableOriginalEffects.getSelectionModel().getSelectedIndex();
                if (table_selected_index != -1) { //show effectset of current table selection
                    String type = cmbAffected.getSelectionModel().getSelectedItem();
                    switch (type) {
                        case "Applicant":
                            tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(new Integer(table_selected_index)).effect_list);
                            txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(new Integer(table_selected_index)).probability);
                            break;
                        case "Recipient":
                            tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(new Integer(table_selected_index)).effect_list);
                            txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(new Integer(table_selected_index)).probability);
                            break;
                        case "Both":
                            tableEffects.setItems(cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(new Integer(table_selected_index)).effect_list);
                            txtProbability.setText("" + cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(new Integer(table_selected_index)).probability);
                            break;
                        default:
                            break;
                    }
                } //end table_selected_index if
            } //if all three previous cmbboxes selected 
        }//radio replace if
    }

    @FXML
    private void handleAddEffect(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText("Unable to add effect.");

        int value;
        int delay;

        if (cmbConditionals.getSelectionModel().getSelectedItem() == null || cmbDecisionToBeChanged.getSelectionModel().getSelectedItem() == null || cmbAffected.getSelectionModel().getSelectedItem() == null) {
            alert.setContentText("Please ensure you have selected the Conditional Number, Decision To Be Changed, and Player Affected before attempting to add an effect.");
            alert.showAndWait();
            return;
        }
        if (radioReplace.isSelected() && tableOriginalEffects.getSelectionModel().getSelectedItem() == null) {
            alert.setContentText("Because you are replacing an effect, please ensure you have selected an effect to replace on the table above before attempting to add an effect.");
            alert.showAndWait();
            return;
        }
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
    private void handleSetProbability(ActionEvent event) {
        //Set probability for the currently selected EditorListConditional's hashmap effectset.
        //(Make sure to get the key right, -1 for add, table effect index for replace.)

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

        String type = cmbAffected.getSelectionModel().getSelectedItem();
        if (type == null) {
            alert.setContentText("No affected player is selected.");
            alert.showAndWait();
            return;
        }

        if (radioAdd.isSelected()) { //ADD CASE
            if (type.equals("Applicant")) {
                cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(-1).probability = new_probability;
            } else if (type.equals("Recipient")) {
                cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(-1).probability = new_probability;
            } else if (type.equals("Both")) {
                cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(-1).probability = new_probability;
            }
        } else if (radioReplace.isSelected()) { //REPLACE CASE
            int table_index = tableOriginalEffects.getSelectionModel().getSelectedIndex();
            if (table_index < 0) { //No table element selected
                alert.setContentText("Please select the effect you are replacing on the table above.");
                alert.showAndWait();
                return;
            }

            if (type.equals("Applicant")) {
                cmbConditionals.getSelectionModel().getSelectedItem().applicant_effect_sets.get(table_index).probability = new_probability;
            } else if (type.equals("Recipient")) {
                cmbConditionals.getSelectionModel().getSelectedItem().recipient_effect_sets.get(table_index).probability = new_probability;
            } else if (type.equals("Both")) {
                cmbConditionals.getSelectionModel().getSelectedItem().both_effect_sets.get(table_index).probability = new_probability;
            }
        }

    }

    @FXML
    private void handleBtnRemoveEffect(ActionEvent event) {
        Effect remove_effect = tableEffects.getSelectionModel().getSelectedItem();
        if (remove_effect == null) {
            Alert alert = new Alert(AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Could not remove Effect.");
            alert.setContentText("Please select an effect in the table before attempting to remove it.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
        } else {
            tableEffects.getItems().remove(remove_effect);
        }
    }

    @FXML
    private void handleBtnDone(ActionEvent event) {
        Alert alert = new Alert(AlertType.ERROR);
        alert.initModality(Modality.APPLICATION_MODAL);
        alert.setHeaderText("Unable to add switch and conditional(s).");

        EditorSwitchConditionalServicer sc_servicer = new EditorSwitchConditionalServicer();

        /////////////////////////////////////////////Check validity of switch properties and add new switch
        String switch_name = txtSwitchName.getText();
        if (switch_name.trim().equals("")) {
            alert.setContentText("Please fill in the topmost text box in order to give the switch a name.");
            alert.showAndWait();
            return;
        }

        if (!sc_servicer.isNameUnique(switch_name, switch_checker, replaceable_name)) {
            alert.setContentText("The name you have assigned to this switch is currently being used.  Please use another name.");
            alert.showAndWait();
            return;
        }

        if (cmbSwitchDecision.getSelectionModel().getSelectedItem() == null) {
            alert.setContentText("Please ensure that the dropdown menu with the text 'Decision that Triggers Switch' has a decision selected.");
            alert.showAndWait();
            return;
        }

        int switch_decision_id = cmbSwitchDecision.getSelectionModel().getSelectedItem().id();

        Switch new_switch = new Switch(switch_decision_id, switch_name);

        switch_checker.add(new_switch);
        /////////////////////////////////////////////Check validity of switch properties and add new switch

        /////////////////////////////////////////////Check+add conditional properties
        boolean no_affected_decision = false;
        ArrayList<Integer> ids_without_decision = new ArrayList<Integer>();
        for (EditorListConditional each_conditional
                : cmbConditionals.getItems()) {
            if (each_conditional.decision_to_be_changed == null) {
                no_affected_decision = true;
                ids_without_decision.add(each_conditional.conditional_id);
            }
        }

        if (no_affected_decision) {
            String alerttext = "Please ensure the dropdown menu with the text 'Decision To Be Changed' has a decision selected for the conditionals with the following IDs: ";
            for (Integer each_int : ids_without_decision) {
                alerttext += "\n " + each_int;
            }
            alert.setContentText(alerttext);
            alert.showAndWait();
            return;
        }

        for (EditorListConditional each_conditional
                : cmbConditionals.getItems()) {
            System.out.println(each_conditional.conditional_id);

            ListDecision affected_decision = each_conditional.decision_to_be_changed;
            int affected_decision_id = affected_decision.id();
            String type = each_conditional.type;

            if (type.equals("Indiv")) {
                //DO FOR APPLICANT
                //CHECK TO MAKE SURE IF THE APPLICANT EFFECT SET IS EMPTY AND THROW AN ALERT IF IT IS

                String applicant_type = "A";
                String recipient_type = "N";
                String both_type = "N";
                ArrayList<String> applicant_changes = new ArrayList<String>();
                ArrayList<String> recipient_changes = new ArrayList<String>();
                ArrayList<String> both_changes = new ArrayList<String>();

                if (sc_servicer.isEffectSetEmpty(each_conditional.applicant_effect_sets)) {
                    continue; //ignore conditionals that have nothing in them
                }

                if (each_conditional.applicant_add_changes) { //ADD
                    applicant_changes = sc_servicer.effectParser(each_conditional.applicant_effect_sets, false);
                } else { //REPLACE
                    applicant_type = "R";
                    applicant_changes = sc_servicer.effectParser(each_conditional.applicant_effect_sets, true);
                }

                ConditionalData new_conditional = new ConditionalData(affected_decision_id, switch_name, applicant_type, recipient_type, both_type, applicant_changes, recipient_changes, both_changes);
                conditional_checker.add(new_conditional);
            } else if (type.equals("Joint")) {
                String applicant_type = "A";
                String recipient_type = "A";
                String both_type = "A";
                ArrayList<String> applicant_changes = new ArrayList<String>();
                ArrayList<String> recipient_changes = new ArrayList<String>();
                ArrayList<String> both_changes = new ArrayList<String>();

                if (sc_servicer.isEffectSetEmpty(each_conditional.applicant_effect_sets) && sc_servicer.isEffectSetEmpty(each_conditional.recipient_effect_sets) && sc_servicer.isEffectSetEmpty(each_conditional.both_effect_sets)) {
                    continue; //ignore conditionals that have nothing in them
                }

                if (each_conditional.applicant_add_changes) {
                    applicant_changes = sc_servicer.effectParser(each_conditional.applicant_effect_sets, false);
                    if (applicant_changes.isEmpty()) {
                        applicant_changes.add("nothing");
                    }
                } else {
                    applicant_changes = sc_servicer.effectParser(each_conditional.applicant_effect_sets, true);
                    if (applicant_changes.isEmpty()) {
                        applicant_changes.add("nothing");
                    } else {
                        applicant_type = "R";
                    }
                }

                if (each_conditional.recipient_add_changes) {
                    recipient_changes = sc_servicer.effectParser(each_conditional.recipient_effect_sets, false);
                    if (recipient_changes.isEmpty()) {
                        recipient_changes.add("nothing");
                    }
                } else {
                    recipient_changes = sc_servicer.effectParser(each_conditional.recipient_effect_sets, true);
                    if (recipient_changes.isEmpty()) {
                        recipient_changes.add("nothing");
                    } else {
                        recipient_type = "R";
                    }
                }

                if (each_conditional.both_add_changes) {
                    both_changes = sc_servicer.effectParser(each_conditional.both_effect_sets, false);
                    if (both_changes.isEmpty()) {
                        both_changes.add("nothing");
                    }
                } else {
                    both_changes = sc_servicer.effectParser(each_conditional.both_effect_sets, true);
                    if (both_changes.isEmpty()) {
                        both_changes.add("nothing");
                    } else {
                        both_type = "R";
                    }

                }
                ConditionalData new_conditional = new ConditionalData(affected_decision_id, switch_name, applicant_type, recipient_type, both_type, applicant_changes, recipient_changes, both_changes);
                conditional_checker.add(new_conditional);
            }
        }
        /////////////////////////////////////////////Check+add conditional properties

        Stage stage = (Stage) btnCancel.getScene().getWindow();

        stage.close();
    }

    @FXML
    private void handleBtnCancel(ActionEvent event
    ) {
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Cancel");
        alert.setContentText("Are you sure you want to cancel this conditional?");
        alert.initModality(Modality.APPLICATION_MODAL);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.get() == ButtonType.OK) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        } else {
            return;
        }
    }

}
