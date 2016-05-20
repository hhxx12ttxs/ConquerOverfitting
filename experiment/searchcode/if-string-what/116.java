/*
 Part of the ReplicatorG project - http://www.replicat.org
 Copyright (c) 2008 Zach Smith

 Forked from Arduino: http://www.arduino.cc

 Based on Processing http://www.processing.org
 Copyright (c) 2004-05 Ben Fry and Casey Reas
 Copyright (c) 2001-04 Massachusetts Institute of Technology

 This program is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software Foundation,
 Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package replicatorg.app.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;

import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;

import replicatorg.app.Base;

/**
 * Message console that sits below the editing area.
 * <P>
 * Debugging this class is tricky... If it's throwing exceptions, don't take
 * over System.err, and debug while watching just System.out or just write
 * println() or whatever directly to systemOut or systemErr.
 */
public class MessagePanel extends JScrollPane {
	MainWindow editor;

	JTextPane consoleTextPane;

	BufferedStyledDocument consoleDoc;

	MutableAttributeSet stdStyle;

	MutableAttributeSet errStyle;

	boolean cerror;

	// int maxCharCount;
	int maxLineCount;

	static File errFile;

	static File outFile;

	static File tempFolder;

	static PrintStream systemOut;

	static PrintStream systemErr;

	static PrintStream consoleOut;

	static PrintStream consoleErr;

	static OutputStream stdoutFile;

	static OutputStream stderrFile;

	public MessagePanel(MainWindow editor) {
		this.editor = editor;

		maxLineCount = Base.preferences.getInt("console.length",500);

		consoleDoc = new BufferedStyledDocument(10000, maxLineCount);
		consoleTextPane = new JTextPane(consoleDoc);
		consoleTextPane.setEditable(false);

		// necessary?
		MutableAttributeSet standard = new SimpleAttributeSet();
		StyleConstants.setAlignment(standard, StyleConstants.ALIGN_LEFT);
		consoleDoc.setParagraphAttributes(0, 0, standard, true);

		// build styles for different types of console output
		Color bgColor = Base.getColorPref("console.color","#000000");
		Color fgColorOut = Base.getColorPref("console.output.color","#ccccbb");
		Color fgColorErr = Base.getColorPref("console.error.color","#ff3000");
		Font font = Base.getFontPref("console.font","Monospaced,plain,11");

		stdStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(stdStyle, fgColorOut);
		StyleConstants.setBackground(stdStyle, bgColor);
		StyleConstants.setFontSize(stdStyle, font.getSize());
		StyleConstants.setFontFamily(stdStyle, font.getFamily());
		StyleConstants.setBold(stdStyle, font.isBold());
		StyleConstants.setItalic(stdStyle, font.isItalic());

		errStyle = new SimpleAttributeSet();
		StyleConstants.setForeground(errStyle, fgColorErr);
		StyleConstants.setBackground(errStyle, bgColor);
		StyleConstants.setFontSize(errStyle, font.getSize());
		StyleConstants.setFontFamily(errStyle, font.getFamily());
		StyleConstants.setBold(errStyle, font.isBold());
		StyleConstants.setItalic(errStyle, font.isItalic());

		consoleTextPane.setBackground(bgColor);

		// add the jtextpane to this scrollpane
		this.setViewportView(consoleTextPane);

		// calculate height of a line of text in pixels
		// and size window accordingly
		FontMetrics metrics = this.getFontMetrics(font);
		int height = metrics.getAscent() + metrics.getDescent();
		int lines = Base.preferences.getInt("console.lines",4); // , 4);
		int sizeFudge = 6; // 10; // unclear why this is necessary, but it is
		setPreferredSize(new Dimension(1024, (height * lines) + sizeFudge));
		setMinimumSize(new Dimension(1024, (height * 4) + sizeFudge));

		if (systemOut == null) {
			systemOut = System.out;
			systemErr = System.err;

			tempFolder = Base.createTempFolder("console");
			try {
				String outFileName = Base.preferences.get("console.output.file","stdout.txt");
				if (outFileName != null) {
					outFile = new File(tempFolder, outFileName);
					stdoutFile = new FileOutputStream(outFile);
					// outFile.deleteOnExit();
				}

				String errFileName = Base.preferences.get("console.error.file","stderr.txt");
				if (errFileName != null) {
					errFile = new File(tempFolder, errFileName);
					stderrFile = new FileOutputStream(errFile);
					// errFile.deleteOnExit();
				}
			} catch (IOException e) {
				Base.showWarning("Console Error",
						"A problem occurred while trying to open the\n"
								+ "files used to store the console output.", e);
			}

			consoleOut = new PrintStream(new EditorConsoleStream(this, false,
					stdoutFile));
			consoleErr = new PrintStream(new EditorConsoleStream(this, true,
					stderrFile));

			if (Base.preferences.getBoolean("console",true)) {
				try {
					System.setOut(consoleOut);
					System.setErr(consoleErr);
				} catch (Exception e) {
					e.printStackTrace(systemOut);
				}
			}
		}

		// to fix ugliness.. normally macosx java 1.3 puts an
		// ugly white border around this object, so turn it off.
		if (Base.isMacOS()) {
			setBorder(null);
		}

		// periodically post buffered messages to the console
		// should the interval come from the preferences file?
		new javax.swing.Timer(250, new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				// only if new text has been added
				if (consoleDoc.hasAppendage) {
					// insert the text that's been added in the meantime
					consoleDoc.insertAll();
					// always move to the end of the text as it's added
					consoleTextPane.setCaretPosition(consoleDoc.getLength());
				}
			}
		}).start();
	}

	/**
	 * Close the streams so that the temporary files can be deleted. <p/>
	 * File.deleteOnExit() cannot be used because the stdout and stderr files
	 * are inside a folder, and have to be deleted before the folder itself is
	 * deleted, which can't be guaranteed when using the deleteOnExit() method.
	 */
	public void handleQuit() {
		// replace original streams to remove references to console's streams
		System.setOut(systemOut);
		System.setErr(systemErr);

		// close the PrintStream
		consoleOut.close();
		consoleErr.close();

		// also have to close the original FileOutputStream
		// otherwise it won't be shut down completely
		try {
			stdoutFile.close();
			stderrFile.close();
		} catch (IOException e) {
			e.printStackTrace(systemOut);
		}

		outFile.delete();
		errFile.delete();
		tempFolder.delete();
	}

	public void write(byte b[], int offset, int length, boolean err) {
		if (err != cerror) {
			// advance the line because switching between err/out streams
			// potentially, could check whether we're already on a new line
			message("", cerror, true);
		}

		// we could do some cross platform CR/LF mangling here before outputting

		// add text to output document
		message(new String(b, offset, length), err, false);
		// set last error state
		cerror = err;
	}

	// added sync for 0091.. not sure if it helps or hinders
	// synchronized public void message(String what, boolean err, boolean
	// advance) {
	public void message(String what, boolean err, boolean advance) {
		if (err) {
			systemErr.print(what);
		} else {
			systemOut.print(what);
		}

		if (advance) {
			appendText("\n", err);
			if (err) {
				systemErr.println();
			} else {
				systemOut.println();
			}
		}

		// to console display
		appendText(what, err);
		// moved down here since something is punting
	}

	/**
	 * append a piece of text to the console.
	 * <P>
	 * Swing components are NOT thread-safe, and since the MessageSiphon
	 * instantiates new threads, and in those callbacks, they often print output
	 * to stdout and stderr, which are wrapped by EditorConsoleStream and
	 * eventually leads to MessagePanel.appendText(), which directly updates
	 * the Swing text components, causing deadlock.
	 * <P>
	 * Updates are buffered to the console and displayed at regular intervals on
	 * Swing's event-dispatching thread. (patch by David Mellis)
	 */
	synchronized private void appendText(String txt, boolean e) {
		consoleDoc.appendString(txt, e ? errStyle : stdStyle);
	}

	public void clear() {
		try {
			consoleDoc.remove(0, consoleDoc.getLength());
		} catch (BadLocationException e) {
			// ignore the error otherwise this will cause an infinite loop
			// maybe not a good idea in the long run?
		}
	}
}

class EditorConsoleStream extends OutputStream {
	MessagePanel parent;

	boolean err; // whether stderr or stdout

	byte single[] = new byte[1];

	OutputStream echo;

	public EditorConsoleStream(MessagePanel parent, boolean err,
			OutputStream echo) {
		this.parent = parent;
		this.err = err;
		this.echo = echo;
	}

	public void close() {
	}

	public void flush() {
	}

	public void write(byte b[]) { // appears never to be used
		parent.write(b, 0, b.length, err);
		if (echo != null) {
			try {
				echo.write(b); // , 0, b.length);
				echo.flush();
			} catch (IOException e) {
				e.printStackTrace();
				echo = null;
			}
		}
	}

	public void write(byte b[], int offset, int length) {
		parent.write(b, offset, length, err);
		if (echo != null) {
			try {
				echo.write(b, offset, length);
				echo.flush();
			} catch (IOException e) {
				e.printStackTrace();
				echo = null;
			}
		}
	}

	public void write(int b) {
		single[0] = (byte) b;
		parent.write(single, 0, 1, err);
		if (echo != null) {
			try {
				echo.write(b);
				echo.flush();
			} catch (IOException e) {
				e.printStackTrace();
				echo = null;
			}
		}
	}
}

/**
 * Buffer updates to the console and output them in batches. For info, see:
 * http://java.sun.com/products/jfc/tsc/articles/text/element_buffer and
 * http://javatechniques.com/public/java/docs/gui/jtextpane-speed-part2.html
 * appendString() is called from multiple threads, and insertAll from the swing
 * event thread, so they need to be synchronized
 */
class BufferedStyledDocument extends DefaultStyledDocument {
	ArrayList<ElementSpec> elements = new ArrayList<ElementSpec>();

	int maxLineLength, maxLineCount;

	int currentLineLength = 0;

	boolean needLineBreak = false;

	boolean hasAppendage = false;

	public BufferedStyledDocument(int maxLineLength, int maxLineCount) {
		this.maxLineLength = maxLineLength;
		this.maxLineCount = maxLineCount;
	}

	/** buffer a string for insertion at the end of the DefaultStyledDocument */
	public synchronized void appendString(String str, AttributeSet a) {
		// do this so that it's only updated when needed (otherwise console
		// updates every 250 ms when an app isn't even running.. see bug 180)
		hasAppendage = true;

		// process each line of the string
		while (str.length() > 0) {
			// newlines within an element have (almost) no effect, so we need to
			// replace them with proper paragraph breaks (start and end tags)
			if (needLineBreak || currentLineLength > maxLineLength) {
				elements.add(new ElementSpec(a, ElementSpec.EndTagType));
				elements.add(new ElementSpec(a, ElementSpec.StartTagType));
				currentLineLength = 0;
			}

			if (str.indexOf('\n') == -1) {
				elements.add(new ElementSpec(a, ElementSpec.ContentType, str
						.toCharArray(), 0, str.length()));
				currentLineLength += str.length();
				needLineBreak = false;
				str = str.substring(str.length()); // eat the string
			} else {
				elements.add(new ElementSpec(a, ElementSpec.ContentType, str
						.toCharArray(), 0, str.indexOf('\n') + 1));
				needLineBreak = true;
				str = str.substring(str.indexOf('\n') + 1); // eat the line
			}
		}
	}

	/** insert the buffered strings */
	public synchronized void insertAll() {
		ElementSpec[] elementArray = new ElementSpec[elements.size()];
		elements.toArray(elementArray);

		try {
			// check how many lines have been used so far
			// if too many, shave off a few lines from the beginning
			Element element = super.getDefaultRootElement();
			int lineCount = element.getElementCount();
			int overage = lineCount - maxLineCount;
			if (overage > 0) {
				// if 1200 lines, and 1000 lines is max,
				// find the position of the end of the 200th line
				// systemOut.println("overage is " + overage);
				Element lineElement = element.getElement(overage);
				if (lineElement == null)
					return; // do nuthin

				int endOffset = lineElement.getEndOffset();
				// remove to the end of the 200th line
				super.remove(0, endOffset);
			}
			super.insert(super.getLength(), elementArray);

		} catch (BadLocationException e) {
			// ignore the error otherwise this will cause an infinite loop
			// maybe not a good idea in the long run?
		}
		elements.clear();
		hasAppendage = false;
	}
}

