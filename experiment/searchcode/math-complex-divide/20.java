package cc.creativecomputing.sound.demo.audiobuffer;

import cc.creativecomputing.CCApp;
import cc.creativecomputing.CCApplicationManager;
import cc.creativecomputing.CCApplicationSettings;
import cc.creativecomputing.graphics.CCGraphics;
import cc.creativecomputing.sound.CCAudioPlayer;
import cc.creativecomputing.sound.CCSoundIO;

/**
 * This sketch is an example of how to use the <code>level</code> method of an <code>AudioBuffer</code> to get the 
 * level of one of an <code>AudioSource</code>'s sample buffers. The classes in Minim that extend <code>AudioSource</code> 
 * and therefore inherit the <code>left</code>, <code>right</code>, and <code>mix</code> buffers of that class, are 
 * <code>AudioInput</code>, <code>AudioOutput</code>, <code>AudioSample</code>, and <code>AudioPlayer</code>. 
 * Not coincidentally, these are also all of the classes in Minim that are <code>Recordable</code>. 
 * <p>
 * The value returned by <code>level</code> will always be between zero and one, but you may find that the value 
 * returned is often smaller than you expect. The level is found by calculating the root-mean-squared amplitude of the 
 * samples in the buffer. First the samples are all squared, then the average (mean) of all the samples is taken (sum
 * and then divide by the number of samples), then the square root of the average is returned. This is why the range can be 
 * determined as [0, 1] because the largest value a squared sample can have is 1. However, in order for the RMS amplitude 
 * to equal 1, every sample must have either -1 or 1 as its value (amplitude). This is only going to be the case 
 * if your sound is a square wave at full amplitude. If your sound is a song or other complex sound source, 
 * the level is generally going to be much lower.
 */
public class CCAudioBufferLevelTest extends CCApp {

	/**
	 * 
	 */
	private static final long serialVersionUID = 0L;

	public CCAudioBufferLevelTest(CCApplicationSettings theSettings) {
		super(theSettings);
	}

	private CCAudioPlayer _myPlayer;

	@Override
	public void setup() {
		CCSoundIO.start();
		_myPlayer = CCSoundIO.loadFile("groove.mp3");
		_myPlayer.loop();
		
		g.rectMode(CCGraphics.CORNERS);
	}

	@Override
	public void draw() {
		g.clear();
		g.beginOrtho2D();
		// draw the current level of the left and right sample buffers
		// level() returns a value between 0 and 1, so we scale it up
		g.rect(0, height, width/2, height - _myPlayer.left.level()*1000);
		g.rect(width/2, height, width, height - _myPlayer.right.level()*1000);
		g.endOrtho2D();
	}

	@Override
	public void finish() {
		// always close Minim audio classes when you finish with them
		  _myPlayer.close();
		  // always stop Minim before exiting
		  CCSoundIO.stop();
	}

	public static void main(String[] args) {
		CCApplicationManager<CCAudioBufferLevelTest> myManager = new CCApplicationManager<CCAudioBufferLevelTest>(CCAudioBufferLevelTest.class);
		myManager.settings().size(500, 500);
		myManager.start();
	}
}

