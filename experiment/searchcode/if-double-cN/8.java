private double[] Cp_array = null;
private double[] Cn_array = null;

public Solver_Asvm(double a_rate) {
super();
this.a = a_rate;
}

@Override
protected double get_C(int i) {
if (Cp_array == null &amp;&amp; Cn_array == null) {

