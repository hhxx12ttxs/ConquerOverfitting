package Ch3;

import static java.lang.Character.isUpperCase;
import static java.lang.Character.isLowerCase;
char symbol = &#39;A&#39;;
symbol = (char)(128.0*Math.random());

if (isUpperCase(symbol))
System.out.println(&quot;CAPITAL &quot; + symbol);

