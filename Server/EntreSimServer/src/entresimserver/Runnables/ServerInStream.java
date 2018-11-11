/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables;

import entresimserver.FXMLServerController;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.concurrent.CountDownLatch;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * A thread built for the sole purpose of receiving information from a single
 * client.
 *
 * @author dylanleong
 */
public class ServerInStream implements Runnable {

    private ObjectInputStream in;
    private int id;
    private volatile int choice;
    private volatile boolean[] show;
    private volatile boolean running;
    private GameController game_controller;
    private FXMLServerController server_controller;
    private volatile CountDownLatch latch;
    private boolean dead;

    public ServerInStream(Socket s, int i, GameController gc, FXMLServerController sc) {
        try {
            in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        //ready_latch = l;
        id = i;
        running = true;
        game_controller = gc;
        server_controller = sc;
        dead = false;
    }

    @Override
    public void run() {
        while (running) {
            String data_type = "";
            data_type = string_read();
            t_sleep(1000);

            if (getDead()) { //Ignore all inputs if dead
                continue;
            }
            
            if (data_type.equals("NAME")) { //Client name (we check that it's unique)
                String name = string_read();
                Platform.runLater(() -> {
                    server_controller.writeTxtLog("Client attempted to choose name: " + name);
                });
                game_controller.approveName(name, id);
            } else if (data_type.equals("DECISION")) {
                String decision = string_read();
                choice = Integer.valueOf(decision);
                latch.countDown();
            } else if (data_type.equals("JOINT_REQUEST")) {
                System.out.println("Received joint dec");
                String joint_req = string_read();
                System.out.println("Contents: " + joint_req);
                game_controller.jointRequest(joint_req + " " + id);
            } else if (data_type.equals("JOINT_ACK")) {
                String joint_reply = string_read();
                game_controller.jointAcknowledge(joint_reply + " " + id);
            }else if (data_type.equals("SHOW")) {
                try {
                    show = (boolean[]) in.readObject();
                } catch (IOException ex) {
                    Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else if (data_type.equals("PAIR_REQUEST")) {
                String request_to = string_read();
                game_controller.pairRequest(request_to + " " + id);//Send to Blocking Queue
                //Eventually block repeqted requests/reverse repeat requests (A requested B alrdy, block A req B && B req A)
            } else if (data_type.equals("PAIR_ACK")) {
                String reply = string_read();
                game_controller.pairAcknowledge(reply + " " + id);
            } else if (data_type.equals("PROPOSAL")) {
                try {
                    String[] s = (String[]) in.readObject();
                    Platform.runLater(() -> {
                        game_controller.customSet(id, s);
                    });
                } catch (IOException ex) {
                    Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
                }
            } else {
                //If the data_type string matches nothing we know, ignore it
            }

            t_sleep(1000);
        }
    }

    private String string_read() {
        try {
            return (String) in.readObject();
        } catch (IOException ex) {
            Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    private void t_sleep(long l) {
        /* try {
            sleep(l);
        } catch (InterruptedException ex) {
            Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    public void updateLatch(CountDownLatch l) {
        latch = l;
    }
    
    public void forceLatch() {
        latch.countDown();
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
    
    public boolean[] getShow() {
        return show;
    }

    public int getChoice() {
        return choice;
    }

    public boolean getDead() {
        return dead;
    }
    
    public void setDead() {
        dead = true;
    }    
    
    public void terminate() {
        try {
            running = false;
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ServerInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
