/*  14:    */   private final double mean;
/*  15:    */   private final double i2s2;
/*  16:    */   private final double norm;
/*  50: 93 */         double g = Gaussian.value(diff, Gaussian.this.norm, Gaussian.this.i2s2);
/*  51: 95 */         if (g == 0.0D) {
/*  52: 97 */           return 0.0D;
/*  53:    */         }

