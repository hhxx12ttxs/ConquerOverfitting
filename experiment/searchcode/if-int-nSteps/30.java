/**
 * 
 */
package org.roettig.SequenceTools;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.biojava.bio.seq.Sequence;
import org.roettig.SequenceTools.exception.FileParseErrorException;
import org.roettig.SequenceTools.helper.ClientHttpRequest;

/**
 * @author roettig
 *
 */
public class ThreeDCoffeeAlignment
{
	public static Logger logger = Logger.getLogger("org.roettig.SequenceTools.ThreeDCoffeeAlignment");

	private String PID;
	private SequenceSet seqs = new SequenceSet();
	private Map<String,String> masked_name_2_orig_name = new HashMap<String,String>();

	private static String CGIURL2    = "http://tcoffee.vital-it.ch/cgi-bin/Tcoffee/tcoffee_cgi/index.cgi";
	private static String RESULTURL2 = "http://tcoffee.vital-it.ch/Tmp/EXPA/";
	private static String CGIURL1    = "http://www.igs.cnrs-mrs.fr/Tcoffee/tcoffee_cgi/index.cgi";
	private static String RESULTURL1 = "http://www.igs.cnrs-mrs.fr/Tcoffee/Tmp/EXPA/";

	private String CGIURL    = CGIURL1;
	private String SUBMITURL = CGIURL+"?stage1=1&daction=EXPRESSO(3DCoffee)::Advanced";
	private String RESULTURL = RESULTURL1;

	public boolean isFinished(String job)
	{
		URL url = null;
		BufferedReader in = null;
		try
		{
			url = new URL(RESULTURL+job+".file_result.html");
			in = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = in.readLine()) != null)
			{
				if(line.indexOf("Error 404")!=-1)
				{
					return false;
				}
			}
			in.close();
		} 
		catch (Exception e)
		{
			//e.printStackTrace();
			return false;
		}
		return true;
	}

	ThreeDCoffeeAlignment()
	{

	}

	public ThreeDCoffeeAlignment(SequenceSet _seqs)
	{
		int idx = 1;
		for(Sequence s: _seqs)
		{
			String sid = String.format("%d",idx);
			Sequence sclone = SeqTools.makeProteinSequence(sid,s.seqString());
			seqs.add(sclone);
			masked_name_2_orig_name.put(sid,s.getName());
			idx++;
		}
	}

	private String fetchPID() throws IOException
	{
		//URL url = new URL("http://www.igs.cnrs-mrs.fr/Tcoffee/tcoffee_cgi/index.cgi?stage1=1&daction=EXPRESSO(3DCoffee)::Advanced");
		URL url = new URL(SUBMITURL);
		BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));

		String ret = null;

		String line;

		while ((line = in.readLine()) != null)
		{

			if(line.indexOf("<input type=\"hidden\" name=\"pid\" value=\"")!=-1)
			{
				Pattern p = Pattern.compile("pid\" value=\"(\\d+)\"");
				Matcher m = p.matcher(line);
				boolean result = m.find();
				if(result)
					ret = m.group(1);
			}
			else
				if(line.indexOf("<input type='hidden' name='pid' value='")!=-1)
				{
					Pattern p = Pattern.compile("pid' value='(\\d+)'");
					Matcher m = p.matcher(line);
					boolean result = m.find();
					if(result)
						ret = m.group(1);
				}
		}
		in.close();
		logger.info("acquired PID "+ret);
		return ret;
	}

	public MSA align() throws Exception
	{
		PID = fetchPID();
		wrapSequences();
		sendData();
		fetchData();
		unwrapSequences();
		return msa;
	}

	public MSA fetchMSA(String _jobid) throws Exception
	{
		jobid = _jobid;
		fetchData();
		unwrapSequences();
		return msa;
	}



	private void sendData() throws IOException
	{
		//ClientHttpRequest req = new ClientHttpRequest("http://www.igs.cnrs-mrs.fr/Tcoffee/tcoffee_cgi/index.cgi");
		ClientHttpRequest req = new ClientHttpRequest(CGIURL);
		req.setParameter("stage2","2"); 
		req.setParameter("pid",PID);
		req.setParameter("daction","EXPRESSO(3DCoffee)::Advanced");
		req.setParameter("executable","t_coffee");
		req.setParameter("-evaluate_mode","t_coffee_slow");
		req.setParameter("-template_file","SCRIPT_webblast.pl@method#pdbid@database#expressopdb@blast_dir#blastexpresso");
		req.setParameter("-output","fasta_aln,score_ascii,score_html");
		req.setParameter("-case","upper");
		req.setParameter("-seqnos","on");
		req.setParameter("-in","Mslow_pair,Mfugue_pair,Msap_pair");
		req.setParameter("-outorder","input");
		req.setParameter("Submit","Submit");
		req.setParameter("list_visible_outfile","fasta_aln,score_ascii,score_html");
		req.setParameter("-uploaded_file-in",tmpIn); 
		InputStream is = req.post();

		BufferedReader bis = new BufferedReader(new InputStreamReader(is) );
		String line=null; 
		while ((line = bis.readLine()) != null) 
		{ 
			//System.out.println(line);
		}  
		Map<String,List<String> > header = req.getRequestHeaders();
		for(String s: header.keySet())
		{
			//System.out.println(s);
			for(String vs: header.get(s) )
			{
				//System.out.println("\t"+vs);

				if(s!=null && s.equals("Refresh"))
				{
					//String s = "feedback.cgi?level=Advanced&child=8132&result_file=tcfEXPA99715_8130.file_result.html&pg_source=/home/igs/public_html/Tcoffee/tcoffee_cgi/index.cgi&time_init=1269625526&email=&mode=EXPRESSO(3DCoffee)";
					String toks1[] = vs.split("&");
					String toks2[] = toks1[2].split("=");
					//System.out.println(toks2[1]);
					joburl = toks2[1];
					String toks3[] = joburl.split("\\.");
					jobid  = toks3[0]; 
					//System.out.println("jobid="+jobid);
					logger.info("submitted job with job-id "+jobid);
				}

			}
		}

	}

	private String jobid;
	private File tmpIn   = null;


	private void wrapSequences()
	{
		try
		{
			tmpIn  = File.createTempFile("3DCoffee", ".IN");
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		seqs.store(tmpIn.getAbsoluteFile().toString());

	}

	private void fetchData() throws Exception
	{
		int     nSteps = 20;
		int     nSecs  = 30;
		boolean ready  = false;
		int     wait   = 0;
		for(int i=0;i<nSteps;i++)
		{

			if(i==10)
				nSecs=120;

			logger.info("waiting for result ("+wait+" sec)");
			if(isFinished(jobid))
			{
				ready = true;
				logger.info("finished");
			}	    
			if(ready)
				break;
			try
			{
				Thread.sleep(nSecs*1000);
			} 
			catch (InterruptedException e)
			{
				e.printStackTrace();
			}
			wait+=nSecs;
		}
		if(ready)
		{
			URL url = new URL(RESULTURL+jobid+".fasta_aln");
			BufferedReader in = new BufferedReader(new InputStreamReader(url.openStream()));
			resultseqs  = SequenceSet.readFromReader(in);
			//resultseqs.store("/tmp/result.afa");
			//msa = new MSA(rseqs);
		}

	}

	private void unwrapSequences()
	{
		msaseqs = new SequenceSet();
		for(Sequence s: resultseqs)
		{
			Sequence sclone = SeqTools.makeProteinSequence(masked_name_2_orig_name.get(s.getName()), s.seqString());
			msaseqs.add(sclone);
		}
		msa = new MSA(msaseqs);
	}

	private MSA msa;
	private SequenceSet msaseqs;
	private SequenceSet resultseqs;
	private String joburl = "";
}

