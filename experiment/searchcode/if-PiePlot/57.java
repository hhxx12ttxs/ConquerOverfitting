str = (String) params.get(&quot;simpleLabels&quot;);
if (str != null)
simpleLabels = &quot;true&quot;.equals(str);

piePlot.setSimpleLabels(simpleLabels);
// Although PiePlot3D extends PiePlot, it does not support exploded sections.
if (! (plot instanceof PiePlot3D)) {
PieDataset ds = piePlot.getDataset();

for (Iterator paramIter=params.entrySet().iterator(); paramIter.hasNext(); ) {

