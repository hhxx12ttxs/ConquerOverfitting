package cn.edu.pku.sei.plde.conqueroverfitting.localization.metric;

/**
 * Created by spirals on 24/07/15.
 */
public class Naish2 implements Metric {

    public double value(int ef, int ep, int nf, int np) {
        // ef - (ep / float(ep + np + 1))
        return ef - (ep / ((double) (ep + np + 1)));
    }
}
