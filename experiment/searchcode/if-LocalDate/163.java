package android.drm.mobile1;

import java.util.Date;

public class DrmConstraintInfo
{
    private int count = -1;
    private long endDate = -1L;
    private long interval = -1L;
    private long startDate = -1L;

    public int getCount()
    {
        return this.count;
    }

    public Date getEndDate()
    {
        if (this.endDate == -1L);
        for (Date localDate = null; ; localDate = new Date(this.endDate))
            return localDate;
    }

    public long getInterval()
    {
        return this.interval;
    }

    public Date getStartDate()
    {
        if (this.startDate == -1L);
        for (Date localDate = null; ; localDate = new Date(this.startDate))
            return localDate;
    }
}

/* Location:                     /home/lithium/miui/chameleon/2.11.16/framework_dex2jar.jar
 * Qualified Name:         android.drm.mobile1.DrmConstraintInfo
 * JD-Core Version:        0.6.2
 */
