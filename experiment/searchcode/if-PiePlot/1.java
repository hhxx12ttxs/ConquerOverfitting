PiePlot piePlot = new PieChartPlot(pieDataset);

if (labels)
{
piePlot.setLabelGenerator(new StandardPieSectionLabelGenerator());
}
if (tooltips)
{
piePlot.setToolTipGenerator(new StandardPieToolTipGenerator());
}
if (urls)

