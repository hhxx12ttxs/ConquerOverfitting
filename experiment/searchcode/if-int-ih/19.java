/*     */   private InstructionHandle ih;
/*     */   private int src_line;
/*     */
/*     */   public LineNumberGen(InstructionHandle ih, int src_line)
/*     */   public void updateTarget(InstructionHandle old_ih, InstructionHandle new_ih)
/*     */   {
/* 100 */     if (old_ih != this.ih) {

