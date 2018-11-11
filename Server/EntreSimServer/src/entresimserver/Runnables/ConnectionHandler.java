/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables;

import entresimserver.FXMLServerController;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylanleong
 */
public class ConnectionHandler implements Runnable {

    private volatile boolean running;
    private GameController game_controller;
    private FXMLServerController server_controller;
    private ArrayList<Socket> clients;
    private ArrayList<ServerInStream> instreams;
    private ArrayList<ServerOutStream> outstreams;
    private ServerSocket server_socket;

    public ConnectionHandler(ArrayList<ServerInStream> i, ArrayList<ServerOutStream> o, GameController gc, FXMLServerController sc) {
        running = true;
        clients = new ArrayList<Socket>(); //MAY CHANGE THIS LATER
        game_controller = gc;
        instreams = i;
        outstreams = o;
        server_controller = sc;
        server_socket = null;
    }

    @Override
    public void run() {
        ExecutorService exec_service = Executors.newFixedThreadPool(500);
        //ServerSocket server_socket = null;
        Socket client_socket = null;

        try {
            server_socket = new ServerSocket(6067);
            server_socket.setSoTimeout(10000);
            while (running) {
                try {
                    client_socket = server_socket.accept();

                    System.out.println(client_socket.getInetAddress() + " has connected.");
                    //Create new client in-out threads
                    //Add threads to array
                    //Execute threads
                    //exec_service.submit(/**/);
                    ServerOutStream o = new ServerOutStream(client_socket, outstreams.size());
                    ServerInStream i = new ServerInStream(client_socket, instreams.size(), game_controller, server_controller);

                    Thread t1 = new Thread(o, "Client Outstream");
                    Thread t2 = new Thread(i, "Client Instream");

                    t1.setDaemon(true);
                    t2.setDaemon(true);
                    
                    clients.add(client_socket);
                    outstreams.add(o);
                    instreams.add(i);

                    t1.start();
                    t2.start(); //isn't there a better way to do this
                } catch (SocketTimeoutException ex) {
                    //Do nothing, wait for more connections
                } catch (IOException ex) {
                    Logger.getLogger(ConnectionHandler.class.getName()).log(Level.INFO, "No longer accepting connections.  If the game has started, this is normal behaviour.", ex);
                }

            } //endwhile
        } catch (IOException ex) {
            Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (client_socket != null) {
                try {
                    server_socket.close();
                } catch (Exception e) {
                    Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, "Unable to close server socket.", e);
                }
            }
        }//end finally

    }

    public void terminate() {
        running = false;
        if (server_socket != null) {
            try {
                server_socket.close();
            } catch (IOException ex) {
                Logger.getLogger(ConnectionHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

}
