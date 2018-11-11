/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author dylanleong
 */
public class FXMLDescriptionWriterController implements Initializable {

    @FXML
    private TextArea txtDescription;
    @FXML
    private Button btnDone;

    private ArrayList<String> share;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }

    public void init(ArrayList<String> s) {
        share = s;
    }

    @FXML
    private void handleBtnDone(ActionEvent event) {
        if (txtDescription.getText().trim().equals("")) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setContentText("Please enter a description first.");
            alert.initModality(Modality.APPLICATION_MODAL);
            alert.showAndWait();
            return;
        }

        if (share.size() == 0) {
            share.add(txtDescription.getText());
        } else {
            share.set(0, txtDescription.getText());
        }
        Stage stage = (Stage) btnDone.getScene().getWindow();
        stage.close();
    }

}
