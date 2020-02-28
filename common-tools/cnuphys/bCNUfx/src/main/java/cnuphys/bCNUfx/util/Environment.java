package cnuphys.bCNUfx.util;

import java.awt.Color;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;


/**
 * This utility class holds environmental information such as the home
 * directory, current working directory, host name, etc.
 * 
 * @author heddle
 * 
 */
public final class Environment {

	// singleton
	private static Environment instance;

	// User's home directory.
	private String _homeDirectory;

	// Current working directory
	private String _currentWorkingDirectory;

	// user name
	private String _userName;

	// operating system name
	private String _osName;

	// temporary directory
	private String _tempDirectory;

	// the java class path
	private String _classPath;

	// the host IP address
	private String _hostAddress;

	// the application name
	private String _applicationName;

	// default panel background color
	private static Color _defaultPanelBackgroundColor;

	// properties from a preferences file
	private Properties _properties;

	// used to save lists as single strings
	private static String LISTSEP = "$$";

	// this is used to recommend to non AWT threads to wait to call for an
	// update
	private boolean _dragging;

	// for scaling things like fonts
	private float _resolutionScaleFactor;

	// screen dots per inch
	private int _dotsPerInch;

	/**
	 * Private constructor for the singleton.
	 */
	private Environment() {
		_homeDirectory = getProperty("user.home");
		_currentWorkingDirectory = getProperty("user.dir");
		_userName = getProperty("user.name");
		_osName = getProperty("os.name");

		_tempDirectory = getProperty("java.io.tmpdir");
		_classPath = getProperty("java.class.path");

		// screen information
		getScreenInformation();
		
		// read the preferences if the file exists
		File pfile = this.getPreferencesFile();
		_properties = null;
		if (pfile.exists()) {
			try {
				_properties = (Properties) SerialIO.serialRead(pfile.getPath());
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (_properties == null) {
			_properties = new Properties();
		}


		if (_properties == null) {
			_properties = new Properties();
		}
	}

	/**
	 * Get the common panel background color
	 * 
	 * @return the common panel background color
	 */
	public static Color getCommonPanelBackground() {
		return _defaultPanelBackgroundColor;
	}

	/**
	 * Check whether we are dragging or modifying an item.
	 * 
	 * @return <code>true</code> if we are dragging or modifying an item.
	 */
	public boolean isDragging() {
		return _dragging;
	}

	/**
	 * Set whether or not dragging is occurring. This cam be used to pause threads
	 * that might be affecting the screen.
	 * 
	 * @param dragging <code>true</code> if dragging is occuring.
	 */
	public void setDragging(boolean dragging) {
		_dragging = dragging;
	}

	// to help with resolution issues
	private void getScreenInformation() {
		_dotsPerInch = java.awt.Toolkit.getDefaultToolkit().getScreenResolution();
		double dpcm = _dotsPerInch / 2.54;
		_resolutionScaleFactor = (float) (dpcm / 42.91);
	}

	/**
	 * For scaling things like fonts. Their size should be multiplied by this.
	 * 
	 * @return the resolutionScaleFactor
	 */
	public float getResolutionScaleFactor() {
		return _resolutionScaleFactor;
	}

	/**
	 * Get the dots per inch for the main display
	 * 
	 * @return the dots per inch
	 */
	public double getDotsPerInch() {
		return _dotsPerInch;
	}

	/**
	 * Get the dots per inch for the main display
	 * 
	 * @return the dots per inch
	 */
	public double getDotsPerCentimeter() {
		return getDotsPerInch() / 2.54;
	}

	/**
	 * Public access for the singleton.
	 * 
	 * @return the singleton object.
	 */
	public static Environment getInstance() {
		if (instance == null) {
			instance = new Environment();
		}
		return instance;
	}

	/**
	 * Convenience routine for getting a system property.
	 * 
	 * @param keyName the key name of the property
	 * @return the property, or <code>null</null>.
	 */
	private String getProperty(String keyName) {
		try {
			return System.getProperty(keyName);
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * Get the JAVA class path.
	 * 
	 * @return the JAVA class path.
	 */
	public String getClassPath() {
		return _classPath;
	}

	/**
	 * Get the current working directory.
	 * 
	 * @return the currentWorkingDirectory.
	 */
	public String getCurrentWorkingDirectory() {
		return _currentWorkingDirectory;
	}

	/**
	 * Gets the user's home directory.
	 * 
	 * @return the user's home directory.
	 */
	public String getHomeDirectory() {
		return _homeDirectory;
	}

	/**
	 * Gets the operating system name.
	 * 
	 * @return the operating system name..
	 */
	public String getOsName() {
		return _osName;
	}

	/**
	 * Gets the temp directory.
	 * 
	 * @return the tempDirectory.
	 */
	public String getTempDirectory() {
		return _tempDirectory;
	}

	/**
	 * Gets the user name.
	 * 
	 * @return the userName.
	 */
	public String getUserName() {
		return _userName;
	}

	/**
	 * Gets the host address.
	 * 
	 * @return the host name.
	 */
	public String getHostAddress() {
		return _hostAddress;
	}

	/**
	 * Check whether we are running on linux
	 * 
	 * @return <code>true</code> if we are running on linux
	 */
	public boolean isLinux() {
		return getOsName().toLowerCase().contains("linux");
	}

	/**
	 * Check whether we are running on Windows
	 * 
	 * @return <code>true</code> if we are running on Windows
	 */
	public boolean isWindows() {
		return getOsName().toLowerCase().contains("windows");
	}

	/**
	 * Check whether we are running on a Mac
	 * 
	 * @return <code>true</code> if we are running on a Mac
	 */
	public boolean isMac() {
		return getOsName().toLowerCase().startsWith("mac");
	}

	/**
	 * Get the application name. This is the simple part of the name of the class
	 * with the main metho. That is, if the main method is in
	 * com.yomama.yopapa.Dude, this returns "dude" (converts to lower case.)
	 * 
	 * @return the application name
	 */
	public String getApplicationName() {
		if (_applicationName == null) {
			try {
				ThreadMXBean temp = ManagementFactory.getThreadMXBean();
				ThreadInfo t = temp.getThreadInfo(1, Integer.MAX_VALUE);
				StackTraceElement st[] = t.getStackTrace();
				_applicationName = st[st.length - 1].getClassName();

				if (_applicationName != null) {
					int index = _applicationName.lastIndexOf(".");
					_applicationName = _applicationName.substring(index + 1);
					_applicationName = _applicationName.toLowerCase();
				}
			} catch (Exception e) {
				_applicationName = null;
			}
		}
		return _applicationName;
	}

	/**
	 * Gets a File object for the configuration file. There is no guarantee that the
	 * file exists. It is the application name with a ".xml" extension in the user's
	 * home directory.
	 * 
	 * @return a File object for the configuration file
	 */
	public File getConfigurationFile() {
		String aname = getApplicationName();
		if (aname != null) {
			if (this.getOsName() == "Windows") {
				try {
					return new File(getHomeDirectory(), aname + ".xml");
				} catch (Exception e) {
					System.err.println("Could not get configuration file object");
				}
			} else { // Unix Based
				try {
					return new File(getHomeDirectory(), "." + aname + ".xml");
				} catch (Exception e) {
					System.err.println("Could not get configuration file object");
				}
			}
		}
		return null;
	}

	/**
	 * On Mac, uses the say command to say something.
	 * 
	 * @param sayThis the string to say
	 */
	public void say(String sayThis) {
		if (sayThis == null) {
			return;
		}
		if (isMac()) {
			try {
				Runtime.getRuntime().exec("say -v Karen " + sayThis);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Singleton objects cannot be cloned, so we override clone to throw a
	 * CloneNotSupportedException.
	 */
	@Override
	public Object clone() throws CloneNotSupportedException {
		throw new CloneNotSupportedException();
	}


	/**
	 * Get a File object representing the preferences file. No guarantee that it
	 * exists.
	 * 
	 * @return a File object representing the preferences file.
	 */
	private File getPreferencesFile() {
		String bareName = getApplicationName() + ".pref";
		String dirName = getHomeDirectory();
		File file = new File(dirName, bareName);
		return file;
	}

	/**
	 * Obtain a preference from the key
	 * 
	 * @param key the key
	 * @return the String corresponding to the key, or <code>null</code>.
	 */
	public String getPreference(String key) {
		if (_properties == null) {
			return null;
		}

		return _properties.getProperty(key);
	}

	/**
	 * Get the properties, which start out as the user preferences (or null) but
	 * which can be added to.
	 * 
	 * @return the properties
	 */
	public Properties getProperties() {
		return _properties;
	}

	/**
	 * Convenience method to get a Vector of strings as a single string in the
	 * preferences file. For example, it might be a Vector of recently visited
	 * files.
	 * 
	 * @param key   the key
	 * @param value the vector holding the strings
	 * @return a Vector of preferences
	 */
	public Vector<String> getPreferenceList(String key) {
		String s = getPreference(key);
		if (s == null) {
			return null;
		}
		String tokens[] = FileUtilities.tokens(s, LISTSEP);

		if ((tokens == null) || (tokens.length < 1)) {
			return null;
		}

		Vector<String> v = new Vector<String>(tokens.length);
		for (String tok : tokens) {
			v.add(tok);
		}
		return v;
	}
	
	/**
	 * Get the visual bounds of the primary screen. This includes
	 * things like the task bar and menu bar, etc.
	 * @return the  bounds of the primary screen
	 */
	public Rectangle2D getPrimaryScreenBounds() {
		return Screen.getPrimary().getBounds();
	}

	
	/**
	 * Get the visual bounds of the primary screen. This subtracts
	 * off the task bar and menu bar, etc.
	 * @return the visual bounds of the primary screen
	 */
	public Rectangle2D getPrimaryScreenVisualBounds() {
		return Screen.getPrimary().getVisualBounds();
	}
	
	public String screenPrint(Screen screen) {
		StringBuffer sb =  new StringBuffer(256);
		
		return sb.toString();
	}

	/**
	 * Save a value in the preferences and write the preferneces file.
	 * 
	 * @param key   the key
	 * @param value the value
	 */
	public void savePreference(String key, String value) {

		if ((key == null) || (value == null)) {
			return;
		}

		if (_properties == null) {
			_properties = new Properties();
		}

		_properties.put(key, value);
		writePreferences();
	}

	/**
	 * Convenience method to save a Vector of strings as a single string in the
	 * preferences file. For example, it might be a Vector of recently visited
	 * files.
	 * 
	 * @param key    the key
	 * @param values the vector holding the strings
	 */
	public void savePreferenceList(String key, Vector<String> values) {
		if ((key == null) || (values == null) || (values.isEmpty())) {
			return;
		}

		String s = "";
		int len = values.size();
		for (int i = 0; i < len; i++) {
			s += values.elementAt(i);
			if (i != (len - 1)) { // the separator
				s += LISTSEP;
			}
		}
		savePreference(key, s);
	}

	/**
	 * Write the preferences file to the home directory.
	 */
	private void writePreferences() {
		try {
			File file = getPreferencesFile();
			if (file.exists() && file.canWrite()) {
				file.delete();
			}

			if ((_properties != null) && !_properties.isEmpty()) {
				SerialIO.serialWrite(_properties, file.getPath());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print a memory report
	 * 
	 * @param message a message to add on
	 */
	public static String memoryReport(String message) {
		System.gc();
		System.gc();

		StringBuilder sb = new StringBuilder(1024);
		double total = (Runtime.getRuntime().totalMemory()) / 1048576.;
		double free = Runtime.getRuntime().freeMemory() / 1048576.;
		double used = total - free;
		sb.append("==== Memory Report =====\n");
		if (message != null) {
			sb.append(message + "\n");
		}
		sb.append(String.format("Total memory in JVM: %6.1f MB\n", total));
		sb.append(String.format("Free memory in JVM: %6.1f MB\n", free));
		sb.append(String.format("Used memory in JVM: %6.1f MB\n", used));

		return sb.toString();

	}

	/**
	 * Get a short summary string
	 * 
	 * @return a short summary string
	 */
	public String summaryString() {
		return " [" + _userName + "]" + " [" + _osName + "]" + " [" + _currentWorkingDirectory
				+ "]";
	}


	public GraphicsDevice[] getGraphicsDevices() {
		GraphicsEnvironment g = GraphicsEnvironment.getLocalGraphicsEnvironment();
		GraphicsDevice[] devices = g.getScreenDevices();

		return devices;
	}

	/**
	 * Convert to a string representation.
	 * 
	 * @return a string representation of the <code>Environment</code> object.
	 */

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder(2048);
		sb.append("Environment: \n");

		File file = getConfigurationFile();
		if (file == null) {
			sb.append("Config File: null\n");
		} else {
			sb.append("Config File: " + file.getAbsolutePath() + "\n");
		}

		sb.append("Host Address: " + getHostAddress() + "\n");
		sb.append("User Name: " + getUserName() + "\n");
		sb.append("Temp Directory: " + getTempDirectory() + "\n");
		sb.append("OS Name: " + getOsName() + "\n");
		sb.append("Home Directory: " + getHomeDirectory() + "\n");
		sb.append("Current Working Directory: " + getCurrentWorkingDirectory() + "\n");
		sb.append("Class Path: " + getClassPath() + "\n");


		sb.append("Dots per Inch: " + _dotsPerInch + "\n");
		sb.append(String.format("Dots per Centimeter: %-6.2f\n", getDotsPerCentimeter()));
		sb.append(String.format("Resolution Scale Factor: %-6.2f\n", getResolutionScaleFactor()));
		
		sb.append("Primary screen bounds: " + getPrimaryScreenBounds() + "\n");
		sb.append("Primary screen visual bounds: " + getPrimaryScreenVisualBounds() + "\n");

		sb.append("Monitors:\n");
		GraphicsDevice[] devices = getGraphicsDevices();
		if (devices != null) {
			for (GraphicsDevice device : devices) {
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				int width = device.getDisplayMode().getWidth();
				int height = device.getDisplayMode().getWidth();
				sb.append("   [W, H] = [" + width + ", " + height + "] bounds: " + bounds + "\n");
			}
		}

		sb.append("\n" + memoryReport(null));
		return sb.toString();
	}

	/**
	 * Main program for testing.
	 * 
	 * @param arg command line arguments (ignored).
	 */
	public static void main(String arg[]) {
		Environment env = Environment.getInstance();
//		env.say("Hello " + env.getUserName() + ", this is the bCNU Environment test.");
		System.out.println(env);
		System.out.println("Done.");

	}
}
