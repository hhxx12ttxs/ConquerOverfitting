/*
<<<<<<< HEAD
 * Copyright (C) 2009 by Eric Lambert <Eric.Lambert@sun.com>
 * Use and distribution licensed under the BSD license.  See
 * the COPYING file in the parent directory for full text.
 */
package org.gearman.worker;

import java.util.HashSet;
import java.util.Set;

import org.gearman.client.GearmanIOEventListener;
import org.gearman.client.GearmanJobResult;
import org.gearman.client.GearmanJobResultImpl;
import org.gearman.common.GearmanPacket;
import org.gearman.common.GearmanPacketImpl;
import org.gearman.common.GearmanPacketMagic;
import org.gearman.common.GearmanPacketType;
import org.gearman.util.ByteUtils;

public abstract class AbstractGearmanFunction implements GearmanFunction {

    protected final String name;
    protected Object data;
    protected byte[] jobHandle;
    protected Set<GearmanIOEventListener> listeners;

    public AbstractGearmanFunction() {
        this(null);
    }

    public AbstractGearmanFunction(String name) {
        listeners = new HashSet<GearmanIOEventListener>();
        jobHandle = new byte[0];
        if (name == null) {
            this.name = this.getClass().getCanonicalName();
        } else {
            this.name = name;
        }
    }

    public String getName() {                                                   
        return name;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public void setJobHandle(byte[] handle) throws IllegalArgumentException {
        if (handle == null) {
            throw new IllegalArgumentException("handle can not be null");
        }
        if (handle.length == 0) {
            throw new IllegalArgumentException("handle can not be empty");
        }
        jobHandle = new byte[handle.length];
        System.arraycopy(handle, 0, jobHandle, 0, handle.length);
    }

    public byte[] getJobHandle() {                                              
        byte[] rt = new byte[jobHandle.length];
        System.arraycopy(jobHandle, 0, rt, 0, jobHandle.length);
        return rt;
    }

    public void registerEventListener(GearmanIOEventListener listener)
            throws IllegalArgumentException {
        if (listener == null) {
            throw new IllegalArgumentException("listener can not be null");
        }
        listeners.add(listener);
    }

    public void fireEvent(GearmanPacket event)
            throws IllegalArgumentException {
        if (event == null) {
            throw new IllegalArgumentException("event can not be null");
        }
        for (GearmanIOEventListener listener : listeners) {
            listener.handleGearmanIOEvent(event);
        }
    }

    public void sendData(byte[] data) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_DATA,
                GearmanPacketImpl.generatePacketData(jobHandle, data)));

    }

    public void sendWarning(byte[] warning) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_WARNING,
                GearmanPacketImpl.generatePacketData(jobHandle, warning)));
    }

    public void sendException(byte[] exception) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_EXCEPTION,
                GearmanPacketImpl.generatePacketData(jobHandle, exception)));
    }

    public void sendStatus(int denominator, int numerator) {
        fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                GearmanPacketType.WORK_STATUS,
                GearmanPacketImpl.generatePacketData(jobHandle,
                ByteUtils.toUTF8Bytes(String.valueOf(numerator)),
                ByteUtils.toUTF8Bytes(String.valueOf(denominator)))));
    }

    public abstract GearmanJobResult executeFunction();

    public GearmanJobResult call() {
        GearmanPacket event = null;
        GearmanJobResult result = null;
        Exception thrown = null;
        try {
            result = executeFunction();
        } catch (Exception e) {
            thrown = e;
        }
        if (result == null) {
            String message = thrown == null ? "function returned null result" :
                thrown.getMessage();
            fireEvent(new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_EXCEPTION,
                    GearmanPacketImpl.generatePacketData(jobHandle,
                    message.getBytes())));
            result = new GearmanJobResultImpl(jobHandle, false, new byte[0],
                    new byte[0], new byte[0], -1, -1);
        }

        if (result.jobSucceeded()) {
            event = new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_COMPLETE,
                    GearmanPacketImpl.generatePacketData(jobHandle,
                    result.getResults()));
        } else {
            event = new GearmanPacketImpl(GearmanPacketMagic.REQ,
                    GearmanPacketType.WORK_FAIL, jobHandle);

        }
        fireEvent(event);
        return result;
    }
=======
 * Copyright (c) <2011> <Marco Tarasconi>
 * 
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 * 
 */

package it.unipr.aotlab.TwitterMiner.dataminer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import it.unipr.aotlab.TwitterMiner.database.RedisBackend;

/**
 * This class mines a database of tweets and users to extract new information
 * 
 * @author Marco Tarasconi
 */
public class DataMiner {

	private static final int ONETAB = 7;
	private static final int TWOTAB = 15;
	private static final int THREETAB = 23;
	private static final int FOURTAB = 31;

	/**
	 * This method writes list oh hashtag mentioned together with a specific
	 * one, ordered by co-occurrence frequency
	 */
	public void printCoOccurenceRates(RedisBackend redisDB, String hashtag) {
		Set<String> set = redisDB.getTagsUsedWith(hashtag);
		System.out.println();
		System.out.println("Hashtags co-occurence with \"#" + hashtag + "\"");
		System.out.println();

		ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<String, Long>();
		for (String s : set) {
			map.put(s, redisDB.getTagsCoOccurrence(s, hashtag));
		}
		int position = 1;
		while (!map.isEmpty()) {
			Set<String> keys = map.keySet();
			long max = 0;
			for (String key : keys) {
				if (max <= map.get(key))
					max = map.get(key);
			}
			for (String key : keys) {
				if (max == map.get(key)) {
					String output = position + ") " + key + ":";
					if (output.length() <= ONETAB)
						System.out.print(output + "\t\t\t\t\t");
					else if (output.length() > ONETAB
							&& output.length() <= TWOTAB)
						System.out.print(output + "\t\t\t\t");
					else if (output.length() > TWOTAB
							&& output.length() <= THREETAB)
						System.out.print(output + "\t\t\t");
					else if (output.length() > THREETAB
							&& output.length() <= FOURTAB)
						System.out.print(output + "\t\t");
					else
						System.out.print(output + "\t");
					long count = redisDB.getTagsCoOccurrence(key, hashtag);
					while (count > 0) {
						System.out.print("#");
						count--;
					}
					System.out.println();
					map.remove(key);
					position++;
				}
			}
		}
	}

	/**
	 * This method writes a .dot file with info about people mentioned together
	 * with an hashtag.
	 * 
	 * To draw the oriented graph of mentions type:
	 * "fdp -Tsvg MentionsRelationsAbout_<name>.dot -o <name>_fdp.svg" or refer
	 * to the dot language manual
	 */
	public void printSocialNetworkGraph(RedisBackend redisDB, String hashtag)
			throws FileNotFoundException {

		FileOutputStream file = new FileOutputStream("MentionsRelationsAbout_"
				+ hashtag + ".dot");
		PrintStream fout = new PrintStream(file);

		fout.println("digraph " + hashtag + "_network {");
		fout.println("\t graph [ splines = spline ];");
		fout.println("\t node [ color = green1, style = filled ];");
		Set<String> users = redisDB.getUsersInvolvedIn(hashtag);
		for (String user : users) {
			fout.println("\"" + user.toLowerCase() + "\";");
			Set<String> mentioned = redisDB.getUsersMentionedBy(user);
			Set<String> mentions = redisDB.getUsersThatMentioned(user);
			for (String mention : mentioned) {
				fout.println("\"" + user.toLowerCase() + "\" -> \""
						+ mention.toLowerCase() + "\";");
			}
			for (String mention : mentions)
				if (!users.contains(mention))
					fout.println("\"" + mention.toLowerCase() + "\" -> \""
							+ user.toLowerCase() + "\";");
		}
		fout.println("}");
	}

	/**
	 * This method writes a .dot file with info about the first two levels of
	 * friendship between hashtags around a given one.
	 * 
	 * To draw the graph of hashtags type:
	 * "fdp -Tsvg <name>_HashtagFriendshipL2.dot -o <name>_fdp.svg" or refer to
	 * the dot language manual
	 */
	public void printHashtagGraph(RedisBackend redisDB, String hashtag)
			throws FileNotFoundException {

		FileOutputStream file = new FileOutputStream(hashtag
				+ "_HashtagFriendshipL2.dot");
		PrintStream fout = new PrintStream(file);
		fout.println("graph " + hashtag + "_network {");
		fout.println("\t graph [ splines = spline ];");
		fout.println("\t node [ color = lightblue2, style = filled ];");
		Set<String> tags_L1 = redisDB.getTagsUsedWith(hashtag);
		fout.println("\"" + hashtag + "\""
				+ " [shape = doubleoctagon, color=skyblue3, fontcolor=white];");
		for (String tag_L1 : tags_L1) {
			fout.println("\"" + tag_L1 + "\" -- \"" + hashtag + "\"");
			Set<String> tags_L2 = redisDB.getTagsUsedWith(tag_L1);
			for (String tag_L2 : tags_L2)
				fout.println("\"" + tag_L2 + "\" -- \"" + tag_L1 + "\";");
		}
		fout.println("}");
	}

	/**
	 * This method computes cosine similarity between two users comparing the
	 * hashtags they cites, but without considering their weight
	 */
	public double computeUserBinaryCosineSimilarity(RedisBackend redisDB,
			String user1, String user2) {

		Set<String> userHashtags1 = redisDB.getTagsUsedBy(user1);
		if (userHashtags1.isEmpty()) {
			System.out.println("User " + user1 + " cited no hashtags");
			return 0;
		}
		Set<String> userHashtags2 = redisDB.getTagsUsedBy(user2);
		if (userHashtags2.isEmpty()) {
			System.out.println("User " + user2 + " cited no hashtags");
			return 0;
		}
		List<Integer> userList1 = new ArrayList<Integer>();
		List<Integer> userList2 = new ArrayList<Integer>();
		for (String u1 : userHashtags1) {
			userList1.add(new Integer(1));
			if (userHashtags2.contains(u1)) {
				userList2.add(new Integer(1));
				userHashtags2.remove(u1);
			} else
				userList2.add(new Integer(0));
		}
		for (String u2 : userHashtags2) {
			userList2.add(new Integer(1));
			if (userHashtags1.contains(u2)) {
				userList1.add(new Integer(1));
			} else
				userList1.add(new Integer(0));
		}

		// for(String u2 : userHashtags2)
		// System.out.print(u2+" ");
		// System.out.println();
		// for(String u1 : userHashtags1)
		// System.out.print(u1+" ");
		// System.out.println();
		for (int u1 : userList1)
			System.out.print(u1 + " ");
		System.out.println();
		for (int u2 : userList2)
			System.out.print(u2 + " ");
		System.out.println();

		int numerator = 0;
		for (int i = 0; i < userList1.size(); i++)
			numerator += userList1.get(i) * userList2.get(i);
		// System.out.println("num = "+numerator);
		double denominator = 0;
		double d1 = 0;
		double d2 = 0;
		for (int i = 0; i < userList1.size(); i++) {
			d1 += (double) (userList1.get(i) * userList1.get(i));
			d2 += (double) (userList2.get(i) * userList2.get(i));
		}
		// System.out.println("den1 = "+d1+" den2= "+d2);
		denominator = Math.sqrt(d1) * Math.sqrt(d2);
		// System.out.println("den = "+denominator);

		return numerator / denominator;

	}

	/**
	 * This method computes cosine similarity between two hashtags comparing the
	 * users citing them, but without considering their weight
	 */
	public double computeHashtagsBinaryCosineSimilarity(RedisBackend redisDB,
			String hashtag1, String hashtag2) {

		Set<String> tagUsers1 = redisDB.getUsersThatCited(hashtag1);
		if (tagUsers1.isEmpty()) {
			System.out.println("The hashtag " + hashtag1
					+ " has never been cited");
			return 0;
		}
		Set<String> tagUsers2 = redisDB.getUsersThatCited(hashtag2);
		if (tagUsers2.isEmpty()) {
			System.out.println("The hashtag " + hashtag2
					+ " has never been cited");
			return 0;
		}
		List<Integer> tagList1 = new ArrayList<Integer>();
		List<Integer> tagList2 = new ArrayList<Integer>();
		for (String u1 : tagUsers1) {
			tagList1.add(new Integer(1));
			if (tagUsers2.contains(u1)) {
				tagList2.add(new Integer(1));
				tagUsers2.remove(u1);
			} else
				tagList2.add(new Integer(0));
		}
		for (String u2 : tagUsers2) {
			tagList2.add(new Integer(1));
			if (tagUsers1.contains(u2)) {
				tagList1.add(new Integer(1));
			} else
				tagList1.add(new Integer(0));
		}

		// for(String u2 : tagUsers2)
		// System.out.print(u2+" ");
		// System.out.println();
		// for(String u1 : tagUsers1)
		// System.out.print(u1+" ");
		// System.out.println();
		for (int u1 : tagList1)
			System.out.print(u1 + " ");
		System.out.println();
		for (int u2 : tagList2)
			System.out.print(u2 + " ");
		System.out.println();

		int numerator = 0;
		for (int i = 0; i < tagList1.size(); i++)
			numerator += tagList1.get(i) * tagList2.get(i);
		// System.out.println("num = "+numerator);
		double denominator = 0;
		double d1 = 0;
		double d2 = 0;
		for (int i = 0; i < tagList1.size(); i++) {
			d1 += (double) (tagList1.get(i) * tagList1.get(i));
			d2 += (double) (tagList2.get(i) * tagList2.get(i));
		}
		// System.out.println("den1 = "+d1+" den2= "+d2);
		denominator = Math.sqrt(d1) * Math.sqrt(d2);
		// System.out.println("den = "+denominator);

		return numerator / denominator;

	}

	/**
	 * This method computes Tf-Idf index of a user for a given hashtag. It is a
	 * measure of the relevance of an hashtag: it is greater if the hashtag is
	 * used frequently by the current user and rarely by the other users
	 */
	public double computeUserTfIdfWithGivenHashtag(RedisBackend redisDB,
			String username, String hashtag) {

		Set<String> citedHashtags = redisDB.getTagsUsedBy(username);
		long totalFreq = 0;
		for (String tag : citedHashtags)
			totalFreq += redisDB.getUserHashtagCoOccurrence(username, tag);
		// System.out.println("totalFreq="+totalFreq);
		double tf = redisDB.getUserHashtagCoOccurrence(username, hashtag)
				/ (double) totalFreq;

		double idf = Math.log10(redisDB.getTwetterersCardinality()
				/ (double) (1 + redisDB.getHowManyUsersCite(hashtag)));
		// System.out.println("idf="+idf);

		return tf * idf;

	}

	/**
	 * This method computes Tf-Idf index of a hashtag for a given user. It is a
	 * measure of the relevance of a user: it is greater if the user cites
	 * frequently the current hashtag and rarely other hashtags
	 */
	public double computeHashtagTfIdfWithGivenUser(RedisBackend redisDB,
			String hashtag, String username) {

		Set<String> citedUsers = redisDB.getUsersThatCited(hashtag);
		long totalFreq = 0;
		for (String user : citedUsers)
			totalFreq += redisDB.getUserHashtagCoOccurrence(user, hashtag);
		// System.out.println("totalFreq="+totalFreq);
		double tf = redisDB.getUserHashtagCoOccurrence(username, hashtag)
				/ (double) totalFreq;

		double idf = Math
				.log10(redisDB.getHashtagsTotalCardinality()
						/ (double) (1 + redisDB
								.getHowManyHashtagsAreCitedBy(username)));
		// System.out.println("idf="+idf);

		return tf * idf;
	}

	/**
	 * This method computes cosine similarity between two users comparing the
	 * hashtags they cites, weighted by their tf-idf index
	 */
	public double computeUsersTfIdfCosineSimilarity(RedisBackend redisDB,
			String user1, String user2) {

		Set<String> userHashtags1 = redisDB.getTagsUsedBy(user1);
		if (userHashtags1.isEmpty()) {
			System.out.println("L'utente " + user1
					+ " non ha citato nessun hashtag");
			return 0;
		}
		Set<String> userHashtags2 = redisDB.getTagsUsedBy(user2);
		if (userHashtags2.isEmpty()) {
			System.out.println("L'utente " + user2
					+ " non ha citato nessun hashtag");
			return 0;
		}
		List<Double> userList1 = new ArrayList<Double>();
		List<Double> userList2 = new ArrayList<Double>();
		for (String u1 : userHashtags1) {
			userList1.add(new Double(computeUserTfIdfWithGivenHashtag(redisDB,
					user1, u1)));
			if (userHashtags2.contains(u1)) {
				userList2.add(new Double(computeUserTfIdfWithGivenHashtag(
						redisDB, user2, u1)));
				userHashtags2.remove(u1);
			} else
				userList2.add(new Double(0));
		}
		for (String u2 : userHashtags2) {
			userList2.add(new Double(computeUserTfIdfWithGivenHashtag(redisDB,
					user2, u2)));
			if (userHashtags1.contains(u2)) {
				userList1.add(new Double(computeUserTfIdfWithGivenHashtag(
						redisDB, user1, u2)));
			} else
				userList1.add(new Double(0));
		}

		// for(String u2 : userHashtags2)
		// System.out.print(u2+" ");
		// System.out.println();
		// for(String u1 : userHashtags1)
		// System.out.print(u1+" ");
		// System.out.println();
		// for(double u1 : userList1)
		// System.out.print(u1+" ");
		// System.out.println();
		// for(double u2 : userList2)
		// System.out.print(u2+" ");
		// System.out.println();

		double numerator = 0;
		for (int i = 0; i < userList1.size(); i++)
			numerator += userList1.get(i) * userList2.get(i);
		// System.out.println("num = "+numerator);
		double denominator = 0;
		double d1 = 0;
		double d2 = 0;
		for (int i = 0; i < userList1.size(); i++) {
			d1 += (double) (userList1.get(i) * userList1.get(i));
			d2 += (double) (userList2.get(i) * userList2.get(i));
		}
		// System.out.println("den1 = "+d1+" den2= "+d2);
		denominator = Math.sqrt(d1) * Math.sqrt(d2);
		// System.out.println("den = "+denominator);
		if (denominator != 0)
			return numerator / denominator;
		else
			return 0;

	}

	/**
	 * This method computes cosine similarity between two hashtags comparing the
	 * users citing them, weighted by their tf-idf index
	 */
	public double computeHashtagsTfIdfCosineSimilarity(RedisBackend redisDB,
			String hashtag1, String hashtag2) {

		Set<String> tagUsers1 = redisDB.getUsersThatCited(hashtag1);
		if (tagUsers1.isEmpty()) {
			System.out.println("The hashtag " + hashtag1
					+ " has never been cited");
			return 0;
		}
		Set<String> tagUsers2 = redisDB.getUsersThatCited(hashtag2);
		if (tagUsers2.isEmpty()) {
			System.out.println("The hashtag " + hashtag2
					+ " has never been cited");
			return 0;
		}

		List<Double> hashtagList1 = new ArrayList<Double>();
		List<Double> hashtagList2 = new ArrayList<Double>();
		for (String u1 : tagUsers1) {
			hashtagList1.add(new Double(computeHashtagTfIdfWithGivenUser(
					redisDB, hashtag1, u1)));
			if (tagUsers2.contains(u1)) {
				hashtagList2.add(new Double(computeHashtagTfIdfWithGivenUser(
						redisDB, hashtag2, u1)));
				tagUsers2.remove(u1);
			} else
				hashtagList2.add(new Double(0));
		}
		for (String u2 : tagUsers2) {
			hashtagList2.add(new Double(computeHashtagTfIdfWithGivenUser(
					redisDB, hashtag2, u2)));
			if (tagUsers1.contains(u2)) {
				hashtagList1.add(new Double(computeHashtagTfIdfWithGivenUser(
						redisDB, hashtag1, u2)));
			} else
				hashtagList1.add(new Double(0));
		}

		// for(String u2 : tagUsers2)
		// System.out.print(u2+" ");
		// System.out.println();
		// for(String u1 : tagUsers1)
		// System.out.print(u1+" ");
		// System.out.println();
		// for(double u1 : hashtagList1)
		// System.out.print(u1+" ");
		// System.out.println();
		// for(double u2 : hashtagList2)
		// System.out.print(u2+" ");
		// System.out.println();

		double numerator = 0;
		for (int i = 0; i < hashtagList1.size(); i++)
			numerator += hashtagList1.get(i) * hashtagList2.get(i);
		// System.out.println("num = "+numerator);
		double denominator = 0;
		double d1 = 0;
		double d2 = 0;
		for (int i = 0; i < hashtagList1.size(); i++) {
			d1 += (double) (hashtagList1.get(i) * hashtagList1.get(i));
			d2 += (double) (hashtagList2.get(i) * hashtagList2.get(i));
		}
		// System.out.println("den1 = "+d1+" den2= "+d2);
		denominator = Math.sqrt(d1) * Math.sqrt(d2);
		// System.out.println("den = "+denominator);
		if (denominator != 0)
			return numerator / denominator;
		else
			return 0;

	}

>>>>>>> 76aa07461566a5976980e6696204781271955163
}

