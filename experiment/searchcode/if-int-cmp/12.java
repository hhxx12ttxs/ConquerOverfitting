package dovetaildb.querynode;

import java.util.ArrayList;
import java.util.List;

import dovetaildb.bagindex.Range;
import dovetaildb.bytes.Bytes;

public class OrderedOrQueryNode extends HeapQueryNode {

	private Bytes tempBytes=null;
	
	protected OrderedOrQueryNode(NodeHeap heap) {
		init(heap);
	}
	public OrderedOrQueryNode(List<? extends QueryNode> queries) {
		NodeHeap heap = new NodeHeap(queries.size()+1);
		for(QueryNode node : queries) {
			heap.insert(node);
		}
		init(heap);
	}
	protected boolean adjustHeap() {
		heap.adjustTop();
		return doc() < Long.MAX_VALUE;
	}

	public boolean next() {
		long curDoc = doc();
		do {
			((QueryNode)heap.top()).next();
			heap.adjustTop();
		} while(curDoc == doc());
		return doc() != Long.MAX_VALUE;
	}
	public NextStatus nextTerm() {
		long curDoc = doc();
		if (curDoc == Long.MAX_VALUE) return NextStatus.AT_END;
		if (tempBytes == null) {
			tempBytes = term().copy();
		} else {
			tempBytes = term().copyInto(tempBytes);
		}
		heap.top().nextTerm();
		heap.adjustTop();
		long newDoc = doc();
		if (curDoc == newDoc) {
			if (tempBytes.equals(term())) { // TODO let's optionally support a version that assumes terms are distinct
				return nextTerm();
			} else {
				return NextStatus.NEXT_TERM;
			}
		} else {
			if (newDoc == Long.MAX_VALUE) return NextStatus.AT_END;
			else return NextStatus.NEXT_DOC;
		}
	}
	public void resetTerms() {
		throw new RuntimeException();
	}
	public void checkpoint() {
		throw new RuntimeException();
	}
	
	public void seek(long seekDoc, Bytes seekTerm) {
//		long start = heap.top().doc();
		int cmp = compareTo(seekDoc, seekTerm);
		if (cmp >= 0) {
			restart();
			cmp = compareTo(seekDoc, seekTerm);
		}
		int ctr=0;
		while (cmp < 0) {
			ctr++;
			heap.top().seek(seekDoc, seekTerm);
			heap.adjustTop();
			cmp = compareTo(seekDoc, seekTerm);
		}
		int s = heap.size();
		for (; s>0; s--) {
			if (heap.heap[s].doc() != Long.MAX_VALUE) break;
		}
//		System.out.println("seek " + seekDoc+ " " +ctr +" out of "+ heap.size()+"  "+start+"->"+heap.top().doc()+" effective cap = "+s);
	}

	@Override
	public Bytes findAnyMatching(long seekDoc, Bytes seekTerm) {
		int ctr=0;
		int seekCtr=0;
		while (true) {
			ctr++;
			Bytes returnValue = null;
			QueryNode node = heap.top();
			long nodeDoc = node.doc();
			Bytes nodeTerm = node.term();
			boolean fastForward = node.compareTo(seekDoc, seekTerm) <= 0;
			if (fastForward) {
				seekCtr++;
				node.seek(seekDoc, seekTerm);
			}
			if (nodeDoc == seekDoc && seekTerm.isPrefixOf(nodeTerm)) {
//				System.out.println("   mtch " + seekDoc+ " " + ctr +" out of "+ heap.size()+", "+seekCtr+" seeks");
				returnValue = nodeTerm;
				node.nextTerm();
			} else if (! fastForward) {
//				System.out.println("no mtch " + seekDoc + " " + ctr +" out of "+ heap.size());
				return null;
			}
			heap.adjustTop();
			if (returnValue != null) {
				return returnValue;
			}
		}
		/*
		QueryNode top = heap.top();
		if (top.doc() == docId && prefix.isPrefixOf(top.term())) {
			
		}
		.seek(seekDoc, seekTerm)
		for(int i = heap.size(); i>0; i--) {
			QueryNode node = (QueryNode)heap.heap[i];
			if (node.compareTo(docId, prefix) < 0) {
				Bytes result = node.positionToAnyMatching(docId, prefix, suffix);
				heap.adjustDownFrom(i);
				if (result != null) {
					System.out.println("posMatch at " + i + " of "+heap.size());
					return result;
				}
			}
		}
		System.out.println("posMatch fallback");
		// the slow thing (seek everything and scan)
		return super.positionToAnyMatching(docId, prefix, suffix);
		*/
	}
	
	public static QueryNode make(ArrayList<QueryNode> clauses) {
//		System.out.println("OrderedOrQN size = "+clauses.size()+" : "+clauses.subList(0, Math.min(8, clauses.size())));
		if (clauses.size() < 2) {
			if (clauses.isEmpty()) {
				return null;
			}
			else return clauses.get(0);
		}
		return new OrderedOrQueryNode(clauses);
	}
	public QueryNode specialize(Range range) {
		ArrayList<QueryNode> clauses = heap.specialize(range);
		return (clauses == null) ? this : make(clauses);
	}
	public QueryNode copy() {
		return new OrderedOrQueryNode(heap.deepcopy());
	}
	public String toString() {
		StringBuffer buf = new StringBuffer("OrederedOrQueryNode([\n");
		for(Object o : this.heap.heap) {
			if (o==null) continue;
			QueryNode qn = (QueryNode)o;
			buf.append("  "+qn+"\n");
		}
		buf.append("])\n");
		return buf.toString();
	}
}

