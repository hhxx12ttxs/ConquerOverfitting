/**
 * Copyright 2013 briman0094 (Briman)
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 * 
 * Amendment 1: This mod may not, under any circumstances, be included
 * in any Technic pack, whether it is an official pack or a pack distributed
 * through Technic Platform. There will be no exceptions.
 * 
 */

package com.briman0094.mineforever.client.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import com.briman0094.mineforever.container.ContainerCardPrinter;
import com.briman0094.mineforever.proxy.ClientProxy;
import com.briman0094.mineforever.proxy.CommonProxy;
import com.briman0094.mineforever.tile.TileEntityCardPrinter;

import cpw.mods.fml.common.network.PacketDispatcher;

public class GuiCardPrinter extends GuiContainer
{
	private static final String		TEXTURE	= "guis/gui_cardprinter.png";
	
	private static final int		xSize	= 176;
	private static final int		ySize	= 192;
	private static final int		NAME_X	= 72;
	private static final int		NAME_Y	= 36;
	private static final int		PROF_X	= 72;
	private static final int		PROF_Y	= 50;
	private static final int		NAME_W	= 92;
	private static final int		NAME_H	= 10;
	private static final int		PROF_W	= 92;
	private static final int		PROF_H	= 10;
	private static final int		LNAME_X	= 10;
	private static final int		LNAME_Y	= 37;
	private static final int		LPROF_X	= 10;
	private static final int		LPROF_Y	= 51;
	private static final int		LINFO_X	= 48;
	private static final int		LINFO_Y	= 65;
	private static final int		PROG_X	= 77;
	private static final int		PROG_Y	= 14;
	private static final int		PROG_W	= 24;
	private static final int		PROG_H	= 17;
	private static final int		PROG_SX	= 176;
	private static final int		PROG_SY	= 0;
	private static final int		B_PRINT	= 0;
	private static final int		PRINT_X	= 56;
	private static final int		PRINT_Y	= 82;
	private static final int		PRINT_W	= 64;
	private static final int		PRINT_H	= 20;
	
	private EntityPlayer			player;
	private TileEntityCardPrinter	tileEntity;
	private int						drawX;
	private int						drawY;
	
	private GuiDigitalTextField		nameField;
	private GuiDigitalTextField		profField;
	private GuiButton				print;
	
	public GuiCardPrinter(EntityPlayer player, TileEntityCardPrinter tileEntity)
	{
		super(new ContainerCardPrinter(player.inventory, tileEntity));
		this.player = player;
		this.tileEntity = tileEntity;
		
		mc = Minecraft.getMinecraft();
		fontRenderer = mc.fontRenderer;
	}
	
	@Override
	public void initGui()
	{
		super.initGui();
		
		allowUserInput = true;
		Keyboard.enableRepeatEvents(true);
		
		drawX = (width - xSize) / 2;
		drawY = (height - ySize) / 2;
		
		nameField = new GuiDigitalTextField(fontRenderer, NAME_X + drawX, NAME_Y + drawY, NAME_W, NAME_H);
		profField = new GuiDigitalTextField(fontRenderer, PROF_X + drawX, PROF_Y + drawY, PROF_W, PROF_H);
		nameField.setEnableBackgroundDrawing(false);
		profField.setEnableBackgroundDrawing(false);
		
		print = new GuiButton(B_PRINT, PRINT_X + drawX, PRINT_Y + drawY, PRINT_W, PRINT_H, "Print");
		print.enabled = false;
		
		buttonList.add(print);
	}
	
	@Override
	public void onGuiClosed()
	{
		Keyboard.enableRepeatEvents(false);
		super.onGuiClosed();
	}
	
	@Override
	public void updateScreen()
	{
		super.updateScreen();
		print.enabled = tileEntity.canPrint() && !nameField.getText().isEmpty() && !profField.getText().isEmpty();
	}
	
	@Override
	protected void mouseClicked(int xPos, int yPos, int button)
	{
		// System.out.println("X: " + Integer.toString(xPos) + "; Y: " +
		// Integer.toString(yPos) + "; Button: " + Integer.toString(button));
		super.mouseClicked(xPos, yPos, button);
		nameField.mouseClicked(xPos, yPos, button);
		profField.mouseClicked(xPos, yPos, button);
	}
	
	@Override
	protected void keyTyped(char character, int keycode)
	{
		// System.out.println("Char: " + character + "; Keycode: " +
		// Integer.toString(keycode));
		if (character == '\t')
		{
			if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT) || Keyboard.isKeyDown(Keyboard.KEY_RSHIFT))
			{
				if (profField.isFocused() && !nameField.isFocused())
				{
					nameField.setFocused(true);
					profField.setFocused(false);
				}
			}
			else
			{
				if (!profField.isFocused() && nameField.isFocused())
				{
					nameField.setFocused(false);
					profField.setFocused(true);
				}
			}
		}
		else
		{
			if (!nameField.textboxKeyTyped(character, keycode) && !profField.textboxKeyTyped(character, keycode))
				super.keyTyped(character, keycode);
		}
	}
	
	public static void drawOutlineRect(int x1, int y1, int x2, int y2, float rR, float rG, float rB, float lR, float lG, float lB)
	{
		/*
		 * int temp;
		 * 
		 * if (x1 < x2)
		 * {
		 * temp = x1;
		 * x1 = x2;
		 * x2 = temp;
		 * }
		 * 
		 * if (y1 < y2)
		 * {
		 * temp = y1;
		 * y1 = y2;
		 * y2 = temp;
		 * }
		 */
		Tessellator tesselator = Tessellator.instance;
		GL11.glEnable(GL11.GL_BLEND);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		GL11.glColor4f(rR, rG, rB, 1f);
		// background
		tesselator.startDrawingQuads();
		tesselator.addVertex((double) x1, (double) y2, 0.0D);
		tesselator.addVertex((double) x2, (double) y2, 0.0D);
		tesselator.addVertex((double) x2, (double) y1, 0.0D);
		tesselator.addVertex((double) x1, (double) y1, 0.0D);
		tesselator.draw();
		// outline
		GL11.glColor4f(lR, lG, lB, 1f);
		tesselator.startDrawingQuads();
		tesselator.addVertex((double) x1, (double) y1, 0.0D);
		tesselator.addVertex((double) x1, (double) y2, 0.0D);
		tesselator.addVertex((double) x1 + 1, (double) y2, 0.0D);
		tesselator.addVertex((double) x1 + 1, (double) y1, 0.0D);
		tesselator.draw();
		tesselator.startDrawingQuads();
		tesselator.addVertex((double) x2 - 1, (double) y1, 0.0D);
		tesselator.addVertex((double) x2 - 1, (double) y2, 0.0D);
		tesselator.addVertex((double) x2, (double) y2, 0.0D);
		tesselator.addVertex((double) x2, (double) y1, 0.0D);
		tesselator.draw();
		tesselator.startDrawingQuads();
		tesselator.addVertex((double) x1, (double) y1, 0.0D);
		tesselator.addVertex((double) x1, (double) y1 + 1, 0.0D);
		tesselator.addVertex((double) x2, (double) y1 + 1, 0.0D);
		tesselator.addVertex((double) x2, (double) y1, 0.0D);
		tesselator.draw();
		tesselator.startDrawingQuads();
		tesselator.addVertex((double) x1, (double) y2 - 1, 0.0D);
		tesselator.addVertex((double) x1, (double) y2, 0.0D);
		tesselator.addVertex((double) x2, (double) y2, 0.0D);
		tesselator.addVertex((double) x2, (double) y2 - 1, 0.0D);
		tesselator.draw();
		
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		GL11.glDisable(GL11.GL_BLEND);
	}
	
	@Override
	protected void actionPerformed(GuiButton button)
	{
		if (button.id == B_PRINT)
		{
			if (print.enabled)
			{
				tileEntity.startPrinting(nameField.getText(), profField.getText());
				NBTTagCompound packetData = new NBTTagCompound();
				packetData.setInteger("action", CommonProxy.PACKET_PRINTER_PRINT);
				packetData.setString("name", nameField.getText());
				packetData.setString("prof", profField.getText());
				packetData.setInteger("x", tileEntity.xCoord);
				packetData.setInteger("y", tileEntity.yCoord);
				packetData.setInteger("z", tileEntity.zCoord);
				PacketDispatcher.sendPacketToServer(CommonProxy.createPacket(packetData));
			}
		}
	}
	
	@Override
	protected void drawGuiContainerBackgroundLayer(float scale, int cursorX, int cursorY)
	{
		ClientProxy.bindTexture(TEXTURE);
		drawTexturedModalRect(drawX, drawY, 0, 0, xSize, ySize);
		drawTexturedModalRect(drawX + PROG_X, drawY + PROG_Y, PROG_SX, PROG_SY, (int) (tileEntity.getProgress() * (float) PROG_W), PROG_H);
		drawOutlineRect(drawX + 7, drawY + 34, (drawX + xSize) - 7, (drawY + 80) - 4, 0, 1f / 8f, 0, 0.5f, 0.5f, 0.5f);
		fontRenderer.drawString("Name:", LNAME_X + drawX, LNAME_Y + drawY, 0x00FF00, false);
		fontRenderer.drawString("Profession:", LPROF_X + drawX, LPROF_Y + drawY, 0x00FF00, false);
		// fontRenderer.drawString(nameField.getText(), NAME_X + drawX, NAME_Y +
		// drawY, 0x00FF00, false);
		// fontRenderer.drawString(profField.getText(), PROF_X + drawX, PROF_Y +
		// drawY, 0x00FF00, false);
		fontRenderer.drawString("ID CARD PRINTER", LINFO_X + drawX, LINFO_Y + drawY, 0x00FF00, false);
		nameField.drawTextBox();
		profField.drawTextBox();
	}
}

