package org.ftc7244.datalogger.listeners;

/**
 * Created by FTC 7244 on 10/29/2017.
 */
public interface OnConnectionUpdate {
	void onConnectionUpdate(boolean connected, Exception exception);
}
