for(int i=0;i<strings.length();i++)
{
String[] tokens = string.get(i).split(&quot; &quot;);
int tokenLen = tokens.length;

for(int j=0;j<maxlen;j++)
{
if(j<tokenLen)
str[j][i] = tokens[j];
else
str[j][i] = &quot;&quot;;
}
}

//print str[i][]

}
}

