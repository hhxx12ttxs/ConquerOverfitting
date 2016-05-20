package phrase;

import io.FileUtil;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import phrase.Corpus.Edge;

import arr.F;

public class Trainer 
{
	public static void main(String[] args) 
	{
        OptionParser parser = new OptionParser();
        parser.accepts("help");
        parser.accepts("in").withRequiredArg().ofType(File.class);
        parser.accepts("in1").withRequiredArg().ofType(File.class);
        parser.accepts("test").withRequiredArg().ofType(File.class);
        parser.accepts("out").withRequiredArg().ofType(File.class);
        parser.accepts("start").withRequiredArg().ofType(File.class);
        parser.accepts("parameters").withRequiredArg().ofType(File.class);
        parser.accepts("topics").withRequiredArg().ofType(Integer.class).defaultsTo(5);
        parser.accepts("iterations").withRequiredArg().ofType(Integer.class).defaultsTo(10);
        parser.accepts("threads").withRequiredArg().ofType(Integer.class).defaultsTo(0);
        parser.accepts("scale-phrase").withRequiredArg().ofType(Double.class).defaultsTo(0.0);
        parser.accepts("scale-context").withRequiredArg().ofType(Double.class).defaultsTo(0.0);
        parser.accepts("seed").withRequiredArg().ofType(Long.class).defaultsTo(0l);
        parser.accepts("convergence-threshold").withRequiredArg().ofType(Double.class).defaultsTo(1e-6);
        parser.accepts("variational-bayes");
        parser.accepts("alpha-emit").withRequiredArg().ofType(Double.class).defaultsTo(0.1);
        parser.accepts("alpha-pi").withRequiredArg().ofType(Double.class).defaultsTo(0.0001);
        parser.accepts("agree-direction");
        parser.accepts("agree-language");
        parser.accepts("no-parameter-cache");
        parser.accepts("skip-large-phrases").withRequiredArg().ofType(Integer.class).defaultsTo(5);
        OptionSet options = parser.parse(args);

        if (options.has("help") || !options.has("in"))
        {
        	try {
				parser.printHelpOn(System.err);
			} catch (IOException e) {
				System.err.println("This should never happen.");
				e.printStackTrace();
			}
        	System.exit(1);     
        }
		
		int tags = (Integer) options.valueOf("topics");
		int iterations = (Integer) options.valueOf("iterations");
		double scale_phrase = (Double) options.valueOf("scale-phrase");
		double scale_context = (Double) options.valueOf("scale-context");
		int threads = (Integer) options.valueOf("threads");
		double threshold = (Double) options.valueOf("convergence-threshold");
		boolean vb = options.has("variational-bayes");
		double alphaEmit = (vb) ? (Double) options.valueOf("alpha-emit") : 0;
		double alphaPi = (vb) ? (Double) options.valueOf("alpha-pi") : 0;
		int skip = (Integer) options.valueOf("skip-large-phrases");
		
		if (options.has("seed"))
			F.rng = new Random((Long) options.valueOf("seed"));
		
		ExecutorService threadPool = null;
		if (threads > 0)
			threadPool = Executors.newFixedThreadPool(threads);			
		
		if (tags <= 1 || scale_phrase < 0 || scale_context < 0 || threshold < 0)
		{
			System.err.println("Invalid arguments. Try again!");
			System.exit(1);
		}
		
		Corpus corpus = null;
		File infile = (File) options.valueOf("in");
		Corpus corpus1 = null;
		File infile1 = (File) options.valueOf("in1");
		try {
			System.out.println("Reading concordance from " + infile);
			corpus = Corpus.readFromFile(FileUtil.reader(infile));
			corpus.printStats(System.out);
			if(options.has("in1")){
				corpus1 = Corpus.readFromFile(FileUtil.reader(infile1));
				corpus1.printStats(System.out);
			}
		} catch (IOException e) {
			System.err.println("Failed to open input file: " + infile);
			e.printStackTrace();
			System.exit(1);
		}
				
		if (!(options.has("agree-direction")||options.has("agree-language")))
			System.out.println("Running with " + tags + " tags " +
					"for " + iterations + " iterations " +
					((skip > 0) ? "skipping large phrases for first " + skip + " iterations " : "") +
					"with scale " + scale_phrase + " phrase and " + scale_context + " context " +
					"and " + threads + " threads");
		else
			System.out.println("Running agreement model with " + tags + " tags " +
	 				"for " + iterations);

	 	System.out.println();
		
 		PhraseCluster cluster = null;
 		Agree2Sides agree2sides = null;
 		Agree agree= null;
 		VB vbModel=null;
 		if (options.has("agree-language"))
 			agree2sides = new Agree2Sides(tags, corpus,corpus1);
 		else if (options.has("agree-direction"))
 			agree = new Agree(tags, corpus);
 		else
 		{
 			if (vb)	
 			{
 				vbModel=new VB(tags,corpus);
 				vbModel.alpha=alphaPi;
 				vbModel.lambda=alphaEmit;
 	 			if (threadPool != null) vbModel.useThreadPool(threadPool);
 			} 
 			else 
 			{
 				cluster = new PhraseCluster(tags, corpus);
 	 			if (threadPool != null) cluster.useThreadPool(threadPool);
 				
	 			if (options.has("no-parameter-cache")) 
	 				cluster.cacheLambda = false;
	 			if (options.has("start"))
	 			{
	 				try {
						System.err.println("Reading starting parameters from " + options.valueOf("start"));
						cluster.loadParameters(FileUtil.reader((File)options.valueOf("start")));
					} catch (IOException e) {
						System.err.println("Failed to open input file: " + options.valueOf("start"));
						e.printStackTrace();
					}
	 			}
 			}
 		}
				
		double last = 0;
		for (int i=0; i < iterations; i++)
		{
			double o;
			if (agree != null)
				o = agree.EM();
			else if(agree2sides!=null)
				o = agree2sides.EM();
			else
			{
				if (i < skip)
					System.out.println("Skipping phrases of length > " + (i+1));
				
				if (scale_phrase <= 0 && scale_context <= 0)
				{
					if (!vb)
						o = cluster.EM((i < skip) ? i+1 : 0);
					else
						o = vbModel.EM();	
				}
				else
					o = cluster.PREM(scale_phrase, scale_context, (i < skip) ? i+1 : 0);
			}
			
			System.out.println("ITER: "+i+" objective: " + o);
			
			// sometimes takes a few iterations to break the ties
			if (i > 5 && Math.abs((o - last) / o) < threshold)
			{
				last = o;
				break;
			}
			last = o;
		}
		
		double pl1lmax = 0, cl1lmax = 0;
		if (cluster != null)
		{
			pl1lmax = cluster.phrase_l1lmax();
			cl1lmax = cluster.context_l1lmax();
		}
		else if (agree != null)
		{
			// fairly arbitrary choice of model1 cf model2
			pl1lmax = agree.model1.phrase_l1lmax();
			cl1lmax = agree.model1.context_l1lmax();
		}
		else if (agree2sides != null)
		{
			// fairly arbitrary choice of model1 cf model2
			pl1lmax = agree2sides.model1.phrase_l1lmax();
			cl1lmax = agree2sides.model1.context_l1lmax();
		}

		System.out.println("\nFinal posterior phrase l1lmax " + pl1lmax + " context l1lmax " + cl1lmax);
		
		if (options.has("out"))
		{
			File outfile = (File) options.valueOf("out");
			try {
				PrintStream ps = FileUtil.printstream(outfile);
				List<Edge> test;
				if (!options.has("test")) // just use the training
					test = corpus.getEdges();
				else
				{	// if --test supplied, load up the file
					infile = (File) options.valueOf("test");
					System.out.println("Reading testing concordance from " + infile);
					test = corpus.readEdges(FileUtil.reader(infile));
				}
				if(vb) {
					assert !options.has("test");
					vbModel.displayPosterior(ps);
				} else if (cluster != null) 
					cluster.displayPosterior(ps, test);
				else if (agree != null) 
					agree.displayPosterior(ps, test);
				else if (agree2sides != null) {
					assert !options.has("test");
					agree2sides.displayPosterior(ps);
				}
				
				ps.close();
			} catch (IOException e) {
				System.err.println("Failed to open either testing file or output file");
				e.printStackTrace();
				System.exit(1);
			}
		}

		if (options.has("parameters"))
		{
			assert !vb;
			File outfile = (File) options.valueOf("parameters");
			PrintStream ps;
			try {
				ps = FileUtil.printstream(outfile);
				cluster.displayModelParam(ps);
				ps.close();
			} catch (IOException e) {
				System.err.println("Failed to open output parameters file: " + outfile);
				e.printStackTrace();
				System.exit(1);
			}
		}
		
		if (cluster != null && cluster.pool != null)
			cluster.pool.shutdown();
	}
}

