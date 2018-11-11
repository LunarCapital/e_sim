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
import java.util.ArrayList;
import java.util.StringTokenizer;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylanleong
 */
public class PairAcknowledger implements Runnable {

    private BlockingQueue<String> ack_queue;
    private GameController game_controller;
    private Environment env;
    private PairExpecting pair_expecting;
    
    public PairAcknowledger(GameController gc, Environment e, PairExpecting pe) {
        ack_queue = new ArrayBlockingQueue<String>(1000);
        game_controller = gc;
        env = e;
        pair_expecting = pe;
    }
    
    @Override
    public void run() {
        try {
            String req = "";
            while (!(req = ack_queue.take()).equals("exit")) {
                StringTokenizer st = new StringTokenizer(req);
                String answer = st.nextToken();
                String requester = st.nextToken();
                String replier = "";
                int replied_from = Integer.valueOf(st.nextToken());
                int reply_to = 0;
                
                for (PlayerDetails pd : env.players) {
                    if (pd.name.equals(requester)) {
                        reply_to = pd.id;
                    }
                    if (pd.id == replied_from) {
                        replier = pd.name;
                    }
                }
                
                System.out.println("Players involved: " + requester + " " + replier);
                System.out.println("IDS involved: " + reply_to + " " + replied_from);
                if (answer.equals("YES")) {
                    env.network[replied_from][reply_to] = 1;
                    env.network[reply_to][replied_from] = 1;
                    System.out.println("Request accepted by " + replier + " to " + requester);
                }
                
                game_controller.sendSingle(reply_to, "PAIR_ACK", "Partnership request answered from Player " + replier + ": " + answer);
                pair_expecting.pollFromQueue();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PairRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addToQueue(String ack) {
        try {
            ack_queue.put(ack);
        } catch (InterruptedException ex) {
            Logger.getLogger(PairAcknowledger.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void terminate() {
        addToQueue("exit");
    }
}
