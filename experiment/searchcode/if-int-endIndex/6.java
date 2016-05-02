
public class SuffixTree 
{
	protected ExplicitNode root;
	protected Adder adder;
	
	public SuffixTree() {
		this.root = new ExplicitNode("");
		//this.adder = new NaiiveAdder();
		//this.adder = new UkkonenAdder();
		this.adder = new MyAdder();
	}
	
	public SuffixTree(String string) {
		this();
		add(string);
	}
	
	SuffixTree(Node... children) {
		root = new ExplicitNode("", children);
	}
	
	public void add(String stringToAdd) {

		if (!root.toString().equals(" []")) {
			// TODO: Multiple strings?
			throw new RuntimeException("Multiple strings not supported");
		}
		root.suffix.source = stringToAdd;
		adder.add(stringToAdd);
	}
	
	public interface Adder {
		public void add(String stringToAdd);
	}
	
	public class NaiiveAdder implements Adder {
	
		ActivePoint<Node> activePoint = new ActivePoint<Node>();
		int remainder;
		
		@Override
		public void add(String suffix) {
			activePoint.node = root;
			activePoint.edge = SubString.nonGlobal(root.suffix.source, 0, 0);
			remainder = 1;
			
			for (SubString nextCharacter = SubString.nonGlobal(suffix, 0, 1); 
					!nextCharacter.isEmpty(); 
					nextCharacter.extendByOneCharacter()) {
				
				add(nextCharacter);
			}
		}

		/**
	    Update( new_suffix )
	    {
	      current_suffix = active_point
	      test_char = last_char in new_suffix
	      done = false;
	      while ( !done ) {
	        if current_suffix ends at an explicit node {
	          if the node has no descendant edge starting with test_char
	            create new leaf edge starting at the explicit node
	          else
	            done = true;
	        } else {
	          if the implicit node's next char isn't test_char {
	            split the edge at the implicit node
	            create new leaf edge starting at the split in the edge
	          } else
	            done = true;
	        }
	        if current_suffix is the empty string
	          done = true;
	        else
	           current_suffix = next_smaller_suffix( current_suffix )
	      }
	      active_point = current_suffix
	    }
	
		 * @param suffix
		 */
		private void add(SubString suffixChar) {
			
			root.append(suffixChar.firstCharacter());
			
			activePoint.edge.append(suffixChar.firstCharacter());
			remainder++;
			
			while ((activePoint.edge.length() > 0)
					&& !contains(activePoint.edge.toString())) {
				
				ExplicitNode nodeToAddTo = root;	// TODO: Active point?
				
				Node nodeToSplit = root.child(activePoint.edge.firstCharacter());
				if (nodeToSplit != null) {
					SubString splitPoint = stringExcludingLastCharacter(activePoint.edge);
					ExplicitNode splitParent = nodeToSplit.split(splitPoint);
					nodeToAddTo.addNode(splitParent);
					nodeToAddTo = splitParent;
				}
				
				nodeToAddTo.addNode(new LeafNode(suffixChar));
				deleteFirstCharacters(activePoint.edge, 1);
				remainder--;
			}
			
			if ((remainder == 1) && (activePoint.node == root)) {
				root.addNode(new LeafNode(suffixChar));
			}
		}
	}
	
	public class UkkonenAdder implements Adder {

		ActivePoint<Node> activePoint = new ActivePoint<Node>();
		
		@Override
		public void add(String stringToAdd) {
			activePoint.node = root;
			activePoint.edge = SubString.nonGlobal(stringToAdd, 0, 0);

			LeafNode newNode = new LeafNode(SubString.nonGlobal(stringToAdd, 0, 1)); 
			root.addNode(newNode);
			newNode.suffixLink = root;
			
			for (SubString nextCharacter = SubString.nonGlobal(stringToAdd, 0, 1); 
					!nextCharacter.isEmpty(); 
					nextCharacter.extendByOneCharacter()) {
				
				add(nextCharacter);
			}
		}
		
		/**
		 *  Create Tree(t1);   slink(root) := root
		     (v, α) := (root, ε)    // (v, α) is the start node
		     for i := 2  to  n+1  do 
		          v´ := 0
		          while  there is no arc from  v  with label prefix  αti   do
		                   if  α ≠ ε  then    // divide the arc w = son(v, αη) into two
		                            son(v, α) := v´´;  son(v´´,ti) := v´´´;   son(v´´,η) := w 
		                  else
		                            son(v,ti) := v´´´;   v´´ := v
		                  if  v´ ≠  0  then  slink(v´)  :=  v´´
		                  v´ := v´´;  v := slink(v);  (v, α) := Canonize(v, α)
		          if  v´ ≠  0   then  slink(v´) := v
		          (v, α)  := Canonize(v, αti)   // (v, α) = start node of the next round
		*/
		private void add(SubString charToAdd) {
			// Since we don't have a magic end point
			root.append(charToAdd.firstCharacter());
			
			//TODO: Avoid alloc
			while (!activePoint.node.contains(activePointEdgePlusNextChar(charToAdd))) {
				Node vd = null;
				Node vdd = null;
				if (activePoint.edge.length() > 0) {
					vd = activePoint.node.split(activePoint.edge);
					((ExplicitNode) vd).addNode(new LeafNode(charToAdd));
				}
				else {
					LeafNode newLeaf = new LeafNode(charToAdd);
					((ExplicitNode) activePoint.node).addNode(newLeaf);
					vdd = activePoint.node;
				}
				
				if (vd != null) {
					vd.suffixLink = vdd;
				}
				vd = vdd;
				activePoint.node = activePoint.node.suffixLink;
				canoniseActivePoint();
			}
			activePoint.edge.append(charToAdd.firstCharacter());
			canoniseActivePoint();
		}

		private SubString activePointEdgePlusNextChar(SubString charToAdd) {
			SubString activePointEdgePlusNextChar = SubString.copyNonGlobal(activePoint.edge);
			activePointEdgePlusNextChar.append(charToAdd.firstCharacter());
			return activePointEdgePlusNextChar;
		}

		/**
			Function Canonize(v, α):
				while son(v, α´) ≠ 0   where   α = α´ α´´, | α´| > 0   do
					v := son(v, α´);  α := α´´
				return (v, α)
		 */
		private void canoniseActivePoint() {
			while (activePoint.edge.length() > 0) {
				Node child = activePoint.node.child(activePoint.edge.firstCharacter());
				if (child != null) {
					activePoint.node = child;
					if (!activePoint.edge.startsWith(child.suffix)) {
						throw new RuntimeException("eh?");
					}
					deleteFirstCharacters(activePoint.edge, child.suffix.length());
				}
			}
		}
	}
	
	public class MyAdder implements Adder {
		
		public ActivePoint<ExplicitNode> activePoint = new ActivePoint<ExplicitNode>();
		public SubString.MutableInteger globalEndIndex;

		@Override
		public void add(String stringToAdd) {
			globalEndIndex = new SubString.MutableInteger(Math.min(stringToAdd.length(), 1));
			activePoint.node = root;
			activePoint.edge = SubString.nonGlobal(stringToAdd, 0, 0);
			
			SubString nextCharacter = SubString.global(stringToAdd, 0, globalEndIndex);
			while (!nextCharacter.isEmpty()) {
				addChar(nextCharacter);
				nextCharacter.consumeFirstCharacter();
				globalEndIndex.value = Math.min(globalEndIndex.value+1, stringToAdd.length());
			}
		}

		private void addChar(SubString charToAdd) {
			Node childNodeForChar = activePoint.node.child(charToAdd.firstCharacter());
			if (childNodeForChar == null) {
				// New node
				Node newLeaf = new LeafNode(charToAdd);
				root.addNode(newLeaf);
				addToEndOfSuffixLinkChain(newLeaf);
			}

			//TODO: Is this all reachable if childNodeForChar doesn't exist?
			if (activePoint.edge.nextCharacter() == charToAdd.firstCharacter()) {
				// Repetition
				if (charToAdd.firstCharacter() == activePoint.edge.firstCharacter()) {
					// Start of a new repetition. Reset activePoint to first character of active edge
					activePoint.edge.startIndex = activePoint.edgeNode().suffix.startIndex;
					activePoint.edge.endIndex.value = activePoint.edge.startIndex + 1;
				}
				else {
					// Continue existing repetition
					activePoint.edge.extendByOneCharacter();
					// Detect crossing edge
					if (activePoint.edge.length() > activePoint.edgeNode().suffix.length()) {
						activePoint.node = (ExplicitNode) activePoint.edgeNode();
						activePoint.edge.startIndex = activePoint.node.child(activePoint.edge.lastCharacter()).suffix.startIndex;
						activePoint.edge.endIndex.value = activePoint.edge.startIndex + 1;
					}
				}
			}
			else {
				// Not repeating, need a new leaf
				followSuffixLinksAndSplit(activePoint.node.child(activePoint.edge.firstCharacter()), charToAdd);
				
				//TODO: Move into followSuffixLinks?
				if (!activePoint.edge.isEmpty()) {
					followSuffixLinksAndSplit(root.child(activePoint.edge.firstCharacter()), charToAdd);
				}
				activePoint.node = root;
				activePoint.edge.startIndex = charToAdd.endIndex.value;
				activePoint.edge.endIndex.value = charToAdd.endIndex.value;
			}
		}
		
		private void addToEndOfSuffixLinkChain(Node nodeToAdd) {
			Node node = lastNonRootNodeInSuffixLinkChain();
			if (node != null) {
				node.suffixLink = nodeToAdd;
			}
			nodeToAdd.suffixLink = root;
		}

		private Node lastNonRootNodeInSuffixLinkChain() {
			Node node = activePoint.edgeNode();
			while ((node != null) && (node.suffixLink != root)) {
				node = node.suffixLink;
			}
			return node;
		}

		private void followSuffixLinksAndSplit(Node nodeToSplit, SubString charToAdd) {
			Node lastSplitChild = null;
			
			while (!activePoint.edge.isEmpty()) {

				Node splitChild = nodeToSplit;
				if (lastSplitChild != null) {
					lastSplitChild.suffixLink = splitChild;
				}

				ExplicitNode newParent = nodeToSplit.split(activePoint.edge.length());
				newParent.addNode(new LeafNode(charToAdd));
				
				nodeToSplit = newParent.suffixLink;
				activePoint.edge.consumeFirstCharacter();
				
				lastSplitChild = splitChild;
			}
		}
		
	}
	
	private SubString stringExcludingLastCharacter(SubString other) {
		SubString result = SubString.copyNonGlobal(other);
		result.endIndex.value--;
		return result;
	}
	
	public static void deleteFirstCharacters(SubString suffix, int numberOfCharacters) {
		suffix.startIndex += numberOfCharacters;
	}

	private static boolean startsWith(SubString suffix, SubString subString) {
		return suffix.source.regionMatches(
				suffix.startIndex, subString.source,
				subString.startIndex, subString.length());
	}
	
	public boolean contains(String string) {
		return root.contains(SubString.fromString(string));
	}
	
	
	
	public class ActivePoint<NodeType extends Node> {
		private NodeType node;
		private SubString edge;
		
		public Node edgeNode() {
			return node.child(edge.firstCharacter());
		}
	}
	
	public char[] suffixAtPosition(int queryIndex) {
		throw new IndexOutOfBoundsException("Not yet implemented");
	}

	//-------------------------------------------------------------------------
	public static class SubString extends Object {
		public String source;
		public int startIndex;
		public MutableInteger endIndex; // Integer, so can be global
		
		public static final int USE_GLOBAL_END_INDEX = -1;
		
		public static SubString global(String source, int startIndex, MutableInteger globalEndIndex) {
			return new SubString(source, startIndex, globalEndIndex);
		}
		
		public static SubString nonGlobal(String source, int startIndex, int endIndex) {
			return new SubString(source, startIndex, new MutableInteger(endIndex));
		}
		
		public static SubString copyNonGlobal(SubString other) {
			return nonGlobal(other.source, other.startIndex, other.endIndex.value);
		}
		
		public static SubString copy(SubString other) {
			return global(other.source, other.startIndex, other.endIndex);
		}
		
		public static SubString fromString(String string) {
			return nonGlobal(string, 0, string.length());
		}

		private SubString(String source, int startIndex, MutableInteger endIndex) {
			this.source = source;
			this.startIndex = startIndex;
			this.endIndex = endIndex;
		}
				
		public void setFrom(SubString other) {
			this.source = other.source;
			this.startIndex = other.startIndex;
			this.endIndex = other.endIndex;
		}
		
		public boolean isEmpty() {
			return startIndex >= endIndex.value; 
		}

		public int length() {
			return endIndex.value - startIndex;
		}
		
		public char firstCharacter() {
			return source.charAt(startIndex);
		}
		
		public char lastCharacter() {
			return source.charAt(endIndex.value-1);
		}
		
		public char nextCharacter() {
			return source.charAt(endIndex.value);
		}
		
		public char charAt(int index) {
			throwExceptionIfIndexInvalid(index);
			return source.charAt(startIndex + index);
		}

		private void throwExceptionIfIndexInvalid(int index) {
			if ((index < 0) || (index >= length())) {
				throw new RuntimeException("Invalid index: " + index + ", length is " + length());
			}
		}

		public void advanceOneCharacter() {
			startIndex++;
			extendByOneCharacter();
		}

		private void extendByOneCharacter() {
			endIndex.value = Math.min(endIndex.value+1, source.length());
		}
		
		public void consumeFirstCharacter() {
			startIndex++;
		}

		public void append(char character) {
			if (nextCharacter() == character) {
				endIndex.value++;
			}
			else {
				throw new RuntimeException("Appending " + character + " but expected " + nextCharacter());
			}
		}
		
		public boolean startsWith(SubString other) {
			return source.regionMatches(startIndex, other.source, other.startIndex, other.length());
		}

		@Override
		public String toString() {
			return source.substring(startIndex, endIndex.value);
		}
		
		protected static class MutableInteger {
			public int value;
			
			public MutableInteger(int value) {
				this.value = value;
			}
			
			@Override
			public String toString() {
				return Integer.toString(value);
			}
		}
	}
	
	//-------------------------------------------------------------------------
	public static abstract class Node {

		protected SubString suffix;
		//TODO: ExplicitNode?
		protected Node suffixLink;
		//TODO: Is this necessary?
		protected ExplicitNode parent;

		public Node(SubString suffix) {
			this.suffix = SubString.copy(suffix);
		}
		
		public Node(String suffix) {
			this.suffix = SubString.nonGlobal(suffix, 0, suffix.length());
		}

		public abstract Node child(char index);
		
		//TODO: No point passing the character?
		public abstract void append(char character);
		
		public boolean contains(SubString searchString) {
			
			// TODO: Avoid recursion
			boolean found = false;
			Node child = child(searchString.firstCharacter());
			
			if (child != null) {
				if (searchString.length() <= child.suffix.length()) {
					found = startsWith(child.suffix, searchString);
				}
				else {
					if (startsWith(searchString, child.suffix)) {
						searchString.startIndex += child.suffix.length();
						found = child.contains(searchString);
						searchString.startIndex -= child.suffix.length();
					}
				}
			}

			return found;
		}
		
		public ExplicitNode split(SubString splitPoint) {
			ExplicitNode newParent = new ExplicitNode(SubString.copyNonGlobal(splitPoint));
			deleteFirstCharacters(suffix, splitPoint.length());
			newParent.addNode(this);
			
			newParent.suffixLink = this.suffixLink;
			this.suffixLink = null;

			return newParent;				
		}
		
		public ExplicitNode split(int splitIndexInSuffix) {
			ExplicitNode newParent = new ExplicitNode(SubString.nonGlobal(suffix.source, suffix.startIndex, suffix.startIndex + splitIndexInSuffix));
			deleteFirstCharacters(suffix, splitIndexInSuffix);
			
			this.parent.addNode(newParent);
			newParent.addNode(this);
			
			newParent.suffixLink = this.suffixLink;
			this.suffixLink = null;

			return newParent;
		}
			
		@Override
		public String toString() {
			StringBuffer buffer = new StringBuffer();
			describeTo(buffer);
			return buffer.toString();
		}
		
		protected void describeTo(StringBuffer buffer) {
			buffer.append(suffix);
		}
	}
	
	public static class ExplicitNode extends Node {

		protected Node[] children = new Node[26];

		private ExplicitNode(String suffix, Node... children) {
			super(suffix);
			
			for (Node child : children) {
				addNode(child);
			}
		}

		public ExplicitNode(SubString suffix) {
			super(suffix);
		}

		@Override
		public Node child(char index) {
			return children[index - 'a'];
		}
		
		public void addNode(Node childNode) {
			children[childNode.suffix.firstCharacter() - 'a'] = childNode;
			childNode.parent = this;
		}
		
		@Override
		public void append(char character) {
			for (Node child : children) {
				if (child != null) {
					child.append(character);
				}
			}
		}

		@Override
		public ExplicitNode split(SubString splitPoint) {
			
			if (splitPoint.length() < suffix.length()) {
				return super.split(splitPoint);
			}
			else {
				// TODO: Avoid alloc?
				SubString childSplitPoint = SubString.nonGlobal(splitPoint.source, 
						splitPoint.startIndex + suffix.length(), 
						splitPoint.endIndex.value);
				
				Node childToSplit = child(childSplitPoint.firstCharacter());
				ExplicitNode newChild = childToSplit.split(childSplitPoint);
				addNode(newChild);
				return newChild;
			}
		}
		
		@Override
		public ExplicitNode split(int splitIndexInSuffix) {
			
			if (splitIndexInSuffix < suffix.length()) {
				return super.split(splitIndexInSuffix);
			}
			else {
				Node childToSplit = child(suffix.charAt(splitIndexInSuffix));
				ExplicitNode newChild = childToSplit.split(splitIndexInSuffix - suffix.length());
				addNode(newChild);
				return newChild;
			}
		}

		@Override
		protected void describeTo(StringBuffer buffer) {
			super.describeTo(buffer);
			
			buffer.append(" [");
			boolean isFirstChild = true;
			for (Node child : children) {
				if (child != null) {
					if (!isFirstChild) {
						buffer.append(", ");
					}
					child.describeTo(buffer);
					isFirstChild = false;
				}
			}
			buffer.append("]");
		}
	}
	
	//TODO: Do leaf nodes even need a suffix or is its index in the parent enough?
	public static class LeafNode extends Node {

		public LeafNode(String suffix) {
			super(suffix);
		}

		public LeafNode(SubString suffix) {
			super(suffix);
		}

		@Override
		public Node child(char index) {
			return null;
		}

		@Override
		public void append(char character) {
			suffix.append(character);
		}
	}

	//-------------------------------------------------------------------------
	public static class Builder {
		public static SuffixTree tree(Node... children) {
			return new SuffixTree(children);
		}
		
		public static ExplicitNode explicit(String suffix, Node... children) {
			return new ExplicitNode(suffix, children);
		}
		
		public static LeafNode leaf(String suffix) {
			return new LeafNode(suffix);
		}
	}
	
	@Override
	public String toString() {
		StringBuffer buffer = new StringBuffer("SuffixTree: ");
		root.describeTo(buffer);
		return buffer.toString();
	}
}

