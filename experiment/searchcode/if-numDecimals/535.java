/*    1:     */ package net.minecraft.util.org.apache.commons.lang3.math;
/*    2:     */ 
/*    3:     */ import java.lang.reflect.Array;
/*    4:     */ import java.math.BigDecimal;
/*    5:     */ import java.math.BigInteger;
/*    6:     */ import net.minecraft.util.org.apache.commons.lang3.StringUtils;
/*    7:     */ 
/*    8:     */ public class NumberUtils
/*    9:     */ {
/*   10:  34 */   public static final Long LONG_ZERO = Long.valueOf(0L);
/*   11:  36 */   public static final Long LONG_ONE = Long.valueOf(1L);
/*   12:  38 */   public static final Long LONG_MINUS_ONE = Long.valueOf(-1L);
/*   13:  40 */   public static final Integer INTEGER_ZERO = Integer.valueOf(0);
/*   14:  42 */   public static final Integer INTEGER_ONE = Integer.valueOf(1);
/*   15:  44 */   public static final Integer INTEGER_MINUS_ONE = Integer.valueOf(-1);
/*   16:  46 */   public static final Short SHORT_ZERO = Short.valueOf((short)0);
/*   17:  48 */   public static final Short SHORT_ONE = Short.valueOf((short)1);
/*   18:  50 */   public static final Short SHORT_MINUS_ONE = Short.valueOf((short)-1);
/*   19:  52 */   public static final Byte BYTE_ZERO = Byte.valueOf((byte)0);
/*   20:  54 */   public static final Byte BYTE_ONE = Byte.valueOf((byte)1);
/*   21:  56 */   public static final Byte BYTE_MINUS_ONE = Byte.valueOf((byte)-1);
/*   22:  58 */   public static final Double DOUBLE_ZERO = Double.valueOf(0.0D);
/*   23:  60 */   public static final Double DOUBLE_ONE = Double.valueOf(1.0D);
/*   24:  62 */   public static final Double DOUBLE_MINUS_ONE = Double.valueOf(-1.0D);
/*   25:  64 */   public static final Float FLOAT_ZERO = Float.valueOf(0.0F);
/*   26:  66 */   public static final Float FLOAT_ONE = Float.valueOf(1.0F);
/*   27:  68 */   public static final Float FLOAT_MINUS_ONE = Float.valueOf(-1.0F);
/*   28:     */   
/*   29:     */   public static int toInt(String str)
/*   30:     */   {
/*   31: 100 */     return toInt(str, 0);
/*   32:     */   }
/*   33:     */   
/*   34:     */   public static int toInt(String str, int defaultValue)
/*   35:     */   {
/*   36: 121 */     if (str == null) {
/*   37: 122 */       return defaultValue;
/*   38:     */     }
/*   39:     */     try
/*   40:     */     {
/*   41: 125 */       return Integer.parseInt(str);
/*   42:     */     }
/*   43:     */     catch (NumberFormatException nfe) {}
/*   44: 127 */     return defaultValue;
/*   45:     */   }
/*   46:     */   
/*   47:     */   public static long toLong(String str)
/*   48:     */   {
/*   49: 149 */     return toLong(str, 0L);
/*   50:     */   }
/*   51:     */   
/*   52:     */   public static long toLong(String str, long defaultValue)
/*   53:     */   {
/*   54: 170 */     if (str == null) {
/*   55: 171 */       return defaultValue;
/*   56:     */     }
/*   57:     */     try
/*   58:     */     {
/*   59: 174 */       return Long.parseLong(str);
/*   60:     */     }
/*   61:     */     catch (NumberFormatException nfe) {}
/*   62: 176 */     return defaultValue;
/*   63:     */   }
/*   64:     */   
/*   65:     */   public static float toFloat(String str)
/*   66:     */   {
/*   67: 199 */     return toFloat(str, 0.0F);
/*   68:     */   }
/*   69:     */   
/*   70:     */   public static float toFloat(String str, float defaultValue)
/*   71:     */   {
/*   72: 222 */     if (str == null) {
/*   73: 223 */       return defaultValue;
/*   74:     */     }
/*   75:     */     try
/*   76:     */     {
/*   77: 226 */       return Float.parseFloat(str);
/*   78:     */     }
/*   79:     */     catch (NumberFormatException nfe) {}
/*   80: 228 */     return defaultValue;
/*   81:     */   }
/*   82:     */   
/*   83:     */   public static double toDouble(String str)
/*   84:     */   {
/*   85: 251 */     return toDouble(str, 0.0D);
/*   86:     */   }
/*   87:     */   
/*   88:     */   public static double toDouble(String str, double defaultValue)
/*   89:     */   {
/*   90: 274 */     if (str == null) {
/*   91: 275 */       return defaultValue;
/*   92:     */     }
/*   93:     */     try
/*   94:     */     {
/*   95: 278 */       return Double.parseDouble(str);
/*   96:     */     }
/*   97:     */     catch (NumberFormatException nfe) {}
/*   98: 280 */     return defaultValue;
/*   99:     */   }
/*  100:     */   
/*  101:     */   public static byte toByte(String str)
/*  102:     */   {
/*  103: 303 */     return toByte(str, (byte)0);
/*  104:     */   }
/*  105:     */   
/*  106:     */   public static byte toByte(String str, byte defaultValue)
/*  107:     */   {
/*  108: 324 */     if (str == null) {
/*  109: 325 */       return defaultValue;
/*  110:     */     }
/*  111:     */     try
/*  112:     */     {
/*  113: 328 */       return Byte.parseByte(str);
/*  114:     */     }
/*  115:     */     catch (NumberFormatException nfe) {}
/*  116: 330 */     return defaultValue;
/*  117:     */   }
/*  118:     */   
/*  119:     */   public static short toShort(String str)
/*  120:     */   {
/*  121: 352 */     return toShort(str, (short)0);
/*  122:     */   }
/*  123:     */   
/*  124:     */   public static short toShort(String str, short defaultValue)
/*  125:     */   {
/*  126: 373 */     if (str == null) {
/*  127: 374 */       return defaultValue;
/*  128:     */     }
/*  129:     */     try
/*  130:     */     {
/*  131: 377 */       return Short.parseShort(str);
/*  132:     */     }
/*  133:     */     catch (NumberFormatException nfe) {}
/*  134: 379 */     return defaultValue;
/*  135:     */   }
/*  136:     */   
/*  137:     */   public static Number createNumber(String str)
/*  138:     */     throws NumberFormatException
/*  139:     */   {
/*  140: 451 */     if (str == null) {
/*  141: 452 */       return null;
/*  142:     */     }
/*  143: 454 */     if (StringUtils.isBlank(str)) {
/*  144: 455 */       throw new NumberFormatException("A blank string is not a valid number");
/*  145:     */     }
/*  146: 458 */     String[] hex_prefixes = { "0x", "0X", "-0x", "-0X", "#", "-#" };
/*  147: 459 */     int pfxLen = 0;
/*  148: 460 */     for (String pfx : hex_prefixes) {
/*  149: 461 */       if (str.startsWith(pfx))
/*  150:     */       {
/*  151: 462 */         pfxLen += pfx.length();
/*  152: 463 */         break;
/*  153:     */       }
/*  154:     */     }
/*  155: 466 */     if (pfxLen > 0)
/*  156:     */     {
/*  157: 467 */       char firstSigDigit = '\000';
/*  158: 468 */       for (int i = pfxLen; i < str.length(); i++)
/*  159:     */       {
/*  160: 469 */         firstSigDigit = str.charAt(i);
/*  161: 470 */         if (firstSigDigit != '0') {
/*  162:     */           break;
/*  163:     */         }
/*  164: 471 */         pfxLen++;
/*  165:     */       }
/*  166: 476 */       int hexDigits = str.length() - pfxLen;
/*  167: 477 */       if ((hexDigits > 16) || ((hexDigits == 16) && (firstSigDigit > '7'))) {
/*  168: 478 */         return createBigInteger(str);
/*  169:     */       }
/*  170: 480 */       if ((hexDigits > 8) || ((hexDigits == 8) && (firstSigDigit > '7'))) {
/*  171: 481 */         return createLong(str);
/*  172:     */       }
/*  173: 483 */       return createInteger(str);
/*  174:     */     }
/*  175: 485 */     char lastChar = str.charAt(str.length() - 1);
/*  176:     */     
/*  177:     */ 
/*  178:     */ 
/*  179: 489 */     int decPos = str.indexOf('.');
/*  180: 490 */     int expPos = str.indexOf('e') + str.indexOf('E') + 1;
/*  181:     */     
/*  182:     */ 
/*  183:     */ 
/*  184: 494 */     int numDecimals = 0;
/*  185:     */     String mant;
/*  186:     */     String dec;
/*  187: 495 */     if (decPos > -1)
/*  188:     */     {
/*  189:     */       String dec;
/*  190:     */       String dec;
/*  191: 497 */       if (expPos > -1)
/*  192:     */       {
/*  193: 498 */         if ((expPos < decPos) || (expPos > str.length())) {
/*  194: 499 */           throw new NumberFormatException(str + " is not a valid number.");
/*  195:     */         }
/*  196: 501 */         dec = str.substring(decPos + 1, expPos);
/*  197:     */       }
/*  198:     */       else
/*  199:     */       {
/*  200: 503 */         dec = str.substring(decPos + 1);
/*  201:     */       }
/*  202: 505 */       String mant = str.substring(0, decPos);
/*  203: 506 */       numDecimals = dec.length();
/*  204:     */     }
/*  205:     */     else
/*  206:     */     {
/*  207:     */       String mant;
/*  208: 508 */       if (expPos > -1)
/*  209:     */       {
/*  210: 509 */         if (expPos > str.length()) {
/*  211: 510 */           throw new NumberFormatException(str + " is not a valid number.");
/*  212:     */         }
/*  213: 512 */         mant = str.substring(0, expPos);
/*  214:     */       }
/*  215:     */       else
/*  216:     */       {
/*  217: 514 */         mant = str;
/*  218:     */       }
/*  219: 516 */       dec = null;
/*  220:     */     }
/*  221: 518 */     if ((!Character.isDigit(lastChar)) && (lastChar != '.'))
/*  222:     */     {
/*  223:     */       String exp;
/*  224:     */       String exp;
/*  225: 519 */       if ((expPos > -1) && (expPos < str.length() - 1)) {
/*  226: 520 */         exp = str.substring(expPos + 1, str.length() - 1);
/*  227:     */       } else {
/*  228: 522 */         exp = null;
/*  229:     */       }
/*  230: 525 */       String numeric = str.substring(0, str.length() - 1);
/*  231: 526 */       boolean allZeros = (isAllZeros(mant)) && (isAllZeros(exp));
/*  232: 527 */       switch (lastChar)
/*  233:     */       {
/*  234:     */       case 'L': 
/*  235:     */       case 'l': 
/*  236: 530 */         if ((dec == null) && (exp == null) && (((numeric.charAt(0) == '-') && (isDigits(numeric.substring(1)))) || (isDigits(numeric)))) {
/*  237:     */           try
/*  238:     */           {
/*  239: 534 */             return createLong(numeric);
/*  240:     */           }
/*  241:     */           catch (NumberFormatException nfe)
/*  242:     */           {
/*  243: 538 */             return createBigInteger(numeric);
/*  244:     */           }
/*  245:     */         }
/*  246: 541 */         throw new NumberFormatException(str + " is not a valid number.");
/*  247:     */       case 'F': 
/*  248:     */       case 'f': 
/*  249:     */         try
/*  250:     */         {
/*  251: 545 */           Float f = createFloat(numeric);
/*  252: 546 */           if ((!f.isInfinite()) && ((f.floatValue() != 0.0F) || (allZeros))) {
/*  253: 549 */             return f;
/*  254:     */           }
/*  255:     */         }
/*  256:     */         catch (NumberFormatException nfe) {}
/*  257:     */       case 'D': 
/*  258:     */       case 'd': 
/*  259:     */         try
/*  260:     */         {
/*  261: 559 */           Double d = createDouble(numeric);
/*  262: 560 */           if ((!d.isInfinite()) && ((d.floatValue() != 0.0D) || (allZeros))) {
/*  263: 561 */             return d;
/*  264:     */           }
/*  265:     */         }
/*  266:     */         catch (NumberFormatException nfe) {}
/*  267:     */         try
/*  268:     */         {
/*  269: 567 */           return createBigDecimal(numeric);
/*  270:     */         }
/*  271:     */         catch (NumberFormatException e) {}
/*  272:     */       }
/*  273: 573 */       throw new NumberFormatException(str + " is not a valid number.");
/*  274:     */     }
/*  275:     */     String exp;
/*  276:     */     String exp;
/*  277: 579 */     if ((expPos > -1) && (expPos < str.length() - 1)) {
/*  278: 580 */       exp = str.substring(expPos + 1, str.length());
/*  279:     */     } else {
/*  280: 582 */       exp = null;
/*  281:     */     }
/*  282: 584 */     if ((dec == null) && (exp == null)) {
/*  283:     */       try
/*  284:     */       {
/*  285: 587 */         return createInteger(str);
/*  286:     */       }
/*  287:     */       catch (NumberFormatException nfe)
/*  288:     */       {
/*  289:     */         try
/*  290:     */         {
/*  291: 592 */           return createLong(str);
/*  292:     */         }
/*  293:     */         catch (NumberFormatException nfe)
/*  294:     */         {
/*  295: 596 */           return createBigInteger(str);
/*  296:     */         }
/*  297:     */       }
/*  298:     */     }
/*  299: 600 */     boolean allZeros = (isAllZeros(mant)) && (isAllZeros(exp));
/*  300:     */     try
/*  301:     */     {
/*  302: 602 */       if (numDecimals <= 7)
/*  303:     */       {
/*  304: 603 */         Float f = createFloat(str);
/*  305: 604 */         if ((!f.isInfinite()) && ((f.floatValue() != 0.0F) || (allZeros))) {
/*  306: 605 */           return f;
/*  307:     */         }
/*  308:     */       }
/*  309:     */     }
/*  310:     */     catch (NumberFormatException nfe) {}
/*  311:     */     try
/*  312:     */     {
/*  313: 612 */       if (numDecimals <= 16)
/*  314:     */       {
/*  315: 613 */         Double d = createDouble(str);
/*  316: 614 */         if ((!d.isInfinite()) && ((d.doubleValue() != 0.0D) || (allZeros))) {
/*  317: 615 */           return d;
/*  318:     */         }
/*  319:     */       }
/*  320:     */     }
/*  321:     */     catch (NumberFormatException nfe) {}
/*  322: 622 */     return createBigDecimal(str);
/*  323:     */   }
/*  324:     */   
/*  325:     */   private static boolean isAllZeros(String str)
/*  326:     */   {
/*  327: 634 */     if (str == null) {
/*  328: 635 */       return true;
/*  329:     */     }
/*  330: 637 */     for (int i = str.length() - 1; i >= 0; i--) {
/*  331: 638 */       if (str.charAt(i) != '0') {
/*  332: 639 */         return false;
/*  333:     */       }
/*  334:     */     }
/*  335: 642 */     return str.length() > 0;
/*  336:     */   }
/*  337:     */   
/*  338:     */   public static Float createFloat(String str)
/*  339:     */   {
/*  340: 656 */     if (str == null) {
/*  341: 657 */       return null;
/*  342:     */     }
/*  343: 659 */     return Float.valueOf(str);
/*  344:     */   }
/*  345:     */   
/*  346:     */   public static Double createDouble(String str)
/*  347:     */   {
/*  348: 672 */     if (str == null) {
/*  349: 673 */       return null;
/*  350:     */     }
/*  351: 675 */     return Double.valueOf(str);
/*  352:     */   }
/*  353:     */   
/*  354:     */   public static Integer createInteger(String str)
/*  355:     */   {
/*  356: 689 */     if (str == null) {
/*  357: 690 */       return null;
/*  358:     */     }
/*  359: 693 */     return Integer.decode(str);
/*  360:     */   }
/*  361:     */   
/*  362:     */   public static Long createLong(String str)
/*  363:     */   {
/*  364: 707 */     if (str == null) {
/*  365: 708 */       return null;
/*  366:     */     }
/*  367: 710 */     return Long.decode(str);
/*  368:     */   }
/*  369:     */   
/*  370:     */   public static BigInteger createBigInteger(String str)
/*  371:     */   {
/*  372: 724 */     if (str == null) {
/*  373: 725 */       return null;
/*  374:     */     }
/*  375: 727 */     int pos = 0;
/*  376: 728 */     int radix = 10;
/*  377: 729 */     boolean negate = false;
/*  378: 730 */     if (str.startsWith("-"))
/*  379:     */     {
/*  380: 731 */       negate = true;
/*  381: 732 */       pos = 1;
/*  382:     */     }
/*  383: 734 */     if ((str.startsWith("0x", pos)) || (str.startsWith("0x", pos)))
/*  384:     */     {
/*  385: 735 */       radix = 16;
/*  386: 736 */       pos += 2;
/*  387:     */     }
/*  388: 737 */     else if (str.startsWith("#", pos))
/*  389:     */     {
/*  390: 738 */       radix = 16;
/*  391: 739 */       pos++;
/*  392:     */     }
/*  393: 740 */     else if ((str.startsWith("0", pos)) && (str.length() > pos + 1))
/*  394:     */     {
/*  395: 741 */       radix = 8;
/*  396: 742 */       pos++;
/*  397:     */     }
/*  398: 745 */     BigInteger value = new BigInteger(str.substring(pos), radix);
/*  399: 746 */     return negate ? value.negate() : value;
/*  400:     */   }
/*  401:     */   
/*  402:     */   public static BigDecimal createBigDecimal(String str)
/*  403:     */   {
/*  404: 759 */     if (str == null) {
/*  405: 760 */       return null;
/*  406:     */     }
/*  407: 763 */     if (StringUtils.isBlank(str)) {
/*  408: 764 */       throw new NumberFormatException("A blank string is not a valid number");
/*  409:     */     }
/*  410: 766 */     if (str.trim().startsWith("--")) {
/*  411: 771 */       throw new NumberFormatException(str + " is not a valid number.");
/*  412:     */     }
/*  413: 773 */     return new BigDecimal(str);
/*  414:     */   }
/*  415:     */   
/*  416:     */   public static long min(long[] array)
/*  417:     */   {
/*  418: 788 */     validateArray(array);
/*  419:     */     
/*  420:     */ 
/*  421: 791 */     long min = array[0];
/*  422: 792 */     for (int i = 1; i < array.length; i++) {
/*  423: 793 */       if (array[i] < min) {
/*  424: 794 */         min = array[i];
/*  425:     */       }
/*  426:     */     }
/*  427: 798 */     return min;
/*  428:     */   }
/*  429:     */   
/*  430:     */   public static int min(int[] array)
/*  431:     */   {
/*  432: 811 */     validateArray(array);
/*  433:     */     
/*  434:     */ 
/*  435: 814 */     int min = array[0];
/*  436: 815 */     for (int j = 1; j < array.length; j++) {
/*  437: 816 */       if (array[j] < min) {
/*  438: 817 */         min = array[j];
/*  439:     */       }
/*  440:     */     }
/*  441: 821 */     return min;
/*  442:     */   }
/*  443:     */   
/*  444:     */   public static short min(short[] array)
/*  445:     */   {
/*  446: 834 */     validateArray(array);
/*  447:     */     
/*  448:     */ 
/*  449: 837 */     short min = array[0];
/*  450: 838 */     for (int i = 1; i < array.length; i++) {
/*  451: 839 */       if (array[i] < min) {
/*  452: 840 */         min = array[i];
/*  453:     */       }
/*  454:     */     }
/*  455: 844 */     return min;
/*  456:     */   }
/*  457:     */   
/*  458:     */   public static byte min(byte[] array)
/*  459:     */   {
/*  460: 857 */     validateArray(array);
/*  461:     */     
/*  462:     */ 
/*  463: 860 */     byte min = array[0];
/*  464: 861 */     for (int i = 1; i < array.length; i++) {
/*  465: 862 */       if (array[i] < min) {
/*  466: 863 */         min = array[i];
/*  467:     */       }
/*  468:     */     }
/*  469: 867 */     return min;
/*  470:     */   }
/*  471:     */   
/*  472:     */   public static double min(double[] array)
/*  473:     */   {
/*  474: 881 */     validateArray(array);
/*  475:     */     
/*  476:     */ 
/*  477: 884 */     double min = array[0];
/*  478: 885 */     for (int i = 1; i < array.length; i++)
/*  479:     */     {
/*  480: 886 */       if (Double.isNaN(array[i])) {
/*  481: 887 */         return (0.0D / 0.0D);
/*  482:     */       }
/*  483: 889 */       if (array[i] < min) {
/*  484: 890 */         min = array[i];
/*  485:     */       }
/*  486:     */     }
/*  487: 894 */     return min;
/*  488:     */   }
/*  489:     */   
/*  490:     */   public static float min(float[] array)
/*  491:     */   {
/*  492: 908 */     validateArray(array);
/*  493:     */     
/*  494:     */ 
/*  495: 911 */     float min = array[0];
/*  496: 912 */     for (int i = 1; i < array.length; i++)
/*  497:     */     {
/*  498: 913 */       if (Float.isNaN(array[i])) {
/*  499: 914 */         return (0.0F / 0.0F);
/*  500:     */       }
/*  501: 916 */       if (array[i] < min) {
/*  502: 917 */         min = array[i];
/*  503:     */       }
/*  504:     */     }
/*  505: 921 */     return min;
/*  506:     */   }
/*  507:     */   
/*  508:     */   public static long max(long[] array)
/*  509:     */   {
/*  510: 936 */     validateArray(array);
/*  511:     */     
/*  512:     */ 
/*  513: 939 */     long max = array[0];
/*  514: 940 */     for (int j = 1; j < array.length; j++) {
/*  515: 941 */       if (array[j] > max) {
/*  516: 942 */         max = array[j];
/*  517:     */       }
/*  518:     */     }
/*  519: 946 */     return max;
/*  520:     */   }
/*  521:     */   
/*  522:     */   public static int max(int[] array)
/*  523:     */   {
/*  524: 959 */     validateArray(array);
/*  525:     */     
/*  526:     */ 
/*  527: 962 */     int max = array[0];
/*  528: 963 */     for (int j = 1; j < array.length; j++) {
/*  529: 964 */       if (array[j] > max) {
/*  530: 965 */         max = array[j];
/*  531:     */       }
/*  532:     */     }
/*  533: 969 */     return max;
/*  534:     */   }
/*  535:     */   
/*  536:     */   public static short max(short[] array)
/*  537:     */   {
/*  538: 982 */     validateArray(array);
/*  539:     */     
/*  540:     */ 
/*  541: 985 */     short max = array[0];
/*  542: 986 */     for (int i = 1; i < array.length; i++) {
/*  543: 987 */       if (array[i] > max) {
/*  544: 988 */         max = array[i];
/*  545:     */       }
/*  546:     */     }
/*  547: 992 */     return max;
/*  548:     */   }
/*  549:     */   
/*  550:     */   public static byte max(byte[] array)
/*  551:     */   {
/*  552:1005 */     validateArray(array);
/*  553:     */     
/*  554:     */ 
/*  555:1008 */     byte max = array[0];
/*  556:1009 */     for (int i = 1; i < array.length; i++) {
/*  557:1010 */       if (array[i] > max) {
/*  558:1011 */         max = array[i];
/*  559:     */       }
/*  560:     */     }
/*  561:1015 */     return max;
/*  562:     */   }
/*  563:     */   
/*  564:     */   public static double max(double[] array)
/*  565:     */   {
/*  566:1029 */     validateArray(array);
/*  567:     */     
/*  568:     */ 
/*  569:1032 */     double max = array[0];
/*  570:1033 */     for (int j = 1; j < array.length; j++)
/*  571:     */     {
/*  572:1034 */       if (Double.isNaN(array[j])) {
/*  573:1035 */         return (0.0D / 0.0D);
/*  574:     */       }
/*  575:1037 */       if (array[j] > max) {
/*  576:1038 */         max = array[j];
/*  577:     */       }
/*  578:     */     }
/*  579:1042 */     return max;
/*  580:     */   }
/*  581:     */   
/*  582:     */   public static float max(float[] array)
/*  583:     */   {
/*  584:1056 */     validateArray(array);
/*  585:     */     
/*  586:     */ 
/*  587:1059 */     float max = array[0];
/*  588:1060 */     for (int j = 1; j < array.length; j++)
/*  589:     */     {
/*  590:1061 */       if (Float.isNaN(array[j])) {
/*  591:1062 */         return (0.0F / 0.0F);
/*  592:     */       }
/*  593:1064 */       if (array[j] > max) {
/*  594:1065 */         max = array[j];
/*  595:     */       }
/*  596:     */     }
/*  597:1069 */     return max;
/*  598:     */   }
/*  599:     */   
/*  600:     */   private static void validateArray(Object array)
/*  601:     */   {
/*  602:1079 */     if (array == null) {
/*  603:1080 */       throw new IllegalArgumentException("The Array must not be null");
/*  604:     */     }
/*  605:1081 */     if (Array.getLength(array) == 0) {
/*  606:1082 */       throw new IllegalArgumentException("Array cannot be empty.");
/*  607:     */     }
/*  608:     */   }
/*  609:     */   
/*  610:     */   public static long min(long a, long b, long c)
/*  611:     */   {
/*  612:1097 */     if (b < a) {
/*  613:1098 */       a = b;
/*  614:     */     }
/*  615:1100 */     if (c < a) {
/*  616:1101 */       a = c;
/*  617:     */     }
/*  618:1103 */     return a;
/*  619:     */   }
/*  620:     */   
/*  621:     */   public static int min(int a, int b, int c)
/*  622:     */   {
/*  623:1115 */     if (b < a) {
/*  624:1116 */       a = b;
/*  625:     */     }
/*  626:1118 */     if (c < a) {
/*  627:1119 */       a = c;
/*  628:     */     }
/*  629:1121 */     return a;
/*  630:     */   }
/*  631:     */   
/*  632:     */   public static short min(short a, short b, short c)
/*  633:     */   {
/*  634:1133 */     if (b < a) {
/*  635:1134 */       a = b;
/*  636:     */     }
/*  637:1136 */     if (c < a) {
/*  638:1137 */       a = c;
/*  639:     */     }
/*  640:1139 */     return a;
/*  641:     */   }
/*  642:     */   
/*  643:     */   public static byte min(byte a, byte b, byte c)
/*  644:     */   {
/*  645:1151 */     if (b < a) {
/*  646:1152 */       a = b;
/*  647:     */     }
/*  648:1154 */     if (c < a) {
/*  649:1155 */       a = c;
/*  650:     */     }
/*  651:1157 */     return a;
/*  652:     */   }
/*  653:     */   
/*  654:     */   public static double min(double a, double b, double c)
/*  655:     */   {
/*  656:1173 */     return Math.min(Math.min(a, b), c);
/*  657:     */   }
/*  658:     */   
/*  659:     */   public static float min(float a, float b, float c)
/*  660:     */   {
/*  661:1189 */     return Math.min(Math.min(a, b), c);
/*  662:     */   }
/*  663:     */   
/*  664:     */   public static long max(long a, long b, long c)
/*  665:     */   {
/*  666:1203 */     if (b > a) {
/*  667:1204 */       a = b;
/*  668:     */     }
/*  669:1206 */     if (c > a) {
/*  670:1207 */       a = c;
/*  671:     */     }
/*  672:1209 */     return a;
/*  673:     */   }
/*  674:     */   
/*  675:     */   public static int max(int a, int b, int c)
/*  676:     */   {
/*  677:1221 */     if (b > a) {
/*  678:1222 */       a = b;
/*  679:     */     }
/*  680:1224 */     if (c > a) {
/*  681:1225 */       a = c;
/*  682:     */     }
/*  683:1227 */     return a;
/*  684:     */   }
/*  685:     */   
/*  686:     */   public static short max(short a, short b, short c)
/*  687:     */   {
/*  688:1239 */     if (b > a) {
/*  689:1240 */       a = b;
/*  690:     */     }
/*  691:1242 */     if (c > a) {
/*  692:1243 */       a = c;
/*  693:     */     }
/*  694:1245 */     return a;
/*  695:     */   }
/*  696:     */   
/*  697:     */   public static byte max(byte a, byte b, byte c)
/*  698:     */   {
/*  699:1257 */     if (b > a) {
/*  700:1258 */       a = b;
/*  701:     */     }
/*  702:1260 */     if (c > a) {
/*  703:1261 */       a = c;
/*  704:     */     }
/*  705:1263 */     return a;
/*  706:     */   }
/*  707:     */   
/*  708:     */   public static double max(double a, double b, double c)
/*  709:     */   {
/*  710:1279 */     return Math.max(Math.max(a, b), c);
/*  711:     */   }
/*  712:     */   
/*  713:     */   public static float max(float a, float b, float c)
/*  714:     */   {
/*  715:1295 */     return Math.max(Math.max(a, b), c);
/*  716:     */   }
/*  717:     */   
/*  718:     */   public static boolean isDigits(String str)
/*  719:     */   {
/*  720:1310 */     if (StringUtils.isEmpty(str)) {
/*  721:1311 */       return false;
/*  722:     */     }
/*  723:1313 */     for (int i = 0; i < str.length(); i++) {
/*  724:1314 */       if (!Character.isDigit(str.charAt(i))) {
/*  725:1315 */         return false;
/*  726:     */       }
/*  727:     */     }
/*  728:1318 */     return true;
/*  729:     */   }
/*  730:     */   
/*  731:     */   public static boolean isNumber(String str)
/*  732:     */   {
/*  733:1335 */     if (StringUtils.isEmpty(str)) {
/*  734:1336 */       return false;
/*  735:     */     }
/*  736:1338 */     char[] chars = str.toCharArray();
/*  737:1339 */     int sz = chars.length;
/*  738:1340 */     boolean hasExp = false;
/*  739:1341 */     boolean hasDecPoint = false;
/*  740:1342 */     boolean allowSigns = false;
/*  741:1343 */     boolean foundDigit = false;
/*  742:     */     
/*  743:1345 */     int start = chars[0] == '-' ? 1 : 0;
/*  744:1346 */     if ((sz > start + 1) && (chars[start] == '0') && (chars[(start + 1)] == 'x'))
/*  745:     */     {
/*  746:1347 */       int i = start + 2;
/*  747:1348 */       if (i == sz) {
/*  748:1349 */         return false;
/*  749:     */       }
/*  750:1352 */       for (; i < chars.length; i++) {
/*  751:1353 */         if (((chars[i] < '0') || (chars[i] > '9')) && ((chars[i] < 'a') || (chars[i] > 'f')) && ((chars[i] < 'A') || (chars[i] > 'F'))) {
/*  752:1356 */           return false;
/*  753:     */         }
/*  754:     */       }
/*  755:1359 */       return true;
/*  756:     */     }
/*  757:1361 */     sz--;
/*  758:     */     
/*  759:1363 */     int i = start;
/*  760:1366 */     while ((i < sz) || ((i < sz + 1) && (allowSigns) && (!foundDigit)))
/*  761:     */     {
/*  762:1367 */       if ((chars[i] >= '0') && (chars[i] <= '9'))
/*  763:     */       {
/*  764:1368 */         foundDigit = true;
/*  765:1369 */         allowSigns = false;
/*  766:     */       }
/*  767:1371 */       else if (chars[i] == '.')
/*  768:     */       {
/*  769:1372 */         if ((hasDecPoint) || (hasExp)) {
/*  770:1374 */           return false;
/*  771:     */         }
/*  772:1376 */         hasDecPoint = true;
/*  773:     */       }
/*  774:1377 */       else if ((chars[i] == 'e') || (chars[i] == 'E'))
/*  775:     */       {
/*  776:1379 */         if (hasExp) {
/*  777:1381 */           return false;
/*  778:     */         }
/*  779:1383 */         if (!foundDigit) {
/*  780:1384 */           return false;
/*  781:     */         }
/*  782:1386 */         hasExp = true;
/*  783:1387 */         allowSigns = true;
/*  784:     */       }
/*  785:1388 */       else if ((chars[i] == '+') || (chars[i] == '-'))
/*  786:     */       {
/*  787:1389 */         if (!allowSigns) {
/*  788:1390 */           return false;
/*  789:     */         }
/*  790:1392 */         allowSigns = false;
/*  791:1393 */         foundDigit = false;
/*  792:     */       }
/*  793:     */       else
/*  794:     */       {
/*  795:1395 */         return false;
/*  796:     */       }
/*  797:1397 */       i++;
/*  798:     */     }
/*  799:1399 */     if (i < chars.length)
/*  800:     */     {
/*  801:1400 */       if ((chars[i] >= '0') && (chars[i] <= '9')) {
/*  802:1402 */         return true;
/*  803:     */       }
/*  804:1404 */       if ((chars[i] == 'e') || (chars[i] == 'E')) {
/*  805:1406 */         return false;
/*  806:     */       }
/*  807:1408 */       if (chars[i] == '.')
/*  808:     */       {
/*  809:1409 */         if ((hasDecPoint) || (hasExp)) {
/*  810:1411 */           return false;
/*  811:     */         }
/*  812:1414 */         return foundDigit;
/*  813:     */       }
/*  814:1416 */       if ((!allowSigns) && ((chars[i] == 'd') || (chars[i] == 'D') || (chars[i] == 'f') || (chars[i] == 'F'))) {
/*  815:1421 */         return foundDigit;
/*  816:     */       }
/*  817:1423 */       if ((chars[i] == 'l') || (chars[i] == 'L')) {
/*  818:1426 */         return (foundDigit) && (!hasExp) && (!hasDecPoint);
/*  819:     */       }
/*  820:1429 */       return false;
/*  821:     */     }
/*  822:1433 */     return (!allowSigns) && (foundDigit);
/*  823:     */   }
/*  824:     */ }


/* Location:           C:\Users\ek1fox\Desktop\dragonet-0.0.3_R2-SNAPSHOT-PE0.9.5_PC1.7.10.jar
 * Qualified Name:     net.minecraft.util.org.apache.commons.lang3.math.NumberUtils
 * JD-Core Version:    0.7.0.1
 */
