private int counter=Constants.limit;
private Random rand=new Random();

public int getNextRandomIndex() throws Exception{
if(counter==1)
public String getDeflatedURL(String url) throws Exception{
String u=deflatedURL.get(url);
if(u!=null)
return u;
else
{
int i=getNextRandomIndex();

