/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables.Queues;

import entreObj.EData;
import entreObj.Environment;
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
public class JointAcknowledger implements Runnable {

    private BlockingQueue<String> joint_ack_queue;
    private GameController game_controller;
    private Environment env;
    private JointExpecting joint_expecting;

    public JointAcknowledger(GameController gc, Environment e, JointExpecting je) {
        joint_ack_queue = new ArrayBlockingQueue<String>(1000);
        game_controller = gc;
        env = e;
        joint_expecting = je;
    }

    @Override
    public void run() {
        try {
            String joint_ack = ""; //reply, requester_name, replier_id, decision_id only if we said yes
            while (!(joint_ack = joint_ack_queue.take()).equals("exit")) {
                StringTokenizer st = new StringTokenizer(joint_ack);
                System.out.println(joint_ack);

                String reply = st.nextToken();

                String requester_name = st.nextToken();
                int requester_id = env.name_to_int.get(requester_name);

                if (reply.equals("YES")) {
                    int decision_id = Integer.parseInt(st.nextToken());
                    int replier_id = Integer.parseInt(st.nextToken());
                    String replier_name = env.int_to_player.get(replier_id).name;

                    game_controller.sendSingle(requester_id, "MESSAGE", "Your joint decision request was accepted by " + replier_name);
                    game_controller.parseJointDecision(requester_id, replier_id, decision_id);
                } else {
                    int replier_id = Integer.parseInt(st.nextToken());
                    String replier_name = env.int_to_player.get(replier_id).name;
                    game_controller.sendSingle(requester_id, "MESSAGE", "Your joint decision request was declined by " + replier_name);
                }
                
                joint_expecting.pollFromQueue(); //No longer expecting response
                
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(JointAcknowledger.class.getName()).log(Level.SEVERE, null, ex);
        }

    }

    public void addToQueue(String ack) {
        try {
            joint_ack_queue.put(ack);
        } catch (InterruptedException ex) {
            Logger.getLogger(JointAcknowledger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void terminate() {
        addToQueue("exit");
    }
    
}
