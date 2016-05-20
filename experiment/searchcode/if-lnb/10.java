package jagex.client;

public class camera {

	public camera(graphics graphics, int l, int i1, int j1)
	{
		cm = 50;
		dm = new int[cm];
		em = new int[cm][256];
		hm = 5;
		im = 1000;
		jm = 1000;
		km = 20;
		lm = 10;
		om = false;
		pm = 1.1000000000000001D;
		qm = 1;
		rm = false;
		vm = 100;
		wm = new model[vm];
		xm = new int[vm];
		ym = 512;
		zm = 256;
		an = 192;
		bn = 256;
		cn = 256;
		dn = 8;
		en = 4;
		wo = new int[40];
		xo = new int[40];
		yo = new int[40];
		zo = new int[40];
		ap = new int[40];
		bp = new int[40];
		cp = false;
		ro = graphics;
		zm = graphics.width / 2;
		an = graphics.height / 2;
		so = graphics.pixels;
		ln = 0;
		mn = l;
		nn = new model[mn];
		on = new int[mn];
		pn = 0;
		qn = new r[i1];
		for(int k1 = 0; k1 < i1; k1++)
			qn[k1] = new r();

		sn = 0;
		ao = new model(j1 * 2, j1);
		tn = new int[j1];
		xn = new int[j1];
		yn = new int[j1];
		un = new int[j1];
		vn = new int[j1];
		wn = new int[j1];
		zn = new int[j1];
		if(po == null)
			po = new byte[17691];
		fn = 0;
		gn = 0;
		hn = 0;
		in = 0;
		jn = 0;
		kn = 0;
		for(int l1 = 0; l1 < 256; l1++)
		{
			nm[l1] = (int)(Math.sin((double)l1 * 0.02454369D) * 32768D);
			nm[l1 + 256] = (int)(Math.cos((double)l1 * 0.02454369D) * 32768D);
		}

		for(int i2 = 0; i2 < 1024; i2++)
		{
			mm[i2] = (int)(Math.sin((double)i2 * 0.00613592315D) * 32768D);
			mm[i2 + 1024] = (int)(Math.cos((double)i2 * 0.00613592315D) * 32768D);
		}

	}

	public void uh(model h1)
	{
		if(ln < mn)
		{
			on[ln] = 0;
			nn[ln++] = h1;
		}
	}

	public void zh(model h1)
	{
		for(int k = 0; k < ln; k++)
			if(nn[k] == h1)
			{
				ln--;
				for(int l = k; l < ln; l++)
				{
					nn[l] = nn[l + 1];
					on[l] = on[l + 1];
				}

			}

	}

	public void si()
	{
		bi();
		for(int k = 0; k < ln; k++)
			nn[k] = null;

		ln = 0;
	}

	public void bi()
	{
		sn = 0;
		ao.xe();
	}

	public void ki(int k)
	{
		sn -= k;
		ao.le(k, k * 2);
		if(sn < 0)
			sn = 0;
	}

	public int lh(int k, int l, int i1, int j1, int k1, int l1, int i2)
	{
		tn[sn] = k;
		un[sn] = l;
		vn[sn] = i1;
		wn[sn] = j1;
		xn[sn] = k1;
		yn[sn] = l1;
		zn[sn] = 0;
		int j2 = ao.de(l, i1, j1);
		int k2 = ao.de(l, i1 - l1, j1);
		int ai1[] = {
				j2, k2
		};
		ao.ne(2, ai1, 0, 0);
		ao.vh[sn] = i2;
		ao.wh[sn++] = 0;
		return sn - 1;
	}

	public void mh(int k)
	{
		ao.wh[k] = 1;
	}

	public void ni(int k, int l)
	{
		zn[k] = l;
	}

	public void wh(int k, int l)
	{
		sm = k - bn;
		tm = l;
		um = 0;
		rm = true;
	}

	public int ri()
	{
		return um;
	}

	public int[] mi()
	{
		return xm;
	}

	public model[] oh()
	{
		return wm;
	}

	public void ei(int k, int l, int i1, int j1, int k1, int l1)
	{
		zm = i1;
		an = j1;
		bn = k;
		cn = l;
		ym = k1;
		dn = l1;
		scanline = new line[j1 + l];
		for(int i2 = 0; i2 < j1 + l; i2++)
			scanline[i2] = new line();

	}

	private void qh(r ar[], int k, int l)
	{
		if(k < l)
		{
			int i1 = k - 1;
			int j1 = l + 1;
			int k1 = (k + l) / 2;
			r r1 = ar[k1];
			ar[k1] = ar[k];
			ar[k] = r1;
			int l1 = r1.fnb;
			while(i1 < j1) 
			{
				do
					j1--;
				while(ar[j1].fnb < l1);
				do
					i1++;
				while(ar[i1].fnb > l1);
				if(i1 < j1)
				{
					r r2 = ar[i1];
					ar[i1] = ar[j1];
					ar[j1] = r2;
				}
			}
			qh(ar, k, j1);
			qh(ar, j1 + 1, l);
		}
	}

	public void hh(int k, r ar[], int l)
	{
		for(int i1 = 0; i1 <= l; i1++)
		{
			ar[i1].lnb = false;
			ar[i1].mnb = i1;
			ar[i1].nnb = -1;
		}

		int j1 = 0;
		do
		{
			while(ar[j1].lnb) 
				j1++;
			if(j1 == l)
				return;
			r r1 = ar[j1];
			r1.lnb = true;
			int k1 = j1;
			int l1 = j1 + k;
			if(l1 >= l)
				l1 = l - 1;
			for(int i2 = l1; i2 >= k1 + 1; i2--)
			{
				r r2 = ar[i2];
				if(r1.xmb < r2.zmb && r2.xmb < r1.zmb && r1.ymb < r2.anb && r2.ymb < r1.anb && r1.mnb != r2.nnb && !ih(r1, r2) && gh(r2, r1))
				{
					vi(ar, k1, i2);
					if(ar[i2] != r2)
						i2++;
					k1 = jp;
					r2.nnb = r1.mnb;
				}
			}

		} while(true);
	}

	public boolean vi(r ar[], int k, int l)
	{
		do
		{
			r r1 = ar[k];
			for(int i1 = k + 1; i1 <= l; i1++)
			{
				r r2 = ar[i1];
				if(!ih(r2, r1))
					break;
				ar[k] = r2;
				ar[i1] = r1;
				k = i1;
				if(k == l)
				{
					jp = k;
					kp = k - 1;
					return true;
				}
			}

			r r3 = ar[l];
			for(int j1 = l - 1; j1 >= k; j1--)
			{
				r r4 = ar[j1];
				if(!ih(r3, r4))
					break;
				ar[l] = r4;
				ar[j1] = r3;
				l = j1;
				if(k == l)
				{
					jp = l + 1;
					kp = l;
					return true;
				}
			}

			if(k + 1 >= l)
			{
				jp = k;
				kp = l;
				return false;
			}
			if(!vi(ar, k + 1, l))
			{
				jp = k;
				return false;
			}
			l = kp;
		} while(true);
	}

	public void qi(int k, int l, int i1)
	{
		int j1 = -in + 1024 & 0x3ff;
		int k1 = -jn + 1024 & 0x3ff;
		int l1 = -kn + 1024 & 0x3ff;
		if(l1 != 0)
		{
			int i2 = mm[l1];
			int l2 = mm[l1 + 1024];
			int k3 = l * i2 + k * l2 >> 15;
				l = l * l2 - k * i2 >> 15;
				k = k3;
		}
		if(j1 != 0)
		{
			int j2 = mm[j1];
			int i3 = mm[j1 + 1024];
			int l3 = l * i3 - i1 * j2 >> 15;
		i1 = l * j2 + i1 * i3 >> 15;
		l = l3;
		}
		if(k1 != 0)
		{
			int k2 = mm[k1];
			int j3 = mm[k1 + 1024];
			int i4 = i1 * k2 + k * j3 >> 15;
				i1 = i1 * j3 - k * k2 >> 15;
		k = i4;
		}
		if(k < dp)
			dp = k;
		if(k > ep)
			ep = k;
		if(l < fp)
			fp = l;
		if(l > gp)
			gp = l;
		if(i1 < hp)
			hp = i1;
		if(i1 > ip)
			ip = i1;
	}

	public void wi()
	{
		cp = ro.interlace;
		int k3 = zm * im >> dn;
		int l3 = an * im >> dn;
		dp = 0;
		ep = 0;
		fp = 0;
		gp = 0;
		hp = 0;
		ip = 0;
		qi(-k3, -l3, im);
		qi(-k3, l3, im);
		qi(k3, -l3, im);
		qi(k3, l3, im);
		qi(-zm, -an, 0);
		qi(-zm, an, 0);
		qi(zm, -an, 0);
		qi(zm, an, 0);
		dp += fn;
		ep += fn;
		fp += gn;
		gp += gn;
		hp += hn;
		ip += hn;
		nn[ln] = ao;
		ao.jh = 2;
		for(int k = 0; k < ln; k++)
			nn[k].he(fn, gn, hn, in, jn, kn, dn, hm);

		nn[ln].he(fn, gn, hn, in, jn, kn, dn, hm);
		pn = 0;
		for(int i4 = 0; i4 < ln; i4++)
		{
			model h1 = nn[i4];
			if(h1.kh)
			{
				for(int l = 0; l < h1.wg; l++)
				{
					int j4 = h1.xg[l];
					int ai2[] = h1.yg[l];
					boolean flag = false;
					for(int i5 = 0; i5 < j4; i5++)
					{
						int k1 = h1.rg[ai2[i5]];
						if(k1 <= hm || k1 >= im)
							continue;
						flag = true;
						break;
					}

					if(flag)
					{
						int j2 = 0;
						for(int i6 = 0; i6 < j4; i6++)
						{
							int l1 = h1.sg[ai2[i6]];
							if(l1 > -zm)
								j2 |= 1;
							if(l1 < zm)
								j2 |= 2;
							if(j2 == 3)
								break;
						}

						if(j2 == 3)
						{
							int k2 = 0;
							for(int j7 = 0; j7 < j4; j7++)
							{
								int i2 = h1.tg[ai2[j7]];
								if(i2 > -an)
									k2 |= 1;
								if(i2 < an)
									k2 |= 2;
								if(k2 == 3)
									break;
							}

							if(k2 == 3)
							{
								r r2 = qn[pn];
								r2.dnb = h1;
								r2.enb = l;
								xi(pn);
								int j9;
								if(r2.jnb < 0)
									j9 = h1.zg[l];
								else
									j9 = h1.ah[l];
								if(j9 != 0xbc614e)
								{
									int l2 = 0;
									for(int j10 = 0; j10 < j4; j10++)
										l2 += h1.rg[ai2[j10]];

									int j3;
									r2.fnb = j3 = l2 / j4 + h1.ih;
									r2.knb = j9;
									pn++;
								}
							}
						}
					}
				}

			}
		}

		model h2 = ao;
		if(h2.kh)
		{
			for(int i1 = 0; i1 < h2.wg; i1++)
			{
				int ai1[] = h2.yg[i1];
				int l4 = ai1[0];
				int j5 = h2.sg[l4];
				int j6 = h2.tg[l4];
				int k7 = h2.rg[l4];
				if(k7 > hm && k7 < jm)
				{
					int k8 = (xn[i1] << dn) / k7;
					int k9 = (yn[i1] << dn) / k7;
					if(j5 - k8 / 2 <= zm && j5 + k8 / 2 >= -zm && j6 - k9 <= an && j6 >= -an)
					{
						r r3 = qn[pn];
						r3.dnb = h2;
						r3.enb = i1;
						zi(pn);
						r3.fnb = (k7 + h2.rg[ai1[1]]) / 2;
						pn++;
					}
				}
			}

		}
		if(pn == 0)
			return;
		gm = pn;
		qh(qn, 0, pn - 1);
		hh(100, qn, pn);
		for(int k4 = 0; k4 < pn; k4++)
		{
			r r1 = qn[k4];
			model h3 = r1.dnb;
			int j1 = r1.enb;
			if(h3 == ao)
			{
				int ai3[] = h3.yg[j1];
				int k6 = ai3[0];
				int l7 = h3.sg[k6];
				int l8 = h3.tg[k6];
				int l9 = h3.rg[k6];
				int k10 = (xn[j1] << dn) / l9;
				int i11 = (yn[j1] << dn) / l9;
				int k11 = l8 - h3.tg[ai3[1]];
				int l11 = ((h3.sg[ai3[1]] - l7) * k11) / i11;
				l11 = h3.sg[ai3[1]] - l7;
				int j12 = l7 - k10 / 2;
				int l12 = (cn + l8) - i11;
				ro.drawentity(j12 + bn, l12, k10, i11, tn[j1], l11, (256 << dn) / l9);
				if(rm && um < vm)
				{
					j12 += (zn[j1] << dn) / l9;
					if(tm >= l12 && tm <= l12 + i11 && sm >= j12 && sm <= j12 + k10 && !h3.ai && h3.wh[j1] == 0)
					{
						wm[um] = h3;
						xm[um] = j1;
						um++;
					}
				}
			} else
			{
				int i9 = 0;
				int l10 = 0;
				int j11 = h3.xg[j1];
				int ai4[] = h3.yg[j1];
				if(h3.dh[j1] != 0xbc614e)
					if(r1.jnb < 0)
						l10 = h3.tj - h3.dh[j1];
					else
						l10 = h3.tj + h3.dh[j1];
				for(int i12 = 0; i12 < j11; i12++)
				{
					int i3 = ai4[i12];
					zo[i12] = h3.pg[i3];
					ap[i12] = h3.qg[i3];
					bp[i12] = h3.rg[i3];
					if(h3.dh[j1] == 0xbc614e)
						if(r1.jnb < 0)
							l10 = (h3.tj - h3.ug[i3]) + h3.vg[i3];
						else
							l10 = h3.tj + h3.ug[i3] + h3.vg[i3];
					if(h3.rg[i3] >= hm)
					{
						wo[i9] = h3.sg[i3];
						xo[i9] = h3.tg[i3];
						yo[i9] = l10;
						if(h3.rg[i3] > lm)
							yo[i9] += (h3.rg[i3] - lm) / km;
						i9++;
					} else
					{
						int i10;
						if(i12 == 0)
							i10 = ai4[j11 - 1];
						else
							i10 = ai4[i12 - 1];
						if(h3.rg[i10] >= hm)
						{
							int i8 = h3.rg[i3] - h3.rg[i10];
							int k5 = h3.pg[i3] - ((h3.pg[i3] - h3.pg[i10]) * (h3.rg[i3] - hm)) / i8;
							int l6 = h3.qg[i3] - ((h3.qg[i3] - h3.qg[i10]) * (h3.rg[i3] - hm)) / i8;
							wo[i9] = (k5 << dn) / hm;
							xo[i9] = (l6 << dn) / hm;
							yo[i9] = l10;
							i9++;
						}
						if(i12 == j11 - 1)
							i10 = ai4[0];
						else
							i10 = ai4[i12 + 1];
						if(h3.rg[i10] >= hm)
						{
							int j8 = h3.rg[i3] - h3.rg[i10];
							int l5 = h3.pg[i3] - ((h3.pg[i3] - h3.pg[i10]) * (h3.rg[i3] - hm)) / j8;
							int i7 = h3.qg[i3] - ((h3.qg[i3] - h3.qg[i10]) * (h3.rg[i3] - hm)) / j8;
							wo[i9] = (l5 << dn) / hm;
							xo[i9] = (i7 << dn) / hm;
							yo[i9] = l10;
							i9++;
						}
					}
				}

				for(int k12 = 0; k12 < j11; k12++)
				{
					if(yo[k12] < 0)
						yo[k12] = 0;
					else
						if(yo[k12] > 255)
							yo[k12] = 255;
					if(r1.knb >= 0)
						if(io[r1.knb] == 1)
							yo[k12] <<= 9;
						else
							yo[k12] <<= 6;
				}

				fh(0, 0, 0, 0, i9, wo, xo, yo, h3, j1);
				if(vo > uo)
					ph(0, 0, j11, zo, ap, bp, r1.knb, h3);
			}
		}

		rm = false;
	}

	private void fh(int k, int l, int i1, int j1, int k1, int ai1[], int ai2[], 
			int ai3[], model h1, int l1)
	{
		if(k1 == 3)
		{
			int i2 = ai2[0] + cn;
			int i3 = ai2[1] + cn;
			int i4 = ai2[2] + cn;
			int i5 = ai1[0];
			int j6 = ai1[1];
			int l7 = ai1[2];
			int j9 = ai3[0];
			int l10 = ai3[1];
			int l11 = ai3[2];
			int l12 = (cn + an) - 1;
			int j13 = 0;
			int l13 = 0;
			int j14 = 0;
			int l14 = 0;
			int j15 = 0xbc614e;
			int l15 = 0xff439eb2;
			if(i4 != i2)
			{
				l13 = (l7 - i5 << 8) / (i4 - i2);
				l14 = (l11 - j9 << 8) / (i4 - i2);
				if(i2 < i4)
				{
					j13 = i5 << 8;
					j14 = j9 << 8;
					j15 = i2;
					l15 = i4;
				} else
				{
					j13 = l7 << 8;
					j14 = l11 << 8;
					j15 = i4;
					l15 = i2;
				}
				if(j15 < 0)
				{
					j13 -= l13 * j15;
					j14 -= l14 * j15;
					j15 = 0;
				}
				if(l15 > l12)
					l15 = l12;
			}
			int j16 = 0;
			int l16 = 0;
			int j17 = 0;
			int l17 = 0;
			int j18 = 0xbc614e;
			int l18 = 0xff439eb2;
			if(i3 != i2)
			{
				l16 = (j6 - i5 << 8) / (i3 - i2);
				l17 = (l10 - j9 << 8) / (i3 - i2);
				if(i2 < i3)
				{
					j16 = i5 << 8;
					j17 = j9 << 8;
					j18 = i2;
					l18 = i3;
				} else
				{
					j16 = j6 << 8;
					j17 = l10 << 8;
					j18 = i3;
					l18 = i2;
				}
				if(j18 < 0)
				{
					j16 -= l16 * j18;
					j17 -= l17 * j18;
					j18 = 0;
				}
				if(l18 > l12)
					l18 = l12;
			}
			int j19 = 0;
			int l19 = 0;
			int j20 = 0;
			int l20 = 0;
			int j21 = 0xbc614e;
			int l21 = 0xff439eb2;
			if(i4 != i3)
			{
				l19 = (l7 - j6 << 8) / (i4 - i3);
				l20 = (l11 - l10 << 8) / (i4 - i3);
				if(i3 < i4)
				{
					j19 = j6 << 8;
					j20 = l10 << 8;
					j21 = i3;
					l21 = i4;
				} else
				{
					j19 = l7 << 8;
					j20 = l11 << 8;
					j21 = i4;
					l21 = i3;
				}
				if(j21 < 0)
				{
					j19 -= l19 * j21;
					j20 -= l20 * j21;
					j21 = 0;
				}
				if(l21 > l12)
					l21 = l12;
			}
			uo = j15;
			if(j18 < uo)
				uo = j18;
			if(j21 < uo)
				uo = j21;
			vo = l15;
			if(l18 > vo)
				vo = l18;
			if(l21 > vo)
				vo = l21;
			int j22 = 0;
			for(i1 = uo; i1 < vo; i1++)
			{
				if(i1 >= j15 && i1 < l15)
				{
					k = l = j13;
					j1 = j22 = j14;
					j13 += l13;
					j14 += l14;
				} else
				{
					k = 0xa0000;
					l = 0xfff60000;
				}
				if(i1 >= j18 && i1 < l18)
				{
					if(j16 < k)
					{
						k = j16;
						j1 = j17;
					}
					if(j16 > l)
					{
						l = j16;
						j22 = j17;
					}
					j16 += l16;
					j17 += l17;
				}
				if(i1 >= j21 && i1 < l21)
				{
					if(j19 < k)
					{
						k = j19;
						j1 = j20;
					}
					if(j19 > l)
					{
						l = j19;
						j22 = j20;
					}
					j19 += l19;
					j20 += l20;
				}
				line t7 = scanline[i1];
				t7.rnb = k;
				t7.snb = l;
				t7.tnb = j1;
				t7.unb = j22;
			}

			if(uo < cn - an)
				uo = cn - an;
		} else
			if(k1 == 4)
			{
				int j2 = ai2[0] + cn;
				int j3 = ai2[1] + cn;
				int j4 = ai2[2] + cn;
				int j5 = ai2[3] + cn;
				int k6 = ai1[0];
				int i8 = ai1[1];
				int k9 = ai1[2];
				int i11 = ai1[3];
				int i12 = ai3[0];
				int i13 = ai3[1];
				int k13 = ai3[2];
				int i14 = ai3[3];
				int k14 = (cn + an) - 1;
				int i15 = 0;
				int k15 = 0;
				int i16 = 0;
				int k16 = 0;
				int i17 = 0xbc614e;
				int k17 = 0xff439eb2;
				if(j5 != j2)
				{
					k15 = (i11 - k6 << 8) / (j5 - j2);
					k16 = (i14 - i12 << 8) / (j5 - j2);
					if(j2 < j5)
					{
						i15 = k6 << 8;
						i16 = i12 << 8;
						i17 = j2;
						k17 = j5;
					} else
					{
						i15 = i11 << 8;
						i16 = i14 << 8;
						i17 = j5;
						k17 = j2;
					}
					if(i17 < 0)
					{
						i15 -= k15 * i17;
						i16 -= k16 * i17;
						i17 = 0;
					}
					if(k17 > k14)
						k17 = k14;
				}
				int i18 = 0;
				int k18 = 0;
				int i19 = 0;
				int k19 = 0;
				int i20 = 0xbc614e;
				int k20 = 0xff439eb2;
				if(j3 != j2)
				{
					k18 = (i8 - k6 << 8) / (j3 - j2);
					k19 = (i13 - i12 << 8) / (j3 - j2);
					if(j2 < j3)
					{
						i18 = k6 << 8;
						i19 = i12 << 8;
						i20 = j2;
						k20 = j3;
					} else
					{
						i18 = i8 << 8;
						i19 = i13 << 8;
						i20 = j3;
						k20 = j2;
					}
					if(i20 < 0)
					{
						i18 -= k18 * i20;
						i19 -= k19 * i20;
						i20 = 0;
					}
					if(k20 > k14)
						k20 = k14;
				}
				int i21 = 0;
				int k21 = 0;
				int i22 = 0;
				int k22 = 0;
				int l22 = 0xbc614e;
				int i23 = 0xff439eb2;
				if(j4 != j3)
				{
					k21 = (k9 - i8 << 8) / (j4 - j3);
					k22 = (k13 - i13 << 8) / (j4 - j3);
					if(j3 < j4)
					{
						i21 = i8 << 8;
						i22 = i13 << 8;
						l22 = j3;
						i23 = j4;
					} else
					{
						i21 = k9 << 8;
						i22 = k13 << 8;
						l22 = j4;
						i23 = j3;
					}
					if(l22 < 0)
					{
						i21 -= k21 * l22;
						i22 -= k22 * l22;
						l22 = 0;
					}
					if(i23 > k14)
						i23 = k14;
				}
				int j23 = 0;
				int k23 = 0;
				int l23 = 0;
				int i24 = 0;
				int j24 = 0xbc614e;
				int k24 = 0xff439eb2;
				if(j5 != j4)
				{
					k23 = (i11 - k9 << 8) / (j5 - j4);
					i24 = (i14 - k13 << 8) / (j5 - j4);
					if(j4 < j5)
					{
						j23 = k9 << 8;
						l23 = k13 << 8;
						j24 = j4;
						k24 = j5;
					} else
					{
						j23 = i11 << 8;
						l23 = i14 << 8;
						j24 = j5;
						k24 = j4;
					}
					if(j24 < 0)
					{
						j23 -= k23 * j24;
						l23 -= i24 * j24;
						j24 = 0;
					}
					if(k24 > k14)
						k24 = k14;
				}
				uo = i17;
				if(i20 < uo)
					uo = i20;
				if(l22 < uo)
					uo = l22;
				if(j24 < uo)
					uo = j24;
				vo = k17;
				if(k20 > vo)
					vo = k20;
				if(i23 > vo)
					vo = i23;
				if(k24 > vo)
					vo = k24;
				int l24 = 0;
				for(i1 = uo; i1 < vo; i1++)
				{
					if(i1 >= i17 && i1 < k17)
					{
						k = l = i15;
						j1 = l24 = i16;
						i15 += k15;
						i16 += k16;
					} else
					{
						k = 0xa0000;
						l = 0xfff60000;
					}
					if(i1 >= i20 && i1 < k20)
					{
						if(i18 < k)
						{
							k = i18;
							j1 = i19;
						}
						if(i18 > l)
						{
							l = i18;
							l24 = i19;
						}
						i18 += k18;
						i19 += k19;
					}
					if(i1 >= l22 && i1 < i23)
					{
						if(i21 < k)
						{
							k = i21;
							j1 = i22;
						}
						if(i21 > l)
						{
							l = i21;
							l24 = i22;
						}
						i21 += k21;
						i22 += k22;
					}
					if(i1 >= j24 && i1 < k24)
					{
						if(j23 < k)
						{
							k = j23;
							j1 = l23;
						}
						if(j23 > l)
						{
							l = j23;
							l24 = l23;
						}
						j23 += k23;
						l23 += i24;
					}
					line t8 = scanline[i1];
					t8.rnb = k;
					t8.snb = l;
					t8.tnb = j1;
					t8.unb = l24;
				}

				if(uo < cn - an)
					uo = cn - an;
			} else
			{
				vo = uo = ai2[0] += cn;
				for(i1 = 1; i1 < k1; i1++)
				{
					int k2;
					if((k2 = ai2[i1] += cn) < uo)
						uo = k2;
					else
						if(k2 > vo)
							vo = k2;
				}

				if(uo < cn - an)
					uo = cn - an;
				if(vo >= cn + an)
					vo = (cn + an) - 1;
				if(uo >= vo)
					return;
				for(i1 = uo; i1 < vo; i1++)
				{
					line t1 = scanline[i1];
					t1.rnb = 0xa0000;
					t1.snb = 0xfff60000;
				}

				int l2 = k1 - 1;
				int k3 = ai2[0];
				int k4 = ai2[l2];
				if(k3 < k4)
				{
					int k5 = ai1[0] << 8;
					int l6 = (ai1[l2] - ai1[0] << 8) / (k4 - k3);
					int j8 = ai3[0] << 8;
					int l9 = (ai3[l2] - ai3[0] << 8) / (k4 - k3);
					if(k3 < 0)
					{
						k5 -= l6 * k3;
						j8 -= l9 * k3;
						k3 = 0;
					}
					if(k4 > vo)
						k4 = vo;
					for(i1 = k3; i1 <= k4; i1++)
					{
						line t3 = scanline[i1];
						t3.rnb = t3.snb = k5;
						t3.tnb = t3.unb = j8;
						k5 += l6;
						j8 += l9;
					}

				} else
					if(k3 > k4)
					{
						int l5 = ai1[l2] << 8;
						int i7 = (ai1[0] - ai1[l2] << 8) / (k3 - k4);
						int k8 = ai3[l2] << 8;
						int i10 = (ai3[0] - ai3[l2] << 8) / (k3 - k4);
						if(k4 < 0)
						{
							l5 -= i7 * k4;
							k8 -= i10 * k4;
							k4 = 0;
						}
						if(k3 > vo)
							k3 = vo;
						for(i1 = k4; i1 <= k3; i1++)
						{
							line t4 = scanline[i1];
							t4.rnb = t4.snb = l5;
							t4.tnb = t4.unb = k8;
							l5 += i7;
							k8 += i10;
						}

					}
				for(i1 = 0; i1 < l2; i1++)
				{
					int i6 = i1 + 1;
					int l3 = ai2[i1];
					int l4 = ai2[i6];
					if(l3 < l4)
					{
						int j7 = ai1[i1] << 8;
						int l8 = (ai1[i6] - ai1[i1] << 8) / (l4 - l3);
						int j10 = ai3[i1] << 8;
						int j11 = (ai3[i6] - ai3[i1] << 8) / (l4 - l3);
						if(l3 < 0)
						{
							j7 -= l8 * l3;
							j10 -= j11 * l3;
							l3 = 0;
						}
						if(l4 > vo)
							l4 = vo;
						for(int j12 = l3; j12 <= l4; j12++)
						{
							line t5 = scanline[j12];
							if(j7 < t5.rnb)
							{
								t5.rnb = j7;
								t5.tnb = j10;
							}
							if(j7 > t5.snb)
							{
								t5.snb = j7;
								t5.unb = j10;
							}
							j7 += l8;
							j10 += j11;
						}

					} else
						if(l3 > l4)
						{
							int k7 = ai1[i6] << 8;
							int i9 = (ai1[i1] - ai1[i6] << 8) / (l3 - l4);
							int k10 = ai3[i6] << 8;
							int k11 = (ai3[i1] - ai3[i6] << 8) / (l3 - l4);
							if(l4 < 0)
							{
								k7 -= i9 * l4;
								k10 -= k11 * l4;
								l4 = 0;
							}
							if(l3 > vo)
								l3 = vo;
							for(int k12 = l4; k12 <= l3; k12++)
							{
								line t6 = scanline[k12];
								if(k7 < t6.rnb)
								{
									t6.rnb = k7;
									t6.tnb = k10;
								}
								if(k7 > t6.snb)
								{
									t6.snb = k7;
									t6.unb = k10;
								}
								k7 += i9;
								k10 += k11;
							}

						}
				}

				if(uo < cn - an)
					uo = cn - an;
			}
		if(rm && um < vm && tm >= uo && tm < vo)
		{
			line t2 = scanline[tm];
			if(sm >= t2.rnb >> 8 && sm <= t2.snb >> 8 && t2.rnb <= t2.snb && !h1.ai && h1.wh[l1] == 0)
			{
				wm[um] = h1;
				xm[um] = l1;
				um++;
			}
		}
	}

	private void ph(int k, int l, int i1, int ai1[], int ai2[], int ai3[], int j1, 
			model h1)
	{
		if(j1 >= 0)
		{
			if(j1 >= fo)
				j1 = 0;
			li(j1);
			int k1 = ai1[0];
			int i2 = ai2[0];
			int l2 = ai3[0];
			int k3 = k1 - ai1[1];
			int i4 = i2 - ai2[1];
			int k4 = l2 - ai3[1];
			i1--;
			int k6 = ai1[i1] - k1;
			int l7 = ai2[i1] - i2;
			int i9 = ai3[i1] - l2;
			if(io[j1] == 1)
			{
				int j10 = k6 * i2 - l7 * k1 << 12;
				int i11 = l7 * l2 - i9 * i2 << (5 - dn) + 7 + 4;
				int k11 = i9 * k1 - k6 * l2 << (5 - dn) + 7;
				int i12 = k3 * i2 - i4 * k1 << 12;
				int k12 = i4 * l2 - k4 * i2 << (5 - dn) + 7 + 4;
				int i13 = k4 * k1 - k3 * l2 << (5 - dn) + 7;
				int k13 = i4 * k6 - k3 * l7 << 5;
				int i14 = k4 * l7 - i4 * i9 << (5 - dn) + 4;
				int k14 = k3 * i9 - k4 * k6 >> dn - 5;
				int i15 = i11 >> 4;
				int k15 = k12 >> 4;
				int i16 = i14 >> 4;
				int k16 = uo - cn;
				int i17 = ym;
				int k17 = bn + uo * i17;
				byte byte1 = 1;
				j10 += k11 * k16;
				i12 += i13 * k16;
				k13 += k14 * k16;
				if(cp)
				{
					if((uo & 1) == 1)
					{
						uo++;
						j10 += k11;
						i12 += i13;
						k13 += k14;
						k17 += i17;
					}
					k11 <<= 1;
					i13 <<= 1;
					k14 <<= 1;
					i17 <<= 1;
					byte1 = 2;
				}
				if(h1.sh)
				{
					for(k = uo; k < vo; k += byte1)
					{
						line t4 = scanline[k];
						l = t4.rnb >> 8;
					int i18 = t4.snb >> 8;
					int i21 = i18 - l;
					if(i21 <= 0)
					{
						j10 += k11;
						i12 += i13;
						k13 += k14;
						k17 += i17;
					} else
					{
						int k22 = t4.tnb;
						int i24 = (t4.unb - k22) / i21;
						if(l < -zm)
						{
							k22 += (-zm - l) * i24;
							l = -zm;
							i21 = i18 - l;
						}
						if(i18 > zm)
						{
							int j18 = zm;
							i21 = j18 - l;
						}
						oi(so, ko[j1], 0, 0, j10 + i15 * l, i12 + k15 * l, k13 + i16 * l, i11, k12, i14, i21, k17 + l, k22, i24 << 2);
						j10 += k11;
						i12 += i13;
						k13 += k14;
						k17 += i17;
					}
					}

					return;
				}
				if(!lo[j1])
				{
					for(k = uo; k < vo; k += byte1)
					{
						line t5 = scanline[k];
						l = t5.rnb >> 8;
					int k18 = t5.snb >> 8;
						int j21 = k18 - l;
						if(j21 <= 0)
						{
							j10 += k11;
							i12 += i13;
							k13 += k14;
							k17 += i17;
						} else
						{
							int l22 = t5.tnb;
							int j24 = (t5.unb - l22) / j21;
							if(l < -zm)
							{
								l22 += (-zm - l) * j24;
								l = -zm;
								j21 = k18 - l;
							}
							if(k18 > zm)
							{
								int l18 = zm;
								j21 = l18 - l;
							}
							th(so, ko[j1], 0, 0, j10 + i15 * l, i12 + k15 * l, k13 + i16 * l, i11, k12, i14, j21, k17 + l, l22, j24 << 2);
							j10 += k11;
							i12 += i13;
							k13 += k14;
							k17 += i17;
						}
					}

					return;
				}
				for(k = uo; k < vo; k += byte1)
				{
					line t6 = scanline[k];
					l = t6.rnb >> 8;
				int i19 = t6.snb >> 8;
							int k21 = i19 - l;
							if(k21 <= 0)
							{
								j10 += k11;
								i12 += i13;
								k13 += k14;
								k17 += i17;
							} else
							{
								int i23 = t6.tnb;
								int k24 = (t6.unb - i23) / k21;
								if(l < -zm)
								{
									i23 += (-zm - l) * k24;
									l = -zm;
									k21 = i19 - l;
								}
								if(i19 > zm)
								{
									int j19 = zm;
									k21 = j19 - l;
								}
								ii(so, 0, 0, 0, ko[j1], j10 + i15 * l, i12 + k15 * l, k13 + i16 * l, i11, k12, i14, k21, k17 + l, i23, k24);
								j10 += k11;
								i12 += i13;
								k13 += k14;
								k17 += i17;
							}
				}

				return;
			}
			int k10 = k6 * i2 - l7 * k1 << 11;
			int j11 = l7 * l2 - i9 * i2 << (5 - dn) + 6 + 4;
			int l11 = i9 * k1 - k6 * l2 << (5 - dn) + 6;
			int j12 = k3 * i2 - i4 * k1 << 11;
			int l12 = i4 * l2 - k4 * i2 << (5 - dn) + 6 + 4;
			int j13 = k4 * k1 - k3 * l2 << (5 - dn) + 6;
			int l13 = i4 * k6 - k3 * l7 << 5;
			int j14 = k4 * l7 - i4 * i9 << (5 - dn) + 4;
			int l14 = k3 * i9 - k4 * k6 >> dn - 5;
			int j15 = j11 >> 4;
			int l15 = l12 >> 4;
			int j16 = j14 >> 4;
			int l16 = uo - cn;
			int j17 = ym;
			int l17 = bn + uo * j17;
			byte byte2 = 1;
			k10 += l11 * l16;
			j12 += j13 * l16;
			l13 += l14 * l16;
			if(cp)
			{
				if((uo & 1) == 1)
				{
					uo++;
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				}
				l11 <<= 1;
				j13 <<= 1;
				l14 <<= 1;
				j17 <<= 1;
				byte2 = 2;
			}
			if(h1.sh)
			{
				for(k = uo; k < vo; k += byte2)
				{
					line t7 = scanline[k];
					l = t7.rnb >> 8;
				int k19 = t7.snb >> 8;
				int l21 = k19 - l;
				if(l21 <= 0)
				{
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				} else
				{
					int j23 = t7.tnb;
					int l24 = (t7.unb - j23) / l21;
					if(l < -zm)
					{
						j23 += (-zm - l) * l24;
						l = -zm;
						l21 = k19 - l;
					}
					if(k19 > zm)
					{
						int l19 = zm;
						l21 = l19 - l;
					}
					xh(so, ko[j1], 0, 0, k10 + j15 * l, j12 + l15 * l, l13 + j16 * l, j11, l12, j14, l21, l17 + l, j23, l24);
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				}
				}

				return;
			}
			if(!lo[j1])
			{
				for(k = uo; k < vo; k += byte2)
				{
					line t8 = scanline[k];
					l = t8.rnb >> 8;
				int i20 = t8.snb >> 8;
				int i22 = i20 - l;
				if(i22 <= 0)
				{
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				} else
				{
					int k23 = t8.tnb;
					int i25 = (t8.unb - k23) / i22;
					if(l < -zm)
					{
						k23 += (-zm - l) * i25;
						l = -zm;
						i22 = i20 - l;
					}
					if(i20 > zm)
					{
						int j20 = zm;
						i22 = j20 - l;
					}
					pi(so, ko[j1], 0, 0, k10 + j15 * l, j12 + l15 * l, l13 + j16 * l, j11, l12, j14, i22, l17 + l, k23, i25);
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				}
				}

				return;
			}
			for(k = uo; k < vo; k += byte2)
			{
				line t9 = scanline[k];
				l = t9.rnb >> 8;
			int k20 = t9.snb >> 8;
				int j22 = k20 - l;
				if(j22 <= 0)
				{
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				} else
				{
					int l23 = t9.tnb;
					int j25 = (t9.unb - l23) / j22;
					if(l < -zm)
					{
						l23 += (-zm - l) * j25;
						l = -zm;
						j22 = k20 - l;
					}
					if(k20 > zm)
					{
						int l20 = zm;
						j22 = l20 - l;
					}
					sh(so, 0, 0, 0, ko[j1], k10 + j15 * l, j12 + l15 * l, l13 + j16 * l, j11, l12, j14, j22, l17 + l, l23, j25);
					k10 += l11;
					j12 += j13;
					l13 += l14;
					l17 += j17;
				}
			}

			return;
		}
		for(int l1 = 0; l1 < cm; l1++)
		{
			if(dm[l1] == j1)
			{
				fm = em[l1];
				break;
			}
			if(l1 == cm - 1)
			{
				int j2 = (int)(Math.random() * (double)cm);
				dm[j2] = j1;
				j1 = -1 - j1;
				int i3 = (j1 >> 10 & 0x1f) * 8;
				int l3 = (j1 >> 5 & 0x1f) * 8;
				int j4 = (j1 & 0x1f) * 8;
				for(int l4 = 0; l4 < 256; l4++)
				{
					int l6 = l4 * l4;
					int i8 = (i3 * l6) / 0x10000;
					int j9 = (l3 * l6) / 0x10000;
					int l10 = (j4 * l6) / 0x10000;
					em[j2][255 - l4] = (i8 << 16) + (j9 << 8) + l10;
				}

				fm = em[j2];
			}
		}

		int k2 = ym;
		int j3 = bn + uo * k2;
		byte byte0 = 1;
		if(cp)
		{
			if((uo & 1) == 1)
			{
				uo++;
				j3 += k2;
			}
			k2 <<= 1;
			byte0 = 2;
		}
		if(h1.transparent)
		{
			for(k = uo; k < vo; k += byte0)
			{
				line t1 = scanline[k];
				l = t1.rnb >> 8;
			int i5 = t1.snb >> 8;
			int i7 = i5 - l;
			if(i7 <= 0)
			{
				j3 += k2;
			} else
			{
				int j8 = t1.tnb;
				int k9 = (t1.unb - j8) / i7;
				if(l < -zm)
				{
					j8 += (-zm - l) * k9;
					l = -zm;
					i7 = i5 - l;
				}
				if(i5 > zm)
				{
					int j5 = zm;
					i7 = j5 - l;
				}
				hi(so, -i7, j3 + l, 0, fm, j8, k9);
				j3 += k2;
			}
			}

			return;
		}
		if(om)
		{
			for(k = uo; k < vo; k += byte0)
			{
				line t2 = scanline[k];
				l = t2.rnb >> 8;
			int k5 = t2.snb >> 8;
					int j7 = k5 - l;
					if(j7 <= 0)
					{
						j3 += k2;
					} else
					{
						int k8 = t2.tnb;
						int l9 = (t2.unb - k8) / j7;
						if(l < -zm)
						{
							k8 += (-zm - l) * l9;
							l = -zm;
							j7 = k5 - l;
						}
						if(k5 > zm)
						{
							int l5 = zm;
							j7 = l5 - l;
						}
						jh(so, -j7, j3 + l, 0, fm, k8, l9);
						j3 += k2;
					}
			}

			return;
		}
		for(k = uo; k < vo; k += byte0)
		{
			line t3 = scanline[k];
			l = t3.rnb >> 8;
		int i6 = t3.snb >> 8;
					int k7 = i6 - l;
					if(k7 <= 0)
					{
						j3 += k2;
					} else
					{
						int l8 = t3.tnb;
						int i10 = (t3.unb - l8) / k7;
						if(l < -zm)
						{
							l8 += (-zm - l) * i10;
							l = -zm;
							k7 = i6 - l;
						}
						if(i6 > zm)
						{
							int j6 = zm;
							k7 = j6 - l;
						}
						yh(so, -k7, j3 + l, 0, fm, l8, i10);
						j3 += k2;
					}
		}

	}

	private static void th(int ai1[], int ai2[], int k, int l, int i1, int j1, int k1, int l1, 
			int i2, int j2, int k2, int l2, int i3, int j3)
	{
		if(k2 <= 0)
			return;
		int k3 = 0;
		int l3 = 0;
		int k4 = 0;
		if(k1 != 0)
		{
			k = i1 / k1 << 7;
			l = j1 / k1 << 7;
		}
		if(k < 0)
			k = 0;
		else
			if(k > 16256)
				k = 16256;
		i1 += l1;
		j1 += i2;
		k1 += j2;
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
		int i4 = k3 - k >> 4;
			int j4 = l3 - l >> 4;
			for(int l4 = k2 >> 4; l4 > 0; l4--)
			{
				k += i3 & 0x600000;
				k4 = i3 >> 23;
			i3 += j3;
			ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
			k += i4;
			l += j4;
			ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
							k += i4;
							l += j4;
							ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
					k += i4;
					l += j4;
					ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
				k += i4;
				l += j4;
				k = (k & 0x3fff) + (i3 & 0x600000);
				k4 = i3 >> 23;
				i3 += j3;
				ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
							k += i4;
							l += j4;
							ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
						ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
						ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
						k = (k & 0x3fff) + (i3 & 0x600000);
						k4 = i3 >> 23;
						i3 += j3;
						ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
					k += i4;
					l += j4;
					ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
					k += i4;
					l += j4;
					ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
						k = (k & 0x3fff) + (i3 & 0x600000);
						k4 = i3 >> 23;
						i3 += j3;
						ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
					k += i4;
					l += j4;
					ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
						ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
						ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
					k = k3;
					l = l3;
					i1 += l1;
					j1 += i2;
					k1 += j2;
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
					i4 = k3 - k >> 4;
						j4 = l3 - l >> 4;
			}

			for(int i5 = 0; i5 < (k2 & 0xf); i5++)
			{
				if((i5 & 3) == 0)
				{
					k = (k & 0x3fff) + (i3 & 0x600000);
					k4 = i3 >> 23;
			i3 += j3;
				}
				ai1[l2++] = ai2[(l & 0x3f80) + (k >> 7)] >>> k4;
						k += i4;
						l += j4;
			}

	}

	private static void oi(int ai1[], int ai2[], int k, int l, int i1, int j1, int k1, int l1, 
			int i2, int j2, int k2, int l2, int i3, int j3)
	{
		if(k2 <= 0)
			return;
		int k3 = 0;
		int l3 = 0;
		int k4 = 0;
		if(k1 != 0)
		{
			k = i1 / k1 << 7;
			l = j1 / k1 << 7;
		}
		if(k < 0)
			k = 0;
		else
			if(k > 16256)
				k = 16256;
		i1 += l1;
		j1 += i2;
		k1 += j2;
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
		int i4 = k3 - k >> 4;
			int j4 = l3 - l >> 4;
			for(int l4 = k2 >> 4; l4 > 0; l4--)
			{
				k += i3 & 0x600000;
				k4 = i3 >> 23;
			i3 += j3;
			ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
			k += i4;
			l += j4;
			ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
			k += i4;
			l += j4;
			ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
			k += i4;
			l += j4;
			ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
			k += i4;
			l += j4;
			k = (k & 0x3fff) + (i3 & 0x600000);
			k4 = i3 >> 23;
					i3 += j3;
					ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
					k += i4;
					l += j4;
					ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
					k += i4;
					l += j4;
					ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
					k += i4;
					l += j4;
					ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
					k += i4;
					l += j4;
					k = (k & 0x3fff) + (i3 & 0x600000);
					k4 = i3 >> 23;
				i3 += j3;
				ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				k = (k & 0x3fff) + (i3 & 0x600000);
				k4 = i3 >> 23;
							i3 += j3;
							ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
							k += i4;
							l += j4;
							ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
							k += i4;
							l += j4;
							ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
							k += i4;
							l += j4;
							ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
							k = k3;
							l = l3;
							i1 += l1;
							j1 += i2;
							k1 += j2;
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
							i4 = k3 - k >> 4;
								j4 = l3 - l >> 4;
			}

			for(int i5 = 0; i5 < (k2 & 0xf); i5++)
			{
				if((i5 & 3) == 0)
				{
					k = (k & 0x3fff) + (i3 & 0x600000);
					k4 = i3 >> 23;
			i3 += j3;
				}
				ai1[l2++] = (ai2[(l & 0x3f80) + (k >> 7)] >>> k4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
			}

	}

	private static void ii(int ai1[], int k, int l, int i1, int ai2[], int j1, int k1, int l1, 
			int i2, int j2, int k2, int l2, int i3, int j3, int k3)
	{
		if(l2 <= 0)
			return;
		int l3 = 0;
		int i4 = 0;
		k3 <<= 2;
		if(l1 != 0)
		{
			l3 = j1 / l1 << 7;
			i4 = k1 / l1 << 7;
		}
		if(l3 < 0)
			l3 = 0;
		else
			if(l3 > 16256)
				l3 = 16256;
		for(int l4 = l2; l4 > 0; l4 -= 16)
		{
			j1 += i2;
			k1 += j2;
			l1 += k2;
			l = l3;
			i1 = i4;
			if(l1 != 0)
			{
				l3 = j1 / l1 << 7;
				i4 = k1 / l1 << 7;
			}
			if(l3 < 0)
				l3 = 0;
			else
				if(l3 > 16256)
					l3 = 16256;
			int j4 = l3 - l >> 4;
				int k4 = i4 - i1 >> 4;
				int i5 = j3 >> 23;
			l += j3 & 0x600000;
			j3 += k3;
			if(l4 < 16)
			{
				for(int j5 = 0; j5 < l4; j5++)
				{
					if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
						ai1[i3] = k;
					i3++;
					l += j4;
					i1 += k4;
					if((j5 & 3) == 3)
					{
						l = (l & 0x3fff) + (j3 & 0x600000);
						i5 = j3 >> 23;
		j3 += k3;
					}
				}

			} else
			{
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				l = (l & 0x3fff) + (j3 & 0x600000);
				i5 = j3 >> 23;
				j3 += k3;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				l = (l & 0x3fff) + (j3 & 0x600000);
				i5 = j3 >> 23;
				j3 += k3;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				l = (l & 0x3fff) + (j3 & 0x600000);
				i5 = j3 >> 23;
				j3 += k3;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0x3f80) + (l >> 7)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
			}
		}

	}

	private static void pi(int ai1[], int ai2[], int k, int l, int i1, int j1, int k1, int l1, 
			int i2, int j2, int k2, int l2, int i3, int j3)
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
			k = k3;
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
			int i4 = k3 - k >> 4;
				int j4 = l3 - l >> 4;
				int l4 = i3 >> 20;
			k += i3 & 0xc0000;
			i3 += j3;
			if(k4 < 16)
			{
				for(int i5 = 0; i5 < k4; i5++)
				{
					ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
		k += i4;
		l += j4;
		if((i5 & 3) == 3)
		{
			k = (k & 0xfff) + (i3 & 0xc0000);
			l4 = i3 >> 20;
		i3 += j3;
		}
				}

			} else
			{
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				k = (k & 0xfff) + (i3 & 0xc0000);
				l4 = i3 >> 20;
				i3 += j3;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				k = (k & 0xfff) + (i3 & 0xc0000);
				l4 = i3 >> 20;
				i3 += j3;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				k = (k & 0xfff) + (i3 & 0xc0000);
				l4 = i3 >> 20;
				i3 += j3;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
				k += i4;
				l += j4;
				ai1[l2++] = ai2[(l & 0xfc0) + (k >> 6)] >>> l4;
			}
		}

	}

	private static void xh(int ai1[], int ai2[], int k, int l, int i1, int j1, int k1, int l1, 
			int i2, int j2, int k2, int l2, int i3, int j3)
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
			k = k3;
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
			int i4 = k3 - k >> 4;
				int j4 = l3 - l >> 4;
				int l4 = i3 >> 20;
			k += i3 & 0xc0000;
			i3 += j3;
			if(k4 < 16)
			{
				for(int i5 = 0; i5 < k4; i5++)
				{
					ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
					k += i4;
					l += j4;
					if((i5 & 3) == 3)
					{
						k = (k & 0xfff) + (i3 & 0xc0000);
						l4 = i3 >> 20;
						i3 += j3;
					}
				}

			} else
			{
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				k = (k & 0xfff) + (i3 & 0xc0000);
				l4 = i3 >> 20;
				i3 += j3;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				k = (k & 0xfff) + (i3 & 0xc0000);
				l4 = i3 >> 20;
				i3 += j3;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				k = (k & 0xfff) + (i3 & 0xc0000);
				l4 = i3 >> 20;
				i3 += j3;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
				k += i4;
				l += j4;
				ai1[l2++] = (ai2[(l & 0xfc0) + (k >> 6)] >>> l4) + (ai1[l2] >> 1 & 0x7f7f7f);
			}
		}

	}

	private static void sh(int ai1[], int k, int l, int i1, int ai2[], int j1, int k1, int l1, 
			int i2, int j2, int k2, int l2, int i3, int j3, int k3)
	{
		if(l2 <= 0)
			return;
		int l3 = 0;
		int i4 = 0;
		k3 <<= 2;
		if(l1 != 0)
		{
			l3 = j1 / l1 << 6;
			i4 = k1 / l1 << 6;
		}
		if(l3 < 0)
			l3 = 0;
		else
			if(l3 > 4032)
				l3 = 4032;
		for(int l4 = l2; l4 > 0; l4 -= 16)
		{
			j1 += i2;
			k1 += j2;
			l1 += k2;
			l = l3;
			i1 = i4;
			if(l1 != 0)
			{
				l3 = j1 / l1 << 6;
				i4 = k1 / l1 << 6;
			}
			if(l3 < 0)
				l3 = 0;
			else
				if(l3 > 4032)
					l3 = 4032;
			int j4 = l3 - l >> 4;
				int k4 = i4 - i1 >> 4;
				int i5 = j3 >> 20;
			l += j3 & 0xc0000;
			j3 += k3;
			if(l4 < 16)
			{
				for(int j5 = 0; j5 < l4; j5++)
				{
					if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
						ai1[i3] = k;
					i3++;
					l += j4;
					i1 += k4;
					if((j5 & 3) == 3)
					{
						l = (l & 0xfff) + (j3 & 0xc0000);
						i5 = j3 >> 20;
		j3 += k3;
					}
				}

			} else
			{
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				l = (l & 0xfff) + (j3 & 0xc0000);
				i5 = j3 >> 20;
				j3 += k3;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				l = (l & 0xfff) + (j3 & 0xc0000);
				i5 = j3 >> 20;
				j3 += k3;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				l = (l & 0xfff) + (j3 & 0xc0000);
				i5 = j3 >> 20;
				j3 += k3;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
				l += j4;
				i1 += k4;
				if((k = ai2[(i1 & 0xfc0) + (l >> 6)] >>> i5) != 0)
					ai1[i3] = k;
				i3++;
			}
		}

	}

	private static void jh(int ai1[], int k, int l, int i1, int ai2[], int j1, int k1)
	{
		if(k >= 0)
			return;
		k1 <<= 1;
		i1 = ai2[j1 >> 8 & 0xff];
		j1 += k1;
		int l1 = k / 8;
		for(int i2 = l1; i2 < 0; i2++)
		{
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
		}

		l1 = -(k % 8);
		for(int j2 = 0; j2 < l1; j2++)
		{
			ai1[l++] = i1;
			if((j2 & 1) == 1)
			{
				i1 = ai2[j1 >> 8 & 0xff];
				j1 += k1;
			}
		}

	}

	private static void hi(int ai1[], int k, int l, int i1, int ai2[], int j1, int k1)
	{
		if(k >= 0)
			return;
		k1 <<= 2;
		i1 = ai2[j1 >> 8 & 0xff];
		j1 += k1;
		int l1 = k / 16;
		for(int i2 = l1; i2 < 0; i2++)
		{
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
		}

		l1 = -(k % 16);
		for(int j2 = 0; j2 < l1; j2++)
		{
			ai1[l++] = i1 + (ai1[l] >> 1 & 0x7f7f7f);
			if((j2 & 3) == 3)
			{
				i1 = ai2[j1 >> 8 & 0xff];
				j1 += k1;
				j1 += k1;
			}
		}

	}

	private static void yh(int ai1[], int k, int l, int i1, int ai2[], int j1, int k1)
	{
		if(k >= 0)
			return;
		k1 <<= 2;
		i1 = ai2[j1 >> 8 & 0xff];
		j1 += k1;
		int l1 = k / 16;
		for(int i2 = l1; i2 < 0; i2++)
		{
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			ai1[l++] = i1;
			i1 = ai2[j1 >> 8 & 0xff];
			j1 += k1;
		}

		l1 = -(k % 16);
		for(int j2 = 0; j2 < l1; j2++)
		{
			ai1[l++] = i1;
			if((j2 & 3) == 3)
			{
				i1 = ai2[j1 >> 8 & 0xff];
				j1 += k1;
			}
		}

	}

	public void ai(int k, int l, int i1, int j1, int k1, int l1, int i2)
	{
		j1 &= 0x3ff;
		k1 &= 0x3ff;
		l1 &= 0x3ff;
		in = 1024 - j1 & 0x3ff;
		jn = 1024 - k1 & 0x3ff;
		kn = 1024 - l1 & 0x3ff;
		int j2 = 0;
		int k2 = 0;
		int l2 = i2;
		if(j1 != 0)
		{
			int i3 = mm[j1];
			int l3 = mm[j1 + 1024];
			int k4 = k2 * l3 - l2 * i3 >> 15;
			l2 = k2 * i3 + l2 * l3 >> 15;
			k2 = k4;
		}
		if(k1 != 0)
		{
			int j3 = mm[k1];
			int i4 = mm[k1 + 1024];
			int l4 = l2 * j3 + j2 * i4 >> 15;
			l2 = l2 * i4 - j2 * j3 >> 15;
			j2 = l4;
		}
		if(l1 != 0)
		{
			int k3 = mm[l1];
			int j4 = mm[l1 + 1024];
			int i5 = k2 * k3 + j2 * j4 >> 15;
			k2 = k2 * j4 - j2 * k3 >> 15;
			j2 = i5;
		}
		fn = k - j2;
		gn = l - k2;
		hn = i1 - l2;
	}

	private void xi(int k)
	{
		r r1 = qn[k];
		model h1 = r1.dnb;
		int l = r1.enb;
		int ai1[] = h1.yg[l];
		int i1 = h1.xg[l];
		int j1 = h1.ch[l];
		int l1 = h1.pg[ai1[0]];
		int i2 = h1.qg[ai1[0]];
		int j2 = h1.rg[ai1[0]];
		int k2 = h1.pg[ai1[1]] - l1;
		int l2 = h1.qg[ai1[1]] - i2;
		int i3 = h1.rg[ai1[1]] - j2;
		int j3 = h1.pg[ai1[2]] - l1;
		int k3 = h1.qg[ai1[2]] - i2;
		int l3 = h1.rg[ai1[2]] - j2;
		int i4 = l2 * l3 - k3 * i3;
		int j4 = i3 * j3 - l3 * k2;
		int k4 = k2 * k3 - j3 * l2;
		if(j1 == -1)
		{
			j1 = 0;
			for(; i4 > 25000 || j4 > 25000 || k4 > 25000 || i4 < -25000 || j4 < -25000 || k4 < -25000; k4 >>= 1)
			{
				j1++;
				i4 >>= 1;
			j4 >>= 1;
			}

			h1.ch[l] = j1;
			h1.bh[l] = (int)((double)en * Math.sqrt(i4 * i4 + j4 * j4 + k4 * k4));
		} else
		{
			i4 >>= j1;
			j4 >>= j1;
			k4 >>= j1;
		}
		r1.jnb = l1 * i4 + i2 * j4 + j2 * k4;
		r1.gnb = i4;
		r1.hnb = j4;
		r1.inb = k4;
		int l4 = h1.rg[ai1[0]];
		int i5 = l4;
		int j5 = h1.sg[ai1[0]];
		int k5 = j5;
		int l5 = h1.tg[ai1[0]];
		int i6 = l5;
		for(int j6 = 1; j6 < i1; j6++)
		{
			int k1 = h1.rg[ai1[j6]];
			if(k1 > i5)
				i5 = k1;
			else
				if(k1 < l4)
					l4 = k1;
			k1 = h1.sg[ai1[j6]];
			if(k1 > k5)
				k5 = k1;
			else
				if(k1 < j5)
					j5 = k1;
			k1 = h1.tg[ai1[j6]];
			if(k1 > i6)
				i6 = k1;
			else
				if(k1 < l5)
					l5 = k1;
		}

		r1.bnb = l4;
		r1.cnb = i5;
		r1.xmb = j5;
		r1.zmb = k5;
		r1.ymb = l5;
		r1.anb = i6;
	}

	private void zi(int k)
	{
		r r1 = qn[k];
		model h1 = r1.dnb;
		int l = r1.enb;
		int ai1[] = h1.yg[l];
		int j1 = 0;
		int k1 = 0;
		int l1 = 1;
		int i2 = h1.pg[ai1[0]];
		int j2 = h1.qg[ai1[0]];
		int k2 = h1.rg[ai1[0]];
		h1.bh[l] = 1;
		h1.ch[l] = 0;
		r1.jnb = i2 * j1 + j2 * k1 + k2 * l1;
		r1.gnb = j1;
		r1.hnb = k1;
		r1.inb = l1;
		int l2 = h1.rg[ai1[0]];
		int i3 = l2;
		int j3 = h1.sg[ai1[0]];
		int k3 = j3;
		if(h1.sg[ai1[1]] < j3)
			j3 = h1.sg[ai1[1]];
		else
			k3 = h1.sg[ai1[1]];
		int l3 = h1.tg[ai1[1]];
		int i4 = h1.tg[ai1[0]];
		int i1 = h1.rg[ai1[1]];
		if(i1 > i3)
			i3 = i1;
		else
			if(i1 < l2)
				l2 = i1;
		i1 = h1.sg[ai1[1]];
		if(i1 > k3)
			k3 = i1;
		else
			if(i1 < j3)
				j3 = i1;
		i1 = h1.tg[ai1[1]];
		if(i1 > i4)
			i4 = i1;
		else
			if(i1 < l3)
				l3 = i1;
		r1.bnb = l2;
		r1.cnb = i3;
		r1.xmb = j3 - 20;
		r1.zmb = k3 + 20;
		r1.ymb = l3;
		r1.anb = i4;
	}

	private boolean ih(r r1, r r2)
	{
		if(r1.xmb >= r2.zmb)
			return true;
		if(r2.xmb >= r1.zmb)
			return true;
		if(r1.ymb >= r2.anb)
			return true;
		if(r2.ymb >= r1.anb)
			return true;
		if(r1.bnb >= r2.cnb)
			return true;
		if(r2.bnb > r1.cnb)
			return false;
		model h1 = r1.dnb;
		model h2 = r2.dnb;
		int k = r1.enb;
		int l = r2.enb;
		int ai1[] = h1.yg[k];
		int ai2[] = h2.yg[l];
		int i1 = h1.xg[k];
		int j1 = h2.xg[l];
		int i3 = h2.pg[ai2[0]];
		int j3 = h2.qg[ai2[0]];
		int k3 = h2.rg[ai2[0]];
		int l3 = r2.gnb;
		int i4 = r2.hnb;
		int j4 = r2.inb;
		int k4 = h2.bh[l];
		int l4 = r2.jnb;
		boolean flag = false;
		for(int i5 = 0; i5 < i1; i5++)
		{
			int k1 = ai1[i5];
			int k2 = (i3 - h1.pg[k1]) * l3 + (j3 - h1.qg[k1]) * i4 + (k3 - h1.rg[k1]) * j4;
			if((k2 >= -k4 || l4 >= 0) && (k2 <= k4 || l4 <= 0))
				continue;
			flag = true;
			break;
		}

		if(!flag)
			return true;
		i3 = h1.pg[ai1[0]];
		j3 = h1.qg[ai1[0]];
		k3 = h1.rg[ai1[0]];
		l3 = r1.gnb;
		i4 = r1.hnb;
		j4 = r1.inb;
		k4 = h1.bh[k];
		l4 = r1.jnb;
		flag = false;
		for(int j5 = 0; j5 < j1; j5++)
		{
			int l1 = ai2[j5];
			int l2 = (i3 - h2.pg[l1]) * l3 + (j3 - h2.qg[l1]) * i4 + (k3 - h2.rg[l1]) * j4;
			if((l2 >= -k4 || l4 <= 0) && (l2 <= k4 || l4 >= 0))
				continue;
			flag = true;
			break;
		}

		if(!flag)
			return true;
		int ai3[];
		int ai4[];
		if(i1 == 2)
		{
			ai3 = new int[4];
			ai4 = new int[4];
			int k5 = ai1[0];
			int i2 = ai1[1];
			ai3[0] = h1.sg[k5] - 20;
			ai3[1] = h1.sg[i2] - 20;
			ai3[2] = h1.sg[i2] + 20;
			ai3[3] = h1.sg[k5] + 20;
			ai4[0] = ai4[3] = h1.tg[k5];
			ai4[1] = ai4[2] = h1.tg[i2];
		} else
		{
			ai3 = new int[i1];
			ai4 = new int[i1];
			for(int l5 = 0; l5 < i1; l5++)
			{
				int k6 = ai1[l5];
				ai3[l5] = h1.sg[k6];
				ai4[l5] = h1.tg[k6];
			}

		}
		int ai5[];
		int ai6[];
		if(j1 == 2)
		{
			ai5 = new int[4];
			ai6 = new int[4];
			int i6 = ai2[0];
			int j2 = ai2[1];
			ai5[0] = h2.sg[i6] - 20;
			ai5[1] = h2.sg[j2] - 20;
			ai5[2] = h2.sg[j2] + 20;
			ai5[3] = h2.sg[i6] + 20;
			ai6[0] = ai6[3] = h2.tg[i6];
			ai6[1] = ai6[2] = h2.tg[j2];
		} else
		{
			ai5 = new int[j1];
			ai6 = new int[j1];
			for(int j6 = 0; j6 < j1; j6++)
			{
				int l6 = ai2[j6];
				ai5[j6] = h2.sg[l6];
				ai6[j6] = h2.tg[l6];
			}

		}
		return !kh(ai3, ai4, ai5, ai6);
	}

	private boolean gh(r r1, r r2)
	{
		model h1 = r1.dnb;
		model h2 = r2.dnb;
		int k = r1.enb;
		int l = r2.enb;
		int ai1[] = h1.yg[k];
		int ai2[] = h2.yg[l];
		int i1 = h1.xg[k];
		int j1 = h2.xg[l];
		int k2 = h2.pg[ai2[0]];
		int l2 = h2.qg[ai2[0]];
		int i3 = h2.rg[ai2[0]];
		int j3 = r2.gnb;
		int k3 = r2.hnb;
		int l3 = r2.inb;
		int i4 = h2.bh[l];
		int j4 = r2.jnb;
		boolean flag = false;
		for(int k4 = 0; k4 < i1; k4++)
		{
			int k1 = ai1[k4];
			int i2 = (k2 - h1.pg[k1]) * j3 + (l2 - h1.qg[k1]) * k3 + (i3 - h1.rg[k1]) * l3;
			if((i2 >= -i4 || j4 >= 0) && (i2 <= i4 || j4 <= 0))
				continue;
			flag = true;
			break;
		}

		if(!flag)
			return true;
		k2 = h1.pg[ai1[0]];
		l2 = h1.qg[ai1[0]];
		i3 = h1.rg[ai1[0]];
		j3 = r1.gnb;
		k3 = r1.hnb;
		l3 = r1.inb;
		i4 = h1.bh[k];
		j4 = r1.jnb;
		flag = false;
		for(int l4 = 0; l4 < j1; l4++)
		{
			int l1 = ai2[l4];
			int j2 = (k2 - h2.pg[l1]) * j3 + (l2 - h2.qg[l1]) * k3 + (i3 - h2.rg[l1]) * l3;
			if((j2 >= -i4 || j4 <= 0) && (j2 <= i4 || j4 >= 0))
				continue;
			flag = true;
			break;
		}

		return !flag;
	}

	public void inittextures(int k, int l, int i1)
	{
		fo = k;
		go = new byte[k][];
		ho = new int[k][];
		io = new int[k];
		jo = new long[k];
		lo = new boolean[k];
		ko = new int[k][];
		mo = 0L;
		no = new int[l][];
		oo = new int[i1][];
	}

	public void vh(int k, byte abyte0[], int ai1[], int l)
	{
		go[k] = abyte0;
		ho[k] = ai1;
		io[k] = l;
		jo[k] = 0L;
		lo[k] = false;
		ko[k] = null;
		li(k);
	}

	public void li(int k)
	{
		if(k < 0)
			return;
		jo[k] = mo++;
		if(ko[k] != null)
			return;
		if(io[k] == 0)
		{
			for(int l = 0; l < no.length; l++)
				if(no[l] == null)
				{
					no[l] = new int[16384];
					ko[k] = no[l];
					ji(k);
					return;
				}

			long l1 = 1L << 30;
			int j1 = 0;
			for(int i2 = 0; i2 < fo; i2++)
				if(i2 != k && io[i2] == 0 && ko[i2] != null && jo[i2] < l1)
				{
					l1 = jo[i2];
					j1 = i2;
				}

			ko[k] = ko[j1];
			ko[j1] = null;
			ji(k);
			return;
		}
		for(int i1 = 0; i1 < oo.length; i1++)
			if(oo[i1] == null)
			{
				oo[i1] = new int[0x10000];
				ko[k] = oo[i1];
				ji(k);
				return;
			}

		long l2 = 1L << 30;
		int k1 = 0;
		for(int j2 = 0; j2 < fo; j2++)
			if(j2 != k && io[j2] == 1 && ko[j2] != null && jo[j2] < l2)
			{
				l2 = jo[j2];
				k1 = j2;
			}

		ko[k] = ko[k1];
		ko[k1] = null;
		ji(k);
	}

	private void ji(int k)
	{
		char c;
		if(io[k] == 0)
			c = '@';
		else
			c = '\200';
		int ai1[] = ko[k];
		int l = 0;
		for(int i1 = 0; i1 < c; i1++)
		{
			for(int j1 = 0; j1 < c; j1++)
			{
				int l1 = ho[k][go[k][j1 + i1 * c] & 0xff];
				l1 &= 0xf8f8ff;
				if(l1 == 0)
					l1 = 1;
				else
					if(l1 == 0xf800ff)
					{
						l1 = 0;
						lo[k] = true;
					}
				ai1[l++] = l1;
			}

		}

		for(int k1 = 0; k1 < l; k1++)
		{
			int i2 = ai1[k1];
			ai1[l + k1] = i2 - (i2 >>> 3) & 0xf8f8ff;
			ai1[l * 2 + k1] = i2 - (i2 >>> 2) & 0xf8f8ff;
			ai1[l * 3 + k1] = i2 - (i2 >>> 2) - (i2 >>> 3) & 0xf8f8ff;
		}

	}

	public void gi(int k)
	{
		if(ko[k] == null)
			return;
		int ai1[] = ko[k];
		for(int l = 0; l < 64; l++)
		{
			int i1 = l + 4032;
			int j1 = ai1[i1];
			for(int l1 = 0; l1 < 63; l1++)
			{
				ai1[i1] = ai1[i1 - 64];
				i1 -= 64;
			}

			ko[k][i1] = j1;
		}

		char c = '\u1000';
		for(int k1 = 0; k1 < c; k1++)
		{
			int i2 = ai1[k1];
			ai1[c + k1] = i2 - (i2 >>> 3) & 0xf8f8ff;
			ai1[c * 2 + k1] = i2 - (i2 >>> 2) & 0xf8f8ff;
			ai1[c * 3 + k1] = i2 - (i2 >>> 2) - (i2 >>> 3) & 0xf8f8ff;
		}

	}

	public int ti(int k)
	{
		if(k == 0xbc614e)
			return 0;
		li(k);
		if(k >= 0)
			return ko[k][0];
		if(k < 0)
		{
			k = -(k + 1);
			int l = k >> 10 & 0x1f;
		int i1 = k >> 5 & 0x1f;
		int j1 = k & 0x1f;
		return (l << 19) + (i1 << 11) + (j1 << 3);
		} else
		{
			return 0;
		}
	}

	public void di(int k, int l, int i1)
	{
		if(k == 0 && l == 0 && i1 == 0)
			k = 32;
		for(int j1 = 0; j1 < ln; j1++)
			nn[j1].ye(k, l, i1);

	}

	public void yi(int k, int l, int i1, int j1, int k1)
	{
		if(i1 == 0 && j1 == 0 && k1 == 0)
			i1 = 32;
		for(int l1 = 0; l1 < ln; l1++)
			nn[l1].be(k, l, i1, j1, k1);

	}

	public static int fi(int k, int l, int i1)
	{
		return -1 - (k / 8) * 1024 - (l / 8) * 32 - i1 / 8;
	}

	public int nh(int k, int l, int i1, int j1, int k1)
	{
		if(j1 == l)
			return k;
		else
			return k + ((i1 - k) * (k1 - l)) / (j1 - l);
	}

	public boolean ci(int k, int l, int i1, int j1, boolean flag)
	{
		if(flag && k <= i1 || k < i1)
		{
			if(k > j1)
				return true;
			if(l > i1)
				return true;
			if(l > j1)
				return true;
			return !flag;
		}
		if(k < j1)
			return true;
		if(l < i1)
			return true;
		if(l < j1)
			return true;
		else
			return flag;
	}

	public boolean rh(int k, int l, int i1, boolean flag)
	{
		if(flag && k <= i1 || k < i1)
		{
			if(l > i1)
				return true;
			return !flag;
		}
		if(l < i1)
			return true;
		else
			return flag;
	}

	public boolean kh(int ai1[], int ai2[], int ai3[], int ai4[])
	{
		int k = ai1.length;
		int l = ai3.length;
		byte byte0 = 0;
		int k20;
		int i21 = k20 = ai2[0];
		int i1 = 0;
		int l20;
		int j21 = l20 = ai4[0];
		int k1 = 0;
		for(int k21 = 1; k21 < k; k21++)
			if(ai2[k21] < k20)
			{
				k20 = ai2[k21];
				i1 = k21;
			} else
				if(ai2[k21] > i21)
					i21 = ai2[k21];

		for(int l21 = 1; l21 < l; l21++)
			if(ai4[l21] < l20)
			{
				l20 = ai4[l21];
				k1 = l21;
			} else
				if(ai4[l21] > j21)
					j21 = ai4[l21];

		if(l20 >= i21)
			return false;
		if(k20 >= j21)
			return false;
		int j1;
		int l1;
		boolean flag;
		if(ai2[i1] < ai4[k1])
		{
			for(j1 = i1; ai2[j1] < ai4[k1]; j1 = (j1 + 1) % k);
			for(; ai2[i1] < ai4[k1]; i1 = ((i1 - 1) + k) % k);
			int i2 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[k1]);
			int i7 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[k1]);
			int j11 = ai3[k1];
			flag = (i2 < j11) | (i7 < j11);
			if(rh(i2, i7, j11, flag))
				return true;
			l1 = (k1 + 1) % l;
			k1 = ((k1 - 1) + l) % l;
			if(i1 == j1)
				byte0 = 1;
		} else
		{
			for(l1 = k1; ai4[l1] < ai2[i1]; l1 = (l1 + 1) % l);
			for(; ai4[k1] < ai2[i1]; k1 = ((k1 - 1) + l) % l);
			int j2 = ai1[i1];
			int k11 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[i1]);
			int j16 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[i1]);
			flag = (j2 < k11) | (j2 < j16);
			if(rh(k11, j16, j2, !flag))
				return true;
			j1 = (i1 + 1) % k;
			i1 = ((i1 - 1) + k) % k;
			if(k1 == l1)
				byte0 = 2;
		}
		while(byte0 == 0) 
			if(ai2[i1] < ai2[j1])
			{
				if(ai2[i1] < ai4[k1])
				{
					if(ai2[i1] < ai4[l1])
					{
						int k2 = ai1[i1];
						int j7 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai2[i1]);
						int l11 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[i1]);
						int k16 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[i1]);
						if(ci(k2, j7, l11, k16, flag))
							return true;
						i1 = ((i1 - 1) + k) % k;
						if(i1 == j1)
							byte0 = 1;
					} else
					{
						int l2 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[l1]);
						int k7 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[l1]);
						int i12 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai4[l1]);
						int l16 = ai3[l1];
						if(ci(l2, k7, i12, l16, flag))
							return true;
						l1 = (l1 + 1) % l;
						if(k1 == l1)
							byte0 = 2;
					}
				} else
					if(ai4[k1] < ai4[l1])
					{
						int i3 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[k1]);
						int l7 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[k1]);
						int j12 = ai3[k1];
						int i17 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai4[k1]);
						if(ci(i3, l7, j12, i17, flag))
							return true;
						k1 = ((k1 - 1) + l) % l;
						if(k1 == l1)
							byte0 = 2;
					} else
					{
						int j3 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[l1]);
						int i8 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[l1]);
						int k12 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai4[l1]);
						int j17 = ai3[l1];
						if(ci(j3, i8, k12, j17, flag))
							return true;
						l1 = (l1 + 1) % l;
						if(k1 == l1)
							byte0 = 2;
					}
			} else
				if(ai2[j1] < ai4[k1])
				{
					if(ai2[j1] < ai4[l1])
					{
						int k3 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai2[j1]);
						int j8 = ai1[j1];
						int l12 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[j1]);
						int k17 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[j1]);
						if(ci(k3, j8, l12, k17, flag))
							return true;
						j1 = (j1 + 1) % k;
						if(i1 == j1)
							byte0 = 1;
					} else
					{
						int l3 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[l1]);
						int k8 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[l1]);
						int i13 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai4[l1]);
						int l17 = ai3[l1];
						if(ci(l3, k8, i13, l17, flag))
							return true;
						l1 = (l1 + 1) % l;
						if(k1 == l1)
							byte0 = 2;
					}
				} else
					if(ai4[k1] < ai4[l1])
					{
						int i4 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[k1]);
						int l8 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[k1]);
						int j13 = ai3[k1];
						int i18 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai4[k1]);
						if(ci(i4, l8, j13, i18, flag))
							return true;
						k1 = ((k1 - 1) + l) % l;
						if(k1 == l1)
							byte0 = 2;
					} else
					{
						int j4 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[l1]);
						int i9 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[l1]);
						int k13 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai4[l1]);
						int j18 = ai3[l1];
						if(ci(j4, i9, k13, j18, flag))
							return true;
						l1 = (l1 + 1) % l;
						if(k1 == l1)
							byte0 = 2;
					}
		while(byte0 == 1) 
			if(ai2[i1] < ai4[k1])
			{
				if(ai2[i1] < ai4[l1])
				{
					int k4 = ai1[i1];
					int l13 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[i1]);
					int k18 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[i1]);
					return rh(l13, k18, k4, !flag);
				}
				int l4 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[l1]);
				int j9 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[l1]);
				int i14 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai4[l1]);
				int l18 = ai3[l1];
				if(ci(l4, j9, i14, l18, flag))
					return true;
				l1 = (l1 + 1) % l;
				if(k1 == l1)
					byte0 = 0;
			} else
				if(ai4[k1] < ai4[l1])
				{
					int i5 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[k1]);
					int k9 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[k1]);
					int j14 = ai3[k1];
					int i19 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai4[k1]);
					if(ci(i5, k9, j14, i19, flag))
						return true;
					k1 = ((k1 - 1) + l) % l;
					if(k1 == l1)
						byte0 = 0;
				} else
				{
					int j5 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[l1]);
					int l9 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[l1]);
					int k14 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai4[l1]);
					int j19 = ai3[l1];
					if(ci(j5, l9, k14, j19, flag))
						return true;
					l1 = (l1 + 1) % l;
					if(k1 == l1)
						byte0 = 0;
				}
		while(byte0 == 2) 
			if(ai4[k1] < ai2[i1])
			{
				if(ai4[k1] < ai2[j1])
				{
					int k5 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[k1]);
					int i10 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[k1]);
					int l14 = ai3[k1];
					return rh(k5, i10, l14, flag);
				}
				int l5 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai2[j1]);
				int j10 = ai1[j1];
				int i15 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[j1]);
				int k19 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[j1]);
				if(ci(l5, j10, i15, k19, flag))
					return true;
				j1 = (j1 + 1) % k;
				if(i1 == j1)
					byte0 = 0;
			} else
				if(ai2[i1] < ai2[j1])
				{
					int i6 = ai1[i1];
					int k10 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai2[i1]);
					int j15 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[i1]);
					int l19 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[i1]);
					if(ci(i6, k10, j15, l19, flag))
						return true;
					i1 = ((i1 - 1) + k) % k;
					if(i1 == j1)
						byte0 = 0;
				} else
				{
					int j6 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai2[j1]);
					int l10 = ai1[j1];
					int k15 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[j1]);
					int i20 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[j1]);
					if(ci(j6, l10, k15, i20, flag))
						return true;
					j1 = (j1 + 1) % k;
					if(i1 == j1)
						byte0 = 0;
				}
		if(ai2[i1] < ai4[k1])
		{
			int k6 = ai1[i1];
			int l15 = nh(ai3[(k1 + 1) % l], ai4[(k1 + 1) % l], ai3[k1], ai4[k1], ai2[i1]);
			int j20 = nh(ai3[((l1 - 1) + l) % l], ai4[((l1 - 1) + l) % l], ai3[l1], ai4[l1], ai2[i1]);
			return rh(l15, j20, k6, !flag);
		}
		int l6 = nh(ai1[(i1 + 1) % k], ai2[(i1 + 1) % k], ai1[i1], ai2[i1], ai4[k1]);
		int i11 = nh(ai1[((j1 - 1) + k) % k], ai2[((j1 - 1) + k) % k], ai1[j1], ai2[j1], ai4[k1]);
		int i16 = ai3[k1];
		return rh(l6, i11, i16, flag);
	}

	public static final int bm = 0;
	int cm;
	int dm[];
	int em[][];
	int fm[];
	public int gm;
	public int hm;
	public int im;
	public int jm;
	public int km;
	public int lm;
	public static int mm[] = new int[2048];
	private static int nm[] = new int[512];
	public boolean om;
	public double pm;
	public int qm;
	private boolean rm;
	private int sm;
	private int tm;
	private int um;
	private int vm;
	private model wm[];
	private int xm[];
	private int ym;
	private int zm;
	private int an;
	private int bn;
	private int cn;
	private int dn;
	private int en;
	private int fn;
	private int gn;
	private int hn;
	private int in;
	private int jn;
	private int kn;
	public int ln;
	public int mn;
	public model nn[];
	private int on[];
	private int pn;
	private r qn[];
	private int rn;
	private int sn;
	private int tn[];
	private int un[];
	private int vn[];
	private int wn[];
	private int xn[];
	private int yn[];
	private int zn[];
	public model ao;
	private static final int bo = 16;
	private static final int co = 4;
	private static final int _flddo = 5;
	private static final int eo = 0xbc614e;
	int fo;
	byte go[][];
	int ho[][];
	int io[];
	long jo[];
	int ko[][];
	boolean lo[];
	private static long mo;
	int no[][];
	int oo[][];
	private static byte po[];
	private static int qo[] = new int[256];
	graphics ro;
	public int so[];
	line scanline[];
	int uo;
	int vo;
	int wo[];
	int xo[];
	int yo[];
	int zo[];
	int ap[];
	int bp[];
	boolean cp;
	static int dp;
	static int ep;
	static int fp;
	static int gp;
	static int hp;
	static int ip;
	int jp;
	int kp;

}
