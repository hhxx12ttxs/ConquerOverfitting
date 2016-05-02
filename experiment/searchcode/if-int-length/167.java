/*
 Copyright (c) 2007-2009 WebAppShowCase DBA Appcloem (http://www.appcloem.com). All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

  1. Redistributions of source code must retain the above copyright notice,
     this list of conditions and the following disclaimer.

  2. Redistributions in binary form must reproduce the above copyright
     notice, this list of conditions and the following disclaimer in
     the documentation and/or other materials provided with the distribution.

  3. The names of the authors may not be used to endorse or promote products
     derived from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED WARRANTIES,
INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL WebAppShowCase
OR ANY CONTRIBUTORS TO THIS SOFTWARE BE LIABLE FOR ANY DIRECT, INDIRECT,
INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF
LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/
package tools.util;
 import java.util.*;
 import java.io.*;
 import java.util.jar.*;
 import java.util.zip.*;
 import java.net.*;
 import tools.io.*;
 

/**
 * This class is used for common methods.
 **/
//SharedMethods
public class FileUtil
{

	public static int bufferSize = 1024;
	static int ftreshHold = 100;
	static long pause = 500;
	
	public static String remove2Sep(String p){
		p = pathToOS(p);
		p = StringUtil.replaceSubstring(p,"//","[!]");	
		p = StringUtil.replaceSubstring(p,"[!]","/");
		p = StringUtil.replaceSubstring(p,"\\\\","[!]");
		p = StringUtil.replaceSubstring(p,"[!]","\\");
		return p;
	}
	
	
	
	public static Vector getFiles(String fs)throws Exception{
		return getFiles(fs,new AllFileNameFilter());

		
	}

	public static Vector getFiles(String fs,FilenameFilter ff)throws Exception{
		Vector v = new Vector();
		listFiles(fs,ff,v);
		return v;

		
	}


	
		 	public static void listFiles(String fs,FilenameFilter ff, Vector v)
	throws Exception
	{
			File f = new File(fs);
			/*if (f.isDirectory()){
				String fl = 	
			}*/
			if (f.getName().length() < 1)
				return;
      String[] flist = f.list(ff);
			if (flist == null || flist.length < 1)
		{
				if (!(f.isDirectory()))
					v.add(f.toString());
				return;
		}
			for(int i = 0 ; i < flist.length ; i++)
		{
				String p = f.getAbsolutePath();
				if (!p.endsWith("" +File.separatorChar))
					p = p + File.separatorChar;
				FileUtil.listFiles( p + flist[i],ff,v);
		}
			if (f != null)
			if (!(f.isDirectory()))
				v.add(f.toString());

	 		
	 }


	
	
	
	
	    public static void writeJar(File jarfile,String srcDir) throws Exception {

        JarOutputStream jarStream = null;
        
            String META_DIR  = "META-INF/";
 String MANIFEST  = META_DIR + "MANIFEST.MF";
        try {
            // clean the addedfiles Vector
            ArrayList addedfiles = new ArrayList();

            /* If the jarfile already exists then whack it and recreate it.
             * Should probably think of a more elegant way to handle this
             * so that in case of errors we don't leave people worse off
             * than when we started =)
             */
            if (jarfile.exists()) {
                jarfile.delete();
            }
            jarfile.getParentFile().mkdirs();
            jarfile.createNewFile();

            InputStream in = null;
            /*Manifest manifest = null;
            try {
                File manifestFile = (File) files.get(MANIFEST);
                if (manifestFile != null && manifestFile.exists()) {
                    in = new FileInputStream(manifestFile);
                } else {
                    String defaultManifest = "/org/apache/tools/ant/defaultManifest.mf";
                    in = this.getClass().getResourceAsStream(defaultManifest);
                    if (in == null) {
                        throw new BuildException("Could not find "
                            + "default manifest: " + defaultManifest);
                    }
                }

                manifest = new Manifest(in);
            } catch (IOException e) {
                throw new Exception ("Unable to read manifest", e, getLocation());
            } finally {
                if (in != null) {
                    in.close();
                }
            }*/

            // Create the streams necessary to write the jarfile

            jarStream = new JarOutputStream(new FileOutputStream(jarfile));
            jarStream.setMethod(JarOutputStream.DEFLATED);

            // Loop through all the class files found and add them to the jar
            //for (Iterator entryIterator = files.keySet().iterator(); entryIterator.hasNext();) 
            {
                //String entryName = (String) entryIterator.next();
                //if (entryName.equals(MANIFEST)) {
                   // continue;
                //}

                File entryFile =new File(srcDir);// (File) files.get(entryName);

				String entryName = "";

                //addFileToJar(jarStream, entryFile, entryName,addedfiles);

                // See if there are any inner classes for this class and add them in if there are
                //InnerClassFilenameFilter flt = new InnerClassFilenameFilter(entryFile.getName());
                File entryDir = entryFile.getParentFile();
                String[] innerfiles = entryDir.list();
                if (innerfiles != null) {
                    for (int i = 0, n = innerfiles.length; i < n; i++) {

                        //get and clean up innerclass name
                        int entryIndex = entryName.lastIndexOf(entryFile.getName()) - 1;
                        if (entryIndex < 0) {
                            entryName = innerfiles[i];
                        } else {
                            entryName = entryName.substring(0, entryIndex)
                                + File.separatorChar + innerfiles[i];
                        }
                        // link the file
                        entryFile = new File(srcDir, entryName);

                         addFileToJar(jarStream, entryFile, entryName,addedfiles);

                    }
                }
            }
        } catch (IOException ioe) {
            String msg = "IOException while processing ejb-jar file '"
                + jarfile.toString()
                + "'. Details: "
                + ioe.getMessage();
            throw new Exception(msg);
        } finally {
            if (jarStream != null) {
                try {
                    jarStream.close();
                } catch (IOException closeException) {
                    // ignore
                }
            }
        }
    }
	
		public static void dos2unix(String file)
  {
  	if (file.indexOf("*") > -1)return;
  	file = StringUtil.replaceSubstring(file,"dos2unix " ,"");
  	file = StringUtil.replaceSubstring(file,"dos2unix" ,"");
  	file = StringUtil.getTrimmedValue(file);
  	dos2unix(new File(file));
  }
	
	public static void dos2unix(File file)
  {
    if (file.isDirectory() || !file.exists())return;
    String name = file.getName();
    boolean found = false;

    /*for(int i = 0; i < extensions.length; i++)
      {
        if(name.endsWith(extensions[i]))
          {
            found = true;
            break;
          }
      }

    if(!found)
      {
        return;
      }*/


    //(file.getAbsolutePath());
    File tempFile = new File(file.getAbsolutePath() + ".tmp");

    try
      {
        BufferedReader in = new BufferedReader(new FileReader(file));
        BufferedWriter out = new BufferedWriter(new FileWriter(tempFile));
        int c;

        while((c = in.read()) != -1)
          {
            if(c != '\r')
              out.write(c);
          }

        in.close();
        out.close();

        file.delete();
        tempFile.renameTo(file);
      }
    catch(IOException e)
      {
        throw new RuntimeException(e.getClass() + e.getMessage());
      }
  }


	
	
	
	protected static void addFileToJar(JarOutputStream jStream,
                                File inputFile,
                                String logicalFilename,ArrayList addedfiles)
        throws Exception {
        FileInputStream iStream = null;
        try {
            if (!addedfiles.contains(logicalFilename)) {
                iStream = new FileInputStream(inputFile);
                // Create the zip entry and add it to the jar file
                ZipEntry zipEntry = new ZipEntry(logicalFilename.replace('\\', '/'));
                jStream.putNextEntry(zipEntry);

                // Create the file input stream, and buffer everything over
                // to the jar output stream
                byte[] byteBuffer = new byte[2 * 1024];
                int count = 0;
                do {
                    jStream.write(byteBuffer, 0, count);
                    count = iStream.read(byteBuffer, 0, byteBuffer.length);
                } while (count != -1);

                //add it to list of files in jar
                addedfiles.add(logicalFilename);
           }
        } catch (IOException ioe) {
            tools.util.LogMgr.err("Filetil.addFileToJar " + ioe);
            ioe.printStackTrace();
        } finally {
            // Close up the file input stream for the class file
            if (iStream != null) {
                try {
                    iStream.close();
                } catch (IOException closeException) {
                    // ignore
                }
            }
        }
    }

	
	
	
	
	
	
	
		public static boolean contains (String f,String t)throws Exception{
			if( new File (f).exists() && new File(f).length() > 0)
			{
			if (new File(f).length() < 999999)
			{
				Template te = new Template(f);
				if (te.indexOf(t) > -1)return true;
			}
			else
			{
				if (SharedMethods.linux())
				{
					String u = tools.util.NDemon.execToTemp("tail -999999 " + f + " | grep \"" + t + "\"");	
					if (StringUtil.isRealString(u) && u.indexOf(t) > -1)return true;
				}
			}
			}
			return false;	
		}
	
	
				//FileUtil.httpDownload
			public static boolean download(String n,String f)throws Exception{
				return download(n,f,1500);
			}
		public static boolean download(String n,String f,int tou)throws Exception{
		//String os = System.getProperty("os.name");
		/*if (os.toLowerCase().indexOf("linux") > -1 && n.indexOf("appcloem.com") < 1){
			tools.util.NDemon.execWait("wget " + n + " -O " + f,tou);
		
			long l = new File(f).length();
			while (true ){
			Thread.sleep(15000);
			if (new File(f).length() == l){
				//(l + "NEW FILE LENGTH LOOKS GOOD " + new File(f).length());
				break;
			}
			l = new File(f).length();
			//(f + " STILL DOWNLOADING " + l);

				
			}
		}*/
			//else
			return httpDownload(n,f);
		}
	
	public static String pathToWin(String p){
		return StringUtil.replaceSubstring(p,"/","\\");	
	}
	public static String sysFile(String f){
		return winSysFile(f);
	}

	public static String winSysFile(String f){
	
			if (new File ("C:\\WINDOWS\\SYSTEM32\\"  + f).exists() )
			return "C:\\WINDOWS\\SYSTEM32\\"  + f;
		if (new File ("C:\\I386\\"  + f).exists() )
			return "C:\\I386\\"  + f;
		if (new File ("C:\\WINDOWS\\ServicePackFiles\\i386\\"  + f).exists() )
			return "C:\\WINDOWS\\ServicePackFiles\\i386\\"  + f;
		if (new File ("C:\\WINDOWS\\SYSTEM32\\DLLCACHE\\"  + f).exists() )
			return "C:\\WINDOWS\\SYSTEM32\\DLLCACHE\\"  + f;
		if (new File ("C:\\WINDOWS\\SYSTEM32\\WBEM\\"  + f).exists() )
			return "C:\\WINDOWS\\SYSTEM32\\WBEM\\"  + f;

		return 	f;
	}

	public static String pathToOS(String p){
		if (SharedMethods.windows())
		 return pathToWin(p);
		else if (SharedMethods.linux())
		 return pathToLinux(p);		
		return p;
	}	
	
		public static String pathToLinux(String p){
		p =  StringUtil.replaceSubstring(p,"\\","/");
		p =  StringUtil.replaceSubstring(p,"C:","");
		p =  StringUtil.replaceSubstring(p,"c:","");
		p =  StringUtil.replaceSubstring(p,"D:","");
		p =  StringUtil.replaceSubstring(p,"d:","");
		return p;	
	}
	
/*	public static boolean httpDownload(String url,String tf)throws Exception{
		if (!url.startsWith("http"))return false;
		//(tf + " Start the Download " + url);

//		URL connect = new URL(url);
  //      HttpConnection http = new HttpConnection(connect);
    //    http.setDebug(true);
      //  http.download(tf);

				URLReader ur = new URLReader(url);
				try{
				ur.download(tf);
				}catch (Exception nf){
					if ((nf instanceof java.io.FileNotFoundException) || (nf instanceof java.net.UnknownHostException) || (nf instanceof java.net.ConnectException)){
					tools.util.LogMgr.err("FileUtil.httpDownload " + nf.toString());
					return false;
				}
				throw nf;
				}
			//(tf + " END the Download " + url);
			
        if (new File(tf).length() < 1)return false;
        return true;
		
	}*/
	
	
		public static boolean httpDownload(String url,String tf)throws Exception{
		if (!url.startsWith("http"))return false;

			org.apache.tools.ant.taskdefs.Get ur = new org.apache.tools.ant.taskdefs.Get();
				try{
					ur.setSrc(new URL(url));
					ur.setDest(new File(tf));
					ur.execute();
					//(url + " Downloaded to " + tf);
				}catch (Exception nf){
					if ((nf instanceof java.io.FileNotFoundException) || (nf instanceof java.net.UnknownHostException) || (nf instanceof java.net.ConnectException)){
					tools.util.LogMgr.err("FileUtil.httpDownload " + nf.toString());
					return false;
				}
				throw nf;
				}
			//(tf + " END the Download " + url);
			
        if (new File(tf).length() < 1)return false;
        return true;
		
	}
	
	
	public static void antUnJar(String src,String dest)throws Exception{
	
/*	import org.apache.tools.ant.Project;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.taskdefs.Expand;
import org.apache.tools.ant.taskdefs.Zip;
import org.apache.tools.ant.types.*;*/
		
			    class jExpander extends org.apache.tools.ant.taskdefs.Expand {
        public jExpander() {
 	    project = new org.apache.tools.ant.Project();
	    project.init();
	    taskType = "unzip";
	    taskName = "unzip";
	    target = new org.apache.tools.ant.Target();
	}	
    }
    jExpander expander = new jExpander();
    expander.setSrc(new File(src));
    new File(dest).mkdirs();
    expander.setDest(new File(dest));
    expander.execute();
	}
	
public static void unJar(String jar,String dir) throws
Exception{
	if (jar.endsWith("tar.gz") || jar.endsWith(".tgz"))
	{
		unTarGZ(jar,dir);	
		return;
	}
	unzip(jar,dir);
    }

public static void unTarGZ(String jar,String dir) throws
Exception{
		tools.util.NDemon.execWait("cd " + dir + "\r\ntar zxf " + jar,18);

    }

public static void unJar(File jar,File
dir) throws
Exception{
	unzip(jar.toString(),dir.toString());
    }


    public static void deleteAll(File f)
throws Exception
{
	deleteAll(f,new AllFileNameFilter(),true);
}
	public static void zip(String path,String source,String target)
	throws IOException
{
	zip( path, source, target,900000000L);
}
	public static void zip(String path,String source,String target,long ml)
	throws IOException
{
	ZipOutputContainer out = new ZipOutputContainer(target);
	LongNum lm = new LongNum();
	zip(path,source,target,out,lm,ml,new StringBuffer());
	out.close();
}

	public static void zip(String path,String source,String target,long ml,StringBuffer msg)
	throws IOException
{
	ZipOutputContainer out = new ZipOutputContainer(target);
	LongNum lm = new LongNum();
	zip(path,source,target,out,lm,ml,msg);
	out.close();
}


	public static void zip(String source,String target)
	throws IOException
{
	File sf = new File(source);
	if (!sf.exists())return;
	if (!sf.isDirectory())
	{
		zipFile(source,target);
		return;
	}
	
	String path = sf.toString();
	int i = path.lastIndexOf("/");
	if (i < 0) i = path.lastIndexOf("\\");
	if (i > -1)
		path = path.substring(0,i + 1);
	long myl = 9000000000L;
	zip( path, source, target,myl);
}
	public static void zipFile(String source,String target)
	throws IOException
	{
		ZipOutputContainer outc = new ZipOutputContainer(target);

		zipFile(new File(source),outc);
		outc.close();
	}

	public static void zipFile(File nf,ZipOutputContainer outc)
	throws IOException
	{
	//File ft = new File (target);	
	ZipEntry ze = null;	
	String p = null;
	//File nf = null;
	InputStream b = null;
	ZipOutputStream out = null;
	String zfn;
	try{


					zfn = nf.toString();
					int i = zfn.lastIndexOf("/");
					if (i < 0) i= zfn.lastIndexOf("\\");
					if (i > -1)
						zfn = zfn.substring(i + 1,zfn.length());
					ze = new ZipEntry(zfn);
					ze.setTime(nf.lastModified());

						b = new FileInputStream(nf);

						out = outc.getOut();
							out.putNextEntry(ze);
							StreamUtil.write(b,out);

					b.close();


					out.closeEntry();


	}finally {
		if (out != null)
		try{
			out.close();
		}catch (Exception io){}
		
	}
		
	}









	 	public static void unzip(String in,String dir)
	throws Exception
{
	unzip (new File(in),dir);
}

	 	public static void unzip(File in,String dir)
	throws Exception
{
	FileInputStream fin = new FileInputStream(in);
	try{
	unzip (fin,dir);
	}finally{
	fin.close();
	}
}

	
		 	public static void gunzip(InputStream fis,String dir)
	throws Exception
	{
    
    	File dest = new File(dir);
 
             FileOutputStream out = null;
            GZIPInputStream zIn = null;
            try {
                out = new FileOutputStream(dest);
                zIn = new GZIPInputStream(fis);
                byte[] buffer = new byte[8 * 1024];
                int count = 0;
                do {
                    out.write(buffer, 0, count);
                    count = zIn.read(buffer, 0, buffer.length);
                } while (count != -1);
            } 
            	 finally {
                fis.close();
                out.close();
                zIn.close();
            }
       
       
    }





	 	public static void unzip(InputStream in,String dir)
	throws Exception
	{


			ZipInputStream	jarIn = new ZipInputStream(in);
			try{
			ZipEntry jarEnty = null;
			File jarFile = null;
			int ct = 0;
			//(in + " jarFile.toString() 0 " + dir);
			
			while ((jarEnty = jarIn.getNextEntry()) != null) {
			//(" getNextEntry.toString() 1 " + dir);
			
				jarFile = new File(dir, StringUtil.replaceSubstring(jarEnty.getName(),"\\","/"));
				//(FileName.isValid(jarFile.toString()) + " jarFile.toString() 1 " + jarFile.toString());
				
				if (jarEnty.isDirectory()) {
				    jarFile.mkdirs(); 
					jarFile.setLastModified(jarEnty.getTime());
				    //(" UZMDIR + " + jarFile);
					
				} else {
					//(jarFile.getParent() + " UNZ " + new File (jarFile.getParent()).exists() + ":" + jarFile);
					if (!new File (jarFile.getParent()).exists())
						new File (jarFile.getParent()).mkdirs();
				    byte[] buffer = new byte[FileUtil.bufferSize];
				    int length = 0;
					//(FileName.isValid(jarFile.toString()) + " jarFile.toString() " + jarFile.toString());
					if (FileName.isValid(jarFile.toString()))
					{
					    FileOutputStream fos = new FileOutputStream(jarFile);
						try{
					    while ((length = jarIn.read(buffer)) >= 0) {
						fos.write(buffer, 0, length);
					    }
					    }finally{
					    fos.close();
						}
						jarFile.setLastModified(jarEnty.getTime());
					}
				}
			}
		}finally{
			jarIn.close();
			}
			}


//}




	public static void zip(String path,String source,String target,ZipOutputContainer outc,LongNum lm)
	throws IOException
	{
		zip(path,source,target,outc,lm,20000000,new StringBuffer());
	}

	public static void zip(String path,String source,String target,ZipOutputContainer outc,LongNum lm,long ml)
	throws IOException
	{
		zip(path,source,target,outc,lm,ml,new StringBuffer());
	}

	public static boolean startsWith(String f,String t){
		if (!FileName.isValid(t))return false;
		if (!t.startsWith(f))return false;
		return true;
	}

	public static void zip(String path,String source,String target,ZipOutputContainer outc,LongNum lm,long ml,StringBuffer msg)
	throws IOException
	{
	
		File f = new File(source);
	String[] flist = f.list();
	String zfn = null;
	if (flist == null || flist.length < 1)
		return;
	File ft = new File (target);	
	ZipEntry ze = null;	
	String p = null;
	File nf = null;
	InputStream b = null;
	ZipOutputStream out = null;
	try{
	for(int i = 0 ; i < flist.length ; i++)
{
		p = f.getAbsolutePath();
		if (!p.endsWith("" + File.separatorChar))
			p = p + File.separatorChar;
		nf = new File (p + flist[i]);
		if (nf.isDirectory())
		{
			out = outc.getOut();
			zfn = nf.toString();
					zfn = zfn.substring(path.length(),zfn.length()	);
					if (zfn.startsWith("/") || zfn.startsWith("\\"))
						zfn = zfn.substring(1,zfn.length());
					if (!zfn.endsWith("/"))
						zfn = zfn + "/";	
					ze = new ZipEntry(zfn);
					ze.setTime(nf.lastModified());
			//(ze + " ZLDIR + " + zfn);
			out.putNextEntry(ze);
			out.closeEntry();
			zip(path,p + flist[i] , target,outc,lm,ml,msg);
		
		}
		else
		{
			if (flist[i].equals("core") && nf.length() > 1000000)
			{
				nf.delete();
				tools.util.LogMgr.debug("******* Deleted existing core from context dir: " + nf);
				msg.append("******* Deleted existing core from context dir: " + nf + "\r\n");
			}
			else
			{
				if (!ft.getParent().equals(nf.getParent()))
				{
					zfn = nf.toString();
					zfn = zfn.substring(path.length(),zfn.length()	);
					if (zfn.startsWith("/") || zfn.startsWith("\\"))
						zfn = zfn.substring(1,zfn.length());
					//ze = new ZipEntry(zfn);
					ze = new ZipEntry(zfn.replace('\\', '/'));
					ze.setTime(nf.lastModified());
					try{
						try{
						b = new FileInputStream(nf);
						}catch (FileNotFoundException fe){
							tools.util.LogMgr.err(nf + " FileUtil.zip " + fe.toString());
							b = null;//new ByteArrayInputStream(new byte[0]);
							msg.append("Unable to Archive the Following File " + nf + "\r\n");
						}
						out = outc.getOut();
						if (ml > 0 && lm.value > ml)
						{
							out.close();
							File ffd = new File (ft.getParent());
							int ct = ffd.list().length + 1;
							target = target.substring(0,target.length() - 4);
							target = target + ct + ".zip";
							out = new ZipOutputStream(new FileOutputStream(new File(target)));
							outc.setOut(out);
				
							//out = new JarOutputStream(new FileOutputStream(new File(target)));
							//out.setMethod(JarOutputStream.DEFLATED);
							//outc.setOut(out);
							
						//	        jarStream = new JarOutputStream(new FileOutputStream(jarfile));
            //jarStream.setMethod(JarOutputStream.DEFLATED);
							
							lm.value = 0;
							
						}
						if (b != null)
						{
							lm.increment(nf.length());
							out.putNextEntry(ze);
							StreamUtil.write(b,out);
						}
					}finally{
					if (b != null)
					b.close();
					}
					
					//out.write(b,0,b.length);
					out.closeEntry();
				}
				

			}
		}
		
		}
	}catch (Throwable e){
		if (out != null)
		try{
			out.close();
		}catch (Exception io){}
	}
		
	}






	 	public static void deleteAll(File f,FilenameFilter ff,boolean tf)
	throws Exception
	{
		JRM.rm.remove(f.toString(),ff,tf);	 		
	 }
	 
	 public static void moveAll(String from,String t)
	throws Exception
	{
		File f = new File(from);
	 	if (f.getName().length() < 1)
	 		return;
		moveFilesInDir(from,t);
		new File(t + f.getName()).delete();
	 	
	}
	
	public static void moveFilesInDir(String from,String t)
	throws Exception
	{
	File f = new File(from);
	String[] flist = f.list();
	
	if (flist == null || flist.length < 1)
		return;
	for(int i = 0 ; i < flist.length ; i++)
{
		String p = f.getAbsolutePath();
		if (!p.endsWith("" + File.separatorChar))
			p = p + File.separatorChar;
		File nf = new File (p + flist[i]);
		String nt = t;
		boolean isd = false;
		if (nf.isDirectory())
		{
			nt = nt + 	flist[i] + File.separator;
			new File(nt).mkdirs();
			isd = true;
			moveFilesInDir(p + flist[i] , nt);
			nt = nt + flist[i];
			File nf2 = new File(nt);
			if (nf2.isDirectory())
				nf2.delete();
			nf.delete();	
		}
		else
			nf.renameTo(new File(t + nf.getName()));
		
		
}
	
		
	}
	
	
	private static void moveAllFiles(String from,String t)
	throws Exception
	{
	File f = new File(from);
		if (f.getName().length() < 1)
			return;
	String[] flist = f.list();
		if (flist == null || flist.length < 1)
	{
			f.renameTo(new File(t + f.getName()));
			return;
	}
		for(int i = 0 ; i < flist.length ; i++)
	{
			String p = f.getAbsolutePath();
			if (!p.endsWith("" + File.separatorChar))
				p = p + File.separatorChar;
			File nf = new File (p + flist[i]);
			String nt = t;
			if (nf.isDirectory())
				nt = nt + 	flist[i] + File.separator;
			moveAllFiles( p + flist[i] , nt );
	}
		if (f != null)
			f.renameTo(new File(t + f.getName()));
	}
	
	
	
	
	 
	
		 	public static void deleteAll(String f)
	throws Exception
	{
		deleteAll(new File(f),new AllFileNameFilter(),true);
	}
	public static void deleteAll(String f,boolean tf)
throws Exception
{
deleteAll(f,new AllFileNameFilter(),tf);
}
	
	
		public static void deleteAll(String f,FilenameFilter ff)
	throws Exception
	{
	deleteAll(new File(f),ff,true);
	}

	public static void deleteAll(String f,FilenameFilter ff,boolean tf)
throws Exception
{
	deleteAll(new File(f),ff,tf);
}


	
	
	
			   public static Hashtable checkForFiles(String path,String ext)
     {
     try
     {
         File f = new File(path);
        String[] flist = f.list();

        Hashtable ch = new Hashtable(flist.length);
        for(int i = 0 ; i < flist.length ; i++)
          if (flist[i].toLowerCase().endsWith("." + ext))
            ch.put(flist[i].substring(0,flist[i].indexOf("." + ext)),flist[i]);
     		return ch;

     }
      catch (Exception e)
      {
        print(e);
      	return null;
      }
     }
	 
	
				   public static Hashtable checkForFiles(String path)
     {
     try
     {
         File f = new File(path);
        String[] flist = f.list();

        Hashtable ch = new Hashtable(flist.length);
        for(int i = 0 ; i < flist.length ; i++)
        {
        if (flist[i].indexOf("." ) > -1)
      ch.put(flist[i].substring(0,flist[i].indexOf("." )),flist[i]);
  	else
      ch.put(flist[i],flist[i]);
        }
     		return ch;
     }
      catch (Exception e)
      {
        print(e);
      	return null;
      }
     }

	
    /**
     * Used for reading files.
     **/
    public static String getStringFromFile(String s,String dir,String path)
    	throws IOException
	{
			return getStringFromFile(path + dir + s);
    }


    public static String getStringFromFile(String s,boolean tf)
	    	throws IOException

    {
    	if (tf && !new File(s).exists())return null;
        return new String(getBytesFromFile(s));
    }
	
	
	    public static String getStringFromFile(String s)
	    	throws IOException

    {
        return new String(getBytesFromFile(s));
    }
	
		    public static String getStringFromFile(File f)
	    	throws IOException

    {
        return new String(getBytesFromFile(f));
    }


		    public static StringBuffer getStringBufferFromFile(String s)
	    	throws IOException

    {
        return new StringBuffer(new String(getBytesFromFile(s)));
    }


	
		    public static byte[] getBytesFromFile(String s)
		    	throws IOException
    {
            return getBytesFromFile(new File(FileName.makeValid(s)));
    }

			    public static byte[] getBytesFromFile(File f)
		    	throws IOException
    {
            InputStream din = null;
			try{
            byte[] b = null;
            din =  new BufferedInputStream(new FileInputStream(f));
            return StreamUtil.readStream(din,(int)f.length());
            }
            finally{
            	if (din != null)
            	try{
            		din.close();
            	}catch (java.io.IOException ioe){
            		}
            }
			
    }
    
         		    public static String getStringFromFile(String s,LongNum p)
		    	throws IOException
    {

            return new String(getBytesFromFile(s,p));
    }
    
    
    
        		    public static byte[] getBytesFromFile(String s,LongNum p)
		    	throws IOException
    {
    		File f = new File(FileName.makeValid(s));
            byte[] b = getBytesFromFile(f,p.value);
            p.value = f.length();
            return b;
    }
    
    
    		    public static byte[] getBytesFromFile(String s,long p)
		    	throws IOException
    {
            return getBytesFromFile(new File(FileName.makeValid(s)),p);
    }

			    public static byte[] getBytesFromFile(File f,long p)
		    	throws IOException
    {
			
            
            
            RandomAccessFile rf =  new RandomAccessFile(f,"r");
            rf.seek(p);
            int len = (int)(rf.length() - p);
            if (len < 1)len = 0;
            byte[] b = new byte[len];
            if (len < 1)return b;
            rf.readFully(b, 0, len);
            rf.close();
            return b;
            //din =  new BufferedInputStream(new FileInputStream(f));
            //return StreamUtil.readStream(din,(int)f.length() - p);
    
			
    }
    
    
    	/*		    
		    	throws IOException
    {
			
            
            
            if (p > 0)p = p-1;
            rf.seek(p);
            int len = (int)(rf.length() - p);
            if (len < 1)len = 0;
            byte[] b = new byte[len];
            if (len < 1)return b;
            rf.readFully(b, p, len);
            return b;
            //din =  new BufferedInputStream(new FileInputStream(f));
            //return StreamUtil.readStream(din,(int)f.length() - p);
    
			
    }*/
    
 


	

	
	private static NameValuePairs m_files = new NameValuePairs(5);
	
	public static RandomAccessFile  getFile(String fullname)
	{
		try
      {
					return (RandomAccessFile)m_files.get(fullname);
      }
      catch (Exception e)
      {
      		print(e);
      		return null;
      }
	}

	public static RandomAccessFile getRandomAccessFile(String fullname)
	{
        try
        {
	        	File f = new File( fullname);
						return new RandomAccessFile(f,"rw");
        }
        catch (Exception e)
        {
            print(e);
        		return null;
        }
	}
	
			    public static void appendToFile(RandomAccessFile lw,StringBuffer appendto)
    {
				appendToFile(lw,appendto.toString());
    }

	
		    public static void appendToFile(RandomAccessFile lw,String appendto)
    {
        try
        {
        	  lw.seek(lw.length());
	        	lw.writeBytes(appendto);
        }
        catch (Exception e)
        {
            print(e);
        }
    }

		    public static void appendToFile(String fullname,StringBuffer appendto,boolean keepopen)
    {
	     appendToFile( fullname, appendto.toString(), keepopen);
    }
	
	    public static void appendToFile(String fullname,String appendto,boolean keepopen)
    {
        RandomAccessFile lw = null;
		try
        {
		
        	lw = getFile(fullname);
        		if(lw == null )
	        {	
	        	File f = new File( fullname);
						lw = new RandomAccessFile(f,"rw");
	        	if (keepopen)
	        		m_files.put(fullname,lw);
	        }
        	  lw.seek(lw.length());
	        	lw.writeBytes(appendto);
        }
        catch (Throwable e)
        {
            print(e);
        }
        finally{
        	if (lw != null && !keepopen)
        	try{
        		lw.close();
        	}catch (java.io.IOException ioe){
        		}
        }
		
    }

		    public static void appendToFile(String fullname,String appendto)
    {
				appendToFile(fullname,appendto,false);
    }


			public static void print(Throwable e)
	{
		e.printStackTrace();
	}
    /**
     * Used for reading files.
     **/
    public static void writeToFile(String s,String dir,String filename)
    {
		writeToFile(s,dir + filename);		
    }
	
    public static void writeToFile(String s,String filename)
    {
        DataOutputStream din = null;
    	try
        {
            File f = new File( filename);
            din = new DataOutputStream(new FileOutputStream(f));
            din.write(s.getBytes());
        }
        catch (Throwable e)
        {
            print(e);
        }
        finally{
        	if (din != null)
    		try{
        		din.close();
    		}catch (java.io.IOException ioe){
    			}
        }
    	
    }
	


    public static boolean writeBytesToFile(String filename,byte[] b,boolean tf)
    {
		if (tf)
		{
		int i = filename.lastIndexOf(File.separator);
		int	i2 = filename.lastIndexOf("/");
		if (i2 > i)
			i = i2;
		if (i > -1)
			new File(filename.substring(0,i + File.separator.length())).mkdirs();
		}
		return writeBytesToFile(filename,b);
    }

    public static boolean writeBytesToFile(String filename,byte[] b)
    {
    	return writeBytesToFile(new File(filename),b);
    }

    public static void copy(String from,String to)
    	throws IOException
{
	copy (new File(from),new File(to));
}


             static  File getFile(String pid ,String relativePath) {
                return new File(new File("/proc/" + pid), relativePath);
            }
         public static String readLinuxProcFile(String pid,String f)throws Exception {
                  String line = "";
                     String line2 = "";
              
                BufferedReader r = new BufferedReader(new FileReader(getFile(pid,f)));
                try {
                    
                    while ((line = r.readLine()) != null) {
                    	line2 = line2 + line;
                        //line = line.toLowerCase(Locale.ENGLISH);
                        //(pid + " RL " + line);
                        /*if (line.startsWith("ppid:")) {
                            ppid = Integer.parseInt(line.substring(5).trim());
                            break;
                        }*/
                    }
                } finally {
                    r.close();
                }
          return line2;
            }


		public static void copy(File from,File to)
			throws IOException
	{
		if (from.isDirectory())
		{
			copyFilesInDir(from,to);
			return;
		}
		//|| from.length() < 1
		if(!from.exists() )return;
		FileOutputStream fout = new FileOutputStream(to);
		FileInputStream fin = new FileInputStream(from);
		StreamUtil.write(fin,fout);
		fin.close();
		fout.close();
	}
	
	public static void copyFilesInDir(File from,File to)throws IOException{
		 copyFilesInDir(from,to.toString());
	}
	
	public static void copyFilesInDir(String from,String to)throws IOException{
		 copyFilesInDir(new File(from),to);
	}
	
	
	public static void copyFilesInDir(File f,String t)
	throws IOException
	{
		JCP.cp.copy(f.toString(),t);
		
	}
	
	public static void dirSize(String fs,LongNum c)
	throws IOException
	{
		JDU.du.dirSize(fs,c);
	}	

	public static boolean winDelete(String f)throws Exception{
		return winDelete(new File(f),100);
	}
	
	public static boolean winDelete(File f)throws Exception{
		return winDelete(f,100);
	}
	public static boolean winDelete(File f,int trs)throws Exception{
		for (int ct = 0; ct <trs;ct++){
			if (!f.exists() || f.delete())
				return true;
			//(f + " try delete again afate wait " + ct);
			Thread.sleep(100);
		}
		return false;
	}

	public static void dirSize(String fs,LongNum c,LongNum lm)
	throws IOException
	{
		JDU.du.dirSize(fs,c,lm);
	}	
	

    public static boolean writeBytesToFile(File f,byte[] b)
    {
		DataOutputStream din = null;
        try
        {
            din = new DataOutputStream(new FileOutputStream(f));
            din.write(b);
			return true;
        }
        catch (Throwable e)
        {
            print(e);
			return false;
        }
		finally{
			if (din != null)
		try{
			din.close();
		}catch (java.io.IOException ioe){
			}
		}
    }

    public static boolean writeTextToFile(File f,String b)
    {
		FileWriter din = null;
        try
        {
	      for (int ct = 0;ct < 10;ct++){
	        try
	        {
	            din = new FileWriter(f);
	            din.write(b);
				return true;
			}
			catch (Throwable e1)
	        {
	        	//being used by another process
	            if (e1.toString().indexOf("being used by another process") < 0 && e1.toString().indexOf("Text file busy") < 0 && e1.toString().indexOf("Access is denied") < 0  && e1.toString().indexOf("The requested operation cannot be performed on a file with a user-mapped section open") < 0)
	            {
	          	            tools.util.LogMgr.err(f + " FileUtil.writeTextToFile ERROR " + e1.toString());

	            	throw e1;
	         }
	            tools.util.LogMgr.red(f + " FileUtil.writeTextToFile TRYING AGAIN " + e1.toString());
	        	Thread.sleep(1000);
	        }
	     }
	     throw new IOException("Out of tries");
        }
        catch (Throwable e)
        {
            tools.util.LogMgr.err(f + " FileUtil.writeTextToFile " + e.toString());
            //print(e);
			return false;
        }
		finally{
			if (din != null)
		try{
			din.close();
		}catch (java.io.IOException ioe){
			}
		}
    }
	

	
	
	
    /**
     * Used for reading files.
     **/
    public static String getStringFromFile(String s,String dir)
    	throws IOException
    {
			return getStringFromFile(dir + s);
    }


}









class JRM{

	static JRM rm = null;
	

	static{
		String os = System.getProperty("os.name");
		//if (os.toLowerCase().indexOf("linux") > -1)
		//	rm = new LRM();
		//else
			rm = new JRM();
	}

	public void remove(String fs)
	throws Exception
	{
	remove(fs,new AllFileNameFilter());
}
	 	public void remove(String fs,FilenameFilter ff)throws Exception
{
	 remove(fs,ff,true);
}

	 	public void remove(String fs,FilenameFilter ff,boolean tf)
	throws Exception
	{
			File f = new File(fs);
			/*if (f.isDirectory()){
				String fl = 	
			}*/
			if (f.getName().length() < 1)
				return;
      String[] flist = f.list(ff);
			if (flist == null || flist.length < 1)
		{
				if (!(f.isDirectory() && !tf))
					f.delete();
				return;
		}
			for(int i = 0 ; i < flist.length ; i++)
		{
				String p = f.getAbsolutePath();
				if (!p.endsWith("" +File.separatorChar))
					p = p + File.separatorChar;
				FileUtil.deleteAll( p + flist[i],ff,tf);
		}
			if (f != null)
			if (!(f.isDirectory() && !tf))
				f.delete();
	 		
	 }


}


class JCP{

	static JCP cp = null;
	

	static{
		String os = System.getProperty("os.name");
		//if (os.toLowerCase().indexOf("linux") > -1)
		//	cp = new LCP();
		//else
			cp = new JCP();
	}

	public void copy(String fs,String t)
	throws IOException
	{
	if (new File(t).isDirectory() && !t.endsWith(File.separator))
		t = t + File.separator;
	
		File f = new File(fs);
	String[] flist = f.list();
	
	if (flist == null || flist.length < 1)
		return;
	for(int i = 0 ; i < flist.length ; i++)
{
		String p = f.getAbsolutePath();
		if (!p.endsWith("" + File.separatorChar))
			p = p + File.separatorChar;
		File nf = new File (p + flist[i]);
		String nt = t;
		boolean isd = false;
		if (nf.isDirectory())
		{
			nt = nt + 	flist[i] + File.separator;
			new File(nt).mkdirs();
			isd = true;
			copy(p + flist[i] , nt);
			nt = nt + flist[i];
			File nf2 = new File(nt);
		}
		else
		try
		{
			FileUtil.copy(nf,new File(t + nf.getName()));
		}catch (IOException ed){
			if (ed.toString().indexOf("Disk quota exceeded") > -1)
				throw ed;
			System.out.println(nf + " Error:" + ed.toString() + " Copying File to " + t + nf.getName());

		}
		//new File(t + f.getName()).delete();
		
}
	
		
	}



}

class JDU{

	static JDU du = null;
	

	static{
		String os = System.getProperty("os.name");
		//if (os.toLowerCase().indexOf("linux") > -1)
		//	du = new LDU();
		//else
			du = new JDU();

	}

	void dirSize(String fs,LongNum c)
	throws IOException
	{
		File f = new File(fs);
	String[] flist = f.list();
	
	if (flist == null || flist.length < 1)
		return;
	for(int i = 0 ; i < flist.length ; i++)
{
		String p = f.getAbsolutePath();
		if (!p.endsWith("" + File.separatorChar))
			p = p + File.separatorChar;
		File nf = new File (p + flist[i]);
		if (nf.isDirectory())
			dirSize(p + flist[i] , c);
		else
		{
			if (flist[i].equals("core") && nf.length() > 1000000)
			{
				nf.delete();
				tools.util.LogMgr.debug("******* Deleted existing core from context dir: " + nf);
			}
			else	
				c.increment(nf.length());
		}
		
		//new File(t + f.getName()).delete();
		
}
	
		
	}


	void dirSize(String fs,LongNum c,LongNum lm)
	throws IOException
	{
		File f = new File(fs);
	String[] flist = f.list();
	
	if (flist == null || flist.length < 1)
		return;
	long lmod = 0;	
	for(int i = 0 ; i < flist.length ; i++)
{
		String p = f.getAbsolutePath();
		if (!p.endsWith("" + File.separatorChar))
			p = p + File.separatorChar;
		File nf = new File (p + flist[i]);
		if (nf.isDirectory())
			dirSize(p + flist[i] , c,lm);
		else
		{
			if (flist[i].equals("core") && nf.length() > 1000000)
			{

				nf.delete();
				tools.util.LogMgr.debug("******* DELETED existing core from context dir: " + nf);
			}
			else	
			{
				c.increment(nf.length());
				lmod = nf.lastModified();
				if (lmod > lm.value)
					lm.value = lmod;
			}
		}
		
		//new File(t + f.getName()).delete();
		
}
	
		
	}




}

class LDU extends JDU{

	void dirSize(String fs,LongNum c)
	throws IOException
	{
		LineNumberReader lr = null;
		Process p = null;
		try{
		File cor = new File(fs + "core");
		if (cor.exists())
			cor.delete();	
			p = new tools.util.NProcess(Runtime.getRuntime().exec("du -s -b " + fs));
			InputStream in = p.getInputStream();
			lr = new LineNumberReader(new InputStreamReader(in));
			StringBuffer l = new StringBuffer();
			String s = null;
			while ((s = lr.readLine()) != null)
				l = l.append(s);
			StringTokenizer sr = new StringTokenizer(l.toString());	
			if (sr.hasMoreElements())
			{
				s = sr.nextToken();
				c.increment(Integer.parseInt(s));
			}
			Thread.currentThread().sleep(FileUtil.pause);
				
		}catch (Exception ex){
			throw new IOException(fs + " LDU: " + ex.toString());	
		}	
		finally{
			if (lr != null)
		lr.close();	
		if (p != null)
		p.destroy();
		}



	}
	
	void dirSize(String fs,LongNum c,LongNum lm)
	throws IOException
	{
		dirSize(fs,c);
	}
}


class LCP extends JCP{

	public void copy(String fs,String t)
	throws IOException
	{
		if (! fs.endsWith("/"))
			fs = fs + "/";
		if (! fs.endsWith("*"))
			fs = fs + "*";
		LineNumberReader lr = null;
		Process p = null;
		try{
			p = new tools.util.NProcess(Runtime.getRuntime().exec("cp -f -R " + fs + " " + t));
			InputStream in = p.getInputStream();
			lr = new LineNumberReader(new InputStreamReader(in));
			StringBuffer l = new StringBuffer();
			String s = null;
			while ((s = lr.readLine()) != null)
				l = l.append(s);
				Thread.currentThread().sleep(FileUtil.pause);
				
		}catch (Exception ex){
			throw new IOException(fs + " LCP: " + ex.toString());	
		}	
		finally{
			if (lr != null)
		lr.close();	
		if (p != null)
		p.destroy();
		}



	}
}


class ZipOutputContainer{

	ZipOutputContainer(String target)throws IOException
	{
		setOut(target);
	}
	
	ZipOutputContainer(OutputStream target)throws IOException
	{
		setOut(new ZipOutputStream(target));
	}
	
	
	
	ZipOutputStream out = null;
	void setOut(String target)throws IOException
	{
		out = new ZipOutputStream(new FileOutputStream(new File(target)));
	}
	
	void setOut(ZipOutputStream o)
	{
		out = o;
	}
	
	
	ZipOutputStream getOut(){
		return out;
	}
	void close()throws IOException{
		out.close();
	}


}


class InnerClassFilenameFilter implements FilenameFilter {
    private String baseClassName;

    InnerClassFilenameFilter(String baseclass) {
        int extidx = baseclass.lastIndexOf(".class");
        if (extidx == -1) {
            extidx = baseclass.length() - 1;
        }
        baseClassName = baseclass.substring(0, extidx);
    }

    public boolean accept (File Dir, String filename) {
        if ((filename.lastIndexOf(".") != filename.lastIndexOf(".class"))
            || (filename.indexOf(baseClassName + "$") != 0)) {
            return false;
        }
        return true;
    }
}
