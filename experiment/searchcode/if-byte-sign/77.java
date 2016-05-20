package core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.net.HttpURLConnection;
import java.net.URL;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.Signature;
import java.security.SignatureException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

import org.bouncycastle.openpgp.PGPPublicKey;

import com.sun.syndication.feed.synd.SyndEntryImpl;

/**
 * This class is just a package with a variety of utility functions. It is
 * quite bad and ugly programmed, names are confusing, interfaces are bad
 * designed and a lot of functions are obsolete. I'm trying to update this
 * class in order to improve those points but it is a hard job and right now
 * I have no much time and other things more important to implement.
 * 
 * TODO: Rewrite this ugly piece of code :-S.
 * 
 * @author ole
 */
public class Util {
	// Constants.
	public static final String DUSTEXTENSION = ".dust";
	public static final String GNUTELLA = "SHA1";
	public static final int TITLELENGTH = 60;
	public static final String DIGEST_ALGORITHM = "SHA1";
	public static final String ZIP_EXT = ".zip";
	public static final String NAME_SEPARATOR = "-";
	public static final String EXT_SEPARATOR = ".";
	public static final int SHALENGTH = 40;
	public static final String IMGFOLDER = "img";
	public static final String POSTEXTENSION = "html";
	public static final String XMLEXTENSION = "xml";
	public static final String BLOG_EXT = "blog";
	public static final String XML_EXT = ".xml";
	public static final int TIMEOUT = 15 * 1000;
	
	/*
	 * Private functions.
	 */
	/**
	 * Function that returns true if fname is a zip file, that means that fname
	 * must end with ".zip", it is not checked if the file is a real zip file,
	 * don't be evil :).
	 * 
	 * @param fname
	 * @return True if fname ends with ".zip", false elsewhere.
	 */
	private static boolean is_zip(String fname) {
		return is_this_extension(fname, "zip");
	}
	
	/**
	 * Function that returns true if fname is a xml file, that means that fname
	 * must end with ".xml", it is not checked if the file is a real xml file,
	 * don't be evil (one more time :).
	 * 
	 * @param fname
	 * @return True if fname ends with ".zip", false elsewhere.
	 */
	private static boolean is_xml(String fname) {
		return is_this_extension(fname, "xml");
	}
	
	/**
	 * Function that checks if fname ends with ".<ext>".
	 * 
	 * @param fname: The filename to check its end.
	 * @param ext: A string with the extesion we want to check.
	 * @return: True if fname ends with ".<ext>", false elsewhere.
	 */
	private static boolean is_this_extension(String fname, String ext) {
		String extension;
		int ext_separator;
		
		ext_separator = fname.lastIndexOf(".");
		extension = fname.substring(ext_separator + 1);
		if (extension.compareTo(ext) == 0)
			return true;
		else
			return false;
	}
	
	/**
	 * Devuelve el titulo acortado
	 * @param title: Titulo a acortar
	 * @return Titulo ya acortado segun la constante TITLELENGTH
	 * */
	private static String shortTitle(String title) {
		String new_title;
		
		// Comprobamos que el max no sea mayor que TITLELENGTH
		if(title.length() > TITLELENGTH) {
			new_title = title.substring(0,TITLELENGTH);
			return new_title;
		}
		
		return title;
	}
	
	/**
	 * Function that unzip the content of a file and put the decompressed
	 * files inside the directory specified.
	 * 
	 * @param fname: The filename of the zipped file.
	 * @param path: The destination directory path.
	 * @throws Exception: If there are problems reading of writing a file or if
	 * the zip file contains directories and we cannot create them.
	 */
	private static void unzipFile(String fname, String path)
	throws RuntimeException {
		final int CHUNK_SIZE = 512;
		FileInputStream fis = null;
		ZipInputStream zis = null;
		
		try {
		
			String checked_path = Util.checkDirectoryPath(path);
			fis = new FileInputStream(fname);
			zis = new ZipInputStream(fis);
		ZipEntry entry;
		File fd, fd_parent;
		FileOutputStream fos;
		String fnameout;
		byte[] uncompressed_data;
		int remain, readed;
		
		// A zip file is just a container of files (that it's compressed also).
		// We read each file and decompress it to a file with the same name
		// inside the given directory.
		while ((entry = zis.getNextEntry()) != null) {
			fnameout = checked_path + entry.getName();
			
			// The entry is a directory.
			if (entry.isDirectory()) {
				// Check it doesn't exists and create it.
				if (!mkdirInDepth(fnameout)) 
					throw new RuntimeException("Cannot create " + fnameout +
										" directory.");
			}
			// The entry is a regular file.
			else {
				fd = new File(fnameout);
				
				// Check if its parent directory doesn't exists and create it.
				// Sometimes the files inside a directory are tried to unzip
				// before the directory itself.
				fd_parent = fd.getParentFile();
				if (!fd_parent.exists() &&
					!mkdirInDepth(fd_parent.getAbsolutePath()))
					throw new RuntimeException("Cannot create " + fnameout +
										" directory.");
				fos = new FileOutputStream(fd);
				
				// Get the length of the uncompressed data and write it to the
				// previous created file.
				remain = (int)entry.getSize();
				if (remain != -1) {
					uncompressed_data = Util.recoverDatafromInputStream(zis, remain);
					fos.write(uncompressed_data);
				}
				
				// Sometimes getSize() doesn't know the length of the uncompressed
				// data so we cannot use our function recoverDataFromInputStream().
				// In that case we read chunks of uncompressed data until we cannot
				// read more chunks. Note that we don't write CHUNK_SIZE byte to
				// the output file, instead we write "readed" bytes because the
				// read() method could read less bytes than CHUNK_SIZE.
				else {
					uncompressed_data = new byte[CHUNK_SIZE];
					while ((readed = zis.read(uncompressed_data)) != -1)
						fos.write(uncompressed_data, 0, readed);
				}
				
				// We have finished to uncompress this entry, so we close it. 
				fos.close();
			}
		}
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally {
			if(zis != null) try { zis.close(); } catch(Exception e) {}
			if(fis != null) try { fis.close(); } catch(Exception e) {}
		}
	}
	
	/**
	 * This function has a bit of magic. The issue is that we want to create a
	 * zip file that contains just the files and directories from a path but
	 * not directories with the hole path. This function implements the magic
	 * needed to do it.
	 * 
	 * Function that creates a zip with the files and directories inside the
	 * path parameter.
	 * 
	 * @param path
	 * 		The path to the directory where the files and directories to be
	 * 		zipped are.
	 * @param root_path
	 * 		Since this function is recursive this parameter let us make the
	 * 		magic mentioned above. If you are trying to use it from outside
	 * 		you should pass the same value as path.
	 * @param zos
	 * 		The zip created to contain the zipped files.
	 * @throws IOException
	 */
	private static void doZipDir(String path, String root_path,
								 ZipOutputStream zos) throws IOException {
		File fd, file_fd;
		FileInputStream fis;
		ZipEntry zipentry;
		String checked_path, checked_root_path;
		String relative_name, fname;
		int file_len, offset;
		byte data_to_zip[];
		
		checked_path = checkDirectoryPath(path);
		checked_root_path = checkDirectoryPath(root_path);
		if (!checked_path.startsWith(checked_root_path))
			throw new RuntimeException("The path " + checked_path +
									   " doesn't starts with " +
									   checked_root_path);
		
		fd = new File(checked_path);
		if (!fd.isDirectory())
			throw new RuntimeException("Path " + checked_path + " is not a" +
									   "directory.");
		
		for (String name: fd.list()) {
			fname = checked_path + name;
			file_fd = new File(fname);
			if (file_fd.isDirectory())
				doZipDir(fname, checked_root_path, zos);
			else {
				offset = checked_root_path.length();
				relative_name = fname.substring(offset);
				
				fis = new FileInputStream(file_fd);
				zipentry = new ZipEntry(relative_name);
				zos.putNextEntry(zipentry);
				
				// Add the content of the file to zip to the zip file
				// (confusing huh?).
				file_len = (int)file_fd.length();
				data_to_zip = recoverDatafromInputStream(fis, file_len);
				zos.write(data_to_zip);
				
				// Close the new entry.
				zos.closeEntry();
				fis.close();
			}
		}
	}
	
	/**
	 * Function that extract from a dustname (a name with the structure
	 * "<blogtitle>-<timestamp>-<md5>.<extension>.<dust>") the blog title.
	 * 
	 * @param fname
	 * @return
	 */
	private static String getBlogTitleFromName(String fname) {
		int first_separator, last_file_separator;
	
		first_separator = fname.indexOf(NAME_SEPARATOR);
		last_file_separator = fname.lastIndexOf(
								System.getProperty("file.separator"));
		return fname.substring(last_file_separator + 1, first_separator);
	}
	
	/**
	 * Function that extract from a dustname (a name with the structure
	 * "<blogtitle>-<timestamp>-<md5>.<extension>.<dust>") the timestamp.
	 * 
	 * @param fname
	 * @return
	 */
	private static String getTimestampFromName(String fname) {
		int first_separator, second_separator;
	
		first_separator = fname.indexOf(NAME_SEPARATOR);
		second_separator = fname.indexOf(NAME_SEPARATOR, first_separator + 1);
		return fname.substring(first_separator + 1, second_separator);
	}
	
	/*
	 * Public functions.
	 */
	
	/**
	 * Function that takes an img tag (of the form <img ...> and adds it an alt
	 * attribute with the given value or, if it already has an alt attribute,
	 * modifies it with the given value.
	 * 
	 * @param imgtag
	 * 		The img tag to modify.
	 * @param value
	 * 		The value of the alt attribute.
	 * @return
	 * 		A String with the new img tag.
	 * @throws RuntimeException
	 * 		If the imgtag passed is not well-formed.
	 */
	public static String addAltAttrWithValue(String imgtag, String value) {
		String imgtag_regex = "<img ([^>]*)>";
		String altattr_regex = "alt=[\"'][^\"']*[\"']";
		Pattern altattr_pat, imgtag_pat;
		Matcher altattr_mat, imgtag_mat;
		String imgtag_content, result = "<img ";
		String new_altattr = "alt=\"" + value + "\" ";
		
		// Checks the sanity of the img tag.
		imgtag_pat = Pattern.compile(imgtag_regex);
		imgtag_mat = imgtag_pat.matcher(imgtag);
		if (!imgtag_mat.find())
			throw new RuntimeException("Img tag \"" + imgtag + "\" is not " +
									   "well-formed.");
		
		// Take the imgtag content (just the content, not the "<img" and the
		// final ">").
		imgtag_content = imgtag_mat.group(1);
		
		// Search of the alt attribute inside the img tag content.
		altattr_pat = Pattern.compile(altattr_regex);
		altattr_mat = altattr_pat.matcher(imgtag_content);
		if (altattr_mat.find())
			result += imgtag_content.replaceAll(altattr_regex, new_altattr) +
					  ">";
		// img tag has no alt attribute.
		else
			result += new_altattr + imgtag_content + ">";
		
		return result;
	}
	
	/**
	 * Adds the sign at the end of a file. This sign should be the byte array
	 * with the sign itself and also at the last byte a byte indicating the
	 * length of this sign.
	 * 
	 * @param fname: The file name to the file that will be prepended to the
	 * sign.
	 * @param dustsign: Byte array containing the sign and the last byte with
	 * the length of the sign.
	 * @throws IOException: Thrown if there are problems writing the file.
	 */
	public static void addSign(String fname, byte[] dustsign) throws IOException {
		File input;
		FileInputStream in;
		FileOutputStream out;
		byte[] buffer;
		
		
		// Opening the file and setting the pointer at the end.
		input = new File(fname);
		if (!input.exists())
			throw new IOException();
		
		in = null;
		out = null;
		try {
			
			
			in = new FileInputStream(input);	
			out = new FileOutputStream(fname + DUSTEXTENSION, false);
			// Reads the whole file, adds the sign at the end and write it to a
			// file with the same name plus our own extension.
			buffer = Util.recoverDatafromInputStream(in, (int)input.length());
			out.write(buffer);
			out.write(dustsign);
			out.close();
		}
		catch(IOException exc) {
			throw exc;
		}
		finally {
			if (in != null)
				in.close();
			if (out != null)
				out.close();

			File file = new File(fname);
			file.delete();
		}
	}
	
	/**
	 * Function that receive a blog in the form of an XML file if it wasn't
	 * embebed, or in a ZIP file if it was embebed and put it in the right
	 * place in the directories structure that Dust use to organize all its
	 * files.
	 * 
	 * @param zip_fname: The filename of the zip.
	 * @return: An string specifying where was copied the XML file of null
	 * if it was a ZIP file.
	 * @throws RunTimeException: Raised if the file is not a zip nor an xml. 
	 */
	public static String buildDirTreeFromFile(String file, String path)
	throws RuntimeException {
		String path_to_build;
		String md5_str, blog_title, time_str;
		String checked_path = checkDirectoryPath(path);
		String new_xml_name;
		File fd;
		
		md5_str = getHashFromName(file);
		blog_title = getBlogTitleFromName(file);
		time_str = getTimestampFromName(file);
		
		// Create a name with the whole internal path to where the file will be
		// left.
		path_to_build = checked_path + checkDirectoryPath(md5_str) +
						checkDirectoryPath(blog_title);
						
		// If the file is a zip we must create a directory with the name like
		// the timestamp.
		if (is_zip(file))
			path_to_build += checkDirectoryPath(time_str);
		
		// If that directories have not been created create them.
		fd = new File(path_to_build);
		if (!fd.exists() && !mkdirInDepth(path_to_build))
			throw new RuntimeException("Cannot create the path " + path_to_build);
		
		// If the file is a zip, unzip it.
		if (is_zip(file)) {
			unzipFile(file, path_to_build);
			fd.delete();
			return null;
		// If the file is XML just copy the file in the <md5>/<blogtitle>
		// directory changing its name to just the timestamp.
		}
		else if (is_xml(file)) {
			new_xml_name = path_to_build + getTimestampFromName(file) + XML_EXT;
			copyFile(file, new_xml_name);
			fd.delete();
			return new_xml_name;
		}
		else
			throw new RuntimeException("Wrong file type, expected a ZIP or an XML.");
	}
	
	/**
	 * Function that returns in a String the hexadecimal representation of a
	 * byte array.
	 * 
	 * @param bytearray: The byte array that contains the bytes to be
	 * represented.
	 * @return: A string with the bytes represented as hexadecimal values.
	 */
	public static String byteArray2HexStr(byte[] bytearray) {
		String result = new String();
		
        for(byte aux : bytearray) {
            int b = aux & 0xff;
            if (Integer.toHexString(b).length() == 1)
            	 result += "0";
            result += Integer.toHexString(b);
        }
        return result;
	}
	
	/**
	 * Function that checks if the path is valid as a directory path, that
	 * means, checks that it has the separator character at the end ('/' in a
	 * un*x OS or '\' in Windows) and returns a string with the same path but
	 * modified for directories (if necessary). Examples (in un*x):
	 * - Input "/tmp", returns "/tmp/".
	 * - Input "tmp", returns "tmp/".
	 * - Input "/tmp/", returns "/tmp/".
	 * - Input "tmp/", returns "tmp/".
	 * 
	 * @param path:
	 * 		A string representing a path to a directory.
	 * @return:
	 * 		The same string as path if it already had the file separator,
	 * 		if not the same as path + the file separator.
	 */
	public static String checkDirectoryPath(String path) {
		String lastchar, separator;
		
		if (path.equals("")) 
			throw new RuntimeException("Path was empty.");
			
		separator = System.getProperty("file.separator");
		lastchar = path.substring(path.length()-1);
		if (lastchar.equalsIgnoreCase(separator))
			return path;
		else
			return path + separator;
	}
	
	// OBSOLETE
//	/**
//	 * Check if given fname already exist in the given path.
//	 * Search for it splitting fname by - and going to that directories.
//	 * 
//	 * @param path:
//	 * 		Path where start to looking for fname
//	 * @param fname:
//	 * 		File to look for
//	 * @return:
//	 * 		A boolean specifying if fname has been found
//	 * @throws Exception 
//	 * */
//	public static boolean checkDustFileAlreadyExist(String path, String fname) throws RuntimeException {
//		String hash, blog_title, timestamp, checked_path, extension, final_path;
//		File fd;
//		boolean exist;
//		
//		checked_path = checkDirectoryPath(path);
//		exist = false;
//		
//		// Recover substrings for hash, timestamp, title and extension
//		hash = getHashFromName(fname);
//		blog_title = getBlogTitleFromName(fname);
//		timestamp = getTimestampFromName(fname);
//		extension = getExtensionFromName(fname);
//		
//		// Check which kind of file it is 
//		//(nolla cabr?n, vengo del futuro, como trunks y el t800 XD)
//		if (extension.compareToIgnoreCase("xml") == 0) {
//			final_path = checked_path;
//			final_path += checkDirectoryPath(hash);
//			final_path += checkDirectoryPath(blog_title);
//			final_path += timestamp+XML_EXT;
//			fd = new File(final_path);
//			if (fd.exists()) {
//				exist = true;
//			}
//		}
//		// Check for unzipped post, to do that check for directory
//		else if (extension.compareToIgnoreCase("zip") == 0) {
//			final_path = checked_path;
//			final_path += checkDirectoryPath(hash);
//			final_path += checkDirectoryPath(blog_title);
//			final_path += checkDirectoryPath(timestamp);
//			fd = new File(final_path);
//			if (fd.isDirectory() && fd.exists()) {
//				exist = true;
//			}
//		}
//		
//		return exist;
//	}
	
	/**
	 * Check if given image exist in given file for a given blog.
	 * 
	 * @param path:
	 * 		Path where start looking for.
	 * @param dust_file:
	 * 		Dust file with blog to look for.
	 * @param fname:
	 * 		Image filename to look for.
	 * @return:
	 * 		boolean specifying if fname has been found.
	 * @throws Exception 
	 * */
	// Se le pasa un fname del estilo <hash>.<extension>
	public static boolean checkImgDustFileAlreadyExist(String path,
													   String dust_file,
													   String fname)
	throws Exception {
		String hash, blog_title, checked_path, final_path;
		File fd;
		boolean exist;
		
		checked_path = checkDirectoryPath(path);
		exist = false;
		
		// Recover substrings for hash and title
		hash = getHashFromName(dust_file);
		blog_title = getBlogTitleFromName(dust_file);
		
		// Check if it exist
		final_path = checked_path;
		final_path = checkDirectoryPath(final_path+hash);
		final_path = checkDirectoryPath(final_path+blog_title);
		final_path = checkDirectoryPath(final_path+IMGFOLDER);
		final_path += fname;
		fd = new File(final_path);
		if (fd.exists()) {
			exist = true;
		}
		
		return exist;
	}
	
	/**
	 * Function that checks if the filename passed is trying to make traverse
	 * path attacks. This is done checking that it has not constructions like
	 * "..\", ".\", "../" and "./".
	 * 
	 * NOTE: It does not checks if the file exists, it just check if the String
	 * passed has traverse path construction or not.
	 * 
	 * @param fname:
	 * 		The filename to be checked.
	 * @return:
	 * 		True if it has one of the mentioned constructions, false elsewhere.
	 */
	public static boolean checkTraversePath(String fname) {
		String file_separator, regex;
		Pattern pat;
		Matcher mat;
		
		// Build the regex. This is quite ugly because since some systems use
		// character '/' as separator and regexs use it also, we have to escape
		// it if so.
		// Is there any other file separator in any system? :-s.
		file_separator = System.getProperty("file.separator");
		if (file_separator.equals("/"))
			// Search for "../" or "./"
			regex = "(\\.\\.\\/|\\.\\/)+";
		else if (file_separator.equals("\\"))
			// Search for "..\" or ".\"
			regex = "(\\.\\.\\\\|\\.\\\\)+";
		else {
			System.err.println("What kind of file separator " +
							   "is using this system?!");
			return true;
		}
		
		// Check if it is "jailing-safe".
		pat = Pattern.compile(regex);
		mat = pat.matcher(fname);
		if (mat.find())
			return true;
		
		return false;
	}
	
	/**
	 * Function that replace all the ocurrences of the characters ' ' and '-'
	 * in the string by '_'.
	 * 
	 * @param str
	 * 		String to clean.
	 * @return
	 * 		A new string with the replaced characters.
	 */
	public static String cleanString(String str) {
		String chars2replace = " |-";
		String tokenchar = "_";
		
		return str.replaceAll(chars2replace, tokenchar);
	}
	
	/**
	 * Function that copies a file into another file.
	 * 
	 * @param srcfile: The to copy.
	 * @param dstfile: The copied file.
	 * @throws IOException: If there are I/O errors.
	 */
	public static void copyFile(String srcfile, String dstfile) throws RuntimeException {
		
		File src_fd = null;
		File dst_fd = null;
		FileInputStream fis = null;
		FileOutputStream fos = null;
		
		try {
			src_fd = new File(srcfile);
			dst_fd = new File(dstfile);
			
			fis = new FileInputStream(src_fd);
			fos = new FileOutputStream(dst_fd);
			byte[] buf;
			int src_len;
		
			src_len = (int)src_fd.length();
			buf = new byte[src_len];
			buf = recoverDatafromInputStream(fis, src_len);
			fos.write(buf);
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}finally {
			if(fos != null) {
				try {
					fos.close();
				}catch(Exception e) {}
			}
			if(fis != null) try {
				fis.close();
			}catch(Exception e) {}
		}	
	}
	
	/** 
	 * Function almost opposite to dusterize(). Recovers the sign, verifies it,
	 * if it is correct saves the file without the sign to the location
	 * specified with the same name minus the extension ".dust" and returns
	 * true, if it was incorrect it returs false and the file is not created.
	 * 
	 * @param fnamein
	 * 		Filename of the file to verify.
	 * @param path
	 * 		The directory where the file dedusterized and truly verified will
	 * 		be created.
	 * @param key:
	 * 		The public key that will be used to check the authenticity of the
	 * 		sign in the file.
	 * @return
	 * 		True if everything goes fine and the sign was truly verified, false
	 * 		elsewhere.
	 */
	public static boolean deDusterize(String fnamein, String path,
									  PublicKey key) 
	throws RuntimeException {
		String checked_path = checkDirectoryPath(path);
		String fnameout;
		File fd;
		FileInputStream fis;
		FileOutputStream fos;
		byte sign[], signed_data[];
		int signed_data_len;
		
		try {
			// Reading the sign.
			sign = Util.recoverSign(fnamein);
		
			// Recovering the data bytes without the sign, the file itself minus
			// the sign length since the sign is at the end of the file minus 1
			// since there is an extra byte at the end of the sign (the one
			// indicating the length of the sign).
			fd = new File(fnamein);
			signed_data_len =  (int)fd.length() - sign.length - 1;
			fis = new FileInputStream(fd);
			signed_data = Util.recoverDatafromInputStream(fis, signed_data_len);
			fis.close();
		
			// Verifying that the data was signed with the publisher.
			if (Util.verifySign(signed_data, sign, key)) {
				fnameout = checked_path + fd.getName().replace(DUSTEXTENSION, "");
				fos = new FileOutputStream(fnameout);
				fos.write(signed_data);
				fos.close();
				return true;
			}
			else
				return false;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/** 
	 * Function that signs a file and adds this sign to the end of the file and
	 * renames it with a final ".dust" extension.
	 * 
	 * @param fname:
	 * 		Filename of the file to dusterize.
	 * @param key:
	 * 		The private key to use for signing.
	 * @throws IOException:
	 * 		If there are errors related to file access. 
	 * @throws SignatureException:
	 * 		If there are errors at the moment of signing the data.
	 * @throws NoSuchAlgorithmException:
	 * 		Should never be thrown. 
	 * @throws InvalidKeyException:
	 * 		If there are errors with they private key.
	 */
	public static void dusterize(String fname, PrivateKey key)
	throws IOException, InvalidKeyException, NoSuchAlgorithmException,
	SignatureException {
		byte[] sign;
		
		sign = Util.signData(fname, key);
		Util.addSign(fname, sign);
	}
	
	/**
	 * Function that takes a char array and creates a string with the special
	 * characters (those that regex interpret) escaped.
	 * 
	 * I haven't found how to make regular expressions match part of its
	 * pattern literally, it seems like we cannot, so we have to escape those
	 * characters. I have made my best here but I'm not really confident on
	 * this function.
	 * 
	 * @param str
	 * 		The char array that may contain regex characters.
	 * @return
	 * 		A string with the regex characters escaped (with '\' before them).
	 * 		Note that the result could be the same as the paramater if it has
	 * 		not regex characters.
	 */
	public static String escapeRegExChars(char[] str) {
		String result = "";
		
		for (int i = 0; i < str.length; i++) {
			if (str[i] == '.')
				result += "\\.";
			else if (str[i] == '?')
				result += "\\?";
			else if (str[i] == '[')
				result += "\\[";
			else if (str[i] == ']')
				result += "\\]";
			else if (str[i] == '*')
				result += "\\*";
			else if (str[i] == '+')
				result += "\\+";
			else if (str[i] == '-')
				result += "\\-";
			else if (str[i] == '^')
				result += "\\^";
			else if (str[i] == '$')
				result += "\\$";
			else if (str[i] == '\\')
				result += "\\\\";
			else if (str[i] == '{')
				result += "\\{";
			else if (str[i] == '}')
				result += "\\}";
			else if (str[i] == '|')
				result += "\\|";
			else if (str[i] == '!')
				result += "\\!";
			else if (str[i] == '/')
				result += "\\/";
			else
				result += str[i];
		}
		
		return result;
	}
	
	/**
	 * Function that eliminates reserved characters for filenames in multiples
	 * OSes. The characters are [\/:*?"<>] (note that '[' and ']', read about
	 * regexps noob! :).
	 * 
	 * @param fname: The string to be filtrated.
	 * @return: The same string as passed without these reservated characters.
	 */
	public static String filterTitle(String str) {
		str = str.replaceAll("[\\/:*?\"<>]*", "");
		str = str.replace("-", "_");
		return str;
	}
	
	/**
	 * Genera un hash para el algoritmo dado
	 * @param content: Byte array a hashear
	 * @param algorithm: Hashing algorithm to use
	 * @return Un byte array conteniendo el hash
	 * @throws IOException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	public static byte[] generateHash(byte[] content, String algorithm) 
	throws IOException, NoSuchAlgorithmException, NoSuchProviderException {
		byte[] hash;
		MessageDigest md;
		
		md = MessageDigest.getInstance(algorithm);
		md.update(content);
		hash = md.digest();
		return hash;
	}
	
	/**
	 * Generate a string representation of a publisher's public key hash
	 * @throws IOException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * 
	 * */
	public static String genStrHashFromPublisher(Publisher publisher) 
	throws RuntimeException {
		try {
		return byteArray2HexStr(generateHash(publisher.getPublicKey().getEncoded(), 
											 DIGEST_ALGORITHM));
		}catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Function that returns a list with the recovered Blogs that were
	 * serialized in that directory.
	 * 
	 * @param path: The directory where the serialized Blogs are.
	 * @return: A list with the serialized Blogs.
	 * @throws Exception 
	 */
	public static List<Blog> getBlogs(String path) {
		String checked_path = Util.checkDirectoryPath(path);
		List<Blog> blogs = new ArrayList<Blog>();
		File dir = new File(checked_path);
		String[] fnames = dir.list();
		
		for (String fname: fnames) {
			if (fname.endsWith(Util.BLOG_EXT)) {
				try {
					blogs.add(Blog.createBlog(checked_path + fname));	
				}
				catch(Exception e) {
					System.err.println("Problems recovering blog from " +
									   checked_path + fname);
					e.printStackTrace();
				}
			}
		}
		return blogs;
	}
	
	// OBSOLETE. To be removed...
//	/**
//	 * Get all the blogs located in a specific directory, that are signed by a
//	 * publisher.
//	 * 
//	 * @param path System folder with the dusterized objects
//	 * @param publish Publisher object 
//	 * @return Blogs list 
//	 * @throws Exception 
//	 */
//	/*
//	 * Recupera todos los blogs y filtra por el MD5 del pubkey
//	 * 
//	 * */
//	public static List<Blog> getBlogsByPublicKey(String path, PublicKey key)
//	throws Exception {
//		String checked_path, md5;
//		File dir/*, fd*/;
//		String files[], fnamein, fnameout;
//		byte[] pubkeymd5/*, blogContent*/;
//		List<Blog> blogs = new ArrayList<Blog>();
////		FileInputStream fis;
//		boolean legitimate;
//		
//		checked_path = Util.checkDirectoryPath(path);
//		dir = new File(checked_path);
//		
//		// Calculamos el MD5 en hexadecimal
//		pubkeymd5 = key.getEncoded();		
//		pubkeymd5 = Util.generateHash(pubkeymd5, Util.DIGEST_ALGORITHM);
//		md5 = Util.byteArray2HexStr(pubkeymd5);
//		
//		// Obtenemos el contenido del path
//		files =  dir.list();
//		// Y leemos los blogs
//		for(String file : files) {
//			if(file.endsWith(md5+Blog.BLOGEXT+DUSTEXTENSION)) {
//				try {
//					
//					//Dedusterizamos y comprobamos que la firma sea correcta
//					fnamein = checked_path + file;
//					fnameout = checked_path + file.replace(DUSTEXTENSION, "");
//					legitimate = Util.deDusterize(fnamein, fnameout, key);
//					if (legitimate) {
//						// Si algun gracioso pone blabla.dustblaMD5correcto.xml.dust "se va a haber un follon que no sabe ni donde sa'metio"
//						blogs.add(Blog.createBlog(fnameout));
////						fd = new File(fnameout);
////						fis = new FileInputStream(fd);
////						blogContent = Util.recoverDatafromInputStream(fis, (int)fd.length());
////						blogs.add(Blog.createBlogFromFile(blogContent));
//					}	
//				}catch(Exception e) {}
//			}
//			
//			else if (file.endsWith(md5 + ZIP_EXT + DUSTEXTENSION)) {
//				try {
//					fnamein = checked_path + file;
//					fnameout = checked_path + file.replace(DUSTEXTENSION, "");
//					legitimate = Util.deDusterize(fnamein, fnameout, key);
//					if (legitimate) {
//						Util.unzipFile(file, checked_path);
//						
//						blogs.add(Blog.createBlog(fnameout));
//						
////						fd = new File(fnameout);
////						fis = new FileInputStream(fd);
////						blogContent = Util.recoverDatafromInputStream(fis, (int)fd.length());
////						blogs.add(Blog.createBlogFromFile(blogContent));
//					}	
//				}
//				catch (Exception e) {}
//			}
//		}
//		return blogs;
//	}
	
	/**
	 * Function that extract from a dustname (a name with the structure
	 * "<blogtitle>-<timestamp>-<md5>.<extension>.dust") the real extension.
	 * 
	 * @param fname
	 * @return
	 */
	public static String getExtensionFromName(String fname) {
		int first_dot, second_dot;
	
		first_dot = fname.indexOf(".");
		second_dot = fname.indexOf(".", first_dot + 1);
		return fname.substring(first_dot + 1, second_dot);
	}
	
	/**
	 * Function that returns an hexadecimal string with the hash of the key
	 * passed.
	 * 
	 * @param key
	 * 		The key from which calculate the hash.
	 * @return
	 * 		An hexadecimal string (a string just with [0-9a-fA-F]) representing
	 * 		the hash of the key.
	 * @throws NoSuchAlgorithmException
	 * @throws NoSuchProviderException
	 * @throws IOException
	 */
	public static String getHashFromKey(PGPPublicKey key)
	throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		byte keyenc[] = key.getEncoded();
		byte hash[];
		
		hash = Util.generateHash(keyenc, DIGEST_ALGORITHM);
		return Util.byteArray2HexStr(hash);
	}
	
	/**
	 * Function that extract from a dustname (a name with the structure
	 * "<blogtitle>-<timestamp>-<md5>.<extension>.<dust>") the MD5.
	 * 
	 * @param fname
	 * @return
	 */
	public static String getHashFromName(String fname) {
		int first_separator, second_separator;
		int first_dot;
	
		first_separator = fname.indexOf(NAME_SEPARATOR);
		second_separator = fname.indexOf(NAME_SEPARATOR, first_separator + 1);
		first_dot = fname.indexOf(".");
		return fname.substring(second_separator + 1, first_dot);
	}
	
	// Bonito user-agent...
	/**
	 * Retrieve HTML code from an URL
	 * @param url: Url to retrieve
	 * @return: HTML code 
	 */
	public static String getHTML(String url) throws IOException {
	    HttpURLConnection connection = null;	    
	    BufferedReader rd  = null;
	    StringBuilder sb = null;
	    String line = null;
	    URL serverAddress = null;
	    serverAddress = new URL(url);
	    connection = null;
        connection = (HttpURLConnection)serverAddress.openConnection();
        connection.setRequestMethod("GET");
        connection.setRequestProperty("User-Agent", "Interneeeeet!!! ENJUUUTOOOO 'drop table useragents;--");
        connection.setRequestProperty("Accept-Charset", "ISO-8859-1,utf-8;q=0.7,*;q=0.3");
        
        connection.setDoOutput(true);
        connection.setReadTimeout(10000);

        connection.connect();

        rd  = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        sb = new StringBuilder();
        while ((line = rd.readLine()) != null){
            sb.append(line + '\n');
        }
      //Cerramos conexion <-- Comentario aclaratorio donde los haya :P. `
        connection.disconnect();
        rd = null;
        connection = null;
        return sb.toString();
	}
	
	/**
	 * Function to extract all images hash from XML feed file
	 * It looks for strings like alt="gnutela:HASH,emule:HASH" and so on.
	 * 
	 * It returns a set so there are no repeated elements. Note that just
	 * one string per image will be inside this set but there could be more
	 * than one reference in the content of the fname file.
	 * 
	 * @param fname:
	 * 		Filename with path to feed file
	 * @param proto:
	 * 		P2P Network to look for (gnutella, emule, etc)
	 * @return:
	 * 		Set of strings with all images hashes (not repeated).
	 * @throws RuntimeException
	 * */
	// Buscar dentro de los alt y devolver todas las imagenes
	// muchas cosas &lt;img jaksjroieose alt="gnutella:8175987450745087450934983,emule:524798572435734759" quizas mas porqueria pero no tiene por que haber/&gt;
	public static Set<String> getImagesFromFeedFile(String fname,
													 String proto)
	throws RuntimeException {
		String reg_exp = "alt=\"[^\"]*" + proto + ":([^\"]*[^\"])\"";
		File fd = null;
		FileInputStream fis = null;
		Set<String> set = new HashSet<String>();
		
		try {
			Pattern pattern = Pattern.compile(reg_exp);
			Matcher matcher = pattern.matcher(fname);
			String content, matched_str;
			int content_len;
		
			// Get the whole file content.
			fd = new File(fname);
			fis = new FileInputStream(fd);
			content_len = (int)fd.length();
			content = new String(recoverDatafromInputStream(fis, content_len));
		
			// Preparing the regular expression to search.
			pattern = Pattern.compile(reg_exp);
			matcher = pattern.matcher(content);
		
			// For each matching substring, get the hash in the alt and add it to
			// the list.
			while (matcher.find()) {
				matched_str = matcher.group(1);
				set.add(matched_str);
			}
		}
		catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		finally {
			if (fis != null) {
				try {
					fis.close();
				}
				catch(Exception e) {}
			}
		}
		
		return set;
	}
	
//	/**
//	 * Recover all the key pairs in the directory specified.
//	 * 
//	 * @param path:
//	 * 		The path where to search for key pairs.
//	 * @return:
//	 * 		A List of KeyPairs.
//	 */
//	public static List<KeyPair> getKeyPairsFromDir(String path) {
//		List<KeyPair> result;
//		File fd;
//		SortedSet<String> aux_listfiles;
//		KeyPair aux_pair;
//		PublicKey aux_pub;
//		PrivateKey aux_priv;
//		
//		result = new ArrayList<KeyPair>();
//		
//		// Check that the file is a directory.
//		fd = new File(path);
//		if (!fd.isDirectory())
//			throw new RuntimeException(path + " is not a directory.");
//		
//		// Get all the files that has KEY_EXTENSION as extension.
//		aux_listfiles = new TreeSet<String>();
//		for (String fname: fd.list())
//			if (fname.endsWith(KeyUtils.KEY_EXTENSION))
//				aux_listfiles.add(fname);
//		
//		// Here is an important assumption. Since aux_listfiles is sorted and
//		// the generation of filenames of keys is controlled by us we will
//		// assume that:
//		//  - A private key filename will has its public pair just after it
//		//	  inside the sorted list, so if we found a private key, the next
//		//	  element is its public key pair.
//		//  - Founding a public key doesn't imply that we have found previously
//		//	  a private key. It could happen that we have a lot of public keys
//		//	  but just a few (or not at all) private keys.
//		/** TODO: It is not controlled if there are 2 private keys files one
//		 * after another. Bad users can force the folder to not have the
//		 * structure we are assuming. **/
//		aux_priv = null;
//		aux_pub = null;
//		for (String fname: aux_listfiles) {
//			try {
//				if (fname.endsWith(KeyUtils.PRIVATEKEY_SUFFIX))
//					aux_priv = KeyUtils.recoverPrivateKey(fname);
//				else if (fname.endsWith(KeyUtils.PUBLICKEY_SUFFIX)) {
//					aux_pub = KeyUtils.recoverPublicKey(fname);
//					aux_pair = new KeyPair(aux_pub, aux_priv);
//					result.add(aux_pair);
//					
//					// If we don't set aux_priv to null here, if we found more
//					// public keys without private keys associated the last
//					// private key will be set for all of them!.
//					aux_priv = null;
//				}
//				else
//					System.err.println("[WARN]: Weird file " + fname +
//									   " in keys folder.");
//			}
//			catch (Exception e) {
//				e.printStackTrace();
//			}
//		}
//		
//		return result;
//	}
	
	/**
	 * Function that returns a list with the recovered Publishers that were
	 * serialized in that directory.
	 * 
	 * @param path: The directory where the serialized Publishers are.
	 * @return: A list with the serialized Publishers.
	 */
	public static List<Publisher> getPublishers(String path)  {
		List<Publisher> publishers = new ArrayList<Publisher>();
		String checked_path;
		String[] files;
		File dir, file;
		Boolean is_directory;
		
		try {
			checked_path = Util.checkDirectoryPath(path);
			dir = new File(checked_path);
			files =  dir.list();
			// For each file in the directory it is checked if it is not a
			// directory and, if that is the case, if its name ends with the
			// extension of serialized publishers. If so we try to create a
			// publisher from it. We silently ignore the exceptions because it
			// could be that the wannabe-publisher file is corrupted (or
			// evil-formed).
			for (String fname: files) {
				file = new File(fname);
				is_directory = file.isDirectory();
				if (!is_directory && fname.endsWith(Publisher.PUBLISHEREXT)) {
					try {
						publishers.add(Publisher.createPublisher(checked_path + file));
					}
					catch(Exception e) {
						throw new RuntimeException(e.getMessage());
					}
				}
			}
			return publishers;
		}
		catch (Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	/**
	 * Function that returns a string with the actual timestamp that covers
	 * from the year to the milliseconds.
	 * 
	 * @return: An string representing the actual time in the form
	 * <year><month><dayofmonth><hourofday><minute><second><milliseconds>.
	 * Just as a clarification, remember that months and days of the months
	 * starts at 0 not at 1. Also take in mind that months, days, hours,
	 * minutes and seconds will always has length 2 padded with 0s if needed
	 * and milliseconds will always has length 3 padded with 0s also.
	 */
	public static String getStrTimeStamp() {
		Calendar cal = Calendar.getInstance();
		String res;
		
		res = String.valueOf(cal.get(Calendar.YEAR));
		res += String.format("%02d", cal.get(Calendar.MONTH));
		res += String.format("%02d", cal.get(Calendar.DAY_OF_MONTH));
		res += String.format("%02d", cal.get(Calendar.HOUR_OF_DAY));
		res += String.format("%02d", cal.get(Calendar.MINUTE));
		res += String.format("%02d", cal.get(Calendar.SECOND));
		res += String.format("%03d", cal.get(Calendar.MILLISECOND));
		
		return res;
	}
	
	/**
	 * Function that retrieves the whole content pointed by the url passed.
	 * 
	 * @param url
	 * 		The URL where we will try to get the content.
	 * @return
	 * 		An string containing the data pointed by one of the urls. If there
	 * 		where problems then null.
	 * @throws IOException 
	 */
	public static String getURLContent(URL url)
	throws IOException {
		BufferedReader br;
		InputStreamReader isr;
		String result = null;
		String line;
		HttpURLConnection conn;
		int conn_state;
		
		// Connect to the url.
		conn = (HttpURLConnection)url.openConnection();
		// Set the read timeout is needed, I don't know what is happening
		// really but there are some servers (i.e. 48bits blog server) that
		// if not sets a high enough read timeout this will raise a
		// "SocketTimeOutException: Read time out". A fuck up.
		conn.setReadTimeout(TIMEOUT);
		conn_state = conn.getResponseCode();
			
		// Successful connection. Get the content of the URL.
		if (conn_state == HttpURLConnection.HTTP_OK) {
			result = new String("");
			isr = new InputStreamReader(conn.getInputStream(), "UTF-8");
			br = new BufferedReader(isr);
			line = br.readLine();
			while (line != null) {
				result += line + "\r\n";
				line = br.readLine();
			}
		}
		
		return result;
	}
	
	/**
	 * Function that checks if the file is a feed file.
	 * 
	 * BEWARE!: This function use a very simple heuristic to know if the file
	 * is a feed file. It looks inside the first 512 bytes of the file and
	 * looks for the regex "<rss[^>]*>", the rss tag possibly having
	 * attributes. This is a bit ugly but is what it is. I bet you can change
	 * it and improve it :).
	 * 
	 * @param fname:
	 * 		The filename of the file to check.
	 * @return:
	 * 		True if the file is a Feed file, false elsewhere.
	 * @throws FileNotFoundException
	 */
	public static boolean isFeed(String fname) throws FileNotFoundException {
		File fd;
		FileInputStream fis;
		Pattern pattern;
		Matcher matcher;
		String head;
		int len;
		
		// Checks that the file exists.
		fd = new File(fname);
		if (!fd.exists())
			throw new FileNotFoundException("File " + fname +
											" doesn't exists.");
		
		// Get the first 512 bytes of the file.
		fis = new FileInputStream(fd);
		len = fd.length() >= 512? 512: (int)fd.length();
		head = new String(Util.recoverDatafromInputStream(fis, len));
		
		// Look for the proper regex.
		pattern = Pattern.compile("<rss[^>]*>");
		matcher = pattern.matcher(head);
		return matcher.find();
	}
	
	/**
	 * Function that generate the hash of the key passed. Right now the
	 * generated hash is a SHA-1 hash.
	 * 
	 * @param key:
	 * 		The key to get the hash from.
	 * @return:
	 * 		A String with the hash or null if there where problems.
	 * @throws IOException 
	 */
	public static String keyHash(PGPPublicKey key) {
		MessageDigest md;
		String result;
		
		/**
		 * TODO: The digest is being calculated with the Java standar classes.
		 * Since we are using The Bouncy Castle Legion provider I think it
		 * would be nicer if it is used as much as it can be. Also using just
		 * a provider reduce dependency. Change this digest calculation to use
		 * BC library.
		 */
		// Calculate the hash of the key and return its string representation.
		try {
			md = MessageDigest.getInstance(DIGEST_ALGORITHM);
			result = byteArray2HexStr(md.digest(key.getEncoded()));
			return result;
		}
		// This should never occur because we are using a standard algorithm,
		// this will occur if you change the DIGEST_ALGORITHM constant or if
		// that algorithm is removed from the standard java library.
		catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	/**
	 * Function that creates a directory in-depth. This means that it will
	 * create all the directories needed. 
	 */
	public static boolean mkdirInDepth(String path) {
		File fd, fd_parent;
		boolean result = true;
		
		try {
			String checked_path = checkDirectoryPath(path);
			fd = new File(checked_path);
			if (!fd.exists()) {
				fd_parent = fd.getParentFile();
				if (!fd_parent.exists())
					result = mkdirInDepth(fd_parent.getAbsolutePath());
				if (result)
					result = fd.mkdir();
			}
			else
				result = true;	
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
		return result;
	}
	
	/**
	 * Move a file to another location.
	 * 
	 * @param fname:
	 * 		The file to move.
	 * @param dir:
	 * 		The directory to move in.
	 * @return:
	 * 		True if success, false elsewhere.
	 */
	public static boolean moveFile(String fname, String dir) {
		File fd_file, fd_dir;
		
		// Check that dir exists and is a directory and that the file to be
		// moved also exists.
		fd_dir = new File(dir);
		fd_file = new File(fname);
		if (!fd_dir.exists() || !fd_dir.isDirectory() || !fd_file.exists())
			return false;
		
		// Move the file to the directory specified.
		return fd_file.renameTo(new File(dir, fd_file.getName()));
	}
	
	/**
	 * Function that stores in a byte array the date readed from an
	 * InputStream. This function should be use everytime we need to read data,
	 * no matter if from a file or from an url (or whatever), this function
	 * hides to the programmer the not really complexity but tedious work of
	 * have an account of how much data have been readed, repeat the reads
	 * until all the data is readed and calculate the offset in the buffer each
	 * time.
	 * 
	 * @param in: The InputStream to read from.
	 * @param len: The amount of data to read from the InputStream, if it is negative we
	 * try to guess the amount of data to read, this cannot assure that all the
	 * data in the InputStream is readed, we stongly recommend not to use this
	 * option :).
	 * @return: A byte array with the readed data.
	 * @throws IOException: Thrown if there are problems during the reading.
	 */
	public static byte[] recoverDatafromInputStream(InputStream in, int len)
	throws RuntimeException {
		byte res[];
		int remain, off, readed;
		try {
		
		if (len < 0)
			len = in.available();

		// We try to read as much bytes as we can but sometimes we cannot, so
		// we will try it again util the whole bytes are readed.
		res = new byte[len];
		remain = len;
		while (remain > 0) {
			off = len - remain;
			readed = in.read(res, off, remain);
			remain -= readed;
		}
		
		return res;
		}catch(Exception e) {
			throw new RuntimeException(e.getMessage());
		}
	}
	
	// OBSOLETE
//	/**Return a list of Blogs recovered from previous blogs downloaded in a path
//	 * @param blogDownloadedPath: Path with downloaded blogs 
//	 * @return List with recovered blogs
//	 * @throws Exception 
//	 * */
//	public static List<Blog> recoverDownloadedBlogFromPath(String path) throws Exception {
//		String checked_path = checkDirectoryPath(path);
//		File dir_path;
//		List<Blog> blogs;
//		
//		// Checks that this path is a directory.
//		dir_path = new File(checked_path);
//		if (!dir_path.isDirectory())
//			throw new Exception("\"" + path + "\" is not a directory.");
//		
//		blogs = new ArrayList<Blog>();
//		// Iterate over each file inside the directory.
//		for (File file: dir_path.listFiles()) {
//			// If it is a regular file and ends with ".blog".
//			if (file.isFile() &&
//				file.getName().endsWith(EXT_SEPARATOR + BLOG_EXT)) {
//				// Create the blog from that file and add it to the list.
//				blogs.add(Blog.createBlog(file.getAbsolutePath()));
//			}
//		}
//		
//		return blogs;
//	}
	
	/**
	 * Function that returns the byte array containing the sign contained at
	 * the end of a dusterized file.
	 * 
	 * @param fname: Filename of the dusterized file.
	 * @return: A byte array containing the sign of the file.
	 * @throws IOException: If there are errors reading the file.
	 */
	public static byte[] recoverSign(String fname)
	throws IOException {
		File fd;
		FileInputStream fis;
		byte sign[], buf_len_sign[];
		int remain, offset, readed, len_file, len_sign, skipped;
		
		// Opening the file to read from it.
		fd = new File(fname);
		fis = new FileInputStream(fd);
		len_file = (int)fd.length();
		
		// Reading the last byte of the file, it contains the length of the
		// sign.
		buf_len_sign = new byte[1];
		remain = 1;
		skipped = (int)fis.skip(len_file - 1);
		if (skipped < len_file - 1)
			throw new IOException();
		if (fis.read(buf_len_sign) < 1)
			throw new IOException();
		fis.close();
		
		// The sign is at the end of the file, except the last byte that
		// indicates the length of the sign.
		fis = new FileInputStream(fd);
		len_sign = (int)(buf_len_sign[0]);
		skipped = (int)fis.skip(len_file - len_sign - 1);
		if (skipped < len_file - len_sign - 1)
			throw new IOException();
		
		// Reading the bytes of the sign.
		sign = new byte[len_sign];
		remain = len_sign;
		while (remain > 0) {
			offset = len_sign - remain;
			readed = fis.read(sign, offset, remain);
			remain -= readed;
		}
		
		fis.close();
		return sign;
	}
	
	/**
	 * Function that removes a directory in-depth. This means that it will
	 * remove all the directories and files inside this directory (it also
	 * can remove a single file but this is not interesting). 
	 */
	public static boolean rmdirInDepth(String path) {
		File fd = new File(path);
		String complete_fname;
		String checked_path;
		boolean result = true;
		
		// It is a regular file, just remove it.
		if (!fd.isDirectory())
			result = fd.delete();
		// It is a directory.
		else {
			checked_path = checkDirectoryPath(path);
			
			// Remove each child file.
			for (String fname: fd.list()) {
				complete_fname = checked_path + fname;
				result &= rmdirInDepth(complete_fname);
			}
			
			// Once remove each child, remove the directory itself.
			result &= fd.delete();
		}
		return result;
	}
	
	/**
	 * Function that looks for the nth newer entry and returns its date.
	 * 
	 * @param list
	 * 		A list of SyndEntryImpl where we must search.
	 * @param n
	 * 		The nth older entry date we want to search for.
	 * @return
	 * 		The oldest date we have found inside the list. Note that this date
	 * 		may not be then nth older entry date if the list has more than one
	 * 		entry published in the same date (a bit weird but possible).
	 */
	public static Date searchNthNewerEntryDate(List<SyndEntryImpl> list,
											   int n) {
		SyndEntryImpl entry;
		Calendar cal = Calendar.getInstance();
		Date oldest_date_found;
		int n_entries_found, i;
		
		// n must be positive and less or than the number of entries inside
		// the list.
		if (n < 1 || n > list.size())
			throw new RuntimeException("the n passed is out of bounds.");
		
		// We will search for entries older than now.
		oldest_date_found = cal.getTime();
		
		// While we are still inside limits boundaries or if we haven't found
		// enough newer entries yet we iterate over the list.
		n_entries_found = 0;
		i = 0;
		while (i < list.size() && n_entries_found < n) {
			entry = list.get(i);
			
			// We have found an entry older than the oldest one that we have
			// found by now.
			if (entry.getPublishedDate().compareTo(oldest_date_found) < 0) {
				oldest_date_found = entry.getPublishedDate();
				n_entries_found++;
			}
			
			i++;
		}
		
		return oldest_date_found;
	}
	
	/**
	 * Reads the data from the specific file and sign it with the private key
	 * of the publisher. The returned byte array is the sign plus the last byte
	 * indicating the length of that sign (because it can vary from 46 to 48).
	 * 
	 * @param fname:
	 * 		The file name to read from.
	 * @param key:
	 * 		The private key to use for signing the data.
	 * @return:
	 * 		Returns a byte array containing the sign and the last byte the
	 * 		length of this sign.
	 * @throws IOException: Thrown if there are problems reading the file.
	 * @throws InvalidKeyException: Thrown if there are problems with the key.
	 * @throws NoSuchAlgorithmException: For convenience, in fact should never
	 * be thrown since we use a standard algorithm to sign. It will be thrown
	 * if the Signature library removes this algorithm.
	 * @throws SignatureException: Thrown if there are problems signing the
	 * data.
	 */
	public static byte[] signData(String fname, PrivateKey key)
	throws IOException, InvalidKeyException, NoSuchAlgorithmException,
	SignatureException {
		File fd;
		InputStream is;
		Signature signature;
		byte[] data;
		byte[] sign, sign_len;
		
		// Reading the data to sign.
		fd = new File(fname);
		is = (InputStream)new FileInputStream(fd); 
		data = recoverDatafromInputStream(is, (int)fd.length());
		
		// Preparing and calculating the sign.
		signature = Signature.getInstance(Publisher.ALGORITHM);
		signature.initSign(key);
		signature.update(data);
		sign = signature.sign();
		
		// We add to the sign a byte containing the length of the sign.
		sign_len = new byte[sign.length + 1];
		for (int i = 0; i < sign.length; i++)
			sign_len[i] = sign[i];
		sign_len[sign.length] = (byte)sign.length;
		is.close();
		return sign_len;
	}
	
	/**
	 * Genera un titulo ya acortado y con el SHA1 de la clave publica del publisher
	 * @param title: Titulo a acortar
	 * @param publisher: Publicador que lo firma
	 * @return Titulo con el MD5 ya a?adido
	 * @throws IOException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 * 
	 * */
	public static String titleWithHash(String title, Publisher publisher) 
	throws NoSuchAlgorithmException, NoSuchProviderException, IOException {
		String new_title;
		byte[] pubkey, pubkeyHash;
		
		pubkey = publisher.getPublicKey().getEncoded();
		
		// Acortamos titulo
		title = Util.shortTitle(title);
		
		// Generamos el MD5
		pubkeyHash = Util.generateHash(pubkey, DIGEST_ALGORITHM);
		new_title = title + NAME_SEPARATOR + Util.byteArray2HexStr(pubkeyHash);
		return new_title;
	}
	
	/**
	 * Reads the data from the specific file and checks the authenticity from
	 * the given sign and the public key of the publisher. This sign must be
	 * the real sign, i mean, it must not contain the last byte indicating the
	 * length of the sign (read commentaries at signData), this function should
	 * be called after call the recoverSign function.
	 * 
	 * @param path: The file name with the data to be verified.
	 * @param sign: Byte array containing the sign.
	 * @param publisher: Publisher object that contains the public key to
	 * verify the data.
	 * @return: True if and only if the sign is correct so the data is
	 * authenticated. 
	 * @throws IOException: Thrown if there are problems reading the data. 
	 * @throws NoSuchAlgorithmException: Should never be thrown since we use
	 * an standard algorithm, it will be thrown if the Signature library
	 * removes this algorithm or changes its name. 
	 * @throws InvalidKeyException: Thrown if there are problems with the key.
	 * @throws SignatureException: Thrown if there are problems with the
	 * signature. 
	 */
	public static boolean verifySign(byte[] data, byte[] sign,
									 PublicKey key)
	throws IOException, NoSuchAlgorithmException, InvalidKeyException,
	SignatureException {
		Signature signature;
		
		// Preparing the sign.
		signature = Signature.getInstance(Publisher.ALGORITHM);
		signature.initVerify(key);
		signature.update(data);
		
		// Checking the sign.
		return signature.verify(sign);
	}
	
	/**
	 * Function that creates a zip file from a directory. This zip file will
	 * contain all the files and directories contained by the directory passed.
	 * 
	 * There is an issue with the java.util.zip library, it doesn't encode
	 * properly the names of the files, so if the platform is not running with
	 * UTF-8 locales (the native JVM locales) the files inside the zip may
	 * contain strange characters. The bug is documented here:
	 * 
	 * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4244499
	 * 
	 * @param path
	 * 		The path to the directory to zip.
	 * @param zipname
	 * 		The name for the zip file.
	 * @throws IOException
	 * @throws RuntimeException
	 */
	public static void zipDir(String path, String zipname)
	throws IOException {
		FileOutputStream fos;
		File fd;
		ZipOutputStream zos;
		String checked_path = checkDirectoryPath(path);
		
		// Checks if the path passed is a directory.
		fd = new File(checked_path);
		if (!fd.isDirectory())
			throw new RuntimeException("The path " + path + " is not a " +
									   "directory.");
		
		// Preparing the zip file.
		fos = new FileOutputStream(zipname);
		zos = new ZipOutputStream(fos);
		
		// Do all the zipping stuff.
		doZipDir(checked_path, checked_path, zos);
		zos.close();
	}
}

