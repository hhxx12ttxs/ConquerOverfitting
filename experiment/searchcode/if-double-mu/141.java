/*
 * unif_rand() produces different random numbers when compared to R
 * whatever the seed is. I think we can pass it until this gets a higher priorty.
 */
package org.renjin.stats.internals.distributions;



//import org.apache.commons.math.random.MersenneTwister;
//import org.renjin.eval.Context;
//import org.renjin.eval.EvalException;
//import org.renjin.eval.Session;
//import org.renjin.invoke.annotations.Builtin;
//import org.renjin.invoke.annotations.Current;
//import org.renjin.invoke.annotations.Internal;
import java.util.ArrayList;
import java.util.List;

import org.renjin.sexp.*;

import beast.util.MersenneTwisterFast;
import beast.util.Randomizer;


public class RNG {

  public MersenneTwisterFast mersenneTwisterAlg = null;
  public RNGtype RNG_kind = RNGtype.MERSENNE_TWISTER; //default
  public N01type N01_kind = N01type.INVERSION; //default
  int randomseed = 0;
//  public Session context;


  public RNG(/*Session globals*/){
    //this.context = globals;
  }

  //@Internal
//  public static IntVector RNGkind(/*@Current Context context,*/ SEXP kindExp, SEXP normalkindExp) {
//    //RNG rng = context.getSession().rng;  
//    
//    if(kindExp != Null.INSTANCE) {
//      int kind = ((AtomicVector)kindExp).getElementAsInt(0);
//      try {
//        rng.RNG_kind = RNGtype.values()[kind];
//      } catch (Exception e) {
//        throw new EvalException("RNGkind: unimplemented RNG kind " + kind);
//      }
//    }
//    if(normalkindExp != Null.INSTANCE) {
//      int normalkind = ((AtomicVector)normalkindExp).getElementAsInt(0);
//      try {
//        rng.N01_kind = N01type.values()[normalkind];
//      } catch (Exception e) {
//        throw new EvalException("invalid Normal type in RNGkind");
//      }
//    } 
//
//    return (new IntArrayVector(rng.RNG_kind.ordinal(), rng.N01_kind.ordinal()));
//  }

  /*
   * Primitives.
   */
  //@Internal("set.seed")
  public static void set_seed(/*@Current Context context,*/ int seed) { //, SEXP kind, SEXP normalkind) {
	  Randomizer.setSeed(seed);
//    //RNG rng = context.getSession().rng;
//    rng.randomseed = seed;
//    RNGkind(/*context,*/ kind, normalkind);
//    switch (rng.RNG_kind) {
//    case WICHMANN_HILL:
//      throw new EvalException(rng.RNG_kind + " not implemented yet");
//
//    case MARSAGLIA_MULTICARRY:
//      throw new EvalException(rng.RNG_kind + " not implemented yet");
//
//    case SUPER_DUPER:
//      throw new EvalException(rng.RNG_kind + " not implemented yet");
//
//    case MERSENNE_TWISTER:
//      if (rng.mersenneTwisterAlg == null) {
//        rng.mersenneTwisterAlg = new MersenneTwister(seed);
//      } else {
//        rng.mersenneTwisterAlg.setSeed(seed);
//      }
//      return;
//
//    case KNUTH_TAOCP:
//    case KNUTH_TAOCP2:
//      throw new EvalException(rng.RNG_kind + " not implemented yet");
//    case USER_UNIF:
//      throw new EvalException(rng.RNG_kind + " not implemented yet");
//    default:
//      throw new EvalException(rng.RNG_kind + " not implemented yet");
//    }
  }

  //@Internal
  public static List<Double> runif(/*@Current Context context,*/ int n, double a, double b) {
    //RNG rng = context.getSession().rng;
    List<Double> vb = new ArrayList<Double>(n);
    for (int i = 0; i < n; i++) {
      vb.add(a + context.rng_unif_rand() * (b - a));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rnorm(/*@Current Context context,*/ int n, double mean, double sd) {
    List<Double> vb = new ArrayList<Double>(n);
    for (int i = 0; i < n; i++) {
      vb.add(Normal.rnorm(/*context.getSession(),*/ mean, sd));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rgamma(/*@Current Context context,*/ int n, double shape, double scale) {
    List<Double> vb = new ArrayList<Double>(n);
    for (int i = 0; i < n; i++) {
      vb.add(Gamma.rgamma(/*context.getSession(),*/ shape, scale));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rchisq(/*@Current Context context,*/ int n, double df) {
    List<Double> vb = new ArrayList<Double>(n);
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rchisq(/*context.getSession(),*/ df));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rnchisq(/*@Current Context context,*/ int n, double df, double ncp) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(ChiSquare.rnchisq(/*context.getSession(),*/ df, ncp));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rexp(/*@Current Context context,*/ int n, double invrate) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Exponantial.rexp(/*context.getSession(),*/ invrate));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rpois(/*@Current Context context,*/ int n, double mu) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Poisson.rpois(/*context.getSession(),*/ mu));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rsignrank(/*@Current Context context,*/ int nn, double n) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < nn; i++) {
      vb.add(SignRank.rsignrank(/*context.getSession(),*/ n));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rwilcox(/*@Current Context context,*/ int nn, double m, double n) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < nn; i++) {
      vb.add(Wilcox.rwilcox(/*context.getSession(),*/ m, n));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rgeom(/*@Current Context context,*/ int n, double p) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Geometric.rgeom(/*context.getSession(),*/ p));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rt(/*@Current Context context,*/ int n, double df) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(StudentsT.rt(/*context.getSession(),*/ df));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rcauchy(/*@Current Context context,*/ int n, double location, double scale) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Cauchy.rcauchy(/*context.getSession(),*/ location, scale));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rlnorm(/*@Current Context context,*/ int n, double meanlog, double sdlog) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(LNorm.rlnorm(/*context.getSession(),*/ meanlog, sdlog));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rlogis(/*@Current Context context,*/ int n, double location, double scale) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(RLogis.rlogis(/*context.getSession(),*/ location, scale));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rweibull(/*@Current Context context,*/ int n, double shape, double scale) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Weibull.rweibull(/*context.getSession(),*/ shape, scale));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rnbinom(/*@Current Context context,*/ int n, double size, double prob) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom(/*context.getSession(),*/ size, prob));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rnbinom_mu(/*@Current Context context,*/ int n, double size, double mu) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(NegativeBinom.rnbinom_mu(/*context.getSession(),*/ size, mu));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rbinom(/*@Current Context context,*/ int n, double size, double prob) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Binom.rbinom(/*context.getSession(),*/ size, prob));
    }
    return vb;
  }


  //@Internal
  public static List<Double> rf(/*@Current Context context,*/ int n, double df1, double df2) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(F.rf(/*context.getSession(),*/ df1, df2));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rbeta(/*@Current Context context,*/ int n, double shape1, double shape2) {
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < n; i++) {
      vb.add(Beta.rbeta(/*context.getSession(),*/ shape1, shape2));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rhyper(/*@Current Context context,*/ int nn, double m, double n, double k){
    List<Double> vb = new ArrayList<Double>();
    for (int i = 0; i < nn; i++) {
      vb.add(HyperGeometric.Random_hyper_geometric.rhyper(/*context.getSession(),*/ m, n, k));
    }
    return vb;
  }

  //@Internal
  public static List<Double> rmultinom(/*@Current Context context,*/ int n, int size, List<Double> prob){
    List<Double> vb = new ArrayList<Double>();
    int[] RN = new int[prob.size()];
    for (int i=0;i<n;i++){
    	Double [] pa = prob.toArray(new Double[0]);
    	double [] pa2 = new double[pa.length];
    	for (int k = 0; k < pa.length; k++) {
    		pa2[k] = pa[k];
    	}
      Multinomial.rmultinom(/*context.getSession(),*/ size, pa2, prob.size(), RN);
      for (int j = 0; j < prob.size(); j++) {
        vb.add((double)RN[j]);
      }
    }
    //vb.setAttribute(Symbols.DIM, new IntArrayVector(prob.length(), n));
    return vb;
  }
  /*
   * One of the Most important method in RNG
   * Before creating a random number from the distribution D,
   * we generate a uniform distributed random variable. 
   * 
   * Generated random numbers depend on the algorithm used.
   * As in original interpreter, the default algorithm is MERSENNE_TWISTER.
   * 
   * MERSENNE_TWISTER algorithm is imported from the apache commons math api.
   * But there is a small problem with this. The original interpreter and the renjin
   * produces different pseudo random numbers even the seed is same.
   * 
   * I am leaving this as is, I think it is not a real problem for now, somebody can 
   * correct the mechanism underlying the uniform random number generation for consistency 
   * with the original interpreter. 
   * 
   * Because I have not got the desired outputs, I can not test my generated random numbers. But one can 
   * see that, for example a sample of 1000 random numbers from a Chisquare(15) distribution has an
   * average of nearly 15. Similarly, a sample drawn from a Normal (0,1) distribution has a mean and variance
   * nearly zero and one, respectively.
   * 
   * mhsatman
   */
//  public double unif_rand() {
//    double value;
//
//    switch (this.RNG_kind) {
//
//    case WICHMANN_HILL:
//      throw new EvalException(RNG_kind + " not implemented yet");
//
//    case MARSAGLIA_MULTICARRY:
//      throw new EvalException(RNG_kind + " not implemented yet");
//
//    case SUPER_DUPER:
//      throw new EvalException(RNG_kind + " not implemented yet");
//
//    case MERSENNE_TWISTER:
//      if (mersenneTwisterAlg == null) {
//        if (this.randomseed == 0) {
//          Randomize(RNG_kind);
//        }
//        mersenneTwisterAlg = new MersenneTwister((long) this.randomseed);
//      }
//      return (mersenneTwisterAlg.nextDouble());
//
//    case KNUTH_TAOCP:
//    case KNUTH_TAOCP2:
//      throw new EvalException(RNG_kind + " not implemented yet");
//    case USER_UNIF:
//      throw new EvalException(RNG_kind + " not implemented yet");
//    default:
//      throw new EvalException(RNG_kind + " not implemented yet");
//    }
//  }

  /*
   * This part of R is platform dependent. this formula is random itself :)
   */
//  public void Randomize(RNGtype kind) {
//    int sseed;
//    sseed = (int) (new java.util.Date()).getTime();
//    this.randomseed = sseed;
//    switch (RNG_kind) {
//
//    case WICHMANN_HILL:
//      throw new EvalException(RNG_kind + " not implemented yet");
//
//    case MARSAGLIA_MULTICARRY:
//      throw new EvalException(RNG_kind + " not implemented yet");
//
//    case SUPER_DUPER:
//      throw new EvalException(RNG_kind + " not implemented yet");
//
//    case MERSENNE_TWISTER:
//      if (mersenneTwisterAlg == null) {
//        mersenneTwisterAlg = new MersenneTwister(sseed);
//      } else {
//        mersenneTwisterAlg.setSeed(sseed);
//      }
//      return;
//
//    case KNUTH_TAOCP:
//    case KNUTH_TAOCP2:
//      throw new EvalException(RNG_kind + " not implemented yet");
//    case USER_UNIF:
//      throw new EvalException(RNG_kind + " not implemented yet");
//    default:
//      throw new EvalException(RNG_kind + " not implemented yet");
//    }
//  }
}

