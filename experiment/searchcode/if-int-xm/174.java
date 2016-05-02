package tools.genomeMatrix;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;


//import net.sf.javaml.clustering.Clusterer;
//import net.sf.javaml.clustering.IterativeKMeans;
//import net.sf.javaml.clustering.KMeans;
//import net.sf.javaml.clustering.evaluation.SumOfSquaredErrors;
//import net.sf.javaml.core.Dataset;
//import net.sf.javaml.core.DefaultDataset;
//import net.sf.javaml.core.DenseInstance;
//import net.sf.javaml.core.Instance;
//import net.sf.javaml.distance.EuclideanDistance;



import tools.gff.gffLine;
import weka.clusterers.ClusterEvaluation;
import weka.clusterers.XMeans;
import weka.core.Attribute;
import weka.core.EuclideanDistance;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

public class genomeMatrixUtils {

	final static String sep="\t";
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		if(args.length>0){
			if(args[0].equals("zgapGFF")&&args.length>5){
				zgapGFF(args[1],args[2],args[3],Integer.parseInt(args[4]),args[5]);
			}else if(args[0].equals("gmToStrainBED")&&args.length>3){
				gmToStrainBED(args[1], args[2], args[3]);
			}else if(args[0].equals("gapStat")&&args.length>3){
				gapStat(args[1], args[2], args[3]);
			}else if(args[0].equals("geneSeq")&&args.length>4){
				geneSeq(args[1], args[2], args[3],args[4]);
			}else if(args[0].equals("geneClust")&&args.length>3){
				geneClust(args[1], args[2], args[3]);
			}else if(args[0].equals("gapClust1")&&args.length>3){
				gapClust1(args[1], args[2],args[3]);
			}else if(args[0].equals("clust1OverlapGFF3")&&args.length>2){
				clust1OverlapGFF3(args[1], args[2]);
			}else if(args[0].equals("geneStat")&&args.length>2){
				geneStat(args[1], args[2]);
			}else{
				System.err.println(printHelp());
				System.exit(616);
			}
		}else{
			System.err.println(printHelp());
			System.exit(616);
		}
	}
	private static String printHelp(){
		String help="Usage: genomeMatrixUtils <cmd> <input>\n";
		help+="where <cmd> is:\n";
		help+="zgapGFF - takes the genomeMatrix file from 1001G, a position file with the chr, start and stop of the Z-gaps in the three first positions. Returns a GFF file for each strain with Z-covered regions and the SNPs in the flanking regions. Discards the first line in the position file as a header.\n";
		help+="\t<input> = <genomeMatrixFile> <positionfile> <strain file> <flanking size> <outprefix>\n";
		help+="gmToStrainBED - takes the genomeMatrix file from 1001G and a file with the strains. Returns a GFF file for each strain with Z-covered regions and the SNPs. Discards the first line in the position file as a header.\n";
		help+="\t<input> = <genomeMatrixFile> <strain file> <outprefix>\n";
		help+="geneStat - takes a tab-separated file where the first columns are id, chr, start, end and counts how many N, D and Z's the region contain\n";
		help+="\t<input> = <genomeMatrixFile> <posFile> \n";
		help+="geneSeq - takes a tab-separated file where the first columns are id, chr, start, end and generates a fasta file with sequences for each strain given by the strain file\n";
		help+="\t<input> = <genomeMatrixFile> <posFile> <strain-file> <outPrefix>\n";
		help+="geneClust - takes a tab-separated file where the first columns are id, chr, start, end and clusters the strains for each section\n";
		help+="\t<input> = <genomeMatrixFile> <posFile> <strain-file>\n";
		help+="gapStat - takes the result of grep [ZDN] and prints stats of it\n";
		help+="\t<input> = <genomeMatrixFile> <strain file> <outprefix>\n";
		help+="gapClust1 - takes the result of grep [ZDN] and clusters strains to a gap if they are 90% present (or only missing one position for lenghts between 1 and 10). Remove these and make new overlap clusters... iterate\n";
		help+="\t<input> = <genomeMatrixFile> <strain file> <outprefix>\n";
		help+="clust1OverlapGFF3 - searches for overlap between a cluster file and a gff3 file\n";
		help+="\t<input> = <clusterFile> <GFF3 file>\n";
		
		
		return help;
	}
	
	private static void geneClust(String genomeMatrixFile, String posFile, String strainFile)throws Exception{
		HashMap<String, geneQue> geneQues= new HashMap<String, geneQue>();
		ArrayList<String> strains= new ArrayList<String>();
		
		BufferedReader in= new BufferedReader(new FileReader(posFile));
		System.err.println("Reading genes from "+posFile);
		for(String s=in.readLine();s!=null;s=in.readLine()){
			final String[] l=s.split("\t");
			final gene g= new gene(l[0], l[1], Integer.parseInt(l[2]), Integer.parseInt(l[3]));
			if(!geneQues.containsKey(g.getChr())){
				geneQues.put(g.getChr(), new geneQue());
			}
			geneQues.get(g.getChr()).add(g);
		}
		
		in= new BufferedReader(new FileReader(strainFile));
		System.err.println("Reading strains from "+strainFile);
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				strains.add(s);
			}
		}
		
		final int nrOfStrains= strains.size();
		final int nrOfColumns= nrOfStrains+3;
		
		String curChr="";
		String[] l;
		geneQue curQue= new geneQue();
		ArrayList<gene> curGenes= new ArrayList<gene>();
		HashMap<String, double[][]> curMatrices= new HashMap<String, double[][]>();
		HashMap<String, int[][]> curInCommonGaps= new HashMap<String, int[][]>();
		double[][] curMatrix;
		int[][] curInCommonGap;
//		BufferedWriter out;
		int curPos;
		in=  new BufferedReader(new FileReader(genomeMatrixFile));
		final HashSet<String> gapNuc= new HashSet<String>();
		gapNuc.add("Z");
		gapNuc.add("D");
		gapNuc.add("N");
//		Dataset data;
//		Dataset[] clusters;
		Instances instances;
		FastVector attributeList= new FastVector();
		for (String strain : strains) {
			attributeList.addElement(new Attribute(strain));
		}
		XMeans xm = new XMeans();
		instances= new Instances("test", attributeList, nrOfStrains);
		instances.add(new Instance(1, new double[nrOfStrains]));
		xm.buildClusterer(instances);
		xm.setMaxNumClusters(5);
		ClusterEvaluation eval;
		
		int linenr=0;
		
		System.err.println("Parsing the genome matrix from "+ genomeMatrixFile);
		System.out.print("id"+sep+"chr"+sep+"start"+sep+"end");
		for (String strain : strains) {
			System.out.print(sep+strain);
		}
		System.out.println();
		for(String s= in.readLine();s!= null; s=in.readLine(),linenr++){
			if(linenr%100000==0){
				System.err.println(linenr);
			}
			if (s.length()>0 && s.trim().charAt(0)!='#') {
				l=s.split("\t");
				curPos=Integer.parseInt(l[1]);
				if(!l[0].equals(curChr)){
					System.err.println("Starting new chromosome: "+l[0]);
					//start on a new chromosome
					if(geneQues.containsKey(l[0])){
						curQue=geneQues.get(l[0]);
					}else{
						curQue=new geneQue();
					}
					//flush genes from previous chromosome
					for (gene g : curGenes) {
						curMatrix= curMatrices.get(g.getId());
//						out=new BufferedWriter(new FileWriter(outPrefix+"_"+g.getId()+".fa"));
						
						instances = new Instances(g.getId(), attributeList, nrOfStrains);
						for(int j=0;j<nrOfStrains;j++){
							instances.add(new Instance(1,curMatrix[j]));
						}
//						System.err.println("Clustering...");
						xm.buildClusterer(instances);
						xm.setMaxNumClusters(5);
						xm.setDistanceF(new EuclideanDistance(instances));
						eval= new ClusterEvaluation();
						eval.setClusterer(xm);
						eval.evaluateClusterer(instances);
//						System.out.println(eval.clusterResultsToString());
						
						System.out.print(g.getId()+sep+g.getChr()+sep+g.getStart()+sep+g.getEnd());
						for (double cluster : eval.getClusterAssignments()) {
							System.out.print(sep+(int)cluster);
						}
						System.out.println();
//						for(int i=0;i<strains.size();i++){
//							out.write(">"+strains.get(i)+"\n"+curSeq[i]+"\n");
//						}
//						out.close();
					}
					//update the last variables
					curGenes= new ArrayList<gene>();
					curMatrices= new HashMap<String, double[][]>();
					curChr=l[0];
				}
				//print and drop all genes with end points smaller than the current position
				for(int i=curGenes.size()-1;i>=0;i--){
					if(curGenes.get(i).getEnd()<curPos){
						gene g =curGenes.remove(i);
//						System.err.println("Flushing gene: "+g.getId());
						curMatrix= curMatrices.remove(g.getId());
						
//						data=new DefaultDataset();
//						System.err.println("Creating Matrix...");
						instances = new Instances(g.getId(), attributeList, nrOfStrains);
						for(int j=0;j<nrOfStrains;j++){
							instances.add(new Instance(1,curMatrix[j]));
						}
//						System.err.println("Clustering...");
						xm.buildClusterer(instances);
						xm.setMaxNumClusters(5);
						xm.setDistanceF(new EuclideanDistance(instances));
						eval= new ClusterEvaluation();
						eval.setClusterer(xm);
						eval.evaluateClusterer(instances);
//						System.out.println(eval.clusterResultsToString());
						
						System.out.print(g.getId()+sep+g.getChr()+sep+g.getStart()+sep+g.getEnd());
						for (double cluster : eval.getClusterAssignments()) {
							System.out.print(sep+(int)cluster);
						}
						System.out.println();
						
//						int clusternr=0;
//						for (Dataset cluster : clusters) {
//							clusternr++;
//							for (Instance specie : cluster) {
//								System.out.println(g.getId()+"\t"+clusternr+"\t"+(Integer)specie.classValue());
//							}
//						}
						
						
						
//						out=new BufferedWriter(new FileWriter(outPrefix+"_"+g.getId()+".fa"));
//						for(int j=0;j<nrOfStrains;j++){
//							for(int k=0;k<j;k++){
//								System.out.println(g.getId()+"\t"+strains.get(j)+"\t"+strains.get(k)+"\t"+curMatrix[j][k]);
//							}
//						}
//						out.close();
					}
				}
				//get all genes with starting points lower than the current position from the que
				for(;curQue.getNextStart()<curPos;){
					gene g=curQue.getNext(); 
					curGenes.add(g);
					curMatrices.put(g.getId(), new double[nrOfStrains][nrOfStrains]);
					curInCommonGaps.put(g.getId(), new int[nrOfStrains][nrOfStrains]);
				}
				//add the nucleotides to the genes
				for (gene g : curGenes) {
					curMatrix= curMatrices.get(g.getId());
					curInCommonGap=curInCommonGaps.get(g.getId());
					for(int i=3;i<nrOfColumns;i++){
						final int x=i-3;
//						curMatrix[x][x]+=1;
						for(int j=3;j<i;j++){
							final int y=j-3;
							//Count differences
							if(gapNuc.contains(l[i]) && gapNuc.contains(l[j])){
								if(curInCommonGap[x][y]!=3){
									curInCommonGap[x][y]=3;
//									curMatrix[x][y]+=1;
//									curMatrix[y][x]+=1;
								}
							}else if(gapNuc.contains(l[i])){
								if(curInCommonGap[x][y]!=2){
									curInCommonGap[x][y]=2;
//									curMatrix[x][y]+=1;
//									curMatrix[y][x]+=1;
								}
							}else if(gapNuc.contains(l[j])){
								if(curInCommonGap[x][y]!=1){
									curInCommonGap[x][y]=1;
//									curMatrix[x][y]+=1;
//									curMatrix[y][x]+=1;
								}
							}else if(l[i].charAt(0)!=l[j].charAt(0)){
								curInCommonGap[x][y]=0;
								curMatrix[x][y]+=1;
								curMatrix[y][x]+=1;
							}else{
								curInCommonGap[x][y]=0;
							}
							
//							if(l[i].charAt(0)!=l[j].charAt(0)){
//								if(gapNuc.contains(l[i]) && gapNuc.contains(l[j])){
//									//Common gap
//									if(!curInCommonGap[x][y]){
//										
//									}
//								}else{
//									
//								}
//							}
//							
//							
//							if(l[i].charAt(0)==l[j].charAt(0)){
//								curMatrix[i-3][j-3]+=1;
//								curMatrix[j-3][i-3]+=1;
//							}else if(gapNuc.contains(l[i]) && gapNuc.contains(l[j])){
//								curMatrix[i-3][j-3]+=1;
//								curMatrix[j-3][i-3]+=1;
//							}
						}
					}
				}
			}
		}
		for (gene g : curGenes) {
//			System.err.println("Flushing gene: "+g.getId());
			curMatrix= curMatrices.remove(g.getId());
			
//			data=new DefaultDataset();
//			System.err.println("Creating Matrix...");
			instances = new Instances(g.getId(), attributeList, nrOfStrains);
			for(int j=0;j<nrOfStrains;j++){
				instances.add(new Instance(1,curMatrix[j]));
			}
//			System.err.println("Clustering...");
			xm.buildClusterer(instances);
			xm.setMaxNumClusters(5);
			xm.setDistanceF(new EuclideanDistance(instances));
			eval= new ClusterEvaluation();
			eval.setClusterer(xm);
			eval.evaluateClusterer(instances);
//			System.out.println(eval.clusterResultsToString());
			
			System.out.print(g.getId()+sep+g.getChr()+sep+g.getStart()+sep+g.getEnd());
			for (double cluster : eval.getClusterAssignments()) {
				System.out.print(sep+(int)cluster);
			}
			System.out.println();
			
			curMatrix= curMatrices.get(g.getId());
//			out=new BufferedWriter(new FileWriter(outPrefix+"_"+g.getId()+".fa"));
//			for(int i=0;i<strains.size();i++){
//				out.write(">"+strains.get(i)+"\n"+curSeq[i]+"\n");
//			}
//			out.close();
		}
	}
	
	private static void geneSeq(String genomeMatrixFile, String posFile, String strainFile, String outPrefix)throws Exception{
		HashMap<String, geneQue> geneQues= new HashMap<String, geneQue>();
		ArrayList<String> strains= new ArrayList<String>();
		
		BufferedReader in= new BufferedReader(new FileReader(posFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			final String[] l=s.split("\t");
			final gene g= new gene(l[0], l[1], Integer.parseInt(l[2]), Integer.parseInt(l[3]));
			if(!geneQues.containsKey(g.getChr())){
				geneQues.put(g.getChr(), new geneQue());
			}
			geneQues.get(g.getChr()).add(g);
		}
		
		in= new BufferedReader(new FileReader(strainFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				strains.add(s);
			}
		}
		
		final int nrOfStrains= strains.size();
		final int nrOfColumns= nrOfStrains+3;
		
		String curChr="";
		String[] l;
		geneQue curQue= new geneQue();
		ArrayList<gene> curGenes= new ArrayList<gene>();
		HashMap<String, String[]> curSeqs= new HashMap<String, String[]>();
		String[] curSeq;
		BufferedWriter out;
		int curPos;
		in=  new BufferedReader(new FileReader(genomeMatrixFile));
		for(String s= in.readLine();s!= null; s=in.readLine()){
			if (s.length()>0 && s.trim().charAt(0)!='#') {
				l=s.split("\t");
				curPos=Integer.parseInt(l[1]);
				if(!l[0].equals(curChr)){
					//start on a new chromosome
					if(geneQues.containsKey(l[0])){
						curQue=geneQues.get(l[0]);
					}else{
						curQue=new geneQue();
					}
					//flush genes from previous chromosome
					for (gene g : curGenes) {
						curSeq= curSeqs.get(g.getId());
						out=new BufferedWriter(new FileWriter(outPrefix+"_"+g.getId()+".fa"));
						for(int i=0;i<strains.size();i++){
							out.write(">"+strains.get(i)+"\n"+curSeq[i]+"\n");
						}
						out.close();
					}
					//update the last variables
					curGenes= new ArrayList<gene>();
					curSeqs=new HashMap<String, String[]>();
					curChr=l[0];
				}
				//print and drop all genes with end points smaller than the current position
				for(int i=curGenes.size()-1;i>=0;i--){
					if(curGenes.get(i).getEnd()<curPos){
						gene g =curGenes.remove(i);
						curSeq= curSeqs.remove(g.getId());
						out=new BufferedWriter(new FileWriter(outPrefix+"_"+g.getId()+".fa"));
						for(int j=0;j<nrOfStrains;j++){
							out.write(">"+strains.get(j)+"\n"+curSeq[j]+"\n");
						}
						out.close();
					}
				}
				//get all genes with starting points lower than the current position from the que
				for(;curQue.getNextStart()<curPos;){
					gene g=curQue.getNext(); 
					curGenes.add(g);
					curSeqs.put(g.getId(), new String[nrOfStrains]);
				}
				//add the nucleotides to the genes
				for (gene g : curGenes) {
					curSeq= curSeqs.get(g.getId());
					for(int i=3;i<nrOfColumns;i++){
						curSeq[i-3]+=l[i];
					}
				}
			}
		}
		for (gene g : curGenes) {
			curSeq= curSeqs.get(g.getId());
			out=new BufferedWriter(new FileWriter(outPrefix+"_"+g.getId()+".fa"));
			for(int i=0;i<strains.size();i++){
				out.write(">"+strains.get(i)+"\n"+curSeq[i]+"\n");
			}
			out.close();
		}
	}
	
	private static void geneStat(String genomeMatrixFile, String posFile)throws Exception{
		HashMap<String, geneQue> geneQues= new HashMap<String, geneQue>();
		
		BufferedReader in= new BufferedReader(new FileReader(posFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			final String[] l=s.split("\t");
			final gene g= new gene(l[0], l[1], Integer.parseInt(l[2]), Integer.parseInt(l[3]));
			if(!geneQues.containsKey(g.getChr())){
				geneQues.put(g.getChr(), new geneQue());
			}
			geneQues.get(g.getChr()).add(g);
		}
		String curChr="";
		String[] l;
		final String[] nucs=new String[]{"Z","D","N"};
		geneQue curQue= new geneQue();
		ArrayList<gene> curGenes=new ArrayList<gene>();
		in= new BufferedReader(new FileReader(genomeMatrixFile));
		int curPos;
		for(String s= in.readLine();s!= null; s=in.readLine()){
			if (s.length()>0 && s.trim().charAt(0)!='#') {
				l=s.split("\t");
				curPos=Integer.parseInt(l[1]);
				if(!l[0].equals(curChr)){
					//start on a new chromosome
					if(geneQues.containsKey(l[0])){
						curQue=geneQues.get(l[0]);
					}else{
						curQue=new geneQue();
					}
					//flush genes from previous chromosome
					for (gene g : curGenes) {
						g.print(nucs, 80);
					}
					//update the last variables
					curGenes= new ArrayList<gene>();
					curChr=l[0];
				}
				//print and drop all genes with end points smaller than the current position
				for(int i=curGenes.size()-1;i>=0;i--){
					if(curGenes.get(i).getEnd()<curPos){
						curGenes.remove(i).print(nucs, 80);
					}
				}
				//get all genes with starting points lower than the current position from the que
				for(;curQue.getNextStart()<curPos;){
					curGenes.add(curQue.getNext());
				}
				//add the nucleotides to the genes
				for (gene g : curGenes) {
					for(int i=3;i<83;i++){
						g.add(l[i]+(i-2));
					}
				}
			}
		}
		for (gene g : curGenes) {
			g.print(nucs, 80);
		}
	}
	
	private static void clust1OverlapGFF3(String clusterFile, String gff3File)throws Exception{
		HashMap<String, ArrayList<gffLine>> gffs = new HashMap<String, ArrayList<gffLine>>();
		BufferedReader in= new BufferedReader(new FileReader(gff3File));
		gffLine tmp;
		for(String s=in.readLine();s!=null;s=in.readLine()){
			tmp=new gffLine(s);
			tmp.setAttribute(tmp.getAttribute().split(";")[0].split("=")[1]);
			final String key=tmp.getChr().replaceAll("Chr", "");
			if(!gffs.containsKey(key)){
				gffs.put(key, new ArrayList<gffLine>());
			}
			gffs.get(key).add(tmp);
		}
		in= new BufferedReader(new FileReader(clusterFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			final String []l= s.split("\t");
			final int start=Integer.parseInt(l[2]);
			final int end = Integer.parseInt(l[3]);
			if(gffs.containsKey(l[1])){
				for (gffLine gff : gffs.get(l[1])) {
					if((gff.getStart()>=start && gff.getStart()<end)|| (start>=gff.getStart()&&start<gff.getEnd())){
						System.out.println(l[0]+"\t"+gff.getAttribute());
					}
				}
			}
		}
	}
	
	private static void gapClust1(String genomeMatrixFile, String strainFile, String outPrefix)throws Exception{
		final int maxGap=1;
		
		BufferedReader in= new BufferedReader(new FileReader(strainFile));
		ArrayList<String> strains= new ArrayList<String>();
		
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				strains.add(s);
			}
		}
		final int nrOfStrains=strains.size();
		final int nrOfCols=nrOfStrains+3;
		in= new BufferedReader(new FileReader(genomeMatrixFile));
		String l[];
		String curChr=null;
		int lastPos=-1,nr=1;
		int[] starts= new int[nrOfStrains];
		genomeGapSet ggs=new genomeGapSet(nrOfStrains);
		BufferedWriter out= new BufferedWriter(new FileWriter(outPrefix+"_clusters.csv"));
		BufferedWriter hard= new BufferedWriter(new FileWriter(outPrefix+"_hard.csv"));
		
		for(String s= in.readLine();s!= null; s=in.readLine()){
			l=s.split("\t");
			if (s.length()>0 && s.trim().charAt(0)!='#' && l.length>=nrOfCols) {
//				l=s.split("\t");
//				if(l.length<nrOfCols){
//					System.err.println("Strange line in the genomeMatrix:\n"+s);
//				}
				final int pos=Integer.parseInt(l[1]);
				if(pos%100000==0){
					System.err.println(pos);
				}
				if(curChr==null){
					//initialize
					curChr=l[0];
					lastPos=pos;
					ggs=new genomeGapSet(nrOfStrains);
					for(int i=0;i<nrOfStrains;i++){
						starts[i]=-1;
					}
				}
				//Check if we are in a new gap
				if(pos>lastPos+maxGap ||!curChr.equals(l[0])){
					for(int i=0;i<nrOfStrains;i++){
						if(starts[i]!=-1){
							ggs.add(new genomeGap(i, starts[i], lastPos+1));
						}
						//reset
						starts[i]=-1;
					}
					curChr=l[0];
					nr=ggs.cluster(out, hard, curChr, nr);
					ggs=new genomeGapSet(nrOfStrains);
				}
				//gather data
				for(int i=0;i<nrOfStrains;i++){
					final char nuc=l[i+3].charAt(0);
					if(nuc=='N'||nuc=='Z'||nuc=='D'){
						if(starts[i]==-1){
							starts[i]=pos;
						}
					}else if(starts[i]!=-1){
						ggs.add(new genomeGap(i, starts[i], pos));
						starts[i]=-1;
					}
				}
				lastPos=pos; //update where data was last gathered
			}else if(l.length<nrOfCols){
				System.err.println("Strange line in the genomeMatrix:\n"+s);
			}
		}
		//print last
		for(int i=0;i<nrOfStrains;i++){
			if(starts[i]!=-1){
				ggs.add(new genomeGap(i, starts[i], lastPos+1));
			}
		}
		nr=ggs.cluster(out, hard, curChr, nr);
		System.out.println("Made "+nr+" clusters");
		
		//close writers
		out.close();
		hard.close();
	}
	
	private static void gapStat(String genomeMatrixFile, String strainFile, String outPrefix)throws Exception{
		final int maxGap=1;
		
		BufferedReader in= new BufferedReader(new FileReader(strainFile));
		ArrayList<String> strains= new ArrayList<String>();
		
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				strains.add(s);
			}
		}
		final int nrOfStrains=strains.size();
		final int nrOfCols=nrOfStrains+3;
		in= new BufferedReader(new FileReader(genomeMatrixFile));
		String l[];
		String curChr=null;
		int lastPos=-1,start=-1,nr=1;
		boolean[] lastWasGap=new boolean[nrOfStrains];
		int[] nrOfGaps= new int[nrOfStrains];
		int[] conc= new int[nrOfStrains];
		
		BufferedWriter concOut= new BufferedWriter(new FileWriter(outPrefix+"_conc.csv"));
		BufferedWriter countOut= new BufferedWriter(new FileWriter(outPrefix+"_count.csv"));
		concOut.write("#id"+sep+"chr"+sep+"start"+sep+"end");
		countOut.write("#id"+sep+"chr"+sep+"start"+sep+"end");
		for(int i=0;i<nrOfStrains;i++){
			concOut.write(sep+strains.get(i));
			countOut.write(sep+strains.get(i));
		}
		concOut.write("\n");
		countOut.write("\n");
		for(String s= in.readLine();s!= null; s=in.readLine()){
			if (s.length()>0 && s.trim().charAt(0)!='#') {
				l=s.split("\t");
				if(l.length<nrOfCols){
					System.err.println("Strange line in the genomeMatrix:\n"+s);
				}
				final int pos=Integer.parseInt(l[1]);
				if(pos%100000==0){
					System.err.println(pos);
				}
				if(curChr==null){
					curChr=l[0];
					lastPos=pos;
					start=pos;
					for(int i=0;i<nrOfStrains;i++){
						lastWasGap[i]=false;
						nrOfGaps[i]=0;
						conc[i]=0;
					}
				}
				if(pos>lastPos+maxGap ||!curChr.equals(l[0])){
					//print and reset
					String id=(nr+"");
					while(id.length()<10){
						id="0"+id;
					}
					id="G"+id;
					nr++;
					concOut.write(id+sep+curChr+sep+start+sep+(lastPos+1));
					countOut.write(id+sep+curChr+sep+start+sep+(lastPos+1));
					for(int i=0;i<nrOfStrains;i++){
						concOut.write(sep+conc[i]);
						countOut.write(sep+nrOfGaps[i]);
						lastWasGap[i]=false;
						nrOfGaps[i]=0;
						conc[i]=0;
					}
					concOut.write("\n");
					countOut.write("\n");
					start=pos;
					curChr=l[0];
				}
				//gather data
				lastPos=pos; //update where data was last gathered
				for(int i=0;i<nrOfStrains;i++){
					final char nuc=l[i+3].charAt(0);
					if(nuc=='N'||nuc=='Z'||nuc=='D'){
						conc[i]++;
						if(!lastWasGap[i]){
							lastWasGap[i]=true;
							nrOfGaps[i]++;
						}
					}else if(lastWasGap[i]){
						lastWasGap[i]=false;
					}
				}
			}
		}
		//print last gap
		String id=(nr+"");
		while(id.length()<10){
			id="0"+id;
		}
		id="G"+id;
		concOut.write(id+sep+curChr+sep+start+sep+(lastPos+1));
		countOut.write(id+sep+curChr+sep+start+sep+(lastPos+1));
		for(int i=0;i<nrOfStrains;i++){
			concOut.write(sep+conc[i]);
			countOut.write(sep+nrOfGaps[i]);
		}
		concOut.write("\n");
		countOut.write("\n");
		
		//close writers
		concOut.close();
		countOut.close();
	}
	
	private static void gmToStrainBED(String genomeMatrixFile,String strainFile,String outPrefix)throws Exception{
		ArrayList<BufferedWriter> outs= new ArrayList<BufferedWriter>();
		ArrayList<gffLine> gffLines= new ArrayList<gffLine>();
		BufferedReader strainIn= new BufferedReader(new FileReader(strainFile));
//		HashMap<String, String> coding= new HashMap<String, String>();
//		coding.put("T", "SO:1000010");
//		coding.put("C", "SO:1000013");
//		coding.put("G", "SO:1000015");
//		coding.put("A", "SO:1000016");
//		coding.put("Z", "SO:1000079");
//		coding.put("D", "SO:0000159");
//		coding.put("N", "SO:1000177");
//		coding.put("-", "SO:0000046");
		
		//read strain file, prepare data structure and initialize output files
		for (String s= strainIn.readLine();s!=null;s= strainIn.readLine() ){
			if(s.length()>0){
				BufferedWriter out=new BufferedWriter(new FileWriter(outPrefix+"_"+s+".bed"));
				out.write("track name="+s+"\n");
				outs.add(out);
				gffLine empty= new gffLine("",s,"",1,1,"1",'+','.',"Name="+s);
				empty.setFlag(""); //empty if already printed, Z or D if it should be continued
				gffLines.add(empty);
			}
		}
		final int nrOfCols=outs.size()+3;
		BufferedReader gmIn= new BufferedReader(new FileReader(genomeMatrixFile));
		String[] k;
		gffLine cur;
		String curChr="";
		for(String s= gmIn.readLine();s!= null; s=gmIn.readLine()){
			if (s.length()>0 && s.trim().charAt(0)!='#') {
				k=s.split("\t");
				if(k.length<nrOfCols){
					System.err.println("Strange line in the genomeMatrix:\n"+s);
				}
				final int pos=Integer.parseInt(k[1]);
				if(pos%100000==0){
					System.err.println(pos);
//					System.err.println(chr+"\t"+start+"\t"+end);
				}
				if(curChr.length()==0){
					//initialize chromosome
					curChr=k[0];
				}
				if(!curChr.equals(k[0])){
					//flush
					for (int i=0;i<outs.size();i++){
						cur=gffLines.get(i);
						BufferedWriter out=outs.get(i);
						if(cur.getFlag().length()>0){
							out.write(cur.toBed()+"\n");
						}
						cur.setFlag("");
					}
				}
				//print
				final char ref=k[2].charAt(0);
				for(int i=3; i<nrOfCols;i++){
					final char specie=k[i].charAt(0);
					cur= gffLines.get(i-3);
					if(ref==specie&&ref!='Z'&&ref!='D'&&ref!='N'){
						//handle unchanged nucleotides
						if(cur.getFlag().length()>0){
							//if at the end of a Z, D or N-stretch... print
							outs.get(i-3).write(cur.toBed()+"\n");
							//reset the flag
							cur.setFlag("");
						}
					}else{
						//discrepancy
						if(specie=='Z'||specie=='D'||specie=='N'){
							if(cur.getFlag().length()==0){
								//start new instance
								cur.setChr(k[0]);
								cur.setStart(pos);
								cur.setEnd(pos);
								cur.setFeature(specie+"");
								cur.setFlag(specie+"");
							}else if (cur.getFlag().equals(specie+"")){
								//extend old instance
								cur.setEnd(pos);
							}else {
								//print old instance and start a new
								outs.get(i-3).write(cur.toBed()+"\n");
								cur.setChr(k[0]);
								cur.setStart(pos);
								cur.setEnd(pos);
								cur.setFeature(specie+"");
								cur.setFlag(specie+"");
							}
						}else{
							//print single SNP
							if(cur.getFlag().length()>0){
								outs.get(i-3).write(cur.toBed()+"\n");
							}
							cur.setChr(k[0]);
							cur.setStart(pos);
							cur.setEnd(pos);
							cur.setFeature(specie+"");
							cur.setFlag("");
							outs.get(i-3).write(cur.toBed()+"\n");
						}
					}
				}
			}
		}	
	}
	
	private static void zgapGFF(String genomeMatrixFile,String positionFile, String strainFile,int flankSize,String outPrefix) throws Exception{
		ArrayList<BufferedWriter> outs= new ArrayList<BufferedWriter>();
		ArrayList<gffLine> gffLines= new ArrayList<gffLine>();
		HashMap<String, String> coding= new HashMap<String, String>();
		coding.put("T", "SO:1000010");
		coding.put("C", "SO:1000013");
		coding.put("G", "SO:1000015");
		coding.put("A", "SO:1000016");
		coding.put("Z", "SO:1000079");
		coding.put("D", "SO:0000159");
		coding.put("N", "SO:1000177");
		coding.put("-", "SO:0000046");
		
		BufferedReader strainIn= new BufferedReader(new FileReader(strainFile));
		
		for (String s= strainIn.readLine();s!=null;s= strainIn.readLine() ){
			if(s.length()>0){
				outs.add(new BufferedWriter(new FileWriter(outPrefix+"_"+s+".gff")));
				gffLine empty= new gffLine("",s,"",1,1,"1",'.','.',"Name:"+s);
				empty.setFlag(""); //empty if already printed, Z or D if it should be continued
				gffLines.add(empty);
			}
		}
		final int nrOfCols=outs.size()+3;
		BufferedReader positionIn = new BufferedReader(new FileReader(positionFile));
		positionIn.readLine();
		String[] l=positionIn.readLine().split("[ \t]");
		System.err.println("firstgap:");
		String chr= l[0];
		int start=Integer.parseInt(l[1])-flankSize;
		int end= Integer.parseInt(l[2])+flankSize;
		while(Integer.parseInt(l[1])-flankSize<end&&chr.equals(l[0])){
			end= Integer.parseInt(l[2])+flankSize;
			l=positionIn.readLine().split("[ \t]");
		}
		System.err.println(chr+"\t"+start+"\t"+end);
		BufferedReader gmIn= new BufferedReader(new FileReader(genomeMatrixFile));
		String[] k;
		gffLine cur;
		for(String s= gmIn.readLine();s!= null; s=gmIn.readLine()){
			if (s.length()>0 && s.trim().charAt(0)!='#') {
				k=s.split("\t");
				if(k.length<nrOfCols){
					System.err.println("Strange line in the genomeMatrix:\n"+s);
				}
				final int pos=Integer.parseInt(k[1]);
				if(pos%1000==0){
					System.err.println(pos);
//					System.err.println(chr+"\t"+start+"\t"+end);
				}
				if(chr.equals(k[0])&& start<=pos&& pos<end){
					//print
					final char ref=k[2].charAt(0);
					for(int i=3; i<nrOfCols;i++){
						final char specie=k[i].charAt(0);
						if(ref==specie&&ref!='Z'&&ref!='D'){
							//handle unchanged nucleotides
						}else{
							//discrepancy
							cur= gffLines.get(i-3);
							if(specie=='Z'||specie=='D'||specie=='N'){
								if(cur.getFlag().length()==0){
									//start new instance
									cur.setChr(k[0]);
									cur.setStart(pos);
									cur.setEnd(pos);
									cur.setFeature(coding.get(specie+""));
									cur.setFlag(specie+"");
								}else if (cur.getFlag().equals(specie+"")){
									//extend old instance
									cur.setEnd(pos);
								}else {
									//print old instance and start a new
									outs.get(i-3).write(cur+"\n");
									cur.setChr(k[0]);
									cur.setStart(pos);
									cur.setEnd(pos);
									cur.setFeature(coding.get(specie+""));
									cur.setFlag(specie+"");
								}
							}else{
								//print single SNP
								if(cur.getFlag().length()>0){
									outs.get(i-3).write(cur+"\n");
								}
								cur.setChr(k[0]);
								cur.setStart(pos);
								cur.setEnd(pos);
								cur.setFeature(coding.get(specie+""));
								cur.setFlag("");
								outs.get(i-3).write(cur+"\n");
							}
						}
					}
				}else if(!chr.equals(k[0])){ //this should not really occur?????
					//continue to next Z-gap
					System.err.println("entering code that should not be accessed");
					while(!k[0].equals(l[0])){
						l=positionIn.readLine().split("[ \t]");
					}
					chr= l[0];
					start=Integer.parseInt(l[1])-flankSize;
					end= Integer.parseInt(l[2])+flankSize;
					while(Integer.parseInt(l[1])-flankSize<end&&chr.equals(l[0])){
						end= Integer.parseInt(l[2])+flankSize;
						l=positionIn.readLine().split("[ \t]");
					}
					System.err.println(chr+"\t"+start+"\t"+end);
					if(chr.equals(k[0])&& start<=pos&& pos<end){
						//print
						final char ref=k[2].charAt(0);
						for(int i=3; i<nrOfCols;i++){
							final char specie=k[i].charAt(0);
							if(ref==specie&&ref!='Z'&&ref!='D'&&ref!='N'){
								//handle unchanged nucleotides
							}else{
								//discrepancy
								cur= gffLines.get(i-3);
								if(specie=='Z'||specie=='D'||specie=='N'){
									if(cur.getFlag().length()==0){
										//start new instance
										cur.setChr(k[0]);
										cur.setStart(pos);
										cur.setEnd(pos);
										cur.setFeature(coding.get(specie+""));
										cur.setFlag(specie+"");
									}else if (cur.getFlag().equals(specie+"")){
										//extend old instance
										cur.setEnd(pos);
									}else {
										//print old instance and start a new
										outs.get(i-3).write(cur+"\n");
										cur.setChr(k[0]);
										cur.setStart(pos);
										cur.setEnd(pos);
										cur.setFeature(coding.get(specie+""));
										cur.setFlag(specie+"");
									}
								}else{
									//print single SNP
									if(cur.getFlag().length()>0){
										outs.get(i-3).write(cur+"\n");
									}
									cur.setChr(k[0]);
									cur.setStart(pos);
									cur.setEnd(pos);
									cur.setFeature(coding.get(specie+""));
									cur.setFlag("");
									outs.get(i-3).write(cur+"\n");
								}
							}
						}
					}
				}else if(pos>end){
					//continue to next Z-gap
					System.err.println("nextgap:");
					chr= l[0];
					start=Integer.parseInt(l[1])-flankSize;
					end= Integer.parseInt(l[2])+flankSize;
					while(Integer.parseInt(l[1])<end&&chr.equals(l[0])){
						l=positionIn.readLine().split("[ \t]");
						end= Integer.parseInt(l[2])+flankSize;
					}
					System.err.println(chr+"\t"+start+"\t"+end);
					//if the new gap is on the next chromosome... forward the position file to that
					while(!chr.equals(k[0])){
						s=gmIn.readLine();
						if (s.length()>0 && s.trim().charAt(0)!='#') {
							k=s.split("\t");
						}
					}
					if(chr.equals(k[0])&& start<=pos&& pos<end){
						//print
						final char ref=k[2].charAt(0);
						for(int i=3; i<nrOfCols;i++){
							final char specie=k[i].charAt(0);
							if(ref==specie&&ref!='Z'&&ref!='D'&&ref!='N'){
								//handle unchanged nucleotides
							}else{
								//discrepancy
								cur= gffLines.get(i-3);
								if(specie=='Z'||specie=='D'||specie=='N'){
									if(cur.getFlag().length()==0){
										//start new instance
										cur.setChr(k[0]);
										cur.setStart(pos);
										cur.setEnd(pos);
										cur.setFeature(coding.get(specie+""));
										cur.setFlag(specie+"");
									}else if (cur.getFlag().equals(specie+"")){
										//extend old instance
										cur.setEnd(pos);
									}else {
										//print old instance and start a new
										outs.get(i-3).write(cur+"\n");
										cur.setChr(k[0]);
										cur.setStart(pos);
										cur.setEnd(pos);
										cur.setFeature(coding.get(specie+""));
										cur.setFlag(specie+"");
									}
								}else{
									//print single SNP
									if(cur.getFlag().length()>0){
										outs.get(i-3).write(cur+"\n");
									}
									cur.setChr(k[0]);
									cur.setStart(pos);
									cur.setEnd(pos);
									cur.setFeature(coding.get(specie+""));
									cur.setFlag("");
									outs.get(i-3).write(cur+"\n");
								}
							}
						}
					}
				}
			}
		}
		for (int i=0;i<outs.size();i++){
			cur=gffLines.get(i);
			BufferedWriter out=outs.get(i);
			if(cur.getFlag().length()>0){
				out.write(cur+"\n");
			}
			out.close();
		}
	}
}

