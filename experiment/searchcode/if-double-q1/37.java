q1=q1.toLowerCase().trim();
if(q1.length()==0)
continue;
total++;
if(!m.containsKey(q1))
m.put(q1, 1.0);
else
m.put(q1, m.get(q1)+1);

if(!forbidden.contains(q1)){

