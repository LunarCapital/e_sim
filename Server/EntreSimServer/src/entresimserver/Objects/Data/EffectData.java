/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Data;

import java.io.Serializable;

/**
 * Data encompassing the effect of a decision.  
 * @author dylanleong
 */
public class EffectData implements Serializable {
    
    public final String type; //applicant, recipient, both, join, or jointgrant
    public final String resource;
    public final int magnitude;
    public final boolean linked;
    public final int applicant_id;
    public final int recipient_id;
    private int delay;
    
    public EffectData(String type, String resource, int magnitude, boolean linked, int applicant_id, int recipient_id, int delay) {
        this.type = type;
        this.resource = resource;
        this.magnitude = magnitude;
        this.linked = linked;
        this.applicant_id = applicant_id;
        this.recipient_id = recipient_id;
        this.delay = delay;
    }
    
    /**
     * Lower the delay by one.
     */
    public void lowerDelay() {
        delay--;
    }
    
    /**
     * 
     * @return delay on this effect.
     */
    public int getDelay() {
        return delay;
    }
    
}
