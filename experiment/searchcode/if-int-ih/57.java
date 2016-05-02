package jagex.client;

import jagex.util;
import java.io.*;

public class model
{

    public model(int i, int k)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        wd(i, k);
        qi = new int[k][1];
        for(int l = 0; l < k; l++)
            qi[l][0] = l;

    }

    public model(int i, int k, boolean flag, boolean flag1, boolean flag2, boolean flag3, boolean flag4)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        xh = flag;
        yh = flag1;
        zh = flag2;
        ai = flag3;
        bi = flag4;
        wd(i, k);
    }

    private model(int i, int k, boolean flag)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        wd(i, k);
        qi = new int[k][];
    }

    private void wd(int i, int k)
    {
        ji = new int[i];
        ki = new int[i];
        li = new int[i];
        ug = new int[i];
        vg = new byte[i];
        xg = new int[k];
        yg = new int[k][];
        zg = new int[k];
        ah = new int[k];
        dh = new int[k];
        ch = new int[k];
        bh = new int[k];
        if(!bi)
        {
            pg = new int[i];
            qg = new int[i];
            rg = new int[i];
            sg = new int[i];
            tg = new int[i];
        }
        if(!ai)
        {
            wh = new byte[k];
            vh = new int[k];
        }
        if(xh)
        {
            mi = ji;
            ni = ki;
            oi = li;
        } else
        {
            mi = new int[i];
            ni = new int[i];
            oi = new int[i];
        }
        if(!zh || !yh)
        {
            eh = new int[k];
            fh = new int[k];
            gh = new int[k];
        }
        if(!yh)
        {
            ri = new int[k];
            si = new int[k];
            ti = new int[k];
            ui = new int[k];
            vi = new int[k];
            wi = new int[k];
        }
        wg = 0;
        og = 0;
        ii = i;
        pi = k;
        xi = yi = zi = 0;
        aj = bj = cj = 0;
        dj = ej = fj = 256;
        gj = hj = ij = jj = kj = lj = 256;
        mj = 0;
    }

    public void te()
    {
        pg = new int[og];
        qg = new int[og];
        rg = new int[og];
        sg = new int[og];
        tg = new int[og];
    }

    public void xe()
    {
        wg = 0;
        og = 0;
    }

    public void le(int i, int k)
    {
        wg -= i;
        if(wg < 0)
            wg = 0;
        og -= k;
        if(og < 0)
            og = 0;
    }

    public model(byte abyte0[], int i, boolean flag)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        int k = util.g2(abyte0, i);
        i += 2;
        int l = util.g2(abyte0, i);
        i += 2;
        wd(k, l);
        qi = new int[l][1];
        for(int i1 = 0; i1 < k; i1++)
        {
            ji[i1] = util.gsmart(abyte0, i);
            i += 2;
        }

        for(int j1 = 0; j1 < k; j1++)
        {
            ki[j1] = util.gsmart(abyte0, i);
            i += 2;
        }

        for(int k1 = 0; k1 < k; k1++)
        {
            li[k1] = util.gsmart(abyte0, i);
            i += 2;
        }

        og = k;
        for(int l1 = 0; l1 < l; l1++)
            xg[l1] = abyte0[i++] & 0xff;

        for(int i2 = 0; i2 < l; i2++)
        {
            zg[i2] = util.gsmart(abyte0, i);
            i += 2;
            if(zg[i2] == 32767)
                zg[i2] = hi;
        }

        for(int j2 = 0; j2 < l; j2++)
        {
            ah[j2] = util.gsmart(abyte0, i);
            i += 2;
            if(ah[j2] == 32767)
                ah[j2] = hi;
        }

        for(int k2 = 0; k2 < l; k2++)
        {
            int l2 = abyte0[i++] & 0xff;
            if(l2 == 0)
                dh[k2] = 0;
            else
                dh[k2] = hi;
        }

        for(int i3 = 0; i3 < l; i3++)
        {
            yg[i3] = new int[xg[i3]];
            for(int j3 = 0; j3 < xg[i3]; j3++)
                if(k < 256)
                {
                    yg[i3][j3] = abyte0[i++] & 0xff;
                } else
                {
                    yg[i3][j3] = util.g2(abyte0, i);
                    i += 2;
                }

        }

        wg = l;
        jh = 1;
    }

    public model(byte abyte0[], int i)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        uj = abyte0;
        vj = i;
        wj = 0;
        re(uj);
        int k = re(uj);
        int l = re(uj);
        boolean flag = false;
        wd(k, l);
        qi = new int[l][];
        for(int i3 = 0; i3 < k; i3++)
        {
            int i1 = re(uj);
            int j1 = re(uj);
            int k1 = re(uj);
            oe(i1, j1, k1);
        }

        for(int j3 = 0; j3 < l; j3++)
        {
            int l1 = re(uj);
            int i2 = re(uj);
            int j2 = re(uj);
            int k2 = re(uj);
            sj = re(uj);
            tj = re(uj);
            int l2 = re(uj);
            int ai1[] = new int[l1];
            for(int k3 = 0; k3 < l1; k3++)
                ai1[k3] = re(uj);

            int ai2[] = new int[k2];
            for(int l3 = 0; l3 < k2; l3++)
                ai2[l3] = re(uj);

            int i4 = ne(l1, ai1, i2, j2);
            qi[j3] = ai2;
            if(l2 == 0)
                dh[i4] = 0;
            else
                dh[i4] = hi;
        }

        jh = 1;
    }

    public model(String s)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        boolean flag = false;
        boolean flag1 = false;
        byte abyte0[] = null;
        try
        {
            java.io.InputStream inputstream = util.openstream(s);
            DataInputStream datainputstream = new DataInputStream(inputstream);
            abyte0 = new byte[3];
            vj = 0;
            wj = 0;
            for(int i = 0; i < 3; i += datainputstream.read(abyte0, i, 3 - i));
            int l = re(abyte0);
            abyte0 = new byte[l];
            vj = 0;
            wj = 0;
            for(int k = 0; k < l; k += datainputstream.read(abyte0, k, l - k));
            datainputstream.close();
        }
        catch(IOException _ex)
        {
            og = 0;
            wg = 0;
            return;
        }
        int i1 = re(abyte0);
        int j1 = re(abyte0);
        boolean flag2 = false;
        wd(i1, j1);
        qi = new int[j1][];
        for(int k3 = 0; k3 < i1; k3++)
        {
            int k1 = re(abyte0);
            int l1 = re(abyte0);
            int i2 = re(abyte0);
            oe(k1, l1, i2);
        }

        for(int l3 = 0; l3 < j1; l3++)
        {
            int j2 = re(abyte0);
            int k2 = re(abyte0);
            int l2 = re(abyte0);
            int i3 = re(abyte0);
            sj = re(abyte0);
            tj = re(abyte0);
            int j3 = re(abyte0);
            int ai1[] = new int[j2];
            for(int i4 = 0; i4 < j2; i4++)
                ai1[i4] = re(abyte0);

            int ai2[] = new int[i3];
            for(int j4 = 0; j4 < i3; j4++)
                ai2[j4] = re(abyte0);

            int k4 = ne(j2, ai1, k2, l2);
            qi[l3] = ai2;
            if(j3 == 0)
                dh[k4] = 0;
            else
                dh[k4] = hi;
        }

        jh = 1;
    }

    public model(model ah1[], int i, boolean flag, boolean flag1, boolean flag2, boolean flag3)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        xh = flag;
        yh = flag1;
        zh = flag2;
        ai = flag3;
        fe(ah1, i, false);
    }

    public model(model ah1[], int i)
    {
        jh = 1;
        kh = true;
        rh = true;
        sh = false;
        transparent = false;
        uh = -1;
        xh = false;
        yh = false;
        zh = false;
        ai = false;
        bi = false;
        gi = 4;
        hi = 0xbc614e;
        nj = 0xbc614e;
        oj = 180;
        pj = 155;
        qj = 95;
        rj = 256;
        sj = 512;
        tj = 32;
        fe(ah1, i, true);
    }

    public void fe(model ah1[], int i, boolean flag)
    {
        int k = 0;
        int l = 0;
        for(int i1 = 0; i1 < i; i1++)
        {
            k += ah1[i1].wg;
            l += ah1[i1].og;
        }

        wd(l, k);
        if(flag)
            qi = new int[k][];
        for(int j1 = 0; j1 < i; j1++)
        {
            model h1 = ah1[j1];
            h1.ie();
            tj = h1.tj;
            sj = h1.sj;
            oj = h1.oj;
            pj = h1.pj;
            qj = h1.qj;
            rj = h1.rj;
            for(int k1 = 0; k1 < h1.wg; k1++)
            {
                int ai1[] = new int[h1.xg[k1]];
                int ai2[] = h1.yg[k1];
                for(int l1 = 0; l1 < h1.xg[k1]; l1++)
                    ai1[l1] = oe(h1.ji[ai2[l1]], h1.ki[ai2[l1]], h1.li[ai2[l1]]);

                int i2 = ne(h1.xg[k1], ai1, h1.zg[k1], h1.ah[k1]);
                dh[i2] = h1.dh[k1];
                ch[i2] = h1.ch[k1];
                bh[i2] = h1.bh[k1];
                if(flag)
                    if(i > 1)
                    {
                        qi[i2] = new int[h1.qi[k1].length + 1];
                        qi[i2][0] = j1;
                        for(int j2 = 0; j2 < h1.qi[k1].length; j2++)
                            qi[i2][j2 + 1] = h1.qi[k1][j2];

                    } else
                    {
                        qi[i2] = new int[h1.qi[k1].length];
                        for(int k2 = 0; k2 < h1.qi[k1].length; k2++)
                            qi[i2][k2] = h1.qi[k1][k2];

                    }
            }

        }

        jh = 1;
    }

    public model(int i, int ai1[], int ai2[], int ai3[], int k, int l)
    {
        this(i, 1);
        og = i;
        for(int i1 = 0; i1 < i; i1++)
        {
            ji[i1] = ai1[i1];
            ki[i1] = ai2[i1];
            li[i1] = ai3[i1];
        }

        wg = 1;
        xg[0] = i;
        int ai4[] = new int[i];
        for(int j1 = 0; j1 < i; j1++)
            ai4[j1] = j1;

        yg[0] = ai4;
        zg[0] = k;
        ah[0] = l;
        jh = 1;
    }

    public int oe(int i, int k, int l)
    {
        for(int i1 = 0; i1 < og; i1++)
            if(ji[i1] == i && ki[i1] == k && li[i1] == l)
                return i1;

        if(og >= ii)
        {
            return -1;
        } else
        {
            ji[og] = i;
            ki[og] = k;
            li[og] = l;
            return og++;
        }
    }

    public int de(int i, int k, int l)
    {
        if(og >= ii)
        {
            return -1;
        } else
        {
            ji[og] = i;
            ki[og] = k;
            li[og] = l;
            return og++;
        }
    }

    public int ne(int i, int ai1[], int k, int l)
    {
        if(wg >= pi)
        {
            return -1;
        } else
        {
            xg[wg] = i;
            yg[wg] = ai1;
            zg[wg] = k;
            ah[wg] = l;
            jh = 1;
            return wg++;
        }
    }

    public model[] ud(int i, int k, int l, int i1, int j1, int k1, int l1, 
            boolean flag)
    {
        ie();
        int ai1[] = new int[k1];
        int ai2[] = new int[k1];
        for(int i2 = 0; i2 < k1; i2++)
        {
            ai1[i2] = 0;
            ai2[i2] = 0;
        }

        for(int j2 = 0; j2 < wg; j2++)
        {
            int k2 = 0;
            int l2 = 0;
            int j3 = xg[j2];
            int ai3[] = yg[j2];
            for(int j4 = 0; j4 < j3; j4++)
            {
                k2 += ji[ai3[j4]];
                l2 += li[ai3[j4]];
            }

            int l4 = k2 / (j3 * l) + (l2 / (j3 * i1)) * j1;
            ai1[l4] += j3;
            ai2[l4]++;
        }

        model ah1[] = new model[k1];
        for(int i3 = 0; i3 < k1; i3++)
        {
            if(ai1[i3] > l1)
                ai1[i3] = l1;
            ah1[i3] = new model(ai1[i3], ai2[i3], true, true, true, flag, true);
            ah1[i3].sj = sj;
            ah1[i3].tj = tj;
        }

        for(int k3 = 0; k3 < wg; k3++)
        {
            int l3 = 0;
            int k4 = 0;
            int i5 = xg[k3];
            int ai4[] = yg[k3];
            for(int j5 = 0; j5 < i5; j5++)
            {
                l3 += ji[ai4[j5]];
                k4 += li[ai4[j5]];
            }

            int k5 = l3 / (i5 * l) + (k4 / (i5 * i1)) * j1;
            ze(ah1[k5], ai4, i5, k3);
        }

        for(int i4 = 0; i4 < k1; i4++)
            ah1[i4].te();

        return ah1;
    }

    public void ze(model h1, int ai1[], int i, int k)
    {
        int ai2[] = new int[i];
        for(int l = 0; l < i; l++)
        {
            int i1 = ai2[l] = h1.oe(ji[ai1[l]], ki[ai1[l]], li[ai1[l]]);
            h1.ug[i1] = ug[ai1[l]];
            h1.vg[i1] = vg[ai1[l]];
        }

        int j1 = h1.ne(i, ai2, zg[k], ah[k]);
        if(!h1.ai && !ai)
            h1.vh[j1] = vh[k];
        h1.dh[j1] = dh[k];
        h1.ch[j1] = ch[k];
        h1.bh[j1] = bh[k];
    }

    public void se(boolean flag, int i, int k, int l, int i1, int j1)
    {
        tj = 256 - i * 4;
        sj = (64 - k) * 16 + 128;
        if(zh)
            return;
        for(int k1 = 0; k1 < wg; k1++)
            if(flag)
                dh[k1] = hi;
            else
                dh[k1] = 0;

        oj = l;
        pj = i1;
        qj = j1;
        rj = (int)Math.sqrt(l * l + i1 * i1 + j1 * j1);
        me();
    }

    public void be(int i, int k, int l, int i1, int j1)
    {
        tj = 256 - i * 4;
        sj = (64 - k) * 16 + 128;
        if(zh)
        {
            return;
        } else
        {
            oj = l;
            pj = i1;
            qj = j1;
            rj = (int)Math.sqrt(l * l + i1 * i1 + j1 * j1);
            me();
            return;
        }
    }

    public void ye(int i, int k, int l)
    {
        if(zh)
        {
            return;
        } else
        {
            oj = i;
            pj = k;
            qj = l;
            rj = (int)Math.sqrt(i * i + k * k + l * l);
            me();
            return;
        }
    }

    public void xd(int i, int k)
    {
        vg[i] = (byte)k;
    }

    public void ve(int i, int k, int l)
    {
        aj = aj + i & 0xff;
        bj = bj + k & 0xff;
        cj = cj + l & 0xff;
        we();
        jh = 1;
    }

    public void ke(int i, int k, int l)
    {
        aj = i & 0xff;
        bj = k & 0xff;
        cj = l & 0xff;
        we();
        jh = 1;
    }

    public void zd(int i, int k, int l)
    {
        xi += i;
        yi += k;
        zi += l;
        we();
        jh = 1;
    }

    public void ge(int i, int k, int l)
    {
        xi = i;
        yi = k;
        zi = l;
        we();
        jh = 1;
    }

    public int af()
    {
        return xi;
    }

    public void vd(int i, int k, int l)
    {
        dj = i;
        ej = k;
        fj = l;
        we();
        jh = 1;
    }

    public void ae(int i, int k, int l, int i1, int j1, int k1)
    {
        gj = i;
        hj = k;
        ij = l;
        jj = i1;
        kj = j1;
        lj = k1;
        we();
        jh = 1;
    }

    private void we()
    {
        if(gj != 256 || hj != 256 || ij != 256 || jj != 256 || kj != 256 || lj != 256)
        {
            mj = 4;
            return;
        }
        if(dj != 256 || ej != 256 || fj != 256)
        {
            mj = 3;
            return;
        }
        if(aj != 0 || bj != 0 || cj != 0)
        {
            mj = 2;
            return;
        }
        if(xi != 0 || yi != 0 || zi != 0)
        {
            mj = 1;
            return;
        } else
        {
            mj = 0;
            return;
        }
    }

    private void bf(int i, int k, int l)
    {
        for(int i1 = 0; i1 < og; i1++)
        {
            mi[i1] += i;
            ni[i1] += k;
            oi[i1] += l;
        }

    }

    private void ee(int i, int k, int l)
    {
        for(int j3 = 0; j3 < og; j3++)
        {
            if(l != 0)
            {
                int i1 = ci[l];
                int l1 = ci[l + 256];
                int k2 = ni[j3] * i1 + mi[j3] * l1 >> 15;
                ni[j3] = ni[j3] * l1 - mi[j3] * i1 >> 15;
                mi[j3] = k2;
            }
            if(i != 0)
            {
                int j1 = ci[i];
                int i2 = ci[i + 256];
                int l2 = ni[j3] * i2 - oi[j3] * j1 >> 15;
                oi[j3] = ni[j3] * j1 + oi[j3] * i2 >> 15;
                ni[j3] = l2;
            }
            if(k != 0)
            {
                int k1 = ci[k];
                int j2 = ci[k + 256];
                int i3 = oi[j3] * k1 + mi[j3] * j2 >> 15;
                oi[j3] = oi[j3] * j2 - mi[j3] * k1 >> 15;
                mi[j3] = i3;
            }
        }

    }

    private void ce(int i, int k, int l, int i1, int j1, int k1)
    {
        for(int l1 = 0; l1 < og; l1++)
        {
            if(i != 0)
                mi[l1] += ni[l1] * i >> 8;
            if(k != 0)
                oi[l1] += ni[l1] * k >> 8;
            if(l != 0)
                mi[l1] += oi[l1] * l >> 8;
            if(i1 != 0)
                ni[l1] += oi[l1] * i1 >> 8;
            if(j1 != 0)
                oi[l1] += mi[l1] * j1 >> 8;
            if(k1 != 0)
                ni[l1] += mi[l1] * k1 >> 8;
        }

    }

    private void je(int i, int k, int l)
    {
        for(int i1 = 0; i1 < og; i1++)
        {
            mi[i1] = mi[i1] * i >> 8;
            ni[i1] = ni[i1] * k >> 8;
            oi[i1] = oi[i1] * l >> 8;
        }

    }

    private void td()
    {
        lh = nh = ph = 0xf423f;
        nj = mh = oh = qh = 0xfff0bdc1;
        for(int i = 0; i < wg; i++)
        {
            int ai1[] = yg[i];
            int l = ai1[0];
            int j1 = xg[i];
            int k1;
            int l1 = k1 = mi[l];
            int i2;
            int j2 = i2 = ni[l];
            int k2;
            int l2 = k2 = oi[l];
            for(int k = 0; k < j1; k++)
            {
                int i1 = ai1[k];
                if(mi[i1] < k1)
                    k1 = mi[i1];
                else
                if(mi[i1] > l1)
                    l1 = mi[i1];
                if(ni[i1] < i2)
                    i2 = ni[i1];
                else
                if(ni[i1] > j2)
                    j2 = ni[i1];
                if(oi[i1] < k2)
                    k2 = oi[i1];
                else
                if(oi[i1] > l2)
                    l2 = oi[i1];
            }

            if(!yh)
            {
                ri[i] = k1;
                si[i] = l1;
                ti[i] = i2;
                ui[i] = j2;
                vi[i] = k2;
                wi[i] = l2;
            }
            if(l1 - k1 > nj)
                nj = l1 - k1;
            if(j2 - i2 > nj)
                nj = j2 - i2;
            if(l2 - k2 > nj)
                nj = l2 - k2;
            if(k1 < lh)
                lh = k1;
            if(l1 > mh)
                mh = l1;
            if(i2 < nh)
                nh = i2;
            if(j2 > oh)
                oh = j2;
            if(k2 < ph)
                ph = k2;
            if(l2 > qh)
                qh = l2;
        }

    }

    public void me()
    {
        if(zh)
            return;
        int i = sj * rj >> 8;
        for(int k = 0; k < wg; k++)
            if(dh[k] != hi)
                dh[k] = (eh[k] * oj + fh[k] * pj + gh[k] * qj) / i;

        int ai1[] = new int[og];
        int ai2[] = new int[og];
        int ai3[] = new int[og];
        int ai4[] = new int[og];
        for(int l = 0; l < og; l++)
        {
            ai1[l] = 0;
            ai2[l] = 0;
            ai3[l] = 0;
            ai4[l] = 0;
        }

        for(int i1 = 0; i1 < wg; i1++)
            if(dh[i1] == hi)
            {
                for(int j1 = 0; j1 < xg[i1]; j1++)
                {
                    int l1 = yg[i1][j1];
                    ai1[l1] += eh[i1];
                    ai2[l1] += fh[i1];
                    ai3[l1] += gh[i1];
                    ai4[l1]++;
                }

            }

        for(int k1 = 0; k1 < og; k1++)
            if(ai4[k1] > 0)
                ug[k1] = (ai1[k1] * oj + ai2[k1] * pj + ai3[k1] * qj) / (i * ai4[k1]);

    }

    public void pe()
    {
        if(zh && yh)
            return;
        for(int i = 0; i < wg; i++)
        {
            int ai1[] = yg[i];
            int k = mi[ai1[0]];
            int l = ni[ai1[0]];
            int i1 = oi[ai1[0]];
            int j1 = mi[ai1[1]] - k;
            int k1 = ni[ai1[1]] - l;
            int l1 = oi[ai1[1]] - i1;
            int i2 = mi[ai1[2]] - k;
            int j2 = ni[ai1[2]] - l;
            int k2 = oi[ai1[2]] - i1;
            int l2 = k1 * k2 - j2 * l1;
            int i3 = l1 * i2 - k2 * j1;
            int j3;
            for(j3 = j1 * j2 - i2 * k1; l2 > 8192 || i3 > 8192 || j3 > 8192 || l2 < -8192 || i3 < -8192 || j3 < -8192; j3 >>= 1)
            {
                l2 >>= 1;
                i3 >>= 1;
            }

            int k3 = (int)(256D * Math.sqrt(l2 * l2 + i3 * i3 + j3 * j3));
            if(k3 <= 0)
                k3 = 1;
            eh[i] = (l2 * 0x10000) / k3;
            fh[i] = (i3 * 0x10000) / k3;
            gh[i] = (j3 * 65535) / k3;
            ch[i] = -1;
        }

        me();
    }

    public void sd()
    {
        if(jh == 2)
        {
            jh = 0;
            for(int i = 0; i < og; i++)
            {
                mi[i] = ji[i];
                ni[i] = ki[i];
                oi[i] = li[i];
            }

            lh = nh = ph = 0xff676981;
            nj = mh = oh = qh = 0x98967f;
            return;
        }
        if(jh == 1)
        {
            jh = 0;
            for(int k = 0; k < og; k++)
            {
                mi[k] = ji[k];
                ni[k] = ki[k];
                oi[k] = li[k];
            }

            if(mj >= 2)
                ee(aj, bj, cj);
            if(mj >= 3)
                je(dj, ej, fj);
            if(mj >= 4)
                ce(gj, hj, ij, jj, kj, lj);
            if(mj >= 1)
                bf(xi, yi, zi);
            td();
            pe();
        }
    }

    public void he(int i, int k, int l, int i1, int j1, int k1, int l1, 
            int i2)
    {
        sd();
        if(ph > camera.ip || qh < camera.hp || lh > camera.ep || mh < camera.dp || nh > camera.gp || oh < camera.fp)
        {
            kh = false;
            return;
        }
        kh = true;
        int i3 = 0;
        int j3 = 0;
        int k3 = 0;
        int l3 = 0;
        int i4 = 0;
        int j4 = 0;
        if(k1 != 0)
        {
            i3 = di[k1];
            j3 = di[k1 + 1024];
        }
        if(j1 != 0)
        {
            i4 = di[j1];
            j4 = di[j1 + 1024];
        }
        if(i1 != 0)
        {
            k3 = di[i1];
            l3 = di[i1 + 1024];
        }
        for(int k4 = 0; k4 < og; k4++)
        {
            int l4 = mi[k4] - i;
            int i5 = ni[k4] - k;
            int j5 = oi[k4] - l;
            if(k1 != 0)
            {
                int j2 = i5 * i3 + l4 * j3 >> 15;
                i5 = i5 * j3 - l4 * i3 >> 15;
                l4 = j2;
            }
            if(j1 != 0)
            {
                int k2 = j5 * i4 + l4 * j4 >> 15;
                j5 = j5 * j4 - l4 * i4 >> 15;
                l4 = k2;
            }
            if(i1 != 0)
            {
                int l2 = i5 * l3 - j5 * k3 >> 15;
                j5 = i5 * k3 + j5 * l3 >> 15;
                i5 = l2;
            }
            if(j5 >= i2)
                sg[k4] = (l4 << l1) / j5;
            else
                sg[k4] = l4 << l1;
            if(j5 >= i2)
                tg[k4] = (i5 << l1) / j5;
            else
                tg[k4] = i5 << l1;
            pg[k4] = l4;
            qg[k4] = i5;
            rg[k4] = j5;
        }

    }

    public void ie()
    {
        sd();
        for(int i = 0; i < og; i++)
        {
            ji[i] = mi[i];
            ki[i] = ni[i];
            li[i] = oi[i];
        }

        xi = yi = zi = 0;
        aj = bj = cj = 0;
        dj = ej = fj = 256;
        gj = hj = ij = jj = kj = lj = 256;
        mj = 0;
    }

    public model qe()
    {
        model ah1[] = new model[1];
        ah1[0] = this;
        model h1 = new model(ah1, 1);
        h1.ih = ih;
        h1.transparent = transparent;
        return h1;
    }

    public model ue(boolean flag, boolean flag1, boolean flag2, boolean flag3)
    {
        model ah1[] = new model[1];
        ah1[0] = this;
        model h1 = new model(ah1, 1, flag, flag1, flag2, flag3);
        h1.ih = ih;
        return h1;
    }

    public void yd(model h1)
    {
        aj = h1.aj;
        bj = h1.bj;
        cj = h1.cj;
        xi = h1.xi;
        yi = h1.yi;
        zi = h1.zi;
        we();
        jh = 1;
    }

    public int re(byte abyte0[])
    {
        for(; abyte0[vj] == 10 || abyte0[vj] == 13; vj++);
        int i = fi[abyte0[vj++] & 0xff];
        int k = fi[abyte0[vj++] & 0xff];
        int l = fi[abyte0[vj++] & 0xff];
        int i1 = (i * 4096 + k * 64 + l) - 0x20000;
        if(i1 == 0x1e240)
            i1 = hi;
        return i1;
    }

    public int og;
    public int pg[];
    public int qg[];
    public int rg[];
    public int sg[];
    public int tg[];
    public int ug[];
    public byte vg[];
    public int wg;
    public int xg[];
    public int yg[][];
    public int zg[];
    public int ah[];
    public int bh[];
    public int ch[];
    public int dh[];
    public int eh[];
    public int fh[];
    public int gh[];
    public int hh;
    public int ih;
    public int jh;
    public boolean kh;
    public int lh;
    public int mh;
    public int nh;
    public int oh;
    public int ph;
    public int qh;
    public boolean rh;
    public boolean sh;
    public boolean transparent;
    public int uh;
    public int vh[];
    public byte wh[];
    private boolean xh;
    public boolean yh;
    public boolean zh;
    public boolean ai;
    public boolean bi;
    private static int ci[];
    private static int di[];
    private static byte ei[];
    private static int fi[];
    private int gi;
    private int hi;
    public int ii;
    public int ji[];
    public int ki[];
    public int li[];
    public int mi[];
    public int ni[];
    public int oi[];
    private int pi;
    private int qi[][];
    private int ri[];
    private int si[];
    private int ti[];
    private int ui[];
    private int vi[];
    private int wi[];
    private int xi;
    private int yi;
    private int zi;
    private int aj;
    private int bj;
    private int cj;
    private int dj;
    private int ej;
    private int fj;
    private int gj;
    private int hj;
    private int ij;
    private int jj;
    private int kj;
    private int lj;
    private int mj;
    private int nj;
    private int oj;
    private int pj;
    private int qj;
    private int rj;
    protected int sj;
    protected int tj;
    private byte uj[];
    private int vj;
    private int wj;

    static 
    {
        ci = new int[512];
        di = new int[2048];
        ei = new byte[64];
        fi = new int[256];
        for(int i = 0; i < 256; i++)
        {
            ci[i] = (int)(Math.sin((double)i * 0.02454369D) * 32768D);
            ci[i + 256] = (int)(Math.cos((double)i * 0.02454369D) * 32768D);
        }

        for(int k = 0; k < 1024; k++)
        {
            di[k] = (int)(Math.sin((double)k * 0.00613592315D) * 32768D);
            di[k + 1024] = (int)(Math.cos((double)k * 0.00613592315D) * 32768D);
        }

        for(int l = 0; l < 10; l++)
            ei[l] = (byte)(48 + l);

        for(int i1 = 0; i1 < 26; i1++)
            ei[i1 + 10] = (byte)(65 + i1);

        for(int j1 = 0; j1 < 26; j1++)
            ei[j1 + 36] = (byte)(97 + j1);

        ei[62] = -93;
        ei[63] = 36;
        for(int k1 = 0; k1 < 10; k1++)
            fi[48 + k1] = k1;

        for(int l1 = 0; l1 < 26; l1++)
            fi[65 + l1] = l1 + 10;

        for(int i2 = 0; i2 < 26; i2++)
            fi[97 + i2] = i2 + 36;

        fi[163] = 62;
        fi[36] = 63;
    }
}
