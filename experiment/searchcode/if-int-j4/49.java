// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   pixmap2.java

package jagex;

import java.awt.*;
import java.awt.image.*;
import java.io.IOException;

// Referenced classes of package jagex:
//            Stream, GameDialog

public class Surface
    implements ImageProducer, ImageObserver
{

    public Surface(int i, int j, int k, Component component)
    {
        _fld047A = true;
        _fld047F = false;
        _fld0480 = new byte[50][];
        fonts = new String[50];
        _fld04A0 = -65281;
        _fld046D = new DirectColorModel(32, 0xff000000, 0xff000, 255);
        _fld046B = _fld0468 = i;
        _fld046C = _fld0469 = j;
        _fld046A = i * j;
        _fld046E = new int[i * j];
        sprite = new int[k][];
        _fld0479 = new boolean[k];
        spriteWidth = new int[k];
        spriteHeight = new int[k];
        _fld0477 = new int[k];
        _fld0478 = new int[k];
        _fld0475 = new int[k];
        _fld0476 = new int[k];
        int l = _fld0468 * _fld0469;
        for(int i1 = 0; i1 < l; i1++)
            _fld046E[i1] = 0;

        _fld0471 = component.createImage(this);
        _mth020D(false);
        component.prepareImage(_fld0471, component);
        _mth020D(false);
        component.prepareImage(_fld0471, component);
        _mth020D(false);
        component.prepareImage(_fld0471, component);
        _fld047C = j;
        _fld047E = i;
        _fld0470 = component;
        _fld0481 = new int[256];
        for(int j1 = 0; j1 < 256; j1++)
        {
            int k1 = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ".indexOf(j1);
            if(k1 == -1)
                k1 = 74;
            _fld0481[j1] = k1 * 9;
        }

    }

    public synchronized void _mth0252(int i, int j)
    {
        if(_fld0468 > _fld046B)
            _fld0468 = _fld046B;
        if(_fld0469 > _fld046C)
            _fld0469 = _fld046C;
        _fld0468 = i;
        _fld0469 = j;
        _fld046A = i * j;
    }

    public synchronized void addConsumer(ImageConsumer imageconsumer)
    {
        _fld046F = imageconsumer;
        imageconsumer.setDimensions(_fld0468, _fld0469);
        imageconsumer.setProperties(null);
        imageconsumer.setColorModel(_fld046D);
        imageconsumer.setHints(14);
    }

    public synchronized boolean isConsumer(ImageConsumer imageconsumer)
    {
        return _fld046F == imageconsumer;
    }

    public synchronized void removeConsumer(ImageConsumer imageconsumer)
    {
        if(_fld046F == imageconsumer)
            _fld046F = null;
    }

    public void startProduction(ImageConsumer imageconsumer)
    {
        addConsumer(imageconsumer);
    }

    public void requestTopDownLeftRightResend(ImageConsumer imageconsumer)
    {
        System.out.println("TDLR");
    }

    public synchronized void _mth020D(boolean flag)
    {
        if(_fld046F == null)
            return;
        if(!flag)
        {
            _fld046F.setPixels(0, 0, _fld0468, _fld0469, _fld046D, _fld046E, 0, _fld0468);
        } else
        {
            for(int i = 0; i < _fld0469; i += 2)
                _fld046F.setPixels(0, i, _fld0468, 1, _fld046D, _fld046E, i * _fld0468, _fld0468);

        }
        _fld046F.imageComplete(2);
    }

    public void _mth0212(int i, int j, int k, int l)
    {
        if(i < 0)
            i = 0;
        if(j < 0)
            j = 0;
        if(k > _fld0468)
            k = _fld0468;
        if(l > _fld0469)
            l = _fld0469;
        _fld047D = i;
        _fld047B = j;
        _fld047E = k;
        _fld047C = l;
    }

    public void _mth01F0()
    {
        _fld047D = 0;
        _fld047B = 0;
        _fld047E = _fld0468;
        _fld047C = _fld0469;
    }

    public void draw(Graphics g, int i, int j)
    {
        _mth020D(_fld047F);
        g.drawImage(_fld0471, i, j, this);
    }

    public void _mth0206(boolean flag)
    {
        if(_fld047F != flag)
        {
            int i = _fld0468 * _fld0469;
            for(int j = 0; j < i; j++)
                _fld046E[j] = 0;

            _mth020D(false);
            _fld0470.prepareImage(_fld0471, _fld0470);
        }
        _fld047F = flag;
    }

    public boolean _mth01EE()
    {
        return _fld047F;
    }

    public void _mth0210()
    {
        int i = _fld0468 * _fld0469;
        if(!_fld047F)
        {
            for(int j = 0; j < i; j++)
                _fld046E[j] = 0;

            return;
        }
        int k = 0;
        for(int l = -_fld0469; l < 0; l += 2)
        {
            for(int i1 = -_fld0468; i1 < 0; i1++)
                _fld046E[k++] = 0;

            k += _fld0468;
        }

    }

    public void _mth01EC(int i, int j, int k, int l, int i1)
    {
        int j1 = 256 - i1;
        int k1 = (l >> 24 & 0xff) * i1;
        int l1 = (l >> 12 & 0xff) * i1;
        int i2 = (l & 0xff) * i1;
        int i3 = j - k;
        if(i3 < 0)
            i3 = 0;
        int j3 = j + k;
        if(j3 >= _fld0469)
            j3 = _fld0469 - 1;
        byte byte0 = 1;
        if(_fld047F)
        {
            byte0 = 2;
            if((i3 & 1) != 0)
                i3++;
        }
        for(int k3 = i3; k3 <= j3; k3 += byte0)
        {
            int l3 = k3 - j;
            int i4 = (int)Math.sqrt(k * k - l3 * l3);
            int j4 = i - i4;
            if(j4 < 0)
                j4 = 0;
            int k4 = i + i4;
            if(k4 >= _fld0468)
                k4 = _fld0468 - 1;
            int l4 = j4 + k3 * _fld0468;
            for(int i5 = j4; i5 <= k4; i5++)
            {
                int j2 = (_fld046E[l4] >> 24 & 0xff) * j1;
                int k2 = (_fld046E[l4] >> 12 & 0xff) * j1;
                int l2 = (_fld046E[l4] & 0xff) * j1;
                int j5 = ((k1 + j2 >> 8) << 24) + ((l1 + k2 >> 8) << 12) + (i2 + l2 >> 8);
                _fld046E[l4++] = j5;
            }

        }

    }

    public void _mth01FE(int i, int j, int k, int l, int i1, int j1)
    {
        if(i < _fld047D)
        {
            k -= _fld047D - i;
            i = _fld047D;
        }
        if(j < _fld047B)
        {
            l -= _fld047B - j;
            j = _fld047B;
        }
        if(i + k > _fld047E)
            k = _fld047E - i;
        if(j + l > _fld047C)
            l = _fld047C - j;
        int k1 = 256 - j1;
        int l1 = (i1 >> 24 & 0xff) * j1;
        int i2 = (i1 >> 12 & 0xff) * j1;
        int j2 = (i1 & 0xff) * j1;
        int j3 = _fld0468 - k;
        byte byte0 = 1;
        if(_fld047F)
        {
            byte0 = 2;
            j3 += _fld0468;
            if((j & 1) != 0)
            {
                j++;
                l--;
            }
        }
        int k3 = i + j * _fld0468;
        for(int l3 = 0; l3 < l; l3 += byte0)
        {
            for(int i4 = -k; i4 < 0; i4++)
            {
                int k2 = (_fld046E[k3] >> 24 & 0xff) * k1;
                int l2 = (_fld046E[k3] >> 12 & 0xff) * k1;
                int i3 = (_fld046E[k3] & 0xff) * k1;
                int j4 = ((l1 + k2 >> 8) << 24) + ((i2 + l2 >> 8) << 12) + (j2 + i3 >> 8);
                _fld046E[k3++] = j4;
            }

            k3 += j3;
        }

    }

    public void _mth0207(int i, int j, int k, int l, int i1, int j1)
    {
        if(i < _fld047D)
        {
            k -= _fld047D - i;
            i = _fld047D;
        }
        if(i + k > _fld047E)
            k = _fld047E - i;
        int k1 = j1 >> 24 & 0xff;
        int l1 = j1 >> 12 & 0xff;
        int i2 = j1 & 0xff;
        int j2 = i1 >> 24 & 0xff;
        int k2 = i1 >> 12 & 0xff;
        int l2 = i1 & 0xff;
        int i3 = _fld0468 - k;
        byte byte0 = 1;
        if(_fld047F)
        {
            byte0 = 2;
            i3 += _fld0468;
            if((j & 1) != 0)
            {
                j++;
                l--;
            }
        }
        int j3 = i + j * _fld0468;
        for(int k3 = 0; k3 < l; k3 += byte0)
            if(k3 + j >= _fld047B && k3 + j < _fld047C)
            {
                int l3 = ((k1 * k3 + j2 * (l - k3)) / l << 24) + ((l1 * k3 + k2 * (l - k3)) / l << 12) + (i2 * k3 + l2 * (l - k3)) / l;
                for(int i4 = -k; i4 < 0; i4++)
                    _fld046E[j3++] = l3;

                j3 += i3;
            } else
            {
                j3 += _fld0468;
            }

    }

    public void _mth01E1(int i, int j, int k, int l, int i1)
    {
        if(i < _fld047D)
        {
            k -= _fld047D - i;
            i = _fld047D;
        }
        if(j < _fld047B)
        {
            l -= _fld047B - j;
            j = _fld047B;
        }
        if(i + k > _fld047E)
            k = _fld047E - i;
        if(j + l > _fld047C)
            l = _fld047C - j;
        int j1 = _fld0468 - k;
        byte byte0 = 1;
        if(_fld047F)
        {
            byte0 = 2;
            j1 += _fld0468;
            if((j & 1) != 0)
            {
                j++;
                l--;
            }
        }
        int k1 = i + j * _fld0468;
        for(int l1 = -l; l1 < 0; l1 += byte0)
        {
            for(int i2 = -k; i2 < 0; i2++)
                _fld046E[k1++] = i1;

            k1 += j1;
        }

    }

    public void _mth01E5(int i, int j, int k, int l, int i1)
    {
        _mth01DE(i, j, k, i1);
        _mth01DE(i, (j + l) - 1, k, i1);
        _mth01EF(i, j, l, i1);
        _mth01EF((i + k) - 1, j, l, i1);
    }

    public void _mth01DE(int i, int j, int k, int l)
    {
        if(j < _fld047B || j >= _fld047C)
            return;
        if(i < _fld047D)
        {
            k -= _fld047D - i;
            i = _fld047D;
        }
        if(i + k > _fld047E)
            k = _fld047E - i;
        int i1 = i + j * _fld0468;
        for(int j1 = 0; j1 < k; j1++)
            _fld046E[i1 + j1] = l;

    }

    public void _mth01EF(int i, int j, int k, int l)
    {
        if(i < _fld047D || i >= _fld047E)
            return;
        if(j < _fld047B)
        {
            k -= _fld047B - j;
            j = _fld047B;
        }
        if(j + k > _fld047E)
            k = _fld047C - j;
        int i1 = i + j * _fld0468;
        for(int j1 = 0; j1 < k; j1++)
            _fld046E[i1 + j1 * _fld0468] = l;

    }

    public void _mth0202(int i, int j, int k)
    {
        if(i < _fld047D || j < _fld047B || i >= _fld047E || j >= _fld047C)
        {
            return;
        } else
        {
            _fld046E[i + j * _fld0468] = k;
            return;
        }
    }

    public void _mth0216(int i, boolean flag)
    {
        int l = _fld0468 * _fld0469;
        for(int k = 0; k < l; k++)
        {
            int j = _fld046E[k] & 0xff0ff0ff;
            _fld046E[k] = (j >>> 1) + (j >>> 2) + (j >>> 3) + (j >>> 4);
        }

    }

    public void _mth0217(int i)
    {
        for(int j = 0; j < _fld0468; j++)
        {
            for(int k = 0; k < _fld0469; k++)
            {
                int l = 0;
                int i1 = 0;
                int j1 = 0;
                int k1 = 0;
                for(int l1 = j - i; l1 <= j + i; l1++)
                    if(l1 >= 0 && l1 < _fld0468)
                    {
                        for(int i2 = k - i; i2 <= k + i; i2++)
                            if(i2 >= 0 && i2 < _fld0469)
                            {
                                int j2 = _fld046E[l1 + _fld0468 * i2];
                                l += j2 >> 24 & 0xff;
                                i1 += j2 >> 12 & 0xff;
                                j1 += j2 & 0xff;
                                k1++;
                            }

                    }

                _fld046E[j + _fld0468 * k] = (l / k1 << 24) + (i1 / k1 << 12) + j1 / k1;
            }

        }

    }

    public void _mth01E9()
    {
        int i = _fld0468 * _fld0469;
        for(int j = 0; j < i; j++)
        {
            int k = _fld046E[j];
            int l = k >> 24 & 0xff;
            int i1 = k >> 12 & 0xff;
            int j1 = k & 0xff;
            int k1 = j1 / 6 + i1 / 2 + l / 3;
            _fld046E[j] = (k1 << 24) + (k1 << 12) + k1;
        }

    }

    public static int _mth0251(int i, int j, int k)
    {
        return (i << 24) + (j << 12) + k;
    }

    public void _mth01D9()
    {
        for(int i = 0; i < sprite.length; i++)
        {
            sprite[i] = null;
            spriteWidth[i] = 0;
            spriteHeight[i] = 0;
        }

    }

    public void _mth0203(String s, int i, boolean flag)
    {
        _mth01E4(GameDialog.loadImage(s), i, flag);
    }

    public void _mth01E8(String s, int i, boolean flag, int j, int k, int l)
    {
        Image image = GameDialog.loadImage(s);
        int i1 = image.getWidth(GameDialog._fld0183);
        int j1 = image.getHeight(GameDialog._fld0183);
        int ai[] = new int[i1 * j1];
        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, i1, j1, ai, 0, i1);
        try
        {
            pixelgrabber.grabPixels();
        }
        catch(InterruptedException _ex)
        {
            System.out.println("Error!");
        }
        int k1 = 0;
        int l1 = 0;
        for(int i2 = 0; i2 < j; i2++)
        {
            int j2 = 0;
            int ai1[] = new int[k * l];
            for(int k2 = l1; k2 < l1 + l; k2++)
            {
                for(int l2 = k1; l2 < k1 + k; l2++)
                    ai1[j2++] = ai[l2 + k2 * i1];

            }

            _mth0204(ai1, k, l, i2 + i, flag);
            k1 += k;
            if(k1 >= i1)
            {
                k1 = 0;
                l1 += l;
            }
        }

    }

    public void _mth01E4(Image image, int i, boolean flag)
    {
        int j = image.getWidth(this);
        int k = image.getHeight(this);
        int ai[] = new int[j * k];
        PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, j, k, ai, 0, j);
        try
        {
            pixelgrabber.grabPixels();
        }
        catch(InterruptedException _ex)
        {
            System.out.println("Error!");
        }
        image.flush();
        image = null;
        _mth0204(ai, j, k, i, flag);
    }

    private void _mth0204(int ai[], int i, int j, int k, boolean flag)
    {
        int l = 0;
        int i1 = 0;
        int j1 = i;
        int k1 = j;
        if(flag && _fld047A)
        {
label0:
            for(int l1 = 0; l1 < j; l1++)
            {
                for(int j2 = 0; j2 < i; j2++)
                {
                    int i3 = ai[j2 + l1 * i];
                    if((i3 & 0xff000000) == 0 || i3 == _fld04A0)
                        continue;
                    i1 = l1;
                    break label0;
                }

            }

label1:
            for(int k2 = 0; k2 < i; k2++)
            {
                for(int j3 = 0; j3 < j; j3++)
                {
                    int i4 = ai[k2 + j3 * i];
                    if((i4 & 0xff000000) == 0 || i4 == _fld04A0)
                        continue;
                    l = k2;
                    break label1;
                }

            }

label2:
            for(int k3 = j - 1; k3 >= 0; k3--)
            {
                for(int j4 = 0; j4 < i; j4++)
                {
                    int i5 = ai[j4 + k3 * i];
                    if((i5 & 0xff000000) == 0 || i5 == _fld04A0)
                        continue;
                    k1 = k3 + 1;
                    break label2;
                }

            }

label3:
            for(int k4 = i - 1; k4 >= 0; k4--)
            {
                for(int j5 = 0; j5 < j; j5++)
                {
                    int k5 = ai[k4 + j5 * i];
                    if((k5 & 0xff000000) == 0 || k5 == _fld04A0)
                        continue;
                    j1 = k4 + 1;
                    break label3;
                }

            }

        }
        sprite[k] = new int[(j1 - l) * (k1 - i1)];
        spriteWidth[k] = j1 - l;
        spriteHeight[k] = k1 - i1;
        _fld0479[k] = flag;
        _fld0475[k] = l;
        _fld0476[k] = i1;
        _fld0477[k] = i;
        _fld0478[k] = j;
        int i2 = 0;
        for(int l2 = i1; l2 < k1; l2++)
        {
            for(int l3 = l; l3 < j1; l3++)
            {
                int l4 = ai[l3 + l2 * i];
                if(flag)
                {
                    if((l4 & 0xff000000) == 0 || l4 == _fld04A0)
                        l4 = 0;
                    if(l4 == 0xff000000)
                        l4 = 0xff010101;
                }
                l4 = ((l4 & 0xff0000) << 8) + ((l4 & 0xff00) << 4) + (l4 & 0xff);
                sprite[k][i2++] = l4;
            }

        }

    }

    public void _mth01FF(int i)
    {
        spriteWidth[i] = _fld0468;
        spriteHeight[i] = _fld0469;
        _fld0479[i] = false;
        _fld0475[i] = 0;
        _fld0476[i] = 0;
        _fld0477[i] = _fld0468;
        _fld0478[i] = _fld0469;
        int j = _fld0468 * _fld0469;
        sprite[i] = new int[j];
        for(int k = 0; k < j; k++)
            sprite[i][k] = _fld046E[k];

    }

    public void _mth01FB(int i, int j)
    {
        spriteWidth[j] = spriteWidth[i];
        spriteHeight[j] = spriteHeight[i];
        _fld0475[j] = _fld0475[i];
        _fld0476[j] = _fld0476[i];
        _fld0479[j] = _fld0479[i];
        _fld0477[j] = _fld0477[i];
        _fld0478[j] = _fld0478[i];
        int k = spriteWidth[i] * spriteHeight[i];
        sprite[j] = new int[k];
        for(int l = 0; l < k; l++)
            sprite[j][l] = sprite[i][l];

    }

    public void _mth01EA(int i, int j)
    {
        spriteWidth[j] = spriteWidth[i];
        spriteHeight[j] = spriteHeight[i];
        _fld0475[j] = _fld0477[i] - spriteWidth[i] - _fld0475[i];
        _fld0476[j] = _fld0476[i];
        _fld0479[j] = _fld0479[i];
        _fld0477[j] = _fld0477[i];
        _fld0478[j] = _fld0478[i];
        int k = spriteWidth[i];
        int l = spriteHeight[i];
        sprite[j] = new int[k * l];
        int i1 = 0;
        for(int j1 = 0; j1 < l; j1++)
        {
            for(int k1 = 0; k1 < k; k1++)
                sprite[j][i1++] = sprite[i][j1 * k + (k - k1 - 1)];

        }

    }

    public void _mth01F3(int i, int j, int k)
    {
        _mth0214(i - _fld0477[k] / 2, j - _fld0478[k] / 2, k);
    }

    public void _mth01DD(int i, int j, int k, int l)
    {
        int i1 = ((l & 0xf0) << 16) + ((l & 0xf) << 8);
        if(_fld0479[k])
        {
            i += _fld0475[k];
            j += _fld0476[k];
        }
        int j1 = spriteHeight[k];
        int k1 = spriteWidth[k];
        int l1 = _fld0468 - k1;
        int i2 = 0;
        int j2 = i + j * _fld0468;
        int k2 = 0;
        if(j < _fld047B)
        {
            int l2 = _fld047B - j;
            j1 -= l2;
            j = _fld047B;
            k2 += l2 * k1;
            j2 += l2 * _fld0468;
        }
        if(j + j1 >= _fld047C)
            j1 -= (j + j1) - _fld047C;
        if(i < _fld047D)
        {
            int i3 = _fld047D - i;
            k1 -= i3;
            i = _fld047D;
            k2 += i3;
            j2 += i3;
            i2 += i3;
            l1 += i3;
        }
        if(i + k1 >= _fld047E)
        {
            int j3 = (i + k1) - _fld047E;
            k1 -= j3;
            i2 += j3;
            l1 += j3;
        }
        if(k1 <= 0 || j1 <= 0)
            return;
        byte byte0 = 1;
        if(_fld047F)
        {
            byte0 = 2;
            l1 += _fld0468;
            i2 += spriteWidth[k];
            if((j & 1) != 0)
            {
                j2 += _fld0468;
                j1--;
            }
        }
        _mth01ED(_fld046E, sprite[k], 0, k2, j2, k1, j1, l1, i2, i1, byte0);
    }

    public void _mth0214(int i, int j, int k)
    {
        if(_fld0479[k])
        {
            i += _fld0475[k];
            j += _fld0476[k];
        }
        int l = i + j * _fld0468;
        int i1 = 0;
        int j1 = spriteHeight[k];
        int k1 = spriteWidth[k];
        int l1 = _fld0468 - k1;
        int i2 = 0;
        if(j < _fld047B)
        {
            int j2 = _fld047B - j;
            j1 -= j2;
            j = _fld047B;
            i1 += j2 * k1;
            l += j2 * _fld0468;
        }
        if(j + j1 >= _fld047C)
            j1 -= (j + j1) - _fld047C;
        if(i < _fld047D)
        {
            int k2 = _fld047D - i;
            k1 -= k2;
            i = _fld047D;
            i1 += k2;
            l += k2;
            i2 += k2;
            l1 += k2;
        }
        if(i + k1 >= _fld047E)
        {
            int l2 = (i + k1) - _fld047E;
            k1 -= l2;
            i2 += l2;
            l1 += l2;
        }
        if(k1 <= 0 || j1 <= 0)
            return;
        byte byte0 = 1;
        if(_fld047F)
        {
            byte0 = 2;
            l1 += _fld0468;
            i2 += spriteWidth[k];
            if((j & 1) != 0)
            {
                l += _fld0468;
                j1--;
            }
        }
        if(_fld0479[k])
        {
            _mth0200(_fld046E, sprite[k], 0, i1, l, k1, j1, l1, i2, byte0);
            return;
        } else
        {
            _mth01D8(_fld046E, sprite[k], i1, l, k1, j1, l1, i2, byte0);
            return;
        }
    }

    private void _mth01D8(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1)
    {
        int l1 = -(k >> 2);
        k = -(k & 3);
        for(int i2 = -l; i2 < 0; i2 += k1)
        {
            for(int j2 = l1; j2 < 0; j2++)
            {
                ai[j++] = ai1[i++];
                ai[j++] = ai1[i++];
                ai[j++] = ai1[i++];
                ai[j++] = ai1[i++];
            }

            for(int k2 = k; k2 < 0; k2++)
                ai[j++] = ai1[i++];

            j += i1;
            i += j1;
        }

    }

    private void _mth0200(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1)
    {
        int i2 = -(l >> 2);
        l = -(l & 3);
        for(int j2 = -i1; j2 < 0; j2 += l1)
        {
            for(int k2 = i2; k2 < 0; k2++)
            {
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i;
                else
                    k++;
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i;
                else
                    k++;
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i;
                else
                    k++;
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i;
                else
                    k++;
            }

            for(int l2 = l; l2 < 0; l2++)
            {
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i;
                else
                    k++;
            }

            k += j1;
            j += k1;
        }

    }

    private void _mth01ED(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2)
    {
        int j2 = -(l >> 2);
        l = -(l & 3);
        for(int k2 = -i1; k2 < 0; k2 += i2)
        {
            for(int l2 = j2; l2 < 0; l2++)
            {
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i | l1;
                else
                    k++;
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i | l1;
                else
                    k++;
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i | l1;
                else
                    k++;
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i | l1;
                else
                    k++;
            }

            for(int i3 = l; i3 < 0; i3++)
            {
                i = ai1[j++];
                if(i != 0)
                    ai[k++] = i | l1;
                else
                    k++;
            }

            k += j1;
            j += k1;
        }

    }

    public void _mth0213(int i, int j, int k, int l, int i1)
    {
        _mth0201(null, 0, 0, 0, l, i + _fld0475[k], j + (_fld0476[k] * (_fld0478[k] - i1)) / _fld0478[k], k, (i1 * spriteHeight[k]) / _fld0478[k]);
    }

    private void _mth0201(int ai[], int i, int j, int k, int l, int i1, int j1, 
            int k1, int l1)
    {
        try
        {
            ai = _fld046E;
            i = i1 + j1 * _fld0468;
            j = 0;
            int ai1[] = sprite[k1];
            int i2 = -spriteHeight[k1];
            int j2 = -spriteWidth[k1];
            int k2 = _fld0468 + j2;
            byte byte0 = 1;
            if(l < 0)
            {
                l = -l;
                byte0 = -1;
            }
            int l2 = 0;
            l = ((l + 1) * 0x10000) / ((-i2 + 1) - l1);
            int i3 = 0;
            l1 = ((l1 + 1) * 0x10000) / ((-i2 + 1) - l1);
            if(j1 < _fld047B)
            {
                i2 -= j1 - _fld047B;
                j += (j1 - _fld047B) * j2;
                i = i1 + _fld047B * _fld0468;
            }
            for(j1 = i2; j1 < 0; j1++)
            {
                for(i1 = j2; i1 < 0; i1++)
                {
                    k = ai1[j++];
                    if(k != 0)
                        ai[i++] = k;
                    else
                        i++;
                }

                i += k2;
                for(l2 += l; l2 > 0x10000;)
                {
                    l2 -= 0x10000;
                    i += byte0;
                }

                for(i3 += l1; i3 > 0x10000;)
                {
                    i3 -= 0x10000;
                    j -= j2;
                    j1++;
                }

            }

            return;
        }
        catch(Exception _ex)
        {
            System.out.println("bf:" + ai + " off:" + i + " co:" + j + " c:" + k + " skew:" + l + " x:" + i1 + " y:" + j1 + " n:" + k1 + " skip:" + l1);
        }
        System.out.println("spr:" + sprite[k1] + " h:" + spriteHeight[k1] + " w:" + spriteWidth[k1]);
    }

    public void _mth01DA(int i, int j, int k, int l, int i1)
    {
        _mth020A(null, 0, 0, 0, l, i + (_fld0475[k] * (_fld0477[k] - i1)) / _fld0477[k], j + _fld0476[k], k, (i1 * spriteWidth[k]) / _fld0477[k]);
    }

    private void _mth020A(int ai[], int i, int j, int k, int l, int i1, int j1, 
            int k1, int l1)
    {
        try
        {
            ai = _fld046E;
            i = i1 + j1 * _fld0468;
            j = 0;
            int ai1[] = sprite[k1];
            int i2 = -spriteHeight[k1];
            int j2 = -spriteWidth[k1];
            int k2 = 1 + _fld0468 * i2;
            int l2 = 1 - j2 * i2;
            int i3 = _fld0468;
            if(l < 0)
            {
                l = -l;
                i3 = -_fld0468;
            }
            int j3 = 0;
            l = ((l + 1) * 0x10000) / ((-j2 + 1) - l1);
            int k3 = 0;
            l1 = ((l1 + 1) * 0x10000) / ((-j2 + 1) - l1);
            for(i1 = j2; i1 < 0; i1++)
            {
                for(j1 = i2; j1 < 0; j1++)
                {
                    k = ai1[j];
                    if(k != 0)
                        ai[i] = k;
                    j -= j2;
                    i += _fld0468;
                }

                i += k2;
                j += l2;
                for(j3 += l; j3 > 0x10000;)
                {
                    j3 -= 0x10000;
                    i += i3;
                }

                for(k3 += l1; k3 > 0x10000;)
                {
                    k3 -= 0x10000;
                    j++;
                    i1++;
                }

            }

            return;
        }
        catch(Exception _ex)
        {
            System.out.println("bf:" + ai + " off:" + i + " co:" + j + " c:" + k + " skew:" + l + " x:" + i1 + " y:" + j1 + " n:" + k1 + " skip:" + l1);
        }
        System.out.println("spr:" + sprite[k1] + " h:" + spriteHeight[k1] + " w:" + spriteWidth[k1]);
    }

    public void _mth01F2(int i, int j, int k, int l, int i1)
    {
        if(_fld0484 == null)
        {
            _fld0484 = new int[512];
            for(int j1 = 0; j1 < 256; j1++)
            {
                _fld0484[j1] = (int)(Math.sin((double)j1 * 0.02454369D) * 32768D);
                _fld0484[j1 + 256] = (int)(Math.cos((double)j1 * 0.02454369D) * 32768D);
            }

        }
        int k1 = -_fld0477[k] / 2;
        int l1 = -_fld0478[k] / 2;
        if(_fld0479[k])
        {
            k1 += _fld0475[k];
            l1 += _fld0476[k];
        }
        int i2 = k1 + spriteWidth[k];
        int j2 = l1 + spriteHeight[k];
        int k2 = i2;
        int l2 = l1;
        int i3 = k1;
        int j3 = j2;
        l &= 0xff;
        int k3 = _fld0484[l] * i1;
        int l3 = _fld0484[l + 256] * i1;
        int i4 = i + (l1 * k3 + k1 * l3 >> 22);
        int j4 = j + (l1 * l3 - k1 * k3 >> 22);
        int k4 = i + (l2 * k3 + k2 * l3 >> 22);
        int l4 = j + (l2 * l3 - k2 * k3 >> 22);
        int i5 = i + (j2 * k3 + i2 * l3 >> 22);
        int j5 = j + (j2 * l3 - i2 * k3 >> 22);
        int k5 = i + (j3 * k3 + i3 * l3 >> 22);
        int l5 = j + (j3 * l3 - i3 * k3 >> 22);
        int i6 = j4;
        int j6 = j4;
        if(l4 < i6)
            i6 = l4;
        else
        if(l4 > j6)
            j6 = l4;
        if(j5 < i6)
            i6 = j5;
        else
        if(j5 > j6)
            j6 = j5;
        if(l5 < i6)
            i6 = l5;
        else
        if(l5 > j6)
            j6 = l5;
        if(i6 < _fld047B)
            i6 = _fld047B;
        if(j6 > _fld047C)
            j6 = _fld047C;
        if(_fld0485 == null)
        {
            _fld0485 = new int[_fld0469 + 1];
            _fld0486 = new int[_fld0469 + 1];
            _fld0490 = new int[_fld0469 + 1];
            _fld0491 = new int[_fld0469 + 1];
            _fld0492 = new int[_fld0469 + 1];
            _fld0493 = new int[_fld0469 + 1];
        }
        for(int k6 = i6; k6 <= j6; k6++)
        {
            _fld0485[k6] = 0x5f5e0ff;
            _fld0486[k6] = 0xfa0a1f01;
        }

        int k7 = 0;
        int i8 = 0;
        int k8 = 0;
        int l8 = spriteWidth[k];
        int i9 = spriteHeight[k];
        k1 = 0;
        l1 = 0;
        k2 = l8 - 1;
        l2 = 0;
        i2 = l8 - 1;
        j2 = i9 - 1;
        i3 = 0;
        j3 = i9 - 1;
        if(l5 != j4)
        {
            k7 = (k5 - i4 << 8) / (l5 - j4);
            k8 = (j3 - l1 << 8) / (l5 - j4);
        }
        int l6;
        int i7;
        int j7;
        int j8;
        if(j4 > l5)
        {
            j7 = k5 << 8;
            j8 = j3 << 8;
            l6 = l5;
            i7 = j4;
        } else
        {
            j7 = i4 << 8;
            j8 = l1 << 8;
            l6 = j4;
            i7 = l5;
        }
        if(l6 < 0)
        {
            j7 -= k7 * l6;
            j8 -= k8 * l6;
            l6 = 0;
        }
        if(i7 > _fld0469 - 1)
            i7 = _fld0469 - 1;
        for(int j9 = l6; j9 <= i7; j9++)
        {
            _fld0485[j9] = _fld0486[j9] = j7;
            j7 += k7;
            _fld0490[j9] = _fld0491[j9] = 0;
            _fld0492[j9] = _fld0493[j9] = j8;
            j8 += k8;
        }

        if(l4 != j4)
        {
            k7 = (k4 - i4 << 8) / (l4 - j4);
            i8 = (k2 - k1 << 8) / (l4 - j4);
        }
        int l7;
        if(j4 > l4)
        {
            j7 = k4 << 8;
            l7 = k2 << 8;
            l6 = l4;
            i7 = j4;
        } else
        {
            j7 = i4 << 8;
            l7 = k1 << 8;
            l6 = j4;
            i7 = l4;
        }
        if(l6 < 0)
        {
            j7 -= k7 * l6;
            l7 -= i8 * l6;
            l6 = 0;
        }
        if(i7 > _fld0469 - 1)
            i7 = _fld0469 - 1;
        for(int k9 = l6; k9 <= i7; k9++)
        {
            if(j7 < _fld0485[k9])
            {
                _fld0485[k9] = j7;
                _fld0490[k9] = l7;
                _fld0492[k9] = 0;
            }
            if(j7 > _fld0486[k9])
            {
                _fld0486[k9] = j7;
                _fld0491[k9] = l7;
                _fld0493[k9] = 0;
            }
            j7 += k7;
            l7 += i8;
        }

        if(j5 != l4)
        {
            k7 = (i5 - k4 << 8) / (j5 - l4);
            k8 = (j2 - l2 << 8) / (j5 - l4);
        }
        if(l4 > j5)
        {
            j7 = i5 << 8;
            l7 = i2 << 8;
            j8 = j2 << 8;
            l6 = j5;
            i7 = l4;
        } else
        {
            j7 = k4 << 8;
            l7 = k2 << 8;
            j8 = l2 << 8;
            l6 = l4;
            i7 = j5;
        }
        if(l6 < 0)
        {
            j7 -= k7 * l6;
            j8 -= k8 * l6;
            l6 = 0;
        }
        if(i7 > _fld0469 - 1)
            i7 = _fld0469 - 1;
        for(int l9 = l6; l9 <= i7; l9++)
        {
            if(j7 < _fld0485[l9])
            {
                _fld0485[l9] = j7;
                _fld0490[l9] = l7;
                _fld0492[l9] = j8;
            }
            if(j7 > _fld0486[l9])
            {
                _fld0486[l9] = j7;
                _fld0491[l9] = l7;
                _fld0493[l9] = j8;
            }
            j7 += k7;
            j8 += k8;
        }

        if(l5 != j5)
        {
            k7 = (k5 - i5 << 8) / (l5 - j5);
            i8 = (i3 - i2 << 8) / (l5 - j5);
        }
        if(j5 > l5)
        {
            j7 = k5 << 8;
            l7 = i3 << 8;
            j8 = j3 << 8;
            l6 = l5;
            i7 = j5;
        } else
        {
            j7 = i5 << 8;
            l7 = i2 << 8;
            j8 = j2 << 8;
            l6 = j5;
            i7 = l5;
        }
        if(l6 < 0)
        {
            j7 -= k7 * l6;
            l7 -= i8 * l6;
            l6 = 0;
        }
        if(i7 > _fld0469 - 1)
            i7 = _fld0469 - 1;
        for(int i10 = l6; i10 <= i7; i10++)
        {
            if(j7 < _fld0485[i10])
            {
                _fld0485[i10] = j7;
                _fld0490[i10] = l7;
                _fld0492[i10] = j8;
            }
            if(j7 > _fld0486[i10])
            {
                _fld0486[i10] = j7;
                _fld0491[i10] = l7;
                _fld0493[i10] = j8;
            }
            j7 += k7;
            l7 += i8;
        }

        int j10 = i6 * _fld0468;
        int ai[] = sprite[k];
        for(int k10 = i6; k10 < j6; k10++)
        {
            int l10 = _fld0485[k10] >> 8;
            int i11 = _fld0486[k10] >> 8;
            if(i11 - l10 <= 0)
            {
                j10 += _fld0468;
            } else
            {
                int j11 = _fld0490[k10] << 9;
                int k11 = ((_fld0491[k10] << 9) - j11) / (i11 - l10);
                int l11 = _fld0492[k10] << 9;
                int i12 = ((_fld0493[k10] << 9) - l11) / (i11 - l10);
                if(l10 < _fld047D)
                {
                    j11 += (_fld047D - l10) * k11;
                    l11 += (_fld047D - l10) * i12;
                    l10 = _fld047D;
                }
                if(i11 > _fld047E)
                    i11 = _fld047E;
                if(!_fld047F || (k10 & 1) == 0)
                    if(!_fld0479[k])
                        _mth01EB(_fld046E, ai, 0, j10 + l10, j11, l11, k11, i12, l10 - i11, l8);
                    else
                        _mth01E3(_fld046E, ai, 0, j10 + l10, j11, l11, k11, i12, l10 - i11, l8);
                j10 += _fld0468;
            }
        }

    }

    private void _mth01EB(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1)
    {
        for(i = k1; i < 0; i++)
        {
            _fld046E[j++] = ai1[(k >> 17) + (l >> 17) * l1];
            k += i1;
            l += j1;
        }

    }

    private void _mth01E3(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1)
    {
        for(int i2 = k1; i2 < 0; i2++)
        {
            i = ai1[(k >> 17) + (l >> 17) * l1];
            if(i != 0)
                _fld046E[j++] = i;
            else
                j++;
            k += i1;
            l += j1;
        }

    }

    public void _mth01E0(int i, int j, int k, int l, int i1)
    {
        try
        {
            if(_fld0479[i1])
            {
                i += (_fld0475[i1] * k) / _fld0477[i1];
                j += (_fld0476[i1] * l) / _fld0478[i1];
                k = (k * spriteWidth[i1]) / _fld0477[i1];
                l = (l * spriteHeight[i1]) / _fld0478[i1];
            }
            int j1 = i + j * _fld0468;
            int k1 = spriteWidth[i1];
            int l1 = spriteHeight[i1];
            int i2 = _fld0468 - k;
            int j2 = 0;
            int k2 = 0;
            int l2 = (k1 << 16) / k;
            int i3 = (l1 << 16) / l;
            if(j < _fld047B)
            {
                int j3 = _fld047B - j;
                l -= j3;
                j = 0;
                j1 += j3 * _fld0468;
                k2 += i3 * j3;
            }
            if(j + l >= _fld047C)
                l -= (j + l) - _fld047C;
            if(i < _fld047D)
            {
                int k3 = _fld047D - i;
                k -= k3;
                i = 0;
                j1 += k3;
                j2 += l2 * k3;
                i2 += k3;
            }
            if(i + k >= _fld047E)
            {
                int l3 = (i + k) - _fld047E;
                k -= l3;
                i2 += l3;
            }
            byte byte0 = 1;
            if(_fld047F)
            {
                byte0 = 2;
                i2 += _fld0468;
                i3 += i3;
                if((j & 1) != 0)
                {
                    j1 += _fld0468;
                    l--;
                }
            }
            if(_fld0479[i1])
            {
                _mth01E7(_fld046E, sprite[i1], 0, j2, k2, j1, i2, k, l, l2, i3, k1, byte0);
                return;
            } else
            {
                _mth020E(_fld046E, sprite[i1], j1, j2, k2, i2, k, l, l2, i3, k1, byte0);
                return;
            }
        }
        catch(Exception _ex)
        {
            System.out.println("error in sprite clipping routine");
        }
    }

    private void _mth020E(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2, int j2)
    {
        try
        {
            int k2 = j;
            for(int l2 = -j1; l2 < 0; l2 += j2)
            {
                int i3 = (k >> 16) * i2;
                for(int j3 = -i1; j3 < 0; j3++)
                {
                    ai[i++] = ai1[(j >> 16) + i3];
                    j += k1;
                }

                k += l1;
                j = k2;
                i += l;
            }

            return;
        }
        catch(Exception _ex)
        {
            System.out.println("error in sprite plot routine");
        }
    }

    private void _mth01E7(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2, int j2, int k2)
    {
        try
        {
            int l2 = j;
            for(int i3 = -k1; i3 < 0; i3 += k2)
            {
                int j3 = (k >> 16) * j2;
                for(int k3 = -j1; k3 < 0; k3++)
                {
                    i = ai1[(j >> 16) + j3];
                    if(i != 0)
                        ai[l++] = i;
                    else
                        l++;
                    j += l1;
                }

                k += i2;
                j = l2;
                l += i1;
            }

            return;
        }
        catch(Exception _ex)
        {
            System.out.println("error in transparent sprite plot routine");
        }
    }

    public void _mth01DC(int i, int j, int k, int l, int i1, int j1)
    {
        if(_fld0479[i1])
        {
            i += (_fld0475[i1] * k) / _fld0477[i1];
            j += (_fld0476[i1] * l) / _fld0478[i1];
            k = (k * spriteWidth[i1]) / _fld0477[i1];
            l = (l * spriteHeight[i1]) / _fld0478[i1];
        }
        int k1 = i + j * _fld0468;
        int l1 = spriteWidth[i1];
        int i2 = spriteHeight[i1];
        int j2 = _fld0468 - k;
        int k2 = 0;
        int l2 = 0;
        int i3 = (l1 << 16) / k;
        int j3 = (i2 << 16) / l;
        if(j < _fld047B)
        {
            int k3 = _fld047B - j;
            l -= k3;
            j = 0;
            k1 += k3 * _fld0468;
            l2 += j3 * k3;
        }
        if(j + l >= _fld047C)
            l -= (j + l) - _fld047C;
        if(i < _fld047D)
        {
            int l3 = _fld047D - i;
            k -= l3;
            i = 0;
            k1 += l3;
            k2 += i3 * l3;
            j2 += l3;
        }
        if(i + k >= _fld047E)
        {
            int i4 = (i + k) - _fld047E;
            k -= i4;
            j2 += i4;
        }
        if(j1 == 128)
        {
            _mth01DB(_fld046E, sprite[i1], 0, k2, l2, k1, j2, k, l, i3, j3, l1);
            return;
        } else
        {
            _mth01E6(_fld046E, sprite[i1], 0, k2, l2, k1, j2, k, l, i3, j3, l1, j1);
            return;
        }
    }

    private void _mth01DB(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2, int j2)
    {
        int k2 = j;
        for(int l2 = -k1; l2 < 0; l2++)
        {
            int i3 = (k >> 16) * j2;
            for(int j3 = -j1; j3 < 0; j3++)
            {
                i = ai1[(j >> 16) + i3];
                if(i != 0)
                    ai[l++] = (i >>> 1 & 0xff0ff0ff) + (ai[l] >>> 1 & 0xff0ff0ff);
                else
                    l++;
                j += l1;
            }

            k += i2;
            j = k2;
            l += i1;
        }

    }

    private void _mth01E6(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2, int j2, int k2)
    {
        int l2 = j;
        int i3 = 256 - k2;
        for(int l4 = -k1; l4 < 0; l4++)
        {
            int i5 = (k >> 16) * j2;
            for(int j5 = -j1; j5 < 0; j5++)
            {
                i = ai1[(j >> 16) + i5];
                if(i != 0)
                {
                    int j3 = (i >> 24 & 0xff) * k2;
                    int k3 = (i >> 12 & 0xff) * k2;
                    int l3 = (i & 0xff) * k2;
                    int i4 = (ai[l] >> 24 & 0xff) * i3;
                    int j4 = (ai[l] >> 12 & 0xff) * i3;
                    int k4 = (ai[l] & 0xff) * i3;
                    int k5 = ((j3 + i4 >> 8) << 24) + ((k3 + j4 >> 8) << 12) + (l3 + k4 >> 8);
                    ai[l++] = k5;
                } else
                {
                    l++;
                }
                j += l1;
            }

            k += i2;
            j = l2;
            l += i1;
        }

    }

    public void _mth0250(int i, int j, int k, int l, int i1)
    {
        _mth0208(_fld046E, 0, 0, i, j, k, l, i1);
    }

    private void _mth0208(int ai[], int i, int j, int k, int l, int i1, int j1, 
            int k1)
    {
        boolean flag = false;
        ai = _fld046E;
        i = k + l * _fld0468;
        j = 0;
        int ai1[] = sprite[i1];
        int i2 = -spriteHeight[i1];
        int j2 = -spriteWidth[i1];
        int k2 = _fld0468 + j2;
        if(l < 0)
        {
            i2 -= l;
            j += l * j2;
            i = k;
        }
        for(l = i2; l < 0; l++)
        {
            int l1 = (int)(Math.sin((double)(l + j1) / 4D) * (double)(2 - k1) + Math.sin((double)(l + j1) / 6D) * (double)(3 - k1) + Math.sin((double)(l + j1) / 8D) * (double)(4 - k1));
            if(l == -1 || l == i2)
                l1 = 0;
            for(k = j2; k < 0; k++)
                ai[l1 + i++] = ai1[j++];

            i += k2;
        }

    }

    public void _mth01FA(int i, int j, int k, int ai[], int l)
    {
        _mth01FD(_fld046E, 0, 0, i, j, k, ai, l);
    }

    private void _mth01FD(int ai[], int i, int j, int k, int l, int i1, int ai1[], 
            int j1)
    {
        try
        {
            ai = _fld046E;
            i = k + l * _fld0468;
            int k1 = spriteWidth[i1];
            int l1 = 0;
            int ai2[] = sprite[i1];
            int i2 = spriteWidth[i1] - 1;
            int j2 = -spriteHeight[i1];
            int k2 = -spriteWidth[i1] / 4;
            int l2 = _fld0468 - spriteWidth[i1];
            byte byte0 = 1;
            if(_fld047F)
            {
                byte0 = 2;
                l2 += _fld0468;
                k1 += spriteWidth[i1];
                if((l & 1) != 0)
                {
                    i += _fld0468;
                    j2--;
                }
            }
            for(l = j2; l < 0; l += byte0)
            {
                j = ai1[l - j2] / j1 & i2;
                for(k = k2; k < 0; k++)
                {
                    ai[i++] = ai2[l1 + j];
                    j = j + 1 & i2;
                    ai[i++] = ai2[l1 + j];
                    j = j + 1 & i2;
                    ai[i++] = ai2[l1 + j];
                    j = j + 1 & i2;
                    ai[i++] = ai2[l1 + j];
                    j = j + 1 & i2;
                }

                i += l2;
                l1 += k1;
            }

            return;
        }
        catch(Exception exception)
        {
            System.out.println("Error in parasprite: " + exception);
            exception.printStackTrace();
            return;
        }
    }

    public void _mth01F1(int i)
    {
        int ai[] = sprite[i];
        int j = spriteWidth[i] * spriteHeight[i];
        for(int k = 0; k < j; k++)
        {
            int l = ai[k];
            if(l != 0)
            {
                l = l >>> 1 & 0xff0ff0ff;
                if(l == 0)
                    l = 1;
                ai[k] = l;
            }
        }

    }

    public void _mth0205(int i, int j)
    {
        int ai[] = sprite[i];
        int k = spriteWidth[i] * spriteHeight[i];
        int l = ((j & 0xf0) << 16) + ((j & 0xf) << 8);
        for(int i1 = 0; i1 < k; i1++)
            if(ai[i1] != 0)
                ai[i1] = ai[i1] & 0xff0ff0ff | l;

    }

    public boolean _mth01E2(int i, int j)
    {
        if(_fld047F && (j & 1) != 0)
            j++;
        if(i < 0 || j < 0 || i >= _fld0468 || j >= _fld0469)
            return false;
        int k = _fld046E[i + j * _fld0468];
        return (k & 0xf00f00) != 0;
    }

    public int _mth0209(int i, int j)
    {
        if(_fld047F && (j & 1) != 0)
            j++;
        if(i < 0 || j < 0 || i >= _fld0468 || j >= _fld0469)
        {
            return 0;
        } else
        {
            int k = _fld046E[i + j * _fld0468];
            return ((k & 0xf00000) >> 16) + ((k & 0xf00) >> 8);
        }
    }

    public int _mth01DF(int i, int j, int k)
    {
        if(i < 0 || j < 0 || i >= spriteWidth[k] || j >= spriteHeight[k])
            return 0;
        else
            return sprite[k][i + j * spriteWidth[k]];
    }

    public int loadFont(String s)
    {
        s = "fonts/" + s + ".jf";
        for(int i = 0; i < fontCount; i++)
            if(s.equalsIgnoreCase(fonts[i]))
                return i;

        try
        {
            Stream w1 = new Stream(s);
            _fld0480[fontCount] = w1._mth02B3();
            fonts[fontCount] = s;
            w1.closeStream();
        }
        catch(IOException ioexception)
        {
            System.out.println("Unable to load font: " + s);
            ioexception.printStackTrace();
        }
        return fontCount++;
    }

    public void _mth01FC(String s, int i, int j, int k, int l)
    {
        drawstring(s, i - _mth020C(s, k) / 2, j, k, l, false);
    }

    public void drawstring(String text, int x, int y, int font, int colour, boolean flag)
    {
        boolean flag1 = _fld047F;
        if(!flag)
            flag1 = false;
        byte abyte0[] = _fld0480[font];
        if(flag1)
            y &= -2;
        for(int i1 = 0; i1 < text.length(); i1++)
            if(text.charAt(i1) == '@' && i1 + 4 < text.length() && text.charAt(i1 + 4) == '@')
            {
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("red"))
                    colour = 0xff000000;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("yel"))
                    colour = 0xff0ff000;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("gre"))
                    colour = 0xff000;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("blu"))
                    colour = 255;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("cya"))
                    colour = 0xff0ff;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("mag"))
                    colour = 0xff0000ff;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("whi"))
                    colour = 0xff0ff0ff;
                else
                if(text.substring(i1 + 1, i1 + 4).equalsIgnoreCase("bla"))
                    colour = 0;
                i1 += 4;
            } else
            if(text.charAt(i1) == '~' && i1 + 4 < text.length() && text.charAt(i1 + 4) == '~')
            {
                char c = text.charAt(i1 + 1);
                char c1 = text.charAt(i1 + 2);
                char c2 = text.charAt(i1 + 3);
                if(c >= '0' && c <= '9' && c1 >= '0' && c1 <= '9' && c2 >= '0' && c2 <= '9')
                    x = Integer.parseInt(text.substring(i1 + 1, i1 + 4));
                i1 += 4;
            } else
            {
                int j1 = _fld0481[text.charAt(i1)];
                int k1 = x + abyte0[j1 + 5];
                int l1 = y - abyte0[j1 + 6];
                int i2 = abyte0[j1 + 3];
                int j2 = abyte0[j1 + 4];
                int k2 = abyte0[j1] * 16384 + abyte0[j1 + 1] * 128 + abyte0[j1 + 2];
                int l2 = k1 + l1 * _fld0468;
                int i3 = _fld0468 - i2;
                int j3 = 0;
                if(l1 < _fld047B)
                {
                    int k3 = _fld047B - l1;
                    j2 -= k3;
                    l1 = _fld047B;
                    k2 += k3 * i2;
                    l2 += k3 * _fld0468;
                }
                if(l1 + j2 >= _fld047C)
                    j2 -= (l1 + j2) - _fld047C;
                if(k1 < _fld047D)
                {
                    int l3 = _fld047D - k1;
                    i2 -= l3;
                    k1 = _fld047D;
                    k2 += l3;
                    l2 += l3;
                    j3 += l3;
                    i3 += l3;
                }
                if(k1 + i2 >= _fld047E)
                {
                    int i4 = (k1 + i2) - _fld047E;
                    i2 -= i4;
                    j3 += i4;
                    i3 += i4;
                }
                if(i2 > 0 && j2 > 0)
                    if(!flag1)
                    {
                        _mth0211(_fld046E, abyte0, colour, k2, l2, i2, j2, i3, j3);
                    } else
                    {
                        byte byte0 = abyte0[j1 + 3];
                        int j4 = j2;
                        i3 += _fld0468;
                        j3 += byte0;
                        if((l1 & 1) != 0)
                        {
                            l2 += _fld0468;
                            k2 += byte0;
                            j2--;
                        }
                        _mth01F5(_fld046E, abyte0, 0, k2, l2, i2, j2, i3, j3, colour, byte0, j4);
                    }
                x += abyte0[j1 + 7];
            }

    }

    private void _mth0211(int ai[], byte abyte0[], int i, int j, int k, int l, int i1, 
            int j1, int k1)
    {
        int l1 = -(l >> 2);
        l = -(l & 3);
        for(int i2 = -i1; i2 < 0; i2++)
        {
            for(int j2 = l1; j2 < 0; j2++)
            {
                if(abyte0[j++] != 0)
                    ai[k++] = i;
                else
                    k++;
                if(abyte0[j++] != 0)
                    ai[k++] = i;
                else
                    k++;
                if(abyte0[j++] != 0)
                    ai[k++] = i;
                else
                    k++;
                if(abyte0[j++] != 0)
                    ai[k++] = i;
                else
                    k++;
            }

            for(int k2 = l; k2 < 0; k2++)
                if(abyte0[j++] != 0)
                    ai[k++] = i;
                else
                    k++;

            k += j1;
            j += k1;
        }

    }

    private void _mth01F5(int ai[], byte abyte0[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2, int j2)
    {
        int k2 = l1 >>> 1 & 0xff0ff0ff;
        int l2 = -(l >> 2);
        l = -(l & 3);
        for(int i3 = -i1; i3 <= 0; i3 += 2)
            if(i3 == 0)
            {
                for(int j3 = l2; j3 < 0; j3++)
                {
                    i = abyte0[-i2 + j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[-i2 + j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[-i2 + j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[-i2 + j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                }

                for(int i4 = l; i4 < 0; i4++)
                {
                    i = abyte0[-i2 + j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                }

                k += j1;
                j += k1;
            } else
            if(i3 == -j2)
            {
                for(int k3 = l2; k3 < 0; k3++)
                {
                    i = abyte0[j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                }

                for(int j4 = l; j4 < 0; j4++)
                {
                    i = abyte0[j++];
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                }

                k += j1;
                j += k1;
            } else
            {
                for(int l3 = l2; l3 < 0; l3++)
                {
                    i = abyte0[j - i2] + abyte0[j++];
                    if(i == 2)
                        ai[k] = l1;
                    else
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[j - i2] + abyte0[j++];
                    if(i == 2)
                        ai[k] = l1;
                    else
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[j - i2] + abyte0[j++];
                    if(i == 2)
                        ai[k] = l1;
                    else
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                    i = abyte0[j - i2] + abyte0[j++];
                    if(i == 2)
                        ai[k] = l1;
                    else
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                }

                for(int k4 = l; k4 < 0; k4++)
                {
                    i = abyte0[j - i2] + abyte0[j++];
                    if(i == 2)
                        ai[k] = l1;
                    else
                    if(i == 1)
                        ai[k] = ((ai[k] & 0xff0ff0ff) >>> 1) + k2;
                    k++;
                }

                k += j1;
                j += k1;
            }

    }

    public int _mth01F4(int i)
    {
        return _fld0480[i][8];
    }

    public int _mth020C(String s, int i)
    {
        int j = 0;
        byte abyte0[] = _fld0480[i];
        for(int k = 0; k < s.length(); k++)
            if(s.charAt(k) == '@' && k + 4 < s.length() && s.charAt(k + 4) == '@')
                k += 4;
            else
            if(s.charAt(k) == '~' && k + 4 < s.length() && s.charAt(k + 4) == '~')
                k += 4;
            else
                j += abyte0[_fld0481[s.charAt(k)] + 7];

        return j;
    }

    public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1)
    {
        return true;
    }

    public int _fld0468;
    public int _fld0469;
    public int _fld046A;
    public int _fld046B;
    public int _fld046C;
    ColorModel _fld046D;
    public int _fld046E[];
    ImageConsumer _fld046F;
    private Component _fld0470;
    public Image _fld0471;
    public int sprite[][];
    public int spriteWidth[];
    public int spriteHeight[];
    public int _fld0475[];
    public int _fld0476[];
    public int _fld0477[];
    public int _fld0478[];
    public boolean _fld0479[];
    public boolean _fld047A;
    private int _fld047B;
    private int _fld047C;
    private int _fld047D;
    private int _fld047E;
    private boolean _fld047F;
    byte _fld0480[][];
    int _fld0481[];
    int fontCount;
    String fonts[];
    int _fld0484[];
    int _fld0485[];
    int _fld0486[];
    int _fld0490[];
    int _fld0491[];
    int _fld0492[];
    int _fld0493[];
    public static final int _fld0494 = 0;
    public static final int _fld0495 = 0xff0ff0ff;
    public static final int _fld0496 = 0xff000000;
    public static final int _fld0497 = 0xc0000000;
    public static final int _fld0498 = 0xff000;
    public static final int _fld0499 = 255;
    public static final int _fld049A = 0xff0ff000;
    public static final int _fld049B = 0xff0ff;
    public static final int _fld049C = 0xff0000ff;
    public static final int _fld049D = 0xc00c00c0;
    public static final int _fld049E = 0x80080080;
    public static final int _fld049F = 0x40040040;
    public int _fld04A0;
}

