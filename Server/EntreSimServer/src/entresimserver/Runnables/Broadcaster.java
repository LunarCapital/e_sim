/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimserver.Runnables;

import java.net.*;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author dylanleong
 */
public class Broadcaster implements Runnable {

    private String server_name;
    private volatile boolean running;

    public Broadcaster(String str) throws UnknownHostException, SocketException {
        server_name = str;
        running = true;
    }

    @Override
    public void run() {

        DatagramSocket broadcaster = null;
        try {
            broadcaster = new DatagramSocket(6066, InetAddress.getByName("0.0.0.0"));
            broadcaster.setBroadcast(true);
            broadcaster.setSoTimeout(5000);
            
            while (running) {
                try {
                    //Receive a packet
                    byte[] recvBuf = new byte[15000];
                    DatagramPacket packet = new DatagramPacket(recvBuf, recvBuf.length);
                    broadcaster.receive(packet);

                    //See if the packet holds the right command (message)
                    String message = new String(packet.getData()).trim();
                    if (message.equals("ENTRE_SIM_BROADCAST_REQ")) {
                        String strData = "ENTRE_SIM_BROADCAST_RESP " + server_name;
                        byte[] sendData = strData.getBytes();

                        //Send a response
                        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, packet.getAddress(), packet.getPort());
                        broadcaster.send(sendPacket);

                        System.out.println(getClass().getName() + ">>>Sent packet to: " + sendPacket.getAddress().getHostAddress());
                    }
                } catch (SocketTimeoutException ste) {
                    //do nothing because we just rebroadcast
                }
            } //endwhile
        } catch (IOException e) {
            System.out.println(e.getMessage());
        } finally { //guaranteed to close socket
            if (broadcaster != null) {
                try {
                    broadcaster.close();
                } catch (Exception e) {
                    Logger.getLogger(Broadcaster.class.getName()).log(Level.SEVERE, "Failed to close broadcaster socket.", e);
                }
            }
        }
    } //endwhile

    public void terminate() {
        running = false;
    }

}
