package org.ftc7244.datalogger.misc;

import java.nio.file.Paths;

/**
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * <p>
 * Written By: brandon on 10/29/17
 */
public class ADBUtils {

	private static OperatingSystem cachedOS;

	public static ADBResult exec(String command, String... args)  {
		if (!isSupported()) {
			return new ADBResult(false, "Unsupported device!");
		}

		try {
			String[] commandGroup = new String[args.length + 2];
			commandGroup[0] = ADBUtils.class.getResource(getOperatingSystem().ADB_PATH).getPath();
			commandGroup[1] = command;
			System.arraycopy(args, 0, commandGroup, 2, args.length);

			Process exec = Runtime.getRuntime().exec(commandGroup);
			boolean successful = exec.waitFor() == 0;
			String output = DeviceUtils.toString(exec.getInputStream())
					+ "\n"
					+ DeviceUtils.toString(exec.getErrorStream());
			return new ADBResult(successful, output);
		} catch (Exception e) {
			e.printStackTrace();
			return new ADBResult(false, e.getMessage());
		}
	}

	public static boolean start() {
		return exec("start-server").isSuccessful();
	}

	public static boolean stop() {
		return exec("kill-server").isSuccessful();
	}

	public static OperatingSystem getOperatingSystem() {
		String osName = System.getProperty("os.name", "generic").toLowerCase();
		if (cachedOS == null) {
			if (osName.contains("mac") || osName.contains("darwin")) {
				cachedOS = OperatingSystem.OSX;
			} else if (osName.contains("win")) {
				cachedOS = OperatingSystem.WINDOWS;
			} else if (osName.contains("nux")) {
				cachedOS = OperatingSystem.LINUX;
			} else {
				cachedOS = OperatingSystem.UNKNOWN;
			}
		}
		return cachedOS;
	}

	public static boolean isSupported() {
		return getOperatingSystem() != OperatingSystem.UNKNOWN;
	}


	public enum OperatingSystem {
		WINDOWS("/adb/win/adb.exe"),
		OSX("/adb/osx"),
		LINUX("/adb/linux"),
		UNKNOWN(null);

		private final String ADB_PATH;

		OperatingSystem(String adbPath) {
			this.ADB_PATH = adbPath;
		}
	}

	public static class ADBResult {
		private boolean successful;
		private String output;

		public ADBResult(boolean successful, String output) {
			this.successful = successful;
			this.output = output;
		}

		public boolean isSuccessful() {
			return successful;
		}

		public String getOutput() {
			return output;
		}
	}
}
