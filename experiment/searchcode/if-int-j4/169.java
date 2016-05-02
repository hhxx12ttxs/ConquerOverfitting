// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   pixmap.java

package jagex;

import java.awt.*;
import java.awt.image.*;

// Referenced classes of package jagex:
//            GameDialog

public class Surface2mby
    implements ImageProducer, ImageObserver
{

    public Surface2mby(int i, int j, int k, Component component)
    {
        _fld0406 = new DirectColorModel(32, 0xff000000, 0xff000, 255);
        _fld0403 = i;
        _fld0404 = j;
        _fld0405 = i * j;
        _fld0407 = new int[i * j];
        _fld040A = new int[k][];
        _fld0412 = new boolean[k];
        _fld040B = new int[k];
        _fld040C = new int[k];
        _fld0410 = new int[k];
        _fld0411 = new int[k];
        _fld040E = new int[k];
        _fld040F = new int[k];
        int l = _fld0403 * _fld0404;
        for(int i1 = 0; i1 < l; i1++)
            _fld0407[i1] = 0;

        _fld0409 = component.createImage(this);
        _mth01D0(false);
        component.prepareImage(_fld0409, component);
        _mth01D0(false);
        component.prepareImage(_fld0409, component);
        _mth01D0(false);
        component.prepareImage(_fld0409, component);
        _fld0414 = j;
        _fld0416 = i;
    }

    public synchronized void addConsumer(ImageConsumer imageconsumer)
    {
        _fld0408 = imageconsumer;
        imageconsumer.setDimensions(_fld0403, _fld0404);
        imageconsumer.setProperties(null);
        imageconsumer.setColorModel(_fld0406);
        imageconsumer.setHints(14);
    }

    public synchronized boolean isConsumer(ImageConsumer imageconsumer)
    {
        return _fld0408 == imageconsumer;
    }

    public synchronized void removeConsumer(ImageConsumer imageconsumer)
    {
        if(_fld0408 == imageconsumer)
            _fld0408 = null;
    }

    public void startProduction(ImageConsumer imageconsumer)
    {
        addConsumer(imageconsumer);
    }

    public void requestTopDownLeftRightResend(ImageConsumer imageconsumer)
    {
        System.out.println("TDLR");
    }

    public synchronized void _mth01D0(boolean flag)
    {
        if(_fld0408 == null)
            return;
        if(!flag)
        {
            _fld0408.setPixels(0, 0, _fld0403, _fld0404, _fld0406, _fld0407, 0, _fld0403);
        } else
        {
            for(int i = 0; i < _fld0404; i += 2)
                _fld0408.setPixels(0, i, _fld0403, 1, _fld0406, _fld0407, i * _fld0403, _fld0403);

        }
        _fld0408.imageComplete(2);
    }

    public void _mth01C2(Graphics g, int i, int j, boolean flag)
    {
        _mth01D0(flag);
        g.drawImage(_fld0409, i, j, this);
    }

    public void _mth01BD(boolean flag)
    {
        int i = _fld0403 * _fld0404;
        if(!flag)
        {
            for(int j = 0; j < i; j++)
                _fld0407[j] = 0;

            return;
        }
        int k = 0;
        for(int l = -_fld0404; l < 0; l += 2)
        {
            for(int i1 = -_fld0403; i1 < 0; i1++)
                _fld0407[k++] = 0;

            k += _fld0403;
        }

    }

    public void _mth01B3(int i, int j, int k, int l, Color color, int i1, boolean flag)
    {
        int j1 = 256 - i1;
        int k1 = color.getRed() * i1;
        int l1 = color.getGreen() * i1;
        int i2 = color.getBlue() * i1;
        int i3 = _fld0403 - k;
        byte byte0 = 1;
        if(flag)
        {
            byte0 = 2;
            i3 += _fld0403;
            if((j & 1) != 0)
            {
                j++;
                l--;
            }
        }
        int j3 = i + j * _fld0403;
        for(int k3 = 0; k3 < l; k3 += byte0)
        {
            for(int l3 = -k; l3 < 0; l3++)
            {
                int j2 = (_fld0407[j3] >> 24 & 0xff) * j1;
                int k2 = (_fld0407[j3] >> 12 & 0xff) * j1;
                int l2 = (_fld0407[j3] & 0xff) * j1;
                int i4 = ((k1 + j2 >> 8) << 24) + ((l1 + k2 >> 8) << 12) + (i2 + l2 >> 8);
                _fld0407[j3++] = i4;
            }

            j3 += i3;
        }

    }

    public void _mth01CA(int i, int j, int k, int l, Color color, Color color1, boolean flag)
    {
        int i1 = color1.getRed();
        int j1 = color1.getGreen();
        int k1 = color1.getBlue();
        int l1 = color.getRed();
        int i2 = color.getGreen();
        int j2 = color.getBlue();
        int k2 = _fld0403 - k;
        byte byte0 = 1;
        if(flag)
        {
            byte0 = 2;
            k2 += _fld0403;
            if((j & 1) != 0)
            {
                j++;
                l--;
            }
        }
        int l2 = i + j * _fld0403;
        for(int i3 = 0; i3 < l; i3 += byte0)
        {
            int j3 = ((i1 * i3 + l1 * (l - i3)) / l << 24) + ((j1 * i3 + i2 * (l - i3)) / l << 12) + (k1 * i3 + j2 * (l - i3)) / l;
            for(int k3 = -k; k3 < 0; k3++)
                _fld0407[l2++] = j3;

            l2 += k2;
        }

    }

    public void _mth01C7(int i, int j, int k, int l, Color color)
    {
        int i1 = (color.getRed() << 24) + (color.getGreen() << 12) + color.getBlue();
        int j1 = i + j * _fld0403;
        int k1 = _fld0403 - k;
        for(int l1 = -l; l1 < 0; l1++)
        {
            for(int i2 = -k; i2 < 0; i2++)
                _fld0407[j1++] = i1;

            j1 += k1;
        }

    }

    public void _mth01B8(int i, int j, int k, int l, Color color)
    {
        int i1 = (color.getRed() << 24) + (color.getGreen() << 12) + color.getBlue();
        int j1 = i + j * _fld0403;
        for(int k1 = 0; k1 < k; k1++)
        {
            _fld0407[j1 + k1] = i1;
            _fld0407[j1 + k1 + (l - 1) * _fld0403] = i1;
        }

        for(int l1 = 0; l1 < l; l1++)
        {
            _fld0407[j1 + l1 * _fld0403] = i1;
            _fld0407[(j1 + l1 * _fld0403 + k) - 1] = i1;
        }

    }

    public void _mth01D2(int i, boolean flag)
    {
        int l = _fld0403 * _fld0404;
        for(int k = 0; k < l; k++)
        {
            int j = _fld0407[k] & 0xff0ff0ff;
            _fld0407[k] = (j >>> 1) + (j >>> 2) + (j >>> 3) + (j >>> 4);
        }

    }

    public void _mth01CD(int i)
    {
        for(int j = 0; j < _fld0403; j++)
        {
            for(int k = 0; k < _fld0404; k++)
            {
                int l = 0;
                int i1 = 0;
                int j1 = 0;
                int k1 = 0;
                for(int l1 = j - i; l1 <= j + i; l1++)
                    if(l1 >= 0 && l1 < _fld0403)
                    {
                        for(int i2 = k - i; i2 <= k + i; i2++)
                            if(i2 >= 0 && i2 < _fld0404)
                            {
                                int j2 = _fld0407[l1 + _fld0403 * i2];
                                l += j2 >> 24 & 0xff;
                                i1 += j2 >> 12 & 0xff;
                                j1 += j2 & 0xff;
                                k1++;
                            }

                    }

                _fld0407[j + _fld0403 * k] = (l / k1 << 24) + (i1 / k1 << 12) + j1 / k1;
            }

        }

    }

    public void _mth01CC()
    {
        int i = _fld0403 * _fld0404;
        for(int j = 0; j < i; j++)
        {
            int k = _fld0407[j];
            int l = k >> 24 & 0xff;
            int i1 = k >> 12 & 0xff;
            int j1 = k & 0xff;
            int k1 = j1 / 6 + i1 / 2 + l / 3;
            _fld0407[j] = (k1 << 24) + (k1 << 12) + k1;
        }

    }

    public void _mth01CF()
    {
        for(int i = 0; i < _fld040A.length; i++)
        {
            _fld040A[i] = null;
            _fld040B[i] = 0;
            _fld040C[i] = 0;
        }

    }

    public void _mth01B7(String s, int i, boolean flag)
    {
        Image image = null;
        for(int j = 0; j < 5; j++)
        {
            image = GameDialog.loadImage(s);
            if(image != null)
                break;
            if(j == 4)
                throw new RuntimeException("Error loading:" + s);
        }

        try
        {
            _mth01D1(image, i, flag);
            return;
        }
        catch(Exception _ex)
        {
            throw new RuntimeException("Error buildling:" + s);
        }
    }

    public void _mth01D4(String s, int i, boolean flag, int j, int k, int l)
    {
        Image image = null;
        for(int i1 = 0; i1 < 5; i1++)
        {
            image = GameDialog.loadImage(s);
            if(image != null)
                break;
            if(i1 == 4)
                throw new RuntimeException("Error loading:" + s);
        }

        try
        {
            int j1 = image.getWidth(GameDialog._fld0183);
            int k1 = image.getHeight(GameDialog._fld0183);
            int ai[] = new int[j1 * k1];
            PixelGrabber pixelgrabber = new PixelGrabber(image, 0, 0, j1, k1, ai, 0, j1);
            try
            {
                pixelgrabber.grabPixels();
            }
            catch(InterruptedException _ex)
            {
                System.out.println("Error!");
            }
            int l1 = 0;
            int i2 = 0;
            for(int j2 = 0; j2 < j; j2++)
            {
                int k2 = 0;
                int ai1[] = new int[k * l];
                for(int l2 = i2; l2 < i2 + l; l2++)
                {
                    for(int i3 = l1; i3 < l1 + k; i3++)
                        ai1[k2++] = ai[i3 + l2 * j1];

                }

                _mth01CE(ai1, k, l, j2 + i, flag);
                l1 += k;
                if(l1 >= j1)
                {
                    l1 = 0;
                    i2 += l;
                }
            }

            image.flush();
            image = null;
            return;
        }
        catch(Exception _ex)
        {
            throw new RuntimeException("Error buildling:" + s);
        }
    }

    public void _mth01D1(Image image, int i, boolean flag)
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
        _mth01CE(ai, j, k, i, flag);
    }

    private void _mth01CE(int ai[], int i, int j, int k, boolean flag)
    {
        int l = 0;
        int i1 = 0;
        int j1 = i;
        int k1 = j;
        if(flag)
        {
label0:
            for(int l1 = 0; l1 < j; l1++)
            {
                for(int j2 = 0; j2 < i; j2++)
                {
                    int i3 = ai[j2 + l1 * i];
                    if((i3 & 0xff000000) == 0 || i3 == -65281)
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
                    if((i4 & 0xff000000) == 0 || i4 == -65281)
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
                    if((i5 & 0xff000000) == 0 || i5 == -65281)
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
                    if((k5 & 0xff000000) == 0 || k5 == -65281)
                        continue;
                    j1 = k4 + 1;
                    break label3;
                }

            }

        }
        _fld040A[k] = new int[(j1 - l) * (k1 - i1)];
        _fld040B[k] = j1 - l;
        _fld040C[k] = k1 - i1;
        _fld0412[k] = flag;
        _fld040E[k] = l;
        _fld040F[k] = i1;
        _fld0410[k] = i;
        _fld0411[k] = j;
        int i2 = 0;
        for(int l2 = i1; l2 < k1; l2++)
        {
            for(int l3 = l; l3 < j1; l3++)
            {
                int l4 = ai[l3 + l2 * i];
                if(l4 == 0xff000000)
                    l4 = 0xff010101;
                if((l4 & 0xff000000) == 0 || l4 == -65281)
                    l4 = 0;
                l4 = ((l4 & 0xff0000) << 8) + ((l4 & 0xff00) << 4) + (l4 & 0xff);
                _fld040A[k][i2++] = l4;
            }

        }

    }

    public void _mth01B1(int i)
    {
        _fld040B[i] = _fld0403;
        _fld040C[i] = _fld0404;
        _fld0412[i] = false;
        _fld040E[i] = 0;
        _fld040F[i] = 0;
        _fld0410[i] = _fld0403;
        _fld0411[i] = _fld0404;
        int j = _fld0403 * _fld0404;
        _fld040A[i] = new int[j];
        for(int k = 0; k < j; k++)
            _fld040A[i][k] = _fld0407[k];

    }

    public void _mth01B6(int i, int j)
    {
        _fld040B[j] = _fld040B[i];
        _fld040C[j] = _fld040C[i];
        _fld040E[j] = _fld040E[i];
        _fld040F[j] = _fld040F[i];
        _fld0412[j] = _fld0412[i];
        _fld0410[j] = _fld0410[i];
        _fld0411[j] = _fld0411[i];
        int k = _fld040B[i] * _fld040C[i];
        _fld040A[j] = new int[k];
        for(int l = 0; l < k; l++)
            _fld040A[j][l] = _fld040A[i][l];

    }

    public void _mth01BC(int i, int j, int k)
    {
        _mth01B5(i - _fld0410[k] / 2, j - _fld0411[k] / 2, k);
    }

    public void _mth01B5(int i, int j, int k)
    {
        if(_fld0412[k])
        {
            i += _fld040E[k];
            j += _fld040F[k];
        }
        int l = i + j * _fld0403;
        int i1 = 0;
        int j1 = -_fld040C[k];
        int k1 = -_fld040B[k];
        int l1 = _fld0403 + k1;
        int i2 = 0;
        if(j < _fld0413)
        {
            int j2 = _fld0413 - j;
            j1 += j2;
            j = 0;
            i1 += j2 * _fld040B[k];
            l += j2 * _fld0403;
        }
        if(j - j1 >= _fld0414)
            j1 += j - j1 - _fld0414;
        if(i < _fld0415)
        {
            int k2 = _fld0415 - i;
            k1 += k2;
            i = 0;
            i1 += k2;
            l += k2;
            i2 += k2;
            l1 += k2;
        }
        if(i - k1 >= _fld0416)
        {
            int l2 = i - k1 - _fld0416;
            k1 += l2;
            i2 += l2;
            l1 += l2;
        }
        if(_fld0412[k])
            if((-k1 & 3) == 0)
            {
                _mth01B2(_fld0407, _fld040A[k], 0, i1, l, k1, j1, l1, i2);
                return;
            } else
            {
                _mth01C6(_fld0407, _fld040A[k], 0, i1, l, k1, j1, l1, i2);
                return;
            }
        if((-k1 & 3) == 0)
        {
            _mth01BE(_fld0407, _fld040A[k], i1, l, k1, j1, l1, i2);
            return;
        } else
        {
            _mth01BA(_fld0407, _fld040A[k], i1, l, k1, j1, l1, i2);
            return;
        }
    }

    private void _mth01BE(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1)
    {
        k >>= 2;
        for(int k1 = l; k1 < 0; k1++)
        {
            for(int l1 = k; l1 < 0; l1++)
            {
                ai[j++] = ai1[i++];
                ai[j++] = ai1[i++];
                ai[j++] = ai1[i++];
                ai[j++] = ai1[i++];
            }

            j += i1;
            i += j1;
        }

    }

    private void _mth01BA(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1)
    {
        for(int k1 = l; k1 < 0; k1++)
        {
            for(int l1 = k; l1 < 0; l1++)
                ai[j++] = ai1[i++];

            j += i1;
            i += j1;
        }

    }

    private void _mth01B2(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1)
    {
        l >>= 2;
        for(int l1 = i1; l1 < 0; l1++)
        {
            for(int i2 = l; i2 < 0; i2++)
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

            k += j1;
            j += k1;
        }

    }

    private void _mth01C6(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1)
    {
        for(int l1 = i1; l1 < 0; l1++)
        {
            for(int i2 = l; i2 < 0; i2++)
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

    public void _mth01CB(int i, int j, int k, int l, int i1)
    {
        _mth01C9(null, 0, 0, 0, l, i + _fld040E[k], j + (_fld040F[k] * (_fld0411[k] - i1)) / _fld0411[k], k, (i1 * _fld040C[k]) / _fld0411[k]);
    }

    private void _mth01C9(int ai[], int i, int j, int k, int l, int i1, int j1, 
            int k1, int l1)
    {
        try
        {
            ai = _fld0407;
            i = i1 + j1 * _fld0403;
            j = 0;
            int ai1[] = _fld040A[k1];
            int i2 = -_fld040C[k1];
            int j2 = -_fld040B[k1];
            int k2 = _fld0403 + j2;
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
            if(j1 < _fld0413)
            {
                i2 -= j1 - _fld0413;
                j += (j1 - _fld0413) * j2;
                i = i1 + _fld0413 * _fld0403;
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
        System.out.println("spr:" + _fld040A[k1] + " h:" + _fld040C[k1] + " w:" + _fld040B[k1]);
    }

    public void _mth01C3(int i, int j, int k, int l, int i1)
    {
        _mth01C0(null, 0, 0, 0, l, i + (_fld040E[k] * (_fld0410[k] - i1)) / _fld0410[k], j + _fld040F[k], k, (i1 * _fld040B[k]) / _fld0410[k]);
    }

    private void _mth01C0(int ai[], int i, int j, int k, int l, int i1, int j1, 
            int k1, int l1)
    {
        try
        {
            ai = _fld0407;
            i = i1 + j1 * _fld0403;
            j = 0;
            int ai1[] = _fld040A[k1];
            int i2 = -_fld040C[k1];
            int j2 = -_fld040B[k1];
            int k2 = 1 + _fld0403 * i2;
            int l2 = 1 - j2 * i2;
            int i3 = _fld0403;
            if(l < 0)
            {
                l = -l;
                i3 = -_fld0403;
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
                    i += _fld0403;
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
        System.out.println("spr:" + _fld040A[k1] + " h:" + _fld040C[k1] + " w:" + _fld040B[k1]);
    }

    public void _mth01C4(int i, int j, int k, int l, int i1)
    {
        try
        {
            if(_fld0412[i1])
            {
                i += (_fld040E[i1] * k) / _fld0410[i1];
                j += (_fld040F[i1] * l) / _fld0411[i1];
                k = (k * _fld040B[i1]) / _fld0410[i1];
                l = (l * _fld040C[i1]) / _fld0411[i1];
            }
            int j1 = i + j * _fld0403;
            int k1 = _fld040B[i1];
            int l1 = _fld040C[i1];
            int i2 = _fld0403 - k;
            int j2 = 0;
            int k2 = 0;
            int l2 = (k1 << 16) / k;
            int i3 = (l1 << 16) / l;
            if(j < _fld0413)
            {
                int j3 = _fld0413 - j;
                l -= j3;
                j = 0;
                j1 += j3 * _fld0403;
                k2 += i3 * j3;
            }
            if(j + l >= _fld0414)
                l -= (j + l) - _fld0414;
            if(i < _fld0415)
            {
                int k3 = _fld0415 - i;
                k -= k3;
                i = 0;
                j1 += k3;
                j2 += l2 * k3;
                i2 += k3;
            }
            if(i + k >= _fld0416)
            {
                int l3 = (i + k) - _fld0416;
                k -= l3;
                i2 += l3;
            }
            if(_fld0412[i1])
            {
                _mth01BB(_fld0407, _fld040A[i1], 0, j2, k2, j1, i2, k, l, l2, i3, k1);
                return;
            } else
            {
                _mth01B9(_fld0407, _fld040A[i1], j1, j2, k2, i2, k, l, l2, i3, k1);
                return;
            }
        }
        catch(Exception _ex)
        {
            System.out.println("error in sprite clipping routine");
        }
    }

    private void _mth01B9(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2)
    {
        try
        {
            int j2 = j;
            for(int k2 = -j1; k2 < 0; k2++)
            {
                int l2 = (k >> 16) * i2;
                for(int i3 = -i1; i3 < 0; i3++)
                {
                    ai[i++] = ai1[(j >> 16) + l2];
                    j += k1;
                }

                k += l1;
                j = j2;
                i += l;
            }

            return;
        }
        catch(Exception _ex)
        {
            System.out.println("error in sprite plot routine");
        }
    }

    private void _mth01BB(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
            int j1, int k1, int l1, int i2, int j2)
    {
        try
        {
            int k2 = j;
            for(int l2 = -k1; l2 < 0; l2++)
            {
                int i3 = (k >> 16) * j2;
                for(int j3 = -j1; j3 < 0; j3++)
                {
                    i = ai1[(j >> 16) + i3];
                    if(i != 0)
                        ai[l++] = i;
                    else
                        l++;
                    j += l1;
                }

                k += i2;
                j = k2;
                l += i1;
            }

            return;
        }
        catch(Exception _ex)
        {
            System.out.println("error in transparent sprite plot routine");
        }
    }

    public void _mth01C8(int i, int j, int k, int l, int i1, int j1)
    {
        if(_fld0412[i1])
        {
            i += (_fld040E[i1] * k) / _fld0410[i1];
            j += (_fld040F[i1] * l) / _fld0411[i1];
            k = (k * _fld040B[i1]) / _fld0410[i1];
            l = (l * _fld040C[i1]) / _fld0411[i1];
        }
        int k1 = i + j * _fld0403;
        int l1 = _fld040B[i1];
        int i2 = _fld040C[i1];
        int j2 = _fld0403 - k;
        int k2 = 0;
        int l2 = 0;
        int i3 = (l1 << 16) / k;
        int j3 = (i2 << 16) / l;
        if(j < _fld0413)
        {
            int k3 = _fld0413 - j;
            l -= k3;
            j = 0;
            k1 += k3 * _fld0403;
            l2 += j3 * k3;
        }
        if(j + l >= _fld0414)
            l -= (j + l) - _fld0414;
        if(i < _fld0415)
        {
            int l3 = _fld0415 - i;
            k -= l3;
            i = 0;
            k1 += l3;
            k2 += i3 * l3;
            j2 += l3;
        }
        if(i + k >= _fld0416)
        {
            int i4 = (i + k) - _fld0416;
            k -= i4;
            j2 += i4;
        }
        if(j1 == 128)
        {
            _mth01C1(_fld0407, _fld040A[i1], 0, k2, l2, k1, j2, k, l, i3, j3, l1);
            return;
        } else
        {
            _mth01BF(_fld0407, _fld040A[i1], 0, k2, l2, k1, j2, k, l, i3, j3, l1, j1);
            return;
        }
    }

    private void _mth01C1(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
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

    private void _mth01BF(int ai[], int ai1[], int i, int j, int k, int l, int i1, 
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

    public void _mth01B4(int i, int j, int k, int l, int i1)
    {
        _mth01D3(_fld0407, 0, 0, i, j, k, l, i1);
    }

    private void _mth01D3(int ai[], int i, int j, int k, int l, int i1, int j1, 
            int k1)
    {
        boolean flag = false;
        ai = _fld0407;
        i = k + l * _fld0403;
        j = 0;
        int ai1[] = _fld040A[i1];
        int i2 = -_fld040C[i1];
        int j2 = -_fld040B[i1];
        int k2 = _fld0403 + j2;
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

    public void _mth01C5(int i)
    {
        int ai[] = _fld040A[i];
        int j = _fld040B[i] * _fld040C[i];
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

    public boolean imageUpdate(Image image, int i, int j, int k, int l, int i1)
    {
        return true;
    }

    public int _fld0403;
    public int _fld0404;
    public int _fld0405;
    ColorModel _fld0406;
    public int _fld0407[];
    ImageConsumer _fld0408;
    public Image _fld0409;
    public int _fld040A[][];
    public int _fld040B[];
    public int _fld040C[];
    public int _fld040E[];
    public int _fld040F[];
    public int _fld0410[];
    public int _fld0411[];
    public boolean _fld0412[];
    public int _fld0413;
    public int _fld0414;
    public int _fld0415;
    public int _fld0416;
}

