package com.can.lps;

import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.Line;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.Mixer;
import javax.sound.sampled.TargetDataLine;

import com.can.poly.AlgebraicSolver;
import com.can.poly.EquationSolver;
import com.can.poly.NewtonMethod;
import com.can.poly.Polynomial;
import com.can.poly.SolutionSet;

import flanagan.analysis.Regression;

public class Test {
	public static void main(String[] args) throws Throwable{
		float sampleRate = 44100;
		int sampleSizeInBits = 8;
		int channels = 1;
		boolean signed = true;
		boolean bigEndian = true;
		final AudioFormat format=new AudioFormat(sampleRate,sampleSizeInBits, channels, signed, bigEndian);
		DataLine.Info info = new DataLine.Info(
				TargetDataLine.class, format);
		final TargetDataLine line = (TargetDataLine)
		AudioSystem.getLine(info);
		line.open(format);
		line.start();
		int bufferSize = (int)format.getSampleRate() 
		* format.getFrameSize();
		byte buffer[] = new byte[2000];

		ByteArrayOutputStream out = new ByteArrayOutputStream();
		LinkedList<Double> running=new LinkedList<Double>();
		while(true){
			try {
				for(int i=0;i<1;i++){
					int count = 
						line.read(buffer, 0, buffer.length);
					if (count > 0) {
						out.write(buffer, 0, count);
					}
				}
				out.close();
			} catch (IOException e) {
				System.err.println("I/O problems: " + e);
				System.exit(-1);
			}
			byte[] array=out.toByteArray();
			double[] xValues=new double[array.length];
			double[] yValues=new double[array.length];
			for(int i=0;i<array.length;i++){
				double phase=((double)i)/22.05d;
				long phaseI=(long)phase;
				double x=phase-phaseI;
				double y=array[i];//(array[i]<<8)+array[i+1];
				xValues[i]=x;
				yValues[i]=y;
			}
			Regression regression=new Regression(xValues,yValues);
			int n=7;
			regression.polynomial(n);
			//regression.polynomialPlot(n);
			//if(running.size()==10)break;
			
			double[] vs=regression.getBestEstimates();
			Polynomial poly=new Polynomial();
			for(int i=0;i<n+1;i++){
				poly.setCoefficient(i, vs[i]);
			}
			Polynomial der=poly.getDerivative(null);
			EquationSolver es=new AlgebraicSolver();
			SolutionSet ss=new SolutionSet();
			//es.solve(der, ss);
			double min=Double.POSITIVE_INFINITY;
			double max=Double.NEGATIVE_INFINITY;
			/*for(int i=0;i<ss.getSolutionCount();i++){
				double soln=ss.getSolution(i);
				double y=poly.evaluate(soln);
				if(soln>=0){
					min=Math.min(min, y);
					max=Math.max(max,y);
				}
			}*/
			
			for(double k=0;k<1;k+=.0001){
				double y=poly.evaluate(k);
				min=Math.min(min, y);
				max=Math.max(max,y);
			}
			if(max>min){
				double distance=max-min;
				running.add(distance);
				if(running.size()>10){
					running.poll();
				}
				double total=0;
				for(Double d:running){
					total+=d;
				}
				System.out.println(total/running.size());
			}else{
				//System.out.println(ss.getSolutionCount()+" "+der);
			}
			//System.out.println(total);
			out.reset();
			
		}
	}
}
