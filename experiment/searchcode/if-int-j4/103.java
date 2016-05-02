/** FILE GuiInventory **/
/** fichier modifie par chaipokoi le 11/08/2012 */
package net.minecraft.src;

import java.util.*;
import net.minecraft.client.Minecraft;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

public class GuiInventory extends GuiContainer
{
    /**
     * x size of the inventory window in pixels. Defined as float, passed as int
     */
    private float xSize_lo;

    /**
     * y size of the inventory window in pixels. Defined as float, passed as int.
     */
    private float ySize_lo;

    public GuiInventory(EntityPlayer par1EntityPlayer)
    {
        super(par1EntityPlayer.inventorySlots);
        allowUserInput = true;
        par1EntityPlayer.addStat(AchievementList.openInventory, 1);
    }

    /**
     * Called from the main game loop to update the screen.
     */
    public void updateScreen()
    {
        if (mc.playerController.isInCreativeMode())
        {
            mc.displayGuiScreen(new GuiContainerCreative(mc.thePlayer));
        }
    }

    /**
     * Adds the buttons (and other controls) to the screen in question.
     */
    public void initGui()
    {
        controlList.clear();

        if (mc.playerController.isInCreativeMode())
        {
            mc.displayGuiScreen(new GuiContainerCreative(mc.thePlayer));
        }
        else
        {
            super.initGui();

            if (!mc.thePlayer.getActivePotionEffects().isEmpty())
            {
                guiLeft = 160 + (width - xSize - 200) / 2;
            }
        }
    }

    /**
     * Draw the foreground layer for the GuiContainer (everythin in front of the items)
     * MODIF GuiInventory: drawGuiContainerForegroundLayer()
     */
    protected void drawGuiContainerForegroundLayer()
    {
	/**
        fontRenderer.drawString(StatCollector.translateToLocal("container.crafting"), 86, 16, 0x404040);
	**/
    }

    /**
     * Draws the screen and all the components in it.
     */
    public void drawScreen(int par1, int par2, float par3)
    {
        drawDefaultBackground();
        int i = guiLeft;
        int j = guiTop;
        drawGuiContainerBackgroundLayer(par3, par1, par2);
        RenderHelper.enableGUIStandardItemLighting();
        GL11.glPushMatrix();
        GL11.glTranslatef(i, j, 0.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        Slot slot = null;
        int k = 240;
        int i1 = 240;
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, (float)k / 1.0F, (float)i1 / 1.0F);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);

        for (int l = 0; l < inventorySlots.inventorySlots.size(); l++)
        {
            Slot slot1 = (Slot)inventorySlots.inventorySlots.get(l);
            drawSlotInventory(slot1);

            if (isMouseOverSlot(slot1, par1, par2))
            {
                slot = slot1;
                GL11.glDisable(GL11.GL_LIGHTING);
                GL11.glDisable(GL11.GL_DEPTH_TEST);
                int j1 = slot1.xDisplayPosition;
                int k1 = slot1.yDisplayPosition;
                int additionalSide=12;
                if(slot1.inventory instanceof InventoryCrafting || slot1.isASlotCrafting || slot1.isASlotArmor)
                {
						additionalSide=10;
				}
                drawGradientRect(j1, k1, j1 + additionalSide, k1 + additionalSide, 0x80ffffff, 0x80ffffff);
                GL11.glEnable(GL11.GL_LIGHTING);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            }
        }

        drawGuiContainerForegroundLayer();
        InventoryPlayer inventoryplayer = mc.thePlayer.inventory;

        if (inventoryplayer.getItemStack() != null)
        {
            GL11.glTranslatef(0.0F, 0.0F, 32F);
            zLevel = 200F;
            itemRenderer.zLevel = 200F;
            itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, inventoryplayer.getItemStack(), par1 - i - 8, par2 - j - 8);
            itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, inventoryplayer.getItemStack(), par1 - i - 8, par2 - j - 8);
            zLevel = 0.0F;
            itemRenderer.zLevel = 0.0F;
        }

        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        if (inventoryplayer.getItemStack() == null && slot != null && slot.getHasStack())
        {
            ItemStack itemstack = slot.getStack();
            List list = itemstack.getItemNameandInformation();

            if (list.size() > 0)
            {
                int l1 = 0;

                for (int i2 = 0; i2 < list.size(); i2++)
                {
                    int k2 = fontRenderer.getStringWidth((String)list.get(i2));

                    if (k2 > l1)
                    {
                        l1 = k2;
                    }
                }

                int j2 = (par1 - i) + 12;
                int l2 = par2 - j - 12;
                int i3 = l1;
                int j3 = 8;

                if (list.size() > 1)
                {
                    j3 += 2 + (list.size() - 1) * 10;
                }

                zLevel = 300F;
                itemRenderer.zLevel = 300F;
                int k3 = 0xf0100010;
                drawGradientRect(j2 - 3, l2 - 4, j2 + i3 + 3, l2 - 3, k3, k3);
                drawGradientRect(j2 - 3, l2 + j3 + 3, j2 + i3 + 3, l2 + j3 + 4, k3, k3);
                drawGradientRect(j2 - 3, l2 - 3, j2 + i3 + 3, l2 + j3 + 3, k3, k3);
                drawGradientRect(j2 - 4, l2 - 3, j2 - 3, l2 + j3 + 3, k3, k3);
                drawGradientRect(j2 + i3 + 3, l2 - 3, j2 + i3 + 4, l2 + j3 + 3, k3, k3);
                int l3 = 0x505000ff;
                int i4 = (l3 & 0xfefefe) >> 1 | l3 & 0xff000000;
                drawGradientRect(j2 - 3, (l2 - 3) + 1, (j2 - 3) + 1, (l2 + j3 + 3) - 1, l3, i4);
                drawGradientRect(j2 + i3 + 2, (l2 - 3) + 1, j2 + i3 + 3, (l2 + j3 + 3) - 1, l3, i4);
                drawGradientRect(j2 - 3, l2 - 3, j2 + i3 + 3, (l2 - 3) + 1, l3, l3);
                drawGradientRect(j2 - 3, l2 + j3 + 2, j2 + i3 + 3, l2 + j3 + 3, i4, i4);

                for (int j4 = 0; j4 < list.size(); j4++)
                {
                    String s = (String)list.get(j4);

                    if (j4 == 0)
                    {
                        s = (new StringBuilder()).append("\247").append(Integer.toHexString(itemstack.getRarity().nameColor)).append(s).toString();
                    }
                    else
                    {
                        s = (new StringBuilder()).append("\2477").append(s).toString();
                    }

                    fontRenderer.drawStringWithShadow(s, j2, l2, -1);

                    if (j4 == 0)
                    {
                        l2 += 2;
                    }

                    l2 += 10;
                }

                zLevel = 0.0F;
                itemRenderer.zLevel = 0.0F;
            }
        }

        GL11.glPopMatrix();
        super.callParentDrawScreen(par1, par2, par3);
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        xSize_lo = par1;
        ySize_lo = par2;
    }

    /**
     * Draw the background layer for the GuiContainer (everything behind the items)
     * MODIF GuiInventory: drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
     */
    protected void drawGuiContainerBackgroundLayer(float par1, int par2, int par3)
    {
    	// ratio de: 
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/Tsukeyithan/Texture/GuiInventory.png"));
        int j = guiLeft;
        int k = guiTop;
        DrawTexturedRect(this.width/2-566/2, this.height/2-318/2, 566, 318);
        // joueur 3D
	/**
        displayDebuffEffects();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
        GL11.glEnable(GL11.GL_COLOR_MATERIAL);
        GL11.glPushMatrix();
        GL11.glTranslatef(this.width/2, k-35, 50F);
        float f = 45F;
        GL11.glScalef(-f, f, f);
        GL11.glRotatef(180F, 0.0F, 0.0F, 1.0F);
        float f1 = mc.thePlayer.renderYawOffset;
        float f2 = mc.thePlayer.rotationYaw;
        float f3 = mc.thePlayer.rotationPitch;
        float f4 = (float)(j + 51) - xSize_lo;
        float f5 = (float)((k + 75) - 50) - ySize_lo;
        GL11.glRotatef(135F, 0.0F, 1.0F, 0.0F);
        RenderHelper.enableStandardItemLighting();
        GL11.glRotatef(-135F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(-(float)Math.atan(f5 / 40F) * 20F, 1.0F, 0.0F, 0.0F);
        mc.thePlayer.renderYawOffset = (float)Math.atan(f4 / 40F) * 20F;
        mc.thePlayer.rotationYaw = (float)Math.atan(f4 / 40F) * 40F;
        mc.thePlayer.rotationPitch = -(float)Math.atan(f5 / 40F) * 20F;
        mc.thePlayer.rotationYawHead = mc.thePlayer.rotationYaw;
        GL11.glTranslatef(0.0F, mc.thePlayer.yOffset, 0.0F);
        RenderManager.instance.playerViewY = 180F;
        RenderManager.instance.renderEntityWithPosYaw(mc.thePlayer, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        mc.thePlayer.renderYawOffset = f1;
        mc.thePlayer.rotationYaw = f2;
        mc.thePlayer.rotationPitch = f3;
        GL11.glPopMatrix();
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
	**/
    }
    
    
     /**
     * Draws an inventory slot
     */
    protected void drawSlotInventory(Slot par1Slot)
    {
        int i = par1Slot.xDisplayPosition;
        int j = par1Slot.yDisplayPosition;
        ItemStack itemstack = par1Slot.getStack();
        boolean flag = false;
        int k = i;
        int l = j;
        zLevel = 100F;
        itemRenderer.zLevel = 100F;

        if (itemstack == null)
        {
            int i1 = par1Slot.getBackgroundIconIndex();
			/**
            if (i1 >= 0)
            {
                GL11.glDisable(GL11.GL_LIGHTING);
                mc.renderEngine.bindTexture(mc.renderEngine.getTexture("/gui/items.png"));
                drawTexturedModalRect(k, l, (i1 % 16) * 16, (i1 / 16) * 16, 16, 16);
                GL11.glEnable(GL11.GL_LIGHTING);
                flag = true;
            }
            * */
        }

        if (!flag)
        {
			if(par1Slot.inventory instanceof InventoryCrafting || par1Slot.isASlotCrafting || par1Slot.isASlotArmor)
			{
				itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, itemstack, k-2, l-2,10,10);	
				itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, itemstack, k-2, l-2);			
			}
			else
			{
				itemRenderer.renderItemIntoGUI(fontRenderer, mc.renderEngine, itemstack, k, l-1);
				itemRenderer.renderItemOverlayIntoGUI(fontRenderer, mc.renderEngine, itemstack, k, l-1);
			}
        }

        itemRenderer.zLevel = 0.0F;
        zLevel = 0.0F;
    }


    /**
     * Fired when a control is clicked. This is the equivalent of ActionListener.actionPerformed(ActionEvent e).
     */
    protected void actionPerformed(GuiButton par1GuiButton)
    {
        if (par1GuiButton.id == 0)
        {
            mc.displayGuiScreen(new GuiAchievements(mc.statFileWriter));
        }

        if (par1GuiButton.id == 1)
        {
            mc.displayGuiScreen(new GuiStats(this, mc.statFileWriter));
        }
    }

    /**
     * Displays debuff/potion effects that are currently being applied to the player
     */
    private void displayDebuffEffects()
    {
        int i = guiLeft - 124;
        int j = guiTop;
        int k = mc.renderEngine.getTexture("/gui/inventory.png");
        Collection collection = mc.thePlayer.getActivePotionEffects();

        if (collection.isEmpty())
        {
            return;
        }

        int l = 33;

        if (collection.size() > 5)
        {
            l = 132 / (collection.size() - 1);
        }

        for (Iterator iterator = mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext();)
        {
            PotionEffect potioneffect = (PotionEffect)iterator.next();
            Potion potion = Potion.potionTypes[potioneffect.getPotionID()];
            GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
            mc.renderEngine.bindTexture(k);
            drawTexturedModalRect(i, j, 0, ySize, 140, 32);

            if (potion.hasStatusIcon())
            {
                int i1 = potion.getStatusIconIndex();
                drawTexturedModalRect(i + 6, j + 7, 0 + (i1 % 8) * 18, ySize + 32 + (i1 / 8) * 18, 18, 18);
            }

            String s = StatCollector.translateToLocal(potion.getName());

            if (potioneffect.getAmplifier() == 1)
            {
                s = (new StringBuilder()).append(s).append(" II").toString();
            }
            else if (potioneffect.getAmplifier() == 2)
            {
                s = (new StringBuilder()).append(s).append(" III").toString();
            }
            else if (potioneffect.getAmplifier() == 3)
            {
                s = (new StringBuilder()).append(s).append(" IV").toString();
            }

            fontRenderer.drawStringWithShadow(s, i + 10 + 18, j + 6, 0xffffff);
            String s1 = Potion.getDurationString(potioneffect);
            fontRenderer.drawStringWithShadow(s1, i + 10 + 18, j + 6 + 10, 0x7f7f7f);
            j += l;
        }
    }
}

