public class RingLocus implements ILocus {
private float x, y;
private float r;
private float[] result = new float[2];
result[0] = (float) (x + r * Math.cos(d));
result[1] = (float) (y + r * Math.sin(d));
d += clockwise ? radianPS : -radianPS;
if (d > 2 * C.PI) {
d = 0;
}
return result;
}
}

