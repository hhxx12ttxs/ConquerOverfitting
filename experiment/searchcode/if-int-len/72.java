/*    */ public class FrenchMinimalStemmer
/*    */ {
/*    */   public int stem(char[] s, int len)
/*    */   {
/* 64 */     if (len < 6) {
/* 65 */       return len;
/*    */     }
/* 67 */     if (s[(len - 1)] == &#39;x&#39;) {
/* 68 */       if ((s[(len - 3)] == &#39;a&#39;) &amp;&amp; (s[(len - 2)] == &#39;u&#39;))

