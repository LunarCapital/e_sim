/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor.Objects.Data;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 *
 * @author dylanleong
 */
public class EffectSet {
    
    public double probability;
    public ObservableList<Effect> effect_list;
    
    public EffectSet() {
        probability = 1.0;
        effect_list = FXCollections.observableArrayList();
    }
    
}
