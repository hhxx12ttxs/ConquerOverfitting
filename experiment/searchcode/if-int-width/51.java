return (width + height) * 2;
}

public void grow(int h, int w) {
this.height += h;
this.width += w;
if (height < 0) {
height = 0;
}
if (width < 0) {
width = 0;
}
}

public void grow(int g) {
grow(g, g);

