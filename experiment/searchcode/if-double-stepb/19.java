String toCurrency = &quot;CAD&quot;;
String amount = &quot;100&quot;;
//        Double result = &quot;143.42016725944438&quot;;
//Rounding result down
Double stepA = (Double.parseDouble(amount)) * from.getCurrency();
Double stepB = stepA / to.getCurrency();

stepB = Math.round(stepB * 100.0) / 100.0;

