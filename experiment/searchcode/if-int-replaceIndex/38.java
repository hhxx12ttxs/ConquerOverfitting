StringBuilder instructions = new StringBuilder();
int replaceIndex = 0;
int instructionNumber=1;
replaceIndex = instructions.indexOf(&quot;<div>&quot;);
if(replaceIndex >= 0)
instructions.replace(replaceIndex, replaceIndex+5 , instructionNumber + &quot;.&quot;);

