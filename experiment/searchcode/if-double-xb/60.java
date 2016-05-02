/**
 * File: ArcConnectionDistanceEstimate.java
 * Created by: mhaimel
 * Created on: Nov 19, 2010
 * CVS:  $Id: ArcConnectionDistanceEstimate.java 1.0 Nov 19, 2010 11:02:57 AM mhaimel Exp $
 */
package uk.ac.ebi.curtain.model.graph.curtain.filter;

import java.util.Collections;
import java.util.List;

import org.apache.commons.math.MathException;
import org.apache.commons.math.MathRuntimeException;
import org.apache.commons.math.distribution.NormalDistribution;
import org.apache.commons.math.distribution.NormalDistributionImpl;

import uk.ac.ebi.curtain.model.graph.Arc;
import uk.ac.ebi.curtain.model.graph.Node;
import uk.ac.ebi.curtain.model.graph.GraphAccess.ProcessEach;
import uk.ac.ebi.curtain.model.graph.curtain.CategoryReadInfo;
import uk.ac.ebi.curtain.model.graph.curtain.ContigInfo;
import uk.ac.ebi.curtain.model.graph.curtain.ReadWrapper;
import uk.ac.ebi.curtain.utils.StatisticHelper;

/**
 * @author mhaimel
 *
 */
public class ArcConnectionDistanceEstimate implements ProcessEach<Arc<ContigInfo, ReadWrapper>> {

	private final int insLen;
	private final int insSd;
	private final int cat;
	private final int min;
	
	public ArcConnectionDistanceEstimate(Integer insLen, Integer insSd, Integer cat, int min) {
		this.insLen = insLen;
		this.insSd = insSd;
		this.cat = cat;
		this.min = min;
	}
	
	public Integer getInsLen() {
		return insLen;
	}
	
	public Integer getInsSd() {
		return insSd;
	}
	
	@Override
	public Arc<ContigInfo, ReadWrapper> process(int idx,Arc<ContigInfo, ReadWrapper> arc) {
		if(null == arc){
			return arc;
		}
		ReadWrapper val = arc.getValue();
		CategoryReadInfo info = val.getInfo(getCategory());
		if(null != info){
			Integer multi = info.getMultiplicity();
			if(multi < min){
				return null;
			}
			List<Integer> gaps = info.getGaps();
			ContigInfo left = arc.getLeft().getValue();
			ContigInfo right = arc.getRight().getValue();
			
			Collections.sort(gaps);
			
			StatisticHelper<Integer> h = new StatisticHelper<Integer>(gaps);
			Integer median = h.median();
			boolean isLeft = left.getLength()>right.getLength();
			double lCovR = calculateFocusedCoverage(getCategory(),arc,arc.getLeft());
			double rCovR = calculateFocusedCoverage(getCategory(),arc,arc.getRight());
			double longCov = isLeft?lCovR:rCovR;
			Long longLen = isLeft?left.getLength():right.getLength();
			Long shortLen = isLeft?right.getLength():left.getLength();

			double num = 0d;
			num = expectedConnections(median.doubleValue(),getInsLen(), getInsSd(),longLen,shortLen,longCov);
//			if(median >= 0) {
//				num = expectedConnections(median.doubleValue(),getInsLen(), getInsSd(),longLen,shortLen,longCov);
//			} else {
//				TODO expected Connections in case of overlap
//			}
			int min = (int) num; 
			if(min > multi){
				return null;
			} 
		}	
		return arc;
	}

	private Integer getCategory() {
		return cat;
	}
		
	public static double calculateFocusedCoverage(int cat, Arc<ContigInfo, ReadWrapper> a,Node<ContigInfo, ReadWrapper> node) {
		ContigInfo v = node.getValue();
		Long len = v.getLength();
		if(len <= 0){
			return 0d;
		}
		double readLen = v.getAvgReadLength(cat);
		double tbp = v.getFull(cat)*readLen;
		tbp += a.getValue().getInfo(cat).getMultiplicity() * readLen;
		return tbp/len;
	}
	
	public static double expectedConnections(double distance, double mu, double sigma, long longLength, long shortLength, double cnt) {
//		double d = getTotalCovered().doubleValue();
//		double mu = getDistance().getLength();
		sigma = Math.sqrt(sigma);
		try {
			NormalDistributionImpl impl = new NormalDistributionImpl();
			impl.setMean(mu);
			impl.setStandardDeviation(sigma);
			if(mu <= 0){
				return 0;
			}
			double A = longLength;
			double B = shortLength;
			double d = Math.abs(distance);
//			double d = distance; // TODO
			double Xa = d;
			double a = (Xa-mu)/sigma;
			double Xb = d+B;
			double Xg = d+A;
			double Xp = d+B+A;
			double p = (Xp-mu)/sigma;

			/* left side */
			double da = impl.density(Xa);
			double db = impl.density(Xb);
			double lCp = cumulativeProbability(Xa, Xb,impl);
			double left = sigma*(da-db-a*lCp);
			
			/* middle bit*/			
			double mCp = cumulativeProbability(Xb,Xg,impl);
			double middle = B * mCp;
			
			/* right side*/
			double dg = impl.density(Xg);
			double dp = impl.density(Xp);
			double rCp = cumulativeProbability(Xg,Xp,impl);
			double right = -sigma*(dg-dp-p*rCp);
			
			double result = (cnt*(left+middle+right))/A;
			
			if(result<=0){
				result = 0;
			}
			return result;
		} catch (MathException e) {
			throw new Error(e);
		}
	}
	public static double cumulativeProbability(double x0, double x1, NormalDistribution impl) throws MathException {
	    if (x0 > x1) {
	        throw MathRuntimeException.createIllegalArgumentException(
	              "lower endpoint ({0}) must be less than or equal to upper endpoint ({1})",
	              x0, x1);
	    }
	    double cp1 = impl.cumulativeProbability(x1);
	    if(Double.isInfinite(cp1)){
	    	cp1 = 1;
	    }
	    double cp0 = impl.cumulativeProbability(x0);
	    if(Double.isInfinite(cp0)){
	    	cp0 = 1;
	    }
		return cp1 - cp0;
	}
	
}

