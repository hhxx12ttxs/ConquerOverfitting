/*  172:     */     }
/*  173: 270 */     if (abort) {
/*  174: 271 */       throw new NonMonotonicSequenceException(Double.valueOf(val[index]), Double.valueOf(previous), index, dir, strict);
/*  388: 623 */     double result = sHighPrev + (prodLowSum + sLowSum);
/*  389: 625 */     if (Double.isNaN(result))
/*  390:     */     {

