/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer.viewerservices;

import entreObj.Decision;
import entresimviewer.Objects.Data.ConditionalData;
import entresimviewer.Objects.Data.Switch;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * Object solely for the purpose of saving files.
 *
 * @author dylanleong
 */
public class FileSaver {

    /**
     * Goes through the decisions array and makes it into a readable string.
     *
     * @param decisions set of decisions
     * @return a long string containing decisions data.
     */
    public String decisionsToText(TreeSet<Decision> decisions) {
        StringBuilder sb = new StringBuilder("");

        for (Decision each_decision : decisions) {
            sb.append(each_decision.id + "\n");
            sb.append(each_decision.title + "\n");
            sb.append(each_decision.description); //\n is included in the description
            sb.append(each_decision.type + "\n");

            //print prereqs
            for (String each_prereq : each_decision.prereqs) {
                if (each_prereq.trim().equals("none")) {
                    sb.append("none");
                    break; //break early, no prerequisites.
                } else {
                    StringTokenizer st = new StringTokenizer(each_prereq);
                    String type = st.nextToken();
                    String magnitude;
                    if (type.equals("joinprereq")) {
                        sb.append(type + " ");
                    } else {
                        magnitude = st.nextToken();
                        sb.append(type + "=" + magnitude + " ");
                    }

                }
            }
            sb.append("\n");

            //print effects
            sb.append(effectsToText(each_decision.applicant_effects) + "\n");
            if (!each_decision.type.equals("Indiv")) {
                sb.append(effectsToText(each_decision.recipient_effects) + "\n");
                sb.append(effectsToText(each_decision.both_effects) + "\n");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Goes through an effects arraylist and converts it to string form.
     *
     * @param effects
     * @return a string of effects in the decisions file format.
     */
    private String effectsToText(ArrayList<String> effects) {
        StringBuilder sb = new StringBuilder("");

        for (String each_effect : effects) {
            StringTokenizer st = new StringTokenizer(each_effect);

            if (st.countTokens() > 4) { //Probability case, more than 4 tokens
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();
                sb.append("P=" + probability + "(");

                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    sb.append(resource + "=");
                    if (linked.equals("true")) {
                        sb.append("=");
                    }
                    sb.append(magnitude + ":" + delay);
                    if (st.countTokens() >= 4) {
                        sb.append(" ");
                    }

                } while (st.countTokens() >= 4);

                sb.append(")");
            } else if (st.countTokens() == 4) { //Normal case
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                sb.append(resource + "=");
                if (linked.equals("true")) {
                    sb.append("=");
                }
                sb.append(magnitude + ":" + delay);

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                String special = st.nextToken();
                sb.append(special);
            }
            sb.append(" ");
        } //endfor

        return sb.toString();
    }

    /**
     * Goes through the array of switches and converts it to a readable string,
     * savable to a file.
     *
     * @param switches array of switches
     * @return a long string containing switch data
     */
    public String switchesToText(ArrayList<Switch> switches) {
        StringBuilder sb = new StringBuilder("");

        for (Switch each_switch : switches) {
            sb.append(each_switch.getId() + "\n");
            sb.append(each_switch.getName() + "\n");
            sb.append("\n");
        }

        return sb.toString();
    }

    /**
     * Goes through the array of conditional data and converts it to a readable
     * string, which is to be saved to a file.
     *
     * @param conditional_data array of conditional data
     * @return string of conditional data
     */
    public String conditionalsToText(ArrayList<ConditionalData> conditional_data, TreeSet<Decision> decisions) {
        StringBuilder sb = new StringBuilder("");

        for (ConditionalData each_conditional : conditional_data) {
            sb.append(each_conditional.decision_id + "\n");
            sb.append(each_conditional.name + "\n");

            DecisionServicer decision_servicer = new DecisionServicer();
            Decision changed_decision = decision_servicer.getDecision(decisions, each_conditional.decision_id);

            if (changed_decision.type.equals("Indiv")) { //indiv
                sb.append(convertConditionalChanges(each_conditional.applicant_type, each_conditional.applicant_changes));
            } else { //joint
                sb.append(convertConditionalChanges(each_conditional.applicant_type, each_conditional.applicant_changes));
                sb.append(convertConditionalChanges(each_conditional.recipient_type, each_conditional.recipient_changes));
                sb.append(convertConditionalChanges(each_conditional.both_type, each_conditional.both_changes));
            }
            
            sb.append("\n");
            //Print for each of the effect categories:
            //Add or Replace
            //Corresponding effects

            //Remember that replace is complicated and needs a few numbers, etc
        }

        return sb.toString();
    }

    /**
     * Handles the conditional changes text conversion.
     *
     * @param type type of conditional change, either A or R (Add or replace
     * respectively)
     * @param changes conditional changes in string form
     * @return a string of converted conditional changes
     */
    private String convertConditionalChanges(String type, ArrayList<String> changes) {
        StringBuilder sb = new StringBuilder("");

        if (type.equals("A")) {
            sb.append("A\n");

            for (String each_change : changes) {
                StringTokenizer st = new StringTokenizer(each_change);

                if (st.countTokens() > 4) { //Probability case, more than 4 tokens
                    st.nextToken(); //Skip past the first "P"
                    String probability = st.nextToken();
                    sb.append("P=" + probability + "(");

                    do { //continue for every effect
                        String resource = st.nextToken();
                        String magnitude = st.nextToken();
                        String delay = st.nextToken();
                        String linked = st.nextToken();

                        sb.append(resource + "=");
                        if (linked.equals("true")) {
                            sb.append("=");
                        }
                        sb.append(magnitude + ":" + delay);
                        if (st.countTokens() >= 4) {
                            sb.append(" ");
                        }

                    } while (st.countTokens() >= 4);

                    sb.append(")");
                } else if (st.countTokens() == 4) { //Normal case
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    sb.append(resource + "=");
                    if (linked.equals("true")) {
                        sb.append("=");
                    }
                    sb.append(magnitude + ":" + delay);

                } else if (st.countTokens() == 1) { //SPECIAL CASE
                    String special = st.nextToken();
                    sb.append(special);
                }
            } //endfor
            sb.append("\n");
        } else {
            StringBuilder replace_changes_sb = new StringBuilder("");
            String replace_string_type = "R";

            for (String each_change : changes) {
                StringTokenizer st = new StringTokenizer(each_change);

                replace_string_type += " " + st.nextToken(); //Take out first token, which is a number

                if (st.countTokens() > 4) { //Probability case, more than 4 tokens
                    st.nextToken(); //Skip past the first "P"
                    String probability = st.nextToken();
                    replace_changes_sb.append("P=" + probability + "(");

                    do { //continue for every effect
                        String resource = st.nextToken();
                        String magnitude = st.nextToken();
                        String delay = st.nextToken();
                        String linked = st.nextToken();

                        replace_changes_sb.append(resource + "=");
                        if (linked.equals("true")) {
                            replace_changes_sb.append("=");
                        }
                        replace_changes_sb.append(magnitude + ":" + delay);
                        if (st.countTokens() >= 4) {
                            replace_changes_sb.append(" ");
                        }

                    } while (st.countTokens() >= 4);

                    replace_changes_sb.append(")");
                } else if (st.countTokens() == 4) { //Normal case
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    replace_changes_sb.append(resource + "=");
                    if (linked.equals("true")) {
                        replace_changes_sb.append("=");
                    }
                    replace_changes_sb.append(magnitude + ":" + delay);

                } else if (st.countTokens() == 1) { //SPECIAL CASE
                    String special = st.nextToken();
                    replace_changes_sb.append(special);
                }
                replace_changes_sb.append(" ");
            } //endfor
            sb.append(replace_string_type + "\n");
            sb.append(replace_changes_sb.toString() + "\n");
        }

        return sb.toString();
    }

}
