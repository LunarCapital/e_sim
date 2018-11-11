/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Handlers;

import entreObj.Environment;
import entreObj.PlayerDetails;
import entreObj.WinData;
import java.util.TreeSet;
import javafx.scene.Scene;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author dylanleong
 */
public class GraphicsHandler {

    public BarChart winGraph(TreeSet<WinData> windata, Environment env) {
        final CategoryAxis xAxis = new CategoryAxis();
        final NumberAxis yAxis = new NumberAxis();
        final BarChart<String, Number> bc = new BarChart<>(xAxis, yAxis);
        bc.setTitle("Change in Resource Summary");
        xAxis.setLabel("Players");
        yAxis.setLabel("Resource Units");

        XYChart.Series series = new XYChart.Series();
        series.setName("Total Change in Resources");
        for (WinData each_data : windata) {
            PlayerDetails player_in_question = env.int_to_player.get(each_data.player_id);
            series.getData().add(new XYChart.Data(player_in_question.name, each_data.getScore()));
        }

        bc.getData().add(series);
        
        return bc;
    }

}
