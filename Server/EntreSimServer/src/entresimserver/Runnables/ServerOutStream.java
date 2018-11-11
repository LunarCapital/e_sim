/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables;

import entreObj.EData;
import entreObj.Environment;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import static java.lang.Thread.sleep;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A thread built for the sole purpose of sending information to a single
 * client. It uses a latch that can be 'pulled' when the server is ready to send
 * info over. The latch is then reset (we just construct it again because the
 * library never made it resettable).
 *
 * @author dylanleong
 */
public class ServerOutStream implements Runnable {

    private ObjectOutputStream out;
    private int id;
    private volatile boolean running;
    private BlockingQueue<EData> out_queue;
    private boolean dead;

    public ServerOutStream(Socket s, int i) {
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ServerOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        id = i;
        running = true;
        out_queue = new ArrayBlockingQueue<EData>(100);
        dead = false;
    }

    @Override
    public void run() {
        EData send = null;
        try {
            while (!((send = out_queue.take()).getType().equals("exit"))) {
                String data_type = send.getType();
                Object data_content = send.getObject();
                try {
                    System.out.println("Sending Type: " + data_type);
                    System.out.println("Sending Content: " +data_content);
                    out.writeObject(data_type);
                    t_sleep(1000);
                    out.writeObject(data_content);
                    
                    out.reset();
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ServerOutStream.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                t_sleep(1000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void t_sleep(long l) {
        /*try {
            sleep(l);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }
    
    public void setData(EData send) {
        try {
            out_queue.put(send);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public int getID() {
        return id;
    }
    
    /**
     * Should only ever be called by the game_controller, when remapping IDs after loading a game.
     * @param new_id 
     */
    public void updateID(int new_id) {
        id = new_id;
    }
    
    public boolean getDead() {
        return dead;
    }
    
    public void setDead() {
        dead = true;
    }
    
    public void terminate() {
        running = false;
        try {
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
}
