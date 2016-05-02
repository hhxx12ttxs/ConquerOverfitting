import java.util.Arrays;

import jagex.util;

public final class cache {

	public static int pushmodel(String name) {
		if (name.equalsIgnoreCase("na"))
			return 0;
		for (int i = 0; i < modelcount; i++)
			if (modelnames[i].equalsIgnoreCase(name))
				return i;
		modelnames[modelcount++] = name;
		return modelcount - 1;
	}

	public static int readByte() {
		int i = intdata[intoff] & 0xff;
		intoff++;
		return i;
	}

	public static int readShort() {
		int i = util.g2(intdata, intoff);
		intoff += 2;
		return i;
	}

	public static int readInteger() {
		int i = util.g4(intdata, intoff);
		intoff += 4;
		if(i > 0x5f5e0ff)
			i = 0x5f5e0ff - i;
		return i;
	}

	public static String readString() {
		String s;
		for(s = ""; strdata[stroff] != 0; s = s + (char)strdata[stroff++]);
		stroff++;
		return s;
	}

	public static void load(byte[] entityarc, boolean members) {
		strdata = util.getf("string.dat", 0, entityarc);
		stroff = 0;
		intdata = util.getf("integer.dat", 0, entityarc);
		intoff = 0;
		itemcount = readShort();
		itemnames = new String[itemcount];
		itemexamines = new String[itemcount];
		itemcommand = new String[itemcount];
		njb = new int[itemcount];
		itembaseprice = new int[itemcount];
		pjb = new int[itemcount];
		itemunused = new int[itemcount];
		rjb = new int[itemcount];
		sjb = new int[itemcount];
		tjb = new int[itemcount];
		itemmembers = new int[itemcount];
		for (int i = 0; i < itemcount; i++)
			itemnames[i] = readString();

		for (int j = 0; j < itemcount; j++)
			itemexamines[j] = readString();

		for (int k = 0; k < itemcount; k++)
			itemcommand[k] = readString();

		for (int l = 0; l < itemcount; l++) {
			njb[l] = readShort();
			if (njb[l] + 1 > jjb) {
				jjb = njb[l] + 1;
			}
		}

		for(int i1 = 0; i1 < itemcount; i1++)
			itembaseprice[i1] = readInteger();

		for(int j1 = 0; j1 < itemcount; j1++)
			pjb[j1] = readByte();

		for(int k1 = 0; k1 < itemcount; k1++)
			itemunused[k1] = readByte();

		for(int l1 = 0; l1 < itemcount; l1++)
			rjb[l1] = readShort();

		for(int i2 = 0; i2 < itemcount; i2++)
			sjb[i2] = readInteger();

		for(int j2 = 0; j2 < itemcount; j2++)
			tjb[j2] = readByte();

		for(int k2 = 0; k2 < itemcount; k2++)
			itemmembers[k2] = readByte();

		for (int i = 0; i < itemcount; i++) {
			if (!members && itemmembers[i] == 1) {
				itemnames[i] = "Members object";
				itemexamines[i] = "You need to be a member to use this object";
				itembaseprice[i] = 0;
				itemcommand[i] = "";
				itemunused[0] = 0;
				rjb[i] = 0;
				tjb[i] = 1;
			}
		}

		vjb = readShort();
		wjb = new String[vjb];
		xjb = new String[vjb];
		yjb = new String[vjb];
		zjb = new int[vjb];
		akb = new int[vjb];
		bkb = new int[vjb];
		ckb = new int[vjb];
		dkb = new int[vjb];
		ekb = new int[vjb][12];
		fkb = new int[vjb];
		gkb = new int[vjb];
		hkb = new int[vjb];
		ikb = new int[vjb];
		jkb = new int[vjb];
		kkb = new int[vjb];
		lkb = new int[vjb];
		mkb = new int[vjb];
		nkb = new int[vjb];
		for(int i3 = 0; i3 < vjb; i3++)
			wjb[i3] = readString();

		for(int j3 = 0; j3 < vjb; j3++)
			xjb[j3] = readString();

		for(int k3 = 0; k3 < vjb; k3++)
			zjb[k3] = readByte();

		for(int l3 = 0; l3 < vjb; l3++)
			akb[l3] = readByte();

		for(int i4 = 0; i4 < vjb; i4++)
			bkb[i4] = readByte();

		for(int j4 = 0; j4 < vjb; j4++)
			ckb[j4] = readByte();

		for(int k4 = 0; k4 < vjb; k4++)
			dkb[k4] = readByte();

		for(int l4 = 0; l4 < vjb; l4++)
		{
			for(int i5 = 0; i5 < 12; i5++)
			{
				ekb[l4][i5] = readByte();
				if(ekb[l4][i5] == 255)
					ekb[l4][i5] = -1;
			}

		}

		for(int j5 = 0; j5 < vjb; j5++)
			fkb[j5] = readInteger();

		for(int k5 = 0; k5 < vjb; k5++)
			gkb[k5] = readInteger();

		for(int l5 = 0; l5 < vjb; l5++)
			hkb[l5] = readInteger();

		for(int i6 = 0; i6 < vjb; i6++)
			ikb[i6] = readInteger();

		for(int j6 = 0; j6 < vjb; j6++)
			jkb[j6] = readShort();

		for(int k6 = 0; k6 < vjb; k6++)
			kkb[k6] = readShort();

		for(int l6 = 0; l6 < vjb; l6++)
			lkb[l6] = readByte();

		for(int i7 = 0; i7 < vjb; i7++)
			mkb[i7] = readByte();

		for(int j7 = 0; j7 < vjb; j7++)
			nkb[j7] = readByte();

		for(int k7 = 0; k7 < vjb; k7++)
			yjb[k7] = readString();

		texturecount = readShort();
		pkb = new String[texturecount];
		qkb = new String[texturecount];
		for(int l7 = 0; l7 < texturecount; l7++)
			pkb[l7] = readString();

		for(int i8 = 0; i8 < texturecount; i8++)
			qkb[i8] = readString();

		rkb = readShort();
		skb = new String[rkb];
		tkb = new int[rkb];
		ukb = new int[rkb];
		vkb = new int[rkb];
		wkb = new int[rkb];
		xkb = new int[rkb];
		for(int j8 = 0; j8 < rkb; j8++)
			skb[j8] = readString();

		for(int k8 = 0; k8 < rkb; k8++)
			tkb[k8] = readInteger();

		for(int l8 = 0; l8 < rkb; l8++)
			ukb[l8] = readByte();

		for(int i9 = 0; i9 < rkb; i9++)
			vkb[i9] = readByte();

		for(int j9 = 0; j9 < rkb; j9++)
			wkb[j9] = readByte();

		for(int k9 = 0; k9 < rkb; k9++)
			xkb[k9] = readByte();

		ykb = readShort();
		zkb = new String[ykb];
		alb = new String[ykb];
		blb = new String[ykb];
		clb = new String[ykb];
		dlb = new int[ykb];
		elb = new int[ykb];
		flb = new int[ykb];
		glb = new int[ykb];
		hlb = new int[ykb];
		for(int l9 = 0; l9 < ykb; l9++)
			zkb[l9] = readString();

		for(int i10 = 0; i10 < ykb; i10++)
			alb[i10] = readString();

		for(int j10 = 0; j10 < ykb; j10++)
			blb[j10] = readString();

		for(int k10 = 0; k10 < ykb; k10++)
			clb[k10] = readString();

		for(int l10 = 0; l10 < ykb; l10++)
			dlb[l10] = pushmodel(readString());

		for(int i11 = 0; i11 < ykb; i11++)
			elb[i11] = readByte();

		for(int j11 = 0; j11 < ykb; j11++)
			flb[j11] = readByte();

		for(int k11 = 0; k11 < ykb; k11++)
			glb[k11] = readByte();

		for(int l11 = 0; l11 < ykb; l11++)
			hlb[l11] = readByte();

		ilb = readShort();
		jlb = new String[ilb];
		klb = new String[ilb];
		llb = new String[ilb];
		mlb = new String[ilb];
		nlb = new int[ilb];
		olb = new int[ilb];
		plb = new int[ilb];
		qlb = new int[ilb];
		rlb = new int[ilb];
		for(int i12 = 0; i12 < ilb; i12++)
			jlb[i12] = readString();

		for(int j12 = 0; j12 < ilb; j12++)
			klb[j12] = readString();

		for(int k12 = 0; k12 < ilb; k12++)
			llb[k12] = readString();

		for(int l12 = 0; l12 < ilb; l12++)
			mlb[l12] = readString();

		for(int i13 = 0; i13 < ilb; i13++)
			nlb[i13] = readShort();

		for(int j13 = 0; j13 < ilb; j13++)
			olb[j13] = readInteger();

		for(int k13 = 0; k13 < ilb; k13++)
			plb[k13] = readInteger();

		for(int l13 = 0; l13 < ilb; l13++)
			qlb[l13] = readByte();

		for(int i14 = 0; i14 < ilb; i14++)
			rlb[i14] = readByte();

		slb = readShort();
		tlb = new int[slb];
		ulb = new int[slb];
		for(int j14 = 0; j14 < slb; j14++)
			tlb[j14] = readByte();

		for(int k14 = 0; k14 < slb; k14++)
			ulb[k14] = readByte();

		vlb = readShort();
		wlb = new int[vlb];
		xlb = new int[vlb];
		ylb = new int[vlb];
		for(int l14 = 0; l14 < vlb; l14++)
			wlb[l14] = readInteger();

		for(int i15 = 0; i15 < vlb; i15++)
			xlb[i15] = readByte();

		for(int j15 = 0; j15 < vlb; j15++)
			ylb[j15] = readByte();

		zlb = readShort();
		amb = readShort();
		bmb = new String[amb];
		cmb = new String[amb];
		dmb = new int[amb];
		emb = new int[amb];
		fmb = new int[amb];
		gmb = new int[amb][];
		hmb = new int[amb][];
		for(int k15 = 0; k15 < amb; k15++)
			bmb[k15] = readString();

		for(int l15 = 0; l15 < amb; l15++)
			cmb[l15] = readString();

		for(int i16 = 0; i16 < amb; i16++)
			dmb[i16] = readByte();

		for(int j16 = 0; j16 < amb; j16++)
			emb[j16] = readByte();

		for(int k16 = 0; k16 < amb; k16++)
			fmb[k16] = readByte();

		for(int l16 = 0; l16 < amb; l16++)
		{
			int i17 = readByte();
			gmb[l16] = new int[i17];
			for(int k17 = 0; k17 < i17; k17++)
				gmb[l16][k17] = readShort();

		}

		for(int j17 = 0; j17 < amb; j17++)
		{
			int l17 = readByte();
			hmb[j17] = new int[l17];
			for(int j18 = 0; j18 < l17; j18++)
				hmb[j17][j18] = readByte();

		}

		imb = readShort();
		jmb = new String[imb];
		kmb = new String[imb];
		lmb = new int[imb];
		mmb = new int[imb];
		for(int i18 = 0; i18 < imb; i18++)
			jmb[i18] = readString();

		for(int k18 = 0; k18 < imb; k18++)
			kmb[k18] = readString();

		for(int l18 = 0; l18 < imb; l18++)
			lmb[l18] = readByte();

		for(int i19 = 0; i19 < imb; i19++)
			mmb[i19] = readByte();

		byte abyte1[] = util.getf("words.txt", 0, entityarc);
		mp(abyte1, 0);
		byte abyte2[] = util.getf("badwords.txt", 0, entityarc);
		sp(abyte2, 0);
		strdata = null;
		intdata = null;
	}

	public static void mp(byte abyte0[], int i)
	{
		do
		{
			try
			{
				String s;
				for(s = ""; abyte0[i] != 13; s = s + (char)abyte0[i++]);
				i++;
				if(abyte0[i] == 10)
					i++;
				if(s.equals("-EOF-") || (s == null || s.length() <= 0))
					break;
				omb[nmb++] = s;
				continue;
			}
			catch(Exception _ex) { }
			break;
		} while(true);
		util.shb = nmb;
		util.thb = omb;
	}

	public static void sp(byte abyte0[], int i)
	{
		do
		{
			try
			{
				String s;
				for(s = ""; abyte0[i] != 13; s = s + (char)abyte0[i++]);
				i++;
				if(abyte0[i] == 10)
					i++;
				if(s.equals("-EOF-") || (s == null || s.length() <= 0))
					break;
				qmb[pmb++] = s;
				continue;
			}
			catch(Exception _ex) { }
			break;
		} while(true);
		String vowels[] = {
				"a", "e", "i", "o", "u"
		};
		int j = pmb;
		for(int k = 0; k < j; k++)
		{
			String s1 = qmb[k];
			if(s1.length() >= 5)
			{
				for(int l = 1; l < s1.length() - 1; l++)
				{
					char c = s1.charAt(l);
					if(c == 'a' || c == 'e' || c == 'i' || c == 'o' || c == 'u' || c == 'y')
					{
						for(int i1 = 0; i1 < 5; i1++)
						{
							String s2 = vowels[i1];
							if(s2.charAt(0) != c)
							{
								String s5 = s1.substring(0, l) + s2 + s1.substring(l + 1);
								qmb[pmb++] = s5;
								s5 = s1.substring(0, l) + s2 + c + s1.substring(l + 1);
								qmb[pmb++] = s5;
							}
						}

						String s3 = s1.substring(0, l) + s1.substring(l + 1);
						qmb[pmb++] = s3;
					}
					char c1 = s1.charAt(l + 1);
					String s4 = s1.substring(0, l) + c1 + c + s1.substring(l + 2);
					qmb[pmb++] = s4;
				}

			}
		}

		util.qhb = pmb;
		util.rhb = qmb;
	}

	public cache()
	{
	}

	public final int hjb = 0xbc614e;
	public static int itemcount;
	public static int jjb;
	public static String itemnames[];
	public static String itemexamines[];
	public static String itemcommand[];
	public static int njb[];
	public static int itembaseprice[];
	public static int pjb[];
	public static int itemunused[];
	public static int rjb[];
	public static int sjb[];
	public static int tjb[];
	public static int itemmembers[];
	public static int vjb;
	public static String wjb[];
	public static String xjb[];
	public static String yjb[];
	public static int zjb[];
	public static int akb[];
	public static int bkb[];
	public static int ckb[];
	public static int dkb[];
	public static int ekb[][];
	public static int fkb[];
	public static int gkb[];
	public static int hkb[];
	public static int ikb[];
	public static int jkb[];
	public static int kkb[];
	public static int lkb[];
	public static int mkb[];
	public static int nkb[];
	public static int texturecount;
	public static String pkb[];
	public static String qkb[];
	public static int rkb;
	public static String skb[];
	public static int tkb[];
	public static int ukb[];
	public static int vkb[];
	public static int wkb[];
	public static int xkb[];
	public static int ykb;
	public static String zkb[];
	public static String alb[];
	public static String blb[];
	public static String clb[];
	public static int dlb[];
	public static int elb[];
	public static int flb[];
	public static int glb[];
	public static int hlb[];
	public static int ilb;
	public static String jlb[];
	public static String klb[];
	public static String llb[];
	public static String mlb[];
	public static int nlb[];
	public static int olb[];
	public static int plb[];
	public static int qlb[];
	public static int rlb[];
	public static int slb;
	public static int tlb[];
	public static int ulb[];
	public static int vlb;
	public static int wlb[];
	public static int xlb[];
	public static int ylb[];
	public static int zlb;
	public static int amb;
	public static String bmb[];
	public static String cmb[];
	public static int dmb[];
	public static int emb[];
	public static int fmb[];
	public static int gmb[][];
	public static int hmb[][];
	public static int imb;
	public static String jmb[];
	public static String kmb[];
	public static int lmb[];
	public static int mmb[];
	public static int nmb;
	public static String omb[] = new String[5000];
	public static int pmb;
	public static String qmb[] = new String[5000];
	public static int modelcount;
	public static String modelnames[] = new String[200];
	static byte strdata[];
	static byte intdata[];
	static int stroff;
	static int intoff;

}
