<<<<<<< HEAD
/*
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

=======
//=============================================================================
// Copyright 2006-2013 Daniel W. Dyer
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
//=============================================================================
package org.uncommons.reportng;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.testng.IInvokedMethod;
import org.testng.ISuite;
import org.testng.ISuiteResult;
import org.testng.ITestContext;
import org.testng.ITestNGMethod;
import org.testng.ITestResult;
import org.testng.Reporter;
import org.testng.SkipException;

/**
 * Utility class that provides various helper methods that can be invoked
 * from a Velocity template.
 * @author Daniel Dyer
 */
public class ReportNGUtils
{
    private static final NumberFormat DURATION_FORMAT = new DecimalFormat("#0.000");
    private static final NumberFormat PERCENTAGE_FORMAT = new DecimalFormat("#0.00%");

    /**
     * Returns the aggregate of the elapsed times for each test result.
     * @param context The test results.
     * @return The sum of the test durations.
     */
    public long getDuration(ITestContext context)
    {
        long duration = getDuration(context.getPassedConfigurations().getAllResults());
        duration += getDuration(context.getPassedTests().getAllResults());
        // You would expect skipped tests to have durations of zero, but apparently not.
        duration += getDuration(context.getSkippedConfigurations().getAllResults());
        duration += getDuration(context.getSkippedTests().getAllResults());
        duration += getDuration(context.getFailedConfigurations().getAllResults());
        duration += getDuration(context.getFailedTests().getAllResults());
        return duration;
    }


    /**
     * Returns the aggregate of the elapsed times for each test result.
     * @param results A set of test results.
     * @return The sum of the test durations.
     */
    private long getDuration(Set<ITestResult> results)
    {
        long duration = 0;
        for (ITestResult result : results)
        {
            duration += (result.getEndMillis() - result.getStartMillis());
        }
        return duration;
    }


    public String formatDuration(long startMillis, long endMillis)
    {
        long elapsed = endMillis - startMillis;
        return formatDuration(elapsed);
    }


    public String formatDuration(long elapsed)
    {
        double seconds = (double) elapsed / 1000;
        return DURATION_FORMAT.format(seconds);
    }


    /**
     * Convert a Throwable into a list containing all of its causes.
     * @param t The throwable for which the causes are to be returned. 
     * @return A (possibly empty) list of {@link Throwable}s.
     */
    public List<Throwable> getCauses(Throwable t)
    {
        List<Throwable> causes = new LinkedList<Throwable>();
        Throwable next = t;
        while (next.getCause() != null)
        {
            next = next.getCause();
            causes.add(next);
        }
        return causes;
    }


    /**
     * Retrieves all log messages associated with a particular test result.
     * @param result Which test result to look-up.
     * @return A list of log messages.
     */
    public List<String> getTestOutput(ITestResult result)
    {
        return Reporter.getOutput(result);
    }


    /**
     * Retieves the output from all calls to {@link org.testng.Reporter#log(String)}
     * across all tests.
     * @return A (possibly empty) list of log messages.
     */
    public List<String> getAllOutput()
    {
        return Reporter.getOutput();
    }


    public boolean hasArguments(ITestResult result)
    {
        return result.getParameters().length > 0;
    }


    public String getArguments(ITestResult result)
    {
        Object[] arguments = result.getParameters();
        List<String> argumentStrings = new ArrayList<String>(arguments.length);
        for (Object argument : arguments)
        {
            argumentStrings.add(renderArgument(argument));
        }
        return commaSeparate(argumentStrings);
    }


    /**
     * Decorate the string representation of an argument to give some
     * hint as to its type (e.g. render Strings in double quotes).
     * @param argument The argument to render.
     * @return The string representation of the argument.
     */
    private String renderArgument(Object argument)
    {
        if (argument == null)
        {
            return "null";
        }
        else if (argument instanceof String)
        {
            return "\"" + argument + "\"";
        }
        else if (argument instanceof Character)
        {
            return "\'" + argument + "\'";
        }
        else
        {
            return argument.toString();
        }
    }


    /**
     * @param result The test result to be checked for dependent groups.
     * @return True if this test was dependent on any groups, false otherwise.
     */
    public boolean hasDependentGroups(ITestResult result)
    {
        return result.getMethod().getGroupsDependedUpon().length > 0;
    }



    /**
     * @return A comma-separated string listing all dependent groups.  Returns an
     * empty string it there are no dependent groups.
     */
    public String getDependentGroups(ITestResult result)
    {
        String[] groups = result.getMethod().getGroupsDependedUpon();
        return commaSeparate(Arrays.asList(groups));
    }


    /**
     * @param result The test result to be checked for dependent methods.
     * @return True if this test was dependent on any methods, false otherwise.
     */
    public boolean hasDependentMethods(ITestResult result)
    {
        return result.getMethod().getMethodsDependedUpon().length > 0;
    }



    /**
     * @return A comma-separated string listing all dependent methods.  Returns an
     * empty string it there are no dependent methods.
     */
    public String getDependentMethods(ITestResult result)
    {
        String[] methods = result.getMethod().getMethodsDependedUpon();
        return commaSeparate(Arrays.asList(methods));
    }
    
    
    public boolean hasSkipException(ITestResult result) 
    {
    	return result.getThrowable() instanceof SkipException;
    }
    
    
    public String getSkipExceptionMessage(ITestResult result) 
    {
        return hasSkipException(result) ? result.getThrowable().getMessage() : "";
    }


    public boolean hasGroups(ISuite suite)
    {
        return !suite.getMethodsByGroups().isEmpty();
    }


    /**
     * Takes a list of Strings and combines them into a single comma-separated
     * String.
     * @param strings The Strings to combine.
     * @return The combined, comma-separated, String.
     */
    private String commaSeparate(Collection<String> strings)
    {
        StringBuilder buffer = new StringBuilder();
        Iterator<String> iterator = strings.iterator();
        while (iterator.hasNext())
        {
            String string = iterator.next();
            buffer.append(string);
            if (iterator.hasNext())
            {
                buffer.append(", ");
            }
        }
        return buffer.toString();
    }


    /**
     * Replace any angle brackets, quotes, apostrophes or ampersands with the
     * corresponding XML/HTML entities to avoid problems displaying the String in
     * an XML document.  Assumes that the String does not already contain any
     * entities (otherwise the ampersands will be escaped again).
     * @param s The String to escape.
     * @return The escaped String.
     */
    public String escapeString(String s)
    {
        if (s == null)
        {
            return null;
        }
        
        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < s.length(); i++)
        {
            buffer.append(escapeChar(s.charAt(i)));
        }
        return buffer.toString();
    }


    /**
     * Converts a char into a String that can be inserted into an XML document,
     * replacing special characters with XML entities as required.
     * @param character The character to convert.
     * @return An XML entity representing the character (or a String containing
     * just the character if it does not need to be escaped).
     */
    private String escapeChar(char character)
    {
        switch (character)
        {
            case '<': return "&lt;";
            case '>': return "&gt;";
            case '"': return "&quot;";
            case '\'': return "&apos;";
            case '&': return "&amp;";
            default: return String.valueOf(character);
        }
    }


    /**
     * Works like {@link #escapeString(String)} but also replaces line breaks with
     * &lt;br /&gt; tags and preserves significant whitespace. 
     * @param s The String to escape.
     * @return The escaped String.
     */
    public String escapeHTMLString(String s)
    {
        if (s == null)
        {
            return null;
        }
        
        StringBuilder buffer = new StringBuilder();
        for(int i = 0; i < s.length(); i++)
        {
            char ch = s.charAt(i);
            switch (ch)
            {
                case ' ':
                    // All spaces in a block of consecutive spaces are converted to
                    // non-breaking space (&nbsp;) except for the last one.  This allows
                    // significant whitespace to be retained without prohibiting wrapping.
                    char nextCh = i + 1 < s.length() ? s.charAt(i + 1) : 0;
                    buffer.append(nextCh==' ' ? "&nbsp;" : " ");
                    break;
                case '\n':
                    buffer.append("<br/>\n");
                    break;
                default:
                    buffer.append(escapeChar(ch));
            }
        }
        return buffer.toString();
    }


    /**
     * TestNG returns a compound thread ID that includes the thread name and its numeric ID,
     * separated by an 'at' sign.  We only want to use the thread name as the ID is mostly
     * unimportant and it takes up too much space in the generated report.
     * @param threadId The compound thread ID.
     * @return The thread name.
     */
    public String stripThreadName(String threadId)
    {
        if (threadId == null)
        {
            return null;
        }
        else
        {
            int index = threadId.lastIndexOf('@');
            return index >= 0 ? threadId.substring(0, index) : threadId;
        }
    }


    /**
     * Find the earliest start time of the specified methods.
     * @param methods A list of test methods.
     * @return The earliest start time.
     */
    public long getStartTime(List<IInvokedMethod> methods)
    {
        long startTime = System.currentTimeMillis();
        for (IInvokedMethod method : methods)
        {
            startTime = Math.min(startTime, method.getDate());
        }
        return startTime;
    }


    public long getEndTime(ISuite suite, IInvokedMethod method, List<IInvokedMethod> methods)
    {
        boolean found = false;
        for (IInvokedMethod m : methods)
        {
            if (m == method)
            {
                found = true;
            }
            // Once a method is found, find subsequent method on same thread.
            else if (found && m.getTestMethod().getId().equals(method.getTestMethod().getId()))
            {
                return m.getDate();
            }
        }
        return getEndTime(suite, method);
    }


    /**
     * Returns the timestamp for the time at which the suite finished executing.
     * This is determined by finding the latest end time for each of the individual
     * tests in the suite.
     * @param suite The suite to find the end time of. 
     * @return The end time (as a number of milliseconds since 00:00 1st January 1970 UTC).
     */
    private long getEndTime(ISuite suite, IInvokedMethod method)
    {
        // Find the latest end time for all tests in the suite.
        for (Map.Entry<String, ISuiteResult> entry : suite.getResults().entrySet())
        {
            ITestContext testContext = entry.getValue().getTestContext();
            for (ITestNGMethod m : testContext.getAllTestMethods())
            {
                if (method == m)
                {
                    return testContext.getEndDate().getTime();
                }
            }
            // If we can't find a matching test method it must be a configuration method.
            for (ITestNGMethod m : testContext.getPassedConfigurations().getAllMethods())
            {
                if (method == m)
                {
                    return testContext.getEndDate().getTime();
                }
            }
            for (ITestNGMethod m : testContext.getFailedConfigurations().getAllMethods())
            {
                if (method == m)
                {
                    return testContext.getEndDate().getTime();
                }
            }
        }
        throw new IllegalStateException("Could not find matching end time.");
    }


    public String formatPercentage(int numerator, int denominator)
    {
        return PERCENTAGE_FORMAT.format(numerator / (double) denominator);
    }
>>>>>>> 76aa07461566a5976980e6696204781271955163
}

