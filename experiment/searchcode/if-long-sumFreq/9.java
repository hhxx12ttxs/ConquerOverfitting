String[] words = sentence.split(&quot;\t&quot;)[0].split(&quot; &quot;);
long sumFreq = 0; //sum of frequencies of trigrams which contain the target word
ResultSet rs = db.execute(query);
if(rs.next()){
long frequency  = rs.getLong(&quot;frequency&quot;);

