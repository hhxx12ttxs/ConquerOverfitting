package sss.mp.scale.applet;

import netscape.javascript.JSException;
import netscape.javascript.JSObject;
import sss.mp.scale.CommErrorListener;
import sss.mp.scale.ErrorCode;
import sss.mp.scale.ScaleConfig;
import sss.mp.scale.ScaleManager;
import sss.mp.scale.ScaleSignalGenerator;
import sss.mp.scale.ScaleStateListener;
import sss.mp.scale.applet.sim.ScaleSignalSimulator;
import sss.mp.scale.impl.ScaleManagerImpl;
import sss.mp.scale.serial.PortConfig;
import sss.mp.scale.serial.SerialScaleConnector;

import javax.swing.*;
import java.awt.*;
import java.util.logging.Logger;

public class MainApplet extends JApplet implements ScaleStateListener, CommErrorListener {
    public static final double TON_DENOMINATOR = 1000.0;
    private ScaleManager scaleManager;
    private ScaleSignalGenerator scaleSignalGenerator;
    private MainForm mainForm = new MainForm();
    private PortConfig portConfig = new PortConfig();
    private ScaleConfig scaleConfig = new ScaleConfig();
    private boolean zeroWaitFired = false;
    private boolean alertFired = false;
    private boolean lockedFired = false;

    protected Logger logger = Logger.getLogger(this.getClass().getName());

    public MainApplet() throws HeadlessException {
        this.getRootPane().setContentPane(mainForm.getMainPanel());
    }

    @Override
    public String[][] getParameterInfo() {
        return new String[][]{
                {"initMaxWeightKgs", "Kgs", "A maximum initial weight in Kgs the applet expects before performing other operations; set to a negative value to disable this feature"},
                {"lockType", "\"AUTO_LOCK\" or \"MANUAL_LOCK\"", "Determines if a user has to manually click the \"lock\" button"},
                {"minStableTimeSecs", "seconds", "A minimum wait time in seconds during which the weight must not swing"},
                {"maxStableSwingWeightKgs", "Kgs", "A maximum weight swing allowance"},
                {"minLockWeightKgs", "Kgs", "A minimum weight on which the applet can lock"},
                {"maxLockWeightKgs", "Kgs", "A maximum weight on which the applet can lock"}
        };
    }

    @Override
    public void init() {
        readScaleConfig();
        readPortConfig();

        scaleManager = new ScaleManagerImpl(scaleConfig);
        scaleManager.addScaleStateListener(this);
        scaleManager.addScaleStateListener(mainForm);

        mainForm.setManager(scaleManager);

        if (Boolean.valueOf(getParameter("simulator"))) {
            scaleSignalGenerator = new ScaleSignalSimulator();
            scaleSignalGenerator.setManager(scaleManager);
        } else {
            scaleSignalGenerator = new SerialScaleConnector(portConfig, this);
            scaleSignalGenerator.setManager(scaleManager);
        }
    }

    private void readPortConfig() {
        if (getParameter("port") != null) portConfig.port = getParameter("port");
        if (getParameter("baud") != null) portConfig.baud = Integer.parseInt(getParameter("baud"));
        if (getParameter("stop") != null) portConfig.stop = Integer.parseInt(getParameter("stop"));
        if (getParameter("data") != null) portConfig.data = Integer.parseInt(getParameter("data"));
        if (getParameter("parity") != null) portConfig.parity = Integer.parseInt(getParameter("parity"));
    }

    private void readScaleConfig() {
        if (getParameter("initMaxWeightKgs") != null)
            scaleConfig.initMaxWeightKgs = Integer.parseInt(getParameter("initMaxWeightKgs"));
        if (getParameter("lockType") != null)
            scaleConfig.lockType = ScaleConfig.ScaleLockType.valueOf(getParameter("lockType"));
        if (getParameter("minStableTimeSecs") != null)
            scaleConfig.minStableTimeSecs = Integer.parseInt(getParameter("minStableTimeSecs"));
        if (getParameter("maxStableSwingWeightKgs") != null)
            scaleConfig.maxStableSwingWeightKgs = Integer.parseInt(getParameter("maxStableSwingWeightKgs"));
        if (getParameter("minLockWeightKgs") != null)
            scaleConfig.minLockWeightKgs = Integer.parseInt(getParameter("minLockWeightKgs"));
        if (getParameter("maxLockWeightKgs") != null)
            scaleConfig.maxLockWeightKgs = Integer.parseInt(getParameter("maxLockWeightKgs"));
    }

    @Override
    public void start() {
        scaleManager.reset();
        scaleManager.setOnline(true);
        if (Boolean.valueOf(getParameter("autostart")) || Boolean.valueOf(getParameter("simulator"))) {
            logger.info("Auto starting signal generator.");
            startGenerator();
        }
        resetEventStates();
    }

    public void startGenerator() {
        logger.info("Starting generator.");
        scaleSignalGenerator.start();
    }

    private void resetEventStates() {
        zeroWaitFired = false;
        alertFired = false;
        lockedFired = false;
    }

    @Override
    public void stop() {
        scaleSignalGenerator.stop();
        scaleManager.setOnline(false);
    }

    @Override
    public void destroy() {
    }

    /**
     * Returns a configuration object that can be modified at run-time by JavaScript.
     *
     * @return <code>ScaleConfig</code> instance.
     */
    public ScaleConfig getScaleConfig() {
        return scaleConfig;
    }

    @Override
    public void onZeroWait(int weight) {
        try {
            if (!zeroWaitFired) {
                zeroWaitFired = true;
                JSObject window = JSObject.getWindow(this);
                window.call("onZeroWait", new Object[]{"Please clear the scale before proceeding."});
            }
        } catch (JSException e) {
            // running in applet viewer?
            logger.warning("Unable to call JavaScript method [onZeroWait].");
        }
    }

    @Override
    public void onManualLockWait(boolean alert, int weight, boolean lockable) {
        try {
            if (!alertFired) {
                alertFired = true;
                if (alert) {
                    JSObject window = JSObject.getWindow(this);
                    window.call("onAlert", new Object[]{"Hey, it's time to hit the lock button!"});
                }
            }
        } catch (JSException e) {
            // running in applet viewer?
            logger.warning("Unable to call JavaScript method [onAlert].");
        }
    }

    @Override
    public void onAutoLockWait(int weight, boolean lockable) {
    }

    @Override
    public void onLocked(int weight) {
        if (!lockedFired) {
            try {
                lockedFired = true;
                JSObject window = JSObject.getWindow(this);
                window.call("onLocked", new Object[]{weight / TON_DENOMINATOR});
            } catch (JSException e) {
                // running in applet viewer?
                logger.warning(String.format("Unable to call JavaScript method [onLocked] with weight [%f].", (weight / TON_DENOMINATOR)));
            }
        }
    }

    @Override
    public void onUnlocked() {
        resetEventStates();
        zeroWaitFired = true;

        try {
            JSObject window = JSObject.getWindow(this);
            window.call("onUnlocked", new Object[]{});
        } catch (JSException e) {
            // running in applet viewer?
            logger.warning(String.format("Unable to call JavaScript method [onUnlocked]."));
        }
    }

    @Override
    public void onCommError(ErrorCode errorCode, Throwable t) {
        try {
            t.printStackTrace();

            try {
                scaleSignalGenerator.stop();
            } catch (Exception e) {
                // nothing else we can do
                e.printStackTrace();
            }

            JSObject window = JSObject.getWindow(this);
            window.call("onCommError", new Object[]{errorCode.getCode()});
        } catch (JSException e) {
            // running in applet viewer?
            logger.warning("Unable to call JavaScript method [onCommError] with code [" + errorCode.getCode() + "].");
        }
    }
}

