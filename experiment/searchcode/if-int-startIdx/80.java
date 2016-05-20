package edu.northwestern.websail.datastructure.trie.impl.w2cSQL;

import java.io.Serializable;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.TreeSet;

import edu.northwestern.websail.datastructure.trie.ds.Node;
import edu.northwestern.websail.datastructure.trie.ds.Trie;
import edu.northwestern.websail.datastructure.trie.impl.w2cSQL.candidateComparators.W2CSQLTrieComparatorImpl;
import edu.northwestern.websail.datastructure.trie.impl.w2cSQL.utils.W2CSQLDataAccess;

public class W2CSQLTrie implements Serializable {

	private static final long serialVersionUID = -3975378500456405841L;
	private Trie<Byte, Integer, W2CSQLMentionNode> trie;
	private W2CSQLDataAccess dba;

	public W2CSQLTrie(Connection conn, boolean doInit) throws SQLException {
		this.dba = new W2CSQLDataAccess(conn, doInit);
		trie = new Trie<Byte, Integer, W2CSQLMentionNode>();
	}

	public W2CSQLTrie(Connection conn) throws SQLException {
		this.dba = new W2CSQLDataAccess(conn);
		trie = new Trie<Byte, Integer, W2CSQLMentionNode>();
	}

	public void initializeDB() throws SQLException {
		this.dba.initializeEdgeTable();
		this.dba.initializeMentionTable();
		this.dba.initializeCandidateTables();
		this.dba.initializePostProcessTables();
	}

	public void postProcessDB() throws SQLException {
		this.dba.populateUnionCandidate();
	}

	public void addCandidate(String[] surfaceFormTokens, String surfaceForm,
			Integer conceptId, Double probInternal, Double probInternalNonCase,
			Double probExternal, Double probExternalNonCase,
			Integer denomInternal, Integer denomInternalNonCase,
			Integer denomExternal, Integer denomExternalNonCase,
			Boolean isLastname, Boolean isTitle, Boolean skipUpdateCheck)
			throws SQLException {
		String[] tokens = new String[surfaceFormTokens.length];
		Byte[] nodes = new Byte[surfaceFormTokens.length];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = surfaceFormTokens[i].toLowerCase();
			nodes[i] = 0;
		}
		Integer[] edges = this.dba.mapEdges(tokens, true);
		Integer key = this.dba.mapMention(surfaceForm);
		if (key == null) {
			this.dba.putMention(surfaceForm);
			key = this.dba.mapMention(surfaceForm);
		}
		Node<Byte, Integer, W2CSQLMentionNode> endNode = this.trie.insertNodes(
				nodes, edges);
		if (endNode.getEndpoint() == null) {
			endNode.setEndpoint(new W2CSQLMentionNode());
		}
		endNode.getEndpoint().addMention(key);
		this.dba.putCandidate(key, conceptId, probInternal,
				probInternalNonCase, probExternal, probExternalNonCase,
				denomInternal, denomInternalNonCase, denomExternal,
				denomExternalNonCase, isLastname, isTitle, skipUpdateCheck);
	}

	public void addMentionFromDB(Integer[] edges, Byte[] nodes,
			String surfaceForm) throws SQLException {

		Integer key = this.dba.mapMention(surfaceForm);
		if (key == null) {
			this.dba.putMention(surfaceForm);
			key = this.dba.mapMention(surfaceForm);
		}

		Node<Byte, Integer, W2CSQLMentionNode> endNode = this.trie.insertNodes(
				nodes, edges);
		if (endNode.getEndpoint() == null) {
			endNode.setEndpoint(new W2CSQLMentionNode());
		}
		endNode.getEndpoint().addMention(key);
	}

	public void addMentionFromDB(Integer[] edges, Byte[] nodes, Integer key)
			throws SQLException {

		Node<Byte, Integer, W2CSQLMentionNode> endNode = this.trie.insertNodes(
				nodes, edges);
		if (endNode.getEndpoint() == null) {
			endNode.setEndpoint(new W2CSQLMentionNode());
		}
		endNode.getEndpoint().addMention(key);
	}

	public HashMap<String, TreeSet<W2CSQLCandidate>> getAllCandidates(
			String[] surfaceFormTokens) throws SQLException {
		String[] tokens = new String[surfaceFormTokens.length];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = surfaceFormTokens[i].toLowerCase();
		}
		Integer[] edges = this.dba.mapEdges(tokens, true);
		Node<Byte, Integer, W2CSQLMentionNode> endNode = this.trie
				.getNode(edges);
		if (endNode == null || endNode.getEndpoint() == null)
			return null;
		HashMap<Integer, TreeSet<W2CSQLCandidate>> intMap = this.dba
				.getCandidates(endNode.getEndpoint().getMentionArray());
		HashMap<String, TreeSet<W2CSQLCandidate>> strMap = new HashMap<String, TreeSet<W2CSQLCandidate>>(
				intMap.size());
		Iterator<Entry<Integer, TreeSet<W2CSQLCandidate>>> it = intMap
				.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry<Integer, TreeSet<W2CSQLCandidate>> pairs = (Map.Entry<Integer, TreeSet<W2CSQLCandidate>>) it
					.next();
			String surface = this.dba.mapMention(pairs.getKey());
			strMap.put(surface, pairs.getValue());
		}
		return strMap;
	}

	public TreeSet<W2CSQLCandidate> getCandidateSet(String[] surfaceFormTokens,
			String surfaceForm) throws SQLException {
		String[] tokens = new String[surfaceFormTokens.length];
		for (int i = 0; i < tokens.length; i++) {
			tokens[i] = surfaceFormTokens[i].toLowerCase();
		}
		Integer[] edges = this.dba.mapEdges(tokens, true);
		Node<Byte, Integer, W2CSQLMentionNode> endNode = this.trie
				.getNode(edges);
		if (endNode == null || endNode.getEndpoint() == null)
			return null;
		Integer key = this.dba.mapMention(surfaceForm);
		return this.dba.getCandidates(key);
	}

	public ArrayList<W2CSQLTrieMaximalMatch> getMaximalMatchingCandidates(
			String[] words, W2CSQLTrieComparatorImpl comparator, int limit)
			throws SQLException {

		ArrayList<W2CSQLTrieMaximalMatch> maximalMatches = new ArrayList<W2CSQLTrieMaximalMatch>();

		Node<Byte, Integer, W2CSQLMentionNode> root = trie.getRoot();
		SurfaceMentionNodePair surfaceMention;

		Integer[] wordIds = getEdgeIdsFromEdgeLabels(words);

		for (int i = 0; i < wordIds.length;) {
			if (wordIds[i] == null) {
				i++;
				continue;
			}
			if (root.hasChild(wordIds[i])) {
				surfaceMention = getMaxMatchLengthFromSource(root, wordIds, i);
				if (surfaceMention == null) {
					i++;
					continue;
				}

				W2CSQLTrieMaximalMatch m = new W2CSQLTrieMaximalMatch(
						surfaceMention.getStartIdx(),
						surfaceMention.getEndIdx());

				for (Integer surfaceID : surfaceMention.getMention()
						.getMentionKeys()) {
					TreeSet<W2CSQLCandidate> candidates = new TreeSet<W2CSQLCandidate>(
							comparator);
					candidates.addAll(this.dba.getCandidates(surfaceID));
					if (limit == -1)
						m.addMentionStrCandidatePair(
								this.dba.mapMention(surfaceID), candidates);
					else{
						m.addMentionStrCandidatePair(
								this.dba.mapMention(surfaceID), candidates , limit);
					}
					maximalMatches.add(m);
				}

				i += surfaceMention.getMentionLength();
			} else {
				i++;
			}
		}

		return maximalMatches;

	}

	private static SurfaceMentionNodePair getMaxMatchLengthFromSource(
			Node<Byte, Integer, W2CSQLMentionNode> root, Integer[] wordIds,
			int startIdx) {

		int i = startIdx;
		Node<Byte, Integer, W2CSQLMentionNode> current = root;
		String surface = "";
		Stack<SurfaceMentionNodePair> mentionStack = new Stack<SurfaceMentionNodePair>();
		while (i < wordIds.length && current != null) {
			if (wordIds[i] != null && current.hasChild(wordIds[i])) {
				surface += wordIds[i] + " ";
				current = current.getChild(wordIds[i]);

				if (current.getEndpoint() != null)
					mentionStack.add(new SurfaceMentionNodePair(startIdx,
							i + 1, surface, current.getEndpoint()));

			} else {
				break;
			}
			i++;
		}

		if (!mentionStack.isEmpty())
			return mentionStack.pop();
		else
			return null;
		//
		// if (current.getEndpoint() == null)
		// if (!mentionStack.isEmpty())
		// return mentionStack.pop();
		// else
		// return null;
		// else {
		// return mentionStack.pop();
		// }

	}

	// getters and setters
	public Trie<Byte, Integer, W2CSQLMentionNode> getTrie() {
		return trie;
	}

	public void setTrie(Trie<Byte, Integer, W2CSQLMentionNode> trie) {
		this.trie = trie;
	}

	public Integer[] getEdgeIdsFromEdgeLabels(String[] edgeLabels)
			throws SQLException {
		return this.dba.mapEdges(edgeLabels, false);
	}

	public W2CSQLDataAccess getDba() {
		return dba;
	}

	public void setDba(W2CSQLDataAccess dba) {
		this.dba = dba;
	}

}

class SurfaceMentionNodePair {
	Integer startIdx;
	Integer endIdx;
	String surface;
	W2CSQLMentionNode mention;
	Integer mentionLength;

	public SurfaceMentionNodePair(Integer startIdx, Integer endIdx,
			String surface, W2CSQLMentionNode mention) {
		super();
		this.startIdx = startIdx;
		this.endIdx = endIdx;
		this.surface = surface;
		this.mention = mention;
		this.mentionLength = endIdx - startIdx;
	}

	public Integer getStartIdx() {
		return startIdx;
	}

	public void setStartIdx(Integer startIdx) {
		this.startIdx = startIdx;
	}

	public Integer getEndIdx() {
		return endIdx;
	}

	public void setEndIdx(Integer endIdx) {
		this.endIdx = endIdx;
	}

	public String getSurface() {
		return surface;
	}

	public void setSurface(String surface) {
		this.surface = surface;
	}

	public W2CSQLMentionNode getMention() {
		return mention;
	}

	public void setMention(W2CSQLMentionNode mention) {
		this.mention = mention;
	}

	public Integer getMentionLength() {
		return mentionLength;
	}

	public void setMentionLength(Integer mentionLength) {
		this.mentionLength = mentionLength;
	}

}
