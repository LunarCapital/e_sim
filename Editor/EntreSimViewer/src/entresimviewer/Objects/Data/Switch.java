/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer.Objects.Data;

import java.io.Serializable;

/**
 * Simple switch class that links a Decision ID with a switch name.
 * @author dylanleong
 */
public class Switch implements Serializable {
    
    private int id;
    private String name;
    
    
    public Switch(int i, String n) {
        id = i;
        name = n;
    }
    
    public int getId() {
        return id;
    }
    
    public String getName() {
        return name;
    }
}
