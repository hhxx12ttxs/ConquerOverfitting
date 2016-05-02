package edu.cuny.qc.speech.AuToBI.featureextractor;

import edu.cuny.qc.speech.AuToBI.core.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Created with IntelliJ IDEA. User: andrew Date: 7/13/12 Time: 11:21 AM To change this template use File | Settings |
 * File Templates.
 */
@SuppressWarnings("unchecked")
public class HighLowComponentFeatureExtractor extends FeatureExtractor {
  private String feature; // the name of the feature name

  public HighLowComponentFeatureExtractor(String feature) {
    this.feature = feature;
    this.required_features.add(feature);

    this.extracted_features.add(feature + "__lowGP");
    this.extracted_features.add(feature + "__highGP");
  }


  @Override
  public void extractFeatures(List regions) throws FeatureExtractorException {
    // 2 component GMM in one dimension. trained with EM.

    // Construct list to cluster.
    ArrayList<Double> data = new ArrayList<Double>();
    for (Region r : (List<Region>) regions) {
      if (r.hasAttribute(feature)) {
        Contour c = (Contour) r.getAttribute(feature);
        for (Pair<Double, Double> x : c) {
          data.add(x.second);
        }
      }
    }

    // Perform EM to fit GMM.
    GParam low = new GParam(0.0, 1.0);
    GParam high = new GParam(1.0, 1.0);
    Pair<GParam, GParam> pair = fit(low, high, data);
    for (Region r : (List<Region>) regions) {
      r.setAttribute(feature + "__lowGP", pair.first);
      r.setAttribute(feature + "__highGP", pair.second);
    }
  }

  private Pair<GParam, GParam> fit(GParam low, GParam high, ArrayList<Double> data) {
    Double EPS = 0.00001;

    Double previous_ll;
    GParam prev_low;
    GParam prev_high;
    Double ll = -Double.MAX_VALUE;
    do {
      previous_ll = ll;
      prev_low = new GParam(low.mean, low.stdev);
      prev_high = new GParam(high.mean, high.stdev);

      // calculate responsibilities
      double pi = 0.0;
      double[][] tau = new double[data.size()][2];
      for (int i = 0; i < data.size(); i++) {
        Double lowl = gaussianLikelihood(data.get(i), low.mean, low.stdev);
        Double highl = gaussianLikelihood(data.get(i), high.mean, high.stdev);
        tau[i][0] = lowl / (lowl + highl);
        tau[i][1] = highl / (lowl + highl);
        pi += tau[i][1];
      }
      pi /= data.size();

      // update params
      double low_mean = 0.0;
      double low_n = 0.0;
      double high_mean = 0.0;
      double high_n = 0.0;
      for (int i = 0; i < data.size(); i++) {
        low_mean += tau[i][0] * data.get(i);
        high_mean += tau[i][1] * data.get(i);
        low_n += tau[i][0];
        high_n += tau[i][1];
      }
      low.mean = low_mean / low_n;
      high.mean = high_mean / high_n;

      double low_stdev = 0.0;
      double high_stdev = 0.0;
      for (int i = 0; i < data.size(); i++) {
        low_stdev += tau[i][0] * (data.get(i) - low.mean) * (data.get(i) - low.mean);
        high_stdev += tau[i][1] * (data.get(i) - high.mean) * (data.get(i) - high.mean);
      }
      low.stdev = Math.sqrt(low_stdev / low_n);
      high.stdev = Math.sqrt(high_stdev / high_n);

      ll = 0.0;
      for (Double aData : data) {
        Double lowl = gaussianLikelihood(aData, low.mean, low.stdev);
        Double highl = gaussianLikelihood(aData, high.mean, high.stdev);
        ll += Math.log(highl * pi + lowl * (1 - pi));
      }
    } while (ll > previous_ll + EPS);

    return new Pair<GParam, GParam>(prev_low, prev_high);
  }

  private Double gaussianLikelihood(Double value, double mean, double stdev) {
    double pdf = 1 / (stdev * Math.sqrt(2 * Math.PI));
    pdf *= Math.pow(Math.E, (-(value - mean) * (value - mean)) / (2 * stdev * stdev));
    return pdf;
  }

}

