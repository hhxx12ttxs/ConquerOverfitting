import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;

public class ct
{
  public xg b = null;
  public int c = 0;
  public boolean d = false;
  public boolean e = false;
  public static boolean a = true;
  public static boolean cfgGrassFix = true;
  public boolean f = false;
  public float g = 0.0F;
  public float h = 0.0F;
  public float i = 0.0F;
  public float j = 0.0F;
  public float k = 0.0F;
  public float l = 0.0F;
  public float m = 0.0F;
  public float n = 0.0F;
  public float o = 0.0F;
  public float p = 0.0F;
  public float q = 0.0F;
  public float r = 0.0F;
  public float s = 0.0F;
  public float t = 0.0F;
  public float u = 0.0F;
  public float v = 0.0F;
  public float w = 0.0F;
  public float x = 0.0F;
  public float y = 0.0F;
  public float z = 0.0F;
  public float A = 0.0F;
  public float B = 0.0F;
  public float C = 0.0F;
  public float D = 0.0F;
  public float E = 0.0F;
  public float F = 0.0F;
  public float G = 0.0F;
  public int H = 0;
  public float I = 0.0F;
  public float J = 0.0F;
  public float K = 0.0F;
  public float L = 0.0F;
  public float M = 0.0F;
  public float N = 0.0F;
  public float O = 0.0F;
  public float P = 0.0F;
  public float Q = 0.0F;
  public float R = 0.0F;
  public float S = 0.0F;
  public float T = 0.0F;
  public boolean U = false;
  public boolean V = false;
  public boolean W = false;
  public boolean X = false;
  public boolean Y = false;
  public boolean Z = false;
  public boolean aa = false;
  public boolean ab = false;
  public boolean ac = false;
  public boolean ad = false;
  public boolean ae = false;
  public boolean af = false;

  public static float[][] redstoneColors = new float[16][];

  static { for (int i = 0; i < redstoneColors.length; i++) {
      float j = i / 15.0F;
      float red = j * 0.6F + 0.4F;
      if (i == 0)
        j = 0.0F;
      float green = j * j * 0.7F - 0.5F;
      float blue = j * j * 0.6F - 0.7F;
      if (green < 0.0F)
        green = 0.0F;
      if (blue < 0.0F)
        blue = 0.0F;
      redstoneColors[i] = { red, green, blue };
    }
  }

  public ct(xg xg1)
  {
    c = -1;
    d = false;
    e = false;
    H = 1;
    b = xg1;
  }

  public ct() {
    c = -1;
    d = false;
    e = false;
    H = 1;
  }

  public void a(un un1, int i1, int j1, int k1, int l1) {
    c = l1;
    a(un1, i1, j1, k1);
    c = -1;
  }

  public boolean a(un un1, int i1, int j1, int k1) {
    int l1 = un1.g();
    un1.a(b, i1, j1, k1);
    if (l1 == 0)
      return j(un1, i1, j1, k1);
    if (l1 == 4)
      return i(un1, i1, j1, k1);
    if (l1 == 13)
      return k(un1, i1, j1, k1);
    if (l1 == 1)
      return g(un1, i1, j1, k1);
    if (l1 == 6)
      return h(un1, i1, j1, k1);
    if (l1 == 2)
      return b(un1, i1, j1, k1);
    if (l1 == 3)
      return d(un1, i1, j1, k1);
    if (l1 == 5)
      return e(un1, i1, j1, k1);
    if (l1 == 8)
      return f(un1, i1, j1, k1);
    if (l1 == 7)
      return n(un1, i1, j1, k1);
    if (l1 == 9)
      return a((oy)un1, i1, j1, k1);
    if (l1 == 10)
      return m(un1, i1, j1, k1);
    if (l1 == 11)
      return l(un1, i1, j1, k1);
    if (l1 == 12)
      return c(un1, i1, j1, k1);
    if (l1 == 14)
      return o(un1, i1, j1, k1);
    if (l1 == 15) return p(un1, i1, j1, k1);
    return ModLoader.RenderWorldBlock(this, b, i1, j1, k1, un1, l1);
  }

  public boolean o(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    int l1 = b.e(i1, j1, k1);
    int i2 = uw.c(l1);
    boolean flag = uw.d(l1);
    float f1 = 0.5F;
    float f2 = 1.0F;
    float f3 = 0.8F;
    float f4 = 0.6F;
    float f5 = f2;
    float f6 = f2;
    float f7 = f2;
    float f8 = f1;
    float f9 = f3;
    float f10 = f4;
    float f11 = f1;
    float f12 = f3;
    float f13 = f4;
    float f14 = f1;
    float f15 = f3;
    float f16 = f4;
    float f17 = un1.d(b, i1, j1, k1);
    ns1.a(f8 * f17, f11 * f17, f14 * f17);
    int i18 = un1.a(b, i1, j1, k1, 0);
    int j2 = (i18 & 0xF) << 4;
    int k2 = i18 & 0xF0;
    double d1 = j2 / 256.0F;
    double d3 = (j2 + 16 - 0.01D) / 256.0D;
    double d5 = k2 / 256.0F;
    double d7 = (k2 + 16 - 0.01D) / 256.0D;
    double d9 = i1 + un1.bs;
    double d11 = i1 + un1.bv;
    double d13 = j1 + un1.bt + 0.1875D;
    double d15 = k1 + un1.bu;
    double d17 = k1 + un1.bx;
    ns1.a(d9, d13, d17, d1, d7);
    ns1.a(d9, d13, d15, d1, d5);
    ns1.a(d11, d13, d15, d3, d5);
    ns1.a(d11, d13, d17, d3, d7);
    float f18 = un1.d(b, i1, j1 + 1, k1);
    ns1.a(f5 * f18, f6 * f18, f7 * f18);
    j2 = un1.a(b, i1, j1, k1, 1);
    k2 = (j2 & 0xF) << 4;
    d1 = j2 & 0xF0;
    double d2 = k2 / 256.0F;
    double d4 = (k2 + 16 - 0.01D) / 256.0D;
    double d6 = (float)d1 / 256.0F;
    double d8 = (d1 + 16.0D - 0.01D) / 256.0D;
    double d10 = d2;
    double d12 = d4;
    double d14 = d6;
    double d16 = d6;
    double d18 = d2;
    double d19 = d4;
    double d20 = d8;
    double d21 = d8;
    if (i2 == 0) {
      d12 = d2;
      d14 = d8;
      d18 = d4;
      d21 = d6;
    } else if (i2 == 2) {
      d10 = d4;
      d16 = d8;
      d19 = d2;
      d20 = d6;
    } else if (i2 == 3) {
      d10 = d4;
      d16 = d8;
      d19 = d2;
      d20 = d6;
      d12 = d2;
      d14 = d8;
      d18 = d4;
      d21 = d6;
    }
    double d22 = i1 + un1.bs;
    double d23 = i1 + un1.bv;
    double d24 = j1 + un1.bw;
    double d25 = k1 + un1.bu;
    double d26 = k1 + un1.bx;
    ns1.a(d23, d24, d26, d18, d20);
    ns1.a(d23, d24, d25, d10, d14);
    ns1.a(d22, d24, d25, d12, d16);
    ns1.a(d22, d24, d26, d19, d21);
    f18 = jg.a[i2];
    if (flag)
      f18 = jg.a[jg.b[i2]];
    j2 = 4;
    switch (i2) {
    case 0:
      j2 = 5;
      break;
    case 3:
      j2 = 2;
      break;
    case 1:
      j2 = 3;
    case 2:
    }
    if ((f18 != 2.0F) && ((e) || (un1.b(b, i1, j1, k1 - 1, 2)))) {
      float f19 = un1.d(b, i1, j1, k1 - 1);
      if (un1.bu > 0.0D)
        f19 = f17;
      ns1.a(f9 * f19, f12 * f19, f15 * f19);
      d = (j2 == 2);
      c(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 2));
    }
    if ((f18 != 3.0F) && ((e) || (un1.b(b, i1, j1, k1 + 1, 3)))) {
      float f20 = un1.d(b, i1, j1, k1 + 1);
      if (un1.bx < 1.0D)
        f20 = f17;
      ns1.a(f9 * f20, f12 * f20, f15 * f20);
      d = (j2 == 3);
      d(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 3));
    }
    if ((f18 != 4.0F) && ((e) || (un1.b(b, i1 - 1, j1, k1, 4)))) {
      float f21 = un1.d(b, i1 - 1, j1, k1);
      if (un1.bs > 0.0D)
        f21 = f17;
      ns1.a(f10 * f21, f13 * f21, f16 * f21);
      d = (j2 == 4);
      e(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 4));
    }
    if ((f18 != 5.0F) && ((e) || (un1.b(b, i1 + 1, j1, k1, 5)))) {
      float f22 = un1.d(b, i1 + 1, j1, k1);
      if (un1.bv < 1.0D)
        f22 = f17;
      ns1.a(f10 * f22, f13 * f22, f16 * f22);
      d = (j2 == 5);
      f(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 5));
    }
    d = false;
    return true;
  }

  public boolean b(un un1, int i1, int j1, int k1) {
    int l1 = b.e(i1, j1, k1);
    ns ns1 = ns.a;
    float f1 = un1.d(b, i1, j1, k1);
    if (un.s[un1.bn] > 0)
      f1 = 1.0F;
    ns1.a(f1, f1, f1);
    double d1 = 0.4000000059604645D;
    double d2 = 0.5D - d1;
    double d3 = 0.2000000029802322D;
    if (l1 == 1) a(un1, i1 - d2, j1 + d3, k1, -d1, 0.0D);
    else if (l1 == 2) a(un1, i1 + d2, j1 + d3, k1, d1, 0.0D);
    else if (l1 == 3) a(un1, i1, j1 + d3, k1 - d2, 0.0D, -d1);
    else if (l1 == 4) a(un1, i1, j1 + d3, k1 + d2, 0.0D, d1); else
      a(un1, i1, j1, k1, 0.0D, 0.0D);
    return true;
  }

  public boolean p(un un1, int i1, int j1, int k1) {
    int l1 = b.e(i1, j1, k1);
    int i2 = l1 & 0x3;
    int j2 = (l1 & 0xC) >> 2;
    j(un1, i1, j1, k1);
    ns ns1 = ns.a;
    float f1 = un1.d(b, i1, j1, k1);
    if (un.s[un1.bn] > 0)
      f1 = (f1 + 1.0F) * 0.5F;
    ns1.a(f1, f1, f1);
    double d1 = -0.1875D;
    double d2 = 0.0D;
    double d3 = 0.0D;
    double d4 = 0.0D;
    double d5 = 0.0D;
    switch (i2) {
    case 0:
      d5 = -0.3125D;
      d3 = wf.a[j2];
      break;
    case 2:
      d5 = 0.3125D;
      d3 = -wf.a[j2];
      break;
    case 3:
      d4 = -0.3125D;
      d2 = wf.a[j2];
      break;
    case 1:
      d4 = 0.3125D;
      d2 = -wf.a[j2];
    }

    a(un1, i1 + d2, j1 + d1, k1 + d3, 0.0D, 0.0D);
    a(un1, i1 + d4, j1 + d1, k1 + d5, 0.0D, 0.0D);
    int k2 = un1.a(1);
    int l2 = (k2 & 0xF) << 4;
    int i3 = k2 & 0xF0;
    double d6 = l2 / 256.0F;
    double d7 = (l2 + 15.99F) / 256.0F;
    double d8 = i3 / 256.0F;
    double d9 = (i3 + 15.99F) / 256.0F;
    float f2 = 0.125F;
    float f3 = i1 + 1;
    float f4 = i1 + 1;
    float f5 = i1 + 0;
    float f6 = i1 + 0;
    float f7 = k1 + 0;
    float f8 = k1 + 1;
    float f9 = k1 + 1;
    float f10 = k1 + 0;
    float f11 = j1 + f2;
    if (i2 == 2) {
      f3 = f4 = i1 + 0;
      f5 = f6 = i1 + 1;
      f7 = f10 = k1 + 1;
      f8 = f9 = k1 + 0;
    } else if (i2 == 3) {
      f3 = f6 = i1 + 0;
      f4 = f5 = i1 + 1;
      f7 = f8 = k1 + 0;
      f9 = f10 = k1 + 1;
    } else if (i2 == 1) {
      f3 = f6 = i1 + 1;
      f4 = f5 = i1 + 0;
      f7 = f8 = k1 + 1;
      f9 = f10 = k1 + 0;
    }
    ns1.a(f6, f11, f10, d6, d8);
    ns1.a(f5, f11, f9, d6, d9);
    ns1.a(f4, f11, f8, d7, d9);
    ns1.a(f3, f11, f7, d7, d8);
    return true;
  }

  public boolean c(un un1, int i1, int j1, int k1) {
    int l1 = b.e(i1, j1, k1);
    int i2 = l1 & 0x7;
    boolean flag = (l1 & 0x8) > 0;
    ns ns1 = ns.a;
    boolean flag1 = c >= 0;
    if (!flag1)
      c = un.x.bm;
    float f1 = 0.25F;
    float f2 = 0.1875F;
    float f3 = 0.1875F;
    if (i2 == 5) un1.a(0.5F - f2, 0.0F, 0.5F - f1, 0.5F + f2, f3, 0.5F + f1);
    else if (i2 == 6) un1.a(0.5F - f1, 0.0F, 0.5F - f2, 0.5F + f1, f3, 0.5F + f2);
    else if (i2 == 4) un1.a(0.5F - f2, 0.5F - f1, 1.0F - f3, 0.5F + f2, 0.5F + f1, 1.0F);
    else if (i2 == 3) un1.a(0.5F - f2, 0.5F - f1, 0.0F, 0.5F + f2, 0.5F + f1, f3);
    else if (i2 == 2) un1.a(1.0F - f3, 0.5F - f1, 0.5F - f2, 1.0F, 0.5F + f1, 0.5F + f2);
    else if (i2 == 1)
      un1.a(0.0F, 0.5F - f1, 0.5F - f2, f3, 0.5F + f1, 0.5F + f2);
    j(un1, i1, j1, k1);
    if (!flag1)
      c = -1;
    float f4 = un1.d(b, i1, j1, k1);
    if (un.s[un1.bn] > 0)
      f4 = 1.0F;
    ns1.a(f4, f4, f4);
    int j2 = un1.a(0);
    if (c >= 0)
      j2 = c;
    int k2 = (j2 & 0xF) << 4;
    int l2 = j2 & 0xF0;
    float f5 = k2 / 256.0F;
    float f6 = (k2 + 15.99F) / 256.0F;
    float f7 = l2 / 256.0F;
    float f8 = (l2 + 15.99F) / 256.0F;
    br[] abr = new br[8];
    float f9 = 0.0625F;
    float f10 = 0.0625F;
    float f11 = 0.625F;
    abr[0] = br.b(-f9, 0.0D, -f10);
    abr[1] = br.b(f9, 0.0D, -f10);
    abr[2] = br.b(f9, 0.0D, f10);
    abr[3] = br.b(-f9, 0.0D, f10);
    abr[4] = br.b(-f9, f11, -f10);
    abr[5] = br.b(f9, f11, -f10);
    abr[6] = br.b(f9, f11, f10);
    abr[7] = br.b(-f9, f11, f10);
    for (int i3 = 0; i3 < 8; i3++) {
      if (flag) {
        abr[i3].c -= 0.0625D;
        abr[i3].a(0.6981317F);
      } else {
        abr[i3].c += 0.0625D;
        abr[i3].a(-0.6981317F);
      }
      if (i2 == 6)
        abr[i3].b(1.570796F);
      if (i2 < 5) {
        abr[i3].b -= 0.375D;
        abr[i3].a(1.570796F);
        if (i2 == 4)
          abr[i3].b(0.0F);
        if (i2 == 3)
          abr[i3].b(3.141593F);
        if (i2 == 2)
          abr[i3].b(1.570796F);
        if (i2 == 1)
          abr[i3].b(-1.570796F);
        abr[i3].a += i1 + 0.5D;
        abr[i3].b += j1 + 0.5F;
        abr[i3].c += k1 + 0.5D;
      } else {
        abr[i3].a += i1 + 0.5D;
        abr[i3].b += j1 + 0.125F;
        abr[i3].c += k1 + 0.5D;
      }
    }

    br br1 = null;
    br br2 = null;
    br br3 = null;
    br br4 = null;
    for (int j3 = 0; j3 < 6; j3++) {
      if (j3 == 0) {
        f5 = (k2 + 7) / 256.0F;
        f6 = (k2 + 9 - 0.01F) / 256.0F;
        f7 = (l2 + 6) / 256.0F;
        f8 = (l2 + 8 - 0.01F) / 256.0F;
      } else if (j3 == 2) {
        f5 = (k2 + 7) / 256.0F;
        f6 = (k2 + 9 - 0.01F) / 256.0F;
        f7 = (l2 + 6) / 256.0F;
        f8 = (l2 + 16 - 0.01F) / 256.0F;
      }
      if (j3 == 0) {
        br1 = abr[0];
        br2 = abr[1];
        br3 = abr[2];
        br4 = abr[3];
      } else if (j3 == 1) {
        br1 = abr[7];
        br2 = abr[6];
        br3 = abr[5];
        br4 = abr[4];
      } else if (j3 == 2) {
        br1 = abr[1];
        br2 = abr[0];
        br3 = abr[4];
        br4 = abr[5];
      } else if (j3 == 3) {
        br1 = abr[2];
        br2 = abr[1];
        br3 = abr[5];
        br4 = abr[6];
      } else if (j3 == 4) {
        br1 = abr[3];
        br2 = abr[2];
        br3 = abr[6];
        br4 = abr[7];
      } else if (j3 == 5) {
        br1 = abr[0];
        br2 = abr[3];
        br3 = abr[7];
        br4 = abr[4];
      }
      ns1.a(br1.a, br1.b, br1.c, f5, f8);
      ns1.a(br2.a, br2.b, br2.c, f6, f8);
      ns1.a(br3.a, br3.b, br3.c, f6, f7);
      ns1.a(br4.a, br4.b, br4.c, f5, f7);
    }

    return true;
  }

  public boolean d(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    int l1 = un1.a(0);
    if (c >= 0)
      l1 = c;
    float f1 = un1.d(b, i1, j1, k1);
    ns1.a(f1, f1, f1);
    int i2 = (l1 & 0xF) << 4;
    int j2 = l1 & 0xF0;
    double d1 = i2 / 256.0F;
    double d3 = (i2 + 15.99F) / 256.0F;
    double d5 = j2 / 256.0F;
    double d7 = (j2 + 15.99F) / 256.0F;
    float f2 = 1.4F;
    if ((b.h(i1, j1 - 1, k1)) || (un.as.c(b, i1, j1 - 1, k1))) {
      double d9 = i1 + 0.5D + 0.2D;
      double d10 = i1 + 0.5D - 0.2D;
      double d13 = k1 + 0.5D + 0.2D;
      double d15 = k1 + 0.5D - 0.2D;
      double d17 = i1 + 0.5D - 0.3D;
      double d19 = i1 + 0.5D + 0.3D;
      double d21 = k1 + 0.5D - 0.3D;
      double d23 = k1 + 0.5D + 0.3D;
      ns1.a(d17, j1 + f2, k1 + 1, d3, d5);
      ns1.a(d9, j1 + 0, k1 + 1, d3, d7);
      ns1.a(d9, j1 + 0, k1 + 0, d1, d7);
      ns1.a(d17, j1 + f2, k1 + 0, d1, d5);
      ns1.a(d19, j1 + f2, k1 + 0, d3, d5);
      ns1.a(d10, j1 + 0, k1 + 0, d3, d7);
      ns1.a(d10, j1 + 0, k1 + 1, d1, d7);
      ns1.a(d19, j1 + f2, k1 + 1, d1, d5);
      d1 = i2 / 256.0F;
      d3 = (i2 + 15.99F) / 256.0F;
      d5 = (j2 + 16) / 256.0F;
      d7 = (j2 + 15.99F + 16.0F) / 256.0F;
      ns1.a(i1 + 1, j1 + f2, d23, d3, d5);
      ns1.a(i1 + 1, j1 + 0, d15, d3, d7);
      ns1.a(i1 + 0, j1 + 0, d15, d1, d7);
      ns1.a(i1 + 0, j1 + f2, d23, d1, d5);
      ns1.a(i1 + 0, j1 + f2, d21, d3, d5);
      ns1.a(i1 + 0, j1 + 0, d13, d3, d7);
      ns1.a(i1 + 1, j1 + 0, d13, d1, d7);
      ns1.a(i1 + 1, j1 + f2, d21, d1, d5);
      d9 = i1 + 0.5D - 0.5D;
      d10 = i1 + 0.5D + 0.5D;
      d13 = k1 + 0.5D - 0.5D;
      d15 = k1 + 0.5D + 0.5D;
      d17 = i1 + 0.5D - 0.4D;
      d19 = i1 + 0.5D + 0.4D;
      d21 = k1 + 0.5D - 0.4D;
      d23 = k1 + 0.5D + 0.4D;
      ns1.a(d17, j1 + f2, k1 + 0, d1, d5);
      ns1.a(d9, j1 + 0, k1 + 0, d1, d7);
      ns1.a(d9, j1 + 0, k1 + 1, d3, d7);
      ns1.a(d17, j1 + f2, k1 + 1, d3, d5);
      ns1.a(d19, j1 + f2, k1 + 1, d1, d5);
      ns1.a(d10, j1 + 0, k1 + 1, d1, d7);
      ns1.a(d10, j1 + 0, k1 + 0, d3, d7);
      ns1.a(d19, j1 + f2, k1 + 0, d3, d5);
      d1 = i2 / 256.0F;
      d3 = (i2 + 15.99F) / 256.0F;
      d5 = j2 / 256.0F;
      d7 = (j2 + 15.99F) / 256.0F;
      ns1.a(i1 + 0, j1 + f2, d23, d1, d5);
      ns1.a(i1 + 0, j1 + 0, d15, d1, d7);
      ns1.a(i1 + 1, j1 + 0, d15, d3, d7);
      ns1.a(i1 + 1, j1 + f2, d23, d3, d5);
      ns1.a(i1 + 1, j1 + f2, d21, d1, d5);
      ns1.a(i1 + 1, j1 + 0, d13, d1, d7);
      ns1.a(i1 + 0, j1 + 0, d13, d3, d7);
      ns1.a(i1 + 0, j1 + f2, d21, d3, d5);
    } else {
      float f4 = 0.2F;
      float f5 = 0.0625F;
      if ((i1 + j1 + k1 & 0x1) == 1) {
        d1 = i2 / 256.0F;
        d3 = (i2 + 15.99F) / 256.0F;
        d5 = (j2 + 16) / 256.0F;
        d7 = (j2 + 15.99F + 16.0F) / 256.0F;
      }
      if ((i1 / 2 + j1 / 2 + k1 / 2 & 0x1) == 1) {
        double d11 = d3;
        d3 = d1;
        d1 = d11;
      }
      if (un.as.c(b, i1 - 1, j1, k1)) {
        ns1.a(i1 + f4, j1 + f2 + f5, k1 + 1, d3, d5);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 1, d3, d7);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 0, d1, d7);
        ns1.a(i1 + f4, j1 + f2 + f5, k1 + 0, d1, d5);
        ns1.a(i1 + f4, j1 + f2 + f5, k1 + 0, d1, d5);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 0, d1, d7);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 1, d3, d7);
        ns1.a(i1 + f4, j1 + f2 + f5, k1 + 1, d3, d5);
      }
      if (un.as.c(b, i1 + 1, j1, k1)) {
        ns1.a(i1 + 1 - f4, j1 + f2 + f5, k1 + 0, d1, d5);
        ns1.a(i1 + 1 - 0, j1 + 0 + f5, k1 + 0, d1, d7);
        ns1.a(i1 + 1 - 0, j1 + 0 + f5, k1 + 1, d3, d7);
        ns1.a(i1 + 1 - f4, j1 + f2 + f5, k1 + 1, d3, d5);
        ns1.a(i1 + 1 - f4, j1 + f2 + f5, k1 + 1, d3, d5);
        ns1.a(i1 + 1 - 0, j1 + 0 + f5, k1 + 1, d3, d7);
        ns1.a(i1 + 1 - 0, j1 + 0 + f5, k1 + 0, d1, d7);
        ns1.a(i1 + 1 - f4, j1 + f2 + f5, k1 + 0, d1, d5);
      }
      if (un.as.c(b, i1, j1, k1 - 1)) {
        ns1.a(i1 + 0, j1 + f2 + f5, k1 + f4, d3, d5);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 0, d3, d7);
        ns1.a(i1 + 1, j1 + 0 + f5, k1 + 0, d1, d7);
        ns1.a(i1 + 1, j1 + f2 + f5, k1 + f4, d1, d5);
        ns1.a(i1 + 1, j1 + f2 + f5, k1 + f4, d1, d5);
        ns1.a(i1 + 1, j1 + 0 + f5, k1 + 0, d1, d7);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 0, d3, d7);
        ns1.a(i1 + 0, j1 + f2 + f5, k1 + f4, d3, d5);
      }
      if (un.as.c(b, i1, j1, k1 + 1)) {
        ns1.a(i1 + 1, j1 + f2 + f5, k1 + 1 - f4, d1, d5);
        ns1.a(i1 + 1, j1 + 0 + f5, k1 + 1 - 0, d1, d7);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 1 - 0, d3, d7);
        ns1.a(i1 + 0, j1 + f2 + f5, k1 + 1 - f4, d3, d5);
        ns1.a(i1 + 0, j1 + f2 + f5, k1 + 1 - f4, d3, d5);
        ns1.a(i1 + 0, j1 + 0 + f5, k1 + 1 - 0, d3, d7);
        ns1.a(i1 + 1, j1 + 0 + f5, k1 + 1 - 0, d1, d7);
        ns1.a(i1 + 1, j1 + f2 + f5, k1 + 1 - f4, d1, d5);
      }
      if (un.as.c(b, i1, j1 + 1, k1)) {
        double d12 = i1 + 0.5D + 0.5D;
        double d14 = i1 + 0.5D - 0.5D;
        double d16 = k1 + 0.5D + 0.5D;
        double d18 = k1 + 0.5D - 0.5D;
        double d20 = i1 + 0.5D - 0.5D;
        double d22 = i1 + 0.5D + 0.5D;
        double d24 = k1 + 0.5D - 0.5D;
        double d25 = k1 + 0.5D + 0.5D;
        double d2 = i2 / 256.0F;
        double d4 = (i2 + 15.99F) / 256.0F;
        double d6 = j2 / 256.0F;
        double d8 = (j2 + 15.99F) / 256.0F;
        j1++;
        float f3 = -0.2F;
        if ((i1 + j1 + k1 & 0x1) == 0) {
          ns1.a(d20, j1 + f3, k1 + 0, d4, d6);
          ns1.a(d12, j1 + 0, k1 + 0, d4, d8);
          ns1.a(d12, j1 + 0, k1 + 1, d2, d8);
          ns1.a(d20, j1 + f3, k1 + 1, d2, d6);
          d2 = i2 / 256.0F;
          d4 = (i2 + 15.99F) / 256.0F;
          d6 = (j2 + 16) / 256.0F;
          d8 = (j2 + 15.99F + 16.0F) / 256.0F;
          ns1.a(d22, j1 + f3, k1 + 1, d4, d6);
          ns1.a(d14, j1 + 0, k1 + 1, d4, d8);
          ns1.a(d14, j1 + 0, k1 + 0, d2, d8);
          ns1.a(d22, j1 + f3, k1 + 0, d2, d6);
        } else {
          ns1.a(i1 + 0, j1 + f3, d25, d4, d6);
          ns1.a(i1 + 0, j1 + 0, d18, d4, d8);
          ns1.a(i1 + 1, j1 + 0, d18, d2, d8);
          ns1.a(i1 + 1, j1 + f3, d25, d2, d6);
          d2 = i2 / 256.0F;
          d4 = (i2 + 15.99F) / 256.0F;
          d6 = (j2 + 16) / 256.0F;
          d8 = (j2 + 15.99F + 16.0F) / 256.0F;
          ns1.a(i1 + 1, j1 + f3, d24, d4, d6);
          ns1.a(i1 + 1, j1 + 0, d16, d4, d8);
          ns1.a(i1 + 0, j1 + 0, d16, d2, d8);
          ns1.a(i1 + 0, j1 + f3, d24, d2, d6);
        }
      }
    }
    return true;
  }

  public static void setRedstoneColors(float[][] colors)
  {
    if (colors.length != 16) {
      throw new IllegalArgumentException("Must be 16 colors.");
    }
    for (int i = 0; i < colors.length; i++) {
      if (colors[i].length != 3)
        throw new IllegalArgumentException("Must be 3 channels in a color.");
    }
    redstoneColors = colors;
  }

  public boolean e(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    int l1 = b.e(i1, j1, k1);
    int i2 = un1.a(1, l1);
    if (c >= 0)
      i2 = c;
    float f1 = un1.d(b, i1, j1, k1);

    float[] color = redstoneColors[l1];
    float f3 = color[0];
    float f4 = color[1];
    float f5 = color[2];
    ns1.a(f1 * f3, f1 * f4, f1 * f5);
    int j2 = (i2 & 0xF) << 4;
    int k2 = i2 & 0xF0;
    double d1 = j2 / 256.0F;
    double d3 = (j2 + 15.99F) / 256.0F;
    double d5 = k2 / 256.0F;
    double d7 = (k2 + 15.99F) / 256.0F;
    boolean flag = (sh.c(b, i1 - 1, j1, k1)) || ((!b.h(i1 - 1, j1, k1)) && (sh.c(b, i1 - 1, j1 - 1, k1)));
    boolean flag1 = (sh.c(b, i1 + 1, j1, k1)) || ((!b.h(i1 + 1, j1, k1)) && (sh.c(b, i1 + 1, j1 - 1, k1)));
    boolean flag2 = (sh.c(b, i1, j1, k1 - 1)) || ((!b.h(i1, j1, k1 - 1)) && (sh.c(b, i1, j1 - 1, k1 - 1)));
    boolean flag3 = (sh.c(b, i1, j1, k1 + 1)) || ((!b.h(i1, j1, k1 + 1)) && (sh.c(b, i1, j1 - 1, k1 + 1)));
    if (!b.h(i1, j1 + 1, k1)) {
      if ((b.h(i1 - 1, j1, k1)) && (sh.c(b, i1 - 1, j1 + 1, k1)))
        flag = true;
      if ((b.h(i1 + 1, j1, k1)) && (sh.c(b, i1 + 1, j1 + 1, k1)))
        flag1 = true;
      if ((b.h(i1, j1, k1 - 1)) && (sh.c(b, i1, j1 + 1, k1 - 1)))
        flag2 = true;
      if ((b.h(i1, j1, k1 + 1)) && (sh.c(b, i1, j1 + 1, k1 + 1)))
        flag3 = true;
    }
    float f6 = i1 + 0;
    float f7 = i1 + 1;
    float f8 = k1 + 0;
    float f9 = k1 + 1;
    byte byte0 = 0;
    if (((flag) || (flag1)) && (!flag2) && (!flag3))
      byte0 = 1;
    if (((flag2) || (flag3)) && (!flag1) && (!flag))
      byte0 = 2;
    if (byte0 != 0) {
      d1 = (j2 + 16) / 256.0F;
      d3 = (j2 + 16 + 15.99F) / 256.0F;
      d5 = k2 / 256.0F;
      d7 = (k2 + 15.99F) / 256.0F;
    }
    if (byte0 == 0) {
      if ((flag1) || (flag2) || (flag3) || (flag)) {
        if (!flag)
          f6 += 0.3125F;
        if (!flag)
          d1 += 0.01953125D;
        if (!flag1)
          f7 -= 0.3125F;
        if (!flag1)
          d3 -= 0.01953125D;
        if (!flag2)
          f8 += 0.3125F;
        if (!flag2)
          d5 += 0.01953125D;
        if (!flag3)
          f9 -= 0.3125F;
        if (!flag3)
          d7 -= 0.01953125D;
      }
      ns1.a(f7, j1 + 0.015625F, f9, d3, d7);
      ns1.a(f7, j1 + 0.015625F, f8, d3, d5);
      ns1.a(f6, j1 + 0.015625F, f8, d1, d5);
      ns1.a(f6, j1 + 0.015625F, f9, d1, d7);
      ns1.a(f1, f1, f1);
      ns1.a(f7, j1 + 0.015625F, f9, d3, d7 + 0.0625D);
      ns1.a(f7, j1 + 0.015625F, f8, d3, d5 + 0.0625D);
      ns1.a(f6, j1 + 0.015625F, f8, d1, d5 + 0.0625D);
      ns1.a(f6, j1 + 0.015625F, f9, d1, d7 + 0.0625D);
    } else if (byte0 == 1) {
      ns1.a(f7, j1 + 0.015625F, f9, d3, d7);
      ns1.a(f7, j1 + 0.015625F, f8, d3, d5);
      ns1.a(f6, j1 + 0.015625F, f8, d1, d5);
      ns1.a(f6, j1 + 0.015625F, f9, d1, d7);
      ns1.a(f1, f1, f1);
      ns1.a(f7, j1 + 0.015625F, f9, d3, d7 + 0.0625D);
      ns1.a(f7, j1 + 0.015625F, f8, d3, d5 + 0.0625D);
      ns1.a(f6, j1 + 0.015625F, f8, d1, d5 + 0.0625D);
      ns1.a(f6, j1 + 0.015625F, f9, d1, d7 + 0.0625D);
    } else if (byte0 == 2) {
      ns1.a(f7, j1 + 0.015625F, f9, d3, d7);
      ns1.a(f7, j1 + 0.015625F, f8, d1, d7);
      ns1.a(f6, j1 + 0.015625F, f8, d1, d5);
      ns1.a(f6, j1 + 0.015625F, f9, d3, d5);
      ns1.a(f1, f1, f1);
      ns1.a(f7, j1 + 0.015625F, f9, d3, d7 + 0.0625D);
      ns1.a(f7, j1 + 0.015625F, f8, d1, d7 + 0.0625D);
      ns1.a(f6, j1 + 0.015625F, f8, d1, d5 + 0.0625D);
      ns1.a(f6, j1 + 0.015625F, f9, d3, d5 + 0.0625D);
    }
    if (!b.h(i1, j1 + 1, k1)) {
      double d2 = (j2 + 16) / 256.0F;
      double d4 = (j2 + 16 + 15.99F) / 256.0F;
      double d6 = k2 / 256.0F;
      double d8 = (k2 + 15.99F) / 256.0F;
      if ((b.h(i1 - 1, j1, k1)) && (b.a(i1 - 1, j1 + 1, k1) == un.aw.bn)) {
        ns1.a(f1 * f3, f1 * f4, f1 * f5);
        ns1.a(i1 + 0.015625F, j1 + 1 + 0.021875F, k1 + 1, d4, d6);
        ns1.a(i1 + 0.015625F, j1 + 0, k1 + 1, d2, d6);
        ns1.a(i1 + 0.015625F, j1 + 0, k1 + 0, d2, d8);
        ns1.a(i1 + 0.015625F, j1 + 1 + 0.021875F, k1 + 0, d4, d8);
        ns1.a(f1, f1, f1);
        ns1.a(i1 + 0.015625F, j1 + 1 + 0.021875F, k1 + 1, d4, d6 + 0.0625D);
        ns1.a(i1 + 0.015625F, j1 + 0, k1 + 1, d2, d6 + 0.0625D);
        ns1.a(i1 + 0.015625F, j1 + 0, k1 + 0, d2, d8 + 0.0625D);
        ns1.a(i1 + 0.015625F, j1 + 1 + 0.021875F, k1 + 0, d4, d8 + 0.0625D);
      }
      if ((b.h(i1 + 1, j1, k1)) && (b.a(i1 + 1, j1 + 1, k1) == un.aw.bn)) {
        ns1.a(f1 * f3, f1 * f4, f1 * f5);
        ns1.a(i1 + 1 - 0.015625F, j1 + 0, k1 + 1, d2, d8);
        ns1.a(i1 + 1 - 0.015625F, j1 + 1 + 0.021875F, k1 + 1, d4, d8);
        ns1.a(i1 + 1 - 0.015625F, j1 + 1 + 0.021875F, k1 + 0, d4, d6);
        ns1.a(i1 + 1 - 0.015625F, j1 + 0, k1 + 0, d2, d6);
        ns1.a(f1, f1, f1);
        ns1.a(i1 + 1 - 0.015625F, j1 + 0, k1 + 1, d2, d8 + 0.0625D);
        ns1.a(i1 + 1 - 0.015625F, j1 + 1 + 0.021875F, k1 + 1, d4, d8 + 0.0625D);
        ns1.a(i1 + 1 - 0.015625F, j1 + 1 + 0.021875F, k1 + 0, d4, d6 + 0.0625D);
        ns1.a(i1 + 1 - 0.015625F, j1 + 0, k1 + 0, d2, d6 + 0.0625D);
      }
      if ((b.h(i1, j1, k1 - 1)) && (b.a(i1, j1 + 1, k1 - 1) == un.aw.bn)) {
        ns1.a(f1 * f3, f1 * f4, f1 * f5);
        ns1.a(i1 + 1, j1 + 0, k1 + 0.015625F, d2, d8);
        ns1.a(i1 + 1, j1 + 1 + 0.021875F, k1 + 0.015625F, d4, d8);
        ns1.a(i1 + 0, j1 + 1 + 0.021875F, k1 + 0.015625F, d4, d6);
        ns1.a(i1 + 0, j1 + 0, k1 + 0.015625F, d2, d6);
        ns1.a(f1, f1, f1);
        ns1.a(i1 + 1, j1 + 0, k1 + 0.015625F, d2, d8 + 0.0625D);
        ns1.a(i1 + 1, j1 + 1 + 0.021875F, k1 + 0.015625F, d4, d8 + 0.0625D);
        ns1.a(i1 + 0, j1 + 1 + 0.021875F, k1 + 0.015625F, d4, d6 + 0.0625D);
        ns1.a(i1 + 0, j1 + 0, k1 + 0.015625F, d2, d6 + 0.0625D);
      }
      if ((b.h(i1, j1, k1 + 1)) && (b.a(i1, j1 + 1, k1 + 1) == un.aw.bn)) {
        ns1.a(f1 * f3, f1 * f4, f1 * f5);
        ns1.a(i1 + 1, j1 + 1 + 0.021875F, k1 + 1 - 0.015625F, d4, d6);
        ns1.a(i1 + 1, j1 + 0, k1 + 1 - 0.015625F, d2, d6);
        ns1.a(i1 + 0, j1 + 0, k1 + 1 - 0.015625F, d2, d8);
        ns1.a(i1 + 0, j1 + 1 + 0.021875F, k1 + 1 - 0.015625F, d4, d8);
        ns1.a(f1, f1, f1);
        ns1.a(i1 + 1, j1 + 1 + 0.021875F, k1 + 1 - 0.015625F, d4, d6 + 0.0625D);
        ns1.a(i1 + 1, j1 + 0, k1 + 1 - 0.015625F, d2, d6 + 0.0625D);
        ns1.a(i1 + 0, j1 + 0, k1 + 1 - 0.015625F, d2, d8 + 0.0625D);
        ns1.a(i1 + 0, j1 + 1 + 0.021875F, k1 + 1 - 0.015625F, d4, d8 + 0.0625D);
      }
    }
    return true;
  }

  public boolean a(oy oy1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    int l1 = b.e(i1, j1, k1);
    int i2 = oy1.a(0, l1);
    if (c >= 0)
      i2 = c;
    if (oy1.h())
      l1 &= 7;
    float f1 = oy1.d(b, i1, j1, k1);
    ns1.a(f1, f1, f1);
    int j2 = (i2 & 0xF) << 4;
    int k2 = i2 & 0xF0;
    double d1 = j2 / 256.0F;
    double d2 = (j2 + 15.99F) / 256.0F;
    double d3 = k2 / 256.0F;
    double d4 = (k2 + 15.99F) / 256.0F;
    float f2 = 0.0625F;
    float f3 = i1 + 1;
    float f4 = i1 + 1;
    float f5 = i1 + 0;
    float f6 = i1 + 0;
    float f7 = k1 + 0;
    float f8 = k1 + 1;
    float f9 = k1 + 1;
    float f10 = k1 + 0;
    float f11 = j1 + f2;
    float f12 = j1 + f2;
    float f13 = j1 + f2;
    float f14 = j1 + f2;
    if ((l1 == 1) || (l1 == 2) || (l1 == 3) || (l1 == 7)) {
      f3 = f6 = i1 + 1;
      f4 = f5 = i1 + 0;
      f7 = f8 = k1 + 1;
      f9 = f10 = k1 + 0;
    } else if (l1 == 8) {
      f3 = f4 = i1 + 0;
      f5 = f6 = i1 + 1;
      f7 = f10 = k1 + 1;
      f8 = f9 = k1 + 0;
    } else if (l1 == 9) {
      f3 = f6 = i1 + 0;
      f4 = f5 = i1 + 1;
      f7 = f8 = k1 + 0;
      f9 = f10 = k1 + 1;
    }
    if ((l1 == 2) || (l1 == 4)) {
      f11 += 1.0F;
      f14 += 1.0F;
    } else if ((l1 == 3) || (l1 == 5)) {
      f12 += 1.0F;
      f13 += 1.0F;
    }
    ns1.a(f3, f11, f7, d2, d3);
    ns1.a(f4, f12, f8, d2, d4);
    ns1.a(f5, f13, f9, d1, d4);
    ns1.a(f6, f14, f10, d1, d3);
    ns1.a(f6, f14, f10, d1, d3);
    ns1.a(f5, f13, f9, d1, d4);
    ns1.a(f4, f12, f8, d2, d4);
    ns1.a(f3, f11, f7, d2, d3);
    return true;
  }

  public boolean f(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    int l1 = un1.a(0);
    if (c >= 0)
      l1 = c;
    float f1 = un1.d(b, i1, j1, k1);
    ns1.a(f1, f1, f1);
    int i2 = (l1 & 0xF) << 4;
    int j2 = l1 & 0xF0;
    double d1 = i2 / 256.0F;
    double d2 = (i2 + 15.99F) / 256.0F;
    double d3 = j2 / 256.0F;
    double d4 = (j2 + 15.99F) / 256.0F;
    int k2 = b.e(i1, j1, k1);
    float f2 = 0.0F;
    float f3 = 0.05F;
    if (k2 == 5) {
      ns1.a(i1 + f3, j1 + 1 + f2, k1 + 1 + f2, d1, d3);
      ns1.a(i1 + f3, j1 + 0 - f2, k1 + 1 + f2, d1, d4);
      ns1.a(i1 + f3, j1 + 0 - f2, k1 + 0 - f2, d2, d4);
      ns1.a(i1 + f3, j1 + 1 + f2, k1 + 0 - f2, d2, d3);
    }
    if (k2 == 4) {
      ns1.a(i1 + 1 - f3, j1 + 0 - f2, k1 + 1 + f2, d2, d4);
      ns1.a(i1 + 1 - f3, j1 + 1 + f2, k1 + 1 + f2, d2, d3);
      ns1.a(i1 + 1 - f3, j1 + 1 + f2, k1 + 0 - f2, d1, d3);
      ns1.a(i1 + 1 - f3, j1 + 0 - f2, k1 + 0 - f2, d1, d4);
    }
    if (k2 == 3) {
      ns1.a(i1 + 1 + f2, j1 + 0 - f2, k1 + f3, d2, d4);
      ns1.a(i1 + 1 + f2, j1 + 1 + f2, k1 + f3, d2, d3);
      ns1.a(i1 + 0 - f2, j1 + 1 + f2, k1 + f3, d1, d3);
      ns1.a(i1 + 0 - f2, j1 + 0 - f2, k1 + f3, d1, d4);
    }
    if (k2 == 2) {
      ns1.a(i1 + 1 + f2, j1 + 1 + f2, k1 + 1 - f3, d1, d3);
      ns1.a(i1 + 1 + f2, j1 + 0 - f2, k1 + 1 - f3, d1, d4);
      ns1.a(i1 + 0 - f2, j1 + 0 - f2, k1 + 1 - f3, d2, d4);
      ns1.a(i1 + 0 - f2, j1 + 1 + f2, k1 + 1 - f3, d2, d3);
    }
    return true;
  }

  public boolean g(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    float f1 = un1.d(b, i1, j1, k1);
    int l1 = un1.b(b, i1, j1, k1);
    float f2 = (l1 >> 16 & 0xFF) / 255.0F;
    float f3 = (l1 >> 8 & 0xFF) / 255.0F;
    float f4 = (l1 & 0xFF) / 255.0F;
    if (pt.a) {
      float f5 = (f2 * 30.0F + f3 * 59.0F + f4 * 11.0F) / 100.0F;
      float f6 = (f2 * 30.0F + f3 * 70.0F) / 100.0F;
      float f7 = (f2 * 30.0F + f4 * 70.0F) / 100.0F;
      f2 = f5;
      f3 = f6;
      f4 = f7;
    }
    ns1.a(f1 * f2, f1 * f3, f1 * f4);
    double d1 = i1;
    double d2 = j1;
    double d3 = k1;
    if (un1 == un.Y) {
      long l2 = i1 * 3129871 ^ k1 * 116129781L ^ j1;
      l2 = l2 * l2 * 42317861L + l2 * 11L;
      d1 += ((float)(l2 >> 16 & 0xF) / 15.0F - 0.5D) * 0.5D;
      d2 += ((float)(l2 >> 20 & 0xF) / 15.0F - 1.0D) * 0.2D;
      d3 += ((float)(l2 >> 24 & 0xF) / 15.0F - 0.5D) * 0.5D;
    }
    a(un1, b.e(i1, j1, k1), d1, d2, d3);
    return true;
  }

  public boolean h(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    float f1 = un1.d(b, i1, j1, k1);
    ns1.a(f1, f1, f1);
    b(un1, b.e(i1, j1, k1), i1, j1 - 0.0625D, k1);
    return true;
  }

  public void a(un un1, double d1, double d2, double d3, double d4, double d5) {
    ns ns1 = ns.a;
    int i1 = un1.a(0);
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    float f1 = j1 / 256.0F;
    float f2 = (j1 + 15.99F) / 256.0F;
    float f3 = k1 / 256.0F;
    float f4 = (k1 + 15.99F) / 256.0F;
    double d6 = f1 + 0.02734375D;
    double d7 = f3 + 0.0234375D;
    double d8 = f1 + 0.03515625D;
    double d9 = f3 + 0.03125D;
    d1 += 0.5D;
    d3 += 0.5D;
    double d10 = d1 - 0.5D;
    double d11 = d1 + 0.5D;
    double d12 = d3 - 0.5D;
    double d13 = d3 + 0.5D;
    double d14 = 0.0625D;
    double d15 = 0.625D;
    ns1.a(d1 + d4 * (1.0D - d15) - d14, d2 + d15, d3 + d5 * (1.0D - d15) - d14, d6, d7);
    ns1.a(d1 + d4 * (1.0D - d15) - d14, d2 + d15, d3 + d5 * (1.0D - d15) + d14, d6, d9);
    ns1.a(d1 + d4 * (1.0D - d15) + d14, d2 + d15, d3 + d5 * (1.0D - d15) + d14, d8, d9);
    ns1.a(d1 + d4 * (1.0D - d15) + d14, d2 + d15, d3 + d5 * (1.0D - d15) - d14, d8, d7);
    ns1.a(d1 - d14, d2 + 1.0D, d12, f1, f3);
    ns1.a(d1 - d14 + d4, d2 + 0.0D, d12 + d5, f1, f4);
    ns1.a(d1 - d14 + d4, d2 + 0.0D, d13 + d5, f2, f4);
    ns1.a(d1 - d14, d2 + 1.0D, d13, f2, f3);
    ns1.a(d1 + d14, d2 + 1.0D, d13, f1, f3);
    ns1.a(d1 + d4 + d14, d2 + 0.0D, d13 + d5, f1, f4);
    ns1.a(d1 + d4 + d14, d2 + 0.0D, d12 + d5, f2, f4);
    ns1.a(d1 + d14, d2 + 1.0D, d12, f2, f3);
    ns1.a(d10, d2 + 1.0D, d3 + d14, f1, f3);
    ns1.a(d10 + d4, d2 + 0.0D, d3 + d14 + d5, f1, f4);
    ns1.a(d11 + d4, d2 + 0.0D, d3 + d14 + d5, f2, f4);
    ns1.a(d11, d2 + 1.0D, d3 + d14, f2, f3);
    ns1.a(d11, d2 + 1.0D, d3 - d14, f1, f3);
    ns1.a(d11 + d4, d2 + 0.0D, d3 - d14 + d5, f1, f4);
    ns1.a(d10 + d4, d2 + 0.0D, d3 - d14 + d5, f2, f4);
    ns1.a(d10, d2 + 1.0D, d3 - d14, f2, f3);
  }

  public void a(un un1, int i1, double d1, double d2, double d3) {
    ns ns1 = ns.a;
    int j1 = un1.a(0, i1);
    if (c >= 0)
      j1 = c;
    int k1 = (j1 & 0xF) << 4;
    int l1 = j1 & 0xF0;
    double d4 = k1 / 256.0F;
    double d5 = (k1 + 15.99F) / 256.0F;
    double d6 = l1 / 256.0F;
    double d7 = (l1 + 15.99F) / 256.0F;
    double d8 = d1 + 0.5D - 0.449999988079071D;
    double d9 = d1 + 0.5D + 0.449999988079071D;
    double d10 = d3 + 0.5D - 0.449999988079071D;
    double d11 = d3 + 0.5D + 0.449999988079071D;
    ns1.a(d8, d2 + 1.0D, d10, d4, d6);
    ns1.a(d8, d2 + 0.0D, d10, d4, d7);
    ns1.a(d9, d2 + 0.0D, d11, d5, d7);
    ns1.a(d9, d2 + 1.0D, d11, d5, d6);
    ns1.a(d9, d2 + 1.0D, d11, d4, d6);
    ns1.a(d9, d2 + 0.0D, d11, d4, d7);
    ns1.a(d8, d2 + 0.0D, d10, d5, d7);
    ns1.a(d8, d2 + 1.0D, d10, d5, d6);
    ns1.a(d8, d2 + 1.0D, d11, d4, d6);
    ns1.a(d8, d2 + 0.0D, d11, d4, d7);
    ns1.a(d9, d2 + 0.0D, d10, d5, d7);
    ns1.a(d9, d2 + 1.0D, d10, d5, d6);
    ns1.a(d9, d2 + 1.0D, d10, d4, d6);
    ns1.a(d9, d2 + 0.0D, d10, d4, d7);
    ns1.a(d8, d2 + 0.0D, d11, d5, d7);
    ns1.a(d8, d2 + 1.0D, d11, d5, d6);
  }

  public void b(un un1, int i1, double d1, double d2, double d3) {
    ns ns1 = ns.a;
    int j1 = un1.a(0, i1);
    if (c >= 0)
      j1 = c;
    int k1 = (j1 & 0xF) << 4;
    int l1 = j1 & 0xF0;
    double d4 = k1 / 256.0F;
    double d5 = (k1 + 15.99F) / 256.0F;
    double d6 = l1 / 256.0F;
    double d7 = (l1 + 15.99F) / 256.0F;
    double d8 = d1 + 0.5D - 0.25D;
    double d9 = d1 + 0.5D + 0.25D;
    double d10 = d3 + 0.5D - 0.5D;
    double d11 = d3 + 0.5D + 0.5D;
    ns1.a(d8, d2 + 1.0D, d10, d4, d6);
    ns1.a(d8, d2 + 0.0D, d10, d4, d7);
    ns1.a(d8, d2 + 0.0D, d11, d5, d7);
    ns1.a(d8, d2 + 1.0D, d11, d5, d6);
    ns1.a(d8, d2 + 1.0D, d11, d4, d6);
    ns1.a(d8, d2 + 0.0D, d11, d4, d7);
    ns1.a(d8, d2 + 0.0D, d10, d5, d7);
    ns1.a(d8, d2 + 1.0D, d10, d5, d6);
    ns1.a(d9, d2 + 1.0D, d11, d4, d6);
    ns1.a(d9, d2 + 0.0D, d11, d4, d7);
    ns1.a(d9, d2 + 0.0D, d10, d5, d7);
    ns1.a(d9, d2 + 1.0D, d10, d5, d6);
    ns1.a(d9, d2 + 1.0D, d10, d4, d6);
    ns1.a(d9, d2 + 0.0D, d10, d4, d7);
    ns1.a(d9, d2 + 0.0D, d11, d5, d7);
    ns1.a(d9, d2 + 1.0D, d11, d5, d6);
    d8 = d1 + 0.5D - 0.5D;
    d9 = d1 + 0.5D + 0.5D;
    d10 = d3 + 0.5D - 0.25D;
    d11 = d3 + 0.5D + 0.25D;
    ns1.a(d8, d2 + 1.0D, d10, d4, d6);
    ns1.a(d8, d2 + 0.0D, d10, d4, d7);
    ns1.a(d9, d2 + 0.0D, d10, d5, d7);
    ns1.a(d9, d2 + 1.0D, d10, d5, d6);
    ns1.a(d9, d2 + 1.0D, d10, d4, d6);
    ns1.a(d9, d2 + 0.0D, d10, d4, d7);
    ns1.a(d8, d2 + 0.0D, d10, d5, d7);
    ns1.a(d8, d2 + 1.0D, d10, d5, d6);
    ns1.a(d9, d2 + 1.0D, d11, d4, d6);
    ns1.a(d9, d2 + 0.0D, d11, d4, d7);
    ns1.a(d8, d2 + 0.0D, d11, d5, d7);
    ns1.a(d8, d2 + 1.0D, d11, d5, d6);
    ns1.a(d8, d2 + 1.0D, d11, d4, d6);
    ns1.a(d8, d2 + 0.0D, d11, d4, d7);
    ns1.a(d9, d2 + 0.0D, d11, d5, d7);
    ns1.a(d9, d2 + 1.0D, d11, d5, d6);
  }

  public boolean i(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    int l1 = un1.b(b, i1, j1, k1);
    float f1 = (l1 >> 16 & 0xFF) / 255.0F;
    float f2 = (l1 >> 8 & 0xFF) / 255.0F;
    float f3 = (l1 & 0xFF) / 255.0F;
    boolean flag = un1.b(b, i1, j1 + 1, k1, 1);
    boolean flag1 = un1.b(b, i1, j1 - 1, k1, 0);
    boolean[] aflag = new boolean[4];
    aflag[0] = un1.b(b, i1, j1, k1 - 1, 2);
    aflag[1] = un1.b(b, i1, j1, k1 + 1, 3);
    aflag[2] = un1.b(b, i1 - 1, j1, k1, 4);
    aflag[3] = un1.b(b, i1 + 1, j1, k1, 5);
    if ((!flag) && (!flag1) && (aflag[0] == 0) && (aflag[1] == 0) && (aflag[2] == 0) && (aflag[3] == 0))
      return false;
    boolean flag2 = false;
    float f4 = 0.5F;
    float f5 = 1.0F;
    float f6 = 0.8F;
    float f7 = 0.6F;
    double d1 = 0.0D;
    double d2 = 1.0D;
    lj lj1 = un1.bA;
    int i2 = b.e(i1, j1, k1);
    float f8 = a(i1, j1, k1, lj1);
    float f9 = a(i1, j1, k1 + 1, lj1);
    float f10 = a(i1 + 1, j1, k1 + 1, lj1);
    float f11 = a(i1 + 1, j1, k1, lj1);
    if ((e) || (flag)) {
      flag2 = true;
      int j2 = un1.a(1, i2);
      float f13 = (float)rk.a(b, i1, j1, k1, lj1);
      if (f13 > -999.0F)
        j2 = un1.a(2, i2);
      int i3 = (j2 & 0xF) << 4;
      int k3 = j2 & 0xF0;
      double d3 = (i3 + 8.0D) / 256.0D;
      double d4 = (k3 + 8.0D) / 256.0D;
      if (f13 < -999.0F) {
        f13 = 0.0F;
      } else {
        d3 = (i3 + 16) / 256.0F;
        d4 = (k3 + 16) / 256.0F;
      }
      float f15 = ik.a(f13) * 8.0F / 256.0F;
      float f17 = ik.b(f13) * 8.0F / 256.0F;
      float f19 = un1.d(b, i1, j1, k1);
      ns1.a(f5 * f19 * f1, f5 * f19 * f2, f5 * f19 * f3);
      ns1.a(i1 + 0, j1 + f8, k1 + 0, d3 - f17 - f15, d4 - f17 + f15);
      ns1.a(i1 + 0, j1 + f9, k1 + 1, d3 - f17 + f15, d4 + f17 + f15);
      ns1.a(i1 + 1, j1 + f10, k1 + 1, d3 + f17 + f15, d4 + f17 - f15);
      ns1.a(i1 + 1, j1 + f11, k1 + 0, d3 + f17 - f15, d4 - f17 - f15);
    }
    if ((e) || (flag1)) {
      float f12 = un1.d(b, i1, j1 - 1, k1);
      ns1.a(f4 * f12, f4 * f12, f4 * f12);
      a(un1, i1, j1, k1, un1.a(0));
      flag2 = true;
    }
    for (int k2 = 0; k2 < 4; k2++) {
      int l2 = i1;
      int j3 = j1;
      int l3 = k1;
      if (k2 == 0)
        l3--;
      if (k2 == 1)
        l3++;
      if (k2 == 2)
        l2--;
      if (k2 == 3)
        l2++;
      int i4 = un1.a(k2 + 2, i2);
      int j4 = (i4 & 0xF) << 4;
      int k4 = i4 & 0xF0;
      if ((!e) && (aflag[k2] == 0))
        continue;
      float f22;
      float f14;
      float f16;
      float f18;
      float f21;
      float f20;
      float f22;
      if (k2 == 0) {
        float f14 = f8;
        float f16 = f11;
        float f18 = i1;
        float f21 = i1 + 1;
        float f20 = k1;
        f22 = k1;
      }
      else
      {
        float f22;
        if (k2 == 1) {
          float f14 = f10;
          float f16 = f9;
          float f18 = i1 + 1;
          float f21 = i1;
          float f20 = k1 + 1;
          f22 = k1 + 1;
        }
        else
        {
          float f22;
          if (k2 == 2) {
            float f14 = f9;
            float f16 = f8;
            float f18 = i1;
            float f21 = i1;
            float f20 = k1 + 1;
            f22 = k1;
          } else {
            f14 = f11;
            f16 = f10;
            f18 = i1 + 1;
            f21 = i1 + 1;
            f20 = k1;
            f22 = k1 + 1;
          }
        }
      }
      flag2 = true;
      double d5 = (j4 + 0) / 256.0F;
      double d6 = (j4 + 16 - 0.01D) / 256.0D;
      double d7 = (k4 + (1.0F - f14) * 16.0F) / 256.0F;
      double d8 = (k4 + (1.0F - f16) * 16.0F) / 256.0F;
      double d9 = (k4 + 16 - 0.01D) / 256.0D;
      float f23 = un1.d(b, l2, j3, l3);
      if (k2 < 2) f23 *= f6; else
        f23 *= f7;
      ns1.a(f5 * f23 * f1, f5 * f23 * f2, f5 * f23 * f3);
      ns1.a(f18, j1 + f14, f20, d5, d7);
      ns1.a(f21, j1 + f16, f22, d6, d8);
      ns1.a(f21, j1 + 0, f22, d6, d9);
      ns1.a(f18, j1 + 0, f20, d5, d9);
    }

    un1.bt = d1;
    un1.bw = d2;
    return flag2;
  }

  public float a(int i1, int j1, int k1, lj lj1) {
    int l1 = 0;
    float f1 = 0.0F;
    for (int i2 = 0; i2 < 4; i2++) {
      int j2 = i1 - (i2 & 0x1);
      int k2 = j1;
      int l2 = k1 - (i2 >> 1 & 0x1);
      if (b.f(j2, k2 + 1, l2) == lj1)
        return 1.0F;
      lj lj2 = b.f(j2, k2, l2);
      if (lj2 == lj1) {
        int i3 = b.e(j2, k2, l2);
        if ((i3 >= 8) || (i3 == 0)) {
          f1 += rk.c(i3) * 10.0F;
          l1 += 10;
        }
        f1 += rk.c(i3);
        l1++;
      }
      else if (!lj2.a()) {
        f1 += 1.0F;
        l1++;
      }
    }

    return 1.0F - f1 / l1;
  }

  public void a(un un1, fb fb, int i1, int j1, int k1) {
    float f1 = 0.5F;
    float f2 = 1.0F;
    float f3 = 0.8F;
    float f4 = 0.6F;
    ns ns1 = ns.a;
    ns1.b();
    float f5 = un1.d(fb, i1, j1, k1);
    float f6 = un1.d(fb, i1, j1 - 1, k1);
    if (f6 < f5)
      f6 = f5;
    ns1.a(f1 * f6, f1 * f6, f1 * f6);
    a(un1, -0.5D, -0.5D, -0.5D, un1.a(0));
    f6 = un1.d(fb, i1, j1 + 1, k1);
    if (f6 < f5)
      f6 = f5;
    ns1.a(f2 * f6, f2 * f6, f2 * f6);
    b(un1, -0.5D, -0.5D, -0.5D, un1.a(1));
    f6 = un1.d(fb, i1, j1, k1 - 1);
    if (f6 < f5)
      f6 = f5;
    ns1.a(f3 * f6, f3 * f6, f3 * f6);
    c(un1, -0.5D, -0.5D, -0.5D, un1.a(2));
    f6 = un1.d(fb, i1, j1, k1 + 1);
    if (f6 < f5)
      f6 = f5;
    ns1.a(f3 * f6, f3 * f6, f3 * f6);
    d(un1, -0.5D, -0.5D, -0.5D, un1.a(3));
    f6 = un1.d(fb, i1 - 1, j1, k1);
    if (f6 < f5)
      f6 = f5;
    ns1.a(f4 * f6, f4 * f6, f4 * f6);
    e(un1, -0.5D, -0.5D, -0.5D, un1.a(4));
    f6 = un1.d(fb, i1 + 1, j1, k1);
    if (f6 < f5)
      f6 = f5;
    ns1.a(f4 * f6, f4 * f6, f4 * f6);
    f(un1, -0.5D, -0.5D, -0.5D, un1.a(5));
    ns1.a();
  }

  public boolean j(un un1, int i1, int j1, int k1) {
    int l1 = un1.b(b, i1, j1, k1);

    float f1 = (l1 >> 16 & 0xFF) / 255.0F;
    float f2 = (l1 >> 8 & 0xFF) / 255.0F;
    float f3 = (l1 & 0xFF) / 255.0F;
    if (pt.a) {
      float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
      float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
      float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
      f1 = f4;
      f2 = f5;
      f3 = f6;
    }
    if (Minecraft.v()) return a(un1, i1, j1, k1, f1, f2, f3);
    return b(un1, i1, j1, k1, f1, f2, f3);
  }

  public boolean a(un un1, int i1, int j1, int k1, float f1, float f2, float f3) {
    f = true;
    boolean flag = false;
    float f4 = g;
    float f11 = g;
    float f18 = g;
    float f25 = g;
    boolean flag1 = true;
    boolean flag2 = true;
    boolean flag3 = true;
    boolean flag4 = true;
    boolean flag5 = true;
    boolean flag6 = true;
    g = un1.d(b, i1, j1, k1);
    h = un1.d(b, i1 - 1, j1, k1);
    i = un1.d(b, i1, j1 - 1, k1);
    j = un1.d(b, i1, j1, k1 - 1);
    k = un1.d(b, i1 + 1, j1, k1);
    l = un1.d(b, i1, j1 + 1, k1);
    m = un1.d(b, i1, j1, k1 + 1);
    V = un.r[b.a(i1 + 1, j1 + 1, k1)];
    ad = un.r[b.a(i1 + 1, j1 - 1, k1)];
    Z = un.r[b.a(i1 + 1, j1, k1 + 1)];
    ab = un.r[b.a(i1 + 1, j1, k1 - 1)];
    W = un.r[b.a(i1 - 1, j1 + 1, k1)];
    ae = un.r[b.a(i1 - 1, j1 - 1, k1)];
    Y = un.r[b.a(i1 - 1, j1, k1 - 1)];
    aa = un.r[b.a(i1 - 1, j1, k1 + 1)];
    X = un.r[b.a(i1, j1 + 1, k1 + 1)];
    U = un.r[b.a(i1, j1 + 1, k1 - 1)];
    af = un.r[b.a(i1, j1 - 1, k1 + 1)];
    ac = un.r[b.a(i1, j1 - 1, k1 - 1)];
    if (un1.bm == 3)
      flag1 = flag3 = flag4 = flag5 = flag6 = 0;
    if (c >= 0)
      flag1 = flag3 = flag4 = flag5 = flag6 = 0;
    if ((e) || (un1.b(b, i1, j1 - 1, k1, 0)))
    {
      float f12;
      float f26;
      float f19;
      float f12;
      float f5;
      if (H > 0) {
        j1--;
        o = un1.d(b, i1 - 1, j1, k1);
        q = un1.d(b, i1, j1, k1 - 1);
        r = un1.d(b, i1, j1, k1 + 1);
        t = un1.d(b, i1 + 1, j1, k1);
        if ((ac) || (ae)) n = un1.d(b, i1 - 1, j1, k1 - 1); else
          n = o;
        if ((af) || (ae)) p = un1.d(b, i1 - 1, j1, k1 + 1); else
          p = o;
        if ((ac) || (ad)) s = un1.d(b, i1 + 1, j1, k1 - 1); else
          s = t;
        if ((af) || (ad)) u = un1.d(b, i1 + 1, j1, k1 + 1); else
          u = t;
        j1++;
        float f5 = (p + o + r + i) / 4.0F;
        float f26 = (r + i + u + t) / 4.0F;
        float f19 = (i + q + t + s) / 4.0F;
        f12 = (o + n + i + q) / 4.0F;
      } else {
        f5 = f12 = f19 = f26 = i;
      }
      I = (this.J = this.K = this.L = (flag1 ? f1 : 1.0F) * 0.5F);
      M = (this.N = this.O = this.P = (flag1 ? f2 : 1.0F) * 0.5F);
      Q = (this.R = this.S = this.T = (flag1 ? f3 : 1.0F) * 0.5F);
      I *= f5;
      M *= f5;
      Q *= f5;
      J *= f12;
      N *= f12;
      R *= f12;
      K *= f19;
      O *= f19;
      S *= f19;
      L *= f26;
      P *= f26;
      T *= f26;
      a(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 0));
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1 + 1, k1, 1)))
    {
      float f20;
      float f27;
      float f20;
      float f13;
      float f6;
      if (H > 0) {
        j1++;
        w = un1.d(b, i1 - 1, j1, k1);
        A = un1.d(b, i1 + 1, j1, k1);
        y = un1.d(b, i1, j1, k1 - 1);
        B = un1.d(b, i1, j1, k1 + 1);
        if ((U) || (W)) v = un1.d(b, i1 - 1, j1, k1 - 1); else
          v = w;
        if ((U) || (V)) z = un1.d(b, i1 + 1, j1, k1 - 1); else
          z = A;
        if ((X) || (W)) x = un1.d(b, i1 - 1, j1, k1 + 1); else
          x = w;
        if ((X) || (V)) C = un1.d(b, i1 + 1, j1, k1 + 1); else
          C = A;
        j1--;
        float f27 = (x + w + B + l) / 4.0F;
        float f6 = (B + l + C + A) / 4.0F;
        float f13 = (l + y + A + z) / 4.0F;
        f20 = (w + v + l + y) / 4.0F;
      } else {
        f6 = f13 = f20 = f27 = l;
      }
      I = (this.J = this.K = this.L = flag2 ? f1 : 1.0F);
      M = (this.N = this.O = this.P = flag2 ? f2 : 1.0F);
      Q = (this.R = this.S = this.T = flag2 ? f3 : 1.0F);
      I *= f6;
      M *= f6;
      Q *= f6;
      J *= f13;
      N *= f13;
      R *= f13;
      K *= f20;
      O *= f20;
      S *= f20;
      L *= f27;
      P *= f27;
      T *= f27;
      b(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 1));
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1, k1 - 1, 2)))
    {
      float f28;
      float f28;
      float f21;
      float f14;
      float f7;
      if (H > 0) {
        k1--;
        D = un1.d(b, i1 - 1, j1, k1);
        q = un1.d(b, i1, j1 - 1, k1);
        y = un1.d(b, i1, j1 + 1, k1);
        E = un1.d(b, i1 + 1, j1, k1);
        if ((Y) || (ac)) n = un1.d(b, i1 - 1, j1 - 1, k1); else
          n = D;
        if ((Y) || (U)) v = un1.d(b, i1 - 1, j1 + 1, k1); else
          v = D;
        if ((ab) || (ac)) s = un1.d(b, i1 + 1, j1 - 1, k1); else
          s = E;
        if ((ab) || (U)) z = un1.d(b, i1 + 1, j1 + 1, k1); else
          z = E;
        k1++;
        float f7 = (D + v + j + y) / 4.0F;
        float f14 = (j + y + E + z) / 4.0F;
        float f21 = (q + j + s + E) / 4.0F;
        f28 = (n + D + q + j) / 4.0F;
      } else {
        f7 = f14 = f21 = f28 = j;
      }
      I = (this.J = this.K = this.L = (flag3 ? f1 : 1.0F) * 0.8F);
      M = (this.N = this.O = this.P = (flag3 ? f2 : 1.0F) * 0.8F);
      Q = (this.R = this.S = this.T = (flag3 ? f3 : 1.0F) * 0.8F);
      I *= f7;
      M *= f7;
      Q *= f7;
      J *= f14;
      N *= f14;
      R *= f14;
      K *= f21;
      O *= f21;
      S *= f21;
      L *= f28;
      P *= f28;
      T *= f28;
      int l1 = un1.a(b, i1, j1, k1, 2);
      c(un1, i1, j1, k1, l1);
      if ((cfgGrassFix) && (l1 == 3) && (c < 0)) {
        I *= f1;
        J *= f1;
        K *= f1;
        L *= f1;
        M *= f2;
        N *= f2;
        O *= f2;
        P *= f2;
        Q *= f3;
        R *= f3;
        S *= f3;
        T *= f3;
        c(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1, k1 + 1, 3)))
    {
      float f15;
      float f29;
      float f22;
      float f15;
      float f8;
      if (H > 0) {
        k1++;
        F = un1.d(b, i1 - 1, j1, k1);
        G = un1.d(b, i1 + 1, j1, k1);
        r = un1.d(b, i1, j1 - 1, k1);
        B = un1.d(b, i1, j1 + 1, k1);
        if ((aa) || (af)) p = un1.d(b, i1 - 1, j1 - 1, k1); else
          p = F;
        if ((aa) || (X)) x = un1.d(b, i1 - 1, j1 + 1, k1); else
          x = F;
        if ((Z) || (af)) u = un1.d(b, i1 + 1, j1 - 1, k1); else
          u = G;
        if ((Z) || (X)) C = un1.d(b, i1 + 1, j1 + 1, k1); else
          C = G;
        k1--;
        float f8 = (F + x + m + B) / 4.0F;
        float f29 = (m + B + G + C) / 4.0F;
        float f22 = (r + m + u + G) / 4.0F;
        f15 = (p + F + r + m) / 4.0F;
      } else {
        f8 = f15 = f22 = f29 = m;
      }
      I = (this.J = this.K = this.L = (flag4 ? f1 : 1.0F) * 0.8F);
      M = (this.N = this.O = this.P = (flag4 ? f2 : 1.0F) * 0.8F);
      Q = (this.R = this.S = this.T = (flag4 ? f3 : 1.0F) * 0.8F);
      I *= f8;
      M *= f8;
      Q *= f8;
      J *= f15;
      N *= f15;
      R *= f15;
      K *= f22;
      O *= f22;
      S *= f22;
      L *= f29;
      P *= f29;
      T *= f29;
      int i2 = un1.a(b, i1, j1, k1, 3);
      d(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 3));
      if ((cfgGrassFix) && (i2 == 3) && (c < 0)) {
        I *= f1;
        J *= f1;
        K *= f1;
        L *= f1;
        M *= f2;
        N *= f2;
        O *= f2;
        P *= f2;
        Q *= f3;
        R *= f3;
        S *= f3;
        T *= f3;
        d(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    if ((e) || (un1.b(b, i1 - 1, j1, k1, 4)))
    {
      float f23;
      float f30;
      float f23;
      float f16;
      float f9;
      if (H > 0) {
        i1--;
        o = un1.d(b, i1, j1 - 1, k1);
        D = un1.d(b, i1, j1, k1 - 1);
        F = un1.d(b, i1, j1, k1 + 1);
        w = un1.d(b, i1, j1 + 1, k1);
        if ((Y) || (ae)) n = un1.d(b, i1, j1 - 1, k1 - 1); else
          n = D;
        if ((aa) || (ae)) p = un1.d(b, i1, j1 - 1, k1 + 1); else
          p = F;
        if ((Y) || (W)) v = un1.d(b, i1, j1 + 1, k1 - 1); else
          v = D;
        if ((aa) || (W)) x = un1.d(b, i1, j1 + 1, k1 + 1); else
          x = F;
        i1++;
        float f30 = (o + p + h + F) / 4.0F;
        float f9 = (h + F + w + x) / 4.0F;
        float f16 = (D + h + v + w) / 4.0F;
        f23 = (n + o + D + h) / 4.0F;
      } else {
        f9 = f16 = f23 = f30 = h;
      }
      I = (this.J = this.K = this.L = (flag5 ? f1 : 1.0F) * 0.6F);
      M = (this.N = this.O = this.P = (flag5 ? f2 : 1.0F) * 0.6F);
      Q = (this.R = this.S = this.T = (flag5 ? f3 : 1.0F) * 0.6F);
      I *= f9;
      M *= f9;
      Q *= f9;
      J *= f16;
      N *= f16;
      R *= f16;
      K *= f23;
      O *= f23;
      S *= f23;
      L *= f30;
      P *= f30;
      T *= f30;
      int j2 = un1.a(b, i1, j1, k1, 4);
      e(un1, i1, j1, k1, j2);
      if ((cfgGrassFix) && (j2 == 3) && (c < 0)) {
        I *= f1;
        J *= f1;
        K *= f1;
        L *= f1;
        M *= f2;
        N *= f2;
        O *= f2;
        P *= f2;
        Q *= f3;
        R *= f3;
        S *= f3;
        T *= f3;
        e(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    if ((e) || (un1.b(b, i1 + 1, j1, k1, 5)))
    {
      float f17;
      float f31;
      float f24;
      float f17;
      float f10;
      if (H > 0) {
        i1++;
        t = un1.d(b, i1, j1 - 1, k1);
        E = un1.d(b, i1, j1, k1 - 1);
        G = un1.d(b, i1, j1, k1 + 1);
        A = un1.d(b, i1, j1 + 1, k1);
        if ((ad) || (ab)) s = un1.d(b, i1, j1 - 1, k1 - 1); else
          s = E;
        if ((ad) || (Z)) u = un1.d(b, i1, j1 - 1, k1 + 1); else
          u = G;
        if ((V) || (ab)) z = un1.d(b, i1, j1 + 1, k1 - 1); else
          z = E;
        if ((V) || (Z)) C = un1.d(b, i1, j1 + 1, k1 + 1); else
          C = G;
        i1--;
        float f10 = (t + u + k + G) / 4.0F;
        float f31 = (k + G + A + C) / 4.0F;
        float f24 = (E + k + z + A) / 4.0F;
        f17 = (s + t + E + k) / 4.0F;
      } else {
        f10 = f17 = f24 = f31 = k;
      }
      I = (this.J = this.K = this.L = (flag6 ? f1 : 1.0F) * 0.6F);
      M = (this.N = this.O = this.P = (flag6 ? f2 : 1.0F) * 0.6F);
      Q = (this.R = this.S = this.T = (flag6 ? f3 : 1.0F) * 0.6F);
      I *= f10;
      M *= f10;
      Q *= f10;
      J *= f17;
      N *= f17;
      R *= f17;
      K *= f24;
      O *= f24;
      S *= f24;
      L *= f31;
      P *= f31;
      T *= f31;
      int k2 = un1.a(b, i1, j1, k1, 5);
      f(un1, i1, j1, k1, k2);
      if ((cfgGrassFix) && (k2 == 3) && (c < 0)) {
        I *= f1;
        J *= f1;
        K *= f1;
        L *= f1;
        M *= f2;
        N *= f2;
        O *= f2;
        P *= f2;
        Q *= f3;
        R *= f3;
        S *= f3;
        T *= f3;
        f(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    f = false;
    return flag;
  }

  public boolean b(un un1, int i1, int j1, int k1, float f1, float f2, float f3) {
    f = false;
    ns ns1 = ns.a;
    boolean flag = false;
    float f4 = 0.5F;
    float f5 = 1.0F;
    float f6 = 0.8F;
    float f7 = 0.6F;
    float f8 = f5 * f1;
    float f9 = f5 * f2;
    float f10 = f5 * f3;
    float f11 = f4;
    float f12 = f6;
    float f13 = f7;
    float f14 = f4;
    float f15 = f6;
    float f16 = f7;
    float f17 = f4;
    float f18 = f6;
    float f19 = f7;
    if (un1 != un.v) {
      f11 *= f1;
      f12 *= f1;
      f13 *= f1;
      f14 *= f2;
      f15 *= f2;
      f16 *= f2;
      f17 *= f3;
      f18 *= f3;
      f19 *= f3;
    }
    float f20 = un1.d(b, i1, j1, k1);
    if ((e) || (un1.b(b, i1, j1 - 1, k1, 0))) {
      float f21 = un1.d(b, i1, j1 - 1, k1);
      ns1.a(f11 * f21, f14 * f21, f17 * f21);
      a(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 0));
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1 + 1, k1, 1))) {
      float f22 = un1.d(b, i1, j1 + 1, k1);
      if ((un1.bw != 1.0D) && (!un1.bA.d()))
        f22 = f20;
      ns1.a(f8 * f22, f9 * f22, f10 * f22);
      b(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 1));
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1, k1 - 1, 2))) {
      float f23 = un1.d(b, i1, j1, k1 - 1);
      if (un1.bu > 0.0D)
        f23 = f20;
      ns1.a(f12 * f23, f15 * f23, f18 * f23);
      int l1 = un1.a(b, i1, j1, k1, 2);
      c(un1, i1, j1, k1, l1);
      if ((cfgGrassFix) && (l1 == 3) && (c < 0)) {
        ns1.a(f12 * f23 * f1, f15 * f23 * f2, f18 * f23 * f3);
        c(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1, k1 + 1, 3))) {
      float f24 = un1.d(b, i1, j1, k1 + 1);
      if (un1.bx < 1.0D)
        f24 = f20;
      ns1.a(f12 * f24, f15 * f24, f18 * f24);
      int i2 = un1.a(b, i1, j1, k1, 3);
      d(un1, i1, j1, k1, i2);
      if ((cfgGrassFix) && (i2 == 3) && (c < 0)) {
        ns1.a(f12 * f24 * f1, f15 * f24 * f2, f18 * f24 * f3);
        d(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    if ((e) || (un1.b(b, i1 - 1, j1, k1, 4))) {
      float f25 = un1.d(b, i1 - 1, j1, k1);
      if (un1.bs > 0.0D)
        f25 = f20;
      ns1.a(f13 * f25, f16 * f25, f19 * f25);
      int j2 = un1.a(b, i1, j1, k1, 4);
      e(un1, i1, j1, k1, j2);
      if ((cfgGrassFix) && (j2 == 3) && (c < 0)) {
        ns1.a(f13 * f25 * f1, f16 * f25 * f2, f19 * f25 * f3);
        e(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    if ((e) || (un1.b(b, i1 + 1, j1, k1, 5))) {
      float f26 = un1.d(b, i1 + 1, j1, k1);
      if (un1.bv < 1.0D)
        f26 = f20;
      ns1.a(f13 * f26, f16 * f26, f19 * f26);
      int k2 = un1.a(b, i1, j1, k1, 5);
      f(un1, i1, j1, k1, k2);
      if ((cfgGrassFix) && (k2 == 3) && (c < 0)) {
        ns1.a(f13 * f26 * f1, f16 * f26 * f2, f19 * f26 * f3);
        f(un1, i1, j1, k1, 38);
      }
      flag = true;
    }
    return flag;
  }

  public boolean k(un un1, int i1, int j1, int k1) {
    int l1 = un1.b(b, i1, j1, k1);
    float f1 = (l1 >> 16 & 0xFF) / 255.0F;
    float f2 = (l1 >> 8 & 0xFF) / 255.0F;
    float f3 = (l1 & 0xFF) / 255.0F;
    if (pt.a) {
      float f4 = (f1 * 30.0F + f2 * 59.0F + f3 * 11.0F) / 100.0F;
      float f5 = (f1 * 30.0F + f2 * 70.0F) / 100.0F;
      float f6 = (f1 * 30.0F + f3 * 70.0F) / 100.0F;
      f1 = f4;
      f2 = f5;
      f3 = f6;
    }
    return c(un1, i1, j1, k1, f1, f2, f3);
  }

  public boolean c(un un1, int i1, int j1, int k1, float f1, float f2, float f3) {
    ns ns1 = ns.a;
    boolean flag = false;
    float f4 = 0.5F;
    float f5 = 1.0F;
    float f6 = 0.8F;
    float f7 = 0.6F;
    float f8 = f4 * f1;
    float f9 = f5 * f1;
    float f10 = f6 * f1;
    float f11 = f7 * f1;
    float f12 = f4 * f2;
    float f13 = f5 * f2;
    float f14 = f6 * f2;
    float f15 = f7 * f2;
    float f16 = f4 * f3;
    float f17 = f5 * f3;
    float f18 = f6 * f3;
    float f19 = f7 * f3;
    float f20 = 0.0625F;
    float f21 = un1.d(b, i1, j1, k1);
    if ((e) || (un1.b(b, i1, j1 - 1, k1, 0))) {
      float f22 = un1.d(b, i1, j1 - 1, k1);
      ns1.a(f8 * f22, f12 * f22, f16 * f22);
      a(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 0));
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1 + 1, k1, 1))) {
      float f23 = un1.d(b, i1, j1 + 1, k1);
      if ((un1.bw != 1.0D) && (!un1.bA.d()))
        f23 = f21;
      ns1.a(f9 * f23, f13 * f23, f17 * f23);
      b(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 1));
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1, k1 - 1, 2))) {
      float f24 = un1.d(b, i1, j1, k1 - 1);
      if (un1.bu > 0.0D)
        f24 = f21;
      ns1.a(f10 * f24, f14 * f24, f18 * f24);
      ns1.c(0.0F, 0.0F, f20);
      c(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 2));
      ns1.c(0.0F, 0.0F, -f20);
      flag = true;
    }
    if ((e) || (un1.b(b, i1, j1, k1 + 1, 3))) {
      float f25 = un1.d(b, i1, j1, k1 + 1);
      if (un1.bx < 1.0D)
        f25 = f21;
      ns1.a(f10 * f25, f14 * f25, f18 * f25);
      ns1.c(0.0F, 0.0F, -f20);
      d(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 3));
      ns1.c(0.0F, 0.0F, f20);
      flag = true;
    }
    if ((e) || (un1.b(b, i1 - 1, j1, k1, 4))) {
      float f26 = un1.d(b, i1 - 1, j1, k1);
      if (un1.bs > 0.0D)
        f26 = f21;
      ns1.a(f11 * f26, f15 * f26, f19 * f26);
      ns1.c(f20, 0.0F, 0.0F);
      e(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 4));
      ns1.c(-f20, 0.0F, 0.0F);
      flag = true;
    }
    if ((e) || (un1.b(b, i1 + 1, j1, k1, 5))) {
      float f27 = un1.d(b, i1 + 1, j1, k1);
      if (un1.bv < 1.0D)
        f27 = f21;
      ns1.a(f11 * f27, f15 * f27, f19 * f27);
      ns1.c(-f20, 0.0F, 0.0F);
      f(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 5));
      ns1.c(f20, 0.0F, 0.0F);
      flag = true;
    }
    return flag;
  }

  public boolean l(un un1, int i1, int j1, int k1) {
    boolean flag = false;
    float f1 = 0.375F;
    float f2 = 0.625F;
    un1.a(f1, 0.0F, f1, f2, 1.0F, f2);
    j(un1, i1, j1, k1);
    flag = true;
    boolean flag1 = false;
    boolean flag2 = false;
    if ((b.a(i1 - 1, j1, k1) == un1.bn) || (b.a(i1 + 1, j1, k1) == un1.bn))
      flag1 = true;
    if ((b.a(i1, j1, k1 - 1) == un1.bn) || (b.a(i1, j1, k1 + 1) == un1.bn))
      flag2 = true;
    boolean flag3 = b.a(i1 - 1, j1, k1) == un1.bn;
    boolean flag4 = b.a(i1 + 1, j1, k1) == un1.bn;
    boolean flag5 = b.a(i1, j1, k1 - 1) == un1.bn;
    boolean flag6 = b.a(i1, j1, k1 + 1) == un1.bn;
    if ((!flag1) && (!flag2))
      flag1 = true;
    f1 = 0.4375F;
    f2 = 0.5625F;
    float f3 = 0.75F;
    float f4 = 0.9375F;
    float f5 = flag3 ? 0.0F : f1;
    float f6 = flag4 ? 1.0F : f2;
    float f7 = flag5 ? 0.0F : f1;
    float f8 = flag6 ? 1.0F : f2;
    if (flag1) {
      un1.a(f5, f3, f1, f6, f4, f2);
      j(un1, i1, j1, k1);
      flag = true;
    }
    if (flag2) {
      un1.a(f1, f3, f7, f2, f4, f8);
      j(un1, i1, j1, k1);
      flag = true;
    }
    f3 = 0.375F;
    f4 = 0.5625F;
    if (flag1) {
      un1.a(f5, f3, f1, f6, f4, f2);
      j(un1, i1, j1, k1);
      flag = true;
    }
    if (flag2) {
      un1.a(f1, f3, f7, f2, f4, f8);
      j(un1, i1, j1, k1);
      flag = true;
    }
    un1.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    return flag;
  }

  public boolean m(un un1, int i1, int j1, int k1) {
    boolean flag = false;
    int l1 = b.e(i1, j1, k1);
    if (l1 == 0) {
      un1.a(0.0F, 0.0F, 0.0F, 0.5F, 0.5F, 1.0F);
      j(un1, i1, j1, k1);
      un1.a(0.5F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
      j(un1, i1, j1, k1);
      flag = true;
    } else if (l1 == 1) {
      un1.a(0.0F, 0.0F, 0.0F, 0.5F, 1.0F, 1.0F);
      j(un1, i1, j1, k1);
      un1.a(0.5F, 0.0F, 0.0F, 1.0F, 0.5F, 1.0F);
      j(un1, i1, j1, k1);
      flag = true;
    } else if (l1 == 2) {
      un1.a(0.0F, 0.0F, 0.0F, 1.0F, 0.5F, 0.5F);
      j(un1, i1, j1, k1);
      un1.a(0.0F, 0.0F, 0.5F, 1.0F, 1.0F, 1.0F);
      j(un1, i1, j1, k1);
      flag = true;
    } else if (l1 == 3) {
      un1.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
      j(un1, i1, j1, k1);
      un1.a(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
      j(un1, i1, j1, k1);
      flag = true;
    }
    un1.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F);
    return flag;
  }

  public boolean n(un un1, int i1, int j1, int k1) {
    ns ns1 = ns.a;
    la la1 = (la)un1;
    boolean flag = false;
    float f1 = 0.5F;
    float f2 = 1.0F;
    float f3 = 0.8F;
    float f4 = 0.6F;
    float f5 = un1.d(b, i1, j1, k1);
    float f6 = un1.d(b, i1, j1 - 1, k1);
    if (la1.bt > 0.0D)
      f6 = f5;
    if (un.s[un1.bn] > 0)
      f6 = 1.0F;
    ns1.a(f1 * f6, f1 * f6, f1 * f6);
    a(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 0));
    flag = true;
    f6 = un1.d(b, i1, j1 + 1, k1);
    if (la1.bw < 1.0D)
      f6 = f5;
    if (un.s[un1.bn] > 0)
      f6 = 1.0F;
    ns1.a(f2 * f6, f2 * f6, f2 * f6);
    b(un1, i1, j1, k1, un1.a(b, i1, j1, k1, 1));
    flag = true;
    f6 = un1.d(b, i1, j1, k1 - 1);
    if (la1.bu > 0.0D)
      f6 = f5;
    if (un.s[un1.bn] > 0)
      f6 = 1.0F;
    ns1.a(f3 * f6, f3 * f6, f3 * f6);
    int l1 = un1.a(b, i1, j1, k1, 2);
    if (l1 < 0) {
      d = true;
      l1 = -l1;
    }
    c(un1, i1, j1, k1, l1);
    flag = true;
    d = false;
    f6 = un1.d(b, i1, j1, k1 + 1);
    if (la1.bx < 1.0D)
      f6 = f5;
    if (un.s[un1.bn] > 0)
      f6 = 1.0F;
    ns1.a(f3 * f6, f3 * f6, f3 * f6);
    l1 = un1.a(b, i1, j1, k1, 3);
    if (l1 < 0) {
      d = true;
      l1 = -l1;
    }
    d(un1, i1, j1, k1, l1);
    flag = true;
    d = false;
    f6 = un1.d(b, i1 - 1, j1, k1);
    if (la1.bs > 0.0D)
      f6 = f5;
    if (un.s[un1.bn] > 0)
      f6 = 1.0F;
    ns1.a(f4 * f6, f4 * f6, f4 * f6);
    l1 = un1.a(b, i1, j1, k1, 4);
    if (l1 < 0) {
      d = true;
      l1 = -l1;
    }
    e(un1, i1, j1, k1, l1);
    flag = true;
    d = false;
    f6 = un1.d(b, i1 + 1, j1, k1);
    if (la1.bv < 1.0D)
      f6 = f5;
    if (un.s[un1.bn] > 0)
      f6 = 1.0F;
    ns1.a(f4 * f6, f4 * f6, f4 * f6);
    l1 = un1.a(b, i1, j1, k1, 5);
    if (l1 < 0) {
      d = true;
      l1 = -l1;
    }
    f(un1, i1, j1, k1, l1);
    flag = true;
    d = false;
    return flag;
  }

  public void a(un un1, double d1, double d2, double d3, int i1) {
    ns ns1 = ns.a;
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    double d4 = (j1 + un1.bs * 16.0D) / 256.0D;
    double d5 = (j1 + un1.bv * 16.0D - 0.01D) / 256.0D;
    double d6 = (k1 + un1.bu * 16.0D) / 256.0D;
    double d7 = (k1 + un1.bx * 16.0D - 0.01D) / 256.0D;
    if ((un1.bs < 0.0D) || (un1.bv > 1.0D)) {
      d4 = (j1 + 0.0F) / 256.0F;
      d5 = (j1 + 15.99F) / 256.0F;
    }
    if ((un1.bu < 0.0D) || (un1.bx > 1.0D)) {
      d6 = (k1 + 0.0F) / 256.0F;
      d7 = (k1 + 15.99F) / 256.0F;
    }
    double d8 = d1 + un1.bs;
    double d9 = d1 + un1.bv;
    double d10 = d2 + un1.bt;
    double d11 = d3 + un1.bu;
    double d12 = d3 + un1.bx;
    if (f) {
      ns1.a(I, M, Q);
      ns1.a(d8, d10, d12, d4, d7);
      ns1.a(J, N, R);
      ns1.a(d8, d10, d11, d4, d6);
      ns1.a(K, O, S);
      ns1.a(d9, d10, d11, d5, d6);
      ns1.a(L, P, T);
      ns1.a(d9, d10, d12, d5, d7);
    } else {
      ns1.a(d8, d10, d12, d4, d7);
      ns1.a(d8, d10, d11, d4, d6);
      ns1.a(d9, d10, d11, d5, d6);
      ns1.a(d9, d10, d12, d5, d7);
    }
  }

  public void b(un un1, double d1, double d2, double d3, int i1) {
    ns ns1 = ns.a;
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    double d4 = (j1 + un1.bs * 16.0D) / 256.0D;
    double d5 = (j1 + un1.bv * 16.0D - 0.01D) / 256.0D;
    double d6 = (k1 + un1.bu * 16.0D) / 256.0D;
    double d7 = (k1 + un1.bx * 16.0D - 0.01D) / 256.0D;
    if ((un1.bs < 0.0D) || (un1.bv > 1.0D)) {
      d4 = (j1 + 0.0F) / 256.0F;
      d5 = (j1 + 15.99F) / 256.0F;
    }
    if ((un1.bu < 0.0D) || (un1.bx > 1.0D)) {
      d6 = (k1 + 0.0F) / 256.0F;
      d7 = (k1 + 15.99F) / 256.0F;
    }
    double d8 = d1 + un1.bs;
    double d9 = d1 + un1.bv;
    double d10 = d2 + un1.bw;
    double d11 = d3 + un1.bu;
    double d12 = d3 + un1.bx;
    if (f) {
      ns1.a(I, M, Q);
      ns1.a(d9, d10, d12, d5, d7);
      ns1.a(J, N, R);
      ns1.a(d9, d10, d11, d5, d6);
      ns1.a(K, O, S);
      ns1.a(d8, d10, d11, d4, d6);
      ns1.a(L, P, T);
      ns1.a(d8, d10, d12, d4, d7);
    } else {
      ns1.a(d9, d10, d12, d5, d7);
      ns1.a(d9, d10, d11, d5, d6);
      ns1.a(d8, d10, d11, d4, d6);
      ns1.a(d8, d10, d12, d4, d7);
    }
  }

  public void c(un un1, double d1, double d2, double d3, int i1) {
    ns ns1 = ns.a;
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    double d4 = (j1 + un1.bs * 16.0D) / 256.0D;
    double d5 = (j1 + un1.bv * 16.0D - 0.01D) / 256.0D;
    double d6 = (k1 + un1.bt * 16.0D) / 256.0D;
    double d7 = (k1 + un1.bw * 16.0D - 0.01D) / 256.0D;
    if (d) {
      double d8 = d4;
      d4 = d5;
      d5 = d8;
    }
    if ((un1.bs < 0.0D) || (un1.bv > 1.0D)) {
      d4 = (j1 + 0.0F) / 256.0F;
      d5 = (j1 + 15.99F) / 256.0F;
    }
    if ((un1.bt < 0.0D) || (un1.bw > 1.0D)) {
      d6 = (k1 + 0.0F) / 256.0F;
      d7 = (k1 + 15.99F) / 256.0F;
    }
    double d9 = d1 + un1.bs;
    double d10 = d1 + un1.bv;
    double d11 = d2 + un1.bt;
    double d12 = d2 + un1.bw;
    double d13 = d3 + un1.bu;
    if (f) {
      ns1.a(I, M, Q);
      ns1.a(d9, d12, d13, d5, d6);
      ns1.a(J, N, R);
      ns1.a(d10, d12, d13, d4, d6);
      ns1.a(K, O, S);
      ns1.a(d10, d11, d13, d4, d7);
      ns1.a(L, P, T);
      ns1.a(d9, d11, d13, d5, d7);
    } else {
      ns1.a(d9, d12, d13, d5, d6);
      ns1.a(d10, d12, d13, d4, d6);
      ns1.a(d10, d11, d13, d4, d7);
      ns1.a(d9, d11, d13, d5, d7);
    }
  }

  public void d(un un1, double d1, double d2, double d3, int i1) {
    ns ns1 = ns.a;
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    double d4 = (j1 + un1.bs * 16.0D) / 256.0D;
    double d5 = (j1 + un1.bv * 16.0D - 0.01D) / 256.0D;
    double d6 = (k1 + un1.bt * 16.0D) / 256.0D;
    double d7 = (k1 + un1.bw * 16.0D - 0.01D) / 256.0D;
    if (d) {
      double d8 = d4;
      d4 = d5;
      d5 = d8;
    }
    if ((un1.bs < 0.0D) || (un1.bv > 1.0D)) {
      d4 = (j1 + 0.0F) / 256.0F;
      d5 = (j1 + 15.99F) / 256.0F;
    }
    if ((un1.bt < 0.0D) || (un1.bw > 1.0D)) {
      d6 = (k1 + 0.0F) / 256.0F;
      d7 = (k1 + 15.99F) / 256.0F;
    }
    double d9 = d1 + un1.bs;
    double d10 = d1 + un1.bv;
    double d11 = d2 + un1.bt;
    double d12 = d2 + un1.bw;
    double d13 = d3 + un1.bx;
    if (f) {
      ns1.a(I, M, Q);
      ns1.a(d9, d12, d13, d4, d6);
      ns1.a(J, N, R);
      ns1.a(d9, d11, d13, d4, d7);
      ns1.a(K, O, S);
      ns1.a(d10, d11, d13, d5, d7);
      ns1.a(L, P, T);
      ns1.a(d10, d12, d13, d5, d6);
    } else {
      ns1.a(d9, d12, d13, d4, d6);
      ns1.a(d9, d11, d13, d4, d7);
      ns1.a(d10, d11, d13, d5, d7);
      ns1.a(d10, d12, d13, d5, d6);
    }
  }

  public void e(un un1, double d1, double d2, double d3, int i1) {
    ns ns1 = ns.a;
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    double d4 = (j1 + un1.bu * 16.0D) / 256.0D;
    double d5 = (j1 + un1.bx * 16.0D - 0.01D) / 256.0D;
    double d6 = (k1 + un1.bt * 16.0D) / 256.0D;
    double d7 = (k1 + un1.bw * 16.0D - 0.01D) / 256.0D;
    if (d) {
      double d8 = d4;
      d4 = d5;
      d5 = d8;
    }
    if ((un1.bu < 0.0D) || (un1.bx > 1.0D)) {
      d4 = (j1 + 0.0F) / 256.0F;
      d5 = (j1 + 15.99F) / 256.0F;
    }
    if ((un1.bt < 0.0D) || (un1.bw > 1.0D)) {
      d6 = (k1 + 0.0F) / 256.0F;
      d7 = (k1 + 15.99F) / 256.0F;
    }
    double d9 = d1 + un1.bs;
    double d10 = d2 + un1.bt;
    double d11 = d2 + un1.bw;
    double d12 = d3 + un1.bu;
    double d13 = d3 + un1.bx;
    if (f) {
      ns1.a(I, M, Q);
      ns1.a(d9, d11, d13, d5, d6);
      ns1.a(J, N, R);
      ns1.a(d9, d11, d12, d4, d6);
      ns1.a(K, O, S);
      ns1.a(d9, d10, d12, d4, d7);
      ns1.a(L, P, T);
      ns1.a(d9, d10, d13, d5, d7);
    } else {
      ns1.a(d9, d11, d13, d5, d6);
      ns1.a(d9, d11, d12, d4, d6);
      ns1.a(d9, d10, d12, d4, d7);
      ns1.a(d9, d10, d13, d5, d7);
    }
  }

  public void f(un un1, double d1, double d2, double d3, int i1) {
    ns ns1 = ns.a;
    if (c >= 0)
      i1 = c;
    int j1 = (i1 & 0xF) << 4;
    int k1 = i1 & 0xF0;
    double d4 = (j1 + un1.bu * 16.0D) / 256.0D;
    double d5 = (j1 + un1.bx * 16.0D - 0.01D) / 256.0D;
    double d6 = (k1 + un1.bt * 16.0D) / 256.0D;
    double d7 = (k1 + un1.bw * 16.0D - 0.01D) / 256.0D;
    if (d) {
      double d8 = d4;
      d4 = d5;
      d5 = d8;
    }
    if ((un1.bu < 0.0D) || (un1.bx > 1.0D)) {
      d4 = (j1 + 0.0F) / 256.0F;
      d5 = (j1 + 15.99F) / 256.0F;
    }
    if ((un1.bt < 0.0D) || (un1.bw > 1.0D)) {
      d6 = (k1 + 0.0F) / 256.0F;
      d7 = (k1 + 15.99F) / 256.0F;
    }
    double d9 = d1 + un1.bv;
    double d10 = d2 + un1.bt;
    double d11 = d2 + un1.bw;
    double d12 = d3 + un1.bu;
    double d13 = d3 + un1.bx;
    if (f) {
      ns1.a(I, M, Q);
      ns1.a(d9, d10, d13, d4, d7);
      ns1.a(J, N, R);
      ns1.a(d9, d10, d12, d5, d7);
      ns1.a(K, O, S);
      ns1.a(d9, d11, d12, d5, d6);
      ns1.a(L, P, T);
      ns1.a(d9, d11, d13, d4, d6);
    } else {
      ns1.a(d9, d10, d13, d4, d7);
      ns1.a(d9, d10, d12, d5, d7);
      ns1.a(d9, d11, d12, d5, d6);
      ns1.a(d9, d11, d13, d4, d6);
    }
  }

  public void a(un un1, int i1) {
    ns ns1 = ns.a;
    int j1 = un1.g();
    if (j1 == 0) {
      un1.f();
      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      ns1.b();
      ns1.b(0.0F, -1.0F, 0.0F);
      a(un1, 0.0D, 0.0D, 0.0D, un1.a(0, i1));
      ns1.a();
      ns1.b();
      ns1.b(0.0F, 1.0F, 0.0F);
      b(un1, 0.0D, 0.0D, 0.0D, un1.a(1, i1));
      ns1.a();
      ns1.b();
      ns1.b(0.0F, 0.0F, -1.0F);
      c(un1, 0.0D, 0.0D, 0.0D, un1.a(2, i1));
      ns1.a();
      ns1.b();
      ns1.b(0.0F, 0.0F, 1.0F);
      d(un1, 0.0D, 0.0D, 0.0D, un1.a(3, i1));
      ns1.a();
      ns1.b();
      ns1.b(-1.0F, 0.0F, 0.0F);
      e(un1, 0.0D, 0.0D, 0.0D, un1.a(4, i1));
      ns1.a();
      ns1.b();
      ns1.b(1.0F, 0.0F, 0.0F);
      f(un1, 0.0D, 0.0D, 0.0D, un1.a(5, i1));
      ns1.a();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    } else if (j1 == 1) {
      ns1.b();
      ns1.b(0.0F, -1.0F, 0.0F);
      a(un1, i1, -0.5D, -0.5D, -0.5D);
      ns1.a();
    } else if (j1 == 13) {
      un1.f();
      GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
      float f1 = 0.0625F;
      ns1.b();
      ns1.b(0.0F, -1.0F, 0.0F);
      a(un1, 0.0D, 0.0D, 0.0D, un1.a(0));
      ns1.a();
      ns1.b();
      ns1.b(0.0F, 1.0F, 0.0F);
      b(un1, 0.0D, 0.0D, 0.0D, un1.a(1));
      ns1.a();
      ns1.b();
      ns1.b(0.0F, 0.0F, -1.0F);
      ns1.c(0.0F, 0.0F, f1);
      c(un1, 0.0D, 0.0D, 0.0D, un1.a(2));
      ns1.c(0.0F, 0.0F, -f1);
      ns1.a();
      ns1.b();
      ns1.b(0.0F, 0.0F, 1.0F);
      ns1.c(0.0F, 0.0F, -f1);
      d(un1, 0.0D, 0.0D, 0.0D, un1.a(3));
      ns1.c(0.0F, 0.0F, f1);
      ns1.a();
      ns1.b();
      ns1.b(-1.0F, 0.0F, 0.0F);
      ns1.c(f1, 0.0F, 0.0F);
      e(un1, 0.0D, 0.0D, 0.0D, un1.a(4));
      ns1.c(-f1, 0.0F, 0.0F);
      ns1.a();
      ns1.b();
      ns1.b(1.0F, 0.0F, 0.0F);
      ns1.c(-f1, 0.0F, 0.0F);
      f(un1, 0.0D, 0.0D, 0.0D, un1.a(5));
      ns1.c(f1, 0.0F, 0.0F);
      ns1.a();
      GL11.glTranslatef(0.5F, 0.5F, 0.5F);
    } else if (j1 == 6) {
      ns1.b();
      ns1.b(0.0F, -1.0F, 0.0F);
      b(un1, i1, -0.5D, -0.5D, -0.5D);
      ns1.a();
    } else if (j1 == 2) {
      ns1.b();
      ns1.b(0.0F, -1.0F, 0.0F);
      a(un1, -0.5D, -0.5D, -0.5D, 0.0D, 0.0D);
      ns1.a();
    } else if (j1 == 10) {
      for (int k1 = 0; k1 < 2; k1++) {
        if (k1 == 0)
          un1.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 0.5F);
        if (k1 == 1)
          un1.a(0.0F, 0.0F, 0.5F, 1.0F, 0.5F, 1.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        ns1.b();
        ns1.b(0.0F, -1.0F, 0.0F);
        a(un1, 0.0D, 0.0D, 0.0D, un1.a(0));
        ns1.a();
        ns1.b();
        ns1.b(0.0F, 1.0F, 0.0F);
        b(un1, 0.0D, 0.0D, 0.0D, un1.a(1));
        ns1.a();
        ns1.b();
        ns1.b(0.0F, 0.0F, -1.0F);
        c(un1, 0.0D, 0.0D, 0.0D, un1.a(2));
        ns1.a();
        ns1.b();
        ns1.b(0.0F, 0.0F, 1.0F);
        d(un1, 0.0D, 0.0D, 0.0D, un1.a(3));
        ns1.a();
        ns1.b();
        ns1.b(-1.0F, 0.0F, 0.0F);
        e(un1, 0.0D, 0.0D, 0.0D, un1.a(4));
        ns1.a();
        ns1.b();
        ns1.b(1.0F, 0.0F, 0.0F);
        f(un1, 0.0D, 0.0D, 0.0D, un1.a(5));
        ns1.a();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      }
    }
    else if (j1 == 11) {
      for (int l1 = 0; l1 < 4; l1++) {
        float f2 = 0.125F;
        if (l1 == 0)
          un1.a(0.5F - f2, 0.0F, 0.0F, 0.5F + f2, 1.0F, f2 * 2.0F);
        if (l1 == 1)
          un1.a(0.5F - f2, 0.0F, 1.0F - f2 * 2.0F, 0.5F + f2, 1.0F, 1.0F);
        f2 = 0.0625F;
        if (l1 == 2)
          un1.a(0.5F - f2, 1.0F - f2 * 3.0F, -f2 * 2.0F, 0.5F + f2, 1.0F - f2, 1.0F + f2 * 2.0F);
        if (l1 == 3)
          un1.a(0.5F - f2, 0.5F - f2 * 3.0F, -f2 * 2.0F, 0.5F + f2, 0.5F - f2, 1.0F + f2 * 2.0F);
        GL11.glTranslatef(-0.5F, -0.5F, -0.5F);
        ns1.b();
        ns1.b(0.0F, -1.0F, 0.0F);
        a(un1, 0.0D, 0.0D, 0.0D, un1.a(0));
        ns1.a();
        ns1.b();
        ns1.b(0.0F, 1.0F, 0.0F);
        b(un1, 0.0D, 0.0D, 0.0D, un1.a(1));
        ns1.a();
        ns1.b();
        ns1.b(0.0F, 0.0F, -1.0F);
        c(un1, 0.0D, 0.0D, 0.0D, un1.a(2));
        ns1.a();
        ns1.b();
        ns1.b(0.0F, 0.0F, 1.0F);
        d(un1, 0.0D, 0.0D, 0.0D, un1.a(3));
        ns1.a();
        ns1.b();
        ns1.b(-1.0F, 0.0F, 0.0F);
        e(un1, 0.0D, 0.0D, 0.0D, un1.a(4));
        ns1.a();
        ns1.b();
        ns1.b(1.0F, 0.0F, 0.0F);
        f(un1, 0.0D, 0.0D, 0.0D, un1.a(5));
        ns1.a();
        GL11.glTranslatef(0.5F, 0.5F, 0.5F);
      }

      un1.a(0.0F, 0.0F, 0.0F, 1.0F, 1.0F, 1.0F); } else {
      ModLoader.RenderInvBlock(this, un1, i1, j1);
    }
  }

  public static boolean a(int i1) {
    if (i1 == 0)
      return true;
    if (i1 == 13)
      return true;
    if (i1 == 10)
      return true;
    return ModLoader.RenderBlockIsItemFull3D(i1);
  }
}
