private HashMap<String, Double> envMap = new HashMap<String, Double>();

public double put(String var, double val) {
envMap.put(var, val);
return val;
}

public double get(double pos, String var) throws EvalException {

if(envMap.get(var) == null)

