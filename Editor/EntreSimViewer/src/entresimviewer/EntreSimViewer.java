/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 *
 * @author dylanleong
 */
public class EntreSimViewer extends Application {
    
    @Override
    public void start(Stage stage) throws Exception {
        //Parent root = FXMLLoader.load(getClass().getResource("FXMLViewer.fxml"));
        FXMLLoader loader = new FXMLLoader(getClass().getResource("FXMLViewer.fxml"));
        Parent root = (Parent)loader.load();
        FXMLViewerController controller = (FXMLViewerController)loader.getController();
        boolean success = controller.startUpSuccess(stage);
        
        Scene scene = new Scene(root);
        
        stage.setScene(scene);
        stage.show();
        
        if (!success) stage.close();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
    
}
