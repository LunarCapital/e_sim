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
public class PairRequester implements Runnable {
    
    public BlockingQueue<String> req_queue;
    private GameController game_controller;
    //private ArrayList<ServerOutStream> outstreams;
    private Environment env;
    private PairExpecting pair_expecting;
    
    public PairRequester(GameController gc, Environment e, PairExpecting pe) {
        req_queue = new ArrayBlockingQueue<String>(1000);
        game_controller = gc;
        env = e;
        pair_expecting = pe;
    }

    @Override
    public void run() {
        try {
            String req = "";
            while (!(req = req_queue.take()).equals("exit")) {
                StringTokenizer st = new StringTokenizer(req);
                String requested_name = st.nextToken();
                String requester_name = "";
                int requested_id = 0;
                int requester_id = Integer.valueOf(st.nextToken());

                
                for (PlayerDetails pd : env.players) {
                    if (pd.id == requester_id) {
                        requester_name = pd.name;
                    }
                    if (pd.name.equals(requested_name)) {
                        requested_id = pd.id;
                    }
                 }
                
                System.out.println("Request from " + requester_name + " to " + requested_name);
                
                if (env.network[requester_id][requested_id] > 0) {//Send back request saying a pair already exists
                    game_controller.sendSingle(requester_id, "MESSAGE", "You are already in a pair.");
                }
                else { //Forward request
                    pair_expecting.addToQueue();
                    game_controller.sendSingle(requested_id, "PAIR_REQUEST", requester_name);
                }
               
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(PairRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void addToQueue(String req) {
        try {
            req_queue.put(req);
        } catch (InterruptedException ex) {
            Logger.getLogger(PairRequester.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void terminate() {
        addToQueue("exit");
    }
    
}
