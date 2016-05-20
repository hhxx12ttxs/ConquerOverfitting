package com.me.artgame.Actors;

import PopUpScenes.PopUpInfoScene;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Touchable;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.Align;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.me.artgame.Scene.GameScene;
import com.me.artgame.TheArt;

public class QuestList extends Group{

	public float dp = TheArt.density;

	public Image Icon;
	public int Type;

	public QuestClass item;

	public Image background;
	public Label descrLabel;
	public Label articleLabel;
	public Label awardAmount;
	public Label typeLabel;
	public Table awardTable;
	public Group awardGroup;
	public Image awardImage;
	public Label exp;
	public Label gold;
	public Label points;

	public QuestList(QuestClass _item)
	{
		this.item = _item;
		createGroup();
	}
	public QuestList(QuestClass _item, boolean isHero){
		this.item = _item;
		if(isHero){
			createHeroGroup();
		}
		else
			createGroup();
	}
	public QuestList(QuestClass _item, int city){
		if(city == 1){
			this.item = _item;
			createCityQuest();
		}
	}
	public QuestClass getQuestClass(){
		return item;
	}
	public int getId(){
		return item.ID;
	}
	public void createHeroGroup(){
		background = new Image(new Texture("data/"+item.ICON));
		background.setSize(30*dp, 30*dp);
		if (item.DONE){
			Label done = new Label("!", GameScene.tabBtnSkin);
			done.setBounds(32*dp, 10*dp, 5*dp, 10*dp);
			done.setFontScale(0.8f*dp);
			addActor(done);
		}

		addActor(background);
	}
    public void getQuestType(int a){
        typeLabel = new Label("", GameScene.tabBtnSkin);
        switch (a){
            case 0:
                Icon = new Image(new Texture("data/red.png"));
                typeLabel.setText("Simple quest");
                typeLabel.setColor(Color.WHITE);
                break;
            case 1:
                Icon = new Image(new Texture("data/orange.png"));
                typeLabel.setText("Search");
                typeLabel.setColor(Color.LIGHT_GRAY);
                break;
            case 2:
                Icon = new Image(new Texture("data/yellow.png"));
                typeLabel.setText("Collecting");
                typeLabel.setColor(Color.GREEN);
                break;
            case 3:
                Icon = new Image(new Texture("data/green.png"));
                typeLabel.setText("Other");
                typeLabel.setColor(Color.CYAN);
                break;
            case 4:
                Icon = new Image(new Texture("data/blue.png"));
                typeLabel.setText("Raid");
                typeLabel.setColor(Color.BLUE);
                break;
            case 5:
                Icon = new Image(new Texture("data/purple.png"));
                typeLabel.setText("Kill boss");
                typeLabel.setColor(Color.RED);
                break;
        }
    }
	public void createCityQuest(){
        //INIT VARIABLES
        Icon = new Image();
        typeLabel = new Label("",TheArt.tabBtnSkin);
        getQuestType(item.TYPE);

		//BACKGROUND IMG
		background = new Image(new Texture("data/styles/interface/questcitybg.png"));
		background.setBounds(0, 0, 193*dp, 33*dp);
		background.setZIndex(0);

		//ICON
		Icon.setBounds(5*dp,5*dp,23*dp, 23*dp);

		//NAME
		articleLabel = new Label(item.ARTICLE, TheArt.tabBtnSkin);
		articleLabel.setFontScale(0.5f*dp);
		articleLabel.setWrap(true);
		articleLabel.setTouchable(Touchable.disabled);
		articleLabel.setBounds(35*dp, 15*dp, 175*dp,20*dp);
		articleLabel.setAlignment(Align.left);

        //TYPE
        typeLabel.setFontScale(0.4f*dp);
        typeLabel.setWrap(true);
        typeLabel.setTouchable(Touchable.disabled);
        typeLabel.setBounds(35*dp, 4*dp, 175*dp,10*dp);
        typeLabel.setAlignment(Align.left);

		//ADDING ACTORS
		addActor(background);
		addActor(Icon);
		addActor(articleLabel);
        addActor(typeLabel);
	}
	public void createGroup(){
        //INIT VARIABLES
        Icon = new Image();
        typeLabel = new Label("",TheArt.tabBtnSkin);

        getQuestType(item.TYPE);

		//BACKGROUND IMG
        background = new Image(new Texture("data/styles/interface/questcitybg.png"));
		background.setBounds(4*dp, 0, 240*dp, 35*dp);
		background.setZIndex(0);

		//ICON
		Icon.setBounds(11*dp,5*dp,24*dp, 24*dp);

		//NAME
		articleLabel = new Label(item.ARTICLE, GameScene.tabBtnSkin);
		articleLabel.setFontScale(0.5f*dp);
		articleLabel.setWrap(true);
		articleLabel.setTouchable(Touchable.disabled);
		articleLabel.setBounds(42*dp, 14*dp, 190*dp,19*dp);
		articleLabel.setAlignment(Align.left|Align.center);

        //TYPE
        typeLabel.setFontScale(0.4f*dp);
        typeLabel.setWrap(true);
        typeLabel.setTouchable(Touchable.disabled);
        typeLabel.setBounds(42*dp, 3*dp, 175*dp,10*dp);
        typeLabel.setAlignment(Align.left);
        /*
		//WOW WOW PALEHCHE!
		Type = 1;

		switch(Type){
		case 0:
			descrLabel = new Label("%Simple% - " + item.DESCR, GameScene.tabBtnSkin);
			descrLabel.setColor(Color.WHITE);
			break;
		case 1:
			descrLabel = new Label("%Cool% - " + item.DESCR, GameScene.tabBtnSkin);
			descrLabel.setColor(Color.BLUE);
			break;
		case 2:
			descrLabel = new Label("%Strange% - " + item.DESCR, GameScene.tabBtnSkin);
			descrLabel.setColor(Color.CYAN);
			break;
		};

		// 1 consumable
		awardTable = new Table();
		if(item.AWARD1AMOUNT!=0){
			awardGroup = new Group();
			awardImage = new Image(new Texture("data/" + item.AWARD1 + ".png"));
			awardImage.setBounds(5*dp, 8*dp, 13*dp, 13*dp);
			awardAmount = new Label("" + item.AWARD1AMOUNT,GameScene.tabBtnSkin);
			awardAmount.setFontScale(0.5f*dp);
			awardAmount.setWrap(true);
			awardAmount.setTouchable(Touchable.disabled);
			awardAmount.setBounds(17*dp, 8*dp, 20*dp, 13*dp);
			awardAmount.setAlignment(Align.left);
			awardGroup.addActor(awardImage);
			awardGroup.addActor(awardAmount);
			awardTable.add(awardGroup).size(30*dp, 10*dp);
		}
		// 2 consumable
		if(item.AWARD2AMOUNT!=0){
			awardGroup = new Group();
			awardImage = new Image(new Texture("data/" + item.AWARD2 + ".png"));
			awardImage.setBounds(5*dp, 8*dp, 13*dp, 13*dp);
			awardAmount = new Label("" + item.AWARD2AMOUNT,GameScene.tabBtnSkin);
			awardAmount.setFontScale(0.5f*dp);
			awardAmount.setWrap(true);
			awardAmount.setTouchable(Touchable.disabled);
			awardAmount.setBounds(17*dp, 8*dp, 20*dp, 13*dp);
			awardAmount.setAlignment(Align.left);
			awardGroup.addActor(awardImage);
			awardGroup.addActor(awardAmount);
			awardTable.add(awardGroup).size(30*dp, 10*dp);
		}
		//3 consumable
		if(item.AWARD3AMOUNT!=0){
			awardGroup = new Group();
			awardImage = new Image(new Texture("data/" + item.AWARD3 + ".png"));
			awardImage.setBounds(5*dp, 8*dp, 13*dp, 13*dp);
			awardAmount = new Label("" + item.AWARD3AMOUNT,GameScene.tabBtnSkin);
			awardAmount.setFontScale(0.5f*dp);
			awardAmount.setWrap(true);
			awardAmount.setTouchable(Touchable.disabled);
			awardAmount.setBounds(17*dp, 8*dp, 20*dp, 13*dp);
			awardAmount.setAlignment(Align.left);
			awardGroup.addActor(awardImage);
			awardGroup.addActor(awardAmount);
			awardTable.add(awardGroup).size(30*dp, 10*dp);
		}
		//4 consumable
		if(item.AWARD4AMOUNT!=0){
			awardGroup = new Group();
			awardImage = new Image(new Texture("data/" + item.AWARD4 + ".png"));
			awardImage.setBounds(5*dp, 8*dp, 13*dp, 13*dp);
			awardAmount = new Label("" + item.AWARD4AMOUNT,GameScene.tabBtnSkin);
			awardAmount.setFontScale(0.5f*dp);
			awardAmount.setWrap(true);
			awardAmount.setTouchable(Touchable.disabled);
			awardAmount.setBounds(17*dp, 8*dp, 20*dp, 13*dp);
			awardAmount.setAlignment(Align.left);
			awardGroup.addActor(awardImage);
			awardGroup.addActor(awardAmount);
			awardTable.add(awardGroup).size(30*dp, 10*dp);
		}
		//5 consumable
		if(item.AWARD5AMOUNT!=0){
			awardGroup = new Group();
			awardImage = new Image(new Texture("data/" + item.AWARD5 + ".png"));
			awardImage.setBounds(5*dp, 8*dp, 13*dp, 13*dp);
			awardAmount = new Label("" + item.AWARD5AMOUNT,GameScene.tabBtnSkin);
			awardAmount.setFontScale(0.5f*dp);
			awardAmount.setWrap(true);
			awardAmount.setTouchable(Touchable.disabled);
			awardAmount.setBounds(18*dp, 8*dp, 20*dp, 13*dp);
			awardAmount.setAlignment(Align.left);
			awardGroup.addActor(awardImage);
			awardGroup.addActor(awardAmount);
			awardTable.add(awardGroup).size(30*dp, 10*dp);
		}
		//if(item.EXP!=0||item.GOLD!=0)
		//	awardTable.row();
		if(item.EXP!=0){
			Label exp = new Label(String.valueOf(item.EXP),GameScene.tabBtnSkin);
			exp.setFontScale(0.5f*dp);
			exp.setPosition(170*dp, 1*dp);
			exp.setColor(Color.CYAN);
			awardTable.addActor(exp);
		}
		if(item.GOLD!=0){
			Label gold = new Label(String.valueOf(item.GOLD),GameScene.tabBtnSkin);
			gold.setFontScale(0.5f*dp);
			gold.setColor(Color.YELLOW);
			gold.setPosition(190*dp, 1*dp);
			awardTable.addActor(gold);
		}
		if(item.IMPROVEPOINTS!=0){
			Label points = new Label(String.valueOf(item.IMPROVEPOINTS),GameScene.tabBtnSkin);
			points.setFontScale(0.5f*dp);
			points.setColor(Color.GREEN);
			points.setPosition(160*dp, 1*dp);
			awardTable.addActor(points);
		}
		awardTable.setPosition(35*dp, 0);
		awardTable.align(Align.left);

        */
		//ADDING ACTORS
		addActor(background);
		addActor(Icon);
		addActor(articleLabel);
        addActor(typeLabel);
		//addActor(awardTable);
	}
	public Table getTableAwards(){
        Table awardsTable = new Table();
        awardsTable.pad(0);
		Label rewards = new Label("Awards:",GameScene.tabBtnSkin);
		rewards.setFontScale(0.6f*dp);
		rewards.setAlignment(Align.left);
		awardsTable.add(rewards).colspan(5).align(Align.left);
		awardsTable.row();
		// 1 consumable
				if(item.AWARD1AMOUNT!=0){
					awardGroup = new Group();
					awardImage = new Image(new Texture("data/" + item.AWARD1 + ".png"));
					awardImage.setBounds(0, 0, 25*dp, 25*dp);
                    awardGroup.addActor(awardImage);

                    if(item.AWARD1AMOUNT >1){
                        awardAmount = new Label("" + item.AWARD1AMOUNT,GameScene.tabBtnSkin);
                        awardAmount.setFontScale(0.5f*dp);
                        awardAmount.setWrap(true);
                        awardAmount.setTouchable(Touchable.disabled);
                        awardAmount.setBounds(0, 1*dp, 22*dp, 25*dp);
                        awardAmount.setAlignment(Align.right|Align.bottom);
                        awardGroup.addActor(awardAmount);
                    }
                    awardGroup.addListener(new ClickListener(){
                        public void clicked(InputEvent event, float x, float y)
                        {

                                TheArt.popupscene2 = new PopUpInfoScene(TheArt.inputMultiplexer, TheArt.HC.myConsumable.getConsumableByCode(item.AWARD1));
                                TheArt.popup2 = true;

                        }
                    });
					awardsTable.add(awardGroup).size(28*dp, 25*dp);
				}
                else{
                    awardsTable.add().size(0,0);
                }
		// 2 consumable
                if(item.AWARD2AMOUNT!=0){
                    awardGroup = new Group();
                    awardImage = new Image(new Texture("data/" + item.AWARD2 + ".png"));
                    awardImage.setBounds(0, 0, 25*dp, 25*dp);
                    awardGroup.addActor(awardImage);

                    if(item.AWARD1AMOUNT >1){
                        awardAmount = new Label("" + item.AWARD2AMOUNT,GameScene.tabBtnSkin);
                        awardAmount.setFontScale(0.5f*dp);
                        awardAmount.setWrap(true);
                        awardAmount.setTouchable(Touchable.disabled);
                        awardAmount.setBounds(0, 1*dp, 22*dp, 25*dp);
                        awardAmount.setAlignment(Align.right|Align.bottom);
                        awardGroup.addActor(awardAmount);
                    }
                    awardGroup.addListener(new ClickListener(){
                        public void clicked(InputEvent event, float x, float y)
                        {

                                TheArt.popupscene2 = new PopUpInfoScene(getStage(), TheArt.HC.myConsumable.getConsumableByCode(item.AWARD2));
                                TheArt.popup2 = true;

                        }
                    });
                    awardsTable.add(awardGroup).size(28*dp, 25*dp);
                }
                else{
                    awardsTable.add().size(0,0);
                }
		// 3 consumable
                if(item.AWARD3AMOUNT!=0){
                    awardGroup = new Group();
                    awardImage = new Image(new Texture("data/" + item.AWARD3 + ".png"));
                    awardImage.setBounds(0, 0, 25*dp, 25*dp);
                    awardGroup.addActor(awardImage);

                    if(item.AWARD1AMOUNT >1){
                        awardAmount = new Label("" + item.AWARD3AMOUNT,GameScene.tabBtnSkin);
                        awardAmount.setFontScale(0.5f*dp);
                        awardAmount.setWrap(true);
                        awardAmount.setTouchable(Touchable.disabled);
                        awardAmount.setBounds(0, 1*dp, 22*dp, 25*dp);
                        awardAmount.setAlignment(Align.right|Align.bottom);
                        awardGroup.addActor(awardAmount);
                    }
                    awardGroup.addListener(new ClickListener(){
                        public void clicked(InputEvent event, float x, float y)
                        {

                                TheArt.popupscene2 = new PopUpInfoScene(getStage(), TheArt.HC.myConsumable.getConsumableByCode(item.AWARD3));
                                TheArt.popup2 = true;

                        }
                    });
                    awardsTable.add(awardGroup).size(28*dp, 25*dp);
                }
                else{
                    awardsTable.add().size(0,0);
                }
		// 4 consumable
                if(item.AWARD4AMOUNT!=0){
                    awardGroup = new Group();
                    awardImage = new Image(new Texture("data/" + item.AWARD4 + ".png"));
                    awardImage.setBounds(0, 0, 25*dp, 25*dp);
                    awardGroup.addActor(awardImage);

                    if(item.AWARD4AMOUNT >1){
                        awardAmount = new Label("" + item.AWARD4AMOUNT,GameScene.tabBtnSkin);
                        awardAmount.setFontScale(0.5f*dp);
                        awardAmount.setWrap(true);
                        awardAmount.setTouchable(Touchable.disabled);
                        awardAmount.setBounds(0, 1*dp, 22*dp, 25*dp);
                        awardAmount.setAlignment(Align.right|Align.bottom);
                        awardGroup.addActor(awardAmount);
                    }
                    awardGroup.addListener(new ClickListener(){
                        public void clicked(InputEvent event, float x, float y)
                        {

                                TheArt.popupscene2 = new PopUpInfoScene(getStage(), TheArt.HC.myConsumable.getConsumableByCode(item.AWARD4));
                                TheArt.popup2 = true;

                        }
                    });
                    awardsTable.add(awardGroup).size(28*dp, 25*dp);
                }
                else{
                    awardsTable.add().size(0,0);
                }
		// 5 consumable
                if(item.AWARD5AMOUNT!=0){
                    awardGroup = new Group();
                    awardImage = new Image(new Texture("data/" + item.AWARD5 + ".png"));
                    awardImage.setBounds(0, 0, 25*dp, 25*dp);
                    awardGroup.addActor(awardImage);

                    if(item.AWARD5AMOUNT >1){
                        awardAmount = new Label("" + item.AWARD5AMOUNT,GameScene.tabBtnSkin);
                        awardAmount.setFontScale(0.5f*dp);
                        awardAmount.setWrap(true);
                        awardAmount.setTouchable(Touchable.disabled);
                        awardAmount.setBounds(0, 1*dp, 22*dp, 25*dp);
                        awardAmount.setAlignment(Align.right|Align.bottom);
                        awardGroup.addActor(awardAmount);
                    }
                    awardGroup.addListener(new ClickListener(){
                        public void clicked(InputEvent event, float x, float y)
                        {
                            TheArt.popupscene2 = new PopUpInfoScene(getStage(), TheArt.HC.myConsumable.getConsumableByCode(item.AWARD5));
                            TheArt.popup2 = true;
                        }
                    });
                    awardsTable.add(awardGroup).size(28*dp, 25*dp);
                }
                else{
                    awardsTable.add().size(0,0);
                }
				if(item.IMPROVEPOINTS!=0){
                    awardsTable.row();
					points = new Label("Skill points: "+String.valueOf(item.IMPROVEPOINTS),GameScene.tabBtnSkin);
					points.setFontScale(0.5f*dp);
					points.setColor(Color.GREEN);
                    points.setAlignment(Align.left);
					awardsTable.add(points).size(awardsTable.getWidth(), 10 * dp).colspan(5).align(Align.left);
				}

				if(item.EXP!=0){
                    awardsTable.row();
					exp = new Label("Experience: "+String.valueOf(item.EXP),GameScene.tabBtnSkin);
					exp.setFontScale(0.5f*dp);
					exp.setColor(Color.CYAN);
                    exp.setAlignment(Align.left);
                    awardsTable.add(exp).size(awardsTable.getWidth(), 10 * dp).colspan(5).align(Align.left);
				}
				if(item.GOLD!=0){
                    awardsTable.row();
					gold = new Label("Gold: "+String.valueOf(item.GOLD),GameScene.tabBtnSkin);
					gold.setFontScale(0.5f*dp);
					gold.setColor(Color.YELLOW);
                    gold.setAlignment(Align.left);
					awardsTable.add(gold).size(awardsTable.getWidth(),10*dp).colspan(5).align(Align.left);
				}

		awardsTable.setPosition(0, 0);
		awardsTable.align(Align.left|Align.left);
		return awardsTable;
	}
	public Group getAwardsHeroQuest(){
		Group awardsQuest = new Group();
		Table awardsTable = new Table();
		// 1 consumable
				if(item.AWARD1AMOUNT!=0){
					awardGroup = new Group();
					awardImage = new Image(new Texture("data/" + item.AWARD1 + ".png"));
					awardImage.setBounds(3*dp, -4*dp, 15*dp, 15*dp);
					awardAmount = new Label("" + item.AWARD1AMOUNT,GameScene.tabBtnSkin);
					awardAmount.setFontScale(0.6f*dp);
					awardAmount.setWrap(true);
					awardAmount.setTouchable(Touchable.disabled);
					awardAmount.setBounds(20*dp, -3*dp, 20*dp, 13*dp);
					awardAmount.setAlignment(Align.left);
					awardGroup.addActor(awardImage);
					awardGroup.addActor(awardAmount);
					awardsTable.add(awardGroup).size(40*dp, 10*dp);
				}
		// 2 consumable
				if(item.AWARD2AMOUNT!=0){
					awardGroup = new Group();
					awardImage = new Image(new Texture("data/" + item.AWARD2 + ".png"));
					awardImage.setBounds(3*dp, -4*dp, 15*dp, 15*dp);
					awardAmount = new Label("" + item.AWARD2AMOUNT,GameScene.tabBtnSkin);
					awardAmount.setFontScale(0.6f*dp);
					awardAmount.setWrap(true);
					awardAmount.setTouchable(Touchable.disabled);
					awardAmount.setBounds(20*dp, -3*dp, 20*dp, 13*dp);
					awardAmount.setAlignment(Align.left);
					awardGroup.addActor(awardImage);
					awardGroup.addActor(awardAmount);
					awardsTable.add(awardGroup).size(40*dp, 10*dp);
				}
		// 3 consumable
				if(item.AWARD3AMOUNT!=0){
					awardGroup = new Group();
					awardImage = new Image(new Texture("data/" + item.AWARD3 + ".png"));
					awardImage.setBounds(3*dp, -4*dp, 15*dp, 15*dp);
					awardAmount = new Label("" + item.AWARD3AMOUNT,GameScene.tabBtnSkin);
					awardAmount.setFontScale(0.6f*dp);
					awardAmount.setWrap(true);
					awardAmount.setTouchable(Touchable.disabled);
					awardAmount.setBounds(20*dp, -3*dp, 20*dp, 13*dp);
					awardAmount.setAlignment(Align.left);
					awardGroup.addActor(awardImage);
					awardGroup.addActor(awardAmount);
					awardsTable.add(awardGroup).size(40*dp, 10*dp);
				}
		// 4 consumable
				if(item.AWARD4AMOUNT!=0){
					awardGroup = new Group();
					awardImage = new Image(new Texture("data/" + item.AWARD4 + ".png"));
					awardImage.setBounds(3*dp, -4*dp, 15*dp, 15*dp);
					awardAmount = new Label("" + item.AWARD4AMOUNT,GameScene.tabBtnSkin);
					awardAmount.setFontScale(0.6f*dp);
					awardAmount.setWrap(true);
					awardAmount.setTouchable(Touchable.disabled);
					awardAmount.setBounds(20*dp, -3*dp, 20*dp, 13*dp);
					awardAmount.setAlignment(Align.left);
					awardGroup.addActor(awardImage);
					awardGroup.addActor(awardAmount);
					awardsTable.add(awardGroup).size(40*dp, 10*dp);
				}
		// 5 consumable
				if(item.AWARD5AMOUNT!=0){
					awardGroup = new Group();
					awardImage = new Image(new Texture("data/" + item.AWARD5 + ".png"));
					awardImage.setBounds(3*dp, -4*dp, 15*dp, 15*dp);
					awardAmount = new Label("" + item.AWARD5AMOUNT,GameScene.tabBtnSkin);
					awardAmount.setFontScale(0.6f*dp);
					awardAmount.setWrap(true);
					awardAmount.setTouchable(Touchable.disabled);
					awardAmount.setBounds(20*dp, -3*dp, 20*dp, 13*dp);
					awardAmount.setAlignment(Align.left);
					awardGroup.addActor(awardImage);
					awardGroup.addActor(awardAmount);
					awardsTable.add(awardGroup).size(40*dp, 10*dp);
				}
				if(item.IMPROVEPOINTS!=0){
					points = new Label(""+String.valueOf(item.IMPROVEPOINTS),GameScene.tabBtnSkin);
					points.setFontScale(0.5f*dp);
					points.setColor(Color.GREEN);
					points.setPosition(5*dp, 0);
					awardsQuest.addActor(points);
				}
				if(item.EXP!=0){
					exp = new Label(""+String.valueOf(item.EXP),GameScene.tabBtnSkin);
					exp.setFontScale(0.6f*dp);
					exp.setPosition(25*dp, 0);
					exp.setColor(Color.CYAN);
					awardsQuest.addActor(exp);
				}
				if(item.GOLD!=0){
					gold = new Label(""+String.valueOf(item.GOLD),GameScene.tabBtnSkin);
					gold.setFontScale(0.6f*dp);
					gold.setColor(Color.YELLOW);
					gold.setPosition(50*dp, 0);
					awardsQuest.addActor(gold);
				}

		awardsTable.setPosition(0, 30*dp);
		awardsTable.align(Align.left);
		awardsQuest.addActor(awardsTable);
		return awardsQuest;
	}
}


