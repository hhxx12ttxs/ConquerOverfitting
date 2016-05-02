// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   cryptgame.java

import jagex.*;
import java.awt.*;
import java.io.IOException;

public class Crypt extends Game
{

    public boolean _mth0195()
    {
        if(_fld0430 != 0)
            return false;
        if(_fld0438 != 0)
            return false;
        return _fld0455 == 0;
    }

    public void _mth01A2(int i, int k)
        throws IOException
    {
        if(i == 255)
        {
            _fld0423 = super.castleStream.getShort(super.castlePdata, 1);
            _fld0424 = super.castleStream.getShort(super.castlePdata, 3);
            _fld0466 = super.castleStream.getShort(super.castlePdata, 5);
            _fld0443 = 0;
            if(_fld0423 != _fld0426 || _fld0443 != _fld0427)
            {
                _fld0426 = _fld0423;
                _fld0427 = _fld0443;
                _fld042E = _fld042B[_fld0423];
                _fld042F = _fld042C[_fld0423];
                _mth01D6();
            }
            return;
        }
        if(i == 254)
        {
            _fld0443 = 0;
            _fld0430 = _fld0431;
            _fld0438 = 40;
            _fld0436 = super.castleStream.byte2int(super.castlePdata[1]);
            _fld0432 = super.castleStream.byte2int(super.castlePdata[2]);
            _fld0433 = super.castleStream.byte2int(super.castlePdata[3]);
            _fld0434 = super.castleStream.byte2int(super.castlePdata[4]);
            _fld0435 = super.castleStream.byte2int(super.castlePdata[5]);
            _fld0437 = super.castleStream.byte2int(super.castlePdata[6]);
            _fld042B[_fld0436] = _fld042E = _fld0432;
            _fld042C[_fld0436] = _fld042F = _fld0433;
            _mth01D6();
            return;
        }
        if(i == 253)
        {
            _fld0443 = 0;
            _fld0430 = -_fld0431;
            _fld0438 = 40;
            _fld0436 = super.castleStream.byte2int(super.castlePdata[1]);
            _fld0432 = super.castleStream.byte2int(super.castlePdata[2]);
            _fld0433 = super.castleStream.byte2int(super.castlePdata[3]);
            _fld0434 = super.castleStream.byte2int(super.castlePdata[4]);
            _fld0435 = super.castleStream.byte2int(super.castlePdata[5]);
            _fld0437 = super.castleStream.byte2int(super.castlePdata[6]);
            _fld042B[_fld0436] = _fld042E = _fld0432;
            _fld042C[_fld0436] = _fld042F = _fld0433;
            _mth01D6();
            return;
        }
        if(i == 252)
        {
            _fld0423 = super.castleStream.getShort(super.castlePdata, 1);
            _fld0424 = super.castleStream.getShort(super.castlePdata, 3);
            _fld0443 = 1;
            _fld0447 = super.castleStream.getShort(super.castlePdata, 5);
            _fld0448 = super.castleStream.getShort(super.castlePdata, 7);
            _fld0466 = super.castleStream.getShort(super.castlePdata, 9);
            if(_fld0423 != _fld0426 || _fld0443 != _fld0427)
            {
                _fld0426 = _fld0423;
                _fld0427 = _fld0443;
                _fld042E = _fld042B[_fld0423];
                _fld042F = _fld042C[_fld0423];
                _mth01D6();
            }
            boolean flag = false;
            boolean flag1 = false;
            _fld044B = 0;
            int l2 = -1;
            int k3 = -1;
            int i4 = _fld0429[_fld0447][_fld0448];
            int k4 = _fld0445[i4];
            for(int l4 = 0; l4 < k4; l4++)
            {
                int i5 = _fld0444[i4][l4];
                if(i5 == 15 || i5 == 16)
                    flag = true;
                if(i5 == 21)
                    flag1 = true;
                if(_fld043C[i5] == 1 && _fld043D[i5] > k3)
                {
                    l2 = l4;
                    k3 = _fld043D[i5];
                }
            }

            if(l2 != -1)
            {
                if(!_fld042A[_fld0447][_fld0448])
                {
                    _fld044D[_fld044B] = l2 * 256;
                    _fld044E[_fld044B++] = "Talk to " + _fld043A[_fld0444[i4][l2]];
                }
                _fld044D[_fld044B] = 1;
                _fld044E[_fld044B++] = "Attack!";
                _fld044D[_fld044B] = 2;
                _fld044E[_fld044B++] = "Retreat";
                for(int j5 = 0; j5 < 7; j5++)
                {
                    for(int i6 = 1; i6 < 5; i6++)
                    {
                        if(_fld044F[j5][i6] == 7 && flag)
                        {
                            _fld044D[_fld044B] = 7;
                            _fld044E[_fld044B++] = "Use Cross";
                        }
                        if(_fld044F[j5][i6] == 9)
                        {
                            _fld044D[_fld044B] = 6;
                            _fld044E[_fld044B++] = "Drink potion & Attack";
                        }
                    }

                }

            } else
            {
                for(int k5 = 0; k5 < k4; k5++)
                {
                    int j6 = _fld0444[i4][k5];
                    if(j6 == 20)
                    {
                        _fld044D[_fld044B] = 5;
                        _fld044E[_fld044B++] = "Open Sarcophagus";
                    }
                    if(_fld043C[j6] == 0)
                    {
                        _fld044D[_fld044B] = 4 + k5 * 256;
                        _fld044E[_fld044B++] = "Take " + _fld043A[j6];
                    }
                }

                _fld044D[_fld044B] = 3;
                _fld044E[_fld044B++] = "End Turn";
            }
            for(int l5 = 0; l5 < 7; l5++)
            {
                for(int k6 = 1; k6 < 5; k6++)
                    if(_fld044F[l5][k6] == 4 && flag1)
                    {
                        _fld044D[_fld044B] = 8;
                        _fld044E[_fld044B++] = "Cut web with sword";
                    }

            }

            return;
        }
        if(i == 251)
        {
            super.castleref.c(7, 63);
            int l = super.castleStream.getShort(super.castlePdata, 1);
            int l1 = super.castleStream.getShort(super.castlePdata, 3);
            int i3 = super.castleStream.getShort(super.castlePdata, 5);
            int l3 = super.castleStream.getShort(super.castlePdata, 7);
            _fld0429[l][l1] = i3;
            _fld0445[i3] = l3;
            if(i3 + 1 > _fld0446)
                _fld0446 = i3 + 1;
            for(int j4 = 0; j4 < l3; j4++)
                _fld0444[i3][j4] = super.castleStream.getShort(super.castlePdata, 9 + j4 * 2);

            _mth01D6();
            return;
        }
        if(i == 250)
        {
            for(int i1 = 0; i1 < 7; i1++)
            {
                int i2 = 0;
                for(int j3 = 0; j3 < 5; j3++)
                {
                    _fld044F[i1][j3] = super.castlePdata[1 + i1 * 5 + j3];
                    if(j3 == 0 && _fld044F[i1][j3] != -1)
                        i2 = _fld043E[_fld044F[i1][j3]];
                    else
                    if(_fld044F[i1][j3] != -1)
                        i2 -= _fld043E[_fld044F[i1][j3]];
                }

                _fld0451[i1] = i2;
            }

            _fld0452 = -1;
            return;
        }
        if(i == 249)
        {
            _fld0456 = super.castleStream.getShort(super.castlePdata, 1);
            int j1 = super.castleStream.getShort(super.castlePdata, 3);
            int j2 = super.castleStream.getShort(super.castlePdata, 5);
            if(_fld0456 == super.castleId)
                _fld042A[j1][j2] = true;
            _fld0457 = super.castleStream.getShort(super.castlePdata, 7);
            _fld045A = super.castleStream.getShort(super.castlePdata, 9);
            _fld045B = super.castleStream.getShort(super.castlePdata, 11);
            _fld0458 = super.castlePdata[13];
            _fld0459 = super.castlePdata[14];
            _fld045C = super.castleStream.getShort(super.castlePdata, 15);
            _fld045E = super.castleStream.getShort(super.castlePdata, 17);
            _fld045F = super.castlePdata[19];
            _fld0462 = super.castlePdata[20];
            _fld0463 = super.castlePdata[21];
            _fld0464 = super.castlePdata[22];
            _fld0465 = super.castlePdata[23];
            _fld0460 = super.castlePdata[24];
            if(_fld0460 == 1)
                _fld0461 = super.castleStream.getShort(super.castlePdata, 25);
            _fld0455 = 200;
            super.castleref.c(0, 63);
            return;
        }
        if(i == 248)
        {
            int k1 = super.castleStream.byte2int(super.castlePdata[1]);
            int k2 = super.castleStream.byte2int(super.castlePdata[2]);
            _fld0428[k1][k2] = 14;
            _fld0429[k1][k2] = -2;
            _mth01D6();
        }
    }

    public void _mth0199()
    {
        super._fld03C3 = 4;
        super.castleSurface2mby._mth01CF();
        super.castleSurface2mby._mth01B7("crypt/back.jpg", 0, false);
        super.castleSurface2mby._mth01D4("crypt/bits.gif", 1, true, 18, 163, 113);
        super.castleSurface2mby._mth01D4("crypt/map.gif", 19, true, 14, 6, 6);
        super.castleSurface2mby._mth01D4("crypt/compass.gif", 33, true, 10, 27, 27);
        super.castleSurface2mby._mth01D4("crypt/knight.gif", 43, true, 6, 31, 34);
        super.castleSurface2mby._mth01D4("crypt/all.gif", 49, true, _fld0439, 50, 75);
        super.castleSurface2mby._mth01D4("crypt/allmini.gif", 49 + _fld0439, true, _fld0439 - 2, 30, 44);
        _mth01D5();
    }

    public void _mth01D5()
    {
        _fld043A = new String[_fld0439];
        _fld043B = new String[_fld0439];
        _fld043C = new int[_fld0439];
        _fld043D = new int[_fld0439];
        _fld043E = new int[_fld0439];
        _fld043F = new int[_fld0439];
        _fld0440 = new int[_fld0439];
        _fld0441 = new int[_fld0439];
        _fld0442 = new int[_fld0439];
        int i = 0;
        try
        {
            Stream w1 = new Stream("crypt/cards.dat");
            for(i = 0; i < _fld0439; i++)
            {
                w1.skipToEqual();
                _fld043A[i] = w1.getPropStr();
                _fld043C[i] = w1.getPropInt();
                _fld043D[i] = w1.getPropInt();
                _fld043E[i] = w1.getPropInt();
                _fld043F[i] = w1.getPropInt();
                _fld0440[i] = w1.getPropInt();
                _fld0441[i] = w1.getPropInt();
                _fld0442[i] = w1.getPropInt();
                _fld043B[i] = w1.getPropStr();
                w1.getPropInt();
            }

            w1.closeStream();
            return;
        }
        catch(IOException _ex)
        {
            System.out.println("Fatal error loading feature-list! n=" + i);
        }
    }

    public void _mth0191()
    {
        super.castleSurface2mby._fld0414 = 290;
        super.castleSurface2mby._fld0413 = 0;
        for(int i = 0; i < 100; i++)
        {
            for(int k = 0; k < 100; k++)
            {
                _fld0428[i][k] = -1;
                _fld0429[i][k] = -1;
                _fld042A[i][k] = false;
            }

        }

        _fld0428[50][50] = 0;
        for(int l = 0; l < 6; l++)
        {
            _fld042B[l] = 50;
            _fld042C[l] = 50;
            _fld042D[l] = 43;
        }

        for(int i1 = 0; i1 < 7; i1++)
        {
            for(int j1 = 0; j1 < 5; j1++)
                _fld044F[i1][j1] = -1;

        }

        _fld042E = 50;
        _fld042F = 50;
        _fld0443 = 0;
        _fld0466 = 80;
        _fld0424 = 60;
        _fld0446 = 0;
        for(int k1 = 0; k1 < 100; k1++)
            _fld0445[k1] = 0;

        _mth01D6();
    }

    public void _mth0197()
    {
        _fld0425++;
        if(_fld0455 > 0)
        {
            _fld0455--;
            if(_fld0455 == 0)
                super.castleref.Y();
        }
        if(_fld0430 > 0)
        {
            _fld0430--;
            if(_fld0430 == 0)
            {
                _fld042B[_fld0436] = _fld0434;
                _fld042C[_fld0436] = _fld0435;
            }
        } else
        if(_fld0430 < 0)
        {
            _fld0430++;
            if(_fld0430 == -_fld0431 / 2)
                super.castleref.c(17, 63);
        }
        if(_fld0430 == (_fld0431 * 2) / 3 || _fld0430 == -(_fld0431 * 2) / 3)
        {
            _fld0428[_fld0434][_fld0435] = _fld0437;
            _mth01D6();
        }
        if(_fld0430 == 0 && _fld0438 > 0)
            _fld0438 = 0;
        if(((GameApplet) (super.castleref)).lastMouseButtonDown == 1 && _fld0452 == -1)
        {
            for(int i = 0; i < 7; i++)
            {
                for(int i1 = 1; i1 < 5; i1++)
                    if(((GameApplet) (super.castleref)).mouseY - 6 <= 29 + 10 * i1 && ((GameApplet) (super.castleref)).mouseY - 6 >= 20 + 10 * i1 && ((GameApplet) (super.castleref)).mouseX - 6 >= 5 + 70 * i && ((GameApplet) (super.castleref)).mouseX - 6 <= 74 + 70 * i && _fld044F[i][i1] != -1)
                    {
                        _fld0452 = i;
                        _fld0453 = i1;
                        _fld0454 = _fld043E[_fld044F[i][i1]];
                    }

            }

        } else
        if(((GameApplet) (super.castleref)).lastMouseButtonDown == 1 && _fld0452 != -1)
        {
            for(int k = 0; k < 7; k++)
            {
                for(int j1 = 1; j1 < 5; j1++)
                    if(((GameApplet) (super.castleref)).mouseY - 6 < 29 + 10 * j1 && ((GameApplet) (super.castleref)).mouseY - 6 >= 20 + 10 * j1 && ((GameApplet) (super.castleref)).mouseX - 6 >= 5 + 70 * k && ((GameApplet) (super.castleref)).mouseX - 6 < 74 + 70 * k)
                    {
                        int k1 = _fld044F[k][j1];
                        if(k1 != -1 && j1 > 0 && _fld0452 != k && _fld0452 != -1 && _fld043E[k1] + _fld0451[k] >= _fld0454 && _fld043E[k1] <= _fld0454 + _fld0451[_fld0452])
                        {
                            super.castleStream.newPacket(253);
                            super.castleStream.putUnsignedShort(_fld0452);
                            super.castleStream.putUnsignedShort(_fld0453);
                            super.castleStream.putUnsignedShort(k);
                            super.castleStream.putUnsignedShort(j1);
                            super.castleStream.sendPacket();
                            _fld0452 = -1;
                        } else
                        if(k1 == -1 && j1 > 0 && _fld0452 != k && _fld0452 != -1 && _fld044F[k][j1 - 1] != -1 && _fld0451[k] >= _fld0454)
                        {
                            super.castleStream.newPacket(253);
                            super.castleStream.putUnsignedShort(_fld0452);
                            super.castleStream.putUnsignedShort(_fld0453);
                            super.castleStream.putUnsignedShort(k);
                            super.castleStream.putUnsignedShort(j1);
                            super.castleStream.sendPacket();
                            _fld0452 = -1;
                        }
                    }

            }

            if(((GameApplet) (super.castleref)).mouseX - 6 >= 5 && ((GameApplet) (super.castleref)).mouseX - 6 <= 74 && ((GameApplet) (super.castleref)).mouseY - 6 >= 70 && ((GameApplet) (super.castleref)).mouseY - 6 <= 79 && _fld0452 != -1)
            {
                super.castleStream.newPacket(253);
                super.castleStream.putUnsignedShort(_fld0452);
                super.castleStream.putUnsignedShort(_fld0453);
                super.castleStream.putUnsignedShort(255);
                super.castleStream.putUnsignedShort(255);
                super.castleStream.sendPacket();
                _fld0452 = -1;
            }
            _fld0452 = -1;
        }
        if(_fld0423 == super.castleId && ((GameApplet) (super.castleref)).lastMouseButtonDown == 1 && _fld0455 == 0 && _fld0424 > 0)
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 433 && ((GameApplet) (super.castleref)).mouseX - 6 < 460 && ((GameApplet) (super.castleref)).mouseY - 6 >= 220 && ((GameApplet) (super.castleref)).mouseY - 6 < 247)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(0);
                super.castleStream.sendPacket();
            } else
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 468 && ((GameApplet) (super.castleref)).mouseX - 6 < 495 && ((GameApplet) (super.castleref)).mouseY - 6 >= 228 && ((GameApplet) (super.castleref)).mouseY - 6 <= 255)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(1);
                super.castleStream.sendPacket();
            } else
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 425 && ((GameApplet) (super.castleref)).mouseX - 6 < 452 && ((GameApplet) (super.castleref)).mouseY - 6 >= 255 && ((GameApplet) (super.castleref)).mouseY - 6 <= 282)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(3);
                super.castleStream.sendPacket();
            } else
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 460 && ((GameApplet) (super.castleref)).mouseX - 6 < 487 && ((GameApplet) (super.castleref)).mouseY - 6 >= 263 && ((GameApplet) (super.castleref)).mouseY - 6 < 290)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(2);
                super.castleStream.sendPacket();
            } else
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 453 && ((GameApplet) (super.castleref)).mouseX - 6 < 466 && ((GameApplet) (super.castleref)).mouseY - 6 >= 248 && ((GameApplet) (super.castleref)).mouseY - 6 < 261)
            {
                super.castleStream.newPacket(255);
                super.castleStream.putByte(4);
                super.castleStream.sendPacket();
            }
        _fld044C = -1;
        if(_fld0443 == 1 && _fld0423 == super.castleId && _fld0455 == 0)
        {
            for(int l = 0; l < _fld044B; l++)
                if(((GameApplet) (super.castleref)).mouseX > 16 && ((GameApplet) (super.castleref)).mouseX < 126 && ((GameApplet) (super.castleref)).mouseY > 106 + l * 12 && ((GameApplet) (super.castleref)).mouseY < 118 + l * 12)
                {
                    _fld044C = l;
                    if(((GameApplet) (super.castleref)).lastMouseButtonDown == 1)
                    {
                        super.castleStream.newPacket(254);
                        super.castleStream.putUnsignedShort(_fld044D[_fld044C]);
                        super.castleStream.sendPacket();
                    }
                }

        }
    }

    public void _mth01D6()
    {
        _fld0452 = -1;
        super.castleSurface2mby._mth01B5(0, 0, 0);
        for(int i = 2; i >= -2; i--)
        {
            for(int k = -2; k <= 2; k++)
            {
                int i1 = _fld0428[i + _fld042E][k + _fld042F];
                if(i1 != -1)
                {
                    int l1 = 170 + i * 113 + k * 66;
                    int k2 = (100 - i * 33) + k * 57;
                    for(int j3 = 0; j3 < _fld041D[i1].length; j3++)
                    {
                        super.castleSurface2mby._mth01B5(l1, k2, _fld041D[i1][j3] + 1);
                        if(i1 == 0)
                            super.castleSurface2mby._mth01B5(l1, k2 - 14, 18);
                    }

                }
            }

        }

        for(int l = 2; l >= -2; l--)
        {
            for(int j1 = -2; j1 <= 2; j1++)
            {
                int i2 = _fld0429[l + _fld042E][j1 + _fld042F];
                if(i2 >= 0)
                {
                    int l2 = 170 + l * 113 + j1 * 66;
                    int k3 = (100 - l * 33) + j1 * 57;
                    for(int l3 = 0; l3 < _fld0445[i2]; l3++)
                        if(_fld0444[i2][l3] < 21)
                            super.castleSurface2mby._mth01B5(l2 + _fld0420[l3], k3 + _fld0421[l3], 49 + _fld0439 + _fld0444[i2][l3]);
                        else
                        if(_fld0444[i2][l3] == 21)
                            super.castleSurface2mby._mth01B5(l2, k3 - 5, 16);

                }
            }

        }

        if(_fld0443 == 0 || _fld0423 != super.castleId)
        {
            super.castleSurface2mby._mth01B3(5, 220, 75, 70, _fld041C, 160, false);
            for(int k1 = -5; k1 <= 5; k1++)
            {
                for(int j2 = -5; j2 <= 5; j2++)
                    if(_fld0428[k1 + _fld042E][j2 + _fld042F] != -1)
                    {
                        int i3 = _fld0428[k1 + _fld042E][j2 + _fld042F];
                        if(i3 == 13)
                            i3 = 12;
                        if(k1 == 0 && j2 == 0)
                            i3 = 13;
                        if(i3 != 14)
                            super.castleSurface2mby._mth01B5(k1 * 6 + 39, j2 * 6 + 252, i3 + 19);
                    }

            }

        }
        super.castleSurface2mby._mth01B1(99);
    }

    public void _mth01D7(int i)
    {
        if(super._fld03C1[i] != null)
        {
            int k = _fld042B[i] - _fld042E;
            int l = _fld042C[i] - _fld042F;
            if(k >= -2 && k <= 2 && l >= -2 && l <= 2)
            {
                int i1 = 170 + k * 113 + l * 66;
                int j1 = (100 - k * 33) + l * 57;
                if(_fld0430 != 0 && _fld0436 == i)
                {
                    int k1 = _fld0434 - _fld042E;
                    int l1 = _fld0435 - _fld042F;
                    if(l1 > 0 || k1 > 0)
                        _fld042D[i] = 46;
                    else
                        _fld042D[i] = 43;
                    _fld042D[i] = _fld042D[i] + _fld0422[_fld0425 / 5 & 3];
                    int i2 = 170 + k1 * 113 + l1 * 66;
                    int j2 = (100 - k1 * 33) + l1 * 57;
                    if(_fld0430 > 0)
                    {
                        i1 = (i1 * _fld0430 + i2 * (_fld0431 - _fld0430)) / _fld0431;
                        j1 = (j1 * _fld0430 + j2 * (_fld0431 - _fld0430)) / _fld0431;
                    } else
                    if(_fld0430 < -_fld0431 / 2)
                    {
                        i1 = (i1 * -_fld0430 + i2 * (_fld0431 + _fld0430)) / _fld0431;
                        j1 = (j1 * -_fld0430 + j2 * (_fld0431 + _fld0430)) / _fld0431;
                    } else
                    {
                        i1 = (i2 * -_fld0430 + i1 * (_fld0431 + _fld0430)) / _fld0431;
                        j1 = (j2 * -_fld0430 + j1 * (_fld0431 + _fld0430)) / _fld0431;
                    }
                }
                if(i == super.castleId)
                {
                    super.castleSurface2mby._mth01B5(i1 + 62, j1 + 35, _fld042D[i]);
                    return;
                }
                super.castleSurface2mby._mth01B5(i1 + 70, j1 + 30, _fld042D[i]);
            }
        }
    }

    public void _mth019D()
    {
        super.castleSurface2mby._mth01B5(0, 0, 99);
        for(int i = 0; i < super._fld03BF; i++)
            if(i != super.castleId)
                _mth01D7(i);

        _mth01D7(super.castleId);
        for(int k = 1; k >= -1; k--)
        {
            for(int l = -1; l <= 1; l++)
            {
                int j1 = 170 + k * 113 + l * 66;
                int j2 = (100 - k * 33) + l * 57;
                int k3 = _fld0428[k + _fld042E][l + _fld042F];
                if(k3 == 0 || k3 == 12)
                    super.castleSurface2mby._mth01B5(j1, j2 - 5, 14);
                if(k3 == 13)
                    super.castleSurface2mby._mth01B5(j1, j2 - 8, 15);
            }

        }

        for(int i1 = 0; i1 < 7; i1++)
        {
            for(int k1 = 0; k1 < 5; k1++)
            {
                int k2 = _fld044F[i1][k1];
                if(_fld0452 == i1 && _fld0453 == k1)
                    super.castleSurface2mby._mth01B3(5 + 70 * i1, 20 + 10 * k1, 69, 9, Color.green, 160, false);
                if(k2 != -1 && k1 > 0 && _fld0452 != i1 && _fld0452 != -1 && _fld043E[k2] + _fld0451[i1] >= _fld0454 && _fld043E[k2] <= _fld0454 + _fld0451[_fld0452])
                    super.castleSurface2mby._mth01B3(5 + 70 * i1, 20 + 10 * k1, 69, 9, Color.red, 160, false);
                else
                if(k2 == -1 && k1 > 0 && _fld0452 != i1 && _fld0452 != -1 && _fld044F[i1][k1 - 1] != -1 && _fld0451[i1] >= _fld0454)
                    super.castleSurface2mby._mth01B3(5 + 70 * i1, 20 + 10 * k1, 69, 9, Color.red, 160, false);
                else
                if(k2 == -1)
                    super.castleSurface2mby._mth01B3(5 + 70 * i1, 20 + 10 * k1, 69, 9, _fld041C, 80, false);
                else
                if(k1 == 0)
                    super.castleSurface2mby._mth01B3(5 + 70 * i1, 20 + 10 * k1, 69, 9, _fld041C, 200, false);
                else
                    super.castleSurface2mby._mth01B3(5 + 70 * i1, 20 + 10 * k1, 69, 9, _fld041C, 160, false);
            }

        }

        if(_fld0452 != -1)
            super.castleSurface2mby._mth01B3(5, 70, 69, 9, Color.red, 160, false);
        if(_fld0443 == 0 && _fld0455 == 0 && _fld0423 == super.castleId)
        {
            super.castleSurface2mby._mth01B3(425, 220, 70, 70, _fld041C, 160, false);
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 453 && ((GameApplet) (super.castleref)).mouseX - 6 < 466 && ((GameApplet) (super.castleref)).mouseY - 6 >= 248 && ((GameApplet) (super.castleref)).mouseY - 6 < 261)
                super.castleSurface2mby._mth01B5(446, 241, 42);
            else
                super.castleSurface2mby._mth01B5(446, 241, 37);
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 433 && ((GameApplet) (super.castleref)).mouseX - 6 < 460 && ((GameApplet) (super.castleref)).mouseY - 6 >= 220 && ((GameApplet) (super.castleref)).mouseY - 6 < 247)
                super.castleSurface2mby._mth01B5(433, 220, 38);
            else
                super.castleSurface2mby._mth01B5(433, 220, 33);
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 468 && ((GameApplet) (super.castleref)).mouseX - 6 < 495 && ((GameApplet) (super.castleref)).mouseY - 6 >= 228 && ((GameApplet) (super.castleref)).mouseY - 6 <= 255)
                super.castleSurface2mby._mth01B5(468, 228, 39);
            else
                super.castleSurface2mby._mth01B5(468, 228, 34);
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 425 && ((GameApplet) (super.castleref)).mouseX - 6 < 452 && ((GameApplet) (super.castleref)).mouseY - 6 >= 255 && ((GameApplet) (super.castleref)).mouseY - 6 <= 282)
                super.castleSurface2mby._mth01B5(425, 255, 41);
            else
                super.castleSurface2mby._mth01B5(425, 255, 36);
            if(((GameApplet) (super.castleref)).mouseX - 6 >= 460 && ((GameApplet) (super.castleref)).mouseX - 6 < 487 && ((GameApplet) (super.castleref)).mouseY - 6 >= 263 && ((GameApplet) (super.castleref)).mouseY - 6 < 290)
                super.castleSurface2mby._mth01B5(460, 263, 40);
            else
                super.castleSurface2mby._mth01B5(460, 263, 35);
        } else
        if(_fld0443 == 1 && _fld0455 == 0)
        {
            int l1 = _fld0429[_fld0447][_fld0448];
            int l2 = _fld0445[l1];
            for(int l3 = 0; l3 < 6; l3++)
            {
                super.castleSurface2mby._mth01CA(_fld0449[l3], _fld044A[l3], 107, 78, _fld041E, _fld041F, false);
                super.castleSurface2mby._mth01B8(_fld0449[l3], _fld044A[l3], 107, 78, Color.black);
                if(l3 < l2)
                    super.castleSurface2mby._mth01B5((_fld0449[l3] + 107) - 53, _fld044A[l3] + 1, 49 + _fld0444[l1][l3]);
            }

            super.castleSurface2mby._mth01B3(152, 83, 319, 28, _fld041C, 160, false);
            if(_fld0423 == super.castleId && _fld0423 == super.castleId)
                super.castleSurface2mby._mth01B3(8, 82, 122, 198, _fld041C, 160, false);
        } else
        if(_fld0455 > 0)
        {
            super.castleSurface2mby._mth01B3(152, 83, 319, 28, _fld041C, 160, false);
            super.castleSurface2mby._mth01B3(141, 119, 347, 163, _fld041C, 160, false);
        }
        super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
        super.castleGameGraphics.setColor(Color.white);
        super.castleGameGraphics.setFont(_fld0417);
        for(int i2 = 0; i2 < 7; i2++)
        {
            for(int i3 = 0; i3 < 5; i3++)
            {
                int i4 = _fld044F[i2][i3];
                if(i4 != -1)
                {
                    String s3 = _fld043A[i4];
                    String s5 = String.valueOf(_fld043D[i4]);
                    if(_fld043D[i4] == 0)
                        s5 = "?";
                    if(_fld043C[i4] == 0)
                        s3 = s3 + "(" + s5 + ")";
                    else
                    if(_fld043C[i4] == 1)
                        s3 = s3 + "(" + _fld0441[i4] + "+" + _fld0442[i4] + ")";
                    super.castleGameGraphics.drawString(s3, 12 + 70 * i2, 34 + 10 * i3);
                    continue;
                }
                if(_fld0451[i2] == 0)
                    continue;
                super.castleGameGraphics.drawString(_fld0451[i2] + " kg left", 12 + 70 * i2, 34 + 10 * i3);
                break;
            }

        }

        if(_fld0452 != -1)
            super.castleGameGraphics.drawString("Drop object", 12, 84);
        if(_fld0443 == 1 && _fld0455 == 0)
        {
            super.castleGameGraphics.setColor(Color.black);
            GameDialog.drawstringCenter(super.castleGameGraphics, "Room Contents", _fld041B, 317, 105);
            if(_fld0423 == super.castleId)
            {
                super.castleGameGraphics.setColor(Color.white);
                super.castleGameGraphics.setFont(_fld0419);
                super.castleGameGraphics.drawString("Choose an option", 16, 101);
            }
            int j3 = _fld0429[_fld0447][_fld0448];
            int j4 = _fld0445[j3];
            for(int l4 = 0; l4 < j4; l4++)
            {
                int i5 = _fld0444[j3][l4];
                super.castleGameGraphics.setFont(_fld0418);
                super.castleGameGraphics.setColor(Color.black);
                super.castleGameGraphics.drawString(_fld043A[i5], _fld0449[l4] + 3 + 6, _fld044A[l4] + 15 + 6);
                super.castleGameGraphics.setFont(_fld0417);
                super.castleGameGraphics.setColor(Color.black);
                if(_fld043C[i5] == 1)
                {
                    super.castleGameGraphics.drawString("Friendly:" + _fld043F[i5] + "%", _fld0449[l4] + 2 + 6, _fld044A[l4] + 55 + 6);
                    super.castleGameGraphics.drawString("Hostile:" + _fld0440[i5] + "%", _fld0449[l4] + 2 + 6, _fld044A[l4] + 65 + 6);
                    super.castleGameGraphics.drawString("Combat:" + _fld0441[i5] + "+" + _fld0442[i5], _fld0449[l4] + 2 + 6, _fld044A[l4] + 75 + 6);
                } else
                if(_fld043C[i5] == 0)
                {
                    String s6 = String.valueOf(_fld043D[i5]);
                    String s8 = _fld043B[i5];
                    if(s6.equalsIgnoreCase("0"))
                        s6 = "?";
                    if(s8 != "")
                    {
                        super.castleGameGraphics.drawString("Weight:" + _fld043E[i5] + "kg", _fld0449[l4] + 2 + 6, _fld044A[l4] + 55 + 6);
                        super.castleGameGraphics.drawString("Value:" + s6, _fld0449[l4] + 2 + 6, _fld044A[l4] + 65 + 6);
                        super.castleGameGraphics.drawString(s8, _fld0449[l4] + 2 + 6, _fld044A[l4] + 75 + 6);
                    } else
                    {
                        super.castleGameGraphics.drawString("Weight: " + _fld043E[i5] + "kg", _fld0449[l4] + 2 + 6, _fld044A[l4] + 65 + 6);
                        super.castleGameGraphics.drawString("Value: " + s6, _fld0449[l4] + 2 + 6, _fld044A[l4] + 75 + 6);
                    }
                } else
                if(_fld043C[i5] == 2)
                {
                    String s7 = _fld043B[i5];
                    if(s7 != "")
                        super.castleGameGraphics.drawString(s7, _fld0449[l4] + 2 + 6, _fld044A[l4] + 75 + 6);
                }
            }

            if(_fld0423 == super.castleId)
            {
                super.castleGameGraphics.setFont(_fld0418);
                super.castleGameGraphics.setColor(Color.white);
                for(int j5 = 0; j5 < _fld044B; j5++)
                {
                    if(_fld044C == j5)
                        super.castleGameGraphics.setColor(Color.red);
                    else
                        super.castleGameGraphics.setColor(Color.white);
                    super.castleGameGraphics.drawString(_fld044E[j5], 16, 116 + j5 * 12);
                }

            }
        } else
        if(_fld0455 > 0)
        {
            super.castleGameGraphics.setColor(Color.black);
            GameDialog.drawstringCenter(super.castleGameGraphics, super._fld03C1[_fld0456] + " is engaged in combat!", _fld041A, 317, 105);
            String s1 = ".Fighting Enemy.";
            if(_fld0455 > 100)
            {
                for(int k4 = 0; k4 < (200 - _fld0455) / 10; k4++)
                    s1 = "." + s1 + ".";

            }
            GameDialog.drawstringCenter(super.castleGameGraphics, s1, _fld041A, 320, 140);
            GameDialog.drawstringCenter(super.castleGameGraphics, super._fld03C1[_fld0456] + "'s total strength is: " + _fld045E, _fld0419, 320, 160);
            String s2 = "";
            if(_fld0464 != -1)
                s2 = s2 + _fld043A[_fld0464] + " strength:" + _fld0458;
            if(_fld0465 != -1)
                s2 = s2 + ", " + _fld043A[_fld0465] + " strength:" + _fld0459;
            if(_fld045C != 0)
                s2 = s2 + ", ranged combat:" + _fld045C;
            GameDialog.drawstringCenter(super.castleGameGraphics, "(" + s2 + ")", _fld0418, 320, 175);
            GameDialog.drawstringCenter(super.castleGameGraphics, "Opponents total strength is: " + _fld045B, _fld0419, 320, 195);
            String s4 = "";
            if(_fld0462 != -1)
                s4 = s4 + _fld043A[_fld0462] + " strength:" + _fld0441[_fld0462];
            if(_fld0463 != -1)
                s4 = s4 + " and " + _fld043A[_fld0463] + " strength:" + _fld0441[_fld0463];
            if(_fld045A != 0)
                s4 = s4 + " and ranged combat:" + _fld045A;
            GameDialog.drawstringCenter(super.castleGameGraphics, "(" + s4 + ")", _fld0418, 320, 210);
            if(_fld045F == 4)
                GameDialog.drawstringCenter(super.castleGameGraphics, "Bonus of +1 for suprise & +3 for potion", _fld0419, 320, 230);
            else
            if(_fld045F == 3)
                GameDialog.drawstringCenter(super.castleGameGraphics, "Bonus +3 for strength potion", _fld0419, 320, 230);
            else
            if(_fld045F > 0)
                GameDialog.drawstringCenter(super.castleGameGraphics, "Bonus of +1 for suprise attack", _fld0419, 320, 230);
            else
            if(_fld045F < 0)
                GameDialog.drawstringCenter(super.castleGameGraphics, "Penalty of -1 for suprise attack", _fld0419, 320, 230);
            if(_fld0455 < 120 && !_fld0467)
                super.castleref.Y();
            if(_fld0455 < 100)
            {
                if(_fld0460 == 0)
                    GameDialog.drawstringCenter(super.castleGameGraphics, super._fld03C1[_fld0456] + " has won the battle!", _fld041A, 320, 260);
                else
                if(_fld0460 == 1)
                {
                    if(!_fld0467)
                    {
                        super.castleref.Y();
                        super.castleref.c(13, 63);
                        _fld0467 = true;
                    }
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Enemy has killed " + super._fld03C1[_fld0456] + "'s " + _fld043A[_fld0461], _fld041A, 320, 260);
                } else
                {
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Battle drawn - Neither side won", _fld041A, 320, 260);
                }
            } else
            {
                _fld0467 = false;
            }
        }
        super.castleGameGraphics.setFont(_fld0419);
        super.castleGameGraphics.setColor(Color.white);
        if(_fld0423 == -1)
            super.castleGameGraphics.drawString("Please wait...", 10, 20);
        else
        if(_fld0423 == super.castleId && _fld0443 == 0)
            super.castleGameGraphics.drawString("Your turn - Click on compass to move!", 10, 20);
        else
        if(_fld0423 == super.castleId && _fld0443 == 1)
            super.castleGameGraphics.drawString("Your turn - Choose an option", 10, 20);
        else
            super.castleGameGraphics.drawString(super._fld03C1[_fld0423] + "'s turn - Please wait", 10, 20);
        GameDialog.D8(super.castleGameGraphics, "Tiles-Left:" + _fld0466 + "    Time-Remaining:" + _fld0424, _fld0419, 502, 20);
    }

    public Crypt()
    {
        _fld0417 = new Font("Helvetica", 1, 9);
        _fld0418 = new Font("Helvetica", 1, 11);
        _fld0419 = new Font("Helvetica", 1, 13);
        _fld041A = new Font("Helvetica", 1, 16);
        _fld041B = new Font("Helvetica", 1, 25);
        _fld041C = new Color(150, 150, 150);
        _fld041E = new Color(110, 110, 110);
        _fld041F = new Color(165, 165, 165);
        _fld0424 = 30;
        _fld0426 = -1;
        _fld0427 = -1;
        _fld0428 = new int[100][100];
        _fld0429 = new int[100][100];
        _fld042A = new boolean[100][100];
        _fld042B = new int[6];
        _fld042C = new int[6];
        _fld042D = new int[6];
        _fld042E = 50;
        _fld042F = 50;
        _fld0431 = 60;
        _fld0439 = 23;
        _fld0444 = new int[100][6];
        _fld0445 = new int[100];
        _fld044C = -1;
        _fld044D = new int[20];
        _fld044E = new String[20];
        _fld044F = new int[7][5];
        _fld0451 = new int[7];
        _fld0452 = -1;
        _fld0453 = -1;
        _fld0454 = -1;
        _fld0466 = 80;
        _fld0467 = false;
    }

    Font _fld0417;
    Font _fld0418;
    Font _fld0419;
    Font _fld041A;
    Font _fld041B;
    Color _fld041C;
    int _fld041D[][] = {
        {
            12
        }, {
            4, 7, 5, 6
        }, {
            1, 3
        }, {
            0, 2
        }, {
            4, 7, 2
        }, {
            4, 5, 3
        }, {
            0, 5, 6
        }, {
            1, 7, 6
        }, {
            8
        }, {
            9
        }, {
            10
        }, {
            11
        }, {
            12
        }, {
            12
        }, {
            16
        }
    };
    Color _fld041E;
    Color _fld041F;
    int _fld0420[] = {
        84, 28, 95, 59, 64, 74
    };
    int _fld0421[] = {
        13, 28, 40, 27, 46, 26
    };
    int _fld0422[] = {
        0, 1, 2, 1
    };
    int _fld0423;
    int _fld0424;
    int _fld0425;
    int _fld0426;
    int _fld0427;
    int _fld0428[][];
    int _fld0429[][];
    boolean _fld042A[][];
    int _fld042B[];
    int _fld042C[];
    int _fld042D[];
    int _fld042E;
    int _fld042F;
    int _fld0430;
    int _fld0431;
    int _fld0432;
    int _fld0433;
    int _fld0434;
    int _fld0435;
    int _fld0436;
    int _fld0437;
    int _fld0438;
    int _fld0439;
    String _fld043A[];
    String _fld043B[];
    int _fld043C[];
    int _fld043D[];
    int _fld043E[];
    int _fld043F[];
    int _fld0440[];
    int _fld0441[];
    int _fld0442[];
    int _fld0443;
    int _fld0444[][];
    int _fld0445[];
    int _fld0446;
    int _fld0447;
    int _fld0448;
    int _fld0449[] = {
        140, 260, 380, 140, 260, 380
    };
    int _fld044A[] = {
        119, 119, 119, 204, 204, 204
    };
    int _fld044B;
    int _fld044C;
    int _fld044D[];
    String _fld044E[];
    int _fld044F[][];
    int _fld0451[];
    int _fld0452;
    int _fld0453;
    int _fld0454;
    int _fld0455;
    int _fld0456;
    int _fld0457;
    int _fld0458;
    int _fld0459;
    int _fld045A;
    int _fld045B;
    int _fld045C;
    int _fld045E;
    int _fld045F;
    int _fld0460;
    int _fld0461;
    int _fld0462;
    int _fld0463;
    int _fld0464;
    int _fld0465;
    int _fld0466;
    boolean _fld0467;
}

