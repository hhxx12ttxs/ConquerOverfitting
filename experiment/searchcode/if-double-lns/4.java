public class E099BigExponents {

public static void main(String[] args) throws IOException{
List<Double> lns = Read.streamLines(E099BigExponents.class)
.collect(Collectors.toList());
int max = 0;
for(int i=1; i<lns.size(); i++){
if(lns.get(i).compareTo(lns.get(max)) > 0) max = i;

