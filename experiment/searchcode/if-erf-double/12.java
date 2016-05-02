/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snips.pml.svm;

//

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import net.snips.pml.DataSet;
import net.snips.pml.Input;
import net.snips.pml.Parameter;
import net.snips.pml.Constants;
import net.snips.pml.Constants.IncrementFunction;
import net.snips.pml.Constants.KernelCacheType;
import net.snips.pml.Constants.SvmKernelType;
import net.snips.pml.Logger;
import net.snips.pml.caching.FileKernelCache;
import net.snips.pml.caching.KernelCache;
import net.snips.pml.caching.RamKernelCache;
import net.snips.pml.maths.MathLib;

// Kernel evaluation
//
// the static method k_function is for doing single kernel evaluation
// the constructor of Kernel prepares to calculate the l*l kernel matrix
// the member function get_Q is for getting one column from the Q Matrix
public class Kernel {

    //Synchronization lock for cache
    private final Object lock = new Object();

    //The dot product cache. We cache this because for large features spaces, it takes a while to recompute.
    //That might be problematic if we have too much inputs.. Maybe we can cache some of it up to a given amount and calculate the dotprod for the rest?
    //Or maybe we should only store non-0 dot products?
    private KernelCache dotCache = null;

    //Did we cache data?
    private boolean dataCached = false;

    //Number of elements
    private int N;

    //The math library used for calculations
    private MathLib math;

    //Matrix is symetric so indexing is done using index = N*(N-1)/2 - ((N-i)*(N-i-1)/2 - j)
    private int cacheIndex(int i, int j){

        if(dotCache == null){
            return -1;
        }

        int index = N*(N-1)/2 - ((N-i)*(N-i-1)/2 - j);

        //Only return indexes that are cached
        return index > dotCache.elements - 1 ? -1 : index;
    }

    //Use this constructor to avoid caching data
    public Kernel(MathLib math){
        this.math = math;
    }

    public Kernel(KernelCacheType cacheType, MathLib math, DataSet dataset) throws FileNotFoundException, IOException{

        this.math = math;

        N = dataset.elements;
        long max = N*(N-1)/2 + N;

        this.dotCache = cacheType == KernelCacheType.FILE ? new FileKernelCache(max, Constants.KERNELCACHEFILE) : cacheType == KernelCacheType.RAM ? new RamKernelCache(max) : null;

        if(dotCache != null){

            for(int i=0; i<dataset.elements; i++){
                for(int j=i; j<dataset.elements; j++){

                    int ii = dataset.inputs[i].id;
                    int jj = dataset.inputs[j].id;

                    if(ii > jj){
                        int t = ii;
                        ii = jj;
                        jj = t;
                    }

                    int index = cacheIndex(ii, jj);

                    //Only cache data within MAXCACHESIZE
                    if(index != -1){
                        dotCache.write(index, math.dot(dataset.inputs[i].values, dataset.inputs[j].values));
                    }
                }
            }

            dataCached = true;
        }
    }


    //Calculate the kernel value
    public final float calculate(SvmKernelType kernelType, Input xi, Input xj, HashMap<String, Parameter> parameters) {

        if(kernelType == SvmKernelType.PRECOMPUTED){
            return xi.values[(int)(xj.values[0])];
        }
        else{

            int i = xi.id;
            int j = xj.id;

            //Make sure i <= j to grab correct index!
            if(i > j){
                int t = i;
                i = j;
                j = t;
            }

            float xixj;

            int ci = cacheIndex(i,j);

            if(dataCached && ci != -1){
                xixj = dotCache.get(ci);
            }
            else{
                xixj = math.dot(xi.values, xj.values);
            }

            if(kernelType == SvmKernelType.LINEAR){
                return xixj;
            }
            else if(kernelType == SvmKernelType.POLYNOMIAL){

                return math.pow(parameters.get("GAMMA").value * xixj + parameters.get("COEF0").value, (int)parameters.get("DEGREE").value);
            }
            else if(kernelType == SvmKernelType.RBF || kernelType == SvmKernelType.SKEWEDRBF){

                //This could be speed up, but it would mess up the already messy design..
                //Bottleneck is really about calculating the dot product quickly for large feature sets

                float xi2, xj2;

                int ci2 = cacheIndex(i, i);
                int cj2 = cacheIndex(j, j);

                if(dataCached && ci2 != -1){
                    xi2 = dotCache.get(ci2);
                }
                else{
                    xi2 = math.dot(xi.values, xi.values);
                }

                if(dataCached && cj2 != -1){
                    xj2 = dotCache.get(cj2);
                }
                else{
                    xj2 = math.dot(xj.values, xj.values);
                }


                float dx = (xi2 + xj2 - 2*xixj);

                if(kernelType == SvmKernelType.RBF){
                    return math.exp(-parameters.get("GAMMA").value * dx);
                }
                //Skewed RBF calculation
                else {
                    float g = parameters.get("GAMMA").value;
                    float a = parameters.get("ALPHA").value;
                    float x = math.sqrt(dx);

                    return (float)((2*g) * rbf(x*g) * cdf(a*x*g));
                }
            }
            else if(kernelType == SvmKernelType.SIGMOID){

                return (float)Math.tanh(parameters.get("GAMMA").value * xixj + parameters.get("COEF0").value);
            }
            else{
                return 0;
            }
        }
    }


    private double rbf(double f) {
        return 0.3989423 * math.exp(-f*f/2);
    }


    private double cdf(double f) {
        return (1 + erf(f / 1.4142136)) / 2;
    }


    //Abramovitz approximation
    private double erf(double f) {

        //Symetric approximation
        if(f < 0){
            return -erf(-f);
        }
        else {
            double t = 1 / (1 + 0.47047*f);
            return (1 - (0.3480242*t - 0.0958798*t*t + 0.7478556*t*t*t) * math.exp(-t*t));
        }
    }


    public final void terminate() throws IOException{

        dataCached = false;

        if(dotCache != null){
            dotCache.terminate();
        }
    }

    //Get default kernel parameters
    public final static HashMap<String, Parameter> getDefaultParameters(SvmKernelType kernelType){

        HashMap<String, Parameter> parameters = new HashMap<String, Parameter>(7);

        Parameter GAMMA  = new Parameter("GAMMA",  0, 1e-15f, 100000, 10, IncrementFunction.MULTIPLY, true);
        Parameter ALPHA  = new Parameter("ALPHA",  0, -100, 100, 0.1f, IncrementFunction.ADD, true);
        Parameter COEF0  = new Parameter("COEF0",  0,  0, 100, 10, IncrementFunction.ADD, true);
        Parameter DEGREE = new Parameter("DEGREE", 0,  1, 5,  1, IncrementFunction.ADD, true);

        if(kernelType == SvmKernelType.POLYNOMIAL){
            parameters.put("GAMMA", GAMMA);
            parameters.put("COEF0", COEF0);
            parameters.put("DEGREE", DEGREE);
        }
        else if(kernelType == SvmKernelType.RBF){
            parameters.put("GAMMA", GAMMA);
        }
        else if(kernelType == SvmKernelType.SKEWEDRBF){
            parameters.put("GAMMA", GAMMA);
            parameters.put("ALPHA", ALPHA);
        }
        else if(kernelType == SvmKernelType.SIGMOID){
            parameters.put("GAMMA", GAMMA);
            parameters.put("COEF0", COEF0);
        }

        return parameters;
    }

}

