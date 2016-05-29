public static Image threshold(Image original, int value) {
if (original == null) {
return null;
}
Image thresholded = (Image) original.clone();
ChannelType c) {
int currentT = T;
int previousT = 0;
int i = 0;
do {
previousT = currentT;
currentT = getAdjustedThreshold(currentT, img, c);

