package dbsi.type;

import java.util.LinkedList;

import dbsi.Main;
import dbsi.classes.HashPair;
import dbsi.classes.OverflowFile;
import dbsi.exception.BucketException;
import dbsi.exception.OverflowException;
import dbsi.helper.ByteHelper;

public class Bucket {
	
	public static final Integer RECORDS_PER_BUCKET = 1;
	public static final Integer POINTER_LENGTH = 8;
	
	Integer bucketSize;
	TypeFormat columnFormat;
	Integer columnLength;
	byte[] bucketByte;
	
	/**
	 * Gets the Bucket Size.
	 * @param columnLength
	 * @return
	 */
	public static Integer getBucketSize(int columnLength) {
		return (RECORDS_PER_BUCKET * (columnLength + POINTER_LENGTH)) + POINTER_LENGTH;
	}
	
	/**
	 * creats a Bucket
	 * @param columnLength
	 * @param columnFormat
	 */
	public Bucket(int columnLength, TypeFormat columnFormat) {
		this.columnLength = columnLength;
		this.bucketSize = getBucketSize(columnLength);
		this.bucketByte = new byte[this.bucketSize];
		this.columnFormat = columnFormat;
	}
	
	public Bucket(Bucket anotherBucket) {
		this.bucketSize = anotherBucket.bucketSize;
		this.bucketByte = new byte[this.bucketSize];
		System.arraycopy(anotherBucket.bucketByte, 0, this.bucketByte, 0, this.bucketByte.length - POINTER_LENGTH);
		this.columnFormat = anotherBucket.columnFormat;
		this.columnLength = anotherBucket.columnLength;
	}
	
	/**
	 * gets the HashPair at the specidied index.
	 * @param index
	 * @return
	 * @throws BucketException
	 */
	public HashPair getHashPair(int index) throws BucketException {
		if(index >= RECORDS_PER_BUCKET)
			throw new BucketException("Queried index("+index+") is greaeter that NumOfRecords/Bucket("+RECORDS_PER_BUCKET+")");
		HashPair hashPair = new HashPair();
		int startIndex = (index) * (this.columnLength + POINTER_LENGTH);
		hashPair.setValue(this.columnFormat.toType(this.bucketByte, startIndex, this.columnLength));
		hashPair.setRecId(ByteHelper.rawToI8(this.bucketByte, startIndex + this.columnLength));
//		System.out.println("-getHashPair- index="+index+", pair="+hashPair);
		return hashPair;
	}
	
	public LinkedList<HashPair> getAllHashPairs(OverflowFile overflowFile) {
		LinkedList<HashPair> allHashPairs = new LinkedList<HashPair>();
		try {
			HashPair hp = null;
			for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
				hp = getHashPair(i);
				if(hp.isEmpty()) {
					if(Main.DEBUG) System.out.println(">>> === HashPair[" + i + "] is Empty. Returning allHashPairs List.");
					// if bucket has empty hashpair, then there is no overflow bucket at all.
					return allHashPairs;	
				} else {
					allHashPairs.offer(hp);
				}
			}
			if(Main.DEBUG) System.out.println(">>> === End of Bucket is reached. Trying Overflow");
			Long overflowBucketIndex = this.getPointer();
			if(overflowBucketIndex == null || overflowBucketIndex.longValue() == 0) {
				// if no Overflow pointer, then return all found hashpairs.
				return allHashPairs;
			}
			
			// in Overflow File
			Bucket currentBucket = null;
			while(true) {
				try {
					currentBucket = overflowFile.getBucket(overflowBucketIndex.longValue(), false);
					if(Main.DEBUG) System.out.println(">>> ===  overflowBucketIndex = " + overflowBucketIndex);
					for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
						hp = currentBucket.getHashPair(i);
						if(hp.isEmpty()) {
							if(Main.DEBUG) System.out.println(">>> === HashPair[" + i + "] is Empty. Returning allHashPairs List.");
							// if current bucket has empty hashpair, then there is no overflow bucket at all.
							return allHashPairs;
						} else {
							allHashPairs.offer(hp);
						}
					}
					overflowBucketIndex = currentBucket.getPointer();
					if(overflowBucketIndex == null || overflowBucketIndex.longValue() == 0) {
						// if Pointer of any bucket is null of 0, then there is no next nucket
						return allHashPairs;
					}
					
				} catch (OverflowException e) {
					e.printStackTrace();
				} catch (BucketException e) {
					e.printStackTrace();
				}
			}
		} catch (BucketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Insert/Replace hashPair at the specified index.
	 * @param index
	 * @param hashPair
	 * @throws BucketException
	 */
	private void insertHashPairAtIndex(int index, HashPair hashPair) throws BucketException {
		if(index >= RECORDS_PER_BUCKET)
			throw new BucketException("Try to insert at index("+index+") is greaeter that NumOfRecords/Bucket("+RECORDS_PER_BUCKET+")");
		int startIndex = (index) * (this.columnLength + POINTER_LENGTH);
		if(Main.DEBUG) System.out.println(">>> start index = " + startIndex);
		if(Main.DEBUG) System.out.println(">>> before - " + new String(this.bucketByte));
		byte[] valByte = this.columnFormat.toRaw(hashPair.getValue(), this.columnLength);
		System.arraycopy(valByte, 0, this.bucketByte, startIndex, valByte.length);
		startIndex += valByte.length;
		byte[] recIDByte = ByteHelper.i8ToRaw(hashPair.getRecId());
		System.arraycopy(recIDByte, 0, this.bucketByte, startIndex, recIDByte.length);
		if(Main.DEBUG) System.out.println(">>>  after - " + new String(this.bucketByte));
	}
	
	/**
	 * adds the given HashPair to the Bucket at the next available free location.
	 * @param hashPair
	 */
	public Boolean putHashPair(HashPair hashPair, OverflowFile overflowFile) {
		try {
			for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
				if(getHashPair(i).isEmpty()) {
					if(Main.DEBUG) System.out.println(">>> HashPair[" + i + "] is Empty. Inserting " + hashPair);
					insertHashPairAtIndex(i, hashPair);
					return false;
				}
			}
			if(Main.DEBUG) System.out.println(">>> +++ Bucket is Full, Trying Overflow");
			if(this.getPointer() == null || this.getPointer().longValue() == 0) {
				this.setPointer(overflowFile.getNextFreeBucketIndex());
			}
			return putHashPairInNextFreeBucketInChain(overflowFile, hashPair, this.getPointer());
		} catch (BucketException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * Puts the HashPair in the NextFreeBucket available in the overflow file.
	 * @param overflowFile
	 * @param hashPair
	 * @param overflowBucketIndex
	 */
	private Boolean putHashPairInNextFreeBucketInChain(OverflowFile overflowFile, HashPair hashPair, long overflowBucketIndex) {
		Bucket currentBucket = null;
		if(Main.DEBUG) System.out.println(">>> +++ overflowBucketIndex 0 = " + overflowBucketIndex);
		while(true) {
			try {
				currentBucket = overflowFile.getBucket(overflowBucketIndex, false);
				for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
					if(Main.DEBUG) System.out.println(">>> +++ +++ i: " + i);
					if(currentBucket.getHashPair(i).isEmpty()) {
						if(Main.DEBUG) System.out.println(">>> +++ overflowBucketIndex 1 = " + overflowBucketIndex);
						if(Main.DEBUG) System.out.println(">>> +++ HashPair[" + i + "] is Empty. Inserting " + hashPair);
						if(Main.DEBUG) System.out.println(currentBucket);
						currentBucket.insertHashPairAtIndex(i, hashPair);
						if(Main.DEBUG) System.out.println(currentBucket);
						if(Main.DEBUG) System.out.println(">>> +++ overflowBucketIndex 2 = " + overflowBucketIndex);
						overflowFile.putBucket(overflowBucketIndex, currentBucket);
						return true;
					}
				}
				if(currentBucket.getPointer() == null || currentBucket.getPointer().longValue() == 0) {
					currentBucket.setPointer(overflowFile.getNextFreeBucketIndex());
					overflowFile.putBucket(overflowBucketIndex, currentBucket);
				}
				overflowBucketIndex = currentBucket.getPointer();
			} catch (OverflowException e) {
				e.printStackTrace();
				continue;
			} catch (BucketException e) {
				e.printStackTrace();
			}
		}
	}
	
	public void setPointer(Long pointer) {
		byte[] longbyte = ByteHelper.i8ToRaw(pointer.longValue());
		int destPos = (RECORDS_PER_BUCKET * (columnLength + POINTER_LENGTH)); 
		System.arraycopy(longbyte, 0, this.bucketByte, destPos, longbyte.length);
	}
	
	public Long getPointer() {
		byte[] longByte = new byte[8];
		int srcPos = (RECORDS_PER_BUCKET * (columnLength + POINTER_LENGTH));
		System.arraycopy(this.bucketByte, srcPos, longByte, 0, longByte.length);
		return ByteHelper.rawToI8(longByte, 0);
	}
	
	public int getBucketSizeInBytes() {
		return this.bucketSize;  
	}
	
	public byte[] getBytes() {
		return this.bucketByte;
	}
	public void setBytes(byte[] bytes) throws BucketException {
		if(bytes.length != this.bucketByte.length)
			throw new BucketException("Attempting to set Bucket Bytes with incompatiable byte[]. " +
					"BucketByte length = " + this.bucketByte.length + ", but incoming bytes length = " + bytes.length);
		this.bucketByte = bytes;
	}
	
	public boolean isEmpty() {
		boolean toRet = true;
		for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
			try {
				toRet = toRet && getHashPair(i).isEmpty();
			} catch (BucketException e) {
				e.printStackTrace();
			}
		}
		return toRet;
	}
	
	@Override
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[Bucket [bucketSize=").append(this.bucketSize).
				append("][collen=").append(this.columnLength).
				append("][byte=").append(new String(this.bucketByte)).
				append("][colFormat=").append(this.columnFormat).
				append("] ]");
		return sb.toString();
	}
	
	public void printDebug(OverflowFile overflowFile) {
		HashPair hp = null;
		try{
			for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
				hp = getHashPair(i);
				hp.printDebug();
				System.out.print(" , ");
			}
			System.out.println(":OVER-Pointer: " +  getPointer());
			
			Bucket b = this;
			while(true) {
				try {
					if(b.getPointer() == null || b.getPointer().longValue() == 0) {
						break;
					}
					System.out.print("    OverflowIndex ::: " + b.getPointer() + " == ");
					b = overflowFile.getBucket(b.getPointer(), false);
					for(int i=0; i<RECORDS_PER_BUCKET; ++i) {
						hp = b.getHashPair(i);
						hp.printDebug();
						System.out.print(" , ");
					}
					System.out.println(":NEXT-Pointer: " +  b.getPointer());
				} catch (OverflowException e) {
					e.printStackTrace();
					System.exit(0);
				} catch (BucketException e) {
					e.printStackTrace();
				}
			}
		} catch (BucketException exe) {
			exe.printStackTrace();
		}
	}

}

