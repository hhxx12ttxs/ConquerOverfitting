/* 22:   */   public boolean equals(Object other)
/* 23:   */   {
/* 24:63 */     if (!(other instanceof IDKey)) {
/* 25:64 */       return false;
/* 28:67 */     if (this.id != idKey.id) {
/* 29:68 */       return false;
/* 30:   */     }
/* 31:71 */     return this.value == idKey.value;

