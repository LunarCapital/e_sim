/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Objects;

import entreObj.WinData;
import java.util.TreeSet;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 *
 * @author dylanleong
 */
public class GraphicsHandler {

    public void winGraph(TreeSet<WinData> windata, int our_id, Window main_window) {
        Stage graphstage = new Stage();
        graphstage.setTitle("Win Graph.  The game will exit when you close this.");
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Change in Resource Summary");
        xAxis.setLabel("Players (All except you are hidden)");
        yAxis.setLabel("Resource Units");

        XYChart.Series series = new XYChart.Series();
        series.setName("Total Change in Resources");
        for (WinData each_data : windata) {
            if (each_data.player_id == our_id) {
                series.getData().add(new XYChart.Data("YOU", each_data.getScore()));
            } else {
                series.getData().add(new XYChart.Data("Unknown Player with ID: " + each_data.player_id, each_data.getScore()));
            }
        }

        Scene scene = new Scene(bc, 800, 600);
        bc.getData().add(series);
        graphstage.initModality(Modality.WINDOW_MODAL);
        graphstage.initOwner(main_window);
        graphstage.setScene(scene);
        graphstage.showAndWait();
    }

}
