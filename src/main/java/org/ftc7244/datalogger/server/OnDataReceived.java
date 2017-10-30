package org.ftc7244.datalogger.server;

/**
 * Created by FTC 7244 on 10/29/2017.
 */
public interface OnDataReceived {
	void update(String graph, double[] values);
}
