// add element, keeping the array sorted
if ((elements.size() == 0) || (x <= elements.get(0))) {
elements.add(0, x);
}
else {
int ih = (int)Math.round(Math.floor(h));
double frac = h - ih;
if ((ih + 1) == elements.size()) {

