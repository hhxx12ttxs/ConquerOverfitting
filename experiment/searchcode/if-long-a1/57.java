// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 

package com.nnee.p_p.p_a;

import com.nnee.p_n.p_a.p_a.cls_e;
import com.nnee.p_n.p_a.p_a.cls_f;
import com.nnee.p_n.p_a.p_a.cls_g;
import com.nnee.p_n.p_a.p_a.cls_i; 
import java.io.UnsupportedEncodingException;
import java.math.BigInteger;

public class cls_a
    implements cls_e, cls_g
{

    public cls_a()
    {
        this(76, b);
    }

    public cls_a(int i1)
    {
        this(i1, b);
    }

    public cls_a(int i1, byte abyte0[])
    {
        h = i1;
        i = new byte[abyte0.length];
        System.arraycopy(abyte0, 0, i, 0, abyte0.length);
        if(i1 > 0)
            k = 4 + abyte0.length;
        else
            k = 4;
        j = -1 + k;
        if(j(abyte0))
        {
            String s;
            try
            {
                s = new String(abyte0, "UTF-8");
            }
            catch(UnsupportedEncodingException unsupportedencodingexception)
            {
                s = new String(abyte0);
            }
            throw new IllegalArgumentException((new StringBuilder()).append("lineSeperator must not contain base64 characters: [").append(s).append("]").toString());
        } else
        {
            return;
        }
    }

    public static boolean a(byte byte0)
    {
        boolean flag;
        if(byte0 == 61 || byte0 >= 0 && byte0 < e.length && e[byte0] != -1)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public static byte[] a(BigInteger biginteger)
    {
        if(biginteger == null)
            throw new NullPointerException("encodeInteger called with null parameter");
        else
            return a(b(biginteger), false);
    }

    public static byte[] a(byte abyte0[], boolean flag)
    {
        if(abyte0 != null && abyte0.length != 0)
        {
            cls_a a1;
            long l1;
            long l2;
            if(flag)
                a1 = new cls_a();
            else
                a1 = new cls_a(0);
            l1 = (4 * abyte0.length) / 3;
            l2 = l1 % 4L;
            if(l2 != 0L)
                l1 += 4L - l2;
            if(flag)
                l1 += (((l1 + 76L) - 1L) / 76L) * (long)b.length;
            if(l1 > 0x7fffffffL)
                throw new IllegalArgumentException("Input array too big, output array would be bigger than Integer.MAX_VALUE=2147483647");
            byte abyte1[] = new byte[(int)l1];
            a1.b(abyte1, 0, abyte1.length);
            a1.c(abyte0, 0, abyte0.length);
            a1.c(abyte0, 0, -1);
            if(a1.l != abyte1)
                a1.a(abyte1, 0, abyte1.length);
            abyte0 = abyte1;
        }
        return abyte0;
    }

    private static boolean b(byte byte0)
    {
    	boolean flag = false;
        switch(byte0)
        {
        
        case 9:
        case 10:
        case 13:
        case 32:
        	flag = true;
        	break;
        	
        default:
        	break;
        }
        
        return flag;

    }

    public static boolean b(byte abyte0[])
    {
        boolean flag;
        int i1;
        flag = false;
        i1 = ((flag) ? 1 : 0);
while(true)
{
		
        if(i1 >= abyte0.length)
            return false;
        if(a(abyte0[i1]) || b(abyte0[i1]))
        	{flag = true;
        	return flag;
        	}
        i1++;
}
    }

    static byte[] b(BigInteger biginteger)
    {
        int i1 = (7 + biginteger.bitLength() >> 3) << 3;
        byte abyte0[] = biginteger.toByteArray();
        byte abyte2[];
        if(biginteger.bitLength() % 8 != 0 && 1 + biginteger.bitLength() / 8 == i1 / 8)
        {
            abyte2 = abyte0;
        } else
        {
            int j1 = 0;
            int k1 = abyte0.length;
            if(biginteger.bitLength() % 8 == 0)
            {
                j1 = 1;
                k1--;
            }
            int l1 = i1 / 8 - k1;
            byte abyte1[] = new byte[i1 / 8];
            System.arraycopy(abyte0, j1, abyte1, l1, k1);
            abyte2 = abyte1;
        }
        return abyte2;
    }

    private void c()
    {
        if(l == null)
        {
            l = new byte[8192];
            m = 0;
            n = 0;
        } else
        {
            byte abyte0[] = new byte[2 * l.length];
            System.arraycopy(l, 0, abyte0, 0, l.length);
            l = abyte0;
        }
    }

    public static byte[] c(byte abyte0[])
    {
        return a(abyte0, false);
    }

    public static byte[] d(byte abyte0[])
    {
        return a(abyte0, true);
    }

    public static byte[] e(byte abyte0[])
    {
        if(abyte0 != null && abyte0.length != 0)
        {
            cls_a a1 = new cls_a();
            byte abyte1[] = new byte[(int)(long)((3 * abyte0.length) / 4)];
            a1.b(abyte1, 0, abyte1.length);
            a1.d(abyte0, 0, abyte0.length);
            a1.d(abyte0, 0, -1);
            abyte0 = new byte[a1.m];
            a1.a(abyte0, 0, abyte0.length);
        }
        return abyte0;
    }

    static byte[] f(byte abyte0[])
    {
        byte abyte1[] = new byte[abyte0.length];
        int i1 = 0;
        int j1 = 0;
        do
            if(i1 < abyte0.length)
            {
                switch(abyte0[i1])
                {
               

                case 9: // '\t'
                case 10: // '\n'
                case 13: // '\r'
                case 32: // ' '
                    i1++;
                    break;
                default:
                    int k1 = j1 + 1;
                    abyte1[j1] = abyte0[i1];
                    j1 = k1;
                    // fall through
                    break;
                }
            } else
            {
                byte abyte2[] = new byte[j1];
                System.arraycopy(abyte1, 0, abyte2, 0, j1);
                return abyte2;
            }
        while(true);
    }

    static byte[] g(byte abyte0[])
    {
        byte abyte1[] = new byte[abyte0.length];
        int i1 = 0;
        int j1 = 0;
        for(; i1 < abyte0.length; i1++)
            if(a(abyte0[i1]))
            {
                int k1 = j1 + 1;
                abyte1[j1] = abyte0[i1];
                j1 = k1;
            }

        byte abyte2[] = new byte[j1];
        System.arraycopy(abyte1, 0, abyte2, 0, j1);
        return abyte2;
    }

    public static BigInteger i(byte abyte0[])
    {
        return new BigInteger(1, e(abyte0));
    }

    private static boolean j(byte abyte0[])
    {
        boolean flag = false;
        int i1 = ((flag) ? 1 : 0);
        do
        {
label0:
            {
                if(i1 < abyte0.length)
                {
                    if(!a(abyte0[i1]))
                        break label0;
                    flag = true;
                }
                return flag;
            }
            i1++;
        } while(true);
    }

    int a(byte abyte0[], int i1, int j1)
    {
        int k1;
        if(l != null)
        {
            k1 = Math.min(b(), j1);
            if(l != abyte0)
            {
                System.arraycopy(l, n, abyte0, i1, k1);
                n = k1 + n;
                if(n >= m)
                    l = null;
            } else
            {
                l = null;
            }
        } else
        if(q)
            k1 = -1;
        else
            k1 = 0;
        return k1;
    }

    public Object a(Object obj) throws cls_i
    {
        if(!(obj instanceof byte[]))
            throw new cls_i("Parameter supplied to Base64 decode is not a byte[]");
        else
            return a((byte[])(byte[])obj);
    }

    boolean a()
    {
        boolean flag;
        if(l != null)
            flag = true;
        else
            flag = false;
        return flag;
    }

    public byte[] a(byte abyte0[])
    {
        return e(abyte0);
    }

    int b()
    {
        int i1;
        if(l != null)
            i1 = m - n;
        else
            i1 = 0;
        return i1;
    }

    public Object b(Object obj) throws cls_f
    {
        if(!(obj instanceof byte[]))
            throw new cls_f("Parameter supplied to Base64 encode is not a byte[]");
        else
            return h((byte[])(byte[])obj);
    }

    void b(byte abyte0[], int i1, int j1)
    {
        if(abyte0 != null && abyte0.length == j1)
        {
            l = abyte0;
            m = i1;
            n = i1;
        }
    }

    void c(byte abyte0[], int i1, int j1)
    {
        if(!q){
        	 if(j1 >= 0) {
        		 int k1 = 0;
        	        while(k1 < j1) 
        	        {
        	            if(l == null || l.length - m < k)
        	                c();
        	            int l1 = 1 + p;
        	            p = l1;
        	            p = l1 % 3;
        	            int i2 = i1 + 1;
        	            int j2 = abyte0[i1];
        	            if(j2 < 0)
        	                j2 += 256;
        	            r = j2 + (r << 8);
        	            if(p == 0)
        	            {
        	                byte abyte1[] = l;
        	                int k2 = m;
        	                m = k2 + 1;
        	                abyte1[k2] = c[0x3f & r >> 18];
        	                byte abyte2[] = l;
        	                int l2 = m;
        	                m = l2 + 1;
        	                abyte2[l2] = c[0x3f & r >> 12];
        	                byte abyte3[] = l;
        	                int i3 = m;
        	                m = i3 + 1;
        	                abyte3[i3] = c[0x3f & r >> 6];
        	                byte abyte4[] = l;
        	                int j3 = m;
        	                m = j3 + 1;
        	                abyte4[j3] = c[0x3f & r];
        	                o = 4 + o;
        	                if(h > 0 && h <= o)
        	                {
        	                    System.arraycopy(i, 0, l, m, i.length);
        	                    m = m + i.length;
        	                    o = 0;
        	                }
        	            }
        	            k1++;
        	            i1 = i2;
        	        }
        	 }else {
        	        q = true;
        	        if(l == null || l.length - m < k)
        	            c();
        	        switch(p)
        	        {
        	        case 1:
        	        	 byte abyte9[] = l;
        	             int k4 = m;
        	             m = k4 + 1;
        	             abyte9[k4] = c[0x3f & r >> 2];
        	             byte abyte10[] = l;
        	             int l4 = m;
        	             m = l4 + 1;
        	             abyte10[l4] = c[0x3f & r << 4];
        	             byte abyte11[] = l;
        	             int i5 = m;
        	             m = i5 + 1;
        	             abyte11[i5] = 61;
        	             byte abyte12[] = l;
        	             int j5 = m;
        	             m = j5 + 1;
        	             abyte12[j5] = 61;
        	        	break;
        	        case 2:
        	        	byte abyte5[] = l;
        	            int k3 = m;
        	            m = k3 + 1;
        	            abyte5[k3] = c[0x3f & r >> 10];
        	            byte abyte6[] = l;
        	            int l3 = m;
        	            m = l3 + 1;
        	            abyte6[l3] = c[0x3f & r >> 4];
        	            byte abyte7[] = l;
        	            int i4 = m;
        	            m = i4 + 1;
        	            abyte7[i4] = c[0x3f & r << 2];
        	            byte abyte8[] = l;
        	            int j4 = m;
        	            m = j4 + 1;
        	            abyte8[j4] = 61;
        	        	break;
        	        default:
        	        	
        	        	break;
        	        
        	        }
        	        if(h > 0)
    	            {
    	                System.arraycopy(i, 0, l, m, i.length);
    	                m = m + i.length;
    	            }
        	 }
        }
 
        return;
 
    }

    void d(byte abyte0[], int i1, int j1)
    {
        if(!q) {
        	 int k1;
             if(j1 < 0)
                 q = true;
             k1 = 0;
             if(k1 >= j1) {
            	 return;
             } else {
            	 int l1;
                 byte byte0;
                 if(l == null || l.length - m < j)
                     c();
                 l1 = i1 + 1;
                 byte0 = abyte0[i1];
                 if(byte0 != 61)
                     return;
                 r = r << 6;
                 switch(p)
                 {
                 case 2:
                	 r = r << 6;
                     byte abyte6[] = l;
                     int k3 = m;
                     m = k3 + 1;
                     abyte6[k3] = (byte)(0xff & r >> 16);
                	 break;
                 case 3:
                	 byte abyte4[] = l;
                     int i3 = m;
                     m = i3 + 1;
                     abyte4[i3] = (byte)(0xff & r >> 16);
                     byte abyte5[] = l;
                     int j3 = m;
                     m = j3 + 1;
                     abyte5[j3] = (byte)(0xff & r >> 8);
                	 break;
                 default:
                	 break;
                 
                 }
                 
                 q = true;
                 
                 if(byte0 >= 0 && byte0 < e.length)
                 {
                     byte byte1 = e[byte0];
                     if(byte1 >= 0)
                     {
                         int i2 = 1 + p;
                         p = i2;
                         p = i2 % 4;
                         r = byte1 + (r << 6);
                         if(p == 0)
                         {
                             byte abyte1[] = l;
                             int j2 = m;
                             m = j2 + 1;
                             abyte1[j2] = (byte)(0xff & r >> 16);
                             byte abyte2[] = l;
                             int k2 = m;
                             m = k2 + 1;
                             abyte2[k2] = (byte)(0xff & r >> 8);
                             byte abyte3[] = l;
                             int l2 = m;
                             m = l2 + 1;
                             abyte3[l2] = (byte)(0xff & r);
                         }
                     }
                 }
                 k1++;
                 i1 = l1;
             }
        }
 
    }

    public byte[] h(byte abyte0[])
    {
        return a(abyte0, false);
    }

    static final int a = 76;
    static final byte b[];
    private static final byte c[];
    private static final byte d = 61;
    private static final byte e[];
    private static final int f = 63;
    private static final int g = 255;
    private final int h;
    private final byte i[];
    private final int j;
    private final int k;
    private byte l[];
    private int m;
    private int n;
    private int o;
    private int p;
    private boolean q;
    private int r;

    static 
    {
        byte abyte0[] = new byte[2];
        abyte0[0] = 13;
        abyte0[1] = 10;
        b = abyte0;
        byte abyte1[] = new byte[64];
        abyte1[0] = 65;
        abyte1[1] = 66;
        abyte1[2] = 67;
        abyte1[3] = 68;
        abyte1[4] = 69;
        abyte1[5] = 70;
        abyte1[6] = 71;
        abyte1[7] = 72;
        abyte1[8] = 73;
        abyte1[9] = 74;
        abyte1[10] = 75;
        abyte1[11] = 76;
        abyte1[12] = 77;
        abyte1[13] = 78;
        abyte1[14] = 79;
        abyte1[15] = 80;
        abyte1[16] = 81;
        abyte1[17] = 82;
        abyte1[18] = 83;
        abyte1[19] = 84;
        abyte1[20] = 85;
        abyte1[21] = 86;
        abyte1[22] = 87;
        abyte1[23] = 88;
        abyte1[24] = 89;
        abyte1[25] = 90;
        abyte1[26] = 97;
        abyte1[27] = 98;
        abyte1[28] = 99;
        abyte1[29] = 100;
        abyte1[30] = 101;
        abyte1[31] = 102;
        abyte1[32] = 103;
        abyte1[33] = 104;
        abyte1[34] = 105;
        abyte1[35] = 106;
        abyte1[36] = 107;
        abyte1[37] = 108;
        abyte1[38] = 109;
        abyte1[39] = 110;
        abyte1[40] = 111;
        abyte1[41] = 112;
        abyte1[42] = 113;
        abyte1[43] = 114;
        abyte1[44] = 115;
        abyte1[45] = 116;
        abyte1[46] = 117;
        abyte1[47] = 118;
        abyte1[48] = 119;
        abyte1[49] = 120;
        abyte1[50] = 121;
        abyte1[51] = 122;
        abyte1[52] = 48;
        abyte1[53] = 49;
        abyte1[54] = 50;
        abyte1[55] = 51;
        abyte1[56] = 52;
        abyte1[57] = 53;
        abyte1[58] = 54;
        abyte1[59] = 55;
        abyte1[60] = 56;
        abyte1[61] = 57;
        abyte1[62] = 43;
        abyte1[63] = 47;
        c = abyte1;
        byte abyte2[] = new byte[123];
        abyte2[0] = -1;
        abyte2[1] = -1;
        abyte2[2] = -1;
        abyte2[3] = -1;
        abyte2[4] = -1;
        abyte2[5] = -1;
        abyte2[6] = -1;
        abyte2[7] = -1;
        abyte2[8] = -1;
        abyte2[9] = -1;
        abyte2[10] = -1;
        abyte2[11] = -1;
        abyte2[12] = -1;
        abyte2[13] = -1;
        abyte2[14] = -1;
        abyte2[15] = -1;
        abyte2[16] = -1;
        abyte2[17] = -1;
        abyte2[18] = -1;
        abyte2[19] = -1;
        abyte2[20] = -1;
        abyte2[21] = -1;
        abyte2[22] = -1;
        abyte2[23] = -1;
        abyte2[24] = -1;
        abyte2[25] = -1;
        abyte2[26] = -1;
        abyte2[27] = -1;
        abyte2[28] = -1;
        abyte2[29] = -1;
        abyte2[30] = -1;
        abyte2[31] = -1;
        abyte2[32] = -1;
        abyte2[33] = -1;
        abyte2[34] = -1;
        abyte2[35] = -1;
        abyte2[36] = -1;
        abyte2[37] = -1;
        abyte2[38] = -1;
        abyte2[39] = -1;
        abyte2[40] = -1;
        abyte2[41] = -1;
        abyte2[42] = -1;
        abyte2[43] = 62;
        abyte2[44] = -1;
        abyte2[45] = -1;
        abyte2[46] = -1;
        abyte2[47] = 63;
        abyte2[48] = 52;
        abyte2[49] = 53;
        abyte2[50] = 54;
        abyte2[51] = 55;
        abyte2[52] = 56;
        abyte2[53] = 57;
        abyte2[54] = 58;
        abyte2[55] = 59;
        abyte2[56] = 60;
        abyte2[57] = 61;
        abyte2[58] = -1;
        abyte2[59] = -1;
        abyte2[60] = -1;
        abyte2[61] = -1;
        abyte2[62] = -1;
        abyte2[63] = -1;
        abyte2[64] = -1;
        abyte2[65] = 0;
        abyte2[66] = 1;
        abyte2[67] = 2;
        abyte2[68] = 3;
        abyte2[69] = 4;
        abyte2[70] = 5;
        abyte2[71] = 6;
        abyte2[72] = 7;
        abyte2[73] = 8;
        abyte2[74] = 9;
        abyte2[75] = 10;
        abyte2[76] = 11;
        abyte2[77] = 12;
        abyte2[78] = 13;
        abyte2[79] = 14;
        abyte2[80] = 15;
        abyte2[81] = 16;
        abyte2[82] = 17;
        abyte2[83] = 18;
        abyte2[84] = 19;
        abyte2[85] = 20;
        abyte2[86] = 21;
        abyte2[87] = 22;
        abyte2[88] = 23;
        abyte2[89] = 24;
        abyte2[90] = 25;
        abyte2[91] = -1;
        abyte2[92] = -1;
        abyte2[93] = -1;
        abyte2[94] = -1;
        abyte2[95] = -1;
        abyte2[96] = -1;
        abyte2[97] = 26;
        abyte2[98] = 27;
        abyte2[99] = 28;
        abyte2[100] = 29;
        abyte2[101] = 30;
        abyte2[102] = 31;
        abyte2[103] = 32;
        abyte2[104] = 33;
        abyte2[105] = 34;
        abyte2[106] = 35;
        abyte2[107] = 36;
        abyte2[108] = 37;
        abyte2[109] = 38;
        abyte2[110] = 39;
        abyte2[111] = 40;
        abyte2[112] = 41;
        abyte2[113] = 42;
        abyte2[114] = 43;
        abyte2[115] = 44;
        abyte2[116] = 45;
        abyte2[117] = 46;
        abyte2[118] = 47;
        abyte2[119] = 48;
        abyte2[120] = 49;
        abyte2[121] = 50;
        abyte2[122] = 51;
        e = abyte2;
    }
}

