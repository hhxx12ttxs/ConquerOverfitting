/*
*
* Copyright (C) 2011-2014 Wang Shiliang
* All rights reserved
* filename : FireRescueGamming.java
* description : The main class for running the game                               
* 
* created by Wang Shiliang at 6/2/2012 21:19:50
*
*/
package org.seedsofempowerment.firerescue;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Random;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.os.Environment;
import android.util.Log;
import android.view.MotionEvent;

public class FireRescueGamming {
	private Bitmap difficultyBackGround;
	private Bitmap[] difficultyButton;
	private Bitmap backGround;
	private int[] difficultyX;
	private int[] difficultyY;
	private Bitmap[] firemanPicture;
	private Bitmap[] arrows;
	private Bitmap[] toolspic;
	private Bitmap[] firepic;
	private Bitmap[] waterpic;
	private Bitmap[] ladderpic;
	private Bitmap jumpSpringpic;
	private Bitmap blood;
	private Bitmap[] objectspic;
	private Bitmap waterNumber;
	private int[] toolSelection;
	private int bloodX;
	private int bloodY;
	private int waterX;
	private int waterY;
	private int difficultyChoose;
	private FileOutputStream fos;
	private DataOutputStream dos;
	private FileInputStream fis;
	private DataInputStream dis;
	public static Fireman fireman;
	private Button[] buttons;
	private Tool[] tools;
	private Water water;
	private Fire[] fire;
	private Object[] objects;
	private int direction;
	private int choose;
	private int gameState;
	private Typeface font;
	private Bitmap doorPicture;
	private Bitmap[] savePicture;
	private Door door;
	private int[] savePictureX;
	private int[] savePictureY;
	public static Ladder[] ladder;
	public static int[] ladderLevel;
	public static int[] jumpSpringLevel;
	public static int ladderCurrentNumber;
	public static int jumpSpringCurrentNumber;
	private int climbLadderIndex;
	public static int[] ladderY;
	public static int[] jumpSpringY;
	public static JumpSpring[] jumpSpring;
	private int touchwatertool;
	public final static int SELECT_DIFFICULTY = 0;
	public final static int GAMMING = 1;
	public final static int GAMESTARTREMIND = 2;
	private int firestate;
	private int showSaveTime;
	private int[] levelpositionY;
	private int changebackimg;
	private int changeObject;
	private int[] firePositionY;
	private int[] downLadderPositionY;
	private int fireindex;
	private boolean successfullySaved;
	private boolean failSaved;
	private int tempLife;
	private int tempWater;
	private Random rand;
	public static boolean isTimeArrived;

	public FireRescueGamming(Bitmap difficultyBackGround,
			Bitmap[] difficultyButton, Bitmap backGround,
			Bitmap[] firemanPicture, Bitmap[] arrows, Bitmap[] toolspicture,
			Bitmap[] fireimg, Bitmap[] waterimg, Bitmap[] ladderimg,
			Typeface font, Bitmap doorPicture, Bitmap[] savePicture,
			Bitmap blood, Bitmap waterNumber, Bitmap[] objects,
			Bitmap jumpSpringImg) {
		this.difficultyBackGround = difficultyBackGround;
		this.difficultyButton = difficultyButton;
		this.backGround = backGround;
		this.firemanPicture = firemanPicture;
		this.arrows = arrows;
		this.toolspic = toolspicture;
		this.firepic = fireimg;
		this.waterpic = waterimg;
		this.font = font;
		this.ladderpic = ladderimg;
		this.jumpSpringpic = jumpSpringImg;
		this.doorPicture = doorPicture;
		this.savePicture = savePicture;
		this.blood = blood;
		this.waterNumber = waterNumber;
		this.objectspic = objects;
		successfullySaved = false;
		failSaved = false;
		isTimeArrived = false;
		fireindex = 0;
		showSaveTime = 0;
		ladderCurrentNumber = 0;
		jumpSpringCurrentNumber = 0;
		difficultyChoose = 0;
		gameState = SELECT_DIFFICULTY;
		difficultyX = new int[4];
		for (int i = 0; i != 4; ++i) {
			difficultyX[i] = PhoneInfo.getRealWidth(260);
		}
		difficultyY = new int[4];
		difficultyY[0] = PhoneInfo.getRealHeight(40);
		difficultyY[1] = PhoneInfo.getRealHeight(110);
		difficultyY[2] = PhoneInfo.getRealHeight(180);
		difficultyY[3] = PhoneInfo.getRealHeight(250);
		rand = new Random();
		initiateButton();
		initiateTool();
		toolSelection = new int[2];

		for (int i = 0; i != 2; ++i) {
			toolSelection[i] = 0;
		}

		ladderY = new int[4];
		ladderY[0] = PhoneInfo.getRealHeight(333); // 1
		ladderY[1] = PhoneInfo.getRealHeight(205);// 1
		ladderY[2] = PhoneInfo.getRealHeight(75);// 2
		ladderY[3] = PhoneInfo.getRealHeight(0);// 3

		jumpSpringY = new int[3];
		jumpSpringY[0] = PhoneInfo.getRealHeight(328);
		jumpSpringY[1] = PhoneInfo.getRealHeight(200);
		jumpSpringY[2] = PhoneInfo.getRealHeight(70);

		firePositionY = new int[3];
		firePositionY[0] = PhoneInfo.getRealHeight(318);// 1
		firePositionY[1] = PhoneInfo.getRealHeight(190);// 1
		firePositionY[2] = PhoneInfo.getRealHeight(60);// 1

		levelpositionY = new int[2];
		levelpositionY[0] = PhoneInfo.getRealHeight(245);
		levelpositionY[1] = PhoneInfo.getRealHeight(80);

		bloodX = PhoneInfo.getRealWidth(98);
		bloodY = PhoneInfo.getRealHeight(0);
		waterX = PhoneInfo.getRealWidth(80);
		waterY = PhoneInfo.getRealHeight(20);

		downLadderPositionY = new int[3];
		downLadderPositionY[0] = PhoneInfo.getRealHeight(130)
				- PhoneInfo.getFigureHeight(ladderimg[0].getHeight());
		downLadderPositionY[1] = PhoneInfo.getRealHeight(260)
				- PhoneInfo.getFigureHeight(ladderimg[1].getHeight());
		downLadderPositionY[2] = PhoneInfo.getRealHeight(388)
				- PhoneInfo.getFigureHeight(ladderimg[2].getHeight());
		savePictureX = new int[2];
		savePictureY = new int[2];
		for (int i = 0; i != 2; ++i) {
			savePictureX[i] = (PhoneInfo.resolutionWidth - PhoneInfo
					.getFigureWidth(savePicture[i].getWidth())) / 2;
			savePictureY[i] = (PhoneInfo.resolutionHeight - PhoneInfo
					.getFigureHeight(savePicture[i].getHeight())) / 2;
		}

		if (GameInfo.isLoad == true) {
			boolean isHaveSDCard = false;
			if (Environment.getExternalStorageState() != null
					&& !Environment.getExternalStorageState().equals("removed")) {
				Log.v("wang", "read from files");
				isHaveSDCard = true;
			}
			try {
				if (isHaveSDCard) {
					File path = new File("/sdcard/FireRescue/save");
					File file = new File("/sdcard/FireRescue/save/"
							+ GameInfo.name + ".txt");
					if (path.exists() && file.exists()) {
						fis = new FileInputStream(file);
					}
				} else {
					if (GameEngineActivity.instance.openFileInput(GameInfo.name
							+ ".txt") != null) {
						fis = GameEngineActivity.instance
								.openFileInput(GameInfo.name + ".txt");
					}
				}
				dis = new DataInputStream(fis);
				// load the info of GameInfo
				String name = dis.readLine();
				GameInfo.age = dis.readInt();
				GameInfo.gender = dis.readInt();
				GameInfo.difficulty = dis.readInt();
				GameInfo.roundStartTime = System.currentTimeMillis();
				GameInfo.roundTime = dis.readLong();
				GameInfo.remainPeople = dis.readInt();
				GameInfo.ladderNumber = dis.readInt();
				GameInfo.jumpSpringNumber = dis.readInt();
				GameInfo.round = dis.readInt();
				GameInfo.firstStart = dis.readBoolean();
				GameInfo.remainTimes = dis.readInt();
				tempLife = dis.readInt();
				tempWater = dis.readInt();
				initLoadGame();
				remindfire(0);
				gameState = GAMMING;
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fis != null) {
						fis.close();
					}
					if (dis != null) {
						dis.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void initiateButton() {
		buttons = new Button[4];

		Bitmap[][] temp = new Bitmap[4][2];
		temp[0][0] = arrows[1];
		temp[0][1] = arrows[5];
		buttons[0] = new Button(temp[0]);
		temp[1][0] = arrows[2];
		temp[1][1] = arrows[6];
		buttons[1] = new Button(temp[1]);
		temp[2][0] = arrows[3];
		temp[2][1] = arrows[7];
		buttons[2] = new Button(temp[2]);
		temp[3][0] = arrows[4];
		temp[3][1] = arrows[8];
		buttons[3] = new Button(temp[3]);

		// set the button position
		buttons[0].setPositionX(PhoneInfo.getRealWidth(0));
		buttons[0].setPositionY(PhoneInfo.getRealHeight(354));
		buttons[1].setPositionX(PhoneInfo.getRealWidth(60));
		buttons[1].setPositionY(PhoneInfo.getRealHeight(300));
		buttons[2].setPositionX(PhoneInfo.getRealWidth(120));
		buttons[2].setPositionY(PhoneInfo.getRealHeight(354));
		buttons[3].setPositionX(PhoneInfo.getRealWidth(60));
		buttons[3].setPositionY(PhoneInfo.getRealHeight(412));

		// set the button state
		buttons[0].setState(Button.UNPRESS);
		buttons[1].setState(Button.UNPRESS);
		buttons[2].setState(Button.UNPRESS);
		buttons[3].setState(Button.UNPRESS);
	}

	public void initiateTool() {
		tools = new Tool[4];
		Bitmap[][] temp = new Bitmap[4][2];

		temp[0][0] = toolspic[1];
		temp[0][1] = toolspic[5];
		tools[0] = new Tool(temp[0]);

		temp[1][0] = toolspic[2];
		temp[1][1] = toolspic[6];
		tools[1] = new Tool(temp[1]);

		temp[2][0] = toolspic[3];
		temp[2][1] = toolspic[7];
		tools[2] = new Tool(temp[2]);

		temp[3][0] = toolspic[4];
		temp[3][1] = toolspic[8];
		tools[3] = new Tool(temp[3]);

		tools[0].setState(Tool.UNPRESS);
		tools[1].setState(Tool.UNPRESS);
		tools[2].setState(Tool.UNPRESS);
		tools[3].setState(Tool.UNPRESS);

		tools[0].setPositionX(PhoneInfo.getRealWidth(480));
		tools[0].setPositionY(PhoneInfo.getRealHeight(390));
		tools[1].setPositionX(PhoneInfo.getRealWidth(560));
		tools[1].setPositionY(PhoneInfo.getRealHeight(390));
		tools[2].setPositionX(PhoneInfo.getRealWidth(640));
		tools[2].setPositionY(PhoneInfo.getRealHeight(390));
		tools[3].setPositionX(PhoneInfo.getRealWidth(720));
		tools[3].setPositionY(PhoneInfo.getRealHeight(390));
	}

	public void onTouchEvent(MotionEvent event) {
		// get the point where user touch
		int pointX = (int) event.getX();
		int pointY = (int) event.getY();
		if (gameState == SELECT_DIFFICULTY) {
			if (pointX >= difficultyX[0]
					&& pointX <= difficultyX[0]
							+ PhoneInfo.getFigureWidth(difficultyButton[0]
									.getWidth())
					&& pointY >= difficultyY[0]
					&& pointY <= difficultyY[0]
							+ PhoneInfo.getFigureHeight(difficultyButton[0]
									.getHeight())) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					GameInfo.vibrate.playVibrate(-1);
					difficultyChoose = 1;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					GameInfo.difficulty = GameInfo.TRAINEE;
					gameState = GAMESTARTREMIND;
					GameInfo.round = 1;
					GameInfo.roundStartTime = System.currentTimeMillis();
				}
			} else if (pointX >= difficultyX[1]
					&& pointX <= difficultyX[1]
							+ PhoneInfo.getFigureWidth(difficultyButton[1]
									.getWidth())
					&& pointY >= difficultyY[1]
					&& pointY <= difficultyY[1]
							+ PhoneInfo.getFigureHeight(difficultyButton[1]
									.getHeight())) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					GameInfo.vibrate.playVibrate(-1);
					difficultyChoose = 2;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					GameInfo.difficulty = GameInfo.FIREFIGHTER;
					gameState = GAMESTARTREMIND;
					GameInfo.roundStartTime = System.currentTimeMillis();
				}
			} else if (pointX >= difficultyX[2]
					&& pointX <= difficultyX[2]
							+ PhoneInfo.getFigureWidth(difficultyButton[2]
									.getWidth())
					&& pointY >= difficultyY[2]
					&& pointY <= difficultyY[2]
							+ PhoneInfo.getFigureHeight(difficultyButton[2]
									.getHeight())) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					GameInfo.vibrate.playVibrate(-1);
					difficultyChoose = 3;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					GameInfo.difficulty = GameInfo.CAPTAIN;
					gameState = GAMESTARTREMIND;
					GameInfo.roundStartTime = System.currentTimeMillis();
				}
			} else if (pointX >= difficultyX[3]
					&& pointX <= difficultyX[3]
							+ PhoneInfo.getFigureWidth(difficultyButton[3]
									.getWidth())
					&& pointY >= difficultyY[3]
					&& pointY <= difficultyY[3]
							+ PhoneInfo.getFigureHeight(difficultyButton[3]
									.getHeight())) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					GameInfo.vibrate.playVibrate(-1);
					difficultyChoose = 4;
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					GameInfo.difficulty = GameInfo.CHIEF;
					gameState = GAMESTARTREMIND;
					GameInfo.roundStartTime = System.currentTimeMillis();
				}
			}
		}

		else if (gameState == GAMMING) {
			int direction = 4;
			for (int i = 0; i != 4; ++i) {
				if (buttons[i].isPositionBelong(pointX, pointY)) {
					direction = i;
					break;
				}
			}
			if (event.getAction() == MotionEvent.ACTION_DOWN) {
				if (direction != 4) {
					GameInfo.vibrate.playVibrate(-1);
					if (direction == Button.LEFT) {
						if (fireman.getClimblevelstate() == 1
								|| fireman.getJumplevelstate() == 1) {
							fireman.setDirection(Fireman.MOTIONLESS);
						} else if (fireman.isCollideState == true) {
							fireman.setDirection(Fireman.MOTIONLESS);
						} else {
							fireman.setOrientation(Fireman.LEFT_DIRECTION);
							fireman.setDirection(direction);
						}
					} else if (direction == Button.RIGHT) {
						if (fireman.getClimblevelstate() == 1
								|| fireman.getJumplevelstate() == 1) {
							fireman.setDirection(Fireman.MOTIONLESS);
						} else if (fireman.isCollideState == true) {
							fireman.setDirection(Fireman.MOTIONLESS);
						} else {
							fireman.setOrientation(Fireman.RIGHT_DIRECTION);
							fireman.setDirection(direction);
						}
					}

					else if (direction == Button.UP) {
						int level = 0;
						fireman.isJumpState = true;
						if (isLadderClimb() == true) {
							if (fireman.getLevel() == ladder[climbLadderIndex]
									.getEndLevel()) {
								level = ladder[climbLadderIndex]
										.getStartLevel();
							} else if (fireman.getLevel() == ladder[climbLadderIndex]
									.getStartLevel()) {
								level = ladder[climbLadderIndex].getEndLevel();
							}
							if (level > fireman.getLevel()) {
								fireman.setClimblevelstate(1);
								fireman.setOrientation(Fireman.LEFT_DIRECTION);
								fireman.setDirection(direction);
							}
							fireman.isJumpState = false;
						}

						if (isJumpSpringClimb() == true) {
							if (fireman.getLevel() == jumpSpring[jumpSpringCurrentNumber - 1]
									.getEndLevel()) {
								level = jumpSpring[jumpSpringCurrentNumber - 1]
										.getStartLevel();
							} else if (fireman.getLevel() == jumpSpring[jumpSpringCurrentNumber - 1]
									.getStartLevel()) {
								level = jumpSpring[jumpSpringCurrentNumber - 1]
										.getEndLevel();
							}
							if (level > fireman.getLevel()) {
								if (fireman.getJumplevelstate() == 0) {
									fireman.setPositionY(fireman.getPositionY()
											- PhoneInfo.getRealHeight(58));
								}
								fireman.setJumplevelstate(1);
								fireman.setOrientation(Fireman.LEFT_DIRECTION);
							}
							fireman.isJumpState = false;
						}
					} 
					
					else if (direction == Button.DOWN) {
						int level = 0;
						if (isLadderClimb() == true) {
							if (fireman.getLevel() == ladder[climbLadderIndex].getEndLevel()) {
								level = ladder[climbLadderIndex].getStartLevel();
							} 
							
							else if (fireman.getLevel() == ladder[climbLadderIndex].getStartLevel()) {
								level = ladder[climbLadderIndex].getEndLevel();
							}
							if (level < fireman.getLevel()) {
								fireman.setClimblevelstate(1);
								fireman.setOrientation(Fireman.LEFT_DIRECTION);
								fireman.setDirection(direction);
							}
							fireman.isJumpState = false;
						}

						if (isJumpSpringClimb() == true) {
							if (fireman.getLevel() == jumpSpring[jumpSpringCurrentNumber - 1]
									.getEndLevel()) {
								level = jumpSpring[jumpSpringCurrentNumber - 1]
										.getStartLevel();
							} else if (fireman.getLevel() == jumpSpring[jumpSpringCurrentNumber - 1]
									.getStartLevel()) {
								level = jumpSpring[jumpSpringCurrentNumber - 1]
										.getEndLevel();
							}
							if (level < fireman.getLevel()) {
								if (fireman.getJumplevelstate() == 0) {
									fireman.setPositionY(fireman.getPositionY()
											- PhoneInfo.getRealHeight(58));
								}
								fireman.setJumplevelstate(1);
								fireman.setOrientation(Fireman.LEFT_DIRECTION);
							}
							fireman.isJumpState = false;
						}
					}
					buttons[direction].setState(Button.PRESS);
				} else {
					fireman.setDirection(Fireman.MOTIONLESS);
					// choose water gun
					if (pointX >= tools[0].getPositionX()
							&& pointX <= tools[0].getPositionX()
									+ tools[0].getWidth()
							&& pointY >= tools[0].getPositionY()
							&& pointY <= tools[0].getPositionY()
									+ tools[0].getHeight()) {
						choose = 1;
						touchwatertool = 1;
						if (fireman.getWater() > 0) {
							if (fireman.getOrientation() == Fireman.RIGHT_DIRECTION) {
								water.setPositionX(fireman.getPositionX()
										+ fireman.getWidth());
								water.setPositionY(fireman.getPositionY()
										+ fireman.getHeight()
										- water.getHeight());
								water.setDirection(Water.RIGHT_DIRECTION);
								water.setIndex(0);
							} else {
								water.setPositionX(fireman.getPositionX()
										- water.getWidth());
								water.setPositionY(fireman.getPositionY()
										+ fireman.getHeight()
										- water.getHeight());
								water.setDirection(Water.LEFT_DIRECTION);
								water.setIndex(2);
							}
							GameInfo.soundEffect[1].play((float) 1.0);
							fireman.setWater(fireman.getWater() - 1);
						}
					}
				}
			} else if (event.getAction() == MotionEvent.ACTION_UP) {
				fireman.setDirection(Fireman.MOTIONLESS);
				for (int i = 0; i != 4; ++i) {
					buttons[i].setState(Button.UNPRESS);
				}
				choose = 0;
				// when the user choose the ladder
				if (fireman.getClimblevelstate() == 0
						&& GameInfo.isLadderSurfaceView == false) {
					if (pointX >= tools[1].getPositionX()
							&& pointX <= tools[1].getPositionX()
									+ tools[1].getWidth()
							&& pointY >= tools[1].getPositionY()
							&& pointY <= tools[1].getPositionY()
									+ tools[1].getHeight()) {
						if (GameInfo.ladderNumber > 0) {
							ladder[ladderCurrentNumber]
									.setPositionX(fireman.getPositionX()
											- (ladder[ladderCurrentNumber]
													.getWidth() - fireman
													.getWidth()) / 2);
							if (ladder[ladderCurrentNumber].getPositionX() < 0) {
								ladder[ladderCurrentNumber].setPositionX(0);
							} else if (ladder[ladderCurrentNumber]
									.getPositionX()
									+ ladder[climbLadderIndex].getWidth() > PhoneInfo.resolutionWidth) {
								ladder[ladderCurrentNumber]
										.setPositionX(ladder[climbLadderIndex]
												.getPositionX()
												- ladder[climbLadderIndex]
														.getWidth());
							}
						}
						if (GameInfo.jumpSpringNumber > 0) {
							jumpSpring[jumpSpringCurrentNumber]
									.setPositionX(fireman.getPositionX()
											- (jumpSpring[ladderCurrentNumber]
													.getWidth() - fireman
													.getWidth()) / 2);
							if (jumpSpring[jumpSpringCurrentNumber]
									.getPositionX() < 0) {
								jumpSpring[jumpSpringCurrentNumber]
										.setPositionX(0);
							} else if (jumpSpring[jumpSpringCurrentNumber]
									.getPositionX()
									+ jumpSpring[jumpSpringCurrentNumber]
											.getWidth() > PhoneInfo.resolutionWidth) {
								jumpSpring[jumpSpringCurrentNumber]
										.setPositionX(ladder[jumpSpringCurrentNumber]
												.getPositionX()
												- ladder[climbLadderIndex]
														.getWidth());
							}
						}
						Intent intent = new Intent(GameEngineActivity.instance,
								SelectLadderActivity.class);
						intent.putExtra("currentLevel", fireman.getLevel());
						GameEngineActivity.instance.startActivity(intent);
						GameInfo.isLadderSurfaceView = true;
					} else {
						choose = 0;
					}
				}

				// when the user press the return button
				if (pointX >= tools[2].getPositionX()
						&& pointX <= tools[2].getPositionX()
								+ tools[2].getWidth()
						&& pointY >= tools[2].getPositionY()
						&& pointY <= tools[2].getPositionY()
								+ tools[2].getHeight()) {
					if (GameInfo.returnNumber > 0) {
						Intent intent = new Intent(GameEngineActivity.instance,
								ReturnStartLevelActivity.class);
						GameEngineActivity.instance.startActivity(intent);
					} else {
						;
					}
				}

				// when the user press the save function
				if (pointX >= tools[3].getPositionX()
						&& pointX <= tools[3].getPositionX()
								+ tools[3].getWidth()
						&& pointY >= tools[3].getPositionY()
						&& pointY <= tools[3].getPositionY()
								+ tools[3].getHeight()) {
					GameSurfaceView.gameState = GameSurfaceView.SAVE_GAME;
					try {
						// judge whether exist the SD card
						if (Environment.getExternalStorageState() != null
								&& !Environment.getExternalStorageState()
										.equals("removed")) {
							Log.v("wang", "exist sd card");
							// declare the path
							File path = new File("/sdcard/FireRescue/save");
							if (!path.exists()) {
								path.mkdirs();
							}
							// create the store file
							File file = new File("/sdcard/FireRescue/save/"
									+ GameInfo.name + ".txt");
							if (!file.exists()) {
								file.createNewFile();
							}
							fos = new FileOutputStream(file, false);
						}

						else {
							fos = GameEngineActivity.instance.openFileOutput(
									GameInfo.name + ".txt",
									Context.MODE_PRIVATE);
						}
						dos = new DataOutputStream(fos);
						// save the info of GameInfo
						dos.writeChars(GameInfo.name);
						dos.writeChar('\n');
						dos.writeInt(GameInfo.age);
						dos.writeInt(GameInfo.gender);
						dos.writeInt(GameInfo.difficulty);
						dos.writeLong(GameInfo.roundTime);
						dos.writeInt(GameInfo.remainPeople);
						dos.writeInt(GameInfo.ladderNumber);
						dos.writeInt(GameInfo.jumpSpringNumber);
						dos.writeInt(GameInfo.round);
						dos.writeBoolean(GameInfo.firstStart);
						dos.writeInt(GameInfo.remainTimes);
						// save the fireman info
						dos.writeInt(fireman.getLife());
						dos.writeInt(fireman.getWater());
						successfullySaved = true;
						GameSurfaceView.gameState = GameSurfaceView.GAMMING;
					} catch (FileNotFoundException e) {
						e.printStackTrace();
						failSaved = true;
						GameSurfaceView.gameState = GameSurfaceView.GAMMING;
					} catch (IOException e) {
						e.printStackTrace();
						failSaved = true;
						GameSurfaceView.gameState = GameSurfaceView.GAMMING;
					} finally {
						try {
							if (fos != null)
								fos.close();
							if (dos != null)
								dos.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	public void logic() {
		if (gameState == GAMESTARTREMIND) {
			if (GameInfo.isLoad == false) {
				remindfire(0);
				initNewGame();
				gameState = GAMMING;
			}
		} else if (gameState == GAMMING) {
			// if the time arrives
			long currentTime = System.currentTimeMillis();
			long time = (currentTime - GameInfo.roundStartTime) / 1000;
			if (GameInfo.roundTime - time < 0 && isTimeArrived == false) {
				Intent intent = new Intent(GameEngineActivity.instance,
						TimeArriveActivity.class);
				GameEngineActivity.instance.startActivity(intent);
				isTimeArrived = true;
			}

			// whether the fireman has arrived at the door
			if (fireman.getLevel() == GameInfo.doorLevel) {
				if (fireman.getPositionX() >= door.getPositionX()) {
					if (fireman.getPositionX() - door.getPositionX() <= door
							.getWidth()) {
						fireman.setAccrosslevel(1);
					}
				} else {
					if (door.getPositionX() - fireman.getPositionX() <= fireman
							.getWidth()) {
						fireman.setAccrosslevel(1);
					}
				}
			}

			// if the fireman get the object
			for (int i = 0; i != 3; ++i) {
				int level = i;
				if (fireman.getCurrentLevel() == i) {
					if (changeObject == 1) {
						level += 3;
					}
					if (objects[level].isExist()) {
						if (fireman.getPositionX() >= objects[level]
								.getPositionX()) {
							if (fireman.getPositionX()
									- objects[level].getPositionX() <= objects[level]
									.getWidth()) {
								if (objects[level].getColor() == Object.GREEN) {
									GameInfo.roundTime += 60;
								} else if (objects[level].getColor() == Object.PURPLE) {
									++(GameInfo.returnNumber);
								} else if (objects[level].getColor() == Object.RED) {
									fireman.setLife(fireman.getLife() + 1);
									if (fireman.getLife() > 5) {
										fireman.setLife(5);
									}
								} else if (objects[level].getColor() == Object.WHITE) {
									fireman.setWater(fireman.getWater() + 2);
									if (fireman.getWater() > 6) {
										fireman.setWater(6);
									}
								} else if (objects[level].getColor() == Object.YELLOW) {
									++(GameInfo.randomLadderNumber);
								}
								objects[level].setNoneExist();
								GameInfo.soundEffect[4].play((float) 1.0);
							}
						} else {
							if (objects[level].getPositionX()
									- fireman.getPositionX() <= fireman
									.getWidth()) {
								if (objects[level].getColor() == Object.GREEN) {
									GameInfo.roundTime += 60;
								} else if (objects[level].getColor() == Object.PURPLE) {
									++(GameInfo.returnNumber);
								} else if (objects[level].getColor() == Object.RED) {
									fireman.setLife(fireman.getLife() + 1);
									if (fireman.getLife() > 5) {
										fireman.setLife(5);
									}
								} else if (objects[level].getColor() == Object.WHITE) {
									fireman.setWater(fireman.getWater() + 2);
									if (fireman.getWater() > 6) {
										fireman.setWater(6);
									}
								} else if (objects[level].getColor() == Object.YELLOW) {
									++(GameInfo.randomLadderNumber);
								}
								objects[level].setNoneExist();
								GameInfo.soundEffect[4].play((float) 1.0);
							}
						}
					}
				}
			}

			for (int i = 0; i != 3; ++i) {
				if (fireman.getCurrentLevel() == fire[i].getLevel()) {
					fireindex = i;
					break;
				}
			}

			if (touchwatertool == 1 && fireman.getClimblevelstate() == 0) {
				if (water.getPositionX() >= fire[fireindex].getPositionX()) {
					if (water.getPositionX() - fire[fireindex].getPositionX() <= fire[fireindex]
							.getWidth()) {
						firestate = 1;
						fire[fireindex].setFirestate(firestate);
					}
				} 
				else {
					if (fire[fireindex].getPositionX() - water.getPositionX() <= water
							.getWidth()) {
						firestate = 1;
						fire[fireindex].setFirestate(firestate);
					}
				}
			}

			for (int i = 0; i != 3; ++i) {
				if (fire[i].getFirestate() == 1) {
					fire[i].setPositionX(PhoneInfo.getRealWidth(-300));
				} else {
					fire[i].logic();
					// collide event
					if (fireman.isCollideState == false
							&& fireman.getClimblevelstate() == 0
							&& fireman.getJumplevelstate() == 0) {
						if (fire[fireindex].getDirection() == Fire.RIGHT_DIRECTION) {
							if (fire[fireindex].getPositionX()
									+ fire[fireindex].getWidth() / 2 >= fireman
									.getPositionX()
									&& fire[fireindex].getPositionX()
											+ fire[fireindex].getWidth() / 2 <= fireman
											.getPositionX()
											+ fireman.getWidth()
									&& fire[fireindex].getPositionY()
											- fireman.getPositionY() < PhoneInfo
											.getRealHeight(10)) {
								fireman.isCollideState = true;
								GameInfo.soundEffect[3].play((float) 1.0);
								if (fireman.getLife() > 0) {
									fireman.setLife(fireman.getLife() - 1);
								} else {
									Intent intent = new Intent(
											GameEngineActivity.instance,
											TimeArriveActivity.class);
									GameEngineActivity.instance
											.startActivity(intent);
								}
								direction = 1;
							}
						} else if (fire[fireindex].getDirection() == Fire.LEFT_DIRECTION) {
							if (fire[fireindex].getPositionX() <= fireman
									.getPositionX()
									+ fireman.getWidth()
									- fire[fireindex].getWidth() / 2
									&& fire[fireindex].getPositionX()
											+ fire[fireindex].getWidth() / 2 >= fireman
											.getPositionX()
									&& fire[fireindex].getPositionY()
											- fireman.getPositionY() < PhoneInfo
											.getRealHeight(10)) {
								fireman.isCollideState = true;
								GameInfo.soundEffect[3].play((float) 1.0);
								if (fireman.getLife() > 0) {
									fireman.setLife(fireman.getLife() - 1);
								} else {
									Intent intent = new Intent(
											GameEngineActivity.instance,
											TimeArriveActivity.class);
									GameEngineActivity.instance
											.startActivity(intent);
								}
								direction = 0;
							}
						}
					}
				}
			}
			if (fireman.isCollideState == true) {
				fireman.parabolaMove(direction);
			} else {
				if (fireman.isJumpState == true) {
					fireman.jumpMove();
				}
				fireman.logic();
			}
			// water event
			if (choose == 1) {
				water.logic();
			}

			if (fireman.getJumplevelstate() == 1) {
				jumpSpring[jumpSpringCurrentNumber - 1].logic();
			}

			// when the fireman is climbing the ladder
			if (fireman.getClimblevelstate() == 1) {
				int ladderlevelsize = 0;
				if (ladder[climbLadderIndex].getStartLevel() == fireman
						.getLevel()) {
					ladderlevelsize = ladder[climbLadderIndex].getEndLevel()
							- fireman.getLevel();
				} else if (ladder[climbLadderIndex].getEndLevel() == fireman
						.getLevel()) {
					ladderlevelsize = ladder[climbLadderIndex].getStartLevel()
							- fireman.getLevel();
				}
				// the state when the fireman is climbing the ladder
				// if the fireman is climbing up
				if (ladderlevelsize > 0) {
					if (fireman.getCurrentLevel() == 0) {
						if (ladderlevelsize < 3) {
							// if the fireman reach the end
							if (fireman.getPositionY() <= ladderY[ladderlevelsize]) {
								fireman.setPositionY(ladderY[ladderlevelsize]);
								fireman.setClimblevelstate(0);
								fireman.setDirection(Fireman.MOTIONLESS);
								fireman.setCurrentLevel(ladderlevelsize);
								fireman.setLevel(fireman.getLevel()
										+ ladderlevelsize);
								GameInfo.endlevel = fireman.getLevel();
								GameInfo.calculation += " + ";
								GameInfo.calculation += ladderlevelsize;
								if(GameInfo.isrescuing != 3){
									remindfire(1);
									GameInfo.isrescuing = 3;
								}
							}
						} else {
							if (changebackimg == 0) {
								if (fireman.getPositionY() <= 0) {
									fireman.setPositionY(PhoneInfo
											.getRealHeight(490));
									changebackimg = 1;
									if (changeObject == 0) {
										changeObject = 1;
									} else {
										changeObject = 0;
									}
								}
							} else {
								if (ladderlevelsize % 3 == 0) {
									ladder[climbLadderIndex].setIndex(0);
									ladder[climbLadderIndex]
											.setPositionY(ladderY[0]);
									// if the fireman reach the end
									if (fireman.getPositionY() <= ladderY[0]) {
										fireman.setPositionY(ladderY[0]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(0);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " + ";
										GameInfo.calculation += ladderlevelsize;
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}

								else if (ladderlevelsize % 3 == 1) {
									ladder[climbLadderIndex].setIndex(1);
									ladder[climbLadderIndex]
											.setPositionY(ladderY[1]);
									// if the fireman reach the end
									if (fireman.getPositionY() <= ladderY[1]) {
										fireman.setPositionY(ladderY[1]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(1);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " + ";
										GameInfo.calculation += ladderlevelsize;
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}

								else if (ladderlevelsize % 3 == 2) {
									ladder[climbLadderIndex].setIndex(2);
									ladder[climbLadderIndex]
											.setPositionY(ladderY[2]);
									// if the fireman reach the end
									if (fireman.getPositionY() <= ladderY[2]) {
										fireman.setPositionY(ladderY[2]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(2);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " + ";
										GameInfo.calculation += ladderlevelsize;
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}
							}
						}
					} else if (fireman.getCurrentLevel() == 1) {
						if (ladderlevelsize < 2) {
							// if the fireman reach the end
							if (fireman.getPositionY() <= ladderY[ladderlevelsize + 1]) {
								fireman.setPositionY(ladderY[ladderlevelsize + 1]);
								fireman.setClimblevelstate(0);
								fireman.setDirection(Fireman.MOTIONLESS);
								fireman.setCurrentLevel(ladderlevelsize + 1);
								fireman.setLevel(fireman.getLevel()
										+ ladderlevelsize);
								GameInfo.endlevel = fireman.getLevel();
								GameInfo.calculation += " + ";
								GameInfo.calculation += ladderlevelsize;
								if(GameInfo.isrescuing != 3){
									remindfire(1);
									GameInfo.isrescuing = 3;
								}
							}
						} else {
							if (changebackimg == 0) {
								if (fireman.getPositionY() <= 0) {
									fireman.setPositionY(PhoneInfo
											.getRealHeight(490));
									changebackimg = 1;
									if (changeObject == 0) {
										changeObject = 1;
									} else {
										changeObject = 0;
									}
								}
							} else {
								if ((ladderlevelsize + 1) % 3 == 0) {
									ladder[climbLadderIndex].setIndex(0);
									ladder[climbLadderIndex]
											.setPositionY(ladderY[0]);
									// if the fireman reach the end
									if (fireman.getPositionY() <= ladderY[0]) {
										fireman.setPositionY(ladderY[0]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(0);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " + ";
										GameInfo.calculation += ladderlevelsize;
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								} else if ((ladderlevelsize + 1) % 3 == 1) {
									ladder[climbLadderIndex].setIndex(1);
									ladder[climbLadderIndex]
											.setPositionY(ladderY[1]);
									// if the fireman reach the end
									if (fireman.getPositionY() <= ladderY[1]) {
										fireman.setPositionY(ladderY[1]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(1);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " + ";
										GameInfo.calculation += ladderlevelsize;
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}

								else if ((ladderlevelsize + 1) % 3 == 2) {
									ladder[climbLadderIndex].setIndex(2);
									ladder[climbLadderIndex]
											.setPositionY(ladderY[2]);
									// if the fireman reach the end
									if (fireman.getPositionY() <= ladderY[2]) {
										fireman.setPositionY(ladderY[2]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(2);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " + ";
										GameInfo.calculation += ladderlevelsize;
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}
							}
						}
					} else if (fireman.getCurrentLevel() == 2) {
						if (changebackimg == 0) {
							if (fireman.getPositionY() <= 0) {
								fireman.setPositionY(PhoneInfo
										.getRealHeight(490));
								changebackimg = 1;
								if (changeObject == 0) {
									changeObject = 1;
								} else {
									changeObject = 0;
								}
							}
						} else {
							if ((ladderlevelsize + 2) % 3 == 0) {
								ladder[climbLadderIndex].setIndex(0);
								ladder[climbLadderIndex]
										.setPositionY(ladderY[0]);
								// if the fireman reach the end
								if (fireman.getPositionY() <= ladderY[0]) {
									fireman.setPositionY(ladderY[0]);
									fireman.setClimblevelstate(0);
									fireman.setDirection(Fireman.MOTIONLESS);
									fireman.setCurrentLevel(0);
									fireman.setLevel(fireman.getLevel()
											+ ladderlevelsize);
									GameInfo.endlevel = fireman.getLevel();
									GameInfo.calculation += " + ";
									GameInfo.calculation += ladderlevelsize;
									if(GameInfo.isrescuing != 3){
										remindfire(1);
										GameInfo.isrescuing = 3;
									}
									changebackimg = 0;
								}
							} else if ((ladderlevelsize + 2) % 3 == 1) {
								ladder[climbLadderIndex].setIndex(1);
								ladder[climbLadderIndex]
										.setPositionY(ladderY[1]);
								// if the fireman reach the end
								if (fireman.getPositionY() <= ladderY[1]) {
									fireman.setPositionY(ladderY[1]);
									fireman.setClimblevelstate(0);
									fireman.setDirection(Fireman.MOTIONLESS);
									fireman.setCurrentLevel(1);
									fireman.setLevel(fireman.getLevel()
											+ ladderlevelsize);
									GameInfo.endlevel = fireman.getLevel();
									GameInfo.calculation += " + ";
									GameInfo.calculation += ladderlevelsize;
									if(GameInfo.isrescuing != 3){
										remindfire(1);
										GameInfo.isrescuing = 3;
									}
									changebackimg = 0;
								}
							}

							else if ((ladderlevelsize + 2) % 3 == 2) {
								ladder[climbLadderIndex].setIndex(2);
								ladder[climbLadderIndex]
										.setPositionY(ladderY[2]);
								// if the fireman reach the end
								if (fireman.getPositionY() <= ladderY[2]) {
									fireman.setPositionY(ladderY[2]);
									fireman.setClimblevelstate(0);
									fireman.setDirection(Fireman.MOTIONLESS);
									fireman.setCurrentLevel(2);
									fireman.setLevel(fireman.getLevel()
											+ ladderlevelsize);
									GameInfo.endlevel = fireman.getLevel();
									GameInfo.calculation += " + ";
									GameInfo.calculation += ladderlevelsize;
									if(GameInfo.isrescuing != 3){
										remindfire(1);
										GameInfo.isrescuing = 3;
									}
									changebackimg = 0;
								}
							}
						}
					}
				}

				else if (ladderlevelsize < 0) {
					if (fireman.getCurrentLevel() == 0) {
						if (changebackimg == 0) {
							if (fireman.getPositionY() >= PhoneInfo
									.getRealHeight(480)) {
								changebackimg = 1;
								if (changeObject == 0) {
									changeObject = 1;
								} else {
									changeObject = 0;
								}
								fireman.setPositionY(-10);
							}
						} else if (changebackimg == 1) {
							int tempsize = Math.abs(ladderlevelsize);
							if (tempsize % 3 == 0) {
								ladder[climbLadderIndex].setIndex(2);
								ladder[climbLadderIndex]
										.setPositionY(downLadderPositionY[2]);
								// when the fireman stop climbing
								if (fireman.getPositionY() >= ladderY[0]) {
									fireman.setPositionY(ladderY[0]);
									fireman.setClimblevelstate(0);
									fireman.setDirection(Fireman.MOTIONLESS);
									fireman.setCurrentLevel(0);
									fireman.setLevel(fireman.getLevel()
											+ ladderlevelsize);
									GameInfo.endlevel = fireman.getLevel();
									GameInfo.calculation += " - ";
									GameInfo.calculation += Math.abs(ladderlevelsize);
									if(GameInfo.isrescuing != 3){
										remindfire(1);
										GameInfo.isrescuing = 3;
									}
									changebackimg = 0;
								}
							} else if (tempsize % 3 == 1) {
								ladder[climbLadderIndex].setIndex(0);
								ladder[climbLadderIndex]
										.setPositionY(downLadderPositionY[0]);
								// when the fireman stop climbing
								if (fireman.getPositionY() >= ladderY[2]) {
									fireman.setPositionY(ladderY[2]);
									fireman.setClimblevelstate(0);
									fireman.setDirection(Fireman.MOTIONLESS);
									fireman.setCurrentLevel(2);
									fireman.setLevel(fireman.getLevel()
											+ ladderlevelsize);
									GameInfo.endlevel = fireman.getLevel();
									GameInfo.calculation += " - ";
									GameInfo.calculation += Math.abs(ladderlevelsize);
									if(GameInfo.isrescuing != 3){
										remindfire(1);
										GameInfo.isrescuing = 3;
									}
									changebackimg = 0;
								}
							} else if (tempsize % 3 == 2) {
								ladder[climbLadderIndex].setIndex(1);
								ladder[climbLadderIndex]
										.setPositionY(downLadderPositionY[1]);
								// when the fireman stop climbing
								if (fireman.getPositionY() >= ladderY[1]) {
									fireman.setPositionY(ladderY[1]);
									fireman.setClimblevelstate(0);
									fireman.setDirection(Fireman.MOTIONLESS);
									fireman.setCurrentLevel(1);
									fireman.setLevel(fireman.getLevel()
											+ ladderlevelsize);
									GameInfo.endlevel = fireman.getLevel();
									GameInfo.calculation += " - ";
									GameInfo.calculation += Math.abs(ladderlevelsize);
									if(GameInfo.isrescuing != 3){
										remindfire(1);
										GameInfo.isrescuing = 3;
									}
									changebackimg = 0;
								}
							}
						}
					} else if (fireman.getCurrentLevel() == 1) {
						if (Math.abs(ladderlevelsize) < 2) {
							// if the fireman reach the end
							if (fireman.getPositionY() >= ladderY[0]) {
								fireman.setPositionY(ladderY[0]);
								fireman.setClimblevelstate(0);
								fireman.setDirection(Fireman.MOTIONLESS);
								fireman.setCurrentLevel(0);
								fireman.setLevel(fireman.getLevel()
										+ ladderlevelsize);
								GameInfo.endlevel = fireman.getLevel();
								GameInfo.calculation += " - ";
								GameInfo.calculation += Math.abs(ladderlevelsize);
								if(GameInfo.isrescuing != 3){
									remindfire(1);
									GameInfo.isrescuing = 3;
								}
							}
						} else {
							if (changebackimg == 0) {
								ladder[climbLadderIndex]
										.setPositionY(ladderY[1]);
								ladder[climbLadderIndex].setIndex(1);
								if (fireman.getPositionY() >= PhoneInfo
										.getRealHeight(480)) {
									changebackimg = 1;
									if (changeObject == 0) {
										changeObject = 1;
									} else {
										changeObject = 0;
									}
									fireman.setPositionY(-10);
								}
							} else {
								int tempsize = Math.abs(ladderlevelsize);
								if ((tempsize - 1) % 3 == 0) {
									ladder[climbLadderIndex].setIndex(2);
									ladder[climbLadderIndex]
											.setPositionY(downLadderPositionY[2]);
									// when the fireman stop climbing
									if (fireman.getPositionY() >= ladderY[0]) {
										fireman.setPositionY(ladderY[0]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(0);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " - ";
										GameInfo.calculation += Math.abs(ladderlevelsize);
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								} else if ((tempsize - 1) % 3 == 1) {
									ladder[climbLadderIndex].setIndex(0);
									ladder[climbLadderIndex]
											.setPositionY(downLadderPositionY[0]);
									// when the fireman stop climbing
									if (fireman.getPositionY() >= ladderY[2]) {
										fireman.setPositionY(ladderY[2]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(2);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " - ";
										GameInfo.calculation += Math.abs(ladderlevelsize);
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								} else if ((tempsize - 1) % 3 == 2) {
									ladder[climbLadderIndex].setIndex(1);
									ladder[climbLadderIndex]
											.setPositionY(downLadderPositionY[1]);
									// when the fireman stop climbing
									if (fireman.getPositionY() >= ladderY[1]) {
										fireman.setPositionY(ladderY[1]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(1);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " - ";
										GameInfo.calculation += Math.abs(ladderlevelsize);
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}
							}
						}
					} else if (fireman.getCurrentLevel() == 2) {
						if (Math.abs(ladderlevelsize) < 3) {
							// if the fireman reach the end
							if (fireman.getPositionY() >= ladderY[2-Math.abs(ladderlevelsize)]) {
								fireman.setPositionY(ladderY[2-Math.abs(ladderlevelsize)]);
								fireman.setClimblevelstate(0);
								fireman.setDirection(Fireman.MOTIONLESS);
								fireman.setCurrentLevel(2-Math.abs(ladderlevelsize));
								fireman.setLevel(fireman.getLevel()
										- Math.abs(ladderlevelsize));
								GameInfo.endlevel = fireman.getLevel();
								GameInfo.calculation += " - ";
								GameInfo.calculation += Math.abs(ladderlevelsize);
								if(GameInfo.isrescuing != 3){
									remindfire(1);
									GameInfo.isrescuing = 3;
								}
							}
						} else {
							if (changebackimg == 0) {
								if (fireman.getPositionY() >= PhoneInfo
										.getRealHeight(480)) {
									changebackimg = 1;
									if (changeObject == 0) {
										changeObject = 1;
									} else {
										changeObject = 0;
									}
									fireman.setPositionY(-10);
								}
							} else {
								int tempsize = Math.abs(ladderlevelsize);
								if ((tempsize - 2) % 3 == 0) {
									ladder[climbLadderIndex].setIndex(2);
									ladder[climbLadderIndex]
											.setPositionY(downLadderPositionY[2]);
									// when the fireman stop climbing
									if (fireman.getPositionY() >= ladderY[0]) {
										fireman.setPositionY(ladderY[0]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(0);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " - ";
										GameInfo.calculation += Math.abs(ladderlevelsize);
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								} else if ((tempsize - 2) % 3 == 1) {
									ladder[climbLadderIndex].setIndex(0);
									ladder[climbLadderIndex]
											.setPositionY(downLadderPositionY[0]);
									// when the fireman stop climbing
									if (fireman.getPositionY() >= ladderY[2]) {
										fireman.setPositionY(ladderY[2]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(2);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " - ";
										GameInfo.calculation += Math.abs(ladderlevelsize);
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								} else if ((tempsize - 2) % 3 == 2) {
									ladder[climbLadderIndex].setIndex(1);
									ladder[climbLadderIndex]
											.setPositionY(downLadderPositionY[1]);
									// when the fireman stop climbing
									if (fireman.getPositionY() >= ladderY[1]) {
										fireman.setPositionY(ladderY[1]);
										fireman.setClimblevelstate(0);
										fireman.setDirection(Fireman.MOTIONLESS);
										fireman.setCurrentLevel(1);
										fireman.setLevel(fireman.getLevel()
												+ ladderlevelsize);
										GameInfo.endlevel = fireman.getLevel();
										GameInfo.calculation += " - ";
										GameInfo.calculation += Math.abs(ladderlevelsize);
										if(GameInfo.isrescuing != 3){
											remindfire(1);
											GameInfo.isrescuing = 3;
										}
										changebackimg = 0;
									}
								}
							}
						}
					}
				}
			}
			// if the fireman cross the level
			if (fireman.getAccrosslevel() == 1) {
				--(GameInfo.remainPeople);
				GameInfo.firstStart = false;
				// generate the calculations
				if (GameInfo.difficulty == GameInfo.TRAINEE) {
					GameInfo.calculation += " = " + GameInfo.doorLevel;
				}

				else if (GameInfo.difficulty == GameInfo.FIREFIGHTER) {
					GameInfo.calculation += " = " + GameInfo.doorLevel;
				}

				else if (GameInfo.difficulty == GameInfo.CAPTAIN) {
					for (int i = 0; i != ladderCurrentNumber; ++i) {
						if (ladder[i].getEndLevel() > ladder[i].getStartLevel()) {
							GameInfo.calculation += " + ";
						} else {
							GameInfo.calculation += " - ";
						}
						GameInfo.calculation += ladder[i].getLaddersize();
					}
					for (int i = 0; i != jumpSpringCurrentNumber; ++i) {
						GameInfo.calculation = fireman.getStartlevel() + " + ";
						GameInfo.calculation += jumpSpring[i].getJumpSize()
								+ " * ";
						GameInfo.calculation += jumpSpring[i].getJumpTimes();
					}
					GameInfo.calculation += " = " + GameInfo.doorLevel;
				}

				else if (GameInfo.difficulty == GameInfo.CHIEF) {
					for (int i = 0; i != ladderCurrentNumber; ++i) {
						if (ladder[i].getEndLevel() > ladder[i].getStartLevel()) {
							GameInfo.calculation += " + ";
						} else {
							GameInfo.calculation += " - ";
						}
						GameInfo.calculation += ladder[i].getLaddersize();
					}
					for (int j = 0; j != jumpSpringCurrentNumber; ++j) {
						if (jumpSpring[j].getEndLevel() > jumpSpring[j]
								.getStartLevel()) {
							GameInfo.calculation += " + ";
						} else {
							GameInfo.calculation += " - ";
						}
						GameInfo.calculation += jumpSpring[j].getJumpSize()
								+ " * ";
						GameInfo.calculation += jumpSpring[j].getJumpTimes();
					}
					GameInfo.calculation += " = " + GameInfo.doorLevel;
				}
				remindfire(0);
				initNewLevelGame();
				GameInfo.soundEffect[2].play((float) 1.0);
			}

			if (successfullySaved || failSaved) {
				++showSaveTime;
				if (showSaveTime == 10) {
					successfullySaved = false;
					failSaved = false;
					showSaveTime = 0;
				}
			}
		}
	}

	public void initNewLevelGame() {
		GameInfo.initNewLevelResources();
		ladderCurrentNumber = 0;
		jumpSpringCurrentNumber = 0;
		RecycleFireArray();
		int tempLife = fireman.getLife();
		int tempWater = fireman.getWater();
		fireman = new Fireman(firemanPicture);
		//GameInfo.RescueProcess = fireman.getLevel() + ""; //初始化营救过程为空
		fireman.isCollideState = false;
		fireman.setPositionX(PhoneInfo.getRealWidth(150));
		fireman.setPositionY(PhoneInfo.getRealHeight(333));
		fireman.setLife(tempLife);
		fireman.setWater(tempWater);
		water = new Water(waterpic);
		ladder = new Ladder[20];
		for (int i = 0; i != 20; ++i) {
			ladder[i] = new Ladder(ladderpic);
		}
		jumpSpring = new JumpSpring[20];
		for (int i = 0; i != 20; ++i) {
			jumpSpring[i] = new JumpSpring(jumpSpringpic);
		}

		door = new Door(doorPicture);
		door.setLevel(GameInfo.doorLevel);
		objects = new Object[6];
		for (int i = 0; i != 6; ++i) {
			objects[i] = new Object(objectspic);
			if (i == 0 || i == 3) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(355));
			} else if (i == 1 || i == 4) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(220));
			} else if (i == 2 || i == 5) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(90));
			}
		}
		choose = 0;
		firestate = 0;
		fireindex = 0;
		changebackimg = 0;
		changeObject = 0;
		// generate the level number
		if (GameInfo.difficulty == GameInfo.TRAINEE) {
			ladderLevel = getTraineeLadder();
		} else if (GameInfo.difficulty == GameInfo.FIREFIGHTER) {
			ladderLevel = getFireFighterLadder();
		} else if (GameInfo.difficulty == GameInfo.CAPTAIN) {
			jumpSpringLevel = getCaptainJumpSpring();
			ladderLevel = new int[10];
		} else if (GameInfo.difficulty == GameInfo.CHIEF) {
			ladderLevel = getChiefLadder();
			jumpSpringLevel = getChiefJumpSpring();
		}
		getRandomFire();
	}

	public void initNewGame() {
		GameInfo.initGameResources();
		ladderCurrentNumber = 0;
		jumpSpringCurrentNumber = 0;
		RecycleFireArray();
		fireman = new Fireman(firemanPicture);
		fireman.isCollideState = false;
		fireman.setPositionX(PhoneInfo.getRealWidth(150));
		fireman.setPositionY(PhoneInfo.getRealHeight(333));
		water = new Water(waterpic);
		ladder = new Ladder[20];
		for (int i = 0; i != 20; ++i) {
			ladder[i] = new Ladder(ladderpic);
		}
		jumpSpring = new JumpSpring[20];
		for (int i = 0; i != 20; ++i) {
			jumpSpring[i] = new JumpSpring(jumpSpringpic);
		}

		door = new Door(doorPicture);
		door.setLevel(GameInfo.doorLevel);
		objects = new Object[6];
		for (int i = 0; i != 6; ++i) {
			objects[i] = new Object(objectspic);
			if (i == 0 || i == 3) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(355));
			} else if (i == 1 || i == 4) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(220));
			} else if (i == 2 || i == 5) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(90));
			}
		}
		choose = 0;
		touchwatertool = 0;
		firestate = 0;
		fireindex = 0;
		changebackimg = 0;
		changeObject = 0;
		// generate the level number
		if (GameInfo.difficulty == GameInfo.TRAINEE) {
			ladderLevel = getTraineeLadder();
		} else if (GameInfo.difficulty == GameInfo.FIREFIGHTER) {
			ladderLevel = getFireFighterLadder();
		} else if (GameInfo.difficulty == GameInfo.CAPTAIN) {
			jumpSpringLevel = getCaptainJumpSpring();
			ladderLevel = new int[10];
		} else if (GameInfo.difficulty == GameInfo.CHIEF) {
			jumpSpringLevel = getChiefJumpSpring();
			ladderLevel = getChiefLadder();		
		}
		getRandomFire();
	}

	public void initLoadGame() throws IOException {
		ladderCurrentNumber = 0;
		jumpSpringCurrentNumber = 0;
		RecycleFireArray();
		fireman = new Fireman(firemanPicture);
		GameInfo.RescueProcess = fireman.getLevel() + ""; //初始化营救过程为空
		fireman.isCollideState = false;
		fireman.setPositionX(PhoneInfo.getRealWidth(150));
		fireman.setPositionY(PhoneInfo.getRealHeight(333));
		fireman.setLife(tempLife);
		fireman.setWater(tempWater);
		water = new Water(waterpic);
		ladder = new Ladder[20];
		for (int i = 0; i != 20; ++i) {
			ladder[i] = new Ladder(ladderpic);
		}
		jumpSpring = new JumpSpring[20];
		for (int i = 0; i != 20; ++i) {
			jumpSpring[i] = new JumpSpring(jumpSpringpic);
		}

		door = new Door(doorPicture);
		door.setLevel(GameInfo.doorLevel);
		objects = new Object[6];
		for (int i = 0; i != 6; ++i) {
			objects[i] = new Object(objectspic);
			if (i == 0 || i == 3) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(355));
			} else if (i == 1 || i == 4) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(220));
			} else if (i == 2 || i == 5) {
				objects[i].setPositionY(PhoneInfo.getRealHeight(90));
			}
		}
		choose = 0;
		touchwatertool = 0;
		firestate = 0;
		fireindex = 0;
		changebackimg = 0;
		changeObject = 0;
		// generate the level number
		if (GameInfo.difficulty == GameInfo.TRAINEE) {
			ladderLevel = getTraineeLadder();
		} else if (GameInfo.difficulty == GameInfo.FIREFIGHTER) {
			ladderLevel = getFireFighterLadder();
		} else if (GameInfo.difficulty == GameInfo.CAPTAIN) {
			jumpSpringLevel = getCaptainJumpSpring();
			ladderLevel = new int[10];
		} else if (GameInfo.difficulty == GameInfo.CHIEF) {
			jumpSpringLevel = getChiefJumpSpring();
			ladderLevel = getChiefLadder();
		}
		getRandomFire();
	}

	public void RecycleFireArray() {
		if (fire != null) {
			for (int i = 0; i != fire.length; ++i) {
				fire[i] = null;
			}
		}
	}

	public void getRandomFire() {
		fire = new Fire[3];
		for (int i = 0; i != 3; ++i) {
			fire[i] = new Fire(firepic);
			fire[i].setPositionY(firePositionY[i]);
			fire[i].setLevel(i);
		}
	}

	// remind the information of the fire
	private void remindfire(int flag) {
		if (flag == 0) {
			
			GameInfo.isrescuing = 0;
			Intent intent = new Intent(GameEngineActivity.instance,
					RemindFireActivity.class);
			GameEngineActivity.instance.startActivity(intent);
		} else if (flag == 1) {
			//GameInfo.isrescuing = 1; //
			Intent intent = new Intent(GameEngineActivity.instance,
					RemindFireActivity.class);
			GameEngineActivity.instance.startActivity(intent);
		}
	}

	public int[] getTraineeLadder() {
		int[] trainneeLadder = new int[5];
		for (int i = 0; i != 5; ++i) {
			trainneeLadder[i] = 0;
		}
		int ran = Math.abs(rand.nextInt());
		int returnRand = fireman.getLevel();
		for (int i = 0; i != 3; ++i) {
			returnRand = fireman.getLevel();
			if (GameInfo.round == 1 || GameInfo.round == 2) {
				while (returnRand == fireman.getLevel()
						|| Math.abs(returnRand - fireman.getLevel()) == Math
								.abs(GameInfo.doorLevel - fireman.getLevel())) {
					ran = Math.abs(rand.nextInt());
					returnRand = ran % 10;
					for (int j = 0; j != i; ++j) {
						if (Math.abs(returnRand - fireman.getLevel()) == trainneeLadder[j]) {
							returnRand = fireman.getLevel();
						}
					}
					Log.v("FireRescueGamming.java", "getTraineeLadder()");
				}
				trainneeLadder[i] = Math.abs(returnRand - fireman.getLevel());
			} else if (GameInfo.round == 3 || GameInfo.round == 4) {
				while (returnRand == fireman.getLevel()
						|| Math.abs(returnRand - fireman.getLevel()) == Math
								.abs(GameInfo.doorLevel - fireman.getLevel())) {
					ran = Math.abs(rand.nextInt());
					returnRand = ran % 199 - 99;
					for (int j = 0; j != i; ++j) {
						if (Math.abs(returnRand - fireman.getLevel()) == trainneeLadder[j]) {
							returnRand = fireman.getLevel();
						}
					}
					Log.v("FireRescueGamming.java", "getTraineeLadder()");
				}
				trainneeLadder[i] = Math.abs(returnRand - fireman.getLevel());
			}
		}
		trainneeLadder[3] = Math.abs(GameInfo.doorLevel - fireman.getLevel());
		BubbleSort(trainneeLadder, 4);
		return trainneeLadder;
	}

	public int[] getFireFighterLadder() {
		int[] fireFighterLadder = new int[7];
		for (int i = 0; i != 7; ++i) {
			fireFighterLadder[i] = 0;
		}
		int ran = Math.abs(rand.nextInt());
		int returnRand1 = 0;
		int returnRand2 = 0;
		int returnRand3 = 0;
		int returnRand4 = 0;
		boolean isEqual = true;
		int temp = 0;
		returnRand1 = 0;
		returnRand2 = 0;
		if (GameInfo.round == 1 || GameInfo.round == 2) {
			while (returnRand1 == 0 || returnRand2 == 0
					|| Math.abs(returnRand1) == Math.abs(returnRand2)) {
				ran = Math.abs(rand.nextInt());
				returnRand1 = ran % 199 - 99;
				returnRand2 = GameInfo.doorLevel - returnRand1
						- fireman.getLevel();
				Log.v("FireRescueGamming.java", "getFireFighterLadder()");
			}
			fireFighterLadder[0] = Math.abs(returnRand1);
			fireFighterLadder[1] = Math.abs(returnRand2);
			// generate other ladders
			for (int i = 2; i != 6; ++i) {
				isEqual = true;
				while (isEqual == true || returnRand3 == 0) {
					temp = 0;
					ran = Math.abs(rand.nextInt());
					returnRand3 = ran % 30;
					for (int j = 0; j != i; ++j) {
						if (Math.abs(returnRand3) == Math
								.abs(fireFighterLadder[j])) {
							temp = 1;
						}
					}
					if (temp == 0) {
						isEqual = false;
					}
					Log.v("FireRescueGamming.java", "getFireFighterLadder()2");
				}
				fireFighterLadder[i] = Math.abs(returnRand3);
			}
		} else if (GameInfo.round == 3 || GameInfo.round == 4) {
			while (returnRand1 == 0 || returnRand2 == 0 || returnRand3 == 0
					|| Math.abs(returnRand1) == Math.abs(returnRand2)
					|| Math.abs(returnRand2) == Math.abs(returnRand3)
					|| Math.abs(returnRand1) == Math.abs(returnRand3)) {
				ran = Math.abs(rand.nextInt());
				returnRand1 = ran % 199 - 99;
				ran = Math.abs(rand.nextInt());
				returnRand2 = ran % 199 - 99;
				returnRand3 = GameInfo.doorLevel - returnRand1 - returnRand2
						- fireman.getLevel();
				Log.v("FireRescueGamming.java", "getFireFighterLadder()");
			}
			fireFighterLadder[0] = Math.abs(returnRand1);
			fireFighterLadder[1] = Math.abs(returnRand2);
			fireFighterLadder[2] = Math.abs(returnRand3);
			// generate other ladders
			for (int i = 3; i != 6; ++i) {
				while (isEqual == true) {
					temp = 0;
					ran = Math.abs(rand.nextInt());
					returnRand4 = ran % 199 - 99;
					for (int j = 0; j != i; ++j) {
						if (Math.abs(returnRand4) == Math
								.abs(fireFighterLadder[j])) {
							temp = 1;
						}
					}
					if (temp == 0) {
						isEqual = false;
					}
					Log.v("FireRescueGamming.java", "getFireFighterLadder()2");
				}
				fireFighterLadder[i] = Math.abs(returnRand4);
			}
		}
		BubbleSort(fireFighterLadder, 6);
		return fireFighterLadder;
	}

	public int[] getCaptainJumpSpring() {
		int[] getCaptainJumpSpring = new int[5];
		boolean isEqual = true;
		for (int i = 0; i != 5; ++i) {
			getCaptainJumpSpring[i] = 0;
		}
		// generate the correct jumpSpring number
		int jumpSize = GameInfo.doorLevel - fireman.getStartlevel();
		int num1 = 0;
		int num2 = 0;
		int num3 = 0;
		int temp = 0;
		int ran = Math.abs(rand.nextInt());
		if (GameInfo.round == 1) {
			while (num1 * num2 != jumpSize) {
				num1 = ran % 9 + 1;
				ran = Math.abs(rand.nextInt());
				num2 = ran % 9 + 1;
				Log.v("FireRescueGamming.java", "getCaptainJumpSpring()");
			}
		} else if (GameInfo.round == 2 || GameInfo.round == 3) {
			while (num1 * num2 != jumpSize) {
				num1 = ran % 30 + 1;
				ran = Math.abs(rand.nextInt());
				num2 = ran % 5 + 1;
				Log.v("FireRescueGamming.java", "getCaptainJumpSpring()");
			}
		} else if (GameInfo.round == 4) {
			while (num1 * num2 != jumpSize) {
				num1 = ran % 50 + 1;
				ran = Math.abs(rand.nextInt());
				num2 = ran % 5 + 1;
				Log.v("FireRescueGamming.java", "getCaptainJumpSpring()");
			}

		}
		getCaptainJumpSpring[0] = num1;
		// generate the random number
		for (int i = 1; i != 4; ++i) {
			isEqual = true;
			while (isEqual == true) {
				temp = 0;
				ran = Math.abs(rand.nextInt());
				num3 = ran % 30 + 1;
				for (int j = 0; j != i; ++j) {
					if (num3 == getCaptainJumpSpring[j]) {
						temp = 1;
						break;
					}
				}
				if (temp == 1) {
					isEqual = true;
				} else {
					isEqual = false;
				}
				Log.v("FireRescueGamming.java", "getCaptainJumpSpring()2");
			}
			getCaptainJumpSpring[i] = Math.abs(num3);
		}
		return getCaptainJumpSpring;
	}

	public int[] getChiefLadder() {
		int[] getChiefLadder = new int[5];
		int temp = 0;
		int num1 = 0;
		int jumpTimes = 0;
		int index = 0;
		boolean isEqual = true;
		for (int i = 0; i != 5; ++i) {
			getChiefLadder[i] = 0;
		}
		int ran = Math.abs(rand.nextInt());
		//generate the correct jump spring times;
		if(GameInfo.round == 1 || GameInfo.round == 2){
			ran = Math.abs(rand.nextInt());
			jumpTimes = ran % 8 + 2;
		}
		else if(GameInfo.round == 3 || GameInfo.round == 4){
			ran = Math.abs(rand.nextInt());
			jumpTimes = ran % 28 + 2;
		}
		//generate the correct jump spring
		ran = Math.abs(rand.nextInt());
		index = ran % 4;
		//generate the correct ladder 
		getChiefLadder[0] = Math.abs(GameInfo.doorLevel - GameInfo.startlevel - jumpSpringLevel[index] * jumpTimes);
		if(getChiefLadder[0] == 0){
			getChiefLadder[0] = 10;
		}
		// generate the ladder randomly
		if (GameInfo.round == 1 || GameInfo.round == 2) {
			for (int i = 1; i != 4; ++i) {
				isEqual = true;
				while (isEqual == true) {
					temp = 0;
					ran = Math.abs(rand.nextInt());
					num1 = ran % 9 + 1;
					for (int j = 0; j != i; ++j) {
						if (num1 == getChiefLadder[j]
								|| num1 == GameInfo.doorLevel) {
							temp = 1;
							break;
						}
					}
					if (temp == 1) {
						isEqual = true;
					} else {
						isEqual = false;
					}
					Log.v("FireRescueGamming.java", "getChiefLadder()");
				}
				getChiefLadder[i] = Math.abs(num1);
			}
		}

		else if (GameInfo.round == 3 || GameInfo.round == 4) {
			for (int i = 1; i != 4; ++i) {
				isEqual = true;
				while (isEqual == true) {
					temp = 0;
					ran = Math.abs(rand.nextInt());
					num1 = ran % 99 + 1;
					for (int j = 0; j != i; ++j) {
						if (num1 == getChiefLadder[j]
								|| num1 == GameInfo.doorLevel) {
							temp = 1;
							break;
						}
					}
					if (temp == 1) {
						isEqual = true;
					} else {
						isEqual = false;
					}
					Log.v("FireRescueGamming.java", "getChiefLadder()");
				}
				getChiefLadder[i] = Math.abs(num1);
			}
		}
		return getChiefLadder;
	}

	public int[] getChiefJumpSpring() {
		int[] getJumpSpring = new int[4];
		int temp = 0;
		int num3 = 0;
		boolean isEqual = true;
		int ran = Math.abs(rand.nextInt());
		for (int i = 0; i != 4; ++i) {
			getJumpSpring[i] = 0;
		}
	
		// generate the random number
		if (GameInfo.round == 1 || GameInfo.round == 2) {
			for (int i = 0; i != 4; ++i) {
				isEqual = true;
				while (isEqual == true) {
					temp = 0;
					ran = Math.abs(rand.nextInt());
					num3 = ran % 8 + 2;
					for (int j = 0; j != i; ++j) {
						if (num3 == getJumpSpring[j]) {
							temp = 1;
							break;
						}
					}
					if (temp == 1) {
						isEqual = true;
					} else {
						isEqual = false;
					}
					Log.v("FireRescueGamming.java", "getChiefJumpSpring()2");
				}
				getJumpSpring[i] = Math.abs(num3);
			}
		}

		else if (GameInfo.round == 3 || GameInfo.round == 4) {
			for (int i = 0; i != 4; ++i) {
				isEqual = true;
				while (isEqual == true) {
					temp = 0;
					ran = Math.abs(rand.nextInt());
					num3 = ran % 28 + 2;
					for (int j = 0; j != i; ++j) {
						if (num3 == getJumpSpring[j]) {
							temp = 1;
							break;
						}
					}
					if (temp == 1) {
						isEqual = true;
					} else {
						isEqual = false;
					}
					Log.v("FireRescueGamming.java", "getChiefJumpSpring()2");
				}
				getJumpSpring[i] = Math.abs(num3);
			}
		}
		return getJumpSpring;
	}

	private void BubbleSort(int arr[], int n) {
		int i = 0, j = 0;
		for (i = 0; i < n; i++) {
			for (j = 0; j < n - 1 - i; j++) {
				if (arr[j] > arr[j + 1]) {
					arr[j] = arr[j] ^ arr[j + 1];
					arr[j + 1] = arr[j] ^ arr[j + 1];
					arr[j] = arr[j] ^ arr[j + 1];
				}
			}
		}
	}

	private boolean isLadderClimb() {
		boolean returnLadder = false;
		// whether the ladder is beside the fireman
		if (GameInfo.ladderNumber > 0) {
			for (int i = 0; i != ladderCurrentNumber; ++i) {
				if (ladder[i].getStartLevel() == fireman.getLevel()
						|| ladder[i].getEndLevel() == fireman.getLevel()) {
					if (ladder[i].getPositionX() >= fireman.getPositionX()) {
						if (ladder[i].getPositionX() - fireman.getPositionX() <= fireman
								.getWidth() / 2) {
							returnLadder = true;
							climbLadderIndex = i;
						}
					} else {
						if (fireman.getPositionX() - ladder[i].getPositionX() <= fireman
								.getWidth() / 2) {
							returnLadder = true;
							climbLadderIndex = i;
						}
					}
				}
			}
		}
		return returnLadder;
	}

	private boolean isJumpSpringClimb() {
		boolean returnJumpSpring = false;
		// whether the jump spring is beside the fireman
		if (GameInfo.jumpSpringNumber > 0 && jumpSpringCurrentNumber > 0) {
			if (jumpSpring[jumpSpringCurrentNumber - 1].getStartLevel() == fireman
					.getLevel()) {
				if (jumpSpring[jumpSpringCurrentNumber - 1].getPositionX() >= fireman
						.getPositionX()) {
					if (jumpSpring[jumpSpringCurrentNumber - 1].getPositionX()
							- fireman.getPositionX() <= fireman.getWidth() / 2) {
						returnJumpSpring = true;
					}
				} else {
					if (fireman.getPositionX()
							- jumpSpring[jumpSpringCurrentNumber - 1]
									.getPositionX() <= fireman.getWidth() / 2) {
						returnJumpSpring = true;
					}
				}
			}
		}
		return returnJumpSpring;
	}

	public void draw(Canvas canvas, Paint paint) {
		if (gameState == SELECT_DIFFICULTY) {
			canvas.drawBitmap(difficultyBackGround, null, new Rect(0, 0,
					PhoneInfo.resolutionWidth, PhoneInfo.resolutionHeight),
					paint);
			for (int i = 0; i != 4; ++i) {
				if (difficultyChoose == i + 1) {
					canvas.drawBitmap(
							difficultyButton[i + 4],
							null,
							new Rect(
									PhoneInfo.getRealWidth(260),
									PhoneInfo.getRealHeight(40) + i
											* PhoneInfo.getRealHeight(70),
									PhoneInfo.getRealWidth(260)
											+ PhoneInfo
													.getFigureWidth(difficultyButton[i]
															.getWidth()),
									PhoneInfo.getRealHeight(40)
											+ i
											* PhoneInfo.getRealHeight(70)
											+ PhoneInfo
													.getFigureHeight(difficultyButton[i + 4]
															.getHeight())),
							paint);
				} else {
					canvas.drawBitmap(
							difficultyButton[i],
							null,
							new Rect(
									PhoneInfo.getRealWidth(260),
									PhoneInfo.getRealHeight(40) + i
											* PhoneInfo.getRealHeight(70),
									PhoneInfo.getRealWidth(260)
											+ PhoneInfo
													.getFigureWidth(difficultyButton[i]
															.getWidth()),
									PhoneInfo.getRealHeight(40)
											+ i
											* PhoneInfo.getRealHeight(70)
											+ PhoneInfo
													.getFigureHeight(difficultyButton[i]
															.getHeight())),
							paint);
				}
			}
		}

		else if (gameState == GAMMING) {
			canvas.drawBitmap(backGround, null, new Rect(0, 0,
					PhoneInfo.resolutionWidth, PhoneInfo.resolutionHeight),
					paint);
			// draw the numbers
			paint.setTypeface(font);
			float textSize = (float) (36 * PhoneInfo.heightRatio);
			paint.setTextSize(textSize);
			paint.setColor(Color.rgb(98, 57, 35));
			int number = 0;

			if (changebackimg == 1) {
				int ladderlevelsize = 0;
				int endlevel = 0;
				if (ladder[climbLadderIndex].getStartLevel() == fireman
						.getLevel()) {
					ladderlevelsize = ladder[climbLadderIndex].getEndLevel()
							- ladder[climbLadderIndex].getStartLevel();
					endlevel = ladder[climbLadderIndex].getEndLevel();
				} else if (ladder[climbLadderIndex].getEndLevel() == fireman
						.getLevel()) {
					ladderlevelsize = ladder[climbLadderIndex].getStartLevel()
							- ladder[climbLadderIndex].getEndLevel();
					endlevel = ladder[climbLadderIndex].getStartLevel();
				} else {
					number = 0;
				}
				if (ladderlevelsize > 0) {
					if (fireman.getCurrentLevel() == 0) {
						if (ladderlevelsize % 3 == 0) {
							number = endlevel;
						} else if (ladderlevelsize % 3 == 1) {
							number = endlevel - 1;
						} else if (ladderlevelsize % 3 == 2) {
							number = endlevel - 2;
						}
					} else if (fireman.getCurrentLevel() == 1) {
						if ((ladderlevelsize + 1) % 3 == 0) {
							number = endlevel;
						} else if ((ladderlevelsize + 1) % 3 == 1) {
							number = endlevel - 1;
						} else if ((ladderlevelsize + 1) % 3 == 2) {
							number = endlevel - 2;
						}
					} else if (fireman.getCurrentLevel() == 2) {
						if ((ladderlevelsize + 2) % 3 == 0) {
							number = endlevel;
						} else if ((ladderlevelsize + 2) % 3 == 1) {
							number = endlevel - 1;
						} else if ((ladderlevelsize + 2) % 3 == 2) {
							number = endlevel - 2;
						}
					}
				} else {
					if (fireman.getCurrentLevel() == 0) {
						if (Math.abs(ladderlevelsize) % 3 == 0) {
							number = endlevel;
						} else if (Math.abs(ladderlevelsize) % 3 == 1) {
							number = endlevel - 2;
						} else if (Math.abs(ladderlevelsize) % 3 == 2) {
							number = endlevel - 1;
						}
					} else if (fireman.getCurrentLevel() == 1) {
						if ((Math.abs(ladderlevelsize) - 1) % 3 == 0) {
							number = endlevel;
						} else if ((Math.abs(ladderlevelsize) - 1) % 3 == 1) {
							number = endlevel - 2;
						} else if ((Math.abs(ladderlevelsize) - 1) % 3 == 2) {
							number = endlevel - 1;
						}
					} else if (fireman.getCurrentLevel() == 2) {
						if ((Math.abs(ladderlevelsize) - 2) % 3 == 0) {
							number = endlevel;
						} else if ((Math.abs(ladderlevelsize) - 2) % 3 == 1) {
							number = endlevel - 2;
						} else if ((Math.abs(ladderlevelsize) - 2) % 3 == 2) {
							number = endlevel - 1;
						}
					}
				}
			} else if (changebackimg == 0) {
				if (fireman.getCurrentLevel() == 0) {
					number = fireman.getLevel();
				} else if (fireman.getCurrentLevel() == 1) {
					number = fireman.getLevel() - 1;
				} else if (fireman.getCurrentLevel() == 2) {
					number = fireman.getLevel() - 2;
				}
			}
			int width = (int) paint.measureText(Integer.toString(number));
			int startX = PhoneInfo.getRealWidth(220)
					+ (PhoneInfo.getRealWidth(80) - width) / 2;
			int startY3 = (int) (PhoneInfo.getRealHeight(50) + textSize / 2);
			int startY2 = (int) (PhoneInfo.getRealHeight(200) + textSize / 2);
			int startY1 = (int) (PhoneInfo.getRealHeight(330) + textSize / 2);
			canvas.drawText(Integer.toString(number), startX, startY1, paint);
			++number;
			canvas.drawText(Integer.toString(number), startX, startY2, paint);
			++number;
			canvas.drawText(Integer.toString(number), startX, startY3, paint);

			// draw the save text
			if (successfullySaved == true) {
				canvas.drawBitmap(
						savePicture[0],
						null,
						new Rect(savePictureX[0], savePictureY[0],
								savePictureX[0]
										+ PhoneInfo
												.getFigureWidth(savePicture[0]
														.getWidth()),
								savePictureY[0]
										+ PhoneInfo
												.getFigureHeight(savePicture[0]
														.getHeight())), paint);
			}
			if (failSaved == true) {
				canvas.drawBitmap(
						savePicture[1],
						null,
						new Rect(savePictureX[1], savePictureY[1],
								savePictureX[1]
										+ PhoneInfo
												.getFigureWidth(savePicture[1]
														.getWidth()),
								savePictureY[0]
										+ PhoneInfo
												.getFigureHeight(savePicture[1]
														.getHeight())), paint);
			}

			// draw the door
			if (number == GameInfo.doorLevel) {
				door.setPositionY(PhoneInfo.getRealHeight(50));
				door.draw(canvas, paint);
			} else if (number - 1 == GameInfo.doorLevel) {
				door.setPositionY(PhoneInfo.getRealHeight(180));
				door.draw(canvas, paint);
			} else if (number - 2 == GameInfo.doorLevel) {
				door.setPositionY(PhoneInfo.getRealHeight(310));
				door.draw(canvas, paint);
			}

			// draw the objects
			if (changeObject == 0) {
				for (int i = 0; i != 3; ++i) {
					if (objects[i].isExist() == true) {
						objects[i].draw(canvas, paint);
					}
				}
			} else if (changeObject == 1) {
				for (int i = 3; i != 6; ++i) {
					if (objects[i].isExist() == true) {
						objects[i].draw(canvas, paint);
					}
				}
			}

			// draw the ladder and the fireman
			if (fireman.getClimblevelstate() == 0) {
				fireman.draw(canvas, paint);
				for (int i = 0; i != ladderCurrentNumber; ++i) {
					if (ladder[i].getStartLevel() == fireman.getLevel()
							|| ladder[i].getEndLevel() == fireman.getLevel()) {
						ladder[i].draw(canvas, paint);
					}
				}
			}

			else if (fireman.getClimblevelstate() == 1) {
				int tempLevel = fireman.getLevel();
				if(changebackimg == 1){
					if(fireman.getDirection() == fireman.UP_DIRECTION){
						if(ladder[climbLadderIndex].getStartLevel() >= ladder[climbLadderIndex].getEndLevel()){
							tempLevel = ladder[climbLadderIndex].getStartLevel();
						}
						else{
							tempLevel = ladder[climbLadderIndex].getEndLevel();
						}
					}
					else{
						if(ladder[climbLadderIndex].getStartLevel() >= ladder[climbLadderIndex].getEndLevel()){
							tempLevel = ladder[climbLadderIndex].getEndLevel();
						}
						else{
							tempLevel = ladder[climbLadderIndex].getStartLevel();
						}
					}
				}
				for (int i = 0; i != ladderCurrentNumber; ++i) {
					if (ladder[i].getStartLevel() == tempLevel
							|| ladder[i].getEndLevel() == tempLevel) {
						ladder[i].draw(canvas, paint);
					}
				}
				fireman.draw(canvas, paint);
			}

			// draw the jumpSpring
			if (jumpSpringCurrentNumber > 0) {
				if (jumpSpring[jumpSpringCurrentNumber - 1].getStartLevel() == fireman
						.getLevel()) {
					jumpSpring[jumpSpringCurrentNumber - 1].draw(canvas, paint);
				}
			}

			// draw the fire
			for (int i = 0; i != 3; ++i) {
				if (fire[i].getFirestate() == 0)
					fire[i].draw(canvas, paint);
			}

			// draw the water
			if (choose == 1 && fireman.getWater() > 0) {
				water.draw(canvas, paint);
			}

			// draw the savePicture
			canvas.drawBitmap(
					arrows[0],
					null,
					new Rect(PhoneInfo.getRealWidth(0), PhoneInfo
							.getRealHeight(295), PhoneInfo.getRealWidth(0)
							+ PhoneInfo.getFigureWidth(arrows[0].getWidth()),
							PhoneInfo.getRealHeight(295)
									+ PhoneInfo.getFigureHeight(arrows[0]
											.getHeight())), paint);
			canvas.drawBitmap(
					toolspic[0],
					null,
					new Rect(PhoneInfo.getRealWidth(472), PhoneInfo
							.getRealHeight(370), PhoneInfo.getRealWidth(635)
							+ PhoneInfo.getFigureWidth(toolspic[0].getWidth()),
							PhoneInfo.getRealHeight(370)
									+ PhoneInfo.getFigureHeight(toolspic[0]
											.getHeight())), paint);
			// draw the arrow
			for (int i = 0; i != 4; ++i) {
				buttons[i].draw(canvas, paint);
			}
			// draw the tool
			for (int i = 0; i != 4; ++i) {
				tools[i].draw(canvas, paint);
			}

			// draw the life of the fireman
			double lifepercent = fireman.getLife() * 1.0 / 5;
			int length = (int) (PhoneInfo.getFigureWidth(blood.getWidth()) * lifepercent);
			canvas.drawBitmap(blood, null,
					new Rect(bloodX, bloodY, bloodX + length, bloodY
							+ PhoneInfo.getFigureHeight(blood.getHeight())),
					paint);
			// draw the water number
			double waterpercent = fireman.getWater() * 1.0 / 6;
			length = (int) (PhoneInfo.getFigureWidth(waterNumber.getWidth()) * waterpercent);
			canvas.drawBitmap(
					waterNumber,
					null,
					new Rect(waterX, waterY, waterX + length,
							waterY
									+ PhoneInfo.getFigureHeight(waterNumber
											.getHeight())), paint);

			// draw the clock
			long currentTime = System.currentTimeMillis();
			long time = (currentTime - GameInfo.roundStartTime) / 1000;
			int minute = (int) ((GameInfo.roundTime - time) / 60);
			int second = (int) ((GameInfo.roundTime - time) % 60);
			startX = PhoneInfo.getRealWidth(390);
			int startY = PhoneInfo.getRealHeight(30);
			textSize = (float) (24 * PhoneInfo.heightRatio);
			paint.setTextSize(textSize);
			paint.setColor(Color.WHITE);
			if (minute == 0 && second < 15) {
				paint.setColor(Color.RED);
			}
			if (GameInfo.roundTime - time < 0) {
				minute = 0;
				second = 0;
			}
			canvas.drawText(minute + ":" + second, startX, startY, paint);

			// draw the target number
			paint.setColor(Color.WHITE);
			width = (int) paint.measureText(GameInfo.doorLevel + "");
			startX = PhoneInfo.getRealWidth(575)
					+ (PhoneInfo.getRealWidth(40) - width) / 2;
			startY = PhoneInfo.getRealHeight(30);
			canvas.drawText(GameInfo.doorLevel + "", startX, startY, paint);

			// draw the remaining people number
			width = (int) paint.measureText(GameInfo.remainPeople + "");
			startX = PhoneInfo.getRealWidth(640)
					+ (PhoneInfo.getRealWidth(45) - width) / 2;
			startY = PhoneInfo.getRealHeight(30);
			canvas.drawText(GameInfo.remainPeople + "", startX, startY, paint);
			// darw the random ladder number
			width = (int) paint.measureText(GameInfo.randomLadderNumber + "");
			startX = PhoneInfo.getRealWidth(698)
					+ (PhoneInfo.getRealWidth(40) - width) / 2;
			startY = PhoneInfo.getRealHeight(30);
			canvas.drawText(GameInfo.randomLadderNumber + "", startX, startY,
					paint);
			// draw the return number
			width = (int) paint.measureText(GameInfo.returnNumber + "");
			startX = PhoneInfo.getRealWidth(770)
					+ (PhoneInfo.getRealWidth(30) - width) / 2;
			startY = PhoneInfo.getRealHeight(30);
			canvas.drawText(GameInfo.returnNumber + "", startX, startY, paint);
		}
	}

	public void imgRecycle(Bitmap picture) {
		if (picture != null) {
			picture.recycle();
		}
	}
}

