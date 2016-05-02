package tools.blast;

import tools.Alignment;
import tools.overlap.overlapMethods;

public class blastM8Alignment extends Object implements Alignment{

	public String qname;
	public String tname;
	public double identity;
	public int alignment_length;
	public int mismatches;
	public int gap_openings;
	public int qstart;
	public int qend;
	public int tstart;
	public int tend;
	public double e_value;
	public double score;
	public char strand[];
	private char compare='e';
	
	public blastM8Alignment(){
		strand=new char[2];
	}
	
	public blastM8Alignment(String blastOut) throws Exception{
		this();
		String[] l=blastOut.split("\t");
		if (l.length<12)
			throw new Exception("Invalid line: "+blastOut);
		this.qname=l[0];
		this.tname=l[1];
		this.identity=Double.parseDouble(l[2]);
		this.alignment_length=Integer.parseInt(l[3]);
		this.mismatches=Integer.parseInt(l[4]);
		this.gap_openings=Integer.parseInt(l[5]);
		this.qstart=Integer.parseInt(l[6]);
		this.qend=Integer.parseInt(l[7]);
		this.tstart=Integer.parseInt(l[8]);
		this.tend=Integer.parseInt(l[9]);
		this.e_value=Double.parseDouble(l[10]);
		this.score=Double.parseDouble(l[11]);
		if(this.tstart>this.tend){
			this.strand[0]='-';
			int tmp=this.tstart;
			this.tstart=this.tend;
			this.tend=tmp;
		}else
			this.strand[0]='+';

	}
	
	public blastM8Alignment(blastM8Alignment ba){
		this();
		this.qname=ba.qname;
		this.tname=ba.tname;
		this.identity=ba.identity;
		this.alignment_length=ba.alignment_length;
		this.mismatches=ba.mismatches;
		this.gap_openings=ba.gap_openings;
		this.qstart=ba.qstart;
		this.qend=ba.qend;
		this.tstart=ba.tstart;
		this.tend=ba.tend;
		this.e_value=ba.e_value;
		this.score=ba.score;
		this.strand[0]=ba.strand[0];
		this.strand[1]=ba.strand[1];
	}
	
	public String getTgi() throws Exception{
		return tname.replace('|', '#').split("#")[1];
	}
	
	public char getCompare() {
		return compare;
	}
	public void setCompare(char compare) {
		this.compare = compare;
	}
	public int compareTo(Object o){
		if(compare=='s'){
			if(((blastM8Alignment)o).score>this.score){
				return 1;
			}else if(((blastM8Alignment)o).score<this.score){
				return -1;
			}else
				return 0;
		} else if(compare=='e'){
			if(((blastM8Alignment)o).e_value<this.e_value){
				return 1;
			}else if(((blastM8Alignment)o).e_value>this.e_value){
				return -1;
			}else
				return 0;
		} else{
			//score as default case
			if(((blastM8Alignment)o).score>this.score){
				return 1;
			}else if(((blastM8Alignment)o).score<this.score){
				return -1;
			}else
				return 0;
		}
	}
	public String toString(){
		if(strand[0]=='-'){
			return qname+"\t"+tname+"\t"+identity+"\t"+alignment_length+"\t"+mismatches+"\t"+gap_openings+"\t"+qstart+"\t"+qend+"\t"+tend+"\t"+tstart+"\t"+e_value+"\t"+score;
		}else{
			return qname+"\t"+tname+"\t"+identity+"\t"+alignment_length+"\t"+mismatches+"\t"+gap_openings+"\t"+qstart+"\t"+qend+"\t"+tstart+"\t"+tend+"\t"+e_value+"\t"+score;
			}
	}
	public String getQname() {
		return qname;
	}
	public char getStrand() {
		return strand[0];
	}
	public int getTend() {
		return tend;
	}
	public String getTname() {
		return tname;
	}
	public int getTstart() {
		return tstart;
	}
	public int getType() {
		return 2;
	}
	public boolean overlaps(Alignment b) throws Exception{
		return overlapMethods.blastM8Overlap(this, b,true);
	}
	
	public boolean overlapsNotStrandSpecific(Alignment b) throws Exception{
		return overlapMethods.blastM8Overlap(this, b,true)||overlapMethods.blastM8Overlap(this, b,false);
	}
}

