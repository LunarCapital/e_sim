/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables.Queues;

import entreObj.EData;
import entreObj.Environment;
import entreObj.PlayerDetails;
import entresimserver.Runnables.GameController;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylanleong
 */
public class JointRequester implements Runnable {
    
    private BlockingQueue<String> joint_req_queue;
    private GameController game_controller;
    private Environment env;
    private JointExpecting joint_expecting;
    
    public JointRequester(GameController gc, Environment e, JointExpecting je) {
        joint_req_queue = new ArrayBlockingQueue<String>(1000);
        game_controller = gc;
        env = e;
        joint_expecting = je;
    }

    @Override
    public void run() {
        try {
            String joint_req = ""; //Target Name, Decision ID, Requester ID
            while (!(joint_req = joint_req_queue.take()).equals("exit")) {
                StringTokenizer st = new StringTokenizer(joint_req);
                
                String target_name = st.nextToken();
                int target_id = env.name_to_int.get(target_name);
                
                int decision_id = Integer.parseInt(st.nextToken());
                
                int requester_id = Integer.parseInt(st.nextToken());
                String requester_name = env.int_to_player.get(requester_id).name;
                
                PlayerDetails requester = null;
                PlayerDetails target = null;
                
                for (PlayerDetails p : env.players) {
                    if (p.id == target_id) target = p;
                    else if (p.id == requester_id) requester = p;
                }
                
                if (requester != null && target != null && requester.isJointAvailable() && target.isJointAvailable()) {
                    joint_expecting.addToQueue(); //only expect response if request is valid
                    requester.useJoint();
                    game_controller.sendSingle(target_id, "JOINT_REQUEST", decision_id + " " + requester_name);
                }
                else {
                    if (!requester.isJointAvailable()) {
                        game_controller.sendSingle(requester_id, "MESSAGE", "You have already made a joint decision this round.");
                    }
                    else if (!target.isJointAvailable()) {
                        game_controller.sendSingle(requester_id, "COMMENT", "The player you are trying to make a joint decision with is not available for the rest of this round.");
                        game_controller.sendSingle(requester_id, "JOINT_RESET", "");
                    }
                }
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JointRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }    
    
    public void addToQueue(String req) {
        try {
            joint_req_queue.put(req);
        } catch (InterruptedException ex) {
            Logger.getLogger(JointRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void terminate() {
        addToQueue("exit");
    }
    
}
