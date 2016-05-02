package edu.cmu.cs.ark.sage;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.math3.random.RandomDataGenerator;
import org.apache.commons.math3.util.FastMath;

import edu.cmu.cs.ark.sage.apps.SupervisedSAGE;
import edu.cmu.cs.ark.sage.effects.Effect;
import edu.cmu.cs.ark.sage.effects.EffectSet;
import edu.cmu.cs.ark.sage.effects.NamedEffect;
import edu.cmu.cs.ark.sage.features.Feature;
import edu.cmu.cs.ark.sage.features.FeatureVector;
import edu.cmu.cs.ark.sage.features.IndexedFeatureVector;
import edu.cmu.cs.ark.sage.features.UnigramFeature;
import edu.cmu.cs.ark.yc.utils.MathMethods;
import edu.cmu.cs.ark.yc.utils.OWLQN;
import edu.cmu.cs.ark.yc.utils.types.HashList;
import edu.stanford.nlp.optimization.DiffFunction;
import gnu.trove.iterator.TIntDoubleIterator;
import gnu.trove.iterator.TIntIterator;
import gnu.trove.list.array.TIntArrayList;

/**
 * Main entry point to use {@link SAGE}. This class implements the functions needed to perform M-step optimization with {@link SAGE} word vectors. See source code of {@link SupervisedSAGE} for an example of how to use {@link SAGE}.
 * <p>
 * <b>Concepts</b>
 * <ul>
 * <li>{@link Effect} refers to individual {@link SAGE} eta vectors, i.e there are one effect per eta vector. {@link Effect}s can be described in several manners. The most common ones are {@link NamedEffect} and fixed {@link Effect}. See {@link Effect} and its subclasses for a list of available effect types. {@link Effect} also store the associated L1 and L2 regularization weights.</li>
 * <li>Each document is described by a set of {@link Effect}s, known as {@link EffectSet}. There can be any number of {@link EffectSet}s containing any combination of {@link Effect}s. When optimizing for each {@link Effect}, words "generated" by {@link EffectSet} containing the {@link Effect} will be counted.</li>
 * <li>{@link Feature} describes the "feature" types of a document. Most of the time, these are simply {@link UnigramFeature}.
 * </ul>
 * <p>
 * <b>Using {@link SAGE}</b> consists of several steps:
 * <ol>
 * <li><b>Set up</b> You first set up the types of {@link Effect}s, {@link Feature}s and {@link EffectSet}s that {@link SAGE} will be dealing with when you load your documents. Each document will have its own {@link EffectSet} (probably not unique), and every word will probably belong to a {@link UnigramFeature}. You add {@link Effect}s using {@link SAGE#addEffect(Effect)}, {@link Feature}s using {@link SAGE#addFeature(Feature)} and {@link EffectSet}s using {@link SAGE#addEffectSet(EffectSet)}.</li>
 * <li><b>Initialization</b> After loading required {@link Effect}s, {@link Feature}s and {@link EffectSet}s, we are ready to {@link SAGE#initialize()} the system. This step will create and instantiate the eta vectors necessary for optimization. After this point, you can no longer add new {@link Effect}s, {@link Feature}s or {@link EffectSet}s. However, you can still change feature counts and effect regularization weights.</li>
 * <li><b>Optimization</b> Depending on whether there are latent variables, you adjust your feature counts accordingly by {@link EffectSet}s, not by documents. This is because {@link SAGE} does not store the document structure, it instead treats each {@link EffectSet} as kind of a "document". You update feature counts using {@link SAGE#incrementFeatureCount(EffectSet, Feature, double)} and {@link SAGE#incrementFeatureVector(EffectSet, FeatureVector)} family of methods. After counts are updated, you call {@link #prepareOptimization()} and then {@link #optimizeAll(boolean)}/{@link #optimizeEta(Effect)} to start optimization.</li>
 * </ol>
 * <p>
 * <b>References</b>
 * <ul>
 * <li>Eisenstein, Jacob, Amr Ahmed, and Eric P. Xing. "Sparse additive generative models of text." In <i>Proceedings of ICML</i>. pp. 1041-1048. 2011. [<a href="http://www.cc.gatech.edu/~jeisenst/papers/icml2011.pdf">PDF</a>]</li>
 * </ul>
 * 
 * @author Yanchuan Sim
 * @version 0.1
 * @see SupervisedSAGE
 */
public class SAGE
{
  private final static RandomDataGenerator R = new RandomDataGenerator();

  private final HashList<Effect> effects = new HashList<>();
  private final HashList<EffectSet> effect_sets = new HashList<>();
  private final HashList<Feature> features = new HashList<>();
  private int dimensions;

  /**
   * Used internally to determine if features, effects and effects set are now fixed. Once <code>initialized</code> is set to <code>true</code>, these things cannot be changed.
   */
  private boolean initialized = false;

  /**
   * A list of feature vectors, where each feature vector belong to a particular effect set. This is essentially a combination of SAGE vectors.
   */
  private IndexedFeatureVector[] feature_counts = null;
  private double[] feature_sums = null;

  /**
   * The actual SAGE vectors, stored as arrays of double.
   */
  private double[][] etas = null;

  private double[] log_Zs = null;
  private double[][] log_eta_probs = null;

  /**
   * An array of {@link TIntArrayList} where <code>effects_lookup[e]</code> is a list of effect sets indexes where effect <code>e</code> can be found.
   */
  private TIntArrayList[] effects_lookup = null;

  private int[][] es_effect_indexes = null;

  private int owl_iters = 10;

  /**
   * This step will create and instantiate the eta vectors necessary for optimization.
   * 
   * @param randomInit
   *          Set <code>true</code> to randomly initialize the eta vectors.
   * @see SAGE
   */
  public void initialize(boolean randomInit)
  {
    feature_counts = new IndexedFeatureVector[effect_sets.size()];
    feature_sums = new double[effect_sets.size()];
    for (int i = 0; i < effect_sets.size(); i++)
      feature_counts[i] = new IndexedFeatureVector();

    es_effect_indexes = new int[effect_sets.size()][];
    effects_lookup = new TIntArrayList[effects.size()];

    for (int i = 0; i < effects.size(); i++)
      effects_lookup[i] = new TIntArrayList();

    for (int i = 0; i < effect_sets.size(); i++)
    {
      es_effect_indexes[i] = new int[effect_sets.size()];
      int j = 0;
      for (Effect e : effect_sets.get(i))
      {
        es_effect_indexes[i][j++] = effects.getIndex(e);
        effects_lookup[effects.getIndex(e)].add(i);
      }
    }

    dimensions = features.size();
    etas = new double[effects.size()][dimensions];
    for (int i = 0; i < effects.size(); i++)
      for (int j = 0; j < features.size(); j++)
        etas[i][j] = effects.get(i).isFixed() ? 1.0 : (randomInit ? R.nextGaussian(0, 1.0 / Math.sqrt(effects.get(i).getL1Regularizer())) : 0.0);

    log_Zs = new double[effect_sets.size()];
    log_eta_probs = new double[effect_sets.size()][features.size()];
    computeEtaProbabilities();

    initialized = true;
  }

  /**
   * Initializes the {@link SAGE} object (with random initialization).
   * 
   * @see #initialize(boolean)
   */
  public void initialize()
  {
    initialize(true);
  }

  /**
   * This step is to be run before every round of optimization. It basically sets up the feature sums and caches necessary data to improve efficiency.
   */
  public void prepareOptimization()
  {
    computeFeatureSums();
  }

  /**
   * Returns the log-likelihood of observing given features with current eta vectors (includes the regularization penalty).
   * 
   * @return the log-likelihood of observing given features with current eta vectors.
   */
  public double computeLogLikelihood()
  {
    computeEtaProbabilities();
    double ll = 0.0;
    for (int i = 0; i < effect_sets.size(); i++)
    {
      for (TIntDoubleIterator it_feat = feature_counts[i].iterator(); it_feat.hasNext();)
      {
        it_feat.advance();
        ll += it_feat.value() * log_eta_probs[i][it_feat.key()];
      }
    }

    for (int i = 0; i < effects.size(); i++)
    {
      double l1_norm = 0.0;
      double l2_norm = 0.0;
      for (double x : etas[i])
      {
        l1_norm += FastMath.abs(x);
        l2_norm += x * x;
      }
      ll -= l1_norm * effects.get(i).getL1Regularizer();
      ll -= l2_norm * effects.get(i).getL2Regularizer();
    }

    return ll;
  }

  /**
   * Compute feature probabilities for each effect set. Call after every round of optimization to update these values or {@link #featureProbability(EffectSet, Feature)} family of methods would return state results.
   */
  public void computeEtaProbabilities()
  {
    for (int i = 0; i < effect_sets.size(); i++)
    {
      double log_Z = 0.0;
      for (int k : es_effect_indexes[i])
        log_Z += etas[k][0];
      log_eta_probs[i][0] = log_Z;

      for (int j = 1; j < features.size(); j++)
      {
        double eta_sum = 0.0;
        for (int k : es_effect_indexes[i])
          eta_sum += etas[k][j];

        log_Z = MathMethods.logSum(log_Z, eta_sum);
        log_eta_probs[i][j] = eta_sum;
      }

      log_Zs[i] = log_Z;
      for (int j = 0; j < features.size(); j++)
        log_eta_probs[i][j] -= log_Z;
    }
  }

  /**
   * Compute feature sums from feature counts for each {@link EffectSet}.
   */
  private void computeFeatureSums()
  {
    for (int es = 0; es < feature_counts.length; es++)
    {
      feature_sums[es] = 0;

      for (TIntDoubleIterator it_feat = feature_counts[es].iterator(); it_feat.hasNext();)
      {
        it_feat.advance();
        feature_sums[es] += it_feat.value();
      }
    }
  }

  /**
   * Optimize all effects.
   * 
   * @param randomOrder
   *          Set <code>true</code> to optimize them in shuffled order.
   */
  public void optimizeAll(boolean randomOrder)
  {
    List<Effect> effect_order = new ArrayList<>(getEffectList());

    if (randomOrder)
      Collections.shuffle(effect_order);

    for (Effect eff : effect_order)
      optimizeEta(eff);
  }

  /**
   * Optimize the ith eta.
   * 
   * @param effectIndex
   *          The ith indexed eta to optimize.
   */
  public void optimizeEta(int effectIndex)
  {
    Effect eff = effects.get(effectIndex);
    if (eff.isFixed())
      return;

    OWLQN owlqn_optimizer = new OWLQN(true);
    owlqn_optimizer.setMaxIters(owl_iters);

    double[] eta = new double[dimensions];
    for (int i = 0; i < dimensions; i++)
      eta[i] = etas[effectIndex][i];

    owlqn_optimizer.minimize(new MaximizeEtaFunction(effectIndex, eff.getL2Regularizer()), eta, eff.getL1Regularizer());

    for (int i = 0; i < dimensions; i++)
      etas[effectIndex][i] = eta[i];
  }

  /**
   * Optimize the eta vector corresponding to {@link Effect} <code>effect</code>.
   * 
   * @param effect
   *          The {@link Effect} to optimize its eta vector for.
   */
  public void optimizeEta(Effect effect)
  {
    optimizeEta(effects.getIndex(effect));
  }

  /**
   * Unsets all data and eta vectors. Releases memory to the garbage collector. After this step, you can add {@link Effect}, {@link EffectSet} and {@link Feature} once again.
   */
  public void uninitialize()
  {
    feature_counts = null;
    feature_sums = null;
    effects_lookup = null;
    etas = null;
    log_Zs = null;
    log_eta_probs = null;
    es_effect_indexes = null;
    dimensions = -1;
    initialized = false;

    System.gc();
  }

  /**
   * Returns the probability of <code>feature</code> being generated by the set of effects <code>es</code>, i.e <code>p(feature | es)</code>.
   * 
   * @param es
   *          {@link EffectSet} generating the feature.
   * @param feature
   *          {@link Feature} to be generated.
   * @return the feature probability.
   */
  public double featureProbability(EffectSet es, Feature feature)
  {
    return featureProbability(effect_sets.getIndex(es), features.getIndex(feature));
  }

  /**
   * See {@link #featureProbability(EffectSet, Feature)}.
   * 
   * @param es
   *          {@link EffectSet} generating the feature.
   * @param feature_index
   *          {@link Feature} (by index) to be generated.
   * @return the feature probability.
   * @see #featureProbability(EffectSet, Feature)
   */
  public double featureProbability(EffectSet es, int feature_index)
  {
    return featureProbability(effect_sets.getIndex(es), feature_index);
  }

  /**
   * See {@link #featureProbability(EffectSet, Feature)}.
   * 
   * @param es_index
   *          {@link EffectSet} (by index) generating the feature.
   * @param feature
   *          {@link Feature} to be generated.
   * @return the feature probability.
   * @see #featureProbability(EffectSet, Feature)
   */
  public double featureProbability(int es_index, Feature feature)
  {
    return featureProbability(es_index, features.getIndex(feature));
  }

  /**
   * See {@link #featureProbability(EffectSet, Feature)}.
   * 
   * @param es_index
   *          {@link EffectSet} (by index) generating the feature.
   * @param feature_index
   *          {@link Feature} (by index) to be generated.
   * @return the feature probability.
   * @see #featureProbability(EffectSet, Feature)
   */
  public double featureProbability(int es_index, int feature_index)
  {
    return FastMath.exp(log_eta_probs[es_index][feature_index]);
  }

  /**
   * Same as {@link #featureProbability(EffectSet, Feature)} but returns the log instead.
   * 
   * @param es
   *          {@link EffectSet} generating the feature.
   * @param feature
   *          {@link Feature} to be generated.
   * @return log feature probability.
   */
  public double logFeatureProbability(EffectSet es, Feature feature)
  {
    return logFeatureProbability(effect_sets.getIndex(es), features.getIndex(feature));
  }

  /**
   * See {@link #logFeatureProbability(EffectSet, Feature)}
   * 
   * @param es
   *          {@link EffectSet} generating the feature.
   * @param feature_index
   *          {@link Feature} (by index) to be generated.
   * @return log feature probability.
   * @see #logFeatureProbability(EffectSet, Feature)
   */
  public double logFeatureProbability(EffectSet es, int feature_index)
  {
    return logFeatureProbability(effect_sets.getIndex(es), feature_index);
  }

  /**
   * See {@link #logFeatureProbability(EffectSet, Feature)}
   * 
   * @param es_index
   *          {@link EffectSet} (by index) generating the feature.
   * @param feature
   *          {@link Feature} to be generated.
   * @return log feature probability.
   * @see #logFeatureProbability(EffectSet, Feature)
   */
  public double logFeatureProbability(int es_index, Feature feature)
  {
    return logFeatureProbability(es_index, features.getIndex(feature));
  }

  /**
   * See {@link #logFeatureProbability(EffectSet, Feature)}
   * 
   * @param es_index
   *          {@link EffectSet} (by index_ generating the feature.
   * @param feature_index
   *          {@link Feature} (by index) to be generated.
   * @return log feature probability.
   * @see #logFeatureProbability(EffectSet, Feature)
   */
  public double logFeatureProbability(int es_index, int feature_index)
  {
    return log_eta_probs[es_index][feature_index];
  }

  /**
   * Adds an {@link Effect} to {@link SAGE}. Does nothing if {@link Effect} already exists.
   * 
   * @param effect
   *          {@link Effect} to add to {@link SAGE}.
   */
  public void addEffect(Effect effect)
  {
    if (initialized)
      throw new RuntimeException("You cannot modify SAGE effects and features after it has been initialized.");

    effects.add(effect);
  }

  /**
   * Returns the index of {@link Effect} effect in the internal effect lists.
   * 
   * @param effect
   *          {@link Effect} to find.
   * @return the index or -1 if not found.
   */
  public int findEffect(Effect effect)
  {
    Integer i = effects.getIndex(effect);
    return (i == null ? -1 : i);
  }

  /**
   * Returns the list of effects that {@link SAGE} knows of.
   * 
   * @return the list of effects that {@link SAGE} knows of.
   */
  public List<Effect> getEffectList()
  {
    return effects.getUnmodifiableList();
  }

  /**
   * Adds an {@link EffectSet} to {@link SAGE}.
   * 
   * @param effect_set
   *          {@link EffectSet} to add to {@link SAGE}.
   */
  public void addEffectSet(EffectSet effect_set)
  {
    if (initialized)
      throw new RuntimeException("You cannot modify SAGE effects and features after it has been initialized.");
    effect_sets.add(effect_set);
  }

  /**
   * Returns the unmodifiable list of {@link EffectSet}s that {@link SAGE} knows of.
   * 
   * @return the list of {@link EffectSet}s that {@link SAGE} knows of.
   */
  public List<EffectSet> getEffectSetList()
  {
    return effect_sets.getUnmodifiableList();
  }

  /**
   * Finds the {@link EffectSet} <code>es</code> in our internal list.
   * 
   * @param es
   *          {@link EffectSet} to find.
   * @return the index of the effect set, or -1 if none is found.
   */
  public int findEffectSet(EffectSet es)
  {
    Integer i = effect_sets.getIndex(es);
    return (i == null ? -1 : i);
  }

  /**
   * Adds a {@link Feature} to {@link SAGE}.
   * 
   * @param feature
   *          {@link Feature} to add to {@link SAGE}.
   */
  public void addFeature(Feature feature)
  {
    if (initialized)
      throw new RuntimeException("You cannot modify SAGE effects and features after it has been initialized.");
    features.add(feature);
  }

  /**
   * Returns the number of features in {@link SAGE}.
   * 
   * @return the number of features in {@link SAGE}.
   */
  public int getNumberOfFeatures()
  {
    return features.size();
  }

  /**
   * Returns the unmodifiable list of features that {@link SAGE} knows of.
   * 
   * @return the list of features that {@link SAGE} knows of.
   */
  public List<Feature> getFeatureList()
  {
    return features.getUnmodifiableList();
  }

  /**
   * Finds a feature in {@link SAGE}.
   * 
   * @param f
   *          {@link Feature} to find in {@link SAGE}.
   * @return the index or -1 if not found.
   */
  public int findFeature(Feature f)
  {
    Integer i = features.getIndex(f);
    return (i == null ? -1 : i);
  }

  /**
   * Remove all features from {@link SAGE}.
   */
  public void clearFeatureCounts()
  {
    for (int i = 0; i < effect_sets.size(); i++)
      feature_counts[i].clear();

    for (int es = 0; es < feature_counts.length; es++)
      feature_sums[es] = 0;
  }

  /**
   * Adds <code>count</code> of {@link Feature} <code>f</code> to {@link EffectSet} <code>e</code>.
   * 
   * @param e
   *          {@link EffectSet} we are talking about.
   * @param f
   *          {@link Feature} we are interested to add.
   * @param count
   *          value to add (or subtract if negative).
   */
  public void incrementFeatureCount(EffectSet e, Feature f, double count)
  {
    feature_counts[effect_sets.getIndex(e)].increment(features.getIndex(f), count);
  }

  /**
   * See {@link #incrementFeatureCount(EffectSet, Feature, double)}.
   * 
   * @param es_index
   *          {@link EffectSet} (by index) we are talking about.
   * @param f
   *          {@link Feature} we are interested to add.
   * @param count
   *          value to add (or subtract if negative).
   */
  public void incrementFeatureCount(int es_index, Feature f, double count)
  {
    feature_counts[es_index].increment(features.getIndex(f), count);
  }

  /**
   * See {@link #incrementFeatureCount(EffectSet, Feature, double)}.
   * 
   * @param es_index
   *          {@link EffectSet} (by index) we are talking about.
   * @param feature_index
   *          {@link Feature} (by index) we are interested to add.
   * @param count
   *          value to add (or subtract if negative).
   */
  public void incrementFeatureCount(int es_index, int feature_index, double count)
  {
    feature_counts[es_index].increment(feature_index, count);
  }

  /**
   * See {@link #incrementFeatureCount(EffectSet, Feature, double)}.
   * 
   * @param e
   *          {@link EffectSet} we are talking about.
   * @param feature_index
   *          {@link Feature} (by index) we are interested to add.
   * @param count
   *          value to add (or subtract if negative).
   */
  public void incrementFeatureCount(EffectSet e, int feature_index, double count)
  {
    feature_counts[effect_sets.getIndex(e)].increment(feature_index, count);
  }

  /**
   * Increments feature counts by an entire vector.
   * 
   * @param e
   *          {@link EffectSet} we are talking about.
   * @param fv
   *          {@link FeatureVector} containing the increments (or decrements if negative) that we want to perform.
   */
  public void incrementFeatureVector(EffectSet e, FeatureVector fv)
  {
    int e_index = effect_sets.getIndex(e);
    for (Entry<Feature, Double> wc : fv.entrySet())
      feature_counts[e_index].increment(features.getIndex(wc.getKey()), wc.getValue());
  }

  /**
   * Sets the eta vector for a particular {@link Effect}. This is usually only done for background effects.
   * 
   * @param eff
   *          {@link Effect} of eta vector to update.
   * @param values
   *          Array of values (with same dimension as eta) to set eta to.
   */
  public void setEta(Effect eff, double[] values)
  {
    int eff_index = effects.getIndex(eff);

    for (int i = 0; i < etas[eff_index].length; i++)
      etas[eff_index][i] = values[i];
  }

  /**
   * Get the eta vector associated with {@link Effect} <code>eff</code>.
   * 
   * @param eff
   *          Associated eta vector to get.
   * @return the eta vector associated with {@link Effect} <code>eff</code>.
   */
  public double[] getEta(Effect eff)
  {
    return etas[effects.getIndex(eff)];
  }

  /**
   * Set the feature count for a specific {@link EffectSet} and {@link Feature}.
   * 
   * @param e
   *          {@link EffectSet} to set feature count.
   * @param f
   *          {@link Feature} to set feature count.
   * @param count
   *          Value to set to.
   */
  public void setFeatureCount(EffectSet e, Feature f, double count)
  {
    feature_counts[effect_sets.getIndex(e)].put(features.getIndex(f), count);
  }

  /**
   * Set the feature vector for a specific {@link EffectSet}.
   * 
   * @param e
   *          {@link EffectSet} to set feature count.
   * @param fv
   *          {@link FeatureVector} containing new counts.
   */
  public void setFeatureVector(EffectSet e, FeatureVector fv)
  {
    int e_index = effect_sets.getIndex(e);
    for (Entry<Feature, Double> wc : fv.entrySet())
      feature_counts[e_index].put(features.getIndex(wc.getKey()), wc.getValue());
  }

  /**
   * Set the maximum number of iterations that {@link OWLQN} will perform.
   * 
   * @param maxOWLQNIters
   *          Maximum number of iterations to perform. Defaults to 10.
   */
  public final void setMaximumOWLQNIterations(int maxOWLQNIters)
  {
    this.owl_iters = maxOWLQNIters;
  }

  /**
   * Outputs {@link SAGE} eta vectors to {@link PrintWriter} <code>ps</code>. Output format is an {@link Effect} vector per line of the form <code>effect&lt;TAB&gt;word1:eta1 word2:eta2 ... wordN:etaN</code>.
   * 
   * @param pw
   *          {@link PrintWriter} to save eta vector information to.
   * @param saveFixedEffects
   *          Set <code>true</code> to include fixed effects vectors.
   */
  public void print(PrintWriter pw, boolean saveFixedEffects)
  {
    for (int i = 0; i < effects.size(); i++)
    {
      if (!saveFixedEffects && effects.get(i).isFixed())
        continue;

      pw.print(effects.get(i).getDescription());
      pw.print('\t');
      List<String> feat_weight = new ArrayList<>();
      for (int j = 0; j < features.size(); j++)
      {
        if (etas[i][j] == 0)
          continue;
        feat_weight.add(String.format("%s:%f", features.get(j).toString(), etas[i][j]));
      }
      Collections.sort(feat_weight, new Comparator<String>()
      {
        @Override
        public int compare(String s1, String s2)
        {
          // double score_1 = Math.abs(Double.parseDouble(s1.substring(s1.indexOf(':') + 1)));
          // double score_2 = Math.abs(Double.parseDouble(s2.substring(s2.indexOf(':') + 1)));
          double score_1 = (Double.parseDouble(s1.substring(s1.indexOf(':') + 1)));
          double score_2 = (Double.parseDouble(s2.substring(s2.indexOf(':') + 1)));

          if (score_2 > score_1)
            return 1;
          if (score_2 < score_1)
            return -1;

          return 0;
        }
      });

      for (String s : feat_weight)
        pw.format("%s ", s);
      pw.println();
    }
  }

  /**
   * Outputs the normalization constants for each {@link EffectSet}. Output format is an {@link EffectSet} per line of the form <code>effect1 effect2 ... effectN&lt;TAB&gt;normalization constant</code>
   * 
   * @param pw
   *          {@link PrintWriter} to save Z information to.
   */
  public void printLogZ(PrintWriter pw)
  {
    for (EffectSet es : effect_sets)
    {
      int i = 0;
      for (Effect e : es)
      {
        if (i++ > 0)
          pw.print(' ');
        pw.print(e.getDescription());
      }

      double beta = 0;
      for (Effect e : es)
        beta += etas[effects.getIndex(e)][0];
      double log_Z = beta;

      for (int j = 1; j < features.size(); j++)
      {
        beta = 0;
        for (Effect e : es)
          beta += etas[effects.getIndex(e)][j];
        log_Z = MathMethods.logSum(log_Z, beta);
      }

      pw.print('\t');
      pw.println(log_Z);
    }
  }

  /**
   * Output {@link SAGE} eta vectors to {@link System#out}. Slightly more descriptive {@link Effect} names than the output to file.
   * 
   * @see #print(PrintWriter, boolean)
   */
  public void print()
  {
    System.out.println("Etas[Effects][Features]");
    for (int i = 0; i < effects.size(); i++)
    {
      System.out.print(effects.get(i) + " ");
      List<String> feat_weight = new ArrayList<>();
      for (int j = 0; j < features.size(); j++)
        feat_weight.add(String.format("%s:%f", features.get(j).toString(), etas[i][j]));
      Collections.sort(feat_weight, new Comparator<String>()
      {
        @Override
        public int compare(String s1, String s2)
        {
          // double score_1 = Math.abs(Double.parseDouble(s1.substring(s1.indexOf(':') + 1)));
          // double score_2 = Math.abs(Double.parseDouble(s2.substring(s2.indexOf(':') + 1)));
          double score_1 = (Double.parseDouble(s1.substring(s1.indexOf(':') + 1)));
          double score_2 = (Double.parseDouble(s2.substring(s2.indexOf(':') + 1)));

          if (score_2 > score_1)
            return 1;
          if (score_2 < score_1)
            return -1;

          return 0;
        }
      });

      for (String s : feat_weight)
        System.out.format("%s ", s);
      System.out.println();
    }
  }

  /**
   * This private class handles the mathematical details of optimizing eta.
   * 
   * @author Yanchuan Sim
   * @version 0.1
   */
  private class MaximizeEtaFunction implements DiffFunction
  {
    private final int effect_index;

    private double[] cache_grad;
    private double[] cache_grad_x;
    private double[] cache_value_x;
    private double cache_value;
    private final double l2regularizer;

    public MaximizeEtaFunction(int effect_index, double l2regularizer)
    {
      this.effect_index = effect_index;

      cache_grad = null;
      cache_grad_x = null;
      cache_value_x = null;
      cache_value = 0;
      this.l2regularizer = l2regularizer;
    }

    /*
     * (non-Javadoc)
     * @see edu.stanford.nlp.optimization.Function#valueAt(double[])
     */
    @Override
    public double valueAt(double[] x)
    {
      if (cache_value_x != null && cache_value_x.equals(x))
        return cache_value;

      double value = 0;

      for (TIntIterator it_es = effects_lookup[effect_index].iterator(); it_es.hasNext();)
      {
        int es_index = it_es.next();

        double Z = 0.0;
        double eta_sum = 0.0;
        for (int e : es_effect_indexes[es_index])
          eta_sum += (e == effect_index ? x[0] : etas[e][0]);
        Z = eta_sum;

        for (int i = 1; i < dimensions; i++)
        {
          eta_sum = 0.0;
          for (int e : es_effect_indexes[es_index])
            eta_sum += (e == effect_index ? x[i] : etas[e][i]);

          Z = MathMethods.logSum(Z, eta_sum);
        }

        for (TIntDoubleIterator it_feat = feature_counts[es_index].iterator(); it_feat.hasNext();)
        {
          it_feat.advance();
          value += it_feat.value() * x[it_feat.key()];
        }
        value -= feature_sums[es_index] * Z;
      }

      for (double element : x)
        value -= 0.5 * l2regularizer * element * element;

      cache_value = -value;
      cache_value_x = x.clone();

      return -value;
    }

    /*
     * (non-Javadoc)
     * @see edu.stanford.nlp.optimization.Function#domainDimension()
     */
    @Override
    public int domainDimension()
    {
      return dimensions;
    }

    /*
     * (non-Javadoc)
     * @see edu.stanford.nlp.optimization.DiffFunction#derivativeAt(double[])
     */
    @Override
    public double[] derivativeAt(double[] x)
    {
      if (cache_grad_x != null && cache_grad_x.equals(x))
        return cache_grad;

      double[] grad = new double[dimensions];

      for (TIntIterator it_es = effects_lookup[effect_index].iterator(); it_es.hasNext();)
      {
        int es_index = it_es.next();

        double Z = 0.0;
        double[] beta = new double[dimensions];

        for (int e : es_effect_indexes[es_index])
          beta[0] += (effect_index == e ? x[0] : etas[e][0]);
        Z = beta[0];

        for (int i = 1; i < dimensions; i++)
        {
          for (int e : es_effect_indexes[es_index])
            beta[i] += (effect_index == e ? x[i] : etas[e][i]);

          Z = MathMethods.logSum(Z, beta[i]);
        }

        for (TIntDoubleIterator it_feat = feature_counts[es_index].iterator(); it_feat.hasNext();)
        {
          it_feat.advance();
          double p_w = FastMath.exp(beta[it_feat.key()] - Z);
          grad[it_feat.key()] += -(it_feat.value() - (feature_sums[es_index] * p_w));
        }
      }

      for (int i = 0; i < x.length; i++)
        grad[i] += l2regularizer * x[i];

      cache_grad = grad.clone();
      cache_grad_x = x.clone();

      return grad;
    }
  }
}

