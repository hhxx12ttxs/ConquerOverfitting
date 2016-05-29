/*  631:     */   public static double[] normalizeArray(double[] values, double normalizedSum)
/*  632:     */   {
/*  633:1055 */     if (Double.isInfinite(normalizedSum)) {
/*  634:1056 */       throw new MathIllegalArgumentException(LocalizedFormats.NORMALIZE_INFINITE, new Object[0]);
/*  635:     */     }
/*  636:1058 */     if (Double.isNaN(normalizedSum)) {

