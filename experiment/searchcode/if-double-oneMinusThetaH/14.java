/* 22:   */   protected void computeInterpolatedStateAndDerivatives(double theta, double oneMinusThetaH)
/* 23:   */   {
/* 24:86 */     if ((this.previousState != null) &amp;&amp; (theta <= 0.5D))
/* 34:93 */         this.interpolatedState[i] = (this.currentState[i] - oneMinusThetaH * this.yDotK[0][i]);
/* 35:   */       }
/* 36:95 */       System.arraycopy(this.yDotK[0], 0, this.interpolatedDerivatives, 0, this.interpolatedDerivatives.length);

