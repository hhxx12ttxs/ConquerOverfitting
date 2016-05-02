package dovetaildb.bagindex;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Iterator;

import dovetaildb.bagindex.BlueSteelBagIndex.PostingNode;
import dovetaildb.bytes.AbstractBytes;
import dovetaildb.bytes.ArrayBytes;
import dovetaildb.bytes.Bytes;
import dovetaildb.store.BytesInterface;
import dovetaildb.store.MappedBytesInterface;
import dovetaildb.store.VarPosition;

public class FsBlueSteelBagIndex extends BlueSteelBagIndex {

	private static final long serialVersionUID = -8987250800532074302L;
	
	transient BytesInterface data, header;
	final boolean sync;
	
	public FsBlueSteelBagIndex(boolean sync) {
		this.sync = sync;
	}

	public String toString() {
		return "FsBlueSteelBagIndex("+homeDir+", rev="+topRevNum+", nextdoc="+nextDocId+", sync="+sync+")";
	}
	
	@Override
	public long getCurrentRevNum() {
		return topRevNum;
	}

	@Override
	public String getHomedir() {
		return homeDir;
	}

	private FileChannel openAsChannel(String filename) {
		File file = new File(homeDir + File.separatorChar + filename);
		try {
			RandomAccessFile raf = new RandomAccessFile(file, "rw");  // think about "rwd" too?
			return raf.getChannel();
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
		 
	}
	
	@Override
	public void setHomedir(String homeDir) {
		File homeDirFile = new File(homeDir);
		if (! homeDirFile.exists()) {
			homeDirFile.mkdirs();
		}
		super.setHomedir(homeDir);
		try {
			header = new MappedBytesInterface(openAsChannel("ddbhdr"));
			data = new MappedBytesInterface(openAsChannel("ddbdat"));
			if (header.getSize() == 0) {
				header.appendBytes(new byte[32]);
				writeHeader();
				MemoryTokenTable emptyRoot = 
					new MemoryTokenTable(ArrayBytes.EMPTY_BYTES, 
							new ArrayList<TokenRec>());
				FsTokenTable.write(data, emptyRoot);
				if (sync) data.force();
			}
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		parseHeader();
	}
	
	final long MAGIC_NUMBER = 4016171645340062763L;

	private void parseHeader() {
		long firstWord = header.getLong(0);
		if (firstWord != MAGIC_NUMBER) {
			throw new RuntimeException("Unknown header file in "+homeDir);
		}
		long ver = header.getLong(1);
		if (ver > 1) throw new RuntimeException("File format version is newer than this code supports");
		this.nextDocId = header.getLong(2);
		this.topRevNum = header.getLong(3);
	}
	
	private void writeHeader() {
		header.putLong(0, MAGIC_NUMBER);
		header.putLong(1, 1);
		header.putLong(2, nextDocId);
		header.putLong(3, topRevNum);
		if (sync) header.force();
	}

	static final class FsBytes extends AbstractBytes {
		BytesInterface data;
		long pos;
		int len;
		public FsBytes(BytesInterface data, long pos, int len) {
			this.data = data;
			this.pos = pos;
			this.len = len;
		}
		public int get(int position) {
			return 0xFF & data.getByte(pos+position);
		}
		public int getLength() { return len; }
		public Bytes subBytes(int start, int count) {
			pos += start;
			len = count;
			return this;
		}
		private static final int MY_CMP_PRIORITY = 5;
		@Override
		public int compareToParts(Bytes other, int o1, int o2, int l1, int l2, int otherCmpPriority) {
			if (otherCmpPriority > MY_CMP_PRIORITY) {
				return -other.compareToParts(this, o2, o1, l2, l1, MY_CMP_PRIORITY);
			} else {
				byte[] bytes;
				if (other instanceof ArrayBytes) {
					bytes = ((ArrayBytes)other).getBackingBytes();
				} else {
					bytes = other.getBytes();
				}
				int minlen = Math.min(l1, l2);
				int diff = data.cmp(pos+o1, minlen, bytes, o2);
				if (diff != 0) return diff;
				return l1 - l2;
			}
		}
		@Override
		public Bytes copy() {
			return new FsBytes(data, pos, len);
		}
		@Override
		public Bytes copyInto(Bytes other) {
			if (other instanceof FsBytes) {
				if (other == this) return this;
				FsBytes oBytes = (FsBytes)other;
				oBytes.data = data;
				oBytes.len = len;
				oBytes.pos = pos;
				return other;
			} else {
				return new ArrayBytes(this.getBytes());
			}
		}
		public void reinit(long position, int numBytes) {
			this.pos = position;
			this.len = numBytes;
		}
		public String toString() {
			return "FsBytes("+flatten()+"@"+pos+")";
		}
	}

	
	
	static final class FsPostingNode extends PostingNode {
		BytesInterface data;
		final VarPosition vp = new VarPosition(0);
		long startPos;
		long pushPos, pushCt;
		private FsPostingNode(FsPostingNode other) {
			docId = other.docId;
			token = other.token.copy();
			vp.position = other.vp.position;
			data = other.data;
			startPos = other.startPos;
			pushPos = other.pushPos;
			pushCt = other.pushCt;
		}
		public FsPostingNode(BytesInterface data, long pos) {
			this.token = new FsBytes(data, 0, 0);
			this.data = data;
			init(pos);
		}
		private void init(long pos) {
			vp.position = pos;
			startPos = pos;
			parse();
		}
		@Override
		public FsPostingNode copy() {
			return new FsPostingNode(this);
		}
		@Override
		public PostingNode copyInto(PostingNode other) {
			if (other == null || !(other instanceof FsPostingNode)) return copy();
			FsPostingNode fsOther = (FsPostingNode)other;
			fsOther.docId = docId;
			fsOther.token = token.copyInto(fsOther.token);
			fsOther.data = data;
			fsOther.pushCt = pushCt;
			fsOther.pushPos = pushPos;
			fsOther.startPos = startPos;
			fsOther.vp.position = vp.position;
			return fsOther;
		}
		private void parse() {
			startPos = vp.position;
			long v1 = data.getVLong(vp);
			docId = v1 >> 1;
			long v2 = data.getVLong(vp);
			int numBytes = (int)v2;
//			System.out.println("P "+vp.position+":"+numBytes+"@ "+data);
			((FsBytes)token).reinit(vp.position, numBytes);
			//token = new FsBytes(data, vp.position, numBytes);
			vp.position += numBytes;
			if ( (v1 & 1) == 1) {
				pushPos = data.getVLong(vp);
				pushCt = data.getVLong(vp);
			} else {
				pushPos = 0;
				pushCt = 0;
			}
		}
		public static PostingNode writeDependentPushes(BytesInterface data, PostingNode node, long ct) {
			if (node instanceof FsPostingNode) return node;
			SegmentPush push = node.getPush();
			if (push == null) return node;
			push = writePush(data, push, ct - 1);
			((MemoryPostingNode)node).setPush(push);
			return node;
		}
		public static FsPostingNode write(BytesInterface data, PostingNode node) {
			if (node instanceof FsPostingNode) return (FsPostingNode)node;
			SegmentPush push = node.getPush();
			long v1 = node.getDocId() << 1;
			if (push != null) {
				v1 |= 1;
			}
			long startPos = data.getSize();
			data.appendVLong(v1);
			Bytes token = node.getToken();
			data.appendVLong(token.getLength());
//			System.out.println("W "+data.getSize()+":"+token.getLength()+" ["+token+"] @ "+data);
			token.appendTo(data);
			if (push != null) {
				data.appendVLong(((FsPostingNode)push.leadNode).startPos);
				data.appendVLong(push.getCount());
			}
			return new FsPostingNode(data, startPos);
		}
		@Override
		public long getCount() {
			return pushCt+1;
		}
		public PostingNode destructiveDown() {
			init(this.pushPos);
			return this;
		}
		public long getPushCount() {
			return pushCt;
		}
		@Override
		public SegmentPush getPush() {
			if (pushPos == 0)  return null;
			FsPostingNode start = new FsPostingNode(data, pushPos);
			return new SegmentPush(start, pushCt);
		}
		@Override
		public PostingNode copyPushLeadInto(PostingNode other) {
			if (other == null || ! (other instanceof FsPostingNode)) {
				return new FsPostingNode(data, pushPos);
			} else {
				FsPostingNode fsOther = (FsPostingNode)other;
				fsOther.init(pushPos);
				return fsOther;
			}
		}
		@Override
		public String toString() {
			return "FsPostingNode @"+startPos+" dId:"+docId+" tok:"+token+" psh@"+pushPos;
		}
		@Override
		public FsPostingNode destructiveNext() {
			parse();
			return this;
		}
	}
	
	public static SegmentPush writePush(BytesInterface data, SegmentPush push, long ct) {
		if (push.leadNode instanceof FsPostingNode) return push;
		ArrayList<MemoryPostingNode> depWrittenNodes = new ArrayList<MemoryPostingNode>();

		for(Iterator<PostingNode> i = push.iterator(); i.hasNext();) {
			MemoryPostingNode node = (MemoryPostingNode)i.next().copy();
			node = (MemoryPostingNode)FsPostingNode.writeDependentPushes(data, node, node.getCount());
			depWrittenNodes.add(node);
		}
		push = new SegmentPush(depWrittenNodes, push.getCount());
		
		FsPostingNode leadNode = null;
		for(Iterator<PostingNode> i = push.iterator(); i.hasNext();) {
			PostingNode node = i.next();
			FsPostingNode written = FsPostingNode.write(data, node);
			if  (leadNode == null) {
				leadNode = written;
			}
		}
		return new SegmentPush(leadNode, ct);
	}
	
	static class FsTokenRec extends TokenRec {
		public FsTokenRec(BytesInterface data, byte token, long subTablePos, long pushPos, long pushCt) {
			super(token, 
					(subTablePos == 0) ? null : new FsTokenTable(data, subTablePos),
					new SegmentPush(new FsPostingNode(data, pushPos), pushCt));
		}
		public FsTokenTable getFsTokenTable() {
			return (FsTokenTable)tokenTable;
		}
		/***
		 * Does not actually write the TokenRec; just writes the summary
		 * push as well as all pushes under sub token tables (if any)
		 */
		public static FsTokenRec write(BytesInterface data, TokenRec rec) {
			SegmentPush push = rec.getSegmentPush();
			SegmentPush writtenPush = writePush(data, push, push.getCount());
			TokenTable tbl = rec.getTokenTable();
			FsTokenTable newTbl = null;
			if (tbl != null) {
				newTbl = FsTokenTable.write(data, tbl);
			}
			long pushStart = ((FsPostingNode)writtenPush.leadNode).startPos;
			return new FsTokenRec(data, rec.token, 
					(newTbl == null) ? 0 : newTbl.tableHeadPtr, 
					pushStart, writtenPush.getCount());
		}
	}
	
	static class FsTokenRecIterator implements Iterator<TokenRec> {
		private BytesInterface data;
		private final VarPosition itrPtr;
		private int numRecs;
		FsTokenRecIterator(BytesInterface data, long ptr) {
			this.data = data;
			this.itrPtr = new VarPosition(ptr);
			this.numRecs = (int)data.getVLong(itrPtr);
		}
		public boolean hasNext() {
			return numRecs > 0;
		}
		public TokenRec next() {
			numRecs--;
			byte ch = data.getByte(itrPtr.position++);
			long subTblPos = data.getVLong(itrPtr);
			long pushPos = data.getVLong(itrPtr);
			long pushCt = data.getVLong(itrPtr);
			return new FsTokenRec(data, ch, subTblPos, pushPos, pushCt);
		}
		public void remove() {
			throw new RuntimeException();
		}
	}
	
	static class FsTokenTable extends TokenTable {
		final long tableHeadPtr, recHeadPtr;
		final BytesInterface data;
		FsBytes fixedPrefix;
		FsTokenTable(BytesInterface data, long tableHeadPtr) {
			this.data = data;
			this.tableHeadPtr = tableHeadPtr;
			VarPosition vp = new VarPosition(tableHeadPtr);
			int prefixLength = (int)data.getVLong(vp);
			fixedPrefix = new FsBytes(data, vp.position, prefixLength);
			recHeadPtr = vp.position + prefixLength;
		}
		@Override
		public Bytes getFixedPrefix() { return fixedPrefix; }
		@Override
		public Iterator<TokenRec> getTokenRecs() {
			return new FsTokenRecIterator(data, recHeadPtr);
		}
		public static FsTokenTable write(BytesInterface data, TokenTable table) {
			if (table instanceof FsTokenTable) return (FsTokenTable)table;
			ArrayList<FsTokenRec> buffer = new ArrayList<FsTokenRec>();
			for(Iterator<TokenRec> i = table.getTokenRecs(); i.hasNext();) {
				TokenRec rec = i.next();
				buffer.add(FsTokenRec.write(data, rec));
			}
			long tblStart = data.getSize();
			Bytes prefix = table.getFixedPrefix();
			data.appendVLong(prefix.getLength());
			prefix.appendTo(data);
			int numRecs = buffer.size();
			data.appendVLong(numRecs);
			for(FsTokenRec rec : buffer) {
				data.appendByte(rec.getToken());
				FsTokenTable subTable = rec.getFsTokenTable();
				data.appendVLong((subTable==null) ? 0 : subTable.tableHeadPtr);
				SegmentPush push = rec.getSegmentPush();
				data.appendVLong(((FsPostingNode)push.leadNode).startPos);
				data.appendVLong(push.getCount());
			}
			return new FsTokenTable(data, tblStart);
		}
	}
	
	@Override
	protected TokenTable getRootTokenTable(long revNum) {
		return new FsTokenTable(data, revNum);
	}

	@Override
	protected void setNewTokenTable(TokenTable newTokenTable) {
		long newHead = FsTokenTable.write(data, newTokenTable).tableHeadPtr;
		if (sync) data.force();
		topRevNum = newHead;
		writeHeader();
	}

	private class RebuiltTokenTable {
		RebuiltTokenTable[] subTables;
		ArrayList<PostingNode> summaryBuffer;
		
		SegmentPush writeAsPush() {
			// TODO : this should be made into a list of node lists
			// where each level is one tier higher than the previous
			
			// fortunately, it isn't called right at the moment (I believe)
			// so throwing an exception:

			throw new RuntimeException("Not Implemented");
			/*
			ArrayList<PostingNode> buffer = summaryBuffer;
			int len = buffer.size();
			long ct=0;
			for(int i=0; i<len; i++) {
				PostingNode node = buffer.get(i);
				ct += node.getCount();
				FsPostingNode fsNode = FsPostingNode.write(data, node);
				buffer.set(i, fsNode);
			}
			SegmentPush push = new SegmentPush(buffer.get(0), ct);
			return push;
			*/
		}
		void add(long docId, Bytes term, int termIdx) {
			if (subTables != null && termIdx < term.getLength()) {
				int b = term.get(termIdx);
				subTables[b].add(docId, term, termIdx + 1);
			}
			if (summaryBuffer != null) {
				if (summaryBuffer.size() < targetPostingListLength) {
					summaryBuffer.add(new MemoryPostingNode(docId, term.subBytes(termIdx)));
				} else {
					SegmentPush push = writeAsPush();
					MemoryPostingNode summaryNode = new MemoryPostingNode(push, docId, term);
					summaryBuffer.clear();
					summaryBuffer.add(summaryNode);
				}
			}
		}
		FsTokenTable writeTokenTables() {
			ArrayList<TokenRec> recs = new ArrayList<TokenRec>();
			for(int i=0; i < subTables.length; i++) {
				RebuiltTokenTable subRebuilt = subTables[i];
				FsTokenTable subWritten = null;
				if (subRebuilt.subTables != null) {
					subWritten = subRebuilt.writeTokenTables();
				}
				SegmentPush push = subRebuilt.writeAsPush();
				recs.add(new TokenRec((byte)i, subWritten, push));
			}
			return FsTokenTable.write(data, new MemoryTokenTable(null, recs));
		}
	}
	
	@Override
	public long rebuild(Iterator<EditRec> edits) {
		RebuiltTokenTable rebuiltTable = new RebuiltTokenTable(); 
		while(edits.hasNext()) {
			EditRec edit = edits.next();
			rebuiltTable.add(edit.getDocId(), edit.getTerm(), 0);
		}
		setNewTokenTable(rebuiltTable.writeTokenTables());
		return topRevNum;
	}
	
	@Override
	public void close() {
		data = header = null; // needed?
	}

	public boolean isSynced() {
		return sync;
	}
};


