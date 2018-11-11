package entreObj;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author dylanleong
 */
public class PlayerDetails implements Serializable {
    
    //Properties
    public final int id;
    public final String name;
    private String role;
    private boolean dead;
    private boolean joint_available;
    public final Set<Integer> successful_decisions;
    public final ArrayList<Integer> parent_players;
    public final ArrayList<Integer> child_players;
    public float red;
    public float green;
    public float blue;
    
    //Resources
    public int finance;
    public int explore;
    public int exploit;
    public int f_change;
    public int explore_change;
    public int exploit_change;
    
    //Toggleable 'shows'
    public boolean show_finance;
    public boolean show_explore;
    public boolean show_exploit;
    
    //Switches
    public HashMap<String, Boolean> switch_map;
    
    /**
     * 
     * 
     * @param i is the ID
     * @param n is the player's name
     */
    public PlayerDetails(int i, String n) {
        id = i;
        name = n;
        dead = false;
        joint_available = true;
        successful_decisions = new HashSet<Integer>();
        parent_players = new ArrayList<Integer>();
        child_players = new ArrayList<Integer>();
        finance = 0; 
        explore = 0; 
        exploit = 0; 
        f_change = 0;
        explore_change = 0;
        exploit_change = 0;
        show_finance = false;
        show_explore = false;
        show_exploit = false;
        switch_map = new HashMap<String, Boolean>();
    }
 
    public void generateResources(String r) {
        role = r;
        int finance_add = 0; int finance_min = 0;
        int explore_add = 0; int explore_min = 0;
        int exploit_add = 0; int exploit_min = 0;
        if (r.equals("R")) { //R&D
            finance_add = 4; finance_min = 1; //Max 5
            explore_add = 20; explore_min = 10; //Max 30
            exploit_add = 5; exploit_min = 5; //Max 10
        }
        else if (r.equals("M")) { //Manufacturing
            finance_add = 10; finance_min = 5; //Max 15
            explore_add = 10; explore_min = 5; //Max 15
            exploit_add = 10; exploit_min = 5; //Max 15
            
        }
        else if (r.equals("F")) { //Financial Backer
            finance_add = 20; finance_min = 10; //Max 30
            explore_add = 4; explore_min = 1; //Max 5
            exploit_add = 5; exploit_min = 5; //Max 10
        }
        finance = (int) (Math.random()*finance_add) + finance_min;
        explore = (int) (Math.random()*explore_add) + explore_min;
        exploit = (int) (Math.random()*exploit_add) + exploit_min;
        
    }
    
    public void addSwitch(String switch_name) {
        switch_map.put(switch_name, false);
    }
    
    public String getRole() {
        return role;
    }
    
    /**
     * A one in a kind role modifier that changes a player's role to Manufacturer.
     * Only occurs when a joint venture is made.
     */
    public void jointRole() {
        role = "M";
    }
    
    public boolean getDead() {
        return dead;
    }
    
    public void setDead() {
        dead = true;
    }
    
    public void useJoint() {
        joint_available = false;
    }
    
    public void resetJoint() {
        joint_available = true;
    }
    
    public boolean isJointAvailable() {
        return joint_available;
    }
}
