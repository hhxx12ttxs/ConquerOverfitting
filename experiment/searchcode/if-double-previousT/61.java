/*     */   public int getHandNum(int num)
/*     */   {
/* 234 */     if (!isWaiting) {
/* 235 */       previoustHandState = currentHandState;
/* 330 */         MovingFrameCount = 0;
/* 331 */       } else if (currentHandState == 4) {
/* 332 */         double prob = getStateProb(StateList, 4,

