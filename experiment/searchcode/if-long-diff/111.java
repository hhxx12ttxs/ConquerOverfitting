package domain;

/**
 * This class represents a task that can occur on a file. This can be uploading, downloading
 * or even hashing a file. The reason for the existance of this file is to visualize the
 * downloading and uploading that's done by the users.
 * 
 * @author Sam Verschueren		<sam.verschueren@gmail.com>
 * @since 6/12/12
 */
public class FileTask {

	private String md5;
	private String fileName;
	private long fileSize;
	private String status;
	private long downloadedSize = 0;
	private boolean isDone = false;	
	
	private long startTime = 0;
	
	private double averageSpeed;
	private int speedNumbers;

	public FileTask(String name) {
		this.setFileName(name);
		
		this.startTime = System.currentTimeMillis();
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public long getFileSize() {
		return fileSize;
	}

	public void setFileSize(long fileSize) {
		this.fileSize = fileSize;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
	
	public int getProgress() {
		if(this.fileSize == 0)
			return 0;
		
		double progress = (double)this.downloadedSize/this.fileSize*100.0;
		
		return (int)(progress);
	}

	public long getSpeed() {
		long now = System.currentTimeMillis();
		
		long diff = now-this.startTime;
		
		if(diff/1000 == 0)
			return 0;
		
		return this.downloadedSize/(diff/1000);
	}

	public int getEta() {		
		this.averageSpeed += this.getSpeed();
		this.speedNumbers++;
		
		double speed = this.averageSpeed/this.speedNumbers;

		if(speed == 0) {
			return Integer.MAX_VALUE;
		}
		
		return (int) ((this.fileSize-this.downloadedSize)/speed);
	}

	public long getDownloadedSize() {
		return this.downloadedSize;
	}

	public void setDownloadedSize(long downloadedSize) {
		this.downloadedSize = downloadedSize;
	}

	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}
	
	public void done() {
		this.isDone = true;
	}
	
	public boolean isDone() {
		return this.isDone;
	}
}

