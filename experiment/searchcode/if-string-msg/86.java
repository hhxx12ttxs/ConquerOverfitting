/**
 * Logger.java   1.00    2004/01/14
 *
 * Sinyee Framework.
 * Copyright 2004-2006 SINYEE I.T. Co., Ltd. All rights reserved.
 * @author SINYEE I.T. Co., Ltd.
 * 
 * History:
 */
package com.rainstars.common.log;

import org.apache.log4j.Priority;

/**
 * Logger class which is used to write log in the file or console.
 *
 * @version 1.00
 * @author  Ura
 */
public class Logger {


    /** The fully qualified name of the Log4JLogger class. */
    private static final String FQCN = Logger.class.getName();

    /**
     * Retrieve a kanamic's logger by class.
     * @param aClass Logger's class
     * @return kanamic logger
     */
    public static Logger getLogger(Class clazz) {
        return new Logger(clazz);
    }

    private org.apache.log4j.Logger logger = null;

    /**
     * Constructor
     * @param s Logger's name
     */
    private Logger(Class clazz) {
        this.logger =  org.apache.log4j.Logger.getLogger(clazz.getName());
    }
    
  

    /**
     * Message format
     * @param msg Message
     * @return
     */
    private String formatMsg(String msg) {
        return (" - " + msg);
    }

    /**
     * Message format
     * @param msg Message
     * @param e Exception
     * @return
     */
    private String formatMsg(String msg, Throwable e) {
        StackTraceElement[] ste = e.getStackTrace();
        if (ste != null && ste.length > 0) {
            return (":" + ste[0].getClassName() + " - " + msg);
        }
        return formatMsg(msg);
    }

    /**
     * Logger a message with the DEBUG level.
     * @param msg Message
     */
    public void debug(String msg) {
    	logger.debug(msg);
    }

    /**
     * Logger a message object with the ERROR Level.
     * @param msg Message
     */
    public void error(String msg) {
    	logger.error(msg);
    }

    /**
     * Logger a message object with the FATAL Level.
     * @param msg Message
     */
    public void fatal(String msg) {
        logger.fatal(msg);
    }

    /**
     * Logger a message object with the INFO Level.
     * @param msg Message
     */
    public void info(String msg) {
    	logger.info(msg);
    }

    /**
     * Logger a message object with the WARN Level.
     * @param msg Message
     */
    public void warn(String msg) {
    	logger.warn(msg);
    }

    /**
     * Logger a message object with the costom level.
     * @param priority Custom level include
     * @param msg Message
     */
    public void log(Priority priority, String msg) {
        logger.log(FQCN, priority, formatMsg(msg), null);
    }

    /**
     * Logger a message object with the costom level.
     * @param priority Custom level include
     * @param msg Message
     * @param e Exception
     */
    public void log(Priority priority, String msg, Throwable e, boolean exlogFlag) {
        logger.log(FQCN, priority, formatMsg(msg, e), exlogFlag?e:null);
    }



    /**
     * Write a message when start a method
     * @param sName Method name
     */
    public void startMethod(String sName) {
        this.debug("Method " + sName + " start.");
    }

    /**
     * Write a message when end a method
     * @param sName Method name
     */
    public void endMethod(String sName) {
    	this.debug("Method " + sName + " end.");
    }

}

