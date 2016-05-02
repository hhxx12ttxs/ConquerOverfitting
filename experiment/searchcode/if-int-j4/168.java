// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   dungeongame.java

import jagex.*;
import java.awt.*;
import java.io.IOException;

public class Dungeon extends Game
{

    public boolean _mth0195()
    {
        if(_fld01A4 != 0)
            return false;
        if(_fld01A6 != 0)
            return false;
        return _fld01A8 == 0;
    }

    public void _mth01A2(int i, int k)
        throws IOException
    {
        if(i == 255)
        {
            _fld01AB = super.castleStream.getShort(super.castlePdata, 1);
            _fld01AC = super.castleStream.getShort(super.castlePdata, 3);
            _fld01AE = 0;
            return;
        }
        if(i == 254)
        {
            int l = 1;
            _fld01A1 = super.castlePdata[l++];
            _fld01A3 = super.castlePdata[l++];
            if(_fld01A3 == 0)
            {
                _fld01A3 = 1;
                _fld01A4 = _fld01A3 * _fld01A5;
                _fld01AA = -1;
            } else
            {
                _fld01AA = _fld01A3;
                _fld01A8 = 40;
                _fld01A4 = 0;
            }
            for(int j1 = 0; j1 <= _fld01A3; j1++)
            {
                _fld019F[j1] = super.castlePdata[l++];
                _fld01A0[j1] = super.castlePdata[l++];
            }

            _fld01A2 = super.castlePdata[l++];
            return;
        }
        if(i == 253)
        {
            int i1 = super.castlePdata[1];
            boolean flag = false;
            for(int l1 = 0; l1 < 12; l1++)
            {
                if(_fld01B2[l1] != i1)
                    continue;
                flag = true;
                break;
            }

            if(!flag)
            {
                super.castleref.c(12, 63);
                for(int i2 = 0; i2 < 12; i2++)
                    if(_fld01B2[i2] == -1)
                    {
                        _fld01A6 = 64;
                        _fld01A7 = i2;
                        _fld01A9 = i1;
                        return;
                    }

                return;
            }
        } else
        {
            if(i == 252)
            {
                byte byte0 = super.castlePdata[1];
                for(int k1 = 0; k1 < 12; k1++)
                {
                    if(_fld01B2[k1] != byte0)
                        continue;
                    super.castleref.c(11, 63);
                    _fld01A6 = -64;
                    _fld01A7 = k1;
                    _fld01A9 = _fld01B2[k1];
                    _fld01B2[k1] = -1;
                    break;
                }

                F5();
                return;
            }
            if(i == 251)
            {
                _fld01AB = super.castleStream.getShort(super.castlePdata, 1);
                _fld01AC = super.castleStream.getShort(super.castlePdata, 3);
                _fld01AE = 1;
                return;
            }
            if(i == 250)
                _fld01B3 = new String(super.castlePdata, 1, k - 1);
        }
    }

    public void _mth0199()
    {
        super._fld03C3 = 6;
        super.castleSurface2mby._mth01CF();
        super.castleSurface2mby._mth01B7("dungeon/caves.jpg", 0, false);
        super.castleSurface2mby._mth01D4("dungeon/all.gif", 1, true, 18, 35, 36);
        super.castleSurface2mby._mth01D4("dungeon/bits.gif", 19, true, 9, 49, 42);
        super.castleSurface2mby._mth01D4("dungeon/compass.gif", 28, true, 11, 60, 60);
        super.castleSurface2mby._mth01D4("dungeon/players.gif", 39, true, 6, 13, 18);
    }

    public void _mth0191()
    {
        super.castleSurface2mby._fld0413 = 0;
        super.castleSurface2mby._fld0414 = 290;
        super.castleSurface2mby._fld0415 = 0;
        super.castleSurface2mby._fld0416 = 512;
        _fld01AB = 0;
        _fld01AC = 60;
        _fld01AD = 0;
        _fld01B3 = "";
        for(int i = 0; i < 8; i++)
        {
            for(int k = 0; k < 5; k++)
                _fld01AF[i][k] = -1;

        }

        for(int l = 0; l < 6; l++)
        {
            _fld01B0[l] = 0;
            _fld01B1[l] = 0;
        }

        for(int i1 = 0; i1 < 12; i1++)
            _fld01B2[i1] = -1;

        F5();
    }

    public void _mth0197()
    {
        _fld01AD++;
        if(((GameApplet) (super.castleref)).lastMouseButtonDown == 1 && _fld01AE == 0 && _fld01AB == super.castleId && _fld01AC > 0)
        {
            int i = ((GameApplet) (super.castleref)).mouseX - 18;
            int l = ((GameApplet) (super.castleref)).mouseY - 8;
            if(i > 0 && l > 0 && i < 20 && l < 20)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(0);
                super.castleStream.sendPacket();
            }
            if(i > 20 && l > 0 && i < 40 && l < 20)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(1);
                super.castleStream.sendPacket();
            }
            if(i > 40 && l > 0 && i < 60 && l < 20)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(2);
                super.castleStream.sendPacket();
            }
            if(i > 0 && l > 20 && i < 20 && l < 40)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(3);
                super.castleStream.sendPacket();
            }
            if(i > 40 && l > 20 && i < 60 && l < 40)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(4);
                super.castleStream.sendPacket();
            }
            if(i > 0 && l > 40 && i < 20 && l < 60)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(5);
                super.castleStream.sendPacket();
            }
            if(i > 20 && l > 40 && i < 40 && l < 60)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(6);
                super.castleStream.sendPacket();
            }
            if(i > 40 && l > 40 && i < 60 && l < 60)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(7);
                super.castleStream.sendPacket();
            }
        }
        if(((GameApplet) (super.castleref)).lastMouseButtonDown == 1 && _fld01AE == 1 && _fld01AB == super.castleId && _fld01AC > 0 && ((GameApplet) (super.castleref)).mouseX > 93 && ((GameApplet) (super.castleref)).mouseX < 485 && ((GameApplet) (super.castleref)).mouseY > 41 && ((GameApplet) (super.castleref)).mouseY < 286)
        {
            int k = (((GameApplet) (super.castleref)).mouseX - 93) / 49;
            int i1 = (((GameApplet) (super.castleref)).mouseY - 41) / 49;
            super.castleStream.newPacket(254);
            super.castleStream.putByte(k);
            super.castleStream.putByte(i1);
            super.castleStream.sendPacket();
        }
        if(_fld01A8 > 0)
        {
            _fld01A8--;
            if(_fld01A8 == 0)
                _fld01A4 = _fld01A3 * _fld01A5;
        }
        if(_fld01A4 > 0)
        {
            _fld01A4--;
            if(_fld01A4 == 0)
            {
                _fld01B0[_fld01A1] = _fld019F[_fld01A3];
                _fld01B1[_fld01A1] = _fld01A0[_fld01A3];
                _fld01AF[_fld019F[_fld01A3]][_fld01A0[_fld01A3]] = _fld01A2;
                F5();
            }
        }
        if(_fld01A6 > 0)
        {
            _fld01A6 -= 2;
            if(_fld01A6 == 0)
            {
                _fld01B2[_fld01A7] = _fld01A9;
                F5();
                return;
            }
        } else
        if(_fld01A6 < 0)
            _fld01A6 += 2;
    }

    public void F5()
    {
        super.castleSurface2mby._mth01B5(0, 0, 0);
        for(int i = 0; i < 8; i++)
        {
            for(int k = 0; k < 5; k++)
            {
                int i1 = 117 + 49 * i;
                int k1 = 65 + 49 * k;
                if(_fld01AF[i][k] >= 0 && _fld01AF[i][k] < 18)
                    super.castleSurface2mby._mth01BC(i1, k1, _fld01AF[i][k] + 1);
                if(_fld01AF[i][k] >= 18 && _fld01AF[i][k] < 36)
                {
                    super.castleSurface2mby._mth01BC(i1, k1, (_fld01AF[i][k] + 1) - 18);
                    super.castleSurface2mby._mth01C8(i1 - 24, k1 - 21, 49, 42, 27, 128);
                }
            }

        }

        for(int l = 0; l < 12; l++)
            if(_fld01B2[l] != -1)
            {
                int j1 = 5 + (l % 2) * 38;
                int l1 = 65 + (l / 2) * 37;
                super.castleSurface2mby._mth01B5(j1, l1, 1 + _fld01B2[l]);
            }

        super.castleSurface2mby._mth01B1(50);
    }

    public void _mth019D()
    {
        super.castleSurface2mby._mth01B5(0, 0, 50);
        for(int i = 0; i < 8; i++)
        {
            for(int k = 0; k < 5; k++)
            {
                int i1 = 117 + 49 * i;
                int j2 = 65 + 49 * k;
                if(i == 0 && k == 0)
                    super.castleSurface2mby._mth01BC(i1, j2, 19);
                else
                if(_fld01AF[i][k] == -1)
                    super.castleSurface2mby._mth01BC(i1, j2, 20 + _fld019C[_fld01AD / 6 & 3]);
                else
                if(_fld01AF[i][k] >= 36)
                    super.castleSurface2mby._mth01BC(i1, j2, 23 + (_fld01AD / 6 & 3));
            }

        }

        for(int l = 0; l < 6; l++)
            if(super._fld03C1[l] != null && (_fld01A4 == 0 || _fld01A1 != l))
            {
                int j1 = 117 + 49 * _fld01B0[l];
                int k2 = 65 + 49 * _fld01B1[l];
                super.castleSurface2mby._mth01B5(j1 + _fld019D[l], k2 + _fld019E[l], l + 39);
            }

        if(_fld01A4 > 0)
        {
            int k1 = _fld01A3 * _fld01A5 - _fld01A4;
            int l2 = k1 / _fld01A5;
            int k3 = l2 + 1;
            int j4 = k1 % _fld01A5;
            int l4 = 117 + 49 * _fld019F[l2];
            int i5 = 65 + 49 * _fld01A0[l2];
            int j5 = 117 + 49 * _fld019F[k3];
            int k5 = 65 + 49 * _fld01A0[k3];
            int l5 = (l4 * (_fld01A5 - j4) + j5 * j4) / _fld01A5;
            int i6 = (i5 * (_fld01A5 - j4) + k5 * j4) / _fld01A5;
            super.castleSurface2mby._mth01B5(l5 + _fld019D[_fld01A1], i6 + _fld019E[_fld01A1], _fld01A1 + 39);
        }
        if(_fld01A8 > 0)
            super.castleSurface2mby._mth01B5(12, 2, 28 + (_fld01AD / 2 & 3));
        else
        if(_fld01A4 > 0 && _fld01AA != -1)
            super.castleSurface2mby._mth01B5(12, 2, 31 + _fld01AA);
        else
        if(_fld01AB == super.castleId && _fld01AE == 0 && _fld01A4 == 0)
            super.castleSurface2mby._mth01B5(12, 2, 38);
        if(_fld01AE == 1 && _fld01AB == super.castleId && ((GameApplet) (super.castleref)).mouseX > 93 && ((GameApplet) (super.castleref)).mouseX < 485 && ((GameApplet) (super.castleref)).mouseY > 41 && ((GameApplet) (super.castleref)).mouseY < 286)
        {
            int l1 = (((GameApplet) (super.castleref)).mouseX - 93) / 49;
            int i3 = (((GameApplet) (super.castleref)).mouseY - 41) / 49;
            int l3 = 117 + 49 * l1;
            int k4 = 65 + 49 * i3;
            super.castleSurface2mby._mth01BC(l3, k4, 23 + (_fld01AD / 6 & 3));
        }
        if(_fld01A6 != 0)
        {
            int i2 = _fld01A6;
            if(i2 < 0)
                i2 = 64 + i2;
            int j3 = 5 + (_fld01A7 % 2) * 38;
            int i4 = 65 + (_fld01A7 / 2) * 37;
            super.castleSurface2mby._mth01C8(j3 - i2 / 2, i4 - i2 / 2, 35 + i2, 36 + i2, 1 + _fld01A9, 256 - i2 * 4);
        }
        super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
        super.castleGameGraphics.setFont(_fld019A);
        super.castleGameGraphics.setColor(Color.white);
        if(_fld01AB == super.castleId && _fld01AE == 0)
            super.castleGameGraphics.drawString("Your turn - Choose a direction", 95, 20);
        else
        if(_fld01AB == super.castleId && _fld01AE == 1)
            super.castleGameGraphics.drawString("Teleporter! - Choose any location", 95, 20);
        else
            super.castleGameGraphics.drawString(super._fld03C1[_fld01AB] + "'s turn - Please wait", 95, 20);
        GameDialog.D8(super.castleGameGraphics, "Time-Remaining:" + _fld01AC, _fld019A, 497, 20);
        super.castleGameGraphics.drawString(_fld01B3, 95, 38);
    }

    public Dungeon()
    {
        _fld0199 = new Font("Helvetica", 1, 16);
        _fld019A = new Font("Helvetica", 1, 13);
        _fld019B = new Font("Helvetica", 1, 11);
        _fld019F = new int[7];
        _fld01A0 = new int[7];
        _fld01A5 = 20;
        _fld01AC = 60;
        _fld01AF = new int[8][5];
        _fld01B0 = new int[6];
        _fld01B1 = new int[6];
        _fld01B2 = new int[12];
        _fld01B3 = "";
    }

    Font _fld0199;
    Font _fld019A;
    Font _fld019B;
    int _fld019C[] = {
        0, 1, 2, 1
    };
    int _fld019D[] = {
        -6, 6, -18, -6, 6, -18
    };
    int _fld019E[] = {
        -18, -14, -14, 1, -3, -3
    };
    int _fld019F[];
    int _fld01A0[];
    int _fld01A1;
    int _fld01A2;
    int _fld01A3;
    int _fld01A4;
    int _fld01A5;
    int _fld01A6;
    int _fld01A7;
    int _fld01A8;
    int _fld01A9;
    int _fld01AA;
    int _fld01AB;
    int _fld01AC;
    int _fld01AD;
    int _fld01AE;
    int _fld01AF[][];
    int _fld01B0[];
    int _fld01B1[];
    int _fld01B2[];
    String _fld01B3;
}

