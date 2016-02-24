package erjang.m.binary;

import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import com.trifork.clj_ds.PersistentTreeMap.Seq;

import erjang.*;
import erjang.driver.IO;
import erjang.driver.IO.BARR;
import erjang.m.erlang.ErlBif;
import erjang.m.erlang.ErlConvert;

/**
 * The implementation of Erlang's binary module
 * 
 * @author Pavlo Baron (pb@pbit.org)
 * @author Kresten Krab Thorup (krab@trifork.com)
 * 
 * TODO: port implementation (as far as possible) from erts/emulator/beam/erl_bif_binary.c
 * TODO: extend signature with EProc where necessary (acc. to the corresponding C code)
 * TODO: take care of correct usage of EBig and ESmall. Don't use EInteger
 * TODO: no EUnsigned class, instead checks for BigInteger.signum where necessary
 * TODO: implement an EPart class locally?
 * TODO: use ETupleN instead of ETuple (specific classes)
 * TODO: bin_to_list:/* C code works with big binaries in several iterations
 * (callback starting with a calculated max. loop count). How should we do that?
 * TODO: can binaries become awkwardly big so that EString.make duplicating the byte array would become a problem?
 *
 */
public class Native extends ENative {
	
	static final EAtom am_scope = EAtom.intern("scope");
	static final EAtom am_nomatch = EAtom.intern("nomatch");

	/**
	 * at(Subject, Pos) -> int()
	 */
	@BIF
	public static EInteger at(EObject subject, EObject pos) {
		throw new NotImplemented();
	}

    protected static EString do_bin_to_list(EObject subject, EObject pos, EObject len) {
        if (subject == null) throw ERT.badarg(subject);

        EBinary bin = subject.testBinary();
        if (bin == null) throw ERT.badarg(subject);
        if (!bin.isBinary()) throw ERT.badarg(subject);

        if (pos.testSmall() == null) throw ERT.badarg(pos);
        if (len.testSmall() == null) throw ERT.badarg(len);

        //TODO: unclear: C code operates with bit_offs which seems to be always 0 for binary:* calls
        //implement using EString for now, but try to find a case where bit_offs actually is set to != 0
        //so the EString implementation won't work (hypothesis)
        //TODO: another question: C code uses callbacks for big binaries. Here, we don't. This aspect needs to be
        //closer evaluated

        //we have to do some dirty hacks with indexes to fool the ErlConverter thinking we start with 1 and
        //expecting the stop instead of length, calculating length itself
        ESmall start = new ESmall(pos.asInt() + 1);
        ESmall stop = new ESmall((len.asInt() == -1) ? bin.byteSize() : len.asInt() + pos.asInt());

        return ErlConvert.binary_to_list(subject, start, stop);
    }

	/**
	 * bin_to_list(Subject) -> list()
	 */
	@BIF
	public static EString bin_to_list(EObject subject) {
        return do_bin_to_list(subject, new ESmall(0), new ESmall(-1));
	}

	/**
	 * bin_to_list(Subject, PosLen) -> list()
	 */
	@BIF
	public static EString bin_to_list(EObject subject, EObject poslen) {
        if (poslen == null) throw ERT.badarg(poslen);

        ETuple tuple = poslen.testTuple();
        if (tuple == null) throw ERT.badarg(poslen);

        ETuple2 tuple2 = ETuple2.cast(tuple);
        if (tuple2 == null) throw ERT.badarg(poslen);

        if (tuple2.elem1.testSmall() == null) throw ERT.badarg(tuple2.elem1);
        if (tuple2.elem2.testSmall() == null) throw ERT.badarg(tuple2.elem2);

		return do_bin_to_list(subject, new ESmall(tuple2.elem1.asInt()), new ESmall(tuple2.elem2.asInt()));
	}
	
	/**
	 * bin_to_list(Subject, Pos, Len) -> list()
	 */
	@BIF
	public static EString bin_to_list(EObject subject, EObject pos, EObject len) {
		return do_bin_to_list(subject, pos, len);
	}
	
	/**
	 * compile_pattern(Pattern) -> cp()
	 */
	@BIF
	public static ETuple compile_pattern(EObject pattern) {
		throw new NotImplemented();
	}
	
	/**
	 * copy(Subject) -> binary()
	 */
	@BIF
	public static EBinary copy(EObject subject) {
		return copy(subject, ERT.box(1));
	}
	
	/**
	 * copy(Subject,N) -> binary()
	 */
	@BIF
	public static EBinary copy(EObject subject, EObject n) {
		ESmall count;
		EBinary bin;
		if ((bin=subject.testBinary()) == null || (count=n.testSmall()) == null) {
			throw ERT.badarg(subject, n);
		}
		
		BARR b = new BARR();
		for (int i = 0; i < count.value; i++) {
			try {
				bin.writeTo(b);
			} catch (IOException e) {
				throw new ErlangError(new EString(e.getMessage()));
			}
		}
		
		return new EBinary(b.toByteArray());		
	}
	
	/**
	 * decode_unsigned(Subject) -> Unsigned
	 */
	@BIF
	public static EInteger decode_unsigned(EObject subject) {
		throw new NotImplemented();
	}
	
	/**
	 * decode_unsigned(Subject, Endianess) -> Unsigned
	 */
	@BIF
	public static EInteger decode_unsigned(EObject subject, EObject endianess) {
		throw new NotImplemented();
	}
	
	/**
	 * encode_unsigned(Unsigned) -> binary()
	 */
	@BIF
	public static EBinary encode_unsigned(EObject unsigned) {
		throw new NotImplemented();
	}
	
	/**
	 * encode_unsigned(Unsigned,Endianess) -> binary()
	 */
	@BIF
	public static EBinary encode_unsigned(EObject unsigned, EObject endianess) {
		throw new NotImplemented();
	}
	
	/**
	 * first(Subject) -> int()
	 */
	@BIF
	public static EInteger first(EObject subject) {
		EBinary bin = subject.testBinary();
		if (bin == null || bin.byteSize() == 0)
			throw ERT.badarg(subject);
		return ERT.box( bin.intBitsAt( 0, 8 ) );
	}
	
	/**
	 * last(Subject) -> int()
	 */
	@BIF
	public static EInteger last(EObject subject) {
		EBinary bin = subject.testBinary();
		if (bin == null || bin.byteSize() == 0)
			throw ERT.badarg(subject);
		return ERT.box( bin.intBitsAt( (bin.bitSize()-8), 8 ) );
	}
	
	/**
	 * list_to_bin(ByteList) -> binary()  
	 */
	@BIF
	public static EBinary list_to_bin(EObject byteList) {
        return ErlBif.list_to_binary(byteList);
	}
	
	/**
	 * longest_common_prefix(Binaries) -> int()
	 */
	@BIF
	public static EInteger longest_common_prefix(EObject binaries) {
		ESeq seq = binaries.testSeq();
		if (seq == null) throw ERT.badarg(binaries);
		EObject[] vals = seq.toArray();
		
		if (vals.length == 0)
			return ERT.box(0);
		
		EBinary first = vals[0].testBinary();
		if (first == null) throw ERT.badarg(binaries);
		
		if (vals.length == 1)
			return ERT.box(first.byteSize());

		for (int pos = 0; true; pos++) {
			
			if (first.byteSize() == pos)
				return ERT.box(pos);
			
			byte ch_first = first.byteAt(pos*8);
			for (int i = 1; i < vals.length; i++) {
				EBinary bin = vals[i].testBinary();
				if (bin == null) throw ERT.badarg(binaries);	
				
				if (bin.byteSize() == pos)
					return ERT.box(pos);
				
				byte ch_this = bin.byteAt(pos*8);

				if (ch_first != ch_this) {
					return ERT.box(pos);
				}
			}
		}
	}
	
	/**
	 * longest_common_suffix(Binaries) -> int()
	 */
	@BIF
	public static EInteger longest_common_suffix(EObject binaries) {
		throw new NotImplemented();
	}
	
	/**
	 * match(Subject, Pattern) -> Found | nomatch
	 */
	@BIF
	public static EObject match(EObject subject, EObject pattern) {
		return match(subject, pattern, ERT.NIL);
	}
	
	/**
	 * match(Subject,Pattern,Options) -> Found | nomatch
	 */
	@BIF
	public static EObject match(EObject subject, EObject pattern, EObject options) {
		ESeq result = matches(subject, pattern, options);
		if (result.length() == 0)
			return am_nomatch;
		else
			return result.head();
	}
	
	/**
	 * matches(Subject, Pattern) -> Found
	 */
	@BIF
	public static EObject matches(EObject subject, EObject pattern) {
		return matches(subject, pattern, ERT.NIL);
	}
	
	/**
	 * matches(Subject,Pattern,Options) -> Found
	 */
	@BIF
	public static ESeq matches(EObject subject, EObject pattern, EObject options) {
		EBinary haystack = subject.testBinary();
		EBinary needle = pattern.testBinary();
		ESeq needles = pattern.testSeq();
		ESeq opts = options.testSeq();
		
		if (opts == null || haystack == null || (needle == null && needles==null)) {
			throw ERT.badarg(subject, pattern, options);
		}
		
		if (needles != null && needles.isNil()) {
			throw ERT.badarg(subject, pattern, options);
		}
		
		if (needle != null && needle.byteSize() == 0) {
			throw ERT.badarg(subject, pattern, options);
		}
		
		if (needle != null) {
			needles = ERT.NIL.cons(needle);
		}

		EObject[] neddleArr = needles.toArray();
		
		int offset = 0;
		int length = haystack.byteSize();
		if (!options.isNil()) {
			ETuple2 opt = ETuple2.cast( opts.head() );
			ETuple2 range = null;
			ESmall from = null, len = null;
			if (opt == null 
					|| opt.elm(1) != am_scope
					|| (range = ETuple2.cast( opt.elm(2) )) == null
					|| (from = range.elem1.testSmall()) == null
					|| (len = range.elem2.testSmall()) == null
					) {
				throw ERT.badarg(subject, pattern, options);
			}
			
			offset = from.value;
			length = len.value;
			
			if (offset < 0 || (offset + length) > haystack.byteSize()) {
				throw ERT.badarg(subject, pattern, options);
			}
		}
		
		byte[] hay = haystack.getByteArray();
		byte[][] needlesArr = new byte[neddleArr.length][];
		for (int i = 0; i < neddleArr.length; i++) {
			needlesArr[i] = neddleArr[i].testBinary().getByteArray();
		}
		
		Arrays.sort(needlesArr, new Comparator<byte[]>() {
			// sort longest-first
			@Override
			public int compare(byte[] arg0, byte[] arg1) {
				return arg1.length - arg0.length;
			}			
		});
		
		int orig_len = length;
		int[] len = new int[1];
		ESeq result = ERT.NIL;
		ESeq last_result;
		do {
			last_result = result;
			len[0] = length;
			int found = indexof(hay, needlesArr, offset, len);
			if (found != -1) {
				offset = found+len[0];
				length = (orig_len - offset);
				result = result.cons(new ETuple2(ERT.box(found), ERT.box(len[0])));
			}
			
		} while(result != last_result);
		
		return result.reverse();
	}
	
	static int indexof(byte[] haystack, byte[][] needles, int from, int[] len)
	{
		for (int pos = from; pos < haystack.length; pos++) {
			for (int i = 0; i < needles.length; i++) {
				if (needles[i].length <= len[0] && looking_at(haystack, pos, needles[i])) {
					len[0] = needles[i].length;
					return pos;
				}
			}
		}
		
		return -1;
	}
	
	static boolean looking_at(byte[] haystack, int off, byte[] needle) {
		if (off + needle.length > haystack.length)
			return false;

		for (int i = 0; i < needle.length; i++) {
			if (haystack[off+i] != needle[i])
				return false;
		}

		return true;
	}
	
	/**
	 * part(Subject, PosLen) -> binary()
	 */
	@BIF
	public static EBinary part(EObject subject, EObject poslen) {
		EBinary sub = subject.testBinary();
		ETuple2 pl = ETuple2.cast(poslen);
		ESmall pos, len;
		if (sub == null 
				|| pl == null
				|| (pos=pl.elem1.testSmall()) == null
				|| (len=pl.elem2.testSmall()) == null
				) {
			throw ERT.badarg(subject, poslen);
		}
		
		return part(sub, pos.value, len.value, false);
	}
	
	/**
	 * part(Subject, Pos, Len) -> binary()
	 */
	@BIF
	public static EBinary part(EObject subject, EObject opos, EObject olen) {
		EBinary sub = subject.testBinary();
		ESmall pos, len;
		if (sub == null 
				|| (pos=opos.testSmall()) == null
				|| (len=olen.testSmall()) == null
				) {
			throw ERT.badarg(subject, opos, olen);
		}
		
		return part(sub, pos.value, len.value, false);
	}
	
	private static EBinary part(EBinary sub, int pos, int len, boolean as_guard) {
		if (len < 0) {
			pos = pos + len;
			len = -len;
		}
		
		if (pos < 0 || (pos + len) > sub.byteSize()) {
			throw ERT.badarg(sub, ERT.box(pos), ERT.box(len));
		}
		
		return sub.sub_binary(pos, len);
	}

	/**
	 * referenced_byte_size(binary()) -> int()
	 */
	@BIF
	public static EInteger referenced_byte_size(EObject subject) {
		throw new NotImplemented();
	}
}

