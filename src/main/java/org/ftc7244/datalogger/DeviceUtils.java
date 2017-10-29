package org.ftc7244.datalogger;

import se.vidstige.jadb.JadbDevice;
import se.vidstige.jadb.JadbException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Scanner;

/**
 * Created by FTC 7244 on 10/29/2017.
 */
public class DeviceUtils {

    public static String getTitle(JadbDevice device) {
        try {
            return getManufacture(device) + getModel(device) + " (" + device.getSerial()  + ")";
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
        Scanner s = new Scanner(propStream).useDelimiter("\\A");
        return s.hasNext() ? s.next().trim() : "";
    }
}
