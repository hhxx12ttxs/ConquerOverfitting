private static final long serialVersionUID = 841107539929338322L;
private MainPanel panel;
private Double[] minRisk;
private Double[] maxGrowth;

public OptimizationPanel(MainPanel panel, Investments invest)
maxGrowth = CalcModels.optimizeHighGrowth(coV, CalcModels.portfolioExpectedValue(histories));
} else {
minRisk = new Double[0];
maxGrowth = new Double[0];

