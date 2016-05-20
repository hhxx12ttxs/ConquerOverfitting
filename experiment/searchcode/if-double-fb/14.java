import java.util.HashMap;
import java.util.Map;
import org.lwjgl.opengl.GL11;

public class tc
{
  private Map<Class<? extends si>, bu> o = new HashMap();

  public static tc a = new tc();
  private se p;
  public static double b;
  public static double c;
  public static double d;
  public jf e;
  public qv f;
  public fb g;
  public lo h;
  public float i;
  public float j;
  public kr k;
  public double l;
  public double m;
  public double n;

  private tc()
  {
    o.put(cl.class, new yo());
    o.put(vz.class, new ma(new ef(), new ef(0.5F), 0.7F));
    o.put(dj.class, new xp(new ms(), new dy(), 0.7F));
    o.put(bv.class, new us(new hf(), 0.7F));
    o.put(gg.class, new vw(new n(), 0.5F));
    o.put(wn.class, new im(new sy(), 0.3F));
    o.put(fz.class, new l());
    o.put(fp.class, new u(new ky(), 0.5F));
    o.put(ur.class, new u(new eh(), 0.5F));
    o.put(up.class, new mh(new nk(16), new nk(0), 0.25F));
    o.put(gq.class, new dq());
    o.put(np.class, new xx(new eh(), 0.5F, 6.0F));
    o.put(bn.class, new pm());
    o.put(xk.class, new eq(new we(), 0.7F));
    o.put(lo.class, new gt(new ff(), 0.5F));

    o.put(si.class, new lx());

    o.put(qq.class, new dw());
    o.put(sg.class, new ly());
    o.put(bw.class, new de(gk.aB.a(0)));
    o.put(vn.class, new de(gk.aN.a(0)));
    o.put(cd.class, new kh());
    o.put(hj.class, new ba());
    o.put(qr.class, new oj());
    o.put(jq.class, new gl());

    o.put(yc.class, new sw());
    o.put(fx.class, new fc());
    o.put(lt.class, new ph());

    o.put(c.class, new pe());

    ModLoader.AddAllRenderers(o);

    for (bu localbu : o.values())
      localbu.a(this);
  }

  public bu a(Class paramClass)
  {
    bu localbu = (bu)o.get(paramClass);
    if ((localbu == null) && (paramClass != si.class)) {
      localbu = a(paramClass.getSuperclass());
      o.put(paramClass, localbu);
    }
    return localbu;
  }

  public bu a(si paramsi) {
    return a(paramsi.getClass());
  }

  public void a(fb paramfb, jf paramjf, se paramse, lo paramlo, kr paramkr, float paramFloat) {
    g = paramfb;
    e = paramjf;
    k = paramkr;
    h = paramlo;
    p = paramse;

    if (paramlo.N()) {
      int i1 = paramfb.a(ik.b(paramlo.aM), ik.b(paramlo.aN), ik.b(paramlo.aO));
      if (i1 == un.T.bn) {
        int i2 = paramfb.e(ik.b(paramlo.aM), ik.b(paramlo.aN), ik.b(paramlo.aO));

        int i3 = i2 & 0x3;

        i = (i3 * 90 + 180);
        j = 0.0F;
      }

    }
    else
    {
      i = (paramlo.aU + (paramlo.aS - paramlo.aU) * paramFloat);
      j = (paramlo.aV + (paramlo.aT - paramlo.aV) * paramFloat);
    }

    l = (paramlo.bl + (paramlo.aM - paramlo.bl) * paramFloat);
    m = (paramlo.bm + (paramlo.aN - paramlo.bm) * paramFloat);
    n = (paramlo.bn + (paramlo.aO - paramlo.bn) * paramFloat);
  }

  public void a(si paramsi, float paramFloat) {
    double d1 = paramsi.bl + (paramsi.aM - paramsi.bl) * paramFloat;
    double d2 = paramsi.bm + (paramsi.aN - paramsi.bm) * paramFloat;
    double d3 = paramsi.bn + (paramsi.aO - paramsi.bn) * paramFloat;
    float f1 = paramsi.aU + (paramsi.aS - paramsi.aU) * paramFloat;

    float f2 = paramsi.a(paramFloat);

    GL11.glColor3f(f2, f2, f2);

    a(paramsi, d1 - b, d2 - c, d3 - d, f1, paramFloat);
  }

  public void a(si paramsi, double paramDouble1, double paramDouble2, double paramDouble3, float paramFloat1, float paramFloat2) {
    bu localbu = a(paramsi);
    if (localbu != null) {
      localbu.a(paramsi, paramDouble1, paramDouble2, paramDouble3, paramFloat1, paramFloat2);
      localbu.b(paramsi, paramDouble1, paramDouble2, paramDouble3, paramFloat1, paramFloat2);
    }
  }

  public void a(fb paramfb)
  {
    g = paramfb;
  }

  public double a(double paramDouble1, double paramDouble2, double paramDouble3) {
    double d1 = paramDouble1 - l;
    double d2 = paramDouble2 - m;
    double d3 = paramDouble3 - n;
    return d1 * d1 + d2 * d2 + d3 * d3;
  }

  public se a() {
    return p;
  }
}
