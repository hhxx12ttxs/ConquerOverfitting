import java.io.Console;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Random;
import java.util.Scanner;

import javax.naming.spi.DirStateFactory.Result;

public class TwoLayerPerceptron {
	
	double theta[];
	double sigma[];
	
	double w[][];
	double v[][];
	
	// 指定した乱数でフィルした配列を返します
	double[] getRandomArray(int size, double min, double max){
		double result[] = new double[size];
		
		double domainLength = max - min;
		double domainMedian = (max + min)/2;
		
		Random random = new Random();
		
		for(int i=0 ; i<size ; i++){
			result[i] = random.nextDouble()*domainLength;
			result[i] -= domainLength/2;
			result[i] += domainMedian;
		}
		
		return result;
	}
	
	// シグモイド関数を返します
	public double sigmoid(double x){
		return 1/(1 + Math.exp(-1*x));
	}
	
	// シグモイド関数の微分を返します
	public double derivativeOfSigmoid(double x){
		return sigmoid(x)*(1 - sigmoid(x));
	}

	// y*を返します
	double[] getYStar(double xP[]){
		double yStar[] = new double[getMiddleSize()];
		for(int j=0 ; j<getMiddleSize() ; j++){
			for(int i=0 ; i<getInputSize() ; i++){
				yStar[j] += getWeightToMiddle(i, j)*xP[i];
			}
			yStar[j] -= getThresholdMiddle(j);
		}
		return yStar;
	}
	
	// yを返します
	double[] getY(double[] yStar){
		double[] y = new double[getMiddleSize()];
		for(int j=0 ; j<getMiddleSize() ; j++){
			y[j] = sigmoid(yStar[j]);
		}
		return y;
	}

	// z*を返します
	double[] getZStar(double y[]){
		double zStar[] = new double[getOutputSize()];
		for(int k=0 ; k<getOutputSize() ; k++){
			for(int j=0 ; j<getMiddleSize() ; j++){
				zStar[k] += getWeightToOutput(j, k)*y[j];
			}
			zStar[k] -= getThresholdOutput(k);
		}
		return zStar;
	}
	
	// zを返します
	double[] getZ(double[] zStar){
		double[] z = new double[getOutputSize()];
		for(int k=0 ; k<getOutputSize() ; k++){
			z[k] = sigmoid(zStar[k]);
		}
		return z;
	}
	
	// delta^outを返します
	double[] getDeltaOutput(double[] z, double[] T){
		double[] deltaOutput = new double[getOutputSize()];
		for(int k=0 ; k<getOutputSize() ; k++){
			deltaOutput[k] = 2*(z[k] - T[k]);
		}
		return deltaOutput;
	}

	// delta^midを返します
	double[] getDeltaMiddle(double[] deltaOutput, double[] zStar){
		double[] deltaMiddle = new double[getMiddleSize()];
		for(int j=0 ; j<getMiddleSize() ; j++){
			for(int k=0 ; k<getOutputSize() ; k++){
				deltaMiddle[j] += getWeightToOutput(j, k)*deltaOutput[k]*derivativeOfSigmoid(zStar[k]);
			}
		}
		return deltaMiddle;
	}
	
	// v(WeightToOutput)を修正します
	void modifyWeightToOutPut(double alpha, double[] deltaOutput, double[] zStar, double[] y){
		for(int k=0 ; k<getOutputSize() ; k++){
			for(int j=0 ; j<getMiddleSize() ; j++){
				setWeightToOutput(getWeightToOutput(j, k) 
						- alpha*deltaOutput[k]*derivativeOfSigmoid(zStar[k])*y[j], j, k);
			}
		}
	}
	
	// sigma(ThresholdOutput)を修正します
	void modifyThresholdOutput(double alpha, double[] deltaOutput, double[] zStar){
		for(int k=0 ; k<getOutputSize() ; k++){
			setThresholdOutput(getThresholdOutput(k)
					+ alpha*deltaOutput[k]*derivativeOfSigmoid(zStar[k]), k); 
		}
	}
	
	// w(WeightToMiddle)を修正します
	void modifyWeightToMiddle(double alpha, double[] deltaMiddle, double[] yStar, double[] xP){
		for(int i=0 ; i<getInputSize() ; i++){
			for(int j=0 ; j<getMiddleSize() ; j++){
				setWeightToMiddle(getWeightToMiddle(i, j)
						- alpha*deltaMiddle[j]*derivativeOfSigmoid(yStar[j])*xP[i], i, j);
			}
		}
	}

	// theta(ThresholdMiddle)を修正します
	void modifyThresholdMiddle(double alpha, double[] deltaMiddle, double[] yStar){
		for(int j=0 ; j<getMiddleSize() ; j++){
			setThresholdMiddle(getThresholdMiddle(j)
			          + alpha*deltaMiddle[j]*derivativeOfSigmoid(yStar[j]), j);
		}
	}

	// 入力が inSize 次元、中間層の細胞の数が midSize、出力が outSize 次元の2層パーセプトロンを生成し
	// 全ての重みと閾値を-0.01〜0.01程度の乱数で初期化する
	TwoLayerPerceptron(int inSize, int midSize, int outSize){
		theta = getRandomArray(midSize, -0.01, 0.01);
		sigma = getRandomArray(outSize, -0.01, 0.01);
		
		w = new double[inSize][];
		for(int i=0 ; i<inSize ; i++){
			w[i] = getRandomArray(midSize, -0.01, 0.01);
		}
		
		v = new double[midSize][];
		for(int j=0 ; j<midSize ; j++){
			v[j] = getRandomArray(outSize, -0.01, 0.01);
		}
	}
	
	// 入力xに対する出力値zを求める
	// 返り値を格納する配列のメモリも確保する必要があることに注意
	public double[] calculateOutput(double x[]){
		double[] yStar = getYStar(x);
		double[] y = getY(yStar);
		double[] zStar = getZStar(y);
		double[] z = getZ(zStar);
		
		return z;
	}
	
	// 学習データ集合exsetを用いた学習率alphaの学習を、誤差の平均がerr以下になるか学習回数がmax回になるまで繰り返す
	// 実習書3.3節におけるアルゴリズムの 2～6 に相当する
	// Exampleクラスのインスタンスは学習例1つに対応し、getInput及びgetOutputメソッドでその例の入力、出力をdouble[]型で返す(Example.java参照)
	// このメソッドは追加的な学習にも用いるため、このメソッド内で重みを初期化してはいけないことに注意
	public void learn(Example exset[], double alpha, double err, int max){
		
		Random random = new Random();
		final int interval = 200;
		
		for(int count=0 ; count<max ; count++){
			Example currentExset = exset[random.nextInt(exset.length)];
		
			// 2.前向き計算
			double[] yStar = getYStar(currentExset.getInput());
			double[] y = getY(yStar);
			double[] zStar = getZStar(y);
			double[] z = getZ(zStar);
			
			// 3.出力層での誤差
			double[] deltaOutput = getDeltaOutput(z, currentExset.getOutput());
			
			// 4.中間層での誤差
			double[] deltaMiddle = getDeltaMiddle(deltaOutput, zStar);
			
			// 5.学習
			modifyWeightToOutPut(alpha, deltaOutput, zStar, y);
			modifyThresholdOutput(alpha, deltaOutput, zStar);
			modifyWeightToMiddle(alpha, deltaMiddle, yStar, currentExset.getInput());
			modifyThresholdMiddle(alpha, deltaMiddle, yStar);
			
			// 6.終了条件
			if(count%interval == 0){
				if(isFinishLearn(exset, err)) break;
			}
		}		
	}
	
	public boolean isFinishLearn(Example[] exset, double threshold){
		double E = 0;
		for(int p=0 ; p<exset.length ; p++){
			double zP[] = calculateOutput(exset[p].getInput());
			for(int k=0 ; k<getOutputSize() ; k++){
				E += Math.pow((zP[k] - exset[p].getOutput()[k]), 2);
			}
		}
		System.out.println(E);
		return (E/exset.length < threshold);
	}
	
	// 入力層のi番目の細胞から中間層のj番目の細胞に至る結合強度を返す
	// 細胞番号は0から開始し、0以上(細胞数-1)以下であるものとする
	public double getWeightToMiddle(int i, int j){
		return w[i][j];
	}
	
	// 中間層のj番目の細胞から出力層のk番目の細胞に至る結合強度を返す
	public double getWeightToOutput(int j, int k){
		return v[j][k];
	}
	
	// 入力層のi番目の細胞から中間層のj番目の細胞に至る結合強度を、wgtに設定する
	public void setWeightToMiddle(double wgt, int i, int j){
		w[i][j] = wgt;
	}
	
	// 中間層のj番目の細胞から出力層のk番目の細胞に至る結合強度を、wgtに設定する
	public void setWeightToOutput(double wgt, int j, int k){
		v[j][k] = wgt;
	}
	
	// 中間層のj番目の細胞の閾値を返す
	public double getThresholdMiddle(int j){
		return theta[j];
	}
	
	// 出力層のk番目の細胞の閾値を返す
	public double getThresholdOutput(int k){
		return sigma[k];
	}
	
	// 中間層のj番目の細胞の閾値をthに設定する
	public void setThresholdMiddle(double th, int j){
		theta[j] = th;
	}
	
	// 出力層のk番目の細胞の閾値をthに設定する
	public void setThresholdOutput(double th, int k){
		sigma[k] = th;
	}

	// 入力層の細胞数（入力の次元数）を返す
	public int getInputSize(){
		return w.length;
	}
	
	// 中間層の細胞数を返す
	public int getMiddleSize(){
		return theta.length;
	}
	
	// 出力層の細胞数（出力の次元数）を返す
	public int getOutputSize(){
		return sigma.length;
	}
	
	// [実装済]
	// 与えられた複数の入力例に対し、それぞれの入力例に対する出力を表示する
	public void printResult(double xset[][]){
		for(int p=0;p<xset.length;p++){
			System.out.printf("Pattern %3d  -->", p);
			double z[] = calculateOutput(xset[p]);
			for(int i=0;i<getOutputSize();i++){
				System.out.printf(" %6.3f", z[i]);
			}
			System.out.println("");
		}
	}

	// [実装済]
	// ファイルpathに書かれた2層パーセプトロンを読み込んで返す
	public static TwoLayerPerceptron readFile(String path){
		try{
			Scanner file = new Scanner(new File(path));
			int inSize = file.nextInt();
			int midSize = file.nextInt();
			int outSize = file.nextInt();
			TwoLayerPerceptron tlp = new TwoLayerPerceptron(inSize, midSize, outSize);
			for(int j=0;j<midSize;j++){
				for(int i=0;i<inSize;i++){
					tlp.setWeightToMiddle(file.nextDouble(), i, j);
				}
				tlp.setThresholdMiddle(file.nextDouble(), j);
			}
			for(int k=0;k<outSize;k++){
				for(int j=0;j<midSize;j++){
					tlp.setWeightToOutput(file.nextDouble(), j, k);
				}
				tlp.setThresholdOutput(file.nextDouble(), k);
			}
			return tlp;
		}catch(Exception e){
			System.out.println("[ERROR] readFile " + path);
			e.printStackTrace();
			return null;
		}
	}

	// [実装済]
	// 2層パーセプトロンをファイルpathに書き出す
	public boolean writeFile(String path){
		try{
			FileWriter fw = new FileWriter(new File(path));
			int inSize = getInputSize();
			int midSize = getMiddleSize();
			int outSize = getOutputSize();
			fw.write(inSize + " " + midSize + " " + outSize + "\n");
			for(int j=0;j<midSize;j++){
				for(int i=0;i<inSize;i++){
					fw.write(getWeightToMiddle(i, j) + " ");
				}
				fw.write(getThresholdMiddle(j) + "\n");
			}
			for(int k=0;k<outSize;k++){
				for(int j=0;j<midSize;j++){
					fw.write(getWeightToOutput(j, k) + " ");
				}
				fw.write(getThresholdOutput(k) + "\n");
			}
			fw.close();
			return true;
		}catch(Exception e){
			e.printStackTrace();
			return false;
		}
	}
	
}

