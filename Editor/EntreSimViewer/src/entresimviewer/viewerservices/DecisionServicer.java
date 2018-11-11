/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimviewer.viewerservices;

import entreObj.Decision;
import entresimviewer.Objects.View.ViewEffect;
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * A class that does decision-related checks and conversions for the Viewer UI.
 *
 * @author dylanleong
 */
public class DecisionServicer {

    /**
     * Convert the abbreviated prereq string to something readable for the user.
     *
     * @param prereqs Array of prereqs specific to one decision
     * @param d_set The treeset of decisions (so we can find IDs for "D" and "N"
     * prereqs)
     * @return an arraylist of readable strings, one for each prereq.
     */
    public ArrayList<String> convertPrereqsReadable(ArrayList<String> prereqs, TreeSet<Decision> d_set) {
        ArrayList<String> readable = new ArrayList<String>();

        for (String each_prereq : prereqs) {
            if (each_prereq.trim().equals("none")) {
                readable.add("No prerequisites.");
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
                    readable.add("Must have made decision '" + getDecision(d_set, Integer.valueOf(magnitude)).title + "'.");
                    break;
                case "N":
                    readable.add("Must not have made decision '" + getDecision(d_set, Integer.valueOf(magnitude)).title + "'.");
                    break;
                case "finance":
                    readable.add("Need at least " + magnitude + " units of finance.");
                    break;
                case "explore":
                    readable.add("Need at least " + magnitude + " of exploratory knowledge.");
                    break;
                case "exploit":
                    readable.add("Need at least " + magnitude + " of exploitative knowledge.");
                    break;
                case "a_role":
                    readable.add("Applicant must be a " + getRole(magnitude) + ".");
                    break;
                case "r_role":
                    readable.add("Recipient must be a " + getRole(magnitude) + ".");
                    break;
                case "a_n_role":
                    readable.add("Applicant can be any role except " + getRole(magnitude) + ".");
                    break;
                case "r_n_role":
                    readable.add("Recipient can be any role except " + getRole(magnitude) + ".");
                    break;
                case "joinprereq":
                    readable.add("Joint Decision prerequisites:\n8 exploitative knowledge\nThe applicant and recipient's resources summed together must exceed 20\nAt least one player needs more than 10 finance\nThe two players must either be the same role, or an R&D-Manufacturer pair ");
                    break;
                default:
                    break;
            }

        }

        return readable;
    }

    /**
     * Convert the abbreviated effects string to a more readable form.
     *
     * @param effects is the arraylist of abbreviated effects
     * @return an arraylist of readable effects
     */
    public ArrayList<ViewEffect> convertEffectsReadable(ArrayList<String> effects, String type) {
        ArrayList<ViewEffect> readable_effects = new ArrayList<ViewEffect>();

        if (effects.size() == 1 && effects.contains("finance 0 0 false")) { //this is an empty effect that we shouldn't bother showing
            return readable_effects;
        }

        //LOOP FOR EVERY EFFECT IN ARRAYLIST AND STRINGTOKENIZE
        for (String each_effect : effects) {
            ViewEffect ve = new ViewEffect();
            ve.setAffected(type.trim()); //applicant, recipient, both
            StringTokenizer st = new StringTokenizer(each_effect);
            if (st.countTokens() > 4) { //Probability case
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();
                ve.setProbability(probability);

                StringBuilder effect = new StringBuilder("");
                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    effect.append("Changes " + getResource(resource) + " by " + magnitude + " ");
                    if (delay.equals("0")) {
                        effect.append("instantly ");
                    } else {
                        effect.append("in " + delay + " turns");
                    }
                    if (linked.equals("true")) {
                        effect.append(" and applies the same effect on any linked Financial Backers.  ");
                    } else {
                        effect.append(".  ");
                    }

                } while (st.countTokens() >= 4);

                ve.setEffect(effect.toString());
                readable_effects.add(ve);

            } else if (st.countTokens() == 4) { //Normal case
                ve.setProbability("1.0");
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                StringBuilder effect = new StringBuilder("");
                effect.append("Changes " + getResource(resource) + " by " + magnitude + " ");
                if (delay.equals("0")) {
                    effect.append("instantly ");
                } else {
                    effect.append("in " + delay + " turns");
                }
                if (linked.equals("true")) {
                    effect.append(" and applies the same effect on any linked Financial Backers.  ");
                } else {
                    effect.append(".  ");
                }
                ve.setEffect(effect.toString());
                readable_effects.add(ve);

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                ve.setProbability("1.0");
                String special = st.nextToken();
                String effect = "";

                if (special.equals("join")) {
                    effect = "Initiates a joint venture.";
                } else if (special.equals("jointgrant")) {
                    ve.setProbability("0.5");
                    effect = "The applicant is given 3 units of finance by the recipient.  If the probability roll is successful, grants the recipient 1 income and links them together with the applicant, allowing the recipient to gain benefits from linked decisions.";
                }

                ve.setEffect(effect);
                readable_effects.add(ve);
            }
        }
        return readable_effects;
    }

    public boolean isDeletionForbidden(TreeSet<Decision> decisions, int decision_id) {
        Decision decision = getDecision(decisions, decision_id);

        boolean forbidden = false;

        for (String each_effect : decision.applicant_effects) {
            if (each_effect.equals("join") || each_effect.equals("jointgrant")) {
                forbidden = true;
                break;
            }
        }
        for (String each_effect : decision.recipient_effects) {
            if (each_effect.equals("join") || each_effect.equals("jointgrant")) {
                forbidden = true;
                break;
            }
        }

        for (String each_effect : decision.both_effects) {
            if (each_effect.equals("join") || each_effect.equals("jointgrant")) {
                forbidden = true;
                break;
            }
        }

        return forbidden;
    }

    public void deleteDecision(TreeSet<Decision> decisions, int decision_id) {
        //Delete Decision
        Decision to_be_deleted = getDecision(decisions, decision_id);
        decisions.remove(to_be_deleted);

        //Delete any reference to other decisions
        for (Decision each_decision : decisions) {
            String must = "D " + decision_id;
            String not = "N " + decision_id;

            each_decision.prereqs.remove(must);
            each_decision.prereqs.remove(not);
        }
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
            return "manufacturer";
        } else if (r.equals("F")) {
            return "financial backer";
        } else {
            return null; //Hopefully we never ever see this (maybe I should end the program if this is the case)
        }
    }

    /**
     * Change abbreviated resource names to their full name.
     */
    public String getResource(String r) {
        if (r.equals("finance")) {
            return "finance";
        } else if (r.equals("explore")) {
            return "exploratory knowledge";
        } else if (r.equals("exploit")) {
            return "exploitative knowledge";
        } else if (r.equals("f_change")) {
            return "income";
        } else if (r.equals("explore_change")) {
            return "exploratory knowledge per turn";
        } else if (r.equals("exploit_change")) {
            return "exploitative knowledge per turn";
        } else {
            return null;
        }
    }

    /**
     * Iterates through decisions set and finds the lowest number that is also
     * an unused decision ID.
     *
     * @param decisions
     * @return a free, unused decision ID that is also the lowest number
     * possible
     */
    public int findFreeDecisionID(TreeSet<Decision> decisions) {
        int free_id = 0;
        boolean found = false;

        while (!found) {

            for (Decision each_decision : decisions) {
                found = true;
                if (each_decision.id == free_id) { //loop will only end if id is unused
                    found = false;
                    break;
                }
            }

            if (found) {
                break;
            } else {
                free_id++;
            }
        } //endwhile
        return free_id;
    }
}
