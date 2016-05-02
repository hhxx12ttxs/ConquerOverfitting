/**
 * File: MaqMatchExec.java
 * Created by: mhaimel
 * Created on: 2 Apr 2009
 * CVS:  $Id: MaqMatchExec.java,v 1.3 2009/08/11 10:30:12 mhaimel Exp $
 */
package uk.ac.ebi.velvet.exec.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.zip.CRC32;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.NotImplementedException;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import uk.ac.ebi.curtain.utils.data.FileType;
import uk.ac.ebi.curtain.utils.data.ReadType;
import uk.ac.ebi.curtain.utils.data.impl.MaqIdentifierProcessor;
import uk.ac.ebi.curtain.utils.data.impl.SolexaIdentifierProcessor;
import uk.ac.ebi.curtain.utils.file.FileInfo;
import uk.ac.ebi.curtain.utils.file.SplitUtil;
import uk.ac.ebi.curtain.utils.file.impl.RollingFileSplitProcessor;
import uk.ac.ebi.curtain.utils.io.impl.FileIO;
import uk.ac.ebi.velvet.VelvetUncheckedException;
import uk.ac.ebi.velvet.config.VelvetConfig;
import uk.ac.ebi.velvet.exec.ProcessorService;
import uk.ac.ebi.velvet.util.config.ConfigFactory;

/**
 * @author mhaimel
 *
 */
public class MaqMatchExec extends AbstractExec implements Callable<Collection<FileInfo>> {
//	private static final long _MIN_FILE_BYTE_SIZE = 1073741824L; // 1 GB
	private static final long _MIN_FILE_BYTE_SIZE = 1000000000L; // 1 GB
	private static final String MAPVIEW_NO_SEQU_OPT = "-b";
	private static final String MAPVIEW_OPT = "mapview";
	private static final String MATCH_PAIRED_OPT = "-a";
	private static final String MATCH_OPT = "match";
	private static final String FASTQ2BFQ_OPT = "fastq2bfq";
	private Log log = LogFactory.getLog(this.getClass());
	private FileInfo read;
	private File ctgbfa;
	private File baseDir;
	private VelvetConfig config;
	private Integer insertLength = null;
	private ProcessorService pc = null;
	private File readBaseDir;
	
	public MaqMatchExec(File basedir, File ctgbfa, FileInfo read) {
		this(basedir, ctgbfa, read, null);
	}
	
	public MaqMatchExec(File basedir, File ctgbfa, FileInfo read, Integer insertLength) {
		this.ctgbfa = ctgbfa;
		this.read = read;
		this.baseDir = basedir;
		this.insertLength = insertLength;
	}

	@Override
	public Collection<FileInfo> call() throws Exception {
		return runAllMaqMatch();
	}
	
	public Collection<FileInfo> runAllMaqMatch(){
		List<FileInfo> fastqs = getFastqFiles();
		List<File> bfqFiles = transform2Bfq(fastqs);
		Collection<File> mapFiles = matchFiles(bfqFiles);
		Collection<File> txtMapFiles = map2Txt(mapFiles);
		List<FileInfo> txtFileInfos = wrap(getRead().getReadType(),FileType.maq,txtMapFiles);
		return txtFileInfos;
	}
	
	public Collection<File> map2Txt(Collection<File> mapFileColl) {
		List<File> txtList = new ArrayList<File>();
		for(File mapFile : mapFileColl){
			getLog().debug("Extract mapping information into output file ...");
			File txtFile = new File(getReadBaseDir(),mapFile.getName()+".txt");
			List<String> cmds = new ArrayList<String>();
			cmds.add(getConfig().getMaqExec());
			cmds.add(MAPVIEW_OPT);
			cmds.add(MAPVIEW_NO_SEQU_OPT);
			cmds.add(mapFile.getAbsolutePath());
			
			File errF = getNextFile(getReadBaseDir(), txtFile.getName()+"_", ".err");
			exec(getReadBaseDir(), cmds.toArray(new String[0]), txtFile, errF);
			if(txtFile.length() <= 0){
				getLog().warn("No Mapping information extracted from " + mapFile);
			}
			txtList.add(txtFile);
		}
		return txtList;
	}

	public Collection<File> matchFiles(List<File> bfqFileList) {
		List<File> matchColl = new ArrayList<File>();
		getLog().debug("Matching bfq files to bfa file ...");
		int stepSize = 1;
		if(isReadPaired()){
			stepSize = 2;
		}
		for(int i = 0; i < bfqFileList.size(); i += stepSize){
			List<File> subList = bfqFileList.subList(i, i+stepSize);
			File mapFile = new File(getReadBaseDir(),subList.get(0).getName()+".map");
			List<String> cmds = new ArrayList<String>();
			cmds.add(getConfig().getMaqExec());
			cmds.add(MATCH_OPT);
			if(subList.size() > 1 && getInsertLength() != null){
				getLog().debug("User insert length information for maq of " + getInsertLength());
				// use read paired information
				cmds.add(MATCH_PAIRED_OPT);
				cmds.add(Integer.valueOf(Double.valueOf(getInsertLength()*1.4).intValue()).toString());
			}
			cmds.add(mapFile.getAbsolutePath());
			cmds.add(getCtgbfa().getAbsolutePath());
			for(File f : subList){
				cmds.add(f.getAbsolutePath());
			}
			File logF = getNextFile(getReadBaseDir(), mapFile.getName()+"_", ".log");
			File errF = getNextFile(getReadBaseDir(), mapFile.getName()+"_", ".err");
			exec(getReadBaseDir(), cmds.toArray(new String[0]), logF, errF,false,true);
			matchColl.add(mapFile);
		}
		return matchColl;
	}

	public List<File> transform2Bfq(List<FileInfo> fileList) {
		getLog().debug("Transform Fastq 2 bfq: " + fileList.size());
		List<File> list = new ArrayList<File>();
		for(FileInfo fi : fileList){
			list.add(transform2Bfq(fi));
		}
		return list;
	}
	private File transform2Bfq(FileInfo file) {
		getLog().debug("Transform file 2 bfq " + file);
		File bfq = new File(getReadBaseDir(),file.getNameWithoutFilePostfix()+".bfq");
		List<String> cmds = new ArrayList<String>();
		cmds.add(getConfig().getMaqExec());
		cmds.add(FASTQ2BFQ_OPT);
		cmds.add(file.getFile().getAbsolutePath());
		cmds.add(bfq.getAbsolutePath());
		File logF = getNextFile(getReadBaseDir(), bfq.getName()+"_", ".log");
		File errF = getNextFile(getReadBaseDir(), bfq.getName()+"_", ".err");
		exec(getReadBaseDir(), cmds.toArray(new String[0]), logF, errF,false,true);
		return bfq;
	}

	public List<FileInfo> getFastqFiles() {
		List<FileInfo> files = new ArrayList<FileInfo>();
		FileInfo fastq = getRead();
		if(fastq.getFileType().equals(FileType.fasta)){
			FileInfo out = new FileInfo(
					new File(
							getReadBaseDir(),
							FileType.fastq.addPostfix(
									getRead().getNameWithoutFilePostfix())),
					FileType.fastq,
					getRead().getReadType());
			transform(getRead(), out);
			fastq = out;
		} else if(! fastq.getFileType().equals(FileType.fastq)) {
			// anything else than fastq
			throw new NotImplementedException(
					"Read files only supported in fasta and fastq file format");
		}
		if(isSplitRequired(fastq)){
			if(isReadPaired()){
				files.addAll(splitFastq(fastq,2,true));
				// even number for paired reads
				if(files.size()%2 != 0){
					throw new VelvetUncheckedException(
							"Even amount of files expected for paired reads: " 
							+ files.size() + " files: " + StringUtils.join(files,','));
				}
			} else {
				files.addAll(splitFastq(fastq,1,false));
			}
		} else {
			files.add(fastq);
		}
		return files;
	}

	private boolean isSplitRequired(FileInfo fastq) {
		return isReadPaired() || (hasMaxFileSize() && fastq.getFile().length() > getMaxFileSize()); // > 1GB (1024*1024*1024)
	}
	
	protected boolean hasMaxFileSize(){
		return getMaxFileSize() > _MIN_FILE_BYTE_SIZE;
	}

	protected long getMaxFileSize() {
		return getConfig().getMaqMaxFastqFile();
	}

	private Collection<? extends FileInfo> splitFastq(FileInfo fastq, int nFiles, boolean idConv) {
		getLog().debug("Split read file into " + nFiles + " subfiles: " + fastq);
		SplitUtil splitter = fastq.getFileType().getSplitter();
		BufferedReader in = null;
		long maxSize = fastq.getFile().length();
		if(hasMaxFileSize()){
			getLog().debug("Set max file size to " + getMaxFileSize() + "!");
			maxSize = getMaxFileSize();
		}
		RollingFileSplitProcessor proc = new RollingFileSplitProcessor(
				fastq.getFileType(), 
				getReadBaseDir(),
				"split-reads",
				nFiles,
				maxSize);
		try{
			in = new FileIO(fastq.getFile()).getBufferedReader();
			if(idConv){
				splitter.split(new SolexaIdentifierProcessor(),new MaqIdentifierProcessor(), in, proc);
			} else {
				splitter.split(null,null, in, proc);
			}
		} catch (Exception e) {
			throw new VelvetUncheckedException("Problems while splitting file " + fastq,e);
		} finally{
			FileIO.closeQuietly(proc);
			IOUtils.closeQuietly(in);
		}
		ReadType rt = fastq.getReadType();
		log.debug("Finished splitting " + fastq + " into " + StringUtils.join(proc.getFileList(),','));
		List<FileInfo> fastqList = wrap(rt, fastq.getFileType(), proc.getFileList());
		return fastqList;
	}
	
	private List<FileInfo> wrap(ReadType rType,FileType fType,Iterable<File> fileColl){
		List<FileInfo> fileList = new ArrayList<FileInfo>();
		for(File f : fileColl){
			fileList.add(new FileInfo(f,fType,rType));
		}
		return fileList;
	}

	private void transform(FileInfo read, FileInfo out) {
		getLog().debug("Transform file "+read + " --> " + out);
		try {
			FileType.transform(read, out);
		} catch (IOException e) {
			throw new VelvetUncheckedException(
					"Problems transforming Read: " + read + " --> " + out);
		}
	}

	private FileInfo getRead() {
		return read;
	}
	
	private boolean isReadPaired(){
		return getRead().getReadType().isPaired();
	}
	
	private File getReadBaseDir(){
		if(null == readBaseDir){
			CRC32 crc = new CRC32();
			crc.update(getRead().getFile().getAbsolutePath().toString().getBytes());
			String hexString = Long.toHexString(crc.getValue());
			readBaseDir = new File(baseDir,getRead().getNameWithoutFilePostfix()+"_"+hexString);
			if(!readBaseDir.exists() && !readBaseDir.mkdirs()){
				throw new VelvetUncheckedException("Not able to create directory " + readBaseDir);
			}
		}
		return readBaseDir;
	}
	
	private Log getLog() {
		return log;
	}
	private VelvetConfig getConfig() {
		if(null == config){
			config = ConfigFactory.create(VelvetConfig.class);
		}
		return config;
	}

	private Integer getInsertLength() {
		return insertLength ;
	}
	
	public void setInsertLength(Integer insertLength) {
		this.insertLength = insertLength;
	}
	
	private File getCtgbfa() {
		return ctgbfa;
	}

	public void setProcessorService(ProcessorService pc){
		this.pc  = pc;
	}

	@Override
	protected ProcessorService getProcessorService() {
		return pc;
	}
}

