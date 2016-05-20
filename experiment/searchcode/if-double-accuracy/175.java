package mathPack;
import java.lang.Math;
public class Problem {
	protected int x,y,answer,numQuestions,numCorrect,numAttempts,numAttemptsTotal;
	protected String question, problemType;
	public Problem() {
		numQuestions = numCorrect = numAttempts = numAttemptsTotal = 0;
	}
	public void divisionProblem() {
		answer = (int)(10*Math.random()+1);
		x = (int)(10*Math.random()+1);
		y = answer*x;
		question = String.format("What is %d divided by %d?", y, x);
	}
	public void multiplicationProblem() {
		x = (int)(10*Math.random()+1);
		y = (int)(10*Math.random()+1);
		answer = x*y;
		question = String.format("What is %d times %d?", x, y);
	}
	public void additionProblem() {
		x = (int)(50*Math.random()+1);
		y = (int)(50*Math.random()+1);
		answer = x+y;
		question = String.format("What is %d plus %d?", x, y);
	}
	public void subtractionProblem() {
		answer = (int)(50*Math.random()+1);
		y = (int)(50*Math.random()+1);
		x = answer+y;
		question = String.format("What is %d minus %d?", x, y);
	}
	public int getX() {
		return x;
	}
	public int getY() {
		return y;
	}
	public int getAnswer() {
		return answer;
	}
	public String getQuestion() {
		return question;
	}
	public boolean submitAnswer(int a) {
		numAttemptsTotal+=1;
		return (a == answer);
	}
	public String interaction(String userin) {
		if(userin.equals("quit") || userin.equals("exit")) {
			numQuestions -= 1;
			double percent = ((double)numCorrect / numQuestions) * 100;
			double accuracy = ((double)numCorrect / numAttemptsTotal) * 100;
			return String.format("Thanks for playing the math game. Your score was:\n%2.0f%% correct\n%d questions total\n%d answers were correct\n%d answers were submitted\nYou were %2.0f%% accurate", percent, numQuestions, numCorrect, numAttemptsTotal, accuracy);
		}
		String output = "";
		if(submitAnswer(Integer.parseInt(userin))) {
			numCorrect+=1;
			output = "Correct! Good job!\nHere's a new problem:";
			newProblem();
		}
		else {
			output = "Guess again";
		}
		if(output.equals("Guess again")) {
			if(numAttempts > 3) {
				output = "WRONG! Too many tries.\nHere's a new problem:";
				newProblem();
			}
			else {
				numAttempts += 1;
			}
		}
		return output;
	}
	public String newProblem() {
		numAttempts = 1;
		numQuestions+=1;
		switch ((int)(4*Math.random()+1)) {
			case 1:
				divisionProblem();
				problemType = "division";
				break;	
			case 2:
				multiplicationProblem();
				problemType = "multiplication";
				break;
			case 3:
				additionProblem();
				problemType = "addition";
				break;
			case 4:
				subtractionProblem();
				problemType = "subtraction";
				break;
			default:break;
		}
		return getQuestion();
	}
}

