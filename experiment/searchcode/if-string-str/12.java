package org.etupirkacms.sync;

import java.io.File;

/**
 * An environment of AppEngine.
 * @author shuji.w6e
 * @since 0.1.0
 */
public class AppEngineEnv {

    /** host name */
    String host = "localhost";
    /** server port */
    int port = 80;
    /** path to datastore */
    String datastorePath = "sync" + File.separator + "local_db.bin";
    /** application id */
    String applicationId = null;
    /** application version no */
    String version = null;
    /** application namespace */
    String nameSpace = "";
    /** development server */
    boolean devServer = false;
    /** Google account */
    String email = "";
    /** password */
    String password = "";

    /**
     * This is a constructor.
     */
    public AppEngineEnv() {
    }

    /**
     * nameSpace?????.
     * @return nameSpace
     */
    public String getNameSpace() {
        return nameSpace;
    }

    /**
     * host?????.
     * @return host
     */
    public String getHost() {
        return host;
    }

    /**
     * port?????.
     * @return port
     */
    public int getPort() {
        return port;
    }

    /**
     * datastorePath?????.
     * @return datastorePath
     */
    public String getDatastorePath() {
        return datastorePath;
    }

    /**
     * applicationId?????.
     * @return applicationId
     */
    public String getApplicationId() {
        return applicationId;
    }

    /**
     * version?????.
     * @return version
     */
    public String getVersion() {
        return version;
    }

    /**
     * devServer?????.
     * @return devServer
     */
    public boolean isDevServer() {
        return devServer;
    }

    /**
     * email?????.
     * @return email
     */
    public String getEmail() {
        return email;
    }

    /**
     * password?????.
     * @return password
     */
    public String getPassword() {
        return password;
    }

    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString() {
        StringBuilder str = new StringBuilder();
        str.append("AppEngineEnv[");
        str.append("host=").append(host).append(":").append(port);
        str.append(", devServer=").append(devServer);
        str.append(", applicationId=").append(applicationId);
        str.append(", version=").append(version);
        str.append(", nameSpace=").append(nameSpace);
        str.append(", datastorePath=").append(datastorePath);
        str.append(", email=").append(email);
        str.append(", password=").append(mask(password));
        str.append("]");
        return str.toString();
    }

    private String mask(String str) {
        if (str == null) return null;
        StringBuilder mask = new StringBuilder();
        for (int i = 0; i < str.length(); i++) {
            mask.append("*");
        } 
        return mask.toString();
    }
}

