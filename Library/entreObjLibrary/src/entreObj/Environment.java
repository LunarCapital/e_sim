package entreObj;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author dylanleong
 */
public class Environment implements Serializable {

    public ArrayList<PlayerDetails> players;
    public HashMap<Integer, PlayerDetails> int_to_player; //A few iterations in and 2-3 months later and I'm not sure if I need these
    public HashMap<PlayerDetails, Integer> player_to_int;
    public HashMap<String, Integer> name_to_int;
    public int[][] network;
    public boolean[][] joint_graph;

    public Environment(ArrayList<PlayerDetails> p) {
        players = p;
        int_to_player = new HashMap<Integer, PlayerDetails>();
        player_to_int = new HashMap<PlayerDetails, Integer>();
        name_to_int = new HashMap<String, Integer>();
        for (int i = 0; i < players.size(); i++) {
            int_to_player.put(p.get(i).id, p.get(i)); //Construct two-way hashmaps linking player id to player
            player_to_int.put(p.get(i), p.get(i).id);
            name_to_int.put(p.get(i).name, p.get(i).id);
        }
        network = new int[players.size()][players.size()]; //Here's our adj matrix that will most likely never get above 1000
        for (int i = 0; i < network.length; i++) {
            for (int j = 0; j < network.length; j++) {
                network[i][j] = -1;
            }
        }
        joint_graph = new boolean[players.size()][players.size()];
        for (int i = 0; i < joint_graph.length; i++) {
            for (int j = 0; j < joint_graph.length; j++) {
                joint_graph[i][j] = false;
            }
        }
        
        //Distribution of roles is 0.5:0.2:0.3
        ArrayList<PlayerDetails> shuffled_players = new ArrayList<PlayerDetails>(players);
        Collections.shuffle(shuffled_players);
        int no_of_players = players.size();
        
        int no_of_M = 0;
        if (no_of_players >= 6) no_of_M = (int) ((0.3*no_of_players < 2) ? 2 : Math.floor(0.3*no_of_players)); //Manufacturers 
        else no_of_M = 1;
        
        int no_of_F = 0;
        if (no_of_players >= 6) no_of_F = (int) ((0.2*no_of_players < 2) ? 2 : Math.round(0.2*no_of_players)); //Financial Backers
        else no_of_F = 1;
        
        int no_of_R = players.size() - no_of_M - no_of_F; //Research and Development
        
        int count = 0;
        for (PlayerDetails each : shuffled_players) {
            if (count < no_of_R) {
                each.generateResources("R");
            }
            else if (count < no_of_R + no_of_F) {
                each.generateResources("F");
            }
            else if (count >= no_of_R + no_of_F) {
                each.generateResources("M");
            }
            count++;
        }
    }

    /**
     * Return a PlayerDetails object based off an ID
     * @param player_id the id of the player that we want
     * @return the corresponding PlayerDetail
     */
    public PlayerDetails getPlayer(int player_id) {
        for (PlayerDetails each : players) {
            if (player_id == each.id) {
                return each;
            }
        }
        return null;
    }
    
}
