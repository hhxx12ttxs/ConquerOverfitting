// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) nonlb 

package com.jagex.client;

import com.jagex.Util;

import java.io.DataInputStream;
import java.io.IOException;

// Referenced classes of package com.jagex.client:
//			j

public class Model {

	public Model(int arg0, int arg1) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		wd(arg0, arg1);
		qi = new int[arg1][1];
		for (int i = 0; i < arg1; i++)
			qi[i][0] = i;

	}

	public Model(int arg0, int arg1, boolean arg2, boolean arg3, boolean arg4,
			boolean arg5, boolean arg6) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		xh = arg2;
		yh = arg3;
		zh = arg4;
		ai = arg5;
		bi = arg6;
		wd(arg0, arg1);
	}

	// previously private -- stormy
	public Model(int arg0, int arg1, boolean arg2) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		wd(arg0, arg1);
		qi = new int[arg1][];
	}

	private void wd(int arg0, int arg1) {
		ji = new int[arg0];
		ki = new int[arg0];
		li = new int[arg0];
		ug = new int[arg0];
		vg = new byte[arg0];
		xg = new int[arg1];
		yg = new int[arg1][];
		zg = new int[arg1];
		ah = new int[arg1];
		dh = new int[arg1];
		ch = new int[arg1];
		bh = new int[arg1];
		if (!bi) {
			pg = new int[arg0];
			qg = new int[arg0];
			rg = new int[arg0];
			sg = new int[arg0];
			tg = new int[arg0];
		}
		if (!ai) {
			wh = new byte[arg1];
			vh = new int[arg1];
		}
		if (xh) {
			mi = ji;
			ni = ki;
			oi = li;
		} else {
			mi = new int[arg0];
			ni = new int[arg0];
			oi = new int[arg0];
		}
		if (!zh || !yh) {
			eh = new int[arg1];
			fh = new int[arg1];
			gh = new int[arg1];
		}
		if (!yh) {
			ri = new int[arg1];
			si = new int[arg1];
			ti = new int[arg1];
			ui = new int[arg1];
			vi = new int[arg1];
			wi = new int[arg1];
		}
		wg = 0;
		og = 0;
		ii = arg0;
		pi = arg1;
		xi = yi = zi = 0;
		aj = bj = cj = 0;
		dj = ej = fj = 256;
		gj = hj = ij = jj = kj = lj = 256;
		mj = 0;
	}

	public void te() {
		pg = new int[og];
		qg = new int[og];
		rg = new int[og];
		sg = new int[og];
		tg = new int[og];
	}

	public void xe() {
		wg = 0;
		og = 0;
	}

	public void le(int arg0, int arg1) {
		wg -= arg0;
		if (wg < 0)
			wg = 0;
		og -= arg1;
		if (og < 0)
			og = 0;
	}

	public Model(byte arg0[], int arg1, boolean arg2) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		int i = Util.readInt8(arg0, arg1);
		arg1 += 2;
		int k = Util.readInt8(arg0, arg1);
		arg1 += 2;
		wd(i, k);
		qi = new int[k][1];
		for (int l = 0; l < i; l++) {
			ji[l] = Util.zn(arg0, arg1);
			arg1 += 2;
		}

		for (int i1 = 0; i1 < i; i1++) {
			ki[i1] = Util.zn(arg0, arg1);
			arg1 += 2;
		}

		for (int j1 = 0; j1 < i; j1++) {
			li[j1] = Util.zn(arg0, arg1);
			arg1 += 2;
		}

		og = i;
		for (int k1 = 0; k1 < k; k1++)
			xg[k1] = arg0[arg1++] & 0xff;

		for (int l1 = 0; l1 < k; l1++) {
			zg[l1] = Util.zn(arg0, arg1);
			arg1 += 2;
			if (zg[l1] == 32767)
				zg[l1] = hi;
		}

		for (int i2 = 0; i2 < k; i2++) {
			ah[i2] = Util.zn(arg0, arg1);
			arg1 += 2;
			if (ah[i2] == 32767)
				ah[i2] = hi;
		}

		for (int j2 = 0; j2 < k; j2++) {
			int k2 = arg0[arg1++] & 0xff;
			if (k2 == 0)
				dh[j2] = 0;
			else
				dh[j2] = hi;
		}

		for (int l2 = 0; l2 < k; l2++) {
			yg[l2] = new int[xg[l2]];
			for (int i3 = 0; i3 < xg[l2]; i3++)
				if (i < 256) {
					yg[l2][i3] = arg0[arg1++] & 0xff;
				} else {
					yg[l2][i3] = Util.readInt8(arg0, arg1);
					arg1 += 2;
				}

		}

		wg = k;
		jh = 1;
	}

	public Model(byte arg0[], int arg1) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		uj = arg0;
		vj = arg1;
		re(uj);
		int i = re(uj);
		int k = re(uj);
		wd(i, k);
		qi = new int[k][];
		for (int l2 = 0; l2 < i; l2++) {
			int l = re(uj);
			int i1 = re(uj);
			int j1 = re(uj);
			oe(l, i1, j1);
		}

		for (int i3 = 0; i3 < k; i3++) {
			int k1 = re(uj);
			int l1 = re(uj);
			int i2 = re(uj);
			int j2 = re(uj);
			sj = re(uj);
			tj = re(uj);
			int k2 = re(uj);
			int ai1[] = new int[k1];
			for (int j3 = 0; j3 < k1; j3++)
				ai1[j3] = re(uj);

			int ai2[] = new int[j2];
			for (int k3 = 0; k3 < j2; k3++)
				ai2[k3] = re(uj);

			int l3 = ne(k1, ai1, l1, i2);
			qi[i3] = ai2;
			if (k2 == 0)
				dh[l3] = 0;
			else
				dh[l3] = hi;
		}

		jh = 1;
	}

	public Model(String arg0) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		byte abyte0[] = null;
		try {
			java.io.InputStream inputstream = Util.openStream(arg0);
			DataInputStream datainputstream = new DataInputStream(inputstream);
			abyte0 = new byte[3];
			vj = 0;
			for (int i = 0; i < 3; i += datainputstream.read(abyte0, i, 3 - i))
				;
			int l = re(abyte0);
			abyte0 = new byte[l];
			vj = 0;
			for (int k = 0; k < l; k += datainputstream.read(abyte0, k, l - k))
				;
			datainputstream.close();
		} catch (IOException _ex) {
			og = 0;
			wg = 0;
			return;
		}
		int i1 = re(abyte0);
		int j1 = re(abyte0);
		wd(i1, j1);
		qi = new int[j1][];
		for (int k3 = 0; k3 < i1; k3++) {
			int k1 = re(abyte0);
			int l1 = re(abyte0);
			int i2 = re(abyte0);
			oe(k1, l1, i2);
		}

		for (int l3 = 0; l3 < j1; l3++) {
			int j2 = re(abyte0);
			int k2 = re(abyte0);
			int l2 = re(abyte0);
			int i3 = re(abyte0);
			sj = re(abyte0);
			tj = re(abyte0);
			int j3 = re(abyte0);
			int ai1[] = new int[j2];
			for (int i4 = 0; i4 < j2; i4++)
				ai1[i4] = re(abyte0);

			int ai2[] = new int[i3];
			for (int j4 = 0; j4 < i3; j4++)
				ai2[j4] = re(abyte0);

			int k4 = ne(j2, ai1, k2, l2);
			qi[l3] = ai2;
			if (j3 == 0)
				dh[k4] = 0;
			else
				dh[k4] = hi;
		}

		jh = 1;
	}

	public Model(Model arg0[], int arg1, boolean arg2, boolean arg3, boolean arg4,
			boolean arg5) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		xh = arg2;
		yh = arg3;
		zh = arg4;
		ai = arg5;
		fe(arg0, arg1, false);
	}

	public Model(Model arg0[], int arg1) {
		jh = 1;
		kh = true;
		rh = true;
		sh = false;
		th = false;
		uh = -1;
		xh = false;
		yh = false;
		zh = false;
		ai = false;
		bi = false;
		hi = 0xbc614e;
		nj = 0xbc614e;
		oj = 180;
		pj = 155;
		qj = 95;
		rj = 256;
		sj = 512;
		tj = 32;
		fe(arg0, arg1, true);
	}

	public void fe(Model arg0[], int arg1, boolean arg2) {
		int i = 0;
		int k = 0;
		for (int l = 0; l < arg1; l++) {
			i += arg0[l].wg;
			k += arg0[l].og;
		}

		wd(k, i);
		if (arg2)
			qi = new int[i][];
		for (int i1 = 0; i1 < arg1; i1++) {
			Model h1 = arg0[i1];
			h1.ie();
			tj = h1.tj;
			sj = h1.sj;
			oj = h1.oj;
			pj = h1.pj;
			qj = h1.qj;
			rj = h1.rj;
			for (int j1 = 0; j1 < h1.wg; j1++) {
				int ai1[] = new int[h1.xg[j1]];
				int ai2[] = h1.yg[j1];
				for (int k1 = 0; k1 < h1.xg[j1]; k1++)
					ai1[k1] = oe(h1.ji[ai2[k1]], h1.ki[ai2[k1]], h1.li[ai2[k1]]);

				int l1 = ne(h1.xg[j1], ai1, h1.zg[j1], h1.ah[j1]);
				dh[l1] = h1.dh[j1];
				ch[l1] = h1.ch[j1];
				bh[l1] = h1.bh[j1];
				if (arg2)
					if (arg1 > 1) {
						qi[l1] = new int[h1.qi[j1].length + 1];
						qi[l1][0] = i1;
						for (int i2 = 0; i2 < h1.qi[j1].length; i2++)
							qi[l1][i2 + 1] = h1.qi[j1][i2];

					} else {
						qi[l1] = new int[h1.qi[j1].length];
						for (int j2 = 0; j2 < h1.qi[j1].length; j2++)
							qi[l1][j2] = h1.qi[j1][j2];

					}
			}

		}

		jh = 1;
	}

	public Model(int arg0, int arg1[], int arg2[], int arg3[], int arg4, int arg5) {
		this(arg0, 1);
		og = arg0;
		for (int i = 0; i < arg0; i++) {
			ji[i] = arg1[i];
			ki[i] = arg2[i];
			li[i] = arg3[i];
		}

		wg = 1;
		xg[0] = arg0;
		int ai1[] = new int[arg0];
		for (int k = 0; k < arg0; k++)
			ai1[k] = k;

		yg[0] = ai1;
		zg[0] = arg4;
		ah[0] = arg5;
		jh = 1;
	}

	public int oe(int arg0, int arg1, int arg2) {
		for (int i = 0; i < og; i++)
			if (ji[i] == arg0 && ki[i] == arg1 && li[i] == arg2)
				return i;

		if (og >= ii) {
			return -1;
		} else {
			ji[og] = arg0;
			ki[og] = arg1;
			li[og] = arg2;
			return og++;
		}
	}

	public int de(int arg0, int arg1, int arg2) {
		if (og >= ii) {
			return -1;
		} else {
			ji[og] = arg0;
			ki[og] = arg1;
			li[og] = arg2;
			return og++;
		}
	}

	public int ne(int arg0, int arg1[], int arg2, int arg3) {
		if (wg >= pi) {
			return -1;
		} else {
			xg[wg] = arg0;
			yg[wg] = arg1;
			zg[wg] = arg2;
			ah[wg] = arg3;
			jh = 1;
			return wg++;
		}
	}

	public Model[] ud(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
			int arg6, boolean arg7) {
		ie();
		int ai1[] = new int[arg5];
		int ai2[] = new int[arg5];
		for (int i = 0; i < arg5; i++) {
			ai1[i] = 0;
			ai2[i] = 0;
		}

		for (int k = 0; k < wg; k++) {
			int l = 0;
			int i1 = 0;
			int k1 = xg[k];
			int ai3[] = yg[k];
			for (int k2 = 0; k2 < k1; k2++) {
				l += ji[ai3[k2]];
				i1 += li[ai3[k2]];
			}

			int i3 = l / (k1 * arg2) + (i1 / (k1 * arg3)) * arg4;
			ai1[i3] += k1;
			ai2[i3]++;
		}

		Model ah1[] = new Model[arg5];
		for (int j1 = 0; j1 < arg5; j1++) {
			if (ai1[j1] > arg6)
				ai1[j1] = arg6;
			ah1[j1] = new Model(ai1[j1], ai2[j1], true, true, true, arg7, true);
			ah1[j1].sj = sj;
			ah1[j1].tj = tj;
		}

		for (int l1 = 0; l1 < wg; l1++) {
			int i2 = 0;
			int l2 = 0;
			int j3 = xg[l1];
			int ai4[] = yg[l1];
			for (int k3 = 0; k3 < j3; k3++) {
				i2 += ji[ai4[k3]];
				l2 += li[ai4[k3]];
			}

			int l3 = i2 / (j3 * arg2) + (l2 / (j3 * arg3)) * arg4;
			ze(ah1[l3], ai4, j3, l1);
		}

		for (int j2 = 0; j2 < arg5; j2++)
			ah1[j2].te();

		return ah1;
	}

	public void ze(Model arg0, int arg1[], int arg2, int arg3) {
		int ai1[] = new int[arg2];
		for (int i = 0; i < arg2; i++) {
			int k = ai1[i] = arg0.oe(ji[arg1[i]], ki[arg1[i]], li[arg1[i]]);
			arg0.ug[k] = ug[arg1[i]];
			arg0.vg[k] = vg[arg1[i]];
		}

		int l = arg0.ne(arg2, ai1, zg[arg3], ah[arg3]);
		if (!arg0.ai && !ai)
			arg0.vh[l] = vh[arg3];
		arg0.dh[l] = dh[arg3];
		arg0.ch[l] = ch[arg3];
		arg0.bh[l] = bh[arg3];
	}

	public void se(boolean arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		tj = 256 - arg1 * 4;
		sj = (64 - arg2) * 16 + 128;
		if (zh)
			return;
		for (int i = 0; i < wg; i++)
			if (arg0)
				dh[i] = hi;
			else
				dh[i] = 0;

		oj = arg3;
		pj = arg4;
		qj = arg5;
		rj = (int) Math.sqrt(arg3 * arg3 + arg4 * arg4 + arg5 * arg5);
		me();
	}

	public void be(int arg0, int arg1, int arg2, int arg3, int arg4) {
		tj = 256 - arg0 * 4;
		sj = (64 - arg1) * 16 + 128;
		if (zh) {
			return;
		} else {
			oj = arg2;
			pj = arg3;
			qj = arg4;
			rj = (int) Math.sqrt(arg2 * arg2 + arg3 * arg3 + arg4 * arg4);
			me();
			return;
		}
	}

	public void ye(int arg0, int arg1, int arg2) {
		if (zh) {
			return;
		} else {
			oj = arg0;
			pj = arg1;
			qj = arg2;
			rj = (int) Math.sqrt(arg0 * arg0 + arg1 * arg1 + arg2 * arg2);
			me();
			return;
		}
	}

	public void xd(int arg0, int arg1) {
		vg[arg0] = (byte) arg1;
	}

	public void ve(int arg0, int arg1, int arg2) {
		aj = aj + arg0 & 0xff;
		bj = bj + arg1 & 0xff;
		cj = cj + arg2 & 0xff;
		we();
		jh = 1;
	}

	public void ke(int arg0, int arg1, int arg2) {
		aj = arg0 & 0xff;
		bj = arg1 & 0xff;
		cj = arg2 & 0xff;
		we();
		jh = 1;
	}

	public void zd(int arg0, int arg1, int arg2) {
		xi += arg0;
		yi += arg1;
		zi += arg2;
		we();
		jh = 1;
	}

	public void ge(int arg0, int arg1, int arg2) {
		xi = arg0;
		yi = arg1;
		zi = arg2;
		we();
		jh = 1;
	}

	public int af() {
		return xi;
	}

	public void vd(int arg0, int arg1, int arg2) {
		dj = arg0;
		ej = arg1;
		fj = arg2;
		we();
		jh = 1;
	}

	public void ae(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		gj = arg0;
		hj = arg1;
		ij = arg2;
		jj = arg3;
		kj = arg4;
		lj = arg5;
		we();
		jh = 1;
	}

	private void we() {
		if (gj != 256 || hj != 256 || ij != 256 || jj != 256 || kj != 256
				|| lj != 256) {
			mj = 4;
			return;
		}
		if (dj != 256 || ej != 256 || fj != 256) {
			mj = 3;
			return;
		}
		if (aj != 0 || bj != 0 || cj != 0) {
			mj = 2;
			return;
		}
		if (xi != 0 || yi != 0 || zi != 0) {
			mj = 1;
			return;
		} else {
			mj = 0;
			return;
		}
	}

	private void bf(int arg0, int arg1, int arg2) {
		for (int i = 0; i < og; i++) {
			mi[i] += arg0;
			ni[i] += arg1;
			oi[i] += arg2;
		}

	}

	private void ee(int arg0, int arg1, int arg2) {
		for (int k2 = 0; k2 < og; k2++) {
			if (arg2 != 0) {
				int i = ci[arg2];
				int i1 = ci[arg2 + 256];
				int l1 = ni[k2] * i + mi[k2] * i1 >> 15;
				ni[k2] = ni[k2] * i1 - mi[k2] * i >> 15;
				mi[k2] = l1;
			}
			if (arg0 != 0) {
				int k = ci[arg0];
				int j1 = ci[arg0 + 256];
				int i2 = ni[k2] * j1 - oi[k2] * k >> 15;
				oi[k2] = ni[k2] * k + oi[k2] * j1 >> 15;
				ni[k2] = i2;
			}
			if (arg1 != 0) {
				int l = ci[arg1];
				int k1 = ci[arg1 + 256];
				int j2 = oi[k2] * l + mi[k2] * k1 >> 15;
				oi[k2] = oi[k2] * k1 - mi[k2] * l >> 15;
				mi[k2] = j2;
			}
		}

	}

	private void ce(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5) {
		for (int i = 0; i < og; i++) {
			if (arg0 != 0)
				mi[i] += ni[i] * arg0 >> 8;
			if (arg1 != 0)
				oi[i] += ni[i] * arg1 >> 8;
			if (arg2 != 0)
				mi[i] += oi[i] * arg2 >> 8;
			if (arg3 != 0)
				ni[i] += oi[i] * arg3 >> 8;
			if (arg4 != 0)
				oi[i] += mi[i] * arg4 >> 8;
			if (arg5 != 0)
				ni[i] += mi[i] * arg5 >> 8;
		}

	}

	private void je(int arg0, int arg1, int arg2) {
		for (int i = 0; i < og; i++) {
			mi[i] = mi[i] * arg0 >> 8;
			ni[i] = ni[i] * arg1 >> 8;
			oi[i] = oi[i] * arg2 >> 8;
		}

	}

	private void td() {
		lh = nh = ph = 0xf423f;
		nj = mh = oh = qh = 0xfff0bdc1;
		for (int i = 0; i < wg; i++) {
			int ai1[] = yg[i];
			int l = ai1[0];
			int j1 = xg[i];
			int k1;
			int l1 = k1 = mi[l];
			int i2;
			int j2 = i2 = ni[l];
			int k2;
			int l2 = k2 = oi[l];
			for (int k = 0; k < j1; k++) {
				int i1 = ai1[k];
				if (mi[i1] < k1)
					k1 = mi[i1];
				else if (mi[i1] > l1)
					l1 = mi[i1];
				if (ni[i1] < i2)
					i2 = ni[i1];
				else if (ni[i1] > j2)
					j2 = ni[i1];
				if (oi[i1] < k2)
					k2 = oi[i1];
				else if (oi[i1] > l2)
					l2 = oi[i1];
			}

			if (!yh) {
				ri[i] = k1;
				si[i] = l1;
				ti[i] = i2;
				ui[i] = j2;
				vi[i] = k2;
				wi[i] = l2;
			}
			if (l1 - k1 > nj)
				nj = l1 - k1;
			if (j2 - i2 > nj)
				nj = j2 - i2;
			if (l2 - k2 > nj)
				nj = l2 - k2;
			if (k1 < lh)
				lh = k1;
			if (l1 > mh)
				mh = l1;
			if (i2 < nh)
				nh = i2;
			if (j2 > oh)
				oh = j2;
			if (k2 < ph)
				ph = k2;
			if (l2 > qh)
				qh = l2;
		}

	}

	public void me() {
		if (zh)
			return;
		int i = sj * rj >> 8;
		for (int k = 0; k < wg; k++)
			if (dh[k] != hi)
				dh[k] = (eh[k] * oj + fh[k] * pj + gh[k] * qj) / i;

		int ai1[] = new int[og];
		int ai2[] = new int[og];
		int ai3[] = new int[og];
		int ai4[] = new int[og];
		for (int l = 0; l < og; l++) {
			ai1[l] = 0;
			ai2[l] = 0;
			ai3[l] = 0;
			ai4[l] = 0;
		}

		for (int i1 = 0; i1 < wg; i1++)
			if (dh[i1] == hi) {
				for (int j1 = 0; j1 < xg[i1]; j1++) {
					int l1 = yg[i1][j1];
					ai1[l1] += eh[i1];
					ai2[l1] += fh[i1];
					ai3[l1] += gh[i1];
					ai4[l1]++;
				}

			}

		for (int k1 = 0; k1 < og; k1++)
			if (ai4[k1] > 0)
				ug[k1] = (ai1[k1] * oj + ai2[k1] * pj + ai3[k1] * qj) / (i * ai4[k1]);

	}

	public void pe() {
		if (zh && yh)
			return;
		for (int i = 0; i < wg; i++) {
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
			for (j3 = j1 * j2 - i2 * k1; l2 > 8192 || i3 > 8192 || j3 > 8192
					|| l2 < -8192 || i3 < -8192 || j3 < -8192; j3 >>= 1) {
				l2 >>= 1;
				i3 >>= 1;
			}

			int k3 = (int) (256D * Math.sqrt(l2 * l2 + i3 * i3 + j3 * j3));
			if (k3 <= 0)
				k3 = 1;
			eh[i] = (l2 * 0x10000) / k3;
			fh[i] = (i3 * 0x10000) / k3;
			gh[i] = (j3 * 65535) / k3;
			ch[i] = -1;
		}

		me();
	}

	public void sd() {
		if (jh == 2) {
			jh = 0;
			for (int i = 0; i < og; i++) {
				mi[i] = ji[i];
				ni[i] = ki[i];
				oi[i] = li[i];
			}

			lh = nh = ph = 0xff676981;
			nj = mh = oh = qh = 0x98967f;
			return;
		}
		if (jh == 1) {
			jh = 0;
			for (int k = 0; k < og; k++) {
				mi[k] = ji[k];
				ni[k] = ki[k];
				oi[k] = li[k];
			}

			if (mj >= 2)
				ee(aj, bj, cj);
			if (mj >= 3)
				je(dj, ej, fj);
			if (mj >= 4)
				ce(gj, hj, ij, jj, kj, lj);
			if (mj >= 1)
				bf(xi, yi, zi);
			td();
			pe();
		}
	}

	public void he(int arg0, int arg1, int arg2, int arg3, int arg4, int arg5,
			int arg6, int arg7) {
		sd();
		if (ph > Scene.ip || qh < Scene.hp || lh > Scene.ep || mh < Scene.dp || nh > Scene.gp
				|| oh < Scene.fp) {
			kh = false;
			return;
		}
		kh = true;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		if (arg5 != 0) {
			i1 = di[arg5];
			j1 = di[arg5 + 1024];
		}
		if (arg4 != 0) {
			i2 = di[arg4];
			j2 = di[arg4 + 1024];
		}
		if (arg3 != 0) {
			k1 = di[arg3];
			l1 = di[arg3 + 1024];
		}
		for (int k2 = 0; k2 < og; k2++) {
			int l2 = mi[k2] - arg0;
			int i3 = ni[k2] - arg1;
			int j3 = oi[k2] - arg2;
			if (arg5 != 0) {
				int i = i3 * i1 + l2 * j1 >> 15;
				i3 = i3 * j1 - l2 * i1 >> 15;
				l2 = i;
			}
			if (arg4 != 0) {
				int k = j3 * i2 + l2 * j2 >> 15;
				j3 = j3 * j2 - l2 * i2 >> 15;
				l2 = k;
			}
			if (arg3 != 0) {
				int l = i3 * l1 - j3 * k1 >> 15;
				j3 = i3 * k1 + j3 * l1 >> 15;
				i3 = l;
			}
			if (j3 >= arg7)
				sg[k2] = (l2 << arg6) / j3;
			else
				sg[k2] = l2 << arg6;
			if (j3 >= arg7)
				tg[k2] = (i3 << arg6) / j3;
			else
				tg[k2] = i3 << arg6;
			pg[k2] = l2;
			qg[k2] = i3;
			rg[k2] = j3;
		}

	}

	public void ie() {
		sd();
		for (int i = 0; i < og; i++) {
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

	public Model qe() {
		Model ah1[] = new Model[1];
		ah1[0] = this;
		Model h1 = new Model(ah1, 1);
		h1.ih = ih;
		h1.th = th;
		return h1;
	}

	public Model ue(boolean arg0, boolean arg1, boolean arg2, boolean arg3) {
		Model ah1[] = new Model[1];
		ah1[0] = this;
		Model h1 = new Model(ah1, 1, arg0, arg1, arg2, arg3);
		h1.ih = ih;
		return h1;
	}

	public void yd(Model arg0) {
		aj = arg0.aj;
		bj = arg0.bj;
		cj = arg0.cj;
		xi = arg0.xi;
		yi = arg0.yi;
		zi = arg0.zi;
		we();
		jh = 1;
	}

	public int re(byte arg0[]) {
		for (; arg0[vj] == 10 || arg0[vj] == 13; vj++)
			;
		int i = fi[arg0[vj++] & 0xff];
		int k = fi[arg0[vj++] & 0xff];
		int l = fi[arg0[vj++] & 0xff];
		int i1 = (i * 4096 + k * 64 + l) - 0x20000;
		if (i1 == 0x1e240)
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
	public boolean th;
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

	static {
		ci = new int[512];
		di = new int[2048];
		ei = new byte[64];
		fi = new int[256];
		for (int i = 0; i < 256; i++) {
			ci[i] = (int) (Math.sin((double) i * 0.02454369D) * 32768D);
			ci[i + 256] = (int) (Math.cos((double) i * 0.02454369D) * 32768D);
		}

		for (int k = 0; k < 1024; k++) {
			di[k] = (int) (Math.sin((double) k * 0.00613592315D) * 32768D);
			di[k + 1024] = (int) (Math.cos((double) k * 0.00613592315D) * 32768D);
		}

		for (int l = 0; l < 10; l++)
			ei[l] = (byte) (48 + l);

		for (int i1 = 0; i1 < 26; i1++)
			ei[i1 + 10] = (byte) (65 + i1);

		for (int j1 = 0; j1 < 26; j1++)
			ei[j1 + 36] = (byte) (97 + j1);

		ei[62] = -93;
		ei[63] = 36;
		for (int k1 = 0; k1 < 10; k1++)
			fi[48 + k1] = k1;

		for (int l1 = 0; l1 < 26; l1++)
			fi[65 + l1] = l1 + 10;

		for (int i2 = 0; i2 < 26; i2++)
			fi[97 + i2] = i2 + 36;

		fi[163] = 62;
		fi[36] = 63;
	}
}

