LinkNeighborhood[] lns = page.getLinkNeighboor();
linkRelevance = new LinkRelevance[lns.length];
for (int i = 0; i < lns.length; i++) {
double relevance = -1;
if(!page.getURL().getHost().equals(lns[i].getLink().getHost())){

