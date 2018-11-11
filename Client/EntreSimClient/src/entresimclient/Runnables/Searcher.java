/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Runnables;

import entresimclient.Objects.Server;

import java.net.DatagramSocket;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;

import java.io.IOException;

import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.logging.Level;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

/**
 * A thread that runs alongside the Client UI and provides the servers in the TableColumn.
 * @author dylanleong
 */
public class Searcher implements Callable {

	public Searcher() {
	}

        
        @Override
        /**
         * A function that searches for broadcasting servers, and returns them in an ObservableList.
         * Most of the searching function is written by Michiel De May at this site:
         * http://michieldemey.be/blog/network-discovery-using-udp-broadcast/
         * or this alternative:
         * http://demey.io/network-discovery-using-udp-broadcast/
         */
	public ObservableList<Server> call() {

            ObservableList<Server> address_list = FXCollections.observableArrayList(); //Address, then server name
            DatagramSocket searcher = null;
            
		try {
			searcher = new DatagramSocket();
			searcher.setBroadcast(true);
			searcher.setSoTimeout(5000); //tentative

			byte[] sendData = "ENTRE_SIM_BROADCAST_REQ".getBytes();

			//Broadcast to 255.255.255.255
			try {
				DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, InetAddress.getByName("255.255.255.255"), 6066);
				searcher.send(sendPacket);
			} 
			catch (Exception e) {
				System.out.println(e.getMessage());
			}
			
			//Send to all networks we know of just in case we missed any
			Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
			while (interfaces.hasMoreElements()) {
				NetworkInterface networkInterface = interfaces.nextElement();

				if (networkInterface.isLoopback() || !networkInterface.isUp()) {
					continue; //Don't send to loopback or offline networks
				}

				for (InterfaceAddress interfaceAddress : networkInterface.getInterfaceAddresses()) {
					InetAddress broadcast = interfaceAddress.getBroadcast();
					if (broadcast == null) {
						continue;
					}

					try {
						DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, broadcast, 6066);
						searcher.send(sendPacket);
					} 
					catch (Exception e) {
						System.out.println(e.getMessage());
					}
				}
			}

			byte[] recvBuf = new byte[15000];
			DatagramPacket receivePacket = new DatagramPacket(recvBuf, recvBuf.length);
			searcher.receive(receivePacket);

			String message = new String(receivePacket.getData()).trim();
			StringTokenizer st = new StringTokenizer(message);

			if (st.countTokens() >= 2) {
				if (st.nextToken().equals("ENTRE_SIM_BROADCAST_RESP")){
					String server_name = "";
					while (st.hasMoreTokens()) server_name = server_name + st.nextToken() + " ";
                                        Server s = new Server();
                                        s.setAddress("" + receivePacket.getAddress().getHostAddress());
                                        s.setServerName(server_name);
					address_list.add(s);
					//txtLog.appendText("Found IP: " + receivePacket.getAddress() + " with server name " + server_name + "\n");
				}
			}

		} 
		catch (SocketTimeoutException ste) {
			//txtLog.appendText("Couldn't find any servers.\n");
		}
		catch (IOException e) {
			System.out.println(e.getMessage());
		}
                finally {
                    if (searcher != null) {
                        try {
                            searcher.close();
                        }
                        catch (Exception e) {
                            Logger.getLogger(Searcher.class.getName()).log(Level.SEVERE, "Unable to close searcher.", e);
                        }
                    }
                }
            return address_list;
	}	
}
