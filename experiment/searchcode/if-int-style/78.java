V[i] = new Vertex(i,degree[i]);
for (int j=0;j<n;j++)
if (A[i][j] == 1) V[i].nebDeg = V[i].nebDeg + degree[j];
}
if (style == 1) Arrays.sort(V);
if (style == 2) minWidthOrder(V);
if (style == 3) Arrays.sort(V,new MCRComparator());

