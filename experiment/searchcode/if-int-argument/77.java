for(Argument member:kb.arguments){
if(!attackRelation.containsKey(member)){
nonAttacked.add(member);
for(Argument arg:attackRelation.keySet()){
// if argument is not member of labelling
if((!labelling.get(&quot;IN&quot;).contains(arg))

