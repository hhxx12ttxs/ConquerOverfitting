package net.minecraft.src;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import net.minecraft.client.Minecraft;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class mod_cjb_moreinfo extends BaseMod {
	
	private long time;
	private boolean keypressed;
	public static mod_cjb_moreinfo instance;
	
	public mod_cjb_moreinfo()
	{
		ModLoader.setInGameHook(this, true, false);
		CJB.modmoreinfo = true;
		CJB.clock12h = CJB_Settings.getBoolean("moreinfo.clock12h", false);
		CJB.bigicons = CJB_Settings.getBoolean("moreinfo.bigicons", false);
		CJB.hideicons = CJB_Settings.getBoolean("moreinfo.hideicons", false);
		CJB.showday = CJB_Settings.getBoolean("moreinfo.showday", true);
		CJB.showtime = CJB_Settings.getBoolean("moreinfo.showtime", true);
		CJB.showfps = CJB_Settings.getBoolean("moreinfo.showfps", false);
		CJB.showlightlevel = CJB_Settings.getBoolean("moreinfo.showlightlevel", false);
		CJB.showbiome = CJB_Settings.getBoolean("moreinfo.showbiome", false);
		CJB.showarrowcount = CJB_Settings.getBoolean("moreinfo.showarrowcount", true);
		CJB.showitemdamage = CJB_Settings.getBoolean("moreinfo.showitemdamage", true);
		CJB.showrain = CJB_Settings.getBoolean("moreinfo.showrain", true);
		CJB.showthunder = CJB_Settings.getBoolean("moreinfo.showthunder", true);
		CJB.showslimes = CJB_Settings.getBoolean("moreinfo.showslimes", true);
		CJB.showslimes = CJB_Settings.getBoolean("moreinfo.showslimes", true);
		CJB.showmobinfo = CJB_Settings.getBoolean("moreinfo.showmobinfo", true);
		CJB.showcoords = CJB_Settings.getBoolean("moreinfo.showcoords", true);
		CJB.useskylight = CJB_Settings.getBoolean("moreinfo.useskylight", true);
		CJB.showmobhealth = CJB_Settings.getBoolean("moreinfo.showmobhealth", true);
		CJB.showdebuffs = CJB_Settings.getBoolean("moreinfo.showdebuffs", true);
		
		CJB.drawlines = CJB_Settings.getBoolean("moreinfo.drawlines", true);
		CJB.showcoal = CJB_Settings.getBoolean("moreinfo.showcoal", true);
		CJB.showiron = CJB_Settings.getBoolean("moreinfo.showiron", true);
		CJB.showgold = CJB_Settings.getBoolean("moreinfo.showgold", true);
		CJB.showdiamond = CJB_Settings.getBoolean("moreinfo.showdiamond", true);
		CJB.showlapis = CJB_Settings.getBoolean("moreinfo.showlapis", true);
		CJB.showredstone = CJB_Settings.getBoolean("moreinfo.showredstone", true);
		CJB.renderWidth = CJB_Settings.getInteger("moreinfo.renderwidth", 2);
		CJB.renderHeight = CJB_Settings.getInteger("moreinfo.renderheight", 1);
		
		CJB.position = CJB_Settings.getInteger("moreinfo.position", 0);
		
		instance = this;
	}

	public boolean onTickInGame(float f, Minecraft mc)
	{
		/*if (!CJB.modchat)
			renderMod(mc);*/
		return true;
	}
	
	public void renderMod(Minecraft mc)
	{
		if (mc.currentScreen == null)
		{
			if (Keyboard.getEventKeyState())
			{				
				if (Keyboard.getEventKey() == CJB_Settings.getInteger("moreinfo.showspawnareakey", Keyboard.KEY_L) && !keypressed)
				{
					keypressed = true;
					CJB.showspawnareas = !CJB.showspawnareas;
				}
				if (Keyboard.getEventKey() == CJB_Settings.getInteger("moreinfo.showoreskey", Keyboard.KEY_K) && !keypressed)
				{
					keypressed = true;
					CJB.showores = !CJB.showores;
				}
			}
			if (!Keyboard.getEventKeyState()) {
				keypressed = false;
			}
		}
		
		/*if (!CJB.modchat)
		{
			if (Minecraft.isDebugInfoEnabled() || mc.currentScreen != null)
				return;
			
		} else
		{*/
			if (mc.gameSettings.showDebugInfo || mc.currentScreen instanceof GuiChat)
				return;
		/*}*/
		
		if (mc.theWorld == null)
			return;
		
		EntityPlayer plr = mc.thePlayer;
		World world = mc.theWorld;
		WorldInfo worldinfo = mc.theWorld.worldInfo;
		time = mc.theWorld.worldInfo.getWorldTime();
        int day = (int) (time / 24000) + 1;
        int hours = (int) (time / 1000 % 24 + 6);
        if (hours > 23) hours = (hours - 29 + 5);
        float mins = (float) (time % 24000);
        mins %= 1000;
        mins = mins / 1000 * 60;
        
        
        
        int arrowcount = 0;
        for(ItemStack itemstack : plr.inventory.mainInventory)
        {
        	if(itemstack != null && itemstack.itemID == 262)
        		arrowcount += itemstack.stackSize;
        }
        
        ItemStack curritem = plr.getCurrentEquippedItem();
        int itemdamage = 0;
        String ItemName = "";
        if (curritem != null) {
        	if (curritem.getMaxDamage() > 0)
	        {
	        	itemdamage = curritem.getMaxDamage()-curritem.getItemDamage() + 1;
	        }
        	ItemName = curritem.itemID + ":" + curritem.getItemDamage() + " " + curritem.getItemNameandInformation().get(0);
		    if (ItemName == null || ItemName.equalsIgnoreCase(""))
		    		ItemName = "Unnamed";
	    	
	    }
        
        String clock = "";
        String sDay = "";
        String sTime = "";
        String sFps = "";
        String sBio = "";
        String sLL = "";
        String sArrows = "";
        String sDamage = "";
        String sRain = "";
        String sThunder = "";
        String sSlimes = "";
        String sMobInfo = "";
        String sCoords = "";
        
        if (CJB.clock12h){
        	clock = "AM";
        	if (hours >= 12) {
        		hours -= 12;
        		clock = "PM";
        	}
        	if (hours == 0) hours = 12;
        }
        
        boolean slimes = false;
        if (CJB.showslimes) {
        	Chunk chunk = world.getChunkFromBlockCoords(MathHelper.floor_double(plr.posX), MathHelper.floor_double(plr.posZ));
        	slimes = chunk.getRandomWithSeed(0x3ad8025fL).nextInt(10) == 0;
        }
        
        EntityLiving entity = GetEntityLiving(world, plr);
        if (entity != null)
        {
        	String entname = entity.getEntityString();
        	
        	if (entity instanceof EntityPlayer)
        		entname = ((EntityPlayer)entity).username;
        	
        	sMobInfo = entname + " - health: " + entity.getHealth();
        }
        
        boolean isBow = curritem != null && curritem.getItem() instanceof ItemBow;
        boolean isRain = worldinfo.isRaining();
        boolean isThunder = worldinfo.isThundering();
        
        int irs = worldinfo.getRainTime() / 20;
        int irm = irs / 60;
        String rs = irm > 0 ? Integer.toString(irm) + "m" : Integer.toString(irs) + "s";
        
        int its = worldinfo.getThunderTime() / 20;
        int itm = its / 60;
        String ts = itm > 0 ? Integer.toString(itm) + "m" : Integer.toString(its) + "s";
        
        sDay = (!CJB.hideicons ? "" : "Day: ") + Integer.toString(day);
        sTime = (!CJB.hideicons ? "" : "Time: ") + (Integer.toString(hours).length() == 1 ? "0" : "") + hours + ":" + (Integer.toString((int)mins).length() == 1 ? "0" : "") + (int)mins + clock;
        sLL = (!CJB.hideicons ? "" : "LightLevel: ") + world.getBlockLightValue(MathHelper.floor_double(plr.posX), MathHelper.floor_double(plr.posY), MathHelper.floor_double(plr.posZ));
        sBio = (!CJB.hideicons ? "" : "Biome: ") + world.getBiomeGenForCoords(MathHelper.floor_double(plr.posX), MathHelper.floor_double(plr.posZ)).biomeName;
        sFps = (!CJB.hideicons ? "" : "FPS: ") + mc.debug.split(" ")[0];     
        sArrows = (!CJB.hideicons ? "" : "Arrows: ") + Integer.toString(arrowcount);
        sDamage = (!CJB.hideicons ? ItemName : "Item: " + ItemName) + ( itemdamage > 0 ? " - " + Integer.toString(itemdamage) : "");
        sRain = (!CJB.hideicons ? "" + (isRain ? "ends in " : "starts in ") : "Rain " + (isRain ? "ends" : "starts") + ": ") + rs;
        sThunder = (!CJB.hideicons ? "" + (isThunder ? "ends in " : "starts in ") : "Thunder " + (isThunder ? "ends" : "starts") + ": ") + ts;
        sSlimes = (!CJB.hideicons ? CJB_Colors.Green + "in Chunk" : CJB_Colors.Green + "Slimes in Chunk");
        sCoords = "Coords: "  + MathHelper.floor_double(plr.posX) + ", " + MathHelper.floor_double(plr.posY) + ", " + MathHelper.floor_double(plr.posZ);
        	
        ScaledResolution sr = new ScaledResolution(mc.gameSettings, mc.displayWidth, mc.displayHeight);
        int k = sr.getScaledWidth();
        int l = sr.getScaledHeight();
        int i = 0;
        
        int j = CJB.bigicons ? 1 : 2;
        int i1 = CJB.hideicons ? 10 : 18;
        
        int x = 0;
        int y = 0;
        boolean posup = CJB.position == 0 || CJB.position == 1;
        
        if (CJB.position == 0)
        {
        	x = 2;
        	y = 2;
        }
        if (CJB.position == 1)
        {
        	x = k;
        	y = 2;
        }
        if (CJB.position == 2)
        {
	        x = 2;
	        y = l - 16;
        } 
        if (CJB.position == 3)
        {
        	x = k;
        	y = l - 16;
        }

        if (CJB.showcoords)drawInfo(mc, sCoords, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 0, 0);
        if (CJB.showitemdamage && curritem != null)drawInfo(mc, sDamage, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 208, 89);
        if (CJB.showbiome)drawInfo(mc, sBio, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 176, 89);
        if (CJB.showtime)drawInfo(mc, sTime, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 192, 73);
        if (CJB.showday)drawInfo(mc, sDay, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 176, 73);
        if (CJB.showlightlevel)drawInfo(mc, sLL, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 208, 73);
        if (CJB.showarrowcount && isBow)drawInfo(mc, sArrows, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 224, 73);
        if (CJB.showfps)drawInfo(mc, sFps, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 192, 89);
        if (!mc.isMultiplayerWorld() && CJB.showrain)drawInfo(mc, sRain, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 240, 73);
        if (!mc.isMultiplayerWorld() && CJB.showthunder)drawInfo(mc, sThunder, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 240, 89);
        if (!mc.isMultiplayerWorld() && CJB.showslimes && slimes)drawInfo(mc, sSlimes, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 224, 89);
        if (!mc.isMultiplayerWorld() && CJB.showmobinfo && entity != null) drawInfo(mc, sMobInfo, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 0, 0);
        if (CJB.modcheats && CJB.flying) drawInfo(mc, "Flying", x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 0, 0);
        
        Collection collection = mc.thePlayer.getActivePotionEffects();

        if (collection.isEmpty() || !CJB.showdebuffs)
        {
            return;
        }
        
        drawInfo(mc, "Debuffs:", x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 0, 0);

        if (collection.size() > 5)
        {
        }

        for (Iterator iterator = mc.thePlayer.getActivePotionEffects().iterator(); iterator.hasNext();)
        {
            PotionEffect potioneffect = (PotionEffect)iterator.next();
            Potion potion = Potion.potionTypes[potioneffect.getPotionID()];

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

            String s1 = Potion.getDurationString(potioneffect);
            
            drawInfo(mc, "   - " + s + ": " + s1, x, posup ? y + (i++ * (i1 / j)) : y - (i++ * (i1 / j)), 0, 0);
        }
	}
	
	private void drawInfo(Minecraft mc, String s, int x, int y, int i, int j)
	{
		
		boolean showicons = CJB.hideicons || (i == 0 && j == 0);
		GL11.glDisable(2896 /*GL_LIGHTING*/);
		
		float scale = CJB.bigicons ? 1 : 0.5f;
		int l = showicons ? 2 : 18;
		x = showicons ? x-2 : x;
		String s1 = showicons ? "" : ": ";
		GL11.glBindTexture(3553 /*GL_TEXTURE_2D*/, mc.renderEngine.getTexture("/cjb/menu.png"));
    	GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
    	
    	GL11.glPushMatrix();
    	
		GL11.glTranslatef(x, y, 0);
		GL11.glScalef(scale, scale, 1f);
		GL11.glTranslatef(-x, -y, 0);
		
		if (CJB.position == 0 || CJB.position == 2)
		{
			if(!showicons)
				mc.ingameGUI.drawTexturedModalRect(x, y, i, j, 16, 16);
			
			mc.fontRenderer.drawStringWithShadow(s1 + "" + s, x + l, y + (showicons? 0: 4), 0xffffff);
		}
		else if (CJB.position == 1 || CJB.position == 3 )
		{
			if(!showicons)
				mc.ingameGUI.drawTexturedModalRect(x - l, y, i, j, 16, 16);
			s1 = showicons ? "" : " :";
			mc.fontRenderer.drawStringWithShadow(s + "" + s1, x - l - mc.fontRenderer.getStringWidth(s + s1) - 2, y + (showicons? 0: 4), 0xffffff);
		}
		
    	GL11.glPopMatrix();
	}
	
	private EntityLiving GetEntityLiving(World world, EntityPlayer plr)
	{
		List list = world.getEntitiesWithinAABB(net.minecraft.src.EntityLiving.class, AxisAlignedBB.getBoundingBoxFromPool(plr.posX, plr.posY, plr.posZ, plr.posX + 1.0D, plr.posY + 1.0D, plr.posZ + 1.0D).expand(32D, 32D, 32D));
        if(!list.isEmpty())
        {
        	for (int i = 0 ; i < list.size() ; i++)
        	{
	        	EntityLiving entity = (EntityLiving) list.get(i);
	        	
	        	if (entity == plr)
	        		continue;
	        	
	        	Vec3D vec3d = plr.getLook(1.0F).normalize();
	            Vec3D vec3d1 = Vec3D.createVector(entity.posX - plr.posX, ((entity.boundingBox.minY + (double)(entity.height / 2.0F)) - plr.posY) + (double)plr.getEyeHeight(), entity.posZ - plr.posZ);
	            double d = vec3d1.lengthVector();
	            vec3d1 = vec3d1.normalize();
	            double d1 = vec3d.dotProduct(vec3d1);
	            if(d1 > 1.0D - 0.125000000000000001D / d && plr.canEntityBeSeen(entity))
	            {
	                return entity;
	            }
        	}
        }
        return null;
    }

	public String getVersion() {
		return CJB.VERSION;
	}

	public void load() {
		
	}  
}
