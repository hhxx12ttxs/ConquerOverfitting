package tools.blat;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;

import tools.blast.blastM8Alignment;
import tools.blast.blastM8Parser;
import tools.fasta.FastaSeq;
import tools.fasta.fastaParser;
import tools.overlap.overlapMethods;

public class pslUtils {

	private static String sep="\t";
	/**
	 * @param args
	 */
	public static void main(String[] args) throws Exception{
		if (args.length==0) {
			System.err.println(printHelp());
			System.exit(616);
		} else {
			if (args[0].equals("split")) {
				if (args.length==4) {
					split(args[1], Integer.parseInt(args[2]), args[3]);
				} else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if (args[0].equals("shortIntron")) {
				if (args.length==3) {
					shortIntron(args[1], Integer.parseInt(args[2]));
				} else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if (args[0].equals("exonCount")) {
				if (args.length==3) {
					exonCount(args[1], Integer.parseInt(args[2]));
				} else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if (args[0].equals("intronLengthHist")) {
				if (args.length==3) {
					intronLengthHist(args[1], Integer.parseInt(args[2]));
				} else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("getDoublets")){
				if (args.length==2) {
					getDoublets(args[1],1);
				}else if(args.length==3){
					getDoublets(args[1], Double.parseDouble(args[2]));
				} else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("clusterOverlap")){
				if (args.length==2) {
					clusterOverlap(args[1]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("splitOnTname")){
				if (args.length==3) {
					splitOnTname(args[1],args[2]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("sub")){
				if (args.length==3) {
					sub(args[1],args[2],true);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("antisub")){
				if (args.length==3) {
					sub(args[1],args[2],false);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("generateFastacmdScript")){
				if (args.length==4) {
					generateFastacmdScript(args[1],Integer.parseInt(args[2]),args[3]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("overlap")){
				if (args.length==2) {
					overlap(args[1]);
				}else if(args.length==4){
					overlap(args[1],args[2],Integer.parseInt(args[3]));
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("overlapKnownGeneSelf")){
				if (args.length==2) {
					overlapKnownGeneSelf(args[1]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("overlap2cluster")){
				if (args.length==2) {
					overlap2cluster(args[1]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("utrOverlap")){
				if (args.length==4) {
					utrOverlap(args[1],args[2],Integer.parseInt(args[3]),true);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("premRNAOverlap")){
				if (args.length==3) {
					premRNAOverlap(args[1],args[2]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("overlapPsl")){
				if (args.length==6) {
					boolean sameStrand=true;
					if(args[3].equals("sense")){
						sameStrand=true;
					}else if(args[3].equals("antisense")){
						sameStrand=false;
					}else{
						System.err.println(printHelp());
						System.exit(616);
					}
					boolean exon=true;
					if(args[4].equals("exon")){
						exon=true;
					}else if(args[4].equals("intron")){
						exon=false;
					}else{
						System.err.println(printHelp());
						System.exit(616);
					}
					boolean complete=true;
					if(args[5].equals("complete")){
						complete=true;
					}else if(args[5].equals("partial")){
						complete=false;
					}else{
						System.err.println(printHelp());
						System.exit(616);
					}
					overlapPsl(args[1],args[2],sameStrand,exon,complete);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("mergeCluster2psl")){
				if (args.length==3) {
					mergeCluster2psl(args[1],args[2]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("makeUnique")){
				if (args.length==2) {
					makeUnique(args[1]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("estLinkage")){
				if (args.length==4) {
					estLinkage(args[1],args[2],args[3]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("ucsc2psl")){
				if (args.length==3) {
					if(args[2].equals("prot")){
						ucsc2psl(args[1],true);
					}else if(args[2].equals("all")){
						ucsc2psl(args[1],false);
					}else{
						System.err.println(printHelp());
						System.exit(616);
					}
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("countSSinAlignment")){
				if (args.length==5) {
					countSSinAlignment(args[1],args[2],args[3],args[4]);
				}else {
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("toGFF")){
				if (args.length==3) {
					toGff(args[1],0,args[2]);
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("removeQsmall")){
				if (args.length==3) {
					removeQsmall(args[1],Double.parseDouble(args[2]));
				}else{
					System.err.println(printHelp());
					System.exit(616);
				}
			}else if(args[0].equals("another method")){
				
			}else {
				System.err.println(printHelp());
				System.exit(616);
			}
		}
		
		System.exit(0);
	}
	public static String printHelp(){
		String help="This program contains different methods to manipulate psl-files\n";
		help+="split - cuts a psl-file into smaller pieces\n";
		help+="\tpslUtils split <original psl-file> <nr of entries in outfiles> <outPrefix>\n";
		help+="getDoublets - prints a list of matches with matches/qsize>limit\n";
		help+="\tpslUtils getDoublets <psl-file> <limit=1.0>\n";
		help+="clusterOverlap - takes a file from overlap.jar (transcript1\\toverlapped_by_transcript2) and makes clusters\n";
		help+="\tpslUtils clusterOverlap <overlap-file>\n";
		help+="toGFF - converts a psl-file to a gff-file\n";
		help+="\ttoGFF <pslFile> <source>\n";
		help+="sub - prints hits with qname specified in subFile\n";
		help+="\tsub <pslFile> <subfile>\n";
		help+="antisub - prints hits with qname not present in subFile\n";
		help+="\tantisub <pslFile> <subfile>\n";
		help+="generateFastacmdScript - generates a fastacmd script which gets the target segment and n nucleotides around it (if possible)\n";
		help+="\tgenerateFastacmdScript <pslFile> <n> <BLAST database>\n";
		help+="overlap - checks for overlap in the given file or to another file (psl or blast m8)\n";
		help+="\toverlap <pslFile> <optional: <BLAST or pslFile> <type(1:psl,2;blast m8)>>\n";
		help+="overlapKnownGeneSelf - checks for overlap in the given file\n";
		help+="\toverlap <pslFile>\n";
		help+="overlapPsl - checks for overlap in the given file or to another psl file\n";
		help+="\toverlapPsl <pslFile> <pslFileB> <sense/antisense> <exon/intron> <partial/complete>>\n";
		help+="utrOverlap - checks if transcripts in the first file only overlaps the UTR of the transcripts in the second file\n";
		help+="\tutrOverlap <pslFile> <pslFileB> <UTRsize>>\n";
		help+="overlap2cluster - converts an overlapfile to a list (id\\tclusternr)\n";
		help+="\toverlap2cluster <overlapFile>\n";
		help+="mergeCluster2psl - takes a cluster list (id\\tclusternr) and a pslfile with all clustred entries and returns a psl file with clusters\n";
		help+="\tmergeCluster2psl <ClusterFile> <pslFile>\n";
		help+="makeUnique - takes an psl file and add a numerical postfix to doublet qnames\n";
		help+="\tmakeUnique <pslFile>\n";
		help+="countSSinAlignment - counts the number of splice sites for each entry in the pslfile in each bin (position given in fafile-coordinates) defined of the binfile (comma-separated line)\n";
		help+="\tcountSSinAlignment <pslFile> <(aligned) fafile> <binfile> <outPrefix>\n";
		help+="splitOnTname - splits a psl file with regard to tname and prints one file for each beginning with the prefix\n";
		help+="\tsplitOnTname <pslFile> <outPrefix>\n";
		help+="ucsc2psl - converts UCSC's gene table format to a crippled psl file that is useable in overlap analysis\n";
		help+="\tucsc2psl <ucsc file> <all= all entries, prot= only entries with a ProteinID>\n";
		help+="estLinkage - prints a list of A and B transcripts that doesn't overlap, but are linked by an est\n";
		help+="\testLinkage <A B overlap file> <A est overlap file> <B est overlap file>\n";
		help+="premRNAOverlap - checks if the A file overlaps the B file according to the pre-mRNA criteria in Majds pek (overlaps a whole intron and two adjactent exons\n";
		help+="\tpremRNAOverlap <A psl file> <B psl file> \n";
		help+="shortIntron - prints the exon sizes of the transcripts with exons shorter than the limit\n";
		help+="\tshortIntron <psl file> <limit> \n";
		help+="intronLengthHist - Prints a histogram with the given bin size over the intron lengths\n";
		help+="\tintronLengthHist <psl file> <bin size> \n";
		help+="exonCount - counts the number of exons and omits introns shorter than limit\n";
		help+="\tintronLengthHist <psl file> <limit> \n";
		help+="removeQsmall - parses a sorted psl file and discards hits with less than identitycutoff % matches. Shorter hits are \"swallowed\" by larger query regions, completely enclosing them. \n";
		help+="\tremoveQsmall <psl file> <identity cutoff 0-1.0>\n";
		
		
		return help;
	}
	
	public static void removeQsmall(String pslFile,double identityCutoff)throws Exception{
		EstAlignment ea,ea2=new EstAlignment();
		PslParser pp = new PslParser(new BufferedReader(new FileReader(pslFile)));
		ArrayList<EstAlignment> cur=new ArrayList<EstAlignment>();
		String curName="";
//		boolean fullLength=false;
		System.out.print(getPslHeader());
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			if(((double)ea.matches)/(ea.qend-ea.qstart)>identityCutoff){
				if(!ea.qname.equals(curName)){
//					if(!fullLength){ 
						//analyze
						for (EstAlignment estAlignment : cur) {
							System.out.print(estAlignment.toPslString());
						}
//					}
					//reset
					curName=ea.qname;
//					fullLength=false;
					cur=new ArrayList<EstAlignment>();
				}
//				if(((double)ea.matches)/ea.qsize>0.95){
//					//if the transcript has a good full length hit, it is excluded as link
//					System.out.print(ea.toPslString());
//					fullLength=true;
//				}else if(!fullLength){
					boolean keep=true;
					for(int i=cur.size()-1;i>=0&&keep;i--){
						ea2=cur.get(i);
						if(ea.qstart>=ea2.qstart&&ea.qend<=ea2.qend){
							keep=false;
						}
						if(ea2.qstart>=ea.qstart&&ea2.qend<=ea.qend){
							cur.remove(i);
						}
					}
					if(keep){
						cur.add(ea);
					}
//				}
			}
		}
//		if(!fullLength){
			for (EstAlignment estAlignment : cur) {
				System.out.print(estAlignment.toPslString());
			}
//		}
	}
	
	public static void exonCount(String pslFile,int limit)throws Exception{
		EstAlignment ea;
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		System.out.println("qname"+sep+"nr of exons");
		for(int count=0;pp.hasNext();count=0){
			ea=pp.nextAlignment();
			for(int i=1;i<ea.blockSizes.length;i++){
				count=(ea.tStarts[i]-ea.tStarts[i-1]-ea.blockSizes[i-1])<limit?count+1:count;
			}
			System.out.println(ea.qname+sep+(ea.blockSizes.length-count));
		}
	}
	
	public static void intronLengthHist(String pslFile,int binsize)throws Exception{
		int maxBin=0,bin;
		HashMap<Integer, Integer> count= new HashMap<Integer, Integer>();
		EstAlignment ea;
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			for(int i=1;i<ea.blockSizes.length;i++){
				bin=(ea.tStarts[i]-ea.tStarts[i-1]-ea.blockSizes[i-1])/binsize;
				maxBin=bin>maxBin?bin:maxBin;
				if(count.containsKey(bin)){
					count.put(bin,count.get(bin)+1);
				}else{
					count.put(bin, 1);
				}
			}
		}
		System.out.println("Max in bin"+sep+"count");
		for(int i=0;i<maxBin+1;i++){
			if(count.containsKey(i)){
				System.out.println((i+1)*binsize+sep+count.get(i));
			}else{
//				System.out.println((i+1)*binsize+sep+"0");
			}
		}
	}
	
	public static void shortIntron(String pslFile, int limit)throws Exception{
		EstAlignment ea;
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		boolean print;
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			print= false;
			for(int i=1;i<ea.blockSizes.length&&!print;i++){
				print=(ea.tStarts[i]-ea.tStarts[i-1]-ea.blockSizes[i-1])<limit;
			}
			if(print){
				System.out.print(ea.qname+sep);
				for(int i=1;i<ea.blockSizes.length;i++){
					System.out.print((ea.tStarts[i]-ea.tStarts[i-1]-ea.blockSizes[i-1])+",");
				}
				System.out.println();
			}
		}
	}
	
	public static void premRNAOverlap(String pslFileA, String pslFileB)throws Exception{
		EstAlignment ea;
		ArrayList<EstAlignment> bList= new ArrayList<EstAlignment>();
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFileB)));
		for(;pp.hasNext();){
			bList.add(pp.nextAlignment());
		}
		pp= new PslParser(new BufferedReader(new FileReader(pslFileA)));
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			for (EstAlignment b : bList) {
				if(overlapMethods.premRNAoverlap(ea, b, true)){
					System.out.println(ea.qname+sep+b.qname);
				}
			}
		}
	}
	
	public static void estLinkage(String overlapAB,String overlapAest,String overlapBest)throws Exception{
		HashMap<String, ArrayList<String>> ab= new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> estA= new HashMap<String, ArrayList<String>>();
		HashMap<String, ArrayList<String>> foundPairs= new HashMap<String, ArrayList<String>>();
		String[] l;
		BufferedReader in= new BufferedReader(new FileReader(overlapAB));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			l=s.split("\t");
			if(!ab.containsKey(l[0])){
				ab.put(l[0], new ArrayList<String>());
			}
			ab.get(l[0]).add(l[1]);
		}
		in= new BufferedReader(new FileReader(overlapAest));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			l=s.split("\t");
			if(!estA.containsKey(l[1])){
				estA.put(l[1], new ArrayList<String>());
			}
			estA.get(l[1]).add(l[0]);
		}
		in= new BufferedReader(new FileReader(overlapBest));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			l=s.split("\t");
			if(estA.containsKey(l[1])){
				for (String a : estA.get(l[1])) {
					if(((!foundPairs.containsKey(a))||(!foundPairs.get(a).contains(l[0])))&&((!ab.containsKey(a))||(!ab.get(a).contains(l[0])))){
						if(!foundPairs.containsKey(a)){
							foundPairs.put(a, new ArrayList<String>());
						}
						foundPairs.get(a).add(l[0]);
						System.out.println(a+sep+l[0]);
					}
				}
			}
		}
	}
	
	public static void utrOverlap(String pslFile,String pslFileB,int utrSize, boolean sameStrand)throws Exception{
		EstAlignment ea;
		ArrayList<EstAlignment> bList = new ArrayList<EstAlignment>();
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFileB)));
		for(;pp.hasNext();){
			bList.add(pp.nextAlignment());
		}
		pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			for (EstAlignment b : bList) {
				if(overlapMethods.UTRoverlap(ea, b, sameStrand, utrSize)){
					System.out.println(ea.qname+sep+b.qname);
				}
			}
		}
	}
	
	public static void overlapPsl(String pslFile,String pslFileB,boolean sameStrand, boolean exon, boolean completeOverlap)throws Exception{
		EstAlignment ea;
		ArrayList<EstAlignment> bList= new ArrayList<EstAlignment>();
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFileB)));
		for(;pp.hasNext();){
			bList.add(pp.nextAlignment());
		}
		pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		if(exon){
			if(completeOverlap){
				System.err.println("complete exon");
				for(;pp.hasNext();){
					ea= pp.nextAlignment();
					for (EstAlignment b : bList) {
						if(overlapMethods.completeOverlap(ea, b, sameStrand)){
							System.out.println(ea.qname+sep+b.qname);
						}
					}
				}
			}else{
				System.err.println("partial exon");
				for(;pp.hasNext();){
					ea= pp.nextAlignment();
					for (EstAlignment b : bList) {
						if(overlapMethods.pslOverlap(ea, b, sameStrand)){
							System.out.println(ea.qname+sep+b.qname);
						}
					}
				}
			}
		}else{
			if(completeOverlap){
				System.err.println("complete intron");
				for(;pp.hasNext();){
					ea= pp.nextAlignment();
					for (EstAlignment b : bList) {
						if(overlapMethods.completeIntronOverlap(ea, b, sameStrand)){
							System.out.println(ea.qname+sep+b.qname);
						}
					}
				}
			}else{
				for(;pp.hasNext();){
					System.err.println("partial intron");
					ea= pp.nextAlignment();
					for (EstAlignment b : bList) {
						if(overlapMethods.intronOverlap(ea, b, sameStrand)){
							System.out.println(ea.qname+sep+b.qname);
						}
					}
				}
			}
		}
	}
	
	public static void ucsc2psl(String ucscFile, boolean onlyProt)throws Exception{
		EstAlignment ea;
		BufferedReader in= new BufferedReader(new FileReader(ucscFile));
		String[] l,ts,te;
		System.out.print(getPslHeader());
		for(String s= in.readLine();s!=null;s=in.readLine()){
			if(!s.startsWith("#")){
				l=s.split("\t");
				if(l.length>=10&&  ((onlyProt&&!l[10].equals(""))||!onlyProt)  ){
					ea= new EstAlignment();
					ea.tname=l[1];
					ea.strand[0]=l[2].charAt(0);
					ea.tstart=Integer.parseInt(l[3]);
					ea.tend=Integer.parseInt(l[4]);
					ea.blockSizes= new int[Integer.parseInt(l[7])];
					ea.tStarts= new int[Integer.parseInt(l[7])];
					ea.qStarts= new int[Integer.parseInt(l[7])];
					ts=l[8].split(",");
					te=l[9].split(",");
					for(int i=0;i<ea.tStarts.length;i++){
						ea.tStarts[i]=Integer.parseInt(ts[i]);
						ea.blockSizes[i]=Integer.parseInt(te[i])-ea.tStarts[i];
					}
					ea.qname=onlyProt?l[10]:l[0];
					//bullshit parameters
					ea.qstart=0;
					ea.qStarts[0]=0;
					ea.qend=ea.blockSizes[0];
					for(int i=1;i<ea.qStarts.length;i++){
						ea.qStarts[i]=ea.qStarts[i-1]+ea.blockSizes[i-1];
						ea.qend+=ea.blockSizes[i];
					}
					ea.qsize=ea.qend;
					ea.matches=ea.qsize;
					ea.mismatches=0;
					ea.repmatches=0;
					ea.ncount=0;
					ea.qnuminsert=0;
					ea.qbaseinsert=0;
					ea.tnuminsert=0;
					ea.tbaseinsert=0;
					ea.tsize=0;
					System.out.print(ea.toPslString());
				}
			}
		}
	}
	
	private static void splitOnTname(String pslFile,String outPrefix)throws Exception{
		HashMap<String, BufferedWriter> outs= new HashMap<String, BufferedWriter>();
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		EstAlignment ea;
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			if(!outs.containsKey(ea.tname)){
				outs.put(ea.tname, new BufferedWriter(new FileWriter(outPrefix+"_"+ea.tname+".psl")));
				outs.get(ea.tname).write(getPslHeader());
			}
			outs.get(ea.tname).write(ea.toPslString());
			
		}
		for (BufferedWriter out : outs.values()) {
			out.close();
		}
	}
	
	private static void makeUnique(String pslFile)throws Exception{
		HashMap<String, Integer> doublets= new HashMap<String, Integer>();
		PslParser pp = new PslParser(new BufferedReader(new FileReader(pslFile)));
		EstAlignment ea;
		System.out.print(getPslHeader());
		for(;pp.hasNext();){
			ea= pp.nextAlignment();
			if(doublets.containsKey(ea.qname)){
				doublets.put(ea.qname, doublets.get(ea.qname)+1);
				ea.qname=ea.qname+"_"+doublets.get(ea.qname);
				System.out.print(ea.toPslString());
			}else{
				System.out.print(ea.toPslString());
				doublets.put(ea.qname, 0);
			}
		}
	}
	
	private static void mergeCluster2psl(String clusterFile, String pslFile)throws Exception{
		String clusterPrefix="Cl";
		String singletPrefix="Si";
		int siCount=1;
		HashMap<String, ArrayList<String>> clusters= new HashMap<String, ArrayList<String>>();
		HashMap<String, String> transcripts= new HashMap<String, String>();
		HashMap<String, EstAlignment> data= new HashMap<String, EstAlignment>();
		BufferedReader in = new BufferedReader(new FileReader(clusterFile));
		String[] l;
		for(String s = in.readLine(); s!=null;s=in.readLine()){
			l=s.split("\t");
			if(l.length>1){
//				if(l[0].equals("M5C1092K24"))
//					System.err.println("tjo");
				transcripts.put(l[0], clusterPrefix+l[1]);
				if(!clusters.containsKey(clusterPrefix+l[1])){
					clusters.put(clusterPrefix+l[1], new ArrayList<String>());
				}
				clusters.get(clusterPrefix+l[1]).add(l[0]);
			}
		}
//		System.err.println(transcripts.size());
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslFile)));
		EstAlignment ea;
		String curClust;
		System.out.print(getPslHeader());
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			if(transcripts.containsKey(ea.qname)){
				curClust=transcripts.get(ea.qname);
				transcripts.remove(ea.qname);
				clusters.get(curClust).remove(ea.qname);
				if(data.containsKey(curClust)){
					//merge with previous entries
					data.get(curClust).mergeWith(ea, false);
				}else{
					ea.qname=curClust;
					data.put(curClust, ea);
				}
				if(clusters.get(curClust).size()==0){
					//print cluster
					System.out.print(data.get(curClust).toPslString());
					data.remove(curClust);
					clusters.remove(curClust);
				}
			}else{
//				System.err.println(ea.qname+" is not clustered. Prints as singlet");
				ea.qname=singletPrefix+(siCount++);
				System.out.print(ea.toPslString());
			}
		}
	}
	
	private static void countSSinAlignment(String pslfile, String fafile, String binfile,String outPrefix)throws Exception{
		ArrayList<Integer> bins=new ArrayList<Integer>();
		HashMap<String, EstAlignment> eas= new HashMap<String, EstAlignment>();
		EstAlignment ea;
		FastaSeq fs;
		BufferedReader binReader=new BufferedReader(new FileReader(binfile));
		BufferedWriter countWriter= new BufferedWriter(new FileWriter(outPrefix+"_ss_count.csv"));
		BufferedWriter binWriter= new BufferedWriter(new FileWriter(outPrefix+"_ss_binSize.csv"));
		int alignPos,seqPos,ssPos,nrOfSS;
		for (String s : binReader.readLine().split(",")) {
			bins.add(Integer.parseInt(s));
		}
		
		//print header
		countWriter.write("qname");
		binWriter.write("qname");
		int previous=0;
		for (Integer integer : bins) {
			countWriter.write(sep+previous+"-"+integer);
			binWriter.write(sep+previous+"-"+integer);
			previous=integer+1;
		}
		countWriter.write(sep+(bins.get(bins.size()-1)+1)+"-\n");
		binWriter.write(sep+(bins.get(bins.size()-1)+1)+"-\n");
		PslParser pp= new PslParser(new BufferedReader(new FileReader(pslfile)));
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			eas.put(ea.qname, ea);
		}
		fastaParser fp= new fastaParser(new BufferedReader(new FileReader(fafile)));
		for(;fp.hasNext();){
			fs=fp.next();
			if(eas.containsKey(fs.getHeader().substring(1).split(" ")[0])){
				ea= eas.get(fs.getHeader().substring(1).split(" ")[0]);
				countWriter.write(ea.qname);
				binWriter.write(ea.qname);
				if(ea.strand[1]=='+'||ea.strand[1]=='-'){
					System.err.println("Not parsed???");
					countWriter.write(sep+"Not Parsed???\n");
				}else{
					//prepare splice sites
					int[] ss= new int[ea.qStarts.length];
					if(ea.strand[0]=='+'){
						for(int i=0;i<ea.qStarts.length;i++){
							ss[i]=ea.qStarts[i];
						}
					}else{
						ss[0]=0;
//						System.out.println(ea.qname);
						for(int i=1;i<ea.qStarts.length;i++){
							ss[i]=ea.qsize-ea.qStarts[ea.qStarts.length-i];
//							System.out.println(ss[i]);
						}
						
					}
					//count Splice sites
					alignPos=0;
					seqPos=0;
					previous=0;
					ssPos=1;
					for (Integer bin : bins) {
						nrOfSS=0;
//						if(ssPos<ea.qStarts.length){
						for(;alignPos<bin;alignPos++){
							if(fs.getSeq().charAt(alignPos)!='-'){
								seqPos++;
							}
						}
						for(;ssPos<ss.length&&ss[ssPos]<=seqPos*3;ssPos++){
//							countWriter.writeln(ea.qStarts[ssPos]+sep+seqPos*3);
							nrOfSS++;
						}
//						}

						countWriter.write(sep+nrOfSS);
						binWriter.write(sep+(seqPos-previous));
						previous=seqPos;
					}
					countWriter.write(sep+(ss.length-ssPos)+"\n");
					binWriter.write(sep+(ea.qsize/3-seqPos)+"\n");
//					break;
				}
			}
		}
		
		countWriter.close();
		binWriter.close();
	}
	
//	private static void overlap2cluster2(String overlapFile)throws Exception{
//		BufferedReader in= new BufferedReader(new FileReader(overlapFile));
//		String[] l;
//		HashMap<Integer, ArrayList<String>> clusters= new HashMap<Integer, ArrayList<String>>();
//		HashMap<String, Integer> id= new HashMap<String, Integer>();
//		HashMap<Integer, ArrayList<Integer>> links= new HashMap<Integer, ArrayList<Integer>>();
//		ArrayList<String> tmp;
//		Integer cur;
//		for(String s= in.readLine();s!=null;s=in.readLine()){
//			l=s.split("\t");
//			if(l[0].equals(l[1])){
//				if(!id.containsKey(l[0])){
//					//add new singlet cluster
//					cur= clusters.size();
//					tmp=new ArrayList<String>();
//					tmp.add(l[0]);
//					clusters.put(cur, tmp);
//					id.put(l[0], cur);
//				}
//			}else{
//				if(id.containsKey(l[0])){
//					if(id.containsKey(l[1])&&id.get(l[0])!=id.get(l[1])){
//						//merge clusters, (add link)
//						if(!links.containsKey(id.get(l[0]))){
//							links.put(id.get(l[0]), new ArrayList<Integer>());
//						}
//						if(!links.containsKey(id.get(l[1]))){
//							links.put(id.get(l[1]), new ArrayList<Integer>());
//						}
//						if(!links.get(id.get(l[0])).contains(id.get(l[1]))){
//							links.get(id.get(l[0])).add(id.get(l[1]));
//						}
//						if(!links.get(id.get(l[1])).contains(id.get(l[0]))){
//							links.get(id.get(l[1])).add(id.get(l[0]));
//						}
//					}else{
//						//add l[1] to l[0] cluster
//						cur=id.get(l[0]);
//						if(!clusters.get(cur).contains(l[1])){
//							clusters.get(cur).add(l[1]);
//						}
//						id.put(l[1], cur);
//					}
//				}else{
//					if(id.containsKey(l[1])){
//						//add l[0] to l[1] cluster
//						cur=id.get(l[1]);
//						if(!clusters.get(cur).contains(l[0])){
//							clusters.get(cur).add(l[0]);
//						}
//						id.put(l[0], cur);
//					}else{
//						//add new doublet cluster
//						cur=clusters.size();
//						tmp=new ArrayList<String>();
//						tmp.add(l[0]);
//						tmp.add(l[1]);
//						clusters.put(cur, tmp);
//						id.put(l[0], cur);
//						id.put(l[1], cur);
//					}
//				}
//			}
//		}
//		//print
//		System.err.println("# clusters with links: "+links.size());
//		
//		
//		ArrayList<Integer> printedClusters= new ArrayList<Integer>();
//		
//		//print without recursion... have to catch links in error 1>stdout 2>stderr 
//		for (Integer cluster : clusters.keySet()) {
//			if(!printedClusters.contains(cluster)){
//				tmp=new ArrayList<String>();
//				for (String s : clusters.get(cluster)) {
//					if(!tmp.contains(s)){
//						System.out.println(s+sep+cluster+sep+cluster);
//						tmp.add(s);
//					}
//				}
//				printedClusters.add(cluster);
//				if(links.containsKey(cluster)){
//					for (Integer nr : links.get(cluster)) {
//						if(!printedClusters.contains(nr)){
//							for (String s : clusters.get(nr)) {
//								if(!tmp.contains(s)){
//									System.out.println(s+sep+cluster+sep+nr);
//									tmp.add(s);
//								}
//							}
//						}
//						printedClusters.add(nr);
//					}
//				}
//			}
//		}
//		//print links
//		for (Integer cluster : links.keySet()) {
//			for (Integer c2 : links.get(cluster)) {
//				System.err.println(cluster+sep+c2);
//			}
//		}
////		craves recurssion
////		for (Integer cluster : clusters.keySet()) {
////			if(!printedClusters.contains(cluster)){
////				tmp=new ArrayList<String>();
////				for (String s : clusters.get(cluster)) {
////					if(!tmp.contains(s)){
////						System.out.println(s+sep+cluster);
////						tmp.add(s);
////					}
////				}
////				printedClusters.add(cluster);
//////				printLinkedCluster(links, clusters, id,cluster, cluster, printedClusters, tmp);
////				//print linked clusters
////				for(Integer n : generatePrintList(links, cluster)){
////					if(!printedClusters.contains(n)){
////						for (String s : clusters.get(n)) {
////							if(!tmp.contains(s)){
////								System.out.println(s+sep+cluster);
////								tmp.add(s);
////							}
////						}
////						printedClusters.add(n);
////					}
////				}
////			}
////		}
//	}
//	
//	private static ArrayList<Integer> generatePrintList(HashMap<Integer, ArrayList<Integer>>links, Integer cluster){
//		ArrayList<Integer> list= new ArrayList<Integer>();
//		if(links.containsKey(cluster)){
//			for(Integer cur : links.get(cluster)){
//				if(!list.contains(cur)){
//					list.addAll(generatePrintList(links, cur));
//				}
//			}
//		}
//		return list;
//	}
//	
//	private static void printLinkedCluster(HashMap<Integer, ArrayList<Integer>> links,HashMap<Integer, ArrayList<String>> clusters,HashMap<String, Integer> id,Integer cluster,Integer clusterNr,ArrayList<Integer> printedClusters, ArrayList<String> printedId)throws Exception{
//		if(links.containsKey(cluster)){
//			for (Integer key : links.get(cluster)) {
//				if(!printedClusters.contains(key)){
//					for (String s : clusters.get(key)) {
//						if(!printedId.contains(s)){
//							System.out.println(s+sep+clusterNr);
//							printedId.add(s);
//						}
//					}
//					printedClusters.add(key);
//					printLinkedCluster(links, clusters, id, key, clusterNr, printedClusters, printedId);
//				}
//			}
//		}
//	}
	
	private static void overlap2cluster(String overlapFile)throws Exception{
		BufferedReader in =new BufferedReader(new FileReader(overlapFile));
		String[] l;
		HashMap<Integer, ArrayList<String>> clusters= new HashMap<Integer, ArrayList<String>>();
		HashMap<String, Integer> id= new HashMap<String, Integer>();
		ArrayList<String> tmp;
		Integer cur;
		for(String s=in.readLine();s!=null;s=in.readLine()){
			l=s.split("\t");
			if(l.length>2){
				if(l[0].equals(l[1])){
					if(!id.containsKey(l[0])){
						//add new singlet cluster
						cur=clusters.size();
						tmp=new ArrayList<String>();
						tmp.add(l[0]);
						clusters.put(cur, tmp);
						id.put(l[0], cur);
					}
				}else{
					if(id.containsKey(l[0])){
						if(id.containsKey(l[1])&&id.get(l[0])!=id.get(l[1])){
							//merge clusters
							cur=id.get(l[1]);
							tmp=clusters.get(cur);
							clusters.put(cur, new ArrayList<String>());
							cur= id.get(l[0]);
							for (String string : tmp) {
								if(!clusters.get(cur).contains(string)){
									clusters.get(cur).add(string);
								}
							}
							for (String t : tmp) {
								id.put(t, cur);
							}
						}else{
							//add l[1] to l[0] cluster
							cur=id.get(l[0]);
							if(!clusters.get(cur).contains(l[1])){
								clusters.get(cur).add(l[1]);
							}
							id.put(l[1], cur);
						}
					}else{
						if(id.containsKey(l[1])){
							//add l[0] to l[1] cluster
							cur=id.get(l[1]);
							if(!clusters.get(cur).contains(l[0])){
								clusters.get(cur).add(l[0]);
							}
							id.put(l[0], cur);
						}else{
							//add new doublet cluster
							cur=clusters.size();
							tmp=new ArrayList<String>();
							tmp.add(l[0]);
							tmp.add(l[1]);
							clusters.put(cur, tmp);
							id.put(l[0], cur);
							id.put(l[1], cur);
						}
					}
				}
			}
		}
		for (Integer key : clusters.keySet()) {
			for (String s : clusters.get(key)) {
				System.out.println(s+"\t"+key);
			}
		}
	}
	private static void overlapKnownGeneSelf(String knownGeneFile)throws Exception{
		KnownGeneToPslParser pp= new KnownGeneToPslParser(new BufferedReader(new FileReader(knownGeneFile)));
		HashMap<String, ArrayList<EstAlignment>> hits= new HashMap<String, ArrayList<EstAlignment>>();
		EstAlignment cur;
		for(;pp.hasNext();){
			cur=pp.nextAlignment();
			System.out.println(cur.qname+sep+cur.qname);
			if(hits.containsKey(cur.tname)){
				for (EstAlignment b : hits.get(cur.tname)) {
					if(b.overlaps(cur)){
						System.out.println(cur.qname+sep+b.qname);
						System.out.println(b.qname+sep+cur.qname);
					}
				}
			}else{
				hits.put(cur.tname, new ArrayList<EstAlignment>());
			}
			hits.get(cur.tname).add(cur);
		}
	}
	
	private static void overlap(String pslFile)throws Exception{
		PslParser pp =new PslParser(new BufferedReader(new FileReader(pslFile)));
		HashMap<String, ArrayList<EstAlignment>> hits= new HashMap<String, ArrayList<EstAlignment>>();
		EstAlignment cur;
		for(;pp.hasNext();){
			cur=pp.nextAlignment();
			System.out.println(cur.qname+sep+cur.qname);
			if(hits.containsKey(cur.tname)){
				for (EstAlignment b : hits.get(cur.tname)) {
					if(b.overlaps(cur)){
						System.out.println(cur.qname+sep+b.qname);
						System.out.println(b.qname+sep+cur.qname);
					}
				}
			}else{
				hits.put(cur.tname, new ArrayList<EstAlignment>());
			}
			hits.get(cur.tname).add(cur);
		}
	}
	
	private static void overlap(String pslFile,String bFile,int type)throws Exception{
		PslParser pp =new PslParser(new BufferedReader(new FileReader(pslFile)));
		HashMap<String, ArrayList<EstAlignment>> hits= new HashMap<String, ArrayList<EstAlignment>>();
		EstAlignment cur;
		for(;pp.hasNext();){
			cur=pp.nextAlignment();
			if(!hits.containsKey(cur.tname)){
				hits.put(cur.tname, new ArrayList<EstAlignment>());
			}
			hits.get(cur.tname).add(cur);
		}
		switch (type) {
		case 1:
			//psl
			pp=new PslParser(new BufferedReader(new FileReader(bFile)));
			for(;pp.hasNext();){
				cur=pp.nextAlignment();
				if(hits.containsKey(cur.tname)){
					for (EstAlignment b : hits.get(cur.tname)) {
						if(b.overlaps(cur)){
							System.out.println(b.qname+sep+cur.qname);
						}
					}
				}
			}
			break;
		case 2:
			//blast
			blastM8Parser bp=new blastM8Parser(new BufferedReader(new FileReader(bFile)));
			blastM8Alignment ba;
			for(;bp.hasMore();){
				ba=bp.nextHit();
				if(hits.containsKey(ba.tname)){
					for (EstAlignment b : hits.get(ba.tname)) {
						if(b.overlaps(ba)){
							System.out.println(b.qname+sep+ba.qname);
						}
					}
				}
			}
			break;
		default:
			throw new Exception("Unknown type: "+ type);
		}
	}
	
	private static void toGff(String pslFile,int offset,String source)throws Exception{
		PslParser pp =new PslParser(new BufferedReader(new FileReader(pslFile)));
		EstAlignment ea;
		HashMap<String, Integer> doublets= new HashMap<String, Integer>();
		System.out.println("##gff-version 3");
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			if(doublets.containsKey(ea.qname)){
				final int count= doublets.get(ea.qname);
				doublets.put(ea.qname, count+1);
				ea.qname=ea.qname+"_"+count;
			}else{
				doublets.put(ea.getQname(), 1);
			}
			System.out.println(ea.toGFF(offset,"exon",source));
		}
	}
	
//	private static void toGff(String pslFile,int offset,String tname,int start,int end,String source)throws Exception{
//		PslParser pp =new PslParser(new BufferedReader(new FileReader(pslFile)));
//		EstAlignment tmp;
//		HashMap<String, Integer> doublets= new HashMap<String, Integer>();
//		System.out.println("##gff-version 3");
//		for(;pp.hasNext();){
//			tmp=pp.nextAlignment();
//			if(tmp.tname.equals(tname)){
//				if((start>=tmp.tstart&&start<=tmp.tend)||(end>=tmp.tstart&&end<=tmp.tend)||(start<tmp.tstart&&end>tmp.tend)){
//					if(doublets.containsKey(tmp.qname)){
//						final int count= doublets.get(tmp.qname);
//						doublets.put(tmp.qname, count+1);
//						tmp.qname=tmp.qname+"_"+count;
//					}else{
//						doublets.put(tmp.getQname(), 1);
//					}
//					System.out.println(tmp.toGFF(offset,"exon",source));
//				}
//			}
//		}
//	}
	
	private static void generateFastacmdScript(String pslFile,int n,String database)throws Exception{
		PslParser pp =new PslParser(new BufferedReader(new FileReader(pslFile)));
		for(;pp.hasNext();){
			System.out.println(pp.nextAlignment().toFastacmdScript(database, n));
		}
	}
	
	private static void clusterOverlap(String inFile)throws Exception{
		BufferedReader in=new BufferedReader(new FileReader(inFile));
		ArrayList<ArrayList<String>> cluster=new ArrayList<ArrayList<String>>();
		HashMap<String, Integer> map=new HashMap<String, Integer>();
		String[] l;
		Integer curClust,otherClust;
		for(String s=in.readLine();s!=null;s=in.readLine()){
			l=s.split("\t");
			if(l.length==2){
				if(map.containsKey(l[0])&&map.containsKey(l[1])){
					//merge clusters
					curClust=map.get(l[0]);
					otherClust=map.get(l[1]);
					if(curClust!=otherClust){
						for (String id : cluster.get(otherClust)) {
							map.put(id, curClust);
							cluster.get(curClust).add(id);
						}
						cluster.set(otherClust, new ArrayList<String>());
					}
				}else if(map.containsKey(l[0])){
					curClust=map.get(l[0]);
					if(!cluster.get(curClust).contains(l[1])){
						cluster.get(curClust).add(l[1]);
						map.put(l[1], curClust);
					}
				}else if(map.containsKey(l[1])){
					curClust=map.get(l[1]);
					if(!cluster.get(curClust).contains(l[0])){
						cluster.get(curClust).add(l[0]);
						map.put(l[0], curClust);
					}
				}else{
					//add new cluster
					curClust=cluster.size();
					cluster.add(new ArrayList<String>());
					cluster.get(curClust).add(l[0]);
					map.put(l[0], curClust);
					if (!l[0].equals(l[1])) {
						cluster.get(curClust).add(l[1]);
						map.put(l[1], curClust);
					}					
				}
			}
		}
		//print
		for(int i=0;i<cluster.size();i++){
			for (String s : cluster.get(i)) {
				System.out.println(s+"\t"+i);
			}
		}
	}
	
	private static void sub(String infile, String subfile, boolean inSubFile)throws Exception{
		ArrayList<String> subset=new ArrayList<String>();
		BufferedReader in=new BufferedReader(new FileReader(subfile));
		for(String s=in.readLine();s!=null;s=in.readLine()){
			if(!subset.contains(s)){
				subset.add(s);
			}
		}
		System.out.print(getPslHeader());
		PslParser pp=new PslParser(new BufferedReader(new FileReader(infile)));
		EstAlignment ea;
		for(;pp.hasNext();){
			ea=pp.nextAlignment();
			if(subset.contains(ea.qname)==inSubFile){
				System.out.print(ea.toPslString());
			}
		}
	}
	
	private static void getDoublets(String infile,double limit)throws Exception{
		PslParser in=new PslParser(new BufferedReader(new FileReader(infile)));
		Hashtable<String, ArrayList<String>>pairs=new Hashtable<String, ArrayList<String>>();
		for(;in.hasNext();){
			EstAlignment ea=in.nextAlignment();
			if(((double)ea.matches)/((double)ea.qsize)>=limit&&!ea.tname.equals(ea.qname)){
//				System.out.println("tjo");
				if(pairs.containsKey(ea.qname)){
					if(!pairs.get(ea.qname).contains(ea.tname)){
						System.out.println(ea.qname+"\t"+ea.tname);
						pairs.get(ea.qname).add(ea.tname);
						if(!pairs.containsKey(ea.tname)){
							pairs.put(ea.tname, new ArrayList<String>());
						}
						pairs.get(ea.tname).add(ea.qname);
					}
				}else if(pairs.containsKey(ea.tname)){
					if(!pairs.get(ea.tname).contains(ea.qname)){
						System.out.println(ea.qname+"\t"+ea.tname);
						pairs.get(ea.tname).add(ea.qname);
						pairs.put(ea.qname, new ArrayList<String>());
						pairs.get(ea.qname).add(ea.tname);
					}
				}else{
					System.out.println(ea.qname+"\t"+ea.tname);
					pairs.put(ea.tname, new ArrayList<String>());
					pairs.get(ea.tname).add(ea.qname);
					pairs.put(ea.qname, new ArrayList<String>());
					pairs.get(ea.qname).add(ea.tname);
				}
			}
		}
	}

	private static void split(String infile, int nrPerFile,String outprefix)throws Exception{
		PslParser in =new PslParser(new BufferedReader(new FileReader(infile)));
		int filenr=1;
		BufferedWriter out=new BufferedWriter(new FileWriter(outprefix+"_"+filenr+".psl"));
		out.write(getPslHeader());
		for (int i = 0; in.hasNext(); i++) {
			if (i%nrPerFile==0) {
				out.close();
				out=new BufferedWriter(new FileWriter(outprefix+"_"+filenr+".psl"));
				out.write(getPslHeader());
				filenr++;
			}
			out.write(in.nextAlignment().toPslString());
		}
		out.close();
	}
	
	private static String getPslHeader(){
		String header="psLayout version 3\n";
		header+="\n";
		header+="match     mis-    rep.    N's     Q gap   Q gap   T gap   T gap   strand  Q               Q       Q       Q       T               T       T       T       block   blockSizes      qStarts  tStarts\n";
		header+="          match   match           count   bases   count   bases           name            size    start   end     name            size    start   end     count\n";
		header+="---------------------------------------------------------------------------------------------------------------------------------------------------------------\n";
		
		return header;
	}
}

