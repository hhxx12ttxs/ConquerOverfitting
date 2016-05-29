double seconds2End = tb - bin.tend;

double leftOverlap = 0,rightOverlap=0;
if(i>0){leftOverlap = listBins.get(i-1).tend - bin.tbeg ;}
if(i<(numWordsUtt-1)){rightOverlap = bin.tend - listBins.get(i+1).tbeg;}

