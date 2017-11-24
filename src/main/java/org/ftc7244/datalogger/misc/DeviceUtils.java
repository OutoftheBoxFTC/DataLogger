package org.ftc7244.datalogger.misc;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by FTC 7244 on 10/29/2017.
 */
public class DeviceUtils {

	public static String getTitle(JadbDevice device) {
		try {
			return getManufacture(device) + getModel(device) + " (" + device.getSerial() + ")";
		} catch (Exception e) {
			return "Unknown Device";
		}
	}

	public static String getModel(JadbDevice device) throws IOException, JadbException {
		return getProp(device, "ro.product.model");
	}

	public static String getManufacture(JadbDevice device) throws IOException, JadbException {
		return getProp(device, "ro.product.Manufacture");
	}

	public static String getProp(JadbDevice device, String prop) throws IOException, JadbException {
		InputStream propStream = device.executeShell("getprop", prop);
		return toString(propStream);
	}

	public static Optional<String> getIPAddress(JadbDevice device) {
		String output = null;
		try {
			output = toString(device.executeShell("ifconfig", "wlan0"));
			Pattern pattern = Pattern.compile("addr:(?<ip>(\\d{1,3}\\.){3}\\d{1,3})");
			Matcher matcher = pattern.matcher(output);
			if (matcher.find())
				return Optional.of(matcher.group("ip"));
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (output != null)
			System.err.println(output);
		return Optional.empty();
	}

	protected static String toString(InputStream stream) {
		Scanner s = new Scanner(stream).useDelimiter("\\A");
		return s.hasNext() ? s.next().trim() : "";
	}
}
