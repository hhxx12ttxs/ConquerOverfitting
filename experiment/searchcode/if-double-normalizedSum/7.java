public class PageRankSpider extends Spider{

public double alpha = 0.15;
public int iterations = 50;

//for the graph
public Graph graph;
//to get a link from the name (P001.html) to the link of the url mapped to it
double normalizedSum = 0.0;
double c = 1.0;

for (int i =0 ;i < allNodes.length; i++){
allNodes[i].rank = 1.0/size;

