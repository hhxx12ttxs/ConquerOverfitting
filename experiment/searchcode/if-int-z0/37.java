public static void generate(int x0, int y0, int z0, int radius, SphereCallback callback) {
sSphereGenerator.pregenerate(x0, y0, z0, callback, false);
public static void generateFilled(int x0, int y0, int z0, int radius, SphereCallback callback) {
sSphereGenerator.pregenerate(x0, y0, z0, callback, true);

