/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor.editorservices;

import entreObj.Decision;
import entresimeditor.Objects.Data.EditorListConditional;
import entresimeditor.Objects.Data.Effect;
import entresimeditor.Objects.Data.EffectSet;
import entresimviewer.Objects.View.ListDecision;
import entresimviewer.Objects.Data.ConditionalData;
import entresimviewer.Objects.Data.Switch;
import entresimviewer.Objects.View.ViewEffect;
import entresimviewer.viewerservices.DecisionServicer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeSet;

/**
 * A class that contains methods to handle switches and conditionals for the
 * editor UI. Performs various tasks like converting switches and conditionals
 * to a list format or a more readable format.
 *
 * @author dylanleong
 */
public class EditorSwitchConditionalServicer {

    public ArrayList<EditorListConditional> fillListConditionalArray(TreeSet<Decision> decisions, ArrayList<ConditionalData> conditional_data, Switch current_switch) {
        ArrayList<EditorListConditional> array_list_conditionals = new ArrayList<EditorListConditional>();
        DecisionServicer decision_servicer = new DecisionServicer();

        int count = 0;
        for (ConditionalData each_conditional : conditional_data) {
            if (each_conditional.name.equals(current_switch.getName())) { //MAKE NEW LIST CONDITIONAL
                EditorListConditional list_conditional = new EditorListConditional(count);
                Decision changed_decision = decision_servicer.getDecision(decisions, each_conditional.decision_id);
                ListDecision list_decision = new ListDecision(changed_decision.title, changed_decision.id);
                list_conditional.decision_to_be_changed = list_decision;
                list_conditional.type = changed_decision.type;

                int applicant_effects = changed_decision.applicant_effects.size();
                int recipient_effects = changed_decision.recipient_effects.size();
                int both_effects = changed_decision.both_effects.size();
                list_conditional.resetEffectSets(applicant_effects, recipient_effects, both_effects);

                if (list_conditional.type.equals("Indiv")) { //INDIVIDUAL
                    if (each_conditional.applicant_type.equals("A")) { //add
                        list_conditional.applicant_add_changes = true;
                        list_conditional.applicant_effect_sets.put(-1, fillAddEffects(each_conditional.applicant_changes));
                    } else { //replace
                        list_conditional.applicant_add_changes = false;
                        list_conditional.applicant_effect_sets.putAll(fillReplaceEffects(each_conditional.applicant_changes));
                    }
                } else { //JOINT
                    if (each_conditional.applicant_type.equals("A")) { //add
                        list_conditional.applicant_add_changes = true;
                        list_conditional.applicant_effect_sets.put(-1, fillAddEffects(each_conditional.applicant_changes));
                    } else { //replace
                        list_conditional.applicant_add_changes = false;
                        list_conditional.applicant_effect_sets.putAll(fillReplaceEffects(each_conditional.applicant_changes));
                    }

                    if (each_conditional.recipient_type.equals("A")) {
                        list_conditional.recipient_add_changes = true;
                        list_conditional.recipient_effect_sets.put(-1, fillAddEffects(each_conditional.recipient_changes));
                    } else {
                        list_conditional.recipient_add_changes = false;
                        list_conditional.recipient_effect_sets.putAll(fillReplaceEffects(each_conditional.recipient_changes));
                    }

                    if (each_conditional.both_type.equals("A")) {
                        list_conditional.both_add_changes = true;
                        list_conditional.both_effect_sets.put(-1, fillAddEffects(each_conditional.both_changes));
                    } else {
                        list_conditional.both_add_changes = false;
                        list_conditional.both_effect_sets.putAll(fillReplaceEffects(each_conditional.both_changes));
                    }
                }
                array_list_conditionals.add(list_conditional);
                count++;
            } //end if
        } //end for

        return array_list_conditionals;
    }

    private EffectSet fillAddEffects(ArrayList<String> conditional_changes) {
        EffectSet effect_set = new EffectSet();

        for (String each_change : conditional_changes) { //Should be an array of size one.

            StringTokenizer st = new StringTokenizer(each_change);
            if (st.countTokens() > 4) { //Probability case
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();
                effect_set.probability = Double.parseDouble(probability);

                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    effect_set.effect_list.add(new Effect(getResource(resource), Integer.parseInt(magnitude), Integer.parseInt(delay), Boolean.parseBoolean(linked)));

                } while (st.countTokens() >= 4);

            } else if (st.countTokens() == 4) { //Normal case
                effect_set.probability = 1.0;
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                effect_set.effect_list.add(new Effect(getResource(resource), Integer.parseInt(magnitude), Integer.parseInt(delay), Boolean.parseBoolean(linked)));

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                effect_set.probability = 1.0;
                String special = st.nextToken();
                String effect = "";

                if (special.equals("join")) {
                    effect_set.effect_list.add(new Effect("Joint Venture Effect", 0, 0, false));
                } else if (special.equals("jointgrant")) {
                    effect_set.probability = 0.5;
                    effect_set.effect_list.add(new Effect("Joint Grant Effect", 0, 0, false));
                }
            }

        } //endfor

        return effect_set;
    }

    private HashMap<Integer, EffectSet> fillReplaceEffects(ArrayList<String> conditional_changes) {
        HashMap<Integer, EffectSet> effect_hash_map = new HashMap<Integer, EffectSet>();

        for (String each_change : conditional_changes) { //Should be an array of size one.

            StringTokenizer st = new StringTokenizer(each_change);
            Integer replace_effect_index = Integer.valueOf(st.nextToken()) - 1; //FIRST TOKEN IS ALWAYS REPLACE INDEX
            EffectSet effect_set = new EffectSet();

            if (st.countTokens() > 4) { //Probability case
                st.nextToken(); //Skip past the first "P"
                String probability = st.nextToken();
                effect_set.probability = Double.parseDouble(probability);

                do { //continue for every effect
                    String resource = st.nextToken();
                    String magnitude = st.nextToken();
                    String delay = st.nextToken();
                    String linked = st.nextToken();

                    effect_set.effect_list.add(new Effect(getResource(resource), Integer.parseInt(magnitude), Integer.parseInt(delay), Boolean.parseBoolean(linked)));

                } while (st.countTokens() >= 4);

            } else if (st.countTokens() == 4) { //Normal case
                effect_set.probability = 1.0;
                String resource = st.nextToken();
                String magnitude = st.nextToken();
                String delay = st.nextToken();
                String linked = st.nextToken();

                effect_set.effect_list.add(new Effect(getResource(resource), Integer.parseInt(magnitude), Integer.parseInt(delay), Boolean.parseBoolean(linked)));

            } else if (st.countTokens() == 1) { //SPECIAL CASE
                effect_set.probability = 1.0;
                String special = st.nextToken();
                String effect = "";

                if (special.equals("join")) {
                    effect_set.effect_list.add(new Effect("Joint Venture Effect", 0, 0, false));
                } else if (special.equals("jointgrant")) {
                    effect_set.probability = 0.5;
                    effect_set.effect_list.add(new Effect("Joint Grant Effect", 0, 0, false));
                }
            }

            effect_hash_map.put(replace_effect_index, effect_set);
        } //endfor

        return effect_hash_map;
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
                    effect = "The applicant is given 3 units of finance by the recipient.  If the probability roll is successful, grants both parties 1 income and links them together, allowing the recipient to gain benefits from linked decisions.";
                }

                ve.setEffect(effect);
                readable_effects.add(ve);
            }
        }
        return readable_effects;
    }

    /**
     * Checks if some proposed switch name is unused in a list of switches, with
     * the exception of some replaceable name. The purpose of the replaceable
     * name is for when we edit an existing switch, where we should be allowed
     * to use the same name as the switch being edited.
     *
     * @param name name to check
     * @param switches list of switches
     * @param replaceable_name an exception name which can be replaced
     * @return
     */
    public boolean isNameUnique(String name, ArrayList<Switch> switches, String replaceable_name) {
        boolean unique = true;
        if (name.equals(replaceable_name)) {
            return true;
        }
        for (Switch each_switch : switches) {
            if (name.equals(each_switch.getName())) {
                unique = false;
                break;
            }
        }
        return unique;
    }

    /**
     * Checks all the inner effect lists of an effect set hashmap and returns
     * true if all the lists are empty.
     *
     * @param effect_set a hashmap containing a number of effectsets
     * @return true if every list is empty.
     */
    public boolean isEffectSetEmpty(HashMap<Integer, EffectSet> effect_set) {
        boolean all_empty = true;
        for (EffectSet each_set : effect_set.values()) {
            if (!each_set.effect_list.isEmpty()) {
                all_empty = false;
                break;
            }
        }

        return all_empty;
    }

    /**
     * Converts all effect sets to String format.
     *
     * @param effect_map a map of effect sets
     * @param replace true if the effects replace existing effects, or false is
     * effects are added on top of existing effects
     * @return An ArrayList of strings, where each string corresponds to one
     * set.
     */
    public ArrayList<String> effectParser(HashMap<Integer, EffectSet> effect_map, boolean replace) {
        ArrayList<String> role_effects = new ArrayList<>();
        for (Map.Entry<Integer, EffectSet> each_entry : effect_map.entrySet()) {
            if (!replace) { //if adding, ignore all keys that aren't -1
                if (each_entry.getKey() != -1) {
                    continue;
                }
            } else { //if replacing, ignore the -1 key
                if (each_entry.getKey() == -1) {
                    continue;
                }
            }

            String effect_str = "P " + each_entry.getValue().probability;
            if (each_entry.getValue().effect_list.isEmpty()) {
                continue; //Don't bother adding anything from an empty set
            }
            for (Effect each_effect : each_entry.getValue().effect_list) {
                String category;
                switch (each_effect.getCategory()) {
                    case "Finance":
                        category = "finance";
                        break;
                    case "Exploratory Knowledge":
                        category = "explore";
                        break;
                    case "Exploitative Knowledge":
                        category = "exploit";
                        break;
                    case "Income":
                        category = "f_change";
                        break;
                    case "Exploratory Knowledge per Turn":
                        category = "explore_change";
                        break;
                    case "Exploitative Knowledge per Turn":
                        category = "exploit_change";
                        break;
                    default:
                        category = "finance"; //default, should never be used.
                        break;
                }
                effect_str += " " + category + " " + each_effect.getValue() + " " + each_effect.getDelay() + " " + String.valueOf(each_effect.getLinked()).toLowerCase();
                if (replace) {
                    effect_str = (each_entry.getKey() + 1) + " " + effect_str;
                }
            }
            //should have format P Double CATEGORY1 VALUE1 DELAY1 FALSE1 CATEGORY2 VALUE2 DELAY2 FALSE2...
            role_effects.add(effect_str);
        }
        return role_effects;
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
        } else if (r.equals("exploit change")) {
            return "Exploitative Knowledge per Turn";
        } else {
            return null;
        }
    }
}
