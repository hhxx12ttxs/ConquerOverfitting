// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.nnee.p_pr.p_activity.p_view;

import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.*;
import android.view.animation.*;
import android.widget.*;
import com.nnee.p_imgex.p_a.cls_a;
import java.util.LinkedList;

// Referenced classes of package com.nnee.p_pr.p_activity.view:
//            bv, bu, a, bl, 
//            bi, ap, i, r, 
//            ao, ca, bs, s

public class cls_FlingGallery extends  FrameLayout
{

    public cls_FlingGallery(Context context)
    {
        this(context, null);
    }

    public cls_FlingGallery(Context context, AttributeSet attributeset)
    {
        super(context, attributeset);
        d = 60;
        e = 300;
        f = 250;
        h = 0;
        i = 500;
        j = 0.7F;
        k = false;
        l = 0;
        m = 0;
        n = false;
        o = false;
        p = false;
        q = 0F;
        r = 0L;
        s = 0;
        t = 0;
        u = 0;
        F = null;
        G = null;
        H = false;
        I = 0;
        K = new LinkedList();
        N = 0;
        O = 0;
        P = new bv(this);
        S = true;
        T = false;
        U = false;
        ab = new bu(this);
        a(context);
    }

    static int A(cls_FlingGallery flinggallery)
    {
        return flinggallery.I;
    }

    static boolean B(cls_FlingGallery flinggallery)
    {
        return flinggallery.S;
    }

    static int C(cls_FlingGallery flinggallery)
    {
        return flinggallery.O;
    }

    static int D(cls_FlingGallery flinggallery)
    {
        return flinggallery.N;
    }

    static cls_ca E(cls_FlingGallery flinggallery)
    {
        return flinggallery.E;
    }

    static cls_ao F(cls_FlingGallery flinggallery)
    {
        return flinggallery.D;
    }

    static cls_r G(cls_FlingGallery flinggallery)
    {
        return flinggallery.C;
    }

    static float a(cls_FlingGallery flinggallery, float f1)
    {
        flinggallery.q = f1;
        return f1;
    }

    private int a(int i1, int j1)
    {
        int k1 = l + h;
        if(i1 != h(j1))
            if(i1 == i(j1))
                k1 *= -1;
            else
                k1 = 0;
        return k1;
    }

    static int a(cls_FlingGallery flinggallery, int i1)
    {
        return flinggallery.i(i1);
    }

    static int a(cls_FlingGallery flinggallery, int i1, int j1)
    {
        return flinggallery.a(i1, j1);
    }

    static long a(cls_FlingGallery flinggallery, long l1)
    {
        flinggallery.r = l1;
        return l1;
    }

    static LinkedList a(cls_FlingGallery flinggallery)
    {
        return flinggallery.K;
    }

    private void a(Context context)
    {
        v = context;
        w = null;
        x = new com.nnee.p_pr.p_activity.p_view.cls_a[3];
        x[0] = new com.nnee.p_pr.p_activity.p_view.cls_a(this, 0, this);
        x[1] = new com.nnee.p_pr.p_activity.p_view.cls_a(this, 1, this);
        x[2] = new com.nnee.p_pr.p_activity.p_view.cls_a(this, 2, this);
        y = new bl(this);
        a = new GestureDetector(new cls_bi(this, null));
        z = AnimationUtils.loadInterpolator(v, 0x10a0006);
        I = ViewConfiguration.get(context).getScaledTouchSlop();
        L = new Scroller(context, new DecelerateInterpolator());
        float f1 = context.getResources().getDisplayMetrics().density;
        N = (int)(f1 * (float)d);
        O = (int)(0.5D + (double)(f1 * (float)e));
    }

    static boolean a(cls_FlingGallery flinggallery, boolean flag)
    {
        flinggallery.J = flag;
        return flag;
    }

    static float b(cls_FlingGallery flinggallery, float f1)
    {
        flinggallery.Q = f1;
        return f1;
    }

    static int b(cls_FlingGallery flinggallery)
    {
        return flinggallery.t;
    }

    static int b(cls_FlingGallery flinggallery, int i1)
    {
        return flinggallery.h(i1);
    }

    static boolean b(cls_FlingGallery flinggallery, boolean flag)
    {
        flinggallery.n = flag;
        return flag;
    }

    static float c(cls_FlingGallery flinggallery, float f1)
    {
        flinggallery.R = f1;
        return f1;
    }

    static int c(cls_FlingGallery flinggallery, int i1)
    {
        return flinggallery.g(i1);
    }

    static Scroller c(cls_FlingGallery flinggallery)
    {
        return flinggallery.L;
    }

    private void c()
    {
        int i1;
        int j1;
        int k1;
        if(aa > 0)
        {
            i1 = i(X);
            j1 = g(t);
            k1 = h(X);
        } else
        {
            i1 = h(X);
            j1 = f(t);
            k1 = i(X);
        }
        x[X].c(X);
        if(x[i1].b() != j1)
            x[i1].e(j1);
        if(i1 != k1)
            x[k1].c(X);
        x[0].a(Z, 0, X);
        x[1].a(Z, 0, X);
        x[2].a(Z, 0, X);
        x[0].d(X);
        x[1].d(X);
        x[2].d(X);
        V = false;
        if(A != null)
            A.a(t);
        if(!W && aa > 0 && B != null && t == h())
            B.a();
        if(!W && aa < 0 && B != null && t == i())
            B.b();
        if(P != null)
            P.sendEmptyMessage(100);
    }

    static boolean c(cls_FlingGallery flinggallery, boolean flag)
    {
        flinggallery.o = flag;
        return flag;
    }

    static int d(cls_FlingGallery flinggallery)
    {
        return flinggallery.aa;
    }

    static int d(cls_FlingGallery flinggallery, int i1)
    {
        return flinggallery.f(i1);
    }

    static boolean d(cls_FlingGallery flinggallery, boolean flag)
    {
        flinggallery.p = flag;
        return flag;
    }

    static int e(cls_FlingGallery flinggallery)
    {
        return flinggallery.X;
    }

    static int e(cls_FlingGallery flinggallery, int i1)
    {
        flinggallery.s = i1;
        return i1;
    }

    static boolean e(cls_FlingGallery flinggallery, boolean flag)
    {
        flinggallery.S = flag;
        return flag;
    }

    private int f(int i1)
    {
        int j1 = i1 + -1;
        if(j1 < h())
            if(k)
                j1 = i();
            else
                j1 = -1 + h();
        return j1;
    }

    static com.nnee.p_pr.p_activity.p_view.a[] f(cls_FlingGallery flinggallery)
    {
        return flinggallery.x;
    }

    private int g(int i1)
    {
        int j1 = i1 + 1;
        if(j1 > i())
            if(k)
                j1 = h();
            else
                j1 = 1 + i();
        return j1;
    }

    static Handler g(cls_FlingGallery flinggallery)
    {
        return flinggallery.P;
    }

    private int h(int i1)
    {
        int j1;
        if(i1 == 0)
            j1 = 2;
        else
            j1 = i1 + -1;
        return j1;
    }

    static void h(cls_FlingGallery flinggallery)
    {
        flinggallery.c();
    }

    private int i(int i1)
    {
        int j1;
        if(i1 == 2)
            j1 = 0;
        else
            j1 = i1 + 1;
        return j1;
    }

    static Context i(cls_FlingGallery flinggallery)
    {
        return flinggallery.v;
    }

    static Adapter j(cls_FlingGallery flinggallery)
    {
        return flinggallery.w;
    }

    static int k(cls_FlingGallery flinggallery)
    {
        return flinggallery.u;
    }

    static s l(cls_FlingGallery flinggallery)
    {
        return flinggallery.G;
    }

    static int m(cls_FlingGallery flinggallery)
    {
        return flinggallery.m;
    }

    static int n(cls_FlingGallery flinggallery)
    {
        return flinggallery.i;
    }

    static Interpolator o(cls_FlingGallery flinggallery)
    {
        return flinggallery.z;
    }

    static ap p(cls_FlingGallery flinggallery)
    {
        return flinggallery.A;
    }

    static boolean q(cls_FlingGallery flinggallery)
    {
        return flinggallery.n;
    }

    static boolean r(cls_FlingGallery flinggallery)
    {
        return flinggallery.o;
    }

    static boolean s(cls_FlingGallery flinggallery)
    {
        return flinggallery.H;
    }

    static boolean t(cls_FlingGallery flinggallery)
    {
        return flinggallery.p;
    }

    static int u(cls_FlingGallery flinggallery)
    {
        return flinggallery.l;
    }

    static long v(cls_FlingGallery flinggallery)
    {
        return flinggallery.r;
    }

    static float w(cls_FlingGallery flinggallery)
    {
        return flinggallery.q;
    }

    static boolean x(cls_FlingGallery flinggallery)
    {
        return flinggallery.k;
    }

    static float y(cls_FlingGallery flinggallery)
    {
        return flinggallery.Q;
    }

    static float z(cls_FlingGallery flinggallery)
    {
        return flinggallery.R;
    }

    public void a()
    {
        s = 1;
        p();
    }

    public void a(float f1)
    {
        j = f1;
    }

    public void a(int i1)
    {
        h = i1;
    }

    public void a(Adapter adapter, int i1)
    {
        w = adapter;
        t = i1;
        x[u].e(t);
        x[i(u)].e(g(t));
        x[h(u)].e(f(t));
        x[u].a(0, 0, u);
        x[i(u)].a(0, 0, u);
        x[h(u)].a(0, 0, u);
    }

    public void a(cls_ao ao)
    {
        D = ao;
    }

    public void a(ap ap1)
    {
        A = ap1;
    }

    public void a(bs bs)
    {
        F = bs;
    }

    public void a(ca ca)
    {
        E = ca;
    }

    public void a(i i1)
    {
        B = i1;
    }

    public void a(r r1)
    {
        C = r1;
    }

    public void a(s s1)
    {
        G = s1;
    }

    public void a(String s1)
    {
        if(!x[0].a(s1) && !x[1].a(s1))
            x[2].a(s1);
    }

    public boolean a(int i1, int j1, Object obj)
    {
        if(i1 < 0 || i1 > 2)
            i1 = u;
        com.nnee.p_pr.p_activity.p_view.a a1 = x[i1];
        boolean flag;
        if(a1 == null)
            flag = false;
        else
            flag = a1.b(i1, j1, obj);
        return flag;
    }

    protected boolean a(MotionEvent motionevent)
    {
        boolean flag = true;
        float f1 = motionevent.getX();
        float f2 = motionevent.getY();
        int i1 = (int)Math.abs(f1 - Q);
        int j1 = (int)Math.abs(f2 - R);
        boolean flag1;
        if(i1 > I)
            flag1 = flag;
        else
            flag1 = false;
        if(!flag1 || j1 > i1)
            flag = false;
        return flag;
    }

    protected boolean a(MotionEvent motionevent, MotionEvent motionevent1, float f1, float f2)
    {
        return false;
    }

    public Object b(int i1, int j1, Object obj)
    {
        if(i1 < 0 || i1 > 2)
            i1 = u;
        com.nnee.p_pr.p_activity.p_view.a a1 = x[i1];
        Object obj1;
        if(a1 == null)
            obj1 = null;
        else
            obj1 = a1.a(i1, j1, obj);
        return obj1;
    }

    public void b()
    {
        s = -1;
        p();
    }

    public void b(int i1)
    {
        f = i1;
    }

    public void b(String s1)
    {
        if(!x[0].b(s1) && !x[1].b(s1))
            x[2].b(s1);
    }

    public void b(boolean flag)
    {
        H = flag;
    }

    public boolean b(MotionEvent motionevent)
    {
        boolean flag = false;
        if(a != null)
            flag = a.onTouchEvent(motionevent);
        if(motionevent.getAction() == 1 && (n || o || p))
        {
            q();
            p();
        }
        return flag;
    }

    public void c(int i1)
    {
        i = i1;
    }

    public void c(boolean flag)
    {
        if(k != flag)
        {
            k = flag;
            if(t == h())
                x[h(u)].e(f(t));
            if(t == i())
                x[i(u)].e(g(t));
        }
    }

    public void d(int i1)
    {
        if(t != i1)
        {
            t = i1;
            u = 0;
            x[0].e(t);
            x[1].e(g(t));
            x[2].e(f(t));
            x[0].a(0, 0, u);
            x[1].a(0, 0, u);
            x[2].a(0, 0, u);
        }
    }

    public void d(boolean flag)
    {
        U = flag;
    }

    public void e(int i1)
    {
        byte byte0 = -1;
        J = true;
        W = false;
        if(X != i1)
        {
            if(V)
            {
                int j1;
                if(i1 == h(X))
                    j1 = 1;
                else
                    j1 = byte0;
                if(aa < 0)
                    byte0 = 1;
                if(byte0 == j1)
                {
                    x[0].a(Z, 0, X);
                    x[1].a(Z, 0, X);
                    x[2].a(Z, 0, X);
                }
            }
            X = i1;
            W = true;
        }
        Y = x[X].c();
        Z = a(X, X);
        aa = Z - Y;
        V = true;
        L.startScroll(Y, 0, aa, 0);
        P.post(ab);
    }

    public com.nnee.p_pr.p_activity.p_view.cls_a[] e()
    {
        return x;
    }

    public void f()
    {
        if(x != null)
        {
            int i1 = x.length;
            for(int j1 = 0; j1 < i1; j1++)
            {
                x[j1].a();
                x[j1] = null;
            }

        }
        if(K != null)
            K.clear();
        if(P != null)
        {
            P.removeMessages(100);
            P.removeCallbacks(null);
            P = null;
        }
        y = null;
        a = null;
        w = null;
        v = null;
    }

    public int g()
    {
        int i1;
        if(w == null)
            i1 = 0;
        else
            i1 = w.getCount();
        return i1;
    }

    public int h()
    {
        return 0;
    }

    public int i()
    {
        int i1;
        if(g() == 0)
            i1 = 0;
        else
            i1 = -1 + g();
        return i1;
    }

    public int j()
    {
        return l;
    }

    public int k()
    {
        return m;
    }

    public int l()
    {
        return t;
    }

    public int m()
    {
        return u;
    }

    public void n()
    {
        LinkedList linkedlist = K;
        linkedlist;
        JVM INSTR monitorenter ;
        if(J)
            K.add(new Integer(-1));
        else
            a();
        return;
    }

    public void o()
    {
        LinkedList linkedlist = K;
        linkedlist;
        JVM INSTR monitorenter ;
        if(J)
            K.add(new Integer(1));
        else
            b();
        return;
    }

    public boolean onInterceptTouchEvent(MotionEvent motionevent)
    {
        boolean flag = false;
        if(!U) goto _L2; else goto _L1
_L1:
        return flag;
_L2:
        int i1 = com.nnee.p_imgex.p_a.cls_a.a(motionevent).b();
        if(!o && i1 == a.c)
        {
            T = true;
            MotionEvent motionevent2 = MotionEvent.obtain(motionevent);
            motionevent2.setAction(3);
            a.onTouchEvent(motionevent2);
        } else
        if(i1 == 1 && T)
            T = flag;
        else
        if(!T)
            switch(i1)
            {
          

            case 0: // '\0'
                flag = J;
                if(!J)
                {
                    Q = motionevent.getX();
                    R = motionevent.getY();
                    S = true;
                    if(a != null)
                        a.onTouchEvent(motionevent);
                }
                break;

            case 2: // '\002'
                flag = a(motionevent);
                if(!flag)
                    flag = a.onTouchEvent(motionevent);
                break;

            case 1: // '\001'
            case 3: // '\003'
                flag = J;
                if(flag)
                    break;
                MotionEvent motionevent1 = MotionEvent.obtain(motionevent);
                if(a != null)
                    a.onTouchEvent(motionevent1);
                break;
            default:
                break;
            }
        if(true) goto _L1; else goto _L3
_L3:
    }

    protected void onLayout(boolean flag, int i1, int j1, int k1, int l1)
    {
        l = k1 - i1;
        m = l1 - j1;
        if(flag && x != null)
        {
            if(x[0] != null)
            {
                x[0].a(m);
                x[0].a(0, 0, u);
            }
            if(x[1] != null)
            {
                x[1].a(m);
                x[1].a(0, 0, u);
            }
            if(x[2] != null)
            {
                x[2].a(m);
                x[2].a(0, 0, u);
            }
        }
        super.onLayout(flag, i1, j1, k1, l1);
    }

    public boolean onTouchEvent(MotionEvent motionevent)
    {
        boolean flag;
        if(J)
            flag = true;
        else
            flag = b(motionevent);
        return flag;
    }

    void p()
    {
        int i1 = u;
        n = false;
        o = false;
        p = false;
        if(s > 0)
        {
            if(t > h() || k)
            {
                i1 = h(u);
                t = f(t);
            }
        } else
        if(s < 0 && (t < i() || k))
        {
            i1 = i(u);
            t = g(t);
        }
        if(i1 == u) goto _L2; else goto _L1
_L1:
        u = i1;
_L4:
        if((x[u].c() != 0 || s != 0) && y != null)
            e(u);
        s = 0;
_L5:
        return;
_L2:
        if(B == null) goto _L4; else goto _L3
_L3:
        if(t != h() || s <= 0)
            continue; /* Loop/switch isn't completed */
        B.a();
        s = 0;
          goto _L5
        if(t != i() || s >= 0) goto _L4; else goto _L6
_L6:
        B.b();
        s = 0;
          goto _L5
    }

    void q()
    {
        float f1 = (float)l * j;
        int i1 = l - (int)f1;
        int j1 = x[u].c();
        if(j1 <= i1 * -1)
            s = 1;
        if(j1 >= i1)
            s = -1;
    }

    private static final int M = 16;
    private static final String b = "cls_FlingGallery";
    private static final boolean c = false;
    private static final int g = 100;
    private cls_ap A;
    private cls_i B;
    private cls_r C;
    private cls_ao D;
    private cls_ca E;
    private cls_bs F;
    private cls_s G;
    private boolean H;
    private int I;
    private boolean J;
    private LinkedList K;
    private Scroller L;
    private int N;
    private int O;
    private Handler P;
    private float Q;
    private float R;
    private boolean S;
    private boolean T;
    private boolean U;
    private boolean V;
    private boolean W;
    private int X;
    private int Y;
    private int Z;
    protected GestureDetector a;
    private int aa;
    private Runnable ab;
    private int d;
    private int e;
    private int f;
    private int h;
    private int i;
    private float j;
    private boolean k;
    private int l;
    private int m;
    private boolean n;
    private boolean o;
    private boolean p;
    private float q;
    private long r;
    private int s;
    private int t;
    private int u;
    private Context v;
    private Adapter w;
    private com.nnee.p_pr.p_activity.p_view.cls_a x[];
    private bl y;
    private Interpolator z;
}

