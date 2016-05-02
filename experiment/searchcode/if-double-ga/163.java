package LD24;

import com.ixee.GraphIX.*;
import java.util.*;
import javax.imageio.ImageIO;
import java.io.*;
import java.awt.Color;
import com.ixee.datastruct.Coordinate;
import java.net.URL;
import java.net.MalformedURLException;
import paulscode.sound.libraries.LibraryLWJGLOpenAL;
import paulscode.sound.SoundSystem;
import paulscode.sound.SoundSystemException;
import paulscode.sound.SoundSystemConfig;
import paulscode.sound.codecs.CodecJOrbis;

public class game{
	static GIXEngine engine;
	static HashMap<String, glEImage> images;
	static final String assetSource = "./assets/";

	static boolean inMenus = true;
	static int menuNumber = 0;
	static int numMenus = 4;
	static final int RETURN = 31;
	static final int ESCAPE = 30;

	static final int timerleft = 3, timertop = -16, timerbottom = -25, width = 74;

	static int level = 0;
	static final int maxLevel = 10;

	static double timePercent = 1f;

	static float basems = 3000;
	static float timeMs = 10000;

	static double oopsTime = 500;
	static double oopsTimeCounter = -1;
	static int cursorPos = 0;

	static int[] currGenome = new int[12], destGenome = new int[12];

	static String[] pairs = new String[]{"at", "ta", "cg", "gc", "ag", "ac", "tg", "tc", "ca", "ct", "ga", "gt"};

	static SoundSystem sounds;

	public static void main(String[] args){
		try{
			boolean sound;
			try{
				SoundSystemConfig.addLibrary(LibraryLWJGLOpenAL.class);
				SoundSystemConfig.setCodec("ogg", CodecJOrbis.class);
				sound = true;
			}catch(SoundSystemException e){
				sound = false;
			}
			sounds = new SoundSystem();
			loadSounds();
			sounds.setLooping("intro", true);
			sounds.setLooping("bgm", true);
			sounds.setLooping("victory", true);

			engine = new GIXEngine("Monkeyectomy: The Game", 160, 144);
			glEEngine g = engine.graphics();
			if(sound){
				sounds.play("intro");
			}
			loadImages();

			double currMs = calcTimeLimit();

			double elapsedTime = 0;
			double frameTime = 0;
			frameTime = System.currentTimeMillis();

			currGenome = new int[6+level/2];

			randomize(currGenome, pairs.length);
			randomize(destGenome, 4);

			while(engine.closeRequested() == false && !GIXVariables.keyPresses[ESCAPE]){
				if(menuNumber < numMenus){
					g.drawImage(images.get("title"+menuNumber));
					g.drawImage(images.get("00"), 64, 64, 80-36, -72+36, 0);
					if(GIXVariables.keyPresses[RETURN] && GIXVariables.diffs[RETURN]){
						menuNumber++;
					}
					if(menuNumber == numMenus){
						if(sound){
							sounds.stop("intro");
							sounds.play("bgm");
						}
					}
				}else if(level < maxLevel){
					g.drawImage(images.get("game"), 0, 0);
					g.drawImage(images.get(level+"0"), 64, 64, 80-40, 72-39, 0);
					g.drawImage(images.get(level+"0%"), 11, 8, 56, -6, 0);
					g.fillRect(new Coordinate(timerleft, timertop), new Coordinate(timerleft+width*timePercent, timerbottom), new Color(153,220,235));

					drawGenome(currGenome, -60);
					g.drawImage(images.get("arrow"), -40, -72+10+cursorPos*11);
					g.drawImage(images.get("border"), -60, -72+10+cursorPos*11);
					drawGenome(destGenome, -20);

					if(timePercent > 0.0){
						if(GIXVariables.keyPresses[22] && GIXVariables.diffs[22]){
							cursorPos = (cursorPos+1)%currGenome.length;
						}
						if(GIXVariables.keyPresses[18] && GIXVariables.diffs[18]){
							cursorPos = (cursorPos+currGenome.length-1)%currGenome.length;
						}
						if(GIXVariables.keyPresses[5] && GIXVariables.diffs[5]){
							if(currGenome[cursorPos] < 4){
								elapsedTime += 2000;
							}else{
								currGenome[cursorPos] = destGenome[cursorPos];
							}
						}

						boolean valid = true;
						for(int i=0;i<currGenome.length;i++){
							valid = valid && (currGenome[i] < 4);
						}
						if(valid){
							level++;
							if(level == maxLevel){
								if(sound){
									sounds.stop("bgm");
									sounds.play("victory");
								}
							}
							elapsedTime = 0;
							currMs = calcTimeLimit();

							currGenome = new int[8+level/2];
							cursorPos = 0;
							randomize(currGenome, pairs.length);
							randomize(destGenome, 4);
						}
					}

					elapsedTime += System.currentTimeMillis()-frameTime;
					timePercent = 1-(elapsedTime/currMs);
					if(timePercent <= 0.0){
						timePercent = 0.0;

						g.drawImage(images.get("oops"), 64, 64, 80-40, 72-39, 0);
						oopsTimeCounter += System.currentTimeMillis()-frameTime;

						if(oopsTimeCounter >= oopsTime){
							level = Math.max(0, level-1);
							currMs = calcTimeLimit();
							elapsedTime = 0;
							oopsTimeCounter = 0;

							currGenome = new int[6+level/2];
							cursorPos = 0;
							randomize(currGenome, pairs.length);
							randomize(destGenome, 4);
						}
					}
				}else{
					g.drawImage(images.get("victory"));
					g.drawImage(images.get("90"), 64, 64, 80-32, 72-48, 0);
				}
				frameTime = System.currentTimeMillis();
				engine.update();
				try{
					Thread.sleep(16);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
			exit();
		}catch(Exception e){
			e.printStackTrace();
			exit();
		}
	}
	public static void loadSounds(){
		try{
			sounds.newSource(false, "intro", new URL("file:"+assetSource+"intro.ogg"), assetSource+"intro.ogg", false, 0f, 0f, -10f, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
			sounds.newSource(false, "bgm", new URL("file:"+assetSource+"ld24.ogg"), assetSource+"ld24.ogg", false, 0f, 0f, -10f, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
			sounds.newSource(false, "victory", new URL("file:"+assetSource+"victory.ogg"), assetSource+"victory.ogg", false, 0f, 0f, -10f, SoundSystemConfig.ATTENUATION_ROLLOFF, SoundSystemConfig.getDefaultRolloff());
		}catch(MalformedURLException e){
			e.printStackTrace();
		}
	}
	public static void exit(){
		try{
			engine.graphics().end();
		}catch(NullPointerException npe){

		}
		try{
			sounds.stop("victory");
		}catch(NullPointerException npe){
		}
		System.exit(0);
	}
	public static double calcTimeLimit(){
		return basems + (1-(double)level/(double)maxLevel)*timeMs;
	}
	public static void drawGenome(int[] genome, int x){
		for(int i=0;i<genome.length;i++){
			int y = -72+10+i*11;
			if(genome[i] >= 4){
				engine.graphics().fillRect(new Coordinate(x - 13, y-5), new Coordinate(x+12, y+6), new Color(205, 16, 26));
			}else{
				engine.graphics().fillRect(new Coordinate(x - 13, y-5), new Coordinate(x+12, y+6), new Color(34, 177, 76));
			}
			engine.graphics().drawImage(images.get(pairs[genome[i]]), x, y);
		}
	}
	public static void randomize(int[] dest, int len){
		Random rand = new Random(System.nanoTime());
		for(int i=0;i<dest.length;i++){
			dest[i] = rand.nextInt(len);
		}
	}
	public static void loadImages(){
		String[][] assets = new String[][]{
			{"title0", "title.png"},
			{"title1", "title1.png"},
			{"title2", "title2.png"},
			{"title3", "title3.png"},
			{"title4", "title4.png"},
			{"ac", "ac.png"},
			{"ag", "ag.png"},
			{"at", "at.png"},
			{"ca", "ca.png"},
			{"cg", "cg.png"},
			{"ct", "ct.png"},
			{"ga", "ga.png"},
			{"gc", "gc.png"},
			{"gt", "gt.png"},
			{"ta", "ta.png"},
			{"tc", "tc.png"},
			{"tg", "tg.png"},
			{"00", "00.png"},
			{"10", "10.png"},
			{"20", "20.png"},
			{"30", "30.png"},
			{"40", "40.png"},
			{"50", "50.png"},
			{"60", "60.png"},
			{"70", "70.png"},
			{"80", "80.png"},
			{"90", "90.png"},
			{"00%", "00p.png"},
			{"10%", "10p.png"},
			{"20%", "20p.png"},
			{"30%", "30p.png"},
			{"40%", "40p.png"},
			{"50%", "50p.png"},
			{"60%", "60p.png"},
			{"70%", "70p.png"},
			{"80%", "80p.png"},
			{"90%", "90p.png"},
			{"game", "game.png"},
			{"arrow", "arrow.png"},
			{"border", "border.png"},
			{"victory", "victory.png"},
			{"oops", "monkeyradiation.png"}
		};
		images = new HashMap<String, glEImage>();
		for(int i=0;i<assets.length;i++){
			try{
				glEImage temp = engine.loadImage(ImageIO.read(new File(assetSource+assets[i][1])));
				images.put(assets[i][0], temp);
			}catch(Exception e){
				System.out.println("Error loading file "+i+", "+assets[i][1]);
				e.printStackTrace();
			}
		}

	}
}
