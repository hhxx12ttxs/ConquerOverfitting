// {(AAPL,2014,12,-0.04074),(AAPL,2014,11,0.0918),...}
int months = 0;
double sum = 0.0, sumsq = 0.0;
ArrayList<Double> x_i = new ArrayList<Double>();
double val1 = sumsq / (months - 1);

double vol = Math.sqrt(val1);

if(vol>0.0){
return FileName+&quot;\t&quot;+ String.valueOf(vol);

