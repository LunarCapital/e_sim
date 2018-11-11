/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package entresimclient.Runnables;

import entreObj.EData;
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
 *
 * @author dylanleong
 */
public class ClientOutStream implements Runnable {

    private ObjectOutputStream out;
    private volatile boolean running;
    private BlockingQueue<EData> out_queue;

    public ClientOutStream(Socket s) {
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
            out.flush();
        } catch (IOException ex) {
            Logger.getLogger(ClientOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
        running = true;
        out_queue = new ArrayBlockingQueue<EData>(100);
    }

    @Override
    public void run() {
        try {
            EData send = null;
            while (!((send = out_queue.take()).getType().equals("exit"))) {
                String data_type = send.getType();
                Object data_content = send.getObject();
                try {
                    System.out.println("Writing type: " + data_type);
                    System.out.println("Writing content: " + data_content);
                    out.writeObject(data_type);
                    t_sleep(1000);
                    out.writeObject(data_content);

                    out.reset();
                    out.flush();
                } catch (IOException ex) {
                    Logger.getLogger(ClientOutStream.class.getName()).log(Level.SEVERE, null, ex);
                }

                t_sleep(1000);
            }
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Just runs the sleep function. In a separate function so things don't get
     * bloated with try/catch statements
     *
     * @param l the time we sleep for
     */
    private void t_sleep(long l) {
        /*try {
            sleep(l);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }*/
    }

    public void setData(EData send) {
        try {
            System.out.println("Placed data on queue");
            out_queue.put(send);
        } catch (InterruptedException ex) {
            Logger.getLogger(ClientOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void terminate() {
        setData(new EData("exit", null));
        try {
            out.close();
        } catch (IOException ex) {
            Logger.getLogger(ClientOutStream.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
