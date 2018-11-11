/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables.Queues;

import entresimserver.Objects.Data.RequestData;
import entresimserver.Runnables.GameController;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylanleong
 */
public class CustomDecisionHandler implements Runnable {
    
    private CountDownLatch request_latch;
    private BlockingQueue<RequestData> request_queue;
    private GameController game_controller;
    
    public CustomDecisionHandler(GameController gc) {
        resetLatch();
        request_queue = new ArrayBlockingQueue<RequestData>(1000);
        game_controller = gc;
    }
    
    @Override
    public void run() {
        try {
            RequestData request = null;
            while ((request = request_queue.take()).id != -1) {
                request_latch.await(); //wait for both a request to arrive AND for the controller to be ready
                game_controller.customReceive(request);
                resetLatch();
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(CustomDecisionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void resetLatch() {
        request_latch = new CountDownLatch(1);
    }
 
    public void pullLatch() {
        request_latch.countDown();
    }
    
    public void addToQueue(int id, String[] s) {
        try {
            request_queue.put(new RequestData(id, s[0], s[1]));
        } catch (InterruptedException ex) {
            Logger.getLogger(CustomDecisionHandler.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public void terminate() {
        addToQueue(-1, new String[2]);
    }
}
