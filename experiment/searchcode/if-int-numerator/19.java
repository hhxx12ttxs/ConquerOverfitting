package featureGenerator;

import java.util.List;
import java.util.Map;

import util.Constants;
import util.MinEdits;
import util.Utility;

import model.AuthorDetail;
import model.FeatureVector;
import model.PaperDetail;
import model.TrainingData;

import datahandler.DBHandler;

public class FeatureGenerator {

	public static void main(String args[]) {

		//Utility.deleteFile(Constants.FILE_TRAINING_FEATURE_VECTOR);
		//Utility.deleteFile(Constants.FILE_TESTING_FEATURE_VECTOR);

		DBHandler dbHandler = new DBHandler();

		List<Integer> authorIdsInTraining = dbHandler.getAllAuthorIdsInTraining(Constants.GET_AUTHOR_IDs_IN_TRAINING);

		for (Integer authorIdInTraining : authorIdsInTraining) {
			TrainingData trainingData = dbHandler.getTrainingDataForAuthor(authorIdInTraining);
			AuthorDetail authorDetail = dbHandler.getAuthorDetail(authorIdInTraining);
			for (Integer deletedPaper : trainingData.getDeletedPapers()) {
				PaperDetail paperDetail = dbHandler.getPaperDetail(deletedPaper);
				computeFeatureValues(paperDetail, authorDetail, -1, true);
			}

			for (Integer deletedPaper : trainingData.getConfirmedPapers()) {
				PaperDetail paperDetail = dbHandler.getPaperDetail(deletedPaper);
				computeFeatureValues(paperDetail, authorDetail, 1, true);
			}
		}
		
		//To generate the validity cases
		List<Integer> authorIdsInValidSet = dbHandler.getAllAuthorIdsInTraining(Constants.GET_AUTHOR_IDs_IN_VALID);
		for(Integer authorIdInValid : authorIdsInValidSet){
			List<Integer> paperIdsToClassify = dbHandler.getPaperIdsFromValidTableForAuthor(authorIdInValid);
			AuthorDetail authorDetail = dbHandler.getAuthorDetail(authorIdInValid);
			for (Integer paperId : paperIdsToClassify) {
				PaperDetail paperDetail = dbHandler.getPaperDetail(paperId);
				computeFeatureValues(paperDetail, authorDetail, 0, false);
			}
		}
		
		
	}

	private static void computeFeatureValues(PaperDetail paperDetail, AuthorDetail authorDetail, int label,
			boolean isTraining) {
		FeatureVector featureVector = new FeatureVector(authorDetail.getAuthorId(), paperDetail.getPaperId());

		featureVector.setDevFromMeanYrOfPublish(getDeviationFromMeanPubYr(authorDetail.getMeanPublishYr(),
				paperDetail.getYear()));

		if (authorDetail.getAuthorName() != null && paperDetail.getAuthorNameInPaper() != null) {
			featureVector.setEditDistanceBetweenAuthorNames(MinEdits.computeLevenshteinDistance(
					authorDetail.getAuthorName(), paperDetail.getAuthorNameInPaper()));
		}

		featureVector.setSumOfOccurOfCoAuthByTotalAuth(getTotalNumTimeCoAuthorsWrkedByTotalCoAuthored(
				authorDetail.getCoAuthorWorkCount(), paperDetail.getCoAuthors(),
				authorDetail.getTotalNumberOfCoAuthorsWorked()));

		featureVector.setSumOfOccurOfKeywrdByTotalKeywrd(getKeyWordOccurenceByTotalKeyWords(
				authorDetail.getKeywordOccurenceCount(), paperDetail.getKeywordsUsed(),
				authorDetail.getTotalNumberKeywordsUsed()));

		featureVector.setTraining(isTraining);

		if (isTraining) {
			featureVector.setLabel(label);
			Utility.appendDataToFile(Constants.FILE_TRAINING_FEATURE_VECTOR, featureVector.getFeatureVectorizedString());
		} else {
			Utility.appendDataToFile(Constants.FILE_TESTING_FEATURE_VECTOR, featureVector.getFeatureVectorizedString());
		}

	}
	private static double getKeyWordOccurenceByTotalKeyWords(Map<String, Integer> keywordOccurenceCount,
			List<String> keywordsUsed, int totalNumberKeywordsUsed) {

		if (keywordsUsed != null) {
			int numerator = 0;
			for (String keywordUsed : keywordsUsed) {
				if (keywordOccurenceCount.containsKey(keywordUsed)) {
					numerator += keywordOccurenceCount.get(keywordUsed);
				}
			}
			double result = numerator / totalNumberKeywordsUsed;
			return Math.ceil(result);
		}
		return 0;
	}

	private static double getTotalNumTimeCoAuthorsWrkedByTotalCoAuthored(Map<Integer, Integer> coAuthorWorkCount,
			List<Integer> coAuthors, int totalNumberOfCoAuthors) {
		int numerator = 0;
		for (Integer coAuthor : coAuthors) {
			if (coAuthorWorkCount.containsKey(coAuthor)) {
				numerator += coAuthorWorkCount.get(coAuthor);
			}
		}
		double result = numerator / totalNumberOfCoAuthors;
		return Math.ceil(result);
	}

	private static int getDeviationFromMeanPubYr(int meanPublishYr, int year) {
		if (year != 0)
			return Math.abs(meanPublishYr - year);

		return 0;
	}
}

