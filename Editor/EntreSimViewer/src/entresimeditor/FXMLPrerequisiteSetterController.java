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
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.CheckBoxListCell;
import javafx.scene.layout.Pane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import entresimeditor.Objects.Data.EditorListDecision;

/**
 * FXML Controller class
 *
 * @author dylanleong
 */
public class FXMLPrerequisiteSetterController implements Initializable {

    @FXML
    private Button btnOk;
    @FXML
    private Button btnCancel;
    @FXML
    private CheckBox checkRD_applicant;
    @FXML
    private CheckBox checkM_applicant;
    @FXML
    private CheckBox checkFB_applicant;
    @FXML
    private CheckBox checkRD_recipient;
    @FXML
    private CheckBox checkM_recipient;
    @FXML
    private CheckBox checkFB_recipient;
    @FXML
    private Label lblRecipient;
    @FXML
    private TextField txtFinance;
    @FXML
    private TextField txtExplore;
    @FXML
    private TextField txtExploit;
    @FXML
    private ListView<EditorListDecision> listMust;
    @FXML
    private ListView<EditorListDecision> listNot;

    private ArrayList<ArrayList<String>> share;
    private TreeSet<Decision> decisions;
    private boolean individual;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void init(ArrayList<Integer> must, ArrayList<Integer> not, int[] min_resources, boolean[] applicant_roles, boolean[] recipient_roles, ArrayList<ArrayList<String>> s, boolean i, TreeSet<Decision> d) {
        share = s;
        individual = i;
        decisions = d;

        //POPULATE MUST AND NOT LISTVIEWS
        ArrayList<EditorListDecision> must_array_list = new ArrayList<EditorListDecision>();
        for (Decision each : d) {
            must_array_list.add(new EditorListDecision(each.title, false, each.id));
        }
        ArrayList<EditorListDecision> not_array_list = new ArrayList<EditorListDecision>();
        for (Decision each : d) {
            not_array_list.add(new EditorListDecision(each.title, false, each.id));
        }

        ObservableList<EditorListDecision> must_list = FXCollections.observableArrayList(must_array_list);
        ObservableList<EditorListDecision> not_list = FXCollections.observableArrayList(not_array_list);

        listMust.setItems(must_list);
        listNot.setItems(not_list);

        listMust.setCellFactory(CheckBoxListCell.forListView((EditorListDecision item) -> item.on()));
        listNot.setCellFactory(CheckBoxListCell.forListView((EditorListDecision item) -> item.on()));

        //CHECK MUST DECISIONS
        for (Integer each_id : must) {
            for (EditorListDecision each_decision : listMust.getItems()) {
                if (each_decision.id() == each_id) {
                    each_decision.check(true);
                    break;
                }
            }

        }

        //CHECK NOT DECISIONS
        for (Integer each_id : not) {
            for (EditorListDecision each_decision : listNot.getItems()) {
                if (each_decision.id() == each_id) {
                    each_decision.check(true);
                    break;
                }
            }
        }

        //SET MINIMUM RESOURCE VALUES
        txtFinance.setText("" + min_resources[0]);
        txtExplore.setText("" + min_resources[1]);
        txtExploit.setText("" + min_resources[2]);

        //SET CHECKBOXES TO TRUE OR FALSE
        checkRD_applicant.setSelected(applicant_roles[0]);
        checkM_applicant.setSelected(applicant_roles[1]);
        checkFB_applicant.setSelected(applicant_roles[2]);

        checkRD_recipient.setSelected(recipient_roles[0]);
        checkM_recipient.setSelected(recipient_roles[1]);
        checkFB_recipient.setSelected(recipient_roles[2]);

        //DISABLE RECIPIENT BOXES IF INDIVIDUAL
        if (individual) {
            checkRD_recipient.setDisable(true);
            checkM_recipient.setDisable(true);
            checkFB_recipient.setDisable(true);

            lblRecipient.setText("Because this is an individual decision, there is no need to set recipient role availability.  Ignore this side of the window.");
        } else {
            checkRD_recipient.setDisable(false);
            checkM_recipient.setDisable(false);
            checkFB_recipient.setDisable(false);

            lblRecipient.setText("Recipient must be one of the following roles:");
        }

    }

    @FXML
    private void handleBtnOk(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initModality(Modality.APPLICATION_MODAL);

        //NO ROLES SELECTED (aka nobody can make this decision)
        if (((!checkRD_applicant.isSelected()) && (!checkM_applicant.isSelected()) && (!checkFB_applicant.isSelected())) || ((!checkRD_recipient.isSelected()) && (!checkM_recipient.isSelected()) && (!checkFB_recipient.isSelected()))) {
            alert.setContentText("Please ensure this decision is available to at least one role.");
            alert.showAndWait();
            return;
        }

        //Ensure listmust and listnot don't have the same decision checked
        boolean decision_on_both_lists = false;
        StringBuilder sb = new StringBuilder("");
        sb.append("The following decisions were found on both the 'Must have made this decision' and 'Must not have made this decision' checklists.\n\n");
        for (EditorListDecision each_must : listMust.getItems()) {
            for (EditorListDecision each_not : listNot.getItems()) {
                if (each_must.id() == each_not.id() && each_must.on().get() && each_not.on().get()) {
                    decision_on_both_lists = true;
                    sb.append(each_must.name().get() + "\n");
                }
            }
        }

        if (decision_on_both_lists) {
            alert.setContentText(sb.toString());
            alert.showAndWait();
            return;
        }

        //Ensure each textbox contains numbers, and said numbers are positive
        try {
            int value = Integer.parseInt(txtFinance.getText());
            if (value < 0) {
                alert.setContentText("Please ensure the number in the minimum finance requirement textbox is positive.");
                alert.showAndWait();
                return;
            }
        } catch (NumberFormatException nfe) { //Value not a whole number
            alert.setContentText("Please ensure the minimum finance requirement textbox contains a single integer.");
            alert.showAndWait();
            return;
        }

        try {
            int value = Integer.parseInt(txtExplore.getText());
            if (value < 0) {
                alert.setContentText("Please ensure the number in the minimum exploratory knowledge requirement textbox is positive.");
                alert.showAndWait();
                return;
            }
        } catch (NumberFormatException nfe) { //Value not a whole number
            alert.setContentText("Please ensure the minimum exploratory knowledge requirement textbox contains a single integer.");
            alert.showAndWait();
            return;
        }

        try {
            int value = Integer.parseInt(txtExploit.getText());
            if (value < 0) {
                alert.setContentText("Please ensure the number in the minimum exploitative knowledge requirement textbox is positive.");
                alert.showAndWait();
                return;
            }
        } catch (NumberFormatException nfe) { //Value not a whole number
            alert.setContentText("Please ensure the minimum exploitative knowledge requirement textbox contains a single integer.");
            alert.showAndWait();
            return;
        }

        //IF THIS STEP IS NOT TAKEN, THE SHARE ARRAYLIST IS EMPTY AND THE PREREQUISITE WINDOW WILL HAVE BEEN DEEMED CANCELLED
        for (int j = 0; j < 4; j++) {
            share.add(new ArrayList<String>());
        }

        //ADD ROLE REQUIREMENTS
        if (checkRD_applicant.isSelected()) {
            share.get(0).add("AR");
        }
        if (checkM_applicant.isSelected()) {
            share.get(0).add("AM");
        }
        if (checkFB_applicant.isSelected()) {
            share.get(0).add("AF");
        }

        if (!individual) {
            if (checkRD_recipient.isSelected()) {
                share.get(0).add("RR");
            }
            if (checkM_recipient.isSelected()) {
                share.get(0).add("RM");
            }
            if (checkFB_recipient.isSelected()) {
                share.get(0).add("RF");
            }
        }

        //ADD MINIMUM RESOURCE REQUIREMENTS
        share.get(1).add(txtFinance.getText());
        share.get(1).add(txtExplore.getText());
        share.get(1).add(txtExploit.getText());

        //ADD MUST
        for (EditorListDecision each : listMust.getItems()) {
            if (each.on().get()) {
                share.get(2).add("" + each.id());
            }
        }

        //ADD NOT
        for (EditorListDecision each : listNot.getItems()) {
            if (each.on().get()) {
                share.get(3).add("" + each.id());
            }
        }

        Stage stage = (Stage) btnOk.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleBtnCancel(ActionEvent event) {
        Alert alert = new Alert(Alert.AlertType.NONE, "Cancel?", ButtonType.YES, ButtonType.NO);
        alert.initModality(Modality.APPLICATION_MODAL);
        if (alert.showAndWait().orElse(ButtonType.NO) == ButtonType.YES) {
            Stage stage = (Stage) btnCancel.getScene().getWindow();
            stage.close();
        }
    }

    @FXML
    private void handleBtnCheckMust(ActionEvent event) {
        if (listMust.getSelectionModel().getSelectedItem() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDecisionQuickView.fxml"));
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene((Pane) loader.load()));
                stage.setResizable(false);

                FXMLDecisionQuickViewController controller = loader.<FXMLDecisionQuickViewController>getController();
                controller.init(decisions, listMust.getSelectionModel().getSelectedItem().id());
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
    private void handleBtnCheckNot(ActionEvent event) {
        if (listNot.getSelectionModel().getSelectedItem() != null) {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLDecisionQuickView.fxml"));
                Stage stage = new Stage();
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.setScene(new Scene((Pane) loader.load()));
                stage.setResizable(false);

                FXMLDecisionQuickViewController controller = loader.<FXMLDecisionQuickViewController>getController();
                controller.init(decisions, listNot.getSelectionModel().getSelectedItem().id());
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

}
