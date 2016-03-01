package sokmotorer;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.common.FastIDSet;
import org.apache.mahout.cf.taste.impl.model.GenericBooleanPrefDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.TanimotoCoefficientSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

public class MahoutCF {

	public static void main(String[] args) throws IOException, TasteException {
		System.out.println(args[0]);
		System.out.println(args[1]);
		File tripletsFile = new File(args[0]);
		File expDir = new File(args[1]);
		File usersFile = new File(expDir, "users.txt");
		File trainFile = new File(expDir, "train.txt");
		List<Long> users = getUsers(usersFile);
		DataModel model = getModel(tripletsFile, trainFile, users);
		UserSimilarity userSimilarity = null;
		
		// Experiment 1 - 4 (user-based loglikelihood similarity)
		userSimilarity = new LogLikelihoodSimilarity(model);
		for(int u : new int[]{32, 64, 128, 256}) {
			userBasedRecommendations(model, 
					userSimilarity,
					new NearestNUserNeighborhood(u, userSimilarity, model),
					users,
					new File(expDir, "userbased-log-" + u + ".txt"));
		}
		
		// Experiment 5 - 8 (user-based tanimoto similarity)
		userSimilarity = new TanimotoCoefficientSimilarity(model);
		for(int u : new int[]{32, 64, 128, 256}) {
			userBasedRecommendations(model, 
					userSimilarity,
					new NearestNUserNeighborhood(u, userSimilarity, model),
					users,
					new File(expDir, "userbased-tanimoto-" + u + ".txt"));
		}
		
		ItemSimilarity itemSimilarity = null;
		// Experiment 9 (item-based loglike similarity)
		itemSimilarity = new LogLikelihoodSimilarity(model);
		itemBasedRecommendations(model, 
					itemSimilarity,
					users,
					new File(expDir, "itembased-log.txt"));

		// Experiment 10
		itemSimilarity = new TanimotoCoefficientSimilarity(model);
		itemBasedRecommendations(model, 
					itemSimilarity,
					users,
					new File(expDir, "itembased-tanimoto.txt"));
	}
	
	public static void userBasedRecommendations(
			DataModel model, 
			UserSimilarity userSimilarity, 
			UserNeighborhood neighborhood, 
			List<Long> users,
			File outputFile) throws IOException, TasteException {
		Recommender recommender =
		          new GenericBooleanPrefUserBasedRecommender(model, neighborhood, userSimilarity);

		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		for(Long u : users) {
			System.out.print("+");
			List<RecommendedItem> recommendations = recommender.recommend(u, 500);
			for(RecommendedItem item : recommendations) {
				out.write(String.valueOf(item.getItemID()));
				out.write(" ");
			}
			out.write("\n");
		}
		out.close();
		System.out.println("\nFile written: " + outputFile.getName());
	}
	
	public static void itemBasedRecommendations(
			DataModel model, 
			ItemSimilarity itemSimilarity, 
			List<Long> users,
			File outputFile) throws IOException, TasteException {
		Recommender recommender =
		          new GenericBooleanPrefItemBasedRecommender(model, itemSimilarity);

		BufferedWriter out = new BufferedWriter(new FileWriter(outputFile));
		for(Long u : users) {
			System.out.print("+");
			List<RecommendedItem> recommendations = recommender.recommend(u, 500);
			for(RecommendedItem item : recommendations) {
				out.write(String.valueOf(item.getItemID()));
				out.write(" ");
			}
			out.write("\n");
		}
		out.close();
		System.out.println("\nFile written: " + outputFile.getName());
	}

	public static List<Long> getUsers(File usersFile) throws FileNotFoundException {
		Scanner scanner = new Scanner(usersFile);
		ArrayList<Long> users = new ArrayList<Long>();
		while(scanner.hasNext()) {
			users.add(scanner.nextLong());
		}
		return users;
	}

	public static DataModel getModel(
			File tripletsFile, File trainFile, List<Long> testUsers)
			throws IOException, NumberFormatException, TasteException {
		FastByIDMap<FastIDSet> prefs = new FastByIDMap<FastIDSet>();
		HashSet<Long> userSet = new HashSet<Long>(testUsers);
		BufferedReader tripletsReader = new BufferedReader(new FileReader(tripletsFile));
		tripletsReader.readLine(); // first line is header!
		while(true) {
			String line = tripletsReader.readLine();
			if(line == null) break;
			String[] parts = line.split(",");
			long user = Long.parseLong(parts[0]);
			long song = Long.parseLong(parts[1]);
			if(userSet.contains(user)) continue;
			FastIDSet songs = prefs.get(user);
			if(songs == null) {
				songs = new FastIDSet();
				prefs.put(user, songs);
			}
			songs.add(song);
		}
		tripletsReader.close();
		BufferedReader trainReader = new BufferedReader(new FileReader(trainFile));
		while(true) {
			String line = trainReader.readLine();
			if(line == null) break;
			String[] parts = line.split(",");
			long user = Long.parseLong(parts[0]);
			long song = Long.parseLong(parts[1]);
			FastIDSet songs = prefs.get(user);
			if(songs == null) {
				songs = new FastIDSet();
				prefs.put(user, songs);
			}
			songs.add(song);
		}
		trainReader.close();
		System.out.println("Triplets loaded!");
		return new GenericBooleanPrefDataModel(prefs);
	}
}

