package jagex.client;

public class menu
{

    public menu(graphics j, int k)
    {
        tf = -1;
        hg = true;
        ue = j;
        we = k;
        xe = new boolean[k];
        ye = new boolean[k];
        ze = new boolean[k];
        af = new boolean[k];
        ff = new boolean[k];
        bf = new int[k];
        cf = new int[k];
        df = new int[k];
        ef = new int[k];
        gf = new int[k];
        hf = new int[k];
        _fldif = new int[k];
        jf = new int[k];
        kf = new int[k];
        lf = new int[k];
        mf = new int[k];
        nf = new String[k];
        of = new String[k][];
        vf = wc(114, 114, 176);
        wf = wc(14, 14, 62);
        xf = wc(200, 208, 232);
        yf = wc(96, 129, 184);
        zf = wc(53, 95, 115);
        ag = wc(117, 142, 171);
        bg = wc(98, 122, 158);
        cg = wc(86, 100, 136);
        dg = wc(135, 146, 179);
        eg = wc(97, 112, 151);
        fg = wc(88, 102, 136);
        gg = wc(84, 93, 120);
    }

    public int wc(int j, int k, int l)
    {
        return graphics.rgbhash((kg * j) / 114, (lg * k) / 114, (mg * l) / 176);
    }

    public void nd()
    {
        rf = 0;
    }

    public void pd(int j, int k, int l, int i1)
    {
        pf = j;
        qf = k;
        sf = i1;
        if(l != 0)
            rf = l;
        if(l == 1)
        {
            for(int j1 = 0; j1 < ve; j1++)
            {
                if(xe[j1] && _fldif[j1] == 10 && pf >= gf[j1] && qf >= hf[j1] && pf <= gf[j1] + jf[j1] && qf <= hf[j1] + kf[j1])
                    af[j1] = true;
                if(xe[j1] && _fldif[j1] == 14 && pf >= gf[j1] && qf >= hf[j1] && pf <= gf[j1] + jf[j1] && qf <= hf[j1] + kf[j1])
                    df[j1] = 1 - df[j1];
            }

        }
        if(i1 == 1)
            uf++;
        else
            uf = 0;
        if(l == 1 || uf > 20)
        {
            for(int k1 = 0; k1 < ve; k1++)
                if(xe[k1] && _fldif[k1] == 15 && pf >= gf[k1] && qf >= hf[k1] && pf <= gf[k1] + jf[k1] && qf <= hf[k1] + kf[k1])
                    af[k1] = true;

            uf -= 5;
        }
    }

    public boolean rd(int j)
    {
        if(xe[j] && af[j])
        {
            af[j] = false;
            return true;
        } else
        {
            return false;
        }
    }

    public void od(int j)
    {
        if(j == 0)
            return;
        if(tf != -1 && nf[tf] != null && xe[tf])
        {
            int k = nf[tf].length();
            if(j == 8 && k > 0)
                nf[tf] = nf[tf].substring(0, k - 1);
            if((j == 10 || j == 13) && k > 0)
                af[tf] = true;
            String s = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789!\"\243$%^&*()-_=+[{]};:'@#~,<.>/?\\| ";
            if(k < lf[tf])
            {
                for(int l = 0; l < s.length(); l++)
                    if(j == s.charAt(l))
                        nf[tf] += (char)j;

            }
            if(j == 9)
            {
                do
                    tf = (tf + 1) % ve;
                while(_fldif[tf] != 5 && _fldif[tf] != 6);
                return;
            }
        }
    }

    public void hc()
    {
        for(int j = 0; j < ve; j++)
            if(xe[j])
                if(_fldif[j] == 0)
                    gd(j, gf[j], hf[j], nf[j], mf[j]);
                else
                if(_fldif[j] == 1)
                    gd(j, gf[j] - ue.textwidth(nf[j], mf[j]) / 2, hf[j], nf[j], mf[j]);
                else
                if(_fldif[j] == 2)
                    hd(gf[j], hf[j], jf[j], kf[j]);
                else
                if(_fldif[j] == 3)
                    uc(gf[j], hf[j], jf[j]);
                else
                if(_fldif[j] == 4)
                    cd(j, gf[j], hf[j], jf[j], kf[j], mf[j], of[j], cf[j], bf[j]);
                else
                if(_fldif[j] == 5 || _fldif[j] == 6)
                    rc(j, gf[j], hf[j], jf[j], kf[j], nf[j], mf[j]);
                else
                if(_fldif[j] == 7)
                    oc(j, gf[j], hf[j], mf[j], of[j]);
                else
                if(_fldif[j] == 8)
                    bc(j, gf[j], hf[j], mf[j], of[j]);
                else
                if(_fldif[j] == 9)
                    fc(j, gf[j], hf[j], jf[j], kf[j], mf[j], of[j], cf[j], bf[j]);
                else
                if(_fldif[j] == 11)
                    fd(gf[j], hf[j], jf[j], kf[j]);
                else
                if(_fldif[j] == 12)
                    xc(gf[j], hf[j], mf[j]);
                else
                if(_fldif[j] == 14)
                    bd(j, gf[j], hf[j], jf[j], kf[j]);

        rf = 0;
    }

    protected void bd(int j, int k, int l, int i1, int j1)
    {
        ue.drawquad(k, l, i1, j1, 0xffffff);
        ue.drawhorline(k, l, i1, dg);
        ue.drawvertline(k, l, j1, dg);
        ue.drawhorline(k, (l + j1) - 1, i1, gg);
        ue.drawvertline((k + i1) - 1, l, j1, gg);
        if(df[j] == 1)
        {
            for(int k1 = 0; k1 < j1; k1++)
            {
                ue.drawhorline(k + k1, l + k1, 1, 0);
                ue.drawhorline((k + i1) - 1 - k1, l + k1, 1, 0);
            }

        }
    }

    protected void gd(int j, int k, int l, String s, int i1)
    {
        int j1 = l + ue.textheight(i1) / 3;
        id(j, k, j1, s, i1);
    }

    protected void id(int j, int k, int l, String s, int i1)
    {
        int j1;
        if(ff[j])
            j1 = 0xffffff;
        else
            j1 = 0;
        ue.drawstring(s, k, l, i1, j1);
    }

    protected void rc(int j, int k, int l, int i1, int j1, String s, int k1)
    {
        if(ze[j])
        {
            int l1 = s.length();
            s = "";
            for(int j2 = 0; j2 < l1; j2++)
                s = s + "X";

        }
        if(_fldif[j] == 5)
        {
            if(rf == 1 && pf >= k && qf >= l - j1 / 2 && pf <= k + i1 && qf <= l + j1 / 2)
                tf = j;
        } else
        if(_fldif[j] == 6)
        {
            if(rf == 1 && pf >= k - i1 / 2 && qf >= l - j1 / 2 && pf <= k + i1 / 2 && qf <= l + j1 / 2)
                tf = j;
            k -= ue.textwidth(s, k1) / 2;
        }
        if(tf == j)
            s = s + "*";
        int i2 = l + ue.textheight(k1) / 3;
        id(j, k, i2, s, k1);
    }

    public void hd(int j, int k, int l, int i1)
    {
        ue.setrend(j, k, j + l, k + i1);
        ue.drawvertgradient(j, k, l, i1, gg, dg);
        if(ig)
        {
            for(int j1 = j - (k & 0x3f); j1 < j + l; j1 += 128)
            {
                for(int k1 = k - (k & 0x1f); k1 < k + i1; k1 += 128)
                    ue.qg(j1, k1, 6 + jg, 128);

            }

        }
        ue.drawhorline(j, k, l, dg);
        ue.drawhorline(j + 1, k + 1, l - 2, dg);
        ue.drawhorline(j + 2, k + 2, l - 4, eg);
        ue.drawvertline(j, k, i1, dg);
        ue.drawvertline(j + 1, k + 1, i1 - 2, dg);
        ue.drawvertline(j + 2, k + 2, i1 - 4, eg);
        ue.drawhorline(j, (k + i1) - 1, l, gg);
        ue.drawhorline(j + 1, (k + i1) - 2, l - 2, gg);
        ue.drawhorline(j + 2, (k + i1) - 3, l - 4, fg);
        ue.drawvertline((j + l) - 1, k, i1, gg);
        ue.drawvertline((j + l) - 2, k + 1, i1 - 2, gg);
        ue.drawvertline((j + l) - 3, k + 2, i1 - 4, fg);
        ue.resetrend();
    }

    public void fd(int j, int k, int l, int i1)
    {
        ue.drawquad(j, k, l, i1, 0);
        ue.drawquadout(j, k, l, i1, ag);
        ue.drawquadout(j + 1, k + 1, l - 2, i1 - 2, bg);
        ue.drawquadout(j + 2, k + 2, l - 4, i1 - 4, cg);
        ue.xg(j, k, 2 + jg);
        ue.xg((j + l) - 7, k, 3 + jg);
        ue.xg(j, (k + i1) - 7, 4 + jg);
        ue.xg((j + l) - 7, (k + i1) - 7, 5 + jg);
    }

    protected void xc(int j, int k, int l)
    {
        ue.xg(j, k, l);
    }

    protected void uc(int j, int k, int l)
    {
        ue.drawhorline(j, k, l, 0);
    }

    protected void cd(int j, int k, int l, int i1, int j1, int k1, String as[], 
            int l1, int i2)
    {
        int j2 = j1 / ue.textheight(k1);
        if(i2 > l1 - j2)
            i2 = l1 - j2;
        if(i2 < 0)
            i2 = 0;
        bf[j] = i2;
        if(j2 < l1)
        {
            int k2 = (k + i1) - 12;
            int i3 = ((j1 - 27) * j2) / l1;
            if(i3 < 6)
                i3 = 6;
            int k3 = ((j1 - 27 - i3) * i2) / (l1 - j2);
            if(sf == 1 && pf >= k2 && pf <= k2 + 12)
            {
                if(qf > l && qf < l + 12 && i2 > 0)
                    i2--;
                if(qf > (l + j1) - 12 && qf < l + j1 && i2 < l1 - j2)
                    i2++;
                bf[j] = i2;
            }
            if(sf == 1 && (pf >= k2 && pf <= k2 + 12 || pf >= k2 - 12 && pf <= k2 + 24 && ye[j]))
            {
                if(qf > l + 12 && qf < (l + j1) - 12)
                {
                    ye[j] = true;
                    int i4 = qf - l - 12 - i3 / 2;
                    i2 = (i4 * l1) / (j1 - 24);
                    if(i2 > l1 - j2)
                        i2 = l1 - j2;
                    if(i2 < 0)
                        i2 = 0;
                    bf[j] = i2;
                }
            } else
            {
                ye[j] = false;
            }
            k3 = ((j1 - 27 - i3) * i2) / (l1 - j2);
            ld(k, l, i1, j1, k3, i3);
        }
        int l2 = j1 - j2 * ue.textheight(k1);
        int j3 = l + (ue.textheight(k1) * 5) / 6 + l2 / 2;
        for(int l3 = i2; l3 < l1; l3++)
        {
            id(j, k + 2, j3, as[l3], k1);
            j3 += ue.textheight(k1) - ng;
            if(j3 >= l + j1)
                return;
        }

    }

    protected void ld(int j, int k, int l, int i1, int j1, int k1)
    {
        int l1 = (j + l) - 12;
        ue.drawquadout(l1, k, 12, i1, 0);
        ue.xg(l1 + 1, k + 1, jg);
        ue.xg(l1 + 1, (k + i1) - 12, 1 + jg);
        ue.drawhorline(l1, k + 13, 12, 0);
        ue.drawhorline(l1, (k + i1) - 13, 12, 0);
        ue.drawvertgradient(l1 + 1, k + 14, 11, i1 - 27, vf, wf);
        ue.drawquad(l1 + 3, j1 + k + 14, 7, k1, yf);
        ue.drawvertline(l1 + 2, j1 + k + 14, k1, xf);
        ue.drawvertline(l1 + 2 + 8, j1 + k + 14, k1, zf);
    }

    protected void oc(int j, int k, int l, int i1, String as[])
    {
        int j1 = 0;
        int k1 = as.length;
        for(int l1 = 0; l1 < k1; l1++)
        {
            j1 += ue.textwidth(as[l1], i1);
            if(l1 < k1 - 1)
                j1 += ue.textwidth("  ", i1);
        }

        int i2 = k - j1 / 2;
        int j2 = l + ue.textheight(i1) / 3;
        for(int k2 = 0; k2 < k1; k2++)
        {
            int l2;
            if(ff[j])
                l2 = 0xffffff;
            else
                l2 = 0;
            if(pf >= i2 && pf <= i2 + ue.textwidth(as[k2], i1) && qf <= j2 && qf > j2 - ue.textheight(i1))
            {
                if(ff[j])
                    l2 = 0x808080;
                else
                    l2 = 0xffffff;
                if(rf == 1)
                {
                    df[j] = k2;
                    af[j] = true;
                }
            }
            if(df[j] == k2)
                if(ff[j])
                    l2 = 0xff0000;
                else
                    l2 = 0xc00000;
            ue.drawstring(as[k2], i2, j2, i1, l2);
            i2 += ue.textwidth(as[k2] + "  ", i1);
        }

    }

    protected void bc(int j, int k, int l, int i1, String as[])
    {
        int j1 = as.length;
        int k1 = l - (ue.textheight(i1) * (j1 - 1)) / 2;
        for(int l1 = 0; l1 < j1; l1++)
        {
            int i2;
            if(ff[j])
                i2 = 0xffffff;
            else
                i2 = 0;
            int j2 = ue.textwidth(as[l1], i1);
            if(pf >= k - j2 / 2 && pf <= k + j2 / 2 && qf - 2 <= k1 && qf - 2 > k1 - ue.textheight(i1))
            {
                if(ff[j])
                    i2 = 0x808080;
                else
                    i2 = 0xffffff;
                if(rf == 1)
                {
                    df[j] = l1;
                    af[j] = true;
                }
            }
            if(df[j] == l1)
                if(ff[j])
                    i2 = 0xff0000;
                else
                    i2 = 0xc00000;
            ue.drawstring(as[l1], k - j2 / 2, k1, i1, i2);
            k1 += ue.textheight(i1);
        }

    }

    protected void fc(int j, int k, int l, int i1, int j1, int k1, String as[], 
            int l1, int i2)
    {
        int j2 = j1 / ue.textheight(k1);
        if(j2 < l1)
        {
            int k2 = (k + i1) - 12;
            int i3 = ((j1 - 27) * j2) / l1;
            if(i3 < 6)
                i3 = 6;
            int k3 = ((j1 - 27 - i3) * i2) / (l1 - j2);
            if(sf == 1 && pf >= k2 && pf <= k2 + 12)
            {
                if(qf > l && qf < l + 12 && i2 > 0)
                    i2--;
                if(qf > (l + j1) - 12 && qf < l + j1 && i2 < l1 - j2)
                    i2++;
                bf[j] = i2;
            }
            if(sf == 1 && (pf >= k2 && pf <= k2 + 12 || pf >= k2 - 12 && pf <= k2 + 24 && ye[j]))
            {
                if(qf > l + 12 && qf < (l + j1) - 12)
                {
                    ye[j] = true;
                    int i4 = qf - l - 12 - i3 / 2;
                    i2 = (i4 * l1) / (j1 - 24);
                    if(i2 < 0)
                        i2 = 0;
                    if(i2 > l1 - j2)
                        i2 = l1 - j2;
                    bf[j] = i2;
                }
            } else
            {
                ye[j] = false;
            }
            k3 = ((j1 - 27 - i3) * i2) / (l1 - j2);
            ld(k, l, i1, j1, k3, i3);
        } else
        {
            i2 = 0;
            bf[j] = 0;
        }
        ef[j] = -1;
        int l2 = j1 - j2 * ue.textheight(k1);
        int j3 = l + (ue.textheight(k1) * 5) / 6 + l2 / 2;
        for(int l3 = i2; l3 < l1; l3++)
        {
            int j4;
            if(ff[j])
                j4 = 0xffffff;
            else
                j4 = 0;
            if(pf >= k + 2 && pf <= k + 2 + ue.textwidth(as[l3], k1) && qf - 2 <= j3 && qf - 2 > j3 - ue.textheight(k1))
            {
                if(ff[j])
                    j4 = 0x808080;
                else
                    j4 = 0xffffff;
                ef[j] = l3;
                if(rf == 1)
                {
                    df[j] = l3;
                    af[j] = true;
                }
            }
            if(df[j] == l3 && hg)
                j4 = 0xff0000;
            ue.drawstring(as[l3], k + 2, j3, k1, j4);
            j3 += ue.textheight(k1);
            if(j3 >= l + j1)
                return;
        }

    }

    public int kc(int j, int k, String s, int l, boolean flag)
    {
        _fldif[ve] = 0;
        xe[ve] = true;
        af[ve] = false;
        mf[ve] = l;
        ff[ve] = flag;
        gf[ve] = j;
        hf[ve] = k;
        nf[ve] = s;
        return ve++;
    }

    public int jd(int j, int k, String s, int l, boolean flag)
    {
        _fldif[ve] = 1;
        xe[ve] = true;
        af[ve] = false;
        mf[ve] = l;
        ff[ve] = flag;
        gf[ve] = j;
        hf[ve] = k;
        nf[ve] = s;
        return ve++;
    }

    public int ad(int j, int k, int l, int i1)
    {
        _fldif[ve] = 2;
        xe[ve] = true;
        af[ve] = false;
        gf[ve] = j - l / 2;
        hf[ve] = k - i1 / 2;
        jf[ve] = l;
        kf[ve] = i1;
        return ve++;
    }

    public int jc(int j, int k, int l, int i1)
    {
        _fldif[ve] = 11;
        xe[ve] = true;
        af[ve] = false;
        gf[ve] = j - l / 2;
        hf[ve] = k - i1 / 2;
        jf[ve] = l;
        kf[ve] = i1;
        return ve++;
    }

    public int lc(int j, int k, int l)
    {
        int i1 = ue.lk[l];
        int j1 = ue.mk[l];
        _fldif[ve] = 12;
        xe[ve] = true;
        af[ve] = false;
        gf[ve] = j - i1 / 2;
        hf[ve] = k - j1 / 2;
        jf[ve] = i1;
        kf[ve] = j1;
        mf[ve] = l;
        return ve++;
    }

    public int dc(int j, int k, int l, int i1, int j1, int k1, boolean flag)
    {
        _fldif[ve] = 4;
        xe[ve] = true;
        af[ve] = false;
        gf[ve] = j;
        hf[ve] = k;
        jf[ve] = l;
        kf[ve] = i1;
        ff[ve] = flag;
        mf[ve] = j1;
        lf[ve] = k1;
        cf[ve] = 0;
        bf[ve] = 0;
        of[ve] = new String[k1];
        return ve++;
    }

    public int ec(int j, int k, int l, int i1, int j1, int k1, boolean flag, 
            boolean flag1)
    {
        _fldif[ve] = 5;
        xe[ve] = true;
        ze[ve] = flag;
        af[ve] = false;
        mf[ve] = j1;
        ff[ve] = flag1;
        gf[ve] = j;
        hf[ve] = k;
        jf[ve] = l;
        kf[ve] = i1;
        lf[ve] = k1;
        nf[ve] = "";
        return ve++;
    }

    public int yc(int j, int k, int l, int i1, int j1, int k1, boolean flag, 
            boolean flag1)
    {
        _fldif[ve] = 6;
        xe[ve] = true;
        ze[ve] = flag;
        af[ve] = false;
        mf[ve] = j1;
        ff[ve] = flag1;
        gf[ve] = j;
        hf[ve] = k;
        jf[ve] = l;
        kf[ve] = i1;
        lf[ve] = k1;
        nf[ve] = "";
        return ve++;
    }

    public int cc(int j, int k, String as[], int l, boolean flag)
    {
        _fldif[ve] = 8;
        xe[ve] = true;
        af[ve] = false;
        mf[ve] = l;
        ff[ve] = flag;
        gf[ve] = j;
        hf[ve] = k;
        of[ve] = as;
        df[ve] = 0;
        return ve++;
    }

    public int qc(int j, int k, int l, int i1, int j1, int k1, boolean flag)
    {
        _fldif[ve] = 9;
        xe[ve] = true;
        af[ve] = false;
        mf[ve] = j1;
        ff[ve] = flag;
        gf[ve] = j;
        hf[ve] = k;
        jf[ve] = l;
        kf[ve] = i1;
        lf[ve] = k1;
        of[ve] = new String[k1];
        cf[ve] = 0;
        bf[ve] = 0;
        df[ve] = -1;
        ef[ve] = -1;
        return ve++;
    }

    public int md(int j, int k, int l, int i1)
    {
        _fldif[ve] = 10;
        xe[ve] = true;
        af[ve] = false;
        gf[ve] = j - l / 2;
        hf[ve] = k - i1 / 2;
        jf[ve] = l;
        kf[ve] = i1;
        return ve++;
    }

    public int sc(int j, int k, int l)
    {
        _fldif[ve] = 14;
        xe[ve] = true;
        af[ve] = false;
        gf[ve] = j - l / 2;
        hf[ve] = k - l / 2;
        jf[ve] = l;
        kf[ve] = l;
        return ve++;
    }

    public void mc(int j)
    {
        cf[j] = 0;
    }

    public void zc(int j)
    {
        bf[j] = 0;
        ef[j] = -1;
    }

    public void dd(int j, int k, String s)
    {
        of[j][k] = s;
        if(k + 1 > cf[j])
            cf[j] = k + 1;
    }

    public void gc(int j, String s, boolean flag)
    {
        int k = cf[j]++;
        if(k >= lf[j])
        {
            k--;
            cf[j]--;
            for(int l = 0; l < k; l++)
                of[j][l] = of[j][l + 1];

        }
        of[j][k] = s;
        if(flag)
            bf[j] = 0xf423f;
    }

    public void kd(int j, String s)
    {
        nf[j] = s;
    }

    public String pc(int j)
    {
        if(nf[j] == null)
            return "null";
        else
            return nf[j];
    }

    public void ed(int j)
    {
        xe[j] = true;
    }

    public void qd(int j)
    {
        xe[j] = false;
    }

    public void nc(int j)
    {
        tf = j;
    }

    public int tc(int j)
    {
        return df[j];
    }

    public int ic(int j)
    {
        int k = ef[j];
        return k;
    }

    public void vc(int j, int k)
    {
        df[j] = k;
    }

    protected graphics ue;
    int ve;
    int we;
    public boolean xe[];
    public boolean ye[];
    public boolean ze[];
    public boolean af[];
    public int bf[];
    public int cf[];
    public int df[];
    public int ef[];
    boolean ff[];
    int gf[];
    int hf[];
    int _fldif[];
    int jf[];
    int kf[];
    int lf[];
    int mf[];
    String nf[];
    String of[][];
    int pf;
    int qf;
    int rf;
    int sf;
    int tf;
    int uf;
    int vf;
    int wf;
    int xf;
    int yf;
    int zf;
    int ag;
    int bg;
    int cg;
    int dg;
    int eg;
    int fg;
    int gg;
    public boolean hg;
    public static boolean ig = true;
    public static int jg;
    public static int kg = 114;
    public static int lg = 114;
    public static int mg = 176;
    public static int ng;

}
