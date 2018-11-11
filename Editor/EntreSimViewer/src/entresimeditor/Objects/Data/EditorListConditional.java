/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimeditor.Objects.Data;

import entresimviewer.Objects.View.ListDecision;
import java.util.HashMap;

/**
 * An object that holds all the conditional data for the conditional editor.
 *
 * @author dylanleong
 */
public class EditorListConditional {

    public int conditional_id;
    public ListDecision decision_to_be_changed;
    public String type; //Indiv or Joint

    public boolean applicant_add_changes;
    public boolean recipient_add_changes;
    public boolean both_add_changes;

    //CONVENTION: -1 for Add, POSITIVE INTEGER corresponding to effect for Replace.
    public HashMap<Integer, EffectSet> applicant_effect_sets;
    public HashMap<Integer, EffectSet> recipient_effect_sets;
    public HashMap<Integer, EffectSet> both_effect_sets;

    public EditorListConditional(int id) {
        conditional_id = id;
        decision_to_be_changed = null;
        type = "";

        applicant_add_changes = true;
        recipient_add_changes = true;
        both_add_changes = true;

        applicant_effect_sets = new HashMap<Integer, EffectSet>();
        recipient_effect_sets = new HashMap<Integer, EffectSet>();
        both_effect_sets = new HashMap<Integer, EffectSet>();

        applicant_effect_sets.put(-1, new EffectSet());
        recipient_effect_sets.put(-1, new EffectSet());
        both_effect_sets.put(-1, new EffectSet());

        //Remember to stop the user from continuing if:       
        //decision_to_be_changed is null
        //type not equal to "Indiv" or "Joint"
        //All three effectsets are empty
    }

    @Override
    public String toString() {
        return "" + conditional_id;
    }

    /**
     * Resets all three effectset maps, and initializes empty effect sets in the
     * [-1, i] index slots. The -1 slot is for ADDED effects, while the [0, i]
     * slots are for REPLACING an existing effect of index [0, i].
     *
     * @param applicant_effects number of applicant effects in the decision we
     * are changing
     * @param recipient_effects number of recipient effects in the decision we
     * are changing
     * @param both_effects number of both effects in the decision we are
     * changing
     */
    public void resetEffectSets(int applicant_effects, int recipient_effects, int both_effects) {
        applicant_effect_sets = new HashMap<Integer, EffectSet>();
        recipient_effect_sets = new HashMap<Integer, EffectSet>();
        both_effect_sets = new HashMap<Integer, EffectSet>();

        applicant_effect_sets.put(-1, new EffectSet());
        recipient_effect_sets.put(-1, new EffectSet());
        both_effect_sets.put(-1, new EffectSet());

        for (int i = 0; i < applicant_effects; i++) {
            applicant_effect_sets.put(i, new EffectSet());
        }

        for (int i = 0; i < recipient_effects; i++) {
            recipient_effect_sets.put(i, new EffectSet());
        }

        for (int i = 0; i < both_effects; i++) {
            both_effect_sets.put(i, new EffectSet());
        }
    }

}
