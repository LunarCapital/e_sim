/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables.Queues;

import entreObj.Environment;
import entresimserver.Runnables.GameController;
import static java.lang.Thread.sleep;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is an inbetween queue to PairAcknowledger and PairRequester. An
 * expecting 'token' is queued when the requester polls, and a expecting 'token'
 * should be polled when the acknowledger polls. We check if this queue is empty
 * before continuing in GameController's main loop.
 *
 * @author dylanleong
 */
public class PairExpecting implements Runnable {

    private BlockingQueue pair_exp_queue;
    private GameController game_controller;
    private Environment env;
    private boolean running;
    private boolean round_end_check;
    private CountDownLatch latch;

    public PairExpecting(GameController gc, Environment e) {
        pair_exp_queue = new ArrayBlockingQueue(1000);
        pair_exp_queue.clear();
        game_controller = gc;
        env = e;
        running = true;
        round_end_check = false;
        latch = new CountDownLatch(1); //default latch so we don't pull a null latch
    }

    @Override
    public void run() {
        while (running) {

            if (round_end_check && pair_exp_queue.isEmpty()) {
                round_end_check = false;
                latch.countDown();
                System.out.println("FLIPPING PAIR LATCH");
            }

            try {
                sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PairExpecting.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public void addToQueue() {
        String token = "token";
        try {
            pair_exp_queue.put(token);
        } catch (InterruptedException ex) {
            Logger.getLogger(JointExpecting.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void pollFromQueue() {
        pair_exp_queue.poll();
    }

    public void updateLatch(CountDownLatch l) {
        latch = l;
        round_end_check = true;
    }

    public void terminate() {
        running = false;
    }

}
