package org.thesandbox.itask;

import org.jdesktop.application.ResourceMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Enumeration;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The Properties container for the iTask application.
 * This class is a singleton.
 *
 * Created by IntelliJ IDEA.
 *
 * Date: 05-Dec-2009
 * @author jrxrs
 */

public class ITaskProperties {
    private static Logger logger = Logger.getLogger(ITaskProperties.class.getCanonicalName());

    private static ITaskProperties cInstance = null;

    public static ITaskProperties getInstance() {
        if(cInstance == null) {
            cInstance = new ITaskProperties();
        }
        return cInstance;
    }

    private final Properties userProperties;
    private ResourceMap resourceMap;
    private File pFile;
    private final String propsFileName = "iTaskSettings.xml";
    public static final String REP_PATH         = "repository";
    public static final String RESCAN_PERIOD    = "rescan";
    public static final String STALE_PERIOD     = "stale";
    public static final String TIMEOUT_PERIOD   = "timeout";
    public static final String NOT_SET          = "null";
    public static final int    RESCAN_DEFAULT   = 300;
    public static final int    STALE_DEFAULT    = 300;
    public static final int    TIMEOUT_DEFAULT  = 60;

    private ITaskProperties() {
        userProperties = new Properties();
        resourceMap = ITaskApp.getApplication().getContext().getResourceMap(this.getClass());
        loadUserProperties();
    }

    public String get(String prop) {
        return userProperties.getProperty(prop);
    }

    public String get(String prop, String def) {
        return userProperties.getProperty(prop, def);
    }

    /**
     * Set a property and if instant is true then ensure the change is persisted
     * by writing the properties to file immediately.
     * @param key       The key to store the value under
     * @param value     The value to store
     * @param instant   true if the properties should be written to file
     *                  false if the properties should not be written to file
     */
    public void set(String key, String value, boolean instant) {
        userProperties.setProperty(key, value);
        if(instant) {
            try {
                writeProps(pFile);
            } catch (Exception e) {
                logger.log(Level.WARNING, "Exception thrown in Properties", e);
            }
        }
    }

    public void dump() {
        Enumeration e = userProperties.propertyNames();
        while(e.hasMoreElements()) {
            String key = (String) e.nextElement();
            logger.log(Level.INFO, key + ": " + userProperties.get(key));
        }
    }

    private void writeProps(File f) throws Exception {
        FileOutputStream fos = new FileOutputStream(f);
        String userName = System.getProperty("user.name");
        userProperties.storeToXML(fos, resourceMap.getString("Application.title")
                + " Settings for " + userName);
        fos.close();
    }

    private File newDefaultProps(File propsFile) throws Exception {
        userProperties.put(REP_PATH, NOT_SET);
        userProperties.put(RESCAN_PERIOD, String.valueOf(RESCAN_DEFAULT));
        userProperties.put(STALE_PERIOD, String.valueOf(STALE_DEFAULT));
        userProperties.put(TIMEOUT_PERIOD, String.valueOf(TIMEOUT_DEFAULT));
        writeProps(propsFile);
        return propsFile;
    }

    private File rinse(File props) throws Exception {
        if(props.exists() && props.isFile())
            return props;
        else if(props.createNewFile())
            return newDefaultProps(props);
        else return null;
    }

    private File propsExist() throws Exception {
        File homeDir = new File(System.getProperty("user.home"));
        if(homeDir.exists() && homeDir.isDirectory()) {
            File appHome = new File(homeDir, ".".concat(resourceMap.getString("Application.title")));
            File props = new File(appHome, propsFileName);
            if(appHome.exists() && appHome.isDirectory()) {
                return rinse(props);
            } else {
                if(appHome.mkdir() && props.createNewFile())
                    return newDefaultProps(props);
                else return null;
            }
        } else { //Could not find user.home - try current directory
            String curDir = System.getProperty("user.dir");
            File props = new File(curDir, propsFileName);
            return rinse(props);
        }
    }

    private void loadUserProperties() {
        try {
            pFile = propsExist();
            if(pFile != null) {
                logger.log(Level.INFO, "Using Properties at: " + pFile.getPath());
                FileInputStream fis = new FileInputStream(pFile);
                userProperties.loadFromXML(fis);
                fis.close();
                dump();
            }
        } catch(Exception e) {
            logger.log(Level.WARNING, "Exception thrown in Properties", e);
        }
    }
}

