/*  84 */   private static int FullPages = -1;
/*     */   private static int currentHandState;
/*     */   private static int previoustHandState;
/*     */   public int getHandNum(int num)
/*     */   {
/* 234 */     if (!isWaiting) {
/* 235 */       previoustHandState = currentHandState;

