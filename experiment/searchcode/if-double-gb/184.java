import java.io.PrintStream;
import java.util.Calendar;
import java.util.Random;

public class of
  implements bf
{
  private Random j;
  private lq k;
  private lq l;
  private lq m;
  private lq n;
  private lq o;
  public lq a;
  public lq b;
  public lq c;
  private da p;
  private double[] q;
  private double[] r = new double[256];
  private double[] s = new double[256];
  private double[] t = new double[256];

  private do1 u = new ku();
  private gb[] v;
  double[] d;
  double[] e;
  double[] f;
  double[] g;
  double[] h;
  int[][] i = new int[32][32];
  private double[] w;

  public of(da paramda, long paramLong)
  {
    this.p = paramda;

    this.j = new Random(paramLong);
    this.k = new lq(this.j, 16);
    this.l = new lq(this.j, 16);
    this.m = new lq(this.j, 8);
    this.n = new lq(this.j, 4);
    this.o = new lq(this.j, 4);

    this.a = new lq(this.j, 10);
    this.b = new lq(this.j, 16);

    this.c = new lq(this.j, 8);
    BiomeTerrain.initialize(this.p, paramLong);
  }

  public void a(int paramInt1, int paramInt2, byte[] paramArrayOfByte, gb[] paramArrayOfgb, double[] paramArrayOfDouble)
  {
    BiomeTerrain.updateWorld(this.p);
    int i1 = 4;
    int i2 = BiomeTerrain.getWaterLevel();

    int i3 = i1 + 1;
    int i4 = 17;
    int i5 = i1 + 1;
    this.q = a(this.q, paramInt1 * i1, 0, paramInt2 * i1, i3, i4, i5);

    for (int i6 = 0; i6 < i1; i6++)
      for (int i7 = 0; i7 < i1; i7++)
        for (int i8 = 0; i8 < 16; i8++) {
          double d1 = 0.125D;
          double d2 = this.q[(((i6 + 0) * i5 + (i7 + 0)) * i4 + (i8 + 0))];
          double d3 = this.q[(((i6 + 0) * i5 + (i7 + 1)) * i4 + (i8 + 0))];
          double d4 = this.q[(((i6 + 1) * i5 + (i7 + 0)) * i4 + (i8 + 0))];
          double d5 = this.q[(((i6 + 1) * i5 + (i7 + 1)) * i4 + (i8 + 0))];

          double d6 = (this.q[(((i6 + 0) * i5 + (i7 + 0)) * i4 + (i8 + 1))] - d2) * d1;
          double d7 = (this.q[(((i6 + 0) * i5 + (i7 + 1)) * i4 + (i8 + 1))] - d3) * d1;
          double d8 = (this.q[(((i6 + 1) * i5 + (i7 + 0)) * i4 + (i8 + 1))] - d4) * d1;
          double d9 = (this.q[(((i6 + 1) * i5 + (i7 + 1)) * i4 + (i8 + 1))] - d5) * d1;

          for (int i9 = 0; i9 < 8; i9++) {
            double d10 = 0.25D;

            double d11 = d2;
            double d12 = d3;
            double d13 = (d4 - d2) * d10;
            double d14 = (d5 - d3) * d10;

            for (int i10 = 0; i10 < 4; i10++) {
              int i11 = i10 + i6 * 4 << 11 | 0 + i7 * 4 << 7 | i8 * 8 + i9;
              int i12 = 128;
              double d15 = 0.25D;

              double d16 = d11;
              double d17 = (d12 - d11) * d15;
              for (int i13 = 0; i13 < 4; i13++)
              {
                double d18 = paramArrayOfDouble[((i6 * 4 + i10) * 16 + (i7 * 4 + i13))];
                int i14 = 0;
                if (i8 * 8 + i9 < i2) {
                  if ((d18 < BiomeTerrain.getIceThreshold()) && (i8 * 8 + i9 >= i2 - 1))
                    i14 = ly.aT.bl;
                  else {
                    i14 = ly.B.bl;
                  }
                }
                if (d16 > 0.0D) {
                  i14 = ly.t.bl;
                }

                paramArrayOfByte[i11] = (byte)i14;
                i11 += i12;
                d16 += d17;
              }
              d11 += d13;
              d12 += d14;
            }

            d2 += d6;
            d3 += d7;
            d4 += d8;
            d5 += d9;
          }
        }
  }

  public void a(int paramInt1, int paramInt2, byte[] paramArrayOfByte, gb[] paramArrayOfBiomeBase)
  {
		if (BiomeTerrain.getOldGen() == false)
		{
	    int i1 = BiomeTerrain.getWaterLevel();
	    double d1 = 0.03125D;

	    this.r = this.n.a(this.r, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1, d1, 1.0D);
	    this.s = this.n.a(this.s, paramInt1 * 16, 109.0134D, paramInt2 * 16, 16, 1, 16, d1, 1.0D, d1);
	    this.t = this.o.a(this.t, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);

	    for (int i2 = 0; i2 < 16; i2++)
	      for (int i3 = 0; i3 < 16; i3++) {
	        gb localBiomeBase = paramArrayOfBiomeBase[(i2 + i3 * 16)];
	        int i4 = this.r[(i2 + i3 * 16)] + this.j.nextDouble() * 0.2D > 0.0D ? 1 : 0;
	        int i5 = this.s[(i2 + i3 * 16)] + this.j.nextDouble() * 0.2D > 3.0D ? 1 : 0;
	        int i6 = (int)(this.t[(i2 + i3 * 16)] / 3.0D + 3.0D + this.j.nextDouble() * 0.25D);

	        int i7 = -1;

	        byte i8 = localBiomeBase.o;
	        byte i9 = localBiomeBase.p;

	        for (int i10 = 127; i10 >= 0; i10--) {
	          int i11 = (i3 * 16 + i2) * 128 + i10;

	          if ((i10 <= 0 + this.j.nextInt(5)) && (BiomeTerrain.createadminium(i10))) {
	            paramArrayOfByte[i11] = (byte)ly.z.bl;
	          } else {
	            int i12 = paramArrayOfByte[i11];

	            if (i12 == 0)
	              i7 = -1;
	            else if (i12 == ly.t.bl)
	              if (i7 == -1) {
	                if (i6 <= 0) {
	                  i8 = 0;
	                  i9 = (byte)ly.t.bl;
	                } else if ((i10 >= i1 - 4) && (i10 <= i1 + 1)) {
	                  i8 = localBiomeBase.o;
	                  i9 = localBiomeBase.p;
	                  if (i5 != 0) i8 = 0;
	                  if (i5 != 0) i9 = (byte)ly.F.bl;
	                  if (i4 != 0) i8 = (byte)ly.E.bl;
	                  if (i4 != 0) i9 = (byte)ly.E.bl;
	                }

	                if ((i10 < i1) && (i8 == 0)) i8 = (byte)ly.A.bl;

	                i7 = i6;
	                if (i10 >= i1 - 1) paramArrayOfByte[i11] = i8; else
	                  paramArrayOfByte[i11] = i9;
	              } else if (i7 > 0) {
	                i7--;
	                paramArrayOfByte[i11] = i9;
	                if ((i7 == 0) && (i9 == ly.E.bl)) {
	                  i7 = this.j.nextInt(4);
	                  i9 = (byte)ly.Q.bl;
	                }
	              }
	          }
	        }
	      }
		}
		else
		{
	    int i1 = BiomeTerrain.getWaterLevel();

	    double d1 = 0.03125D;
	    this.r = this.n.a(this.r, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1, d1, 1.0D);
	    this.s = this.n.a(this.s, paramInt2 * 16, 109.0134D, paramInt1 * 16, 16, 1, 16, d1, 1.0D, d1);
	    this.t = this.o.a(this.t, paramInt1 * 16, paramInt2 * 16, 0.0D, 16, 16, 1, d1 * 2.0D, d1 * 2.0D, d1 * 2.0D);

	    for (int i2 = 0; i2 < 16; i2++)
	      for (int i3 = 0; i3 < 16; i3++) {
	        gb localBiomeBase = paramArrayOfBiomeBase[(i2 * 16 + i3)];
	        int i4 = this.r[(i2 * 16 + i3)] + this.j.nextDouble() * 0.2D > 0.0D ? 1 : 0;
	        int i5 = this.s[(i2 * 16 + i3)] + this.j.nextDouble() * 0.2D > 3.0D ? 1 : 0;
	        int i6 = (int)(this.t[(i2 * 16 + i3)] / 3.0D + 3.0D + this.j.nextDouble() * 0.25D);

	        int i7 = -1;

	        byte i8 = localBiomeBase.o;
	        byte i9 = localBiomeBase.p;

	        for (int i10 = 127; i10 >= 0; i10--) {
	          int i11 = (i2 * 16 + i3) * 128 + i10;

	          if ((i10 <= 0 + this.j.nextInt(5)) && (BiomeTerrain.createadminium(i10))) {
	            paramArrayOfByte[i11] = BiomeTerrain.getadminium();
	          } else {
	            int i12 = paramArrayOfByte[i11];

	            if (i12 == 0)
	              i7 = -1;
	            else if (i12 == ly.t.bl)
	              if (i7 == -1) {
	                if (i6 <= 0) {
	                  i8 = 0;
	                  i9 = (byte)ly.t.bl;
	                } else if ((i10 >= i1 - 4) && (i10 <= i1 + 1)) {
	                  i8 = localBiomeBase.o;
	                  i9 = localBiomeBase.p;
	                  if (i5 != 0) i8 = 0;
	                  if (i5 != 0) i9 = (byte)ly.F.bl;
	                  if (i4 != 0) i8 = (byte)ly.E.bl;
	                  if (i4 != 0) i9 = (byte)ly.E.bl;
	                }

	                if ((i10 < i1) && (i8 == 0)) i8 = (byte)ly.A.bl;

	                i7 = i6;
	                if (i10 >= i1 - 1) paramArrayOfByte[i11] = i8; else
	                  paramArrayOfByte[i11] = i9;
	              } else if (i7 > 0) {
	                i7--;
	                paramArrayOfByte[i11] = i9;
	                if ((i7 == 0) && (i9 == ly.E.bl)) {
	                  i7 = this.j.nextInt(4);
	                  i9 = (byte)ly.Q.bl;
	                }
	              }
	          }
	        }
	      }
		}
  }

  public gr c(int paramInt1, int paramInt2)
  {
    return b(paramInt1, paramInt2);
  }

  public gr b(int paramInt1, int paramInt2) {
    BiomeTerrain.updateWorld(this.p);
    this.j.setSeed(paramInt1 * 341873128712L + paramInt2 * 132897987541L);
    BiomeTerrain.updateRandom(this.j);

    byte[] arrayOfByte = new byte[32768];
    gr localgr = new gr(this.p, arrayOfByte, paramInt1, paramInt2);
    this.v = this.p.a().a(this.v, paramInt1 * 16, paramInt2 * 16, 16, 16);
    double[] arrayOfDouble = this.p.a().a;

    a(paramInt1, paramInt2, arrayOfByte, this.v, arrayOfDouble);
    a(paramInt1, paramInt2, arrayOfByte, this.v);

    BiomeTerrain.processChunkBlocks(arrayOfByte, this.v);

    this.u.a(this, this.p, paramInt1, paramInt2, arrayOfByte);

    localgr.b();

    return localgr;
  }

  private double[] a(double[] paramArrayOfDouble, int paramInt1, int paramInt2, int paramInt3, int paramInt4, int paramInt5, int paramInt6)
  {
    BiomeTerrain.updateWorld(this.p);
    if (paramArrayOfDouble == null) {
      paramArrayOfDouble = new double[paramInt4 * paramInt5 * paramInt6];
    }

    double d1 = 684.41200000000003D * BiomeTerrain.getFractureHorizontal();
    double d2 = 684.41200000000003D * BiomeTerrain.getFractureVertical();

    double[] arrayOfDouble1 = this.p.a().a;
    double[] arrayOfDouble2 = this.p.a().b;
    this.g = this.a.a(this.g, paramInt1, paramInt3, paramInt4, paramInt6, 1.121D, 1.121D, 0.5D);
    this.h = this.b.a(this.h, paramInt1, paramInt3, paramInt4, paramInt6, 200.0D, 200.0D, 0.5D);

    this.d = this.m.a(this.d, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1 / 80.0D, d2 / 160.0D, d1 / 80.0D);
    this.e = this.k.a(this.e, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);
    this.f = this.l.a(this.f, paramInt1, paramInt2, paramInt3, paramInt4, paramInt5, paramInt6, d1, d2, d1);

    int i1 = 0;
    int i2 = 0;

    int i3 = 16 / paramInt4;
    for (int i4 = 0; i4 < paramInt4; i4++) {
      int i5 = i4 * i3 + i3 / 2;

      for (int i6 = 0; i6 < paramInt6; i6++) {
        int i7 = i6 * i3 + i3 / 2;
        double d3 = arrayOfDouble1[(i5 * 16 + i7)];
        double d4 = arrayOfDouble2[(i5 * 16 + i7)] * d3;
        double d5 = 1.0D - d4;
        d5 *= d5;
        d5 *= d5;
        d5 = 1.0D - d5;

        double d6 = (this.g[i2] + 256.0D) / 512.0D;
        d6 *= d5;
        if (d6 > 1.0D) d6 = 1.0D;

        double d7 = this.h[i2] / 8000.0D;
        if (d7 < 0.0D) d7 = -d7 * 0.3D;
        d7 = d7 * 3.0D - 2.0D;

        if (d7 < 0.0D) {
          d7 /= 2.0D;
          if (d7 < -1.0D) d7 = -1.0D;
          d7 -= BiomeTerrain.getMaxAverageDepth();
          d7 /= 1.4D;
          d7 /= 2.0D;
          d6 = 0.0D;
        } else {
          if (d7 > 1.0D) d7 = 1.0D;
          d7 += BiomeTerrain.getMaxAverageHeight();
          d7 /= 8.0D;
        }

        if (d6 < 0.0D) d6 = 0.0D;
        d6 += 0.5D;
        d7 = d7 * paramInt5 / 16.0D;

        double d8 = paramInt5 / 2.0D + d7 * 4.0D;

        i2++;

        for (int i8 = 0; i8 < paramInt5; i8++) {
          double d9 = 0.0D;

          double d10 = (i8 - d8) * 12.0D / d6;
          if (d10 < 0.0D) d10 *= 4.0D;

          double d11 = this.e[i1] / 512.0D * BiomeTerrain.getVolatility1();
          double d12 = this.f[i1] / 512.0D * BiomeTerrain.getVolatility2();

          double d13 = (this.d[i1] / 10.0D + 1.0D) / 2.0D;
          if (d13 < BiomeTerrain.getVolatilityWeight1())
            d9 = d11;
          else if (d13 > BiomeTerrain.getVolatilityWeight2())
            d9 = d12;
          else
            d9 = d11 + (d12 - d11) * d13;
          d9 -= d10;

          if (i8 > paramInt5 - 4) {
            double d14 = (i8 - (paramInt5 - 4)) / 3.0F;
            d9 = d9 * (1.0D - d14) + -10.0D * d14;
          }

          paramArrayOfDouble[i1] = d9;
          i1++;
        }
      }
    }
    return paramArrayOfDouble;
  }

  public boolean a(int paramInt1, int paramInt2) {
    return true;
  }

  public void a(bf parambf, int paramInt1, int paramInt2)
  {
    BiomeTerrain.updateWorld(this.p);

    dx.a = true;
    int i1 = paramInt1 * 16;
    int i2 = paramInt2 * 16;

    gb localgb = this.p.a().a(i1 + 16, i2 + 16);

    this.j.setSeed(this.p.l());
    long l1 = this.j.nextLong() / 2L * 2L + 1L;
    long l2 = this.j.nextLong() / 2L * 2L + 1L;
    this.j.setSeed(paramInt1 * l1 + paramInt2 * l2 ^ this.p.l());
    double d1 = 0.25D;

    BiomeTerrain.updateRandom(this.j);
    BiomeTerrain.processUndergroundDeposits(i1, i2);
    BiomeTerrain.processUndergroundLakes(paramInt1, paramInt2);
    int i3;
    int i4;
    int i5;
    int i6;
   if (!BiomeTerrain.getDisableNotchPonds())
    {
    if (this.j.nextInt(4) == 0) {
      i3 = i1 + this.j.nextInt(16) + 8;
      i4 = this.j.nextInt(128);
      i5 = i2 + this.j.nextInt(16) + 8;
      new bw(ly.B.bl).a(this.p, this.j, i3, i4, i5);
    }

      if (this.j.nextInt(8) == 0) {
        i3 = i1 + this.j.nextInt(16) + 8;
        i4 = this.j.nextInt(this.j.nextInt(120) + 8);
        i5 = i2 + this.j.nextInt(16) + 8;
        if ((i4 < 64) || (this.j.nextInt(10) == 0)) new bw(ly.D.bl).a(this.p, this.j, i3, i4, i5);
      }

    }

    d1 = 0.5D;
    i3 = (int)((this.c.a(i1 * d1, i2 * d1) / 8.0D + this.j.nextDouble() * 4.0D + 4.0D) / 3.0D);
    i4 = 0;
    if (this.j.nextInt(10) == 0) i4++;
    i4 = BiomeTerrain.processTrees(localgb, i4, i3);
    for (i5 = 0; i5 < i4; i5++)
    {
      boolean CustomObjectGenerated = false;
      i6 = i1 + this.j.nextInt(16) + 8;
      int i7 = i2 + this.j.nextInt(16) + 8;
      is localWorldGenerator = null;
      if ((!BiomeTerrain.getNotchBiomeTrees()) && (!BiomeTerrain.getCustomObjects()))
      {
        localWorldGenerator = new oj();
      }
      else if ((BiomeTerrain.getNotchBiomeTrees()) && (!BiomeTerrain.getCustomObjects()))
      {
        localWorldGenerator = localgb.a(this.j);
      }
      else if ((!BiomeTerrain.getNotchBiomeTrees()) && (BiomeTerrain.getCustomObjects())) {
        int paramRandom = this.j.nextInt(BiomeTerrain.getObjectSpawnRatio() + 1);
        if (paramRandom < 1)
        {
          CustomObjectGenerated = true;
          localWorldGenerator = new CustomObjectGen();
        }
        else
        {
          localWorldGenerator = new oj();
        }
      }
      else
      {
        int paramRandom = this.j.nextInt(BiomeTerrain.getObjectSpawnRatio() + 1);
        if (paramRandom < 1)
        {
          CustomObjectGenerated = true;
          localWorldGenerator = new CustomObjectGen();
        }
        else
        {
          localWorldGenerator = localgb.a(this.j);
        }
      }
      if (!CustomObjectGenerated)
      {
        localWorldGenerator.a(1.0D, 1.0D, 1.0D);
        localWorldGenerator.a(this.p, this.j, i6, this.p.d(i6, i7), i7);
      }
      else
      {
        localWorldGenerator.CustomObjectBuilder(this.p, this.j, i6, this.p.d(i6, i7), i7, localgb);
      }
    }
    BiomeTerrain.processAboveGroundMaterials(i1, i2);
    i5 = 0;
    int i7;
    int i8;
    int i9;

    this.w = this.p.a().a(this.w, i1 + 8, i2 + 8, 16, 16);
    for (i6 = i1 + 8; i6 < i1 + 8 + 16; i6++) {
      for (i7 = i2 + 8; i7 < i2 + 8 + 16; i7++) {
        i8 = i6 - (i1 + 8);
        i9 = i7 - (i2 + 8);
        int i10 = this.p.e(i6, i7);
        double d2 = this.w[(i8 * 16 + i9)] - (i10 - 64) / 64.0D * 0.3D;
        if ((d2 >= 0.5D) || 
          (i10 <= 0) || (i10 >= 128) || (!this.p.e(i6, i10, i7)) || (!this.p.c(i6, i10 - 1, i7).c()) || 
          (this.p.c(i6, i10 - 1, i7) == gs.r)) continue; this.p.e(i6, i10, i7, ly.aS.bl);
      }

    }

    Calendar localCalendar = Calendar.getInstance();
    localCalendar.setTimeInMillis(System.currentTimeMillis());

    if ((localCalendar.get(2) == 3) && (localCalendar.get(5) == 1)) {
      i7 = i1 + this.j.nextInt(16) + 8;
      i8 = this.j.nextInt(128);
      i9 = i2 + this.j.nextInt(16) + 8;
      if ((this.p.a(i7, i8, i9) == 0) && (this.p.d(i7, i8 - 1, i9))) {
        System.out.println("added a chest!!");
        this.p.e(i7, i8, i9, ly.bj.bl);
      }
    }

    dx.a = false;
  }

  public boolean a(boolean paramBoolean, oe paramoe) {
    return true;
  }

  public boolean a() {
    return false;
  }

  public boolean b() {
    return true;
  }
}
