import java.util.Random;

public class xh extends rr
{
  private Random a = new Random();

  protected xh(int paramInt) {
    super(paramInt, lj.e);
    bm = 45;
  }

  public int d() {
    return 4;
  }

  public int a(int paramInt, Random paramRandom) {
    return un.Q.bn;
  }

  public void e(fb paramfb, int paramInt1, int paramInt2, int paramInt3) {
    super.e(paramfb, paramInt1, paramInt2, paramInt3);
    h(paramfb, paramInt1, paramInt2, paramInt3);
  }

  private void h(fb paramfb, int paramInt1, int paramInt2, int paramInt3) {
    if (paramfb.B) {
      return;
    }

    int i = paramfb.a(paramInt1, paramInt2, paramInt3 - 1);
    int j = paramfb.a(paramInt1, paramInt2, paramInt3 + 1);
    int k = paramfb.a(paramInt1 - 1, paramInt2, paramInt3);
    int m = paramfb.a(paramInt1 + 1, paramInt2, paramInt3);

    int n = 3;
    if ((un.o[i] != 0) && (un.o[j] == 0))
      n = 3;
    if ((un.o[j] != 0) && (un.o[i] == 0))
      n = 2;
    if ((un.o[k] != 0) && (un.o[m] == 0))
      n = 5;
    if ((un.o[m] != 0) && (un.o[k] == 0))
      n = 4;
    paramfb.d(paramInt1, paramInt2, paramInt3, n);
  }

  public int a(xg paramxg, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    if (paramInt4 == 1)
      return bm + 17;
    if (paramInt4 == 0) {
      return bm + 17;
    }
    int i = paramxg.e(paramInt1, paramInt2, paramInt3);

    if (paramInt4 != i)
      return bm;
    return bm + 1;
  }

  public int a(int paramInt) {
    if (paramInt == 1)
      return bm + 17;
    if (paramInt == 0)
      return bm + 17;
    if (paramInt == 3)
      return bm + 1;
    return bm;
  }

  public boolean a(fb paramfb, int paramInt1, int paramInt2, int paramInt3, gq paramgq) {
    if (paramfb.B) {
      return true;
    }
    ay localay = (ay)paramfb.b(paramInt1, paramInt2, paramInt3);
    paramgq.a(localay);

    return true;
  }

  private void c(fb paramfb, int paramInt1, int paramInt2, int paramInt3, Random paramRandom) {
    int i = paramfb.e(paramInt1, paramInt2, paramInt3);

    int j = 0;
    int k = 0;
    if (i == 3) k = 1;
    else if (i == 2) k = -1;
    else if (i == 5) j = 1;
    else {
      j = -1;
    }

    ay localay = (ay)paramfb.b(paramInt1, paramInt2, paramInt3);
    iw localiw = localay.b();
    double d1 = paramInt1 + j * 0.6D + 0.5D;
    double d2 = paramInt2 + 0.5D;
    double d3 = paramInt3 + k * 0.6D + 0.5D;
    if (localiw == null) {
      paramfb.e(1001, paramInt1, paramInt2, paramInt3, 0);
    } else {
      boolean handled = ModLoader.DispenseEntity(paramfb, d1, d2, d3, j, k, localiw);
      if (!handled)
      {
        if (localiw.c == gk.j.be) {
          si localObject = new sg(paramfb, d1, d2, d3);
          ((sg)localObject).a(j, 0.1000000014901161D, k, 1.1F, 6.0F);
          ((sg)localObject).a = true;
          paramfb.b(localObject);
          paramfb.e(1002, paramInt1, paramInt2, paramInt3, 0);
        } else if (localiw.c == gk.aN.be) {
          si localObject = new vn(paramfb, d1, d2, d3);
          ((vn)localObject).a(j, 0.1000000014901161D, k, 1.1F, 6.0F);
          paramfb.b(localObject);
          paramfb.e(1002, paramInt1, paramInt2, paramInt3, 0);
        } else if (localiw.c == gk.aB.be) {
          si localObject = new bw(paramfb, d1, d2, d3);
          ((bw)localObject).a(j, 0.1000000014901161D, k, 1.1F, 6.0F);
          paramfb.b(localObject);
          paramfb.e(1002, paramInt1, paramInt2, paramInt3, 0);
        } else {
          si localObject = new hj(paramfb, d1, d2 - 0.3D, d3, localiw);

          double d4 = paramRandom.nextDouble() * 0.1D + 0.2D;
          ((hj)localObject).aP = (j * d4);
          ((hj)localObject).aQ = 0.2000000029802322D;
          ((hj)localObject).aR = (k * d4);

          localObject.aP += paramRandom.nextGaussian() * 0.007499999832361937D * 6.0D;
          localObject.aQ += paramRandom.nextGaussian() * 0.007499999832361937D * 6.0D;
          localObject.aR += paramRandom.nextGaussian() * 0.007499999832361937D * 6.0D;

          paramfb.b(localObject);
          paramfb.e(1000, paramInt1, paramInt2, paramInt3, 0);
        }
      }
      paramfb.e(2000, paramInt1, paramInt2, paramInt3, j + 1 + (k + 1) * 3);
    }
  }

  public void a(fb paramfb, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    if ((paramInt4 > 0) && (un.m[paramInt4].e())) {
      int i = (paramfb.s(paramInt1, paramInt2, paramInt3)) || (paramfb.s(paramInt1, paramInt2 + 1, paramInt3)) ? 1 : 0;
      if (i != 0)
        paramfb.c(paramInt1, paramInt2, paramInt3, bn, d());
    }
  }

  public void a(fb paramfb, int paramInt1, int paramInt2, int paramInt3, Random paramRandom) {
    if ((paramfb.s(paramInt1, paramInt2, paramInt3)) || (paramfb.s(paramInt1, paramInt2 + 1, paramInt3)))
      c(paramfb, paramInt1, paramInt2, paramInt3, paramRandom);
  }

  protected os a_() {
    return new ay();
  }

  public void a(fb paramfb, int paramInt1, int paramInt2, int paramInt3, lo paramlo) {
    int i = ik.b(paramlo.aS * 4.0F / 360.0F + 0.5D) & 0x3;

    if (i == 0)
      paramfb.d(paramInt1, paramInt2, paramInt3, 2);
    if (i == 1)
      paramfb.d(paramInt1, paramInt2, paramInt3, 5);
    if (i == 2)
      paramfb.d(paramInt1, paramInt2, paramInt3, 3);
    if (i == 3)
      paramfb.d(paramInt1, paramInt2, paramInt3, 4);
  }

  public void b(fb paramfb, int paramInt1, int paramInt2, int paramInt3) {
    ay localay = (ay)paramfb.b(paramInt1, paramInt2, paramInt3);
    for (int i = 0; i < localay.a(); i++) {
      iw localiw = localay.d_(i);
      if (localiw != null) {
        float f1 = a.nextFloat() * 0.8F + 0.1F;
        float f2 = a.nextFloat() * 0.8F + 0.1F;
        float f3 = a.nextFloat() * 0.8F + 0.1F;

        while (localiw.a > 0) {
          int j = a.nextInt(21) + 10;
          if (j > localiw.a)
            j = localiw.a;
          localiw.a -= j;

          hj localhj = new hj(paramfb, paramInt1 + f1, paramInt2 + f2, paramInt3 + f3, new iw(localiw.c, j, localiw.i()));
          float f4 = 0.05F;
          localhj.aP = ((float)a.nextGaussian() * f4);
          localhj.aQ = ((float)a.nextGaussian() * f4 + 0.2F);
          localhj.aR = ((float)a.nextGaussian() * f4);
          paramfb.b(localhj);
        }
      }
    }
    super.b(paramfb, paramInt1, paramInt2, paramInt3);
  }
}
