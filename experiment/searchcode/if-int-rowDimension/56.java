g.drawLine(AXIS_OFFSET, getHeight() - AXIS_OFFSET, AXIS_OFFSET, AXIS_OFFSET);
paintWire(g);
paintSeries(g);
}

private void paintWire(Graphics g) {
int rowDimension = seriesMatrix.getRowDimension();
for (int i = 1; i < rowDimension; i++) {
g.setColor(Color.LIGHT_GRAY);

