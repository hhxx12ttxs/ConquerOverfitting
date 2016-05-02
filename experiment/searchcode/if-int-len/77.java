package com.objectwave.utility;

import java.io.*;
import java.util.*;
/**
 *  Extension fo the RandomAccessFile to cache the file until flush() is called.
 *  Usable with the <code>com.objectwave.persist.FileBroker</code> . Publically
 *  identical to <code>java.io.RandomAccessFile</code> , except for the
 *  constuctor, <code>flush()</code> , and <code>readFile()</code> . <p>
 *
 *  <b>Note:</b> This class is not threadsafe.
 *
 * @author  Steven Sinclair
 * @version  $Id: CachedRandomAccessFile.java,v 2.2 2004/12/14 02:26:07 dave_hoag Exp $
 * @see  java.io.RandomAccessFile
 */
public class CachedRandomAccessFile extends BufferedRandomAccessFile
{

/////////////////////////////// Implementation

	int blockSize;
	long fileLength;
	long filePos;
	ArrayList blockList;
	FileBlock firstModifiedBlock = null;

	/**
	 *  Constructor for the CachedRandomAccessFile object
	 *
	 * @param  file Description of Parameter
	 * @param  mode Description of Parameter
	 * @param  blockSize Description of Parameter
	 * @param  fullCache Description of Parameter
	 * @exception  IOException Description of Exception
	 */
	public CachedRandomAccessFile(File file, String mode, int blockSize, boolean fullCache) throws IOException
	{
		super(file, mode);
		fileLength = delegate.length();

		this.blockSize = blockSize;
		int numBlocks = (int) (fileLength / blockSize + 1);
		blockList = new ArrayList(numBlocks);

		for(int i = 0; i < numBlocks; i++)
		{
			FileBlock block = getBlock(i * blockSize);
			if(fullCache)
			{
				block.read();
				if(block.len < blockSize)
				{
					break;
				}
			}
		}
	}

	/**
	 *  Sets the Length attribute of the CachedRandomAccessFile object
	 *
	 * @param  newLength The new Length value
	 * @exception  IOException Description of Exception
	 */
	public void setLength(long newLength) throws IOException
	{
		delegate.setLength(newLength);
		FileBlock block = getBlock(newLength);

		if(newLength < length())
		{
			// file size has been reduced: remove blocks.

			for(int i = (int) (newLength / blockSize + 1); i < blockList.size(); i++)
			{
				blockList.remove(i);
			}
		}
		else
		{
			// file size has grown: create new (unloaded) blocks

			for(int i = blockList.size(); i < (int) (newLength / blockSize); i++)
			{
				getBlock(i);
			}
		}
		fileLength = newLength;
	}

/////////////////////////////  Support Reader & Writer

	/**
	 *  Gets the Reader attribute of the CachedRandomAccessFile object
	 *
	 * @return  The Reader value
	 */
	public Reader getReader()
	{
		return
			new Reader()
			{
				/**
				 *  Description of the Method
				 *
				 * @exception  IOException Description of Exception
				 */
				public void close() throws IOException
				{
					CachedRandomAccessFile.this.close();
				}
				/**
				 *  Description of the Method
				 *
				 * @param  readAhreadLimit Description of Parameter
				 * @exception  IOException Description of Exception
				 */
				public void mark(int readAhreadLimit) throws IOException
				{
					throw new IOException("mark not supported");
				}
				/**
				 *  Description of the Method
				 *
				 * @return  Description of the Returned Value
				 */
				public boolean markSupported()
				{
					return false;
				}
				/**
				 *  Description of the Method
				 *
				 * @return  Description of the Returned Value
				 * @exception  IOException Description of Exception
				 */
				public int read() throws IOException
				{
					return CachedRandomAccessFile.this.readChar();
				}
				/**
				 *  Description of the Method
				 *
				 * @param  buf Description of Parameter
				 * @return  Description of the Returned Value
				 * @exception  IOException Description of Exception
				 */
				public int read(char[] buf) throws IOException
				{
					return read(buf, 0, buf.length);
				}
				/**
				 *  Description of the Method
				 *
				 * @param  buf Description of Parameter
				 * @param  pos Description of Parameter
				 * @param  len Description of Parameter
				 * @return  Description of the Returned Value
				 * @exception  IOException Description of Exception
				 */
				public int read(char[] buf, int pos, int len) throws IOException
				{
					for(int i = 0; i < len; i++)
					{
						buf[pos + i] = readChar();
					}
					return len;
				}
				/**
				 *  Description of the Method
				 *
				 * @return  Description of the Returned Value
				 * @exception  IOException Description of Exception
				 */
				public boolean ready() throws IOException
				{
					return (currBuf.pos < currBuf.dataLen) ||
							(length() < currBuf.filePos + currBuf.pos);
				}
				/**
				 *  Description of the Method
				 *
				 * @param  n Description of Parameter
				 * @return  Description of the Returned Value
				 * @exception  IOException Description of Exception
				 */
				public long skip(long n) throws IOException
				{
					skipBytes(n);
					return n;
				}
			};
	}

	/**
	 *  Gets the Writer attribute of the CachedRandomAccessFile object
	 *
	 * @return  The Writer value
	 */
	public Writer getWriter()
	{
		return
			new Writer()
			{
				/**
				 *  Description of the Method
				 *
				 * @exception  IOException Description of Exception
				 */
				public void close() throws IOException
				{
					CachedRandomAccessFile.this.close();
				}
				/**
				 *  Description of the Method
				 *
				 * @exception  IOException Description of Exception
				 */
				public void flush() throws IOException
				{
					CachedRandomAccessFile.this.flush();
				}
				/**
				 *  Description of the Method
				 *
				 * @param  ch Description of Parameter
				 * @exception  IOException Description of Exception
				 */
				public void write(int ch) throws IOException
				{
					writeChar(ch);
				}
				/**
				 *  Description of the Method
				 *
				 * @param  ch Description of Parameter
				 * @exception  IOException Description of Exception
				 */
				public void write(char[] ch) throws IOException
				{
					write(ch, 0, ch.length);
				}
				/**
				 *  Description of the Method
				 *
				 * @param  ch Description of Parameter
				 * @param  pos Description of Parameter
				 * @param  len Description of Parameter
				 * @exception  IOException Description of Exception
				 */
				public void write(char[] ch, int pos, int len) throws IOException
				{
					for(int i = 0; i < len; i++)
					{
						writeChar(ch[pos + i]);
					}
				}
				/**
				 *  Description of the Method
				 *
				 * @param  str Description of Parameter
				 * @exception  IOException Description of Exception
				 */
				public void write(String str) throws IOException
				{
					write(str, 0, str.length());
				}
				/**
				 *  Description of the Method
				 *
				 * @param  str Description of Parameter
				 * @param  pos Description of Parameter
				 * @param  len Description of Parameter
				 * @exception  IOException Description of Exception
				 */
				public void write(String str, int pos, int len) throws IOException
				{
					for(int i = 0; i < len; i++)
					{
						writeChar(str.charAt(pos + i));
					}
				}
			};
	}

	/**
	 *  Gets the FilePointer attribute of the CachedRandomAccessFile object
	 *
	 * @return  The FilePointer value
	 */
	public long getFilePointer()
	{
		return filePos;
	}

	/**
	 *  Description of the Method
	 *
	 * @return  Description of the Returned Value
	 */
	public long length()
	{
		return fileLength;
	}

	/**
	 *  Description of the Method
	 *
	 * @return  Description of the Returned Value
	 * @exception  IOException Description of Exception
	 */
	public int read() throws IOException
	{
		//System.out.println("read(): read from pos " + filePos + ", fileLen=" + fileLength + ", filePos=" + filePos);
		if(filePos >= fileLength)
		{
			return -1;
		}
		FileBlock block = getBlock(filePos);
		block.read();
		int b = 0xff & block.bytes[block.getOffset(filePos++)];
		return b;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  b Description of Parameter
	 * @return  Description of the Returned Value
	 * @exception  IOException Description of Exception
	 */
	public int read(byte[] b) throws IOException
	{
		return read(b, 0, b.length);
	}

	/**
	 *  Description of the Method
	 *
	 * @param  b Description of Parameter
	 * @param  pos Description of Parameter
	 * @param  len Description of Parameter
	 * @return  Description of the Returned Value
	 * @exception  IOException Description of Exception
	 */
	public int read(byte[] b, int pos, int len) throws IOException
	{
		int readLen = (int) Math.min(len, length() - filePos);
		if(readLen <= 0)
		{
			return 0;
		}
		int numRead = 0;
		while(numRead < readLen)
		{
			FileBlock block = getBlock(filePos);
			block.read();
			int idx = block.getOffset(filePos);
			int blockReadLen = Math.min(readLen - numRead, block.len - idx);
			if(blockReadLen < 1)
			{
				//System.out.println("read(byte[], pos, len) returning prematurely with " + numRead);
				//System.out.println("Block " + block + ", idx=" + idx + ", readLen=" + readLen + ", len=" + len + ", block.len=" + block.len);
				return numRead;
			}
			////System.out.println("Reading " + blockReadLen + " bytes from index " + idx + " of block " + block);
			System.arraycopy(block.bytes, idx, b, pos + numRead, blockReadLen);
			numRead += blockReadLen;
			filePos += blockReadLen;
		}
		////System.out.println("read(byte[], pos, len) returning with " + readLen);
		return readLen;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  pos Description of Parameter
	 * @exception  IOException Description of Exception
	 */
	public void seek(long pos) throws IOException
	{
		if(pos >= 0)
		{
			filePos = pos;
		}
		//System.out.println("Seek: file position is now " + pos);
	}

	/**
	 *  Description of the Method
	 *
	 * @param  n Description of Parameter
	 * @return  Description of the Returned Value
	 * @exception  IOException Description of Exception
	 */
	public int skipBytes(int n) throws IOException
	{
		return (int) skipBytes((long) n);
	}

	/**
	 *  Description of the Method
	 *
	 * @param  n Description of Parameter
	 * @return  Description of the Returned Value
	 * @exception  IOException Description of Exception
	 */
	public long skipBytes(long n) throws IOException
	{
		long skipNum = 0;
		if(n >= 0)
		{
			skipNum = Math.min(length() - filePos, n);
			filePos += skipNum;
		}
		return skipNum;
	}

	/**
	 *  Description of the Method
	 *
	 * @param  b Description of Parameter
	 * @exception  IOException Description of Exception
	 */
	public void write(byte[] b) throws IOException
	{
		write(b, 0, b.length);
	}

	/**
	 *  Description of the Method
	 *
	 * @param  b Description of Parameter
	 * @param  pos Description of Parameter
	 * @param  len Description of Parameter
	 * @exception  IOException Description of Exception
	 */
	public void write(byte[] b, int pos, int len) throws IOException
	{
		if(filePos > length())
		{
			throw new EOFException();
		}
		int numWritten = 0;
		while(numWritten < len)
		{
			FileBlock block = getBlock(filePos);
			block.read();
			int idx = block.getOffset(filePos);
			int numToWrite = Math.min(len - numWritten, blockSize - idx);
			//System.out.println("Writing " + numToWrite + " bytes to index " + idx + " of block " + block);
			System.arraycopy(b, pos + numWritten, block.bytes, idx, numToWrite);
			filePos += numToWrite;
			numWritten += numToWrite;
			int newIdx = idx + numToWrite;
			if(newIdx > block.len)
			{
				block.len = Math.min(blockSize, newIdx);
			}
			block.modified();
			if(filePos > fileLength)
			{
				fileLength = filePos;
			}
		}
	}

	/**
	 *  Description of the Method
	 *
	 * @param  b Description of Parameter
	 * @exception  IOException Description of Exception
	 */
	public void write(int b) throws IOException
	{
		if(filePos > length())
		{
			throw new EOFException();
		}
		FileBlock block = getBlock(filePos);
		block.read();
		block.bytes[block.getOffset(filePos++)] = (byte) b;
		if(block.getOffset(filePos) > block.len)
		{
			block.len++;
		}
		block.modified();
		//System.out.println("Wrote byte " + (char)b + " into block at filePos " + (filePos-1) + " to block " + block);
		if(filePos > fileLength)
		{
			fileLength = filePos;
		}
	}

	/**
	 *  Read any unloaded blocks into memory.
	 *
	 * @exception  IOException Description of Exception
	 */
	public void readFile() throws IOException
	{
		for(int i = 0; i < length(); i += blockSize)
		{
			FileBlock block = getBlock(i);
			if(!block.isModified())
			{
				block.read();
			}
		}
	}

	/**
	 *  Description of the Method
	 *
	 * @exception  IOException Description of Exception
	 */
	public void flush() throws IOException
	{
		int count = 0;
		while(firstModifiedBlock != null)
		{
			FileBlock curr = firstModifiedBlock;
			boolean alreadyModified = curr.isModified();
			firstModifiedBlock.write();
			// alters modified list
			if(alreadyModified && !curr.isModified())
			{
				count++;
			}
		}
		//System.out.println("flush(): saved " + count + "/" + blockList.size() + " blocks.");
	}

	/**
	 *  Gets the Block attribute of the CachedRandomAccessFile object
	 *
	 * @param  position Description of Parameter
	 * @return  The Block value
	 */
	protected FileBlock getBlock(long position)
	{
		int idx = (int) (position / blockSize);
		FileBlock result = null;
		if(idx < blockList.size())
		{
			result = (FileBlock) blockList.get(idx);
		}
		if(result == null && position <= length())
		{
			result = new FileBlock(blockSize);
			result.filePos = (position / blockSize) * blockSize;
			blockList.add(idx, result);
		}
		return result;
	}
	protected class FileBlock
	{

		/**
		 *  Description of the Field
		 */
		public byte[] bytes;
		/**
		 *  Description of the Field
		 */
		public int len;
		/**
		 *  Description of the Field
		 */
		public long filePos;
		/**
		 *  Description of the Field
		 */
		public FileBlock prevModifiedBlock = null;
		/**
		 *  Description of the Field
		 */
		public FileBlock nextModifiedBlock = null;

		private boolean modified;
		private boolean loaded;
		/**
		 *  Constructor for the FileBlock object
		 *
		 * @param  blockSize Description of Parameter
		 */
		public FileBlock(int blockSize)
		{
			bytes = new byte[blockSize];
		}

		/**
		 *  Gets the Modified attribute of the FileBlock object
		 *
		 * @return  The Modified value
		 */
		public boolean isModified()
		{
			return modified;
		}

		/**
		 *  Gets the Loaded attribute of the FileBlock object
		 *
		 * @return  The Loaded value
		 */
		public boolean isLoaded()
		{
			return loaded;
		}

		/**
		 *  Gets the Offset attribute of the FileBlock object
		 *
		 * @param  filePos Description of Parameter
		 * @return  The Offset value
		 */
		public int getOffset(long filePos)
		{
			return (int) (filePos - this.filePos);
		}
		/**
		 *  Description of the Method
		 *
		 * @return  Description of the Returned Value
		 * @exception  IOException Description of Exception
		 */
		public int read() throws IOException
		{
			if(!loaded)
			{
				delegate.seek(filePos);
				len = delegate.read(bytes);
				if(len > 0)
				{
					//System.out.println("Loaded " + len + " bytes into block " + this);
				}
				else
				{
					len = 0;
				}
				loaded = true;
			}
			return len;
		}
		/**
		 *  Description of the Method
		 *
		 * @exception  IOException Description of Exception
		 */
		public void write() throws IOException
		{
			if(modified)
			{
				delegate.seek(filePos);
				delegate.write(bytes, 0, len);
				modified = false;
			}

			// Remove self from doubly-linked list of modified blocks.
			//
			if(firstModifiedBlock == this)
			{
				firstModifiedBlock = nextModifiedBlock;
			}
			else
			{
				prevModifiedBlock.nextModifiedBlock = nextModifiedBlock;
			}

			if(nextModifiedBlock != null)
			{
				nextModifiedBlock.prevModifiedBlock = prevModifiedBlock;
			}
		}

		/**
		 *  Description of the Method
		 */
		public void modified()
		{
			if(!modified)
			{
				// Add self to doubly-linked list of modified blocks.
				//
				if(firstModifiedBlock != null)
				{
					firstModifiedBlock.prevModifiedBlock = this;
				}
				nextModifiedBlock = firstModifiedBlock;
				firstModifiedBlock = this;
			}
			modified = true;
		}

		/**
		 *  Description of the Method
		 *
		 * @return  Description of the Returned Value
		 */
		public String toString()
		{
			return "FileBlock[" + filePos + ", " + hashCode() + "]";
		}
	}
}


