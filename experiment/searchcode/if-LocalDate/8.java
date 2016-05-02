package com.netease.b.a;

import com.netease.framework.a.k;
import com.netease.framework.a.m;
import com.netease.pris.atom.aa;
import com.netease.pris.atom.ac;
import com.netease.pris.atom.b;
import com.netease.pris.atom.g;
import com.netease.pris.atom.o;
import com.netease.pris.atom.u;
import com.netease.pris.c.h;
import com.netease.pris.c.l;
import com.netease.pris.c.n;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

public class i extends j
{
  static final String a = "PRISTransactionSubscrips";
  private static final int o = 20;
  private static final boolean p = true;
  private static final int q = 5;
  String b;
  String c;
  m d;
  com.netease.pris.protocol.d e;
  u f;
  String g;
  LinkedList i;
  b j;
  boolean k;

  private i(String paramString1, String paramString2, int paramInt)
  {
    super(paramInt);
    this.b = paramString1;
    this.c = paramString2;
  }

  private i(String paramString1, String paramString2, u paramu, int paramInt)
  {
    this(paramString1, paramString2, paramInt);
    this.f = paramu;
    this.e = new com.netease.pris.protocol.d();
    this.e.h.add(paramu);
  }

  private i(String paramString1, String paramString2, com.netease.pris.protocol.d paramd, int paramInt)
  {
    this(paramString1, paramString2, paramInt);
    this.e = paramd;
  }

  private i(String paramString1, String paramString2, com.netease.pris.protocol.d paramd, b paramb, int paramInt)
  {
    this(paramString1, paramString2, paramInt);
    this.e = paramd;
    this.j = paramb;
  }

  private i(String paramString1, String paramString2, String paramString3, int paramInt)
  {
    this(paramString1, paramString2, paramInt);
    this.g = paramString3;
  }

  private m A()
  {
    LinkedList localLinkedList = com.netease.pris.c.c.a(d.h().a(), com.netease.pris.protocol.c.b, true);
    if ((localLinkedList == null) || (localLinkedList.size() == 0))
      c(0, new com.netease.pris.protocol.d());
    for (k localk = null; ; localk = com.netease.pris.protocol.c.a(new com.netease.pris.protocol.d(localLinkedList), null, this.b, this.c))
    {
      return localk;
      Iterator localIterator = localLinkedList.iterator();
      while (localIterator.hasNext())
        ((u)localIterator.next()).b(-1);
    }
  }

  private m B()
  {
    return com.netease.pris.protocol.c.c(this.b, this.c);
  }

  private m C()
  {
    h.b(d.h().a(), this.b, System.currentTimeMillis());
    return com.netease.pris.protocol.c.c(this.e, this.b, this.c);
  }

  private m D()
  {
    k localk = null;
    this.j = this.f.be();
    ac localac;
    if (a(this.j))
    {
      localac = b(this.j);
      if (localac != null)
        break label52;
      d(501, null);
      h();
    }
    while (true)
    {
      return localk;
      localac = null;
      label52: if (this.j == b.a)
      {
        com.netease.pris.protocol.d locald = new com.netease.pris.protocol.d();
        locald.h.add(this.f);
        localk = com.netease.pris.protocol.c.a(locald, localac, this.b, this.c);
        continue;
      }
      localk = com.netease.pris.protocol.c.a(this.f, localac, this.b, this.c);
    }
  }

  private void E()
  {
    switch (n())
    {
    default:
    case 311:
    }
    while (true)
    {
      return;
      if (this.e != null)
      {
        Iterator localIterator = this.e.h.iterator();
        while (localIterator.hasNext())
          ((u)localIterator.next()).e(true);
        continue;
      }
      if (this.f == null)
        continue;
      this.f.e(true);
    }
  }

  private m F()
  {
    if (d.h().i())
    {
      LinkedList localLinkedList = this.e.h;
      this.e.a(true);
      Iterator localIterator = localLinkedList.iterator();
      while (localIterator.hasNext())
      {
        u localu = (u)localIterator.next();
        localu.a(com.netease.pris.c.c.b(d.h().a(), this.b, localu));
        localu.a(aa.b);
        String str = localu.V();
        o localo = new o();
        localo.h(str);
        this.e.j.add(localo);
      }
      com.netease.pris.c.c.b(d.h().a(), this.b, localLinkedList);
      n.b(d.h().a(), this.b, localLinkedList);
      c(n(), this.e);
    }
    for (k localk = null; ; localk = com.netease.pris.protocol.c.b(this.e, null, this.b, this.c))
      return localk;
  }

  public static i a(String paramString1, String paramString2)
  {
    return new i(paramString1, paramString2, 2);
  }

  public static i a(String paramString1, String paramString2, u paramu)
  {
    return new i(paramString1, paramString2, paramu, 309);
  }

  public static i a(String paramString1, String paramString2, com.netease.pris.protocol.d paramd)
  {
    return new i(paramString1, paramString2, paramd, 302);
  }

  private static i a(String paramString1, String paramString2, com.netease.pris.protocol.d paramd, b paramb)
  {
    return new i(paramString1, paramString2, paramd, paramb, 310);
  }

  public static i a(String paramString1, String paramString2, String paramString3, b paramb)
  {
    i locali = new i(paramString1, paramString2, paramString3, 304);
    locali.j = paramb;
    return locali;
  }

  public static i a(String paramString1, String paramString2, LinkedList paramLinkedList)
  {
    if (paramLinkedList != null);
    for (com.netease.pris.protocol.d locald = new com.netease.pris.protocol.d(paramLinkedList); ; locald = new com.netease.pris.protocol.d())
      return new i(paramString1, paramString2, locald, 308);
  }

  public static i a(String paramString1, String paramString2, boolean paramBoolean)
  {
    i locali = new i(paramString1, paramString2, 300);
    locali.k = paramBoolean;
    return locali;
  }

  private m a(ac paramac)
  {
    return com.netease.pris.protocol.c.c(this.e, paramac, this.b, this.c);
  }

  private void a()
  {
    boolean bool1 = false;
    if ((this.i != null) && (this.i.size() > 0))
    {
      int m;
      boolean bool2;
      label88: LinkedList localLinkedList1;
      switch (n())
      {
      default:
        m = com.netease.pris.c.c.a(d.h().a(), this.b);
        bool2 = bool1;
        localLinkedList1 = this.i;
        if (this.e == null)
          break;
      case 305:
      case 311:
      case 300:
      case 307:
      case 308:
      }
      for (LinkedList localLinkedList2 = this.e.h; ; localLinkedList2 = null)
      {
        a(localLinkedList1, localLinkedList2, m, true, bool2);
        com.netease.pris.c.c.d(d.h().a(), this.b, this.i);
        Iterator localIterator = this.i.iterator();
        while (localIterator.hasNext())
          ((u)localIterator.next()).c();
        bool2 = true;
        m = 0;
        break label88;
        bool1 = true;
        break;
      }
      this.i.clear();
    }
  }

  private void a(int paramInt, com.netease.pris.protocol.d paramd)
  {
    if (f())
      c(0, paramd);
    while (true)
    {
      return;
      c(-1000, paramd);
    }
  }

  private void a(int paramInt, LinkedList paramLinkedList, b paramb)
  {
    int m = paramLinkedList.size();
    int n = 0;
    Object localObject = null;
    if (n < m)
    {
      LinkedList localLinkedList = new LinkedList();
      int i1;
      if (n + 20 > m)
      {
        i1 = m;
        label42: localLinkedList.addAll(paramLinkedList.subList(n, i1));
        switch (paramInt)
        {
        default:
        case 310:
        case 311:
        }
      }
      while (true)
      {
        a((com.netease.framework.b.a)localObject);
        n += 20;
        break;
        i1 = n + 20;
        break label42;
        localObject = a(this.b, this.c, new com.netease.pris.protocol.d(localLinkedList), paramb);
        continue;
        localObject = b(this.b, this.c, new com.netease.pris.protocol.d(localLinkedList), paramb);
      }
    }
  }

  private void a(LinkedList paramLinkedList1, LinkedList paramLinkedList2, int paramInt, boolean paramBoolean1, boolean paramBoolean2)
  {
    if (paramLinkedList1 == null);
    while (true)
    {
      return;
      if ((paramLinkedList2 != null) && (paramLinkedList2.size() != 0))
        break;
      Iterator localIterator1 = paramLinkedList1.iterator();
      while (localIterator1.hasNext())
      {
        u localu1 = (u)localIterator1.next();
        paramInt++;
        localu1.b(paramInt);
        if (!paramBoolean1)
          continue;
        localu1.d(true);
      }
    }
    HashMap localHashMap = new HashMap();
    Iterator localIterator2 = paramLinkedList2.iterator();
    while (localIterator2.hasNext())
    {
      u localu4 = (u)localIterator2.next();
      localHashMap.put(localu4.V(), localu4);
    }
    Iterator localIterator3 = paramLinkedList1.iterator();
    label275: label409: 
    while (true)
    {
      label126: u localu2;
      u localu3;
      if (localIterator3.hasNext())
      {
        localu2 = (u)localIterator3.next();
        localu3 = (u)localHashMap.get(localu2.V());
        if (localu3 != null)
        {
          if (localu3.h() <= 0)
            break label325;
          localu2.b(localu3.h());
          label186: String str1 = localu3.b(g.f);
          localu2.i(str1);
          localu2.b(localu3.bk());
          if (paramBoolean1)
          {
            if (!localu3.a(localu2))
              break label369;
            String str2 = localu2.b(g.b);
            if ((str2 == null) || (str2.length() <= 0))
              break label346;
            if (localu3.v())
              break label337;
            localu2.b(localu3.bk());
            localu2.d(false);
          }
          localu3.i(str1);
          if ((paramBoolean2) || (!localu3.S()))
            break label378;
          localu3.h(localu2.d());
        }
      }
      while (true)
      {
        if (localu2.h() > 0)
          break label409;
        paramInt++;
        localu2.b(paramInt);
        break label126;
        break;
        label325: paramInt++;
        localu2.b(paramInt);
        break label186;
        label337: localu2.d(true);
        break label275;
        label346: localu2.b(localu3.bk());
        localu2.d(localu3.v());
        break label275;
        label369: localu2.d(true);
        break label275;
        label378: if (localu3.w())
        {
          localu3.a(localu2);
          localu3.c();
          continue;
        }
        localu3.a(localu2);
      }
    }
  }

  private static boolean a(b paramb)
  {
    if ((paramb == b.b) || (paramb == b.c));
    for (int m = 1; ; m = 0)
      return m;
  }

  public static i b(String paramString1, String paramString2)
  {
    return new i(paramString1, paramString2, 3);
  }

  public static i b(String paramString1, String paramString2, u paramu)
  {
    return new i(paramString1, paramString2, paramu, 303);
  }

  public static i b(String paramString1, String paramString2, com.netease.pris.protocol.d paramd)
  {
    return new i(paramString1, paramString2, paramd, 305);
  }

  private static i b(String paramString1, String paramString2, com.netease.pris.protocol.d paramd, b paramb)
  {
    return new i(paramString1, paramString2, paramd, paramb, 311);
  }

  public static i b(String paramString1, String paramString2, String paramString3, b paramb)
  {
    i locali = new i(paramString1, paramString2, paramString3, 316);
    locali.j = paramb;
    return locali;
  }

  public static i b(String paramString1, String paramString2, LinkedList paramLinkedList)
  {
    com.netease.pris.protocol.d locald = new com.netease.pris.protocol.d();
    if (paramLinkedList != null)
      locald.h = paramLinkedList;
    return new i(paramString1, paramString2, locald, 306);
  }

  private m b(ac paramac)
  {
    return com.netease.pris.protocol.c.a(this.e, paramac, this.b, this.c);
  }

  private ac b(b paramb)
  {
    ac localac = null;
    if (paramb == b.b)
    {
      com.netease.b.b.e.j localj2 = l.b(d.h().a(), this.b, 3);
      if (localj2 != null)
        localac = new ac(localj2);
    }
    while (true)
    {
      return localac;
      if (paramb != b.c)
        continue;
      com.netease.b.b.e.j localj1 = l.b(d.h().a(), this.b, 1);
      if (localj1 == null)
        continue;
      localac = new ac(localj1);
    }
  }

  private void b(int paramInt, com.netease.pris.protocol.d paramd)
  {
    if (f())
      c(0, paramd);
    while (true)
    {
      return;
      c(-1000, paramd);
    }
  }

  public static i c(String paramString1, String paramString2)
  {
    return new i(paramString1, paramString2, 307);
  }

  public static i c(String paramString1, String paramString2, u paramu)
  {
    return new i(paramString1, paramString2, paramu, 305);
  }

  public static i c(String paramString1, String paramString2, String paramString3, b paramb)
  {
    i locali = new i(paramString1, paramString2, paramString3, 312);
    locali.j = paramb;
    return locali;
  }

  private void c(int paramInt, com.netease.pris.protocol.d paramd)
  {
    switch (paramInt)
    {
    default:
    case 602:
    case 305:
    }
    while (true)
    {
      return;
      HashMap localHashMap2 = new HashMap();
      if (this.e != null)
      {
        Iterator localIterator6 = this.e.h.iterator();
        while (localIterator6.hasNext())
        {
          u localu7 = (u)localIterator6.next();
          localHashMap2.put(localu7.V(), localu7);
        }
      }
      LinkedList localLinkedList2 = new LinkedList();
      this.e = new com.netease.pris.protocol.d(localLinkedList2);
      this.e.a(2);
      Date localDate = paramd.l();
      if (localDate != null)
      {
        long l = localDate.getTime();
        if (l > h.g(d.h().a(), this.b))
        {
          h.b(d.h().a(), this.b, l);
          Iterator localIterator5 = paramd.h.iterator();
          int i2 = 0;
          while (localIterator5.hasNext())
          {
            u localu6 = (u)localIterator5.next();
            i2++;
            localu6.b(i2);
          }
          this.e.a(16);
        }
      }
      int m = com.netease.pris.c.c.a(d.h().a(), this.b);
      Iterator localIterator3 = paramd.h.iterator();
      int n = m;
      if (localIterator3.hasNext())
      {
        u localu4 = (u)localIterator3.next();
        localu4.d(false);
        u localu5 = (u)localHashMap2.remove(localu4.V());
        if (localu5 != null)
        {
          if (localu4.h() > 0)
            localu5.b(localu4.h());
          if (localu4.S())
            localu5.e(localu4.T());
          localLinkedList2.add(localu5);
        }
        for (int i1 = n; ; i1 = n)
        {
          n = i1;
          break;
          if (localu4.h() < 0)
          {
            n++;
            localu4.b(n);
          }
          localLinkedList2.add(localu4);
          this.e.a(16);
        }
      }
      localHashMap2.remove("-1");
      if ((!this.e.i()) && (localHashMap2.size() > 0))
        this.e.a(16);
      Collection localCollection = localHashMap2.values();
      Collections.sort(localLinkedList2);
      com.netease.pris.c.c.a(d.h().a(), this.b, localCollection);
      if (localLinkedList2.size() > 0)
      {
        com.netease.pris.c.c.d(d.h().a(), this.b, localLinkedList2);
        Iterator localIterator4 = localLinkedList2.iterator();
        while (localIterator4.hasNext())
          ((u)localIterator4.next()).c();
        a(b(this.b, this.c, this.e));
        continue;
      }
      this.e.a(true);
      c(0, this.e);
      continue;
      if (paramd.h.size() > 0)
      {
        LinkedList localLinkedList1 = new LinkedList();
        HashMap localHashMap1 = new HashMap();
        if (this.e.h != null)
        {
          Iterator localIterator2 = this.e.h.iterator();
          while (localIterator2.hasNext())
          {
            u localu3 = (u)localIterator2.next();
            localHashMap1.put(localu3.V(), localu3);
          }
        }
        Iterator localIterator1 = paramd.h.iterator();
        while (localIterator1.hasNext())
        {
          u localu1 = (u)localIterator1.next();
          u localu2 = (u)localHashMap1.get(localu1.V());
          if (localu2 != null)
          {
            localu2.d(localu1.v());
            String str = localu2.b(g.f);
            localu1.b(localu2.bk());
            localu1.i(str);
          }
          localLinkedList1.add(localu1);
        }
        new com.netease.pris.protocol.d(localLinkedList1);
      }
      if (!f())
        continue;
      f(this.e);
      n.a(d.h().a(), this.b, this.e.h);
      this.e.a(true);
      c(0, this.e);
    }
  }

  public static i d(String paramString1, String paramString2)
  {
    return new i(paramString1, paramString2, 301);
  }

  public static i e(String paramString1, String paramString2)
  {
    return new i(paramString1, paramString2, 315);
  }

  private void e(int paramInt)
  {
    if (this.f != null)
    {
      b localb = this.f.be();
      LinkedList localLinkedList = new LinkedList();
      localLinkedList.add(this.f);
      a(paramInt, localLinkedList, localb);
    }
    while (true)
    {
      return;
      if ((this.e != null) && (this.e.h.size() > 0))
      {
        g(paramInt);
        continue;
      }
      d(0, null);
      h();
    }
  }

  private void e(com.netease.pris.protocol.d paramd)
  {
    LinkedList localLinkedList1 = paramd.h;
    if ((localLinkedList1 != null) && (localLinkedList1.size() > 0))
    {
      LinkedList localLinkedList2 = new LinkedList();
      Iterator localIterator = localLinkedList1.iterator();
      while (localIterator.hasNext())
      {
        u localu = (u)localIterator.next();
        b localb = localu.be();
        if ((localb != b.c) && (localb != b.b) && (localb != b.e) && (localb != b.d))
          continue;
        localLinkedList2.add(localu);
      }
      paramd.h = localLinkedList2;
    }
  }

  private m f(int paramInt)
  {
    m localm = null;
    if (a(this.j));
    for (ac localac = b(this.j); ; localac = null)
    {
      switch (paramInt)
      {
      default:
      case 310:
      case 311:
      }
      while (true)
      {
        return localm;
        localm = b(localac);
        continue;
        if ((a(this.j)) && (localac == null))
        {
          d(501, null);
          h();
          continue;
        }
        localm = a(localac);
      }
    }
  }

  private void f(com.netease.pris.protocol.d paramd)
  {
    if ((paramd.h.size() > 0) && ("-1".equals(((u)paramd.h.getLast()).V())))
    {
      LinkedList localLinkedList = new LinkedList();
      localLinkedList.addAll(paramd.h);
      localLinkedList.removeLast();
      paramd.h = localLinkedList;
    }
  }

  private void g(int paramInt)
  {
    LinkedList localLinkedList1 = new LinkedList();
    LinkedList localLinkedList2 = new LinkedList();
    LinkedList localLinkedList3 = new LinkedList();
    Iterator localIterator = this.e.h.iterator();
    while (localIterator.hasNext())
    {
      u localu = (u)localIterator.next();
      b localb = localu.be();
      if (localb == b.c)
      {
        localLinkedList1.add(localu);
        continue;
      }
      if (localb == b.b)
      {
        localLinkedList2.add(localu);
        continue;
      }
      localLinkedList3.add(localu);
    }
    if (localLinkedList1.size() > 0)
      a(paramInt, localLinkedList1, b.c);
    if (localLinkedList2.size() > 0)
      a(paramInt, localLinkedList2, b.b);
    if (localLinkedList3.size() > 0)
      a(paramInt, localLinkedList3, b.a);
  }

  private com.netease.pris.protocol.d p()
  {
    LinkedList localLinkedList = com.netease.pris.c.c.a(d.h().a(), this.b, true);
    com.netease.pris.protocol.d locald;
    if (localLinkedList != null)
    {
      locald = new com.netease.pris.protocol.d(localLinkedList);
      n.a(d.h().a(), this.b, localLinkedList);
    }
    while (true)
    {
      locald.a(1);
      return locald;
      locald = new com.netease.pris.protocol.d();
    }
  }

  private void q()
  {
    com.netease.pris.protocol.d locald = p();
    if (locald.h.size() == 0)
    {
      if (d.h().i())
        locald.a(true);
      c(0, locald);
      this.e = new com.netease.pris.protocol.d(locald.h);
      if (!locald.d())
        break label69;
      h();
    }
    while (true)
    {
      return;
      com.netease.pris.c.c.a(locald);
      break;
      label69: if (d.h().i())
      {
        a(b(this.b, this.c, this.e));
        continue;
      }
      if (this.k)
      {
        a(e(this.b, this.c));
        continue;
      }
      a(r.a(this.b, this.c));
    }
  }

  private void r()
  {
    if (d.h().i())
      if ((this.e.h.size() == 0) || (((u)this.e.h.getFirst()).V().equals("-1")))
      {
        this.e = new com.netease.pris.protocol.d();
        this.e.a(true);
        c(0, this.e);
        h();
      }
    while (true)
    {
      return;
      a(b(this.b, this.c, this.e));
      continue;
      a(r.a(this.b, this.c));
    }
  }

  private void s()
  {
    com.netease.pris.protocol.d locald = p();
    locald.a(true);
    c(0, locald);
    h();
  }

  private m t()
  {
    k localk = com.netease.pris.protocol.c.b(this.b, this.c);
    localk.d(true);
    localk.g(true);
    return localk;
  }

  private m w()
  {
    k localk = null;
    ac localac;
    if (a(this.j))
    {
      localac = b(this.j);
      if (localac != null)
        break label43;
      d(10501, this.j.name());
    }
    while (true)
    {
      return localk;
      localac = null;
      label43: localk = com.netease.pris.protocol.c.a(this.g, localac, this.b, this.c);
      localk.b(this.b);
      localk.d(true);
      localk.g(true);
    }
  }

  private void x()
  {
    e(311);
  }

  private void y()
  {
    e(310);
  }

  private void z()
  {
    LinkedList localLinkedList = com.netease.pris.c.c.a(d.h().a(), com.netease.pris.protocol.c.b, true);
    if ((localLinkedList == null) || (localLinkedList.size() == 0))
    {
      c(0, new com.netease.pris.protocol.d());
      h();
    }
    while (true)
    {
      return;
      Iterator localIterator = localLinkedList.iterator();
      while (localIterator.hasNext())
        ((u)localIterator.next()).b(-1);
      a(a(this.b, this.c, new com.netease.pris.protocol.d(localLinkedList)));
    }
  }

  public void a(u paramu)
  {
    super.a(paramu);
    if (this.i != null)
    {
      this.i.add(paramu);
      if (this.i.size() >= 5)
        a();
    }
  }

  public void a(com.netease.pris.protocol.d paramd)
  {
    super.a(paramd);
    switch (n())
    {
    default:
    case 3:
    case 301:
    case 302:
    case 305:
    case 308:
    case 309:
    case 310:
    case 311:
    }
    while (true)
    {
      return;
      LinkedList localLinkedList = com.netease.pris.c.c.a(d.h().a(), this.b, true);
      if ((localLinkedList != null) && (localLinkedList.size() > 0))
        this.e = new com.netease.pris.protocol.d(localLinkedList);
      this.i = new LinkedList();
    }
  }

  protected void b(int paramInt, Object paramObject)
  {
    E();
    super.b(paramInt, paramObject);
  }

  public void b(com.netease.pris.protocol.d paramd)
  {
    a();
    c(paramd);
    d(paramd);
  }

  public void c()
  {
    if (!com.netease.pris.b.a.S())
      d.h().a(this);
    while (true)
    {
      return;
      m localm = null;
      switch (n())
      {
      default:
      case 2:
      case 304:
      case 312:
      case 316:
      case 3:
      case 306:
      case 303:
      case 309:
      case 302:
      case 300:
      case 307:
      case 308:
      case 305:
      case 301:
      case 310:
      case 311:
      case 315:
      }
      while (true)
      {
        if (localm == null)
          break label309;
        this.d = localm;
        this.d.b(j());
        a(this.d);
        break;
        localm = t();
        continue;
        localm = w();
        continue;
        localm = B();
        continue;
        localm = C();
        continue;
        localm = F();
        continue;
        localm = D();
        continue;
        y();
        break;
        q();
        break;
        s();
        break;
        r();
        break;
        x();
        break;
        z();
        break;
        localm = f(n());
        continue;
        localm = A();
      }
      label309: h();
    }
  }

  public void c(int paramInt1, int paramInt2, int paramInt3, Object paramObject)
  {
    if ((n() == 300) && (paramInt2 == 315))
      a(r.a(this.b, this.c));
    while (true)
    {
      return;
      if ((paramObject != null) && ((paramObject instanceof com.netease.pris.protocol.d)))
      {
        com.netease.pris.protocol.d locald = (com.netease.pris.protocol.d)paramObject;
        switch (n())
        {
        case 303:
        case 304:
        case 306:
        case 307:
        default:
          break;
        case 300:
        case 308:
          c(paramInt2, locald);
          break;
        case 305:
          b(paramInt2, locald);
          break;
        case 301:
        case 302:
          a(paramInt2, locald);
          continue;
        }
      }
    }
  }

  void c(com.netease.pris.protocol.d paramd)
  {
    if (paramd == null);
    LinkedList localLinkedList;
    label245: u localu2;
    while (true)
    {
      return;
      localLinkedList = paramd.h;
      Iterator localIterator3;
      switch (n())
      {
      default:
        break;
      case 3:
        paramd.a(8);
        paramd.a(true);
        if ((this.e != null) && (this.e.h.size() > 0))
          localIterator3 = this.e.h.iterator();
      case 311:
      case 309:
      case 310:
      case 303:
        while (true)
          if (localIterator3.hasNext())
          {
            u localu3 = (u)localIterator3.next();
            if (paramd.h.contains(localu3))
              continue;
            paramd.h.add(localu3);
            continue;
            com.netease.pris.c.c.a(paramd);
            break;
            paramd.a(true);
            com.netease.pris.c.c.a(paramd);
            com.netease.pris.c.c.a(paramd);
            if (!d.h().i())
              break;
            com.netease.pris.b.a.d(com.netease.pris.b.a.p() + localLinkedList.size());
            break;
            paramd.a(true);
            if (this.e != null)
            {
              Iterator localIterator1 = paramd.j.iterator();
              if (localIterator1.hasNext())
              {
                o localo = (o)localIterator1.next();
                Iterator localIterator2 = this.e.h.iterator();
                while (localIterator2.hasNext())
                {
                  localu2 = (u)localIterator2.next();
                  if (!localo.a().equals(localu2.V()))
                    continue;
                  localu2.a(com.netease.pris.c.c.b(d.h().a(), this.b, localu2));
                  localu2.a(aa.b);
                  paramd.h.add(localu2);
                }
              }
            }
          }
      case 304:
      case 316:
      case 312:
      }
    }
    while (true)
    {
      this.e.h.remove(localu2);
      break label245;
      com.netease.pris.c.c.c(d.h().a(), this.b, paramd.j);
      n.a(d.h().a(), this.b, paramd.j);
      break;
      Collections.sort(paramd.h);
      com.netease.pris.c.c.a(paramd);
      break;
      u[] arrayOfu = new u[localLinkedList.size()];
      localLinkedList.toArray(arrayOfu);
      int m = arrayOfu.length;
      for (int n = 0; n < m; n++)
      {
        u localu1 = arrayOfu[n];
        if ((!localu1.S()) || (!localu1.Q()))
          continue;
        localLinkedList.remove(localu1);
      }
      com.netease.pris.c.c.a(paramd, this.b);
      break;
      e(paramd);
      break;
      localu2 = null;
    }
  }

  public void d(int paramInt1, int paramInt2, int paramInt3, Object paramObject)
  {
    if ((n() == 300) && (paramInt2 == 315))
      a(r.a(this.b, this.c));
    do
      return;
    while (!f());
    switch (n())
    {
    default:
    case 300:
    case 308:
    }
    while (true)
    {
      d(paramInt1, paramObject);
      break;
      if ((this.e == null) || (!this.e.f()))
        continue;
      this.e.a(true);
      this.e.a(32);
      n.a(d.h().a(), this.b, this.e.h);
      c(0, this.e);
    }
  }

  void d(com.netease.pris.protocol.d paramd)
  {
    if (paramd == null);
    LinkedList localLinkedList;
    do
    {
      return;
      if (paramd.h.size() == 0)
        E();
      localLinkedList = paramd.i;
    }
    while ((localLinkedList == null) || (localLinkedList.size() <= 0));
    if ((paramd.h.size() == 0) && (((com.netease.pris.atom.r)localLinkedList.getFirst()).f() == 501))
    {
      if (this.j != b.c)
        break label140;
      l.a(d.h().a(), 1);
    }
    while (true)
      switch (n())
      {
      default:
        break;
      case 303:
      case 310:
      case 311:
        com.netease.pris.c.c.e(d.h().a(), this.b, localLinkedList);
        break;
        label140: if (this.j != b.b)
          continue;
        l.a(d.h().a(), 3);
      }
  }
}

/* Location:           D:\android\hack\dex2jar-0.0.9.8\classes_dex2jar.jar
 * Qualified Name:     com.netease.b.a.i
 * JD-Core Version:    0.6.0
 */
