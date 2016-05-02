package tools.blat;
//package dg;

import java.io.Serializable;
import java.util.ArrayList;

import tools.Alignment;
import tools.Jalview.Feature;
import tools.overlap.overlapMethods;



//TODO: strand!

public class EstAlignment implements Comparable<EstAlignment>, Serializable,Alignment
{
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	// Number of bases that match that aren't repeats
	public int matches;
	
	// Number of bases that don't match
	public int mismatches;
	
	// Number of bases that match but are part of repeats
	public int repmatches;
	
	// Number of 'N' bases
	public int ncount;
	
	// Number of inserts in query
	public int qnuminsert;
	
	// Number of bases inserted in query
	public int qbaseinsert;
	
	// Number of inserts in target
	public int tnuminsert;
	
	// Number of bases inserted in target
	public int tbaseinsert;
	
	public char strand[];
	
	public int qsize;
	
	// Alignment start position in query
	public int qstart;
	
	// Alignment end position in query
	public int qend;
	
	public int tsize;
	
	// Alignment start position in target
	public int tstart;
	
	// Alignment end position in target
	public int tend;
	
	public int vgeneId;
	
	// Query sequence name
	public String qname;
	
	// Target sequence name
	public String tname;

	// Size of each block
	public int[] blockSizes;

	// Start of each block in query
	public int[] qStarts;
	
	// Start of each block in target
	public int[] tStarts;
		
	// Contamination class, 0 if none
	public int contClass;
	
	// Contamination start
	public int contStart;
	
	// Contamination end
	public int contEnd;
		
	public EstAlignment() {
		blockSizes= new int[0];
		qStarts= new int[0];
		tStarts= new int[0];
		strand= new char[2];
	}
	public EstAlignment(EstAlignment tmp){
		this.matches=tmp.matches;
		this.mismatches=tmp.mismatches;
		this.repmatches=tmp.repmatches;
		this.ncount=tmp.ncount;
		this.qnuminsert=tmp.qnuminsert;
		this.qbaseinsert=tmp.qbaseinsert;
		this.tnuminsert=tmp.tnuminsert;
		this.tbaseinsert=tmp.tbaseinsert;
		
		this.strand=new char[2];
		for(int i=0;i<this.strand.length;i++)
			this.strand[i]=tmp.strand[i];
		
		this.qsize=tmp.qsize;
		this.qstart=tmp.qstart;
		this.qend=tmp.qend;
		this.tsize=tmp.tsize;
		this.tstart=tmp.tstart;
		this.tend=tmp.tend;
		this.vgeneId=tmp.vgeneId;
		this.qname=tmp.qname;
		this.tname=tmp.tname;
		this.blockSizes=new int[tmp.blockSizes.length];
		this.qStarts=new int[tmp.qStarts.length];
		this.tStarts=new int[tmp.tStarts.length];
		for(int i=0;i<this.blockSizes.length;i++){
			this.blockSizes[i]=tmp.blockSizes[i];
			this.tStarts[i]=tmp.tStarts[i];
			this.qStarts[i]=tmp.qStarts[i];
		}
			
		this.contClass=tmp.contClass;
		this.contStart=tmp.contStart;
		this.contEnd=tmp.contEnd;
	}
	
	public String toString()
	{
		return "Est from gene "+vgeneId+", qstart,qend="+qstart+","+qend+" tstart,tend="+tstart+","+tend+" qname="+qname+" tname="+tname;
	}
	public String toPslString(){
		String pslString= this.matches+"\t"+this.mismatches+"\t"+this.repmatches+"\t"+this.ncount+"\t"+this.qnuminsert+"\t"+this.qbaseinsert+"\t"+this.tnuminsert+"\t"+this.tbaseinsert+"\t"+this.strand[0];
		if (this.strand[1]=='+'||this.strand[1]=='-') {
			pslString+=this.strand[1];
		}
		pslString+="\t"+this.qname+"\t"+this.qsize+"\t"+this.qstart+"\t"+this.qend+"\t"+this.tname+"\t"+this.tsize+"\t"+this.tstart+"\t"+this.tend+"\t"+this.blockSizes.length+"\t";
		for (int i = 0; i < this.blockSizes.length; i++) {
			pslString+=this.blockSizes[i]+",";
		}
		pslString+="\t";
		for (int i = 0; i < this.qStarts.length; i++) {
			pslString+=this.qStarts[i]+",";
		}
		pslString+="\t";
		for (int i = 0; i < this.tStarts.length; i++) {
			pslString+=this.tStarts[i]+",";
		}
		pslString+="\n";
		
		return pslString;
	}
	
	public void mergeWith(EstAlignment b,boolean ignoreStrand)throws Exception{
		//only changes target position stats
		if(!this.tname.equals(b.tname)){
			throw new Exception("Can't merge! Located on different targets.");
		}
		if(this.strand[0]!=b.strand[0]){
			if(ignoreStrand){
				System.err.println(this.qname+" was merged with a transcript on the other strand... this was ignored");
			}else{
				throw new Exception("Can't merge! Located on different strands.");
			}
		}
		//All OK, go on and merge
		this.tstart=this.tstart<b.tstart?this.tstart:b.tstart;
		this.tend=this.tend>b.tend?this.tend:b.tend;
		ArrayList<Integer> bs= new ArrayList<Integer>();
		ArrayList<Integer> ts= new ArrayList<Integer>();
		int start, size;
		for(int i=0,j=0;i<this.blockSizes.length||j<b.blockSizes.length;){
			if(i==this.tStarts.length||j==b.tStarts.length){
				if(i==this.tStarts.length){
					start=b.tStarts[j];
					size=b.blockSizes[j];
					j++;
				}else{
					start=this.tStarts[i];
					size=this.blockSizes[i];
					i++;
				}
			}else {
				if(this.tStarts[i]<b.tStarts[j]){
					start=this.tStarts[i];
					size=this.blockSizes[i];
					i++;
				}else{
					start=b.tStarts[j];
					size=b.blockSizes[j];
					j++;
				}
				while((j<b.tStarts.length&&b.tStarts[j]<start+size)||(i<this.tStarts.length&&this.tStarts[i]<start+size)){
					if(j<b.tStarts.length){
						for(;j<b.tStarts.length&&b.tStarts[j]<start+size;j++){
							if(b.tStarts[j]+b.blockSizes[j]>start+size){
								size=b.tStarts[j]+b.blockSizes[j]-start;
							}
						}
					}
					if(i<this.tStarts.length){
						for(;i<this.tStarts.length&&this.tStarts[i]<start+size;i++){
							if(this.tStarts[i]+this.blockSizes[i]>start+size){
								size=this.tStarts[i]+this.blockSizes[i]-start;
							}
						}
					}
				}
			}
			ts.add(start);
			bs.add(size);
		}
		this.blockSizes= new int[bs.size()];
		this.tStarts= new int[ts.size()];
		this.qStarts= new int[ts.size()];
		this.qsize=0;
		for(int i=0;i<bs.size();i++){
			this.blockSizes[i]=bs.get(i);
			this.tStarts[i]=ts.get(i);
			this.qStarts[i]=this.qsize;
			this.qsize+=this.blockSizes[i];
		}
	}
	
	public int compareTo(EstAlignment ea)
    {
		return this.tstart-ea.tstart;
//      int val = ((EstAlignment)o).tstart;
//      if (tstart < val)
//        return -1;
//      else if (tstart > val)
//        return 1;
//      else
//        return 0;
    }


	public double getIdentity() {
	//	return 1.0 - mismatches/( ( (double) matches + repmatches + mismatches) );
		return ((matches+repmatches)-mismatches-qnuminsert-tnuminsert);

	}
	public ArrayList<Feature> toJalviewSpliceFeatures(boolean prot){
		return toJalviewSpliceFeatures("",prot);
	}
	public ArrayList<Feature> toJalviewSpliceFeatures(String group,boolean prot){
		ArrayList<Feature> features=new ArrayList<Feature>();
		for (int start : qStarts) {
			if(start!=qStarts[0]){
				Feature tmp;
				if(prot){
					if(strand[0]=='+'){
						tmp=new Feature("BLAT generated splicesite",qname,"SpliceSite",group,-1,start/3,start/3);
					}else if(strand[0]=='-'){
						tmp=new Feature("BLAT generated splicesite",qname,"SpliceSite",group,-1,(qsize-start)/3,(qsize-start)/3);
					}else{
						tmp=null;
					}
				}else{
					if(strand[0]=='+'){
						tmp=new Feature("BLAT generated splicesite",qname,"SpliceSite",group,-1,start-1,start+1);
					}else if(strand[0]=='-'){
						tmp=new Feature("BLAT generated splicesite",qname,"SpliceSite",group,-1,qsize-start-1,qsize-start+1);
					}else{
						tmp=null;
					}
				}
				if(strand[1]=='+'||strand[1]=='-'){
					tmp=null;
					System.err.println("File not parsed... ???");
				}
				features.add(tmp);
			}
		}
		return features;
	}
	
	public String toFastacmdScript(){
		return toFastacmdScript("nr");
	}
	
	public String toFastacmdScript(String database){
		return toFastacmdScript(database, 0);
	}
	public String toFastacmdScript(String database, int n){
		return "fastacmd -d "+database+" -s "+tname+" -L "+(tstart-n<0?0:tstart-n)+","+(tend+n>tsize?tsize:tend+n);
	}
	
	public String toGFF(int offset){
		return toGFF(offset,"exon","BLAT");
	}
	public String toGFF(int offset, String feature){
		return toGFF(offset,feature,"BLAT");
	}
	public String toGFF(int offset,String feature,String source){
		String gff=tname+"\t"+source+"\tmRNA\t"+(tstart+offset)+"\t"+(tend+offset)+"\t"+((matches*100)/(qsize))+"\t"+strand[0]+"\t.\tID="+this.getQname()+";Name="+this.getQname();
		for(int i=0;i<tStarts.length;i++){
			gff+="\n"+tname+"\t"+source+"\t"+feature+"\t"+(tStarts[i]+offset)+"\t"+(tStarts[i]+blockSizes[i]+offset)+"\t"+((100*matches)/(qsize))+"\t"+strand[0]+"\t.\tParent="+this.getQname();
		}
		return gff;
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
	public String getQname(){
		return qname;
	}
	public int getTstart() {
		return tstart;
	}
	public int getType() {
		return 1;
	}
	public boolean overlaps(Alignment b) throws Exception{
		return overlapMethods.pslOverlap(this, b,true);
	}
}
	

