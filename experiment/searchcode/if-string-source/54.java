String s = System.console().readLine();

StringBuffer source = new StringBuffer(s);
source.reverse();
String rs = new String(source);
if(s.equals(rs))
{
System.out.println(&quot;yes&quot;);
}
else
{
System.out.println(&quot;no&quot;);
}

}
}

