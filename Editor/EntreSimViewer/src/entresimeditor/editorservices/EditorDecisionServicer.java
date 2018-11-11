/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor.editorservices;

import entreObj.Decision;
import entresimeditor.Objects.Data.Effect;
import entresimeditor.Objects.Data.EffectSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.StringTokenizer;
import java.util.TreeSet;
import javafx.collections.ObservableList;

/**
 * A class that contains methods to read decisions for the Editor UI. It
 * performs various misc tasks like converting Decision properties to a readable
 * format for the UI.
 *
 * @author dylanleong
 */
public class EditorDecisionServicer {

    public void fillEffects(ArrayList<String> effects, ObservableList<Integer> effect_set, HashMap<Integer, EffectSet> effect_map) {

        if (effects.size() == 1 && effects.contains("finance 0 0 false")) { //this is an empty effect that we shouldn't bother showing
            return;
        }
        int effect_incrementing = 0;

        //LOOP FOR EVERY EFFECT IN ARRAYLIST AND STRINGTOKENIZE
        for (String each_effect : effects) {

            effect_set.add(effect_incrementing);
            effect_map.put(effect_incrementing, new EffectSet());

            StringTokenizer st = new StringTokenizer(each_effect);
            if (st.countTokens() > 4) { //Probability case
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();
                effect_map.get(effect_incrementing).probability = Double.parseDouble(probability);

                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();
                    
                    effect_map.get(effect_incrementing).effect_list.add(new Effect(getResource(resource), Integer.parseInt(magnitude), Integer.parseInt(delay), Boolean.parseBoolean(linked)));

                } while (st.countTokens() >= 4);

            } else if (st.countTokens() == 4) { //Normal case
                effect_map.get(effect_incrementing).probability = 1.0;
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                effect_map.get(effect_incrementing).effect_list.add(new Effect(getResource(resource), Integer.parseInt(magnitude), Integer.parseInt(delay), Boolean.parseBoolean(linked)));

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                effect_map.get(effect_incrementing).probability = 1.0;
                String special = st.nextToken();
                String effect = "";

                if (special.equals("join")) {
                    effect_map.get(effect_incrementing).effect_list.add(new Effect("Joint Venture Effect", 0, 0, false));
                } else if (special.equals("jointgrant")) {
                    effect_map.get(effect_incrementing).probability = 0.5;
                    effect_map.get(effect_incrementing).effect_list.add(new Effect("Joint Grant Effect", 0, 0, false));
                }

            }
            effect_incrementing++;
        } //endfor
        
    }

    /**
     * Reads the preqreqs of a decision and converts them to a UI-readable form.
     *
     * @param prereqs of a decision
     * @param must includes decisions that must be made
     * @param not includes decisions that must NOT be made
     * @param min_resources
     * @param applicant_roles
     * @param recipient_roles
     * @return false if there is no joinprereq condition, and true if there is.
     */
    public boolean fillPrereqs(ArrayList<String> prereqs, ArrayList<Integer> must, ArrayList<Integer> not, int[] min_resources, boolean[] applicant_roles, boolean[] recipient_roles) {
        boolean join = false;

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

            switch (type) {
                case "D":
                    must.add(Integer.parseInt(magnitude));
                    break;
                case "N":
                    not.add(Integer.parseInt(magnitude));
                    break;
                case "finance":
                    min_resources[0] = Integer.parseInt(magnitude);
                    break;
                case "explore":
                    min_resources[1] = Integer.parseInt(magnitude);
                    break;
                case "exploit":
                    min_resources[2] = Integer.parseInt(magnitude);
                    break;
                case "a_role":
                    if (magnitude.equals("R")) {
                        applicant_roles[0] = true; //R&D
                        applicant_roles[1] = false; //MANUFACTURER
                        applicant_roles[2] = false; //FINANCIAL BACKER
                    } else if (magnitude.equals("M")) {
                        applicant_roles[0] = false; //R&D
                        applicant_roles[1] = true; //MANUFACTURER
                        applicant_roles[2] = false; //FINANCIAL BACKER
                    } else if (magnitude.equals("F")) {
                        applicant_roles[0] = false; //R&D
                        applicant_roles[1] = false; //MANUFACTURER
                        applicant_roles[2] = true; //FINANCIAL BACKER
                    }
                    break;
                case "r_role":
                    if (magnitude.equals("R")) {
                        recipient_roles[0] = true; //R&D
                        recipient_roles[1] = false; //MANUFACTURER
                        recipient_roles[2] = false; //FINANCIAL BACKER
                    } else if (magnitude.equals("M")) {
                        recipient_roles[0] = false; //R&D
                        recipient_roles[1] = true; //MANUFACTURER
                        recipient_roles[2] = false; //FINANCIAL BACKER
                    } else if (magnitude.equals("F")) {
                        recipient_roles[0] = false; //R&D
                        recipient_roles[1] = false; //MANUFACTURER
                        recipient_roles[2] = true; //FINANCIAL BACKER
                    }
                    break;
                case "a_n_role":
                    if (magnitude.equals("R")) {
                        applicant_roles[0] = false; //R&D
                        applicant_roles[1] = true; //MANUFACTURER
                        applicant_roles[2] = true; //FINANCIAL BACKER
                    } else if (magnitude.equals("M")) {
                        applicant_roles[0] = true; //R&D
                        applicant_roles[1] = false; //MANUFACTURER
                        applicant_roles[2] = true; //FINANCIAL BACKER
                    } else if (magnitude.equals("F")) {
                        applicant_roles[0] = true; //R&D
                        applicant_roles[1] = true; //MANUFACTURER
                        applicant_roles[2] = false; //FINANCIAL BACKER
                    }
                    break;
                case "r_n_role":
                    if (magnitude.equals("R")) {
                        recipient_roles[0] = false; //R&D
                        recipient_roles[1] = true; //MANUFACTURER
                        recipient_roles[2] = true; //FINANCIAL BACKER
                    } else if (magnitude.equals("M")) {
                        recipient_roles[0] = true; //R&D
                        recipient_roles[1] = false; //MANUFACTURER
                        recipient_roles[2] = true; //FINANCIAL BACKER
                    } else if (magnitude.equals("F")) {
                        recipient_roles[0] = true; //R&D
                        recipient_roles[1] = true; //MANUFACTURER
                        recipient_roles[2] = false; //FINANCIAL BACKER
                    }
                    break;
                case "joinprereq":
                    join = true;
                    break;
                default:
                    break;
            } //endswitch
        } //endfor

        return join;
    }

    /**
     * Grab a decision via an ID.
     *
     * @param id of decision
     * @return corresponding decision
     */
    public Decision getDecision(TreeSet<Decision> d, int decision_id) {
        for (Decision each : d) {
            if (each.id == decision_id) {
                return each;
            }
        }
        return null; //This should never ever happen
    }

    /**
     * Change one letter role abbreviations to their full name.
     *
     * @param r the abbreviation
     * @return the full name of the role
     */
    public String getRole(String r) {
        if (r.equals("R")) {
            return "R&D";
        } else if (r.equals("M")) {
            return "Manufacturer";
        } else if (r.equals("F")) {
            return "Financial backer";
        } else {
            return null; //Hopefully we never ever see this (maybe I should end the program if this is the case)
        }
    }

    /**
     * Change abbreviated resource names to their full name.
     */
    public String getResource(String r) {
        if (r.equals("finance")) {
            return "Finance";
        } else if (r.equals("explore")) {
            return "Exploratory Knowledge";
        } else if (r.equals("exploit")) {
            return "Exploitative Knowledge";
        } else if (r.equals("f_change")) {
            return "Income";
        } else if (r.equals("explore_change")) {
            return "Exploratory Knowledge per Turn";
        } else if (r.equals("exploit_change")) {
            return "Exploitative Knowledge per Turn";
        } else {
            return null;
        }
    }

}
