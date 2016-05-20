/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.snips.pml.svm;

import net.snips.pml.Model;
import net.snips.pml.Parameter;
import net.snips.pml.DataSet;
import java.util.HashMap;
import net.snips.pml.Algorithm;
import net.snips.pml.Input;
import static net.snips.pml.Constants.*;
import net.snips.pml.exceptions.MissingParametersException;
import net.snips.pml.Logger;

/**
 *
 * @author rhindi
 */
public abstract class Svm extends Algorithm {

    //The type of SVM being used
    protected SvmType svmType;

    //The type of kernel being used
    protected SvmKernelType kernelType;

    //The cache size
    protected int cacheSizeInMB;

    //The stopping criteria EPS
    protected float stoppingCriteria;


    public Svm(String name, SvmType svmType, SvmKernelType kernelType, int cachesizeInMB, float stoppingCriteria){

        super(name, AiMethod.SVM,   (svmType == SvmType.C_SVC
                               || svmType == SvmType.NU_SVC
                               || svmType == SvmType.ONE_CLASS) ? AiType.CLASSIFICATION : AiType.REGRESSION);

        this.svmType = svmType;
        this.kernelType = kernelType;
        this.cacheSizeInMB = cachesizeInMB;
        this.stoppingCriteria = stoppingCriteria;
    }

    public final SvmModel learn(Kernel kernel, DataSet normalizedTrainingSet, HashMap<String, Parameter> parameters) throws Exception {

        if(aiType == AiType.REGRESSION || svmType == SvmType.ONE_CLASS){
            return learnRegression(kernel, normalizedTrainingSet, parameters);
        }
        else if(aiType == AiType.CLASSIFICATION){
            return learnClassification(kernel, normalizedTrainingSet, parameters);
        }

        Logger.log(0, "Error, we should never be here!");

        return null;
    }

    public final float predict(Kernel kernel, Model model, Input input) throws Exception {

        SvmModel smod = (SvmModel)model;

        float[] decValues;

        if (svmType == SvmType.ONE_CLASS || svmType == SvmType.EPSILON_SVR || svmType == SvmType.NU_SVR) {
            decValues = new float[1];
        }
        else {
            decValues = new float[smod.nClasses * (smod.nClasses - 1) / 2];
        }

        float pred_result = svmPredict(kernel, (SvmModel)model, input, decValues);

        return pred_result;
    }

    private SvmModel learnClassification(Kernel kernel, DataSet trainingSet, HashMap<String, Parameter> parameters) throws Exception {

        Logger.log(2, "Learning Classification SVM");

        SvmModel model = new SvmModel();
        model.learnStartTime = System.currentTimeMillis();

        int elements = trainingSet.elements;
        int[] tmp_nr_class = new int[1];
        int[][] tmp_label = new int[1][];
        int[][] tmp_start = new int[1][];
        int[][] tmp_count = new int[1][];
        int[] perm = new int[elements];

        // group training data of the same class
        groupClasses(trainingSet, tmp_nr_class, tmp_label, tmp_start, tmp_count, perm);

        Logger.log(2, "Classes grouped");

        int nr_class = tmp_nr_class[0];
        int[] label = tmp_label[0];
        int[] start = tmp_start[0];
        int[] count = tmp_count[0];

        Input[] x = new Input[elements];
        double[] xo = new double[elements];

        for (int i = 0; i < elements; i++) {
            x[i] = trainingSet.inputs[perm[i]];
            xo[i] = trainingSet.outputs[perm[i]];
        }

        Logger.log(2, "X set");

        // calculate weighted C


        float[] weighted_C = new float[nr_class];

        //The C parameter is only used for C-SVC!
        if(svmType == SvmType.C_SVC){

            for (int i = 0; i < nr_class; i++) {
                weighted_C[i] = parameters.get("C").value;
            }

            int nweight = parameters.containsKey("NWEIGHTS") ? (int)parameters.get("NWEIGHTS").value : 0;

            for (int i = 0; i < nweight; i++) {

                int j;
                for (j = 0; j < nr_class; j++) {
                    if (parameters.get("WEIGHT_LABEL_"+i).value == label[j]) {
                        break;
                    }
                }

                if (j == nr_class) {
                    Logger.log(1, "Warning: class label " + parameters.get("WEIGHT_LABEL_"+i).value + " specified in weight is not found\n");
                }
                else {
                    weighted_C[j] *= parameters.get("WEIGHT_"+i).value;
                }
            }
        }

        // train k*(k-1)/2 models

        boolean[] nonzero = new boolean[elements];
        for (int i = 0; i < elements; i++) {
            nonzero[i] = false;
        }

        SolutionInfo[] sinfo = new SolutionInfo[nr_class * (nr_class - 1) / 2];

        float[] probA = null, probB = null;

        int p = 0;

        Logger.log(2, "Solving for " + nr_class + " classes");

        for (int i = 0; i < nr_class; i++) {

            for (int j = i + 1; j < nr_class; j++) {


                int si = start[i], sj = start[j];
                int ci = count[i], cj = count[j];

                DataSet subProb = new DataSet(ci+cj);

                int k;
                for (k = 0; k < ci; k++) {
                    subProb.inputs[k] = x[si + k];
                    subProb.outputs[k] = +1;
                }
                for (k = 0; k < cj; k++) {
                    subProb.inputs[ci + k] = x[sj + k];
                    subProb.outputs[ci + k] = -1;
                }

                sinfo[p] = solve(kernel, subProb, parameters, weighted_C[i], weighted_C[j]);

                for (k = 0; k < ci; k++) {
                    if (!nonzero[si + k] && Math.abs(sinfo[p].alpha[k]) > 0) {
                        nonzero[si + k] = true;
                    }
                }
                for (k = 0; k < cj; k++) {
                    if (!nonzero[sj + k] && Math.abs(sinfo[p].alpha[ci + k]) > 0) {
                        nonzero[sj + k] = true;
                    }
                }
                ++p;
            }
        }

        // build output


        model.parameters = parameters;
        model.nClasses = nr_class;

        model.classLabels = new int[nr_class];
        System.arraycopy(label, 0, model.classLabels, 0, nr_class);

        model.rho = new float[nr_class * (nr_class - 1) / 2];
        for (int i = 0; i < nr_class * (nr_class - 1) / 2; i++) {
            model.rho[i] = sinfo[i].rho;
        }

        int nnz = 0;
        int[] nz_count = new int[nr_class];

        model.nClassSV = new int[nr_class];

        for (int i = 0; i < nr_class; i++) {
            int nSV = 0;
            for (int j = 0; j < count[i]; j++) {
                if (nonzero[start[i] + j]) {
                    ++nSV;
                    ++nnz;
                }
            }
            model.nClassSV[i] = nSV;
            nz_count[i] = nSV;
        }

        model.nSV = nnz;
        model.SVs = new Input[nnz];
        model.svOuts = new double[nnz];

        p = 0;
        for (int i = 0; i < elements; i++) {
            if (nonzero[i]) {
                model.SVs[p] = x[i];
                model.svOuts[p] = xo[i];
                p++;
            }
        }

        int[] nz_start = new int[nr_class];
        nz_start[0] = 0;
        for (int i = 1; i < nr_class; i++) {
            nz_start[i] = nz_start[i - 1] + nz_count[i - 1];
        }

        model.svCoef = new float[nr_class - 1][];
        for (int i = 0; i < nr_class - 1; i++) {
            model.svCoef[i] = new float[nnz];
        }

        p = 0;
        for (int i = 0; i < nr_class; i++) {
            for (int j = i + 1; j < nr_class; j++) {
                // classifier (i,j): coefficients with
                // i are in sv_coef[j-1][nz_start[i]...],
                // j are in sv_coef[i][nz_start[j]...]

                int si = start[i];
                int sj = start[j];
                int ci = count[i];
                int cj = count[j];

                int q = nz_start[i];
                int k;
                for (k = 0; k < ci; k++) {
                    if (nonzero[si + k]) {
                        model.svCoef[j - 1][q++] = sinfo[p].alpha[k];
                    }
                }
                q = nz_start[j];
                for (k = 0; k < cj; k++) {
                    if (nonzero[sj + k]) {
                        model.svCoef[i][q++] = sinfo[p].alpha[ci + k];
                    }
                }
                ++p;
            }
        }

        model.learnEndTime = System.currentTimeMillis();

        return model;
    }

    //General regression learning function
    private SvmModel learnRegression(Kernel kernel, DataSet trainingSet, HashMap<String, Parameter> parameters) throws Exception {

        Logger.log(3, "Learning Regression SVM");

        SvmModel model = new SvmModel();

        model.learnStartTime = System.currentTimeMillis();
        model.parameters = parameters;
        model.nClasses = 2;
        model.SVs = null;
        model.svCoef = new float[1][];

        Logger.log(3, "Solving regression SVM");

        SolutionInfo si = solve(kernel, trainingSet, parameters, 0, 0);

        int nSV = 0;
        for (int i = 0; i < trainingSet.elements; i++) {

            if (si.alpha[i] != 0) {
                nSV++;
            }
        }

        model.rho = new float[1];
        model.rho[0] = si.rho;

        model.SVs = new Input[nSV];
        model.svOuts = new double[nSV];
        model.svCoef[0] = new float[nSV];

        int pos = 0;
        for (int i = 0; i < trainingSet.elements; i++) {
            if (si.alpha[i] != 0) {
                model.SVs[pos] = new Input(trainingSet.inputs[i]);
                model.svOuts[pos] = trainingSet.outputs[i];
                model.svCoef[0][pos] = si.alpha[i];
                pos++;
            }
        }

        model.learnEndTime = System.currentTimeMillis();
        model.nSV = nSV;

        Logger.log(3, "Done learning regression SVM");

        return model;
    }

    // label: label name, start: begin of each class, count: #data of classes, perm: indices to the original data
    // perm, length l, must be allocated before calling this subroutine
    private static void groupClasses(DataSet trainingSet, int[] nr_class_ret, int[][] label_ret, int[][] start_ret, int[][] count_ret, int[] perm) {

        int l = trainingSet.elements;
        int max_nr_class = 16;
        int nr_class = 0;
        int[] label = new int[max_nr_class];
        int[] count = new int[max_nr_class];
        int[] data_label = new int[l];
        int i;

        for (i = 0; i < l; i++) {

            int this_label = (int) (trainingSet.outputs[i]);

            int j;
            for (j = 0; j < nr_class; j++) {
                if (this_label == label[j]) {
                    ++count[j];
                    break;
                }
            }

            data_label[i] = j;

            if (j == nr_class) {

                if (nr_class == max_nr_class) {
                    max_nr_class *= 2;
                    int[] new_data = new int[max_nr_class];
                    System.arraycopy(label, 0, new_data, 0, label.length);
                    label = new_data;
                    new_data = new int[max_nr_class];
                    System.arraycopy(count, 0, new_data, 0, count.length);
                    count = new_data;
                }

                label[nr_class] = this_label;
                count[nr_class] = 1;
                ++nr_class;
            }
        }

        int[] start = new int[nr_class];
        start[0] = 0;
        for (i = 1; i < nr_class; i++) {
            start[i] = start[i - 1] + count[i - 1];
        }
        for (i = 0; i < l; i++) {
            perm[start[data_label[i]]] = i;
            ++start[data_label[i]];
        }
        start[0] = 0;
        for (i = 1; i < nr_class; i++) {
            start[i] = start[i - 1] + count[i - 1];
        }

        nr_class_ret[0] = nr_class;
        label_ret[0] = label;
        start_ret[0] = start;
        count_ret[0] = count;
    }

    public final boolean parametersAreValid(HashMap<String, Parameter> parameters) {

        if (parameters.containsKey("GAMMA") && parameters.get("GAMMA").value < 0) {
            Logger.log(1, "GAMMA < 0");
            return false;
        }

        if (parameters.containsKey("DEGREE") && parameters.get("DEGREE").value < 0) {
            Logger.log(1, "DEGREE < 0");
            return false;
        }

        if (parameters.containsKey("CACHESIZE") && parameters.get("CACHESIZE").value <= 0) {
            Logger.log(1, "CACHESIZE <= 0");
            return false;
        }

        if (parameters.containsKey("EPS") && parameters.get("EPS").value <= 0) {
            Logger.log(1, "EPS <= 0");
            return false;
        }

        if (svmType == SvmType.C_SVC || svmType == SvmType.EPSILON_SVR || svmType == SvmType.NU_SVR) {
            if (parameters.containsKey("C") && parameters.get("C").value <= 0) {
                Logger.log(1, "C <= 0");
                return false;
            }
        }

        if (svmType == SvmType.NU_SVC || svmType == SvmType.ONE_CLASS || svmType == SvmType.NU_SVR) {
            if (parameters.containsKey("NU") && (parameters.get("NU").value <= 0 || parameters.get("NU").value > 1)) {
                Logger.log(1, "NU <= 0 or NU > 1");
                return false;
            }
        }

        if (svmType == SvmType.EPSILON_SVR) {
            if (parameters.containsKey("P") && parameters.get("P").value < 0) {
                Logger.log(1, "P < 0");
                return false;
            }
        }

        return true;
    }

    //Generate parameters stub
    public final HashMap<String, Parameter> generateStartingParameters(){

        //Get kernel parameters
        HashMap<String, Parameter> parameters = Kernel.getDefaultParameters(kernelType);

        //Get SVM default parameters
        parameters.putAll(getDefaultParameters());

        //Get general parameters
        parameters.put("EPS", new Parameter("EPS", stoppingCriteria));
        parameters.put("CACHESIZE", new Parameter("CACHESIZE", cacheSizeInMB));

        return parameters;
    }

    public final float svmPredict(Kernel kernel, SvmModel model, Input input, float[] decValues) throws MissingParametersException {

        if (aiType == AiType.REGRESSION || svmType == SvmType.ONE_CLASS) {

            float[] sv_coef = model.svCoef[0];

            float sum = 0;
            for (int i = 0; i < model.SVs.length; i++) {
                sum += sv_coef[i] * kernel.calculate(kernelType, input, model.SVs[i], model.parameters);
            }
            sum -= model.rho[0];

            decValues[0] = sum;

            if (svmType == SvmType.ONE_CLASS) {
                return (sum > 0) ? 1 : -1;
            }
            else {
                return sum;
            }
        }
        else {

            int nr_class = model.nClasses;

            float[] kvalue = new float[model.SVs.length];
            for (int i = 0; i < model.SVs.length; i++) {
                kvalue[i] = kernel.calculate(kernelType, input, model.SVs[i], model.parameters);
            }

            int[] start = new int[nr_class];
            start[0] = 0;
            for (int i = 1; i < nr_class; i++) {
                start[i] = start[i - 1] + model.nClassSV[i - 1];
            }

            int[] vote = new int[nr_class];
            for (int i = 0; i < nr_class; i++) {
                vote[i] = 0;
            }

            int p = 0;
            for (int i = 0; i < nr_class; i++) {
                for (int j = i + 1; j < nr_class; j++) {

                    float sum = 0;
                    int si = start[i];
                    int sj = start[j];
                    int ci = model.nClassSV[i];
                    int cj = model.nClassSV[j];

                    int k;
                    float[] coef1 = model.svCoef[j - 1];
                    float[] coef2 = model.svCoef[i];
                    for (k = 0; k < ci; k++) {
                        sum += coef1[si + k] * kvalue[si + k];
                    }
                    for (k = 0; k < cj; k++) {
                        sum += coef2[sj + k] * kvalue[sj + k];
                    }
                    sum -= model.rho[p];
                    decValues[p] = sum;

                    if (decValues[p] > 0) {
                        ++vote[i];
                    } else {
                        ++vote[j];
                    }
                    p++;
                }
            }

            int vote_max_idx = 0;
            for (int i = 1; i < nr_class; i++) {
                if (vote[i] > vote[vote_max_idx]) {
                    vote_max_idx = i;
                }
            }

            return model.classLabels[vote_max_idx];
        }
    }

    //This is used to return the SVM specific default parameters. Does not include kernel or general params
    protected abstract HashMap<String, Parameter> getDefaultParameters();

    //This is to be implemented for each type of SVM
    protected abstract SolutionInfo solve(Kernel kernel, DataSet trainingSet, HashMap<String, Parameter> parameters, float Cp, float Cn) throws Exception;



}

