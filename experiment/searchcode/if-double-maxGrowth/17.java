/*  18:    */   private double minReduction;
/*  19:    */   private double maxGrowth;
/*  20:    */
/*  21:    */   protected EmbeddedRungeKuttaIntegrator(String name, boolean fsal, double[] c, double[][] a, double[] b, RungeKuttaStepInterpolator prototype, double minStep, double maxStep, double scalAbsoluteTolerance, double scalRelativeTolerance)
/* 109:233 */         if (firstTime)
/* 110:    */         {
/* 111:234 */           double[] scale = new double[this.mainSetDimension];

