package com.jeasonzhao.report.dataset;

import java.util.Comparator;

import com.jeasonzhao.commons.basic.TimePointer;
import com.jeasonzhao.commons.utils.Guid;

public class RowInfoComparator implements Comparator<Object>
{
    private SortSettingCollection m_settings = null;
    public RowInfoComparator()
    {
    }

    public RowInfoComparator(SortSettingCollection s)
    {
        m_settings = s;
    }

    public RowInfoComparator(int ncol,boolean b)
    {
        this(new SortSetting(ncol,b));
    }

    public RowInfoComparator(int ncol)
    {
        this(new SortSetting(ncol));
    }

    public RowInfoComparator(SortSetting s)
    {
        if(s == null)
        {
            return;
        }
        m_settings = new SortSettingCollection();
        m_settings.add(s);
    }

    public RowInfoComparator add(int ncol,boolean b)
    {
        return this.add(new SortSetting(ncol,b));
    }

    public RowInfoComparator add(int ncol)
    {
        return this.add(new SortSetting(ncol));
    }

    public RowInfoComparator add(SortSetting s)
    {
        if(s == null)
        {
            return this;
        }
        if(null == m_settings)
        {
            m_settings = new SortSettingCollection();
        }
        m_settings.add(s);
        return this;
    }

    public RowInfoComparator add(SortSetting[] s)
    {
        if(s == null)
        {
            return this;
        }
        if(null == m_settings)
        {
            m_settings = new SortSettingCollection();
        }
        m_settings.addAll(s);
        return this;
    }

    public RowInfoComparator add(SortSettingCollection s)
    {
        if(s == null)
        {
            return this;
        }
        if(null == m_settings)
        {
            m_settings = new SortSettingCollection();
        }
        m_settings.addAll(s);
        return this;
    }

    public int compare(Object o1,Object o2)
    {
        if(null == m_settings || null == o1 || null == o2)
        {
            return null == o2 ? 1 : (o1 == null ? -1 : 0);
        }
        if(o1 instanceof RowInfo && o2 instanceof RowInfo)
        {
            java.util.HashSet<Integer> set = new java.util.HashSet<Integer>();
            RowInfo row1 = (RowInfo) o1;
            RowInfo row2 = (RowInfo) o2;
            for(int n = 0;n < this.m_settings.size();n++)
            {
                SortSetting s = this.m_settings.get(n);
                if(s == null || s.getColumnIndex() >= row1.size() || s.getColumnIndex() >= row2.size() ||
                   set.contains(new Integer(s.getColumnIndex())))
                {
                    continue;
                }
                set.add(Integer.valueOf(s.getColumnIndex()));
                DataCell cell1 = row1.elementAt(s.getColumnIndex());
                DataCell cell2 = row2.elementAt(s.getColumnIndex());
                int nx = cell1 == null ? -1 : cell1.compareTo(cell2);
                nx = s.isAscend() ? nx : 0 - nx;
                if(nx != 0)
                {
                    return nx;
                }
            }
            return 0;
        }
        else
        {
            return 0; //do not handler
        }
    }

    public int size()
    {
        return null == m_settings ? 0 : this.m_settings.size();
    }

    public boolean isContains(int ncol)
    {
        if(null == m_settings)
        {
            return false;
        }
        for(int n = 0;n < this.m_settings.size();n++)
        {
            if(m_settings.get(n).getColumnIndex() == ncol)
            {
                return true;
            }
        }
        return false;
    }

    public static void main(String[] argvs)
    {
        RowInfoCollection rows = new RowInfoCollection();
//        rows.createRow(new Object[]
//                       {"A","B","C","D"});
//        rows.createRow(new Object[]
//                       {"A","B2","C2","D2"});
//        rows.createRow(new Object[]
//                       {"A","B3","A1","D"});
//        rows.createRow(new Object[]
//                       {"E","B1","C1","D1"});
//        rows.createRow(new Object[]
//                       {"B","C1","A3","A"});
//        rows.createRow(new Object[]
//                       {"B","C3","B7","E"});
//        rows.createRow(new Object[]
//                       {"B","C2","B5","C"});
        for(int n = 0;n < 10000;n++)
        {
            rows.createRow(new Object[]
                           {Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
                           Guid.newGuid(),
            });
        }
//        System.out.println(rows.toCSV());
        RowInfoComparator p = new RowInfoComparator();
        p.add(0,false).add(1,true).add(2,true).add(3,true).add(4,true);
        TimePointer t = new TimePointer(true);
        rows.sort(p);
        long x = t.getMilliSecondes();
        System.out.println("------------------------------[" + rows.size() + "]-" + x + "-----------------------\r\n");
//        System.out.println(rows.toCSV());
    }
}

