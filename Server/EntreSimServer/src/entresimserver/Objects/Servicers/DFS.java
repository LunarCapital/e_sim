/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Servicers;

import entreObj.Environment;
import java.util.ArrayList;
import java.util.Stack;

/**
 *
 * @author dylanleong
 */
public class DFS {

    public ArrayList<ArrayList<Integer>> getJointGroups(Environment env) {
        Stack<Integer> stack = new Stack<Integer>();
        ArrayList<Integer> visited = new ArrayList<Integer>();
        ArrayList<ArrayList<Integer>> connected_groups = new ArrayList<ArrayList<Integer>>();
        int players_size = env.players.size();

        while (visited.size() != players_size) {
            int first_found_unvisited_node = findUnvisited(visited);

            stack.push(first_found_unvisited_node);
            addToGroup(connected_groups, first_found_unvisited_node, first_found_unvisited_node);

            while (!stack.empty()) {
                int node_being_examined = stack.pop();
                if (!visited.contains(node_being_examined)) {
                    visited.add(node_being_examined);
                    for (int i = 0; i < players_size; i++) {
                        if (env.joint_graph[node_being_examined][i]) {
                            stack.push(i);
                            addToGroup(connected_groups, i, node_being_examined);
                        }
                    }
                }
            }
        } //end_dfs
        return connected_groups;
    }

    private int findUnvisited(ArrayList<Integer> visited) {
        int first_found_unvisited_node = 0;
        boolean unvisited_node_found = false;
        while (!unvisited_node_found) {
            if (visited.contains(first_found_unvisited_node)) {
                first_found_unvisited_node++;
            } else {
                unvisited_node_found = true;
                break;
            }
        }
        return first_found_unvisited_node;
    }

    private void addToGroup(ArrayList<ArrayList<Integer>> connected_groups, int node, int parent) {
        if (node == parent) { //ADDING A NEW GROUP
            boolean already_added = false;
            for (ArrayList<Integer> each_group : connected_groups) {
                if (each_group.contains(node)) {
                    already_added = true;
                    break;
                }
            }
            
            if (!already_added) {
                int index = connected_groups.size();
                connected_groups.add(new ArrayList<Integer>());
                connected_groups.get(index).add(node);
            }
            
        } else { //ADD NODE TO EXISTING GROUP
            boolean already_added = false;
            int index = 0;
            for (int i = 0; i < connected_groups.size(); i++) {
                if (connected_groups.get(i).contains(parent)) {
                    index = i;
                }
                if (connected_groups.get(i).contains(node)) {
                    already_added = true;
                    break;
                }
            }
            
            if (!already_added) {
                connected_groups.get(index).add(node);
            }
        } //else
    } //func
}
