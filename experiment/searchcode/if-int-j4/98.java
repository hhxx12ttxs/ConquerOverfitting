// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   cybergame.java

import jagex.*;
import java.awt.*;
import java.io.IOException;

public class Cyber extends Game
{

    public void _mth0198(int i)
    {
        int k = -1;
        String s = "Sorry, no help available for this screen";
        if(_fld030E == 2)
            k = 4;
        if(_fld030E == 3)
            k = 12;
        if(_fld030E == 4)
            k = 13;
        if(_fld0311 == 15)
            k = 5;
        if(_fld030E == 5)
        {
            if(_fld0311 == 17)
                k = 6;
            if(_fld0311 == 18)
                k = 7;
            if(_fld0311 == 19)
                if(_fld0361 < 16)
                {
                    k = 14;
                } else
                {
                    int l = _fld032A[_fld0361 - 16];
                    if(l == 0)
                        k = 8;
                    else
                        k = 9;
                }
            if(_fld0311 == 20)
                k = 10;
        }
        if(_fld030E == 6)
            k = 11;
        if(i != -1)
            k = i;
        if(k == 4)
            s = "Here is a list of all the creatures you can beam from your ship in orbit, and the other actions your ship can do. You can choose one action each turn. To choose an action just click on it's icon.";
        if(k == 5)
            s = "You can either beam this creature from your ship, (in which case there is a chance that it will be destroyed in transit), or you can project a hologram, (which is bound to work, but can be destroyed with the HoloDetector).";
        if(k == 6)
            s = "Action phase: When it is your turn you must indicate which square you want to target your action at. You normally need to choose a square near your commander, who is indicated with a flashing red & yellow square.";
        if(k == 7)
            s = "Movement phase: On you turn select a creature by clicking on it with your mouse. When you have finished click the 'end-turn' button. You can only select your own commander, or creatures you beamed down.";
        if(k == 8)
            s = "Land-based creatures can only move one square at a time. Click on a square adjacent to your selected creature to move it. Repeat to use all your movement points. To attack an enemy just try to move your creature into it.";
        if(k == 9)
            s = "Flying creatures use all their movement at once, just click where you want to land. To attack an enemy just try to land on top of it.";
        if(k == 10)
            s = "Because you are standing next to an enemy, your creature has become engaged in combat. Click on an adjacent creature to attack it.";
        if(k == 11)
            s = "This creature has a ranged-combat ability. Just click on a target to take a shot at it! The target must be in range, and there must be no obstacles between the 2 creatures.";
        if(k == 12)
            s = "This is the action information screen. You can also display it during the game by right-clicking on any creature on the board.";
        if(k == 13)
            s = "This is the board preview screen. It is particularly useful for determining if there is anything near your commander to use one of the short-range offensive actions on.";
        if(k == 14)
            s = "You have selected your commander. Try to move your commander to a safe space, either in a building or vehicle, or as far from danger as possible. Click on a square adjacent to your commander to move.";
        String as[] = new String[10];
        int i1 = 0;
        String s1 = "";
        int j1 = 0;
        as[i1++] = " ";
        for(int k1 = 0; k1 < s.length(); k1++)
            if(j1 > 75 && s.charAt(k1) == ' ')
            {
                as[i1++] = s1;
                s1 = "";
                j1 = 0;
            } else
            {
                s1 = s1 + s.charAt(k1);
                j1++;
            }

        if(j1 > 0)
            as[i1++] = s1;
        for(int l1 = 0; l1 < i1; l1++)
        {
            for(int i2 = 2; i2 >= 1; i2--)
                super._fld03C7[i2] = super._fld03C7[i2 - 1];

            super._fld03C7[0] = as[l1];
        }

    }

    public boolean _mth0195()
    {
        if(!_fld02E8 && (_fld033B != 0 || _fld0397 != 0 || _fld039A != 0 || _fld038A != 0 || _fld0392 != 0 || _fld0391 != 0 || _fld0345[0] != 0 || _fld03A0 != 0 || _fld03A6 != 0))
            return false;
        return !_fld02E8 || _fld0397 == 0 && _fld039A == 0 && (_fld038A == 0 || _fld03A6 == 0);
    }

    public void _mth01A2(int i, int k)
        throws IOException
    {
        if(i == 255)
        {
            for(int l = 0; l < 18; l++)
                _fld032E[l] = super.castleStream.getShort(super.castlePdata, 1 + l * 2);

            _fld033F = super.castleStream.getShort(super.castlePdata, 37);
            _fld032D = true;
            return;
        }
        if(i == 254)
        {
            _fld0339 = super.castleStream.getShort(super.castlePdata, 1);
            _fld039E = super.castleStream.byte2int(super.castlePdata[3]) - 128;
            if(_fld030E != 2 && _fld030E != 3 && _fld030E != 4 && (_fld030E != 7 || _fld03AC != 4))
            {
                _fld030E = 2;
                _fld0311 = 12;
            }
            _fld0337 = -1;
            _fld0338 = -1;
            return;
        }
        if(i == 253)
        {
            int i1 = 1;
            for(int l7 = 0; l7 < 15; l7++)
            {
                for(int j14 = 0; j14 < 9; j14++)
                {
                    _fld0331[l7][j14] = super.castleStream.byte2int(super.castlePdata[i1++]);
                    _fld0332[l7][j14] = super.castleStream.byte2int(super.castlePdata[i1++]);
                    _fld0333[l7][j14] = super.castleStream.byte2int(super.castlePdata[i1++]);
                    _fld0334[l7][j14] = super.castleStream.byte2int(super.castlePdata[i1++]);
                    _fld0335[l7][j14] = super.castleStream.byte2int(super.castlePdata[i1++]);
                    if(_fld0332[l7][j14] == _fld033F && _fld0331[l7][j14] < 16)
                    {
                        _fld0340 = l7;
                        _fld0341 = j14;
                    }
                }

            }

            return;
        }
        if(i == 252)
        {
            _fld0336 = super.castleStream.getShort(super.castlePdata, 1);
            _fld0339 = super.castleStream.getShort(super.castlePdata, 3);
            _fld033A = super.castleStream.getShort(super.castlePdata, 5);
            _fld03AE = super.castleStream.getShort(super.castlePdata, 7);
            _fld03AF = super.castleStream.getShort(super.castlePdata, 9);
            if(_fld0336 != _fld0338)
            {
                _fld030E = 5;
                _fld0311 = 17;
                _fld033B = 40;
                _fld0338 = _fld0336;
            }
            _fld0337 = -1;
            return;
        }
        if(i == 251)
        {
            _fld0336 = super.castleStream.getShort(super.castlePdata, 1);
            _fld0339 = super.castleStream.getShort(super.castlePdata, 3);
            if(_fld030E != 6 && _fld030E != 7)
                _fld030E = 5;
            if(_fld0311 != 19 && _fld0311 != 20 && _fld0311 != 21 && _fld0311 != 22)
                _fld0311 = 18;
            if(_fld0336 != _fld0337)
            {
                _fld0311 = 18;
                _fld030E = 5;
                super.castleref.c(2, 63);
                int j1 = 0;
                for(int i8 = 0; i8 < 15; i8++)
                {
                    for(int k14 = 0; k14 < 9; k14++)
                        if(_fld0332[i8][k14] == _fld0336)
                        {
                            _fld0360[j1] = 11;
                            _fld0343[j1] = i8;
                            _fld0344[j1] = k14;
                            _fld0345[j1] = 16;
                            j1++;
                        }

                }

                _fld0337 = _fld0336;
                return;
            }
        } else
        {
            if(i == 250)
            {
                super.castleref.Y();
                super.castleref.c(11, 63);
                _fld0319 = 50;
                _fld031A = "Action Failed";
                _fld031B = Color.magenta;
                int k1 = super.castleStream.byte2int(super.castlePdata[1]);
                int j8 = super.castleStream.byte2int(super.castlePdata[2]);
                _fld0345[0] = 15;
                _fld0343[0] = k1;
                _fld0344[0] = j8;
                _fld0360[0] = 8;
                return;
            }
            if(i == 249)
            {
                super.castleref.Y();
                super.castleref.c(10, 63);
                _fld0319 = 50;
                _fld031A = "Action Succeeded";
                _fld031B = Color.white;
                int l1 = super.castleStream.byte2int(super.castlePdata[1]);
                int k8 = super.castleStream.byte2int(super.castlePdata[2]);
                int l14 = super.castleStream.byte2int(super.castlePdata[3]);
                int l17 = super.castleStream.byte2int(super.castlePdata[4]);
                if(_fld0334[l1][k8] != 255)
                {
                    _fld0334[l1][k8] = l17;
                } else
                {
                    _fld0331[l1][k8] = l17;
                    _fld0332[l1][k8] = l14;
                    _fld0335[l1][k8] = 255;
                }
                int j20;
                for(j20 = 0; _fld0360[j20] == 1 && _fld0345[j20] > 0 && j20 < 19; j20++);
                _fld0345[j20] = 20;
                _fld0343[j20] = l1;
                _fld0344[j20] = k8;
                _fld0360[j20] = 1;
                return;
            }
            if(i == 248)
            {
                super.castleref.Y();
                super.castleref.c(11, 63);
                _fld0319 = 50;
                _fld031A = "Beam Down Failure";
                _fld031B = Color.magenta;
                int i2 = super.castleStream.byte2int(super.castlePdata[1]);
                int l8 = super.castleStream.byte2int(super.castlePdata[2]);
                _fld0345[0] = 31;
                _fld0343[0] = i2;
                _fld0344[0] = l8;
                _fld0360[0] = 4;
                return;
            }
            if(i == 247)
            {
                super.castleref.c(3, 63);
                _fld0319 = 50;
                _fld031A = "Target cannot move";
                _fld031B = Color.cyan;
                _fld0345[0] = 0;
                return;
            }
            if(i == 246)
            {
                super.castleref.c(8, 63);
                _fld0311 = 19;
                _fld0361 = super.castleStream.byte2int(super.castlePdata[1]);
                _fld0374 = super.castleStream.byte2int(super.castlePdata[2]);
                _fld0375 = super.castleStream.byte2int(super.castlePdata[3]);
                if(_fld0361 < 16)
                    _fld037A = _fld0385[_fld0361] * 2;
                else
                    _fld037A = _fld0325[_fld0361 - 16] * 2;
                _fld0342 = false;
                return;
            }
            if(i == 245)
            {
                super.castleref.c(3, 63);
                _fld0319 = 50;
                _fld031A = "Target has already moved";
                _fld031B = Color.cyan;
                _fld0345[0] = 0;
                return;
            }
            if(i == 244)
            {
                _fld0311 = 18;
                return;
            }
            if(i == 243)
            {
                int j2 = super.castleStream.byte2int(super.castlePdata[1]);
                _fld037E[j2] = super.castleStream.byte2int(super.castlePdata[2]);
                _fld0384[j2] = super.castleStream.byte2int(super.castlePdata[3]);
                _fld0385[j2] = super.castleStream.byte2int(super.castlePdata[4]);
                _fld0386[j2] = super.castleStream.byte2int(super.castlePdata[5]);
                _fld0387[j2] = super.castleStream.byte2int(super.castlePdata[6]);
                _fld0388[j2] = super.castleStream.byte2int(super.castlePdata[7]);
                _fld0389[j2] = super.castleStream.byte2int(super.castlePdata[8]);
                return;
            }
            if(i == 242)
            {
                super.castleref.c(7, 63);
                int k2 = super.castleStream.byte2int(super.castlePdata[1]);
                int i9 = super.castleStream.byte2int(super.castlePdata[2]);
                int i15 = super.castleStream.byte2int(super.castlePdata[3]);
                int i18 = super.castleStream.byte2int(super.castlePdata[4]);
                _fld0331[k2][i9] = _fld0331[i15][i18];
                _fld0331[i15][i18] = 255;
                _fld0332[k2][i9] = _fld0332[i15][i18];
                _fld0332[i15][i18] = 255;
                _fld0334[k2][i9] = _fld0334[i15][i18];
                _fld0334[i15][i18] = 255;
                _fld0374 = k2;
                _fld0375 = i9;
                if((_fld0331[k2][i9] < 16 || _fld0334[k2][i9] < 16) && _fld0332[k2][i9] == _fld033F)
                {
                    _fld0340 = k2;
                    _fld0341 = i9;
                }
                _fld037A = super.castleStream.byte2int(super.castlePdata[5]);
                _fld0342 = true;
                return;
            }
            if(i == 241)
            {
                super.castleref.a(8, 63, 5000);
                _fld0311 = 20;
                return;
            }
            if(i == 240)
            {
                super.castleref.c(0, 63);
                _fld038A = 64;
                _fld038C = super.castleStream.byte2int(super.castlePdata[1]);
                _fld038E = super.castleStream.byte2int(super.castlePdata[2]);
                _fld038F = super.castleStream.byte2int(super.castlePdata[3]);
                _fld0390 = super.castleStream.byte2int(super.castlePdata[4]);
                _fld039D = super.castleStream.byte2int(super.castlePdata[5]);
                _fld0345[0] = 48;
                _fld0343[0] = _fld038C;
                _fld0344[0] = _fld038E;
                _fld0360[0] = 0;
                if(_fld0336 == _fld033F)
                {
                    _fld0311 = 18;
                    return;
                }
            } else
            if(i == 239)
            {
                super.castleref.c(0, 63);
                _fld0391 = 48;
                int l2 = super.castleStream.byte2int(super.castlePdata[1]);
                int j9 = super.castleStream.byte2int(super.castlePdata[2]);
                _fld0345[0] = 48;
                _fld0343[0] = l2;
                _fld0344[0] = j9;
                _fld0360[0] = 0;
                if(_fld0336 == _fld033F)
                {
                    _fld0311 = 18;
                    return;
                }
            } else
            if(i == 238)
            {
                super.castleref.c(7, 63);
                int i3 = super.castleStream.byte2int(super.castlePdata[1]);
                int k9 = super.castleStream.byte2int(super.castlePdata[2]);
                int j15 = super.castleStream.byte2int(super.castlePdata[3]);
                int j18 = super.castleStream.byte2int(super.castlePdata[4]);
                _fld0334[i3][k9] = _fld0331[j15][j18];
                _fld0331[j15][j18] = 255;
                _fld0332[j15][j18] = 255;
                _fld0334[j15][j18] = 255;
                if(_fld0332[i3][k9] == _fld033F)
                {
                    _fld0340 = i3;
                    _fld0341 = k9;
                }
                if(_fld0336 == _fld033F)
                {
                    _fld0311 = 18;
                    return;
                }
            } else
            if(i == 237)
            {
                super.castleref.c(7, 63);
                int j3 = super.castleStream.byte2int(super.castlePdata[1]);
                int l9 = super.castleStream.byte2int(super.castlePdata[2]);
                int k15 = super.castleStream.byte2int(super.castlePdata[3]);
                int k18 = super.castleStream.byte2int(super.castlePdata[4]);
                _fld0331[j3][l9] = _fld0334[k15][k18];
                _fld0332[j3][l9] = _fld0332[k15][k18];
                _fld0334[k15][k18] = 255;
                _fld0334[j3][l9] = 255;
                if(_fld0332[j3][l9] == _fld033F)
                {
                    _fld0340 = j3;
                    _fld0341 = l9;
                }
                if(_fld0336 == _fld033F)
                {
                    _fld0311 = 18;
                    return;
                }
            } else
            {
                if(i == 236)
                {
                    super.castleref.c(4, 63);
                    int k3 = super.castleStream.byte2int(super.castlePdata[1]);
                    _fld0393 = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0394 = super.castleStream.byte2int(super.castlePdata[3]);
                    _fld0395 = super.castleStream.byte2int(super.castlePdata[4]);
                    if(_fld030E < 4)
                        _mth0189();
                    if(_fld030E == 7)
                    {
                        _fld030E = _fld03AC;
                        _fld0311 = _fld03AB;
                    }
                    int i10 = 0;
                    for(int l15 = 0; l15 < 15; l15++)
                    {
                        for(int l18 = 0; l18 < 9; l18++)
                        {
                            if(_fld0335[l15][l18] == k3)
                            {
                                _fld0335[l15][l18] = 255;
                                if(_fld0331[l15][l18] == 49 || _fld0331[l15][l18] == 47)
                                    _fld0334[l15][l18] = 255;
                            }
                            if(_fld0332[l15][l18] == k3)
                            {
                                _fld0360[i10] = 9;
                                _fld0345[i10] = 15;
                                _fld0343[i10] = l15;
                                _fld0344[i10] = l18;
                                i10++;
                                if(_fld0335[l15][l18] != 255 && _fld0334[l15][l18] != 255 && (_fld0331[l15][l18] == 49 || _fld0331[l15][l18] == 47))
                                {
                                    _fld0331[l15][l18] = _fld0334[l15][l18];
                                    _fld0332[l15][l18] = _fld0335[l15][l18];
                                    _fld0334[l15][l18] = 255;
                                    _fld0335[l15][l18] = 255;
                                } else
                                {
                                    _fld0331[l15][l18] = 255;
                                    _fld0332[l15][l18] = 255;
                                    _fld0334[l15][l18] = 255;
                                    _fld0335[l15][l18] = 255;
                                }
                            }
                        }

                    }

                    _fld0392 = 63;
                    _fld0396 = super._fld03C1[k3] + " left the game!";
                    return;
                }
                if(i == 235)
                {
                    super.castleref.c(4, 63);
                    int l3 = super.castleStream.byte2int(super.castlePdata[1]);
                    _fld0393 = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0394 = super.castleStream.byte2int(super.castlePdata[3]);
                    _fld0395 = super.castleStream.byte2int(super.castlePdata[4]);
                    if(_fld030E < 4)
                        _mth0189();
                    if(_fld030E == 7)
                    {
                        _fld030E = _fld03AC;
                        _fld0311 = _fld03AB;
                    }
                    int j10 = 0;
                    for(int i16 = 0; i16 < 15; i16++)
                    {
                        for(int i19 = 0; i19 < 9; i19++)
                        {
                            if(_fld0335[i16][i19] == l3)
                            {
                                _fld0335[i16][i19] = 255;
                                if(_fld0331[i16][i19] == 49 || _fld0331[i16][i19] == 47)
                                    _fld0334[i16][i19] = 255;
                            }
                            if(_fld0332[i16][i19] == l3)
                            {
                                _fld0360[j10] = 9;
                                _fld0345[j10] = 15;
                                _fld0343[j10] = i16;
                                _fld0344[j10] = i19;
                                j10++;
                                if(_fld0335[i16][i19] != 255 && _fld0334[i16][i19] != 255 && (_fld0331[i16][i19] == 49 || _fld0331[i16][i19] == 47))
                                {
                                    _fld0331[i16][i19] = _fld0334[i16][i19];
                                    _fld0332[i16][i19] = _fld0335[i16][i19];
                                    _fld0334[i16][i19] = 255;
                                    _fld0335[i16][i19] = 255;
                                } else
                                {
                                    _fld0331[i16][i19] = 255;
                                    _fld0332[i16][i19] = 255;
                                    _fld0334[i16][i19] = 255;
                                    _fld0335[i16][i19] = 255;
                                }
                            }
                        }

                    }

                    _fld0392 = 63;
                    _fld0396 = super._fld03C1[l3] + " has died!";
                    return;
                }
                if(i == 234)
                {
                    super.castleref.c(9, 63);
                    _fld0397 = 63;
                    _fld0398 = super.castleStream.byte2int(super.castlePdata[1]);
                    _fld0399 = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0345[0] = 63;
                    _fld0343[0] = _fld0398;
                    _fld0344[0] = _fld0399;
                    _fld0360[0] = 6;
                    return;
                }
                if(i == 233)
                {
                    super.castleref.c(9, 63);
                    _fld0397 = -63;
                    _fld0398 = super.castleStream.byte2int(super.castlePdata[1]);
                    _fld0399 = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0345[0] = 63;
                    _fld0343[0] = _fld0398;
                    _fld0344[0] = _fld0399;
                    _fld0360[0] = 6;
                    return;
                }
                if(i == 232)
                {
                    super.castleref.c(6, 63);
                    _fld039A = 63;
                    _fld039B = super.castleStream.byte2int(super.castlePdata[1]);
                    _fld039C = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0345[0] = 63;
                    _fld0343[0] = _fld039B;
                    _fld0344[0] = _fld039C;
                    _fld0360[0] = 5;
                    return;
                }
                if(i == 231)
                {
                    super.castleref.c(6, 63);
                    _fld039A = -63;
                    _fld039B = super.castleStream.byte2int(super.castlePdata[1]);
                    _fld039C = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0345[0] = 63;
                    _fld0343[0] = _fld039B;
                    _fld0344[0] = _fld039C;
                    _fld0360[0] = 5;
                    return;
                }
                if(i == 230)
                {
                    super.castleref.c(0, 63);
                    _fld0391 = 48;
                    int i4 = super.castleStream.byte2int(super.castlePdata[1]);
                    int k10 = super.castleStream.byte2int(super.castlePdata[2]);
                    _fld0345[0] = 48;
                    _fld0343[0] = i4;
                    _fld0344[0] = k10;
                    _fld0360[0] = 0;
                    if(_fld0331[i4][k10] == 49 || _fld0331[i4][k10] == 47)
                        _fld0332[i4][k10] = _fld0335[i4][k10];
                    _fld0331[i4][k10] = _fld0334[i4][k10];
                    _fld0334[i4][k10] = 255;
                    if(_fld0331[i4][k10] == 255)
                    {
                        _fld0332[i4][k10] = 255;
                        return;
                    }
                } else
                {
                    if(i == 229)
                    {
                        super.castleref.c(0, 63);
                        _fld0391 = 48;
                        int j4 = super.castleStream.byte2int(super.castlePdata[1]);
                        int l10 = super.castleStream.byte2int(super.castlePdata[2]);
                        _fld0345[0] = 48;
                        _fld0343[0] = j4;
                        _fld0344[0] = l10;
                        _fld0360[0] = 0;
                        return;
                    }
                    if(i == 228)
                    {
                        super.castleref.c(9, 63);
                        _fld0391 = 63;
                        int k4 = super.castleStream.byte2int(super.castlePdata[1]);
                        int i11 = super.castleStream.byte2int(super.castlePdata[2]);
                        _fld0345[0] = 63;
                        _fld0343[0] = k4;
                        _fld0344[0] = i11;
                        _fld0360[0] = 6;
                        _fld0331[k4][i11] = super.castleStream.byte2int(super.castlePdata[3]);
                        return;
                    }
                    if(i == 227)
                    {
                        super.castleref.c(9, 63);
                        _fld0391 = 63;
                        int l4 = super.castleStream.byte2int(super.castlePdata[1]);
                        int j11 = super.castleStream.byte2int(super.castlePdata[2]);
                        _fld0345[0] = 63;
                        _fld0343[0] = l4;
                        _fld0344[0] = j11;
                        _fld0360[0] = 6;
                        return;
                    }
                    if(i == 226)
                    {
                        super.castleref.c(6, 63);
                        _fld0391 = 63;
                        int i5 = super.castleStream.byte2int(super.castlePdata[1]);
                        int k11 = super.castleStream.byte2int(super.castlePdata[2]);
                        _fld0345[0] = 63;
                        _fld0343[0] = i5;
                        _fld0344[0] = k11;
                        _fld0360[0] = 5;
                        _fld0332[i5][k11] = super.castleStream.byte2int(super.castlePdata[3]);
                        _fld0319 = 64;
                        _fld031A = "Mind Control Success";
                        _fld031B = Color.magenta;
                        return;
                    }
                    if(i == 225)
                    {
                        super.castleref.c(11, 63);
                        _fld0391 = 63;
                        int j5 = super.castleStream.byte2int(super.castlePdata[1]);
                        int l11 = super.castleStream.byte2int(super.castlePdata[2]);
                        _fld0345[0] = 63;
                        _fld0343[0] = j5;
                        _fld0344[0] = l11;
                        _fld0360[0] = 5;
                        _fld0319 = 64;
                        _fld031A = "Mind Control Failure";
                        _fld031B = Color.magenta;
                        return;
                    }
                    if(i == 224)
                    {
                        _fld0319 = 50;
                        _fld031A = "Destroying Hologram";
                        _fld031B = Color.white;
                        super.castleref.c(11, 63);
                        _fld0391 = 48;
                        int k5 = super.castleStream.byte2int(super.castlePdata[1]);
                        int i12 = super.castleStream.byte2int(super.castlePdata[2]);
                        _fld0345[0] = 48;
                        _fld0343[0] = k5;
                        _fld0344[0] = i12;
                        _fld0360[0] = 7;
                        if(_fld0331[k5][i12] == 49 || _fld0331[k5][i12] == 47)
                            _fld0332[k5][i12] = _fld0335[k5][i12];
                        _fld0331[k5][i12] = _fld0334[k5][i12];
                        _fld0334[k5][i12] = 255;
                        if(_fld0331[k5][i12] == 255)
                        {
                            _fld0332[k5][i12] = 255;
                            return;
                        }
                    } else
                    {
                        if(i == 223)
                        {
                            _fld0319 = 50;
                            _fld031A = "Not A Hologram";
                            _fld031B = Color.magenta;
                            super.castleref.c(11, 63);
                            int l5 = super.castleStream.byte2int(super.castlePdata[1]);
                            int j12 = super.castleStream.byte2int(super.castlePdata[2]);
                            _fld0345[0] = 48;
                            _fld0343[0] = l5;
                            _fld0344[0] = j12;
                            _fld0360[0] = 7;
                            return;
                        }
                        if(i == 222)
                        {
                            _fld0319 = 50;
                            _fld031A = "Slime cannot be attacked!";
                            _fld031B = Color.cyan;
                            super.castleref.c(11, 63);
                            return;
                        }
                        if(i == 221)
                        {
                            _fld0319 = 50;
                            _fld031A = "Resurrect Succeeded";
                            _fld031B = Color.white;
                            int i6 = super.castleStream.byte2int(super.castlePdata[1]);
                            int k12 = super.castleStream.byte2int(super.castlePdata[2]);
                            int j16 = super.castleStream.byte2int(super.castlePdata[3]);
                            int j19 = super.castleStream.byte2int(super.castlePdata[4]);
                            _fld0331[i6][k12] = j16;
                            _fld0332[i6][k12] = j19;
                            _fld0333[i6][k12] = 255;
                            super.castleref.c(8, 63);
                            _fld0345[0] = 48;
                            _fld0343[0] = i6;
                            _fld0344[0] = k12;
                            _fld0360[0] = 9;
                            return;
                        }
                        if(i == 220)
                        {
                            _fld0319 = 50;
                            _fld031A = "Action Succeeded";
                            _fld031B = Color.white;
                            return;
                        }
                        if(i == 219)
                        {
                            _fld0319 = 50;
                            _fld031A = "Action Succeeded";
                            _fld031B = Color.white;
                            int j6 = super.castleStream.byte2int(super.castlePdata[1]);
                            int l12 = super.castleStream.byte2int(super.castlePdata[2]);
                            int k16 = super.castleStream.byte2int(super.castlePdata[3]);
                            int k19 = super.castleStream.byte2int(super.castlePdata[4]);
                            int k20 = super.castleStream.byte2int(super.castlePdata[5]);
                            if(_fld0334[k16][k19] != 255)
                            {
                                _fld0331[j6][l12] = _fld0334[k16][k19];
                                _fld0332[j6][l12] = k20;
                                _fld0334[k16][k19] = 255;
                                _fld0335[k16][k19] = 255;
                            } else
                            {
                                _fld0331[j6][l12] = _fld0331[k16][k19];
                                _fld0332[j6][l12] = k20;
                                _fld0334[j6][l12] = 255;
                                _fld0335[j6][l12] = 255;
                                _fld0331[k16][k19] = 255;
                                _fld0332[k16][k19] = 255;
                            }
                            super.castleref.c(4, 63);
                            _fld0345[0] = 48;
                            _fld0343[0] = j6;
                            _fld0344[0] = l12;
                            _fld0360[0] = 7;
                            _fld0345[1] = 48;
                            _fld0343[1] = k16;
                            _fld0344[1] = k19;
                            _fld0360[1] = 7;
                            if(k20 == _fld033F)
                            {
                                _fld0340 = j6;
                                _fld0341 = l12;
                                return;
                            }
                        } else
                        if(i == 218)
                        {
                            super.castleref.c(7, 63);
                            int k6 = super.castleStream.byte2int(super.castlePdata[1]);
                            int i13 = super.castleStream.byte2int(super.castlePdata[2]);
                            int l16 = super.castleStream.byte2int(super.castlePdata[3]);
                            int l19 = super.castleStream.byte2int(super.castlePdata[4]);
                            _fld0334[k6][i13] = _fld0331[l16][l19];
                            _fld0335[k6][i13] = _fld0332[l16][l19];
                            _fld0331[l16][l19] = 255;
                            _fld0332[l16][l19] = 255;
                            _fld0334[l16][l19] = 255;
                            if(_fld0335[k6][i13] == _fld033F)
                            {
                                _fld0340 = k6;
                                _fld0341 = i13;
                            }
                            if(_fld0336 == _fld033F)
                            {
                                _fld0311 = 18;
                                return;
                            }
                        } else
                        {
                            if(i == 217)
                            {
                                _fld030E = 5;
                                if(_fld0311 != 19 && _fld0311 != 20)
                                {
                                    _fld0311 = 18;
                                    _fld0336 = 0;
                                }
                                int l6 = super.castleStream.byte2int(super.castlePdata[1]);
                                int j13 = super.castleStream.byte2int(super.castlePdata[2]);
                                int i17 = super.castleStream.byte2int(super.castlePdata[3]);
                                _fld0331[j13][i17] = _fld0334[j13][i17];
                                _fld0332[j13][i17] = _fld0335[j13][i17];
                                _fld0334[j13][i17] = 255;
                                _fld0335[j13][i17] = 255;
                                super.castleref.c(12, 63);
                                _fld0343[0] = j13;
                                _fld0344[0] = i17;
                                _fld0345[0] = 50;
                                _fld0360[0] = 2;
                                _fld0319 = 50;
                                _fld031A = super._fld03C1[l6] + " gained an action";
                                _fld031B = Color.white;
                                _fld03A0 = 60;
                                return;
                            }
                            if(i == 216)
                            {
                                super.castleref.c(7, 63);
                                _fld030E = 5;
                                if(_fld0311 != 19 && _fld0311 != 20)
                                {
                                    _fld0311 = 18;
                                    _fld0336 = 0;
                                }
                                int i7 = super.castleStream.byte2int(super.castlePdata[1]);
                                int k13 = super.castleStream.byte2int(super.castlePdata[2]);
                                int j17 = super.castleStream.byte2int(super.castlePdata[3]);
                                int i20 = super.castleStream.byte2int(super.castlePdata[4]);
                                if(_fld0331[j17][i20] >= 16 && i7 == 49)
                                {
                                    _fld0334[j17][i20] = _fld0331[j17][i20];
                                    _fld0335[j17][i20] = _fld0332[j17][i20];
                                } else
                                {
                                    _fld0334[j17][i20] = 255;
                                    _fld0335[j17][i20] = 255;
                                }
                                _fld0331[j17][i20] = i7;
                                _fld0332[j17][i20] = k13;
                                _fld0333[j17][i20] = 255;
                                return;
                            }
                            if(i == 215)
                            {
                                super.castleref.c(7, 63);
                                _fld030E = 5;
                                if(_fld0311 != 19 && _fld0311 != 20)
                                {
                                    _fld0311 = 18;
                                    _fld0336 = 0;
                                }
                                int j7 = super.castleStream.byte2int(super.castlePdata[1]);
                                int l13 = super.castleStream.byte2int(super.castlePdata[2]);
                                int k17 = super.castleStream.byte2int(super.castlePdata[3]);
                                if(j7 == 49 && _fld0334[l13][k17] != 255)
                                {
                                    _fld0331[l13][k17] = _fld0334[l13][k17];
                                    _fld0332[l13][k17] = _fld0335[l13][k17];
                                    _fld0333[l13][k17] = 255;
                                    _fld0334[l13][k17] = 255;
                                    _fld0335[l13][k17] = 255;
                                    return;
                                } else
                                {
                                    _fld0331[l13][k17] = 255;
                                    _fld0332[l13][k17] = 255;
                                    _fld0333[l13][k17] = 255;
                                    _fld0334[l13][k17] = 255;
                                    _fld0335[l13][k17] = 255;
                                    return;
                                }
                            }
                            if(i == 214)
                            {
                                _fld030E = 5;
                                if(_fld0311 != 19 && _fld0311 != 20)
                                {
                                    _fld0311 = 18;
                                    _fld0336 = 0;
                                }
                                int k7 = super.castleStream.byte2int(super.castlePdata[1]);
                                int i14 = super.castleStream.byte2int(super.castlePdata[2]);
                                super.castleref.c(5, 63);
                                _fld0345[0] = 15;
                                _fld0343[0] = k7;
                                _fld0344[0] = i14;
                                _fld0360[0] = 9;
                                _fld0331[k7][i14] = _fld0334[k7][i14];
                                _fld0334[k7][i14] = 255;
                                return;
                            }
                            if(i == 213)
                            {
                                super.castleref.c(8, 63);
                                _fld030E = 6;
                                _fld0311 = 21;
                                return;
                            }
                            if(i == 212)
                            {
                                super.castleref.a(5, 63, 5000);
                                if(_fld0311 == 21)
                                {
                                    _fld0311 = 18;
                                    _fld030E = 5;
                                }
                                _fld03A4 = super.castleStream.byte2int(super.castlePdata[1]);
                                _fld03A5 = super.castleStream.byte2int(super.castlePdata[2]);
                                _fld03A1 = super.castleStream.byte2int(super.castlePdata[3]);
                                _fld03A3 = super.castleStream.byte2int(super.castlePdata[4]);
                                super.castleStream.byte2int(super.castlePdata[5]);
                                _fld03A8 = _fld03AA[super.castleStream.byte2int(super.castlePdata[5])];
                                _fld03A6 = -_fld03A7;
                                return;
                            }
                            if(i == 211)
                            {
                                super.castleref.a(5, 63, 5000);
                                if(_fld0311 == 21)
                                {
                                    _fld0311 = 18;
                                    _fld030E = 5;
                                }
                                _fld03A4 = super.castleStream.byte2int(super.castlePdata[1]);
                                _fld03A5 = super.castleStream.byte2int(super.castlePdata[2]);
                                _fld03A1 = super.castleStream.byte2int(super.castlePdata[3]);
                                _fld03A3 = super.castleStream.byte2int(super.castlePdata[4]);
                                _fld03A8 = _fld03AA[super.castleStream.byte2int(super.castlePdata[5])];
                                _fld03A9 = super.castleStream.byte2int(super.castlePdata[6]);
                                _fld03A6 = _fld03A7;
                            }
                        }
                    }
                }
            }
        }
    }

    public void _mth017A(int i, int k, boolean flag)
    {
        super.castleref.c(5, 63);
        if(_fld0331[i][k] < 16)
        {
            int l = 0;
            for(int i1 = 0; i1 < 15; i1++)
            {
                for(int j1 = 0; j1 < 9; j1++)
                    if(_fld0332[i1][j1] == _fld0332[i][k] && _fld0331[i1][j1] >= 16 && (!flag || _fld0321[_fld0331[i1][j1] - 16] > 0) && (flag || _fld0321[_fld0331[i1][j1] - 16] < 0))
                    {
                        if(_fld0331[i1][j1] == 49 || _fld0331[i1][j1] == 47)
                            _fld0332[i1][j1] = _fld0335[i1][j1];
                        _fld0331[i1][j1] = _fld0334[i1][j1];
                        _fld0334[i1][j1] = 255;
                        if(_fld0331[i1][j1] == 255)
                            _fld0332[i1][j1] = 255;
                        _fld0345[l] = 15;
                        _fld0343[l] = i1;
                        _fld0344[l] = j1;
                        _fld0360[l] = 9;
                        l++;
                    }

            }

            return;
        }
        if(_fld0331[i][k] == 49 || _fld0331[i][k] == 47)
            _fld0332[i][k] = _fld0335[i][k];
        _fld0331[i][k] = _fld0334[i][k];
        _fld0334[i][k] = 255;
        if(_fld0331[i][k] == 255)
            _fld0332[i][k] = 255;
        _fld0345[0] = 15;
        _fld0343[0] = i;
        _fld0344[0] = k;
        _fld0360[0] = 9;
    }

    public void _mth0199()
    {
        super._fld03C3 = 6;
        super.castleSurface2mby._mth01CF();
        super.castleSurface2mby._mth01B7("crypt/back.jpg", 0, false);
        super.castleSurface2mby._mth01B7("cyber/rider.gif", 1, false);
        super.castleSurface2mby._mth01D4("cyber/monsters.gif", 2, true, 120, 32, 32);
        super.castleSurface2mby._mth01D4("cyber/commanders.gif", 122, true, 56, 32, 32);
        super.castleSurface2mby._mth01D4("cyber/sfx.gif", 178, true, 48, 32, 32);
        super.castleSurface2mby._mth01D4("cyber/buildings.gif", 226, true, 24, 32, 32);
        super.castleSurface2mby._mth01D4("cyber/bullets.gif", 250, true, 36, 32, 32);
        super.castleSurface2mby._mth01D4("cyber/select.gif", 286, true, 18, 32, 32);
        _mth0182();
        _fld0302 = new Font("TimesRoman", 0, 16);
        _fld0303 = new Font("TimesRoman", 0, 12);
        _fld0305 = new Font("TimesRoman", 0, 36);
        _fld0304 = new Font("TimesRoman", 0, 24);
        _fld0306 = new Font("TimesRoman", 0, 18);
        _fld0307 = new Font("Helvetica", 1, 30);
        _fld0308 = new Font("Helvetica", 1, 20);
        _fld030A = new Font("Helvetica", 1, 15);
        _fld0309 = new Font("Helvetica", 1, 16);
        _fld030B = new Font("Helvetica", 1, 14);
        _fld030C = new Font("Helvetica", 1, 13);
        _fld030D = new Font("Helvetica", 1, 13);
    }

    public void _mth0191()
    {
        super.castleSurface2mby._fld0413 = 0;
        super.castleSurface2mby._fld0414 = 290;
        super.castleSurface2mby._fld0415 = 0;
        super.castleSurface2mby._fld0416 = 512;
        _fld0336 = 0;
        _fld0339 = 60;
        for(int i = 0; i < 8; i++)
        {
            _fld037E[i] = 5;
            _fld0384[i] = 5;
            _fld0385[i] = 1;
            _fld0387[i] = 0;
            _fld0389[i] = 0;
            _fld0386[i] = 7;
        }

        _fld030E = 2;
        _fld0311 = 12;
        _fld032D = false;
        for(int k = 2; k >= 1; k--)
            super._fld03C7[k] = super._fld03C7[k - 1];

        super._fld03C7[0] = "Welcome to CyberWars! For further instructions on any screen just type: help";
    }

    public void _mth0182()
    {
        _fld031D = new String[_fld031C];
        _fld031E = new String[_fld031C];
        _fld031F = new int[_fld031C];
        _fld0320 = new int[_fld031C];
        _fld0321 = new int[_fld031C];
        _fld0322 = new int[_fld031C];
        _fld0323 = new int[_fld031C];
        _fld0324 = new int[_fld031C];
        _fld0325 = new int[_fld031C];
        _fld0326 = new int[_fld031C];
        _fld0327 = new int[_fld031C];
        _fld0328 = new int[_fld031C];
        _fld0329 = new int[_fld031C];
        _fld032A = new int[_fld031C];
        _fld032B = new int[_fld031C];
        int i = 0;
        for(int k = 0; k < 16; k++)
            _fld03AA[k] = 8;

        try
        {
            Stream w1 = new Stream("cyber/actions.dat");
            for(int l = 0; l < _fld031C; l++)
            {
                w1.skipToEqual();
                _fld031D[l] = w1.getPropStr();
                _fld031F[l] = w1.getPropInt();
                _fld0320[l] = w1.getPropInt();
                _fld0321[l] = w1.getPropInt();
                _fld0322[l] = w1.getPropInt();
                _fld0323[l] = w1.getPropInt();
                _fld0324[l] = w1.getPropInt();
                _fld0325[l] = w1.getPropInt();
                _fld0326[l] = w1.getPropInt();
                _fld0327[l] = w1.getPropInt();
                _fld0328[l] = w1.getPropInt();
                _fld0329[l] = w1.getPropInt();
                _fld032A[l] = w1.getPropInt();
                _fld032B[l] = w1.getPropInt();
                _fld031E[l] = w1.getPropStr2();
                if(_fld0327[l] != 0)
                    _fld03AA[l + 16] = i++;
            }

            w1.closeStream();
            return;
        }
        catch(IOException _ex)
        {
            throw new RuntimeException("Fatal error loading feature-list!");
        }
    }

    public void _mth0197()
    {
        _fld0312++;
        _fld03B0++;
        if(_fld0319 > 0)
            _fld0319--;
        else
        if(_fld03A0 > 0)
            _fld03A0--;
        else
        if(_fld033B > 0)
            _fld033B--;
        if(_fld0392 > 0)
            _fld0392--;
        for(int i = 0; i < 300; i++)
            if(_fld0345[i] > 0 && _fld0392 % 2 == 0)
                _fld0345[i]--;

        if(_fld0391 > 0)
        {
            _fld0391--;
            if(_fld0391 == 0)
                super.castleref.Y();
        }
        if(_fld038A > 0)
        {
            _fld038A--;
            if(_fld038A == 0 && _fld0331[_fld038F][_fld0390] != 46 && _fld0331[_fld038C][_fld038E] == 255)
            {
                super.castleref.c(7, 63);
                _fld0331[_fld038C][_fld038E] = _fld0331[_fld038F][_fld0390];
                _fld0331[_fld038F][_fld0390] = 255;
                _fld0332[_fld038C][_fld038E] = _fld0332[_fld038F][_fld0390];
                _fld0332[_fld038F][_fld0390] = 255;
                _fld0334[_fld038C][_fld038E] = _fld0334[_fld038F][_fld0390];
                _fld0334[_fld038F][_fld0390] = 255;
                if((_fld0331[_fld038C][_fld038E] < 16 || _fld0334[_fld038C][_fld038E] < 16) && _fld0332[_fld038C][_fld038E] == _fld033F)
                {
                    _fld0340 = _fld038C;
                    _fld0341 = _fld038E;
                }
                _fld0374 = _fld038C;
                _fld0375 = _fld038E;
            }
            if(_fld038A == 16)
            {
                super.castleref.Y();
                if(_fld0331[_fld038C][_fld038E] >= 16 && _fld0331[_fld038C][_fld038E] < 46 && _fld039D == 1)
                {
                    super.castleref.c(5, 63);
                    _mth017F(_fld038C, _fld038E, _fld0331[_fld038C][_fld038E] - 16);
                }
                if(_fld0334[_fld038C][_fld038E] != 255)
                {
                    if(_fld0331[_fld038C][_fld038E] == 49 || _fld0331[_fld038C][_fld038E] == 47)
                        _fld0332[_fld038C][_fld038E] = _fld0335[_fld038C][_fld038E];
                    _fld0331[_fld038C][_fld038E] = _fld0334[_fld038C][_fld038E];
                    _fld0334[_fld038C][_fld038E] = 255;
                    _fld0335[_fld038C][_fld038E] = 255;
                } else
                {
                    _fld0331[_fld038C][_fld038E] = 255;
                    _fld0332[_fld038C][_fld038E] = 255;
                }
            }
            if(_fld038A == 31 && _fld0331[_fld038C][_fld038E] >= 16 && _fld0331[_fld038C][_fld038E] < 54 && _fld0321[_fld0331[_fld038C][_fld038E] - 16] < 0)
            {
                _fld0345[0] = 15;
                _fld0360[0] = 3;
                _fld0343[0] = _fld038C;
                _fld0344[0] = _fld038E;
            }
        }
        if(_fld0397 > 0)
        {
            _fld0397--;
            if(_fld0397 == 0)
            {
                super.castleref.Y();
                _mth017A(_fld0398, _fld0399, true);
            }
        }
        if(_fld0397 < 0)
        {
            _fld0397++;
            if(_fld0397 == 0)
                super.castleref.Y();
        }
        if(_fld039A > 0)
        {
            _fld039A--;
            if(_fld039A == 0)
            {
                super.castleref.Y();
                _mth017A(_fld039B, _fld039C, false);
            }
        }
        if(_fld039A < 0)
        {
            _fld039A++;
            if(_fld039A == 0)
                super.castleref.Y();
        }
        if(_fld03A6 > 0)
        {
            _fld03A6--;
            if(_fld03A6 == 0)
            {
                int k = _fld03A4;
                int j1 = _fld03A5;
                int i2 = _fld03A9;
                if(_fld0331[k][j1] >= 16 && _fld0331[k][j1] < 46 && i2 == 1)
                {
                    super.castleref.c(5, 63);
                    _mth017F(k, j1, _fld0331[k][j1] - 16);
                }
                if(_fld0334[k][j1] != 255)
                {
                    if(_fld0331[k][j1] == 49 || _fld0331[k][j1] == 47)
                        _fld0332[k][j1] = _fld0335[k][j1];
                    _fld0331[k][j1] = _fld0334[k][j1];
                    _fld0334[k][j1] = 255;
                    _fld0335[k][j1] = 255;
                } else
                {
                    _fld0331[k][j1] = 255;
                    _fld0332[k][j1] = 255;
                }
            }
        }
        if(_fld03A6 < 0)
            _fld03A6++;
        if(((GameApplet) (super.castleref)).lastMouseButtonDown == 2)
        {
            int l = ((GameApplet) (super.castleref)).mouseX;
            int k1 = ((GameApplet) (super.castleref)).mouseY;
            if(_fld030E == 4 || _fld030E == 5 || _fld030E == 6)
            {
                int j2 = _fld033C;
                int i3 = _fld033D;
                if(j2 >= 0 && j2 < 15 && i3 >= 0 && i3 < 9)
                {
                    int l3 = _fld0331[j2][i3];
                    int k4 = _fld0332[j2][i3];
                    if(l3 != 255)
                    {
                        _fld03AC = _fld030E;
                        _fld03AB = _fld0311;
                        _fld030E = 7;
                        _fld0311 = 22;
                        _mth0187(l3, k4);
                        return;
                    }
                }
            } else
            {
                if(_fld030E == 2 && _fld032D)
                {
                    int k2 = 14;
                    int j3 = 46;
                    for(int i4 = 0; i4 < _fld0330; i4++)
                    {
                        if(l > k2 && k1 > j3 && l < k2 + 152 && k1 < j3 + 34)
                        {
                            super.castleref.c(1, 63);
                            _mth0185(_fld032F[i4]);
                            return;
                        }
                        if((j3 += 34) > 230)
                        {
                            j3 = 46;
                            k2 += 160;
                        }
                    }

                    return;
                }
                if(_fld030E == 7)
                {
                    super.castleref.c(1, 63);
                    _fld030E = _fld03AC;
                    _fld0311 = _fld03AB;
                    return;
                }
                if(_fld030E == 3)
                {
                    super.castleref.c(1, 63);
                    _fld030E = 2;
                    _fld0311 = 12;
                    return;
                }
            }
        } else
        if(((GameApplet) (super.castleref)).lastMouseButtonDown == 1)
        {
            int i1 = ((GameApplet) (super.castleref)).mouseX;
            int l1 = ((GameApplet) (super.castleref)).mouseY;
            if(_fld0311 == 19 && _fld0334[_fld0374][_fld0375] != 255)
            {
                char c;
                if(_fld0374 < 7)
                    c = '\u01B5';
                else
                    c = '\024';
                if(l1 >= 20 && i1 >= c && i1 <= c + 55 && l1 <= 60)
                {
                    if(l1 >= 20 && l1 <= 31)
                    {
                        if(i1 >= c + 8 && i1 <= c + 19)
                            _mth0179(_fld0374 - 1, _fld0375 - 1);
                        if(i1 >= c + 22 && i1 <= c + 33)
                            _mth0179(_fld0374, _fld0375 - 1);
                        if(i1 >= c + 36 && i1 <= c + 47)
                            _mth0179(_fld0374 + 1, _fld0375 - 1);
                    }
                    if(l1 >= 34 && l1 <= 45)
                    {
                        if(i1 >= c && i1 <= c + 11)
                            _mth0179(_fld0374 - 1, _fld0375);
                        if(i1 >= c + 43 && i1 <= c + 54)
                            _mth0179(_fld0374 + 1, _fld0375);
                    }
                    if(l1 >= 48 && l1 <= 59)
                    {
                        if(i1 >= c + 8 && i1 <= c + 19)
                            _mth0179(_fld0374 - 1, _fld0375 + 1);
                        if(i1 >= c + 22 && i1 <= c + 33)
                            _mth0179(_fld0374, _fld0375 + 1);
                        if(i1 >= c + 36 && i1 <= c + 47)
                            _mth0179(_fld0374 + 1, _fld0375 + 1);
                    }
                    return;
                }
            }
            if(_fld030E == 3 && i1 > 210 && l1 <= 13 && i1 < 360)
            {
                super.castleref.c(1, 63);
                _fld030E = 2;
                _fld0311 = 12;
                return;
            }
            if(_fld030E == 4 && l1 <= 13 && i1 > 16 && i1 < 192)
            {
                super.castleref.c(1, 63);
                _fld030E = 2;
                _fld0311 = 12;
                return;
            }
            if(_fld030E == 7)
            {
                super.castleref.c(1, 63);
                _fld030E = _fld03AC;
                _fld0311 = _fld03AB;
                return;
            }
            if(_fld0311 == 12)
            {
                if(l1 > 275 && l1 < 295 && i1 > 64 && i1 < 192)
                {
                    super.castleref.c(1, 63);
                    _mth0189();
                    return;
                }
                if(l1 > 275 && l1 < 295 && i1 > 192 && i1 < 320)
                {
                    super.castleref.c(1, 63);
                    _mth018C(-1, 0);
                    return;
                }
            }
            if(_fld0311 == 15)
            {
                if(l1 > 275 && l1 < 295 && i1 > 64 && i1 < 192)
                {
                    super.castleref.c(1, 63);
                    _mth018C(_fld032C, 0);
                    return;
                }
                if(l1 > 275 && l1 < 295 && i1 > 192 && i1 < 320)
                {
                    super.castleref.c(1, 63);
                    _mth018C(_fld032C, 1);
                    return;
                }
                if(l1 > 275 && l1 < 295 && i1 > 320 && i1 < 448)
                {
                    super.castleref.c(1, 63);
                    _fld030E = 2;
                    _fld0311 = 12;
                    return;
                }
            }
            if(_fld030E == 3)
            {
                super.castleref.c(1, 63);
                _fld030E = 2;
                _fld0311 = 12;
                return;
            }
            if(_fld030E == 2 && _fld032D)
            {
                int l2 = 14;
                int k3 = 46;
                for(int j4 = 0; j4 < _fld0330; j4++)
                {
                    if(i1 > l2 && l1 > k3 && i1 < l2 + 152 && l1 < k3 + 34)
                    {
                        super.castleref.c(1, 63);
                        _mth0177(j4);
                        return;
                    }
                    if((k3 += 34) > 230)
                    {
                        k3 = 46;
                        l2 += 160;
                    }
                }

            }
            if(_fld0311 == 17)
                if(i1 > 210 && l1 <= 13 && i1 < 360)
                {
                    super.castleref.c(3, 63);
                    _mth0184();
                } else
                if(_fld033C != -1 && _fld033E == 1)
                    _mth017C(_fld033C, _fld033D, _fld033E);
            if(_fld0311 == 18)
                if(i1 > 210 && l1 <= 13 && i1 < 360)
                {
                    super.castleref.c(3, 63);
                    _mth0180();
                } else
                if(_fld033C != -1 && _fld033E == 1)
                    _mth017B(_fld033C, _fld033D, _fld033E);
            if(_fld0311 == 19 || _fld0311 == 20)
            {
                if(i1 > 210 && l1 <= 13 && i1 < 360)
                {
                    _fld0311 = 18;
                    super.castleref.c(3, 63);
                    _mth0183();
                    return;
                }
                if(_fld033C != -1)
                    _mth0188(_fld033C, _fld033D, _fld033E);
            }
            if(_fld0311 == 21)
            {
                if(i1 > 210 && l1 <= 13 && i1 < 360)
                {
                    _fld0311 = 18;
                    super.castleref.c(3, 63);
                    _mth017E();
                    return;
                }
                if(_fld033C != -1)
                    _mth018A(_fld033C, _fld033D, _fld033E);
            }
        }
    }

    public void _mth019D()
    {
        super.castleGameGraphics.setColor(new Color(0, 104, 130));
        super.castleGameGraphics.fillRect(0, 0, 512, 6);
        super.castleGameGraphics.setColor(Color.black);
        super.castleGameGraphics.drawLine(5, 5, 507, 5);
        if(_fld030E == 5 || _fld030E == 6)
        {
            super.castleSurface2mby._mth01C7(0, 0, 500, 290, Color.black);
            for(int i = 0; i < 15; i++)
            {
                for(int j1 = 0; j1 < 9; j1++)
                {
                    int k2 = _fld0331[i][j1];
                    if(k2 != 255)
                    {
                        int l4 = -1;
                        if(k2 < 16)
                            l4 = 122 + k2 * 4 + _fld02E9[(_fld0312 / 6) % 4];
                        else
                        if(k2 < 46)
                        {
                            k2 -= 16;
                            l4 = 2 + k2 * 4 + _fld02E9[(_fld0312 / 6) % 4];
                        } else
                        {
                            k2 -= 46;
                            l4 = 226 + k2 * 4 + (_fld0312 / 6) % 4;
                        }
                        super.castleSurface2mby._mth01B5(i * 32 + 10, j1 * 32 + 2, l4);
                    } else
                    if(_fld0333[i][j1] != 255)
                        super.castleSurface2mby._mth01B5(i * 32 + 10, j1 * 32 + 2, 2 + _fld0333[i][j1] * 4 + 3);
                }

            }

            boolean flag = false;
            if(_fld0311 == 19 && _fld0334[_fld0374][_fld0375] != 255)
            {
                char c;
                if(_fld0374 < 7)
                    c = '\u01B5';
                else
                    c = '\024';
                if(((GameApplet) (super.castleref)).mouseY >= 20 && ((GameApplet) (super.castleref)).mouseX >= c && ((GameApplet) (super.castleref)).mouseX <= c + 55 && ((GameApplet) (super.castleref)).mouseY <= 60)
                    flag = true;
            }
            if(((GameApplet) (super.castleref)).mouseX > 16 && ((GameApplet) (super.castleref)).mouseY > 13 && ((GameApplet) (super.castleref)).mouseX < 496 && ((GameApplet) (super.castleref)).mouseY < 297 && _fld0336 == _fld033F && !flag)
            {
                _fld033C = (((GameApplet) (super.castleref)).mouseX - 16) / 32;
                _fld033D = (((GameApplet) (super.castleref)).mouseY - 9) / 32;
                _fld033E = 1;
                if(_fld0311 == 17 && _fld0322[_fld033A] > 0)
                {
                    if(_mth018B(_fld0340, _fld0341, _fld033C, _fld033D) / 2 > _fld0322[_fld033A])
                        _fld033E = 4;
                    else
                    if(_fld0322[_fld033A] != 20 && !_mth0186(_fld0340, _fld0341, _fld033C, _fld033D))
                        _fld033E = 6;
                    int l2 = _fld033A;
                    int i5 = _fld033C;
                    int j7 = _fld033D;
                    if(_fld031F[l2] == 0 && _fld0331[i5][j7] != 255)
                        _fld033E = 5;
                    if(_fld031F[l2] == 1 && _fld0331[i5][j7] != 255 && _fld0322[l2] > 0)
                        _fld033E = 5;
                    if(l2 == 42)
                        if(_fld0331[i5][j7] == 255 || _fld0332[i5][j7] == _fld033F)
                            _fld033E = 5;
                        else
                        if(_fld0331[i5][j7] >= 16 && _fld0321[_fld0331[i5][j7] - 16] <= 0)
                            _fld033E = 5;
                    if(l2 == 43)
                        if(_fld0331[i5][j7] == 255 || _fld0332[i5][j7] == _fld033F)
                            _fld033E = 5;
                        else
                        if(_fld0331[i5][j7] >= 16 && _fld0321[_fld0331[i5][j7] - 16] >= 0)
                            _fld033E = 5;
                    if((l2 == 44 || l2 == 45 || l2 == 47 || l2 == 54) && (_fld0331[i5][j7] == 255 || _fld0332[i5][j7] == _fld033F))
                        _fld033E = 5;
                    if(l2 == 46 && _fld0331[i5][j7] == 255)
                        _fld033E = 5;
                    if(l2 == 52 && (_fld0333[i5][j7] == 255 || _fld0331[i5][j7] != 255))
                        _fld033E = 5;
                    if(l2 == 53 && _fld0331[i5][j7] != 255)
                        _fld033E = 5;
                    if((l2 == 46 || l2 == 47) && (_fld0334[i5][j7] != 255 || _fld0331[i5][j7] < 16))
                        _fld033E = 5;
                }
                if(_fld0311 == 18 && (_fld0331[_fld033C][_fld033D] == 255 || _fld0332[_fld033C][_fld033D] != _fld033F))
                    _fld033E = 5;
                if(_fld0311 == 19 || _fld0311 == 20)
                {
                    int j5;
                    int k7;
                    if(_fld0361 < 16)
                    {
                        String s1 = super._fld03C1[_fld0361];
                        k7 = _fld0385[_fld0361];
                        j5 = _fld0389[_fld0361];
                    } else
                    {
                        String s2 = _fld031D[_fld0361 - 16];
                        k7 = _fld0325[_fld0361 - 16];
                        j5 = _fld032A[_fld0361 - 16];
                    }
                    int l9 = _mth018B(_fld0374, _fld0375, _fld033C, _fld033D) / 2;
                    if(l9 > k7 && k7 != 0 || l9 > 1 && j5 == 0)
                        _fld033E = 4;
                    else
                    if(_fld0331[_fld033C][_fld033D] == 47)
                    {
                        if(_fld0331[_fld0374][_fld0375] < 16 && _fld0334[_fld033C][_fld033D] == 255)
                            _fld033E = 7;
                        else
                        if(_fld0334[_fld033C][_fld033D] != 255 && _fld0335[_fld033C][_fld033D] != _fld033F)
                            _fld033E = 3;
                        else
                            _fld033E = 5;
                    } else
                    if(_fld0331[_fld033C][_fld033D] != 255 && _fld0332[_fld033C][_fld033D] == _fld033F)
                    {
                        if(_fld0331[_fld033C][_fld033D] < 16 || _fld032B[_fld0331[_fld033C][_fld033D] - 16] == 0 || _fld0331[_fld0374][_fld0375] >= 16)
                            _fld033E = 5;
                        else
                            _fld033E = 7;
                    } else
                    if(_fld0331[_fld033C][_fld033D] != 255 && _fld0331[_fld033C][_fld033D] >= 16 && _fld0324[_fld0331[_fld033C][_fld033D] - 16] == 0)
                        _fld033E = 5;
                    else
                    if(_fld0331[_fld033C][_fld033D] != 255)
                        _fld033E = 3;
                    else
                    if(j5 == 1)
                    {
                        _fld033E = 2;
                    } else
                    {
                        int k11 = _fld0374;
                        int l12 = _fld0375;
                        if(_fld033C == k11 && _fld033D < l12)
                            _fld033E = 8;
                        else
                        if(_fld033C > k11 && _fld033D < l12)
                            _fld033E = 9;
                        else
                        if(_fld033C > k11 && _fld033D == l12)
                            _fld033E = 10;
                        else
                        if(_fld033C > k11 && _fld033D > l12)
                            _fld033E = 11;
                        else
                        if(_fld033C == k11 && _fld033D > l12)
                            _fld033E = 12;
                        else
                        if(_fld033C < k11 && _fld033D > l12)
                            _fld033E = 13;
                        else
                        if(_fld033C < k11 && _fld033D == l12)
                            _fld033E = 14;
                        else
                        if(_fld033C < k11 && _fld033D < l12)
                            _fld033E = 15;
                    }
                    if(k7 == 0 && (l9 > 1 || _fld033E != 3))
                        _fld033E = 4;
                    if(_fld0311 == 20 && (_fld033E != 3 || l9 > 1))
                        _fld033E = 5;
                }
                if(_fld0311 == 21)
                {
                    int k5;
                    if(_fld0361 < 16)
                    {
                        String s3 = super._fld03C1[_fld0361];
                        k5 = _fld0387[_fld0361];
                    } else
                    {
                        String s4 = _fld031D[_fld0361 - 16];
                        k5 = _fld0327[_fld0361 - 16];
                    }
                    _fld033E = 7;
                    int l7 = _mth018B(_fld0374, _fld0375, _fld033C, _fld033D) / 2;
                    if(l7 > k5)
                        _fld033E = 4;
                    else
                    if(!_mth0186(_fld0374, _fld0375, _fld033C, _fld033D))
                        _fld033E = 6;
                    if(_fld0331[_fld033C][_fld033D] == 255)
                        _fld033E = 5;
                    else
                    if(_fld0331[_fld033C][_fld033D] == 47)
                    {
                        if(_fld0334[_fld033C][_fld033D] == 255 || _fld0335[_fld033C][_fld033D] == _fld033F)
                            _fld033E = 5;
                    } else
                    if(_fld0332[_fld033C][_fld033D] == _fld033F)
                        _fld033E = 5;
                }
                super.castleSurface2mby._mth01B5(_fld033C * 32 + 10, _fld033D * 32 + 2, _fld033E + 286);
            } else
            {
                _fld033C = -1;
                _fld033D = -1;
                _fld033E = 0;
            }
            if(_fld0392 > 16)
            {
                int i3 = (64 - _fld0392) / 2;
                for(int l5 = 0; l5 < i3; l5++)
                {
                    _mth0178(_fld0393 - l5, _fld0394, _fld0395 * 4 + 3 * ((_fld0392 + l5) % 2));
                    _mth0178(_fld0393 + l5, _fld0394, _fld0395 * 4 + 3 * ((_fld0392 + l5) % 2));
                    _mth0178(_fld0393, _fld0394 - l5, _fld0395 * 4 + 3 * ((_fld0392 + l5) % 2));
                    _mth0178(_fld0393, _fld0394 + l5, _fld0395 * 4 + 3 * ((_fld0392 + l5) % 2));
                }

            }
            for(int j3 = 0; j3 < 300; j3++)
                if(_fld0345[j3] > 0)
                    super.castleSurface2mby._mth01B5(_fld0343[j3] * 32 + 10, _fld0344[j3] * 32 + 2, 178 + _fld0360[j3] * 4 + (_fld0345[j3] / 4) % 4);

            if(_fld03A6 != 0)
            {
                int i6 = _fld03A6;
                if(i6 < 0)
                    i6 = -_fld03A6;
                int i8 = _fld03A1 * 32 + 10;
                int i10 = _fld03A3 * 32 + 2;
                int l11 = _fld03A4 * 32 + 10;
                int i13 = _fld03A5 * 32 + 2;
                int j13 = (i8 * i6 + l11 * (_fld03A7 - i6)) / _fld03A7;
                int k13 = (i10 * i6 + i13 * (_fld03A7 - i6)) / _fld03A7;
                super.castleSurface2mby._mth01B5(j13, k13, 250 + _fld03A8 * 4 + (i6 / 4) % 4);
            }
            if(_fld0311 == 17)
                super.castleSurface2mby._mth01B5(_fld03AE * 32 + 10, _fld03AF * 32 + 2, 302 + (_fld03B0 / 6) % 2);
            if(_fld0311 == 19 && _fld0334[_fld0374][_fld0375] != 255 && !_fld0342)
                if(_fld0374 < 7)
                    super.castleSurface2mby._mth01B5(431, 14, 1);
                else
                    super.castleSurface2mby._mth01B5(14, 14, 1);
            super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
            if(_fld0311 == 19 || _fld0311 == 20)
                _mth0181(super.castleGameGraphics, 14, 7, 484, 289, Color.red);
            else
            if(_fld0311 == 21)
                _mth0181(super.castleGameGraphics, 14, 7, 484, 289, Color.magenta);
            else
                _mth0181(super.castleGameGraphics, 14, 7, 484, 289, Color.blue);
            if(_fld0392 > 0)
            {
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString(_fld0396, 12, 12);
            } else
            if(_fld0319 > 0)
            {
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString(_fld031A, 12, 12);
            } else
            if(_fld0311 == 17)
            {
                String s6 = "";
                if(_fld033B == 35 || _fld033B == 20 || _fld033B == 5)
                    super.castleref.c(2, 63);
                if(_fld033B <= 35)
                    s6 = s6 + super._fld03C1[_fld0336] + ":";
                if(_fld033B <= 20)
                    s6 = s6 + " " + _fld031D[_fld033A] + ":";
                if(_fld033B <= 5)
                    s6 = s6 + " " + _fld0322[_fld033A];
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString(s6, 12, 12);
                if(_fld0336 == _fld033F)
                    super.castleGameGraphics.drawString("Click target square", 225, 12);
                else
                    super.castleGameGraphics.drawString("Please wait...", 225, 12);
            } else
            if(_fld0336 != _fld033F && _fld0311 >= 18 && _fld0311 <= 21)
            {
                String s7 = "";
                if(super._fld03C1[_fld0336] != null)
                    s7 = super._fld03C1[_fld0336].trim() + "'s Turn - Please wait...";
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString(s7, 12, 12);
            } else
            if(_fld0311 == 18)
            {
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString("Your Turn - Click on creature", 12, 12);
                super.castleGameGraphics.setColor(_fld0318);
                if(((GameApplet) (super.castleref)).mouseX > 210 && ((GameApplet) (super.castleref)).mouseY <= 13 && ((GameApplet) (super.castleref)).mouseX < 360)
                    super.castleGameGraphics.setColor(Color.red);
                super.castleGameGraphics.drawString("Click here to end turn", 215, 12);
            } else
            if(_fld0311 == 19)
            {
                if(_fld0361 < 16)
                {
                    String s8 = super._fld03C1[_fld0361];
                    int j10 = _fld0385[_fld0361];
                    int j8 = _fld0389[_fld0361];
                } else
                {
                    String s9 = _fld031D[_fld0361 - 16];
                    int k10 = _fld0325[_fld0361 - 16];
                    int k8 = _fld032A[_fld0361 - 16];
                }
                String s13 = "Movement points left: " + _fld037A / 2;
                if(_fld037A % 2 == 1)
                    s13 = s13 + "\275";
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString(s13, 12, 12);
                super.castleGameGraphics.setColor(_fld0318);
                if(((GameApplet) (super.castleref)).mouseX > 210 && ((GameApplet) (super.castleref)).mouseY <= 13 && ((GameApplet) (super.castleref)).mouseX < 360)
                    super.castleGameGraphics.setColor(Color.red);
                super.castleGameGraphics.drawString("Click here to abort", 225, 12);
            } else
            if(_fld0311 == 20)
            {
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString("Engaged To Enemy!", 12, 12);
                super.castleGameGraphics.setColor(_fld0318);
                if(((GameApplet) (super.castleref)).mouseX > 210 && ((GameApplet) (super.castleref)).mouseY <= 13 && ((GameApplet) (super.castleref)).mouseX < 360)
                    super.castleGameGraphics.setColor(Color.red);
                super.castleGameGraphics.drawString("Click here to abort", 225, 12);
            } else
            if(_fld0311 == 21)
            {
                int l8;
                if(_fld0361 < 16)
                {
                    String s10 = super._fld03C1[_fld0361];
                    l8 = _fld0387[_fld0361];
                } else
                {
                    String s11 = _fld031D[_fld0361 - 16];
                    l8 = _fld0327[_fld0361 - 16];
                }
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString("Ranged Combat: " + l8, 12, 12);
                super.castleGameGraphics.setColor(_fld0318);
                if(((GameApplet) (super.castleref)).mouseX > 210 && ((GameApplet) (super.castleref)).mouseY <= 13 && ((GameApplet) (super.castleref)).mouseX < 360)
                    super.castleGameGraphics.setColor(Color.red);
                super.castleGameGraphics.drawString("Click here to abort", 225, 12);
            }
            if(_fld0392 == 0)
            {
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.drawString("Time-Remaining:" + _fld0339, 378, 12);
            }
        }
        if(_fld030E == 4)
        {
            super.castleSurface2mby._mth01C7(0, 0, 500, 290, Color.black);
            for(int k = 0; k < 15; k++)
            {
                for(int k1 = 0; k1 < 9; k1++)
                {
                    int k3 = _fld0331[k][k1];
                    if(k3 != 255)
                    {
                        int j6 = -1;
                        if(k3 < 16)
                            j6 = 122 + k3 * 4 + _fld02E9[(_fld0312 / 6) % 4];
                        else
                        if(k3 < 46)
                        {
                            k3 -= 16;
                            j6 = 2 + k3 * 4 + _fld02E9[(_fld0312 / 6) % 4];
                        } else
                        {
                            k3 -= 46;
                            j6 = 226 + k3 * 4 + (_fld0312 / 6) % 4;
                        }
                        super.castleSurface2mby._mth01B5(k * 32 + 10, k1 * 32 + 2, j6);
                    } else
                    if(_fld0333[k][k1] != 255)
                        super.castleSurface2mby._mth01B5(k * 32 + 10, k1 * 32 + 2, 2 + _fld0333[k][k1] * 4 + 3);
                }

            }

            if(((GameApplet) (super.castleref)).mouseX > 16 && ((GameApplet) (super.castleref)).mouseY > 13 && ((GameApplet) (super.castleref)).mouseX < 496 && ((GameApplet) (super.castleref)).mouseY < 297 && _fld0336 == _fld033F)
            {
                _fld033C = (((GameApplet) (super.castleref)).mouseX - 16) / 32;
                _fld033D = (((GameApplet) (super.castleref)).mouseY - 9) / 32;
                _fld033E = 1;
                super.castleSurface2mby._mth01B5(_fld033C * 32 + 10, _fld033D * 32 + 2, _fld033E + 286);
            }
            if(_fld0392 > 16)
            {
                int l1 = (64 - _fld0392) / 2;
                for(int l3 = 0; l3 < l1; l3++)
                {
                    _mth0178(_fld0393 - l3, _fld0394, _fld0395 * 4 + 3 * ((_fld0392 + l3) % 2));
                    _mth0178(_fld0393 + l3, _fld0394, _fld0395 * 4 + 3 * ((_fld0392 + l3) % 2));
                    _mth0178(_fld0393, _fld0394 - l3, _fld0395 * 4 + 3 * ((_fld0392 + l3) % 2));
                    _mth0178(_fld0393, _fld0394 + l3, _fld0395 * 4 + 3 * ((_fld0392 + l3) % 2));
                }

            }
            for(int i2 = 0; i2 < 300; i2++)
                if(_fld0345[i2] > 0)
                    super.castleSurface2mby._mth01B5(_fld0343[i2] * 32 + 10, _fld0344[i2] * 32 + 2, 178 + _fld0360[i2] * 4 + (_fld0345[i2] / 4) % 4);

            super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
            _mth0181(super.castleGameGraphics, 14, 7, 484, 289, Color.blue);
            if(_fld0392 > 0)
            {
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.setColor(Color.yellow);
                super.castleGameGraphics.drawString(_fld0396, 12, 12);
            } else
            {
                super.castleGameGraphics.setFont(_fld030D);
                super.castleGameGraphics.setColor(_fld0318);
                if(((GameApplet) (super.castleref)).mouseY <= 13 && ((GameApplet) (super.castleref)).mouseX > 8 && ((GameApplet) (super.castleref)).mouseX < 192)
                    super.castleGameGraphics.setColor(Color.red);
                super.castleGameGraphics.drawString("Back to previous screen", 12, 12);
                super.castleGameGraphics.setColor(_fld0318);
                super.castleGameGraphics.drawString("Time-Remaining:" + _fld0339, 378, 12);
            }
        }
        if(_fld030E == 3 || _fld030E == 7)
        {
            super.castleSurface2mby._mth01B5(0, 0, 0);
            super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
            _mth017D(super.castleGameGraphics, 96, 16, 320, 249, Color.blue);
            int l = 195;
            String s = "";
            if(_fld03AD >= 16)
            {
                int i4 = _fld03AD - 16;
                super.castleGameGraphics.setColor(_fld0318);
                GameDialog.drawstringCenter(super.castleGameGraphics, _fld031D[i4], _fld0307, 256, 34);
                String as[] = {
                    "Beam-down", "Special", "Equip-commander", "Special"
                };
                String as1[] = {
                    "Mechanoid (2)", "Mechanoid (1)", "Neutral", "Alien (1)", "Alien (2)"
                };
                if(_fld031F[i4] != 0)
                {
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Action type: " + as[_fld031F[i4]], _fld0308, 256, 104);
                    int l10 = _fld0320[i4];
                    if(_fld0321[i4] > 0 && _fld039E > 0)
                        l10 += 10 * (_fld039E / _fld039F);
                    if(_fld0321[i4] < 0 && _fld039E < 0)
                        l10 += 10 * (-_fld039E / _fld039F);
                    if(l10 > 100)
                        l10 = 100;
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Success Probability: " + l10 + "%", _fld0308, 256, 127);
                    int i12 = _fld0321[i4];
                    if(i12 >= 3 || i12 <= -3)
                        i12 /= 3;
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Alignment: " + as1[i12 + 2], _fld0308, 256, 149);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Range: " + _fld0322[i4], _fld0308, 256, 171);
                }
                if(_fld031F[i4] == 0)
                {
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Action type: " + as[_fld031F[i4]], _fld0308, 256, 64);
                    int i11 = _fld0320[i4];
                    if(_fld0321[i4] > 0 && _fld039E > 0)
                        i11 += 10 * (_fld039E / _fld039F);
                    if(_fld0321[i4] < 0 && _fld039E < 0)
                        i11 += 10 * (-_fld039E / _fld039F);
                    if(i11 > 100)
                        i11 = 100;
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Beam-down Probability: " + i11 + "%", _fld0308, 256, 87);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Alignment: " + as1[_fld0321[i4] + 2], _fld0308, 256, 109);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Range: " + _fld0322[i4], _fld0308, 256, 131);
                    super.castleGameGraphics.setFont(_fld0306);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Strength:" + _fld0323[i4], _fld030A, 181, 157);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Defence:" + _fld0324[i4], _fld030A, 181, 182);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Speed:" + _fld0325[i4], _fld030A, 181, 207);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Resistance:" + _fld0326[i4], _fld030A, 331, 157);
                    GameDialog.drawstringCenter(super.castleGameGraphics, "Combat-Range:" + _fld0327[i4], _fld030A, 331, 182);
                    String s14 = "";
                    if(_fld0329[i4] == 1)
                        s14 = s14 + "(Slime)";
                    if(_fld032A[i4] == 1)
                        s14 = s14 + "(Flying)";
                    if(_fld032B[i4] == 1)
                        s14 = s14 + "(Transport)";
                    if(s14 == "")
                        s14 = "(no special)";
                    GameDialog.drawstringCenter(super.castleGameGraphics, s14, _fld030A, 331, 207);
                    l = 230;
                }
                s = _fld031E[i4];
            } else
            {
                int j4 = _fld03AD;
                super.castleGameGraphics.setColor(_fld0318);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Commander", _fld0307, 256, 41);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Name: " + super._fld03C1[j4], _fld0308, 256, 64);
                super.castleGameGraphics.setFont(_fld0306);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Strength:" + _fld037E[j4], _fld0306, 181, 107);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Defence:" + _fld0384[j4], _fld0306, 181, 132);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Speed:" + _fld0385[j4], _fld0306, 181, 157);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Resistance:" + _fld0386[j4], _fld0306, 331, 107);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Combat-Range:" + _fld0387[j4], _fld0306, 331, 132);
                String s12 = "";
                if(_fld0389[j4] == 1)
                    s12 = s12 + "(Flying)";
                if(s12 == "")
                    s12 = "(no special)";
                GameDialog.drawstringCenter(super.castleGameGraphics, s12, _fld0306, 331, 157);
                l = 200;
                s = "Commanders, are the most important piece in the game. Kill all the enemy commanders to win!";
            }
            String s5 = "";
            int k6 = 0;
            for(int i9 = 0; i9 < s.length(); i9++)
                if(k6 > 32 && s.charAt(i9) == ' ')
                {
                    GameDialog.drawstringCenter(super.castleGameGraphics, s5, _fld0306, 256, l + 7);
                    l += 17;
                    s5 = "";
                    k6 = 0;
                } else
                {
                    s5 = s5 + s.charAt(i9);
                    k6++;
                }

            if(k6 > 0)
                GameDialog.drawstringCenter(super.castleGameGraphics, s5, _fld0306, 256, l + 7);
            super.castleGameGraphics.setColor(_fld0318);
            if(((GameApplet) (super.castleref)).mouseY < 295 && ((GameApplet) (super.castleref)).mouseY > 275 && ((GameApplet) (super.castleref)).mouseX > 64 && ((GameApplet) (super.castleref)).mouseX < 192)
                super.castleGameGraphics.setColor(Color.red);
            GameDialog.drawstringCenter(super.castleGameGraphics, "Back to previous screen", _fld030B, 120, 285);
            super.castleGameGraphics.setColor(_fld0318);
            GameDialog.drawstringCenter(super.castleGameGraphics, "Time-Remaining:" + _fld0339, _fld030B, 392, 285);
        }
        if(_fld030E == 2)
        {
            super.castleSurface2mby._mth01B5(0, 0, 0);
            if(_fld032D)
            {
                int i1 = 20;
                int j2 = 46;
                for(int k4 = 0; k4 < 18; k4++)
                {
                    int l6 = _fld032E[k4];
                    if(l6 != 255)
                    {
                        int j9 = -1;
                        if(_fld031F[l6] == 0)
                            j9 = 2 + l6 * 4 + _fld02E9[(_fld0312 / 6) % 4];
                        if(_fld031F[l6] == 1)
                            j9 = 226 + (l6 - 30) * 4 + (_fld0312 / 6) % 4;
                        if(_fld031F[l6] == 2)
                            j9 = 122 + ((l6 - 36) + 8) * 4 + _fld02E9[(_fld0312 / 6) % 4];
                        if(_fld031F[l6] == 3)
                        {
                            byte byte0 = 2;
                            if(l6 == 42 || l6 == 46)
                                byte0 = 6;
                            if(l6 == 43 || l6 == 47)
                                byte0 = 5;
                            if(l6 == 44 || l6 == 45)
                                byte0 = 0;
                            if(l6 == 53 || l6 == 54)
                                byte0 = 7;
                            j9 = (178 + byte0 * 4 + 3) - (_fld0312 / 4) % 4;
                        }
                        if(j9 != -1)
                            super.castleSurface2mby._mth01B5((i1 + 1) - 6, (j2 + 1) - 6, j9);
                        if((j2 += 34) > 230)
                        {
                            j2 = 46;
                            i1 += 160;
                        }
                    }
                }

            }
            super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
            Color color = new Color(0, 103, 128);
            Color color1 = new Color(1, 166, 201);
            Color color2 = new Color(192, 192, 192);
            int i7 = 20;
            int k9 = 46;
            _fld0330 = 0;
            for(int j11 = 0; j11 < 18; j11++)
            {
                int j12 = _fld032E[j11];
                if(j12 != 255)
                {
                    _fld032F[_fld0330++] = j12;
                    super.castleGameGraphics.setColor(Color.black);
                    super.castleGameGraphics.fillRect(i7 + 34, k9, 118, 34);
                    super.castleGameGraphics.setColor(color);
                    super.castleGameGraphics.drawRect(i7, k9, 152, 34);
                    super.castleGameGraphics.drawRect(i7, k9, 34, 34);
                    super.castleGameGraphics.setColor(color2);
                    if(_fld032D)
                        GameDialog.drawstringCenter(super.castleGameGraphics, _fld031D[j12], _fld030D, i7 + 93, k9 + 18);
                    if((k9 += 34) > 230)
                    {
                        k9 = 46;
                        i7 += 160;
                    }
                }
            }

            super.castleGameGraphics.setColor(color1);
            GameDialog.drawstringCenter(super.castleGameGraphics, "Choose an action - For more info right click an icon", _fld0309, 256, 34);
            int k12 = _fld039E / _fld039F;
            super.castleGameGraphics.setColor(color2);
            if(k12 == 0)
                GameDialog.drawstringCenter(super.castleGameGraphics, "World alignment - Neutral", _fld030B, 256, 267);
            else
            if(k12 < 0)
                GameDialog.drawstringCenter(super.castleGameGraphics, "World alignment - Mechanoid " + -k12, _fld030B, 256, 267);
            else
            if(k12 > 0)
                GameDialog.drawstringCenter(super.castleGameGraphics, "World alignment - Alien " + k12, _fld030B, 256, 267);
            if(_fld0311 == 12)
            {
                super.castleGameGraphics.setColor(color2);
                if(((GameApplet) (super.castleref)).mouseY > 275 && ((GameApplet) (super.castleref)).mouseY < 295 && ((GameApplet) (super.castleref)).mouseX > 64 && ((GameApplet) (super.castleref)).mouseX < 192)
                    super.castleGameGraphics.setColor(Color.red);
                GameDialog.drawstringCenter(super.castleGameGraphics, "View Game-Board", _fld030B, 120, 285);
                super.castleGameGraphics.setColor(color2);
                if(((GameApplet) (super.castleref)).mouseY > 275 && ((GameApplet) (super.castleref)).mouseY < 295 && ((GameApplet) (super.castleref)).mouseX > 192 && ((GameApplet) (super.castleref)).mouseX < 320)
                    super.castleGameGraphics.setColor(Color.red);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Skip-Action", _fld030B, 256, 285);
                super.castleGameGraphics.setColor(color2);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Time-Remaining:" + _fld0339, _fld030B, 392, 285);
            }
            if(_fld0311 == 15)
            {
                super.castleGameGraphics.setColor(Color.white);
                if(((GameApplet) (super.castleref)).mouseY > 275 && ((GameApplet) (super.castleref)).mouseY < 295 && ((GameApplet) (super.castleref)).mouseX > 64 && ((GameApplet) (super.castleref)).mouseX < 192)
                    super.castleGameGraphics.setColor(Color.red);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Project Hologram", _fld030B, 120, 285);
                super.castleGameGraphics.setColor(Color.white);
                if(((GameApplet) (super.castleref)).mouseY > 275 && ((GameApplet) (super.castleref)).mouseY < 295 && ((GameApplet) (super.castleref)).mouseX > 192 && ((GameApplet) (super.castleref)).mouseX < 320)
                    super.castleGameGraphics.setColor(Color.red);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Beam-down", _fld030B, 256, 285);
                super.castleGameGraphics.setColor(Color.white);
                if(((GameApplet) (super.castleref)).mouseY > 275 && ((GameApplet) (super.castleref)).mouseY < 295 && ((GameApplet) (super.castleref)).mouseX > 320 && ((GameApplet) (super.castleref)).mouseX < 448)
                    super.castleGameGraphics.setColor(Color.red);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Cancel", _fld030B, 392, 285);
            }
            if(_fld0311 == 16)
            {
                super.castleGameGraphics.setColor(color2);
                String s15;
                if(_fld032C == -1)
                    s15 = "No action";
                else
                    s15 = _fld031D[_fld032F[_fld032C]];
                GameDialog.drawstringCenter(super.castleGameGraphics, "Selected:" + s15, _fld030C, 104, 285);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Awaiting other players", _fld030C, 272, 285);
                GameDialog.drawstringCenter(super.castleGameGraphics, "Time-Remaining:" + _fld0339, _fld030C, 424, 285);
            }
        }
    }

    public void _mth017D(Graphics g, int i, int k, int l, int i1, Color color)
    {
        g.setColor(Color.black);
        g.fillRect(i, k, l, i1);
        int j1 = color.getRed();
        int k1 = color.getGreen();
        int l1 = color.getBlue();
        for(int i2 = 7; i2 >= 0; i2--)
        {
            int j2 = j1 - i2 * 32;
            if(j2 < 0)
                j2 = 0;
            int k2 = k1 - i2 * 32;
            if(k2 < 0)
                k2 = 0;
            int l2 = l1 - i2 * 32;
            if(l2 < 0)
                l2 = 0;
            g.setColor(new Color(j2, k2, l2));
            g.drawRect(i - i2, k - i2, l + i2 + i2, i1 + i2 + i2);
            int i3 = i2 / 2;
            g.drawRect(i + i3, k + i3, l - i3 - i3, i1 - i3 - i3);
        }

    }

    public void _mth0181(Graphics g, int i, int k, int l, int i1, Color color)
    {
        int j1 = color.getRed();
        int k1 = color.getGreen();
        int l1 = color.getBlue();
        for(int i2 = 7; i2 >= 0; i2--)
        {
            int j2 = j1 - i2 * 32;
            if(j2 < 0)
                j2 = 0;
            int k2 = k1 - i2 * 32;
            if(k2 < 0)
                k2 = 0;
            int l2 = l1 - i2 * 32;
            if(l2 < 0)
                l2 = 0;
            g.setColor(new Color(j2, k2, l2));
            g.drawLine(i - i2, k - i2, i + l + i2, k - i2);
            g.drawLine(i - i2, k - i2, i - i2, k + i1);
            g.drawLine(i + l + i2, k - i2, i + l + i2, k + i1);
            int i3 = i2 / 2;
            g.drawLine(i + i3, k + i3, (i + l) - i3, k + i3);
            g.drawLine(i + i3, k + i3, i + i3, k + i1);
            g.drawLine((i + l) - i3, k + i3, (i + l) - i3, k + i1);
        }

    }

    public void _mth0185(int i)
    {
        if(i < 0 || i >= _fld031C)
        {
            return;
        } else
        {
            _fld03AD = i + 16;
            _fld030E = 3;
            _fld0311 = 13;
            return;
        }
    }

    public void _mth0187(int i, int k)
    {
        if(i < 0 || i >= _fld031C + 16)
            return;
        if(i >= 16)
            _fld03AD = i;
        else
            _fld03AD = k;
        _fld030E = 7;
        _fld0311 = 22;
    }

    public void _mth0189()
    {
        _fld030E = 4;
        _fld0311 = 14;
    }

    public void _mth017F(int i, int k, int l)
    {
        if(l >= 21 && l <= 24)
        {
            return;
        } else
        {
            _fld0333[i][k] = l;
            return;
        }
    }

    public void _mth0177(int i)
    {
        int k = _fld032F[i];
        _fld032C = i;
        if(_fld031F[k] == 0)
        {
            _fld030E = 2;
            _fld0311 = 15;
            return;
        } else
        {
            _mth018C(i, 0);
            return;
        }
    }

    public void _mth018C(int i, int k)
    {
        super.castleStream.newPacket(255);
        super.castleStream.putByte(i);
        super.castleStream.putByte(k);
        super.castleStream.sendPacket();
        _fld032C = i;
        _fld030E = 2;
        _fld0311 = 16;
    }

    public void _mth017C(int i, int k, int l)
    {
        if(i == -1 || k == -1 || l != 1)
            return;
        if(_fld0322[_fld033A] == 0)
        {
            i = _fld0340;
            k = _fld0341;
        }
        super.castleStream.newPacket(254);
        super.castleStream.putByte(i);
        super.castleStream.putByte(k);
        super.castleStream.sendPacket();
        super.castleref.c(12, 63);
        _fld0343[0] = i;
        _fld0344[0] = k;
        _fld0345[0] = 50;
        _fld0360[0] = 2;
    }

    public void _mth0188(int i, int k, int l)
    {
        if(i == -1 || k == -1)
            return;
        if(l == 4 || l == 5 || l == 6)
        {
            return;
        } else
        {
            super.castleStream.newPacket(251);
            super.castleStream.putByte(i);
            super.castleStream.putByte(k);
            super.castleStream.sendPacket();
            _fld0343[0] = i;
            _fld0344[0] = k;
            _fld0345[0] = 15;
            _fld0360[0] = 10;
            return;
        }
    }

    public void _mth018A(int i, int k, int l)
    {
        if(i == -1 || k == -1)
            return;
        if(l == 4 || l == 5 || l == 6)
        {
            return;
        } else
        {
            super.castleStream.newPacket(247);
            super.castleStream.putByte(i);
            super.castleStream.putByte(k);
            super.castleStream.sendPacket();
            _fld0343[0] = i;
            _fld0344[0] = k;
            _fld0345[0] = 15;
            _fld0360[0] = 10;
            return;
        }
    }

    public void _mth0179(int i, int k)
    {
        super.castleStream.newPacket(249);
        super.castleStream.putByte(i);
        super.castleStream.putByte(k);
        super.castleStream.sendPacket();
    }

    public void _mth017B(int i, int k, int l)
    {
        if(i == -1 || k == -1 || l != 1)
            return;
        if(_fld0331[i][k] == 255 || _fld0332[i][k] != _fld033F)
        {
            return;
        } else
        {
            super.castleStream.newPacket(252);
            super.castleStream.putByte(i);
            super.castleStream.putByte(k);
            super.castleStream.sendPacket();
            _fld0343[0] = i;
            _fld0344[0] = k;
            _fld0345[0] = 15;
            _fld0360[0] = 10;
            return;
        }
    }

    public void _mth0180()
    {
        super.castleStream.newPacket(253);
        super.castleStream.sendPacket();
    }

    public void _mth0184()
    {
        super.castleStream.newPacket(248);
        super.castleStream.sendPacket();
    }

    public void _mth0183()
    {
        super.castleStream.newPacket(250);
        super.castleStream.sendPacket();
    }

    public void _mth017E()
    {
        super.castleStream.newPacket(246);
        super.castleStream.sendPacket();
        _fld0311 = 18;
        _fld030E = 5;
        super.castleref.c(3, 63);
    }

    public boolean _mth0186(int i, int k, int l, int i1)
    {
        if(i < 0 || k < 0 || i >= 15 || k >= 9)
            return false;
        if(l < 0 || i1 < 0 || l >= 15 || i1 >= 9)
            return false;
        if(i == l && k == i1)
            return true;
        int j1 = i - l;
        if(j1 < 0)
            j1 = -j1;
        int k1 = k - i1;
        if(k1 < 0)
            k1 = -k1;
        int l1 = (i << 8) + 128;
        int i2 = (k << 8) + 128;
        int j2 = 256;
        int k2 = 256;
        int l2;
        if(j1 > k1)
        {
            k2 = (k1 << 8) / j1;
            l2 = j1;
        } else
        {
            j2 = (j1 << 8) / k1;
            l2 = k1;
        }
        if(l < i)
            j2 = -j2;
        if(i1 < k)
            k2 = -k2;
        for(int i3 = 0; i3 < l2 - 1; i3++)
        {
            l1 += j2;
            i2 += k2;
            if(_fld0331[l1 - 4 >> 8][i2 - 4 >> 8] != 255 || _fld0331[l1 + 4 >> 8][i2 - 4 >> 8] != 255 || _fld0331[l1 - 4 >> 8][i2 + 4 >> 8] != 255 || _fld0331[l1 + 4 >> 8][i2 + 4 >> 8] != 255)
                return false;
        }

        return true;
    }

    public int _mth018B(int i, int k, int l, int i1)
    {
        int j1 = i - l;
        if(j1 < 0)
            j1 = -j1;
        int k1 = k - i1;
        if(k1 < 0)
            k1 = -k1;
        int l1 = 0;
        for(; j1 > 0 && k1 > 0; k1--)
        {
            l1 += 3;
            j1--;
        }

        return l1 + j1 + j1 + k1 + k1;
    }

    public void _mth0178(int i, int k, int l)
    {
        if(i < 0 || k < 0 || i >= 15 || k >= 9)
        {
            return;
        } else
        {
            super.castleSurface2mby._mth01B5(i * 32 + 10, k * 32 + 2, 122 + l);
            return;
        }
    }

    public Cyber()
    {
        _fld02E8 = false;
        _fld0300 = false;
        _fld030F = "";
        _fld0310 = "";
        _fld0317 = new Color(250, 250, 250);
        _fld0318 = new Color(230, 230, 230);
        _fld031A = "";
        _fld031B = Color.red;
        _fld031C = 55;
        _fld032D = false;
        _fld032E = new int[18];
        _fld032F = new int[18];
        _fld0331 = new int[15][9];
        _fld0332 = new int[15][9];
        _fld0333 = new int[15][9];
        _fld0334 = new int[15][9];
        _fld0335 = new int[15][9];
        _fld0337 = -1;
        _fld0338 = -1;
        _fld033C = -1;
        _fld033D = -1;
        _fld0342 = false;
        _fld0343 = new int[300];
        _fld0344 = new int[300];
        _fld0345 = new int[300];
        _fld0360 = new int[300];
        _fld037E = new int[8];
        _fld0384 = new int[8];
        _fld0385 = new int[8];
        _fld0386 = new int[8];
        _fld0387 = new int[8];
        _fld0388 = new int[8];
        _fld0389 = new int[8];
        _fld0396 = "";
        _fld039F = 3;
        _fld03A7 = 25;
        _fld03AA = new int[100];
        _fld03AB = 18;
        _fld03AC = 5;
    }

    boolean _fld02E8;
    int _fld02E9[] = {
        0, 1, 2, 1
    };
    boolean _fld0300;
    static boolean _fld0301 = true;
    Font _fld0302;
    Font _fld0303;
    Font _fld0304;
    Font _fld0305;
    Font _fld0306;
    Font _fld0307;
    Font _fld0308;
    Font _fld0309;
    Font _fld030A;
    Font _fld030B;
    Font _fld030C;
    Font _fld030D;
    int _fld030E;
    String _fld030F;
    String _fld0310;
    int _fld0311;
    int _fld0312;
    int _fld0313;
    int _fld0314;
    int _fld0315;
    int _fld0316;
    Color _fld0317;
    Color _fld0318;
    int _fld0319;
    String _fld031A;
    Color _fld031B;
    int _fld031C;
    String _fld031D[];
    String _fld031E[];
    int _fld031F[];
    int _fld0320[];
    int _fld0321[];
    int _fld0322[];
    int _fld0323[];
    int _fld0324[];
    int _fld0325[];
    int _fld0326[];
    int _fld0327[];
    int _fld0328[];
    int _fld0329[];
    int _fld032A[];
    int _fld032B[];
    int _fld032C;
    boolean _fld032D;
    int _fld032E[];
    int _fld032F[];
    int _fld0330;
    int _fld0331[][];
    int _fld0332[][];
    int _fld0333[][];
    int _fld0334[][];
    int _fld0335[][];
    int _fld0336;
    int _fld0337;
    int _fld0338;
    int _fld0339;
    int _fld033A;
    int _fld033B;
    int _fld033C;
    int _fld033D;
    int _fld033E;
    int _fld033F;
    int _fld0340;
    int _fld0341;
    boolean _fld0342;
    int _fld0343[];
    int _fld0344[];
    int _fld0345[];
    int _fld0360[];
    int _fld0361;
    int _fld0374;
    int _fld0375;
    int _fld037A;
    int _fld037E[];
    int _fld0384[];
    int _fld0385[];
    int _fld0386[];
    int _fld0387[];
    int _fld0388[];
    int _fld0389[];
    int _fld038A;
    int _fld038C;
    int _fld038E;
    int _fld038F;
    int _fld0390;
    int _fld0391;
    int _fld0392;
    int _fld0393;
    int _fld0394;
    int _fld0395;
    String _fld0396;
    int _fld0397;
    int _fld0398;
    int _fld0399;
    int _fld039A;
    int _fld039B;
    int _fld039C;
    int _fld039D;
    int _fld039E;
    int _fld039F;
    int _fld03A0;
    int _fld03A1;
    int _fld03A3;
    int _fld03A4;
    int _fld03A5;
    int _fld03A6;
    int _fld03A7;
    int _fld03A8;
    int _fld03A9;
    int _fld03AA[];
    int _fld03AB;
    int _fld03AC;
    int _fld03AD;
    int _fld03AE;
    int _fld03AF;
    int _fld03B0;

}

