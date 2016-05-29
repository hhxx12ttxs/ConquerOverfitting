render3D.renderDistancelimiter();
draw(render3D, 0, 0);
}

private void fullBlock(int xb, int zb) {
if (Math.abs(Render3D.forward - 8 * zb) < renderDistance &amp;&amp; Math.abs(Render3D.sideways - 8 * xb) < renderDistance) {

