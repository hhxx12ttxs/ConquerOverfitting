/* 155 */     char[] c = pattern.toCharArray();
/* 156 */     int fmtCount = 0;
/* 157 */     while (pos.getIndex() < pattern.length()) {
/*     */       case &#39;{&#39;:
/* 163 */         fmtCount++;
/* 164 */         seekNonWs(pattern, pos);
/* 165 */         int start = pos.getIndex();

