package utils;

import java.io.DataInput;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.StringTokenizer;

public class Utils {
	public static String form(int a) {
		String result = "0x"
				+ (Integer.toHexString(a).length() == 1
						|| Integer.toHexString(a).length() == 3 ? "0"
						+ Integer.toHexString(a) : Integer.toHexString(a));
		return result;
	}

	public static String last2byte(int a) {
		String result = "";
		result = (Integer.toHexString(a).length() == 1
				|| Integer.toHexString(a).length() == 3 ? "0"
				+ Integer.toHexString(a) : Integer.toHexString(a));
		result = result.substring(result.length() - 2);
		return result;
	}

	public static String first2byte(int a) {
		String result = "";
		result = Integer.toHexString(a).length() == 1 ? "000"
				+ Integer.toHexString(a)
				: Integer.toHexString(a).length() == 3 ? "0"
						+ Integer.toHexString(a) : Integer.toHexString(a)
						.length() == 2 ? "00" + Integer.toHexString(a)
						: Integer.toHexString(a);
		result = result.substring(0, 2);
		return result;
	}

	public static String getUTF(byte[] ab) {
		String str = "";
		try {
			str += new String(ab, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	public static String getMUTF(byte[] ab) {
		String str = "";
		try {
			str += new String(ab, "UTF-16");
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}

	public static byte[] readBytes(int len, DataInput di) {
		// TODO Auto-generated method stub
		byte[] result = new byte[len];
		for (int i = 0; i < len; i++) {
			int info;
			try {
				info = di.readUnsignedByte();
				result[i] = (byte) info;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return result;
			}
		}
		return result;
	}

	public static byte[] readBytesNull(DataInput di) throws IOException {
		// TODO Auto-generated method stub
		byte[] sresult = new byte[99999];
		int cnt = 0;
		for (int i = 0; i < 99999; i++) {
			int info;
			info = di.readUnsignedByte();
			cnt++;
			if (info == 0) {
				break;
			}
			sresult[i] = (byte) info;
		}
		byte[] result = new byte[cnt];
		for (int i = 0; i < cnt; i++) {
			result[i] = sresult[i];
		}
		return result;
	}

	public static int endianInt(int ival) {
		byte[] buf = breakInt2Byte(ival);
		buf = reverseArr(buf);
		int result = convByte2Int(buf);
		return result;
	}

	public static int endianShort(int ival) {
		byte[] buf = breakShort2Byte(ival);
		buf = reverseArr(buf);
		int result = convByte2Int(buf);
		return result;
	}

	public static byte[] reverseArr(byte[] ba) {
		byte[] result = new byte[ba.length];
		for (int i = 0; i < ba.length; i++) {
			result[ba.length - i - 1] = ba[i];
		}

		return result;
	}

	public static int convByte2Int(byte[] ba) {
		byte[] buf = new byte[4];
		if (ba.length < 4) {
			for (int i = 0; i < 4; i++) {
				buf[i] = (buf.length - ba.length - i > 0) ? 0 : ba[i
						- (buf.length - ba.length)];
			}
		} else {
			for (int i = 0; i < 4; i++) {
				buf[i] = ba[i];
			}
		}
		ByteBuffer wrapper = ByteBuffer.wrap(buf);
		int result = wrapper.getInt();
		return result;
	}

	public static int convULEBint(byte[] ba, int hm) {
		if(hm==5){
			System.out.println(5);
		}
		String[] sb = new String[hm];
		for (int i = 0; i < hm; i++) {
			sb[i] = getBinary(ba[i]);
			sb[i] = sb[i].substring(1);
		}
		String sresult = "";
		for (int i = 0; i < hm; i++) {
			sresult = sb[i] + sresult;
		}
		int result = Integer.parseInt(sresult, 2);
		return result;
	}

	public static int convSLEBint(byte[] ba, int hm, boolean sign) {
		String[] sb = new String[hm];
		for (int i = 0; i < hm; i++) {
			sb[i] = getBinary(ba[i]);
			if (i == hm - 1) {
				sb[i] = sb[i].substring(2);
			} else {
				sb[i] = sb[i].substring(1);
			}
		}
		String sresult = "";
		for (int i = 0; i < hm; i++) {
			sresult = sb[i] + sresult;
		}
		if (!sign) {
			sresult = sresult.replace("0", "3");
			sresult = sresult.replace("1", "0");
			sresult = sresult.replace("3", "1");
			char[] ca = sresult.toCharArray();
			for (int i = sresult.length() - 1; i > 0; i--) {
				if (ca[i] == '1') {
					ca[i] = '0';
				} else {
					ca[i] = '1';
					break;
				}
			}
			sresult = "";
			for (int i = ca.length - 1; i >= 0; i--) {
				sresult = String.valueOf(ca[i]) + sresult;
			}
		}
		int result = Integer.parseInt(sresult, 2);
		if (!sign)
			result = result * (-1);
		return result;
	}

	public static String getBinary(byte bv) {
		String result = "";
		int trimval = 0;

		for (int i = 7; i >= 0; i--) {
			trimval = bv - (int) Math.pow(2, i);
			if (trimval < 0) {
				result += "0";
				trimval = bv;
			} else {
				result += "1";
				bv = (byte) trimval;
			}
		}
		return result;
	}

	public static String getBinary(int iv) {
		String result = "";
		int trimval = 0;

		for (int i = 7; i >= 0; i--) {
			trimval = iv - (int) Math.pow(2, i);
			if (trimval < 0) {
				result += "0";
				trimval = iv;
			} else {
				result += "1";
				iv = (byte) trimval;
			}
		}
		return result;
	}

	public static byte[] breakInt2Byte(int ival) {
		return new byte[] { (byte) (ival >> 24), (byte) (ival >> 16),
				(byte) (ival >> 8), (byte) ival };
	}

	public static byte[] breakShort2Byte(int ival) {
		return new byte[] { (byte) (ival >> 8), (byte) ival };
	}

	public static int[] readBytes2Int(int len, DataInput di) throws IOException {
		// TODO Auto-generated method stub
		int[] result = new int[len];
		for (int i = 0; i < len; i++) {
			int info;
			try {
				info = di.readUnsignedByte();
				result[i] = info;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				throw e;
			}
		}
		return result;
	}

	public static String accessFlag(int val) {
		String result = "";
		String af = Integer.toBinaryString(val);
		int idx = 0;
		for (int i = af.length() - 1; i >= 0; i--) {
			int f = Integer.parseInt(af.substring(i, i + 1));
			switch (f) {
			case 0:
				break;
			case 1:
				switch (idx) {
				case 0:
					result += "public ";
					break;
				case 1:
					result += "private ";
					break;
				case 2:
					result += "protected ";
					break;
				case 3:
					result += "static ";
					break;
				case 4:
					result += "final ";
					break;
				case 5:
					result += "synchronized ";
					break;
				case 6:
					result += "volatile ";
					break;
				case 8:
					result += "transient ";
					break;
				case 10:
					result += "native ";
					break;
				case 11:
					result += "interface ";
					break;
				case 12:
					result += "abstract ";
					break;
				case 13:
					result += "strictfp ";
					break;
				case 14:
					result += "not defined ";
					break;
				case 15:
					result += "annotation ";
					break;
				case 16:
					result += "enumerated ";
					break;
				case 18:
					result += "constructor ";
					break;
				case 19:
					result += "synchronized ";
					break;
				}
				break;
			}
			idx++;
		}
		return result;
	}

	public static void addURL(URI url) throws Exception {
		URLClassLoader classLoader = (URLClassLoader) ClassLoader
				.getSystemClassLoader();
		Class clazz = URLClassLoader.class;

		// Use reflection
		Method method = clazz.getDeclaredMethod("addURL",
				new Class[] { URL.class });
		method.setAccessible(true);
		method.invoke(classLoader, new Object[] { url.toURL() });
	}

	public static String getOPDesc(int opcode) {
		String result = "";
		switch (opcode) {
		case 0x0:
			result = "nop";
			break;
		case 0x1:
			result = "move vx,vy";
			break;
		case 0x2:
			result = "move/from16 vx,vy";
			break;
		case 0x3:
			result = "move/16";
			break;
		case 0x4:
			result = "move-wide";
			break;
		case 0x5:
			result = "move-wide/from16 vx,vy";
			break;
		case 0x6:
			result = "move-wide/16";
			break;
		case 0x7:
			result = "move-object vx,vy";
			break;
		case 0x8:
			result = "move-object/from16 vx,vy";
			break;
		case 0x9:
			result = "move-object/16";
			break;
		case 0x0A:
			result = "move-result vx";
			break;
		case 0x0B:
			result = "move-result-wide vx";
			break;
		case 0x0C:
			result = "move-result-object vx";
			break;
		case 0x0D:
			result = "move-exception vx";
			break;
		case 0x0E:
			result = "return-void";
			break;
		case 0x0F:
			result = "return vx";
			break;
		case 0x10:
			result = "return-wide vx";
			break;
		case 0x11:
			result = "return-object vx";
			break;
		case 0x12:
			result = "const/4 vx,lit4";
			break;
		case 0x13:
			result = "const/16 vx,lit16";
			break;
		case 0x14:
			result = "const vx, lit32";
			break;
		case 0x15:
			result = "const/high16 v0, lit16";
			break;
		case 0x16:
			result = "const-wide/16 vx, lit16";
			break;
		case 0x17:
			result = "const-wide/32 vx, lit32";
			break;
		case 0x18:
			result = "const-wide vx, lit64";
			break;
		case 0x19:
			result = "const-wide/high16 vx,lit16";
			break;
		case 0x1A:
			result = "const-string vx,string_id";
			break;
		case 0x1B:
			result = "const-string-jumbo";
			break;
		case 0x1C:
			result = "const-class vx,type_id";
			break;
		case 0x1D:
			result = "monitor-enter vx";
			break;
		case 0x1E:
			result = "monitor-exit";
			break;
		case 0x1F:
			result = "check-cast vx, type_id";
			break;
		case 0x20:
			result = "instance-of vx,vy,type_id";
			break;
		case 0x21:
			result = "array-length vx,vy";
			break;
		case 0x22:
			result = "new-instance vx,type";
			break;
		case 0x23:
			result = "new-array vx,vy,type_id";
			break;
		case 0x24:
			result = "filled-new-array {parameters},type_id";
			break;
		case 0x25:
			result = "filled-new-array-range {vx..vy},type_id";
			break;
		case 0x26:
			result = "fill-array-data vx,array_data_offset";
			break;
		case 0x27:
			result = "throw vx";
			break;
		case 0x28:
			result = "goto target";
			break;
		case 0x29:
			result = "goto/16 target";
			break;
		case 0x2A:
			result = "goto/32 target";
			break;
		case 0x2B:
			result = "packed-switch vx,table";
			break;
		case 0x2C:
			result = "sparse-switch vx,table";
			break;
		case 0x2D:
			result = "cmpl-float";
			break;
		case 0x2E:
			result = "cmpg-float vx, vy, vz";
			break;
		case 0x2F:
			result = "cmpl-double vx,vy,vz";
			break;
		case 0x30:
			result = "cmpg-double vx, vy, vz";
			break;
		case 0x31:
			result = "cmp-long vx, vy, vz";
			break;
		case 0x32:
			result = "if-eq vx,vy,target";
			break;
		case 0x33:
			result = "if-ne vx,vy,target";
			break;
		case 0x34:
			result = "if-lt vx,vy,target";
			break;
		case 0x35:
			result = "if-ge vx, vy,target";
			break;
		case 0x36:
			result = "if-gt vx,vy,target";
			break;
		case 0x37:
			result = "if-le vx,vy,target";
			break;
		case 0x38:
			result = "if-eqz vx,target";
			break;
		case 0x39:
			result = "if-nez vx,target";
			break;
		case 0x3A:
			result = "if-ltz vx,target";
			break;
		case 0x3B:
			result = "if-gez vx,target";
			break;
		case 0x3C:
			result = "if-gtz vx,target";
			break;
		case 0x3D:
			result = "if-lez vx,target";
			break;
		case 0x3E:
			result = "unused_3E";
			break;
		case 0x3F:
			result = "unused_3F";
			break;
		case 0x40:
			result = "unused_40";
			break;
		case 0x41:
			result = "unused_41";
			break;
		case 0x42:
			result = "unused_42";
			break;
		case 0x43:
			result = "unused_43";
			break;
		case 0x44:
			result = "aget vx,vy,vz";
			break;
		case 0x45:
			result = "aget-wide vx,vy,vz";
			break;
		case 0x46:
			result = "aget-object vx,vy,vz";
			break;
		case 0x47:
			result = "aget-boolean vx,vy,vz";
			break;
		case 0x48:
			result = "aget-byte vx,vy,vz";
			break;
		case 0x49:
			result = "aget-char vx, vy,vz";
			break;
		case 0x4A:
			result = "aget-short vx,vy,vz";
			break;
		case 0x4B:
			result = "aput vx,vy,vz";
			break;
		case 0x4C:
			result = "aput-wide vx,vy,vz";
			break;
		case 0x4D:
			result = "aput-object vx,vy,vz";
			break;
		case 0x4E:
			result = "aput-boolean vx,vy,vz";
			break;
		case 0x4F:
			result = "aput-byte vx,vy,vz";
			break;
		case 0x50:
			result = "aput-char vx,vy,vz";
			break;
		case 0x51:
			result = "aput-short vx,vy,vz";
			break;
		case 0x52:
			result = "iget vx, vy, field_id";
			break;
		case 0x53:
			result = "iget-wide vx,vy,field_id";
			break;
		case 0x54:
			result = "iget-object vx,vy,field_id";
			break;
		case 0x55:
			result = "iget-boolean vx,vy,field_id";
			break;
		case 0x56:
			result = "iget-byte vx,vy,field_id";
			break;
		case 0x57:
			result = "iget-char vx,vy,field_id";
			break;
		case 0x58:
			result = "iget-short vx,vy,field_id";
			break;
		case 0x59:
			result = "iput vx,vy, field_id";
			break;
		case 0x5A:
			result = "iput-wide vx,vy, field_id";
			break;
		case 0x5B:
			result = "iput-object vx,vy,field_id";
			break;
		case 0x5C:
			result = "iput-boolean vx,vy, field_id";
			break;
		case 0x5D:
			result = "iput-byte vx,vy,field_id";
			break;
		case 0x5E:
			result = "iput-char vx,vy,field_id";
			break;
		case 0x5F:
			result = "iput-short vx,vy,field_id";
			break;
		case 0x60:
			result = "sget vx,field_id";
			break;
		case 0x61:
			result = "sget-wide vx, field_id";
			break;
		case 0x62:
			result = "sget-object vx,field_id";
			break;
		case 0x63:
			result = "sget-boolean vx,field_id";
			break;
		case 0x64:
			result = "sget-byte vx,field_id";
			break;
		case 0x65:
			result = "sget-char vx,field_id";
			break;
		case 0x66:
			result = "sget-short vx,field_id";
			break;
		case 0x67:
			result = "sput vx, field_id";
			break;
		case 0x68:
			result = "sput-wide vx, field_id";
			break;
		case 0x69:
			result = "sput-object vx,field_id";
			break;
		case 0x6A:
			result = "sput-boolean vx,field_id";
			break;
		case 0x6B:
			result = "sput-byte vx,field_id";
			break;
		case 0x6C:
			result = "sput-char vx,field_id";
			break;
		case 0x6D:
			result = "sput-short vx,field_id";
			break;
		case 0x6E:
			result = "invoke-virtual { parameters }, methodtocall";
			break;
		case 0x6F:
			result = "invoke-super {parameter},methodtocall";
			break;
		case 0x70:
			result = "invoke-direct { parameters }, methodtocall";
			break;
		case 0x71:
			result = "invoke-static {parameters}, methodtocall";
			break;
		case 0x72:
			result = "invoke-interface {parameters},methodtocall";
			break;
		case 0x73:
			result = "unused_73";
			break;
		case 0x74:
			result = "invoke-virtual/range {vx..vy},methodtocall";
			break;
		case 0x75:
			result = "invoke-super/range";
			break;
		case 0x76:
			result = "invoke-direct/range {vx..vy},methodtocall";
			break;
		case 0x77:
			result = "invoke-static/range {vx..vy},methodtocall";
			break;
		case 0x78:
			result = "invoke-interface-range";
			break;
		case 0x79:
			result = "unused_79";
			break;
		case 0x7A:
			result = "unused_7A";
			break;
		case 0x7B:
			result = "neg-int vx,vy";
			break;
		case 0x7C:
			result = "not-int vx,vy";
			break;
		case 0x7D:
			result = "neg-long vx,vy";
			break;
		case 0x7E:
			result = "not-long vx,vy";
			break;
		case 0x7F:
			result = "neg-float vx,vy";
			break;
		case 0x80:
			result = "neg-double vx,vy";
			break;
		case 0x81:
			result = "int-to-long vx, vy";
			break;
		case 0x82:
			result = "int-to-float vx, vy";
			break;
		case 0x83:
			result = "int-to-double vx, vy";
			break;
		case 0x84:
			result = "long-to-int vx,vy";
			break;
		case 0x85:
			result = "long-to-float vx, vy";
			break;
		case 0x86:
			result = "long-to-double vx, vy";
			break;
		case 0x87:
			result = "float-to-int vx, vy";
			break;
		case 0x88:
			result = "float-to-long vx,vy";
			break;
		case 0x89:
			result = "float-to-double vx, vy";
			break;
		case 0x8A:
			result = "double-to-int vx, vy";
			break;
		case 0x8B:
			result = "double-to-long vx, vy";
			break;
		case 0x8C:
			result = "double-to-float vx, vy";
			break;
		case 0x8D:
			result = "int-to-byte vx,vy";
			break;
		case 0x8E:
			result = "int-to-char vx,vy";
			break;
		case 0x8F:
			result = "int-to-short vx,vy";
			break;
		case 0x90:
			result = "add-int vx,vy,vz";
			break;
		case 0x91:
			result = "sub-int vx,vy,vz";
			break;
		case 0x92:
			result = "mul-int vx, vy, vz";
			break;
		case 0x93:
			result = "div-int vx,vy,vz";
			break;
		case 0x94:
			result = "rem-int vx,vy,vz";
			break;
		case 0x95:
			result = "and-int vx, vy, vz";
			break;
		case 0x96:
			result = "or-int vx, vy, vz";
			break;
		case 0x97:
			result = "xor-int vx, vy, vz";
			break;
		case 0x98:
			result = "shl-int vx, vy, vz";
			break;
		case 0x99:
			result = "shr-int vx, vy, vz";
			break;
		case 0x9A:
			result = "ushr-int vx, vy, vz";
			break;
		case 0x9B:
			result = "add-long vx, vy, vz";
			break;
		case 0x9C:
			result = "sub-long vx,vy,vz";
			break;
		case 0x9D:
			result = "mul-long vx,vy,vz";
			break;
		case 0x9E:
			result = "div-long vx, vy, vz";
			break;
		case 0x9F:
			result = "rem-long vx,vy,vz";
			break;
		case 0xA0:
			result = "and-long vx, vy, vz";
			break;
		case 0xA1:
			result = "or-long vx, vy, vz";
			break;
		case 0xA2:
			result = "xor-long vx, vy, vz";
			break;
		case 0xA3:
			result = "shl-long vx, vy, vz";
			break;
		case 0xA4:
			result = "shr-long vx,vy,vz";
			break;
		case 0xA5:
			result = "ushr-long vx, vy, vz";
			break;
		case 0xA6:
			result = "add-float vx,vy,vz";
			break;
		case 0xA7:
			result = "sub-float vx,vy,vz";
			break;
		case 0xA8:
			result = "mul-float vx, vy, vz";
			break;
		case 0xA9:
			result = "div-float vx, vy, vz";
			break;
		case 0xAA:
			result = "rem-float vx,vy,vz";
			break;
		case 0xAB:
			result = "add-double vx,vy,vz";
			break;
		case 0xAC:
			result = "sub-double vx,vy,vz";
			break;
		case 0xAD:
			result = "mul-double vx, vy, vz";
			break;
		case 0xAE:
			result = "div-double vx, vy, vz";
			break;
		case 0xAF:
			result = "rem-double vx,vy,vz";
			break;
		case 0xB0:
			result = "add-int/2addr vx,vy";
			break;
		case 0xB1:
			result = "sub-int/2addr vx,vy";
			break;
		case 0xB2:
			result = "mul-int/2addr vx,vy";
			break;
		case 0xB3:
			result = "div-int/2addr vx,vy";
			break;
		case 0xB4:
			result = "rem-int/2addr vx,vy";
			break;
		case 0xB5:
			result = "and-int/2addr vx, vy";
			break;
		case 0xB6:
			result = "or-int/2addr vx, vy";
			break;
		case 0xB7:
			result = "xor-int/2addr vx, vy";
			break;
		case 0xB8:
			result = "shl-int/2addr vx, vy";
			break;
		case 0xB9:
			result = "shr-int/2addr vx, vy";
			break;
		case 0xBA:
			result = "ushr-int/2addr vx, vy";
			break;
		case 0xBB:
			result = "add-long/2addr vx,vy";
			break;
		case 0xBC:
			result = "sub-long/2addr vx,vy";
			break;
		case 0xBD:
			result = "mul-long/2addr vx,vy";
			break;
		case 0xBE:
			result = "div-long/2addr vx, vy";
			break;
		case 0xBF:
			result = "rem-long/2addr vx,vy";
			break;
		case 0xC0:
			result = "and-long/2addr vx, vy";
			break;
		case 0xC1:
			result = "or-long/2addr vx, vy";
			break;
		case 0xC2:
			result = "xor-long/2addr vx, vy";
			break;
		case 0xC3:
			result = "shl-long/2addr vx, vy";
			break;
		case 0xC4:
			result = "shr-long/2addr vx, vy";
			break;
		case 0xC5:
			result = "ushr-long/2addr vx, vy";
			break;
		case 0xC6:
			result = "add-float/2addr vx,vy";
			break;
		case 0xC7:
			result = "sub-float/2addr vx,vy";
			break;
		case 0xC8:
			result = "mul-float/2addr vx, vy";
			break;
		case 0xC9:
			result = "div-float/2addr vx, vy";
			break;
		case 0xCA:
			result = "rem-float/2addr vx,vy";
			break;
		case 0xCB:
			result = "add-double/2addr vx, vy";
			break;
		case 0xCC:
			result = "sub-double/2addr vx, vy";
			break;
		case 0xCD:
			result = "mul-double/2addr vx, vy";
			break;
		case 0xCE:
			result = "div-double/2addr vx, vy";
			break;
		case 0xCF:
			result = "rem-double/2addr vx,vy";
			break;
		case 0xD0:
			result = "add-int/lit16 vx,vy,lit16";
			break;
		case 0xD1:
			result = "sub-int/lit16 vx,vy,lit16";
			break;
		case 0xD2:
			result = "mul-int/lit16 vx,vy,lit16";
			break;
		case 0xD3:
			result = "div-int/lit16 vx,vy,lit16";
			break;
		case 0xD4:
			result = "rem-int/lit16 vx,vy,lit16";
			break;
		case 0xD5:
			result = "and-int/lit16 vx,vy,lit16";
			break;
		case 0xD6:
			result = "or-int/lit16 vx,vy,lit16";
			break;
		case 0xD7:
			result = "xor-int/lit16 vx,vy,lit16";
			break;
		case 0xD8:
			result = "add-int/lit8 vx,vy,lit8";
			break;
		case 0xD9:
			result = "sub-int/lit8 vx,vy,lit8";
			break;
		case 0xDA:
			result = "mul-int/lit8 vx,vy,lit8";
			break;
		case 0xDB:
			result = "div-int/lit8 vx,vy,lit8";
			break;
		case 0xDC:
			result = "rem-int/lit8 vx,vy,lit8";
			break;
		case 0xDD:
			result = "and-int/lit8 vx,vy,lit8";
			break;
		case 0xDE:
			result = "or-int/lit8 vx, vy, lit8";
			break;
		case 0xDF:
			result = "xor-int/lit8 vx, vy, lit8";
			break;
		case 0xE0:
			result = "shl-int/lit8 vx, vy, lit8";
			break;
		case 0xE1:
			result = "shr-int/lit8 vx, vy, lit8";
			break;
		case 0xE2:
			result = "ushr-int/lit8 vx, vy, lit8";
			break;
		case 0xE3:
			result = "unused_E3";
			break;
		case 0xE4:
			result = "unused_E4";
			break;
		case 0xE5:
			result = "unused_E5";
			break;
		case 0xE6:
			result = "unused_E6";
			break;
		case 0xE7:
			result = "unused_E7";
			break;
		case 0xE8:
			result = "unused_E8";
			break;
		case 0xE9:
			result = "unused_E9";
			break;
		case 0xEA:
			result = "unused_EA";
			break;
		case 0xEB:
			result = "unused_EB";
			break;
		case 0xEC:
			result = "unused_EC";
			break;
		case 0xED:
			result = "unused_ED";
			break;
		case 0xEE:
			result = "execute-inline {parameters},inline ID";
			break;
		case 0xEF:
			result = "unused_EF";
			break;
		case 0xF0:
			result = "invoke-direct-empty";
			break;
		case 0xF1:
			result = "unused_F1";
			break;
		case 0xF2:
			result = "iget-quick vx,vy,offset";
			break;
		case 0xF3:
			result = "iget-wide-quick vx,vy,offset";
			break;
		case 0xF4:
			result = "iget-object-quick vx,vy,offset";
			break;
		case 0xF5:
			result = "iput-quick vx,vy,offset";
			break;
		case 0xF6:
			result = "iput-wide-quick vx,vy,offset";
			break;
		case 0xF7:
			result = "iput-object-quick vx,vy,offset";
			break;
		case 0xF8:
			result = "invoke-virtual-quick {parameters},vtable offset";
			break;
		case 0xF9:
			result = "invoke-virtual-quick/range {parameter range},vtable offset";
			break;
		case 0xFA:
			result = "invoke-super-quick {parameters},vtable offset";
			break;
		case 0xFB:
			result = "invoke-super-quick/range {register range},vtable offset";
			break;
		case 0xFC:
			result = "unused_FC";
			break;
		case 0xFD:
			result = "unused_FD";
			break;
		case 0xFE:
			result = "unused_FE";
			break;
		case 0xFF:
			result = "unused_FF";
			break;
		default:
			result = "NONE";
			break;
		}
		return result;
	}

	public static String getOPClass(int opcode) {
		String result = "";
		switch (opcode) {
		case 0x0:
			result = "nop";
			break;
		case 0x1:
			result = "move";
			break;
		case 0x2:
			result = "move";
			break;
		case 0x3:
			result = "move";
			break;
		case 0x4:
			result = "move";
			break;
		case 0x5:
			result = "move";
			break;
		case 0x6:
			result = "move";
			break;
		case 0x7:
			result = "move object";
			break;
		case 0x8:
			result = "move object";
			break;
		case 0x9:
			result = "move object";
			break;
		case 0x0A:
			result = "move result";
			break;
		case 0x0B:
			result = "move result";
			break;
		case 0x0C:
			result = "move result";
			break;
		case 0x0D:
			result = "move exception";
			break;
		case 0x0E:
			result = "return void";
			break;
		case 0x0F:
			result = "return";
			break;
		case 0x10:
			result = "return";
			break;
		case 0x11:
			result = "return object";
			break;
		case 0x12:
			result = "put constant";
			break;
		case 0x13:
			result = "put constant";
			break;
		case 0x14:
			result = "put constant";
			break;
		case 0x15:
			result = "put constant";
			break;
		case 0x16:
			result = "put constant";
			break;
		case 0x17:
			result = "put constant";
			break;
		case 0x18:
			result = "put constant";
			break;
		case 0x19:
			result = "put constant";
			break;
		case 0x1A:
			result = "put constant";
			break;
		case 0x1B:
			result = "put constant";
			break;
		case 0x1C:
			result = "put constant";
			break;
		case 0x1D:
			result = "monitor";
			break;
		case 0x1E:
			result = "monitor release";
			break;
		case 0x1F:
			result = "check";
			break;
		case 0x20:
			result = "check";
			break;
		case 0x21:
			result = "array";
			break;
		case 0x22:
			result = "instance";
			break;
		case 0x23:
			result = "array new";
			break;
		case 0x24:
			result = "array new";
			break;
		case 0x25:
			result = "array new";
			break;
		case 0x26:
			result = "array fill";
			break;
		case 0x27:
			result = "throw";
			break;
		case 0x28:
			result = "goto";
			break;
		case 0x29:
			result = "goto";
			break;
		case 0x2A:
			result = "goto";
			break;
		case 0x2B:
			result = "switch";
			break;
		case 0x2C:
			result = "switch";
			break;
		case 0x2D:
			result = "compare";
			break;
		case 0x2E:
			result = "compare";
			break;
		case 0x2F:
			result = "compare";
			break;
		case 0x30:
			result = "compare";
			break;
		case 0x31:
			result = "compare";
			break;
		case 0x32:
			result = "goto if";
			break;
		case 0x33:
			result = "goto if";
			break;
		case 0x34:
			result = "goto if";
			break;
		case 0x35:
			result = "goto if";
			break;
		case 0x36:
			result = "goto if";
			break;
		case 0x37:
			result = "goto if";
			break;
		case 0x38:
			result = "goto if";
			break;
		case 0x39:
			result = "if";
			break;
		case 0x3A:
			result = "if";
			break;
		case 0x3B:
			result = "if";
			break;
		case 0x3C:
			result = "if";
			break;
		case 0x3D:
			result = "if";
			break;
		case 0x3E:
			result = "unused";
			break;
		case 0x3F:
			result = "unused";
			break;
		case 0x40:
			result = "unused";
			break;
		case 0x41:
			result = "unused";
			break;
		case 0x42:
			result = "unused";
			break;
		case 0x43:
			result = "unused";
			break;
		case 0x44:
			result = "get array";
			break;
		case 0x45:
			result = "get array";
			break;
		case 0x46:
			result = "get object array";
			break;
		case 0x47:
			result = "get array";
			break;
		case 0x48:
			result = "get array";
			break;
		case 0x49:
			result = "get array";
			break;
		case 0x4A:
			result = "get array";
			break;
		case 0x4B:
			result = "put array";
			break;
		case 0x4C:
			result = "put array";
			break;
		case 0x4D:
			result = "put object array";
			break;
		case 0x4E:
			result = "put array";
			break;
		case 0x4F:
			result = "put array";
			break;
		case 0x50:
			result = "put array";
			break;
		case 0x51:
			result = "put array";
			break;
		case 0x52:
			result = "get instance";
			break;
		case 0x53:
			result = "get instance";
			break;
		case 0x54:
			result = "get object instance";
			break;
		case 0x55:
			result = "get instance";
			break;
		case 0x56:
			result = "get instance";
			break;
		case 0x57:
			result = "get instance";
			break;
		case 0x58:
			result = "get instance";
			break;
		case 0x59:
			result = "put instance";
			break;
		case 0x5A:
			result = "put instance";
			break;
		case 0x5B:
			result = "put object instance";
			break;
		case 0x5C:
			result = "put instance";
			break;
		case 0x5D:
			result = "put instance";
			break;
		case 0x5E:
			result = "put instance";
			break;
		case 0x5F:
			result = "put instance";
			break;
		case 0x60:
			result = "get static";
			break;
		case 0x61:
			result = "get static";
			break;
		case 0x62:
			result = "get object static";
			break;
		case 0x63:
			result = "get static";
			break;
		case 0x64:
			result = "get static";
			break;
		case 0x65:
			result = "get static";
			break;
		case 0x66:
			result = "get static";
			break;
		case 0x67:
			result = "put static";
			break;
		case 0x68:
			result = "put static";
			break;
		case 0x69:
			result = "put object static";
			break;
		case 0x6A:
			result = "put static";
			break;
		case 0x6B:
			result = "put static";
			break;
		case 0x6C:
			result = "put static";
			break;
		case 0x6D:
			result = "put static";
			break;
		case 0x6E:
			result = "invoke virtual";
			break;
		case 0x6F:
			result = "invoke virtual";
			break;
		case 0x70:
			result = "invoke direct";
			break;
		case 0x71:
			result = "invoke static";
			break;
		case 0x72:
			result = "invoke interface";
			break;
		case 0x73:
			result = "unused";
			break;
		case 0x74:
			result = "invoke virtual";
			break;
		case 0x75:
			result = "invoke virtual";
			break;
		case 0x76:
			result = "invoke direct";
			break;
		case 0x77:
			result = "invoke static";
			break;
		case 0x78:
			result = "invoke interface";
			break;
		case 0x79:
			result = "unused";
			break;
		case 0x7A:
			result = "unused";
			break;
		case 0x7B:
			result = "neg";
			break;
		case 0x7C:
			result = "not";
			break;
		case 0x7D:
			result = "neg";
			break;
		case 0x7E:
			result = "not";
			break;
		case 0x7F:
			result = "neg";
			break;
		case 0x80:
			result = "neg";
			break;
		case 0x81:
			result = "convert";
			break;
		case 0x82:
			result = "convert";
			break;
		case 0x83:
			result = "convert";
			break;
		case 0x84:
			result = "convert";
			break;
		case 0x85:
			result = "convert";
			break;
		case 0x86:
			result = "convert";
			break;
		case 0x87:
			result = "convert";
			break;
		case 0x88:
			result = "convert";
			break;
		case 0x89:
			result = "convert";
			break;
		case 0x8A:
			result = "convert";
			break;
		case 0x8B:
			result = "convert";
			break;
		case 0x8C:
			result = "convert";
			break;
		case 0x8D:
			result = "convert";
			break;
		case 0x8E:
			result = "convert";
			break;
		case 0x8F:
			result = "convert";
			break;
		case 0x90:
			result = "calculate arithmetic";
			break;
		case 0x91:
			result = "calculate arithmetic";
			break;
		case 0x92:
			result = "calculate arithmetic";
			break;
		case 0x93:
			result = "calculate arithmetic";
			break;
		case 0x94:
			result = "calculate arithmetic";
			break;
		case 0x95:
			result = "calculate logic";
			break;
		case 0x96:
			result = "calculate logic";
			break;
		case 0x97:
			result = "calculate logic";
			break;
		case 0x98:
			result = "calculate logic";
			break;
		case 0x99:
			result = "calculate logic";
			break;
		case 0x9A:
			result = "calculate logic";
			break;
		case 0x9B:
			result = "calculate arithmetic";
			break;
		case 0x9C:
			result = "calculate arithmetic";
			break;
		case 0x9D:
			result = "calculate arithmetic";
			break;
		case 0x9E:
			result = "calculate arithmetic";
			break;
		case 0x9F:
			result = "calculate arithmetic";
			break;
		case 0xA0:
			result = "calculate logic";
			break;
		case 0xA1:
			result = "calculate logic";
			break;
		case 0xA2:
			result = "calculate logic";
			break;
		case 0xA3:
			result = "calculate logic";
			break;
		case 0xA4:
			result = "calculate logic";
			break;
		case 0xA5:
			result = "calculate logic";
			break;
		case 0xA6:
			result = "calculate arithmetic";
			break;
		case 0xA7:
			result = "calculate arithmetic";
			break;
		case 0xA8:
			result = "calculate arithmetic";
			break;
		case 0xA9:
			result = "calculate arithmetic";
			break;
		case 0xAA:
			result = "calculate arithmetic";
			break;
		case 0xAB:
			result = "calculate arithmetic";
			break;
		case 0xAC:
			result = "calculate arithmetic";
			break;
		case 0xAD:
			result = "calculate arithmetic";
			break;
		case 0xAE:
			result = "calculate arithmetic";
			break;
		case 0xAF:
			result = "calculate arithmetic";
			break;
		case 0xB0:
			result = "calculate arithmetic";
			break;
		case 0xB1:
			result = "calculate arithmetic";
			break;
		case 0xB2:
			result = "calculate arithmetic";
			break;
		case 0xB3:
			result = "calculate arithmetic";
			break;
		case 0xB4:
			result = "calculate arithmetic";
			break;
		case 0xB5:
			result = "calculate logic";
			break;
		case 0xB6:
			result = "calculate logic";
			break;
		case 0xB7:
			result = "calculate logic";
			break;
		case 0xB8:
			result = "calculate logic";
			break;
		case 0xB9:
			result = "calculate logic";
			break;
		case 0xBA:
			result = "calculate logic";
			break;
		case 0xBB:
			result = "calculate arithmetic";
			break;
		case 0xBC:
			result = "calculate arithmetic";
			break;
		case 0xBD:
			result = "calculate arithmetic";
			break;
		case 0xBE:
			result = "calculate arithmetic";
			break;
		case 0xBF:
			result = "calculate arithmetic";
			break;
		case 0xC0:
			result = "calculate logic";
			break;
		case 0xC1:
			result = "calculate logic";
			break;
		case 0xC2:
			result = "calculate logic";
			break;
		case 0xC3:
			result = "calculate logic";
			break;
		case 0xC4:
			result = "calculate logic";
			break;
		case 0xC5:
			result = "calculate logic";
			break;
		case 0xC6:
			result = "calculate arithmetic";
			break;
		case 0xC7:
			result = "calculate arithmetic";
			break;
		case 0xC8:
			result = "calculate arithmetic";
			break;
		case 0xC9:
			result = "calculate arithmetic";
			break;
		case 0xCA:
			result = "calculate arithmetic";
			break;
		case 0xCB:
			result = "calculate arithmetic";
			break;
		case 0xCC:
			result = "calculate arithmetic";
			break;
		case 0xCD:
			result = "calculate arithmetic";
			break;
		case 0xCE:
			result = "calculate arithmetic";
			break;
		case 0xCF:
			result = "calculate arithmetic";
			break;
		case 0xD0:
			result = "calculate arithmetic";
			break;
		case 0xD1:
			result = "calculate arithmetic";
			break;
		case 0xD2:
			result = "calculate arithmetic";
			break;
		case 0xD3:
			result = "calculate arithmetic";
			break;
		case 0xD4:
			result = "calculate arithmetic";
			break;
		case 0xD5:
			result = "calculate logic";
			break;
		case 0xD6:
			result = "calculate logic";
			break;
		case 0xD7:
			result = "calculate logic";
			break;
		case 0xD8:
			result = "calculate arithmetic";
			break;
		case 0xD9:
			result = "calculate arithmetic";
			break;
		case 0xDA:
			result = "calculate arithmetic";
			break;
		case 0xDB:
			result = "calculate arithmetic";
			break;
		case 0xDC:
			result = "calculate arithmetic";
			break;
		case 0xDD:
			result = "calculate logic";
			break;
		case 0xDE:
			result = "calculate logic";
			break;
		case 0xDF:
			result = "calculate logic";
			break;
		case 0xE0:
			result = "calculate logic";
			break;
		case 0xE1:
			result = "calculate logic";
			break;
		case 0xE2:
			result = "calculate logic";
			break;
		case 0xE3:
			result = "unused";
			break;
		case 0xE4:
			result = "unused";
			break;
		case 0xE5:
			result = "unused";
			break;
		case 0xE6:
			result = "unused";
			break;
		case 0xE7:
			result = "unused";
			break;
		case 0xE8:
			result = "unused";
			break;
		case 0xE9:
			result = "unused";
			break;
		case 0xEA:
			result = "unused";
			break;
		case 0xEB:
			result = "unused";
			break;
		case 0xEC:
			result = "unused";
			break;
		case 0xED:
			result = "unused";
			break;
		case 0xEE:
			result = "execute";
			break;
		case 0xEF:
			result = "unused";
			break;
		case 0xF0:
			result = "nop";
			break;
		case 0xF1:
			result = "unused";
			break;
		case 0xF2:
			result = "get instance";
			break;
		case 0xF3:
			result = "get instance";
			break;
		case 0xF4:
			result = "get object instance";
			break;
		case 0xF5:
			result = "put instance";
			break;
		case 0xF6:
			result = "put instance";
			break;
		case 0xF7:
			result = "put object instance";
			break;
		case 0xF8:
			result = "invoke virtual";
			break;
		case 0xF9:
			result = "invoke virtual";
			break;
		case 0xFA:
			result = "invoke virtual";
			break;
		case 0xFB:
			result = "invoke virtual";
			break;
		case 0xFC:
			result = "unused";
			break;
		case 0xFD:
			result = "unused";
			break;
		case 0xFE:
			result = "unused";
			break;
		case 0xFF:
			result = "unused";
			break;
		}
		return result;
	}
}

