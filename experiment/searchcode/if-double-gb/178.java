import java.util.Random;

public class oc
{
  private lr e;
  private lr f;
  private lr g;
  public double[] a;
  public double[] b;
  public double[] c;
  public gb[] d;

  protected oc()
  {
  }

  public oc(da paramda)
  {
    this.e = new lr(new Random(paramda.l() * 9871L), 4);
    this.f = new lr(new Random(paramda.l() * 39811L), 4);
    this.g = new lr(new Random(paramda.l() * 543321L), 2);
  }

  public gb a(ov paramov)
  {
    return a(paramov.a << 4, paramov.b << 4);
  }

  public gb a(int paramInt1, int paramInt2) {
    return a(paramInt1, paramInt2, 1, 1)[0];
  }

  public gb[] a(int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    this.d = a(this.d, paramInt1, paramInt2, paramInt3, paramInt4);
    return this.d;
  }

  public double[] a(double[] paramArrayOfDouble, int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
    if ((paramArrayOfDouble == null) || (paramArrayOfDouble.length < paramInt3 * paramInt4)) {
      paramArrayOfDouble = new double[paramInt3 * paramInt4];
    }

    paramArrayOfDouble = this.e.a(paramArrayOfDouble, paramInt1, paramInt2, paramInt3, paramInt4, 0.025000000372529D, 0.025000000372529D, 0.25D);
    this.c = this.g.a(this.c, paramInt1, paramInt2, paramInt3, paramInt4, 0.25D, 0.25D, 0.5882352941176471D);

    int i = 0;
    for (int j = 0; j < paramInt3; j++) {
      for (int k = 0; k < paramInt4; k++) {
        double d1 = this.c[i] * 1.1D + 0.5D;

        double d2 = 0.01D;
        double d3 = 1.0D - d2;
        double d4 = (paramArrayOfDouble[i] * 0.15D + 0.7D) * d3 + d1 * d2;
        d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

        if (d4 < BiomeTerrain.getMinimumTemperature())
          d4 = BiomeTerrain.getMinimumTemperature();
        if (d4 > BiomeTerrain.getMaximumTemperature()) {
          d4 = BiomeTerrain.getMaximumTemperature();
        }
        paramArrayOfDouble[i] = d4;
        i++;
      }

    }

    return paramArrayOfDouble;
  }

  public gb[] a(gb[] paramArrayOfgb, int paramInt1, int paramInt2, int paramInt3, int paramInt4)
  {
    if ((paramArrayOfgb == null) || (paramArrayOfgb.length < paramInt3 * paramInt4)) {
      paramArrayOfgb = new gb[paramInt3 * paramInt4];
    }

    this.a = this.e.a(this.a, paramInt1, paramInt2, paramInt3, paramInt3, 0.025000000372529D, 0.025000000372529D, 0.25D);
    this.b = this.f.a(this.b, paramInt1, paramInt2, paramInt3, paramInt3, 0.0500000007450581D, 0.0500000007450581D, 0.3333333333333333D);
    this.c = this.g.a(this.c, paramInt1, paramInt2, paramInt3, paramInt3, 0.25D, 0.25D, 0.5882352941176471D);

    int i = 0;
    for (int j = 0; j < paramInt3; j++) {
      for (int k = 0; k < paramInt4; k++)
      {
        double d1 = this.c[i] * 1.1D + 0.5D;

        double d2 = 0.01D;
        double d3 = 1.0D - d2;
        double d4 = (this.a[i] * 0.15D + 0.7D) * d3 + d1 * d2;
        d2 = 0.002D;
        d3 = 1.0D - d2;
        double d5 = (this.b[i] * 0.15D + 0.5D) * d3 + d1 * d2;
        d4 = 1.0D - (1.0D - d4) * (1.0D - d4);

        if (d4 < BiomeTerrain.getMinimumTemperature())
          d4 = BiomeTerrain.getMinimumTemperature();
        if (d5 < BiomeTerrain.getMinimumMoisture())
          d5 = BiomeTerrain.getMinimumMoisture();
        if (d4 > BiomeTerrain.getMaximumTemperature())
          d4 = BiomeTerrain.getMaximumTemperature();
        if (d5 > BiomeTerrain.getMaximumMoisture()) {
          d5 = BiomeTerrain.getMaximumMoisture();
        }
        this.a[i] = d4;
        this.b[i] = d5;

        paramArrayOfgb[(i++)] = gb.a(d4, d5);
      }

    }

    return paramArrayOfgb;
  }
}
