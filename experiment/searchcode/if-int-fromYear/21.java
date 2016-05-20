import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

public class Saver implements Runnable {
	public static String newline = System.getProperty("line.separator");
	
	private File dataFile;
	private Hashtable<String, ArrayList<Integer>> ht;
	private int fromYear;
	private int toYear;
	
	public Saver(File dataFile, Hashtable<String, ArrayList<Integer>> ht, int fromYear, int toYear){
		this.ht = ht;
		this.dataFile = dataFile;
		this.fromYear = fromYear;
		this.toYear = toYear;
	}
	public void run() {
		FileOutputStream dataSt = null;
		OutputStreamWriter dataWr = null;		
		
		if(dataFile.exists())
			dataFile.delete();
		try
		{
			dataSt = new FileOutputStream(dataFile);
			dataWr = new OutputStreamWriter(dataSt);
			
			Vector<String> v = new Vector<String>(ht.keySet());
		    Collections.sort(v);
			
			dataWr.write("year;");
			Iterator<String> it = v.iterator();
		    while (it.hasNext()) {
		       String categ =  (String)it.next();
		       dataWr.write(categ + ";");
		    }
		    
		    for(int y=fromYear;y<=toYear;++y){
		    	dataWr.write(newline);
		    	
		    	dataWr.write(Integer.toString(y) + ";");
		    	it = v.iterator();
			    while (it.hasNext()) {
			       String categ =  (String)it.next();
			       ArrayList<Integer> ar = ht.get(categ);
			       if(y - fromYear < ar.size()) {
			    	   dataWr.write(ar.get(y - fromYear).toString());
			    	   dataWr.write(";");
			       }
			    }
		    }		    
		    
		    dataWr.close();
			dataSt.close();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			for (StackTraceElement el : ex.getStackTrace()) {
				System.err.println(el.toString());
			}
		}
	}

	
	public static void SaveDetailsInfo(String dir, String[] infos, IProvider prov){
		File dirFile = new File(dir);
		if(dirFile.exists() == false)
			dirFile.mkdirs();
		
		FileOutputStream dataSt = null;
		OutputStreamWriter dataWr = null;		
		
		try
		{			
			String fileName = infos[0];
			if(fileName == null)
				fileName = Integer.toString(dirFile.list().length + 1);
			else
				fileName = fileName.replace('\\', '_').replace(':', '_');			
			dataSt = new FileOutputStream(dir + File.separator + fileName + ".txt");
			dataWr = new OutputStreamWriter(dataSt);
			
			for(int idx=0;idx < infos.length;++idx) {
				if(idx != 0)
					dataWr.write(newline);
				String kw = prov.getKeyWordName(idx);
				if(kw.contains("Url"))
					dataWr.write(kw + ": " + prov.getURL(infos[idx]));
				else
					dataWr.write(kw + ": " + infos[idx]);				
			}
			
		    dataWr.close();
			dataSt.close();
		} catch (Exception ex) {
			System.err.println(ex.getMessage());
			for (StackTraceElement el : ex.getStackTrace()) {
				System.err.println(el.toString());
			}
		}
		
	}
}

