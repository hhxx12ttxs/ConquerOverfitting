package quizProgram;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;
import java.util.ArrayList;

/**
 * A test file for running small Java projects.
 * @author Daniel Wu
 */
public class RunTests {
    private static Scanner inScanner = new Scanner(System.in);
    private static boolean firstRun = true;
    private static int counter = 10;
    private static ArrayList<String> questionSet = null;
    private static ArrayList<String> answerSet = null;

    /**
     * The main quiz driver.
     * @param args  The arguments passed in from the terminal.
     */
    public static void main(String[] args) {
        System.out.println("\n");
        System.out.println("This is a UNIX comprehension quiz. You will be asked to answer a randomized set of questions about UNIX scripting.\n\nPlease press the [ENTER] key to start your quiz.");
        while (inScanner.hasNextLine()) {
            if (inScanner.nextLine().equals("q")) {
                System.out.println("\n");
                System.exit(1);
            }
            System.out.println("\n\n");
            runQuiz();
            System.out.println("\n\nPress \"q\" to quit the quiz, or press [ENTER] to take another quiz.");
        }
    }

    /**
     * Runs one quiz.
     */
    public static void runQuiz () {
        int i = 0;
        if (firstRun) {
            firstRun = false;
            setup();
        }

        int correctAnswers = 0;
        while (i < counter) {
            if (needSetup())
                setup();
            int tempIndex = (int) Math.round(Math.random() * (questionSet.size() - 1));
            String question = questionSet.get(tempIndex);
            String answer = answerSet.get(tempIndex);
            System.out.println("-------------------------------------------------");
            System.out.println(question + ":");
            String givenAnswer = inScanner.nextLine();
            if (validate(question, answer, givenAnswer)) {
                correctAnswers += 1;
                System.out.println("Correct!");
            } else
                System.out.println("Incorrect. The correct answer was:\n" + answer);
            System.out.println("\n");
            questionSet.remove(question);
            answerSet.remove(answer);
            i += 1;
        }
        System.out.println("-------------------------------------------------");
        System.out.println("Quiz score: " + correctAnswers + " / " + counter);
        System.out.println("-------------------------------------------------");
    }

    private static boolean validate(String question,
                                    String answer, String givenAnswer) {
        String tempAnswer = answer;
        String tempGivenAnswer = givenAnswer;

        if (tempAnswer.contains("scp ") || tempAnswer.contains("ssh ")) {
            tempAnswer = tempAnswer.replaceAll(" [^\\ \\@]*@[^\\.]*\\.[^\\.]*\\.berkeley\\.edu", " account@star.cs.berkeley.edu");
            tempGivenAnswer = tempGivenAnswer.replaceAll(" [^\\ \\@]*@[^\\.]*\\.[^\\.]*\\.berkeley\\.edu", " account@star.cs.berkeley.edu");
            tempAnswer = tempAnswer.replaceAll(":.*", ":.");
            tempGivenAnswer = tempGivenAnswer.replaceAll(":.*", ":.");
        }
        tempAnswer = tempAnswer.replaceAll("scp -r ", "scp ");
        tempGivenAnswer = tempGivenAnswer.replaceAll("scp -r ", "scp ");

        if (question.contains(" source ") || question.contains(" sourcing ")) {
            tempAnswer = tempAnswer.replaceFirst("source [^\\ ]*", ". file");
            tempGivenAnswer = tempGivenAnswer.replaceFirst("source [^\\ ]*", ". file");
            tempAnswer = tempAnswer.replaceFirst("\\. [^\\ ]*", ". file");
            tempGivenAnswer = tempGivenAnswer.replaceFirst("\\. [^\\ ]*", ". file");
        }

        tempAnswer = tempAnswer.replaceAll("cp -R", "cp -r");
        tempGivenAnswer = tempGivenAnswer.replaceAll("cp -R", "cp -r");

        tempAnswer = tempAnswer.replaceAll("svn status", "svn st");
        tempGivenAnswer = tempGivenAnswer.replaceAll("svn status", "svn st");

        tempAnswer = tempAnswer.replaceAll("svn checkout", "svn co");
        tempGivenAnswer = tempGivenAnswer.replaceAll("svn checkout", "svn co");

        tempAnswer = tempAnswer.replaceAll("svn update", "svn up");
        tempGivenAnswer = tempGivenAnswer.replaceAll("svn update", "svn up");

        tempAnswer = tempAnswer.replaceAll("svn remove", "svn rm");
        tempGivenAnswer = tempGivenAnswer.replaceAll("svn remove", "svn rm");

        tempAnswer = tempAnswer.replaceAll("svn delete", "svn rm");
        tempGivenAnswer = tempGivenAnswer.replaceAll("svn delete", "svn rm");

        tempAnswer = tempAnswer.replaceAll("git remove", "git rm");
        tempGivenAnswer = tempGivenAnswer.replaceAll("git remove", "git rm");

        if (question.contains("autocomplete"))
            tempGivenAnswer = tempGivenAnswer.toLowerCase();

        if (question.contains(" alias ")) {
            while (!tempAnswer.equals("") && tempAnswer.charAt(0) != '=')
                tempAnswer = tempAnswer.substring(1);
            if (tempAnswer.length() > 2)
                tempAnswer = tempAnswer.substring(2);

            while (!tempGivenAnswer.equals("") && tempGivenAnswer.charAt(0) != '=')
                tempGivenAnswer = tempGivenAnswer.substring(1);
            if (tempGivenAnswer.length() > 2)
                tempGivenAnswer = tempGivenAnswer.substring(2);
        } else if (question.contains(" command ")) {
            // do nothing
        } else {
            System.err.println("Unknown question format: " + question);
            System.exit(1);
        }

        return tempAnswer.equals(tempGivenAnswer);
    }

    private static boolean needSetup() {
        return (questionSet == null) || questionSet.isEmpty();
    }

    private static void setup() {
        questionSet = fileToArrayList("quizProgram/questions.txt");
        answerSet = fileToArrayList("quizProgram/answers.txt");
        if (questionSet.size() != answerSet.size()) {
            System.err.println("The number of questions does not correspond to the number of correct answers in this quiz.");
            System.exit(1);
        }
    }

    private static ArrayList<String> fileToArrayList(String fileName) {
        File f = new File(fileName);
        ArrayList<String> stringList = new ArrayList<String>();
        try {
            Scanner s = new Scanner(f);
            while (s.hasNextLine())
                stringList.add(s.nextLine());
            return stringList;
        } catch (FileNotFoundException e) {
            System.err.println("File was not found: " + fileName);
            System.exit(1);
        }
        return null;
    }

    private static void printArray(ArrayList<String> ints) {
        for (int i = 0; i < ints.size(); i += 1)
            System.out.print(ints.get(i) + " ");
        System.out.println();
    }

}

