imageres = new BufferedImage(imagesrc.getWidth(),imagesrc.getHeight(),BufferedImage.TYPE_INT_ARGB);
int offsetPrev = 0;
int offset =0;
public int calcOffset(int numTh,int offsetPrev){
int nbTh = slider.getValue() - numTh;
if(nbTh == 0)
return imagesrc.getWidth() - offsetPrev;

