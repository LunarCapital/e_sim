/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient;

import java.net.URL;
import java.time.LocalDateTime;
import java.util.ResourceBundle;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;

/**
 * FXML Controller class
 *
 * @author dylanleong
 */
public class FXMLGameLogController implements Initializable {

    @FXML
    private TextArea txtLog;

    /**
     * Initializes the controller class.
     */
    @Override
    public void initialize(URL url, ResourceBundle rb) {
        // TODO
    }    
    
    public void writeTxtLog(String text, int round) {
        if (!text.trim().equals("")) {
            LocalDateTime ldt = LocalDateTime.now();

            String day = ldt.toLocalDate().toString();
            String time = ldt.getHour() + ":" + ldt.getMinute() + ":" + ldt.getSecond();
            String timestamp = "[" + day + " " + time + "]";

            txtLog.appendText(timestamp + " " + "[Round " + (round + 1) + "] " + text + "\n\n");
        } else {
            txtLog.appendText("\n\n");
        }
    }
    
    public String getTxtLog() {
        return txtLog.getText();
    }
    
    public void close() {
        Stage log_stage = (Stage) txtLog.getScene().getWindow();
        log_stage.close();
    }
}
