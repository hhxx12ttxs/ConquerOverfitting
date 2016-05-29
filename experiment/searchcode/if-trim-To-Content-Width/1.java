TDSet theTDs = tr.getTDSet(i-skiplines);

//time_start
if (tokens[0].trim().equals(theTDs.getContent(1).substring(0, 10) + &quot;T&quot; + theTDs.getContent(1).substring(11, 19) + &quot;Z&quot;)) j = 0;
else System.out.println(tokens[0].trim() + &quot; &quot; + tokens[17].trim() + &quot; &quot; + theTDs.getContent(18));

//pa_width
if (tokens[18].trim().equals(theTDs.getContent(19) + &quot;NaN&quot;)) j=0;
else if (theTDs.getContent(19).equals(tokens[18].trim() + &quot;.0&quot;)) j = 0;

