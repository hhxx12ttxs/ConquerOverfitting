// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.nnee.p_pr.p_activity.p_a;

import android.content.Context;
import android.view.*;
import android.widget.*;
import com.nnee.p_fk.cls_k;
import com.nnee.p_img.cls_d;
import com.nnee.p_pr.p_activity.p_view.cls_c;
import com.nnee.p_pr.p_atom.cls_g;
import com.nnee.p_pr.p_atom.cls_u;
import java.util.LinkedList;

// Referenced classes of package com.nnee.p_pr.p_activity.a:
//            q, e

public class cls_p extends BaseAdapter
{

    public cls_p(Context context)
    {
        b = null;
        d = null;
        e = null;
        if(context != null)
        {
            b = context;
            d = LayoutInflater.from(b);
        }
    }

    private void a(q q1, View view)
    {
        q1.a = (ImageView)view.findViewById(0x7f0d0024);
        q1.b = (ImageView)view.findViewById(0x7f0d007a);
        q1.c = (TextView)view.findViewById(0x7f0d0018);
        q1.d = (TextView)view.findViewById(0x7f0d007d);
        q1.e = (Button)view.findViewById(0x7f0d007e);
        q1.e.setOnClickListener(e);
    }

    private void a(q q1, cls_u u1, int i)
    {
        if(q1 != null && u1 != null) goto _L2; else goto _L1
_L1:
        return;
_L2:
        String s = u1.U();
        q1.c.setText(s);
        String s1 = u1.X();
        q1.d.setText(s1);
        q1.e.setFocusable(false);
        q1.e.setClickable(true);
        cls_c c1 = new cls_c();
        c1.a = u1.V();
        c1.b = i;
        q1.e.setTag(c1);
        c c2 = new cls_c();
        c2.a = (new StringBuilder()).append(c1.a).append("mark").toString();
        q1.b.setTag(c2);
        if(!u1.ak())
            break MISSING_BLOCK_LABEL_271;
        q1.e.setVisibility(0);
        q1.b.setVisibility(0);
        q1.e.setText(0x7f0a0057);
        q1.e.setCompoundDrawablesWithIntrinsicBounds(0x7f02002c, 0, 0, 0);
_L3:
        q1.a.setImageDrawable(k.a(b).b(0x7f02012c));
        String s2 = u1.b(g.g);
        if(s2 != null)
        {
            StringBuilder stringbuilder = new StringBuilder();
            q1.a.setTag(stringbuilder);
            com.nnee.p_img.cls_d.a().a(stringbuilder, 1, s2, new cls_e(this, q1), -1, -1, 2, a);
        }
          goto _L1
        NullPointerException nullpointerexception;
        nullpointerexception;
        nullpointerexception.printStackTrace();
          goto _L1
        if(u1.al())
        {
            q1.e.setVisibility(0);
            q1.b.setVisibility(8);
            q1.e.setText(0x7f0a0075);
            q1.e.setCompoundDrawablesWithIntrinsicBounds(0x7f02002b, 0, 0, 0);
        } else
        {
            q1.e.setVisibility(8);
            q1.b.setVisibility(8);
        }
          goto _L3
    }

    /**
     * @deprecated Method a is deprecated
     */

    public void a()
    {
        this;
        JVM INSTR monitorenter ;
        if(c != null)
        {
            c.clear();
            notifyDataSetChanged();
        }
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public void a(int i)
    {
        a = i;
    }

    public void a(android.view.View.OnClickListener onclicklistener)
    {
        e = onclicklistener;
    }

    public void a(LinkedList linkedlist)
    {
        c = linkedlist;
    }

    public cls_u b(int i)
    {
        cls_u u1;
        if(c != null && i >= 0 && c.size() > 0)
        {
            if(i >= c.size())
                i = -1 + c.size();
            u1 = (u)c.get(i);
        } else
        {
            u1 = null;
        }
        return u1;
    }

    /**
     * @deprecated Method b is deprecated
     */

    public void b(LinkedList linkedlist)
    {
        this;
        JVM INSTR monitorenter ;
        if(c != null)
        {
            c.addAll(linkedlist);
            notifyDataSetChanged();
        }
        this;
        JVM INSTR monitorexit ;
        return;
        Exception exception;
        exception;
        throw exception;
    }

    public int getCount()
    {
        int i = 0;
        if(c != null)
            i = c.size();
        return i;
    }

    public Object getItem(int i)
    {
        return b(i);
    }

    public long getItemId(int i)
    {
        return (long)i;
    }

    public View getView(int i, View view, ViewGroup viewgroup)
    {
        q q1;
        cls_u u1;
        if(view != null && (view.getTag() instanceof cls_q))
        {
            q1 = (q)view.getTag();
        } else
        {
            view = d.inflate(0x7f030026, null);
            q1 = new cls_q(this);
            a(q1, view);
            view.setTag(q1);
        }
        u1 = b(i);
        if(u1 != null)
            a(q1, u1, i);
        return view;
    }

    private int a;
    private Context b;
    private LinkedList c;
    private LayoutInflater d;
    private android.view.View.OnClickListener e;
}

