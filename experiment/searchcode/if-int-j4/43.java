// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   world3d.java

package jagex;

import java.awt.*;
import java.awt.image.PixelGrabber;

// Referenced classes of package jagex:
//            GameModel, GameDialog

public class Scene
{

    public Scene(int ai[], int i, int j, int l)
    {
        _fld04A1 = 5;
        _fld04A2 = -5000;
        _fld04A3 = 1000;
        fog = 20;
        _fld04A5 = 3;
        _fld04A6 = 512;
        _fld04A7 = new int[512];
        _fld04A8 = 1.1000000000000001D;
        _fld04A9 = 1;
        _fld04AA = 180;
        _fld04AB = 155;
        _fld04AC = 95;
        _fld04AD = 256;
        _fld04AE = 256;
        _fld04AF = 192;
        _fld04B0 = 256;
        _fld04B1 = 256;
        _fld04B2 = 4;
        _fld04D3 = 0xbc614e;
        _fld04DD = new int[0x30000];
        _fld04DE = new int[384];
        _fld04DF = new int[384];
        _fld04E0 = new int[384];
        _fld04E1 = new int[384];
        _fld04E2 = false;
        _fld04AE = 256;
        _fld04AF = 192;
        _fld04DD = ai;
        modelCount = 0;
        modelCountMax = i;
        models = new GameModel[modelCountMax];
        _fld04BC = new int[modelCountMax];
        _fld04BE = 0;
        _fld04BD = new GameModel[j];
        _fld04BF = new int[j];
        _fld04C0 = new int[j];
        _fld04C1 = new int[j];
        _fld04C2 = new int[j];
        _fld04C3 = new int[j];
        _fld04C4 = new int[j];
        _fld04C7 = new int[j];
        _fld04D1 = new int[j];
        _fld04D2 = new int[j];
        _fld04C8 = new int[j];
        _fld04CC = new int[j];
        _fld04CB = new int[j];
        _fld04D0 = new int[j];
        _fld04B3 = 0;
        _fld04B4 = 0;
        _fld04B5 = 0;
        _fld04B6 = 0;
        _fld04B7 = 0;
        _fld04B8 = 0;
        for(int i1 = 0; i1 < 256; i1++)
        {
            _fld04A7[i1] = (int)(Math.sin((double)i1 * 0.02454369D) * 32768D);
            _fld04A7[i1 + 256] = (int)(Math.cos((double)i1 * 0.02454369D) * 32768D);
        }

    }

    public void addModel(GameModel k1)
    {
        if(modelCount < modelCountMax)
        {
            _fld04BC[modelCount] = 0;
            models[modelCount++] = k1;
        }
    }

    public void _mth0258(GameModel k1)
    {
        for(int i = 0; i < modelCount; i++)
            if(models[i] == k1)
            {
                modelCount--;
                for(int j = i; j < modelCount; j++)
                {
                    models[j] = models[j + 1];
                    _fld04BC[j] = _fld04BC[j + 1];
                }

            }

    }

    public void _mth0253(GameModel k1, int i)
    {
        int j = k1._mth013B();
        int l = k1._mth012F();
        int i1 = k1._mth0132();
        k1._mth011B(0, 0, 0);
        GameModel ak[] = k1._mth0127();
        _mth0258(k1);
        for(int j1 = 0; j1 < ak.length; j1++)
            if(modelCount < modelCountMax)
            {
                _fld04BC[modelCount] = i;
                models[modelCount++] = ak[j1];
                ak[j1]._mth012C(j, l, i1);
                ak[j1]._mth0131();
            }

    }

    public void clear()
    {
        for(int i = 0; i < modelCount; i++)
            models[i] = null;

        modelCount = 0;
    }

    public void _mth026A()
    {
        for(int i = 0; i < modelCount; i++)
            if(_fld04BC[i] > 0)
            {
                _fld04BC[i]--;
                models[i]._mth0140(_fld04A8);
                models[i]._mth013E(_fld04A9, _fld04A9, _fld04A9);
                if(_fld04BC[i] == 0)
                {
                    _mth0258(models[i]);
                    i--;
                }
            }

    }

    public void _mth0262()
    {
        if(modelCount == 0)
            return;
        for(int i = 0; i < modelCount; i++)
            models[i]._mth0135(0, 0, 0, 0, 0, 0, 8, _fld04A1);

    }

    public void _mth0256(Graphics g, int i, int j)
    {
        if(modelCount == 0)
            return;
        g.setColor(Color.yellow);
        for(int l = 0; l < modelCount; l++)
        {
            GameModel k1 = models[l];
            if(k1._fld0274 == 0)
            {
                for(int i1 = 0; i1 < k1._fld0266; i1++)
                {
                    int l1 = k1._fld0267[i1];
                    int ai[] = k1._fld0268[i1];
                    for(int l2 = 0; l2 < l1; l2++)
                    {
                        int i3;
                        int k3;
                        if(l2 == l1 - 1)
                        {
                            i3 = ai[l2];
                            k3 = ai[0];
                        } else
                        {
                            i3 = ai[l2];
                            k3 = ai[l2 + 1];
                        }
                        if(i == 0)
                            g.drawLine((k1._fld025D[i3] - _fld04B5 << 8) / j, (k1._fld025C[i3] - _fld04B4 << 8) / j, (k1._fld025D[k3] - _fld04B5 << 8) / j, (k1._fld025C[k3] - _fld04B4 << 8) / j);
                        if(i == 1)
                            g.drawLine((k1._fld025B[i3] - _fld04B3 << 8) / j, (k1._fld025C[i3] - _fld04B4 << 8) / j, (k1._fld025B[k3] - _fld04B3 << 8) / j, (k1._fld025C[k3] - _fld04B4 << 8) / j);
                        if(i == 2)
                            g.drawLine((k1._fld025B[i3] - _fld04B3 << 8) / j, (_fld04B5 - k1._fld025D[i3] << 8) / j, (k1._fld025B[k3] - _fld04B3 << 8) / j, (_fld04B5 - k1._fld025D[k3] << 8) / j);
                    }

                }

            }
        }

        for(int j1 = 0; j1 < modelCount; j1++)
        {
            GameModel k2 = models[j1];
            if(k2._fld0274 == 1)
            {
                for(int i2 = 0; i2 < k2._fld0266; i2++)
                {
                    if(k2._fld0266 > 1)
                        g.setColor(Color.green);
                    else
                        g.setColor(Color.magenta);
                    int j2 = k2._fld0267[i2];
                    int ai1[] = k2._fld0268[i2];
                    for(int j3 = 0; j3 < j2; j3++)
                    {
                        int l3;
                        int i4;
                        if(j3 == j2 - 1)
                        {
                            l3 = ai1[j3];
                            i4 = ai1[0];
                        } else
                        {
                            l3 = ai1[j3];
                            i4 = ai1[j3 + 1];
                        }
                        if(i == 0)
                            g.drawLine((k2._fld025D[l3] - _fld04B5 << 8) / j, (k2._fld025C[l3] - _fld04B4 << 8) / j, (k2._fld025D[i4] - _fld04B5 << 8) / j, (k2._fld025C[i4] - _fld04B4 << 8) / j);
                        if(i == 1)
                            g.drawLine((k2._fld025B[l3] - _fld04B3 << 8) / j, (k2._fld025C[l3] - _fld04B4 << 8) / j, (k2._fld025B[i4] - _fld04B3 << 8) / j, (k2._fld025C[i4] - _fld04B4 << 8) / j);
                        if(i == 2)
                            g.drawLine((k2._fld025B[l3] - _fld04B3 << 8) / j, (_fld04B5 - k2._fld025D[l3] << 8) / j, (k2._fld025B[i4] - _fld04B3 << 8) / j, (_fld04B5 - k2._fld025D[i4] << 8) / j);
                    }

                }

            }
        }

    }

    private void _mth0264(Graphics g, Color color, int i, int j)
    {
        g.setColor(color);
        g.drawLine(i - 3, j - 3, i + 3, j + 3);
        g.drawLine(i + 3, j - 3, i - 3, j + 3);
    }

    public void _mth0263(Graphics g, Graphics g1, Graphics g2, GameModel k1, int i)
    {
        Color color = Color.white;
        for(int j = 0; j < k1._fld0266; j++)
        {
            int l = k1._fld0267[j];
            int ai[] = k1._fld0268[j];
            for(int j1 = 0; j1 < l; j1++)
            {
                int i1 = ai[j1];
                _mth0264(g, color, (k1._fld025D[i1] - _fld04B5 << 8) / i, (k1._fld025C[i1] - _fld04B4 << 8) / i);
                _mth0264(g1, color, (k1._fld025B[i1] - _fld04B3 << 8) / i, (k1._fld025C[i1] - _fld04B4 << 8) / i);
                _mth0264(g2, color, (k1._fld025B[i1] - _fld04B3 << 8) / i, (_fld04B5 - k1._fld025D[i1] << 8) / i);
            }

        }

    }

    public void _mth0270(int i, int j, int l, int i1, int j1)
    {
        _fld04AE = l;
        _fld04AF = i1;
        _fld04B0 = i;
        _fld04B1 = j;
        _fld04A6 = j1;
    }

    public void _mth025A(boolean flag)
    {
        _fld04E2 = flag;
        if(modelCount == 0)
            return;
        for(int l3 = 0; l3 < modelCount; l3++)
            models[l3]._mth0135(_fld04B3, _fld04B4, _fld04B5, _fld04B6, _fld04B7, _fld04B8, 8, _fld04A1);

        _fld04BE = 0;
        for(int i4 = 0; i4 < modelCount; i4++)
        {
            GameModel k1 = models[i4];
            int j4 = 0;
            for(int k4 = 0; k4 < k1._fld025A; k4++)
                j4 += k1._fld025D[k4];

            if(k1._fld025A <= 0 || j4 / k1._fld025A >= _fld04A2)
            {
                for(int i = 0; i < k1._fld0266; i++)
                {
                    int j5 = k1._fld0267[i];
                    int ai1[] = k1._fld0268[i];
                    if(j5 != 0)
                    {
                        boolean flag2 = false;
                        boolean flag5 = false;
                        for(int i8 = 0; i8 < j5; i8++)
                        {
                            int j1 = k1._fld025D[ai1[i8]];
                            if(j1 > _fld04A1)
                                flag2 = true;
                            if(j1 < _fld04A3)
                                flag5 = true;
                        }

                        if(flag2 && flag5)
                        {
                            boolean flag3 = false;
                            boolean flag6 = false;
                            for(int l8 = 0; l8 < j5; l8++)
                            {
                                int l1 = k1._fld025E[ai1[l8]];
                                if(l1 > -_fld04AE)
                                    flag3 = true;
                                if(l1 < _fld04AE)
                                    flag6 = true;
                            }

                            if(flag3 && flag6)
                            {
                                boolean flag4 = false;
                                boolean flag7 = false;
                                for(int j9 = 0; j9 < j5; j9++)
                                {
                                    int i2 = k1._fld025F[ai1[j9]];
                                    if(i2 > -_fld04AF)
                                        flag4 = true;
                                    if(i2 < _fld04AF)
                                        flag7 = true;
                                }

                                if(flag4 && flag7)
                                {
                                    _fld04BD[_fld04BE] = k1;
                                    _fld04BF[_fld04BE] = i;
                                    _mth0274(_fld04BE);
                                    if(_fld04C4[_fld04BE] < 0)
                                        _fld04C7[_fld04BE] = k1._fld0269[i];
                                    else
                                        _fld04C7[_fld04BE] = k1._fld026A[i];
                                    if(_fld04C7[_fld04BE] != _fld04D3)
                                    {
                                        int j2 = 0;
                                        for(int l9 = 0; l9 < j5; l9++)
                                            j2 += k1._fld025D[ai1[l9]];

                                        int j3;
                                        _fld04C0[_fld04BE] = j3 = j2 / j5 + k1._fld0275;
                                        _fld04BE++;
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        if(_fld04BE == 0)
            return;
        int ai[] = new int[_fld04BE];
        for(int j = 0; j < _fld04BE; j++)
        {
            int l4 = -5000;
            for(int k5 = 0; k5 < _fld04BE; k5++)
                if(_fld04C0[k5] > l4)
                {
                    l4 = _fld04C0[k5];
                    ai[j] = k5;
                }

            _fld04C0[ai[j]] = -5000;
        }

        boolean flag1 = false;
        for(int l5 = 0; l5 < _fld04A5; l5++)
        {
            for(int i6 = 1; i6 <= 4; i6++)
            {
                for(int l = 0; l < _fld04BE - i6; l++)
                    if(!_mth0276(ai[l], ai[l + i6]))
                    {
                        int i5 = i6;
                        int l2 = ai[l + i6];
                        for(int k6 = i6 - 1; k6 >= 0; k6--)
                        {
                            if(!_mth0276(l2, ai[l + k6]))
                                break;
                            ai[l + k6 + 1] = ai[l + k6];
                            ai[l + k6] = l2;
                            i5 = k6;
                        }

                        l2 = ai[l];
                        for(int j7 = 1; j7 <= i5; j7++)
                        {
                            if(!_mth0276(ai[l + j7], l2))
                                break;
                            ai[(l + j7) - 1] = ai[l + j7];
                            ai[l + j7] = l2;
                        }

                    }

            }

        }

        for(int j6 = 0; j6 < _fld04BE; j6++)
            try
            {
                int k3 = ai[j6];
                GameModel k2 = _fld04BD[k3];
                int i1 = _fld04BF[k3];
                int i9 = 0;
                int i10 = 0;
                int j10 = k2._fld0267[i1];
                int ai2[] = k2._fld0268[i1];
                int ai3[] = new int[j10 + 10];
                int ai4[] = new int[j10 + 10];
                int ai5[] = new int[j10 + 10];
                int ai6[] = new int[j10];
                int ai7[] = new int[j10];
                int ai8[] = new int[j10];
                int k10 = (k2._fld0271[i1] * _fld04AD) / 256;
                if(!k2._fld0273[i1])
                {
                    if(k2._fld0270[i1] == _fld04D3)
                        k2._fld0270[i1] = (k2._fld026D[i1] * _fld04AA + k2._fld026E[i1] * _fld04AB + k2._fld026F[i1] * _fld04AC) / k10;
                    if(_fld04C4[k3] < 0)
                        i10 = k2._fld0272[i1] - k2._fld0270[i1];
                    else
                        i10 = k2._fld0272[i1] + k2._fld0270[i1];
                }
                for(int l10 = 0; l10 < j10; l10++)
                {
                    int i3 = ai2[l10];
                    ai6[l10] = k2._fld025B[i3];
                    ai7[l10] = k2._fld025C[i3];
                    ai8[l10] = k2._fld025D[i3];
                    if(k2._fld0273[i1])
                    {
                        if(k2._fld0263[i3] == _fld04D3)
                            k2._fld0263[i3] = (k2._fld0260[i3] * _fld04AA + k2._fld0261[i3] * _fld04AB + k2._fld0262[i3] * _fld04AC) / (k10 * k2._fld0265[i3]);
                        if(_fld04C4[k3] < 0)
                            i10 = k2._fld0272[i1] - k2._fld0263[i3];
                        else
                            i10 = k2._fld0272[i1] + k2._fld0263[i3];
                    }
                    if(k2._fld025D[i3] >= _fld04A1)
                    {
                        ai3[i9] = k2._fld025E[i3];
                        ai4[i9] = k2._fld025F[i3];
                        ai5[i9] = k2._fld025D[i3] / fog + i10;
                        i9++;
                    } else
                    {
                        int k9;
                        if(l10 == 0)
                            k9 = ai2[j10 - 1];
                        else
                            k9 = ai2[l10 - 1];
                        if(k2._fld025D[k9] >= _fld04A1)
                        {
                            int j8 = k2._fld025D[i3] - k2._fld025D[k9];
                            int l6 = k2._fld025B[i3] - ((k2._fld025B[i3] - k2._fld025B[k9]) * (k2._fld025D[i3] - _fld04A1)) / j8;
                            int k7 = k2._fld025C[i3] - ((k2._fld025C[i3] - k2._fld025C[k9]) * (k2._fld025D[i3] - _fld04A1)) / j8;
                            ai3[i9] = (l6 << 8) / _fld04A1;
                            ai4[i9] = (k7 << 8) / _fld04A1;
                            ai5[i9] = _fld04A1 / fog + i10;
                            i9++;
                        }
                        if(l10 == j10 - 1)
                            k9 = ai2[0];
                        else
                            k9 = ai2[l10 + 1];
                        if(k2._fld025D[k9] >= _fld04A1)
                        {
                            int k8 = k2._fld025D[i3] - k2._fld025D[k9];
                            int i7 = k2._fld025B[i3] - ((k2._fld025B[i3] - k2._fld025B[k9]) * (k2._fld025D[i3] - _fld04A1)) / k8;
                            int l7 = k2._fld025C[i3] - ((k2._fld025C[i3] - k2._fld025C[k9]) * (k2._fld025D[i3] - _fld04A1)) / k8;
                            ai3[i9] = (i7 << 8) / _fld04A1;
                            ai4[i9] = (l7 << 8) / _fld04A1;
                            ai5[i9] = _fld04A1 / fog + i10;
                            i9++;
                        }
                    }
                }

                _mth025B(i9, j10, ai6, ai7, ai8, ai3, ai4, ai5, _fld04C7[k3]);
            }
            catch(Exception exception)
            {
                System.out.println(String.valueOf(exception));
            }

    }

    public void _mth025B(int i, int j, int ai[], int ai1[], int ai2[], int ai3[], int ai4[], 
            int ai5[], int l)
    {
        for(int i1 = 0; i1 < i; i1++)
            if(ai5[i1] < 0)
                ai5[i1] = 0;
            else
            if(ai5[i1] > 319)
                ai5[i1] = 319;

        if(l >= _fld04D9)
            l = 0;
        int j1;
        int k1 = j1 = ai4[0] += _fld04B1;
        for(int l1 = 1; l1 < i; l1++)
        {
            int i2;
            if((i2 = ai4[l1] += _fld04B1) < j1)
                j1 = i2;
            else
            if(i2 > k1)
                k1 = i2;
        }

        if(j1 < _fld04B1 - _fld04AF)
            j1 = _fld04B1 - _fld04AF;
        if(k1 >= _fld04B1 + _fld04AF)
            k1 = (_fld04B1 + _fld04AF) - 1;
        if(j1 >= k1)
            return;
        for(int j2 = j1; j2 < k1; j2++)
        {
            _fld04DE[j2] = 0x10000;
            _fld04DF[j2] = 0xffff0000;
        }

        int k2 = i - 1;
        int l2 = ai4[0];
        int j3 = ai4[k2];
        if(l2 < j3)
        {
            int l3 = ai3[0] << 8;
            int k4 = (ai3[k2] - ai3[0] << 8) / (j3 - l2);
            int i6 = ai5[0] << 8;
            int l7 = (ai5[k2] - ai5[0] << 8) / (j3 - l2);
            if(l2 < 0)
            {
                l3 -= k4 * l2;
                i6 -= l7 * l2;
                l2 = 0;
            }
            if(j3 > 383)
                j3 = 383;
            for(int k9 = l2; k9 <= j3; k9++)
            {
                _fld04DE[k9] = _fld04DF[k9] = l3;
                _fld04E0[k9] = _fld04E1[k9] = i6;
                l3 += k4;
                i6 += l7;
            }

        } else
        if(l2 > j3)
        {
            int i4 = ai3[k2] << 8;
            int l4 = (ai3[0] - ai3[k2] << 8) / (l2 - j3);
            int j6 = ai5[k2] << 8;
            int i8 = (ai5[0] - ai5[k2] << 8) / (l2 - j3);
            if(j3 < 0)
            {
                i4 -= l4 * j3;
                j6 -= i8 * j3;
                j3 = 0;
            }
            if(l2 > 383)
                l2 = 383;
            for(int l9 = j3; l9 <= l2; l9++)
            {
                _fld04DE[l9] = _fld04DF[l9] = i4;
                _fld04E0[l9] = _fld04E1[l9] = j6;
                i4 += l4;
                j6 += i8;
            }

        }
        for(int j4 = 0; j4 < k2; j4++)
        {
            int i5 = j4 + 1;
            int i3 = ai4[j4];
            int k3 = ai4[i5];
            if(i3 < k3)
            {
                int k6 = ai3[j4] << 8;
                int j8 = (ai3[i5] - ai3[j4] << 8) / (k3 - i3);
                int i10 = ai5[j4] << 8;
                int j11 = (ai5[i5] - ai5[j4] << 8) / (k3 - i3);
                if(i3 < 0)
                {
                    k6 -= j8 * i3;
                    i10 -= j11 * i3;
                    i3 = 0;
                }
                if(k3 > 383)
                    k3 = 383;
                for(int k12 = i3; k12 <= k3; k12++)
                {
                    if(k6 < _fld04DE[k12])
                    {
                        _fld04DE[k12] = k6;
                        _fld04E0[k12] = i10;
                    }
                    if(k6 > _fld04DF[k12])
                    {
                        _fld04DF[k12] = k6;
                        _fld04E1[k12] = i10;
                    }
                    k6 += j8;
                    i10 += j11;
                }

            } else
            if(i3 > k3)
            {
                int l6 = ai3[i5] << 8;
                int k8 = (ai3[j4] - ai3[i5] << 8) / (i3 - k3);
                int j10 = ai5[i5] << 8;
                int k11 = (ai5[j4] - ai5[i5] << 8) / (i3 - k3);
                if(k3 < 0)
                {
                    l6 -= k8 * k3;
                    j10 -= k11 * k3;
                    k3 = 0;
                }
                if(i3 > 383)
                    i3 = 383;
                for(int l12 = k3; l12 <= i3; l12++)
                {
                    if(l6 < _fld04DE[l12])
                    {
                        _fld04DE[l12] = l6;
                        _fld04E0[l12] = j10;
                    }
                    if(l6 > _fld04DF[l12])
                    {
                        _fld04DF[l12] = l6;
                        _fld04E1[l12] = j10;
                    }
                    l6 += k8;
                    j10 += k11;
                }

            }
        }

        if(l >= 0 && _fld04DC[l] == 1)
        {
            int j5 = ai[0];
            int i7 = ai1[0];
            int l8 = ai2[0];
            int k10 = j5 - ai[1];
            int l11 = i7 - ai1[1];
            int i13 = l8 - ai2[1];
            j--;
            int l13 = ai[j] - j5;
            int k14 = ai1[j] - i7;
            int i15 = ai2[j] - l8;
            int k15 = l13 * i7 - k14 * j5 << 12;
            int i16 = k14 * l8 - i15 * i7 << 8;
            int k16 = i15 * j5 - l13 * l8 << 4;
            int i17 = k10 * i7 - l11 * j5 << 12;
            int k17 = l11 * l8 - i13 * i7 << 8;
            int i18 = i13 * j5 - k10 * l8 << 4;
            int k18 = l11 * l13 - k10 * k14 << 5;
            int i19 = i13 * k14 - l11 * i15 << 1;
            int k19 = k10 * i15 - i13 * l13 >> 3;
            int i20 = i16 >> 4;
            int k20 = k17 >> 4;
            int i21 = i19 >> 4;
            int k21 = j1 - _fld04B1;
            int i22 = _fld04B0 + j1 * _fld04A6;
            k15 += k16 * k21;
            i17 += i18 * k21;
            k18 += k19 * k21;
            if(!_fld04DB[l])
            {
                for(int k22 = j1; k22 < k1; k22++)
                {
                    int k23 = _fld04DE[k22] >> 8;
                    int k24 = _fld04DF[k22] >> 8;
                    if(k24 - k23 <= 0)
                    {
                        k15 += k16;
                        i17 += i18;
                        k18 += k19;
                        i22 += _fld04A6;
                    } else
                    {
                        int k25 = _fld04E0[k22] << 9;
                        int k26 = ((_fld04E1[k22] << 9) - k25) / (k24 - k23);
                        if(k23 < -_fld04AE)
                        {
                            k25 += (-_fld04AE - k23) * k26;
                            k23 = -_fld04AE;
                        }
                        if(k24 > _fld04AE)
                            k24 = _fld04AE;
                        if(!_fld04E2 || (k22 & 1) == 0)
                            _mth026B(_fld04DD, _fld04DA[l], 0, 0, k15 + i20 * k23, i17 + k20 * k23, k18 + i21 * k23, i16, k17, i19, k24 - k23, i22 + k23, k25, k26);
                        k15 += k16;
                        i17 += i18;
                        k18 += k19;
                        i22 += _fld04A6;
                    }
                }

                return;
            }
            for(int l22 = j1; l22 < k1; l22++)
            {
                int l23 = _fld04DE[l22] >> 8;
                int l24 = _fld04DF[l22] >> 8;
                if(l24 - l23 <= 0)
                {
                    k15 += k16;
                    i17 += i18;
                    k18 += k19;
                    i22 += _fld04A6;
                } else
                {
                    int l25 = _fld04E0[l22] << 9;
                    int l26 = ((_fld04E1[l22] << 9) - l25) / (l24 - l23);
                    if(l23 < -_fld04AE)
                    {
                        l25 += (-_fld04AE - l23) * l26;
                        l23 = -_fld04AE;
                    }
                    if(l24 > _fld04AE)
                        l24 = _fld04AE;
                    if(!_fld04E2 || (l22 & 1) == 0)
                        _mth0267(_fld04DD, 0, 0, 0, _fld04DA[l], k15 + i20 * l23, i17 + k20 * l23, k18 + i21 * l23, i16, k17, i19, l24 - l23, i22 + l23, l25, l26);
                    k15 += k16;
                    i17 += i18;
                    k18 += k19;
                    i22 += _fld04A6;
                }
            }

            return;
        }
        if(l >= 0 && _fld04DC[l] == 0)
        {
            int k5 = ai[0];
            int j7 = ai1[0];
            int i9 = ai2[0];
            int l10 = k5 - ai[1];
            int i12 = j7 - ai1[1];
            int j13 = i9 - ai2[1];
            j--;
            int i14 = ai[j] - k5;
            int l14 = ai1[j] - j7;
            int j15 = ai2[j] - i9;
            int l15 = i14 * j7 - l14 * k5 << 11;
            int j16 = l14 * i9 - j15 * j7 << 7;
            int l16 = j15 * k5 - i14 * i9 << 3;
            int j17 = l10 * j7 - i12 * k5 << 11;
            int l17 = i12 * i9 - j13 * j7 << 7;
            int j18 = j13 * k5 - l10 * i9 << 3;
            int l18 = i12 * i14 - l10 * l14 << 5;
            int j19 = j13 * l14 - i12 * j15 << 1;
            int l19 = l10 * j15 - j13 * i14 >> 3;
            int j20 = j16 >> 4;
            int l20 = l17 >> 4;
            int j21 = j19 >> 4;
            int l21 = j1 - _fld04B1;
            int j22 = _fld04B0 + j1 * _fld04A6;
            l15 += l16 * l21;
            j17 += j18 * l21;
            l18 += l19 * l21;
            if(!_fld04DB[l])
            {
                for(int i23 = j1; i23 < k1; i23++)
                {
                    int i24 = _fld04DE[i23] >> 8;
                    int i25 = _fld04DF[i23] >> 8;
                    if(i25 - i24 <= 0)
                    {
                        l15 += l16;
                        j17 += j18;
                        l18 += l19;
                        j22 += _fld04A6;
                    } else
                    {
                        int i26 = _fld04E0[i23] << 6;
                        int i27 = ((_fld04E1[i23] << 6) - i26) / (i25 - i24);
                        if(i24 < -_fld04AE)
                        {
                            i26 += (-_fld04AE - i24) * i27;
                            i24 = -_fld04AE;
                        }
                        if(i25 > _fld04AE)
                            i25 = _fld04AE;
                        if(!_fld04E2 || (i23 & 1) == 0)
                            _mth0265(_fld04DD, _fld04DA[l], 0, 0, l15 + j20 * i24, j17 + l20 * i24, l18 + j21 * i24, j16, l17, j19, i25 - i24, j22 + i24, i26, i27);
                        l15 += l16;
                        j17 += j18;
                        l18 += l19;
                        j22 += _fld04A6;
                    }
                }

                return;
            }
            for(int j23 = j1; j23 < k1; j23++)
            {
                int j24 = _fld04DE[j23] >> 8;
                int j25 = _fld04DF[j23] >> 8;
                if(j25 - j24 <= 0)
                {
                    l15 += l16;
                    j17 += j18;
                    l18 += l19;
                    j22 += _fld04A6;
                } else
                {
                    int j26 = _fld04E0[j23] << 6;
                    int j27 = ((_fld04E1[j23] << 6) - j26) / (j25 - j24);
                    if(j24 < -_fld04AE)
                    {
                        j26 += (-_fld04AE - j24) * j27;
                        j24 = -_fld04AE;
                    }
                    if(j25 > _fld04AE)
                        j25 = _fld04AE;
                    if(!_fld04E2 || (j23 & 1) == 0)
                        _mth025D(_fld04DD, 0, 0, 0, _fld04DA[l], l15 + j20 * j24, j17 + l20 * j24, l18 + j21 * j24, j16, l17, j19, j25 - j24, j22 + j24, j26, j27);
                    l15 += l16;
                    j17 += j18;
                    l18 += l19;
                    j22 += _fld04A6;
                }
            }

            return;
        }
        l = -1 - l;
        int l5 = ((l & 0x7c00) << 17) + ((l & 0x3e0) << 10) + ((l & 0x1f) << 3);
        int k7 = _fld04B0 + j1 * _fld04A6;
        for(int j9 = j1; j9 < k1; j9++)
        {
            int i11 = _fld04DE[j9] >> 8;
            int j12 = _fld04DF[j9] >> 8;
            if(j12 - i11 <= 0)
            {
                k7 += _fld04A6;
            } else
            {
                int k13 = _fld04E0[j9];
                int j14 = (_fld04E1[j9] - k13) / (j12 - i11);
                if(i11 < -_fld04AE)
                {
                    k13 += (-_fld04AE - i11) * j14;
                    i11 = -_fld04AE;
                }
                if(j12 > _fld04AE)
                    j12 = _fld04AE;
                if(!_fld04E2 || (j9 & 1) == 0)
                    _mth0269(_fld04DD, i11 - j12, k7 + i11, l5, k13, j14);
                k7 += _fld04A6;
            }
        }

    }

    public static void _mth026B(int ai[], int ai1[], int i, int j, int l, int i1, int j1, int k1, 
            int l1, int i2, int j2, int k2, int l2, int i3)
    {
        if(j2 <= 0)
            return;
        int j3 = 0;
        int k3 = 0;
        i3 <<= 2;
        if(j1 != 0)
        {
            j3 = l / j1 << 7;
            k3 = i1 / j1 << 7;
        }
        if(j3 < 0)
            j3 = 0;
        else
        if(j3 > 16256)
            j3 = 16256;
        for(int j4 = j2; j4 > 0; j4 -= 16)
        {
            l += k1;
            i1 += l1;
            j1 += i2;
            i = j3;
            j = k3;
            if(j1 != 0)
            {
                j3 = l / j1 << 7;
                k3 = i1 / j1 << 7;
            }
            if(j3 < 0)
                j3 = 0;
            else
            if(j3 > 16256)
                j3 = 16256;
            int l3 = j3 - i >> 4;
            int i4 = k3 - j >> 4;
            int k4 = l2 >> 23;
            i += l2 & 0x600000;
            l2 += i3;
            if(j4 < 16)
            {
                for(int l4 = 0; l4 < j4; l4++)
                {
                    ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                    i += l3;
                    j += i4;
                    if((l4 & 3) == 3)
                    {
                        i = (i & 0x3fff) + (l2 & 0x600000);
                        k4 = l2 >> 23;
                        l2 += i3;
                    }
                }

            } else
            {
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                i = (i & 0x3fff) + (l2 & 0x600000);
                k4 = l2 >> 23;
                l2 += i3;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                i = (i & 0x3fff) + (l2 & 0x600000);
                k4 = l2 >> 23;
                l2 += i3;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                i = (i & 0x3fff) + (l2 & 0x600000);
                k4 = l2 >> 23;
                l2 += i3;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0x3f80) + (i >> 7)] >>> k4;
            }
        }

    }

    public static void _mth0265(int ai[], int ai1[], int i, int j, int l, int i1, int j1, int k1, 
            int l1, int i2, int j2, int k2, int l2, int i3)
    {
        if(j2 <= 0)
            return;
        int j3 = 0;
        int k3 = 0;
        i3 <<= 2;
        if(j1 != 0)
        {
            j3 = l / j1 << 6;
            k3 = i1 / j1 << 6;
        }
        if(j3 < 0)
            j3 = 0;
        else
        if(j3 > 4032)
            j3 = 4032;
        for(int j4 = j2; j4 > 0; j4 -= 16)
        {
            l += k1;
            i1 += l1;
            j1 += i2;
            i = j3;
            j = k3;
            if(j1 != 0)
            {
                j3 = l / j1 << 6;
                k3 = i1 / j1 << 6;
            }
            if(j3 < 0)
                j3 = 0;
            else
            if(j3 > 4032)
                j3 = 4032;
            int l3 = j3 - i >> 4;
            int i4 = k3 - j >> 4;
            int k4 = l2 >> 20;
            i += l2 & 0xc0000;
            l2 += i3;
            if(j4 < 16)
            {
                for(int l4 = 0; l4 < j4; l4++)
                {
                    ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                    i += l3;
                    j += i4;
                    if((l4 & 3) == 3)
                    {
                        i = (i & 0xfff) + (l2 & 0xc0000);
                        k4 = l2 >> 20;
                        l2 += i3;
                    }
                }

            } else
            {
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                i = (i & 0xfff) + (l2 & 0xc0000);
                k4 = l2 >> 20;
                l2 += i3;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                i = (i & 0xfff) + (l2 & 0xc0000);
                k4 = l2 >> 20;
                l2 += i3;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                i = (i & 0xfff) + (l2 & 0xc0000);
                k4 = l2 >> 20;
                l2 += i3;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
                i += l3;
                j += i4;
                ai[k2++] = ai1[(j & 0xfc0) + (i >> 6)] >>> k4;
            }
        }

    }

    public static void _mth025D(int ai[], int i, int j, int l, int ai1[], int i1, int j1, int k1, 
            int l1, int i2, int j2, int k2, int l2, int i3, int j3)
    {
        if(k2 <= 0)
            return;
        int k3 = 0;
        int l3 = 0;
        j3 <<= 2;
        if(k1 != 0)
        {
            k3 = i1 / k1 << 6;
            l3 = j1 / k1 << 6;
        }
        if(k3 < 0)
            k3 = 0;
        else
        if(k3 > 4032)
            k3 = 4032;
        for(int k4 = k2; k4 > 0; k4 -= 16)
        {
            i1 += l1;
            j1 += i2;
            k1 += j2;
            j = k3;
            l = l3;
            if(k1 != 0)
            {
                k3 = i1 / k1 << 6;
                l3 = j1 / k1 << 6;
            }
            if(k3 < 0)
                k3 = 0;
            else
            if(k3 > 4032)
                k3 = 4032;
            int i4 = k3 - j >> 4;
            int j4 = l3 - l >> 4;
            int l4 = i3 >> 20;
            j += i3 & 0xc0000;
            i3 += j3;
            if(k4 < 16)
            {
                for(int i5 = 0; i5 < k4; i5++)
                {
                    if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                        ai[l2] = i;
                    l2++;
                    j += i4;
                    l += j4;
                    if((i5 & 3) == 3)
                    {
                        j = (j & 0xfff) + (i3 & 0xc0000);
                        l4 = i3 >> 20;
                        i3 += j3;
                    }
                }

            } else
            {
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                j = (j & 0xfff) + (i3 & 0xc0000);
                l4 = i3 >> 20;
                i3 += j3;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                j = (j & 0xfff) + (i3 & 0xc0000);
                l4 = i3 >> 20;
                i3 += j3;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                j = (j & 0xfff) + (i3 & 0xc0000);
                l4 = i3 >> 20;
                i3 += j3;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0xfc0) + (j >> 6)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
            }
        }

    }

    public static void _mth0267(int ai[], int i, int j, int l, int ai1[], int i1, int j1, int k1, 
            int l1, int i2, int j2, int k2, int l2, int i3, int j3)
    {
        if(k2 <= 0)
            return;
        int k3 = 0;
        int l3 = 0;
        j3 <<= 2;
        if(k1 != 0)
        {
            k3 = i1 / k1 << 7;
            l3 = j1 / k1 << 7;
        }
        if(k3 < 0)
            k3 = 0;
        else
        if(k3 > 16256)
            k3 = 16256;
        for(int k4 = k2; k4 > 0; k4 -= 16)
        {
            i1 += l1;
            j1 += i2;
            k1 += j2;
            j = k3;
            l = l3;
            if(k1 != 0)
            {
                k3 = i1 / k1 << 7;
                l3 = j1 / k1 << 7;
            }
            if(k3 < 0)
                k3 = 0;
            else
            if(k3 > 16256)
                k3 = 16256;
            int i4 = k3 - j >> 4;
            int j4 = l3 - l >> 4;
            int l4 = i3 >> 23;
            j += i3 & 0x600000;
            i3 += j3;
            if(k4 < 16)
            {
                for(int i5 = 0; i5 < k4; i5++)
                {
                    if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                        ai[l2] = i;
                    l2++;
                    j += i4;
                    l += j4;
                    if((i5 & 3) == 3)
                    {
                        j = (j & 0x3fff) + (i3 & 0x600000);
                        l4 = i3 >> 23;
                        i3 += j3;
                    }
                }

            } else
            {
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                j = (j & 0x3fff) + (i3 & 0x600000);
                l4 = i3 >> 23;
                i3 += j3;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                j = (j & 0x3fff) + (i3 & 0x600000);
                l4 = i3 >> 23;
                i3 += j3;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                j = (j & 0x3fff) + (i3 & 0x600000);
                l4 = i3 >> 23;
                i3 += j3;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
                j += i4;
                l += j4;
                if((i = ai1[(l & 0x3f80) + (j >> 7)] >>> l4) != 0)
                    ai[l2] = i;
                l2++;
            }
        }

    }

    public static void _mth0269(int ai[], int i, int j, int l, int i1, int j1)
    {
        if(i >= 0)
            return;
        int ai1[] = {
            l, l - (l >>> 3) & 0xff0ff0ff, l - (l >>> 2) & 0xff0ff0ff, l - (l >>> 2) - (l >>> 3) & 0xff0ff0ff
        };
        j1 <<= 2;
        l = ai1[i1 >> 12 & 3] >>> (i1 >> 14);
        i1 += j1;
        int k1 = i / 16;
        for(int l1 = k1; l1 < 0; l1++)
        {
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            l = ai1[i1 >> 12 & 3] >>> (i1 >> 14);
            i1 += j1;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            l = ai1[i1 >> 12 & 3] >>> (i1 >> 14);
            i1 += j1;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            l = ai1[i1 >> 12 & 3] >>> (i1 >> 14);
            i1 += j1;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            ai[j++] = l;
            l = ai1[i1 >> 12 & 3] >>> (i1 >> 14);
            i1 += j1;
        }

        k1 = -(i % 16);
        for(int i2 = 0; i2 < k1; i2++)
        {
            ai[j++] = l;
            if((i2 & 3) == 3)
            {
                l = ai1[i1 >> 12 & 3] >>> (i1 >> 14);
                i1 += j1;
            }
        }

    }

    public void _mth0275(int i, int j, int l, int i1, int j1, int k1)
    {
        _fld04B3 = i;
        _fld04B4 = j;
        _fld04B5 = l;
        _fld04B6 = i1;
        _fld04B7 = j1;
        _fld04B8 = k1;
    }

    public void _mth026F(int i, int j, int l)
    {
        _fld04B7 = (int)(Math.atan2(i - _fld04B3, l - _fld04B5) * 40.740000000000002D) & 0xff;
        _fld04B6 = (int)(Math.atan2(j - _fld04B4, l - _fld04B5) * 40.740000000000002D) & 0xff;
        _fld04B7 = 256 - _fld04B7 & 0xff;
        _fld04B8 = 0;
    }

    public int _mth0254(int i, int j, int l, int i1)
    {
        return (int)(Math.atan2(l - i, i1 - j) * 40.740000000000002D);
    }

    public void _mth025C(int i, int j, int l, int i1, int j1, int k1, int l1)
    {
        _fld04B6 = (256 - i1) % 256;
        _fld04B7 = (256 - j1) % 256;
        _fld04B8 = (256 - k1) % 256;
        int i2 = 0;
        int j2 = 0;
        int k2 = l1;
        if(i1 != 0)
        {
            int l2 = _fld04A7[i1];
            int k3 = _fld04A7[i1 + 256];
            int j4 = j2 * k3 - k2 * l2 >> 15;
            k2 = j2 * l2 + k2 * k3 >> 15;
            j2 = j4;
        }
        if(j1 != 0)
        {
            int i3 = _fld04A7[j1];
            int l3 = _fld04A7[j1 + 256];
            int k4 = k2 * i3 + i2 * l3 >> 15;
            k2 = k2 * l3 - i2 * i3 >> 15;
            i2 = k4;
        }
        if(k1 != 0)
        {
            int j3 = _fld04A7[k1];
            int i4 = _fld04A7[k1 + 256];
            int l4 = j2 * j3 + i2 * i4 >> 15;
            j2 = j2 * i4 - i2 * j3 >> 15;
            i2 = l4;
        }
        _fld04B3 = i - i2;
        _fld04B4 = j - j2;
        _fld04B5 = l - k2;
    }

    public void _mth026C(int ai[], int i, int j, int l)
    {
        int k2 = ai[0];
        int l2 = ai[1];
        int i3 = ai[2];
        if(l != 0)
        {
            int i1 = _fld04A7[l];
            int l1 = _fld04A7[l + 256];
            ai[0] = l2 * i1 + k2 * l1 >> 15;
            ai[1] = l2 * l1 - k2 * i1 >> 15;
        }
        if(i != 0)
        {
            int j1 = _fld04A7[i];
            int i2 = _fld04A7[i + 256];
            ai[1] = l2 * i2 - i3 * j1 >> 15;
            ai[2] = l2 * j1 + i3 * i2 >> 15;
        }
        if(j != 0)
        {
            int k1 = _fld04A7[j];
            int j2 = _fld04A7[j + 256];
            ai[0] = i3 * k1 + k2 * j2 >> 15;
            ai[2] = i3 * j2 - k2 * k1 >> 15;
        }
    }

    private void _mth0274(int i)
    {
        GameModel k1 = _fld04BD[i];
        int j = _fld04BF[i];
        int ai[] = k1._fld0268[j];
        int l = k1._fld0267[j];
        int i1 = k1._fld026B[j];
        int l1 = k1._fld025B[ai[0]];
        int i2 = k1._fld025C[ai[0]];
        int j2 = k1._fld025D[ai[0]];
        int k2 = k1._fld025B[ai[1]] - l1;
        int l2 = k1._fld025C[ai[1]] - i2;
        int i3 = k1._fld025D[ai[1]] - j2;
        int j3 = k1._fld025B[ai[2]] - l1;
        int k3 = k1._fld025C[ai[2]] - i2;
        int l3 = k1._fld025D[ai[2]] - j2;
        int i4 = l2 * l3 - k3 * i3;
        int j4 = i3 * j3 - l3 * k2;
        int k4 = k2 * k3 - j3 * l2;
        if(i1 == -1)
        {
            i1 = 0;
            for(; i4 > 25000 || j4 > 25000 || k4 > 25000 || i4 < -25000 || j4 < -25000 || k4 < -25000; k4 >>= 1)
            {
                i1++;
                i4 >>= 1;
                j4 >>= 1;
            }

            k1._fld026B[j] = i1;
            k1._fld026C[j] = (int)((double)_fld04B2 * Math.sqrt(i4 * i4 + j4 * j4 + k4 * k4));
        } else
        {
            i4 >>= i1;
            j4 >>= i1;
            k4 >>= i1;
        }
        _fld04C4[i] = l1 * i4 + i2 * j4 + j2 * k4;
        _fld04C1[i] = i4;
        _fld04C2[i] = j4;
        _fld04C3[i] = k4;
        int l4 = k1._fld025D[ai[0]];
        int i5 = l4;
        int j5 = k1._fld025E[ai[0]];
        int k5 = j5;
        int l5 = k1._fld025F[ai[0]];
        int i6 = l5;
        for(int j6 = 1; j6 < l; j6++)
        {
            int j1 = k1._fld025D[ai[j6]];
            if(j1 > i5)
                i5 = j1;
            else
            if(j1 < l4)
                l4 = j1;
            j1 = k1._fld025E[ai[j6]];
            if(j1 > k5)
                k5 = j1;
            else
            if(j1 < j5)
                j5 = j1;
            j1 = k1._fld025F[ai[j6]];
            if(j1 > i6)
                i6 = j1;
            else
            if(j1 < l5)
                l5 = j1;
        }

        _fld04D1[i] = l4;
        _fld04D2[i] = i5;
        _fld04C8[i] = j5;
        _fld04CC[i] = k5;
        _fld04CB[i] = l5;
        _fld04D0[i] = i6;
    }

    private boolean _mth0276(int i, int j)
    {
        if(_fld04D1[i] > _fld04D2[j])
            return true;
        if(_fld04D1[j] > _fld04D2[i])
            return false;
        if(_fld04C8[i] > _fld04CC[j])
            return true;
        if(_fld04C8[j] > _fld04CC[i])
            return true;
        if(_fld04CB[i] > _fld04D0[j])
            return true;
        if(_fld04CB[j] > _fld04D0[i])
            return true;
        GameModel k1 = _fld04BD[i];
        GameModel k2 = _fld04BD[j];
        int l = _fld04BF[i];
        int i1 = _fld04BF[j];
        int ai[] = k1._fld0268[l];
        int ai1[] = k2._fld0268[i1];
        int j1 = k1._fld0267[l];
        int l1 = k2._fld0267[i1];
        int j3 = k2._fld025B[ai1[0]];
        int k3 = k2._fld025C[ai1[0]];
        int l3 = k2._fld025D[ai1[0]];
        int i4 = _fld04C1[j];
        int j4 = _fld04C2[j];
        int k4 = _fld04C3[j];
        int l4 = k2._fld026C[i1];
        int i5 = _fld04C4[j];
        boolean flag = false;
        for(int j5 = 0; j5 < j1; j5++)
        {
            int i2 = ai[j5];
            int l2 = (j3 - k1._fld025B[i2]) * i4 + (k3 - k1._fld025C[i2]) * j4 + (l3 - k1._fld025D[i2]) * k4;
            if((l2 >= -l4 || i5 >= 0) && (l2 <= l4 || i5 <= 0))
                continue;
            flag = true;
            break;
        }

        if(!flag)
            return true;
        j3 = k1._fld025B[ai[0]];
        k3 = k1._fld025C[ai[0]];
        l3 = k1._fld025D[ai[0]];
        i4 = _fld04C1[i];
        j4 = _fld04C2[i];
        k4 = _fld04C3[i];
        l4 = k1._fld026C[l];
        i5 = _fld04C4[i];
        flag = false;
        for(int k5 = 0; k5 < l1; k5++)
        {
            int j2 = ai1[k5];
            int i3 = (j3 - k2._fld025B[j2]) * i4 + (k3 - k2._fld025C[j2]) * j4 + (l3 - k2._fld025D[j2]) * k4;
            if((i3 >= -l4 || i5 <= 0) && (i3 <= l4 || i5 >= 0))
                continue;
            flag = true;
            break;
        }

        return !flag;
    }

    public int loadTextures(String s, int ai[], int i, int j)
    {
        Image image = GameDialog.loadImage(s);
        return _mth0273(image, ai, i, j);
    }

    public int _mth0273(Image image, int ai[], int i, int j)
    {
        if(j > 0)
        {
            i = (i + j) / 2;
            GameDialog.setLoadingProgress(i);
        }
        image.getWidth(GameDialog._fld0183);
        int l = image.getHeight(GameDialog._fld0183);
        int i1 = l / 128;
        _fld04D9 = i1;
        _fld04DA = new int[i1][];
        _fld04DB = new boolean[i1];
        if(ai == null)
        {
            _fld04DC = new int[i1];
            int ai1[] = new int[4096];
            for(int k1 = 0; k1 < i1; k1++)
            {
                PixelGrabber pixelgrabber = new PixelGrabber(image, 64, k1 * 128, 64, 64, ai1, 0, 64);
                try
                {
                    pixelgrabber.grabPixels();
                }
                catch(InterruptedException _ex)
                {
                    System.out.println("Error!");
                }
                _fld04DC[k1] = 0;
                int i2 = ai1[0];
                for(int j2 = 0; j2 < 4096; j2++)
                {
                    if(ai1[j2] == i2)
                        continue;
                    _fld04DC[k1] = 1;
                    break;
                }

            }

        } else
        {
            _fld04DC = ai;
        }
        for(int j1 = 0; j1 < i1; j1++)
        {
            char c = '@';
            if(_fld04DC[j1] == 1)
                c = '\200';
            int l1 = c * c;
            _fld04DA[j1] = new int[l1 * 4];
            PixelGrabber pixelgrabber1 = new PixelGrabber(image, 0, j1 * 128, c, c, _fld04DA[j1], 0, c);
            try
            {
                pixelgrabber1.grabPixels();
            }
            catch(InterruptedException _ex)
            {
                System.out.println("Error!");
            }
            _mth0259(j1);
            if(j > 0)
                GameDialog.setLoadingProgress(i + ((j - i) * (j1 + 1)) / i1);
        }

        image.flush();
        image = null;
        return i1;
    }

    public void _mth0259(int i)
    {
        char c;
        if(_fld04DC[i] == 0)
            c = '\u1000';
        else
            c = '\u4000';
        for(int j = 0; j < c; j++)
        {
            int l = _fld04DA[i][j];
            l = ((l & 0xff0000) << 8) + ((l & 0xff00) << 4) + (l & 0xff);
            _fld04DA[i][j] = l;
        }

        if(_fld04DA[i][0] == 0)
            _fld04DB[i] = true;
        else
            _fld04DB[i] = false;
        for(int i1 = 1; i1 < 4; i1++)
        {
            for(int j1 = 0; j1 < c; j1++)
            {
                int k1 = _fld04DA[i][j1];
                if(i1 == 1)
                    _fld04DA[i][i1 * c + j1] = k1 - (k1 >>> 3) & 0xff0ff0ff;
                if(i1 == 2)
                    _fld04DA[i][i1 * c + j1] = k1 - (k1 >>> 2) & 0xff0ff0ff;
                if(i1 == 3)
                    _fld04DA[i][i1 * c + j1] = k1 - (k1 >>> 2) - (k1 >>> 3) & 0xff0ff0ff;
            }

        }

    }

    public GameModel[] _mth026D(int i, int j, int l, int i1, int j1, int k1, int l1)
    {
        int ai[] = new int[modelCount];
        int i2 = 0;
        for(int j2 = 0; j2 < modelCount; j2++)
            if(models[j2]._mth013D(i, j, l, i1, j1, k1, l1))
                ai[i2++] = j2;

        if(i2 == 0)
            return null;
        GameModel ak[] = new GameModel[i2];
        for(int k2 = 0; k2 < i2; k2++)
            ak[k2] = models[ai[k2]];

        return ak;
    }

    public GameModel[] _mth026E(int i, int j, int l, int i1, int j1, int k1, int l1,
            int i2)
    {
        int ai[] = new int[modelCount];
        int j2 = 0;
        for(int k2 = i; k2 < modelCount; k2++)
            if(models[k2]._mth013D(j, l, i1, j1, k1, l1, i2))
                ai[j2++] = k2;

        if(j2 == 0)
            return null;
        GameModel ak[] = new GameModel[j2];
        for(int l2 = 0; l2 < j2; l2++)
            ak[l2] = models[ai[l2]];

        return ak;
    }

    public GameModel _mth0271(GameModel k1, int i, int j, int l, int i1, int j1, int l1,
            int i2, int j2)
    {
        for(int k2 = i; k2 < modelCount; k2++)
            if(models[k2] != k1 && models[k2]._mth013D(j, l, i1, j1, l1, i2, j2))
                return models[k2];

        return null;
    }

    public GameModel _mth0260(int i, int j, int l, int i1, int j1, int k1, int l1)
    {
        _fld04D4 = (_fld04D4 + 1) % modelCount;
        for(int i2 = 0; i2 < modelCount; i2++)
        {
            int j2 = (i2 + _fld04D4) % modelCount;
            if(models[j2]._mth013D(i, j, l, i1, j1, k1, l1) && models[j2]._fld0274 == 1)
                return models[j2];
        }

        return null;
    }

    public GameModel _mth0257(int i, int j, int l, int i1, int j1, int k1, int l1)
    {
        _fld04D4 = (_fld04D4 + 1) % modelCount;
        for(int i2 = 0; i2 < modelCount; i2++)
        {
            int j2 = (i2 + _fld04D4) % modelCount;
            if(models[j2]._mth013D(i, j, l, i1, j1, k1, l1) && models[j2]._fld0274 == 0)
                return models[j2];
        }

        return null;
    }

    public GameModel[] _mth0261(int i, int j, int l, int i1)
    {
        int ai[] = new int[modelCount];
        int j1 = 0;
        for(int k1 = 0; k1 < modelCount; k1++)
            if(models[k1]._mth011C(i, j, l, i1))
                ai[j1++] = k1;

        if(j1 == 0)
            return null;
        GameModel ak[] = new GameModel[j1];
        for(int l1 = 0; l1 < j1; l1++)
            ak[l1] = models[ai[l1]];

        return ak;
    }

    public GameModel _mth0272(int i, int j, int l, int i1)
    {
        _fld04D4 = (_fld04D4 + 1) % modelCount;
        for(int j1 = 0; j1 < modelCount; j1++)
        {
            int k1 = (j1 + _fld04D4) % modelCount;
            if(models[k1]._mth011C(i, j, l, i1) && models[k1]._fld0274 == 0)
                return models[k1];
        }

        return null;
    }

    public void _mth0255(String s)
    {
        GameModel k1 = new GameModel(models, modelCount);
        k1._mth0119(s);
    }

    public void _mth025F(int i, int j, int l)
    {
        if(i == 0 && j == 0 && l == 0)
            i = 32;
        _fld04AA = i;
        _fld04AB = j;
        _fld04AC = l;
        _fld04AD = (int)Math.sqrt(i * i + j * j + l * l);
        for(int i1 = 0; i1 < modelCount; i1++)
            models[i1]._mth0133();

    }

    public int _fld04A1;
    public int _fld04A2;
    public int _fld04A3;
    public int fog;
    public int _fld04A5;
    public int _fld04A6;
    public int _fld04A7[];
    public double _fld04A8;
    public int _fld04A9;
    private int _fld04AA;
    private int _fld04AB;
    private int _fld04AC;
    private int _fld04AD;
    private int _fld04AE;
    private int _fld04AF;
    private int _fld04B0;
    private int _fld04B1;
    private int _fld04B2;
    private int _fld04B3;
    private int _fld04B4;
    private int _fld04B5;
    private int _fld04B6;
    private int _fld04B7;
    private int _fld04B8;
    private int modelCount;
    private int modelCountMax;
    private GameModel models[];
    private int _fld04BC[];
    private GameModel _fld04BD[];
    private int _fld04BE;
    private int _fld04BF[];
    private int _fld04C0[];
    private int _fld04C1[];
    private int _fld04C2[];
    private int _fld04C3[];
    private int _fld04C4[];
    private int _fld04C7[];
    private int _fld04C8[];
    private int _fld04CB[];
    private int _fld04CC[];
    private int _fld04D0[];
    private int _fld04D1[];
    private int _fld04D2[];
    private int _fld04D3;
    private int _fld04D4;
    public static final int _fld04D5 = 16;
    public static final int _fld04D6 = 4;
    public static final int _fld04D7 = 5;
    public static final int _fld04D8 = 8;
    int _fld04D9;
    int _fld04DA[][];
    boolean _fld04DB[];
    int _fld04DC[];
    public int _fld04DD[];
    int _fld04DE[];
    int _fld04DF[];
    int _fld04E0[];
    int _fld04E1[];
    boolean _fld04E2;
}

