/*     */   public int validate(QName[] children, int offset, int length)
/*     */   {
/* 166 */     if (this.fOrdered) {
/* 167 */       int inIndex = 0;
/* 168 */       for (int outIndex = 0; outIndex < length; outIndex++)
/*     */       {
/* 171 */         QName curChild = children[(offset + outIndex)];

