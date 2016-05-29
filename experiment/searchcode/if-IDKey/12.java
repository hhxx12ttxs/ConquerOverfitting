/*    */   public boolean equals(Object other)
/*    */   {
/* 63 */     if (!(other instanceof IDKey)) {
/* 64 */       return false;
/*    */     }
/* 66 */     IDKey idKey = (IDKey)other;
/* 67 */     if (this.id != idKey.id) {
/* 68 */       return false;

