/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package qosmonitor;

import core.EPAgent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import log.MyLogger;

/**
 *
 * @author epaln
 */
public class QoSMonitor {

    public static final short MODE_STAT_COLLECT = 0;
    public static final short MODE_MONITOR = 1;
    public static final short RESIZE_DIRECT = 0;
    public static final short RESIZE_PROGRESSIVE = 1;
    private static QoSMonitor instance = null;
    private final HashMap<String, QoSMeasures> allQosMeasures;
    private ArrayList<EPAgent> EPNetwork;
    private final ScheduledExecutorService scheduledExecutorService;
    private ScheduledFuture scheduledFuture = null;
    private final MyLogger logger;
    private int mode;
    private long _frequency;
    private MonitoringTask mTask;
    private short resizing_mode = 0;

    private QoSMonitor() {
        allQosMeasures = new HashMap<>();
        EPNetwork = new ArrayList<>();
        mode = MODE_MONITOR;
        scheduledExecutorService = Executors.newSingleThreadScheduledExecutor();
        logger = new MyLogger("QoSMeasures", QoSMonitor.class.getName());
        logger.log("EPU_IDENTIFIER, EPU_INFO, #EPU_NETWORK_NOTIFICATION, EPU_MEAN_NOTIFICATION_LATENCY, EPU_OUTPUTQ_CAPACITY, EPU_PROCESSING_TIME, #EVENTS_PROCESSED, #EVENTS_PRODUCED, #INPUTQ_DEFAULTS, BATCH_SIZE");
        mTask = new MonitoringTask(this);
    }

    public int getMode() {
        return mode;
    }

    public void setMode(int mode) {
        this.mode = mode;
    }

    public MonitoringTask getmTask() {
        return mTask;
    }

    public long getFrequency() {
        return _frequency;
    }

    public short getResizing_mode() {
        return resizing_mode;
    }

    public void setResizing_mode(short resizing_mode) {
        this.resizing_mode = resizing_mode;
    }

    public void setFrequency(long _frequency) {
        this._frequency = _frequency;
    }

    public static QoSMonitor getInstance() {
        if (instance == null) {
            instance = new QoSMonitor();
        }
        return instance;
    }

    /**
     * start the QOS monitor after a time period, and monitor with a given
     * frequency. The behavior is this one: (i) wait a specific time period,
     * (ii) start the QoS Monitor with the given frequency
     *
     * @param skipAfter the time period before starting the QoSMonitor
     * @param frequency the frequency at which the QoSMonitor should be started
     * @param unit the associated time unit
     */
    public void startMonitoringAfter(long skipPeriod, long frequency, TimeUnit unit) {
        if (unit == TimeUnit.DAYS) {
            _frequency = frequency * 86400000;
        } else if (unit == TimeUnit.HOURS) {
            _frequency = frequency * 3600000;
        } else if (unit == TimeUnit.MINUTES) {
            _frequency = frequency * 60000;
        } else if (unit == TimeUnit.SECONDS) {
            _frequency = frequency * 1000;
        } else {
            _frequency = frequency;
        }
        mTask.setStepsBeforeMonitoring(skipPeriod);
        scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(mTask, _frequency, _frequency, TimeUnit.MILLISECONDS);
    }

    public void startMonitoring() {
        startMonitoring(1, TimeUnit.MINUTES);
    }

    public void startMonitoring(long frequency, TimeUnit timeUnit) {
        _frequency = frequency;
        startMonitoringAfter(1, frequency, timeUnit);
        //scheduledFuture = scheduledExecutorService.scheduleAtFixedRate(new MonitoringTask(5), frequency, frequency, timeUnit);
    }

    public void stopMonitoring() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }

    public void addEPAgent(EPAgent epa) {
        EPNetwork.add(epa);
    }

    public HashMap<String, QoSMeasures> getQosMeasuresAllEpa() {
        return allQosMeasures;
    }

    public ArrayList<EPAgent> getEPNetwork() {
        return EPNetwork;
    }

    public void setEPNetwork(ArrayList<EPAgent> EPNetwork) {
        this.EPNetwork = EPNetwork;
    }

    public MyLogger getLogger() {
        return logger;
    }
}

class MonitoringTask implements Runnable {

    private long stepsBeforeMonitoring;
    static boolean isMonitoring = false;
    static long turn = 0;
    double numInsertions = 1;
    private QoSMonitor qosMonitor;

    public MonitoringTask(QoSMonitor qosm) {
        stepsBeforeMonitoring = 0;
        qosMonitor = qosm;
    }

    public MonitoringTask(QoSMonitor qosm, long stepsBeforeMonitoring) {
        qosMonitor = qosm;
        this.stepsBeforeMonitoring = stepsBeforeMonitoring;
    }

    public long getStepsBeforeMonitoring() {
        return stepsBeforeMonitoring;
    }

    public void setStepsBeforeMonitoring(long stepsBeforeMonitoring) {
        this.stepsBeforeMonitoring = stepsBeforeMonitoring;
    }

    @Override
    public void run() {
        turn++;
        if (turn == stepsBeforeMonitoring) {
            isMonitoring = true;
        }
        QoSTuner tuner = new QoSTuner();
        for (EPAgent epa : QoSMonitor.getInstance().getEPNetwork()) {
            QoSMeasures qosM = QoSMonitor.getInstance().getQosMeasuresAllEpa().get(epa.getName());
            if (qosM == null) {
                qosM = new QoSMeasures();
            }
            // collect the QoS measures of each epu
            if (epa.numEventNotifiedNetwork == 0) { // no networked event notification, => this epa notified locally, the latency is set to 0, means negligible
                qosM.setObservedMeanLatency(0);
                qosM.setObservedmeanNetworkLatency(0);
            } else {
                qosM.setObservedMeanLatency(Math.round(epa.sumLatencies / epa.numEventNotifiedNetwork));
                qosM.setObservedmeanNetworkLatency(Math.round(epa.sumNetworkLatencies / epa.numEventNotifiedNetwork));
            }
            qosM.setObservedNumberNotifications(epa.numAchievedNotifications);
            qosM.setNumEventProcessed(epa.numEventProcessed);
            qosM.setProcessingTime(epa.processingTime);
            qosM.setNumEventProduced(epa.numEventProduced);
            numInsertions = qosM.getNumEventProduced(); // / qosMonitor.getFrequency();
            // reset the counters
            epa.sumLatencies = 0;
            epa.numEventNotifiedNetwork = 0;
            epa.numEventNotified = 0;
            epa.numEventProcessed = 0;
            epa.numAchievedNotifications = 0;
            epa.processingTime = 0;
            epa.numEventProduced = 0;
            epa.sumNetworkLatencies = 0;
            QoSMonitor.getInstance().getQosMeasuresAllEpa().put(epa.getName(), qosM);
            //log the qos measures
            int def = epa.getInputTerminals().iterator().next().getReceiver().getInputQueue().getNumberDefaults();
            int size = epa.getInputTerminals().iterator().next().getReceiver().getInputQueue().size();
            QoSMonitor.getInstance().getLogger().log(epa.getName() + ", " + epa.getInfo() + ", " + qosM.getObservedNumberNotifications() + ", "
                    + qosM.getObservedMeanLatency() + ", " + epa.getOutputQueue().getCapacity() + ", " + qosM.getProcessingTime() + ", "
                    + qosM.getNumEventProcessed() + ", " + qosM.getNumEventProduced() + ", " + def + "/" + size + ", " + epa.getOutputNotifier().getBatch_size());

            // check for any constraint violation if monitoring is activated
            if ((QoSMonitor.getInstance().getMode() == QoSMonitor.MODE_MONITOR) && isMonitoring) {
                // network occupation violation ?
                tuner.bound(epa);
                if ((epa.getQosConstraint().getNetworkOccupationMax() != 0) && (qosM.getObservedNumberNotifications() > epa.getQosConstraint().getNetworkOccupationMax())) {
                    if (epa.getQosConstraint().getNetworkOccupationMax() == 0) {
                        tuner.setNotificationStrategy(QoSTuner.NOTIFICATION_PRIORITY);
                    } else {
                        tuner.setNotificationStrategy(QoSTuner.NOTIFICATION_BATCH);
                        float observed, maxim;
                        observed = qosM.getObservedNumberNotifications();
                        maxim = epa.getQosConstraint().getNetworkOccupationMax();
                        tuner.setNotificationBatchSize((int) Math.ceil(observed / maxim));
                        System.out.println("[QoSMonitor] Batch notification strategy applied at " + epa.getName() + "-" + epa.getInfo());
                        System.out.println("Network occupation max: " + epa.getQosConstraint().getNetworkOccupationMax()
                                + "; Observed: " + qosM.getObservedNumberNotifications());
                        System.out.println("new batch size: " + epa.getOutputNotifier().getBatch_size());
                    }
                }
                // latency violation?
                if ((epa.getQosConstraint().getMaxLatency() != 0) && (qosM.getObservedMeanLatency() > epa.getQosConstraint().getMaxLatency())) {
                    long newK;

                    System.out.println("[QoSMonitor] Notification latency violated at " + epa.getName() + "-" + epa.getInfo());
                    System.out.println("Notification latency max: " + epa.getQosConstraint().getMaxLatency()
                            + "; Observed: " + qosM.getObservedMeanLatency());
                    if (epa.getOutputNotifier().getStrategy() == QoSTuner.NOTIFICATION_BATCH) {
                        if (epa.getQosConstraint().getMaxLatency() <= qosM.getObservedmeanNetworkLatency()) {
                            // we cannot do anything, the network is too slow... at least, we set the batch size to 1 to do our best
                            System.out.println("[Warning] The given latency cannot be achieved with the current network. New batch size is 1");
                            tuner.setNotificationBatchSize(1);
                        } else {
                            if (QoSMonitor.getInstance().getResizing_mode() == QoSMonitor.RESIZE_PROGRESSIVE) {
                                // update the size of the batch                        
                                newK = Math.max(1, (int) Math.floor(epa.getOutputNotifier().getBatch_size() * epa.getQosConstraint().getMaxLatency() / qosM.getObservedMeanLatency()));
                                long deltaTime = epa.getQosConstraint().getMaxLatency() - qosM.getObservedmeanNetworkLatency();
                                System.out.println("old batch size: " + epa.getOutputNotifier().getBatch_size() + "; new batch size: " + newK+" delta: "+deltaTime);                                
                                tuner.setNotificationBatchSize((int) newK, deltaTime);
                            } else { // resize direct...
                                long deltaTime = epa.getQosConstraint().getMaxLatency() - qosM.getObservedmeanNetworkLatency();
                                double kk = deltaTime * (numInsertions / qosMonitor.getFrequency());
//                                kk *= qosM.getObservedMeanLatency();
//                                kk /= epa.getQosConstraint().getMaxLatency();
                                newK = Math.max(1, Math.round(kk));
                                System.out.println("old batch size: " + epa.getOutputNotifier().getBatch_size() + "; new batch size: " + newK);
                                System.out.println("delta, numInsertion, period= " + deltaTime + ", " + numInsertions + ", " + qosMonitor.getFrequency());
                                
                                /*long delta = qosM.getObservedMeanLatency() - epa.getQosConstraint().getMaxLatency();
                                double kk = epa.getOutputNotifier().getBatch_size() - (numInsertions*delta/qosMonitor.getFrequency());
                                newK = Math.max(1, Math.round(kk));                            
                                System.out.println("old batch size: " + epa.getOutputNotifier().getBatch_size() + "; new batch size: " + newK);
                                System.out.println("delta, numInsertion, period= " + delta + ", " + numInsertions + ", " + qosMonitor.getFrequency());
                                        */
                                tuner.setNotificationBatchSize((int) newK);
                            }
                        }

                    } else {
                        newK = Math.max(1, (int) Math.floor(epa.getOutputQueue().getCapacity() * epa.getQosConstraint().getMaxLatency() / qosM.getObservedMeanLatency()));
                        System.out.println("old ouputQ size: " + epa.getOutputQueue().getCapacity() + "; new outputQ size: " + newK);
                        tuner.setOutputQueueCapacity((int) newK);
                    }

                }
            }
        }
    }

}

