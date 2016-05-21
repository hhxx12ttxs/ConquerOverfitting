  }
  private static void assertComplexEqual(
  private static boolean[] _complex = {true,false};
  private static boolean[] _overwrite = {true};
    float[][][] h = fft.applyInverse(g);
    if (complex)
      assertComplexEqual(n1,n2,n3,f,h);
    float[][] h = fft.applyInverse(g);
    if (complex)
      assertComplexEqual(n1,n2,f,h);
    float[] h = fft.applyInverse(g);
    if (complex)
      assertComplexEqual(n1,f,h);
    int n1, int n2, float[][] ce, float[][] ca) 

