public Name(String nameToParse)
{
StringTokenizer st = new StringTokenizer(nameToParse);
if (st.countTokens() == 2) // If we only have two tokens, we have no middle name.
retString += this.lastName + &quot;, &quot; + this.firstName + &quot; &quot;;
if (!this.middleName.equals(&quot;&quot;))
retString += this.middleName.charAt(0) + &quot;.&quot;;

return retString;
}
}

