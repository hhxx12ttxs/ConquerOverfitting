// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.nnee.p_pr.p_activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.nnee.p_b.p_a.cls_d;
import com.nnee.p_fk.cls_ActivityEx;
import com.nnee.p_fk.cls_k;
import com.nnee.p_h.cls_b;
import com.nnee.p_i.cls_c;
import com.nnee.p_pr.p_activity.p_view.cls_FlingGallery;
import com.nnee.p_pr.p_activity.p_view.cls_RelativeLayoutEx;
import com.nnee.p_pr.p_activity.p_view.cls_TouchInterceptor;
import com.nnee.p_pr.p_activity.p_view.cls_bw;
import com.nnee.p_pr.p_activity.p_view.cls_cb;
import com.nnee.p_pr.p_activity.p_view.cls_pageList;
import com.nnee.p_pr.p_app.cls_PrisApp;
import com.nnee.p_pr.p_atom.cls_g;
import com.nnee.p_pr.p_atom.cls_u;
import com.nnee.p_pr.p_b.cls_a;
import com.nnee.p_pr.p_c.cls_h;
//import com.nnee.p_pr.p_d.cls_c;
import com.nnee.p_pr.p_heartbeat.cls_AndroidHeartBeatService;
import com.nnee.p_pr.cls_v;
import com.nnee.p_up.cls_CheckVersionService;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

// Referenced classes of package com.nnee.p_pr.p_activity:
//            bc, n, dj, o, 
//            s, ak, ac, ab, 
//            aa, z, y, x, 
//            PRISActivitySetting, MallListActivity, PRISActivityWBSetting, SubsSourceActivity, 
//            q, fw, SubsInfoActivity, f, 
//            u, v, w, ar, 
//            aq, an, am, ap, 
//            ao, aj, ai, al, 
//            fh, cm, gf, el, 
//            bv, ed, m, p, 
//            FavoriteListActivity, LoginActivity, PRISActivityUserInfo, be, 
//            ek, cr

public class cls_MainGridActivity extends cls_ActivityEx
{

    public cls_MainGridActivity()
    {
        K = false;
        L = 0;
        M = 0;
        N = 0;
        O = false;
        P = false;
        Q = 0;
        R = null;
        S = null;
        U = null;
        X = 0;
        Y = 0;
        Z = 0;
        aa = 0;
        ai = 0;
        aw = -1F;
        ax = -1F;
        aB = 0;
        aC = false;
        z = false;
        aD = com.nnee.p_pr.p_activity.cls_bc.aGRID_VIEW;
        aH = new cls_n(this);
        aJ = false;
        aK = new cls_dj(this, null);
        aL = false;
        A = new cls_o(this);
        B = new cls_s(this);
        C = new cls_ak(this);
        D = new cls_ac(this);
        E = new cls_ab(this);
        F = new cls_aa(this);
        aM = new cls_z(this);
        aN = new cls_y(this);
        aO = new cls_x(this);
    }

    static cls_bc A(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aD;
    }

    private void A()
    {
        z();
        if(at != null)
            at.a(true);
    }

    private float B()
    {
        return getResources().getDisplayMetrics().density;
    }

    static void B(cls_MainGridActivity parammaingridactivity)
    {
    	parammaingridactivity.x();
    }

    static void C(cls_MainGridActivity parammaingridactivity)
    {
    	parammaingridactivity.e();
    }

    static void D(cls_MainGridActivity parammaingridactivity)
    {
    	parammaingridactivity.r();
    }

    static TextView E(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.T;
    }

    static void F(cls_MainGridActivity parammaingridactivity)
    {
    	parammaingridactivity.f();
    }

    static void G(cls_MainGridActivity parammaingridactivity)
    {
    	parammaingridactivity.h();
    }

    static View H(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.as;
    }

    static Animation I(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ay;
    }

    static cls_cb J(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aO;
    }

    static boolean K(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aC;
    }

    static Bitmap L(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aF;
    }

    static void M(cls_MainGridActivity parammaingridactivity)
    {
    	parammaingridactivity.g();
    }

    static Bitmap a(cls_MainGridActivity parammaingridactivity, Bitmap bitmap)
    {
    	parammaingridactivity.aF = bitmap;
        return bitmap;
    }

    static Animation a(cls_MainGridActivity parammaingridactivity, Animation animation)
    {
    	parammaingridactivity.ay = animation;
        return animation;
    }

    static ImageView a(cls_MainGridActivity parammaingridactivity, ImageView imageview)
    {
    	parammaingridactivity.aG = imageview;
        return imageview;
    }

    static com.nnee.p_pr.p_activity.p_a.cls_d a(cls_MainGridActivity parammaingridactivity, com.nnee.p_pr.p_activity.p_a.cls_d d1)
    {
    	parammaingridactivity.af = d1;
        return d1;
    }

    static cls_cr a(cls_MainGridActivity parammaingridactivity, cls_cr cr)
    {
    	parammaingridactivity.av = cr;
        return cr;
    }

    private  cls_u a(int i1)
    {
        cls_u u1 = null;
        if(ab != null && i1 >= 0 && i1 < ab.size())
            u1 = (cls_u)ab.get(i1);
        return u1;
    }

    static  cls_u a(cls_MainGridActivity parammaingridactivity, int i1)
    {
        return parammaingridactivity.a(i1);
    }

    static cls_u a(cls_MainGridActivity parammaingridactivity, LinkedList linkedlist, String s1)
    {
        return parammaingridactivity.a(linkedlist, s1);
    }

    private cls_u a(LinkedList linkedlist, String s1)
    {
    	cls_u u1 = null;
        if(linkedlist != null && !TextUtils.isEmpty(s1)) {
        	for(Iterator iterator = linkedlist.iterator(); iterator.hasNext();)
            {
                u1 = (cls_u)iterator.next();
                if(u1 != null && !TextUtils.isEmpty(u1.V()) && u1.V().equals(s1))
                    {
                	return u1;
                    }
            }

            u1 = null;
        }
 
        return u1;
 
    }

    private String a(long l1)
    {
        String s1;
        if(l1 <= 0L)
        {
            s1 = "";
        } else
        {
            long l2 = (System.currentTimeMillis() - l1) / 1000L;
            if(l2 < 60L)
            {
                Object aobj3[] = new Object[2];
                aobj3[0] = Long.valueOf(l2);
                aobj3[1] = getString(0x7f0a0208);
                s1 = getString(0x7f0a0207, aobj3);
            } else
            if(l2 < 3600L)
            {
                Object aobj2[] = new Object[2];
                aobj2[0] = Long.valueOf(l2 / 60L);
                aobj2[1] = getString(0x7f0a0209);
                s1 = getString(0x7f0a0207, aobj2);
            } else
            if(l2 < 0x15180L)
            {
                Object aobj1[] = new Object[2];
                aobj1[0] = Long.valueOf(l2 / 3600L);
                aobj1[1] = getString(0x7f0a020a);
                s1 = getString(0x7f0a0207, aobj1);
            } else
            {
                Object aobj[] = new Object[2];
                aobj[0] = Long.valueOf(l2 / 0x15180L);
                aobj[1] = getString(0x7f0a020b);
                s1 = getString(0x7f0a0207, aobj);
            }
        }
        return s1;
    }

    private HashMap a(int i1, int j1)
    {
    	HashMap hashmap = null;
        if(i1 != j1 && j1 < -1 + ab.size()) {
        	 HashMap hashmap1;
             cls_u u1;
             int k1;
             String s1;
             hashmap1 = new HashMap();
             u1 = (cls_u)ab.get(j1);
             k1 = u1.h();
             s1 = u1.V();
             if(i1 >= j1)
             {
               int l1 = j1 + 1;
               cls_u u2;
               cls_u u3;
               for(u2 = u1; l1 <= i1; u2 = u3)
               {
                   u3 = (cls_u)ab.get(l1);
                   hashmap1.put(s1, Integer.valueOf(u3.h()));
                   u2.b(u3.h());
                   s1 = u3.V();
                   l1++;
               }
       
               u2.b(k1);
               hashmap1.put(s1, Integer.valueOf(k1));
               ab.remove(i1);
               ab.add(j1, u2);
               
               hashmap = hashmap1;
             }
             else
             {
             int i2 = j1 + -1;
             cls_u u4;
             cls_u u5;
             for(u4 = u1; i2 >= i1; u4 = u5)
             {
                 u5 = (cls_u)ab.get(i2);
                 hashmap1.put(s1, Integer.valueOf(u5.h()));
                 u4.b(u5.h());
                 s1 = u5.V();
                 i2--;
             }

             u4.b(k1);
             hashmap1.put(s1, Integer.valueOf(k1));
             ab.add(j1 + 1, u4);
             ab.remove(i1);
             
             hashmap = hashmap1;
             }
        }
 
        return hashmap;
//_L2:
//        HashMap hashmap1;
//        cls_u u1;
//        int k1;
//        String s1;
//        hashmap1 = new HashMap();
//        u1 = (cls_u)ab.get(j1);
//        k1 = u1.h();
//        s1 = u1.V();
//        if(i1 >= j1)
//            break; /* Loop/switch isn't completed */
//        int i2 = j1 + -1;
//        u u4;
//        u u5;
//        for(u4 = u1; i2 >= i1; u4 = u5)
//        {
//            u5 = (u)ab.get(i2);
//            hashmap1.put(s1, Integer.valueOf(u5.h()));
//            u4.b(u5.h());
//            s1 = u5.V();
//            i2--;
//        }
//
//        u4.b(k1);
//        hashmap1.put(s1, Integer.valueOf(k1));
//        ab.add(j1 + 1, u4);
//        ab.remove(i1);
//_L5:
//        hashmap = hashmap1;
//        if(true) goto _L4; else goto _L3
//_L3:
//        int l1 = j1 + 1;
//        u u2;
//        u u3;
//        for(u2 = u1; l1 <= i1; u2 = u3)
//        {
//            u3 = (u)ab.get(l1);
//            hashmap1.put(s1, Integer.valueOf(u3.h()));
//            u2.b(u3.h());
//            s1 = u3.V();
//            l1++;
//        }
//
//        u2.b(k1);
//        hashmap1.put(s1, Integer.valueOf(k1));
//        ab.remove(i1);
//        ab.add(j1, u2);
//          goto _L5
//        if(true) goto _L4; else goto _L6
//_L6:
    }

    static HashMap a(cls_MainGridActivity parammaingridactivity, int i1, int j1)
    {
        return parammaingridactivity.a(i1, j1);
    }

    private void a(int i1, Object obj, String s1, String s2)
    {
        switch(i1){
        	case 1:
        		 L = com.nnee.p_pr.cls_v.a().a(s1, s2, 0);
        		break;
        	case 3:
        		L = com.nnee.p_pr.cls_v.a().i();
        		break;
        	case 5:
        		L = com.nnee.p_pr.cls_v.a().m();
        		break;
        	case 300:
        		 boolean flag;
                if(obj != null)
                    flag = ((Boolean)obj).booleanValue();
                else
                    flag = false;
                if(flag)
                {
                    L = com.nnee.p_pr.cls_v.a().h();
                } else
                {
                    String s3 = com.nnee.p_b.p_a.cls_d.h().c();
                    if(M == 1 && com.nnee.p_pr.p_c.cls_h.e(R, s3))
                    {
                        com.nnee.p_pr.p_c.cls_h.a(R, s3, false);
                        L = com.nnee.p_pr.cls_v.a().a(true);
                    } else
                    {
                        L = com.nnee.p_pr.cls_v.a().a(false);
                    }
                }
        		break;
        	case 301:
        		 L = com.nnee.p_pr.cls_v.a().g();
        		break;
        	case 303:
        		L = com.nnee.p_pr.cls_v.a().b((cls_u)obj);
        		break;
        	case 308:
        		L = com.nnee.p_pr.cls_v.a().a(ab);
        		break;
        	case 313:
        		L = com.nnee.p_pr.cls_v.a().a((LinkedList)obj, c(), b());
        		break;
        		default:
        			return;
        }
        ac.add(new Integer(L));
       
    }

    public static void a(Context context, int i1, int j1)
    {
        a(context, i1, false, j1);
    }

    public static void a(Context context, int i1, boolean flag, int j1)
    {
        Intent intent = new Intent(context, com.nnee.p_pr.p_activity.cls_MainGridActivity.class);
        intent.addFlags(0x4000000);
        intent.putExtra("state", i1);
        intent.putExtra("change_user", flag);
        intent.putExtra("extra_is_refresh_screen", j1);
        context.startActivity(intent);
    }

    private void a(Intent intent)
    {
        boolean flag;
        String s1;
        boolean flag1;
        if(intent != null)
        {
            M = intent.getIntExtra("state", 0);
            flag = intent.getBooleanExtra("change_user", false);
            K = flag;
        } else
        {
            flag = false;
        }
        s1 = com.nnee.p_pr.p_b.cls_a.c();
        if(s1 != null && !s1.equals(com.nnee.p_b.p_a.cls_d.h().c()))
            com.nnee.p_b.p_a.cls_d.h().a(s1, com.nnee.p_pr.p_b.cls_a.g());
        if(flag)
            flag1 = false;
        else
        if(!com.nnee.p_pr.p_activity.cls_PRISActivitySetting.e(R))
            flag1 = true;
        else
            flag1 = false;
        switch(M)
        {
        case 0:
        	if(com.nnee.p_pr.p_b.cls_a.n())
            {
                com.nnee.p_pr.p_b.cls_a.d(false);
                a(3, null, ((String) (null)), ((String) (null)));
                com.nnee.p_pr.cls_v.a().a(cls_b.aEInstall);
                com.nnee.p_pr.p_d.cls_c.d("");
            } else
            {
                a(300, Boolean.valueOf(flag1), ((String) (null)), ((String) (null)));
                y();
            }
        	break;
        case 1:
        	a(300, null, ((String) (null)), ((String) (null)));
            y();
            if(at != null)
                at.a(false);
        	break;
        case 7:
        	M = 1;
            a(300, Boolean.valueOf(flag1), ((String) (null)), ((String) (null)));
        	break;
        default:
        	break;
        }
        
      
    }

    static void a(cls_MainGridActivity parammaingridactivity, int i1, Object obj, String s1, String s2)
    {
        parammaingridactivity.a(i1, obj, s1, s2);
    }

    static void a(cls_MainGridActivity parammaingridactivity, com.nnee.p_pr.p_activity.cls_bc bc1)
    {
        parammaingridactivity.a(bc1);
    }

    static void a(cls_MainGridActivity parammaingridactivity, com.nnee.p_pr.p_atom.cls_b b1)
    {
        parammaingridactivity.a(b1);
    }

    static void a(cls_MainGridActivity parammaingridactivity, cls_u u1, boolean flag)
    {
        parammaingridactivity.a(u1, flag);
    }

    static void a(cls_MainGridActivity parammaingridactivity, com.nnee.p_pr.p_prot.cls_d d1)
    {
        parammaingridactivity.a(d1);
    }

    static void a(cls_MainGridActivity parammaingridactivity, LinkedList linkedlist)
    {
        parammaingridactivity.a(linkedlist);
    }

    private void a(com.nnee.p_pr.p_activity.cls_bc bc1)
    {
        if(com.nnee.p_pr.p_activity.cls_bc.aGRID_VIEW == bc1)
        {
            b(true);
            com.nnee.p_pr.p_d.cls_c.a(p(), 545);
        } else
        {
            b(false);
            com.nnee.p_pr.p_d.cls_c.a(p(), 546);
        }
    }

    private void a(com.nnee.p_pr.p_atom.cls_b b1)
    {
        if(ab != null)
        {
            LinkedList linkedlist = new LinkedList();
            Iterator iterator = ab.iterator();
            do
            {
                if(!iterator.hasNext())
                    break;
                cls_u u1 = (cls_u)iterator.next();
                if(u1 != null && u1.be() == b1)
                    linkedlist.add(u1);
            } while(true);
            if(linkedlist.size() > 0)
                com.nnee.p_pr.cls_v.a().a(new com.nnee.p_pr.p_prot.cls_d(linkedlist));
        }
    }

    private void a(cls_u u1, boolean flag)
    {
        if(u1 != null) { if(u1.V().equals("-1"))
        {
            com.nnee.p_pr.p_activity.cls_MallListActivity.a(R);
            com.nnee.p_pr.p_d.cls_c.a(p(), 1560);
        } else
        if(!com.nnee.p_pr.p_activity.cls_PRISActivityWBSetting.a(this, u1.be()))
            if(a(u1))
            {
                Toast.makeText(R, 0x7f0a006d, 0).show();
            } else
            {
                String s1 = u1.b(cls_g.bEAlernate);
                if(s1 == null)
                {
                    com.nnee.p_pr.p_atom.cls_b b1 = u1.be();
                    if(u1.x())
                    {
                        u1.e(false);
                        com.nnee.p_pr.cls_v.a().c(u1);
                    } else
                    if(b1 == com.nnee.p_pr.p_atom.cls_b.cNetEaseMblog || b1 == com.nnee.p_pr.p_atom.cls_b.bSinaMblog)
                    {
                        com.nnee.p_pr.cls_v.a().c(u1);
                        com.nnee.p_pr.p_a.cls_h.a(this, 0x7f0a003b);
                    }
                } else
                {
                    com.nnee.p_pr.p_activity.cls_SubsSourceActivity.a(R, s1, u1);
                    if(flag)
                        com.nnee.p_pr.p_d.cls_c.a(p(), 1567, u1.U());
                }
            }
        }

    }

    private void a(com.nnee.p_pr.p_prot.cls_d d1)
    {
        if(d1 != null)
        {
            cls_u u1 = new cls_u();
            u1.q("-1");
            if(!d1.h.contains(u1))
                d1.h.addLast(u1);
            ab.clear();
            ab.addAll(d1.h);
            z = true;
            a(((LinkedList) (null)));
        }
    }

    /**
     * @deprecated Method a is deprecated
     */

    private void a(LinkedList linkedlist)
    {
        int i1 = 0;
       
        if(aD != com.nnee.p_pr.p_activity.cls_bc.aGRID_VIEW)
            return;
        if(linkedlist == null) {
        	if(!aC) {
        		F.postDelayed(new cls_q(this), i1);
                z = false;
                 
                
        	}else {
        		  if(z) {
        			  b(getString(0x7f0a01f1));
        		        i1 = 200;
        		        
        		        F.postDelayed(new cls_q(this), i1);
        		        z = false;
        		           
        		  } else {
        			  aC = false;
        		  }
        	}
        } else {
        	for(int j1 = i1; j1 < linkedlist.size(); j1++)
            {
                cls_u u1 = (cls_u)linkedlist.get(j1);
                ae.a(u1.V());
            }

              
        }
 
    }

    private void a(boolean flag, cls_u u1, int i1, View view)
    {
        com.nnee.p_pr.p_c.cls_c.a(this, com.nnee.p_b.p_a.cls_d.h().c(), u1.V(), flag);
        u1.b(flag);
        if(view instanceof cls_RelativeLayoutEx)
        {
            ((cls_RelativeLayoutEx)view).a(flag);
            view.invalidate();
        }
    }

    static boolean a(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aI;
    }

    static boolean a(cls_MainGridActivity parammaingridactivity, cls_u u1)
    {
        return parammaingridactivity.a(u1);
    }

    static boolean a(cls_MainGridActivity parammaingridactivity, cls_u u1, int i1, int j1, View view)
    {
        return parammaingridactivity.a(u1, i1, j1, view);
    }

    static boolean a(cls_MainGridActivity parammaingridactivity, boolean flag)
    {
        parammaingridactivity.aL = flag;
        return flag;
    }

    private boolean a(cls_u u1)
    {
        boolean flag = true;
        if(cls_PRISActivitySetting.i(R))
            switch(u1.av())
            {
          

            case 512: 
            case 2048: 
                break;
            default:
                flag = false;
                break;
            }
        else
            flag = false;
        return flag;
    }

    private boolean a(cls_u u1, int i1, int j1, View view)
    {
        if(u1 != null) {
        	if(!u1.V().equals("-1"))
        	{
        		switch(i1)
                {
                case 256: 
                    a(u1, false);
                    com.nnee.p_pr.p_d.cls_c.a(p(), 1561, u1.U());
                    break;

                case 257: 
                    com.nnee.p_pr.p_activity.cls_SubsInfoActivity.a(R, u1);
                    com.nnee.p_pr.p_d.cls_c.a(p(), 1562, u1.U());
                    break;

                case 258: 
                    com.nnee.p_pr.p_activity.cls_SubsInfoActivity.a(R, u1, true);
                    com.nnee.p_pr.p_d.cls_c.a(p(), 1563, u1.U());
                    break;

                case 259: 
                    if(D != null)
                        D.a(j1);
                    com.nnee.p_pr.p_d.cls_c.a(p(), 1564, u1.U());
                    break;

                case 260: 
                    a(false, u1, j1, view);
                    com.nnee.p_pr.p_d.cls_c.a(p(), 1566, u1.U());
                    break;

                case 261: 
                    a(true, u1, j1, view);
                    com.nnee.p_pr.p_d.cls_c.a(p(), 1565, u1.U());
                    break;
                }
        	}
        	else
        	{
            com.nnee.p_pr.p_activity.cls_MallListActivity.a(R);
        	}
            
        }
 
        return true;
 
    }

    static int b(cls_MainGridActivity parammaingridactivity, int i1)
    {
    	parammaingridactivity.aa = i1;
        return i1;
    }

    static cls_dj b(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aK;
    }

    private void b(int i1)
    {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(R);
        switch(i1)
        {
        case 3:
        	if(cls_PrisApp.a().y())
            {
                android.app.AlertDialog.Builder builder1 = new android.app.AlertDialog.Builder(this);
                View view = getLayoutInflater().inflate(0x7f030028, null);
                ((CheckBox)view.findViewById(0x7f0d0080)).setOnCheckedChangeListener(new cls_ar(this));
                builder1.setView(view);
                builder1.setIcon(0x7f020087);
                builder1.setTitle(0x7f0a005d);
                O = true;
                builder1.setPositiveButton(0x7f0a0052, new cls_aq(this));
                builder1.setNegativeButton(0x7f0a0216, new cls_an(this));
                builder = builder1;
            } else
            {
                builder = new android.app.AlertDialog.Builder(this);
                builder.setIcon(0x7f02008b);
                builder.setTitle(0x7f0a005d);
                builder.setMessage(0x7f0a005c);
                builder.setPositiveButton(0x7f0a0029, new cls_am(this));
                builder.setNegativeButton(0x7f0a0216, new cls_ap(this));
            }
        	break;
        case 4:
        	builder.setIcon(0x7f02002c);
            builder.setTitle(0x7f0a0061);
            builder.setMessage(0x7f0a0062);
            builder.setPositiveButton(0x7f0a0029, new com.nnee.p_pr.p_activity.cls_u(this));
        	break;
        case 5:
        	builder.setIcon(0x1080027);
            builder.setTitle(0x7f0a0063);
            builder.setMessage(0x7f0a0064);
            com.nnee.p_pr.p_activity.cls_v v1 = new com.nnee.p_pr.p_activity.cls_v(this);
            builder.setPositiveButton(0x7f0a0067, v1);
            builder.setNegativeButton(0x7f0a0216, v1);
        	break;
        case 6:
        	builder.setIcon(0x108009b);
            builder.setTitle(0x7f0a0065);
            builder.setMessage(0x7f0a0066);
            cls_w w1 = new cls_w(this);
            builder.setPositiveButton(0x7f0a0029, w1);
            builder.setNegativeButton(0x7f0a0068, w1);
        	break;
        case 7:
        	builder.setIcon(0x7f020087);
            builder.setTitle(0x7f0a006e);
            builder.setMessage(0x7f0a006f);
            builder.setPositiveButton(0x7f0a0070, new cls_ao(this));
            builder.setNegativeButton(0x7f0a0071, new cls_aj(this));
        	break;
        case 8:
        	builder.setIcon(0x7f020087);
            builder.setTitle(0x7f0a0072);
            builder.setMessage(0x7f0a0073);
            builder.setPositiveButton(0x7f0a0070, new cls_ai(this));
            builder.setNegativeButton(0x7f0a0071, new cls_al(this));
        	break;
        default:
        	break;
        }
        builder.show();
     
    }

    private void b(LinkedList linkedlist)
    {
        if(linkedlist != null)
        {
            Iterator iterator = linkedlist.iterator();
            while(iterator.hasNext()) 
            {
                cls_u _tmp = (cls_u)iterator.next();
            }
        }
    }

    private void b(boolean flag)
    {
        Object obj;
        if(flag)
            obj = as;
        else
            obj = at;
        if((double)aw < -0.01D)
        {
            aw = (float)as.getWidth() / 2F;
            ax = (float)as.getHeight() / 2F;
        }
        if(az == null)
        {
            az = AnimationUtils.loadAnimation(this, 0x7f04000c);
            az.setDuration(500L);
            az.setFillAfter(true);
            az.setInterpolator(new AccelerateInterpolator());
            aA = new cls_fh(this, flag);
            az.setAnimationListener(aA);
        } else
        if(aA == null)
        {
            aA = new cls_fh(this, flag);
            az.setAnimationListener(aA);
        } else
        {
            aA.a(flag);
        }
        ((View) (obj)).startAnimation(az);
    }

    static boolean b(cls_MainGridActivity parammaingridactivity, boolean flag)
    {
        parammaingridactivity.aC = flag;
        return flag;
    }

    static int c(cls_MainGridActivity parammaingridactivity, int i1)
    {
        parammaingridactivity.Q = i1;
        return i1;
    }

    static LinkedList c(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ab;
    }

    static boolean c(cls_MainGridActivity parammaingridactivity, boolean flag)
    {
        parammaingridactivity.O = flag;
        return flag;
    }

    static int d(cls_MainGridActivity parammaingridactivity, int i1)
    {
        parammaingridactivity.ai = i1;
        return i1;
    }

    static cls_FlingGallery d(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ae;
    }

    static boolean d(cls_MainGridActivity parammaingridactivity, boolean flag)
    {
        parammaingridactivity.aJ = flag;
        return flag;
    }

    static int e(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.N;
    }

    static int e(cls_MainGridActivity parammaingridactivity, int i1)
    {
        parammaingridactivity.N = i1;
        return i1;
    }

    private void e()
    {
        at = (cls_TouchInterceptor)getLayoutInflater().inflate(0x7f03001b, null);
        au = new cls_fw(this, null);
        at.setAdapter(au);
        at.setOnItemClickListener(B);
        at.a(aM);
        at.setOnCreateContextMenuListener(A);
    }

    static cls_fw f(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.au;
    }

    private void f()
    {
        at.setAdapter(null);
        au = null;
        at = null;
        System.gc();
    }

    private void g()
    {
        long l1 = com.nnee.p_pr.p_c.cls_h.a(this, com.nnee.p_b.p_a.cls_d.h().c());
        if(l1 > 0L)
            Toast.makeText(this, a(l1), 1).show();
    }

    static void g(cls_MainGridActivity parammaingridactivity)
    {
        parammaingridactivity.v();
    }

    static int h(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aa;
    }

    private void h()
    {
        if(as == null)
        {
            throw new RuntimeException("mGridView is null! You should not see this");
        } else
        {
            aD = com.nnee.p_pr.p_activity.cls_bc.aGRID_VIEW;
            setContentView(as);
            return;
        }
    }

    static int i(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.Z;
    }

    static cls_cr j(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.av;
    }

    static int k(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ai;
    }

    static int l(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.Q;
    }

    static cls_pageList m(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ag;
    }

    static com.nnee.p_pr.p_activity.p_a.cls_d n(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.af;
    }

    static boolean o(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.O;
    }

    static Context p(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.R;
    }

    static int q(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.aB;
    }

    private void r()
    {
        aD = com.nnee.p_pr.p_activity.cls_bc.bLIST_VIEW;
        if(at == null)
        {
            throw new RuntimeException("mListView is null! You should not see this.");
        } else
        {
            setContentView(at);
            return;
        }
    }

    static void r(cls_MainGridActivity parammaingridactivity)
    {
        parammaingridactivity.z();
    }

    static cls_TouchInterceptor s(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.at;
    }

    private void s()
    {
        boolean flag = false;
        if(com.nnee.p_pr.p_a.cls_e.a()) {
        	if(cls_PrisApp.a().j())
        	{
        		switch(cls_PrisApp.a().t())
                {
               

                case 1: // '\001'
                    if(cls_PRISActivitySetting.f(R) == com.nnee.p_pr.p_activity.cls_cm.bEAny)
                        flag = true;
                    else
                        b(6);
                    break;

                case 3: // '\003'
                    if(cls_PRISActivitySetting.f(R) == com.nnee.p_pr.p_activity.cls_cm.bEAny)
                        flag = true;
                    else
                        b(6);
                    break;
                case 2: // '\002'
                default:
                    flag = true;
                    break;
                }
        	}
        	else
        	{
            b(5);
            return;
        	}
        } else {
        	b(4);
        	
        	
        }
        
        if(flag)
            if(cls_PrisApp.a().y())
                com.nnee.p_pr.p_a.cls_h.a(R, 0x7f0a0200);
            else
            if(com.nnee.p_pr.p_c.cls_c.e(R, com.nnee.p_b.p_a.cls_d.h().c()) > 0)
            {
                com.nnee.p_pr.p_a.cls_h.a(R, 0x7f0a0060);
                cls_PrisApp.a().a(new Handler());
            } else
            {
                b(8);
            }
 
    }

    static cls_ed t(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ar;
    }

    private void t()
    {
        S = (ImageView)findViewById(0x7f0d0008);
        T = (TextView)findViewById(0x7f0d0009);
        U = (ProgressBar)findViewById(0x7f0d000b);
        V = (LinearLayout)findViewById(0x7f0d000a);
        W = (RelativeLayout)findViewById(0x7f0d0001);
    }

    static LinkedList u(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ac;
    }

    private void u()
    {
        a(new android.widget.LinearLayout.LayoutParams(-1, com.nnee.p_img.cls_c.a(R, 48F)));
        a(((android.graphics.drawable.Drawable) (null)));
        b(com.nnee.p_fk.cls_k.a(this).b(0x7f0200b8));
        if(S != null)
        {
            S.setImageDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f02013c));
            S.setVisibility(8);
        }
        if(T != null)
        {
            T.setBackgroundDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f020036));
            T.setVisibility(8);
            android.widget.LinearLayout.LayoutParams layoutparams = (android.widget.LinearLayout.LayoutParams)T.getLayoutParams();
            int j1 = (int)getResources().getDimension(0x7f090004);
            T.setPadding(j1, 0, j1, 0);
            layoutparams.leftMargin = j1;
            T.setLayoutParams(layoutparams);
        }
        V.setBackgroundDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f0200c1));
        V.setVisibility(0);
        U.setVisibility(0);
        int i1 = com.nnee.p_img.cls_c.a(R, 8F);
        LinearLayout linearlayout = (LinearLayout)findViewById(0x7f0d0005);
        android.view.ViewGroup.MarginLayoutParams marginlayoutparams = (android.view.ViewGroup.MarginLayoutParams)linearlayout.getLayoutParams();
        marginlayoutparams.setMargins(0, 0, i1, 0);
        linearlayout.setLayoutParams(marginlayoutparams);
    }

    private void v()
    {
        if(ae != null)
        {
            X = ae.getHeight();
            Y = ae.getWidth();
        }
        if(X <= 0 && Y <= 0)
        {
            Display display = getWindowManager().getDefaultDisplay();
            Y = display.getWidth();
            X = -8 + (-32 + (-32 + (-52 + display.getHeight())));
        }
        Z = 6;
        ah = -2 + (-16 + X) / 3;
    }

    static void v(cls_MainGridActivity parammaingridactivity)
    {
        parammaingridactivity.A();
    }

    static int w(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.Y;
    }

    private void w()
    {
        if(S != null)
            S.setOnClickListener(E);
        if(T != null)
            T.setOnClickListener(E);
    }

    static int x(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.ah;
    }

    private void x()
    {
        b(7);
    }

    static int y(cls_MainGridActivity parammaingridactivity)
    {
        return parammaingridactivity.M;
    }

    private void y()
    {
        F.removeCallbacks(aK);
        U.setVisibility(0);
        V.setVisibility(0);
        S.setVisibility(8);
        T.setVisibility(8);
    }

    private void z()
    {
        U.setVisibility(8);
        V.setVisibility(8);
        S.setVisibility(0);
        T.setVisibility(0);
        if(!aJ)
            if(aI)
                aL = true;
            else
                F.postDelayed(aK, 1500L);
    }

    static void z(cls_MainGridActivity parammaingridactivity)
    {
        parammaingridactivity.y();
    }

    public int b()
    {
        return ah;
    }

    public int c()
    {
        return Y / 2;
    }

    protected boolean e_()
    {
        return true;
    }

    public void f_()
    {
        a(((android.graphics.drawable.Drawable) (null)));
        b(com.nnee.p_fk.cls_k.a(this).b(0x7f0200b8));
        if(S != null)
            S.setImageDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f02013c));
        if(T != null)
            T.setBackgroundDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f020036));
        if(V != null)
            V.setBackgroundDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f0200c1));
        if(ag != null)
            ag.a();
        if(aG != null)
        {
            aG.setImageDrawable(com.nnee.p_fk.cls_k.a(this).b(0x7f020004));
            aG.setBackgroundColor(com.nnee.p_fk.cls_k.a(this).c(0x7f0800f0));
        }
        if(au != null)
            au.notifyDataSetChanged();
    }

    public void onCreate(Bundle bundle)
    {
        super.onCreate(bundle);
        aB = getWindow().getAttributes().windowAnimations;
        getWindow().setWindowAnimations(0x7f0b000f);
        cls_PrisApp.a().o();
        ae = new cls_FlingGallery(this);
        int i1 = com.nnee.p_img.cls_c.a(this, 8F);
        ae.a(i1);
        ae.a(new cls_gf(this));
        ae.a(new cls_el(this));
        ab = new LinkedList();
        ac = new LinkedList();
        ad = new HashMap();
        LinearLayout linearlayout = new LinearLayout(getApplicationContext());
        linearlayout.setOrientation(1);
        android.widget.LinearLayout.LayoutParams layoutparams = new android.widget.LinearLayout.LayoutParams(-1, 0);
        layoutparams.setMargins(i1, i1, i1, 0);
        layoutparams.weight = 1F;
        linearlayout.addView(ae, layoutparams);
        ag = new cls_pageList(this);
        linearlayout.addView(ag, new android.widget.LinearLayout.LayoutParams(-1, 32));
        ag.setGravity(17);
        ag.a(new cls_bv(this));
        as = linearlayout;
        setContentView(linearlayout);
        aD = com.nnee.p_pr.p_activity.cls_bc.aGRID_VIEW;
        R = this;
        Q = 0;
        com.nnee.p_pr.cls_v.a().a(C);
        com.nnee.p_b.p_b.p_e.cls_h.a().a(aN);
        t();
        w();
        u();
        a(getIntent());
        cls_CheckVersionService.a(this, false);
        aq = com.nnee.p_b.p_a.cls_d.f();
        P = cls_PRISActivitySetting.i(this);
        v();
        ar = new cls_ed();
        ar.a(F);
        ar.a(aq);
        ar.a(Y / 2, ah);
        if(!P && com.nnee.p_pr.p_b.cls_a.a() && (cls_PrisApp.a().t() == 1 || cls_PrisApp.a().t() == 3))
        {
            android.os.Message message = F.obtainMessage(2);
            F.sendMessageDelayed(message, 1000L);
        }
        a(5, null, ((String) (null)), ((String) (null)));
    }

    public Dialog onCreateDialog(int i1)
    {
        Object obj = null;
        switch(i1)
        {
        case 2:
        	 android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
             builder.setIcon(0x7f020087);
             builder.setTitle(0x7f0a0057);
             builder.setMessage(0x7f0a0058);
             builder.setPositiveButton(0x7f0a0059, new cls_m(this));
             builder.setNegativeButton(0x7f0a005a, new cls_p(this));
             obj = builder.create();
        	break;
        default:
        	break;
        }
        
        return ((Dialog) (obj));
 
    }

    public void onDestroy()
    {
        super.onDestroy();
        com.nnee.p_pr.cls_v.a().b(C);
        if(C != null)
        {
            C.c();
            C = null;
        }
        com.nnee.p_b.p_b.p_e.cls_h.a().b(aN);
        if(ae != null)
            ae = null;
        if(ab != null)
        {
            ab.clear();
            ab = null;
        }
        if(ac != null)
        {
            ac.clear();
            ac = null;
        }
        if(ad != null)
        {
            ad.clear();
            ad = null;
        }
        if(ar != null)
        {
            ar.a();
            ar = null;
        }
        if(aF != null)
        {
            aF.recycle();
            aF = null;
        }
        cls_AndroidHeartBeatService androidheartbeatservice = cls_AndroidHeartBeatService.a(getApplicationContext());
        if(aH != null && androidheartbeatservice.a(aH))
        {
            androidheartbeatservice.b(aH);
            aH = null;
        }
        com.nnee.p_img.cls_d.a().a(aq);
    }

    public boolean onKeyDown(int i1, KeyEvent keyevent)
    {
        boolean flag = true;
        if(4 == i1)
        {
            if(n())
                o();
            else
                b(3);
        } else
        {
            flag = super.onKeyDown(i1, keyevent);
        }
        return flag;
    }

    protected void onNewIntent(Intent intent)
    {
        super.onNewIntent(intent);
        if(intent.getIntExtra("extra_is_refresh_screen", 0) <= 0)
        {
            setIntent(intent);
            a(intent);
        }
    }

    public boolean onOptionsItemSelected(MenuItem menuitem)
    {
        switch(menuitem.getItemId())
        {
        case 2131558715:
        	com.nnee.p_pr.p_activity.cls_FavoriteListActivity.a(R);
            com.nnee.p_pr.p_d.cls_c.a(p(), 532);
        	break;
        case 2131558724:
        	 com.nnee.p_pr.p_activity.cls_MallListActivity.a(R);
             com.nnee.p_pr.p_d.cls_c.a(p(), 533);
        	break;
        case 2131558725:
        	if(M == 0)
            {
                com.nnee.p_pr.p_activity.cls_LoginActivity.a(this, 0, 0);
                com.nnee.p_pr.p_d.cls_c.a(p(), 529);
            } else
            if(1 == M)
            {
                com.nnee.p_pr.p_activity.cls_PRISActivityUserInfo.a(this, com.nnee.p_b.p_a.cls_d.h().c());
                com.nnee.p_pr.p_d.cls_c.a(p(), 531);
            }
        	break;
        case 2131558726:
        	s();
            com.nnee.p_pr.p_d.cls_c.a(p(), 530);
        	break;
        case 2131558727:
        	com.nnee.p_pr.p_activity.cls_PRISActivitySetting.g(this);
            com.nnee.p_pr.p_d.cls_c.a(p(), 534);
        	break;
        case 2131558728:
        	b(3);
        	break;
        	
        	default:
        		break;
        }
        
        return super.onOptionsItemSelected(menuitem);
    }

    public boolean onPrepareOptionsMenu(Menu menu)
    {
        MenuInflater menuinflater;
        menu.clear();
        menuinflater = getMenuInflater();
        if(M != 0) {
        	if(1 == M)
                menuinflater.inflate(0x7f0c0003, menu);
            else
            if(7 == M)
                menuinflater.inflate(0x7f0c0004, menu);
        } else {
        menuinflater.inflate(0x7f0c0004, menu);
        }
        return super.onPrepareOptionsMenu(menu);

    }

    public void onResume()
    {
        boolean flag;
        flag = com.nnee.p_b.p_a.cls_d.h().i();
        com.nnee.p_img.cls_d.a().e();
        cls_PrisApp.a().o();
        if(flag || M != 0) {
        	if(flag && 1 == M)
            {
                M = 0;
                K = true;
                a(300, null, ((String) (null)), ((String) (null)));
                com.nnee.p_pr.p_activity.cls_ek.a(R);
                Toast.makeText(R, 0x7f0a0002, 1).show();
            }
        } else {
        	M = 1;
            K = true;
            a(300, null, ((String) (null)), ((String) (null)));
            y();
        }
        boolean flag1 = cls_PRISActivitySetting.i(this);
        if(P != flag1)
        {
            z = true;
            a(((LinkedList) (null)));
            P = flag1;
        }
        if(cls_PrisApp.a().e())
        {
        	cls_be be1 = new cls_be(this, null);
            com.nnee.p_pr.p_activity.cls_be.a(be1, cls_PrisApp.a().d());
            be1.execute(new Void[0]);
        }
        cls_AndroidHeartBeatService androidheartbeatservice = cls_AndroidHeartBeatService.a(getApplicationContext());
        super.onResume();
        aI = false;
        if(aL)
        {
            aL = false;
            F.postDelayed(aK, 1500L);
        }
        if(!androidheartbeatservice.a(aH))
        {
            com.nnee.p_g.cls_b.e("MainGridActivity", "register heart beat call-back");
            androidheartbeatservice.a(aH, 0x6ddd00L);
        }
        return;
 
    }

    private static final String G = "MainGridActivity";
    private static final String H = "state";
    private static final String I = "change_user";
    private static final boolean J = false;
    public static final int a = 0;
    private static final int aj = 8;
    private static final int ak = 2;
    private static final int al = 3;
    private static final int am = -2;
    private static final int an = 32;
    private static final int ao = 0x6ddd00;
    private static final long ap = 1500L;
    public static final int b = 1;
    public static final int c = 2;
    public static final int d = 3;
    public static final int e = 4;
    public static final int f = 5;
    public static final int g = 6;
    public static final int h = 7;
    public static final int i = 8;
    public static final int j = 2;
    public static final int k = 1;
    public static final int l = 2;
    public static final int m = 3;
    public static final int n = 4;
    public static final int o = 5;
    public static final int p = 6;
    public static final int q = 7;
    public static final int r = 8;
    public static final int s = 256;
    public static final int t = 257;
    public static final int u = 258;
    public static final int v = 259;
    public static final int w = 260;
    public static final int x = 261;
    public static final String y = "extra_is_refresh_screen";
    android.view.View.OnCreateContextMenuListener A;
    android.widget.AdapterView.OnItemClickListener B;
    com.nnee.p_pr.cls_b C;
    cls_f D;
    android.view.View.OnClickListener E;
    Handler F;
    private boolean K;
    private int L;
    private int M;
    private int N;
    private boolean O;
    private boolean P;
    private int Q;
    private Context R;
    private ImageView S;
    private TextView T;
    private ProgressBar U;
    private LinearLayout V;
    private RelativeLayout W;
    private int X;
    private int Y;
    private int Z;
    private cls_fh aA;
    private int aB;
    private boolean aC;
    private cls_bc aD;
    private final int aE = 60;
    private Bitmap aF;
    private ImageView aG;
    private com.nnee.p_pr.p_heartbeat.cls_a aH;
    private boolean aI;
    private boolean aJ;
    private cls_dj aK;
    private boolean aL;
    private cls_bw aM;
    private com.nnee.p_b.p_b.p_e.cls_e aN;
    private cls_cb aO;
    private int aa;
    private LinkedList ab;
    private LinkedList ac;
    private HashMap ad;
    private cls_FlingGallery ae;
    private com.nnee.p_pr.p_activity.p_a.cls_d af;
    private cls_pageList ag;
    private int ah;
    private int ai;
    private int aq;
    private cls_ed ar;
    private View as;
    private cls_TouchInterceptor at;
    private cls_fw au;
    private cls_cr av;
    private float aw;
    private float ax;
    private Animation ay;
    private Animation az;
    protected boolean z;
}

