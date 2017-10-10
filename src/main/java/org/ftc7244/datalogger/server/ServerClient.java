package org.ftc7244.datalogger.server;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerClient implements Runnable {
    private Socket client;
    private BufferedReader in;
    private Server server;
    private Thread clientThread;

    public ServerClient(Socket client, Server server) throws IOException {
        this.client = client;
        this.server = server;
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        clientThread = new Thread(this);
        clientThread.start();
    }

    @Override
    public void run() {
        boolean running = true;
        while (running) {
            try {
                String input = in.readLine();
                if(in == null) running = false;
                else server.messageReceived(input);
            } catch (IOException e){
                e.printStackTrace();
            }
        }
        try {
            client.close();
            server.clientLeft();
            clientThread.join();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }
}