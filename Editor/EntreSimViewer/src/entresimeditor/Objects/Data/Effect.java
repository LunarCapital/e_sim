/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor.Objects.Data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dylanleong
 */
public class Effect {
    
    private final StringProperty category;
    private final IntegerProperty value;
    private final IntegerProperty delay;
    private final BooleanProperty linked;
    
    public Effect(String c, int v, int d, boolean l) {
        category = new SimpleStringProperty(c);
        value = new SimpleIntegerProperty(v);
        delay = new SimpleIntegerProperty(d);
        linked = new SimpleBooleanProperty(l);
    }
 
    public String getCategory() {
        return category.get();
    }
    public void setCategory(String c) {
        category.set(c);
    }
    
    public Integer getValue() {
        return value.get();
    }
    public void setValue(int v) {
        value.set(v);
    }
    
    public Integer getDelay() {
        return delay.get();
    }
    public void setDelay(int d) {
        delay.set(d);
    }
    
    public Boolean getLinked() {
        return linked.get();
    }
    public void setLinked(Boolean l) {
        linked.set(l);
    }
}
