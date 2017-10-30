package org.ftc7244.datalogger.server;

import org.ftc7244.datalogger.DataLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by FTC 7244 on 10/29/2017.
 */
public class DataStream implements Runnable {

	private int port;
	private String ip;
	private boolean running;
	private Set<OnConnectionUpdate> onConnectionUpdates;
	private Set<OnDataReceived> onDataReceived;

	public DataStream(int port, String ip) {
		this.port = port;
		this.ip = ip;
		running = false;
		onConnectionUpdates = new HashSet<>();
		onDataReceived = new HashSet<>();
	}

	public DataStream start() {
		if (!running)
			DataLogger.getService().submit(this);
		return this;
	}

	@Override
	public void run() {
		Socket socket = null;
		running = true;
		try {
			socket = new Socket(ip, port);
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			this.onConnectionUpdates.forEach(x -> x.update(true, null));
			while (!Thread.currentThread().isInterrupted()) {
				String[] split = in.readLine().split(":");
				String tag = split[0];
				double[] data = new double[tag.length() - 1];
				for (int i = 1; i < split.length; i++)
					data[i - 1] = Double.parseDouble(split[i]);
				this.onDataReceived.forEach(x -> x.update(tag, data));
			}
		} catch (IOException e) {
			this.onConnectionUpdates.forEach(x -> x.update(false, e));
		} finally {
			running = false;
			try {
				if (socket != null)
					socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public DataStream addListener(OnConnectionUpdate onConnectionUpdate) {
		this.onConnectionUpdates.add(onConnectionUpdate);
		return this;
	}

	public DataStream addListener(OnDataReceived onDataReceived) {
		this.onDataReceived.add(onDataReceived);
		return this;
	}
}
