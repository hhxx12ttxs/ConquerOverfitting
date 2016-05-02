package tools.hhsearch;

import tools.DistanceMatrix.DistanceObject;

public class hhsAlignment {

	private String query,hit;
	private int no,cols,qstart,qend,tstart,tend,tlength;
	private double prob,evalue,pvalue,score,ss;
	
	public hhsAlignment(){
		
	}
	public hhsAlignment(hhsAlignment h){
		this.query=h.query;
		this.hit=h.hit;
		this.no=h.no;
		this.cols=h.cols;
		this.qstart=h.qstart;
		this.qend=h.qend;
		this.tstart=h.tstart;
		this.tend=h.tend;
		this.tlength=h.tlength;
		this.prob=h.prob;
		this.evalue=h.evalue;
		this.pvalue=h.pvalue;
		this.score=h.score;
		this.ss=h.ss;
	}
	
	public hhsAlignment(String query,String hhs_result_line)throws Exception{
		this.query=query;
		String[] l=hhs_result_line.trim().split(" +");
		no= Integer.parseInt(l[0]);
		int effectiveLength=l.length;
		if(l[effectiveLength-1].startsWith("(")){
			//ok file format
			tlength=Integer.parseInt(l[effectiveLength-1].substring(1,l[effectiveLength-1].length()-1));
			tend=Integer.parseInt(l[effectiveLength-2].split("-")[1]);
		}else{
			//the two last columns are merged... deal with it
			tlength=Integer.parseInt(l[effectiveLength-1].substring(l[effectiveLength-1].indexOf('(')+1,l[effectiveLength-1].length()-1));
			tend=Integer.parseInt(l[effectiveLength-1].split("-")[1].substring(0, l[effectiveLength-1].split("-")[1].indexOf('(')));
			effectiveLength++;
//			System.err.println("crazy file format has been dealt with");
		}
		tstart=Integer.parseInt(l[effectiveLength-2].split("-")[0]);
		qend=Integer.parseInt(l[effectiveLength-3].split("-")[1]);
		qstart=Integer.parseInt(l[effectiveLength-3].split("-")[0]);
		cols=Integer.parseInt(l[effectiveLength-4]);
		ss=Double.parseDouble(l[effectiveLength-5]);
		score=Double.parseDouble(l[effectiveLength-6]);
		pvalue=Double.parseDouble(l[effectiveLength-7]);
		evalue=Double.parseDouble(l[effectiveLength-8]);
		prob=Double.parseDouble(l[effectiveLength-9]);
//		hit=l[2];
		hit=hhs_result_line.substring(hhs_result_line.indexOf(l[1]),hhs_result_line.indexOf(" "+l[effectiveLength-9])).trim();

	}
	
	public int overlap(hhsAlignment ha){
		return this.overlap(ha.qstart,ha.qend);
	}
	
	public int overlap(int tqstart, int tqend){
		boolean tstartInside=tqstart>=qstart&&tqstart<=qend;
		boolean tendInside=tqend>=qstart&&tqend<=qend;
		if(tstartInside&&tendInside){
			//whole other inside
			return tqend-tqstart+1;
		}else if(tstartInside){
			return qend-tqstart+1;
		}else if(tendInside){
			return tqend-qstart+1;
		}else if(tqstart<=qstart&&tqend>=qend){
			return qend-qstart+1;
		}else{
			return 0;
		}
	}
	
	/**
	 * 
	 * @param ha
	 * @return true if ha and this overlaps to more than 50% of the length of the shorter
	 */
	public boolean overlap50(hhsAlignment ha){
//		System.out.println(this.toString()+"\n"+ha.toString());
//		System.out.println(this.overlap(ha)+"\t"+((ha.qend-ha.qstart)<(this.qend-this.qstart)?(ha.qend-ha.qstart):(this.qend-this.qstart))+"\n");
		if(ha.qend-ha.qstart==0||this.qend-this.qstart==0){
			return this.overlap(ha)==1;
		}else{
			return (double)this.overlap(ha)/((double)(ha.qend-ha.qstart)<(this.qend-this.qstart)?(ha.qend-ha.qstart):(this.qend-this.qstart))>=0.5;
		}
	}
	
	public String header(){
		return "query\tNo\tHit\tProb\tE-value\tP-value\tScore\tSS\tCols\tqstart\tqend\ttstart\tend\tlength";
	}
	public String toString(){
		return query+"\t"+no+"\t"+hit+"\t"+prob+"\t"+evalue+"\t"+pvalue+"\t"+score+"\t"+ss+"\t"+cols+"\t"+qstart+"\t"+qend+"\t"+tstart+"\t"+tend+"\t"+tlength;
	}
	public DistanceObject toDistanceObject(){
		return new DistanceObject(query,hit,score+ss);
	}
	
	public int getCols() {
		return cols;
	}
	public void setCols(int cols) {
		this.cols = cols;
	}
	public double getEvalue() {
		return evalue;
	}
	public void setEvalue(double evalue) {
		this.evalue = evalue;
	}
	public String getHit() {
		return hit;
	}
	public void setHit(String hit) {
		this.hit = hit;
	}
	public int getNo() {
		return no;
	}
	public void setNo(int no) {
		this.no = no;
	}
	public double getProb() {
		return prob;
	}
	public void setProb(double prob) {
		this.prob = prob;
	}
	public double getPvalue() {
		return pvalue;
	}
	public void setPvalue(double pvalue) {
		this.pvalue = pvalue;
	}
	public int getQend() {
		return qend;
	}
	public void setQend(int qend) {
		this.qend = qend;
	}
	public int getQstart() {
		return qstart;
	}
	public void setQstart(int qstart) {
		this.qstart = qstart;
	}
	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public double getScore() {
		return score;
	}
	public void setScore(double score) {
		this.score = score;
	}
	public double getSs() {
		return ss;
	}
	public void setSs(double ss) {
		this.ss = ss;
	}
	public int getTend() {
		return tend;
	}
	public void setTend(int tend) {
		this.tend = tend;
	}
	public int getTlength() {
		return tlength;
	}
	public void setTlength(int tlength) {
		this.tlength = tlength;
	}
	public int getTstart() {
		return tstart;
	}
	public void setTstart(int tstart) {
		this.tstart = tstart;
	}
	
	
}

