/*     */   public Vector3d()
/*     */   {
/*     */   }
/*     */
/*     */   public final void cross(Vector3d v1, Vector3d v2)
/*     */   {
/* 130 */     double x = v1.y * v2.z - v1.z * v2.y;
/* 131 */     double y = v2.x * v1.z - v2.z * v1.x;
/* 132 */     this.z = (v1.x * v2.y - v1.y * v2.x);

