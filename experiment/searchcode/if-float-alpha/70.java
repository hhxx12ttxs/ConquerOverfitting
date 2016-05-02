/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snips.pml.svm;

// An SMO algorithm in Fan et al., JMLR 6(2005), p. 1889--1918

import net.snips.pml.Constants;
import net.snips.pml.Logger;

// Solves:
//
//	min 0.5(\alpha^T Q \alpha) + p^T \alpha
//
//		y^T \alpha = \delta
//		y_i = +1 or -1
//		0 <= alpha_i <= Cp for y_i = 1
//		0 <= alpha_i <= Cn for y_i = -1
//
// Given:
//
//	Q, p, y, Cp, Cn, and an initial feasible point \alpha
//	l is the size of vectors and matrices
//	eps is the stopping tolerance
//
// solution will be put in \alpha, objective value will be put in obj
//
public class Solver {

    protected int active_size;
    protected byte[] y;
    protected float[] G;		// gradient of objective function
    protected static final byte LOWER_BOUND = 0;
    protected static final byte UPPER_BOUND = 1;
    protected static final byte FREE = 2;
    protected byte[] alpha_status;	// LOWER_BOUND, UPPER_BOUND, FREE
    protected float[] alpha;
    protected QMatrix Q;
    protected float[] QD;
    protected float eps;
    protected float Cp, Cn;
    protected float[] p;
    protected int[] active_set;
    protected float[] G_bar;		// gradient, if we treat free variables as 0
    protected int l;
    protected boolean unshrink;	// XXX


    protected float getC(int i) {
        return (y[i] > 0) ? Cp : Cn;
    }

    protected void updateAlphaStatus(int i) {
        if (alpha[i] >= getC(i)) {
            alpha_status[i] = UPPER_BOUND;
        } else if (alpha[i] <= 0) {
            alpha_status[i] = LOWER_BOUND;
        } else {
            alpha_status[i] = FREE;
        }
    }

    protected boolean isUpperBound(int i) {
        return alpha_status[i] == UPPER_BOUND;
    }

    protected boolean isLowerBound(int i) {
        return alpha_status[i] == LOWER_BOUND;
    }

    protected boolean isFree(int i) {
        return alpha_status[i] == FREE;
    }

    

    protected void reconstructGradient() throws Exception {
        // reconstruct inactive elements of G from G_bar and free variables

        if (active_size == l) {
            return;
        }

        int i, j;
        int nr_free = 0;

        for (j = active_size; j < l; j++) {
            G[j] = G_bar[j] + p[j];
        }

        for (j = 0; j < active_size; j++) {
            if (isFree(j)) {
                nr_free++;
            }
        }

        if (nr_free * l > 2 * active_size * (l - active_size)) {
            for (i = active_size; i < l; i++) {
                float[] Q_i = Q.getQ(i, active_size);
                for (j = 0; j < active_size; j++) {
                    if (isFree(j)) {
                        G[i] += alpha[j] * Q_i[j];
                    }
                }
            }
        } else {
            for (i = 0; i < active_size; i++) {
                if (isFree(i)) {
                    float[] Q_i = Q.getQ(i, l);
                    float alpha_i = alpha[i];
                    for (j = active_size; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                }
            }
        }
    }

    protected void solve(int l, QMatrix Q, float[] p_, byte[] y_, float[] alpha_, float Cp, float Cn, float eps, SolutionInfo si) throws Exception {

        Logger.log(4, "Solving Solver");

        this.l = l;
        this.Q = Q;
        QD = Q.getQD();
        p = (float[]) p_.clone();
        y = (byte[]) y_.clone();
        alpha = (float[]) alpha_.clone();
        this.Cp = Cp;
        this.Cn = Cn;
        this.eps = eps;
        this.unshrink = false;

        Logger.log(4, "Initialize alpha_status");

        {
            alpha_status = new byte[l];
            for (int i = 0; i < l; i++) {
                updateAlphaStatus(i);
            }
        }

        Logger.log(4, "Initialize active set (for shrinking)");
        {
            active_set = new int[l];
            for (int i = 0; i < l; i++) {
                active_set[i] = i;
            }
            active_size = l;
        }

        Logger.log(4, "Initialize gradient");
        {
            G = new float[l];
            G_bar = new float[l];
            int i;
            for (i = 0; i < l; i++) {
                G[i] = p[i];
                G_bar[i] = 0;
            }
            for (i = 0; i < l; i++) {
                if (!isLowerBound(i)) {
                    float[] Q_i = Q.getQ(i, l);
                    float alpha_i = alpha[i];
                    int j;
                    for (j = 0; j < l; j++) {
                        G[j] += alpha_i * Q_i[j];
                    }
                    if (isUpperBound(i)) {
                        for (j = 0; j < l; j++) {
                            G_bar[j] += getC(i) * Q_i[j];
                        }
                    }
                }
            }
        }

        Logger.log(4, "Optimization step");

        int iter = 0;
        int counter = Math.min(l, 1000) + 1;
        int[] working_set = new int[2];

        while (true) {

            // show progress and do shrinking

            if(iter > Constants.SvmMaxIterations){
                break;
            }

            if (--counter == 0) {
                counter = Math.min(l, 1000);
            }

            if (select_working_set(working_set) != 0) {
                // reconstruct the whole gradient
                reconstructGradient();
                // reset active set size and check
                active_size = l;
                if (select_working_set(working_set) != 0) {
                    break;
                }
                else {
                    counter = 1;	// do shrinking next iteration
                }
            }

            int i = working_set[0];
            int j = working_set[1];

            ++iter;

            // update alpha[i] and alpha[j], handle bounds carefully

            float[] Q_i = Q.getQ(i, active_size);
            float[] Q_j = Q.getQ(j, active_size);

            float C_i = getC(i);
            float C_j = getC(j);

            float old_alpha_i = alpha[i];
            float old_alpha_j = alpha[j];

            if (y[i] != y[j]) {
                float quad_coef = QD[i] + QD[j] + 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12f;
                }
                float delta = (-G[i] - G[j]) / quad_coef;
                float diff = alpha[i] - alpha[j];
                alpha[i] += delta;
                alpha[j] += delta;

                if (diff > 0) {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = diff;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = -diff;
                    }
                }
                if (diff > C_i - C_j) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = C_i - diff;
                    }
                } else {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = C_j + diff;
                    }
                }
            } else {
                float quad_coef = QD[i] + QD[j] - 2 * Q_i[j];
                if (quad_coef <= 0) {
                    quad_coef = 1e-12f;
                }
                float delta = (G[i] - G[j]) / quad_coef;
                float sum = alpha[i] + alpha[j];
                alpha[i] -= delta;
                alpha[j] += delta;

                if (sum > C_i) {
                    if (alpha[i] > C_i) {
                        alpha[i] = C_i;
                        alpha[j] = sum - C_i;
                    }
                } else {
                    if (alpha[j] < 0) {
                        alpha[j] = 0;
                        alpha[i] = sum;
                    }
                }
                if (sum > C_j) {
                    if (alpha[j] > C_j) {
                        alpha[j] = C_j;
                        alpha[i] = sum - C_j;
                    }
                } else {
                    if (alpha[i] < 0) {
                        alpha[i] = 0;
                        alpha[j] = sum;
                    }
                }
            }

            // update G

            float delta_alpha_i = alpha[i] - old_alpha_i;
            float delta_alpha_j = alpha[j] - old_alpha_j;

            for (int k = 0; k < active_size; k++) {
                G[k] += Q_i[k] * delta_alpha_i + Q_j[k] * delta_alpha_j;
            }

            // update alpha_status and G_bar

            {
                boolean ui = isUpperBound(i);
                boolean uj = isUpperBound(j);
                updateAlphaStatus(i);
                updateAlphaStatus(j);
                int k;
                if (ui != isUpperBound(i)) {
                    Q_i = Q.getQ(i, l);
                    if (ui) {
                        for (k = 0; k < l; k++) {
                            G_bar[k] -= C_i * Q_i[k];
                        }
                    } else {
                        for (k = 0; k < l; k++) {
                            G_bar[k] += C_i * Q_i[k];
                        }
                    }
                }

                if (uj != isUpperBound(j)) {
                    Q_j = Q.getQ(j, l);
                    if (uj) {
                        for (k = 0; k < l; k++) {
                            G_bar[k] -= C_j * Q_j[k];
                        }
                    } else {
                        for (k = 0; k < l; k++) {
                            G_bar[k] += C_j * Q_j[k];
                        }
                    }
                }
            }

        }

        Logger.log(4, "Calculate rho");

        si.rho = calculate_rho(si);

        Logger.log(4, "Calculate objective value");

        float v = 0;
        
        for (int i = 0; i < l; i++) {
            v += alpha[i] * (G[i] + p[i]);
        }

        si.obj = v / 2;

        Logger.log(4, "Put back the solution");

        for (int i = 0; i < l; i++) {
            alpha_[active_set[i]] = alpha[i];
        }

        si.upper_bound_p = Cp;
        si.upper_bound_n = Cn;
    }

    // return 1 if already optimal, return 0 otherwise
    protected int select_working_set(int[] working_set) throws Exception {
        // return i,j such that
        // i: maximizes -y_i * grad(f)_i, i in I_up(\alpha)
        // j: mimimizes the decrease of obj value
        //    (if quadratic coefficeint <= 0, replace it with tau)
        //    -y_j*grad(f)_j < -y_i*grad(f)_i, j in I_low(\alpha)

        float Gmax = -Float.POSITIVE_INFINITY;
        float Gmax2 = -Float.POSITIVE_INFINITY;
        int Gmax_idx = -1;
        int Gmin_idx = -1;
        float obj_diff_min = Float.POSITIVE_INFINITY;

        for (int t = 0; t < active_size; t++) {
            if (y[t] == +1) {
                if (!isUpperBound(t)) {
                    if (-G[t] >= Gmax) {
                        Gmax = -G[t];
                        Gmax_idx = t;
                    }
                }
            } else {
                if (!isLowerBound(t)) {
                    if (G[t] >= Gmax) {
                        Gmax = G[t];
                        Gmax_idx = t;
                    }
                }
            }
        }

        int i = Gmax_idx;
        float[] Q_i = null;
        if (i != -1) // null Q_i not accessed: Gmax=-INF if i=-1
        {
            Q_i = Q.getQ(i, active_size);
        }

        for (int j = 0; j < active_size; j++) {
            if (y[j] == +1) {
                if (!isLowerBound(j)) {
                    float grad_diff = Gmax + G[j];
                    if (G[j] >= Gmax2) {
                        Gmax2 = G[j];
                    }
                    if (grad_diff > 0) {
                        float obj_diff;
                        float quad_coef = QD[i] + QD[j] - 2.0f * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12f;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            } else {
                if (!isUpperBound(j)) {
                    float grad_diff = Gmax - G[j];
                    if (-G[j] >= Gmax2) {
                        Gmax2 = -G[j];
                    }
                    if (grad_diff > 0) {
                        float obj_diff;
                        float quad_coef = QD[i] + QD[j] + 2.0f * y[i] * Q_i[j];
                        if (quad_coef > 0) {
                            obj_diff = -(grad_diff * grad_diff) / quad_coef;
                        } else {
                            obj_diff = -(grad_diff * grad_diff) / 1e-12f;
                        }

                        if (obj_diff <= obj_diff_min) {
                            Gmin_idx = j;
                            obj_diff_min = obj_diff;
                        }
                    }
                }
            }
        }

        if (Gmax + Gmax2 < eps) {
            return 1;
        }

        working_set[0] = Gmax_idx;
        working_set[1] = Gmin_idx;
        return 0;
    }

    protected float calculate_rho(SolutionInfo si) {
        float r;
        int nr_free = 0;
        float ub = Float.POSITIVE_INFINITY;
        float lb = -Float.POSITIVE_INFINITY;
        float sum_free = 0;
        
        for (int i = 0; i < active_size; i++) {
            float yG = y[i] * G[i];

            if (isLowerBound(i)) {
                if (y[i] > 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else if (isUpperBound(i)) {
                if (y[i] < 0) {
                    ub = Math.min(ub, yG);
                } else {
                    lb = Math.max(lb, yG);
                }
            } else {
                ++nr_free;
                sum_free += yG;
            }
        }

        if (nr_free > 0) {
            r = sum_free / nr_free;
        } else {
            r = (ub + lb) / 2;
        }

        return r;
    }
}

