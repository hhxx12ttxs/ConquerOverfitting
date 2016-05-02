import java.io.FileWriter;
import java.io.IOException;

import edu.hk.csie.u100b219.finalproject.models.Guess;
import edu.hk.csie.u100b219.finalproject.models.GuessNumber;

public class GuessNumberTest {

	/**
	 * test count
	 */
	private static final int count = 100000;

	/**
	 * @param args
	 * @throws IOException 
	 */
	public static void main(String[] args) throws IOException {
		FileWriter fw = new FileWriter("test.txt");
		int min = 100, max = 0;
		double avg = 0;
		GuessNumber gn = new GuessNumber();
		for (int i = 0; i < count; i++) {
			IQuestion question = new Question();
			boolean isGuessed = false;
			while (!isGuessed) {
				try {
					Guess guess = question.guess(gn.genNumber());
					gn.guess(guess);
					if (guess.getA() == 3) {
						int gc = question.getGuessCount();
						//System.out.println("²q¤¤¤F¡I ²q¤F" + gc + "¦¸");
						fw.write(String.valueOf(gc) + "\n");
						avg += gc;
						if (min > gc)
							min = gc;
						if (max < gc)
							max = gc;
						isGuessed = true;
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			gn.clearAll();
		}
		avg /= count;
		System.out.println("¥­§¡:" + avg + ", ³Ì¤p:" + min + ", ³Ì¤j:" + max);
		fw.close();
	}

}

