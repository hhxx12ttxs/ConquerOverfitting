/*  54:132 */     double[] ranks = this.naturalRanking.rank(z);
/*  55:    */
/*  56:134 */     double sumRankX = 0.0D;
/*  58:141 */       sumRankX += ranks[i];
/*  59:    */     }
/*  60:148 */     double U1 = sumRankX - x.length * (x.length + 1) / 2;

