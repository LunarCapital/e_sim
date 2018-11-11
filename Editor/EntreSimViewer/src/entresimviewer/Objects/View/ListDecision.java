/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer.Objects.View;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/**
 *
 * @author dylanleong
 */
public class ListDecision {

    private final StringProperty name = new SimpleStringProperty();
    private final int id;

    public ListDecision(String n, int i) {
        name.set(n);
        id = i;
    }

    public final StringProperty name() {
        return name;
    }
    
    public final int id() {
        return id;
    }

    @Override
    public String toString() {
        return name.get();
    }

}
