/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entreObj;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Data containing the win conditions.
 *
 * @author dylanleong
 */
public class WinData implements Serializable, Comparable<WinData> {

    private int score;
    public final int player_id;

    public final int init_finance;
    public final int init_explore;
    public final int init_exploit;

    public WinData(PlayerDetails player) {
        init_finance = player.finance;
        init_explore = player.explore;
        init_exploit = player.exploit;
        player_id = player.id;
        score = 0;
    }

    public void calculateChange(ArrayList<PlayerDetails> list_of_players, PlayerDetails player, ArrayList<ArrayList<Integer>> connected_groups) {
        int size_of_our_group = 1;
        int index_of_our_group = 0;
        for (int i = 0; i < connected_groups.size(); i++) {
            ArrayList<Integer> each_group = connected_groups.get(i);
            if (each_group.contains(player.id)) {
                size_of_our_group = each_group.size();
                index_of_our_group = i;
                break;
            }
        }

        if (size_of_our_group == 1) { //PLAYER WITHOUT JOINT VENTURE
            int change_in_resources = 0;

            change_in_resources += player.finance - init_finance;
            change_in_resources += player.explore - init_explore;
            change_in_resources += player.exploit - init_exploit;
            //change_in_resources += player.f_change - init_f_change; //f_change is how a resource changes over time

            score = change_in_resources;
        } else { //PLAYER WITH JOINT VENTURE
            PlayerDetails strongest_player = findJointPlayer(list_of_players, connected_groups.get(index_of_our_group));
            if (strongest_player != null) {
                System.out.println("Strongest is : " + strongest_player.name);
                int change_in_resources = 0;

                System.out.println("Size of group is: " + size_of_our_group);
                
                int divided_finance = strongest_player.finance / size_of_our_group;
                int divided_explore = strongest_player.explore / size_of_our_group;
                int divided_exploit = strongest_player.exploit / size_of_our_group;

                System.out.println(strongest_player.finance + " compared to " + divided_finance);

                change_in_resources += divided_finance - init_finance;
                change_in_resources += divided_explore - init_explore;
                change_in_resources += divided_exploit - init_exploit;

                score = change_in_resources;
            } else { //somehow everyone in the joint group lost all of their resources
                int change_in_resources = 0;

                int divided_finance = 0;
                int divided_explore = 0;
                int divided_exploit = 0;

                change_in_resources += divided_finance - init_finance;
                change_in_resources += divided_explore - init_explore;
                change_in_resources += divided_exploit - init_exploit;

                score = change_in_resources;
            }
        }
    }

    private PlayerDetails findJointPlayer(ArrayList<PlayerDetails> list_of_all_players, ArrayList<Integer> connected_group_of_players) {
        PlayerDetails strongest_player = null;
        for (Integer each_id : connected_group_of_players) {
            PlayerDetails player_being_examined = null;
            for (PlayerDetails each_player : list_of_all_players) {
                if (each_player.id == each_id) {
                    player_being_examined = each_player;
                    break;
                }
            }
            if (player_being_examined != null) {
                if (player_being_examined.exploit != 0 || player_being_examined.explore != 0 || player_being_examined.finance != 0) {
                    strongest_player = player_being_examined;
                    break;
                }
            }

        } //loop_group
        return strongest_player;
    }

    public int getScore() {
        return score;
    }

    @Override
    public int compareTo(WinData o) {
        if (this.score > o.score) {
            return 1;
        } else if (this.score == o.score) {
            if (this.player_id > o.player_id) {
                return 1;
            } else {
                return -1;
            }
        } else {
            return -1;
        }
    }

}
