/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Objects;

import entreObj.Decision;
import entreObj.Environment;
import entreObj.PlayerDetails;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javafx.util.Pair;

/**
 * An object with the capability to check the prerequisites of a button.
 *
 * @author dylanleong
 */
public class PrereqChecker {

    private final TreeSet<Decision> decisions;

    public PrereqChecker(TreeSet<Decision> d) {
        decisions = d;
    }

    /**
     * Checks prerequisites one by one, compiling reasons for failure and either
     * returns an empty string (success) or a string detailing why the decision
     * could not be made.
     *
     * @param prereqs
     * @param env
     * @param my_name
     * @param recipient_name
     * @return
     */
    public String checkPrerequisites(ArrayList<String> prereqs, Environment env, String my_name, String recipient_name) {

        boolean prerequisites_met = true; //innocent until proven guilty
        PlayerDetails me = env.getPlayer(env.name_to_int.get(my_name));
        PlayerDetails recipient = (recipient_name.equals("")) ? null : env.getPlayer(env.name_to_int.get(recipient_name));

        System.out.println("I am : " + me.name);
        if (recipient != null) {
            System.out.println("Recipient is : " + recipient.name);
        }

        System.out.println("I have made decisions: " + me.successful_decisions);
        StringBuilder failure_reason = new StringBuilder("");

        for (String each_prereq : prereqs) {
            if (each_prereq.trim().equals("none")) {
                break; //break early if no prereqs
            }
            StringTokenizer st = new StringTokenizer(each_prereq);
            String type = st.nextToken();
            String magnitude;
            if (type.equals("joinprereq")) {
                magnitude = "0";
            } else {
                magnitude = st.nextToken();
            }

            System.out.println("Type is " + type + " and magnitude is " + magnitude);

            switch (type) {
                case "D": //Must have made decision [magnitude] before
                    if (!me.successful_decisions.contains(Integer.parseInt(magnitude))) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("You have not yet made the decision '" + getDecision(Integer.parseInt(magnitude)).title + "'.\n");
                        prerequisites_met = false;
                    }
                    break;
                case "N": //Must NOT have made decision [magnitude] before
                    if (me.successful_decisions.contains(Integer.parseInt(magnitude))) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("You have made the decision '" + getDecision(Integer.parseInt(magnitude)).title + "'.\n");
                        prerequisites_met = false;
                    }
                    break;
                case "finance": //Requires at least [magnitude] finance
                    if (me.finance < Integer.parseInt(magnitude)) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("Your finance is below " + Integer.parseInt(magnitude) + ".\n");
                        prerequisites_met = false;
                    }
                    break;
                case "explore": //Requires at least [magnitude] exploratory knowledge
                    if (me.explore < Integer.parseInt(magnitude)) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("Your exploratory knowledge is below " + Integer.parseInt(magnitude) + ".\n");
                        prerequisites_met = false;
                    }
                    break;
                case "exploit": //Requires at least [magnitude] exploitative knowledge
                    if (me.exploit < Integer.parseInt(magnitude)) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("Your exploratory knowledge is below " + Integer.parseInt(magnitude) + ".\n");
                        prerequisites_met = false;
                    }
                    break;
                case "f_change": //Requires at least [magnitude] financial income
                    if (me.f_change < Integer.parseInt(magnitude)) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("Your income is below " + Integer.parseInt(magnitude) + ".\n");
                        prerequisites_met = false;
                    }
                    break;
                case "a_role": //Role of this player must be [magnitude]
                    if (!me.getRole().equals(magnitude)) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("You must be a " + getRole(magnitude) + ", but you are a " + getRole(me.getRole()) + ".\n");
                        prerequisites_met = false;
                    }
                    break;
                case "r_role": //Role of recipient player must be [magnitude]
                    if (recipient != null) {
                        if (!recipient.getRole().equals(magnitude)) {
                            failure_reason.append("\n\nCould not make decision because: \n");
                            failure_reason.append("Your recipient must be a " + getRole(magnitude) + ", but they are a " + getRole(recipient.getRole()) + ".\n");
                            prerequisites_met = false;
                        }
                    } else {
                        prerequisites_met = false;
                    }
                    break;
                case "a_n_role": //role of this player must NOT be [magnitude]
                    if (me.getRole().equals(magnitude)) {
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("You must NOT be a " + getRole(magnitude) + " to make this decision.");
                        prerequisites_met = false;
                    }
                    break;
                case "r_n_role": //role of recipient player must NOT be [magnitude]
                    if (recipient != null) {
                        if (recipient.getRole().equals(magnitude)) {
                            failure_reason.append("\n\nCould not make decision because: \n");
                            failure_reason.append("Your recipient must NOT be a " + getRole(magnitude) + ".");
                            prerequisites_met = false;
                        }
                    } else {
                        prerequisites_met = false;
                    }
                    break;
                case "joinprereq": //Special prereq specifically for joint ventures
                    if (recipient == null) {
                        prerequisites_met = false;
                    } else {
                        String my_role = me.getRole();
                        String recipient_role = recipient.getRole();
                        System.out.println("I'm " + my_role + ", rec is " + recipient_role);
                        if (my_role.equals(recipient_role)) { //If same role, or...

                        } else if (my_role.equals("R") && recipient_role.equals("M")) { //R&D w/ Manufacturer pairing...

                        } else if (my_role.equals("M") && recipient_role.equals("R")) { //...joint prereqs are reached.

                        } //excsue this else-if logic
                        else {
                            failure_reason.append("\n\nCould not make decision because: \n");
                            failure_reason.append("You need to be an R&D-R&D, Manufacturer-Manufacturer, FB-FB, or R&D-Manufacturer pair.");
                            prerequisites_met = false;
                        }
                    }
                    if (me.exploit < 8) { //Initiating player needs at least 8 exploitative knowledge
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("You need to have more than 8 exploitative knowledge.");
                        prerequisites_met = false;
                    }
                    if ((me.finance + me.explore + me.exploit + recipient.finance + recipient.explore + recipient.exploit) < 20) { //Both players need at least 20 overall resources
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("Your resources added to the recipient's resources needs to exceed a value of 20.");
                        prerequisites_met = false;
                    }
                    if (me.finance < 10 && recipient.finance < 10) { //at least one player needs more than 10 finance
                        failure_reason.append("\n\nCould not make decision because: \n");
                        failure_reason.append("At least one player needs more than 10 finance.");
                        prerequisites_met = false;
                    }
                    break;
                default:
                    break;
            }

            if (!prerequisites_met) {
                break; //No need to check further
            }
        }

        //return prerequisites_met;
        if (prerequisites_met) { //Everything is ok
            return "";
        } else {
            return failure_reason.toString();
        }
    }

    public String checkNegativeKnowledge(Decision decision, Environment env, String my_name) {
        if (decision.type.equals("Indiv")) {
            Pair<Integer, Integer> total_cost = totalKnowledgeCost(decision.applicant_effects);
            int total_explore_cost = total_cost.getKey();
            int total_exploit_cost = total_cost.getValue();

            PlayerDetails me = env.getPlayer(env.name_to_int.get(my_name));

            if (me.explore < total_explore_cost || me.exploit < total_exploit_cost) {
                return "This decision can cost more knowledge than you presently have.";
            } else {
                return "";
            }
        } else {
            Pair<Integer, Integer> total_applicant_cost = totalKnowledgeCost(decision.applicant_effects);
            Pair<Integer, Integer> total_both_cost = totalKnowledgeCost(decision.both_effects);
            int total_explore_cost = total_applicant_cost.getKey() + total_both_cost.getKey();
            int total_exploit_cost = total_applicant_cost.getValue() + total_both_cost.getValue();

            PlayerDetails me = env.getPlayer(env.name_to_int.get(my_name));

            if (me.explore < total_explore_cost || me.exploit < total_exploit_cost) {
                return "This decision can cost more knowledge than you presently have.";
            } else {
                return "";
            }
        }
    }

    public boolean checkNegativeJointKnowledge(Decision decision, Environment env, String my_name) {
        Pair<Integer, Integer> total_recipient_cost = totalKnowledgeCost(decision.recipient_effects);
        Pair<Integer, Integer> total_both_cost = totalKnowledgeCost(decision.both_effects);
        int total_explore_cost = total_recipient_cost.getKey() + total_both_cost.getKey();
        int total_exploit_cost = total_recipient_cost.getValue() + total_both_cost.getValue();

        PlayerDetails me = env.getPlayer(env.name_to_int.get(my_name));

        if (me.explore < total_explore_cost || me.exploit < total_exploit_cost) {
            return false;
        } else {
            return true;
        }
    }

    public boolean checkRoleRestricted(ArrayList<String> prereqs, Environment env, String name) {
        PlayerDetails me = env.getPlayer(env.name_to_int.get(name));
        boolean prerequisites_met = true; //true until proven false

        for (String each_prereq : prereqs) {
            if (each_prereq.trim().equals("none")) {
                return true; //No restrictions, nothing to worry about
            }

            StringTokenizer st = new StringTokenizer(each_prereq);
            String type = st.nextToken();
            String magnitude;
            if (type.equals("joinprereq")) {
                magnitude = "0";
            } else {
                magnitude = st.nextToken();
            }

            if (type.equals("a_role")) {
                if (!me.getRole().equals(magnitude)) {
                    prerequisites_met = false;
                }
            } else if (type.equals("a_n_role")) {
                if (me.getRole().equals(magnitude)) {
                    prerequisites_met = false;
                }
            }
        }
        return prerequisites_met;
    }

    /**
     * Change one letter role abbreviations to their full name.
     *
     * @param r the abbreviation
     * @return the full name of the role
     */
    private String getRole(String r) {
        if (r.equals("R")) {
            return "R&D";
        } else if (r.equals("M")) {
            return "manufacturer";
        } else if (r.equals("F")) {
            return "financial backer";
        } else {
            return null; //Hopefully we never ever see this (maybe I should end the program if this is the case)
        }
    }

    /**
     * Grab a decision via an ID.
     *
     * @param id of decision
     * @return corresponding decision
     */
    private Decision getDecision(int id) {
        for (Decision each : decisions) {
            if (each.id == id) {
                return each;
            }
        }
        return null; //if we find none
    }

    private Pair<Integer, Integer> totalKnowledgeCost(ArrayList<String> effects) {
        int total_explore_cost = 0;
        int total_exploit_cost = 0;

        if (effects.size() == 1 && effects.contains("finance 0 0 false")) { //this is an empty effect that we shouldn't bother showing
            return new Pair<Integer, Integer>(0, 0);
        }

        //LOOP FOR EVERY EFFECT IN ARRAYLIST AND STRINGTOKENIZE
        for (String each_effect : effects) {
            StringTokenizer st = new StringTokenizer(each_effect);
            if (st.countTokens() > 4) { //Probability case
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();

                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    if (resource.equals("explore")) {
                        total_explore_cost -= Integer.valueOf(magnitude);
                    } else if (resource.equals("exploit")) {
                        total_exploit_cost -= Integer.valueOf(magnitude);
                    }

                } while (st.countTokens() >= 4);
            } else if (st.countTokens() == 4) { //Normal case
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                if (resource.equals("explore")) {
                    total_explore_cost -= Integer.valueOf(magnitude);
                } else if (resource.equals("exploit")) {
                    total_exploit_cost -= Integer.valueOf(magnitude);
                }

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                continue;
            }
        }
        return new Pair<Integer, Integer>(total_explore_cost, total_exploit_cost);
    }

}
