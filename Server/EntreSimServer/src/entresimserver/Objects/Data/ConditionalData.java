/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Data;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * An object that represents the changes a conditional switch brings to a
 * decision.
 *
 * @author dylanleong
 */
public class ConditionalData implements Serializable {

    public final int decision_id; //decision affected
    public final String name; //Switch name

    //Whether we add new effects, or replace old ones.  Each string will be either A for add, or R for replace.
    //If the string is R, it will be followed by some number(s) which represent the index(es) of the old effects to replace.
    public final String applicant_type;
    public final String recipient_type;
    public final String both_type;

    //List of effects that will be added or replace existing effects.  
    //If we are REPLACING OLD EFFECTS, there should be N number of effects, where N is the number of indexes that appear after R in X_type.
    //Additionally, each effect will be prefixed with the index of the effect it replaces.
    public final ArrayList<String> applicant_changes;
    public final ArrayList<String> recipient_changes;
    public final ArrayList<String> both_changes;

    public ConditionalData(int d, String n, String at, String rt, String bt, ArrayList<String> ac, ArrayList<String> rc, ArrayList<String> bc) {
        decision_id = d;
        name = n;

        applicant_type = at;
        recipient_type = rt;
        both_type = bt;

        applicant_changes = ac;
        recipient_changes = rc;
        both_changes = bc;
    }

}
