/*
 * BandPassPole.java
 *
 * Created on 2006/11/23, 4:01
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package TypeDef;

/**
 *
 * @author T2
 */
public class BandPassPole {
    
    public static final int LOW=0;
    public static final int HIGH=1;
    private int nPole;
    public double[] low;
    public double[] high;
    private String poleType;
    private String winType;
    
    /** Creates a new instance of BandPassPole */
    
    public BandPassPole(String poleType,String winType,double p1,double p2){
	this.poleType=poleType;
	this.winType=winType;
        nPole=1;
        low=new double[nPole];
        high=new double[nPole];
        if(poleType.substring(poleType.length()-2,poleType.length()).equalsIgnoreCase("LH")){
            low[0]=Math.min(p1,p2);
            high[0]=Math.max(p1,p2);
        } else if(poleType.substring(poleType.length()-2,poleType.length()).equalsIgnoreCase("BC")){
            low[0]=p2-p1/2d;
            high[0]=p2+p1/2d;
        }
    }
    public BandPassPole(String poleType,String winType,double[] p1,double[] p2){
        this(poleType,winType,p1[0],p2[0]);
        int ndam=Math.min(p1.length,p2.length);
        for(int i=1;i<ndam;i++){
            addPassWindow(poleType,p1[i],p2[i]);
        }
    }
    
    public BandPassPole(String poleType,double p1,double p2){
	this(poleType,"FButter",p1,p2);
    }
    public BandPassPole(String type,double[] p1,double[] p2) {
        this(type,p1[0],p2[0]);
        int ndam=Math.min(p1.length,p2.length);
        for(int i=1;i<ndam;i++){
            addPassWindow(type,p1[i],p2[i]);
        }
    }
    public void addPassWindow(String type,double p1,double p2){
        if(type.substring(type.length()-2,type.length()).equalsIgnoreCase("LH")){
            addPassWindowLH(p1,p2);
        }else if(type.substring(type.length()-2,type.length()).equalsIgnoreCase("BC")){
            addPassWindowBC(p1,p2);
        }
    }
    public void addPassWindowLH(double p1,double p2){
        if(p1>p2){
            double dam=p1;
            p1=p2;
            p2=dam;
        }
        for(int i=0;i<nPole;i++){
            if(low[i]<=p1 && p2<=high[i])break;
            else if(p1<=low[i] && high[i]<=p2)break;
            else if(low[i]<=p1 && p1<high[i] && high[i]<=p2){
                high[i]=p2;
            }else if(p1<low[i] && low[i]<p2 && p2<=high[i]){
                low[i]=p1;
            }
            if(i==nPole-1){
                double[] damA1=new double[nPole+1];
                double[] damA2=new double[nPole+1];
                for(int j=0;j<nPole;j++){
                    damA1[j]=low[j];
                    damA2[j]=high[j];
                }
                damA1[nPole]=p1;
                damA2[nPole]=p2;
                nPole++;
                low=damA1;
                high=damA2;
            }
        }
    }
    public void addPassWindowBC(double p1,double p2){
        double dam1=p1;
        double dam2=p2;
        p1=dam2-dam1/2d;
        p2=dam2+dam1/2d;
        addPassWindowLH(p1,p2);
    }
    
    public double[] convertBCtoLH(double p1,double p2){
        double[] lh=new double[2];
        lh[0]=p2-p1/2d;
        lh[1]=p2+p1/2d;
        return lh;
    }
    public double[] convertLHtoBC(double p1,double p2){
        if(p1>p2){
            double dam=p1;
            p1=p2;
            p2=dam;
        }
        double[] bc=new double[2];
        bc[0]=p2-p1;
        bc[1]=(p1+p2)/2d;
        return bc;
    }
    
    public double[][] getPoleLH(){
        double[][] pole=new double[2][nPole];
        for(int i=0;i<nPole;i++){
            pole[0][i]=low[i];
            pole[1][i]=high[i];
        }
        return pole;
    }
    public double[][] getPoleBC(){
        double[][] pole=new double[2][nPole];
        for(int i=0;i<nPole;i++){
            pole[0][i]=high[i]-low[i];
            pole[1][i]=(low[i]+high[i])/2d;
        }
        return pole;
    }
    public double[] getPoleLow(){
        return low;
    }
    public double[] getPoleHigh(){
        return high;
    }
    
    public String getWindowType(){
	return winType;
    }
}

