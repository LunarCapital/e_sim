/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.UIHandlers;

import entreObj.Decision;
import java.util.ArrayList;
import java.util.Optional;
import javafx.scene.control.Alert;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextInputDialog;
import javafx.stage.Modality;

/**
 *
 * @author dylanleong
 */
public class CustomUIHandler {

    ArrayList<String> applicant_effects;
    ArrayList<String> recipient_effects;
    ArrayList<String> both_effects;
    
    public CustomUIHandler() {
        resetEffectLists();
    }
    
    public void rejectProposal(RoomUIHandler room_ui_handler) {
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
        resetEffectLists();
    }

    public void acceptProposal(RoomUIHandler room_ui_handler, TextField txtFinance, TextField txtExplore, TextField txtExploit, TextField txtChange, TextArea txtProposal, TextField txtProposalName) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Error");
        alert.setHeaderText("Please ensure you have specified numbers for each resource.");
        alert.initModality(Modality.APPLICATION_MODAL);

        if (!txtFinance.getText().trim().matches("^-?\\d+$")) {
            alert.setContentText("The finance input has been detected to have an invalid or no number.");
            alert.showAndWait();
            return;
        }
        if (txtFinance.getText().length() >= 9) {
            alert.setContentText("The finance textbox contains too big of a number.");
            alert.showAndWait();
            return;
        }
        if (!txtExplore.getText().trim().matches("^-?\\d+$")) {
            alert.setContentText("The exploratory knowledge input has been detected to have an invalid or no number.");
            alert.showAndWait();
            return;
        }
        if (txtExplore.getText().length() >= 9) {
            alert.setContentText("The explore textbox contains too big of a number.");
            alert.showAndWait();
            return;
        }
        if (!txtExploit.getText().trim().matches("^-?\\d+$")) {
            alert.setContentText("The exploitative knowledge input has been detected to have an invalid or no number.");
            alert.showAndWait();
            return;
        }
        if (txtExploit.getText().length() >= 9) {
            alert.setContentText("The exploit textbox contains too big of a number.");
            alert.showAndWait();
            return;
        }
        if (!txtChange.getText().trim().matches("^-?\\d+$")) {
            alert.setContentText("The finance/turn input has been detected to have an invalid or no number.");
            alert.showAndWait();
            return;
        }
        if (txtChange.getText().length() >= 9) {
            alert.setContentText("The finance/turn textbox contains too big of a number.");
            alert.showAndWait();
            return;
        }

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

        StringBuilder description = new StringBuilder(txtProposal.getText());
        description.append("\n");

        ArrayList<String> effects = new ArrayList<String>();
        if (Integer.parseInt(txtFinance.getText().trim()) != 0) {
            effects.add("finance " + txtFinance.getText().trim() + " 0 false");
            description.append("Changes finance by: " + txtFinance.getText().trim() + "\n");
        }
        if (Integer.parseInt(txtExplore.getText().trim()) != 0) {
            effects.add("explore " + txtExplore.getText().trim() + " 0 false");
            description.append("Changes exploratory knowledge by: " + txtExplore.getText().trim() + "\n");
        }
        if (Integer.parseInt(txtExploit.getText().trim()) != 0) {
            effects.add("exploit " + txtExploit.getText().trim() + " 0 false");
            description.append("Changes exploitative knowledge by: " + txtExploit.getText().trim() + "\n");
        }
        if (Integer.parseInt(txtChange.getText().trim()) != 0) {
            effects.add("f_change " + txtChange.getText().trim() + " 0 false");
            description.append("Changes finance change/turn by: " + txtChange.getText().trim() + "\n");
        }

        int decision_id = 0;
        for (Decision each : room_ui_handler.decisions) { //note decisions is auto-sorted
            if (each.id == decision_id) {
                decision_id++;
            } else {
                break; //pick the first id that's untaken
            }
        }

        System.out.println("Picked id " + decision_id);

        ArrayList<String> prereqs = new ArrayList<String>();
        prereqs.add("none"); //PLACEHOLDER

        Decision d = new Decision(decision_id, txtProposalName.getText(), description.toString(), "Indiv", prereqs, effects, new ArrayList<String>(), new ArrayList<String>());
        if (room_ui_handler.decisions != null) {
            room_ui_handler.decisions.add(d);
        }

        room_ui_handler.game_controller.customAccept(comment, d);
        resetEffectLists();
    }

    private void resetEffectLists() {
        applicant_effects = new ArrayList<String>();
        recipient_effects = new ArrayList<String>();
        both_effects = new ArrayList<String>();
    }
    
}
