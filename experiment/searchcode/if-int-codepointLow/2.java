/*  74 */     return outsideOf(0, codepoint);
/*     */   }
/*     */
/*     */   public static UnicodeEscaper outsideOf(int codepointLow, int codepointHigh)
/*  85 */     return new UnicodeEscaper(codepointLow, codepointHigh, false);
/*     */   }
/*     */
/*     */   public static UnicodeEscaper between(int codepointLow, int codepointHigh)

