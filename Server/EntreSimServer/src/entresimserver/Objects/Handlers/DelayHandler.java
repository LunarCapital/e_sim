/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Objects.Handlers;

import entresimserver.Objects.Data.EffectData;
import java.io.Serializable;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Queue;

/**
 * An object that specifically handles the delay part of decisions.
 * The flow of decisions goes: Game controller -> Decision Handler (Probability) -> Delay Handler -> Parsed Decision Handler
 * Even if a decision has zero delay, it still comes to this queue before being parsed by the decision handler.
 * Will be called by the Decision Handler at the END of each round.
 * @author dylanleong
 */
public class DelayHandler implements Serializable {
    
    private Queue<EffectData> delay_queue;
    
    public DelayHandler() {
        delay_queue = new ArrayDeque<EffectData>();
    }
    
    /**
     * Takes EffectData out of the queue and checks the delay.
     * If delay > 0, the effect has its delay reduced by one, and is placed back on the queue.
     * If delay == 0, the effect is placed in an arraylist instead to be returned to the caller.  
     * @return an arraylist of effects with zero delay
     */
    public ArrayList<EffectData> cycleQueue() {
        ArrayList<EffectData> effective = new ArrayList<EffectData>();
        int cycle = delay_queue.size();
        
        for (int i = 0; i < cycle; i++) {
            EffectData look = delay_queue.poll();
            if (look.getDelay() > 0) { //effect still has delay
                look.lowerDelay();
                delay_queue.add(look);
            }
            else {
                effective.add(look);
            }
        }
        
        return effective;
    }
    
    public void placeOnQueue(EffectData new_effect) {
        delay_queue.add(new_effect);
    }
}
