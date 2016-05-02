import jagex.util;
import jagex.client.camera;
import jagex.client.graphics;
import jagex.client.model;

import java.io.IOException;

public class world {

	public world(camera camera, graphics graphics)
	{
		xhb = false;
		yhb = true;
		bib = 750;
		eib = new int[256];
		nib = new byte[4][2304];
		oib = new byte[4][2304];
		pib = new byte[4][2304];
		qib = new byte[4][2304];
		rib = new byte[4][2304];
		sib = new byte[4][2304];
		tib = new byte[4][2304];
		uib = new int[4][2304];
		vib = 96;
		wib = 96;
		xib = new int[vib * wib * 2];
		yib = new int[vib * wib * 2];
		zib = new int[vib][wib];
		ajb = new int[vib][wib];
		bjb = new int[vib][wib];
		cjb = false;
		djb = new model[64];
		ejb = new model[4][64];
		fjb = new model[4][64];
		aib = camera;
		zhb = graphics;
		for(int l = 0; l < 64; l++)
			eib[l] = camera.fi(255 - l * 4, 255 - (int)((double)l * 1.75D), 255 - l * 4);

		for(int i1 = 0; i1 < 64; i1++)
			eib[i1 + 64] = camera.fi(i1 * 3, 144, 0);

		for(int k1 = 0; k1 < 64; k1++)
			eib[k1 + 128] = camera.fi(192 - (int)((double)k1 * 1.5D), 144 - (int)((double)k1 * 1.5D), 0);

		for(int l1 = 0; l1 < 64; l1++)
			eib[l1 + 192] = camera.fi(96 - (int)((double)l1 * 1.5D), 48 + (int)((double)l1 * 1.5D), 0);

	}

	public int ho(int k, int l, int i1, int j1, int k1, int l1, int ai[], 
			int ai1[], boolean flag)
	{
		for(int i2 = 0; i2 < vib; i2++)
		{
			for(int j2 = 0; j2 < wib; j2++)
				zib[i2][j2] = 0;

		}

		int k2 = 0;
		int l2 = 0;
		int i3 = k;
		int j3 = l;
		zib[k][l] = 99;
		ai[k2] = k;
		ai1[k2++] = l;
		int k3 = ai.length;
		boolean flag1 = false;
		while(l2 != k2) 
		{
			i3 = ai[l2];
			j3 = ai1[l2];
			l2 = (l2 + 1) % k3;
			if(i3 >= i1 && i3 <= k1 && j3 >= j1 && j3 <= l1)
			{
				flag1 = true;
				break;
			}
			if(flag)
			{
				if(i3 > 0 && i3 - 1 >= i1 && i3 - 1 <= k1 && j3 >= j1 && j3 <= l1 && (ajb[i3 - 1][j3] & 8) == 0)
				{
					flag1 = true;
					break;
				}
				if(i3 < vib - 1 && i3 + 1 >= i1 && i3 + 1 <= k1 && j3 >= j1 && j3 <= l1 && (ajb[i3 + 1][j3] & 2) == 0)
				{
					flag1 = true;
					break;
				}
				if(j3 > 0 && i3 >= i1 && i3 <= k1 && j3 - 1 >= j1 && j3 - 1 <= l1 && (ajb[i3][j3 - 1] & 4) == 0)
				{
					flag1 = true;
					break;
				}
				if(j3 < wib - 1 && i3 >= i1 && i3 <= k1 && j3 + 1 >= j1 && j3 + 1 <= l1 && (ajb[i3][j3 + 1] & 1) == 0)
				{
					flag1 = true;
					break;
				}
			}
			if(i3 > 0 && zib[i3 - 1][j3] == 0 && (ajb[i3 - 1][j3] & 0x78) == 0)
			{
				ai[k2] = i3 - 1;
				ai1[k2] = j3;
				k2 = (k2 + 1) % k3;
				zib[i3 - 1][j3] = 2;
			}
			if(i3 < vib - 1 && zib[i3 + 1][j3] == 0 && (ajb[i3 + 1][j3] & 0x72) == 0)
			{
				ai[k2] = i3 + 1;
				ai1[k2] = j3;
				k2 = (k2 + 1) % k3;
				zib[i3 + 1][j3] = 8;
			}
			if(j3 > 0 && zib[i3][j3 - 1] == 0 && (ajb[i3][j3 - 1] & 0x74) == 0)
			{
				ai[k2] = i3;
				ai1[k2] = j3 - 1;
				k2 = (k2 + 1) % k3;
				zib[i3][j3 - 1] = 1;
			}
			if(j3 < wib - 1 && zib[i3][j3 + 1] == 0 && (ajb[i3][j3 + 1] & 0x71) == 0)
			{
				ai[k2] = i3;
				ai1[k2] = j3 + 1;
				k2 = (k2 + 1) % k3;
				zib[i3][j3 + 1] = 4;
			}
			if(i3 > 0 && j3 > 0 && (ajb[i3][j3 - 1] & 0x74) == 0 && (ajb[i3 - 1][j3] & 0x78) == 0 && (ajb[i3 - 1][j3 - 1] & 0x7c) == 0 && zib[i3 - 1][j3 - 1] == 0)
			{
				ai[k2] = i3 - 1;
				ai1[k2] = j3 - 1;
				k2 = (k2 + 1) % k3;
				zib[i3 - 1][j3 - 1] = 3;
			}
			if(i3 < vib - 1 && j3 > 0 && (ajb[i3][j3 - 1] & 0x74) == 0 && (ajb[i3 + 1][j3] & 0x72) == 0 && (ajb[i3 + 1][j3 - 1] & 0x76) == 0 && zib[i3 + 1][j3 - 1] == 0)
			{
				ai[k2] = i3 + 1;
				ai1[k2] = j3 - 1;
				k2 = (k2 + 1) % k3;
				zib[i3 + 1][j3 - 1] = 9;
			}
			if(i3 > 0 && j3 < wib - 1 && (ajb[i3][j3 + 1] & 0x71) == 0 && (ajb[i3 - 1][j3] & 0x78) == 0 && (ajb[i3 - 1][j3 + 1] & 0x79) == 0 && zib[i3 - 1][j3 + 1] == 0)
			{
				ai[k2] = i3 - 1;
				ai1[k2] = j3 + 1;
				k2 = (k2 + 1) % k3;
				zib[i3 - 1][j3 + 1] = 6;
			}
			if(i3 < vib - 1 && j3 < wib - 1 && (ajb[i3][j3 + 1] & 0x71) == 0 && (ajb[i3 + 1][j3] & 0x72) == 0 && (ajb[i3 + 1][j3 + 1] & 0x73) == 0 && zib[i3 + 1][j3 + 1] == 0)
			{
				ai[k2] = i3 + 1;
				ai1[k2] = j3 + 1;
				k2 = (k2 + 1) % k3;
				zib[i3 + 1][j3 + 1] = 12;
			}
		}
		if(!flag1)
			return -1;
		l2 = 0;
		ai[l2] = i3;
		ai1[l2++] = j3;
		int i4;
		for(int l3 = i4 = zib[i3][j3]; i3 != k || j3 != l; l3 = zib[i3][j3])
		{
			if(l3 != i4)
			{
				i4 = l3;
				ai[l2] = i3;
				ai1[l2++] = j3;
			}
			if((l3 & 2) != 0)
				i3++;
			else
				if((l3 & 8) != 0)
					i3--;
			if((l3 & 1) != 0)
				j3++;
			else
				if((l3 & 4) != 0)
					j3--;
		}

		return l2;
	}

	public void gp(int k, int l, int i1)
	{
		ajb[k][l] |= i1;
	}

	public void zo(int k, int l, int i1)
	{
		ajb[k][l] &= 65535 - i1;
	}

	public void hp(int k, int l, int i1, int j1)
	{
		if(k < 0 || l < 0 || k >= vib - 1 || l >= wib - 1)
			return;
		if(cache.qlb[j1] == 1)
		{
			if(i1 == 0)
			{
				ajb[k][l] |= 1;
				if(l > 0)
					gp(k, l - 1, 4);
			} else
				if(i1 == 1)
				{
					ajb[k][l] |= 2;
					if(k > 0)
						gp(k - 1, l, 8);
				} else
					if(i1 == 2)
						ajb[k][l] |= 0x10;
					else
						if(i1 == 3)
							ajb[k][l] |= 0x20;
			no(k, l, 1, 1);
		}
	}

	public void fo(int k, int l, int i1, int j1)
	{
		if(k < 0 || l < 0 || k >= vib - 1 || l >= wib - 1)
			return;
		if(cache.qlb[j1] == 1)
		{
			if(i1 == 0)
			{
				ajb[k][l] &= 0xfffe;
				if(l > 0)
					zo(k, l - 1, 4);
			} else
				if(i1 == 1)
				{
					ajb[k][l] &= 0xfffd;
					if(k > 0)
						zo(k - 1, l, 8);
				} else
					if(i1 == 2)
						ajb[k][l] &= 0xffef;
					else
						if(i1 == 3)
							ajb[k][l] &= 0xffdf;
			no(k, l, 1, 1);
		}
	}

	public void vo(int k, int l, int i1)
	{
		if(k < 0 || l < 0 || k >= vib - 1 || l >= wib - 1)
			return;
		if(cache.glb[i1] == 1 || cache.glb[i1] == 2)
		{
			int j1 = io(k, l);
			int k1;
			int l1;
			if(j1 == 0 || j1 == 4)
			{
				k1 = cache.elb[i1];
				l1 = cache.flb[i1];
			} else
			{
				l1 = cache.elb[i1];
				k1 = cache.flb[i1];
			}
			for(int i2 = k; i2 < k + k1; i2++)
			{
				for(int j2 = l; j2 < l + l1; j2++)
					if(cache.glb[i1] == 1)
						ajb[i2][j2] |= 0x40;
					else
						if(j1 == 0)
						{
							ajb[i2][j2] |= 2;
							if(i2 > 0)
								gp(i2 - 1, j2, 8);
						} else
							if(j1 == 2)
							{
								ajb[i2][j2] |= 4;
								if(j2 < wib - 1)
									gp(i2, j2 + 1, 1);
							} else
								if(j1 == 4)
								{
									ajb[i2][j2] |= 8;
									if(i2 < vib - 1)
										gp(i2 + 1, j2, 2);
								} else
									if(j1 == 6)
									{
										ajb[i2][j2] |= 1;
										if(j2 > 0)
											gp(i2, j2 - 1, 4);
									}

			}

			no(k, l, k1, l1);
		}
	}

	public void fp(int k, int l, int i1)
	{
		if(k < 0 || l < 0 || k >= vib - 1 || l >= wib - 1)
			return;
		if(cache.glb[i1] == 1 || cache.glb[i1] == 2)
		{
			int j1 = io(k, l);
			int k1;
			int l1;
			if(j1 == 0 || j1 == 4)
			{
				k1 = cache.elb[i1];
				l1 = cache.flb[i1];
			} else
			{
				l1 = cache.elb[i1];
				k1 = cache.flb[i1];
			}
			for(int i2 = k; i2 < k + k1; i2++)
			{
				for(int j2 = l; j2 < l + l1; j2++)
					if(cache.glb[i1] == 1)
						ajb[i2][j2] &= 0xffbf;
					else
						if(j1 == 0)
						{
							ajb[i2][j2] &= 0xfffd;
							if(i2 > 0)
								zo(i2 - 1, j2, 8);
						} else
							if(j1 == 2)
							{
								ajb[i2][j2] &= 0xfffb;
								if(j2 < wib - 1)
									zo(i2, j2 + 1, 1);
							} else
								if(j1 == 4)
								{
									ajb[i2][j2] &= 0xfff7;
									if(i2 < vib - 1)
										zo(i2 + 1, j2, 2);
								} else
									if(j1 == 6)
									{
										ajb[i2][j2] &= 0xfffe;
										if(j2 > 0)
											zo(i2, j2 - 1, 4);
									}

			}

			no(k, l, k1, l1);
		}
	}

	public void no(int k, int l, int i1, int j1)
	{
		if(k < 1 || l < 1 || k + i1 >= vib || l + j1 >= wib)
			return;
		for(int k1 = k; k1 <= k + i1; k1++)
		{
			for(int l1 = l; l1 <= l + j1; l1++)
				if((lp(k1, l1) & 0x63) != 0 || (lp(k1 - 1, l1) & 0x59) != 0 || (lp(k1, l1 - 1) & 0x56) != 0 || (lp(k1 - 1, l1 - 1) & 0x6c) != 0)
					to(k1, l1, 35);
				else
					to(k1, l1, 0);

		}

	}

	public void to(int k, int l, int i1)
	{
		int j1 = k / 12;
		int k1 = l / 12;
		int l1 = (k - 1) / 12;
		int i2 = (l - 1) / 12;
		ip(j1, k1, k, l, i1);
		if(j1 != l1)
			ip(l1, k1, k, l, i1);
		if(k1 != i2)
			ip(j1, i2, k, l, i1);
		if(j1 != l1 && k1 != i2)
			ip(l1, i2, k, l, i1);
	}

	public void ip(int k, int l, int i1, int j1, int k1)
	{
		model h1 = djb[k + l * 8];
		for(int l1 = 0; l1 < h1.og; l1++)
			if(h1.ji[l1] == i1 * 128 && h1.li[l1] == j1 * 128)
			{
				h1.xd(l1, k1);
				return;
			}

	}

	public int lp(int k, int l)
	{
		if(k < 0 || l < 0 || k >= vib || l >= wib)
			return 0;
		else
			return ajb[k][l];
	}

	public int oo(int k, int l)
	{
		int i1 = k >> 7;
		int j1 = l >> 7;
		int k1 = k & 0x7f;
		int l1 = l & 0x7f;
		if(i1 < 0 || j1 < 0 || i1 >= vib - 1 || j1 >= wib - 1)
			return 0;
		int i2;
		int j2;
		int k2;
		if(k1 <= 128 - l1)
		{
			i2 = uo(i1, j1);
			j2 = uo(i1 + 1, j1) - i2;
			k2 = uo(i1, j1 + 1) - i2;
		} else
		{
			i2 = uo(i1 + 1, j1 + 1);
			j2 = uo(i1, j1 + 1) - i2;
			k2 = uo(i1 + 1, j1) - i2;
			k1 = 128 - k1;
			l1 = 128 - l1;
		}
		int l2 = i2 + (j2 * k1) / 128 + (k2 * l1) / 128;
		return l2;
	}

	public int uo(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return (nib[byte0][k * 48 + l] & 0xff) * 3;
	}

	public int go(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return oib[byte0][k * 48 + l] & 0xff;
	}

	public int wo(int k, int l, int i1)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return sib[byte0][k * 48 + l] & 0xff;
	}

	public void so(int k, int l, int i1)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		sib[byte0][k * 48 + l] = (byte)i1;
	}

	public int cp(int k, int l, int i1)
	{
		int j1 = wo(k, l, i1);
		if(j1 == 0)
			return -1;
		int k1 = cache.xlb[j1 - 1];
		return k1 != 2 ? 0 : 1;
	}

	public int mo(int k, int l, int i1, int j1)
	{
		int k1 = wo(k, l, i1);
		if(k1 == 0)
			return j1;
		else
			return cache.wlb[k1 - 1];
	}

	public int lo(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return uib[byte0][k * 48 + l];
	}

	public int bp(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return rib[byte0][k * 48 + l];
	}

	public int io(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return tib[byte0][k * 48 + l];
	}

	public boolean ep(int k, int l)
	{
		return bp(k, l) > 0 || bp(k - 1, l) > 0 || bp(k - 1, l - 1) > 0 || bp(k, l - 1) > 0;
	}

	public boolean dp(int k, int l)
	{
		return bp(k, l) > 0 && bp(k - 1, l) > 0 && bp(k - 1, l - 1) > 0 && bp(k, l - 1) > 0;
	}

	public int po(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return qib[byte0][k * 48 + l] & 0xff;
	}

	public int yo(int k, int l)
	{
		if(k < 0 || k >= 96 || l < 0 || l >= 96)
			return 0;
		byte byte0 = 0;
		if(k >= 48 && l < 48)
		{
			byte0 = 1;
			k -= 48;
		} else
			if(k < 48 && l >= 48)
			{
				byte0 = 2;
				l -= 48;
			} else
				if(k >= 48 && l >= 48)
				{
					byte0 = 3;
					k -= 48;
					l -= 48;
				}
		return pib[byte0][k * 48 + l] & 0xff;
	}

	public void eo(int k, int l, int i1, int j1)
	{
		String s = "m" + i1 + k / 10 + k % 10 + l / 10 + l % 10;
		int k1;
		try
		{
			if(lands != null)
			{
				byte abyte0[] = util.getf(s + ".hei", 0, lands);
				if(abyte0 == null && memlands != null)
					abyte0 = util.getf(s + ".hei", 0, memlands);
				if(abyte0 != null && abyte0.length > 0)
				{
					int l1 = 0;
					int i3 = 0;
					for(int l3 = 0; l3 < 2304;)
					{
						int l4 = abyte0[l1++] & 0xff;
						if(l4 < 128)
						{
							nib[j1][l3++] = (byte)l4;
							i3 = l4;
						}
						if(l4 >= 128)
						{
							for(int j6 = 0; j6 < l4 - 128; j6++)
								nib[j1][l3++] = (byte)i3;

						}
					}

					i3 = 64;
					for(int i5 = 0; i5 < 48; i5++)
					{
						for(int k6 = 0; k6 < 48; k6++)
						{
							i3 = nib[j1][k6 * 48 + i5] + i3 & 0x7f;
							nib[j1][k6 * 48 + i5] = (byte)(i3 * 2);
						}

					}

					i3 = 0;
					for(int l6 = 0; l6 < 2304;)
					{
						int i8 = abyte0[l1++] & 0xff;
						if(i8 < 128)
						{
							oib[j1][l6++] = (byte)i8;
							i3 = i8;
						}
						if(i8 >= 128)
						{
							for(int k9 = 0; k9 < i8 - 128; k9++)
								oib[j1][l6++] = (byte)i3;

						}
					}

					i3 = 35;
					for(int j8 = 0; j8 < 48; j8++)
					{
						for(int l9 = 0; l9 < 48; l9++)
						{
							i3 = oib[j1][l9 * 48 + j8] + i3 & 0x7f;
							oib[j1][l9 * 48 + j8] = (byte)(i3 * 2);
						}

					}

				} else
				{
					for(int i2 = 0; i2 < 2304; i2++)
					{
						nib[j1][i2] = 0;
						oib[j1][i2] = 0;
					}

				}
				abyte0 = util.getf(s + ".dat", 0, maps);
				if(abyte0 == null && memmaps != null)
					abyte0 = util.getf(s + ".dat", 0, memmaps);
				if(abyte0 == null || abyte0.length == 0)
					throw new IOException();
				int j2 = 0;
				for(int j3 = 0; j3 < 2304;)
				{
					int i4 = abyte0[j2++] & 0xff;
					if(i4 < 128)
					{
						pib[j1][j3++] = (byte)i4;
					} else
					{
						for(int j5 = 0; j5 < i4 - 128; j5++)
							pib[j1][j3++] = 0;

					}
				}

				for(int j4 = 0; j4 < 2304;)
				{
					int k5 = abyte0[j2++] & 0xff;
					if(k5 < 128)
					{
						qib[j1][j4++] = (byte)k5;
					} else
					{
						for(int i7 = 0; i7 < k5 - 128; i7++)
							qib[j1][j4++] = 0;

					}
				}

				for(int l5 = 0; l5 < 2304;)
				{
					int j7 = abyte0[j2++] & 0xff;
					if(j7 < 128)
					{
						uib[j1][l5++] = j7;
					} else
					{
						for(int k8 = 0; k8 < j7 - 128; k8++)
							uib[j1][l5++] = 0;

					}
				}

				for(int k7 = 0; k7 < 2304;)
				{
					int l8 = abyte0[j2++] & 0xff;
					if(l8 < 128)
						uib[j1][k7++] = l8 + 12000;
					else
						k7 += l8 - 128;
				}

				for(int i9 = 0; i9 < 2304;)
				{
					int i10 = abyte0[j2++] & 0xff;
					if(i10 < 128)
					{
						rib[j1][i9++] = (byte)i10;
					} else
					{
						for(int l10 = 0; l10 < i10 - 128; l10++)
							rib[j1][i9++] = 0;

					}
				}

				int j10 = 0;
				for(int i11 = 0; i11 < 2304;)
				{
					int k11 = abyte0[j2++] & 0xff;
					if(k11 < 128)
					{
						sib[j1][i11++] = (byte)k11;
						j10 = k11;
					} else
					{
						for(int j12 = 0; j12 < k11 - 128; j12++)
							sib[j1][i11++] = (byte)j10;

					}
				}

				for(int l11 = 0; l11 < 2304;)
				{
					int k12 = abyte0[j2++] & 0xff;
					if(k12 < 128)
					{
						tib[j1][l11++] = (byte)k12;
					} else
					{
						for(int j13 = 0; j13 < k12 - 128; j13++)
							tib[j1][l11++] = 0;

					}
				}

				abyte0 = util.getf(s + ".loc", 0, maps);
				if(abyte0 != null && abyte0.length > 0)
				{
					int k2 = 0;
					for(int l12 = 0; l12 < 2304;)
					{
						int k13 = abyte0[k2++] & 0xff;
						if(k13 < 128)
							uib[j1][l12++] = k13 + 48000;
						else
							l12 += k13 - 128;
					}

					return;
				}
			} else
			{
				byte abyte1[] = new byte[20736];
				util.readfile("../gamedata/maps/" + s + ".jm", abyte1, 20736);
				int l2 = 0;
				int k3 = 0;
				for(int k4 = 0; k4 < 2304; k4++)
				{
					l2 = l2 + abyte1[k3++] & 0xff;
					nib[j1][k4] = (byte)l2;
				}

				l2 = 0;
				for(int i6 = 0; i6 < 2304; i6++)
				{
					l2 = l2 + abyte1[k3++] & 0xff;
					oib[j1][i6] = (byte)l2;
				}

				for(int l7 = 0; l7 < 2304; l7++)
					pib[j1][l7] = abyte1[k3++];

				for(int j9 = 0; j9 < 2304; j9++)
					qib[j1][j9] = abyte1[k3++];

				for(int k10 = 0; k10 < 2304; k10++)
				{
					uib[j1][k10] = (abyte1[k3] & 0xff) * 256 + (abyte1[k3 + 1] & 0xff);
					k3 += 2;
				}

				for(int j11 = 0; j11 < 2304; j11++)
					rib[j1][j11] = abyte1[k3++];

				for(int i12 = 0; i12 < 2304; i12++)
					sib[j1][i12] = abyte1[k3++];

				for(int i13 = 0; i13 < 2304; i13++)
					tib[j1][i13] = abyte1[k3++];

			}
			return;
		}
		catch(IOException _ex)
		{
			k1 = 0;
		}
		for(; k1 < 2304; k1++)
		{
			nib[j1][k1] = 0;
			oib[j1][k1] = 0;
			pib[j1][k1] = 0;
			qib[j1][k1] = 0;
			uib[j1][k1] = 0;
			rib[j1][k1] = 0;
			sib[j1][k1] = 0;
			if(i1 == 0)
				sib[j1][k1] = -6;
			if(i1 == 3)
				sib[j1][k1] = 8;
			tib[j1][k1] = 0;
		}

	}

	public void kp()
	{
		if(yhb)
			aib.si();
		for(int k = 0; k < 64; k++)
		{
			djb[k] = null;
			for(int l = 0; l < 4; l++)
				ejb[l][k] = null;

			for(int i1 = 0; i1 < 4; i1++)
				fjb[i1][k] = null;

		}

		System.gc();
	}

	public void xo(int k, int l, int i1)
	{
		kp();
		int j1 = (k + 24) / 48;
		int k1 = (l + 24) / 48;
		ko(k, l, i1, true);
		if(i1 == 0)
		{
			ko(k, l, 1, false);
			ko(k, l, 2, false);
			eo(j1 - 1, k1 - 1, i1, 0);
			eo(j1, k1 - 1, i1, 1);
			eo(j1 - 1, k1, i1, 2);
			eo(j1, k1, i1, 3);
			ro();
		}
	}

	public void ro()
	{
		for(int k = 0; k < 96; k++)
		{
			for(int l = 0; l < 96; l++)
				if(wo(k, l, 0) == 250)
					if(k == 47 && wo(k + 1, l, 0) != 250 && wo(k + 1, l, 0) != 2)
						so(k, l, 9);
					else
						if(l == 47 && wo(k, l + 1, 0) != 250 && wo(k, l + 1, 0) != 2)
							so(k, l, 9);
						else
							so(k, l, 2);

		}

	}

	public void jo(int k, int l, int i1, int j1, int k1)
	{
		int l1 = k * 3;
		int i2 = l * 3;
		int j2 = aib.ti(j1);
		int k2 = aib.ti(k1);
		j2 = j2 >> 1 & 0x7f7f7f;
			k2 = k2 >> 1 & 0x7f7f7f;
			if(i1 == 0)
			{
				zhb.drawhorline(l1, i2, 3, j2);
				zhb.drawhorline(l1, i2 + 1, 2, j2);
				zhb.drawhorline(l1, i2 + 2, 1, j2);
				zhb.drawhorline(l1 + 2, i2 + 1, 1, k2);
				zhb.drawhorline(l1 + 1, i2 + 2, 2, k2);
				return;
			}
			if(i1 == 1)
			{
				zhb.drawhorline(l1, i2, 3, k2);
				zhb.drawhorline(l1 + 1, i2 + 1, 2, k2);
				zhb.drawhorline(l1 + 2, i2 + 2, 1, k2);
				zhb.drawhorline(l1, i2 + 1, 1, j2);
				zhb.drawhorline(l1, i2 + 2, 2, j2);
			}
	}

	public void ko(int k, int l, int i1, boolean flag)
	{
		int j1 = (k + 24) / 48;
		int k1 = (l + 24) / 48;
		eo(j1 - 1, k1 - 1, i1, 0);
		eo(j1, k1 - 1, i1, 1);
		eo(j1 - 1, k1, i1, 2);
		eo(j1, k1, i1, 3);
		ro();
		if(gjb == null)
			gjb = new model(vib * wib * 2 + 256, vib * wib * 2 + 256, true, true, false, false, true);
		if(flag)
		{
			zhb.clear();
			for(int l1 = 0; l1 < 96; l1++)
			{
				for(int j2 = 0; j2 < 96; j2++)
					ajb[l1][j2] = 0;

			}

			model h1 = gjb;
			h1.xe();
			for(int l2 = 0; l2 < 96; l2++)
			{
				for(int k3 = 0; k3 < 96; k3++)
				{
					int k4 = -uo(l2, k3);
					if(wo(l2, k3, i1) > 0 && cache.xlb[wo(l2, k3, i1) - 1] == 4)
						k4 = 0;
					if(wo(l2 - 1, k3, i1) > 0 && cache.xlb[wo(l2 - 1, k3, i1) - 1] == 4)
						k4 = 0;
					if(wo(l2, k3 - 1, i1) > 0 && cache.xlb[wo(l2, k3 - 1, i1) - 1] == 4)
						k4 = 0;
					if(wo(l2 - 1, k3 - 1, i1) > 0 && cache.xlb[wo(l2 - 1, k3 - 1, i1) - 1] == 4)
						k4 = 0;
					int l5 = h1.oe(l2 * 128, k4, k3 * 128);
					int l7 = (int)(Math.random() * 10D) - 5;
					h1.xd(l5, l7);
				}

			}

			for(int l3 = 0; l3 < 95; l3++)
			{
				for(int l4 = 0; l4 < 95; l4++)
				{
					int i6 = go(l3, l4);
					int i8 = eib[i6];
					int k10 = i8;
					int i13 = i8;
					int j15 = 0;
					if(i1 == 1 || i1 == 2)
					{
						i8 = 0xbc614e;
						k10 = 0xbc614e;
						i13 = 0xbc614e;
					}
					if(wo(l3, l4, i1) > 0)
					{
						int j17 = wo(l3, l4, i1);
						int j6 = cache.xlb[j17 - 1];
						int k19 = cp(l3, l4, i1);
						i8 = k10 = cache.wlb[j17 - 1];
						if(j6 == 4)
						{
							i8 = 1;
							k10 = 1;
							if(j17 == 12)
							{
								i8 = 31;
								k10 = 31;
							}
						}
						if(j6 == 5)
						{
							if(lo(l3, l4) > 0 && lo(l3, l4) < 24000)
								if(mo(l3 - 1, l4, i1, i13) != 0xbc614e && mo(l3, l4 - 1, i1, i13) != 0xbc614e)
								{
									i8 = mo(l3 - 1, l4, i1, i13);
									j15 = 0;
								} else
									if(mo(l3 + 1, l4, i1, i13) != 0xbc614e && mo(l3, l4 + 1, i1, i13) != 0xbc614e)
									{
										k10 = mo(l3 + 1, l4, i1, i13);
										j15 = 0;
									} else
										if(mo(l3 + 1, l4, i1, i13) != 0xbc614e && mo(l3, l4 - 1, i1, i13) != 0xbc614e)
										{
											k10 = mo(l3 + 1, l4, i1, i13);
											j15 = 1;
										} else
											if(mo(l3 - 1, l4, i1, i13) != 0xbc614e && mo(l3, l4 + 1, i1, i13) != 0xbc614e)
											{
												i8 = mo(l3 - 1, l4, i1, i13);
												j15 = 1;
											}
						} else
							if(j6 != 2 || lo(l3, l4) > 0 && lo(l3, l4) < 24000)
								if(cp(l3 - 1, l4, i1) != k19 && cp(l3, l4 - 1, i1) != k19)
								{
									i8 = i13;
									j15 = 0;
								} else
									if(cp(l3 + 1, l4, i1) != k19 && cp(l3, l4 + 1, i1) != k19)
									{
										k10 = i13;
										j15 = 0;
									} else
										if(cp(l3 + 1, l4, i1) != k19 && cp(l3, l4 - 1, i1) != k19)
										{
											k10 = i13;
											j15 = 1;
										} else
											if(cp(l3 - 1, l4, i1) != k19 && cp(l3, l4 + 1, i1) != k19)
											{
												i8 = i13;
												j15 = 1;
											}
						if(cache.ylb[j17 - 1] != 0)
							ajb[l3][l4] |= 0x40;
						if(cache.xlb[j17 - 1] == 2)
							ajb[l3][l4] |= 0x80;
					}
					jo(l3, l4, j15, i8, k10);
					int k17 = ((uo(l3 + 1, l4 + 1) - uo(l3 + 1, l4)) + uo(l3, l4 + 1)) - uo(l3, l4);
					if(i8 != k10 || k17 != 0)
					{
						int ai[] = new int[3];
						int ai7[] = new int[3];
						if(j15 == 0)
						{
							if(i8 != 0xbc614e)
							{
								ai[0] = l4 + l3 * vib + vib;
								ai[1] = l4 + l3 * vib;
								ai[2] = l4 + l3 * vib + 1;
								int j22 = h1.ne(3, ai, 0xbc614e, i8);
								xib[j22] = l3;
								yib[j22] = l4;
								h1.vh[j22] = 0x30d40 + j22;
							}
							if(k10 != 0xbc614e)
							{
								ai7[0] = l4 + l3 * vib + 1;
								ai7[1] = l4 + l3 * vib + vib + 1;
								ai7[2] = l4 + l3 * vib + vib;
								int k22 = h1.ne(3, ai7, 0xbc614e, k10);
								xib[k22] = l3;
								yib[k22] = l4;
								h1.vh[k22] = 0x30d40 + k22;
							}
						} else
						{
							if(i8 != 0xbc614e)
							{
								ai[0] = l4 + l3 * vib + 1;
								ai[1] = l4 + l3 * vib + vib + 1;
								ai[2] = l4 + l3 * vib;
								int l22 = h1.ne(3, ai, 0xbc614e, i8);
								xib[l22] = l3;
								yib[l22] = l4;
								h1.vh[l22] = 0x30d40 + l22;
							}
							if(k10 != 0xbc614e)
							{
								ai7[0] = l4 + l3 * vib + vib;
								ai7[1] = l4 + l3 * vib;
								ai7[2] = l4 + l3 * vib + vib + 1;
								int i23 = h1.ne(3, ai7, 0xbc614e, k10);
								xib[i23] = l3;
								yib[i23] = l4;
								h1.vh[i23] = 0x30d40 + i23;
							}
						}
					} else
						if(i8 != 0xbc614e)
						{
							int ai1[] = new int[4];
							ai1[0] = l4 + l3 * vib + vib;
							ai1[1] = l4 + l3 * vib;
							ai1[2] = l4 + l3 * vib + 1;
							ai1[3] = l4 + l3 * vib + vib + 1;
							int j20 = h1.ne(4, ai1, 0xbc614e, i8);
							xib[j20] = l3;
							yib[j20] = l4;
							h1.vh[j20] = 0x30d40 + j20;
						}
				}

			}

			for(int i5 = 1; i5 < 95; i5++)
			{
				for(int k6 = 1; k6 < 95; k6++)
					if(wo(i5, k6, i1) > 0 && cache.xlb[wo(i5, k6, i1) - 1] == 4)
					{
						int j8 = cache.wlb[wo(i5, k6, i1) - 1];
						int l10 = h1.oe(i5 * 128, -uo(i5, k6), k6 * 128);
						int j13 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6), k6 * 128);
						int k15 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6 + 1), (k6 + 1) * 128);
						int l17 = h1.oe(i5 * 128, -uo(i5, k6 + 1), (k6 + 1) * 128);
						int ai2[] = {
								l10, j13, k15, l17
						};
						int k20 = h1.ne(4, ai2, j8, 0xbc614e);
						xib[k20] = i5;
						yib[k20] = k6;
						h1.vh[k20] = 0x30d40 + k20;
						jo(i5, k6, 0, j8, j8);
					} else
						if(wo(i5, k6, i1) == 0 || cache.xlb[wo(i5, k6, i1) - 1] != 3)
						{
							if(wo(i5, k6 + 1, i1) > 0 && cache.xlb[wo(i5, k6 + 1, i1) - 1] == 4)
							{
								int k8 = cache.wlb[wo(i5, k6 + 1, i1) - 1];
								int i11 = h1.oe(i5 * 128, -uo(i5, k6), k6 * 128);
								int k13 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6), k6 * 128);
								int l15 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6 + 1), (k6 + 1) * 128);
								int i18 = h1.oe(i5 * 128, -uo(i5, k6 + 1), (k6 + 1) * 128);
								int ai3[] = {
										i11, k13, l15, i18
								};
								int l20 = h1.ne(4, ai3, k8, 0xbc614e);
								xib[l20] = i5;
								yib[l20] = k6;
								h1.vh[l20] = 0x30d40 + l20;
								jo(i5, k6, 0, k8, k8);
							}
							if(wo(i5, k6 - 1, i1) > 0 && cache.xlb[wo(i5, k6 - 1, i1) - 1] == 4)
							{
								int l8 = cache.wlb[wo(i5, k6 - 1, i1) - 1];
								int j11 = h1.oe(i5 * 128, -uo(i5, k6), k6 * 128);
								int l13 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6), k6 * 128);
								int i16 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6 + 1), (k6 + 1) * 128);
								int j18 = h1.oe(i5 * 128, -uo(i5, k6 + 1), (k6 + 1) * 128);
								int ai4[] = {
										j11, l13, i16, j18
								};
								int i21 = h1.ne(4, ai4, l8, 0xbc614e);
								xib[i21] = i5;
								yib[i21] = k6;
								h1.vh[i21] = 0x30d40 + i21;
								jo(i5, k6, 0, l8, l8);
							}
							if(wo(i5 + 1, k6, i1) > 0 && cache.xlb[wo(i5 + 1, k6, i1) - 1] == 4)
							{
								int i9 = cache.wlb[wo(i5 + 1, k6, i1) - 1];
								int k11 = h1.oe(i5 * 128, -uo(i5, k6), k6 * 128);
								int i14 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6), k6 * 128);
								int j16 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6 + 1), (k6 + 1) * 128);
								int k18 = h1.oe(i5 * 128, -uo(i5, k6 + 1), (k6 + 1) * 128);
								int ai5[] = {
										k11, i14, j16, k18
								};
								int j21 = h1.ne(4, ai5, i9, 0xbc614e);
								xib[j21] = i5;
								yib[j21] = k6;
								h1.vh[j21] = 0x30d40 + j21;
								jo(i5, k6, 0, i9, i9);
							}
							if(wo(i5 - 1, k6, i1) > 0 && cache.xlb[wo(i5 - 1, k6, i1) - 1] == 4)
							{
								int j9 = cache.wlb[wo(i5 - 1, k6, i1) - 1];
								int l11 = h1.oe(i5 * 128, -uo(i5, k6), k6 * 128);
								int j14 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6), k6 * 128);
								int k16 = h1.oe((i5 + 1) * 128, -uo(i5 + 1, k6 + 1), (k6 + 1) * 128);
								int l18 = h1.oe(i5 * 128, -uo(i5, k6 + 1), (k6 + 1) * 128);
								int ai6[] = {
										l11, j14, k16, l18
								};
								int k21 = h1.ne(4, ai6, j9, 0xbc614e);
								xib[k21] = i5;
								yib[k21] = k6;
								h1.vh[k21] = 0x30d40 + k21;
								jo(i5, k6, 0, j9, j9);
							}
						}

			}

			h1.se(true, 40, 48, -50, -10, -50);
			djb = gjb.ud(0, 0, 1536, 1536, 8, 64, 233, false);
			for(int l6 = 0; l6 < 64; l6++)
				aib.uh(djb[l6]);

			for(int k9 = 0; k9 < 96; k9++)
			{
				for(int i12 = 0; i12 < 96; i12++)
					bjb[k9][i12] = uo(k9, i12);

			}

		}
		gjb.xe();
		int i2 = 0x606060;
		for(int k2 = 0; k2 < 95; k2++)
		{
			for(int i3 = 0; i3 < 95; i3++)
			{
				int i4 = po(k2, i3);
				if(i4 > 0 && (cache.rlb[i4 - 1] == 0 || xhb))
				{
					jp(gjb, i4 - 1, k2, i3, k2 + 1, i3);
					if(flag && cache.qlb[i4 - 1] != 0)
					{
						ajb[k2][i3] |= 1;
						if(i3 > 0)
							gp(k2, i3 - 1, 4);
					}
					if(flag)
						zhb.drawhorline(k2 * 3, i3 * 3, 3, i2);
				}
				i4 = yo(k2, i3);
				if(i4 > 0 && (cache.rlb[i4 - 1] == 0 || xhb))
				{
					jp(gjb, i4 - 1, k2, i3, k2, i3 + 1);
					if(flag && cache.qlb[i4 - 1] != 0)
					{
						ajb[k2][i3] |= 2;
						if(k2 > 0)
							gp(k2 - 1, i3, 8);
					}
					if(flag)
						zhb.drawvertline(k2 * 3, i3 * 3, 3, i2);
				}
				i4 = lo(k2, i3);
				if(i4 > 0 && i4 < 12000 && (cache.rlb[i4 - 1] == 0 || xhb))
				{
					jp(gjb, i4 - 1, k2, i3, k2 + 1, i3 + 1);
					if(flag && cache.qlb[i4 - 1] != 0)
						ajb[k2][i3] |= 0x20;
					if(flag)
					{
						zhb.setpixel(k2 * 3, i3 * 3, i2);
						zhb.setpixel(k2 * 3 + 1, i3 * 3 + 1, i2);
						zhb.setpixel(k2 * 3 + 2, i3 * 3 + 2, i2);
					}
				}
				if(i4 > 12000 && i4 < 24000 && (cache.rlb[i4 - 12001] == 0 || xhb))
				{
					jp(gjb, i4 - 12001, k2 + 1, i3, k2, i3 + 1);
					if(flag && cache.qlb[i4 - 12001] != 0)
						ajb[k2][i3] |= 0x10;
					if(flag)
					{
						zhb.setpixel(k2 * 3 + 2, i3 * 3, i2);
						zhb.setpixel(k2 * 3 + 1, i3 * 3 + 1, i2);
						zhb.setpixel(k2 * 3, i3 * 3 + 2, i2);
					}
				}
			}

		}

		if(flag)
			zhb.kf(bib - 1, 0, 0, 285, 285);
		gjb.se(false, 60, 24, -50, -10, -50);
		ejb[i1] = gjb.ud(0, 0, 1536, 1536, 8, 64, 338, true);
		for(int j3 = 0; j3 < 64; j3++)
			aib.uh(ejb[i1][j3]);

		for(int j4 = 0; j4 < 95; j4++)
		{
			for(int j5 = 0; j5 < 95; j5++)
			{
				int i7 = po(j4, j5);
				if(i7 > 0)
					ap(i7 - 1, j4, j5, j4 + 1, j5);
				i7 = yo(j4, j5);
				if(i7 > 0)
					ap(i7 - 1, j4, j5, j4, j5 + 1);
				i7 = lo(j4, j5);
				if(i7 > 0 && i7 < 12000)
					ap(i7 - 1, j4, j5, j4 + 1, j5 + 1);
				if(i7 > 12000 && i7 < 24000)
					ap(i7 - 12001, j4 + 1, j5, j4, j5 + 1);
			}

		}

		for(int k5 = 1; k5 < 95; k5++)
		{
			for(int j7 = 1; j7 < 95; j7++)
			{
				int l9 = bp(k5, j7);
				if(l9 > 0)
				{
					int j12 = k5;
					int k14 = j7;
					int l16 = k5 + 1;
					int i19 = j7;
					int l19 = k5 + 1;
					int l21 = j7 + 1;
					int j23 = k5;
					int l23 = j7 + 1;
					int j24 = 0;
					int l24 = bjb[j12][k14];
					int j25 = bjb[l16][i19];
					int l25 = bjb[l19][l21];
					int j26 = bjb[j23][l23];
					if(l24 > 0x13880)
						l24 -= 0x13880;
					if(j25 > 0x13880)
						j25 -= 0x13880;
					if(l25 > 0x13880)
						l25 -= 0x13880;
					if(j26 > 0x13880)
						j26 -= 0x13880;
					if(l24 > j24)
						j24 = l24;
					if(j25 > j24)
						j24 = j25;
					if(l25 > j24)
						j24 = l25;
					if(j26 > j24)
						j24 = j26;
					if(j24 >= 0x13880)
						j24 -= 0x13880;
					if(l24 < 0x13880)
						bjb[j12][k14] = j24;
					else
						bjb[j12][k14] -= 0x13880;
					if(j25 < 0x13880)
						bjb[l16][i19] = j24;
					else
						bjb[l16][i19] -= 0x13880;
					if(l25 < 0x13880)
						bjb[l19][l21] = j24;
					else
						bjb[l19][l21] -= 0x13880;
					if(j26 < 0x13880)
						bjb[j23][l23] = j24;
					else
						bjb[j23][l23] -= 0x13880;
				}
			}

		}

		gjb.xe();
		for(int k7 = 1; k7 < 95; k7++)
		{
			for(int i10 = 1; i10 < 95; i10++)
			{
				int k12 = bp(k7, i10);
				if(k12 > 0)
				{
					int l14 = k7;
					int i17 = i10;
					int j19 = k7 + 1;
					int i20 = i10;
					int i22 = k7 + 1;
					int k23 = i10 + 1;
					int i24 = k7;
					int k24 = i10 + 1;
					int i25 = k7 * 128;
					int k25 = i10 * 128;
					int i26 = i25 + 128;
					int k26 = k25 + 128;
					int l26 = i25;
					int i27 = k25;
					int j27 = i26;
					int k27 = k26;
					int l27 = bjb[l14][i17];
					int i28 = bjb[j19][i20];
					int j28 = bjb[i22][k23];
					int k28 = bjb[i24][k24];
					int l28 = cache.tlb[k12 - 1];
					if(dp(l14, i17) && l27 < 0x13880)
					{
						l27 += l28 + 0x13880;
						bjb[l14][i17] = l27;
					}
					if(dp(j19, i20) && i28 < 0x13880)
					{
						i28 += l28 + 0x13880;
						bjb[j19][i20] = i28;
					}
					if(dp(i22, k23) && j28 < 0x13880)
					{
						j28 += l28 + 0x13880;
						bjb[i22][k23] = j28;
					}
					if(dp(i24, k24) && k28 < 0x13880)
					{
						k28 += l28 + 0x13880;
						bjb[i24][k24] = k28;
					}
					if(l27 >= 0x13880)
						l27 -= 0x13880;
					if(i28 >= 0x13880)
						i28 -= 0x13880;
					if(j28 >= 0x13880)
						j28 -= 0x13880;
					if(k28 >= 0x13880)
						k28 -= 0x13880;
					byte byte0 = 16;
					if(!ep(l14 - 1, i17))
						i25 -= byte0;
					if(!ep(l14 + 1, i17))
						i25 += byte0;
					if(!ep(l14, i17 - 1))
						k25 -= byte0;
					if(!ep(l14, i17 + 1))
						k25 += byte0;
					if(!ep(j19 - 1, i20))
						i26 -= byte0;
					if(!ep(j19 + 1, i20))
						i26 += byte0;
					if(!ep(j19, i20 - 1))
						i27 -= byte0;
					if(!ep(j19, i20 + 1))
						i27 += byte0;
					if(!ep(i22 - 1, k23))
						j27 -= byte0;
					if(!ep(i22 + 1, k23))
						j27 += byte0;
					if(!ep(i22, k23 - 1))
						k26 -= byte0;
					if(!ep(i22, k23 + 1))
						k26 += byte0;
					if(!ep(i24 - 1, k24))
						l26 -= byte0;
					if(!ep(i24 + 1, k24))
						l26 += byte0;
					if(!ep(i24, k24 - 1))
						k27 -= byte0;
					if(!ep(i24, k24 + 1))
						k27 += byte0;
					k12 = cache.ulb[k12 - 1];
					l27 = -l27;
					i28 = -i28;
					j28 = -j28;
					k28 = -k28;
					if(lo(k7, i10) > 12000 && lo(k7, i10) < 24000 && bp(k7 - 1, i10 - 1) == 0)
					{
						int ai8[] = new int[3];
						ai8[0] = gjb.oe(j27, j28, k26);
						ai8[1] = gjb.oe(l26, k28, k27);
						ai8[2] = gjb.oe(i26, i28, i27);
						gjb.ne(3, ai8, k12, 0xbc614e);
					} else
						if(lo(k7, i10) > 12000 && lo(k7, i10) < 24000 && bp(k7 + 1, i10 + 1) == 0)
						{
							int ai9[] = new int[3];
							ai9[0] = gjb.oe(i25, l27, k25);
							ai9[1] = gjb.oe(i26, i28, i27);
							ai9[2] = gjb.oe(l26, k28, k27);
							gjb.ne(3, ai9, k12, 0xbc614e);
						} else
							if(lo(k7, i10) > 0 && lo(k7, i10) < 12000 && bp(k7 + 1, i10 - 1) == 0)
							{
								int ai10[] = new int[3];
								ai10[0] = gjb.oe(l26, k28, k27);
								ai10[1] = gjb.oe(i25, l27, k25);
								ai10[2] = gjb.oe(j27, j28, k26);
								gjb.ne(3, ai10, k12, 0xbc614e);
							} else
								if(lo(k7, i10) > 0 && lo(k7, i10) < 12000 && bp(k7 - 1, i10 + 1) == 0)
								{
									int ai11[] = new int[3];
									ai11[0] = gjb.oe(i26, i28, i27);
									ai11[1] = gjb.oe(j27, j28, k26);
									ai11[2] = gjb.oe(i25, l27, k25);
									gjb.ne(3, ai11, k12, 0xbc614e);
								} else
									if(l27 == i28 && j28 == k28)
									{
										int ai12[] = new int[4];
										ai12[0] = gjb.oe(i25, l27, k25);
										ai12[1] = gjb.oe(i26, i28, i27);
										ai12[2] = gjb.oe(j27, j28, k26);
										ai12[3] = gjb.oe(l26, k28, k27);
										gjb.ne(4, ai12, k12, 0xbc614e);
									} else
										if(l27 == k28 && i28 == j28)
										{
											int ai13[] = new int[4];
											ai13[0] = gjb.oe(l26, k28, k27);
											ai13[1] = gjb.oe(i25, l27, k25);
											ai13[2] = gjb.oe(i26, i28, i27);
											ai13[3] = gjb.oe(j27, j28, k26);
											gjb.ne(4, ai13, k12, 0xbc614e);
										} else
										{
											boolean flag1 = true;
											if(bp(k7 - 1, i10 - 1) > 0)
												flag1 = false;
											if(bp(k7 + 1, i10 + 1) > 0)
												flag1 = false;
											if(!flag1)
											{
												int ai14[] = new int[3];
												ai14[0] = gjb.oe(i26, i28, i27);
												ai14[1] = gjb.oe(j27, j28, k26);
												ai14[2] = gjb.oe(i25, l27, k25);
												gjb.ne(3, ai14, k12, 0xbc614e);
												int ai16[] = new int[3];
												ai16[0] = gjb.oe(l26, k28, k27);
												ai16[1] = gjb.oe(i25, l27, k25);
												ai16[2] = gjb.oe(j27, j28, k26);
												gjb.ne(3, ai16, k12, 0xbc614e);
											} else
											{
												int ai15[] = new int[3];
												ai15[0] = gjb.oe(i25, l27, k25);
												ai15[1] = gjb.oe(i26, i28, i27);
												ai15[2] = gjb.oe(l26, k28, k27);
												gjb.ne(3, ai15, k12, 0xbc614e);
												int ai17[] = new int[3];
												ai17[0] = gjb.oe(j27, j28, k26);
												ai17[1] = gjb.oe(l26, k28, k27);
												ai17[2] = gjb.oe(i26, i28, i27);
												gjb.ne(3, ai17, k12, 0xbc614e);
											}
										}
				}
			}

		}

		gjb.se(true, 50, 50, -50, -10, -50);
		fjb[i1] = gjb.ud(0, 0, 1536, 1536, 8, 64, 169, true);
		for(int j10 = 0; j10 < 64; j10++)
			aib.uh(fjb[i1][j10]);

		for(int l12 = 0; l12 < 96; l12++)
		{
			for(int i15 = 0; i15 < 96; i15++)
				if(bjb[l12][i15] >= 0x13880)
					bjb[l12][i15] -= 0x13880;

		}

	}

	public void qo(model ah[])
	{
		for(int k = 0; k < vib - 2; k++)
		{
			for(int l = 0; l < wib - 2; l++)
				if(lo(k, l) > 48000 && lo(k, l) < 60000)
				{
					int i1 = lo(k, l) - 48001;
					int j1 = io(k, l);
					int k1;
					int l1;
					if(j1 == 0 || j1 == 4)
					{
						k1 = cache.elb[i1];
						l1 = cache.flb[i1];
					} else
					{
						l1 = cache.elb[i1];
						k1 = cache.flb[i1];
					}
					vo(k, l, i1);
					model h1 = ah[cache.dlb[i1]].ue(false, true, false, false);
					int i2 = ((k + k + k1) * 128) / 2;
					int k2 = ((l + l + l1) * 128) / 2;
					h1.zd(i2, -oo(i2, k2), k2);
					h1.ke(0, io(k, l) * 32, 0);
					aib.uh(h1);
					h1.be(48, 48, -50, -10, -50);
					if(k1 > 1 || l1 > 1)
					{
						for(int i3 = k; i3 < k + k1; i3++)
						{
							for(int j3 = l; j3 < l + l1; j3++)
								if((i3 > k || j3 > l) && lo(i3, j3) - 48001 == i1)
								{
									int j2 = i3;
									int l2 = j3;
									byte byte0 = 0;
									if(j2 >= 48 && l2 < 48)
									{
										byte0 = 1;
										j2 -= 48;
									} else
										if(j2 < 48 && l2 >= 48)
										{
											byte0 = 2;
											l2 -= 48;
										} else
											if(j2 >= 48 && l2 >= 48)
											{
												byte0 = 3;
												j2 -= 48;
												l2 -= 48;
											}
									uib[byte0][j2 * 48 + l2] = 0;
								}

						}

					}
				}

		}

	}

	public void jp(model h1, int k, int l, int i1, int j1, int k1)
	{
		to(l, i1, 40);
		to(j1, k1, 40);
		int l1 = cache.nlb[k];
		int i2 = cache.olb[k];
		int j2 = cache.plb[k];
		int k2 = l * 128;
		int l2 = i1 * 128;
		int i3 = j1 * 128;
		int j3 = k1 * 128;
		int k3 = h1.oe(k2, -bjb[l][i1], l2);
		int l3 = h1.oe(k2, -bjb[l][i1] - l1, l2);
		int i4 = h1.oe(i3, -bjb[j1][k1] - l1, j3);
		int j4 = h1.oe(i3, -bjb[j1][k1], j3);
		int ai[] = {
				k3, l3, i4, j4
		};
		int k4 = h1.ne(4, ai, i2, j2);
		if(cache.rlb[k] == 5)
		{
			h1.vh[k4] = 30000 + k;
			return;
		} else
		{
			h1.vh[k4] = 0;
			return;
		}
	}

	public void ap(int k, int l, int i1, int j1, int k1)
	{
		int l1 = cache.nlb[k];
		if(bjb[l][i1] < 0x13880)
			bjb[l][i1] += 0x13880 + l1;
		if(bjb[j1][k1] < 0x13880)
			bjb[j1][k1] += 0x13880 + l1;
	}

	boolean xhb;
	boolean yhb;
	graphics zhb;
	camera aib;
	int bib;
	final int cib = 0xbc614e;
	final int dib = 128;
	int eib[];
	int fib;
	int gib[];
	int hib[];
	int iib[];
	byte lands[];
	byte maps[];
	byte memlands[];
	byte memmaps[];
	byte nib[][];
	byte oib[][];
	byte pib[][];
	byte qib[][];
	byte rib[][];
	byte sib[][];
	byte tib[][];
	int uib[][];
	int vib;
	int wib;
	int xib[];
	int yib[];
	int zib[][];
	int ajb[][];
	int bjb[][];
	boolean cjb;
	model djb[];
	model ejb[][];
	model fjb[][];
	model gjb;
}
