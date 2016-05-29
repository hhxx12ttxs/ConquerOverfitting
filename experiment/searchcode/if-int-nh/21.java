return s;
}

@Override
public void init() {
int nh = Math.round(h * 1.0f / size);
int nw = Math.round(w * 1.0f / size);
for (int i = 0; i < nh; i++) {
for (int j = 0; j <= nw; j++) {
if (j == 0 || j == nw) {

