if(i % (hubRelevance.length/99) == 0 ){
rel--;
}
if(hubRelevance[i].getWord() != null){
//				double weight = (hubRelevance[i].getWeight()/hubRelevance[0].getWeight())*100 + 100;
LinkNeighborhood[] outLNs = rep.getLNs();
for (int i = 0; i < outLNs.length; i++) {
if(outLNs[i] != null){
LinkRelevance lr = outlinkClassifier.classify(outLNs[i]);

