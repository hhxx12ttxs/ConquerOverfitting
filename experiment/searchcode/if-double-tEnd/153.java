package typeTeacher;

import java.awt.Toolkit;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JTextArea;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.util.*;
import javax.swing.JTextPane;
import java.awt.Color;
import java.awt.font.*;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Highlighter;
import javax.swing.text.BadLocationException;
import javax.swing.JButton;
import javax.swing.JPanel;
import java.util.Timer;
import java.util.Date;

/* Creates TextArea with generated text. The user shell type what he or she sees!*/

public class Typer extends JFrame {

	JTextArea textArea;
	int errors;
	int i;
	String string;
	Timer timer;
	long tStart;

	void generateTyper(String typetext) {

		JFrame frame = new JFrame();
		frame.setVisible(true);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // interesting,
		// setting this line would made
		// the whole program close on pressing the textArea "X"...
		frame.setLocation(400, 200);
		frame.setSize(700, 500);

		KeyHandler handler = new KeyHandler();
		Font font = new Font("Verdana", Font.PLAIN, 16);

		this.string = typetext;

		textArea = new JTextArea();
		textArea.append(string);
		frame.add(textArea);

		textArea.setEditable(false);
		textArea.setBackground(Color.white);
		textArea.setFont(font);
		textArea.setForeground(Color.black);
		textArea.setLineWrap(true);
		textArea.setWrapStyleWord(true);

		textArea.addKeyListener(handler);

	}
	
	/**
	 * 
	 * the very heart of the program follows
	 *
	 */

	private class KeyHandler implements KeyListener {
		public void keyTyped(KeyEvent event) {

			Highlighter h = textArea.getHighlighter();
			char pressed = event.getKeyChar();

			if (i == 0)
				tStart = System.currentTimeMillis();

			if (pressed == string.charAt(i)) {

				try {
					h.addHighlight(i, i + 1, DefaultHighlighter.DefaultPainter);
				} catch (BadLocationException ble) {
				}
				i++;

			} else {
				Toolkit.getDefaultToolkit().beep();
				errors++;
			}
			if (i == string.length()) {

				long tEnd = System.currentTimeMillis();
				long tDelta = tEnd - tStart;
				double elapsedTime = tDelta / 1000.0; // in seconds

				if (errors==0 && elapsedTime <= 60)
					JOptionPane.showMessageDialog(null,
							"Перфектно! Направихте " + errors
									+ " грешки!" + " Отне Ви " + elapsedTime
									+ " секунди!");
					
				else	if (errors < 10 && elapsedTime <= 80)
					JOptionPane.showMessageDialog(null,
							"Добра работа! Направихте само " + errors
									+ " грешки!" + " Отне Ви " + elapsedTime
									+ " секунди!");

				else if (errors < 6)
					JOptionPane
							.showMessageDialog(
									null,
									"Добра работа! Направихте само "
											+ errors
											+ " грешки!"
											+ " Отне Ви "
											+ elapsedTime
											+ " секунди! Можете да започнете да забързвате темпото.");
				else if (errors >= 6)
					JOptionPane
							.showMessageDialog(
									null,
									"Не бързайте! Направихте  "
											+ errors
											+ " грешки!"
											+ " Отне Ви "
											+ elapsedTime
											+ " секунди! Не гледайте времето си, докато не сте сигурни в местоположението на буквите.");

			}
		}

		public void keyPressed(KeyEvent event) {
		}

		public void keyReleased(KeyEvent event) {
		}

	}

}

