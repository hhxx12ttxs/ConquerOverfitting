public historicalData Data;
public double MaxVal;

public patternValue(historicalData HD ) {
Data = HD;
private void get_max_val_for_multi() {
MaxVal = 0;
if (Data.Avg > MaxVal) {
MaxVal = Data.Avg;
}

if (Data.SecondSession > MaxVal) {

