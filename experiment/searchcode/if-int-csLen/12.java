/*  942 */         int len = cslen - i;
/*      */
/*  944 */         if (len > 3) {
/*  945 */           cs1 = cs[(i + 1)];
/*      */   private static int charArrayIndexOf(char c, char[] cs, int cslen, int index) {
/* 1020 */     for (int i = index; i < cslen; i++) {
/* 1021 */       if (cs[i] == c) return i;

