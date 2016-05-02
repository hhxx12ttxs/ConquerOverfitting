package dbsi.classes;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.LinkedList;

import dbsi.Main;
import dbsi.exception.BucketException;
import dbsi.exception.InvalidFileException;
import dbsi.exception.OverflowException;
import dbsi.helper.ByteHelper;
import dbsi.type.Bucket;
import dbsi.type.TypeFormat;

public class OverflowFile {

	private Integer headerLength = 4;
	private File overflow;
	private Integer columnLength;
	private TypeFormat columnFormat;
	private Integer bucketSize;
	private LinkedList<Long> freeBucketIndexes;

	/**
	 * 
	 * @param path
	 * @param isToBeCreated
	 * @param columnLength
	 * @param colFormat
	 * @throws InvalidFileException
	 */
	public OverflowFile(String path, boolean isToBeCreated, int columnLength, TypeFormat colFormat) throws InvalidFileException {
		try {
			this.columnLength = columnLength;
			this.columnFormat = colFormat;
			this.bucketSize = Bucket.getBucketSize(this.columnLength);
			this.freeBucketIndexes = new LinkedList<Long>();
			if (isToBeCreated) {
				overflow = new File(path + ".over");
				if(overflow.exists())
					throw new InvalidFileException("Attempting to create an Overflow File("+path+".over) that already exist!!!");
				overflow.createNewFile();
				initialize(columnLength);
				freeBucketIndexes.add((long)1);				
			} else {
				overflow = new File(path + ".over");
				if(!overflow.exists())
					throw new InvalidFileException("Attempting to read an Overflow File("+path+".over) that does NOT exist!!!");
				getFreeBucketIndexes();
			}
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
	
	/**
	 * initializes the header and writes the first bucket to file.
	 * @param colLen
	 */
	private void initialize(int colLen) {
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(overflow, "rw");
			raf.write(ByteHelper.i4ToRaw(colLen));
			raf.write(new byte[this.bucketSize]);		// create bucket 0 
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	public boolean isExists() {
		if(this.overflow == null) return false;
		return this.overflow.exists();
	}
	
	public void getFreeBucketIndexes() {
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(overflow, "r");
			raf.seek(4); // skip colLength
			byte[] recByte = new byte[this.bucketSize];
			Bucket recBucket = new Bucket(this.columnLength, this.columnFormat);
			long index = 1;
			while(raf.read(recByte) != -1) {
				recBucket.setBytes(recByte);
				if(recBucket.isEmpty()) 
					this.freeBucketIndexes.offer(index);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BucketException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * returns the nextFreeBucket. Caution while using this. 
	 * @return
	 */
	public Bucket getNextFreeBucket() {
		if(freeBucketIndexes.isEmpty()) {
			freeBucketIndexes.add(getNewBucketIndex());
			putBucket(freeBucketIndexes.peek(), new Bucket(this.columnLength, this.columnFormat));
		}
		try {
			return getBucket(freeBucketIndexes.poll(), true);
		} catch (OverflowException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * returns the nextFreeBucket's Index
	 * @return
	 */
	public Long getNextFreeBucketIndex() {
		if(freeBucketIndexes.isEmpty()) {
			freeBucketIndexes.add(getNewBucketIndex());
			if(Main.DEBUG) System.out.println(">>> +++ creating new free bucket: " + freeBucketIndexes.peek());
			putBucket(freeBucketIndexes.peek(), new Bucket(this.columnLength, this.columnFormat));
		}
		if(Main.DEBUG) System.out.println(">>> +++ getNextFreeBucketIndex: " + freeBucketIndexes.peek());
		return freeBucketIndexes.poll();
	}	
	
	/**
	 * Returns the index of the new bucket.
	 * @return
	 */
	private Long getNewBucketIndex() {
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(overflow, "rw");
			long fileLength = raf.length();
			return 1 + (fileLength - this.headerLength) / this.bucketSize;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
	
	/**
	 * inserts/replaces the bucket.
	 * @param bucketIndex
	 * @param bucket
	 */
	public void putBucket(long bucketIndex, Bucket bucket) {
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(overflow, "rw");
			long toSeek = this.headerLength + ((bucketIndex-1) * this.bucketSize);
			raf.seek(toSeek);
			raf.write(bucket.getBytes());
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	/**
	 * gets the bucket at the given Index. 
	 * 
	 * @param bucketIndex
	 * @param createBucketIfNotPresent - if true creats a new bucket if bucket dosent exist in the specified index.
	 * @return
	 * @throws OverflowException
	 */
	public Bucket getBucket(long bucketIndex, boolean createBucketIfNotPresent) throws OverflowException {
		RandomAccessFile raf = null;
		Bucket bucket = null;
		try{
			raf = new RandomAccessFile(overflow, "rw");
			long toSeek = this.headerLength + ((bucketIndex-1) * this.bucketSize);
			if( !createBucketIfNotPresent && (toSeek >= raf.length()) )
				throw new OverflowException("BucketIndex(" + (bucketIndex-1) + ") is NOT yet created!!");
			raf.seek(toSeek);
			byte[] bucketBytes = new byte[this.bucketSize]; 
			int result = raf.read(bucketBytes);
			if(result == -1) {
				raf.seek(toSeek);
				bucketBytes = new byte[this.bucketSize]; 
				raf.write(bucketBytes);
			}
			bucket = new Bucket(this.columnLength, this.columnFormat);
			bucket.setBytes(bucketBytes);
			return bucket;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BucketException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		return null;
	}
	
	public void freeLinkedBucketsAtIndex(Long index) {
		RandomAccessFile raf = null;
		Bucket bucket = null;
		byte[] bucketBytes = null;
		try{
			raf = new RandomAccessFile(overflow, "rw");
			while(true) {
				if(Main.DEBUG) System.out.println(">>> --- freeing bucket at index = " + index);
				long toSeek = this.headerLength + ((index-1) * this.bucketSize);
				// read the bucket at index
				raf.seek(toSeek);
				bucketBytes = new byte[this.bucketSize]; 
				raf.read(bucketBytes);
				bucket = new Bucket(this.columnLength, this.columnFormat);
				bucket.setBytes(bucketBytes);
				// write empty bucket at index which is nothing but freeing the bucket
				raf.seek(toSeek);
				bucketBytes = new byte[this.bucketSize];
				raf.write(bucketBytes);
				// add the freed bucket index to freeBucketIndexes.
				this.freeBucketIndexes.offer(index);
				index = bucket.getPointer();
				if(Main.DEBUG) System.out.println(">>> --- next pointer of freed bucket = " + index);
				if(index == null || index .longValue() == 0)
					return;
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BucketException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[OverflowFile [name=").append(this.overflow.getName()).
				append("][ColLength=").append(this.columnLength).
				append("][ColFormat=").append(this.columnFormat).
				append("] ]");
		return sb.toString();
	}
	
	public void printDebug() {
		System.out.println("--- OverflowFile Contents");
		System.out.println(this);
		RandomAccessFile raf = null;
		try{
			raf = new RandomAccessFile(this.overflow, "r");
			raf.seek(this.headerLength);
			Bucket bucketAtIndex = null;
			byte[] bucketByte = new byte[this.bucketSize];
			int index = 0;
			while(raf.read(bucketByte) != -1) {
				bucketAtIndex = new Bucket(this.columnLength, this.columnFormat);
				bucketAtIndex.setBytes(bucketByte);
				System.out.println("->OverflowIndex = " + (index+1)  + "<- ");
				System.out.println("  bucket: " + bucketAtIndex);
				bucketAtIndex.printDebug(this);
				index++;
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (BucketException e) {
			e.printStackTrace();
		} finally {
			if(raf != null)
				try {
					raf.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
		}
		System.out.println("--- END OverflowFile Contents");
	}

}

