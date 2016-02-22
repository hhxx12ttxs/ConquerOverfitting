import java.util.Random;

public class hj extends si
{
  public iw a;
  private int e;
  public int b = 0;
  public int c;
  private int f = 5;

  public float d = (float)(Math.random() * 3.141592653589793D * 2.0D);

  public hj(fb paramfb, double paramDouble1, double paramDouble2, double paramDouble3, iw paramiw) {
    super(paramfb);
    b(0.25F, 0.25F);
    bf = (bh / 2.0F);
    d(paramDouble1, paramDouble2, paramDouble3);
    a = paramiw;

    aS = (float)(Math.random() * 360.0D);

    aP = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
    aQ = 0.2000000029802322D;
    aR = (float)(Math.random() * 0.2000000029802322D - 0.1000000014901161D);
  }

  protected boolean n()
  {
    return false;
  }

  public hj(fb paramfb)
  {
    super(paramfb);
    b(0.25F, 0.25F);
    bf = (bh / 2.0F);
  }

  protected void b()
  {
  }

  public void w_()
  {
    super.w_();
    if (c > 0) c -= 1;
    aJ = aM;
    aK = aN;
    aL = aO;

    aQ -= 0.03999999910593033D;
    if (aI.f(ik.b(aM), ik.b(aN), ik.b(aO)) == lj.h) {
      aQ = 0.2000000029802322D;
      aP = ((bs.nextFloat() - bs.nextFloat()) * 0.2F);
      aR = ((bs.nextFloat() - bs.nextFloat()) * 0.2F);
      aI.a(this, "random.fizz", 0.4F, 2.0F + bs.nextFloat() * 0.4F);
    }
    c(aM, (aW.b + aW.e) / 2.0D, aO);
    b(aP, aQ, aR);

    float f1 = 0.98F;
    if (aX) {
      f1 = 0.5880001F;
      int i = aI.a(ik.b(aM), ik.b(aW.b) - 1, ik.b(aO));
      if (i > 0) {
        f1 = un.m[i].bB * 0.98F;
      }
    }

    aP *= f1;
    aQ *= 0.9800000190734863D;
    aR *= f1;

    if (aX) {
      aQ *= -0.5D;
    }

    e += 1;
    b += 1;
    if (b >= 6000)
      K();
  }

  public boolean j_()
  {
    return aI.a(aW, lj.g, this);
  }

  protected void a(int paramInt) {
    a(null, paramInt);
  }

  public boolean a(si paramsi, int paramInt) {
    ai();
    f -= paramInt;
    if (f <= 0) {
      K();
    }
    return false;
  }

  public void b(nq paramnq) {
    paramnq.a("Health", (short)(byte)f);
    paramnq.a("Age", (short)b);
    paramnq.a("Item", a.a(new nq()));
  }

  public void a(nq paramnq) {
    f = (paramnq.d("Health") & 0xFF);
    b = paramnq.d("Age");
    nq localnq = paramnq.k("Item");
    a = new iw(localnq);
  }

  public void b(gq paramgq) {
    if (aI.B) return;

    int i = a.a;
    if ((c == 0) && (paramgq.c.a(a))) {
      if (a.c == un.K.bn) paramgq.a(en.g);
      if (a.c == gk.aD.be) paramgq.a(en.t);
      ModLoader.OnItemPickup(paramgq, a);
      aI.a(this, "random.pop", 0.2F, ((bs.nextFloat() - bs.nextFloat()) * 0.7F + 1.0F) * 2.0F);
      paramgq.b(this, i);

      if (a.a <= 0) K();
    }
  }
}
