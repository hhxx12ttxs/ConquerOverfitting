package pixelmon.client.gui.battles;

import java.util.Timer;
import java.util.TimerTask;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.Packet250CustomPayload;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import pixelmon.battles.attacks.Attack;
import pixelmon.battles.participants.ParticipantType;
import pixelmon.client.ServerStorageDisplay;
import pixelmon.client.gui.GuiPixelmonOverlay;
import pixelmon.comm.EnumPackets;
import pixelmon.comm.PacketCreator;
import pixelmon.comm.PixelmonDataPacket;
import pixelmon.comm.PixelmonMovesetDataPacket;
import pixelmon.comm.PixelmonStatsPacket;
import pixelmon.config.PixelmonItems;
import pixelmon.config.PixelmonItemsPokeballs;
import pixelmon.database.DatabaseMoves;
import pixelmon.enums.BagSection;
import pixelmon.gui.ContainerEmpty;
import pixelmon.items.ItemData;
import pixelmon.items.ItemEther;
import pixelmon.items.ItemPokeBall;
import pixelmon.items.ItemPotion;
import pixelmon.items.PixelmonItem;
import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiBattle extends GuiContainer {

	public enum BattleMode {
		Waiting, MainMenu, ChoosePokemon, ChooseBag, UseBag, ChooseAttack, ApplyToPokemon, YesNo, EnforcedSwitch;
	}

	private int battleControllerIndex = -1;
	public static BattleMode mode;
	public static BagSection bagSection;
	public static boolean battleEnded = false;
	private int guiWidth = 300;
	private int guiHeight = 60;
	boolean wasThirdPerson = false;

	public GuiBattle(int battleControllerIndex) {
		super(new ContainerEmpty());
		this.battleControllerIndex = battleControllerIndex;
		mode = BattleMode.Waiting;
		GuiPixelmonOverlay.isVisible = false;
		ClientBattleManager.clearMessages();
		battleEnded = false;
	}

	public GuiBattle() {
		super(new ContainerEmpty());
		this.mode = BattleMode.Waiting;
		GuiPixelmonOverlay.isVisible = false;
		ClientBattleManager.clearMessages();
		battleEnded = false;
	}

	boolean first = true;

	@Override
	protected void drawGuiContainerBackgroundLayer(float mfloat, int mouseX, int mouseY) {
		if (first) {
			if (Minecraft.getMinecraft().gameSettings.thirdPersonView == 1)
				wasThirdPerson = true;
			first = false;
			Minecraft.getMinecraft().gameSettings.thirdPersonView = 1;
		}

		if (!ClientBattleManager.hasMoreMessages() && battleEnded && !ClientBattleManager.hasLevelUps() && !ClientBattleManager.hasNewAttacks()) {
			if (!wasThirdPerson)
				Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
			Minecraft.getMinecraft().renderViewEntity = Minecraft.getMinecraft().thePlayer;
			mc.thePlayer.closeScreen();
			mc.setIngameFocus();
			GuiPixelmonOverlay.isVisible = true;

			return;
		}
		int left = (width - xSize) / 2;
		int top = (height - ySize) / 2;
		RenderHelper.disableStandardItemLighting();

		if (!ClientBattleManager.hasLevelUps() && !ClientBattleManager.hasNewAttacks()
				&& (mode == BattleMode.Waiting || mode == BattleMode.MainMenu || mode == BattleMode.ChooseAttack)) {
			drawPokemonOverlays();
		}
		if (ClientBattleManager.hasMoreMessages())
			drawMessageScreen();
		else if (mode == BattleMode.YesNo)
			drawYesNoDialog(mouseX, mouseY);
		else if (ClientBattleManager.hasLevelUps() || ClientBattleManager.hasNewAttacks()) {
			if (!ClientBattleManager.hasNewAttacks()
					|| (ClientBattleManager.hasLevelUps() && ClientBattleManager.levelUpList.get(0).level <= ClientBattleManager.newAttackList.get(0).level)) {
				mode = oldMode;
				drawLevelUp(mouseX, mouseY);
			} else
				drawReplaceAttack(mouseX, mouseY);

		} else if (mode == BattleMode.MainMenu)
			drawMainMenu(mouseX, mouseY);
		else if (mode == BattleMode.ChooseAttack)
			drawChooseAttack(mouseX, mouseY);
		else if (mode == BattleMode.ChoosePokemon || mode == BattleMode.ApplyToPokemon || mode == BattleMode.EnforcedSwitch)
			drawChoosePokemon(mouseX, mouseY);
		else if (mode == BattleMode.ChooseBag)
			drawChooseBag(mouseX, mouseY);
		else if (mode == BattleMode.UseBag)
			drawUseBag(mouseX, mouseY);
		else if (mode == BattleMode.Waiting)
			drawMessageScreen();
	}

	private void drawPokemonOverlays() {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/pokemonInfoP1.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width - 120, height - (guiHeight + 45), 120, 45, 0, 0, 120f / 128f, 45f / 64f);
		PixelmonDataPacket userPokemon = ClientBattleManager.getUserPokemonPacket();
		String name = userPokemon.nickname.equals("") ? userPokemon.name : userPokemon.nickname;
		drawString(fontRenderer, name, width - 113, height - (guiHeight + 37), 0xFFFFFF);
		drawExpBar(width - 114, height - (guiHeight + 8), 105, 4, userPokemon);
		drawImageQuad(guiIndex, width - 115, height - (guiHeight + 11), 109, 7, 1f / 128f, 56f / 64f, 110f / 128f, 62f / 64f);
		// drawImageQuad(-1, width - 113, height - (guiHeight + 7), 105, 2,
		// 0,0,1,1);

		drawHealthBar(width - 101, height - (guiHeight + 26), 97, 6, userPokemon);
		drawImageQuad(guiIndex, width - 111, height - (guiHeight + 28), 103, 9, 1f / 128f, 45f / 64f, 104f / 128f, 55f / 64f);
		drawString(fontRenderer, "" + userPokemon.health + "/" + userPokemon.hp, width - 10 - fontRenderer.getStringWidth("" + userPokemon.health + "/" + userPokemon.hp), height
				- (guiHeight + 18), 0xFFFFFF);
		if (userPokemon.isMale)
			drawImageQuad(guiIndex, width - 113 + fontRenderer.getStringWidth(name), height - (guiHeight + 39), 7, 10, 119f / 128f, 52f / 64f, 126f / 128f, 62f / 64f);
		else
			drawImageQuad(guiIndex, width - 113 + fontRenderer.getStringWidth(name), height - (guiHeight + 39), 7, 10, 111f / 128f, 52f / 64f, 118f / 128f, 62f / 64f);
		drawString(fontRenderer, "Lv. " + userPokemon.lvl, width - 10 - fontRenderer.getStringWidth("Lv. " + userPokemon.lvl), height - (guiHeight + 37), 0xFFFFFF);

		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/pokemonInforP2.png");
		PixelmonDataPacket targetPokemon = ClientBattleManager.getOpponent();
		if (targetPokemon != null) {
			String targetName = targetPokemon.nickname.equals("") ? targetPokemon.name : targetPokemon.nickname;
			drawImageQuad(guiIndex, 0, 0, 119, 34, 0, 0, 119f / 128f, 34f / 64f);
			drawString(fontRenderer, targetName, 8, 8, 0xFFFFFF);
			drawHealthBar(18, 19, 56, 6, targetPokemon);
			drawImageQuad(guiIndex, 8, 18, 62, 9, 1f / 128f, 43f / 64f, 63f / 128f, 53f / 64f);
			if (targetPokemon.isMale)
				drawImageQuad(guiIndex, 8 + fontRenderer.getStringWidth(targetName), 6, 7, 10, 72f / 128f, 42f / 64f, 79f / 128f, 52f / 64f);
			else
				drawImageQuad(guiIndex, 8 + fontRenderer.getStringWidth(targetName), 6, 7, 10, 64f / 128f, 42f / 64f, 71f / 128f, 52f / 64f);
			drawString(fontRenderer, "Lv. " + targetPokemon.lvl, 111 - fontRenderer.getStringWidth("Lv. " + targetPokemon.lvl), 8, 0xFFFFFF);
		}
	}

	private void drawExpBar(int x, int y, int width, int height, PixelmonDataPacket p) {
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(3553 /* GL_TEXTURE_2D */);
		tessellator.startDrawingQuads();

		int barWidth = (int) (((float) p.xp) / ((float) p.nextLvlXP) * (((float) width) - 6f));
		tessellator.setColorRGBA_F(0.0f, 0.0f, 0.4f, 1.0F);
		tessellator.addVertex(x, y, 0.0);
		tessellator.addVertex(x, y + height, 0.0);
		tessellator.addVertex(x + width, y + height, 0.0);
		tessellator.addVertex(x + width, y, 0.0);
		tessellator.setColorRGBA_F(0.3f, 1.0f, 1.0f, 1.0F);
		tessellator.addVertex(x, y, 0.0);
		tessellator.addVertex(x, y + height, 0.0);
		tessellator.addVertex(x + barWidth, y + height, 0.0);
		tessellator.addVertex(x + barWidth, y, 0.0);
		tessellator.draw();
		GL11.glPopMatrix();
		GL11.glEnable(3553);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
	}

	private enum LevelStage {
		First, Second, Third
	}

	private LevelStage drawLevelStage = LevelStage.First;

	private void drawLevelUp(int mouseX, int mouseY) {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/levelUpPopUp.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - 52, height / 2 - 66, 104, 113, 0, 0, 104f / 256f, 113f / 256f);
		drawString(fontRenderer, "HP", width / 2 - 43, height / 2 - 54, 0xFFFFFF);
		drawString(fontRenderer, "Attack", width / 2 - 43, height / 2 - 38, 0xFFFFFF);
		drawString(fontRenderer, "Defence", width / 2 - 43, height / 2 - 22, 0xFFFFFF);
		drawString(fontRenderer, "Sp. Attack", width / 2 - 43, height / 2 - 6, 0xFFFFFF);
		drawString(fontRenderer, "Sp. Defence", width / 2 - 43, height / 2 + 10, 0xFFFFFF);
		drawString(fontRenderer, "Speed", width / 2 - 43, height / 2 + 26, 0xFFFFFF);
		if (drawLevelStage == LevelStage.First) {
			drawString(fontRenderer, "+" + (ClientBattleManager.levelUpList.get(0).statsLevel2.HP - ClientBattleManager.levelUpList.get(0).statsLevel1.HP), width / 2 + 25,
					height / 2 - 54, 0xFFFFFF);
			drawString(fontRenderer, "+" + (ClientBattleManager.levelUpList.get(0).statsLevel2.Attack - ClientBattleManager.levelUpList.get(0).statsLevel1.Attack), width / 2 + 25,
					height / 2 - 38, 0xFFFFFF);
			drawString(fontRenderer, "+" + (ClientBattleManager.levelUpList.get(0).statsLevel2.Defence - ClientBattleManager.levelUpList.get(0).statsLevel1.Defence),
					width / 2 + 25, height / 2 - 22, 0xFFFFFF);
			drawString(fontRenderer, "+" + (ClientBattleManager.levelUpList.get(0).statsLevel2.SpecialAttack - ClientBattleManager.levelUpList.get(0).statsLevel1.SpecialAttack),
					width / 2 + 25, height / 2 - 6, 0xFFFFFF);
			drawString(fontRenderer, "+" + (ClientBattleManager.levelUpList.get(0).statsLevel2.SpecialDefence - ClientBattleManager.levelUpList.get(0).statsLevel1.SpecialDefence),
					width / 2 + 25, height / 2 + 10, 0xFFFFFF);
			drawString(fontRenderer, "+" + (ClientBattleManager.levelUpList.get(0).statsLevel2.Speed - ClientBattleManager.levelUpList.get(0).statsLevel1.Speed), width / 2 + 25,
					height / 2 + 26, 0xFFFFFF);
		} else if (drawLevelStage == LevelStage.Second) {
			PixelmonStatsPacket stats = ClientBattleManager.levelUpList.get(0).statsLevel2;
			drawString(fontRenderer, "" + stats.HP, stats.HP < 100 ? width / 2 + 28 : width / 2 + 22, height / 2 - 54, 0xFFFFFF);
			drawString(fontRenderer, "" + stats.Attack, stats.Attack < 100 ? width / 2 + 28 : width / 2 + 22, height / 2 - 38, 0xFFFFFF);
			drawString(fontRenderer, "" + stats.Defence, stats.Defence < 100 ? width / 2 + 28 : width / 2 + 22, height / 2 - 22, 0xFFFFFF);
			drawString(fontRenderer, "" + stats.SpecialAttack, stats.SpecialAttack < 100 ? width / 2 + 28 : width / 2 + 22, height / 2 - 6, 0xFFFFFF);
			drawString(fontRenderer, "" + stats.SpecialDefence, stats.SpecialDefence < 100 ? width / 2 + 28 : width / 2 + 22, height / 2 + 10, 0xFFFFFF);
			drawString(fontRenderer, "" + stats.Speed, stats.Speed < 100 ? width / 2 + 28 : width / 2 + 22, height / 2 + 26, 0xFFFFFF);
		}

		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/battleGui3.png");

		String name = "";
		if (ServerStorageDisplay.get(ClientBattleManager.levelUpList.get(0).pokemonID).nickname.equals(""))
			name = ServerStorageDisplay.get(ClientBattleManager.levelUpList.get(0).pokemonID).name;
		else
			name = ServerStorageDisplay.get(ClientBattleManager.levelUpList.get(0).pokemonID).nickname;
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - guiWidth / 2, height - guiHeight, guiWidth, guiHeight, 0, 0, 1, 146f / 480f);
		drawCenteredString(fontRenderer, "Your " + name + " has grown to level " + ClientBattleManager.levelUpList.get(0).level + "!", width / 2, height - 35, 0xFFFFFF);
		flashCount++;
		if (flashCount > 30) {
			drawImageQuad(guiIndex, width / 2 + 130, height - 15, 10, 6, 611f / 640f, 149f / 480f, 628f / 640f, 159f / 480f);
			if (flashCount > 60)
				flashCount = 0;
		}
	}

	private void drawYesNoDialog(int mouseX, int mouseY) {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/yesNo.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - 256 / 2, height / 2 - 50, 256, 100, 0, 0, 1, 100f / 128f);

		Attack newAttack = ClientBattleManager.newAttackList.get(0).attack;
		float textAreaWidth = 170;
		if (selectedAttack == -1) {
			float textWidth = fontRenderer.getStringWidth("Do you want to give up learning " + newAttack.baseAttack.attackName + "?");
			int numLines = (int) (textWidth / textAreaWidth) + 1;
			fontRenderer.drawSplitString("Do you want to give up learning " + newAttack.baseAttack.attackName + "?", width / 2 - 109, height / 2 + 1 - numLines * 10 / 2,
					(int) textAreaWidth, 0x000000);
			fontRenderer.drawSplitString("Do you want to give up learning " + newAttack.baseAttack.attackName + "?", width / 2 - 110, height / 2 - numLines * 10 / 2,
					(int) textAreaWidth, 0xFFFFFF);
		} else {
			String text = "Do you want to replace " + attacks[selectedAttack].baseAttack.attackName + " with " + newAttack.baseAttack.attackName + "?";
			float textWidth = fontRenderer.getStringWidth(text);
			int numLines = (int) (textWidth / textAreaWidth) + 1;
			fontRenderer.drawSplitString(text, width / 2 - 109, height / 2 + 1 - numLines * 10 / 2, (int) textAreaWidth, 0x000000);
			fontRenderer.drawSplitString(text, width / 2 - 110, height / 2 - numLines * 10 / 2, (int) textAreaWidth, 0xFFFFFF);
		}

		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 108 && mouseY > height / 2 - 33 && mouseY < height / 2 - 7)
			drawImageQuad(guiIndex, width / 2 + 63, height / 2 - 33, 45, 26, 154f / 256f, 101f / 128f, 199f / 256f, 127f / 128f);
		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 108 && mouseY > height / 2 + 5 && mouseY < height / 2 + 31)
			drawImageQuad(guiIndex, width / 2 + 63, height / 2 + 5, 45, 26, 154f / 256f, 101f / 128f, 199f / 256f, 127f / 128f);
		drawString(fontRenderer, "Yes", width / 2 + 76, height / 2 - 23, 0xFFFFFF);
		drawString(fontRenderer, "No", width / 2 + 80, height / 2 + 15, 0xFFFFFF);
	}

	private int selectedAttack = -1;
	private Attack[] attacks = new Attack[4];

	private void drawReplaceAttack(int mouseX, int mouseY) {
		int guiIndex = -1, typesIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/chooseMove.png");
		typesIndex = mc.renderEngine.getTexture("/pixelmon/gui/types.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - 256 / 2, height / 2 - 102, 256, 205, 0, 0, 1, 205f / 256f);
		PixelmonDataPacket pokemonToLearnAttack = ServerStorageDisplay.get(ClientBattleManager.newAttackList.get(0).pokemonID);
		for (int i = 0; i < pokemonToLearnAttack.numMoves; i++) {
			PixelmonMovesetDataPacket move = pokemonToLearnAttack.moveset[i];
			drawString(fontRenderer, move.attackName, width / 2 + 11, height / 2 - 85 + 22 * i, 0xFFFFFF);
			drawString(fontRenderer, move.pp + "/" + move.ppBase, width / 2 + 90, height / 2 - 83 + 22 * i, 0xFFFFFF);
			float x = move.type.textureX;
			float y = move.type.textureY;
			drawImageQuad(typesIndex, width / 2 - 30, height / 2 - 92 + 22 * i, 38, 21, x / 256f, y / 128f, (x + 38f) / 256f, (y + 21f) / 128f);
		}
		for (int i = 0; i < pokemonToLearnAttack.numMoves; i++) {
			if (mouseX > width / 2 - 30 && mouseX < width / 2 + 120 && mouseY > height / 2 - 94 + 22 * i && mouseY < height / 2 - 94 + 22 * (i + 1)) {
				drawImageQuad(guiIndex, width / 2 - 30, height / 2 - 94 + 22 * i, 152, 24, 97f / 256f, 209f / 256f, 249f / 256f, 234f / 256f);
				if (attacks[i] == null || !attacks[i].baseAttack.attackName.equals(pokemonToLearnAttack.moveset[i].attackName))
					attacks[i] = DatabaseMoves.getAttack(pokemonToLearnAttack.moveset[i].attackName);
				drawMoveInfo(attacks[i]);
			}
		}

		Attack newAttack = ClientBattleManager.newAttackList.get(0).attack;

		drawString(fontRenderer, newAttack.baseAttack.attackName, width / 2 + 11, height / 2 - 78 + 22 * 4, 0xFFFFFF);
		drawString(fontRenderer, newAttack.pp + "/" + newAttack.ppBase, width / 2 + 90, height / 2 - 76 + 22 * 4, 0xFFFFFF);
		float x = newAttack.baseAttack.attackType.textureX;
		float y = newAttack.baseAttack.attackType.textureY;
		drawImageQuad(typesIndex, width / 2 - 30, height / 2 + 3, 38, 21, x / 256f, y / 128f, (x + 38f) / 256f, (y + 21f) / 128f);
		if (mouseX > width / 2 - 30 && mouseX < width / 2 + 120 && mouseY > height / 2 + 3 && mouseY < height / 2 + 25) {
			drawImageQuad(guiIndex, width / 2 - 30, height / 2 + 1, 152, 24, 97f / 256f, 209f / 256f, 249f / 256f, 234f / 256f);
			drawMoveInfo(newAttack);
		}
		String numString = "";
		if (pokemonToLearnAttack.getNationalPokedexNumber() < 10)
			numString = "00" + pokemonToLearnAttack.getNationalPokedexNumber();
		else if (pokemonToLearnAttack.getNationalPokedexNumber() < 100)
			numString = "0" + pokemonToLearnAttack.getNationalPokedexNumber();
		else
			numString = "" + pokemonToLearnAttack.getNationalPokedexNumber();
		int var9;
		if (pokemonToLearnAttack.isShiny)
			var9 = Minecraft.getMinecraft().renderEngine.getTexture("/pixelmon/shinysprites/" + numString + ".png");
		else
			var9 = Minecraft.getMinecraft().renderEngine.getTexture("/pixelmon/sprites/" + numString + ".png");
		drawImageQuad(var9, width / 2 - 114, height / 2 - 76, 64f, 64f, 0f, 0f, 1f, 1f);
		drawCenteredString(fontRenderer, pokemonToLearnAttack.nickname.equals("") ? pokemonToLearnAttack.name : pokemonToLearnAttack.nickname, width / 2 - 82, height / 2 + 8,
				0xFFFFFF);

		drawString(fontRenderer, "Effect", width / 2 - 96, height / 2 + 38, 0xFFFFFF);
		drawString(fontRenderer, "Description", width / 2 - 20, height / 2 + 38, 0xFFFFFF);
	}

	private void drawMoveInfo(Attack attack) {
		drawString(fontRenderer, "Power", width / 2 - 120, height / 2 + 58, 0xFFFFFF);
		drawString(fontRenderer, "Accuracy", width / 2 - 120, height / 2 + 78, 0xFFFFFF);
		int bpextra = 0, acextra = 0;
		;
		if (attack.baseAttack.basePower >= 100)
			bpextra = fontRenderer.getCharWidth('0');
		if (attack.baseAttack.accuracy >= 100)
			acextra = fontRenderer.getCharWidth('0');
		if (attack.baseAttack.basePower != -1)
			drawString(fontRenderer, "" + attack.baseAttack.basePower, width / 2 - 55 - bpextra, height / 2 + 58, 0xFFFFFF);
		else
			drawString(fontRenderer, "--", width / 2 - 55 - bpextra, height / 2 + 58, 0xFFFFFF);

		if (attack.baseAttack.accuracy == -1)
			drawString(fontRenderer, "--", width / 2 - 55 - acextra, height / 2 + 78, 0xFFFFFF);
		else
			drawString(fontRenderer, "" + attack.baseAttack.accuracy, width / 2 - 55 - acextra, height / 2 + 78, 0xFFFFFF);

		fontRenderer.drawSplitString(attack.baseAttack.description, width / 2 - 25, height / 2 + 55, 100, 0xFFFFFF);
	}

	private int startIndex = 0;

	private void drawUseBag(int mouseX, int mouseY) {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/itemGui2.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - 256 / 2, height / 2 - 102, 256, 205, 0, 0, 1, 205f / 256f);
		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 111 && mouseY > height / 2 - 91 && mouseY < height / 2 - 74)
			drawImageQuad(guiIndex, width / 2 + 63, height / 2 - 91, 48, 17, 198f / 256f, 234 / 256f, 246f / 256f, 251f / 256f);
		drawString(fontRenderer, "Back", width / 2 + 76, height / 2 - 85, 0xFFFFFF);

		drawCenteredString(fontRenderer, bagSection.displayString, width / 2 - 40, height / 2 - 80, 0xFFFFFF);

		for (int i = startIndex; i < 6 + startIndex; i++) {
			if (i < ClientBattleManager.bagStore.size()) {
				if (mouseX > width / 2 - 98 && mouseX < width / 2 - 98 + 187 && mouseY > height / 2 - 44 + (i - startIndex) * 21
						&& mouseY < height / 2 - 24 + (i - startIndex) * 21)
					drawImageQuad(guiIndex, width / 2 - 98, height / 2 - 44 + (i - startIndex) * 21, 187, 20, 3f / 256f, 206 / 256f, 194f / 256f, 225f / 256f);
				else
					drawImageQuad(guiIndex, width / 2 - 98, height / 2 - 44 + (i - startIndex) * 21, 187, 20, 3f / 256f, 227 / 256f, 194f / 256f, 246f / 256f);
				Item item = PixelmonItems.getItem(ClientBattleManager.bagStore.get(i).id);
				if (item == null)
					item = PixelmonItemsPokeballs.getItemFromID(ClientBattleManager.bagStore.get(i).id);
				drawString(fontRenderer, item.getItemDisplayName(null), width / 2 - 55, height / 2 - 38 + (i - startIndex) * 21, 0xFFFFFF);
				drawString(fontRenderer, "x" + ClientBattleManager.bagStore.get(i).count, width / 2 + 55, height / 2 - 38 + (i - startIndex) * 21, 0xFFFFFF);
			}
		}

		if (startIndex > 0) {
			if (mouseX > width / 2 - 11 && mouseX < width / 2 + 6 && mouseY > height / 2 - 55 && mouseY < height / 2 - 45)
				drawImageQuad(guiIndex, width / 2 - 11, height / 2 - 55, 17, 10, 211f / 256f, 220 / 256f, 228f / 256f, 230f / 256f);
		} else
			drawImageQuad(guiIndex, width / 2 - 11, height / 2 - 55, 17, 10, 10f / 256f, 10 / 256f, 27f / 256f, 20f / 256f);
		if (startIndex + 6 < ClientBattleManager.bagStore.size() - 1) {
			if (mouseX > width / 2 - 11 && mouseX < width / 2 + 6 && mouseY > height / 2 + 82 && mouseY < height / 2 + 92)
				drawImageQuad(guiIndex, width / 2 - 11, height / 2 + 82, 17, 10, 236f / 256f, 220 / 256f, 253f / 256f, 230f / 256f);
		} else
			drawImageQuad(guiIndex, width / 2 - 11, height / 2 + 82, 17, 10, 10f / 256f, 10 / 256f, 27f / 256f, 20f / 256f);
		for (int i = startIndex; i < 6 + startIndex; i++) {
			if (i < ClientBattleManager.bagStore.size()) {
				Item item = PixelmonItems.getItem(ClientBattleManager.bagStore.get(i).id);
				if (item == null)
					item = PixelmonItemsPokeballs.getItemFromID(ClientBattleManager.bagStore.get(i).id);
				itemRenderer.renderItemIntoGUI(this.fontRenderer, this.mc.renderEngine, new ItemStack(item), width / 2 - 85, height / 2 - 42 + (i - startIndex) * 21);
			}
		}

	}

	private void drawChooseBag(int mouseX, int mouseY) {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/itemGui1_Test.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - 256 / 2, height / 2 - 76, 256, 153, 0, 0, 1, 153f / 256f);
		drawCenteredString(fontRenderer, "Poke Balls", width / 2 + 53, height / 2 - 36, 0xFFFFFF);
		drawCenteredString(fontRenderer, "HP/PP Restore", width / 2 + 53, height / 2 + 31, 0xFFFFFF);
		drawCenteredString(fontRenderer, "Status Restore", width / 2 - 53, height / 2 - 36, 0xFFFFFF);
		drawCenteredString(fontRenderer, "Battle Items", width / 2 - 53, height / 2 + 31, 0xFFFFFF);
		int x1, x2, y1, y2;
		x1 = width / 2 - 103;
		x2 = width / 2 + 3;
		y1 = height / 2 - 63;
		y2 = height / 2 + 4;
		int buttonWidth = 100, buttonHeight = 62;
		if (mouseX > x1 && mouseX < x1 + buttonWidth && mouseY > y1 && mouseY < y1 + buttonHeight)
			drawImageQuad(guiIndex, x1, y1, buttonWidth, buttonHeight, 28f / 256f, 176f / 256f, 128f / 256f, 238f / 256f);
		if (mouseX > x1 && mouseX < x1 + buttonWidth && mouseY > y2 && mouseY < y2 + buttonHeight)
			drawImageQuad(guiIndex, x1, y2, buttonWidth, buttonHeight, 28f / 256f, 176f / 256f, 128f / 256f, 238f / 256f);
		if (mouseX > x2 && mouseX < x2 + buttonWidth && mouseY > y1 && mouseY < y1 + buttonHeight)
			drawImageQuad(guiIndex, x2, y1, buttonWidth, buttonHeight, 28f / 256f, 176f / 256f, 128f / 256f, 238f / 256f);
		if (mouseX > x2 && mouseX < x2 + buttonWidth && mouseY > y2 && mouseY < y2 + buttonHeight)
			drawImageQuad(guiIndex, x2, y2, buttonWidth, buttonHeight, 28f / 256f, 176f / 256f, 128f / 256f, 238f / 256f);

		if (mouseX > width / 2 + 106 && mouseX < width / 2 + 126 && mouseY > height / 2 + 55 && mouseY < height / 2 + 77)
			drawImageQuad(guiIndex, width / 2 + 106, height / 2 + 55, 20, 22, 234f / 256f, 154f / 256f, 254f / 256f, 176f / 256f);
	}

	int flashCount = 0;

	private void drawMessageScreen() {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/battleGui3.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - guiWidth / 2, height - guiHeight, guiWidth, guiHeight, 0, 0, 1, 146f / 480f);
		if (mode != BattleMode.Waiting || ClientBattleManager.hasMoreMessages()) {
			drawCenteredString(fontRenderer, ClientBattleManager.getNextMessage(), width / 2, height - 35, 0xFFFFFF);
			flashCount++;
			if (flashCount > 30) {
				drawImageQuad(guiIndex, width / 2 + 130, height - 15, 10, 6, 611f / 640f, 149f / 480f, 628f / 640f, 159f / 480f);
				if (flashCount > 60)
					flashCount = 0;
			}
		} else {
			flashCount++;
			if (flashCount >= 160)
				flashCount = 0;
			if (flashCount < 40)
				drawCenteredString(fontRenderer, "Waiting", width / 2, height - 35, 0xFFFFFF);
			else if (flashCount < 80)
				drawCenteredString(fontRenderer, "Waiting.", width / 2, height - 35, 0xFFFFFF);
			else if (flashCount < 120)
				drawCenteredString(fontRenderer, "Waiting..", width / 2, height - 35, 0xFFFFFF);
			else if (flashCount < 160)
				drawCenteredString(fontRenderer, "Waiting...", width / 2, height - 35, 0xFFFFFF);

		}
	}

	private void drawMainMenu(int mouseX, int mouseY) {
		if (Minecraft.getMinecraft().renderViewEntity != Minecraft.getMinecraft().thePlayer)
			Minecraft.getMinecraft().renderViewEntity = Minecraft.getMinecraft().thePlayer;
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/battleGui1.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - guiWidth / 2, height - guiHeight, guiWidth, guiHeight, 0, 0, 1, 146f / 480f);
		drawButton(width / 2 + 31, height - guiHeight + 9, 48, 16, "FIGHT", mouseX, mouseY, guiIndex, 0);
		drawButton(width / 2 + 31, height - guiHeight + 35, 48, 16, "BAG", mouseX, mouseY, guiIndex, 1);
		drawButton(width / 2 + 90, height - guiHeight + 9, 48, 16, "POKEMON", mouseX, mouseY, guiIndex, 2);
		drawButton(width / 2 + 90, height - guiHeight + 35, 48, 16, "RUN", mouseX, mouseY, guiIndex, 3);
		drawString(fontRenderer, "What will " + ClientBattleManager.getUserPokemonPacket().name + " do?", width / 2 - 130, height - 35, 0xFFFFFF);
	}

	boolean isHealing = false;

	int healAmount = 0;

	private void drawChoosePokemon(int mouseX, int mouseY) {
		isHealing = false;
		if (mode == BattleMode.ApplyToPokemon && pixelmonToHeal != null) {
			if (pixelmonToHeal.health >= healAmount || pixelmonToHeal.health >= pixelmonToHeal.hp) {
				PacketDispatcher.sendPacketToServer(PacketCreator.createPacket(EnumPackets.BagPacket, itemToUse.id, battleControllerIndex, 0));
				removeItem(itemToUse);
				mode = BattleMode.Waiting;
				return;
			} else {
				isHealing = true;
			}
		}

		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/choosePokemon.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - 128, height - 203, 256, 203, 0, 0, 1, 203f / 256f);

		if (mode == BattleMode.ApplyToPokemon)
			drawCenteredString(fontRenderer, "Select a Pokemon to use on", width / 2 - 40, height - 23, 0xFFFFFF);
		else
			drawCenteredString(fontRenderer, "Choose a Pokemon to send out", width / 2 - 40, height - 23, 0xFFFFFF);

		if (mode != BattleMode.EnforcedSwitch) {
			if (mouseX > width / 2 + 63 && mouseX < width / 2 + 63 + 48 && mouseY > height - 27 && mouseY < height - 27 + 17 && !isHealing) {
				drawImageQuad(guiIndex, width / 2 + 63, height - 27, 48, 17, 198f / 256f, 210f / 256f, 246f / 256, 227f / 256f);
			}
			drawString(fontRenderer, "Back", width / 2 + 75, height - 22, 0xFFFFFF);
		}

		PixelmonDataPacket p = ClientBattleManager.getUserPokemonPacket();
		String numString = "";
		if (p.getNationalPokedexNumber() < 10)
			numString = "00" + p.getNationalPokedexNumber();
		else if (p.getNationalPokedexNumber() < 100)
			numString = "0" + p.getNationalPokedexNumber();
		else
			numString = "" + p.getNationalPokedexNumber();
		int var9;
		if (p.isShiny)
			var9 = Minecraft.getMinecraft().renderEngine.getTexture("/pixelmon/shinysprites/" + numString + ".png");
		else
			var9 = Minecraft.getMinecraft().renderEngine.getTexture("/pixelmon/sprites/" + numString + ".png");
		drawImageQuad(var9, width / 2 - 121, height - 176, 24f, 24f, 0f, 0f, 1f, 1f);
		drawHealthBar(width / 2 - 85, height - 135, 56, 9, p);
		drawImageQuad(guiIndex, width / 2 - 95, height - 135, 61, 9, 86f / 256f, 240f / 256f, 147f / 256f, 249f / 256f);
		drawCenteredString(fontRenderer, p.health + "/" + p.hp, width / 2 - 59, height - 123, 0xffffff);
		drawString(fontRenderer, p.nickname.equals("") ? p.name : p.nickname, width / 2 - 90, height - 161, 0xffffff);
		drawString(fontRenderer, "Lv. " + p.lvl, width / 2 - 90, height - 148, 0xffffff);
		if (p.isMale)
			drawImageQuad(guiIndex, width / 2 - 60, height - 149, 6, 9, 32f / 256f, 208f / 256f, 38f / 256f, 217f / 256f);
		else
			drawImageQuad(guiIndex, width / 2 - 60, height - 149, 6, 9, 32f / 256f, 218f / 256f, 38f / 256f, 227f / 256f);

		if (mode == BattleMode.ApplyToPokemon && !isHealing) {
			if (mouseX > width / 2 - 120 && mouseX < width / 2 - 21 && mouseY > height - 165 && mouseY < height - 113)
				drawImageQuad(mc.renderEngine.getTexture("/pixelmon/gui/selectCurrentPokemon.png"), width / 2 - 120, height - 165, 89, 52, 0, 0, 1, 1);
		}

		int pos = -1;
		for (int i = 0; i < 6; i++) {
			if (i != p.order) {
				pos++;
				PixelmonDataPacket pdata = ServerStorageDisplay.pokemon[i];
				if (pdata != null) {

					numString = "";
					if (pdata.getNationalPokedexNumber() < 10)
						numString = "00" + pdata.getNationalPokedexNumber();
					else if (pdata.getNationalPokedexNumber() < 100)
						numString = "0" + pdata.getNationalPokedexNumber();
					else
						numString = "" + pdata.getNationalPokedexNumber();
					if (pdata.isShiny)
						var9 = Minecraft.getMinecraft().renderEngine.getTexture("/pixelmon/shinysprites/" + numString + ".png");
					else
						var9 = Minecraft.getMinecraft().renderEngine.getTexture("/pixelmon/sprites/" + numString + ".png");
					drawImageQuad(var9, width / 2 - 23, height - 192 + pos * 30, 24f, 24f, 0f, 0f, 1f, 1f);
					drawHealthBar(width / 2 + 65, height - 192 + pos * 30, 56, 9, pdata);
					drawImageQuad(guiIndex, width / 2 + 55, height - 192 + pos * 30, 61, 9, 86f / 256f, 240f / 256f, 147f / 256f, 249f / 256f);
					drawString(fontRenderer, pdata.health + "/" + pdata.hp, width / 2 + 75, height - 180 + pos * 30, 0xffffff);
					drawString(fontRenderer, pdata.nickname.equals("") ? pdata.name : pdata.nickname, width / 2 + 5, height - 190 + pos * 30, 0xffffff);
					drawString(fontRenderer, "Lv. " + pdata.lvl, width / 2 + 5, height - 176 + pos * 30, 0xffffff);
					if (pdata.isMale)
						drawImageQuad(guiIndex, width / 2 + 40, height - 176 + pos * 30, 6, 9, 32f / 256f, 208f / 256f, 38f / 256f, 217f / 256f);
					else
						drawImageQuad(guiIndex, width / 2 + 40, height - 176 + pos * 30, 6, 9, 32f / 256f, 218f / 256f, 38f / 256f, 227f / 256f);

					int xpos = width / 2 - 30;
					int ypos = height - 195 + pos * 30;
					if (mouseX > xpos && mouseX < xpos + 150 && mouseY > ypos + 1 && mouseY < ypos + 31 && !isHealing)
						// if (mode==BattleMode.ApplyToPokemon &&
						// PixelmonItems.getItem(itemToUse.id) instanceof
						// ItemRevive)
						// else
						if (!pdata.isFainted)
							drawImageQuad(guiIndex, xpos, ypos, 150, 32, 43f / 256f, 205f / 256f, 194f / 256f, 237f / 256f);
				}
			}
		}
	}

	public void drawHealthBar(int x, int y, int width, int height, PixelmonDataPacket p) {
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);
		GL11.glEnable(GL11.GL_COLOR_MATERIAL);
		GL11.glPushMatrix();
		Tessellator tessellator = Tessellator.instance;
		GL11.glDisable(3553 /* GL_TEXTURE_2D */);
		tessellator.startDrawingQuads();

		int barWidth = width - 6;
		tessellator.setColorRGBA_F(1.0F, 0.2F, 0.2F, 1.0F);
		tessellator.addVertex(x, y, 0.0);
		tessellator.addVertex(x, y + height, 0.0);
		tessellator.addVertex(x + barWidth, y + height, 0.0);
		tessellator.addVertex(x + barWidth, y, 0.0);
		barWidth = (int) (((float) p.health) / ((float) p.hp) * (((float) width) - 6f));
		tessellator.setColorRGBA_F(0.2F, 1F, 0.2F, 1.0F);
		tessellator.addVertex(x, y, 0.0);
		tessellator.addVertex(x, y + height, 0.0);
		tessellator.addVertex(x + barWidth, y + height, 0.0);
		tessellator.addVertex(x + barWidth, y, 0.0);
		tessellator.draw();
		GL11.glPopMatrix();
		GL11.glEnable(3553);
		GL11.glDisable(GL12.GL_RESCALE_NORMAL);
		GL11.glDisable(GL11.GL_COLOR_MATERIAL);
	}

	private void drawChooseAttack(int mouseX, int mouseY) {
		int guiIndex = -1;
		guiIndex = mc.renderEngine.getTexture("/pixelmon/gui/battleGui2.png");

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		drawImageQuad(guiIndex, width / 2 - guiWidth / 2, height - guiHeight, guiWidth, guiHeight, 0, 0, 1, 146f / 480f);
		PixelmonMovesetDataPacket[] moveset = ClientBattleManager.getUserPokemonPacket().moveset;
		int numMoves = ClientBattleManager.getUserPokemonPacket().numMoves;
		if (numMoves > 0)
			drawButton(width / 2 - 141, height - guiHeight + 9, 87, 20, moveset[0].attackName, mouseX, mouseY, guiIndex, 0);
		if (numMoves > 1)
			drawButton(width / 2 - 50, height - guiHeight + 9, 87, 20, moveset[1].attackName, mouseX, mouseY, guiIndex, 1);
		if (numMoves > 2)
			drawButton(width / 2 - 141, height - guiHeight + 33, 87, 20, moveset[2].attackName, mouseX, mouseY, guiIndex, 2);
		if (numMoves > 3)
			drawButton(width / 2 - 50, height - guiHeight + 33, 87, 20, moveset[3].attackName, mouseX, mouseY, guiIndex, 3);
		if (moveset[mouseOverButton] != null) {
			drawCenteredString(fontRenderer, "PP: " + moveset[mouseOverButton].pp + "/" + moveset[mouseOverButton].ppBase, width / 2 + 99, height - guiHeight + 18, 0xFFFFFF);
			drawString(fontRenderer, "Type: ", width / 2 + 99 - fontRenderer.getStringWidth("Type: " + moveset[mouseOverButton].type.toString()) / 2, height - guiHeight + 33,
					0xFFFFFF);
			fontRenderer.drawString(moveset[mouseOverButton].type.toString(), width / 2 + 99 - fontRenderer.getStringWidth("Type: " + moveset[mouseOverButton].type.toString()) / 2
					+ fontRenderer.getStringWidth("Type: "), height - guiHeight + 33, moveset[mouseOverButton].type.getColor());
		}
		if (mouseX > width / 2 + 137 && mouseX < width / 2 + 148 && mouseY > height - 11 && mouseY < height - 1)
			drawImageQuad(guiIndex, width / 2 + 137, height - 11, 11, 10, 613f / 640f, 151f / 480f, 635f / 640f, 171f / 480f);

	}

	private int mouseOverButton = 0;

	private void drawButton(int x, int y, int buttonWidth, int buttonHeight, String string, int mouseX, int mouseY, int guiIndex, int ind) {
		if (mode == BattleMode.MainMenu) {
			if (mouseX > x && mouseX < x + buttonWidth && mouseY > y && mouseY < y + buttonHeight) {
				drawImageQuad(guiIndex, x, y, buttonWidth, buttonHeight, 387f / 640f, 158f / 480f, 489f / 640f, 196f / 480f);
			}
			drawCenteredString(fontRenderer, string, x + buttonWidth / 2, y + buttonHeight / 2 - 3, 0xFFFFFF);
		} else if (mode == BattleMode.ChooseAttack) {
			drawImageQuad(guiIndex, x, y, buttonWidth, buttonHeight, 206f / 640f, 152f / 480f, 393f / 640f, 202f / 480f);
			if (mouseX > x && mouseX < x + buttonWidth && mouseY > y && mouseY < y + buttonHeight) {
				drawImageQuad(guiIndex, x + 2, y + 2, buttonWidth - 5, buttonHeight - 4, 23f / 640f, 155f / 480f, 200f / 640f, 195f / 480f);
				mouseOverButton = ind;
			}
			drawCenteredString(fontRenderer, string, x + buttonWidth / 2, y + buttonHeight / 2 - 3, 0xFFFFFF);
		}
	}

	@Override
	public void handleKeyboardInput() {
		return;
	}

	@Override
	protected void mouseClicked(int mouseX, int mouseY, int mouseButton) {
		if (ClientBattleManager.hasMoreMessages()) {
			ClientBattleManager.removeMessage();
			return;
		}
		if (mode == BattleMode.YesNo)
			YesNoDialogClicked(mouseX, mouseY);
		else if (ClientBattleManager.hasLevelUps() || ClientBattleManager.hasNewAttacks()) {
			if (!ClientBattleManager.hasNewAttacks()
					|| (ClientBattleManager.hasLevelUps() && ClientBattleManager.levelUpList.get(0).level <= ClientBattleManager.newAttackList.get(0).level))
				LevelUpClick(mouseX, mouseY);
			else
				ReplaceAttackClicked(mouseX, mouseY);
		} else if (mode == BattleMode.MainMenu) {
			int x1 = width / 2 + 31;
			int y1 = height - guiHeight + 9;
			int x2 = width / 2 + 90;
			int y2 = height - guiHeight + 35;
			int w = 48, h = 16;
			if (mouseX > x1 && mouseX < x1 + w && mouseY > y1 && mouseY < y1 + h) {
				mode = BattleMode.ChooseAttack;
				Minecraft.getMinecraft().renderViewEntity = ClientBattleManager.getUserPokemon();
			} else if (mouseX > x2 && mouseX < x2 + w && mouseY > y1 && mouseY < y1 + h)
				mode = BattleMode.ChoosePokemon;
			else if (mouseX > x1 && mouseX < x1 + w && mouseY > y2 && mouseY < y2 + h) {
				mode = BattleMode.ChooseBag;
			} else if (mouseX > x2 && mouseX < x2 + w && mouseY > y2 && mouseY < y2 + h) {
				if (ClientBattleManager.opponentType == ParticipantType.WildPokemon) {
					PacketDispatcher.sendPacketToServer(PacketCreator.createPacket(EnumPackets.Flee, 0));
					mode = BattleMode.Waiting;
				} else {
					ClientBattleManager.addMessage("You can't run from a trainer battle!");
				}
			}
			return;

		} else if (mode == BattleMode.ChooseAttack) {
			ChooseAttackClick(mouseX, mouseY);
		} else if (mode == BattleMode.ChoosePokemon || mode == BattleMode.EnforcedSwitch) {
			ChoosePokemonClick(mouseX, mouseY);
		} else if (mode == BattleMode.ApplyToPokemon) {
			ApplyToPokemonClick(mouseX, mouseY);
		} else if (mode == BattleMode.ChooseBag) {
			ChooseBagClick(mouseX, mouseY);
		} else if (mode == BattleMode.UseBag) {
			UseBagClick(mouseX, mouseY);
		}
		super.mouseClicked(mouseX, mouseY, mouseButton);
	}

	private void LevelUpClick(int mouseX, int mouseY) {
		if (drawLevelStage == LevelStage.First)
			drawLevelStage = LevelStage.Second;
		else {
			ClientBattleManager.levelUpList.remove(0);
			if (ClientBattleManager.hasLevelUps())
				drawLevelStage = LevelStage.First;
			else {
				if (battleControllerIndex == -1 && !ClientBattleManager.hasNewAttacks()) {
					if (!wasThirdPerson)
						Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
					mc.thePlayer.closeScreen();
					mc.setIngameFocus();
					GuiPixelmonOverlay.isVisible = true;
					return;
				} else {
					mode = oldMode;
				}

			}
		}
	}

	private void ChooseAttackClick(int mouseX, int mouseY) {
		if (mouseX > width / 2 + 137 && mouseX < width / 2 + 148 && mouseY > height - 11 && mouseY < height - 1) {
			mode = BattleMode.MainMenu;
			return;
		}
		int x1 = width / 2 - 141;
		int x2 = width / 2 - 50;
		int y1 = height - guiHeight + 9;
		int y2 = height - guiHeight + 33;
		int w = 87, h = 20;
		if (mouseX > x1 && mouseX < x1 + w && mouseY > y1 && mouseY < y1 + h) {
			PacketDispatcher
					.sendPacketToServer(PacketCreator.createPacket(EnumPackets.ChooseAttack, 0, battleControllerIndex, ClientBattleManager.getUserPokemonPacket().pokemonID));
			mode = BattleMode.Waiting;
			return;
		} else if (mouseX > x2 && mouseX < x2 + w && mouseY > y1 && mouseY < y1 + h) {
			PacketDispatcher
					.sendPacketToServer(PacketCreator.createPacket(EnumPackets.ChooseAttack, 1, battleControllerIndex, ClientBattleManager.getUserPokemonPacket().pokemonID));
			mode = BattleMode.Waiting;
			return;
		} else if (mouseX > x1 && mouseX < x1 + w && mouseY > y2 && mouseY < y2 + h) {
			PacketDispatcher
					.sendPacketToServer(PacketCreator.createPacket(EnumPackets.ChooseAttack, 2, battleControllerIndex, ClientBattleManager.getUserPokemonPacket().pokemonID));
			mode = BattleMode.Waiting;
			return;
		} else if (mouseX > x2 && mouseX < x2 + w && mouseY > y2 && mouseY < y2 + h) {
			PacketDispatcher
					.sendPacketToServer(PacketCreator.createPacket(EnumPackets.ChooseAttack, 3, battleControllerIndex, ClientBattleManager.getUserPokemonPacket().pokemonID));
			mode = BattleMode.Waiting;
			return;
		}
	}

	private void ChoosePokemonClick(int mouseX, int mouseY) {
		Minecraft.getMinecraft().renderViewEntity = Minecraft.getMinecraft().thePlayer;
		if (mode != BattleMode.EnforcedSwitch && mouseX > width / 2 + 63 && mouseX < width / 2 + 63 + 48 && mouseY > height - 27 && mouseY < height - 27 + 17) {
			mode = BattleMode.MainMenu;
			return;
		}
		int pos = 0;
		for (int i = 0; i < 6; i++) {
			if (i != ClientBattleManager.getUserPokemonPacket().order) {
				PixelmonDataPacket pdata = ServerStorageDisplay.pokemon[i];
				if (pdata != null) {
					int xpos = width / 2 - 30;
					int ypos = height - 195 + pos * 30;
					if (mouseX > xpos && mouseX < xpos + 150 && mouseY > ypos + 1 && mouseY < ypos + 31) {
						if (!pdata.isFainted) {
							PacketDispatcher.sendPacketToServer(PacketCreator.createPacket(EnumPackets.SwitchPokemon, pdata.order, battleControllerIndex, 0));
							mode = BattleMode.Waiting;
							return;
						}
					}
				}
				pos++;
			}
		}
	}

	private void YesNoDialogClicked(int mouseX, int mouseY) {
		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 108 && mouseY > height / 2 - 33 && mouseY < height / 2 - 7) {
			if (selectedAttack != -1) {
				PacketDispatcher.sendPacketToServer(sendPacket);
			}
			ClientBattleManager.newAttackList.remove(0);
			if (battleControllerIndex != -1 || ClientBattleManager.hasNewAttacks() || ClientBattleManager.hasLevelUps())
				mode = oldMode;
			else {
				if (!wasThirdPerson)
					Minecraft.getMinecraft().gameSettings.thirdPersonView = 0;
				mc.thePlayer.closeScreen();
				mc.setIngameFocus();
				GuiPixelmonOverlay.isVisible = true;
				return;
			}
		}
		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 108 && mouseY > height / 2 + 5 && mouseY < height / 2 + 31) {
			mode = oldMode;
		}
	}

	private Packet250CustomPayload sendPacket;

	public static BattleMode oldMode;

	private void ReplaceAttackClicked(int mouseX, int mouseY) {
		Attack newAttack = ClientBattleManager.newAttackList.get(0).attack;
		PixelmonDataPacket pokemonToLearnAttack = ServerStorageDisplay.get(ClientBattleManager.newAttackList.get(0).pokemonID);

		for (int i = 0; i < pokemonToLearnAttack.numMoves; i++) {
			if (mouseX > width / 2 - 30 && mouseX < width / 2 + 120 && mouseY > height / 2 - 94 + 22 * i && mouseY < height / 2 - 94 + 22 * (i + 1)) {
				sendPacket = PacketCreator.createPacket(EnumPackets.ReplaceMove, pokemonToLearnAttack.pokemonID, newAttack.baseAttack.attackIndex, i);
				selectedAttack = i;
				oldMode = mode;
				mode = BattleMode.YesNo;
			}
		}

		if (mouseX > width / 2 - 30 && mouseX < width / 2 + 120 && mouseY > height / 2 + 3 && mouseY < height / 2 + 25) {
			oldMode = mode;
			mode = BattleMode.YesNo;
			selectedAttack = -1;
		}
	}

	private void ApplyToPokemonClick(int mouseX, int mouseY) {
		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 63 + 48 && mouseY > height - 27 && mouseY < height - 27 + 17) {
			mode = BattleMode.UseBag;
			return;
		}
		int pokemonToApplyTo = -1;
		if (mouseX > width / 2 - 120 && mouseX < width / 2 - 21 && mouseY > height - 165 && mouseY < height - 113) {
			pokemonToApplyTo = ClientBattleManager.getUserPokemonPacket().order;
		}
		int pos = 0;
		for (int i = 0; i < 6; i++) {
			if (i != ClientBattleManager.getUserPokemonPacket().order) {
				PixelmonDataPacket pdata = ServerStorageDisplay.pokemon[i];
				if (pdata != null) {
					int xpos = width / 2 - 30;
					int ypos = height - 195 + pos * 30;
					if (mouseX > xpos && mouseX < xpos + 150 && mouseY > ypos + 1 && mouseY < ypos + 31) {
						pokemonToApplyTo = i;
					}
				}
				pos++;
			}
		}
		if (pokemonToApplyTo != -1) {
			if (ServerStorageDisplay.pokemon[pokemonToApplyTo].isFainted// &&
																		// PixelmonItems.getItem(itemToUse.id)
																		// instanceof
																		// ItemRevive)
			)
				return;
			if (PixelmonItems.getItem(itemToUse.id) instanceof ItemPotion) {
				pixelmonToHeal = ServerStorageDisplay.pokemon[pokemonToApplyTo];
				healAmount = pixelmonToHeal.health + ((ItemPotion) PixelmonItems.getItem(itemToUse.id)).type.getHealAmount();
				Timer timer = new Timer();
				timer.scheduleAtFixedRate(new HealerTask(), 100, 100);
			}
		}
	}

	private ItemData itemToUse = null;

	private void UseBagClick(int mouseX, int mouseY) {
		if (mouseX > width / 2 - 11 && mouseX < width / 2 + 6 && mouseY > height / 2 - 55 && mouseY < height / 2 - 45) {
			if (startIndex > 0) {
				startIndex--;
				return;
			}
		}
		if (mouseX > width / 2 - 11 && mouseX < width / 2 + 6 && mouseY > height / 2 + 82 && mouseY < height / 2 + 92) {
			if (startIndex + 6 < ClientBattleManager.bagStore.size() - 1) {
				startIndex++;
				return;
			}
		}

		if (mouseX > width / 2 + 63 && mouseX < width / 2 + 111 && mouseY > height / 2 - 91 && mouseY < height / 2 - 74) {
			mode = BattleMode.ChooseBag;
			return;
		}

		for (int i = startIndex; i < 6 + startIndex; i++) {
			if (i < ClientBattleManager.bagStore.size()) {
				if (mouseX > width / 2 - 98 && mouseX < width / 2 - 98 + 187 && mouseY > height / 2 - 44 + (i - startIndex) * 21
						&& mouseY < height / 2 - 24 + (i - startIndex) * 21) {
					itemToUse = ClientBattleManager.bagStore.get(i);
					if (bagSection == bagSection.Pokeballs) {
						PacketDispatcher.sendPacketToServer(PacketCreator.createPacket(EnumPackets.BagPacket, ClientBattleManager.bagStore.get(i).id, battleControllerIndex, 0));
						mode = BattleMode.Waiting;
					} else {
						mode = BattleMode.ApplyToPokemon;
					}
					ItemStack[] inv = Minecraft.getMinecraft().thePlayer.inventory.mainInventory;
					((PixelmonItem) PixelmonItems.getItem(itemToUse.id)).removeFromInventory(inv);
				}
			}
		}
	}

	private void ChooseBagClick(int mouseX, int mouseY) {
		if (mouseX > width / 2 + 106 && mouseX < width / 2 + 126 && mouseY > height / 2 + 55 && mouseY < height / 2 + 77)
			mode = BattleMode.MainMenu;

		int x1, x2, y1, y2;
		x1 = width / 2 - 103;
		x2 = width / 2 + 3;
		y1 = height / 2 - 63;
		y2 = height / 2 + 4;
		int buttonWidth = 100, buttonHeight = 62;
		bagSection = null;
		if (mouseX > x1 && mouseX < x1 + buttonWidth && mouseY > y1 && mouseY < y1 + buttonHeight)
			bagSection = BagSection.StatusRestore;

		else if (mouseX > x1 && mouseX < x1 + buttonWidth && mouseY > y2 && mouseY < y2 + buttonHeight)
			bagSection = BagSection.BattleItems;

		else if (mouseX > x2 && mouseX < x2 + buttonWidth && mouseY > y1 && mouseY < y1 + buttonHeight) {
			if (ClientBattleManager.opponentType == ParticipantType.WildPokemon)
				bagSection = BagSection.Pokeballs;
			else {
				ClientBattleManager.addMessage("You can't use Poke Balls in this battle!");
			}
		}

		else if (mouseX > x2 && mouseX < x2 + buttonWidth && mouseY > y2 && mouseY < y2 + buttonHeight)
			bagSection = BagSection.HP;

		if (bagSection != null) {
			mode = BattleMode.UseBag;
			ClientBattleManager.bagStore.clear();
			getInventory();
			startIndex = 0;
		}
	}

	private PixelmonDataPacket pixelmonToHeal = null;

	class HealerTask extends TimerTask {
		public void run() {
			pixelmonToHeal.health++;
			if (pixelmonToHeal.health >= pixelmonToHeal.hp)
				this.cancel();
		}
	}

	@Override
	public void drawBackground(int par1) {
	}

	@Override
	public void drawDefaultBackground() {
	}

	private void drawImageQuad(int textureHandle, int x, int y, float w, float h, float us, float vs, float ue, float ve) {
		// activate the specified texture
		GL11.glBindTexture(GL11.GL_TEXTURE_2D, textureHandle);

		float var7 = 0.00390625F;
		float var8 = 0.00390625F;
		Tessellator var9 = Tessellator.instance;
		var9.startDrawingQuads();
		var9.addVertexWithUV((double) (x + 0), (double) (y + h), (double) this.zLevel, (double) ((float) us), (double) ((float) ve));
		var9.addVertexWithUV((double) (x + w), (double) (y + h), (double) this.zLevel, (double) ((float) ue), (double) ((float) ve));
		var9.addVertexWithUV((double) (x + w), (double) (y + 0), (double) this.zLevel, (double) ((float) ue), (double) ((float) vs));
		var9.addVertexWithUV((double) (x + 0), (double) (y + 0), (double) this.zLevel, (double) ((float) us), (double) ((float) vs));
		var9.draw();
	}

	private void getInventory() {
		InventoryPlayer inventory = Minecraft.getMinecraft().thePlayer.inventory;
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (bagSection == BagSection.Pokeballs) {
				if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() instanceof ItemPokeBall)
					checkExists(inventory.mainInventory[i].itemID, inventory.mainInventory[i].stackSize);
			} else if (bagSection == BagSection.HP) {
				if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() instanceof ItemPotion)
					checkExists(inventory.mainInventory[i].itemID, inventory.mainInventory[i].stackSize);
				else if (inventory.mainInventory[i] != null && inventory.mainInventory[i].getItem() instanceof ItemEther)
					checkExists(inventory.mainInventory[i].itemID, inventory.mainInventory[i].stackSize);
			}
		}
	}

	private void removeItem(ItemData itemToUse) {
		InventoryPlayer inventory = Minecraft.getMinecraft().thePlayer.inventory;
		for (int i = 0; i < inventory.mainInventory.length; i++) {
			if (inventory.mainInventory[i] != null && inventory.mainInventory[i].itemID == itemToUse.id) {
				inventory.mainInventory[i].stackSize--;
				return;
			}
		}
	}

	private void checkExists(int itemID, int count) {
		boolean hasItem = false;
		for (ItemData d : ClientBattleManager.bagStore) {
			if (d.id == itemID) {
				hasItem = true;
				d.count += count;
			}
		}
		if (!hasItem)
			ClientBattleManager.bagStore.add(new ItemData(itemID, count));

	}
}

