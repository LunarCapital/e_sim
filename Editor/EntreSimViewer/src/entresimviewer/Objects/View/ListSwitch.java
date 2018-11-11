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
public class ListSwitch {

    private final StringProperty name = new SimpleStringProperty();

    public ListSwitch(String n) {
        name.set(n);
    }

    public final StringProperty name() {
        return name;
    }

    @Override
    public String toString() {
        return name.get();
    }

}
