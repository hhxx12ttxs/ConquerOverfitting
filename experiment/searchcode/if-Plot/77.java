Location to = event.getTo();

String idTo = PlotManager.getPlotId(to);

if(!idTo.equalsIgnoreCase(&quot;&quot;))
{
Plot plot = PlotManager.getPlotById(p, idTo);

if(plot != null &amp;&amp; plot.isDenied(p.getName()))

