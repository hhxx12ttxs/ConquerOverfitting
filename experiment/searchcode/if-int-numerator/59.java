package bio.tit.narise.RSASCore.model.factory.tool;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import bio.tit.narise.RSASCore.model.factory.product.CalcResult;
import bio.tit.narise.RSASCore.model.factory.product.ChooseRes;

/**
 *
 * @author TN
 */
public class ToolCombinationCalculator implements Callable<Map<Integer, ChooseRes>>, Tool {

    private final int N;
    private final int m;
    private final Map<Integer, ChooseRes> chooseResult;
    
    public ToolCombinationCalculator(int N, int m) {
        this.N = N;
        this.m = m;
        this.chooseResult = new HashMap();
    }
    
    @Override
    public Map<Integer, ChooseRes> call() throws Exception {
        ChooseRes result = (ChooseRes) calc();
        this.chooseResult.put(this.m, result);
        return this.chooseResult;
    }
    
    @Override
    public CalcResult calc() {
        
        // nCr, Iriyama algorizm
        int n = N;
        int r = m;
        if (n - r < r) { r = n - r; }
        if (r == 0) { 
            ChooseRes result = new ChooseRes(BigInteger.ZERO);
            return result;}
        if (r == 1) { 
            ChooseRes result = new ChooseRes(BigInteger.valueOf(n));
            return result;
        }
        
        int[] numerator = new int[r];
        int[] denominator = new int[r];
        
        for (int k = 0; k < r; k++) {
            numerator[k] = n - r + k + 1;
            denominator[k] = k + 1;
        }
        
        for (int p = 2; p < r+1; p++) {
            int pivot = denominator[p - 1];
            if (pivot > 1) {
                int offset = (n - r) % p;
                for (int k = p - 1; k < r; k += p) {
                    numerator[k - offset] /= pivot;
                    denominator[k] /= pivot;
                }
            }
        }
        
        BigInteger resultNum = BigInteger.ONE;
        for (int k = 0; k < r; k++) {
            if (numerator[k] > 1) { resultNum = resultNum.multiply(BigInteger.valueOf(numerator[k])); }
        }
        
        ChooseRes result = new ChooseRes(resultNum);
        return result;
    }
}

