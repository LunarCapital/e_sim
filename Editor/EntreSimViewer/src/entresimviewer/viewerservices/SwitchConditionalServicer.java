/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer.viewerservices;

import entresimviewer.Objects.Data.ConditionalData;
import entresimviewer.Objects.Data.Switch;
import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * A class that does conditional-based services for the viewer UI, including
 * comparisons and checks.
 *
 * @author dylanleong
 */
public class SwitchConditionalServicer {

    /**
     * Makes changes to a decision's effects specified by a conditional switch
     * (if one exists).
     *
     * @return
     */
    public ArrayList<String> changeConditional(int decision_id, ArrayList<ConditionalData> conditional_data, ArrayList<String> replace_effects, String type) {
        ArrayList<String> dummy_effects = new ArrayList<String>();
        dummy_effects.addAll(replace_effects);

        for (ConditionalData each_conditional : conditional_data) {
            if (each_conditional.decision_id == decision_id) { //if the decision is changed by a conditional
                if (type.equals("applicant")) {
                    if (each_conditional.applicant_type.equals("A")) {
                        if (!each_conditional.applicant_changes.contains("nothing")) {
                            dummy_effects.addAll(each_conditional.applicant_changes);
                        }
                    } else if (each_conditional.applicant_type.equals("R")) {
                        for (String each_change : each_conditional.applicant_changes) {
                            //use first token as index (minus one)
                            StringTokenizer st = new StringTokenizer(each_change);
                            String index_str = st.nextToken();
                            int index = Integer.parseInt(index_str);
                            String remaining_change = each_change.substring(index_str.length() + 1, each_change.length());
                            dummy_effects.set(index - 1, remaining_change);
                        }
                    }
                } else if (type.equals("recipient")) {
                    if (each_conditional.recipient_type.equals("A")) {
                        if (!each_conditional.recipient_changes.contains("nothing")) {
                            dummy_effects.addAll(each_conditional.recipient_changes);
                        }
                    } else if (each_conditional.recipient_type.equals("R")) {
                        for (String each_change : each_conditional.recipient_changes) {
                            //use first token as index (minus one)
                            StringTokenizer st = new StringTokenizer(each_change);
                            String index_str = st.nextToken();
                            int index = Integer.parseInt(index_str);
                            String remaining_change = each_change.substring(index_str.length() + 1, each_change.length());
                            dummy_effects.set(index - 1, remaining_change);
                        }
                    }
                } else if (type.equals("both")) {
                    if (each_conditional.both_type.equals("A")) {
                        if (!each_conditional.both_changes.contains("nothing")) {
                            dummy_effects.addAll(each_conditional.both_changes);
                        }
                    } else if (each_conditional.both_type.equals("R")) {
                        for (String each_change : each_conditional.both_changes) {
                            //use first token as index (minus one)
                            StringTokenizer st = new StringTokenizer(each_change);
                            String index_str = st.nextToken();
                            int index = Integer.parseInt(index_str);
                            String remaining_change = each_change.substring(index_str.length() + 1, each_change.length());
                            dummy_effects.set(index - 1, remaining_change);
                        }
                    }
                }
            }
        }
        return dummy_effects;
    }

    /**
     * Deletes a switch from a list of switches, and deletes all its conditionals from conditional data list.
     * @param switches list of switches
     * @param conditional_data list of conditional data
     * @param switch_name name of switch to delete
     */
    public void deleteSwitch(ArrayList<Switch> switches, ArrayList<ConditionalData> conditional_data, String switch_name) {
        Switch to_be_deleted = getSwitch(switches, switch_name);
        switches.remove(to_be_deleted);
        
        ArrayList<ConditionalData> removable_conditionals = new ArrayList<ConditionalData>();
        for (ConditionalData each_conditional : conditional_data) {
            if (each_conditional.name.equals(switch_name)) {
                removable_conditionals.add(each_conditional);
            }
        }
        
        conditional_data.removeAll(removable_conditionals);        
    }
    
    
    /**
     * Grab a switch via the name
     *
     * @param conditional_data list of conditionals to search
     * @param conditional_name of conditional we search for
     * @return corresponding conditional
     */
    public Switch getSwitch(ArrayList<Switch> switches, String switch_name) {
        for (Switch each : switches) {
            if (each.getName().equals(switch_name)) {
                return each;
            }
        }
        return null;
    }

}
