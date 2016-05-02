package whois;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

/**
 * Get info from current directory about domain
 * @author Happy
 *
 */
public class Domain {
	
	/** Local path of domain */
	private String path;
	
	/** Requested info. Must be sent to whois-client */
	private String info;
	
	/** Flag of availability current domain */
	private boolean isAvailable;
	
	/** Flag of existing current domain */
	private boolean isExist;
	
	/**
	 * Determine existing of info
	 * @param domain client's info
	 */
	public Domain(String domain) {
		path = convertFromDomainToPath(domain);
		isAvailable = path == null ? false : true;
		if (isAvailable == true) {
			try {
				info = readFile(path);
				isExist = true;
			} catch (IOException e) {
				isExist = false;
			}
		}			
	}
	
	/**
	 * getter for isExist
	 * @return isExists
	 */
	public boolean isExist() {
		return isExist;
	}
	
	/**
	 * getter for info
	 * @return info
	 */
	public String getInfo() {
		return info;
	}
	
	/**
	 * Reads file to String
	 * @param file filename
	 * @return content of file
	 * @throws IOException
	 */
	private static String readFile(String file) throws IOException {
		BufferedReader reader = new BufferedReader(new FileReader(file));
		String buffer = null;
	    StringBuilder stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");
	    while( ( buffer = reader.readLine() ) != null ) {
	        stringBuilder.append( buffer );
	        stringBuilder.append( ls );
	    }
	    
	    reader.close();
	    return stringBuilder.toString();
	}
	
	/**
	 * Converts client's request to local path of information
	 * @param domain client's request
	 * @return local path of information
	 */
	private String convertFromDomainToPath(String domain) {
		String filename = null;
		String[] parts = domain.split("[.]");
		if (parts.length != 0) {
			filename = new String("db\\");
			for (int i = parts.length - 1; i != 0; i--)
				filename += parts[i] + '\\';
			filename += parts[0] + ".txt";
		}
		System.out.println(filename);
		return filename;
	}	
}

