/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Objects.Data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dylanleong
 */
public class ViewEffect {
        
    private StringProperty probability;
    public void setProbability(String value) {probabilityProperty().set(value);}
    public String getProbability() {return probabilityProperty().get();}
    public StringProperty probabilityProperty() {
        if (probability == null) probability = new SimpleStringProperty(this, "Probability");
        return probability;
    }
    
    private StringProperty affected;
    public void setAffected(String value) {affectedProperty().set(value);}
    public String getAffected() {return affectedProperty().get();}
    public StringProperty affectedProperty() {
        if (affected == null) affected = new SimpleStringProperty(this, "affected");
        return affected;
    }
    
    private StringProperty effects;
    public void setEffect(String value) {effectsProperty().set(value);}
    public String getEffect() {return effectsProperty().get();}
    public StringProperty effectsProperty() {
        if (effects == null) effects = new SimpleStringProperty(this, "effects");
        return effects;
    }
}
