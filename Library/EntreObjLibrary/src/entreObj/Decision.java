package entreObj;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */


import java.io.Serializable;
import java.util.ArrayList;

/**
 *
 * @author dylanleong
 */
public class Decision implements Serializable, Comparable<Decision> {

    public final int id;
    public final String title;
    public final String description;
    public final String type;
    public final ArrayList<String> prereqs;
    public final ArrayList<String> applicant_effects;
    public final ArrayList<String> recipient_effects;
    public final ArrayList<String> both_effects;
    
    public Decision(int i, String t, String d, String y, ArrayList<String> p, ArrayList<String> ae, ArrayList<String> re, ArrayList<String> be) {
        id = i;
        title = t;
        description = d;
        type = y;
        prereqs = p;
        applicant_effects = ae;
        recipient_effects = re;
        both_effects = be;
    }

    @Override
    public int compareTo(Decision o) {
        if (this.id == o.id) return 0;
        if (this.id > o.id) return 1;
        else return -1;
    }
   
}
