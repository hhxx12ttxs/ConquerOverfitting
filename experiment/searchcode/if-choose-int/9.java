package dewafer.backword.core;

import java.util.Timer;
import java.util.TimerTask;

/**
 * The <code>Quiz</code> has been changed. 2010/3/15 by dewafer<br/>
 * 
 * @author dewafer
 */
public class Quiz {

	private Paper paper;
	private String question;
	private String[] answersList;
	private int overtime = 0;
	private int correctAnswerNumber = -1;
	private int userSelectedAnswerNumber = -1;
	private boolean isFinished = false;
	private boolean isCorrect = false;
	private boolean isAbandoned = false;
	private QuizTimeoutEventHandler timeoutEvent;
	private Timer timer;
	private Quiz thisQuiz = this;

	protected Quiz() {
	}

	protected Quiz(Paper paper) {
		this.paper = paper;
	}

	public Quiz(Paper paper, String[] answersList, int overtime,
			int correctAnswerNumber, int userSelectedAnswerNumber,
			boolean isFinished, boolean isCorrect, boolean isAbandoned,
			QuizTimeoutEventHandler timeoutEvent) {
		super();
		this.paper = paper;
		this.answersList = answersList;
		this.overtime = overtime;
		this.correctAnswerNumber = correctAnswerNumber;
		this.userSelectedAnswerNumber = userSelectedAnswerNumber;
		this.isFinished = isFinished;
		this.isCorrect = isCorrect;
		this.isAbandoned = isAbandoned;
		this.timeoutEvent = timeoutEvent;
	}

	/**
	 * @return the question
	 */
	public String getQuestion() {
		return question;
	}

	/**
	 * @param question
	 *            the question to set
	 */
	public void setQuestion(String question) {
		this.question = question;
	}

	/**
	 * Abandon the question.
	 * 
	 * @return always be false
	 */
	public boolean abandon() {
		isAbandoned = true;
		isFinished = true;
		isCorrect = false;
		paper.finishQuiz(this);
		if (timer != null) {
			timer.cancel();
		}
		return isCorrect;
	}

	/**
	 * Answer the question.
	 * 
	 * @return Is it correct?
	 */
	public boolean answer(int choose) {
		isAbandoned = false;
		isFinished = true;
		userSelectedAnswerNumber = choose;
		if (choose == correctAnswerNumber) {
			isCorrect = true;
		} else {
			isCorrect = false;
		}
		paper.finishQuiz(this);
		if (timer != null) {
			timer.cancel();
		}
		return isCorrect;
	}

	/**
	 * @return the errorAnswerList
	 */
	public String[] getAnswersList() {
		return answersList;
	}

	/**
	 * @param allAnswerCountNumber
	 *            the allAnswerCountNumber to set
	 */
	// protected void setAllAnswerCount(int allAnswerCountNumber) {
	// this.allAnswerCount = allAnswerCountNumber;
	// }
	/**
	 * @return the currentAnswerNumber
	 */
	public int getCorrentAnswerNumber() {
		return correctAnswerNumber;
	}

	/**
	 * @return the overtime
	 */
	public int getOvertime() {
		return overtime;
	}

	/**
	 * @return the userSelectAnswerNumber
	 */
	public int getUserSelectedAnswerNumber() {
		return userSelectedAnswerNumber;
	}

	/**
	 * @param isCorrect
	 *            the isCorrect to set
	 */
	// protected void setCorrect(boolean isCorrect) {
	// this.isCorrect = isCorrect;
	// }
	/**
	 * @return the isAbandoned
	 */
	public boolean isAbandoned() {
		return isAbandoned;
	}

	/**
	 * @param isFinished
	 *            the isFinished to set
	 */
	// protected void setFinished(boolean isFinished) {
	// this.isFinished = isFinished;
	// }
	/**
	 * @return the isCorrect
	 */
	public boolean isCorrect() {
		return isCorrect;
	}

	/**
	 * @return the isFinished
	 */
	public boolean isFinished() {
		return isFinished;
	}

	/**
	 * @param errorAnswerList
	 *            the errorAnswerList to set
	 */
	protected void setAnswersList(String[] errorAnswerList) {
		this.answersList = errorAnswerList;
	}

	/**
	 * @param correntAnswerNumber
	 *            the currentAnswerNumber to set
	 */
	protected void setCorrentAnswerNumber(int correntAnswerNumber) {
		this.correctAnswerNumber = correntAnswerNumber;
	}

	/**
	 * @param overtime
	 *            the overtime to set
	 */
	protected void setOvertime(int overtime) {
		this.overtime = overtime;
	}

	/**
	 * @param timeoutEvent
	 *            the timeoutEvent to set
	 */
	protected void setTimeoutEvent(QuizTimeoutEventHandler timeoutEvent) {
		this.timeoutEvent = timeoutEvent;
	}

	/**
	 * @param userSelectedAnswerNumber
	 *            the userSelectAnswerNumber to set
	 */
	protected void setUserSelectedAnswerNumber(int userSelectedAnswerNumber) {
		this.userSelectedAnswerNumber = userSelectedAnswerNumber;
	}

	/**
	 * @return the paper
	 */
	public Paper getPaper() {
		return paper;
	}

	/**
	 * @param paper
	 *            the paper to set
	 */
	protected void setPaper(Paper paper) {
		this.paper = paper;
	}

	/**
	 * @param isAbandoned
	 *            the isAbandoned to set
	 */
	// protected void setAbandoned(boolean isAbandoned) {
	// this.isAbandoned = isAbandoned;
	// }

	public void start() {
		if (timer == null) {
			timer = new Timer();
		}
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				if (timeoutEvent != null) {
					timeoutEvent.TimeoutEvent(thisQuiz);
				}
				thisQuiz.abandon();
			}
		}, overtime);
	}
}

