/*
 * jPOS Project [http://jpos.org]
 * Copyright (C) 2000-2013 Alejandro P. Revilla
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jpos.q2.qbean;

import org.jpos.core.Configuration;
import org.jpos.core.ConfigurationException;
import org.jpos.iso.ISOUtil;
import org.jpos.q2.Q2;
import org.jpos.q2.QBeanSupport;
import org.jpos.util.LogEvent;
import org.jpos.util.Loggeable;
import org.jpos.util.Logger;
import org.jpos.util.NameRegistrar;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.InetAddress;

/**
 * Periodically dumps Thread and memory usage
 * 
 * @author apr@cs.com.uy
 * @version $Id$
 * @jmx:mbean description="System Monitor"
 *            extends="org.jpos.q2.QBeanSupportMBean"
 * @see Logger
 */
public class SystemMonitor extends QBeanSupport
        implements Runnable, SystemMonitorMBean, Loggeable
{
    private long sleepTime = 60 * 60 * 1000;
    private long delay = 0;
    private boolean detailRequired = false;
    private Thread me = null;
    private static final int MB = 1024*1024;
    private String[] scripts;
    private String frozenDump;

    public void startService() {
        try {
            log.info("Starting SystemMonitor");
            me = new Thread(this,"SystemMonitor");
            me.start();
        } catch (Exception e) {
            log.warn("error starting service", e);
        }
    }

    public void stopService() {
        log.info("Stopping SystemMonitor");
        if (me != null)
            me.interrupt();
    }

    /**
     * @jmx:managed-attribute description="Milliseconds between dump"
     */
    public synchronized void setSleepTime(long sleepTime) {
        this.sleepTime = sleepTime;
        setModified(true);
        if (me != null)
            me.interrupt();
    }

    /**
     * @jmx:managed-attribute description="Milliseconds between dump"
     */
    public synchronized long getSleepTime() {
        return sleepTime;
    }

    /**
     * @jmx:managed-attribute description="Detail required?"
     */
    public synchronized void setDetailRequired(boolean detail) {
        this.detailRequired = detail;
        setModified(true);
        if (me != null)
            me.interrupt();
    }

    /**
     * @jmx:managed-attribute description="Detail required?"
     */
    public synchronized boolean getDetailRequired() {
        return detailRequired;
    }

    void dumpThreads(ThreadGroup g, PrintStream p, String indent) {
        Thread[] list = new Thread[g.activeCount() + 5];
        int nthreads = g.enumerate(list);
        for (int i = 0; i < nthreads; i++)
            p.println(indent + list[i]);
    }

    public void showThreadGroup(ThreadGroup g, PrintStream p, String indent) {
        if (g.getParent() != null)
            showThreadGroup(g.getParent(), p, indent + "  ");
        else
            dumpThreads(g, p, indent + "    ");
    }

    public void run() {
        while (running()) {
            log.info(this);
            frozenDump = null;
            try {
                long expected = System.currentTimeMillis() + sleepTime;
                Thread.sleep(sleepTime);
                delay = (System.currentTimeMillis() - expected);
            } catch (InterruptedException e) {
            }
        }
    }
    public void dump (PrintStream p, String indent) {
        if (frozenDump == null)
            frozenDump = generateFrozenDump(indent);
        p.print(frozenDump);
    }
    @Override
    public void setConfiguration(Configuration cfg) throws ConfigurationException {
        super.setConfiguration(cfg);
        scripts = cfg.getAll("script");
    }

    private SecurityManager getSecurityManager() {
        return System.getSecurityManager();
    }

    private boolean hasSecurityManager() {
        return getSecurityManager() != null;
    }

    private Runtime getRuntimeInstance() {
	    return Runtime.getRuntime();
    }

    private long getServerUptimeAsMillisecond() {
        return getServer().getUptime();
    }

    private String getInstanceIdAsString() {
        return getServer().getInstanceId().toString();
    }

    private String getRevision() {
        return getServer().getRevision();
    }
    private String getLocalHost () {
        try {
            return InetAddress.getLocalHost().toString();
        } catch (Exception e) {
            return e.getMessage();
        }
    }
    private String generateFrozenDump(String indent) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream p = new PrintStream(baos);
        String newIndent = indent + "  ";
        Runtime r = getRuntimeInstance();
        p.printf ("%s           OS: %s%n", indent, System.getProperty("os.name"));
        p.printf ("%s         host: %s%n", indent, getLocalHost());
        p.printf ("%s      version: %s (%s)%n", indent, Q2.getVersion(), getRevision());
        p.printf ("%s     instance: %s%n", indent, getInstanceIdAsString());
        p.printf ("%s       uptime: %s%n", indent, ISOUtil.millisToString(getServerUptimeAsMillisecond()));
        p.printf ("%s   processors: %d%n", indent, r.availableProcessors());
        p.printf ("%s       drift : %d%n", indent, delay);
        p.printf ("%smemory(t/u/f): %d/%d/%d%n", indent,
                r.totalMemory()/MB, (r.totalMemory() - r.freeMemory())/MB, r.freeMemory()/MB);
        if (hasSecurityManager())
            p.printf("%s  sec-manager: %s%n", indent, getSecurityManager());
        p.printf("%s      threads: %d%n", indent, Thread.activeCount());
        showThreadGroup(Thread.currentThread().getThreadGroup(), p, newIndent);
        NameRegistrar.getInstance().dump(p, indent, detailRequired);
        for (String s : scripts) {
            p.printf("%s%s:%n", indent, s);
            exec(s, p, newIndent);
        }
        return baos.toString();
    }
    private void exec (String script, PrintStream ps, String indent) {
        try {
            Process p = Runtime.getRuntime().exec(script);
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()) );
            String line;
            while ((line = in.readLine()) != null) {
                ps.printf("%s%s%n", indent, line);
            }
        } catch (Exception e) {
            e.printStackTrace(ps);
        }
    }
}

