
package com.gappcms.search;

import com.gappcms.model.v2.PMF;
import com.google.appengine.api.datastore.*;
import javax.jdo.annotations.PrimaryKey;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.NotPersistent;
import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.Query;

import com.google.appengine.api.datastore.*;

import java.util.*;
import org.apache.lucene.store.RAMDirectory;
import java.io.*;

import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.store.SingleInstanceLockFactory;
import org.apache.lucene.index.*;
import org.apache.lucene.util.Version;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.snowball.*;

import javax.jdo.PersistenceManager;

import java.util.concurrent.locks.*;
import javax.sql.rowset.serial.SerialArray;

@PersistenceCapable
public class SerializableLuceneDirectory
{
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key key;

	@Persistent(defaultFetchGroup="true")
	private LinkedList<Key> blocks = new LinkedList<Key>();

	@Persistent(defaultFetchGroup="true")
	int size = 0;

	public Key getKey() {
		return this.key;
	}

	public SerializableLuceneDirectory(RAMDirectory directory)
			throws IOException
	{
		// RAMDirectories are (mostly) serializable :D
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		ObjectOutputStream objOut = new ObjectOutputStream(out);
		objOut.writeObject(directory);

		// write all of the files in this direectory
		String[] files = directory.listAll();
		for (String fileName : files) {
			RAMDirectoryFileContainer contain = new RAMDirectoryFileContainer();
			contain.name = fileName;
			IndexInput ramIn = directory.openInput(fileName);
			contain.contents = new byte[(int)ramIn.length()];
			ramIn.readBytes(contain.contents, 0, (int)ramIn.length());
			objOut.writeObject(contain);
			ramIn.close();
		}

		objOut.flush();
		out.flush();
		out.close();
		byte[] contents = out.toByteArray();

		PersistenceManager pm = PMF.get();

		LinkedList<Key> newBlocks = new LinkedList<Key>();
		for (int x = 0; x <= contents.length / SerializableLuceneDirectoryBlock.BLOCK_SIZE; x++) {
			int startingPos = SerializableLuceneDirectoryBlock.BLOCK_SIZE * x;
			int endPos = SerializableLuceneDirectoryBlock.BLOCK_SIZE * (x+1);
			if (endPos > contents.length) {
				endPos = contents.length -1;
			}
			int length = endPos - startingPos+1;
			byte[] block = new byte[length];
			System.arraycopy(contents, startingPos, block, 0, length);
			SerializableLuceneDirectoryBlock serializableBlock = new SerializableLuceneDirectoryBlock(this.key);
			serializableBlock.setBlock(block);
			pm.makePersistent(serializableBlock);
			Key blockKey = serializableBlock.getKey();
			newBlocks.add(blockKey);
		}
		this.size = contents.length;
		this.blocks = newBlocks;
	}

	public RAMDirectory load()
			throws IOException, ClassNotFoundException
	{
		// create a new massive array from the blocks and read into it
		byte[] contents = new byte[this.size];
		int destPos = 0;
		PersistenceManager pm = PMF.get();
		for (Key blockKey : this.blocks) {
			try {
				SerializableLuceneDirectoryBlock block = pm.getObjectById(SerializableLuceneDirectoryBlock.class, blockKey);
				byte[] tmp = block.getBlock();
				//System.out.println("Copying bytes from "+destPos +" to " +(destPos+tmp.length));
				System.arraycopy(tmp, 0, contents, destPos, tmp.length);
				destPos += tmp.length-1;
			} catch (Exception err) {
				System.out.println("datastore corrupted :/, creating clean store");
				//lock.unlock();
				this.size = 0;
				return  this.load();
			}
		}

		ByteArrayInputStream in = new ByteArrayInputStream(contents);
		// try and get a directory out of it!
		ObjectInputStream objIn = new ObjectInputStream(in);
		RAMDirectory directory = (RAMDirectory)objIn.readObject();

		try {
			RAMDirectoryFileContainer contain = (RAMDirectoryFileContainer)objIn.readObject();
			while (contain != null ) {
				IndexOutput out = directory.createOutput(contain.name);
				out.writeBytes(contain.contents, contain.contents.length);
				out.close();
				contain = (RAMDirectoryFileContainer)objIn.readObject();
			}
		}
		catch (EOFException err) { }
		objIn.close();
		directory.setLockFactory(new SingleInstanceLockFactory());
		return directory;
	}

}


