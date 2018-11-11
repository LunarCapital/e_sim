/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor.Objects.Data;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dylanleong
 */
public class EditorListDecision {

    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty on = new SimpleBooleanProperty();
    private final int id;

    public EditorListDecision(String n, boolean o, int i) {
        name.set(n);
        on.set(o);
        id = i;
    }

    public final StringProperty name() {
        return name;
    }

    public final BooleanProperty on() {
        return on;
    }
    
    public void check(boolean c) {
        on.set(c);
    }
    
    public final int id() {
        return id;
    }

    @Override
    public String toString() {
        return name.get();
    }

}
