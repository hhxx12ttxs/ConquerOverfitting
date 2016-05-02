package vjo.java.sun.text;

/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.kpdus.com/jad.html
//Decompiler options: packimports(3) radix(10) lradix(10) 
//Source File Name:   NormalizerImpl.java
import java.lang.IllegalArgumentException;

import vjo.java.lang.* ;
import vjo.java.lang.Character ;
import vjo.java.lang.System ;

import vjo.java.io.BufferedInputStream;
import vjo.java.io.ByteArrayInputStream;
import vjo.java.io.InputStream ;

import vjo.java.javaconversion.ClassUtil;

import vjo.java.security.AccessController;
import vjo.java.security.PrivilegedAction;

//Referenced classes of package sun.text:
//         IntTrie, CharTrie, Normalizer, NormalizerDataReader, 
//         Trie

public final class NormalizerImpl {
	private static final class ComposePartArgs {

		int prevCC;

		int length;

		private ComposePartArgs() {
		}

	}

	private static final class RecomposeArgs {

		char source[];

		int start;

		int limit;

		private RecomposeArgs() {
		}

	}

	private static final class NextCombiningArgs {

		char source[];

		int start;

		char c;

		char c2;

		int combiningIndex;

		char cc;

		private NextCombiningArgs() {
		}

	}

	private static final class PrevArgs {

		char src[];

		int start;

		int current;

		char c;

		char c2;

		private PrevArgs() {
		}

	}

	private static final class NextCCArgs {

		char source[];

		int next;

		int limit;

		char c;

		char c2;

		private NextCCArgs() {
		}

	}

	private static final class DecomposeArgs {

		int cc;

		int trailCC;

		int length;

		private DecomposeArgs() {
		}

	}

	private static final class FCDTrieImpl implements Trie.DataManipulate {

		public int getFoldingOffset(int i) {
			return i;
		}

		static CharTrie fcdTrie = null;

		private FCDTrieImpl() {
		}

	}

	private static final class NormTrieImpl implements Trie.DataManipulate {

		public int getFoldingOffset(int i) {
			return 2048 + (i >> 11 & 32736);
		}

		static IntTrie normTrie = null;

		private NormTrieImpl() {
		}

	}

	static int getFromIndexesArr(int i) {
		return indexes[i];
	}

	private NormalizerImpl() throws Exception {
		if (!isDataLoaded) {
			Object obj = AccessController.doPrivileged(new PrivilegedAction() {

				public Object run() {
					try {
//MrP - Needs ClassUtil
//						InputStream inputstream = getClass()
//								.getResourceAsStream("resources/unorm.icu");
						InputStream inputstream = ClassUtil.getResourceAsStream(getClass(),"resources/unorm.icu");
						BufferedInputStream bufferedinputstream = new BufferedInputStream(
								inputstream, 25000);
						NormalizerDataReader normalizerdatareader = new NormalizerDataReader(
								bufferedinputstream);
						NormalizerImpl.indexes = normalizerdatareader
								.readIndexes(32);
						byte abyte0[] = new byte[NormalizerImpl.indexes[0]];
						int i = NormalizerImpl.indexes[2];
						NormalizerImpl.combiningTable = new char[i];
						int j = NormalizerImpl.indexes[1];
						NormalizerImpl.extraData = new char[j];
						byte abyte1[] = new byte[NormalizerImpl.indexes[10]];
						NormalizerImpl.fcdTrieImpl = new FCDTrieImpl();
						NormalizerImpl.normTrieImpl = new NormTrieImpl();
						normalizerdatareader.read(abyte0, abyte1, null,
								NormalizerImpl.extraData,
								NormalizerImpl.combiningTable, null);
						NormTrieImpl.normTrie = new IntTrie(
								new ByteArrayInputStream(abyte0),
								NormalizerImpl.normTrieImpl);
						FCDTrieImpl.fcdTrie = new CharTrie(
								new ByteArrayInputStream(abyte1),
								NormalizerImpl.fcdTrieImpl);
						NormalizerImpl.isDataLoaded = true;
						bufferedinputstream.close();
						inputstream.close();
						return null;
					} catch (Exception exception) {
						return exception;
					}
				}
//MrP - JAD Casuality
//				final NormalizerImpl this$0;
//
//				{
//					this$0 = NormalizerImpl.this;
//					super();
//				}
			});
			if (obj instanceof Exception)
				throw (Exception) obj;
		}
	}

	private static boolean isHangulWithoutJamoT(char c) {
		c -= '\uAC00';
		return c < '\u2BA4' && c % 28 == 0;
	}

	private static boolean isNorm32Regular(long l) {
		return l < 4227858432L;
	}

	private static boolean isNorm32LeadSurrogate(long l) {
		return 4227858432L <= l && l < 4293918720L;
	}

	static boolean isNorm32HangulOrJamo(long l) {
		return l >= 4293918720L;
	}

	private static boolean isJamoVTNorm32JamoV(long l) {
		return l < 4294115328L;
	}

	static long getNorm32(char c) {
		return 4294967295L & (long) NormTrieImpl.normTrie.getLeadValue(c);
	}

	static long getNorm32FromSurrogatePair(long l, char c) {
		return 4294967295L & (long) NormTrieImpl.normTrie.getTrailValue(
				(int) l, c);
	}

	private static long getNorm32(int i) {
		return 4294967295L & (long) NormTrieImpl.normTrie.getCodePointValue(i);
	}

	private static long getNorm32(char ac[], int i, int j) {
		long l = getNorm32(ac[i]);
		if ((l & (long) j) > 0L && isNorm32LeadSurrogate(l))
			l = getNorm32FromSurrogatePair(l, ac[i + 1]);
		return l;
	}

	public static char getFCD16(char c) {
		return FCDTrieImpl.fcdTrie.getLeadValue(c);
	}

	public static char getFCD16FromSurrogatePair(char c, char c1) {
		return FCDTrieImpl.fcdTrie.getTrailValue(c, c1);
	}

	private static int getExtraDataIndex(long l) {
		return (int) (l >> 16);
	}

	private static int decompose(long l, int i, DecomposeArgs decomposeargs) {
		int j = getExtraDataIndex(l);
		decomposeargs.length = extraData[j++];
		if ((l & (long) i & 8L) != 0L && decomposeargs.length >= 256) {
			j += (decomposeargs.length >> 7 & 1) + (decomposeargs.length & 127);
			decomposeargs.length >>= 8;
		}
		if ((decomposeargs.length & 128) > 0) {
			char c = extraData[j++];
			decomposeargs.cc = 255 & c >> 8;
			decomposeargs.trailCC = 255 & c;
		} else {
			decomposeargs.cc = decomposeargs.trailCC = 0;
		}
		decomposeargs.length &= 127;
		return j;
	}

	private static int decompose(long l, DecomposeArgs decomposeargs) {
		int i = getExtraDataIndex(l);
		decomposeargs.length = extraData[i++];
		if ((decomposeargs.length & 128) > 0) {
			char c = extraData[i++];
			decomposeargs.cc = 255 & c >> 8;
			decomposeargs.trailCC = 255 & c;
		} else {
			decomposeargs.cc = decomposeargs.trailCC = 0;
		}
		decomposeargs.length &= 127;
		return i;
	}

	private static int getNextCC(NextCCArgs nextccargs) {
		nextccargs.c = nextccargs.source[nextccargs.next++];
		long l = getNorm32(nextccargs.c);
		if ((l & 65280L) == 0L) {
			nextccargs.c2 = '\0';
			return 0;
		}
		if (!isNorm32LeadSurrogate(l))
			nextccargs.c2 = '\0';
		else if (nextccargs.next != nextccargs.limit
				&& Character
						.isLowSurrogate(nextccargs.c2 = nextccargs.source[nextccargs.next])) {
			nextccargs.next++;
			l = getNorm32FromSurrogatePair(l, nextccargs.c2);
		} else {
			nextccargs.c2 = '\0';
			return 0;
		}
		return (int) (255L & l >> 8);
	}

	private static long getPrevNorm32(PrevArgs prevargs, int i, int j) {
		prevargs.c = prevargs.src[--prevargs.current];
		prevargs.c2 = '\0';
		if (prevargs.c < i)
			return 0L;
		if (prevargs.c < '\uD800' || prevargs.c > '\uDFFF')
			return getNorm32(prevargs.c);
		if (Character.isHighSurrogate(prevargs.c))
			return 0L;
		if (prevargs.current != prevargs.start
				&& Character
						.isHighSurrogate(prevargs.c2 = prevargs.src[prevargs.current - 1])) {
			prevargs.current--;
			long l = getNorm32(prevargs.c2);
			if ((l & (long) j) == 0L)
				return 0L;
			else
				return getNorm32FromSurrogatePair(l, prevargs.c);
		} else {
			prevargs.c2 = '\0';
			return 0L;
		}
	}

	private static int getPrevCC(PrevArgs prevargs) {
		return (int) (255L & getPrevNorm32(prevargs, 768, 65280) >> 8);
	}

	static boolean isNFDSafe(long l, int i, int j) {
		if ((l & (long) i) == 0L)
			return true;
		if (isNorm32Regular(l) && (l & (long) j) != 0L) {
			DecomposeArgs decomposeargs = new DecomposeArgs();
			decompose(l, j, decomposeargs);
			return decomposeargs.cc == 0;
		} else {
			return (l & 65280L) == 0L;
		}
	}

	static boolean isTrueStarter(long l, int i, int j) {
		if ((l & (long) i) == 0L)
			return true;
		if ((l & (long) j) != 0L) {
			DecomposeArgs decomposeargs = new DecomposeArgs();
			int k = decompose(l, j, decomposeargs);
			if (decomposeargs.cc == 0) {
				int i1 = i & 63;
				if ((getNorm32(extraData, k, i1) & (long) i1) == 0L)
					return true;
			}
		}
		return false;
	}

	private static int insertOrdered(char ac[], int i, int j, int k, char c,
			char c1, int l) {
		int k2 = l;
		if (i < j && l != 0) {
			int i1;
			int k1 = i1 = j;
			PrevArgs prevargs = new PrevArgs();
			prevargs.current = j;
			prevargs.start = i;
			prevargs.src = ac;
			int i2 = getPrevCC(prevargs);
			k1 = prevargs.current;
			if (l < i2) {
				k2 = i2;
				int j1 = k1;
				do {
					if (i >= k1)
						break;
					int j2 = getPrevCC(prevargs);
					k1 = prevargs.current;
					if (l >= j2)
						break;
					j1 = k1;
				} while (true);
				int l1 = k;
				do
					ac[--l1] = ac[--j];
				while (j1 != j);
			}
		}
		ac[j] = c;
		if (c1 != 0)
			ac[j + 1] = c1;
		return k2;
	}

	private static int mergeOrdered(char ac[], int i, int j, char ac1[], int k,
			int l, boolean flag) {
		int k1 = 0;
		boolean flag1 = j == k;
		NextCCArgs nextccargs = new NextCCArgs();
		nextccargs.source = ac1;
		nextccargs.next = k;
		nextccargs.limit = l;
		if (i != j || !flag)
			do {
				if (nextccargs.next >= nextccargs.limit)
					break;
				int j1 = getNextCC(nextccargs);
				if (j1 == 0) {
					k1 = 0;
					if (flag1) {
						j = nextccargs.next;
					} else {
						ac1[j++] = nextccargs.c;
						if (nextccargs.c2 != 0)
							ac1[j++] = nextccargs.c2;
					}
					if (flag)
						break;
					i = j;
				} else {
					int i1 = j + (nextccargs.c2 != 0 ? 2 : 1);
					k1 = insertOrdered(ac, i, j, i1, nextccargs.c,
							nextccargs.c2, j1);
					j = i1;
				}
			} while (true);
		if (nextccargs.next == nextccargs.limit)
			return k1;
		if (!flag1) {
			do
				ac[j++] = ac1[nextccargs.next++];
			while (nextccargs.next != nextccargs.limit);
			nextccargs.limit = j;
		}
		PrevArgs prevargs = new PrevArgs();
		prevargs.src = ac1;
		prevargs.start = i;
		prevargs.current = nextccargs.limit;
		return getPrevCC(prevargs);
	}

	private static int mergeOrdered(char ac[], int i, int j, char ac1[], int k,
			int l) {
		return mergeOrdered(ac, i, j, ac1, k, l, true);
	}

	static boolean checkFCD(char ac[], int i, int j, int k) {
		int l = 0;
		int j1 = i;
		int k1 = j;
		do {
			if (j1 == k1)
				return true;
			char c;
			char c1;
			if ((c1 = ac[j1++]) < '\u0300')
				l = -c1;
			else if ((c = getFCD16(c1)) == 0) {
				l = 0;
			} else {
				char c2;
				if (Character.isHighSurrogate(c1)) {
					if (j1 != k1 && Character.isLowSurrogate(c2 = ac[j1])) {
						j1++;
						c = getFCD16FromSurrogatePair(c, c2);
					} else {
						c2 = '\0';
						c = '\0';
					}
				} else {
					c2 = '\0';
				}
				if (nx_contains(k, c1, c2)) {
					l = 0;
				} else {
					int i1 = c >> 8;
					if (i1 != 0) {
						if (l < 0)
							if (!nx_contains(k, -l))
								l = FCDTrieImpl.fcdTrie
										.getBMPValue((char) (-l)) & 255;
							else
								l = 0;
						if (i1 < l)
							return false;
					}
					l = c & 255;
				}
			}
		} while (true);
	}

	static Normalizer.QuickCheckResult quickCheck(char ac[], int i, int j,
			int k, int l, boolean flag, int i1) {
		ComposePartArgs composepartargs = new ComposePartArgs();
		int i2 = i;
		if (!isDataLoaded)
			return Normalizer.MAYBE;
		int j1 = 65280 | l;
		Normalizer.QuickCheckResult quickcheckresult = Normalizer.YES;
		int k1 = 0;
		label0: do {
			do {
				if (i == j)
					return quickcheckresult;
				long l1;
				char c;
				if ((c = ac[i++]) < k
						|| ((l1 = getNorm32(c)) & (long) j1) == 0L) {
					k1 = 0;
					continue;
				}
				char c1;
				if (isNorm32LeadSurrogate(l1)) {
					if (i != j && Character.isLowSurrogate(c1 = ac[i])) {
						i++;
						l1 = getNorm32FromSurrogatePair(l1, c1);
					} else {
						l1 = 0L;
						c1 = '\0';
					}
				} else {
					c1 = '\0';
				}
				if (nx_contains(i1, c, c1))
					l1 = 0L;
				char c2 = (char) (int) (l1 >> 8 & 255L);
				if (c2 != 0 && c2 < k1)
					return Normalizer.NO;
				k1 = c2;
				long l2 = l1 & (long) l;
				if ((l2 & 15L) >= 1L) {
					quickcheckresult = Normalizer.NO;
					break label0;
				}
				if (l2 == 0L)
					continue;
				if (!flag)
					break;
				quickcheckresult = Normalizer.MAYBE;
			} while (true);
			int k2 = l << 2 & 15;
			int j2 = i - 1;
			if (Character.isLowSurrogate(ac[j2]))
				j2--;
			j2 = findPreviousStarter(ac, i2, j2, j1, k2, (char) k);
			i = findNextStarter(ac, i, j, l, k2, (char) k);
			composepartargs.prevCC = k1;
			char ac1[] = composePart(composepartargs, j2, ac, i, j, l, i1);
			if (0 == strCompare(ac1, 0, composepartargs.length, ac, j2, i - j2,
					false))
				continue;
			quickcheckresult = Normalizer.NO;
			break;
		} while (true);
		return quickcheckresult;
	}

	private static boolean needSingleQuotation(char c) {
		return c >= '\t' && c <= '\r' || c >= ' ' && c <= '/' || c >= ':'
				&& c <= '@' || c >= '[' && c <= '`' || c >= '{' && c <= '~';
	}

	public static String canonicalDecomposeWithSingleQuotation(String s) {
		char ac[] = s.toCharArray();
		int i = 0;
		int j = ac.length;
		char ac1[] = new char[ac.length * 3];
		int k = 0;
		int l = ac1.length;
		char ac2[] = new char[3];
		int k1 = 4;
		char c2 = (char) indexes[8];
		int j1 = 65280 | k1;
		int i2 = 0;
		int i3 = 0;
		long l1 = 0L;
		char c = '\0';
		boolean flag = false;
		byte byte1;
		byte byte0 = byte1 = -1;
		do {
			int i1 = i;
			for (; i != j
					&& ((c = ac[i]) < c2
							|| ((l1 = getNorm32(c)) & (long) j1) == 0L || c >= '\uAC00'
							&& c <= '\uD7A3'); i++)
				i3 = 0;

			if (i != i1) {
				int j2 = i - i1;
				if (k + j2 <= l)
					System.arraycopy(ac, i1, ac1, k, j2);
				k += j2;
				i2 = k;
			}
			if (i == j)
				break;
			i++;
			int k2;
			char c1;
			if (isNorm32Regular(l1)) {
				c1 = '\0';
				k2 = 1;
			} else if (i != j && Character.isLowSurrogate(c1 = ac[i])) {
				i++;
				k2 = 2;
				l1 = getNorm32FromSurrogatePair(l1, c1);
			} else {
				c1 = '\0';
				k2 = 1;
				l1 = 0L;
			}
			int l2;
			int j3;
			char ac3[];
			int k3;
			if ((l1 & (long) k1) == 0L) {
				l2 = j3 = (int) (255L & l1 >> 8);
				ac3 = null;
				k3 = -1;
			} else {
				DecomposeArgs decomposeargs = new DecomposeArgs();
				k3 = decompose(l1, k1, decomposeargs);
				ac3 = extraData;
				k2 = decomposeargs.length;
				l2 = decomposeargs.cc;
				j3 = decomposeargs.trailCC;
				if (k2 == 1) {
					c = ac3[k3];
					c1 = '\0';
					ac3 = null;
					k3 = -1;
				}
			}
			if (k + k2 * 3 >= l) {
				char ac4[] = new char[l * 2];
				System.arraycopy(ac1, 0, ac4, 0, k);
				ac1 = ac4;
				l = ac1.length;
			}
			int l3 = k;
			if (ac3 == null) {
				if (needSingleQuotation(c)) {
					ac1[k++] = '\'';
					ac1[k++] = c;
					ac1[k++] = '\'';
					j3 = 0;
				} else if (l2 != 0 && l2 < i3) {
					k += k2;
					j3 = insertOrdered(ac1, i2, l3, k, c, c1, l2);
				} else {
					ac1[k++] = c;
					if (c1 != 0)
						ac1[k++] = c1;
				}
			} else if (needSingleQuotation(ac3[k3])) {
				ac1[k++] = '\'';
				ac1[k++] = ac3[k3++];
				ac1[k++] = '\'';
				k2--;
				do
					ac1[k++] = ac3[k3++];
				while (--k2 > 0);
			} else if (l2 != 0 && l2 < i3) {
				k += k2;
				j3 = mergeOrdered(ac1, i2, l3, ac3, k3, k3 + k2);
			} else {
				do
					ac1[k++] = ac3[k3++];
				while (--k2 > 0);
			}
			i3 = j3;
			if (i3 == 0)
				i2 = k;
		} while (true);
		return new String(ac1, 0, k);
	}

	static int decompose(char ac[], int i, int j, char ac1[], int k, int l,
			boolean flag, int ai[], int i1) {
		char ac2[] = new char[3];
		int i2;
		char c2;
		if (!flag) {
			c2 = (char) indexes[8];
			i2 = 4;
		} else {
			c2 = (char) indexes[9];
			i2 = 8;
		}
		int k1 = 65280 | i2;
		int j2 = 0;
		int j3 = 0;
		long l1 = 0L;
		char c = '\0';
		int l3 = 0;
		int k3;
		int i3 = k3 = -1;
		do {
			int j1 = i;
			for (; i != j
					&& ((c = ac[i]) < c2 || ((l1 = getNorm32(c)) & (long) k1) == 0L); i++)
				j3 = 0;

			if (i != j1) {
				int k2 = i - j1;
				if (k + k2 <= l)
					System.arraycopy(ac, j1, ac1, k, k2);
				k += k2;
				j2 = k;
			}
			if (i == j)
				break;
			i++;
			int l2;
			char c1;
			char ac3[];
			if (isNorm32HangulOrJamo(l1)) {
				if (nx_contains(i1, c)) {
					c1 = '\0';
					ac3 = null;
					l2 = 1;
				} else {
					ac3 = ac2;
					l3 = 0;
					i3 = k3 = 0;
					c -= '\uAC00';
					c1 = (char) (c % 28);
					c /= '\034';
					if (c1 > 0) {
						ac2[2] = (char) (4519 + c1);
						l2 = 3;
					} else {
						l2 = 2;
					}
					ac2[1] = (char) (4449 + c % 21);
					ac2[0] = (char) (4352 + c / 21);
				}
			} else {
				if (isNorm32Regular(l1)) {
					c1 = '\0';
					l2 = 1;
				} else if (i != j && Character.isLowSurrogate(c1 = ac[i])) {
					i++;
					l2 = 2;
					l1 = getNorm32FromSurrogatePair(l1, c1);
				} else {
					c1 = '\0';
					l2 = 1;
					l1 = 0L;
				}
				if (nx_contains(i1, c, c1)) {
					i3 = k3 = 0;
					ac3 = null;
				} else if ((l1 & (long) i2) == 0L) {
					i3 = k3 = (int) (255L & l1 >> 8);
					ac3 = null;
					l3 = -1;
				} else {
					DecomposeArgs decomposeargs = new DecomposeArgs();
					l3 = decompose(l1, i2, decomposeargs);
					ac3 = extraData;
					l2 = decomposeargs.length;
					i3 = decomposeargs.cc;
					k3 = decomposeargs.trailCC;
					if (l2 == 1) {
						c = ac3[l3];
						c1 = '\0';
						ac3 = null;
						l3 = -1;
					}
				}
			}
			if (k + l2 <= l) {
				int i4 = k;
				if (ac3 == null) {
					if (i3 != 0 && i3 < j3) {
						k += l2;
						k3 = insertOrdered(ac1, j2, i4, k, c, c1, i3);
					} else {
						ac1[k++] = c;
						if (c1 != 0)
							ac1[k++] = c1;
					}
				} else if (i3 != 0 && i3 < j3) {
					k += l2;
					k3 = mergeOrdered(ac1, j2, i4, ac3, l3, l3 + l2);
				} else {
					do
						ac1[k++] = ac3[l3++];
					while (--l2 > 0);
				}
			} else {
				k += l2;
			}
			j3 = k3;
			if (j3 == 0)
				j2 = k;
		} while (true);
		ai[0] = j3;
		return k;
	}

	private static int getNextCombining(NextCombiningArgs nextcombiningargs,
			int i, int j) {
		nextcombiningargs.c = nextcombiningargs.source[nextcombiningargs.start++];
		long l = getNorm32(nextcombiningargs.c);
		nextcombiningargs.c2 = '\0';
		nextcombiningargs.combiningIndex = 0;
		nextcombiningargs.cc = '\0';
		if ((l & 65472L) == 0L)
			return 0;
		if (!isNorm32Regular(l)) {
			if (isNorm32HangulOrJamo(l)) {
				nextcombiningargs.combiningIndex = (int) (4294967295L & (65520L | l >> 16));
				return (int) (l & 192L);
			}
			if (nextcombiningargs.start != i
					&& Character
							.isLowSurrogate(nextcombiningargs.c2 = nextcombiningargs.source[nextcombiningargs.start])) {
				nextcombiningargs.start++;
				l = getNorm32FromSurrogatePair(l, nextcombiningargs.c2);
			} else {
				nextcombiningargs.c2 = '\0';
				return 0;
			}
		}
		if (nx_contains(j, nextcombiningargs.c, nextcombiningargs.c2))
			return 0;
		nextcombiningargs.cc = (char) (byte) (int) (l >> 8);
		int k = (int) (l & 192L);
		if (k != 0) {
			int i1 = getExtraDataIndex(l);
			nextcombiningargs.combiningIndex = i1 <= 0 ? 0
					: ((int) (extraData[i1 - 1]));
		}
		return k;
	}

	private static int getCombiningIndexFromStarter(char c, char c1) {
		long l = getNorm32(c);
		if (c1 != 0)
			l = getNorm32FromSurrogatePair(l, c1);
		return extraData[getExtraDataIndex(l) - 1];
	}

	private static int combine(char ac[], int i, int j, int ai[]) {
		if (ai.length < 2)
			throw new IllegalArgumentException();
		char c;
		do {
			c = ac[i++];
			if (c >= j)
				break;
			i += (ac[i] & 32768) == 0 ? 1 : 2;
		} while (true);
		if ((c & 32767) == j) {
			int l = ac[i];
			int k = (int) (4294967295L & (long) ((l & 8192) + 1));
			char c1;
			if ((l & 32768) != 0) {
				if ((l & 16384) != 0) {
					l = (int) (4294967295L & (long) (l & 1023 | 55296));
					c1 = ac[i + 1];
				} else {
					l = ac[i + 1];
					c1 = '\0';
				}
			} else {
				l &= 8191;
				c1 = '\0';
			}
			ai[0] = l;
			ai[1] = c1;
			return k;
		} else {
			return 0;
		}
	}

	private static char recompose(RecomposeArgs recomposeargs, int i) {
		int l3 = 0;
		int i4 = 0;
		int ai[] = new int[2];
		int k4 = -1;
		int i3 = 0;
		boolean flag = false;
		int j4 = 0;
		NextCombiningArgs nextcombiningargs = new NextCombiningArgs();
		nextcombiningargs.source = recomposeargs.source;
		nextcombiningargs.cc = '\0';
		nextcombiningargs.c2 = '\0';
		do {
			nextcombiningargs.start = recomposeargs.start;
			int l2 = getNextCombining(nextcombiningargs, recomposeargs.limit, i);
			int j3 = nextcombiningargs.combiningIndex;
			recomposeargs.start = nextcombiningargs.start;
			int k3;
			if ((l2 & 128) != 0 && k4 != -1)
				if ((j3 & 32768) != 0) {
					int j = -1;
					nextcombiningargs.c2 = recomposeargs.source[k4];
					if (j3 == 65522) {
						nextcombiningargs.c2 = (char) (nextcombiningargs.c2 - 4352);
						if (nextcombiningargs.c2 < '\023') {
							j = recomposeargs.start - 1;
							nextcombiningargs.c = (char) (44032 + (nextcombiningargs.c2 * 21 + (nextcombiningargs.c - 4449)) * 28);
							if (recomposeargs.start != recomposeargs.limit
									&& (nextcombiningargs.c2 = (char) (recomposeargs.source[recomposeargs.start] - 4519)) < '\034') {
								recomposeargs.start++;
								nextcombiningargs.c += nextcombiningargs.c2;
							}
							if (!nx_contains(i, nextcombiningargs.c)) {
								recomposeargs.source[k4] = nextcombiningargs.c;
							} else {
								if (!isHangulWithoutJamoT(nextcombiningargs.c))
									recomposeargs.start--;
								j = recomposeargs.start;
							}
						}
					}
					if (j != -1) {
						int l = j;
						for (int l1 = recomposeargs.start; l1 < recomposeargs.limit;)
							recomposeargs.source[l++] = recomposeargs.source[l1++];

						recomposeargs.start = j;
						recomposeargs.limit = l;
					}
					nextcombiningargs.c2 = '\0';
				} else if ((i3 & 32768) == 0
						&& (j4 < nextcombiningargs.cc || j4 == 0)
						&& 0 != (k3 = combine(combiningTable, i3, j3, ai))
						&& !nx_contains(i, (char) l3, (char) i4)) {
					l3 = ai[0];
					i4 = ai[1];
					int k = nextcombiningargs.c2 != 0 ? recomposeargs.start - 2
							: recomposeargs.start - 1;
					recomposeargs.source[k4] = (char) l3;
					if (flag) {
						if (i4 != 0) {
							recomposeargs.source[k4 + 1] = (char) i4;
						} else {
							flag = false;
							int i1 = k4 + 1;
							for (int i2 = i1 + 1; i2 < k;)
								recomposeargs.source[i1++] = recomposeargs.source[i2++];

							k--;
						}
					} else if (i4 != 0) {
						flag = true;
						k4++;
						int j1 = k;
						int j2 = ++k;
						while (k4 < j1)
							recomposeargs.source[--j2] = recomposeargs.source[--j1];
						recomposeargs.source[k4] = (char) i4;
						k4--;
					}
					if (k < recomposeargs.start) {
						int k1 = k;
						for (int k2 = recomposeargs.start; k2 < recomposeargs.limit;)
							recomposeargs.source[k1++] = recomposeargs.source[k2++];

						recomposeargs.start = k;
						recomposeargs.limit = k1;
					}
					if (recomposeargs.start == recomposeargs.limit)
						return (char) j4;
					if (k3 > 1)
						i3 = getCombiningIndexFromStarter((char) l3, (char) i4);
					else
						k4 = -1;
					continue;
				}
			j4 = nextcombiningargs.cc;
			if (recomposeargs.start == recomposeargs.limit)
				return (char) j4;
			if (nextcombiningargs.cc == 0)
				if ((l2 & 64) != 0) {
					if (nextcombiningargs.c2 == 0) {
						flag = false;
						k4 = recomposeargs.start - 1;
					} else {
						flag = false;
						k4 = recomposeargs.start - 2;
					}
					i3 = j3;
				} else {
					k4 = -1;
				}
		} while (true);
	}

	private static int findPreviousStarter(char ac[], int i, int j, int k,
			int l, char c) {
		PrevArgs prevargs = new PrevArgs();
		prevargs.src = ac;
		prevargs.start = i;
		prevargs.current = j;
		long l1;
		do {
			if (prevargs.start >= prevargs.current)
				break;
			l1 = getPrevNorm32(prevargs, c, k | l);
		} while (!isTrueStarter(l1, k, l));
		return prevargs.current;
	}

	private static int findNextStarter(char ac[], int i, int j, int k, int l,
			char c) {
		int j1 = 65280 | k;
		DecomposeArgs decomposeargs = new DecomposeArgs();
		char c2;
		for (; i != j; i += c2 != 0 ? 2 : 1) {
			char c1 = ac[i];
			if (c1 < c)
				break;
			long l1 = getNorm32(c1);
			if ((l1 & (long) j1) == 0L)
				break;
			if (isNorm32LeadSurrogate(l1)) {
				if (i + 1 == j || !Character.isLowSurrogate(c2 = ac[i + 1]))
					break;
				l1 = getNorm32FromSurrogatePair(l1, c2);
				if ((l1 & (long) j1) == 0L)
					break;
			} else {
				c2 = '\0';
			}
			if ((l1 & (long) l) == 0L)
				continue;
			int i1 = decompose(l1, l, decomposeargs);
			if (decomposeargs.cc == 0
					&& (getNorm32(extraData, i1, k) & (long) k) == 0L)
				break;
		}

		return i;
	}

	private static char[] composePart(ComposePartArgs composepartargs, int i,
			char ac[], int j, int k, int l, int i1) {
		boolean flag = (l & 34) != 0;
		int ai[] = new int[1];
		char ac1[] = new char[(k - i) * 20];
		do {
			composepartargs.length = decompose(ac, i, j, ac1, 0, ac1.length,
					flag, ai, i1);
			if (composepartargs.length <= ac1.length)
				break;
			ac1 = new char[composepartargs.length];
		} while (true);
		int j1 = composepartargs.length;
		if (composepartargs.length >= 2) {
			RecomposeArgs recomposeargs = new RecomposeArgs();
			recomposeargs.source = ac1;
			recomposeargs.start = 0;
			recomposeargs.limit = j1;
			composepartargs.prevCC = recompose(recomposeargs, i1);
			j1 = recomposeargs.limit;
		}
		composepartargs.length = j1;
		return ac1;
	}

	private static boolean composeHangul(char c, char c1, long l, char ac[],
			int ai[], int i, boolean flag, char ac1[], int j, int k) {
		int i1 = ai[0];
		if (isJamoVTNorm32JamoV(l)) {
			c -= '\u1100';
			if (c < '\023') {
				c1 = (char) (44032 + (c * 21 + (c1 - 4449)) * 28);
				if (i1 != i) {
					char c2 = ac[i1];
					char c3;
					if ((c3 = (char) (c2 - 4519)) < '\034') {
						i1++;
						c1 += c3;
					} else if (flag) {
						l = getNorm32(c2);
						if (isNorm32Regular(l) && (l & 8L) != 0L) {
							DecomposeArgs decomposeargs = new DecomposeArgs();
							int j1 = decompose(l, 8, decomposeargs);
							char c4;
							if (decomposeargs.length == 1
									&& (c4 = (char) (extraData[j1] - 4519)) < '\034') {
								i1++;
								c1 += c4;
							}
						}
					}
				}
				if (nx_contains(k, c1)) {
					if (!isHangulWithoutJamoT(c1))
						i1--;
					return false;
				} else {
					ac1[j] = c1;
					ai[0] = i1;
					return true;
				}
			}
		} else if (isHangulWithoutJamoT(c)) {
			c1 = (char) (c + (c1 - 4519));
			if (nx_contains(k, c1)) {
				return false;
			} else {
				ac1[j] = c1;
				ai[0] = i1;
				return true;
			}
		}
		return false;
	}

	static int compose(char ac[], int i, int j, char ac1[], int k, int l,
			boolean flag, int i1) {
		int ai[] = new int[1];
		int j2;
		char c2;
		if (!flag) {
			c2 = (char) indexes[6];
			j2 = 17;
		} else {
			c2 = (char) indexes[7];
			j2 = 34;
		}
		int k1 = i;
		int i2 = 65280 | j2;
		int k2 = 0;
		int k3 = 0;
		long l1 = 0L;
		char c = '\0';
		do {
			int j1 = i;
			for (; i != j
					&& ((c = ac[i]) < c2 || ((l1 = getNorm32(c)) & (long) i2) == 0L); i++)
				k3 = 0;

			if (i != j1) {
				int l2 = i - j1;
				if (k + l2 <= l)
					System.arraycopy(ac, j1, ac1, k, l2);
				k += l2;
				k2 = k;
				k1 = i - 1;
				if (Character.isLowSurrogate(ac[k1]) && j1 < k1
						&& Character.isHighSurrogate(ac[k1 - 1]))
					k1--;
				j1 = i;
			}
			if (i == j)
				break;
			i++;
			int i3;
			char c1;
			int j3;
			if (isNorm32HangulOrJamo(l1)) {
				k3 = j3 = 0;
				k2 = k;
				ai[0] = i;
				if (k > 0
						&& composeHangul(ac[j1 - 1], c, l1, ac, ai, j, flag,
								ac1, k > l ? 0 : k - 1, i1)) {
					i = ai[0];
					k1 = i;
					continue;
				}
				i = ai[0];
				c1 = '\0';
				i3 = 1;
				k1 = j1;
			} else {
				if (isNorm32Regular(l1)) {
					c1 = '\0';
					i3 = 1;
				} else if (i != j && Character.isLowSurrogate(c1 = ac[i])) {
					i++;
					i3 = 2;
					l1 = getNorm32FromSurrogatePair(l1, c1);
				} else {
					c1 = '\0';
					i3 = 1;
					l1 = 0L;
				}
				ComposePartArgs composepartargs = new ComposePartArgs();
				if (nx_contains(i1, c, c1))
					j3 = 0;
				else if ((l1 & (long) j2) == 0L) {
					j3 = (int) (255L & l1 >> 8);
				} else {
					int i4 = j2 << 2 & 15;
					if (isTrueStarter(l1, 65280 | j2, i4))
						k1 = j1;
					else
						k -= j1 - k1;
					i = findNextStarter(ac, i, j, j2, i4, c2);
					composepartargs.prevCC = k3;
					composepartargs.length = i3;
					char ac2[] = composePart(composepartargs, k1, ac, i, j, j2,
							i1);
					if (ac2 == null)
						break;
					k3 = composepartargs.prevCC;
					i3 = composepartargs.length;
					if (k + composepartargs.length <= l) {
						for (int j4 = 0; j4 < composepartargs.length;) {
							ac1[k++] = ac2[j4++];
							i3--;
						}

					} else {
						k += i3;
					}
					k1 = i;
					continue;
				}
			}
			if (k + i3 <= l) {
				if (j3 != 0 && j3 < k3) {
					int l3 = k;
					k += i3;
					k3 = insertOrdered(ac1, k2, l3, k, c, c1, j3);
				} else {
					ac1[k++] = c;
					if (c1 != 0)
						ac1[k++] = c1;
					k3 = j3;
				}
			} else {
				k += i3;
				k3 = j3;
			}
		} while (true);
		return k;
	}

	public static int getCombiningClass(int i) {
		long l = getNorm32(i);
		return (char) (int) (l >> 8 & 255L);
	}

	static int getDecompose(int ai[], String as[]) {
		DecomposeArgs decomposeargs = new DecomposeArgs();
		boolean flag = false;
		long l = 0L;
		int i = -1;
		boolean flag1 = false;
		int k = 0;
		do {
			if (++i >= 195102)
				break;
			if (i == 12543)
				i = 63744;
			else if (i == 65536)
				i = 119134;
			else if (i == 119233)
				i = 194560;
			long l1 = getNorm32(i);
			if ((l1 & 4L) != 0L && k < ai.length) {
				ai[k] = i;
				int j = decompose(l1, decomposeargs);
				as[k++] = new String(extraData, j, decomposeargs.length);
			}
		} while (true);
		return k;
	}

	private static int strCompare(char ac[], int i, int j, char ac1[], int k,
			int l, boolean flag) {
		int i1 = i;
		int j1 = k;
		int i2 = j - i;
		int j2 = l - k;
		int k1;
		byte byte0;
		if (i2 < j2) {
			byte0 = -1;
			k1 = i1 + i2;
		} else if (i2 == j2) {
			byte0 = 0;
			k1 = i1 + i2;
		} else {
			byte0 = 1;
			k1 = i1 + j2;
		}
		if (ac == ac1)
			return byte0;
		char c;
		char c1;
		do {
			if (i == k1)
				return byte0;
			c = ac[i];
			c1 = ac1[k];
			if (c != c1)
				break;
			i++;
			k++;
		} while (true);
		k1 = i1 + i2;
		int l1 = j1 + j2;
		if (c >= '\uD800' && c1 >= '\uD800' && flag) {
			if ((c > '\uDBFF' || i + 1 == k1 || !Character
					.isLowSurrogate(ac[i + 1]))
					&& (!Character.isLowSurrogate(c) || i1 == i || !Character
							.isHighSurrogate(ac[i - 1])))
				c -= '\u2800';
			if ((c1 > '\uDBFF' || k + 1 == l1 || !Character
					.isLowSurrogate(ac1[k + 1]))
					&& (!Character.isLowSurrogate(c1) || j1 == k || !Character
							.isHighSurrogate(ac1[k - 1])))
				c1 -= '\u2800';
		}
		return c - c1;
	}

	private static final boolean nx_contains(int i, int j) {
		if (i != 1)
			return false;
		else
			return j >= 44032 && j <= 55203;
	}

	private static final boolean nx_contains(int i, char c, char c1) {
		if (i != 1)
			return false;
		else
			return c1 == 0 && c >= '\uAC00' && c <= '\uD7A3';
	}

	static final NormalizerImpl IMPL;

	static final int UNSIGNED_BYTE_MASK = 255;

	static final long UNSIGNED_INT_MASK = 4294967295L;

	private static final String DATA_FILE_NAME = "resources/unorm.icu";

	static final int QC_NFC = 17;

	static final int QC_NFKC = 34;

	static final int QC_NFD = 4;

	static final int QC_NFKD = 8;

	static final int QC_MASK = 63;

	private static final int QC_ANY_NO = 15;

	private static final int COMBINES_FWD = 64;

	private static final int COMBINES_BACK = 128;

	private static final int COMBINES_ANY = 192;

	private static final int CC_SHIFT = 8;

	static final int CC_MASK = 65280;

	private static final int EXTRA_SHIFT = 16;

	private static final long MIN_SPECIAL = 4227858432L;

	private static final long SURROGATES_TOP = 4293918720L;

	private static final long MIN_HANGUL = 4293918720L;

	private static final long JAMO_V_TOP = 4294115328L;

	private static final int INDEX_TRIE_SIZE = 0;

	private static final int INDEX_CHAR_COUNT = 1;

	private static final int INDEX_COMBINE_DATA_COUNT = 2;

	private static final int INDEX_COMBINE_FWD_COUNT = 3;

	private static final int INDEX_COMBINE_BOTH_COUNT = 4;

	private static final int INDEX_COMBINE_BACK_COUNT = 5;

	static final int INDEX_MIN_NFC_NO_MAYBE = 6;

	static final int INDEX_MIN_NFKC_NO_MAYBE = 7;

	static final int INDEX_MIN_NFD_NO_MAYBE = 8;

	static final int INDEX_MIN_NFKD_NO_MAYBE = 9;

	private static final int INDEX_FCD_TRIE_SIZE = 10;

	private static final int INDEX_AUX_TRIE_SIZE = 11;

	private static final int INDEX_CANON_SET_COUNT = 12;

	private static final int INDEX_TOP = 32;

	private static final int MAX_BUFFER_SIZE = 20;

	private static FCDTrieImpl fcdTrieImpl;

	private static NormTrieImpl normTrieImpl;

	private static int indexes[];

	private static char combiningTable[];

	private static char extraData[];

	private static boolean isDataLoaded;

	private static final int DATA_BUFFER_SIZE = 25000;

	static final int MIN_WITH_LEAD_CC = 768;

	private static final int DECOMP_FLAG_LENGTH_HAS_CC = 128;

	private static final int DECOMP_LENGTH_MASK = 127;

	private static final int BMP_INDEX_LENGTH = 2048;

	private static final int SURROGATE_BLOCK_BITS = 5;

	private static final int JAMO_L_BASE = 4352;

	private static final int JAMO_V_BASE = 4449;

	private static final int JAMO_T_BASE = 4519;

	private static final int HANGUL_BASE = 44032;

	private static final int JAMO_L_COUNT = 19;

	private static final int JAMO_V_COUNT = 21;

	private static final int JAMO_T_COUNT = 28;

	private static final int HANGUL_COUNT = 11172;

	static {
		try {
			IMPL = new NormalizerImpl();
		} catch (Exception exception) {
			RuntimeException runtimeexception = new RuntimeException(exception
					.getMessage());
			runtimeexception.initCause(exception);
			throw runtimeexception;
		}
	}

}


/*
	DECOMPILATION REPORT

	Decompiled from: C:\opt\java5-ibm-2007-12-13\jre\lib\core.jar
	Total time: 125 ms
	Jad reported messages/errors:
	Exit status: 0
	Caught exceptions:
*/

