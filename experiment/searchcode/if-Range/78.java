int range = (int) (bearing / (360f / 16f));
String dirTxt = &quot;&quot;;
if (range == 15 || range == 0)
dirTxt = &quot;N&quot;;
else if (range == 1 || range == 2)
dirTxt = &quot;NE&quot;;
else if (range == 3 || range == 4)

