private static void setTexture(IIcon i) {
if(render.overrideBlockTexture != null)
i = render.overrideBlockTexture;
uMin = i.getMinU();
rt.addVertexWithUV(max, bottom, min, uMin, vb);

if(bottomcap) {
double d1 = (6/16.0)*(vMax-vMin), d2=(10/16.0)*(vMax-vMin);

