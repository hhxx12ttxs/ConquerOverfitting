//++++++++++++++++++++++++++++++++++++++++++++++++++++++//
//     Fuzzy Inference Engine Prioridadv      //
//++++++++++++++++++++++++++++++++++++++++++++++++++++++//

package xfuzzy;

public class Prioridadv implements FuzzyInferenceEngine {

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //      Membership function of an input variable       //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private abstract class InnerMembershipFunction {
  double min, max, step;
  abstract double param(int i);
  double center() { return 0; }
  double basis() { return 0; }
  abstract double isEqual(double x);

  double isSmallerOrEqual(double x) {
   double degree=0, mu;
   for(double y=max; y>=x ; y-=step) if((mu = isEqual(y))>degree) degree=mu;
   return degree;
  }

  double isGreaterOrEqual(double x) {
   double degree=0, mu;
   for(double y=min; y<=x ; y+=step) if((mu = isEqual(y))>degree) degree=mu;
   return degree;
  }

  double isEqual(MembershipFunction mf) {
   if(mf instanceof FuzzySingleton)
    { return isEqual( ((FuzzySingleton) mf).getValue()); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = isEqual(val[i][0]);
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = isEqual(x);
    minmu = (mu1<mu2 ? mu1 : mu2);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isGreaterOrEqual(MembershipFunction mf) {
   if(mf instanceof FuzzySingleton)
    { return isGreaterOrEqual( ((FuzzySingleton) mf).getValue()); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = isGreaterOrEqual(val[i][0]);
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0,greq=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = isEqual(x);
    if( mu2>greq ) greq = mu2;
    if( mu1<greq ) minmu = mu1; else minmu = greq;
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isSmallerOrEqual(MembershipFunction mf) {
   if(mf instanceof FuzzySingleton)
    { return isSmallerOrEqual( ((FuzzySingleton) mf).getValue()); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = isSmallerOrEqual(val[i][0]);
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0,smeq=0;
   for(double x=max; x>=min; x-=step){
    mu1 = mf.compute(x);
    mu2 = isEqual(x);
    if( mu2>smeq ) smeq = mu2;
    if( mu1<smeq ) minmu = mu1; else minmu = smeq;
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isGreater(MembershipFunction mf, InnerOperatorset op) {
   if(mf instanceof FuzzySingleton)
    { return op.not(isSmallerOrEqual( ((FuzzySingleton) mf).getValue())); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = op.not(isSmallerOrEqual(val[i][0]));
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,gr,degree=0,smeq=0;
   for(double x=max; x>=min; x-=step){
    mu1 = mf.compute(x);
    mu2 = isEqual(x);
    if( mu2>smeq ) smeq = mu2;
    gr = op.not(smeq);
    minmu = ( mu1<gr ? mu1 : gr);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isSmaller(MembershipFunction mf, InnerOperatorset op) {
   if(mf instanceof FuzzySingleton)
    { return op.not(isGreaterOrEqual( ((FuzzySingleton) mf).getValue())); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = op.not(isGreaterOrEqual(val[i][0]));
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,sm,degree=0,greq=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = isEqual(x);
    if( mu2>greq ) greq = mu2;
    sm = op.not(greq);
    minmu = ( mu1<sm ? mu1 : sm);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isNotEqual(MembershipFunction mf, InnerOperatorset op) {
   if(mf instanceof FuzzySingleton)
    { return op.not(isEqual( ((FuzzySingleton) mf).getValue())); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = op.not(isEqual(val[i][0]));
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = op.not(isEqual(x));
    minmu = (mu1<mu2 ? mu1 : mu2);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isApproxEqual(MembershipFunction mf, InnerOperatorset op) {
   if(mf instanceof FuzzySingleton)
    { return op.moreorless(isEqual( ((FuzzySingleton) mf).getValue())); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = op.moreorless(isEqual(val[i][0]));
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = op.moreorless(isEqual(x));
    minmu = (mu1<mu2 ? mu1 : mu2);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isVeryEqual(MembershipFunction mf, InnerOperatorset op) {
   if(mf instanceof FuzzySingleton)
    { return op.very(isEqual( ((FuzzySingleton) mf).getValue())); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = op.very(isEqual(val[i][0]));
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = op.very(isEqual(x));
    minmu = (mu1<mu2 ? mu1 : mu2);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }

  double isSlightlyEqual(MembershipFunction mf, InnerOperatorset op) {
   if(mf instanceof FuzzySingleton)
    { return op.slightly(isEqual( ((FuzzySingleton) mf).getValue())); }
   if((mf instanceof OutputMembershipFunction) &&
      ((OutputMembershipFunction) mf).isDiscrete() ) {
    double[][] val = ((OutputMembershipFunction) mf).getDiscreteValues();
    double deg = 0;
    for(int i=0; i<val.length; i++){
     double mu = op.slightly(isEqual(val[i][0]));
     double minmu = (mu<val[i][1] ? mu : val[i][1]);
     if( deg<minmu ) deg = minmu;
    }
    return deg;
   }
   double mu1,mu2,minmu,degree=0;
   for(double x=min; x<=max; x+=step){
    mu1 = mf.compute(x);
    mu2 = op.slightly(isEqual(x));
    minmu = (mu1<mu2 ? mu1 : mu2);
    if( degree<minmu ) degree = minmu;
   }
   return degree;
  }
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //          Abstract class of an operator set          //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private abstract class InnerOperatorset {
  abstract double and(double a, double b);
  abstract double or(double a, double b);
  abstract double also(double a, double b);
  abstract double imp(double a, double b);
  abstract double not(double a);
  abstract double very(double a);
  abstract double moreorless(double a);
  abstract double slightly(double a);
  abstract double defuz(OutputMembershipFunction mf);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //      Class for the conclusion of a fuzzy rule       //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class InnerConclusion {
  private double degree;
  private InnerMembershipFunction mf;
  private InnerOperatorset op;

  InnerConclusion(double degree, InnerMembershipFunction mf, InnerOperatorset op) {
   this.op = op;
   this.degree = degree;
   this.mf = mf;
  }

  public double degree() { return degree; }
  public double compute(double x) { return op.imp(degree,mf.isEqual(x)); }
  public double center() { return mf.center(); }
  public double basis() { return mf.basis(); }
  public double param(int i) { return mf.param(i); }
  public double min() { return mf.min; }
  public double max() { return mf.max; }
  public double step() { return mf.step; }
  public boolean isSingleton() { 
   return mf.getClass().getName().endsWith("MF_xfl_singleton");
  }
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //      Membership function of an output variable      //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class OutputMembershipFunction implements MembershipFunction {
  public InnerConclusion[] conc;
  public double[] input;
  private InnerOperatorset op;

  OutputMembershipFunction() {
   this.conc = new InnerConclusion[0];
  }

  public void set(int size, InnerOperatorset op, double[] input) {
   this.input = input;
   this.op = op;
   this.conc = new InnerConclusion[size];
  }

  public void set(int pos, double dg, InnerMembershipFunction imf) {
   this.conc[pos] = new InnerConclusion(dg,imf,op);
  }

  public double compute(double x) {
   double dom = conc[0].compute(x);
   for(int i=1; i<conc.length; i++) dom = op.also(dom,conc[i].compute(x));
   return dom;
  }

  public double defuzzify() {
   return op.defuz(this);
  }

  public double min() {
   return conc[0].min();
  }

  public double max() {
   return conc[0].max();
  }

  public double step() {
   return conc[0].step();
  }

  public boolean isDiscrete() {
   for(int i=0; i<conc.length; i++) if(!conc[i].isSingleton()) return false;
   return true;
  }
 
  public double[][] getDiscreteValues() {
   double[][] value = new double[conc.length][2];
   for(int i=0; i<conc.length; i++) {
    value[i][0] = conc[i].param(0);
    value[i][1] = conc[i].degree();
   }
   return value;
  }

 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //      Membership function MF_xfl_trapezoid      //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

  private class MF_xfl_trapezoid extends InnerMembershipFunction {
   double a;
   double b;
   double c;
   double d;

   MF_xfl_trapezoid( double min, double max, double step, double param[]){
    super.min = min;
    super.max = max;
    super.step = step;
    this.a = param[0];
    this.b = param[1];
    this.c = param[2];
    this.d = param[3];
   }
   double param(int _i) {
    switch(_i) {
     case 0: return a;
     case 1: return b;
     case 2: return c;
     case 3: return d;
     default: return 0;
    }
   }
   double isEqual(double x) {
    return (x<a || x>d? 0: (x<b? (x-a)/(b-a) : (x<c?1 : (d-x)/(d-c)))); 
   }
   double isGreaterOrEqual(double x) {
    return (x<a? 0 : (x>b? 1 : (x-a)/(b-a) )); 
   }
   double isSmallerOrEqual(double x) {
    return (x<c? 1 : (x>d? 0 : (d-x)/(d-c) )); 
   }
   double center() {
    return (b+c)/2; 
   }
   double basis() {
    return (d-a); 
   }
  }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //      Membership function MF_xfl_triangle      //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

  private class MF_xfl_triangle extends InnerMembershipFunction {
   double a;
   double b;
   double c;

   MF_xfl_triangle( double min, double max, double step, double param[]){
    super.min = min;
    super.max = max;
    super.step = step;
    this.a = param[0];
    this.b = param[1];
    this.c = param[2];
   }
   double param(int _i) {
    switch(_i) {
     case 0: return a;
     case 1: return b;
     case 2: return c;
     default: return 0;
    }
   }
   double isEqual(double x) {
    return (a<x && x<=b? (x-a)/(b-a) : (b<x && x<c? (c-x)/(c-b) : 0)); 
   }
   double isGreaterOrEqual(double x) {
    return (x<a? 0 : (x>b? 1 : (x-a)/(b-a) )); 
   }
   double isSmallerOrEqual(double x) {
    return (x<b? 1 : (x>c? 0 : (c-x)/(c-b) )); 
   }
   double center() {
    return b; 
   }
   double basis() {
    return (c-a); 
   }
  }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //     Operator set OP_op      //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class OP_op extends InnerOperatorset {
  double and(double a, double b) {
    return (a<b? a : b); 
  }
  double or(double a, double b) {
    return (a>b? a : b); 
  }
  double also(double a, double b) {
    return (a>b? a : b); 
  }
  double imp(double a, double b) {
    return (a<b? a : b); 
  }
  double not(double a) {
    return 1-a; 
  }
  double very(double a) {
   double w = 2.0;
    return Math.pow(a,w); 
  }
  double moreorless(double a) {
   double w = 0.5;
    return Math.pow(a,w); 
  }
  double slightly(double a) {
    return 4*a*(1-a); 
  }
 double defuz(OutputMembershipFunction mf) {
   double min = mf.min();
   double max = mf.max();
   double step = mf.step();
  double num=0, denom=0;
  for(double x=min; x<=max; x+=step) {
   double m = mf.compute(x);
   num += x*m;
   denom += m;
  }
  if(denom==0) return (min+max)/2;
  return num/denom;
  }
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Type TP_e1  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class TP_e1 {
  private double min = 0.0;
  private double max = 13.0;
  private double step = 1.0833333333333333;
  double _p_Ambulancia[] = { -1.1818181818181819,0.0,2.3636363636363638,3.545454545454546 };
  double _p_Bombero[] = { 2.3636363636363638,3.545454545454546,5.909090909090909,7.090909090909092 };
  double _p_Policia[] = { 5.909090909090909,7.090909090909092,9.454545454545455,10.636363636363637 };
  double _p_Funcionario[] = { 9.454545454545455,10.636363636363637,13.0,14.181818181818183 };
  MF_xfl_trapezoid Ambulancia = new MF_xfl_trapezoid(min,max,step,_p_Ambulancia);
  MF_xfl_trapezoid Bombero = new MF_xfl_trapezoid(min,max,step,_p_Bombero);
  MF_xfl_trapezoid Policia = new MF_xfl_trapezoid(min,max,step,_p_Policia);
  MF_xfl_trapezoid Funcionario = new MF_xfl_trapezoid(min,max,step,_p_Funcionario);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Type TP_e2i  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class TP_e2i {
  private double min = 0.0;
  private double max = 60.0;
  private double step = 1.0169491525423728;
  double _p_HaceMuyPoco[] = { -12.0,0.0,5.0,10.0 };
  double _p_HacePoco[] = { 8.0,15.0,22.0 };
  double _p_HaceMucho[] = { 20.0,28.0,36.0 };
  double _p_HaceMuchisimo[] = { 32.0,45.0,60.0,72.0 };
  MF_xfl_trapezoid HaceMuyPoco = new MF_xfl_trapezoid(min,max,step,_p_HaceMuyPoco);
  MF_xfl_triangle HacePoco = new MF_xfl_triangle(min,max,step,_p_HacePoco);
  MF_xfl_triangle HaceMucho = new MF_xfl_triangle(min,max,step,_p_HaceMucho);
  MF_xfl_trapezoid HaceMuchisimo = new MF_xfl_trapezoid(min,max,step,_p_HaceMuchisimo);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Type TP_e3a  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class TP_e3a {
  private double min = 0.0;
  private double max = 60.0;
  private double step = 1.0169491525423728;
  double _p_HaceMuyPoco[] = { -12.0,0.0,5.0,10.0 };
  double _p_HacePoco[] = { 8.0,15.0,22.0 };
  double _p_HaceMucho[] = { 20.0,28.0,36.0 };
  double _p_HaceMuchisimo[] = { 32.0,45.0,60.0,72.0 };
  MF_xfl_trapezoid HaceMuyPoco = new MF_xfl_trapezoid(min,max,step,_p_HaceMuyPoco);
  MF_xfl_triangle HacePoco = new MF_xfl_triangle(min,max,step,_p_HacePoco);
  MF_xfl_triangle HaceMucho = new MF_xfl_triangle(min,max,step,_p_HaceMucho);
  MF_xfl_trapezoid HaceMuchisimo = new MF_xfl_trapezoid(min,max,step,_p_HaceMuchisimo);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Type TP_e4d  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class TP_e4d {
  private double min = 0.0;
  private double max = 60.0;
  private double step = 1.0169491525423728;
  double _p_HaceMuyPoco[] = { -12.0,0.0,5.0,10.0 };
  double _p_HacePoco[] = { 8.0,15.0,22.0 };
  double _p_HaceMucho[] = { 20.0,28.0,36.0 };
  double _p_HaceMuchisimo[] = { 32.0,45.0,60.0,72.0 };
  MF_xfl_trapezoid HaceMuyPoco = new MF_xfl_trapezoid(min,max,step,_p_HaceMuyPoco);
  MF_xfl_triangle HacePoco = new MF_xfl_triangle(min,max,step,_p_HacePoco);
  MF_xfl_triangle HaceMucho = new MF_xfl_triangle(min,max,step,_p_HaceMucho);
  MF_xfl_trapezoid HaceMuchisimo = new MF_xfl_trapezoid(min,max,step,_p_HaceMuchisimo);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Type TP_e5f  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class TP_e5f {
  private double min = 0.0;
  private double max = 60.0;
  private double step = 1.0169491525423728;
  double _p_HaceMuyPoco[] = { -12.0,0.0,5.0,10.0 };
  double _p_HacePoco[] = { 8.0,15.0,22.0 };
  double _p_HaceMucho[] = { 20.0,28.0,36.0 };
  double _p_HaceMuchisimo[] = { 32.0,45.0,60.0,72.0 };
  MF_xfl_trapezoid HaceMuyPoco = new MF_xfl_trapezoid(min,max,step,_p_HaceMuyPoco);
  MF_xfl_triangle HacePoco = new MF_xfl_triangle(min,max,step,_p_HacePoco);
  MF_xfl_triangle HaceMucho = new MF_xfl_triangle(min,max,step,_p_HaceMucho);
  MF_xfl_trapezoid HaceMuchisimo = new MF_xfl_trapezoid(min,max,step,_p_HaceMuchisimo);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Type TP_s1  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private class TP_s1 {
  private double min = 0.0;
  private double max = 40.0;
  private double step = 1.0256410256410255;
  double _p_Baja[] = { -8.0,0.0,8.0,16.0 };
  double _p_Media[] = { 8.0,16.0,24.0 };
  double _p_Alta[] = { 16.0,24.0,32.0 };
  double _p_MuyAlta[] = { 24.0,32.0,40.0,48.0 };
  MF_xfl_trapezoid Baja = new MF_xfl_trapezoid(min,max,step,_p_Baja);
  MF_xfl_triangle Media = new MF_xfl_triangle(min,max,step,_p_Media);
  MF_xfl_triangle Alta = new MF_xfl_triangle(min,max,step,_p_Alta);
  MF_xfl_trapezoid MuyAlta = new MF_xfl_trapezoid(min,max,step,_p_MuyAlta);
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Rulebase RL_rbi  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private MembershipFunction[] RL_rbi(MembershipFunction TipoVehiculo, MembershipFunction EvIncendio) {
  double _rl;
  double _input[] = new double[2];
  if(TipoVehiculo instanceof FuzzySingleton)
   _input[0] = ((FuzzySingleton) TipoVehiculo).getValue();
  if(EvIncendio instanceof FuzzySingleton)
   _input[1] = ((FuzzySingleton) EvIncendio).getValue();
  OP_op _op = new OP_op();
  OutputMembershipFunction prioridad = new OutputMembershipFunction();
  prioridad.set(16,_op,_input);
  TP_e1 _t_TipoVehiculo = new TP_e1();
  TP_e2i _t_EvIncendio = new TP_e2i();
  TP_s1 _t_prioridad = new TP_s1();
  int _i_prioridad=0;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuyPoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvIncendio.HacePoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvIncendio.HaceMucho.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuchisimo.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuyPoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.MuyAlta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvIncendio.HacePoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.MuyAlta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvIncendio.HaceMucho.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuchisimo.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuyPoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvIncendio.HacePoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvIncendio.HaceMucho.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuchisimo.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuyPoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvIncendio.HacePoco.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvIncendio.HaceMucho.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvIncendio.HaceMuchisimo.isEqual(EvIncendio));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  MembershipFunction[] _output = new MembershipFunction[1];
  _output[0] = new FuzzySingleton(prioridad.defuzzify());
  return _output;
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Rulebase RL_rba  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private MembershipFunction[] RL_rba(MembershipFunction TipoVehiculo, MembershipFunction EvAccidente) {
  double _rl;
  double _input[] = new double[2];
  if(TipoVehiculo instanceof FuzzySingleton)
   _input[0] = ((FuzzySingleton) TipoVehiculo).getValue();
  if(EvAccidente instanceof FuzzySingleton)
   _input[1] = ((FuzzySingleton) EvAccidente).getValue();
  OP_op _op = new OP_op();
  OutputMembershipFunction prioridad = new OutputMembershipFunction();
  prioridad.set(16,_op,_input);
  TP_e1 _t_TipoVehiculo = new TP_e1();
  TP_e3a _t_EvAccidente = new TP_e3a();
  TP_s1 _t_prioridad = new TP_s1();
  int _i_prioridad=0;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuyPoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvAccidente.HacePoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvAccidente.HaceMucho.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuchisimo.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuyPoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvAccidente.HacePoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Alta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvAccidente.HaceMucho.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuchisimo.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuyPoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.MuyAlta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvAccidente.HacePoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.MuyAlta); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvAccidente.HaceMucho.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Media); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuchisimo.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuyPoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvAccidente.HacePoco.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvAccidente.HaceMucho.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvAccidente.HaceMuchisimo.isEqual(EvAccidente));
  prioridad.set(_i_prioridad,_rl, _t_prioridad.Baja); _i_prioridad++;
  MembershipFunction[] _output = new MembershipFunction[1];
  _output[0] = new FuzzySingleton(prioridad.defuzzify());
  return _output;
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Rulebase RL_rbd  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private MembershipFunction[] RL_rbd(MembershipFunction TipoVehiculo, MembershipFunction EvDisturbio) {
  double _rl;
  double _input[] = new double[2];
  if(TipoVehiculo instanceof FuzzySingleton)
   _input[0] = ((FuzzySingleton) TipoVehiculo).getValue();
  if(EvDisturbio instanceof FuzzySingleton)
   _input[1] = ((FuzzySingleton) EvDisturbio).getValue();
  OP_op _op = new OP_op();
  OutputMembershipFunction Prioridad = new OutputMembershipFunction();
  Prioridad.set(16,_op,_input);
  TP_e1 _t_TipoVehiculo = new TP_e1();
  TP_e4d _t_EvDisturbio = new TP_e4d();
  TP_s1 _t_Prioridad = new TP_s1();
  int _i_Prioridad=0;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuyPoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvDisturbio.HacePoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMucho.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuchisimo.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.MuyAlta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuyPoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvDisturbio.HacePoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMucho.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Alta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuchisimo.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.MuyAlta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuyPoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.MuyAlta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvDisturbio.HacePoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.MuyAlta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMucho.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Alta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuchisimo.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Alta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuyPoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvDisturbio.HacePoco.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMucho.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvDisturbio.HaceMuchisimo.isEqual(EvDisturbio));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  MembershipFunction[] _output = new MembershipFunction[1];
  _output[0] = new FuzzySingleton(Prioridad.defuzzify());
  return _output;
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //  Rulebase RL_rbf  //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 private MembershipFunction[] RL_rbf(MembershipFunction TipoVehiculo, MembershipFunction EvFuncionario) {
  double _rl;
  double _input[] = new double[2];
  if(TipoVehiculo instanceof FuzzySingleton)
   _input[0] = ((FuzzySingleton) TipoVehiculo).getValue();
  if(EvFuncionario instanceof FuzzySingleton)
   _input[1] = ((FuzzySingleton) EvFuncionario).getValue();
  OP_op _op = new OP_op();
  OutputMembershipFunction Prioridad = new OutputMembershipFunction();
  Prioridad.set(16,_op,_input);
  TP_e1 _t_TipoVehiculo = new TP_e1();
  TP_e5f _t_EvFuncionario = new TP_e5f();
  TP_s1 _t_Prioridad = new TP_s1();
  int _i_Prioridad=0;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuyPoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvFuncionario.HacePoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMucho.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Ambulancia.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuchisimo.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuyPoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvFuncionario.HacePoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMucho.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Bombero.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuchisimo.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuyPoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvFuncionario.HacePoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMucho.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Policia.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuchisimo.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuyPoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Alta); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvFuncionario.HacePoco.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Media); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMucho.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  _rl = _op.and(_t_TipoVehiculo.Funcionario.isEqual(TipoVehiculo),_t_EvFuncionario.HaceMuchisimo.isEqual(EvFuncionario));
  Prioridad.set(_i_Prioridad,_rl, _t_Prioridad.Baja); _i_Prioridad++;
  MembershipFunction[] _output = new MembershipFunction[1];
  _output[0] = new FuzzySingleton(Prioridad.defuzzify());
  return _output;
 }

 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//
 //               Fuzzy Inference Engine                //
 //+++++++++++++++++++++++++++++++++++++++++++++++++++++//

 public double[] crispInference(double[] _input) {
  MembershipFunction TipoVehiculo = new FuzzySingleton(_input[0]);
  MembershipFunction EventIncendio = new FuzzySingleton(_input[1]);
  MembershipFunction EventAccidente = new FuzzySingleton(_input[2]);
  MembershipFunction EventDistur = new FuzzySingleton(_input[3]);
  MembershipFunction EventFuncionario = new FuzzySingleton(_input[4]);
  MembershipFunction PrioI;
  MembershipFunction PrioA;
  MembershipFunction PrioD;
  MembershipFunction PrioF;
  MembershipFunction[] _call;
  _call = RL_rbi(TipoVehiculo,EventIncendio); PrioI=_call[0];
  _call = RL_rba(TipoVehiculo,EventAccidente); PrioA=_call[0];
  _call = RL_rbd(TipoVehiculo,EventDistur); PrioD=_call[0];
  _call = RL_rbf(TipoVehiculo,EventFuncionario); PrioF=_call[0];
  double _output[] = new double[4];
  if(PrioI instanceof FuzzySingleton)
   _output[0] = ((FuzzySingleton) PrioI).getValue();
  else _output[0] = ((OutputMembershipFunction) PrioI).defuzzify();
  if(PrioA instanceof FuzzySingleton)
   _output[1] = ((FuzzySingleton) PrioA).getValue();
  else _output[1] = ((OutputMembershipFunction) PrioA).defuzzify();
  if(PrioD instanceof FuzzySingleton)
   _output[2] = ((FuzzySingleton) PrioD).getValue();
  else _output[2] = ((OutputMembershipFunction) PrioD).defuzzify();
  if(PrioF instanceof FuzzySingleton)
   _output[3] = ((FuzzySingleton) PrioF).getValue();
  else _output[3] = ((OutputMembershipFunction) PrioF).defuzzify();
  return _output;
 }

 public double[] crispInference(MembershipFunction[] _input) {
  MembershipFunction TipoVehiculo = _input[0];
  MembershipFunction EventIncendio = _input[1];
  MembershipFunction EventAccidente = _input[2];
  MembershipFunction EventDistur = _input[3];
  MembershipFunction EventFuncionario = _input[4];
  MembershipFunction PrioI;
  MembershipFunction PrioA;
  MembershipFunction PrioD;
  MembershipFunction PrioF;
  MembershipFunction[] _call;
  _call = RL_rbi(TipoVehiculo,EventIncendio); PrioI=_call[0];
  _call = RL_rba(TipoVehiculo,EventAccidente); PrioA=_call[0];
  _call = RL_rbd(TipoVehiculo,EventDistur); PrioD=_call[0];
  _call = RL_rbf(TipoVehiculo,EventFuncionario); PrioF=_call[0];
  double _output[] = new double[4];
  if(PrioI instanceof FuzzySingleton)
   _output[0] = ((FuzzySingleton) PrioI).getValue();
  else _output[0] = ((OutputMembershipFunction) PrioI).defuzzify();
  if(PrioA instanceof FuzzySingleton)
   _output[1] = ((FuzzySingleton) PrioA).getValue();
  else _output[1] = ((OutputMembershipFunction) PrioA).defuzzify();
  if(PrioD instanceof FuzzySingleton)
   _output[2] = ((FuzzySingleton) PrioD).getValue();
  else _output[2] = ((OutputMembershipFunction) PrioD).defuzzify();
  if(PrioF instanceof FuzzySingleton)
   _output[3] = ((FuzzySingleton) PrioF).getValue();
  else _output[3] = ((OutputMembershipFunction) PrioF).defuzzify();
  return _output;
 }

 public MembershipFunction[] fuzzyInference(double[] _input) {
  MembershipFunction TipoVehiculo = new FuzzySingleton(_input[0]);
  MembershipFunction EventIncendio = new FuzzySingleton(_input[1]);
  MembershipFunction EventAccidente = new FuzzySingleton(_input[2]);
  MembershipFunction EventDistur = new FuzzySingleton(_input[3]);
  MembershipFunction EventFuncionario = new FuzzySingleton(_input[4]);
  MembershipFunction PrioI;
  MembershipFunction PrioA;
  MembershipFunction PrioD;
  MembershipFunction PrioF;
  MembershipFunction[] _call;
  _call = RL_rbi(TipoVehiculo,EventIncendio); PrioI=_call[0];
  _call = RL_rba(TipoVehiculo,EventAccidente); PrioA=_call[0];
  _call = RL_rbd(TipoVehiculo,EventDistur); PrioD=_call[0];
  _call = RL_rbf(TipoVehiculo,EventFuncionario); PrioF=_call[0];
  MembershipFunction _output[] = new MembershipFunction[4];
  _output[0] = PrioI;
  _output[1] = PrioA;
  _output[2] = PrioD;
  _output[3] = PrioF;
  return _output;
 }

 public MembershipFunction[] fuzzyInference(MembershipFunction[] _input) {
  MembershipFunction TipoVehiculo = _input[0];
  MembershipFunction EventIncendio = _input[1];
  MembershipFunction EventAccidente = _input[2];
  MembershipFunction EventDistur = _input[3];
  MembershipFunction EventFuncionario = _input[4];
  MembershipFunction PrioI;
  MembershipFunction PrioA;
  MembershipFunction PrioD;
  MembershipFunction PrioF;
  MembershipFunction[] _call;
  _call = RL_rbi(TipoVehiculo,EventIncendio); PrioI=_call[0];
  _call = RL_rba(TipoVehiculo,EventAccidente); PrioA=_call[0];
  _call = RL_rbd(TipoVehiculo,EventDistur); PrioD=_call[0];
  _call = RL_rbf(TipoVehiculo,EventFuncionario); PrioF=_call[0];
  MembershipFunction _output[] = new MembershipFunction[4];
  _output[0] = PrioI;
  _output[1] = PrioA;
  _output[2] = PrioD;
  _output[3] = PrioF;
  return _output;
 }

}

