package fao.org.owl2skos;

import java.io.File;
import java.io.FileReader;
import java.util.Date;
import java.util.Properties;

import fao.org.owl2skos.examples.LoadAOSCommonFile;

public class RunConversion {

	public static void main(String[] args){
		
		String protegeDbConfigFile = "";
		String rootDir = "";
		String aosFile = "";
		String persistance1 = "";
		String persistance2 = "";
		String owlartImplClass = null;
		
		if (args.length < 5) 
		{
			System.out.println("usage:\n" +
			"java fao.org.owl2skos.RunConversion.Main <ProtegeDBConfigFile> <root-dir> <aos-file> <persistence-1> <persistence-2> [OWLART Implementing Class] \n" +
			"\t<ProtegeDBConfigFile>  : path to database configuration file \n" +
			"\t<root-dir>                     : path to root directory \n" +
			"\t<aos-file>                      : path to aos file\n" +
			"\t<persistance-1>                : 'true' or 'false\n" +
			"\t<persistance-2>                : 'true' or 'false'");
			return;
		}
		
		protegeDbConfigFile  	= args[0];
		rootDir 				= args[1];
		aosFile 				= args[2];
		persistance1			= args[3];
		persistance2			= args[4];
		if(args.length == 6)
			owlartImplClass = args[5];
		
		Properties props = new java.util.Properties();
		FileReader fileReader;
		try {
			fileReader = new FileReader(protegeDbConfigFile);
			props.load(fileReader);
		} catch (Exception e) {
			e.printStackTrace();
		}
		String dbTableName = props.getProperty("dbTableName");
		
		if(manageDir(rootDir, dbTableName) < 1)
		{
			System.out.println("Error cleaning/creating directory: " + dbTableName);
			return;
		}
		String format = "%-30s:%s\n";
		System.out.println("CONVERSION STARTING - " + new Date());
		System.out.format(format, "<ProtegeDBConfigFile>", protegeDbConfigFile);
		System.out.format(format, "<root-dir>", rootDir);
		System.out.format(format, "<aos file>", aosFile);
		System.out.format(format, "<persistence-1>", persistance1);
		System.out.format(format, "<persistence-2>", persistance2);
		System.out.format(format, "Processing Database", dbTableName);
		
		String[] args1 = {protegeDbConfigFile, rootDir+"/"+dbTableName+"/memorystore.nt", persistance1, rootDir+"/"+dbTableName};
		String[] args2 = {aosFile, rootDir+"/"+dbTableName+"/memorystore.nt", persistance2, rootDir+"/"+dbTableName};
		
		try {
			Date d1 = new Date();
			Main.main(args1);
			Date d2 = new Date();
			System.out.println("FIRST PHASE ENDED - TIME ELAPSED : " +  getTimeDifference(d2,d1));
			//LoadAOSCommonFile.main(args2);
			//Date d3 = new Date();
			
			//System.out.println("\n");
			//System.out.println("FIRST PHASE ENDED - TIME ELAPSED : " +  getTimeDifference(d2,d1));
			//System.out.println("SECOND PHASE ENDED - TIME ELAPSED : " +  getTimeDifference(d3,d2));
			//System.out.println("TOTAL TIME ELAPSED : " +  getTimeDifference(d3,d1));
		} catch (Exception e) {
			e.printStackTrace();
		}		
	}
	
	/**
	 * Get formatted time difference between two dates
	 * @param end
	 * @param start
	 * @return
	 */
	public static String getTimeDifference(Date end, Date start)
    {
        long lstart = start.getTime()/1000;
        long lend = end.getTime()/1000;

        long secsIn = (long) (lend - lstart); 
        
        long secondIn = 1;
        long minuteIn = secondIn * 60;
        long hourIn = minuteIn * 60;
        long dayIn = hourIn * 24;
        long yearIn = dayIn * 365;

        long diff = secsIn;
        long elapsedYears = diff / yearIn;
        diff = diff % yearIn;
        long elapsedDays = diff / dayIn;
        diff = diff % dayIn;
        long elapsedHours = diff / hourIn;
        diff = diff % hourIn;
        long elapsedMinutes = diff / minuteIn;
        diff = diff % minuteIn;
        long elapsedSeconds = diff / secondIn;
        
        return ( (elapsedDays < 10 ? "0" : "") + elapsedDays
        		+ ":" + (elapsedHours < 10 ? "0" : "") + elapsedHours
                + ":" + (elapsedMinutes < 10 ? "0" : "") + elapsedMinutes
                + ":" + (elapsedSeconds< 10 ? "0" : "") + elapsedSeconds );    

    }

	
	/**
	 * Manage directory
	 * @param rootDir
	 * @param dirName
	 * @return
	 */
	public static int manageDir(String rootDir, String dirName)
	{
		String target = rootDir+"/"+dirName;
		File file=new File(target);
		return file.isDirectory()? empty(target) : mkdir(target);   
	}
	/**
	 * Create a directory
	 * @param target
	 * @return
	 */
	public static int mkdir(String target)
	{
		boolean success = (new File(target)).mkdir();
		if(!success)
		{
			System.out.println("Failed creating directory: " + target);
			return -1;
		}
		else
			return 1;
	}
	/**
	 * Empty a given directory
	 * @param directory
	 */
	public static int empty(String directory)
	{
		File dir = new File(directory);
		if (!dir.exists()) {
			System.out.println(directory + " does not exist");
			return -1;
		}

		String[] info = dir.list();
		for (int i = 0; i < info.length; i++) 
		{
			File n = new File(directory + File.separator + info[i]);
			if (!n.isFile()) // skip ., .., other directories too
				continue;
			System.out.println("removing " + n.getPath());
			if (!n.delete())
				System.err.println("Couldn't remove " + n.getPath());
		}
		return 1;
	}
}

