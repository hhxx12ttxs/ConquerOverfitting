package tools.blast;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Hashtable;

import tools.clustering.ClusterUtils;
import tools.clustering.DoubleLink;
import tools.clustering.LinkageCluster;
import tools.fasta.fastaParser;
import tools.utils.DoubleMatrix;

public class blastUtils {
	
	private static String sep= "\t";

	public static void main(String[] args)throws Exception{
		if(args.length>0){
			if(args[0].equals("topResult")){
				if(args.length==2){
					topResultByEvalue(new BufferedReader(new FileReader(args[1])),5);
				}else if(args.length==3){
					topResultByEvalue(new BufferedReader(new FileReader(args[1])),Integer.parseInt(args[2]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}if(args[0].equals("topResultPipe")&&args.length>0){
				if(args.length==1){
					topResultByEvalue(new BufferedReader(new InputStreamReader(System.in)),5);
				}else if(args.length==2){
					topResultByEvalue(new BufferedReader(new InputStreamReader(System.in)),Integer.parseInt(args[1]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("topResultNonOverlapping")&&args.length==2){
				topResultNonOverlappingByScore(args[1],5);
			}else if(args[0].equals("topResultNonOverlapping")&&args.length==3){
				topResultNonOverlappingByScore(args[1],Integer.parseInt(args[2]));
			}else if(args[0].equals("topUniq")){
				if(args.length==2){
					topUniqTnameResultByEvalue(args[1],5);
				}else if(args.length==3){
					topUniqTnameResultByEvalue(args[1],Integer.parseInt(args[2]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("topTagged")){
				if(args.length==2){
					extractTaggedResultByEvalue(args[1],0.01,5,1);
				}else if(args.length==5){
					extractTaggedResultByEvalue(args[1],Double.parseDouble(args[2]),Integer.parseInt(args[3]),Integer.parseInt(args[4]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("toDistanceMatrix")){
				if(args.length==4){
					toDistanceMatrix(args[1],args[2],args[3]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("compileMultiTopTaggedTable")||args[0].equals("cmttt")){
				if(args.length==2){
					compileMultiTopTaggedTable(args[1],4,null);
				}else if(args.length==3){
					compileMultiTopTaggedTable(args[1],Integer.parseInt(args[2]),null);
				}else if(args.length==4){
					compileMultiTopTaggedTable(args[1],Integer.parseInt(args[2]),args[3]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("toMinEvalueDistanceMatrix")){
				if(args.length==3){
					toMinEvalueDistanceMatrix(args[1], args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("simpleCluster")){
				if(args.length==2){
					simpleCluster(args[1]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("blastClust2Table")){
				if(args.length==2){
					blastClust2Table(args[1]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("sub")){
				if(args.length==3){
					subBoth(args[1], args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("subQ")){
				if(args.length==3){
					subQ(args[1], args[2],true);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("topMultFile")){
				if(args.length==3){
					topMultFile(args[1], Integer.parseInt(args[2]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("insertDesc")){
				if(args.length==3){
					insertDesc(args[1], args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("deleteOnTname")){
				if(args.length==3){
					deleteOnTname(args[1], args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("deleteOnGi")){
				if(args.length==3){
					deleteOnGi(args[1], args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("evalueCutoff")){
				if(args.length==3){
					evalueCutoff(args[1], Double.parseDouble(args[2]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("orthologs")){
				if(args.length==3){
					orthologs(args[1], args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("evalueCluster")){
				if(args.length==3){
					evalueCluster(args[1], Double.parseDouble(args[2]),new ArrayList<String>(),true);
				}else if(args.length==4){
					ArrayList<String> al= new ArrayList<String>();
					BufferedReader in= new BufferedReader(new FileReader(args[3]));
					for(String s=in.readLine();s!=null;s=in.readLine()){
						al.add(s);
					}
					evalueCluster(args[1], Double.parseDouble(args[2]), al, false);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("evalueHier")){
				if(args.length==4){
					evalueHier(args[1], Integer.parseInt(args[2]),Integer.parseInt(args[3]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("overlapCluster")){
				if(args.length==2){
					overlapCluster(args[1]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("getDoublets")){
				if(args.length==2){
					System.err.println("use BLAT instead");
//					getDoublets(args[1]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}
		}else{
			System.err.println(printHelp());
			System.exit(616);
		}
	}
	private static String printHelp(){
		String help="Usage: blastUtils <cmd> <input>\n";
		help+="where <cmd> is:\n";
		help+="topResult(Pipe) - extracts the n best hits for each query, if n is excluded it is set to 5. The blastFile should be in -m8 format.\n";
		help+="\t<input> = <blastFile> <n=5>\n";
		help+="topResultNonOverlapping - extracts the n best non-overlapping (in the query) hits for each query, if n is excluded it is set to 5. The blastFile should be in -m8 format. Hits are sorted by score\n";
		help+="\t<input> = <blastFile> <n=5>\n";
		help+="topUniq - extracts the n best uniq (uniq tNames) hits for each query, if n is excluded it is set to 5. The blastFile should be in -m8 format.\n";
		help+="\t<input> = <blastFile> <n=5>\n";
		help+="topMultFile - extracts the n best uniq (uniq tNames) hits for each query from a set of blastFiles. The blastFiles should be in -m8 format.\n";
		help+="\t<input> = <blastFileList> <n>\n";
		help+="topTagged - extracts the number of hits (E-value cutoff default=0.01) to a certain group of tagged (tag= ><tag>_<name>) transcripts among the N (default=5) best hits. Reports only those with at least Q (default=1) hits. Only handles input of either all parameters or none\n";
		help+="\t<input> = <blastFile> <max E-value=0.01> <N=5> <Q=1\n";
		help+="compileMultiTopTaggedTable - puts together a table based on the topTagged-files in the filelist... counts every entry with at least n hits. Specified limits can be supplied in a file on the format (tag<tab>limit), but then the general limit must be set\n";
		help+="\t<input> = <fileList> <n=4> <limitfile=null>\n";
		help+="toDistanceMatrix - creates an distance matrix from the BLAST-hits\n";
		help+="\t<input> = <nameFile (gi\tname) <faFile> <blastM8file>\n";
		help+="toMinEvalueDistanceMatrix - creates an distance matrix from the BLAST-hits where the distance is the negative minimum E-value between two data. All non-existent conections are set to -616\n";
		help+="\t<input> = <nameFile (gi\tname) <blastM8file>\n";
		help+="simpleCluster - clusters all transcripts with single linkage and 0.01 as maximal e-value\n";
		help+="\t<input> = <blastFile>\n";
		help+="sub - extracts the subset defined by the subsetfile. Both tname and qname has to be in the file\n";
		help+="\t<input> = <blastFile> <sub-file>\n";
		help+="subQ - extracts the subset of qnames defined by the subsetfile.\n";
		help+="\t<input> = <blastFile> <sub-file>\n";
		help+="insertDesc - Takes the descriptions for the target from the desc file (gi\\tdesc) and inserts them after the hit\n";
		help+="\t<input> = <blastFile> <desc-file>\n";
		help+="deleteOnTname - prints only those not present in the tname-file\n";
		help+="\t<input> = <blastFile> <tname-file>\n";
		help+="deleteOnGi - prints only those not present in the gi-file\n";
		help+="\t<input> = <blastFile> <gi-file>\n";
		help+="blastClust2Table - converts blastclust resultfile to a tabulated csv-file\n";
		help+="\t<input> = <blastClustFile>\n";
		help+="getDoublets - reads a blastFile and prints a list of identical transcripts\n";
		help+="\t<input> = <blastFile>\n";
		help+="evalueCutoff - only returns hits with an E-value below the cutoff\n";
		help+="\t<input> = <blastFile> <e-value cutoff\n";
		help+="ortholog - prints a list of potential orthologs for the queries in the first file. Prints the evalue for the top hit, and the sequence its hit along with the e-value of the BLAST search back on the first sequence\n";
		help+="\t<input> = <blastFile> <e-value cutoff\n";
		help+="evalueCluster - cluster sequences with single linkage on evalue with the evalue cutoff\n";
		help+="\t<input> = <blastFile> <evalue cutoff (0.0-10.0) <subsetFile, optional>\n";
		help+="evalueHier - clusters a blastfile hierachaly on the evalue with single linkage. All clusters larger than n are reclustered with a stricter cutoff. Starts at 1^p (where p is an integer) and decreases the cutoff for relevant links by a factor 10 for each step. \n";
		help+="\t<input> = <blastFile> <maximum cluster size N> <start value p>\n";
		help+="overlapCluster - prints the regions overlapped by hits\n";
		help+="\t<input> = <blastFile>\n";
		
		return help;
	}
	
	private static void overlapCluster(String blastFile) throws Exception{
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba,old;
		HashMap<String, ArrayList<blastM8Alignment>> regions= new HashMap<String, ArrayList<blastM8Alignment>>();
		ArrayList<blastM8Alignment> curList;
		for(int i=0;bp.hasMore();i++){
			if(i%10000==0){
				System.err.print("          "+i+"\r");
			}
			ba=bp.nextHit();
			if(regions.containsKey(ba.getTname())){
				curList=regions.get(ba.getTname());
				for(int j=curList.size()-1;j>=0;--j){
					old=curList.get(j);
					if(ba.overlapsNotStrandSpecific(old)){
						if(old.getTstart()<ba.getTstart()){
							ba.tstart=old.getTstart();
						}
						if(old.getTend()>ba.getTend()){
							ba.tend=old.getTend();
						}
						curList.remove(j);
					}
				}
				curList.add(ba);
			}else{
				curList= new ArrayList<blastM8Alignment>();
				curList.add(ba);
				regions.put(ba.getTname(), curList);
			}
		}
		
		//print
		for (String scaf : regions.keySet()) {
			curList=regions.get(scaf);
			for (blastM8Alignment cur : curList) {
				System.out.println(cur.getTname()+sep+cur.getTstart()+sep+cur.getTend());
			}
		}
	}
	
	
	private static void evalueHier(String blastFile,int N, int pow) throws Exception{
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		LinkageCluster<DoubleLink> lc= new LinkageCluster<DoubleLink>("c");
		for(int i=0;bp.hasMore();i++){
			if(i%10000==0){
				System.err.println("read: "+i+" kept "+ lc.getLinks().size()+" to a size of "+lc.size());
			}
			ba=bp.nextHit();
			if(ba.e_value<=Math.pow(10.0, pow)){
				lc.addLink(new DoubleLink(ba.qname,ba.tname,ba.e_value));
			}
		}
		System.err.println("Analyzing:\n"+lc.toString());
		ArrayList<LinkageCluster<DoubleLink>> toCluster= new ArrayList<LinkageCluster<DoubleLink>>();
		toCluster.add(lc);
		ArrayList<LinkageCluster<DoubleLink>> clustered= ClusterUtils.hierCluster(toCluster, N, pow, true);
		//print
		int cluster=0;
		for(LinkageCluster<DoubleLink> l : clustered){
			for (String s : l.getData()) {
				System.out.println(s+sep+cluster);
			}
			cluster++;
		}
	}
	
	private static void evalueCluster(String blastFile, double cutoff,ArrayList<String> al, boolean excludeInAL)throws Exception{
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		HashMap<String, Integer>seq2cluster = new HashMap<String, Integer>();
		HashMap<Integer, ArrayList<String>> cluster2seq = new HashMap<Integer, ArrayList<String>>();
		ArrayList<String> t1,t2;
		int cluster=0,c1,c2;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(ba.e_value<=cutoff && !ba.qname.equals(ba.tname)&&((excludeInAL&&!al.contains(ba.qname)&&!al.contains(ba.tname))||(!excludeInAL&&al.contains(ba.qname)&&al.contains(ba.tname)))){
				if(seq2cluster.containsKey(ba.qname)){
					c1=seq2cluster.get(ba.qname);
					if(seq2cluster.containsKey(ba.tname)){
						c2=seq2cluster.get(ba.tname);
						if(c1!=c2){
							//merge cluster c1 and c2, by adding c2 to c1
							t2=cluster2seq.get(c2);
							for(String s : t2){
								seq2cluster.put(s, c1);
							}
							cluster2seq.get(c1).addAll(t2);
						}
					}else{
						//add 2 to c1
						cluster2seq.get(c1).add(ba.tname);
						seq2cluster.put(ba.tname, c1);
					}
				}else if(seq2cluster.containsKey(ba.tname)){
					//add 1 to c2
					c2=seq2cluster.get(ba.tname);
					cluster2seq.get(c2).add(ba.qname);
					seq2cluster.put(ba.qname, c2);
				}else{
					//create new cluster
					t1= new ArrayList<String>();
					t1.add(ba.qname);
					t1.add(ba.tname);
					cluster2seq.put(cluster, t1);
					seq2cluster.put(ba.qname, cluster);
					seq2cluster.put(ba.tname, cluster);
					cluster++;
				}
			}
		}
		//print
		for(String s : seq2cluster.keySet()){
			System.out.println(s+sep+seq2cluster.get(s));
		}
	}

	private static void orthologs(String queryFile, String targetFile)throws Exception{
		blastM8Parser bp =new blastM8Parser(new BufferedReader(new FileReader(queryFile)));
		blastM8Alignment ba;
		HashMap<String, Double> max= new HashMap<String, Double>();
		HashMap<String, String> target= new HashMap<String, String>();
		HashMap<String, Double> qmax= new HashMap<String, Double>();
		System.err.println("parsing first file");
		//parse first file
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(max.containsKey(ba.qname)){
				if(ba.e_value<max.get(ba.qname)){
					max.put(ba.qname, ba.e_value);
					target.put(ba.qname, ba.tname);
				}
			}else{
				max.put(ba.qname, ba.e_value);
				target.put(ba.qname, ba.tname);
			}
		}
		System.err.println("parsing second file");
		//parse secondfile
		bp= new blastM8Parser(new BufferedReader(new FileReader(targetFile)));
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(target.containsKey(ba.tname)&&target.get(ba.tname).replace('|', '#').equals(ba.qname.replace('|', '#'))){
				if(qmax.containsKey(ba.tname)){
					if(ba.e_value<qmax.get(ba.tname)){
						qmax.put(ba.tname, ba.e_value);
					}
				}else{
					qmax.put(ba.tname, ba.e_value);
				}
			}
		}
		//print
		String out;
		for(String key : target.keySet()){
			out=key+"\t"+max.get(key)+"\t";
			if(qmax.containsKey(key)){
				out+=target.get(key)+"\t"+qmax.get(key);
			}else{
				out+="N/A\t";
			}
			System.out.println(out);
		}
	}
	
	private static void evalueCutoff(String blastFile, Double cutoff)throws Exception{
		blastM8Parser bp =new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba= bp.nextHit();
			if(ba.e_value<cutoff){
				System.out.println(ba.toString());
			}
		}
	}
	
	private static void subQ(String blastFile, String giFile, boolean inList)throws Exception{
		ArrayList<String> list= new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(giFile));
		for(String e = in.readLine();e!=null;e= in.readLine()){
			if(e.length()>0){
				list.add(e);
			}
		}
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(list.contains(ba.qname)==inList){
				System.out.println(ba.toString());
			}
		}
	}
	
	private static void deleteOnTname(String blastFile,String tnameFile)throws Exception{
		ArrayList<String> delete= new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(tnameFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			delete.add(s);
		}
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(!delete.contains(ba.tname)){
				System.out.println(ba.toString());
			}
		}
	}
	
	private static void deleteOnGi(String blastFile, String giFile)throws Exception{
		ArrayList<String> delete= new ArrayList<String>();
		BufferedReader in = new BufferedReader(new FileReader(giFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			delete.add(s);
		}
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(!delete.contains(ba.getTgi())){
				System.out.println(ba.toString());
			}
		}
	}
	
	private static void topMultFile(String fileList,int N)throws Exception{
		HashMap<String, ArrayList<blastM8Alignment>> top = new HashMap<String, ArrayList<blastM8Alignment>>();
		BufferedReader list= new BufferedReader(new FileReader(fileList));
		blastM8Alignment ba;
		ArrayList<blastM8Alignment> tmp;
		for(String s=list.readLine();s!=null;s=list.readLine()){
			try{
				blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(s)));
				for(;bp.hasMore();){
					ba=bp.nextHit();
					if(top.containsKey(ba.qname)){
						tmp=top.get(ba.qname);
						if(ba.e_value<tmp.get(0).e_value){
							tmp.add(0, ba);
						}else{
							int i=0;
							//find insert site
							for(;i<tmp.size();i++){
								if(ba.e_value<tmp.get(i).e_value){
									tmp.add(i,ba);
									break;
								}
							}
							//if last place
							if(i<N&&i==tmp.size()){
								tmp.add(ba);
							}
							//remove the unneeded hits
							while (tmp.size()>N) {
								tmp.remove(N);
							}
						}
						
					}else{
						top.put(ba.qname, new ArrayList<blastM8Alignment>());
						top.get(ba.qname).add(ba);
					}
				}
			}catch(FileNotFoundException f){
				System.err.println("no such file: "+ s);
				f.printStackTrace(System.err);
			}
		}
		for (String s : top.keySet()) {
			for (blastM8Alignment b : top.get(s)) {
				System.out.println(b);
			}
		}
	}
	
	private static void compileMultiTopTaggedTable(String fileList,int N,String limitfile)throws Exception{
		HashMap<String, HashMap<String, Integer>> data=new HashMap<String, HashMap<String,Integer>>();
		ArrayList<String> groups=new ArrayList<String>();
		HashMap<String, Integer> limits=new HashMap<String, Integer>();
		int n;
		if(limitfile!=null){
			BufferedReader tmp=new BufferedReader(new FileReader(limitfile));
			for(String s=tmp.readLine();s!=null;s=tmp.readLine()){
				String[] l=s.split("\t");
				if(l.length==2){
					limits.put(l[0], Integer.parseInt(l[1]));
				}
			}
		}
		BufferedReader in=new BufferedReader(new FileReader(fileList)),file;
		//read all files
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(s.length()>0){
				file=new BufferedReader(new FileReader(s));
				file.readLine();
				for(String s1=file.readLine();s1!=null;s1=file.readLine()){
					String[] l=s1.split("\t");
					if(l.length==3){
						//Define n
						if(limits.containsKey(l[1])){
							n=limits.get(l[1]);
							n=n<N?n:N;
						}else{
							n=N;
						}
						//test
						if(Integer.parseInt(l[2])>=n){
							if(!groups.contains(l[1])){
								groups.add(l[1]);
							}
							if(!data.containsKey(l[0])){
								data.put(l[0], new HashMap<String, Integer>());
							}
							if(data.get(l[0]).containsKey(l[1])){
								data.get(l[0]).put(l[1], data.get(l[0]).get(l[1])+1);
							}else{
								data.get(l[0]).put(l[1], 1);
							}
						}
					}
				}
			}
		}
		//print results
		System.out.print("id");
		for (String s : groups) {
			System.out.print("\t"+s);
		}
		System.out.print("\n");
		for (String id : data.keySet()) {
			System.out.print(id);
			HashMap<String, Integer> tmp=data.get(id);
			for (String group : groups) {
				if(tmp.containsKey(group)){
					System.out.print("\t"+tmp.get(group));
				}else{
					System.out.print("\t0");
				}
			}
			System.out.println();
		}
	}
	
	public static void insertDesc(String blastFile, String descFile)throws Exception{
		HashMap<String, ArrayList<blastM8Alignment>> hits =new HashMap<String, ArrayList<blastM8Alignment>>();
		blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(!hits.containsKey(ba.tname)){
				hits.put(ba.tname, new ArrayList<blastM8Alignment>());
			}
			hits.get(ba.tname).add(ba);
		}
		BufferedReader in=new BufferedReader(new FileReader(descFile));
		String[] l;
		for(String s=in.readLine();s!=null;s=in.readLine()){
			l= s.split("\t");
			if(hits.containsKey(l[0])){
				for (blastM8Alignment b : hits.get(l[0])) {
					System.out.println(b.toString()+"\t"+l[1]);
				}
				hits.put(l[0], new ArrayList<blastM8Alignment>());
			}
		}
		for (String key : hits.keySet()) {
			for (blastM8Alignment b : hits.get(key)) {
				System.out.println(b.toString()+"\tN/A");
			}
		}
	}
	
	public static void getDoublets(String blastFile)throws Exception{
		blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		Hashtable<String, String>pairs=new Hashtable<String, String>();
		for(;bp.hasMore();){
			blastM8Alignment ba=bp.nextHit();
			if(!ba.qname.equals(ba.tname)&&ba.identity==100.0){
				if(pairs.containsKey(ba.qname)){
					if(!ba.tname.equals(pairs.get(ba.qname))){
						System.out.println(ba.qname+"\t"+ba.tname);
						pairs.put(ba.qname, ba.tname);
						pairs.put(ba.tname, ba.qname);
					}
				}else if(pairs.containsKey(ba.tname)){
					if(!ba.qname.equals(pairs.get(ba.tname))){
						System.out.println(ba.qname+"\t"+ba.tname);
						pairs.put(ba.qname, ba.tname);
						pairs.put(ba.tname, ba.qname);
					}
				}else{
					System.out.println(ba.qname+"\t"+ba.tname);
					pairs.put(ba.qname, ba.tname);
					pairs.put(ba.tname, ba.qname);
				}
			}
		}
	}
	
	public static void blastClust2Table(String blastClustFile)throws Exception{
		BufferedReader in =new BufferedReader(new FileReader(blastClustFile));
		int i=0;
		System.out.println("id\tcluster");
		for(String s=in.readLine();s!=null;s=in.readLine(),i++){
			if(s.length()>0){
				String[] l=s.split(" ");
				for (String id : l) {
					System.out.println(id+"\t"+i);
				}
			}
		}
		in.close();
	}
	
	public static void subBoth(String blastFile,String subFile)throws Exception{
		ArrayList<String> subset=new ArrayList<String>();
		BufferedReader in=new BufferedReader(new FileReader(subFile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			subset.add(s);
		}
		blastM8Parser bp =new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(subset.contains(ba.qname)&&subset.contains(ba.tname)){
				System.out.println(ba.toString());
			}
		}
	}
	
	public static void extractTaggedResultByEvalue(String blastFile,final double E, final int N,final int Q)throws Exception{
		blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba=bp.nextHit();
		String cur=ba.qname;
		int n=1;
		ArrayList<String> found=new ArrayList<String>();
		Hashtable<String, Integer> ht;
		found.add(ba.tname);
		System.out.println("qName\tclusterNr\tNumber");
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(!ba.qname.equals(cur)){
				//print result
				ht=new Hashtable<String, Integer>();
				for (String tname : found) {
					String i=tname.split("_")[0];
					if(ht.containsKey(i)){
						ht.put(i, ht.get(i)+1);
					}else{
						ht.put(i, 1);
					}
				}
				for (String i : ht.keySet()) {
					if(ht.get(i).intValue()>=Q){
						System.out.println(cur+"\t"+i+"\t"+ht.get(i));
					}
				}
				//reset
				n=0;
				if(ba.qname.length()>50){
					System.err.println(ba);
				}
				cur=ba.qname;
				found=new ArrayList<String>();
			}
			if(n<N&&!found.contains(ba.tname)){
				n++;
				found.add(ba.tname);
			}
		}
	}
	
	public static void topResultNonOverlappingByScore(String blastFile,final int N)throws Exception{
		blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba,baTmp,baTmp2;
		String cur="";
		ArrayList<blastM8Alignment> curHits= new ArrayList<blastM8Alignment>();
		Comparator<blastM8Alignment> scoreSort= new blastM8ScoreRevComp();
		int start1,start2,end1,end2;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if(ba.qname.equals(cur)){
				curHits.add(ba);
			}else{
				//print
				for(int i=0;i<N&&curHits.size()>0;++i){
					Collections.sort(curHits, scoreSort);
					baTmp=curHits.get(0);
					curHits.remove(0);
					System.out.println(baTmp);
					if(baTmp.qstart<baTmp.qend){
						start1=baTmp.qstart;
						end1=baTmp.qend;
					}else{
						start1=baTmp.qend;
						end1=baTmp.qstart;
					}
					//remove overlapping
					for(int j=curHits.size()-1;j>=0;--j){
						baTmp2=curHits.get(j);
						if(baTmp2.qstart<baTmp2.qend){
							start2=baTmp2.qstart;
							end2=baTmp2.qend;
						}else{
							start2=baTmp2.qend;
							end2=baTmp2.qstart;
						}
						if((start1>=start2&&start1<end2)||(start2>=start1&&start2<end1)){
							curHits.remove(j);
						}
					}
				}
				//restart
				cur=ba.qname;
				curHits=new ArrayList<blastM8Alignment>();
				curHits.add(ba);
			}
		}
		//print last
		for(int i=0;i<N&&curHits.size()>0;i++){
			Collections.sort(curHits, scoreSort);
			baTmp=curHits.get(0);
			System.out.println(baTmp);
			//remove overlapping
			for(int j=curHits.size()-1;j>=0;j--){
				baTmp2=curHits.get(j);
				if((baTmp.qstart>=baTmp2.qstart&&baTmp.qstart<baTmp2.qend)||(baTmp2.qstart>=baTmp.qstart&&baTmp2.qstart<baTmp.qend)){
					curHits.remove(j);
				}
			}
		}
	}
	
	public static void topResultByEvalue(BufferedReader blast, final int N) throws Exception{
		blastM8Parser bp=new blastM8Parser(blast);
		blastM8Alignment ba;
		String cur="";
		int n=0;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if (!ba.qname.equals(cur)) {
				n=0;
				cur=ba.qname;
			}
			if(n<N){
				n++;
				System.out.println(ba.toString());
			}
		}
	}
	public static void topUniqTnameResultByEvalue(String blastFile, final int N) throws Exception{
		blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		blastM8Alignment ba;
		String cur="";
		int n=0;
		ArrayList<String> printed=new ArrayList<String>();
		for(;bp.hasMore();){
			ba=bp.nextHit();
			if (!ba.qname.equals(cur)) {
				n=0;
				cur=ba.qname;
				printed=new ArrayList<String>();
			}
			if(n<N){
				if(!printed.contains(ba.tname)){
					n++;
					printed.add(ba.tname);
					System.out.println(ba.toString());
				}
			}
		}
	}
	
	public static void toMinEvalueDistanceMatrix(String nameFile,String blastFile)throws Exception{
		Hashtable<String, String[]> giMap=new Hashtable<String, String[]>(); //Value= String[]{'id (name to put in output)','nr (index to put it in)'}
		ArrayList<String> ids =new ArrayList<String>();
		BufferedReader in=new BufferedReader(new FileReader(nameFile));
		String s=in.readLine();
		for(int i=0;s!=null;s=in.readLine()){
			String[] l=s.split("\t");
			if(l.length==2){
				String[] tmp=new String[3];
				if (giMap.containsKey(l[1])) {
					tmp=giMap.get(l[1]);
				}else{
					tmp=new String[3];
					tmp[1]=i+"";
					ids.add(l[0]);
					i++;
				}
				tmp[0]=l[0];
				giMap.put(l[1], tmp);
			}
		}
		in.close();
		blastM8Parser bp =new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		DoubleMatrix dm=new DoubleMatrix(ids.size(),ids.size());
		for(int i=0;i<dm.getHeight();i++){
			for(int j=0;j<dm.getWidth();j++){
				dm.set(i, j, -616);
			}
		}
		//set distance as the negative minimum e-value between two data
		blastM8Alignment ba;
		for(;bp.hasMore();){
			ba=bp.nextHit();
			int i=Integer.parseInt(giMap.get(ba.qname)[1]);
			int j=Integer.parseInt(giMap.get(ba.tname)[1]);
			if(dm.get(i, j)<-ba.e_value){
				dm.set(i, j, -ba.e_value);
			}
		}
		//printing
		System.out.print("id_"+ids.get(0));
		for (int i = 1; i < ids.size(); i++) {
			System.out.print("\tid_"+ids.get(i));
		}
		System.out.println("");
		for (int i = 0; i < dm.getHeight(); i++) {
			System.out.print(dm.get(i, 0));
			for (int j = 1; j < dm.getWidth(); j++) {
				System.out.print("\t"+dm.get(i, j));
			}
			System.out.println("");
		}
	}
	/**
	 * 
	 * @param nameFile gi_nummer\tid
	 * @param faFile
	 * @param blastFile
	 * @throws Exception
	 */
	public static void toDistanceMatrix(String nameFile,String faFile,String blastFile)throws Exception{
		Hashtable<String, String[]> giMap=new Hashtable<String, String[]>(); //Value= String[]{'id (name to put in output)','nr (index to put it in)','length of sequence'}
		ArrayList<String> ids =new ArrayList<String>();
		BufferedReader in=new BufferedReader(new FileReader(nameFile));
		String s=in.readLine();
		for(int i=0;s!=null;s=in.readLine()){
			String[] l=s.split("\t");
			if(l.length==2){
				String[] tmp=new String[3];
				if (giMap.containsKey(l[1])) {
					tmp=giMap.get(l[1]);
				}else{
					tmp=new String[3];
					tmp[1]=i+"";
					ids.add(l[0]);
					i++;
				}
				tmp[0]=l[0];
				giMap.put(l[1], tmp);
			}
		}
		in.close();
		fastaParser fa=new fastaParser(new BufferedReader(new FileReader(faFile)));
		for(;fa.hasNext();){
			s=fa.nextHit().substring(1).split(" ")[0];
			if(giMap.containsKey(s)){
				String[] tmp=giMap.get(s);
				tmp[2]=fa.getSeq().length()+"";
			}
		}
		blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(blastFile)));
		DoubleMatrix dm=new DoubleMatrix(ids.size(),ids.size());
		dm.setToZero();
		//calculating distance
		for(;bp.hasMore();){
			blastM8Alignment ba=bp.nextHit();
			int qlength = Integer.parseInt(giMap.get(ba.qname)[2]);
			int tlength= Integer.parseInt(giMap.get(ba.tname)[2]);
			int i=Integer.parseInt(giMap.get(ba.qname)[1]);
			int j=Integer.parseInt(giMap.get(ba.tname)[1]);
			dm.add(i, j, calculateDistanceFromBLAST(ba, qlength, tlength));
		}
		//printing
		System.out.print("id_"+ids.get(0));
		for (int i = 1; i < ids.size(); i++) {
			System.out.print("\tid_"+ids.get(i));
		}
		System.out.println("");
		for (int i = 0; i < dm.getHeight(); i++) {
			System.out.print(dm.get(i, 0));
			for (int j = 1; j < dm.getWidth(); j++) {
				System.out.print("\t"+dm.get(i, j));
			}
			System.out.println("");
		}
//		System.out.println("\n"+dm.toString());
	}
	private static double calculateDistanceFromBLAST(blastM8Alignment ba,int qlength,int tlength){
		return (((double)(qlength+tlength))/(qlength*tlength*(ba.alignment_length+ba.gap_openings)))*ba.score;
//		return ba.score/(ba.alignment_length+ba.gap_openings);
	}
	
	public static void simpleCluster(String inFile)throws Exception{
		Hashtable<String, String> transcripts=new Hashtable<String, String>(); //<id, clusterid>
		Hashtable<String, ArrayList<String>> clusters= new Hashtable<String, ArrayList<String>>(); //<clusterid, list with all data>
		blastM8Parser bp= new blastM8Parser(new BufferedReader(new FileReader(inFile)));
		blastM8Alignment ba;
		//Cluster
		for(int clusterid=0;bp.hasMore();){
			ba=bp.nextHit();
			if(ba.e_value<=0.01 && !(ba.qname.equals(ba.tname))){
				if (transcripts.containsKey(ba.qname)) {
					if (transcripts.containsKey(ba.tname)) { //merge clusters
						String qClust=transcripts.get(ba.qname), tClust=transcripts.get(ba.tname);
						if(!qClust.equals(tClust)){ //check if merge is needed
							ArrayList<String> tList=clusters.get(tClust), qList=clusters.get(qClust);
							for (String s : tList) {
								qList.add(s);
								transcripts.put(s, qClust);
							}
							clusters.put(qClust, qList);
							clusters.remove(tClust);
						}
					} else { //add target transcript to query transcript cluster
						String curClust=transcripts.get(ba.qname);
						ArrayList<String> tmp=clusters.get(curClust);
						tmp.add(ba.tname);
						clusters.put(curClust, tmp);
						transcripts.put(ba.tname, curClust);
					}
				}else if(transcripts.containsKey(ba.tname)){ //add query transcript to target transcript cluster
					String curClust=transcripts.get(ba.tname);
					ArrayList<String> tmp=clusters.get(curClust);
					tmp.add(ba.qname);
					clusters.put(curClust, tmp);
					transcripts.put(ba.qname, curClust);
				}else{ //create new cluster
					ArrayList<String> tmp=new ArrayList<String>();
					tmp.add(ba.qname);
					tmp.add(ba.tname);
					clusters.put(clusterid+"", tmp);
					transcripts.put(ba.qname, clusterid+"");
					transcripts.put(ba.tname, clusterid+"");
					clusterid++;
				}
			}
		}
		//print
		for (String id : transcripts.keySet()) {
			System.out.println(id+"\t"+transcripts.get(id));
		}
	}
}

