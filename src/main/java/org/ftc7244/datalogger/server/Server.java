package org.ftc7244.datalogger.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class Server implements Runnable {
    private ServerClient client;
    private ServerSocket serverSocket;
    private Thread thread;
    private static final int PORT = 0;

    public Server(){
        client = null;
        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        while(client == null){
            try {
                Socket socket = serverSocket.accept();
                client = new ServerClient(socket, this);

                clientJoined();
            }
            catch (IOException e){
                e.printStackTrace();
            }
        }
    }

    private void clientJoined(){

    }

    /**
     *
     * @param message the message received from the android device
     */

    public void messageReceived(String message){
        String[] sections = message.split(":");
        String tag = sections[0];
        double[] data = new double[sections.length-1];
        for (int i = 1; i < sections.length; i++) {
            data[i-1] = Double.parseDouble(sections[i]);
        }

    }

    public void clientLeft() {
        client = null;
        thread.start();
    }
}