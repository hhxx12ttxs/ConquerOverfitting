package streaming;

public class EvaluationMetric {

public double accuracy;
public double recall;
this.recall = recall;
}

public double getFMeasure() {
if(accuracy!=0 &amp;&amp; recall!=0){
return 2*accuracy*recall/(accuracy+recall);

