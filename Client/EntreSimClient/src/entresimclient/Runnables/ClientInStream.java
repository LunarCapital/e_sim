/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Runnables;

import entresimclient.FXMLGameController;
import entreObj.Environment;
import entreObj.Decision;
import entreObj.WinData;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;

/**
 * A thread built for the sole purpose of receiving information from the server.
 *
 * @author dylanleong
 */
public class ClientInStream implements Runnable {

    private ObjectInputStream in;
    private volatile boolean running;
    private volatile String name_approved;
    private final FXMLGameController controller;
    private volatile CyclicBarrier recv_latch;
    public volatile Environment env;

    public ClientInStream(Socket s, FXMLGameController c, CyclicBarrier l) {
        try {
            in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        controller = c;
        recv_latch = l;
        running = true;
        name_approved = "NO";
        env = null;
    }

    @Override
    public void run() {
        while (running) {
            String data_type = "";
            try {
                data_type = (String) in.readObject();
                System.out.println("Data is: " + data_type);
                t_sleep(1000);
            } catch (IOException | ClassNotFoundException ex) {
                Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (data_type.equals("NAME_ACK")) { //Acknowlegement of client name
                receiveNameAck();
            } else if (data_type.equals("CHOICES")) { //Only sent once, the choices for the game
                System.out.println("Reading choices.");
                receiveChoices();
            } else if (data_type.equals("ROUND_DATA")) { //The data for this round
                receiveRoundData();
            } else if (data_type.equals("NEW_ROUND")) { //Signifies that a new round will start
                receiveNewRound();
            } else if (data_type.equals("JOINT_REQUEST")) {
                receiveJointRequest();
            } else if (data_type.equals("JOINT_RESET")) {
                receiveJointReset();
            } else if (data_type.equals("PAIR_REQUEST")) {
                receivePairRequest();
            } else if (data_type.equals("PAIR_ACK")) {
                receivePairAck();
            } else if (data_type.equals("COMMENT")) {
                receiveComment();
            } else if (data_type.equals("MESSAGE")) {
                receiveMessage();
            } else if (data_type.equals("DECISION_UPDATE")) {
                receiveDecisionUpdate();
            } else if (data_type.equals("END")) {
                receiveEnd();
                terminate();
            } else {
                //If the data_type string matches nothing we know, ignore it
            }

            t_sleep(1000);
        }
    }

    private void t_sleep(long l) {
        /*try {
            sleep(l);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    private void receiveNameAck() {
        final String reply;
        try {
            reply = (String) in.readObject();
            if (reply.equals("YES")) {
                name_approved = "YES";
            }
            else if (reply.equals("NO_LOAD")) {
                name_approved = "NO_LOAD";
            }
            recv_latch.await();
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (BrokenBarrierException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveChoices() {
        TreeSet<Decision> decisions;
        try {
            System.out.println("Reading decisions.");
            decisions = (TreeSet<Decision>) in.readObject();
            System.out.println("Read decisions.");
            Platform.runLater(() -> {
                controller.populateChoiceButtons(decisions);
            });
            //Create Buttons
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveRoundData() {
        try {
            env = (Environment) in.readObject();           
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveNewRound() {
        try {
            int round = (Integer) in.readObject();
            Platform.runLater(() -> {
                controller.prepareRound(round, env);
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveJointRequest() {
        try {
            System.out.println("Receiving joint request!");
            String joint_request = (String) in.readObject();
            Platform.runLater(() -> {
                controller.jointRequested(joint_request);
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveJointReset() {
        try {
            String joint_reset = (String) in.readObject();
            Platform.runLater(() -> {
                controller.resetJoint();
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receivePairRequest() {
        try {
            String requester = (String) in.readObject();
            System.out.println(requester + " sent request.");
            Platform.runLater(() -> {
               controller.pairRequested(requester);
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receivePairAck() { //Just need to notify
        try {
            String ack = (String) in.readObject();
            Platform.runLater(() -> {
                controller.writeText(ack);
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    private void receiveComment() {
        try {
            String comment = (String) in.readObject();
            Platform.runLater(() -> {
                controller.dialogComment(comment);
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveMessage() {
        try {
            String message = (String) in.readObject();
            Platform.runLater(() -> {
                controller.writeText(message);
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveDecisionUpdate() {
        try {
            ArrayList<Decision> decision_updates = (ArrayList<Decision>) in.readObject();
            Platform.runLater(() -> {
               controller.updateConditionalDecisions(decision_updates); 
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    private void receiveEnd() {
        try {
            TreeSet<WinData> windata = (TreeSet<WinData>) in.readObject();
            Platform.runLater(() -> {
               controller.endGame(windata); 
            });
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public String getNameApproved() {
        return name_approved;
    }

    public void terminate() {
        running = false;
        try {
            in.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientInStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
