/*     */   public final void mul(Quat4d q1, Quat4d q2)
/*     */   {
/* 185 */     if ((this != q1) &amp;&amp; (this != q2)) {
/* 193 */       double w = q1.w * q2.w - q1.x * q2.x - q1.y * q2.y - q1.z * q2.z;
/* 194 */       double x = q1.w * q2.x + q2.w * q1.x + q1.y * q2.z - q1.z * q2.y;

