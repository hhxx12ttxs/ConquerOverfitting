GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

double uMin = block.side.getInterpolatedU(0.0D);
double uMax = block.side.getInterpolatedU(16.0D);
Tessellator tessellator = Tessellator.instance;
tessellator.addTranslation(x, y, z);

double uMin = block.side.getInterpolatedU(0.0D);

