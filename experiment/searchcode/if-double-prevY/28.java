double xInc = (double)(w - 2*PAD)/(data.length-1);
double scale = (double)(h - 2*PAD)/getMax();
prevx=PAD;
prevy=h-PAD;
double y = h - PAD - scale*data[i];
newx=x;
newy=y;
g2.draw(new Line2D.Double(prevx, prevy, newx, newy));

