/*  96:199 */       matrix.setEntry(zIndex, getSlackVariableOffset() - 1, getInvertedCoefficientSum(objectiveCoefficients));
/*  97:    */     }
/*  98:204 */     int slackVar = 0;
/* 207:353 */       int col = 0;
/* 208:354 */       for (int j = 0; j < getWidth(); j++) {
/* 209:355 */         if (!columnsToDrop.contains(Integer.valueOf(j))) {

