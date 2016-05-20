package gamers.associate.malwg.game;

import gamers.associate.malwg.Assets;
import gamers.associate.malwg.GameItemSprite;
import gamers.associate.malwg.GameState;
import gamers.associate.malwg.GameType;
import gamers.associate.malwg.Malwg;
import gamers.associate.malwg.MiniGame;

import com.badlogic.gdx.scenes.scene2d.Action;
import com.badlogic.gdx.scenes.scene2d.OnActionCompleted;
import com.badlogic.gdx.scenes.scene2d.actions.MoveTo;
import com.badlogic.gdx.scenes.scene2d.actions.Sequence;

public class Lvl4 extends MiniGame {
	private static final String FOND_PNG = "data/lvl4/fond.png";
	private static final String BOIS = "data/lvl4/bois.png";
	private static final String CLOU = "data/lvl4/clou_19_128.png";
	private static final String MARTEAU = "data/lvl4/marteau_128_52.png";
	private GameItemSprite marteau;
	private GameItemSprite clou;
	private GameItemSprite bois;
	private float smx;
	private float smy;
	private float scx;
	private float scy;
	
	private int countToWin = 3;
	private int countWin;
	private float offset_marteau = -100;
	private int backDirection;
	
	public Lvl4() {
		this.disableMoveKeys = true;
		Assets.addTexture(MARTEAU);
		Assets.addTexture(CLOU);
		Assets.addTexture(BOIS);
		
		smx = - Malwg.WIDTH / 2 + 256 / 2;
		smy = Malwg.HEIGHT / 3f;
		this.marteau = new GameItemSprite(MARTEAU, smx, smy, 256, 104, 128, 52, 128, 128);
		
		scy = -50;
		this.clou = new GameItemSprite(CLOU, scx, scy, 19, 128, 19, 128, 128, 128);
		
		this.bois = new GameItemSprite(BOIS, 0, 0, Malwg.WIDTH, Malwg.HEIGHT, 256, 256, 256, 256);
		
		this.stage.addActor(clou);
		this.stage.addActor(bois);
		this.stage.addActor(marteau);
		
	}
	
	@Override
	public void render(float delta) {
		if (this.state == GameState.RUNNING) {
			if (this.marteau.x + this.marteau.width / 2f > Malwg.WIDTH / 2f) {
				this.direction = -1;
			}
			
			if (this.marteau.x - this.marteau.width / 2f < - Malwg.WIDTH / 2f) {
				this.direction = 1;
			}
			
			this.marteau.x += this.direction * delta * 400;
		}
		super.render(delta);
	}

	@Override
	protected GameType getGameType() {
		return GameType.ANNELAURE;
	}

	@Override
	protected String getTitle() {
		return "Bricolage";
	}

	@Override
	public MiniGame getNextLevel() {
		return new Lvl51();
	}

	private float lastAction;
	@Override
	protected void action() {
		if (this.lastAction == 0 || this.totalDelta - this.lastAction > 1) {
			if (Math.abs(this.clou.x - (this.marteau.x + offset_marteau )) < 35) {
				this.lastAction = this.totalDelta;
				this.backDirection = this.direction;
				float baseY = this.marteau.y;
				float baseX = this.marteau.x;
				MoveTo mt = MoveTo.$(baseX, this.clou.y + this.clou.height / 2f, 0.1f);
				MoveTo mti = MoveTo.$(baseX, baseY, 0.1f);
			
				Sequence seq = Sequence.$(mt, mti);
				seq.setCompletionListener(new OnActionCompleted() {
					
					@Override
					public void completed(Action action) {
						direction = backDirection;
					}
				});
				this.marteau.action(seq);
				
				this.clou.y -= this.clou.height / 4;
				this.countWin ++;
				
				if (this.countWin >= this.countToWin) {
					this.win();
				}
			}
		}
	}

	@Override
	protected void initLvl() {
		this.countWin = 0;
		this.marteau.x = smx;
		this.marteau.y = smy;
		this.clou.x = scx;
		this.clou.y = scy;
		this.direction = 1;
	}

	@Override
	protected String getMusic() {
		return "data/lvl4/marteau.ogg";
	}

	@Override
	protected String getBackground() {
		return FOND_PNG;
	}

}

