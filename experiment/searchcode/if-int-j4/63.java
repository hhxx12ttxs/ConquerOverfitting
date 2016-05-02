// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   object3d.java

package jagex;

import java.awt.Color;
import java.io.*;

// Referenced classes of package jagex:
//            Stream, GameDialog

public class GameModel
{

    public GameModel(int i, int j)
    {
        _fld0258 = -1;
        _fld0259 = -1;
        _fld0279 = 4;
        _fld027A = 0xbc614e;
        _fld027B = 1;
        _mth0123(i, j);
        _fld0284 = new int[j][1];
        for(int l = 0; l < j; l++)
            _fld0284[l][0] = l;

    }

    private GameModel(int i, int j, boolean flag)
    {
        _fld0258 = -1;
        _fld0259 = -1;
        _fld0279 = 4;
        _fld027A = 0xbc614e;
        _fld027B = 1;
        _mth0123(i, j);
        _fld0284 = new int[j][];
    }

    private void _mth0123(int i, int j)
    {
        _fld027D = new int[i];
        _fld027E = new int[i];
        _fld027F = new int[i];
        _fld0280 = new int[i];
        _fld0281 = new int[i];
        _fld0282 = new int[i];
        _fld025B = new int[i];
        _fld025C = new int[i];
        _fld025D = new int[i];
        _fld025E = new int[i];
        _fld025F = new int[i];
        _fld0260 = new int[i];
        _fld0261 = new int[i];
        _fld0262 = new int[i];
        _fld0263 = new int[i];
        _fld0264 = new int[i];
        _fld0265 = new int[i];
        _fld0267 = new int[j];
        _fld0268 = new int[j][];
        _fld0269 = new int[j];
        _fld026A = new int[j];
        _fld026D = new int[j];
        _fld026E = new int[j];
        _fld026F = new int[j];
        _fld0270 = new int[j];
        _fld026B = new int[j];
        _fld026C = new int[j];
        _fld0285 = new int[j];
        _fld0286 = new int[j];
        _fld0287 = new int[j];
        _fld0288 = new int[j];
        _fld0289 = new int[j];
        _fld028A = new int[j];
        _fld0273 = new boolean[j];
        _fld0272 = new int[j];
        _fld0271 = new int[j];
        for(int l = 0; l < j; l++)
        {
            _fld0273[l] = false;
            _fld0272[l] = 32;
            _fld0271[l] = 512;
        }

        _fld0266 = 0;
        _fld025A = 0;
        _fld027C = i;
        _fld0283 = j;
        _fld028B = _fld028C = _fld028D = 0;
        _fld028E = _fld028F = _fld0290 = 0;
        _fld0291 = _fld0292 = _fld0293 = 256;
        _fld0294 = _fld0295 = _fld0296 = _fld0297 = _fld0298 = _fld0299 = 256;
        _fld029A = 0;
    }

    public GameModel(Stream w1, String s)
    {
        _fld0258 = -1;
        _fld0259 = -1;
        _fld0279 = 4;
        _fld027A = 0xbc614e;
        _fld027B = 1;
        byte abyte0[] = w1._fld05B4;
        _fld02A2 = w1._mth02B5(s);
        if(_fld02A2 == 0)
            return;
        _fld02A3 = 0;
        _mth012A(abyte0);
        int i = _mth012A(abyte0);
        int j = _mth012A(abyte0);
        int k2 = 0;
        int l2 = 0;
        int i3 = 0;
        boolean flag = true;
        if(s.toLowerCase().endsWith(".obj"))
            flag = false;
        _mth0123(i, j);
        _fld0284 = new int[j][];
        for(int j3 = 0; j3 < i; j3++)
        {
            int l = _mth012A(abyte0);
            int i1 = _mth012A(abyte0);
            int j1 = _mth012A(abyte0);
            _mth012D(l, i1, j1);
        }

        for(int k3 = 0; k3 < j; k3++)
        {
            int k1 = _mth012A(abyte0);
            int l1 = _mth012A(abyte0);
            int i2 = _mth012A(abyte0);
            int j2 = _mth012A(abyte0);
            if(flag)
            {
                k2 = _mth012A(abyte0);
                l2 = _mth012A(abyte0);
                i3 = _mth012A(abyte0);
            }
            int ai[] = new int[k1];
            for(int l3 = 0; l3 < k1; l3++)
                ai[l3] = _mth012A(abyte0);

            int ai1[] = new int[j2];
            for(int i4 = 0; i4 < j2; i4++)
                ai1[i4] = _mth012A(abyte0);

            int j4 = _mth012E(k1, ai, l1, i2);
            _fld0284[k3] = ai1;
            if(flag)
            {
                _fld0272[j4] = l2;
                _fld0271[j4] = k2;
                if(i3 == 0)
                    _fld0273[j4] = false;
                else
                    _fld0273[j4] = true;
            }
        }

        _fld027B = 1;
    }

    public GameModel(String s)
    {
        _fld0258 = -1;
        _fld0259 = -1;
        _fld0279 = 4;
        _fld027A = 0xbc614e;
        _fld027B = 1;
        boolean flag = false;
        boolean flag1 = false;
        byte abyte0[] = null;
        try
        {
            java.io.InputStream inputstream = GameDialog.DC(s);
            DataInputStream datainputstream = new DataInputStream(inputstream);
            abyte0 = new byte[3];
            _fld02A2 = 0;
            _fld02A3 = 0;
            for(int i = 0; i < 3; i += datainputstream.read(abyte0, i, 3 - i));
            int l = _mth012A(abyte0);
            abyte0 = new byte[l];
            _fld02A2 = 0;
            _fld02A3 = 0;
            for(int j = 0; j < l; j += datainputstream.read(abyte0, j, l - j));
            datainputstream.close();
        }
        catch(IOException _ex)
        {
            GameDialog.showError("Unable to load file!");
        }
        int i1 = _mth012A(abyte0);
        int j1 = _mth012A(abyte0);
        int j3 = 0;
        int k3 = 0;
        int l3 = 0;
        boolean flag2 = true;
        if(s.toLowerCase().endsWith(".obj"))
            flag2 = false;
        _mth0123(i1, j1);
        _fld0284 = new int[j1][];
        for(int i4 = 0; i4 < i1; i4++)
        {
            int k1 = _mth012A(abyte0);
            int l1 = _mth012A(abyte0);
            int i2 = _mth012A(abyte0);
            _mth012D(k1, l1, i2);
        }

        for(int j4 = 0; j4 < j1; j4++)
        {
            int j2 = _mth012A(abyte0);
            int k2 = _mth012A(abyte0);
            int l2 = _mth012A(abyte0);
            int i3 = _mth012A(abyte0);
            if(flag2)
            {
                j3 = _mth012A(abyte0);
                k3 = _mth012A(abyte0);
                l3 = _mth012A(abyte0);
            }
            int ai[] = new int[j2];
            for(int k4 = 0; k4 < j2; k4++)
                ai[k4] = _mth012A(abyte0);

            int ai1[] = new int[i3];
            for(int l4 = 0; l4 < i3; l4++)
                ai1[l4] = _mth012A(abyte0);

            int i5 = _mth012E(j2, ai, k2, l2);
            _fld0284[j4] = ai1;
            if(flag2)
            {
                _fld0272[i5] = k3;
                _fld0271[i5] = j3;
                if(l3 == 0)
                    _fld0273[i5] = false;
                else
                    _fld0273[i5] = true;
            }
        }

        _fld027B = 1;
    }

    public GameModel(GameModel ak[], int i)
    {
        _fld0258 = -1;
        _fld0259 = -1;
        _fld0279 = 4;
        _fld027A = 0xbc614e;
        _fld027B = 1;
        int j = 0;
        int l = 0;
        for(int i1 = 0; i1 < i; i1++)
        {
            j += ak[i1]._fld0266;
            l += ak[i1]._fld025A;
        }

        _mth0123(l, j);
        _fld0284 = new int[j][];
        for(int j1 = 0; j1 < i; j1++)
        {
            GameModel k1 = ak[j1];
            k1._mth0117();
            for(int l1 = 0; l1 < k1._fld0266; l1++)
            {
                int ai[] = new int[k1._fld0267[l1]];
                int ai1[] = k1._fld0268[l1];
                for(int i2 = 0; i2 < k1._fld0267[l1]; i2++)
                    ai[i2] = _mth012D(k1._fld027D[ai1[i2]], k1._fld027E[ai1[i2]], k1._fld027F[ai1[i2]]);

                int j2 = _mth012E(k1._fld0267[l1], ai, k1._fld0269[l1], k1._fld026A[l1]);
                _fld0273[j2] = k1._fld0273[l1];
                _fld0272[j2] = k1._fld0272[l1];
                _fld0271[j2] = k1._fld0271[l1];
                if(i > 1)
                {
                    _fld0284[j2] = new int[k1._fld0284[l1].length + 1];
                    _fld0284[j2][0] = j1;
                    for(int k2 = 0; k2 < k1._fld0284[l1].length; k2++)
                        _fld0284[j2][k2 + 1] = k1._fld0284[l1][k2];

                } else
                {
                    _fld0284[j2] = new int[k1._fld0284[l1].length];
                    for(int l2 = 0; l2 < k1._fld0284[l1].length; l2++)
                        _fld0284[j2][l2] = k1._fld0284[l1][l2];

                }
            }

        }

        _fld027B = 1;
    }

    public GameModel(int i, int ai[], int ai1[], int ai2[], int ai3[], int ai4[], int ai5[],
                     boolean flag, int j, int l)
    {
        this(i * 2, flag ? i + 2 : i);
        _fld025A = i * 2;
        for(int i1 = 0; i1 < i; i1++)
        {
            _fld027D[i1] = ai[i1];
            _fld027E[i1] = ai1[i1];
            _fld027F[i1] = ai2[i1];
            _fld027D[i1 + i] = ai3[i1];
            _fld027E[i1 + i] = ai4[i1];
            _fld027F[i1 + i] = ai5[i1];
        }

        _fld0266 = flag ? i + 2 : i;
        for(int j1 = 0; j1 < i; j1++)
        {
            int ai6[] = new int[4];
            _fld0267[j1] = 4;
            ai6[0] = j1;
            ai6[1] = (j1 + 1) % i;
            ai6[2] = (j1 + 1) % i + i;
            ai6[3] = j1 + i;
            _fld0268[j1] = ai6;
            _fld0269[j1] = j;
            _fld026A[j1] = l;
            _fld0273[j1] = true;
        }

        if(flag)
        {
            int ai7[] = new int[i];
            _fld0267[i] = i;
            for(int k1 = 0; k1 < i; k1++)
                ai7[i - k1 - 1] = k1;

            _fld0268[i] = ai7;
            _fld0269[i] = j;
            _fld026A[i] = l;
            int ai8[] = new int[i];
            _fld0267[i + 1] = i;
            for(int l1 = 0; l1 < i; l1++)
                ai8[l1] = l1 + i;

            _fld0268[i + 1] = ai8;
            _fld0269[i + 1] = j;
            _fld026A[i + 1] = l;
        }
        _fld027B = 1;
    }

    public GameModel(int i, int ai[], int ai1[], int ai2[], int j, int l, int i1,
                     boolean flag, int j1, int k1)
    {
        this(i + 1, flag ? i + 1 : i);
        for(int l1 = 0; l1 < i; l1++)
        {
            _fld027D[l1] = ai[l1];
            _fld027E[l1] = ai1[l1];
            _fld027F[l1] = ai2[l1];
        }

        _fld027D[i] = j;
        _fld027E[i] = l;
        _fld027F[i] = i1;
        _fld025A = i + 1;
        _fld0266 = flag ? i + 1 : i;
        for(int i2 = 0; i2 < i; i2++)
        {
            int ai3[] = new int[3];
            ai3[0] = i2;
            ai3[1] = (i2 + 1) % i;
            ai3[2] = i;
            _fld0267[i2] = 3;
            _fld0268[i2] = ai3;
            _fld0269[i2] = j1;
            _fld026A[i2] = k1;
        }

        if(flag)
        {
            int ai4[] = new int[i];
            _fld0267[i] = i;
            for(int j2 = 0; j2 < i; j2++)
                ai4[i - j2 - 1] = j2;

            _fld0268[i] = ai4;
            _fld0269[i] = j1;
            _fld026A[i] = k1;
        }
        _fld027B = 1;
    }

    public GameModel(int i, int ai[], int ai1[], int ai2[], int j, int l)
    {
        this(i, 1);
        _fld025A = i;
        for(int i1 = 0; i1 < i; i1++)
        {
            _fld027D[i1] = ai[i1];
            _fld027E[i1] = ai1[i1];
            _fld027F[i1] = ai2[i1];
        }

        _fld0266 = 1;
        _fld0267[0] = i;
        int ai3[] = new int[i];
        for(int j1 = 0; j1 < i; j1++)
            ai3[j1] = j1;

        _fld0268[0] = ai3;
        _fld0269[0] = j;
        _fld026A[0] = l;
        _fld027B = 1;
    }

    public int _mth012D(int i, int j, int l)
    {
        for(int i1 = 0; i1 < _fld025A; i1++)
            if(_fld027D[i1] == i && _fld027E[i1] == j && _fld027F[i1] == l)
                return i1;

        if(_fld025A >= _fld027C)
        {
            return -1;
        } else
        {
            _fld027D[_fld025A] = i;
            _fld027E[_fld025A] = j;
            _fld027F[_fld025A] = l;
            return _fld025A++;
        }
    }

    public int _mth012E(int i, int ai[], int j, int l)
    {
        if(_fld0266 >= _fld0283)
        {
            return -1;
        } else
        {
            _fld0267[_fld0266] = i;
            _fld0268[_fld0266] = ai;
            _fld0269[_fld0266] = j;
            _fld026A[_fld0266] = l;
            _fld027B = 1;
            return _fld0266++;
        }
    }

    public void _mth0121(boolean flag, int i, int j)
    {
        i = 256 - i * 4;
        j = (64 - j) * 16 + 128;
        for(int l = 0; l < _fld0266; l++)
        {
            _fld0273[l] = flag;
            _fld0272[l] = i;
            _fld0271[l] = j;
        }

        _mth0133();
    }

    public void _mth0130(int i, int j, int l, int i1)
    {
        if(j != _fld027A)
            _fld027D[i] = j;
        if(l != _fld027A)
            _fld027E[i] = l;
        if(i1 != _fld027A)
            _fld027F[i] = i1;
        _fld027B = 1;
    }

    public int _mth013A(int i, int j, int l)
    {
        int i1 = 0x98967f;
        boolean flag = false;
        int k1 = 0;
        for(int l1 = 0; l1 < _fld025A; l1++)
        {
            int j1 = 0;
            if(i != _fld027A)
                j1 += Math.abs(i - _fld027D[l1]);
            if(j != _fld027A)
                j1 += Math.abs(j - _fld027E[l1]);
            if(l != _fld027A)
                j1 += Math.abs(l - _fld027F[l1]);
            if(j1 < i1)
            {
                i1 = j1;
                k1 = l1;
            }
        }

        return k1;
    }

    public void _mth0139(int i, int j)
    {
        _fld0269[i] = j;
    }

    public void _mth0138(int i, int j)
    {
        _fld026A[i] = j;
    }

    public int _mth0126(int i)
    {
        return _fld0269[i];
    }

    public int _mth0124(int i)
    {
        return _fld026A[i];
    }

    public void _mth0131()
    {
        for(int i = 0; i < _fld0266; i++)
        {
            if(_fld0269[i] == _fld027A)
                _fld0269[i] = _fld026A[i];
            if(_fld026A[i] == _fld027A)
                _fld026A[i] = _fld0269[i];
        }

    }

    public void _mth0129(int i)
    {
        int ai[] = _fld0268[i];
        int j = _fld0267[i];
        int ai1[] = new int[j];
        for(int l = 0; l < j; l++)
            ai1[l] = ai[j - l - 1];

        for(int i1 = 0; i1 < j; i1++)
            ai[i1] = ai1[i1];

        _fld027B = 1;
    }

    public void _mth011A(int i)
    {
        int ai[] = _fld0268[i];
        int j = _fld0267[i];
        int l = ai[0];
        for(int i1 = 0; i1 < j - 1; i1++)
            ai[i1] = ai[i1 + 1];

        ai[j - 1] = l;
        _fld027B = 1;
    }

    public void _mth0143(int i)
    {
        int ai[] = _fld0268[i];
        int j = _fld0267[i];
        int l = ai[j - 1];
        for(int i1 = j - 1; i1 > 0; i1--)
            ai[i1] = ai[i1 - 1];

        ai[0] = l;
        _fld027B = 1;
    }

    public void _mth0128(int i)
    {
        int j = _fld0267[i];
        if(j <= 3)
            return;
        int ai[] = _fld0268[i];
        int i1 = _fld027D[ai[0]];
        int j1 = _fld027E[ai[0]];
        int k1 = _fld027F[ai[0]];
        int l1 = _fld027D[ai[1]] - i1;
        int i2 = _fld027E[ai[1]] - j1;
        int j2 = _fld027F[ai[1]] - k1;
        int k2 = _fld027D[ai[2]] - i1;
        int l2 = _fld027E[ai[2]] - j1;
        int i3 = _fld027F[ai[2]] - k1;
        long l3 = i2 * i3 - l2 * j2;
        long l4 = j2 * k2 - i3 * l1;
        long l5 = l1 * l2 - k2 * i2;
        long l6 = (long)Math.sqrt(l3 * l3 + l4 * l4 + l5 * l5);
        if(l6 == 0L)
            return;
        for(int j3 = 3; j3 < j; j3++)
        {
            int l = ai[j3];
            long l7 = ((long)(_fld027D[l] - i1) * l3 + (long)(_fld027E[l] - j1) * l4 + (long)(_fld027F[l] - k1) * l5) / l6;
            if(l7 != 0L)
            {
                _fld027D[l] -= (l7 * l3) / l6;
                _fld027E[l] -= (l7 * l4) / l6;
                _fld027F[l] -= (l7 * l5) / l6;
            }
        }

        _fld027B = 1;
    }

    public void _mth013E(int i, int j, int l)
    {
        _fld028E = _fld028E + i & 0xff;
        _fld028F = _fld028F + j & 0xff;
        _fld0290 = _fld0290 + l & 0xff;
        _mth0141();
        _fld027B = 1;
    }

    public void _mth011F(int i, int j, int l)
    {
        _fld028E = i & 0xff;
        _fld028F = j & 0xff;
        _fld0290 = l & 0xff;
        _mth0141();
        _fld027B = 1;
    }

    public void _mth012C(int i, int j, int l)
    {
        _fld028B += i;
        _fld028C += j;
        _fld028D += l;
        _mth0141();
        _fld027B = 1;
    }

    public void _mth011B(int i, int j, int l)
    {
        _fld028B = i;
        _fld028C = j;
        _fld028D = l;
        _mth0141();
        _fld027B = 1;
    }

    public void _mth0140(double d)
    {
        int i = 0;
        int j = 0;
        int l = 0;
        for(int i1 = 0; i1 < _fld025A; i1++)
        {
            i += _fld027D[i1];
            j += _fld027E[i1];
            l += _fld027F[i1];
        }

        i /= _fld025A;
        j /= _fld025A;
        l /= _fld025A;
        i = (int)((double)i * d) - i;
        j = (int)((double)j * d) - j;
        l = (int)((double)l * d) - l;
        for(int j1 = 0; j1 < _fld025A; j1++)
        {
            _fld027D[j1] += i;
            _fld027E[j1] += j;
            _fld027F[j1] += l;
        }

        _fld027B = 1;
    }

    public int _mth013B()
    {
        return _fld028B;
    }

    public int _mth012F()
    {
        return _fld028C;
    }

    public int _mth0132()
    {
        return _fld028D;
    }

    public void _mth0142(int i, int j, int l)
    {
        _fld0291 = i;
        _fld0292 = j;
        _fld0293 = l;
        _mth0141();
        _fld027B = 1;
    }

    public void _mth0122(int i, int j, int l, int i1, int j1, int k1)
    {
        _fld0294 = i;
        _fld0295 = j;
        _fld0296 = l;
        _fld0297 = i1;
        _fld0298 = j1;
        _fld0299 = k1;
        _mth0141();
        _fld027B = 1;
    }

    public void _mth0120(int i, int j, int l)
    {
        int i1 = _fld028B - i;
        int j1 = _fld028C - j;
        int k1 = _fld028D - l;
        for(int l1 = 0; l1 < _fld025A; l1++)
        {
            _fld027D[l1] += i1;
            _fld027E[l1] += j1;
            _fld027F[l1] += k1;
        }

        _fld028B = i;
        _fld028C = j;
        _fld028D = l;
        _fld027B = 1;
    }

    private void _mth0141()
    {
        if(_fld0294 != 256 || _fld0295 != 256 || _fld0296 != 256 || _fld0297 != 256 || _fld0298 != 256 || _fld0299 != 256)
        {
            _fld029A = 4;
            return;
        }
        if(_fld0291 != 256 || _fld0292 != 256 || _fld0293 != 256)
        {
            _fld029A = 3;
            return;
        }
        if(_fld028E != 0 || _fld028F != 0 || _fld0290 != 0)
        {
            _fld029A = 2;
            return;
        }
        if(_fld028B != 0 || _fld028C != 0 || _fld028D != 0)
        {
            _fld029A = 1;
            return;
        } else
        {
            _fld029A = 0;
            return;
        }
    }

    private void _mth013C(int i, int j, int l)
    {
        for(int i1 = 0; i1 < _fld025A; i1++)
        {
            _fld0280[i1] += i;
            _fld0281[i1] += j;
            _fld0282[i1] += l;
        }

    }

    private void _mth0125(int i, int j, int l)
    {
        for(int j3 = 0; j3 < _fld025A; j3++)
        {
            if(l != 0)
            {
                int i1 = _fld0276[l];
                int l1 = _fld0276[l + 256];
                int k2 = _fld025C[j3] * i1 + _fld025B[j3] * l1 >> 15;
                _fld025C[j3] = _fld025C[j3] * l1 - _fld025B[j3] * i1 >> 15;
                _fld025B[j3] = k2;
            }
            if(j != 0)
            {
                int j1 = _fld0276[j];
                int i2 = _fld0276[j + 256];
                int l2 = _fld025D[j3] * j1 + _fld025B[j3] * i2 >> 15;
                _fld025D[j3] = _fld025D[j3] * i2 - _fld025B[j3] * j1 >> 15;
                _fld025B[j3] = l2;
            }
            if(i != 0)
            {
                int k1 = _fld0276[i];
                int j2 = _fld0276[i + 256];
                int i3 = _fld025C[j3] * j2 - _fld025D[j3] * k1 >> 15;
                _fld025D[j3] = _fld025C[j3] * k1 + _fld025D[j3] * j2 >> 15;
                _fld025C[j3] = i3;
            }
        }

    }

    private void _mth0118(int i, int j, int l)
    {
        for(int j3 = 0; j3 < _fld025A; j3++)
        {
            if(l != 0)
            {
                int i1 = _fld0276[l];
                int l1 = _fld0276[l + 256];
                int k2 = _fld0281[j3] * i1 + _fld0280[j3] * l1 >> 15;
                _fld0281[j3] = _fld0281[j3] * l1 - _fld0280[j3] * i1 >> 15;
                _fld0280[j3] = k2;
            }
            if(i != 0)
            {
                int j1 = _fld0276[i];
                int i2 = _fld0276[i + 256];
                int l2 = _fld0281[j3] * i2 - _fld0282[j3] * j1 >> 15;
                _fld0282[j3] = _fld0281[j3] * j1 + _fld0282[j3] * i2 >> 15;
                _fld0281[j3] = l2;
            }
            if(j != 0)
            {
                int k1 = _fld0276[j];
                int j2 = _fld0276[j + 256];
                int i3 = _fld0282[j3] * k1 + _fld0280[j3] * j2 >> 15;
                _fld0282[j3] = _fld0282[j3] * j2 - _fld0280[j3] * k1 >> 15;
                _fld0280[j3] = i3;
            }
        }

    }

    private void _mth0134(int i, int j, int l, int i1, int j1, int k1)
    {
        for(int l1 = 0; l1 < _fld025A; l1++)
        {
            if(i != 0)
                _fld0280[l1] += _fld0281[l1] * i >> 8;
            if(j != 0)
                _fld0282[l1] += _fld0281[l1] * j >> 8;
            if(l != 0)
                _fld0280[l1] += _fld0282[l1] * l >> 8;
            if(i1 != 0)
                _fld0281[l1] += _fld0282[l1] * i1 >> 8;
            if(j1 != 0)
                _fld0282[l1] += _fld0280[l1] * j1 >> 8;
            if(k1 != 0)
                _fld0281[l1] += _fld0280[l1] * k1 >> 8;
        }

    }

    private void _mth011E(int i, int j, int l)
    {
        for(int i1 = 0; i1 < _fld025A; i1++)
        {
            _fld0280[i1] = _fld0280[i1] * i >> 8;
            _fld0281[i1] = _fld0281[i1] * j >> 8;
            _fld0282[i1] = _fld0282[i1] * l >> 8;
        }

    }

    private void _mth011D()
    {
        _fld029B = _fld029D = _fld029F = 0xf423f;
        _fld029C = _fld029E = _fld02A0 = 0xfff0bdc1;
        for(int i = 0; i < _fld0266; i++)
        {
            int ai[] = _fld0268[i];
            int l = ai[0];
            int j1 = _fld0267[i];
            int k1;
            int l1 = k1 = _fld0280[l];
            int i2;
            int j2 = i2 = _fld0281[l];
            int k2;
            int l2 = k2 = _fld0282[l];
            for(int j = 0; j < j1; j++)
            {
                int i1 = ai[j];
                if(_fld0280[i1] < k1)
                    k1 = _fld0280[i1];
                else
                if(_fld0280[i1] > l1)
                    l1 = _fld0280[i1];
                if(_fld0281[i1] < i2)
                    i2 = _fld0281[i1];
                else
                if(_fld0281[i1] > j2)
                    j2 = _fld0281[i1];
                if(_fld0282[i1] < k2)
                    k2 = _fld0282[i1];
                else
                if(_fld0282[i1] > l2)
                    l2 = _fld0282[i1];
            }

            _fld0285[i] = k1;
            _fld0286[i] = l1;
            _fld0287[i] = i2;
            _fld0288[i] = j2;
            _fld0289[i] = k2;
            _fld028A[i] = l2;
            if(k1 < _fld029B)
                _fld029B = k1;
            if(l1 > _fld029C)
                _fld029C = l1;
            if(i2 < _fld029D)
                _fld029D = i2;
            if(j2 > _fld029E)
                _fld029E = j2;
            if(k2 < _fld029F)
                _fld029F = k2;
            if(l2 > _fld02A0)
                _fld02A0 = l2;
        }

    }

    public void _mth0133()
    {
        for(int i = 0; i < _fld025A; i++)
        {
            _fld0263[i] = _fld027A;
            _fld0264[i] = _fld027A;
            _fld0260[i] = 0;
            _fld0261[i] = 0;
            _fld0262[i] = 0;
            _fld0265[i] = 0;
        }

        for(int j = 0; j < _fld0266; j++)
        {
            int ai[] = _fld0268[j];
            int l = _fld0280[ai[0]];
            int i1 = _fld0281[ai[0]];
            int j1 = _fld0282[ai[0]];
            int k1 = _fld0280[ai[1]] - l;
            int l1 = _fld0281[ai[1]] - i1;
            int i2 = _fld0282[ai[1]] - j1;
            int j2 = _fld0280[ai[2]] - l;
            int k2 = _fld0281[ai[2]] - i1;
            int l2 = _fld0282[ai[2]] - j1;
            int i3 = l1 * l2 - k2 * i2;
            int j3 = i2 * j2 - l2 * k1;
            int k3;
            for(k3 = k1 * k2 - j2 * l1; i3 > 8192 || j3 > 8192 || k3 > 8192 || i3 < -8192 || j3 < -8192 || k3 < -8192; k3 >>= 1)
            {
                i3 >>= 1;
                j3 >>= 1;
            }

            int l3 = (int)(256D * Math.sqrt(i3 * i3 + j3 * j3 + k3 * k3));
            if(l3 <= 0)
                l3 = 1;
            _fld026D[j] = (i3 * 0x10000) / l3;
            _fld026E[j] = (j3 * 0x10000) / l3;
            _fld026F[j] = (k3 * 0x10000) / l3;
            _fld026B[j] = -1;
            _fld0270[j] = _fld027A;
            if(_fld0273[j])
            {
                for(int i4 = 0; i4 < _fld0267[j]; i4++)
                {
                    int j4 = _fld0268[j][i4];
                    _fld0260[j4] += _fld026D[j];
                    _fld0261[j4] += _fld026E[j];
                    _fld0262[j4] += _fld026F[j];
                    _fld0265[j4]++;
                }

            }
        }

    }

    private void _mth0137()
    {
        if(_fld027B == 1)
        {
            _fld027B = 0;
            for(int i = 0; i < _fld025A; i++)
            {
                _fld0280[i] = _fld027D[i];
                _fld0281[i] = _fld027E[i];
                _fld0282[i] = _fld027F[i];
            }

            if(_fld029A >= 2)
                _mth0118(_fld028E, _fld028F, _fld0290);
            if(_fld029A >= 3)
                _mth011E(_fld0291, _fld0292, _fld0293);
            if(_fld029A >= 4)
                _mth0134(_fld0294, _fld0295, _fld0296, _fld0297, _fld0298, _fld0299);
            if(_fld029A >= 1)
                _mth013C(_fld028B, _fld028C, _fld028D);
            _mth011D();
            _mth0133();
        }
    }

    public void _mth0135(int i, int j, int l, int i1, int j1, int k1, int l1, 
            int i2)
    {
        _mth0137();
        for(int j2 = 0; j2 < _fld025A; j2++)
        {
            _fld025B[j2] = _fld0280[j2] - i;
            _fld025C[j2] = _fld0281[j2] - j;
            _fld025D[j2] = _fld0282[j2] - l;
        }

        _mth0125(i1, j1, k1);
        for(int k2 = 0; k2 < _fld025A; k2++)
        {
            if(_fld025D[k2] >= i2)
                _fld025E[k2] = (_fld025B[k2] << l1) / _fld025D[k2];
            else
                _fld025E[k2] = _fld025B[k2] << l1;
            if(_fld025D[k2] >= i2)
                _fld025F[k2] = (_fld025C[k2] << l1) / _fld025D[k2];
            else
                _fld025F[k2] = _fld025C[k2] << l1;
        }

    }

    public void _mth0117()
    {
        _mth0137();
        for(int i = 0; i < _fld025A; i++)
        {
            _fld027D[i] = _fld0280[i];
            _fld027E[i] = _fld0281[i];
            _fld027F[i] = _fld0282[i];
        }

        _fld028B = _fld028C = _fld028D = 0;
        _fld028E = _fld028F = _fld0290 = 0;
        _fld0291 = _fld0292 = _fld0293 = 256;
        _fld0294 = _fld0295 = _fld0296 = _fld0297 = _fld0298 = _fld0299 = 256;
        _fld029A = 0;
    }

    public boolean _mth013D(int i, int j, int l, int i1, int j1, int k1, int l1)
    {
        _mth0137();
        if((i >= _fld029B || i1 >= _fld029B) && (i <= _fld029C || i1 <= _fld029C) && (j >= _fld029D || j1 >= _fld029D) && (j <= _fld029E || j1 <= _fld029E) && (l >= _fld029F || k1 >= _fld029F) && (l <= _fld02A0 || k1 <= _fld02A0))
        {
            for(int i2 = 0; i2 < _fld0266; i2++)
                if((i > _fld0285[i2] || i1 > _fld0285[i2]) && (i < _fld0286[i2] || i1 < _fld0286[i2]))
                {
                    int j2 = i;
                    int k2 = j;
                    int l2 = l;
                    int i3 = i1;
                    int j3 = j1;
                    int k3 = k1;
                    if(i3 != j2)
                    {
                        double d = (double)(j3 - k2) / (double)(i3 - j2);
                        double d2 = (double)(k3 - l2) / (double)(i3 - j2);
                        if(j2 < _fld0285[i2])
                        {
                            k2 = (int)((double)k2 + (double)(_fld0285[i2] - j2) * d);
                            l2 = (int)((double)l2 + (double)(_fld0285[i2] - j2) * d2);
                            j2 = _fld0285[i2];
                        } else
                        if(j2 > _fld0286[i2])
                        {
                            k2 = (int)((double)k2 + (double)(_fld0286[i2] - j2) * d);
                            l2 = (int)((double)l2 + (double)(_fld0286[i2] - j2) * d2);
                            j2 = _fld0286[i2];
                        }
                        if(i3 < _fld0285[i2])
                        {
                            j3 = (int)((double)j3 + (double)(_fld0285[i2] - i3) * d);
                            k3 = (int)((double)k3 + (double)(_fld0285[i2] - i3) * d2);
                            i3 = _fld0285[i2];
                        } else
                        if(i3 > _fld0286[i2])
                        {
                            j3 = (int)((double)j3 + (double)(_fld0286[i2] - i3) * d);
                            k3 = (int)((double)k3 + (double)(_fld0286[i2] - i3) * d2);
                            i3 = _fld0286[i2];
                        }
                    }
                    if((k2 >= _fld0287[i2] || j3 >= _fld0287[i2]) && (k2 <= _fld0288[i2] || j3 <= _fld0288[i2]))
                    {
                        if(j3 != k2)
                        {
                            double d1 = (double)(k3 - l2) / (double)(j3 - k2);
                            if(k2 < _fld0287[i2])
                            {
                                l2 = (int)((double)l2 + (double)(_fld0287[i2] - k2) * d1);
                                k2 = _fld0287[i2];
                            } else
                            if(k2 > _fld0288[i2])
                            {
                                l2 = (int)((double)l2 + (double)(_fld0288[i2] - k2) * d1);
                                k2 = _fld0288[i2];
                            }
                            if(j3 < _fld0287[i2])
                            {
                                k3 = (int)((double)k3 + (double)(_fld0287[i2] - j3) * d1);
                                j3 = _fld0287[i2];
                            } else
                            if(j3 > _fld0288[i2])
                            {
                                k3 = (int)((double)k3 + (double)(_fld0288[i2] - j3) * d1);
                                j3 = _fld0288[i2];
                            }
                        }
                        if((l2 >= _fld0289[i2] || k3 >= _fld0289[i2]) && (l2 <= _fld028A[i2] || k3 <= _fld028A[i2]))
                        {
                            int l3 = _fld0268[i2][0];
                            int i4 = _fld0280[l3];
                            int j4 = _fld0281[l3];
                            int k4 = _fld0282[l3];
                            int l4 = (i - i4) * _fld026D[i2] + (j - j4) * _fld026E[i2] + (l - k4) * _fld026F[i2];
                            int i5 = (i1 - i4) * _fld026D[i2] + (j1 - j4) * _fld026E[i2] + (k1 - k4) * _fld026F[i2];
                            l1 *= 256;
                            if(l4 <= l1 && i5 >= -l1 || l4 >= -l1 && i5 <= l1)
                                return true;
                        }
                    }
                }

        }
        return false;
    }

    public boolean _mth011C(int i, int j, int l, int i1)
    {
        _mth0137();
        if(i >= _fld029B && i <= _fld029C && j >= _fld029D && j <= _fld029E && l >= _fld029F && l <= _fld02A0)
        {
            for(int j1 = 0; j1 < _fld0266; j1++)
                if(i >= _fld0285[j1] && i <= _fld0286[j1] && j >= _fld0287[j1] && j <= _fld0288[j1] && l >= _fld0289[j1] && l <= _fld0289[j1])
                {
                    int k1 = _fld0268[j1][0];
                    int l1 = _fld0280[k1];
                    int i2 = _fld0281[k1];
                    int j2 = _fld0282[k1];
                    int k2 = (i - l1) * _fld026D[j1] + (j - i2) * _fld026E[j1] + (l - j2) * _fld026F[j1];
                    i1 *= 256;
                    if(k2 >= -i1 && k2 <= i1)
                        return true;
                }

        }
        return false;
    }

    public GameModel _mth013F()
    {
        GameModel ak[] = new GameModel[1];
        ak[0] = this;
        return new GameModel(ak, 1);
    }

    private void _mth012B(int i)
    {
        if(i == _fld027A)
            i = 0x1e240;
        i += 0x20000;
        _fld02A1[_fld02A2++] = _fld0277[i >> 12 & 0x3f];
        _fld02A1[_fld02A2++] = _fld0277[i >> 6 & 0x3f];
        _fld02A1[_fld02A2++] = _fld0277[i & 0x3f];
    }

    public int _mth012A(byte abyte0[])
    {
        for(; abyte0[_fld02A2] == 10 || abyte0[_fld02A2] == 13; _fld02A2++);
        int i = _fld0278[abyte0[_fld02A2++] & 0xff];
        int j = _fld0278[abyte0[_fld02A2++] & 0xff];
        int l = _fld0278[abyte0[_fld02A2++] & 0xff];
        int i1 = (i * 4096 + j * 64 + l) - 0x20000;
        if(i1 == 0x1e240)
            i1 = _fld027A;
        return i1;
    }

    public void _mth0136(String s)
    {
        _mth0117();
        int i = 3 + _fld025A * 3 + _fld0266 * 4;
        for(int j = 0; j < _fld0266; j++)
            i += _fld0267[j] + _fld0284[j].length;

        i *= 3;
        _fld02A1 = new byte[i + i / 120];
        _fld02A2 = 0;
        _fld02A3 = 0;
        _mth012B(i - 3);
        _mth012B(_fld025A);
        _mth012B(_fld0266);
        for(int l = 0; l < _fld025A; l++)
        {
            _mth012B(_fld027D[l]);
            _mth012B(_fld027E[l]);
            _mth012B(_fld027F[l]);
        }

        for(int i1 = 0; i1 < _fld0266; i1++)
        {
            _mth012B(_fld0267[i1]);
            _mth012B(_fld0269[i1]);
            _mth012B(_fld026A[i1]);
            _mth012B(_fld0284[i1].length);
            for(int j1 = 0; j1 < _fld0267[i1]; j1++)
                _mth012B(_fld0268[i1][j1]);

            for(int k1 = 0; k1 < _fld0284[i1].length; k1++)
                _mth012B(_fld0284[i1][k1]);

        }

        try
        {
            FileOutputStream fileoutputstream = new FileOutputStream(s);
            fileoutputstream.write(_fld02A1, 0, _fld02A2);
            fileoutputstream.close();
            return;
        }
        catch(Exception _ex)
        {
            GameDialog.showError("Error! Unable to save file");
        }
    }

    public void _mth0119(String s)
    {
        _mth0117();
        int i = 3 + _fld025A * 3 + _fld0266 * 7;
        for(int j = 0; j < _fld0266; j++)
            i += _fld0267[j] + _fld0284[j].length;

        i *= 3;
        _fld02A1 = new byte[i + i / 120];
        _fld02A2 = 0;
        _fld02A3 = 0;
        _mth012B(i - 3);
        _mth012B(_fld025A);
        _mth012B(_fld0266);
        for(int l = 0; l < _fld025A; l++)
        {
            _mth012B(_fld027D[l]);
            _mth012B(_fld027E[l]);
            _mth012B(_fld027F[l]);
        }

        for(int i1 = 0; i1 < _fld0266; i1++)
        {
            _mth012B(_fld0267[i1]);
            _mth012B(_fld0269[i1]);
            _mth012B(_fld026A[i1]);
            _mth012B(_fld0284[i1].length);
            _mth012B(_fld0271[i1]);
            _mth012B(_fld0272[i1]);
            _mth012B(_fld0273[i1] ? 1 : 0);
            for(int j1 = 0; j1 < _fld0267[i1]; j1++)
                _mth012B(_fld0268[i1][j1]);

            for(int k1 = 0; k1 < _fld0284[i1].length; k1++)
                _mth012B(_fld0284[i1][k1]);

        }

        try
        {
            FileOutputStream fileoutputstream = new FileOutputStream(s);
            fileoutputstream.write(_fld02A1, 0, _fld02A2);
            fileoutputstream.close();
            return;
        }
        catch(Exception _ex)
        {
            GameDialog.showError("Error! Unable to save file");
        }
    }

    public GameModel[] _mth0127()
    {
        _mth0117();
        int i = 0;
        for(int j = 0; j < _fld0266; j++)
            if(_fld0284[j][0] > i)
                i = _fld0284[j][0];

        int ai[] = new int[++i];
        int ai1[] = new int[i];
        for(int l = 0; l < i; l++)
        {
            ai[l] = 0;
            ai1[l] = 0;
        }

        for(int i1 = 0; i1 < _fld0266; i1++)
        {
            ai1[_fld0284[i1][0]]++;
            ai[_fld0284[i1][0]] += _fld0267[i1];
        }

        GameModel ak[] = new GameModel[i];
        for(int j1 = 0; j1 < i; j1++)
            ak[j1] = new GameModel(ai[j1], ai1[j1], true);

        for(int k1 = 0; k1 < _fld0266; k1++)
        {
            GameModel k2 = ak[_fld0284[k1][0]];
            int ai2[] = new int[_fld0267[k1]];
            int ai3[] = _fld0268[k1];
            for(int l1 = 0; l1 < _fld0267[k1]; l1++)
                ai2[l1] = k2._mth012D(_fld027D[ai3[l1]], _fld027E[ai3[l1]], _fld027F[ai3[l1]]);

            int i2 = k2._mth012E(_fld0267[k1], ai2, _fld0269[k1], _fld026A[k1]);
            k2._fld0273[i2] = _fld0273[k1];
            k2._fld0272[i2] = _fld0272[k1];
            k2._fld0271[i2] = _fld0271[k1];
            int j2 = _fld0284[k1].length - 1;
            if(j2 < 1)
                j2 = 1;
            k2._fld0284[i2] = new int[j2];
            if(j2 > 1)
            {
                for(int l2 = 0; l2 < j2; l2++)
                    k2._fld0284[i2][l2] = _fld0284[k1][l2 + 1];

            } else
            {
                k2._fld0284[i2][0] = i2;
            }
        }

        return ak;
    }

    public Color _fld0256;
    public Color _fld0257;
    public int _fld0258;
    public int _fld0259;
    public int _fld025A;
    public int _fld025B[];
    public int _fld025C[];
    public int _fld025D[];
    public int _fld025E[];
    public int _fld025F[];
    public int _fld0260[];
    public int _fld0261[];
    public int _fld0262[];
    public int _fld0263[];
    public int _fld0264[];
    public int _fld0265[];
    public int _fld0266;
    public int _fld0267[];
    public int _fld0268[][];
    public int _fld0269[];
    public int _fld026A[];
    public int _fld026B[];
    public int _fld026C[];
    public int _fld026D[];
    public int _fld026E[];
    public int _fld026F[];
    public int _fld0270[];
    public int _fld0271[];
    public int _fld0272[];
    public boolean _fld0273[];
    public int _fld0274;
    public int _fld0275;
    private static int _fld0276[];
    private static byte _fld0277[];
    private static int _fld0278[];
    private int _fld0279;
    private int _fld027A;
    private int _fld027B;
    private int _fld027C;
    private int _fld027D[];
    private int _fld027E[];
    private int _fld027F[];
    private int _fld0280[];
    private int _fld0281[];
    private int _fld0282[];
    private int _fld0283;
    private int _fld0284[][];
    private int _fld0285[];
    private int _fld0286[];
    private int _fld0287[];
    private int _fld0288[];
    private int _fld0289[];
    private int _fld028A[];
    private int _fld028B;
    private int _fld028C;
    private int _fld028D;
    private int _fld028E;
    private int _fld028F;
    private int _fld0290;
    private int _fld0291;
    private int _fld0292;
    private int _fld0293;
    private int _fld0294;
    private int _fld0295;
    private int _fld0296;
    private int _fld0297;
    private int _fld0298;
    private int _fld0299;
    private int _fld029A;
    private int _fld029B;
    private int _fld029C;
    private int _fld029D;
    private int _fld029E;
    private int _fld029F;
    private int _fld02A0;
    private byte _fld02A1[];
    private int _fld02A2;
    private int _fld02A3;

    static 
    {
        _fld0276 = new int[512];
        _fld0277 = new byte[64];
        _fld0278 = new int[256];
        for(int i = 0; i < 256; i++)
        {
            _fld0276[i] = (int)(Math.sin((double)i * 0.02454369D) * 32768D);
            _fld0276[i + 256] = (int)(Math.cos((double)i * 0.02454369D) * 32768D);
        }

        for(int j = 0; j < 10; j++)
            _fld0277[j] = (byte)(48 + j);

        for(int l = 0; l < 26; l++)
            _fld0277[l + 10] = (byte)(65 + l);

        for(int i1 = 0; i1 < 26; i1++)
            _fld0277[i1 + 36] = (byte)(97 + i1);

        _fld0277[62] = -93;
        _fld0277[63] = 36;
        for(int j1 = 0; j1 < 10; j1++)
            _fld0278[48 + j1] = j1;

        for(int k1 = 0; k1 < 26; k1++)
            _fld0278[65 + k1] = k1 + 10;

        for(int l1 = 0; l1 < 26; l1++)
            _fld0278[97 + l1] = l1 + 36;

        _fld0278[163] = 62;
        _fld0278[36] = 63;
    }
}

