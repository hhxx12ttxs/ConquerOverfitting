package net.moraleboost.junsai.learner.lbfgs;

import net.moraleboost.junsai.learner.lbfgs.LBFGS.SharedParam;

public class LineSearch
{
    private static final double FTOL = 1e-4;
    private static final double XTOL = 1e-16;
    private static final double LB3_1_GTOL = 0.9;
    private static final double LB3_1_STPMIN = 1e-20;
    private static final double LB3_1_STPMAX = 1e20;
    private static final double P5 = 0.5;
    private static final double P66 = 0.66;
    private static final double XTRAPF = 4.0;
    private static final int MAXFEV = 20;

    private int info;
    private int infoc;
    private boolean stage1;
    private boolean brackt;
    private double finit;
    private double dginit;
    private double dgtest;
    private double width;
    private double width1;
    private double stx;
    private double fx;
    private double dgx;
    private double sty;
    private double fy;
    private double dgy;
    private double stmin;
    private double stmax;

    public LineSearch()
    {
        infoc = 0;
        stage1 = false;
        brackt = false;
        finit = 0.0;
        dginit = 0.0;
        dgtest = 0.0;
        width = 0.0;
        width1 = 0.0;
        stx = 0.0;
        fx = 0.0;
        dgx = 0.0;
        sty = 0.0;
        fy = 0.0;
        dgy = 0.0;
        stmin = 0.0;
        stmax = 0.0;
    }
    
    public void setInfo(int info)
    {
        this.info = info;
    }

    public int search(double[] x, double f, double[] g,
            double[] s, int soff, double[] wa,
            SharedParam sharedParam,
            boolean orthant, double C)
    {
        int size = x.length;
        boolean restart = false;

        if (info == -1) {
            // restart
            restart = true;
        } else {
            // fresh start
            infoc = 1;

            if (size <= 0 || sharedParam.stp <= 0.0) {
                return info;
            }

            dginit = LbfgsMath.ddot(g, 0, s, soff, size);
            if (dginit >= 0.0) {
                return info;
            }

            brackt = false;
            stage1 = true;
            sharedParam.nfev = 0;
            finit = f;
            dgtest = FTOL * dginit;
            width = LB3_1_STPMAX - LB3_1_STPMIN;
            width1 = width / P5;
            for (int j = 0; j < size; ++j) {
                wa[j] = x[j];
            }

            stx = 0.0;
            fx = finit;
            dgx = dginit;
            sty = 0.0;
            fy = finit;
            dgy = dginit;
        }

        while (true) {
            if (restart) {
                // ????????-1??????????
                restart = false;
            } else {
                if (brackt) {
                    stmin = Math.min(stx, sty);
                    stmax = Math.max(stx, sty);
                } else {
                    stmin = stx;
                    stmax = sharedParam.stp + XTRAPF * (sharedParam.stp - stx);
                }

                sharedParam.stp = Math.max(sharedParam.stp, LB3_1_STPMIN);
                sharedParam.stp = Math.min(sharedParam.stp, LB3_1_STPMAX);

                if ((brackt && ((sharedParam.stp <= stmin || sharedParam.stp >= stmax)
                        || sharedParam.nfev >= MAXFEV - 1 || infoc == 0))
                        || (brackt && (stmax - stmin <= XTOL * stmax))) {
                    sharedParam.stp = stx;
                }

                if (orthant) {
                    for (int j = 0; j < size; ++j) {
                        double grad_neg = 0.0;
                        double grad_pos = 0.0;
                        double grad = 0.0;
                        if (wa[j] == 0.0) {
                            grad_neg = g[j] - 1.0 / C;
                            grad_pos = g[j] + 1.0 / C;
                        } else {
                            grad_pos = grad_neg = g[j] + 1.0
                                    * Math.signum(wa[j]) / C;
                        }
                        if (grad_neg > 0.0) {
                            grad = grad_neg;
                        } else if (grad_pos < 0.0) {
                            grad = grad_pos;
                        } else {
                            grad = 0.0;
                        }
                        double p = LbfgsMath.pi(s[soff + j], -grad);
                        double xi = wa[j] == 0.0 ? Math.signum(-grad)
                                : Math.signum(wa[j]);
                        x[j] = LbfgsMath.pi(wa[j] + sharedParam.stp * p, xi);
                    }
                } else {
                    for (int j = 0; j < size; ++j) {
                        x[j] = wa[j] + sharedParam.stp * s[soff + j];
                    }
                }
                info = -1;
                return info;
            }

            // L45
            // ?? -1??????????????
            info = 0;
            ++sharedParam.nfev;
            double dg = LbfgsMath.ddot(g, 0, s, soff, size);
            double ftest1 = finit + sharedParam.stp * dgtest;

            if (brackt && ((sharedParam.stp <= stmin || sharedParam.stp >= stmax) || infoc == 0)) {
                info = 6;
            }
            if (sharedParam.stp == LB3_1_STPMAX && f <= ftest1 && dg <= dgtest) {
                info = 5;
            }
            if (sharedParam.stp == LB3_1_STPMIN && (f > ftest1 || dg >= dgtest)) {
                info = 4;
            }
            if (sharedParam.nfev >= MAXFEV) {
                info = 3;
            }
            if (brackt && stmax - stmin <= XTOL * stmax) {
                info = 2;
            }
            if (f <= ftest1 && Math.abs(dg) <= LB3_1_GTOL * (-dginit)) {
                info = 1;
            }

            if (info != 0) {
                return info;
            }

            if (stage1 && f <= ftest1
                    && dg >= Math.min(FTOL, LB3_1_GTOL) * dginit) {
                stage1 = false;
            }

            if (stage1 && f <= fx && f > ftest1) {
                double fm = f - sharedParam.stp * dgtest;
                double dgm = dg - dgtest;
                fx = fx - stx * dgtest;
                fy = fy - sty * dgtest;
                dgx = dgx - dgtest;
                dgy = dgy - dgtest;
                mcstep(sharedParam, fm, dgm); // modifies fx, fy dgx, dgy
                fx = fx + stx * dgtest;
                fy = fy + sty * dgtest;
                dgx = dgx + dgtest;
                dgy = dgy + dgtest;
            } else {
                mcstep(sharedParam, f, dg);
            }

            if (brackt) {
                double d1 = sty - stx;
                if (Math.abs(d1) >= P66 * width1) {
                    sharedParam.stp = stx + P5 * d1;
                }
                width1 = width;
                width = Math.abs(d1);
            }
        }
    }

    private void mcstep(SharedParam sharedParam, double fp, double dp)
    {
        boolean bound = true;
        double p, q, s, d1, d2, d3, r, gamma, theta, stpq, stpc, stpf;
        infoc = 0;

        if (brackt
                && ((sharedParam.stp <= Math.min(stx, sty) || sharedParam.stp >= Math.max(stx, sty))
                        || dgx * (sharedParam.stp - stx) >= 0.0 || stmax < stmin)) {
            return;
        }

        double sgnd = dp * Math.signum(dgx);

        if (fp > fx) {
            infoc = 1;
            bound = true;
            theta = (fx - fp) * 3 / (sharedParam.stp - stx) + dgx + dp;
            d1 = Math.abs(theta);
            d2 = Math.abs(dgx);
            d1 = Math.max(d1, d2);
            d2 = Math.abs(dp);
            s = Math.max(d1, d2);
            d1 = theta / s;
            gamma = s * Math.sqrt(d1 * d1 - dgx / s * (dp / s));
            if (sharedParam.stp < stx) {
                gamma = -gamma;
            }
            p = gamma - dgx + theta;
            q = gamma - dgx + gamma + dp;
            r = p / q;
            stpc = stx + r * (sharedParam.stp - stx);
            stpq = stx + dgx / ((fx - fp) / (sharedParam.stp - stx) + dgx) / 2
                    * (sharedParam.stp - stx);
            d1 = stpc - stx;
            d2 = stpq - stx;
            if (Math.abs(d1) < Math.abs(d2)) {
                stpf = stpc;
            } else {
                stpf = stpc + (stpq - stpc) / 2;
            }
            brackt = true;
        } else if (sgnd < 0.0) {
            infoc = 2;
            bound = false;
            theta = (fx - fp) * 3 / (sharedParam.stp - stx) + dgx + dp;
            d1 = Math.abs(theta);
            d2 = Math.abs(dgx);
            d1 = Math.max(d1, d2);
            d2 = Math.abs(dp);
            s = Math.max(d1, d2);
            d1 = theta / s;
            gamma = s * Math.sqrt(d1 * d1 - dgx / s * (dp / s));
            if (sharedParam.stp > stx) {
                gamma = -gamma;
            }
            p = gamma - dp + theta;
            q = gamma - dp + gamma + dgx;
            r = p / q;
            stpc = sharedParam.stp + r * (stx - sharedParam.stp);
            stpq = sharedParam.stp + dp / (dp - dgx) * (stx - sharedParam.stp);
            d1 = stpc - sharedParam.stp;
            d2 = stpq - sharedParam.stp;
            if (Math.abs(d1) > Math.abs(d2)) {
                stpf = stpc;
            } else {
                stpf = stpq;
            }
            brackt = true;
        } else if (Math.abs(dp) < Math.abs(dgx)) {
            infoc = 3;
            bound = true;
            theta = (fx - fp) * 3 / (sharedParam.stp - stx) + dgx + dp;
            d1 = Math.abs(theta);
            d2 = Math.abs(dgx);
            d1 = Math.max(d1, d2);
            d2 = Math.abs(dp);
            s = Math.max(d1, d2);
            d3 = theta / s;
            d1 = 0.0;
            d2 = d3 * d3 - dgx / s * (dp / s);
            gamma = s * Math.sqrt(Math.max(d1, d2));
            if (sharedParam.stp > stx) {
                gamma = -gamma;
            }
            p = gamma - dp + theta;
            q = gamma + (dgx - dp) + gamma;
            r = p / q;
            if (r < 0.0 && gamma != 0.0) {
                stpc = sharedParam.stp + r * (stx - sharedParam.stp);
            } else if (sharedParam.stp > stx) {
                stpc = stmax;
            } else {
                stpc = stmin;
            }
            stpq = sharedParam.stp + dp / (dp - dgx) * (stx - sharedParam.stp);
            if (brackt) {
                d1 = sharedParam.stp - stpc;
                d2 = sharedParam.stp - stpq;
                if (Math.abs(d1) < Math.abs(d2)) {
                    stpf = stpc;
                } else {
                    stpf = stpq;
                }
            } else {
                d1 = sharedParam.stp - stpc;
                d2 = sharedParam.stp - stpq;
                if (Math.abs(d1) > Math.abs(d2)) {
                    stpf = stpc;
                } else {
                    stpf = stpq;
                }
            }
        } else {
            infoc = 4;
            bound = false;
            if (brackt) {
                theta = (fp - fy) * 3 / (sty - sharedParam.stp) + dgy + dp;
                d1 = Math.abs(theta);
                d2 = Math.abs(dgy);
                d1 = Math.max(d1, d2);
                d2 = Math.abs(dp);
                s = Math.max(d1, d2);
                d1 = theta / s;
                gamma = s * Math.sqrt(d1 * d1 - dgy / s * (dp / s));
                if (sharedParam.stp > sty) {
                    gamma = -gamma;
                }
                p = gamma - dp + theta;
                q = gamma - dp + gamma + dgy;
                r = p / q;
                stpc = sharedParam.stp + r * (sty - sharedParam.stp);
                stpf = stpc;
            } else if (sharedParam.stp > stx) {
                stpf = stmax;
            } else {
                stpf = stmin;
            }
        }

        if (fp > fx) {
            sty = sharedParam.stp;
            fy = fp;
            dgy = dp;
        } else {
            if (sgnd < 0.0) {
                sty = stx;
                fy = fx;
                dgy = dgx;
            }
            stx = sharedParam.stp;
            fx = fp;
            dgx = dp;
        }

        stpf = Math.min(stmax, stpf);
        stpf = Math.max(stmin, stpf);
        sharedParam.stp = stpf;
        if (brackt && bound) {
            if (sty > stx) {
                d1 = stx + (sty - stx) * P66;
                sharedParam.stp = Math.min(d1, sharedParam.stp);
            } else {
                d1 = stx + (sty - stx) * P66;
                sharedParam.stp = Math.max(d1, sharedParam.stp);
            }
        }

        return;
    }
}

