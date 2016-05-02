package com.locate.rmds.statistic;

import java.io.IOException;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServerConnection;

import com.sun.management.OperatingSystemMXBean;

/**
 * Utility to compute resources such as CPU, memory, and maintain running
 * statistics.
 */
public class ResourceStatistics
{
    // access to system resources 
    static OperatingSystemMXBean _osBean = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    static OperatingSystemMXBean _osLinuxMBean = null;
    
    // memory
    long   memoryCount;
    public double   cumMemoryAverage;
    double cumMemoryMax;
    double cumMemoryMin;
    public double currentMemoryUsage;
    
    // CPU
    long   CPUCount;
    public double   cumCPUAverage;
    double cumCPUMax;
    double cumCPUMin;
    double currentCPULoad;

    // OS support
    boolean _bIsLinux = false;
    long _nanoBefore;
    long _cpuBefore;
    
    /* constructor, initialize */
    public ResourceStatistics()
    {
        String OSName = getOsName();
        if( OSName.startsWith("Linux") )
        {
            _bIsLinux = true;

            MBeanServerConnection mbsc = ManagementFactory.getPlatformMBeanServer();

            try
            {
                _osLinuxMBean = ManagementFactory.newPlatformMXBeanProxy(
                                                                     mbsc, 
                                                                     ManagementFactory.OPERATING_SYSTEM_MXBEAN_NAME, 
                                                                     OperatingSystemMXBean.class);
            }
            catch (IOException e)
            {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        clear();
    }

    public void clear()
    {
        CPUCount = 0;
        memoryCount = 0;
        
        cumMemoryAverage = 0;
        cumMemoryMax = -Long.MIN_VALUE;
        cumMemoryMin = Long.MAX_VALUE;

        cumCPUAverage = 0;
        cumCPUMax = -Long.MIN_VALUE;
        cumCPUMin = Long.MAX_VALUE;
    }
    
    public String getOsName()
    {
        String OS = System.getProperty("os.name");
        return OS;
    }

    public String getCurrentCPULoadAsPercentString()
    {
        if ( currentCPULoad < 0 )
            return "n/a";
        else
            return( String.format("%6.2f%%",currentCPULoad) );
    }
    
    public String getCurrentCPULoadAsString()
    {
        if ( currentCPULoad < 0 )
            return "n/a";
        else
            return( String.format("%.2f",currentCPULoad) );
    }
    
    // if count is 0, return 0 instead of the initialization value
    public double getCPUMaxValue()
    {
        if( CPUCount == 0 )
            return 0;
        else
            return cumCPUMax;
    }
    
    // if count is 0, return 0 instead of the initialization value
    public double getCPUMinValue()
    {
        if( CPUCount == 0 )
            return 0;
        else
            return cumCPUMin;
    }

    // if count is 0, return 0 instead of the initialization value
    public double getMemoryMaxValue()
    {
        if( memoryCount == 0 )
            return 0;
        else
            return cumMemoryMax;
    }
    
    // if count is 0, return 0 instead of the initialization value
    public double getMemoryMinValue()
    {
        if( memoryCount == 0 )
            return 0;
        else
            return cumMemoryMin;
    }

    // get the current resource usage and compute the stats 
    public void updateResourceStatistics()
    {
        // note: Java 7 (Oracle JDK) introduced the following method for obtaining the current proceses's CPU usage.
        // See http://sellmic.com/blog/2011/07/21/hidden-java-7-features-cpu-load-monitoring/ for details

        if( _bIsLinux == true )
            getLinuxCPUUsage();
        else
            currentCPULoad = _osBean.getProcessCpuLoad() * 100;

        currentMemoryUsage = (_osBean.getCommittedVirtualMemorySize() / 1048576.0);

          // CPU Load - max & min
        if( (long)currentCPULoad > 0 )
        {
            ++CPUCount;
            
            if (currentCPULoad > cumCPUMax) 
                cumCPUMax = currentCPULoad;

            if (currentCPULoad < cumCPUMin) 
                cumCPUMin = currentCPULoad;

            // Update the cumulative average for CPU 
            // which is equal to the previous cumulative average plus 
            // the difference between the latest data point and 
            // the previous average divided by the number of points received so far     

            cumCPUAverage = (currentCPULoad + cumCPUAverage * (  CPUCount-1))/CPUCount;
        }

        // Memory - max & min 
        if( (long)currentMemoryUsage > 0 )
        {
            ++memoryCount;
            
            if (currentMemoryUsage > cumMemoryMax) 
                cumMemoryMax = currentMemoryUsage;

            if (currentMemoryUsage < cumMemoryMin) 
                cumMemoryMin = currentMemoryUsage;
            
            // Update the cumulative average for memory usage, 
            // which is equal to the previous cumulative average plus 
            // the difference between the latest data point and 
            // the previous average divided by the number of points received so far  
            
            cumMemoryAverage = (currentMemoryUsage + cumMemoryAverage * (  memoryCount-1))/memoryCount;
        }
    }
    
    void getLinuxCPUUsage()
    {
        long cpuAfter = _osLinuxMBean.getProcessCpuTime();
        long nanoAfter = System.nanoTime();

        long percent;
        if (nanoAfter > _nanoBefore)
         percent = ((cpuAfter-_cpuBefore)*100L)/(nanoAfter-_nanoBefore);
        else 
            percent = 0;

        currentCPULoad = percent;
        
        _nanoBefore = nanoAfter;
        _cpuBefore = cpuAfter;
    }
}

