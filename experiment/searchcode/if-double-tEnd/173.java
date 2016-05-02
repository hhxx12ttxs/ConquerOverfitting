package tools.hmmer;


import tools.Jalview.Feature;
import tools.parents.Alignment;

public class hmmerModelAlignment extends Alignment implements Comparable{

	//Model name
	public String hmmname;
	//seq-f - start on the target transcript
	public int tstart;
	//seq-t - end on the target transcript
	public int tend;
	//properties of the Talignment
	public char[] tProp;
	//hmm-f - start in the hmm model
	public int hmmstart;
	//hmm-t - end in the hmm model
	public int hmmend;
	//properties of the hmmalignment
	public char[] hmmProp;
	//score
	public double score;
	//e-value
	public double eValue;
	
	private char compare='s';
	
	public hmmerModelAlignment(){
		tProp=new char[2];
		hmmProp=new char[2];
	}

	public String toString(){
		return hmmname+"\t"+tstart+"\t"+tend+"\t"+tProp[0]+tProp[1]+"\t"+hmmstart+"\t"+hmmend+"\t"+hmmProp[0]+hmmProp[1]+"\t"+score+"\t"+eValue;
	}
	
	public char getCompare() {
		return compare;
	}

	public void setCompare(char compare) {
		this.compare = compare;
	}

	public int compareTo(Object o){
		if(compare=='s'){
			if(((hmmerModelAlignment)o).score>this.score){
				return 1;
			}else if(((hmmerModelAlignment)o).score<this.score){
				return -1;
			}else
				return 0;
		} else if(compare=='e'){
			if(((hmmerModelAlignment)o).eValue<this.eValue){
				return 1;
			}else if(((hmmerModelAlignment)o).eValue>this.eValue){
				return -1;
			}else
				return 0;
		} else{
			//score as default case
			if(((hmmerModelAlignment)o).score>this.score){
				return 1;
			}else if(((hmmerModelAlignment)o).score<this.score){
				return -1;
			}else
				return 0;
		}
	}
	public Feature toJalviewDomainFeature(String tname){
		return new Feature("HMMER generated domain",tname,"Domain",hmmname,-1,tstart,tend);
	}
	public String toGFF(String strand){
		return hmmname+"\tPFAM\tCDS\t"+tstart+"\t"+tend+"\t"+eValue+"\t"+strand+"\t.";
	}
}

