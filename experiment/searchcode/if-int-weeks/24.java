final int QUIT = 99;
int weeks;
int reviews;
int x, y;

System.out.print(&quot;Enter number of full weeks worked or &quot; + QUIT + &quot; to quit >> &quot;);
System.out.print(&quot;Enter number of positive reviews received >> &quot;);
reviews = keyboard.nextInt();
if(weeks >= bonuses.length)
weeks = bonuses.length - 1;

