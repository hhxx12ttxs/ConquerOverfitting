double dSq = Math.pow(x1 - x, 2.0D) + Math.pow(z1 - z, 2.0D) + Math.pow(y1 - y, 2.0D);
if (Math.round(Math.sqrt(dSq)) < radius) {
if (dSq >= Math.pow(radius - 2, 2.0D)) {
if (world.getBlock(x1, y1, z1) != Blocks.water)

