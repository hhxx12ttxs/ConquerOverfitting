package maicliant.reifnsk.minimap;

public class PixelColor
{
    static final float d = 0.003921569F;
    public final boolean alphaComposite;
    public float red;
    public float green;
    public float blue;
    public float alpha;

    public PixelColor()
    {
        this(true);
    }

    public PixelColor(boolean var1)
    {
        this.alphaComposite = var1;
    }

    public void clear()
    {
        this.red = this.green = this.blue = this.alpha = 0.0F;
    }

    public void composite(int var1)
    {
        this.composite(var1, 1.0F);
    }

    public void composite(int var1, float var2)
    {
        if (this.alphaComposite)
        {
            float var3 = (float)(var1 >> 24 & 255) * 0.003921569F;
            float var4 = (float)(var1 >> 16 & 255) * 0.003921569F * var2;
            float var5 = (float)(var1 >> 8 & 255) * 0.003921569F * var2;
            float var6 = (float)(var1 >> 0 & 255) * 0.003921569F * var2;
            this.red += (var4 - this.red) * var3;
            this.green += (var5 - this.green) * var3;
            this.blue += (var6 - this.blue) * var3;
            this.alpha += (1.0F - this.alpha) * var3;
        }
        else
        {
            this.alpha = (float)(var1 >> 24 & 255) * 0.003921569F;
            this.red = (float)(var1 >> 16 & 255) * 0.003921569F * var2;
            this.green = (float)(var1 >> 8 & 255) * 0.003921569F * var2;
            this.blue = (float)(var1 >> 0 & 255) * 0.003921569F * var2;
        }
    }

    public void composite(float var1, int var2, float var3)
    {
        if (this.alphaComposite)
        {
            float var5 = (float)(var2 >> 16 & 255) * 0.003921569F * var3;
            float var6 = (float)(var2 >> 8 & 255) * 0.003921569F * var3;
            float var7 = (float)(var2 >> 0 & 255) * 0.003921569F * var3;
            this.red += (var5 - this.red) * var1;
            this.green += (var6 - this.green) * var1;
            this.blue += (var7 - this.blue) * var1;
            this.alpha += (1.0F - this.alpha) * var1;
        }
        else
        {
            this.alpha = (float)(var2 >> 24 & 255) * 0.003921569F;
            this.red = (float)(var2 >> 16 & 255) * 0.003921569F * var3;
            this.green = (float)(var2 >> 8 & 255) * 0.003921569F * var3;
            this.blue = (float)(var2 >> 0 & 255) * 0.003921569F * var3;
        }
    }

    public void composite(float var1, int var2, float var3, float var4, float var5)
    {
        if (this.alphaComposite)
        {
            float var7 = (float)(var2 >> 16 & 255) * 0.003921569F * var3;
            float var8 = (float)(var2 >> 8 & 255) * 0.003921569F * var4;
            float var9 = (float)(var2 >> 0 & 255) * 0.003921569F * var5;
            this.red += (var7 - this.red) * var1;
            this.green += (var8 - this.green) * var1;
            this.blue += (var9 - this.blue) * var1;
            this.alpha += (1.0F - this.alpha) * var1;
        }
        else
        {
            this.alpha = (float)(var2 >> 24 & 255) * 0.003921569F;
            this.red = (float)(var2 >> 16 & 255) * 0.003921569F * var3;
            this.green = (float)(var2 >> 8 & 255) * 0.003921569F * var4;
            this.blue = (float)(var2 >> 0 & 255) * 0.003921569F * var5;
        }
    }

    public void composite(float var1, float var2, float var3, float var4)
    {
        if (this.alphaComposite)
        {
            this.red += (var2 - this.red) * var1;
            this.green += (var3 - this.green) * var1;
            this.blue += (var4 - this.blue) * var1;
            this.alpha += (1.0F - this.alpha) * var1;
        }
        else
        {
            this.alpha = var1;
            this.red = var2;
            this.green = var3;
            this.blue = var4;
        }
    }

    public void composite(float var1, float var2, float var3, float var4, float var5)
    {
        if (this.alphaComposite)
        {
            this.red += (var2 * var5 - this.red) * var1;
            this.green += (var3 * var5 - this.green) * var1;
            this.blue += (var4 * var5 - this.blue) * var1;
            this.alpha += (1.0F - this.alpha) * var1;
        }
        else
        {
            this.alpha = var1;
            this.red = var2 * var5;
            this.green = var3 * var5;
            this.blue = var4 * var5;
        }
    }
}

