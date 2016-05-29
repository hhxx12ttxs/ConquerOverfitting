public class DitherFloydSteinberg {
public static BufferedImage dither(BufferedImage img) {

C3[] palette = new C3[] {
private static C3 findClosestPaletteColor(C3 c, C3[] palette) {
C3 closest = palette[0];

for (C3 n : palette)
if (n.diff(c) < closest.diff(c))

