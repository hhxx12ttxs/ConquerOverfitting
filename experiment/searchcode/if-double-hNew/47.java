IJ.showProgress((double)i/nFrames);
ipNew = ipOld.createProcessor(wNew, hNew);
if (zeroFill)
ipNew.setValue(0.0);
public ImageProcessor expandImage(ImageProcessor ipOld, int wNew, int hNew, int xOff, int yOff) {
ImageProcessor ipNew = ipOld.createProcessor(wNew, hNew);
if (zeroFill)
ipNew.setValue(0.0);

