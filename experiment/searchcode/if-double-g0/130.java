package net.moraleboost.junsai.learner.lbfgs;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class LBFGS
{
    protected static class SharedParam
    {
        public int nfev;
        public double stp1;
        public double stp;
        
        public void clear()
        {
            nfev = 0;
            stp = 0.0;
            stp1 = 0.0;
        }
    }
    
    // ?????????
    public static final int IFLAG_INIT = 0;
    // ????
    public static final int IFLAG_TERM = 0;
    // ?????????????f?g??????????????
    public static final int IFLAG_RESTART = 1;
    // ???
    public static final int IFLAG_ERROR = -1;
    
    private static final double EPS = 1e-7;
    private static final int MSIZE = 5;
    
    private static final Log LOGGER = LogFactory.getLog(LBFGS.class);

    private int iflag; // internal state
    private int point; // history, alpha??????????
    private int npt;   // point * size?history, gradDiff??????
    private int iter;
    private SharedParam sharedParam; // LineSearch??????????
    
    private double[] diag;
    private double[] work;
    private double[] xDiff; // ??msize??x?correction?????s?
    private double[] gDiff; // ??msize??g?correction?????y?
    private double[] rho; // ??msize???
    private double[] alpha; // ??msize???
    
    private LineSearch lineSearch;

    public LBFGS()
    {
    }

    public void clear()
    {
        iflag = 0;
        point = 0;
        npt = 0;
        iter = 0;
        sharedParam = null;

        diag = null;
        work = null;
        xDiff = null;
        gDiff = null;
        rho = null;
        alpha = null;
        
        lineSearch = null;
    }

    /*
     * x: [in,out] ?????????
     * f: [in] x???????
     * g: [in,out] x?????????
     */
    public int optimize(double[] x, double f, double[] g,
            boolean orthant, double C)
    {
        if (x.length != g.length) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("len(x) != len(g)");
            }
            return -1;
        }
        
        int size = x.length;
        
        if (diag == null) {
            iflag = 0;
            sharedParam = new SharedParam();
            diag = new double[size];
            work = new double[size];
            xDiff = new double[MSIZE * size];
            gDiff = new double[MSIZE * size];
            rho = new double[MSIZE];
            alpha = new double[MSIZE];
            lineSearch = new LineSearch();
        } else if (diag.length != size) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("len(diag) != len(x)");
            }
            return -1;
        }

        optimize(MSIZE, x, f, g, orthant, C);

        if (iflag == IFLAG_ERROR) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Routine stops with unexpected error");
            }
            return -1;
        }

        if (iflag == IFLAG_TERM) {
            clear();
            return 0;
        }

        return 1;
    }
    
    private void initialize(double[] g)
    {
        point = 0;
        
        // H0
        for (int i = 0; i < g.length; ++i) {
            diag[i] = 1.0;
        }
        
        // y0
        for (int i = 0; i < g.length; ++i) {
            xDiff[i] = -g[i] * diag[i];
        }
        
        // stp1 = 1/|g0|
        sharedParam.stp1 =
            1.0 / Math.sqrt(LbfgsMath.ddot(g, 0, g, 0, g.length));
    }

    private void optimize(int msize, double[] x,
            double f, double[] g, boolean orthant, double C)
    {
        int size = x.length;
        double yy = 0.0;
        double ys = 0.0;
        int bound = 0;
        int cp = 0;
        boolean restartFromLineSearch = false;

        // ???
        if (iflag == IFLAG_INIT) {
            initialize(g);
        }
        
        if (iflag == IFLAG_RESTART) {
            restartFromLineSearch = true;
        }
        
        while (true) {
            if (restartFromLineSearch) {
                // ???????????iflag==1?????
                // ??????????????
                restartFromLineSearch = false;
            } else {
                ++iter;
                lineSearch.setInfo(0);
                
                if (iter != 1) {
                    // ????
                    if (iter > size) {
                        bound = size;
                    }
    
                    // y[k] dot s[k] = (g[k+1] - g[k]) dot (x[k+1] - x[k])
                    ys = LbfgsMath.ddot(gDiff, npt, xDiff, npt, size);
                    // y[k] dot y[k]
                    yy = LbfgsMath.ddot(gDiff, npt, gDiff, npt, size);
                    for (int i = 0; i < size; ++i) {
                        diag[i] = ys / yy;
                    }
    
                    // L100
                    cp = point;
                    if (point == 0) {
                        cp = msize;
                    }
                    rho[cp] = 1.0 / ys;
    
                    // q[bound]
                    for (int i = 0; i < size; ++i) {
                        work[i] = -g[i];
                    }
    
                    // IF ITER <= M SET BOUND=ITER
                    // ELSE SET BOUND=M
                    bound = Math.min(iter - 1, msize);
    
                    // FOR i=(BOUND-1), ..., 0
                    cp = point; // cp?????j???
                    for (int i = 0; i < bound; ++i) {
                        --cp;
                        if (cp == -1) {
                            cp = msize - 1;
                        }
                        // ?????? = ?*s*q?work?q????
                        double sq = LbfgsMath.ddot(xDiff, cp*size, work, 0, size);
                        alpha[cp] = rho[cp] * sq;
                        // q += -?*y i.e. q[i] = q[i+1] - ?[i]y[j]
                        LbfgsMath.daxpy(-alpha[cp], gDiff, cp*size, work);
                    }
    
                    // r0 = H0 * q0, ???work?r0???
                    for (int i = 0; i < size; ++i) {
                        work[i] = diag[i] * work[i];
                    }
    
                    // FOR i=0,1,...,(BOUND-1)
                    for (int i = 0; i < bound; ++i) {
                        // y * r
                        double yr = LbfgsMath.ddot(gDiff, cp*size, work, 0, size);
                        // ? = ?*y*r
                        double beta = rho[cp] * yr;
                        // ?-?
                        double diff = alpha[cp] - beta;
                        // r????r[k+1] = r[k] + (?-?)s????work = r[k+1]
                        LbfgsMath.daxpy(diff, xDiff, cp*size, work);
                        ++cp;
                        if (cp == msize) {
                            cp = 0;
                        }
                    }
    
                    // ??????????
                    // s = r
                    for (int i = 0; i < size; ++i) {
                        xDiff[point * size + i] = work[i];
                    }
                }
                
                // iter==1??????????????????
                // L165
                sharedParam.nfev = 0;
                sharedParam.stp = 1.0;
                if (iter == 1) {
                    sharedParam.stp = sharedParam.stp1;
                }
                // ???work?g[i+1]
                for (int i = 0; i < size; ++i) {
                    work[i] = g[i];
                }
            }

            // iflag == 1?????????????????
            // L172
            // ?????????
            int ret = lineSearch.search(
                    x, f, g, xDiff, point*size, diag, sharedParam, orthant, C);

            if (ret == -1) {
                iflag = IFLAG_RESTART;
                return;
            } else if (ret != 1) {
                iflag = IFLAG_ERROR;
                return;
            }

            // xDiff?gDiff???
            npt = point * size;
            for (int i = 0; i < size; ++i) {
                xDiff[npt + i] = sharedParam.stp * xDiff[npt + i];
                gDiff[npt + i] = g[i] - work[i];
            }
            ++point;
            if (point == msize) {
                point = 0;
            }

            double gnorm = Math.sqrt(LbfgsMath.ddot(g, 0, g, 0, size));
            double xnorm = Math.max(1.0,
                Math.sqrt(LbfgsMath.ddot(x, 0, x, 0, size)));
            if (gnorm / xnorm <= EPS) {
                iflag = IFLAG_TERM;
                return;
            }
        } // end while(true)
    }
}

