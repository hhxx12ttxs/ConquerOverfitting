package codechicken.wirelessredstone.addons;

import java.util.Iterator;

import org.lwjgl.opengl.GL11;

import codechicken.core.ClientUtils;
import codechicken.wirelessredstone.core.*;

import net.minecraft.client.gui.MapItemRenderer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.storage.MapData;
import net.minecraftforge.client.IItemRenderer;

public class WirelessMapRenderer extends MapItemRenderer implements IItemRenderer
{
    public WirelessMapRenderer(GameSettings gamesettings, TextureManager renderEngine)
    {
        super(gamesettings, renderEngine);
    }
    
    private void renderPass(int xCenter, int zCenter, int scale, WirelessMapNodeStorage mapstorage, long worldTime, float size, float alpha, float light)
    {
        Tessellator tessellator = Tessellator.instance;
        float blockscale = 1 << scale;
        
        for(Iterator<FreqCoord> iterator = mapstorage.nodes.iterator(); iterator.hasNext();)
        {
            FreqCoord node = iterator.next();
            float relx = node.x / blockscale + 64;
            float relz = node.z / blockscale + 64;
            
            int colour = RedstoneEther.client().getFreqColour(node.freq);
            if(colour == 0xFFFFFFFF)
            {
                colour = 0xFFFF0000;
            }
            float r = ((colour >> 16) & 0xFF) / 255F * light;
            float g = ((colour >> 8) & 0xFF) / 255F * light;
            float b = (colour & 0xFF) / 255F * light;
            tessellator.setColorRGBA_F(r, g, b, alpha);
            
            float rot = RedstoneEther.getRotation(ClientUtils.getRenderTime(), node.freq);
            float xrot = (float) (Math.sin(rot) * size);
            float zrot = (float) (Math.cos(rot) * size);
            
            tessellator.addVertex(relx - zrot, relz + xrot, -0.2);
            tessellator.addVertex(relx + xrot, relz + zrot, -0.2);
            tessellator.addVertex(relx + zrot, relz - xrot, -0.2);
            tessellator.addVertex(relx - xrot, relz - zrot, -0.2);
        }
        
        for(Iterator<FreqCoord> iterator = mapstorage.devices.iterator(); iterator.hasNext();)
        {
            FreqCoord node = iterator.next();
            float relx = (node.x - xCenter) / blockscale + 64;
            float relz = (node.z - zCenter) / blockscale + 64;
            
            int colour = RedstoneEther.client().getFreqColour(node.freq);
            if(colour == 0xFFFFFFFF)
            {
                colour = 0xFFFF0000;
            }
            float r = ((colour >> 16) & 0xFF) / 255F * light;
            float g = ((colour >> 8) & 0xFF) / 255F * light;
            float b = (colour & 0xFF) / 255F * light;
            tessellator.setColorRGBA_F(r, g, b, alpha);
            
            float rot = RedstoneEther.getRotation(ClientUtils.getRenderTime(), node.freq);
            float xrot = (float) (Math.sin(rot) * size);
            float zrot = (float) (Math.cos(rot) * size);
            
            tessellator.addVertex(relx - zrot, relz + xrot, -0.2);
            tessellator.addVertex(relx + xrot, relz + zrot, -0.2);
            tessellator.addVertex(relx + zrot, relz - xrot, -0.2);
            tessellator.addVertex(relx - xrot, relz - zrot, -0.2);
        }
    }
    
    public void renderMap(EntityPlayer entityplayer, TextureManager renderEngine, MapData mapdata)
    {
        super.renderMap(entityplayer, renderEngine, mapdata);
        
        WirelessMapNodeStorage mapstorage = RedstoneEtherAddons.client().getMapNodes();
        Tessellator tessellator = Tessellator.instance;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
        float light = 1;
        long worldTime = entityplayer.worldObj.getWorldTime();
        int xCenter = mapdata.xCenter;
        int zCenter = mapdata.zCenter;
        int scale = mapdata.scale;

        ItemStack currentitem = entityplayer.inventory.getCurrentItem();
        if(currentitem == null || currentitem.getItem() != WirelessRedstoneAddons.wirelessMap)
        {
            return;
        }
        ClientMapInfo mapinfo = RedstoneEtherAddons.client().getMPMapInfo((short) currentitem.getItemDamage());
        if(mapinfo == null)
        {
            return;
        }
        xCenter = mapinfo.xCenter;
        zCenter = mapinfo.zCenter;
        scale = mapinfo.scale;
        
        tessellator.startDrawingQuads();        
        renderPass(xCenter, zCenter, scale, mapstorage, worldTime, 0.75F, 1F, light * 0.5F);
        renderPass(xCenter, zCenter, scale, mapstorage, worldTime, 0.6F, 1F, light);
        tessellator.draw();

        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE_MINUS_SRC_ALPHA);
    }
    
    @Override
    public boolean handleRenderType(ItemStack item, ItemRenderType type)
    {
        return type == ItemRenderType.FIRST_PERSON_MAP;
    }
    
    @Override
    public void renderItem(ItemRenderType type, ItemStack item, Object... data)
    {
        renderMap((EntityPlayer)data[0], (TextureManager)data[1], (MapData)data[2]);
    }
    
    @Override
    public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper)
    {
        return true;
    }
}

