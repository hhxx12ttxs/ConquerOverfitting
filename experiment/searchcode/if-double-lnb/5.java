* evaluation.
*/
package lv.ailab.lnb.fraktur.ngram;

import lv.ailab.lnb.fraktur.translit.Variant;
import java.util.Comparator;
double ev1 = t1.estimate(evaluator);
double ev2 = t2.estimate(evaluator);
if (ev1 < ev2) return 1;
else if (ev1 == ev2) return 0;
else return -1;
}

}

