package org.roettig.SequenceTools;

import java.util.Iterator;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.io.*;
import java.net.URL;
import java.util.*;
import org.biojava.bio.Annotation;
import org.biojava.bio.seq.Sequence;
import org.roettig.SequenceTools.exception.FileParseErrorException;


/**
 * The MSA class is used to build and operate on multiple sequence alignments. 
 * 
 * @author roettig
 */
public class MSA implements Iterable<Sequence>, Serializable
{
	protected SequenceSet seqs = null;
	protected HashMap<Integer,Integer> quality = null;
	private static String MUSCLEPATH = null;

	public MSA()
	{
		seqs    = new SequenceSet();
		quality = new HashMap<Integer,Integer>();
	}

	public MSA(SequenceSet _seqs)
	{
		seqs    = new SequenceSet(_seqs);
		quality = new HashMap<Integer,Integer>();
	}

	private static void checkMusclePath() throws Exception
	{	
		String path = System.getProperty("musclepath");
		if(path!=null)
			MUSCLEPATH = path;
		else
			MUSCLEPATH = "/usr/bin";

	}

	/**
	 * Static creation method to create new MSA using Muscle.
	 * 
	 * @param seqs
	 * @return MSA
	 * @throws Exception
	 */
	public static MSA createMuscleMSA(SequenceSet seqs) throws Exception
	{
		MSA ret     = new MSA();

		if(MUSCLEPATH==null)
		{
			checkMusclePath();            
		}

		File tmpIn  = null;
		File tmpOut = null;
		try
		{
			tmpIn  = File.createTempFile("createmsa", ".IN");
			tmpOut = File.createTempFile("createmsa", ".OUT");
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return ret;
		}

		seqs.store(tmpIn.getAbsoluteFile().toString());


		try
		{
			ProcessBuilder builder = new ProcessBuilder( "/bin/bash", "-c", MUSCLEPATH+"/muscle -in "+tmpIn.getAbsoluteFile().toString()+" -out "+tmpOut.getAbsoluteFile().toString()); 
			Process p = builder.start();   
			p.waitFor();

			/*
          BufferedReader reader = new BufferedReader( new InputStreamReader(p.getErrorStream()) );
          String s;
          while( null != (s = reader.readLine()) ) 
          {
              System.out.println(s);
          }
          reader.close();
			 */
		}
		catch(InterruptedException e)
		{
			e.printStackTrace();
		} 
		catch (IOException e)
		{
			e.printStackTrace();
		}

		SequenceSet msaseqs = null;
		msaseqs = SequenceSet.readFromFile(tmpOut.toString());

		for(Sequence s: msaseqs)
		{
			ret.add( s );
		}

		tmpIn.delete();
		tmpOut.delete();
		return ret;
	}

	/**
	 * Store MSA to file.
	 * 
	 * @param filename
	 */
	public void store(String filename)
	{
		seqs.store(filename);
	}

	public static MSA loadFromFile(String filename) throws FileNotFoundException, FileParseErrorException
	{
		MSA ret = new MSA();
		ret.load(filename);
		return ret;
	}

	/**
	 * Load MSA from file.
	 * 
	 * @param filename
	 */
	public void load(String filename) throws FileNotFoundException, FileParseErrorException
	{

		SequenceSet seqsIn = null;
		try
		{
			seqsIn = SequenceSet.readFromFile(filename);
		} 
		catch (FileNotFoundException e)
		{
			e.printStackTrace();
		} 
		catch (FileParseErrorException e)
		{
			e.printStackTrace();
		}

		seqs.clear();
		for(Sequence s: seqsIn)
		{
			add( s );
		}
	}

	public static MSA align(MSA msa, SequenceSet seqs)
	{
		HMM hmm = null;
		try
		{
			hmm = new HMM(msa);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}

		MSA ret = null;

		try
		{
			ret = hmm.align(seqs);
		} 
		catch (Exception e)
		{
			e.printStackTrace();
		}
		return ret;
	}

	/**
	 * Get sequence in MSA by ID.
	 * @param id
	 * @return Sequence
	 */
	public Sequence getById(String id)
	{
		return seqs.getById(id);
	}

	/**
	 * Get i-th sequence in MSA.
	 * @param idx
	 * @return Sequence
	 */
	public Sequence getByIndex(int idx)
	{
		return seqs.getByIndex(idx);
	}

	/**
	 * Get number of sequences in the MSA (its depth).
	 * @return depth
	 */
	public int depth()
	{
		return seqs.size();
	}

	/**
	 * Get number of columns in the MSA (its width).
	 * @return width
	 */
	public int width()
	{
		if(depth()>0)
			return seqs.getByIndex(0).length();
		else
			return 0;
	}

	/**
	 * Get all columns from the MSA that have at most <i>fraction</i>*depth gaps. 
	 * 
	 * @param frac  fraction of gaps allowed in a column to be kept 
	 * @return SequenceSet
	 */
	public SequenceSet getAlignedSubSequences(double frac)
	{
		SequenceSet ret   = new SequenceSet();
		Set<Integer> keep = new HashSet<Integer>();
		for(int c=0;c<width();c++)
		{
			int nGaps        = 0;
			int nAllowedGaps = (int) (depth()*frac);
			boolean keepCol = true;
			for(int r=0;r<depth();r++)
			{
				if(getSymbol(r,c).equals("-"))
					nGaps++;
				if(nGaps>nAllowedGaps)
				{
					keepCol = false;
					break;
				}
			}

			if(keepCol)
				keep.add(c+1);
		}

		for(Sequence s: this)
		{
			Sequence subseq = getSubSequence(s.getName(), keep);
			Annotation seqAn = subseq.getAnnotation();
			seqAn.setProperty("parent", s);
			ret.add( subseq );
		}

		return ret;
	}

	/**
	 * Get all columns with indices given by <i>cols</i>. Indices are interpreted
	 * relative to the sequence with ID given by <i>refsid</i>.
	 * @param cols
	 * @param refsid
	 * @return SequenceSet
	 * 
	 */
	public SequenceSet getSubSequences( Set<Integer> cols, String refsid)
	{
		TreeMap<Integer,Integer> nidxs = new TreeMap<Integer,Integer>();
		for(Integer c: cols)
		{
			int idxN = mapSequenceIndexToColumnIndex(refsid,c);
			nidxs.put(idxN, 1);
		}
		SequenceSet ret = new SequenceSet();
		for(Sequence s: this)
		{
			Sequence subseq = getSubSequence(s.getName(), nidxs.keySet());
			Annotation seqAn = subseq.getAnnotation();
			seqAn.setProperty("parent", s);
			ret.add( subseq );
		}
		return ret;
	}


	/**
	 * Get all columns with indices given by <i>cols</i>. Indices are interpreted
	 * relative to the sequence with ID given by <i>refsid</i>. 
	 * @param sid
	 * @param cols
	 * @return Sequence
	 */
	public Sequence getSubSequence(String sid, Set<Integer> cols)
	{
		String subseq = "";
		for(Integer c: cols)
		{
			subseq += getSymbol(sid,  c-1 );
		}
		Sequence retseq = SeqTools.makeProteinSequence(sid, subseq);
		/*
        Sequence retseq = null;
        try
        {
            retseq = SeqTools.makeProteinSequence(sid, subseq);
            //retseq = ProteinTools.createProteinSequence(subseq, sid);
        } 
        catch (IllegalSymbolException e)
        {
            e.printStackTrace();
        } 
		 */
		return retseq;
	}

	/**
	 * Get symbol in row <i>i</i> and column <i>j</i> in MSA.
	 * 
	 * @param i
	 * @param j
	 * @return String
	 */
	public final String getSymbol(int i, int j)
	{
		Sequence s = seqs.getByIndex(i);
		return s.seqString().substring(j, j+1);
	}

	/**
	 * Get symbol in sequence with ID <i>sid</i> and column <i>j</i> in MSA.
	 * 
	 * @param sid
	 * @param j
	 * @return String
	 */
	public String getSymbol(String sid, int j)
	{
		Sequence s = seqs.getById(sid);
		return s.seqString().substring(j, j+1);
	}

	/**
	 * Map column index <i>idx</i> of MSA to sequence-internal index
	 * of sequence given by ID <i>sid</i>. 
	 * 
	 * @param sid
	 * @param idx
	 * @return int
	 */
	public int mapColumnIndexToSequenceIndex(String sid, int idx)
	{
		Sequence s = seqs.getById(sid);
		int sM=0;
		for(int i=0;i<idx;i++)
		{
			String symb = s.subStr(i+1,i+1);   
			if(!symb.equals("-"))             
				sM++;
		}
		return sM;
	}

	/**
	 * Map sequence-internal index <i>idx</i> of sequence given by ID <i>sid</i> to
	 * column index of MSA.  
	 * @param sid
	 * @param idx
	 * @return int
	 */
	public int mapSequenceIndexToColumnIndex(String sid, int idx)
	{
		Sequence s = seqs.getById(sid);
		int sM=0;
		for(int i=0;i<s.length();i++)
		{
			if(idx==sM)
				return i;
			String symb = s.subStr(i+1,i+1);
			//System.out.println(symb);
			if(!symb.equals("-"))
				sM++;
		}
		return -1;
	}

	/**
	 * Add sequence to MSA.
	 * 
	 * @param seq
	 */
	protected void add(Sequence seq)
	{
		seqs.add( seq );    
	}

	@Override
	public Iterator<Sequence> iterator()
	{
		return seqs.iterator();
	}

	/**
	 * Calculate the overall quality of the MSA.
	 * 
	 */
	public void calculateQuality()
	{

		for(int c=0;c<width();c++)
		{
			double s1 = 0.0;
			double s2 = 0.0;
			double s  = 0.0;

			boolean gappy = false;
			for(int r1=0;r1<depth();r1++)
			{
				int nGaps = 0;
				for(int r2=0;r2<depth();r2++)
				{
					if(getSymbol(r2,c).equals("-"))
						nGaps++;
					if(getSymbol(r1,c).equals("-") || getSymbol(r2,c).equals("-"))
					{
						s2+=12;
						continue;
					}

					double z1 = 10+SeqTools.getBLOSUM62Score(getSymbol(r1,c).charAt(0),getSymbol(r2,c).charAt(0));
					double z2 = 10+SeqTools.getBLOSUM62Score(getSymbol(r2,c).charAt(0),getSymbol(r1,c).charAt(0));
					double n1 = 10+SeqTools.getBLOSUM62Score(getSymbol(r1,c).charAt(0),getSymbol(r1,c).charAt(0));
					double n2 = 10+SeqTools.getBLOSUM62Score(getSymbol(r2,c).charAt(0),getSymbol(r2,c).charAt(0));
					/*
                if(c==21||c==20||c==22)
                {
                    System.out.format("z1:%.3f z2:%.3f  n1:%.3f n2:%.3f %s %s\n",z1,z2,n1,n2,getSymbol(r1,c),getSymbol(r2,c));

                }
					 */
					s+=(z1/(1.0*n1))+(z2/(1.0*n2));
					s1+=(z1+z2);
					s2+=(n1+n2);
				}
				if( nGaps > (0.6*depth()) )
				{
					//gappy = true;
					//break;
				}
			}
			if(gappy)
			{
				int qual = 0;
				setColumnQuality(c, qual);
			}
			else
			{
				int qual = (int) ((s1/s2)*10);
				//int qual = (int) s;
				setColumnQuality(c, qual);
			}
		}

	}

	/**
	 * Set the overall quality of column <i>c</i> within the MSA.
	 * 
	 * @param c
	 * @param q
	 */
	public void setColumnQuality(int c, int q)
	{
		quality.put(c,q);
	}

	/**
	 * Get the overall quality of column <i>c</i> within the MSA.
	 * 
	 * @param c
	 * @return int
	 */
	public int getColumnQuality(int c)
	{
		return quality.get(c);
	}
}

