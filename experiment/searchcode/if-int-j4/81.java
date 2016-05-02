// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   chessgame.java

import jagex.*;
import java.awt.*;
import java.io.IOException;

public class Chess extends Game
{

    public boolean _mth0195()
    {
        return x <= 0;
    }

    public void _mth01A2(int i1, int j1)
        throws IOException
    {
        if(i1 == 255)
        {
            C4 = false;
            int k1 = super.castleStream.getShort(super.castlePdata, 1);
            int i2 = super.castleStream.getShort(super.castlePdata, 3);
            if(i2 > h)
                C5 = false;
            h = i2;
            if(!C5 || g != -1)
            {
                if(g != k1)
                    R();
                g = k1;
            }
            return;
        }
        if(i1 == 254)
        {
            for(int l1 = 0; l1 < 8; l1++)
            {
                for(int j2 = 0; j2 < 8; j2++)
                    j[l1][j2] = super.castleStream.byte2int(super.castlePdata[1 + l1 * 8 + j2]);

            }

            C5 = false;
            u = -1;
            v = -1;
            O();
            R();
            return;
        }
        if(i1 == 252)
        {
            y = super.castleStream.byte2int(super.castlePdata[1]);
            z = super.castleStream.byte2int(super.castlePdata[2]);
            C0 = super.castleStream.byte2int(super.castlePdata[3]);
            C1 = super.castleStream.byte2int(super.castlePdata[4]);
            C2 = j[y][z];
            j[y][z] = 0;
            O();
            Q(y, z, C0, C1, C2);
            return;
        }
        if(i1 == 251)
        {
            CC = true;
            C4 = true;
        }
    }

    public void _mth0199()
    {
        super._fld03C3 = 2;
        super.castleSurface2mby._mth01CF();
        super.castleSurface2mby._mth01B7("checkers/board.jpg", 0, false);
        super.castleSurface2mby._mth01D4("chess/all.gif", 1, true, 6, 45, 91);
        for(int i1 = 0; i1 < 6; i1++)
        {
            super.castleSurface2mby._mth01B6(i1 + 1, i1 + 7);
            super.castleSurface2mby._mth01C5(i1 + 7);
        }

        super.castleSurface2mby._mth01D4("chess/allr.gif", 13, true, 6, 45, 75);
        for(int j1 = 0; j1 < 6; j1++)
        {
            super.castleSurface2mby._mth01B6(j1 + 13, j1 + 19);
            super.castleSurface2mby._mth01C5(j1 + 19);
        }

        super.castleSurface2mby._mth01B7("chess/sel.gif", 26, true);
    }

    public void _mth0191()
    {
        super.castleSurface2mby._fld0414 = 279;
        super.castleSurface2mby._fld0413 = 0;
        for(int i1 = 0; i1 < 8; i1++)
        {
            for(int j1 = 0; j1 < 8; j1++)
                j[i1][j1] = 0;

        }

        u = -1;
        v = -1;
        w = 0;
        x = 0;
        C6 = true;
        C7 = true;
        C8 = -1;
        CA = false;
        CB = false;
        C5 = false;
        g = 0;
        CC = false;
        R();
    }

    public void _mth0197()
    {
        i++;
        if(x > 0)
        {
            x--;
            if(x == 0)
            {
                super.castleref.c(16, 63);
                j[C0][C1] = C2;
                O();
            }
        }
        if(!CC)
        {
            V(((GameApplet) (super.castleref)).mouseX, ((GameApplet) (super.castleref)).mouseY);
            if(g == super.castleId && k != -1 && u == -1 && ((GameApplet) (super.castleref)).lastMouseButtonDown == 1)
                D(k, l);
            if(g == super.castleId && u != -1 && ((GameApplet) (super.castleref)).lastMouseButtonDown == 2)
                S();
            if(g == super.castleId && u != -1 && ((GameApplet) (super.castleref)).lastMouseButtonDown == 1)
                K(k, l);
            super.castleref.lastMouseButtonDown = 0;
        }
        if(CC && ((GameApplet) (super.castleref)).lastMouseButtonDown == 1)
        {
            if(((GameApplet) (super.castleref)).mouseX > 155 && ((GameApplet) (super.castleref)).mouseX < 205)
            {
                super.castleStream.newPacket(254);
                super.castleStream.putByte(2);
                super.castleStream.sendPacket();
            }
            if(((GameApplet) (super.castleref)).mouseX > 205 && ((GameApplet) (super.castleref)).mouseX < 255)
            {
                super.castleStream.newPacket(254);
                super.castleStream.putByte(3);
                super.castleStream.sendPacket();
            }
            if(((GameApplet) (super.castleref)).mouseX > 255 && ((GameApplet) (super.castleref)).mouseX < 305)
            {
                super.castleStream.newPacket(254);
                super.castleStream.putByte(4);
                super.castleStream.sendPacket();
            }
            if(((GameApplet) (super.castleref)).mouseX > 305 && ((GameApplet) (super.castleref)).mouseX < 355)
            {
                super.castleStream.newPacket(254);
                super.castleStream.putByte(5);
                super.castleStream.sendPacket();
            }
            CC = false;
            C4 = true;
        }
    }

    public void I()
    {
        int i1 = u;
        int j1 = v;
        r = 0;
        for(int k1 = 0; k1 < m; k1++)
            if(i1 == n[k1] && j1 == o[k1])
            {
                s[r] = p[k1];
                t[r++] = q[k1];
            }

    }

    public void R()
    {
        C4 = true;
        m = 0;
        r = 0;
        J(super.castleId);
        for(int i1 = 0; i1 < m; i1++)
        {
            int j1;
            for(j1 = 0; j1 < r; j1++)
                if(s[j1] == n[i1] && t[j1] == o[i1])
                    break;

            if(j1 == r)
            {
                s[r] = n[i1];
                t[r++] = o[i1];
            }
        }

    }

    public void J(int i1)
    {
        int j1 = i1 * 6;
        for(int k2 = 0; k2 < 8; k2++)
        {
            for(int l2 = 0; l2 < 8; l2++)
                if(j[k2][l2] != 0)
                {
                    if(j[k2][l2] == 6 + j1)
                    {
                        if(i1 == 0)
                        {
                            if(G(k2, l2 + 1))
                                T(k2, l2, k2, l2 + 1);
                            if(l2 == 1 && G(k2, l2 + 1) && G(k2, l2 + 2))
                                T(k2, l2, k2, l2 + 2);
                            if(M(k2 - 1, l2 + 1, i1))
                                T(k2, l2, k2 - 1, l2 + 1);
                            if(M(k2 + 1, l2 + 1, i1))
                                T(k2, l2, k2 + 1, l2 + 1);
                            if(C8 == k2 - 1 && C9 == l2 && G(k2 - 1, l2 + 1) && M(k2 - 1, l2, i1))
                                T(k2, l2, k2 - 1, l2 + 1);
                            if(C8 == k2 + 1 && C9 == l2 && G(k2 + 1, l2 + 1) && M(k2 + 1, l2, i1))
                                T(k2, l2, k2 + 1, l2 + 1);
                        } else
                        if(i1 == 1)
                        {
                            if(G(k2, l2 - 1))
                                T(k2, l2, k2, l2 - 1);
                            if(l2 == 6 && G(k2, l2 - 1) && G(k2, l2 - 2))
                                T(k2, l2, k2, l2 - 2);
                            if(M(k2 - 1, l2 - 1, i1))
                                T(k2, l2, k2 - 1, l2 - 1);
                            if(M(k2 + 1, l2 - 1, i1))
                                T(k2, l2, k2 + 1, l2 - 1);
                            if(C8 == k2 - 1 && C9 == l2 && G(k2 - 1, l2 - 1) && M(k2 - 1, l2, i1))
                                T(k2, l2, k2 - 1, l2 - 1);
                            if(C8 == k2 + 1 && C9 == l2 && G(k2 + 1, l2 - 1) && M(k2 + 1, l2, i1))
                                T(k2, l2, k2 + 1, l2 - 1);
                        }
                    } else
                    if(j[k2][l2] == 5 + j1 || j[k2][l2] == 2 + j1)
                    {
                        int k1 = k2 - 1;
                        int i2;
                        for(i2 = l2 - 1; G(k1, i2); i2--)
                        {
                            T(k2, l2, k1, i2);
                            k1--;
                        }

                        if(M(k1, i2, i1))
                            T(k2, l2, k1, i2);
                        k1 = k2 + 1;
                        for(i2 = l2 - 1; G(k1, i2); i2--)
                        {
                            T(k2, l2, k1, i2);
                            k1++;
                        }

                        if(M(k1, i2, i1))
                            T(k2, l2, k1, i2);
                        k1 = k2 - 1;
                        for(i2 = l2 + 1; G(k1, i2); i2++)
                        {
                            T(k2, l2, k1, i2);
                            k1--;
                        }

                        if(M(k1, i2, i1))
                            T(k2, l2, k1, i2);
                        k1 = k2 + 1;
                        for(i2 = l2 + 1; G(k1, i2); i2++)
                        {
                            T(k2, l2, k1, i2);
                            k1++;
                        }

                        if(M(k1, i2, i1))
                            T(k2, l2, k1, i2);
                    } else
                    if(j[k2][l2] == 4 + j1)
                    {
                        if(N(k2 - 1, l2 + 2, i1))
                            T(k2, l2, k2 - 1, l2 + 2);
                        if(N(k2 + 1, l2 + 2, i1))
                            T(k2, l2, k2 + 1, l2 + 2);
                        if(N(k2 - 1, l2 - 2, i1))
                            T(k2, l2, k2 - 1, l2 - 2);
                        if(N(k2 + 1, l2 - 2, i1))
                            T(k2, l2, k2 + 1, l2 - 2);
                        if(N(k2 + 2, l2 - 1, i1))
                            T(k2, l2, k2 + 2, l2 - 1);
                        if(N(k2 + 2, l2 + 1, i1))
                            T(k2, l2, k2 + 2, l2 + 1);
                        if(N(k2 - 2, l2 - 1, i1))
                            T(k2, l2, k2 - 2, l2 - 1);
                        if(N(k2 - 2, l2 + 1, i1))
                            T(k2, l2, k2 - 2, l2 + 1);
                    }
                    if(j[k2][l2] == 3 + j1 || j[k2][l2] == 2 + j1)
                    {
                        int l1 = k2 - 1;
                        int j2;
                        for(j2 = l2; G(l1, j2); l1--)
                            T(k2, l2, l1, j2);

                        if(M(l1, j2, i1))
                            T(k2, l2, l1, j2);
                        l1 = k2 + 1;
                        for(j2 = l2; G(l1, j2); l1++)
                            T(k2, l2, l1, j2);

                        if(M(l1, j2, i1))
                            T(k2, l2, l1, j2);
                        l1 = k2;
                        for(j2 = l2 - 1; G(l1, j2); j2--)
                            T(k2, l2, l1, j2);

                        if(M(l1, j2, i1))
                            T(k2, l2, l1, j2);
                        l1 = k2;
                        for(j2 = l2 + 1; G(l1, j2); j2++)
                            T(k2, l2, l1, j2);

                        if(M(l1, j2, i1))
                            T(k2, l2, l1, j2);
                    } else
                    if(j[k2][l2] == 1 + j1)
                    {
                        if(N(k2 - 1, l2 - 1, i1))
                            T(k2, l2, k2 - 1, l2 - 1);
                        if(N(k2, l2 - 1, i1))
                            T(k2, l2, k2, l2 - 1);
                        if(N(k2 + 1, l2 - 1, i1))
                            T(k2, l2, k2 + 1, l2 - 1);
                        if(N(k2 - 1, l2, i1))
                            T(k2, l2, k2 - 1, l2);
                        if(N(k2 + 1, l2, i1))
                            T(k2, l2, k2 + 1, l2);
                        if(N(k2 - 1, l2 + 1, i1))
                            T(k2, l2, k2 - 1, l2 + 1);
                        if(N(k2, l2 + 1, i1))
                            T(k2, l2, k2, l2 + 1);
                        if(N(k2 + 1, l2 + 1, i1))
                            T(k2, l2, k2 + 1, l2 + 1);
                        if(!CB)
                        {
                            if(C6 && G(k2 - 1, l2) && G(k2 - 2, l2) && G(k2 - 3, l2) && !H(0, 0, 0, 0) && !H(k2, l2, k2 - 1, l2))
                                T(k2, l2, k2 - 2, l2);
                            if(C7 && G(k2 + 1, l2) && G(k2 + 2, l2) && !H(0, 0, 0, 0) && !H(k2, l2, k2 + 1, l2))
                                T(k2, l2, k2 + 2, l2);
                        }
                    }
                }

        }

    }

    public boolean N(int i1, int j1, int k1)
    {
        return G(i1, j1) || M(i1, j1, k1);
    }

    public boolean G(int i1, int j1)
    {
        if(i1 < 0 || j1 < 0 || i1 >= 8 || j1 >= 8)
            return false;
        return j[i1][j1] == 0;
    }

    public boolean M(int i1, int j1, int k1)
    {
        if(i1 < 0 || j1 < 0 || i1 >= 8 || j1 >= 8)
            return false;
        if(j[i1][j1] == 0)
            return false;
        return (j[i1][j1] - 1) / 6 != k1;
    }

    public void T(int i1, int j1, int k1, int l1)
    {
        if(j[k1][l1] == 1 || j[k1][l1] == 7)
            CA = true;
        if(!CB)
        {
            if(H(i1, j1, k1, l1))
                return;
            n[m] = i1;
            o[m] = j1;
            p[m] = k1;
            q[m++] = l1;
        }
    }

    public boolean H(int i1, int j1, int k1, int l1)
    {
        int i2 = j[i1][j1];
        int j2 = j[k1][l1];
        j[i1][j1] = 0;
        j[k1][l1] = i2;
        CA = false;
        CB = true;
        J(1 - super.castleId);
        CB = false;
        j[i1][j1] = i2;
        j[k1][l1] = j2;
        return CA;
    }

    public void D(int i1, int j1)
    {
        if(i1 == -1 || j1 == -1)
            return;
        if(u != -1)
            return;
        if(g != super.castleId)
            return;
        for(int k1 = 0; k1 < r; k1++)
        {
            if(s[k1] == i1 && t[k1] == j1)
                break;
            if(k1 == r - 1)
                return;
        }

        u = i1;
        v = j1;
        w = j[i1][j1];
        I();
    }

    public void S()
    {
        if(u == -1)
            return;
        if(g != super.castleId)
        {
            return;
        } else
        {
            u = -1;
            v = -1;
            R();
            return;
        }
    }

    public void K(int i1, int j1)
    {
        if(i1 == -1 || j1 == -1 || h == 0)
            return;
        if(u == -1)
            return;
        if(C5)
            return;
        if(g != super.castleId)
            return;
        for(int k1 = 0; k1 < m; k1++)
        {
            if(n[k1] == u && o[k1] == v && p[k1] == i1 && q[k1] == j1)
                break;
            if(k1 == m - 1)
                return;
        }

        g = -1;
        C5 = true;
        super.castleStream.newPacket(255);
        super.castleStream.putByte(u);
        super.castleStream.putByte(v);
        super.castleStream.putByte(i1);
        super.castleStream.putByte(j1);
        super.castleStream.sendPacket();
        y = u;
        z = v;
        C0 = i1;
        C1 = j1;
        C2 = j[y][z];
        j[y][z] = 0;
        O();
        u = -1;
        v = -1;
        Q(y, z, C0, C1, C2);
    }

    public void Q(int i1, int j1, int k1, int l1, int i2)
    {
        int j2 = Math.abs(k1 - i1);
        int k2 = Math.abs(l1 - j1);
        int l2 = j2;
        if(k2 > l2)
            l2 = k2;
        C3 = 20 + l2 * 5;
        x = C3;
        if(j1 == super.castleId * 7 && i1 == 0)
            C6 = false;
        if(j1 == super.castleId * 7 && i1 == 7)
            C7 = false;
        if(i2 == 1 + super.castleId * 6)
        {
            C6 = false;
            C7 = false;
        }
        C8 = -1;
        if((i2 == 6 || i2 == 12) && (l1 - j1 == 2 || j1 - l1 == 2))
        {
            C8 = k1;
            C9 = l1;
        }
    }

    public void V(int i1, int j1)
    {
        i1 -= 6;
        if((j1 -= 6) < b[0] || j1 >= b[8])
        {
            l = -1;
            k = -1;
            return;
        }
        l = 0;
        for(int k1 = 0; k1 < 8; k1++)
        {
            if(j1 >= b[k1 + 1])
                continue;
            l = k1;
            break;
        }

        if(super.castleId == 0)
            l = 7 - l;
        int ai[] = new int[9];
        for(int l1 = 0; l1 < 9; l1++)
            ai[l1] = (c[l1] * (b[8] - j1) + d[l1] * (j1 - b[0])) / (b[8] - b[0]);

        super.castleAppletGraphics.setColor(Color.white);
        if(i1 < ai[0] || i1 >= ai[8])
        {
            l = -1;
            k = -1;
            return;
        }
        k = 0;
        for(int i2 = 0; i2 < 8; i2++)
        {
            if(i1 >= ai[i2 + 1])
                continue;
            k = i2;
            break;
        }

        if(super.castleId == 1)
            k = 7 - k;
    }

    public void P(int i1, int j1)
    {
        if(super.castleId == 0)
            j1 = 7 - j1;
        if(super.castleId == 1)
            i1 = 7 - i1;
        int k1 = (b[j1] + b[j1 + 1]) / 2;
        int l1 = (c[i1] + c[i1 + 1]) / 2;
        int i2 = (d[i1] + d[i1 + 1]) / 2;
        int j2 = (l1 * (b[8] - k1 - 4) + i2 * ((k1 + 4) - b[0])) / (b[8] - b[0]);
        int k2 = 32 + (21 * j1) / 7;
        int l2 = 21 + (20 * j1) / 7;
        super.castleSurface2mby._mth01C4(j2 - k2 / 2, k1 - l2 / 2, k2, l2, 26);
    }

    public void E(int i1, int j1, int k1)
    {
        if(super.castleId == 0)
            j1 = 7 - j1;
        if(super.castleId == 1)
            i1 = 7 - i1;
        int l1 = (b[j1] + b[j1 + 1]) / 2;
        int i2 = (c[i1] + c[i1 + 1]) / 2;
        int j2 = (d[i1] + d[i1 + 1]) / 2;
        int k2 = (i2 * (b[8] - l1 - 4) + j2 * ((l1 + 4) - b[0])) / (b[8] - b[0]);
        int l2 = 27 + (18 * j1) / 7;
        int i3 = 38 + (37 * j1) / 7;
        super.castleSurface2mby._mth01C8(k2 - l2 / 2, l1 - (i3 * 16) / 75, l2, i3, k1 + 12, 128);
    }

    public void U(int i1, int j1, int k1)
    {
        if(super.castleId == 0)
            j1 = 7 - j1;
        if(super.castleId == 1)
            i1 = 7 - i1;
        int l1 = (b[j1] + b[j1 + 1]) / 2;
        int i2 = (c[i1] + c[i1 + 1]) / 2;
        int j2 = (d[i1] + d[i1 + 1]) / 2;
        int k2 = (i2 * (b[8] - l1 - 4) + j2 * ((l1 + 4) - b[0])) / (b[8] - b[0]);
        int l2 = 27 + (18 * j1) / 7;
        int i3 = 46 + (45 * j1) / 7;
        super.castleSurface2mby._mth01C4(k2 - l2 / 2, l1 - (i3 * 77) / 91, l2, i3, k1);
    }

    public void F(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
    {
        if(super.castleId == 0)
        {
            j1 = 7 - j1;
            l1 = 7 - l1;
        }
        if(super.castleId == 1)
        {
            i1 = 7 - i1;
            k1 = 7 - k1;
        }
        int l2 = (b[j1] + b[j1 + 1]) / 2;
        int i3 = (c[i1] + c[i1 + 1]) / 2;
        int j3 = (d[i1] + d[i1 + 1]) / 2;
        int k3 = (i3 * (b[8] - l2 - 4) + j3 * ((l2 + 4) - b[0])) / (b[8] - b[0]);
        int l3 = 27 + (18 * j1) / 7;
        int i4 = 38 + (37 * j1) / 7;
        int j4 = (b[l1] + b[l1 + 1]) / 2;
        int k4 = (c[k1] + c[k1 + 1]) / 2;
        int l4 = (d[k1] + d[k1 + 1]) / 2;
        int i5 = (k4 * (b[8] - j4 - 4) + l4 * ((j4 + 4) - b[0])) / (b[8] - b[0]);
        int j5 = 27 + (18 * l1) / 7;
        int k5 = 38 + (37 * l1) / 7;
        int l5 = (k3 * j2 + i5 * (k2 - j2)) / k2;
        int i6 = (l2 * j2 + j4 * (k2 - j2)) / k2;
        int j6 = (l3 * j2 + j5 * (k2 - j2)) / k2;
        int k6 = (i4 * j2 + k5 * (k2 - j2)) / k2;
        super.castleSurface2mby._mth01C8(l5 - j6 / 2, i6 - (k6 * 16) / 75, j6, k6, i2 + 12, 128);
    }

    public void L(int i1, int j1, int k1, int l1, int i2, int j2, int k2)
    {
        if(super.castleId == 0)
        {
            j1 = 7 - j1;
            l1 = 7 - l1;
        }
        if(super.castleId == 1)
        {
            i1 = 7 - i1;
            k1 = 7 - k1;
        }
        int l2 = (b[j1] + b[j1 + 1]) / 2;
        int i3 = (c[i1] + c[i1 + 1]) / 2;
        int j3 = (d[i1] + d[i1 + 1]) / 2;
        int k3 = (i3 * (b[8] - l2 - 4) + j3 * ((l2 + 4) - b[0])) / (b[8] - b[0]);
        int l3 = 27 + (18 * j1) / 7;
        int i4 = 46 + (45 * j1) / 7;
        int j4 = (b[l1] + b[l1 + 1]) / 2;
        int k4 = (c[k1] + c[k1 + 1]) / 2;
        int l4 = (d[k1] + d[k1 + 1]) / 2;
        int i5 = (k4 * (b[8] - j4 - 4) + l4 * ((j4 + 4) - b[0])) / (b[8] - b[0]);
        int j5 = 27 + (18 * l1) / 7;
        int k5 = 46 + (45 * l1) / 7;
        int l5 = (k3 * j2 + i5 * (k2 - j2)) / k2;
        int i6 = (l2 * j2 + j4 * (k2 - j2)) / k2;
        int j6 = (l3 * j2 + j5 * (k2 - j2)) / k2;
        int k6 = (i4 * j2 + k5 * (k2 - j2)) / k2;
        super.castleSurface2mby._mth01C4(l5 - j6 / 2, i6 - (k6 * 77) / 91, j6, k6, i2);
    }

    public void O()
    {
        super.castleSurface2mby._fld0414 = 290;
        super.castleSurface2mby._mth01B5(0, 0, 0);
        super.castleSurface2mby._fld0414 = 279;
        if(super.castleId == 0)
        {
            for(int i1 = 0; i1 <= 7; i1++)
            {
                for(int k1 = 7; k1 >= 0; k1--)
                    if(j[i1][k1] != 0)
                        E(i1, k1, j[i1][k1]);

            }

        } else
        {
            for(int j1 = 7; j1 >= 0; j1--)
            {
                for(int l1 = 0; l1 <= 7; l1++)
                    if(j[j1][l1] != 0)
                        E(j1, l1, j[j1][l1]);

            }

        }
        super.castleSurface2mby._mth01B1(25);
    }

    public void _mth019D()
    {
        super.castleSurface2mby._fld0414 = 290;
        super.castleSurface2mby._mth01B5(0, 0, 25);
        super.castleSurface2mby._fld0414 = 279;
        if(i % 20 < 10 && g == super.castleId && !C4 && x == 0 && !CC)
        {
            for(int i1 = 0; i1 < r; i1++)
                P(s[i1], t[i1]);

        }
        if(x == 0)
        {
            if(super.castleId == 0)
            {
                for(int j1 = 0; j1 <= 7; j1++)
                {
                    for(int i2 = 7; i2 >= 0; i2--)
                        if(j[j1][i2] != 0)
                            U(j1, i2, j[j1][i2]);

                }

            } else
            {
                for(int k1 = 7; k1 >= 0; k1--)
                {
                    for(int j2 = 0; j2 <= 7; j2++)
                        if(j[k1][j2] != 0)
                            U(k1, j2, j[k1][j2]);

                }

            }
        } else
        {
            F(y, z, C0, C1, C2, x, C3);
            int l1 = (z * x + C1 * (C3 - x)) / C3;
            if(super.castleId == 0)
            {
                for(int k2 = 0; k2 <= 7; k2++)
                {
                    for(int k3 = 7; k3 > l1; k3--)
                        if(j[k2][k3] != 0)
                            U(k2, k3, j[k2][k3]);

                }

            } else
            {
                for(int l2 = 7; l2 >= 0; l2--)
                {
                    for(int l3 = 0; l3 < l1; l3++)
                        if(j[l2][l3] != 0)
                            U(l2, l3, j[l2][l3]);

                }

            }
            L(y, z, C0, C1, C2, x, C3);
            if(super.castleId == 0)
            {
                for(int i3 = 0; i3 <= 7; i3++)
                {
                    for(int i4 = l1; i4 >= 0; i4--)
                        if(j[i3][i4] != 0)
                            U(i3, i4, j[i3][i4]);

                }

            } else
            {
                for(int j3 = 7; j3 >= 0; j3--)
                {
                    for(int j4 = l1; j4 <= 7; j4++)
                        if(j[j3][j4] != 0)
                            U(j3, j4, j[j3][j4]);

                }

            }
        }
        if(CC)
        {
            super.castleSurface2mby._mth01B3(130, 50, 240, 150, f, 224, false);
            super.castleSurface2mby._mth01B5(155, 90, 2);
            super.castleSurface2mby._mth01B5(205, 90, 3);
            super.castleSurface2mby._mth01B5(255, 90, 4);
            super.castleSurface2mby._mth01B5(305, 90, 5);
        }
        super.castleSurface2mby._mth01C2(super.castleGameGraphics, 6, 6, false);
        if(CC)
        {
            super.castleGameGraphics.setColor(Color.white);
            GameDialog.drawstringCenter(super.castleGameGraphics, "Select Piece", e, 256, 76);
        }
        super.castleGameGraphics.setFont(e);
        super.castleGameGraphics.setColor(Color.white);
        if(g == -1)
            super.castleGameGraphics.drawString("Please wait...", 10, 20);
        else
        if(g == super.castleId)
            super.castleGameGraphics.drawString("Your turn - Make a move!", 10, 20);
        else
            super.castleGameGraphics.drawString(super._fld03C1[g] + "'s turn - Please wait", 10, 20);
        GameDialog.D8(super.castleGameGraphics, "Time-Remaining:" + h, e, 502, 20);
    }

    public Chess()
    {
        e = new Font("Helvetica", 1, 13);
        f = new Color(150, 150, 150);
        h = 30;
        j = new int[8][8];
        n = new int[250];
        o = new int[250];
        p = new int[250];
        q = new int[250];
        s = new int[64];
        t = new int[64];
        u = -1;
        v = -1;
        C3 = 25;
        C4 = true;
        C5 = false;
        C6 = true;
        C7 = true;
        C8 = -1;
        CA = false;
        CB = false;
        CC = false;
    }

    int b[] = {
        22, 43, 65, 91, 119, 151, 188, 230, 278
    };
    int c[] = {
        110, 145, 180, 215, 250, 285, 320, 355, 390
    };
    int d[] = {
        22, 78, 136, 194, 250, 308, 366, 422, 480
    };
    Font e;
    Color f;
    int g;
    int h;
    int i;
    int j[][];
    int k;
    int l;
    int m;
    int n[];
    int o[];
    int p[];
    int q[];
    int r;
    int s[];
    int t[];
    int u;
    int v;
    int w;
    int x;
    int y;
    int z;
    int C0;
    int C1;
    int C2;
    int C3;
    boolean C4;
    boolean C5;
    boolean C6;
    boolean C7;
    int C8;
    int C9;
    boolean CA;
    boolean CB;
    boolean CC;
}

