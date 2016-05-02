/**********************************************************\
|                                                          |
|                          hprose                          |
|                                                          |
| Official WebSite: http://www.hprose.com/                 |
|                   http://www.hprose.net/                 |
|                   http://www.hprose.org/                 |
|                                                          |
\**********************************************************/
/**********************************************************\
 *                                                        *
 * HproseReader.java                                      *
 *                                                        *
 * hprose reader class for Java.                          *
 *                                                        *
 * LastModified: Feb 8, 2014                              *
 * Author: Ma Bingyao <andot@hprose.com>                  *
 *                                                        *
\**********************************************************/
package hprose.io;

import hprose.common.HproseException;
import hprose.common.UUID;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;
import java.util.Stack;
import java.util.TimeZone;
import java.util.Vector;

public final class HproseReader {

    public final InputStream stream;
    private final Vector ref = new Vector();
    private final Vector classref = new Vector();
    private final Hashtable propertyref = new Hashtable();

    public HproseReader(InputStream stream) {
        this.stream = stream;
    }

    public Object unserialize() throws IOException {
        return unserialize(stream.read(), null);
    }

    public Object unserialize(Class type) throws IOException {
        return unserialize(stream.read(), type);
    }

    private Object unserialize(int tag, Class type) throws IOException {
        switch (tag) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return readDigit(tag, type);
            case HproseTags.TagInteger:
                return readInteger(type);
            case HproseTags.TagLong:
                return readLong(type);
            case HproseTags.TagDouble:
                return readDouble(type);
            case HproseTags.TagNull:
                return null;
            case HproseTags.TagEmpty:
                return readEmpty(type);
            case HproseTags.TagTrue:
                return readTrue(type);
            case HproseTags.TagFalse:
                return readFalse(type);
            case HproseTags.TagNaN:
                return readNaN(type);
            case HproseTags.TagInfinity:
                return readInfinity(type);
            case HproseTags.TagDate:
                return readDate(false, type);
            case HproseTags.TagTime:
                return readTime(false, type);
            case HproseTags.TagBytes:
                return readBytes(type);
            case HproseTags.TagUTF8Char:
                return readUTF8Char(type);
            case HproseTags.TagString:
                return readString(false, type);
            case HproseTags.TagGuid:
                return readUUID(false, type);
            case HproseTags.TagList:
                return readList(false, type);
            case HproseTags.TagMap:
                return readMap(false, type);
            case HproseTags.TagClass:
                readClass();
                return unserialize(stream.read(), type);
            case HproseTags.TagObject:
                return readObject(false, type);
            case HproseTags.TagRef:
                return readRef(type);
            case HproseTags.TagError:
                throw new HproseException((String)readString());
            case -1:
                throw new HproseException("No byte found in stream");
        }
        throw new HproseException("Unexpected serialize tag '" +
                                  (char) tag + "' in stream");
    }

    private Object readDigit(int tag, Class type) throws IOException {
        if ((type == null) ||
            Integer.class.equals(type) ||
            Object.class.equals(type)) {
            return HproseHelper.valueOf((int)(tag - '0'));
        }
        if (Byte.class.equals(type)) {
            return HproseHelper.valueOf((byte)(tag - '0'));
        }
        if (Long.class.equals(type)) {
            return HproseHelper.valueOf((long)(tag - '0'));
        }
        if (Short.class.equals(type)) {
            return HproseHelper.valueOf((short)(tag - '0'));
        }
        if (Float.class.equals(type)) {
            return HproseHelper.valueOf((float)(tag - '0'));
        }
        if (Double.class.equals(type)) {
            return HproseHelper.valueOf((double)(tag - '0'));
        }
        if (String.class.equals(type)) {
            return String.valueOf((char)tag);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf((char)tag);
        }
        if (Boolean.class.equals(type)) {
            return HproseHelper.valueOf(tag != '0');
        }
        if (Calendar.class.equals(type)) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(new Date((long)(tag - '0')));
            return calendar;
        }
        if (Date.class.equals(type)) {
            return new Date((long)(tag - '0'));
        }
        return castError("Integer", type);
    }

    private Object readInteger(Class type) throws IOException {
        if ((type == null) ||
            Integer.class.equals(type) ||
            Object.class.equals(type)) {
            return new Integer(readInt(HproseTags.TagSemicolon));
        }
        if (Byte.class.equals(type)) {
            return new Byte(readByte(HproseTags.TagSemicolon));
        }
        if (Long.class.equals(type)) {
            return new Long(readLong(HproseTags.TagSemicolon));
        }
        if (Short.class.equals(type)) {
            return new Short(readShort(HproseTags.TagSemicolon));
        }
        if (Float.class.equals(type)) {
            return Float.valueOf(readUntil(HproseTags.TagSemicolon));
        }
        if (Double.class.equals(type)) {
            return Double.valueOf(readUntil(HproseTags.TagSemicolon));
        }
        if (String.class.equals(type)) {
            return readUntil(HproseTags.TagSemicolon);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf((char) readInteger(false));
        }
        if (Boolean.class.equals(type)) {
            return HproseHelper.valueOf(readInteger(false) != 0);
        }
        if (Calendar.class.equals(type)) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(new Date(readLong(false)));
            return calendar;
        }
        if (Date.class.equals(type)) {
            return new Date(readLong(false));
        }
        return castError("Integer", type);
    }

    private Object readLong(Class type) throws IOException {
        if ((type == null) ||
            String.class.equals(type) ||
            Object.class.equals(type)) {
            return readUntil(HproseTags.TagSemicolon);
        }
        if (Long.class.equals(type)) {
            return new Long(readLong(HproseTags.TagSemicolon));
        }
        if (Integer.class.equals(type)) {
            return new Integer(readInt(HproseTags.TagSemicolon));
        }
        if (Float.class.equals(type)) {
            return Float.valueOf(readUntil(HproseTags.TagSemicolon));
        }
        if (Double.class.equals(type)) {
            return Double.valueOf(readUntil(HproseTags.TagSemicolon));
        }
        if (Byte.class.equals(type)) {
            return new Byte(readByte(HproseTags.TagSemicolon));
        }
        if (Short.class.equals(type)) {
            return new Short(readShort(HproseTags.TagSemicolon));
        }
        if (Boolean.class.equals(type)) {
            return HproseHelper.valueOf(readLong(false) != 0);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf((char) readLong(false));
        }
        if (Calendar.class.equals(type)) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(new Date(readLong(false)));
            return calendar;
        }
        if (Date.class.equals(type)) {
            return new Date(readLong(false));
        }
        return castError("Long", type);
    }

    private Object readDouble(Class type) throws IOException {
        if ((type == null) ||
            Double.class.equals(type) ||
            Object.class.equals(type)) {
            return Double.valueOf(readUntil(HproseTags.TagSemicolon));
        }
        if (Float.class.equals(type)) {
            return Float.valueOf(readUntil(HproseTags.TagSemicolon));
        }
        if (String.class.equals(type)) {
            return readUntil(HproseTags.TagSemicolon);
        }
        if (Integer.class.equals(type)) {
            return HproseHelper.valueOf(Double.valueOf(readUntil(HproseTags.TagSemicolon)).intValue());
        }
        if (Long.class.equals(type)) {
            return HproseHelper.valueOf(Double.valueOf(readUntil(HproseTags.TagSemicolon)).longValue());
        }
        if (Byte.class.equals(type)) {
            return HproseHelper.valueOf(Double.valueOf(readUntil(HproseTags.TagSemicolon)).byteValue());
        }
        if (Short.class.equals(type)) {
            return HproseHelper.valueOf(Double.valueOf(readUntil(HproseTags.TagSemicolon)).shortValue());
        }
        if (Boolean.class.equals(type)) {
            return HproseHelper.valueOf(readDouble(false) != 0.0);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf((char) Double.valueOf(readUntil(HproseTags.TagSemicolon)).intValue());
        }
        if (Calendar.class.equals(type)) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(new Date(Double.valueOf(readUntil(HproseTags.TagSemicolon)).longValue()));
            return calendar;
        }
        if (Date.class.equals(type)) {
            return new Date(Double.valueOf(readUntil(HproseTags.TagSemicolon)).longValue());
        }
        return castError("Double", type);
    }

    private Object readEmpty(Class type) throws IOException {
        if (type == null ||
            String.class.equals(type) ||
            Object.class.equals(type)) {
            return "";
        }
        if (StringBuffer.class.equals(type)) {
            return new StringBuffer();
        }
        if (char[].class.equals(type)) {
            return new char[0];
        }
        if (byte[].class.equals(type)) {
            return new byte[0];
        }
        if (Boolean.class.equals(type)) {
            return Boolean.FALSE;
        }
        if (Integer.class.equals(type)) {
            return HproseHelper.valueOf(0);
        }
        if (Long.class.equals(type)) {
            return HproseHelper.valueOf((long) 0);
        }
        if (Byte.class.equals(type)) {
            return HproseHelper.valueOf((byte) 0);
        }
        if (Short.class.equals(type)) {
            return HproseHelper.valueOf((short) 0);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf((char) 0);
        }
        if (Float.class.equals(type)) {
            return new Float(0);
        }
        if (Double.class.equals(type)) {
            return new Double(0);
        }
        return castError("Empty String", type);
    }

    private Object readTrue(Class type) throws IOException {
        if (type == null ||
            Boolean.class.equals(type) ||
            Object.class.equals(type)) {
            return Boolean.TRUE;
        }
        if (String.class.equals(type)) {
            return "true";
        }
        if (Integer.class.equals(type)) {
            return HproseHelper.valueOf(1);
        }
        if (Long.class.equals(type)) {
            return HproseHelper.valueOf((long) 1);
        }
        if (Byte.class.equals(type)) {
            return HproseHelper.valueOf((byte) 1);
        }
        if (Short.class.equals(type)) {
            return HproseHelper.valueOf((short) 1);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf('T');
        }
        if (Float.class.equals(type)) {
            return new Float(1);
        }
        if (Double.class.equals(type)) {
            return new Double(1);
        }
        return castError("Boolean", type);
    }

    private Object readFalse(Class type) throws IOException {
        if (type == null ||
            Boolean.class.equals(type) ||
            Object.class.equals(type)) {
            return Boolean.FALSE;
        }
        if (String.class.equals(type)) {
            return "false";
        }
        if (Integer.class.equals(type)) {
            return HproseHelper.valueOf(0);
        }
        if (Long.class.equals(type)) {
            return HproseHelper.valueOf((long) 0);
        }
        if (Byte.class.equals(type)) {
            return HproseHelper.valueOf((byte) 0);
        }
        if (Short.class.equals(type)) {
            return HproseHelper.valueOf((short) 0);
        }
        if (Character.class.equals(type)) {
            return HproseHelper.valueOf('F');
        }
        if (Float.class.equals(type)) {
            return new Float(0);
        }
        if (Double.class.equals(type)) {
            return new Double(0);
        }
        return castError("Boolean", type);
    }

    private Object readNaN(Class type) throws IOException {
        if ((type == null) ||
            Double.class.equals(type) ||
            Object.class.equals(type)) {
            return HproseHelper.valueOf(Double.NaN);
        }
        if (Float.class.equals(type)) {
            return HproseHelper.valueOf(Float.NaN);
        }
        if (String.class.equals(type)) {
            return "NaN";
        }
        return castError("NaN", type);
    }

    private Object readInfinity(Class type) throws IOException {
        if ((type == null) ||
            Double.class.equals(type) ||
            Object.class.equals(type)) {
            return HproseHelper.valueOf(readInfinity(false));
        }
        if (Float.class.equals(type)) {
            return HproseHelper.valueOf((float) readInfinity(false));
        }
        if (String.class.equals(type)) {
            return String.valueOf(readInfinity(false));
        }
        return castError("Infinity", type);
    }

    private Object readBytes(Class type) throws IOException {
        if ((type == null) ||
            byte[].class.equals(type) ||
            Object.class.equals(type)) {
            return readBytes(false);
        }
        if (String.class.equals(type)) {
            return new String(readBytes(false));
        }
        return castError("byte[]", type);
    }

    private Object readUTF8Char(Class type) throws IOException {
        char u = readUTF8Char(false);
        if ((type == null) ||
            Character.class.equals(type)) {
            return HproseHelper.valueOf(u);
        }
        if (String.class.equals(type) ||
            Object.class.equals(type)) {
            return String.valueOf((char)u);
        }
        if (Integer.class.equals(type)) {
            return HproseHelper.valueOf((int)u);
        }
        if (Byte.class.equals(type)) {
            return HproseHelper.valueOf((byte)u);
        }
        if (Long.class.equals(type)) {
            return HproseHelper.valueOf((long)u);
        }
        if (Short.class.equals(type)) {
            return HproseHelper.valueOf((short)u);
        }
        if (Float.class.equals(type)) {
            return HproseHelper.valueOf((float)u);
        }
        if (Double.class.equals(type)) {
            return HproseHelper.valueOf((double)u);
        }
        if (char[].class.equals(type)) {
            return new char[] { u };
        }
        if (Boolean.class.equals(type)) {
            return HproseHelper.valueOf(u != 0 && u != '0' && u != 'F' && u != 'f');
        }
        if (Calendar.class.equals(type)) {
            Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
            calendar.setTime(new Date((long)u));
            return calendar;
        }
        if (Date.class.equals(type)) {
            return new Date((long)u);
        }
        return castError("Character", type);
    }

    public void checkTag(int expectTag, int tag) throws IOException {
        if (tag != expectTag) {
            throw new HproseException("Tag '" + (char) expectTag +
                                      "' expected, but '" + (char) tag +
                                      "' found in stream");
        }
    }

    public void checkTag(int expectTag) throws IOException {
        checkTag(expectTag, stream.read());
    }

    public int checkTags(String expectTags, int tag) throws IOException {
        if (expectTags.indexOf(tag) == -1) {
            throw new HproseException("Tag '" + expectTags +
                                      "' expected, but '" + (char) tag +
                                      "' found in stream");
        }
        return tag;
    }

    public int checkTags(String expectTags) throws IOException {
        return checkTags(expectTags, stream.read());
    }

    public String readUntil(int tag) throws IOException {
        StringBuffer sb = new StringBuffer();
        int i = stream.read();
        while ((i != tag) && (i != -1)) {
            sb.append((char) i);
            i = stream.read();
        }
        return sb.toString();
    }

    public byte readByte(int tag) throws IOException {
        byte result = 0;
        int i = stream.read();
        if (i == tag) return result;
        byte sign = 1;
        if (i == '+') {
            i = stream.read();
        }
        else if (i == '-') {
            sign = -1;
            i = stream.read();
        }
        while ((i != tag) && (i != -1)) {
            result *= 10;
            result += (i - '0') * sign;
            i = stream.read();
        }
        return result;
    }

    public short readShort(int tag) throws IOException {
        short result = 0;
        int i = stream.read();
        if (i == tag) return result;
        short sign = 1;
        if (i == '+') {
            i = stream.read();
        }
        else if (i == '-') {
            sign = -1;
            i = stream.read();
        }
        while ((i != tag) && (i != -1)) {
            result *= 10;
            result += (i - '0') * sign;
            i = stream.read();
        }
        return result;
    }

    public int readInt(int tag) throws IOException {
        int result = 0;
        int i = stream.read();
        if (i == tag) return result;
        int sign = 1;
        if (i == '+') {
            i = stream.read();
        }
        else if (i == '-') {
            sign = -1;
            i = stream.read();
        }
        while ((i != tag) && (i != -1)) {
            result *= 10;
            result += (i - '0') * sign;
            i = stream.read();
        }
        return result;
    }

    public long readLong(int tag) throws IOException {
        long result = 0;
        int i = stream.read();
        if (i == tag) return result;
        long sign = 1;
        if (i == '+') {
            i = stream.read();
        }
        else if (i == '-') {
            sign = -1;
            i = stream.read();
        }
        while ((i != tag) && (i != -1)) {
            result *= 10;
            result += (i - '0') * sign;
            i = stream.read();
        }
        return result;
    }

    public int readInteger() throws IOException {
        return readInteger(true);
    }

    public int readInteger(boolean includeTag) throws IOException {
        if (includeTag) {
            int tag = stream.read();
            if ((tag >= '0') && (tag <= '9')) {
                return tag - '0';
            }
            checkTag(HproseTags.TagInteger, tag);
        }
        return readInt(HproseTags.TagSemicolon);
    }

    public long readLong() throws IOException {
        return readLong(true);
    }

    public long readLong(boolean includeTag) throws IOException {
        if (includeTag) {
            int tag = stream.read();
            if ((tag >= '0') && (tag <= '9')) {
                return (long)(tag - '0');
            }
            checkTags((char) HproseTags.TagInteger + "" +
                      (char) HproseTags.TagLong, tag);
        }
        return readLong(HproseTags.TagSemicolon);
    }

    public double readDouble() throws IOException {
        return readDouble(true);
    }

    public double readDouble(boolean includeTag) throws IOException {
        if (includeTag) {
            int tag = stream.read();
            if ((tag >= '0') && (tag <= '9')) {
                return (double)(tag - '0');
            }
            checkTags((char) HproseTags.TagInteger + "" +
                       (char) HproseTags.TagLong + "" +
                       (char) HproseTags.TagDouble + "" +
                       (char) HproseTags.TagNaN + "" +
                       (char) HproseTags.TagInfinity, tag);
            if (tag == HproseTags.TagNaN) {
                return Double.NaN;
            }
            if (tag == HproseTags.TagInfinity) {
                return readInfinity(false);
            }
        }
        return Double.parseDouble(readUntil(HproseTags.TagSemicolon));
    }

    public double readNaN() throws IOException {
        checkTag(HproseTags.TagNaN);
        return Double.NaN;
    }

    public double readInfinity() throws IOException {
        return readInfinity(true);
    }

    public double readInfinity(boolean includeTag) throws IOException {
        if (includeTag) {
            checkTag(HproseTags.TagInfinity);
        }
        return ((stream.read() == HproseTags.TagNeg) ? Double.NEGATIVE_INFINITY : Double.POSITIVE_INFINITY);
    }

    public Object readNull() throws IOException {
        checkTag(HproseTags.TagNull);
        return null;
    }

    public Object readEmpty() throws IOException {
        checkTag(HproseTags.TagEmpty);
        return "";
    }

    public boolean readBoolean() throws IOException {
        int tag = checkTags((char) HproseTags.TagTrue + "" + (char) HproseTags.TagFalse);
        return (tag == HproseTags.TagTrue);
    }

    public Object readDate() throws IOException {
        return readDate(true, null);
    }

    public Object readDate(boolean includeTag) throws IOException {
        return readDate(includeTag, null);
    }

    public Object readDate(Class type) throws IOException {
        return readDate(true, type);
    }

    public Object readDate(boolean includeTag, Class type) throws IOException {
        int tag;
        if (includeTag) {
            tag = checkTags((char) HproseTags.TagDate + "" + (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
        }
        Calendar calendar;
        int year = stream.read() - '0';
        year = year * 10 + stream.read() - '0';
        year = year * 10 + stream.read() - '0';
        year = year * 10 + stream.read() - '0';
        int month = stream.read() - '0';
        month = month * 10 + stream.read() - '0';
        int day = stream.read() - '0';
        day = day * 10 + stream.read() - '0';
        tag = stream.read();
        if (tag == HproseTags.TagTime) {
            int hour = stream.read() - '0';
            hour = hour * 10 + stream.read() - '0';
            int minute = stream.read() - '0';
            minute = minute * 10 + stream.read() - '0';
            int second = stream.read() - '0';
            second = second * 10 + stream.read() - '0';
            int millisecond = 0;
            tag = stream.read();
            if (tag == HproseTags.TagPoint) {
                millisecond = stream.read() - '0';
                millisecond = millisecond * 10 + stream.read() - '0';
                millisecond = millisecond * 10 + stream.read() - '0';
                tag = stream.read();
                if (tag >= '0' && tag <= '9') {
                    stream.read();
                    stream.read();
                    tag = stream.read();
                    if (tag >= '0' && tag <= '9') {
                        stream.read();
                        stream.read();
                        tag = stream.read();
                    }
                }
            }
            calendar = Calendar.getInstance(tag == HproseTags.TagUTC ? HproseHelper.UTC : TimeZone.getDefault());
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DATE, day);
            calendar.set(Calendar.HOUR_OF_DAY, hour);
            calendar.set(Calendar.MINUTE, minute);
            calendar.set(Calendar.SECOND, second);
            calendar.set(Calendar.MILLISECOND, millisecond);
        }
        else {
            calendar = Calendar.getInstance(tag == HproseTags.TagUTC ? HproseHelper.UTC : TimeZone.getDefault());
            calendar.set(Calendar.YEAR, year);
            calendar.set(Calendar.MONTH, month - 1);
            calendar.set(Calendar.DATE, day);
        }
        Object o = changeCalendarType(calendar, type);
        ref.addElement(o);
        return o;
    }

    public Object readTime() throws IOException {
        return readTime(true, null);
    }

    public Object readTime(boolean includeTag) throws IOException {
        return readTime(includeTag, null);
    }

    public Object readTime(Class type) throws IOException {
        return readTime(true, type);
    }

    public Object readTime(boolean includeTag, Class type) throws IOException {
        int tag;
        if (includeTag) {
            tag = checkTags((char) HproseTags.TagTime + "" + (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
        }
        Calendar calendar;
        int hour = stream.read() - '0';
        hour = hour * 10 + stream.read() - '0';
        int minute = stream.read() - '0';
        minute = minute * 10 + stream.read() - '0';
        int second = stream.read() - '0';
        second = second * 10 + stream.read() - '0';
        int millisecond = 0;
        tag = stream.read();
        if (tag == HproseTags.TagPoint) {
            millisecond = stream.read() - '0';
            millisecond = millisecond * 10 + stream.read() - '0';
            millisecond = millisecond * 10 + stream.read() - '0';
            tag = stream.read();
            if (tag >= '0' && tag <= '9') {
                stream.read();
                stream.read();
                tag = stream.read();
                if (tag >= '0' && tag <= '9') {
                    stream.read();
                    stream.read();
                    tag = stream.read();
                }
            }
        }
        calendar = Calendar.getInstance(tag == HproseTags.TagUTC ? HproseHelper.UTC : TimeZone.getDefault());
        calendar.set(Calendar.YEAR, 1970);
        calendar.set(Calendar.MONTH, 0);
        calendar.set(Calendar.DATE, 1);
        calendar.set(Calendar.HOUR_OF_DAY, hour);
        calendar.set(Calendar.MINUTE, minute);
        calendar.set(Calendar.SECOND, second);
        calendar.set(Calendar.MILLISECOND, millisecond);
        Object o = changeCalendarType(calendar, type);
        ref.addElement(o);
        return o;
    }

    public Object readDateTime() throws IOException {
        return readDateTime(null);
    }

    public Object readDateTime(Class type) throws IOException {
        int tag = checkTags((char) HproseTags.TagDate + "" +
                            (char) HproseTags.TagTime + "" +
                            (char) HproseTags.TagRef);
        if (tag == HproseTags.TagRef) {
            return readRef(type);
        }
        if (tag == HproseTags.TagDate) {
            return readDate(false, type);
        }
        return readTime(false, type);
    }

    private Object changeCalendarType(Calendar calendar, Class type) throws IOException {
        if (type == null ||
            Calendar.class.equals(type) ||
            Object.class.equals(type)) {
            return calendar;
        }
        if (Date.class.equals(type)) {
            return calendar.getTime();
        }
        if (Long.class.equals(type)) {
            return new Long(calendar.getTime().getTime());
        }
        if (String.class.equals(type)) {
            return calendar.getTime().toString();
        }
        return castError(calendar, type);
    }

    public byte[] readBytes() throws IOException {
        return readBytes(true);
    }

    public byte[] readBytes(boolean includeTag) throws IOException {
        if (includeTag) {
            int tag = checkTags((char) HproseTags.TagBytes + "" + (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return (byte[]) readRef(byte[].class);
            }
        }
        int len = readInt(HproseTags.TagQuote);
        int off = 0;
        byte[] b = new byte[len];
        while (len > 0) {
            int size = stream.read(b, off, len);
            off += size;
            len -= size;
        }
        checkTag(HproseTags.TagQuote);
        ref.addElement(b);
        return b;
    }

    public char readUTF8Char(boolean includeTag) throws IOException {
        if (includeTag) {
            checkTag(HproseTags.TagUTF8Char);
        }
        char u;
        int c = stream.read();
        switch (c >>> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7: {
                // 0xxx xxxx
                u = (char) c;
                break;
            }
            case 12:
            case 13: {
                // 110x xxxx   10xx xxxx
                int c2 = stream.read();
                u = (char) (((c & 0x1f) << 6) |
                            (c2 & 0x3f));
                break;
            }
            case 14: {
                // 1110 xxxx  10xx xxxx  10xx xxxx
                int c2 = stream.read();
                int c3 = stream.read();
                u = (char) (((c & 0x0f) << 12) |
                           ((c2 & 0x3f) << 6) |
                            (c3 & 0x3f));
                break;
            }
            default:
                throw new HproseException("bad utf-8 encoding at " +
                                          ((c < 0) ? "end of stream" : "0x" + Integer.toHexString(c & 0xff)));
        }
        return u;
    }

    public Object readString() throws IOException {
        return readString(true, null, true);
    }

    public Object readString(boolean includeTag) throws IOException {
        return readString(includeTag, null, true);
    }

    public Object readString(Class type) throws IOException {
        return readString(true, type, true);
    }

    public Object readString(boolean includeTag, Class type) throws IOException {
        return readString(includeTag, type, true);
    }

    private Object readString(boolean includeTag, Class type, boolean includeRef) throws IOException {
        if (includeTag) {
            int tag = checkTags((char) HproseTags.TagString + "" +
                                (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
        }
        int count = readInt(HproseTags.TagQuote);
        char[] buf = new char[count];
        for (int i = 0; i < count; i++) {
            int c = stream.read();
            switch (c >>> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7: {
                    // 0xxx xxxx
                    buf[i] = (char) c;
                    break;
                }
                case 12:
                case 13: {
                    // 110x xxxx   10xx xxxx
                    int c2 = stream.read();
                    buf[i] = (char) (((c & 0x1f) << 6) |
                                     (c2 & 0x3f));
                    break;
                }
                case 14: {
                    // 1110 xxxx  10xx xxxx  10xx xxxx
                    int c2 = stream.read();
                    int c3 = stream.read();
                    buf[i] = (char) (((c & 0x0f) << 12) |
                                     ((c2 & 0x3f) << 6) |
                                     (c3 & 0x3f));
                    break;
                }
                case 15: {
                    // 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
                    if ((c & 0xf) <= 4) {
                        int c2 = stream.read();
                        int c3 = stream.read();
                        int c4 = stream.read();
                        int s = ((c & 0x07) << 18) |
                                ((c2 & 0x3f) << 12) |
                                ((c3 & 0x3f) << 6) |
                                (c4 & 0x3f) - 0x10000;
                        if (0 <= s && s <= 0xfffff) {
                            buf[i++] = (char) (((s >>> 10) & 0x03ff) | 0xd800);
                            buf[i] = (char) ((s & 0x03ff) | 0xdc00);
                            break;
                        }
                    }
                // no break here!! here need throw exception.
                }
                default:
                    throw new HproseException("bad utf-8 encoding at " +
                                              ((c < 0) ? "end of stream" : "0x" + Integer.toHexString(c & 0xff)));
            }
        }
        checkTag(HproseTags.TagQuote);
        Object o = changeStringType(buf, type);
        if (includeRef) {
            ref.addElement(o);
        }
        return o;
    }

    private Object changeStringType(char[] str, Class type) throws IOException {
        if (char[].class.equals(type)) {
            return str;
        }
        if (StringBuffer.class.equals(type)) {
            return new StringBuffer(str.length).append(str);
        }
        String s = new String(str);
        if ((type == null) ||
            String.class.equals(type) ||
            Object.class.equals(type)) {
            return s;
        }
        if (Byte.class.equals(type)) {
            return new Byte(Byte.parseByte(s));
        }
        if (Short.class.equals(type)) {
            return new Short(Short.parseShort(s));
        }
        if (Integer.class.equals(type)) {
            return new Integer(Integer.parseInt(s));
        }
        if (Long.class.equals(type)) {
            return new Long(Long.parseLong(s));
        }
        if (Float.class.equals(type)) {
            return new Float(Float.parseFloat(s));
        }
        if (Double.class.equals(type)) {
            return new Double(Double.parseDouble(s));
        }
        if (Character.class.equals(type)) {
            if (str.length == 1) {
                return new Character(str[0]);
            }
            else {
                return new Character((char) Integer.parseInt(s));
            }
        }
        if (Boolean.class.equals(type)) {
            if (str.length == 0) {
                return Boolean.FALSE;
            }
            else if (str.length == 1) {
                return HproseHelper.valueOf(str[0] != 0);
            }
            else {
                return HproseHelper.valueOf(s.equalsIgnoreCase("true"));
            }
        }
        if (byte[].class.equals(type)) {
            try {
                return s.getBytes("UTF-8");
            }
            catch (Exception e) {
                return s.getBytes();
            }
        }
        if (UUID.class.equals(type)) {
            return UUID.fromString(s);
        }
        return castError(str, type);
    }

    public Object readUUID() throws IOException {
        return readUUID(true, null);
    }

    public Object readUUID(boolean includeTag) throws IOException {
        return readUUID(includeTag, null);
    }

    public Object readUUID(Class type) throws IOException {
        return readUUID(true, type);
    }

    public Object readUUID(boolean includeTag, Class type) throws IOException {
        if (includeTag) {
            int tag = checkTags((char)HproseTags.TagGuid + "" +
                                (char)HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
        }
        checkTag(HproseTags.TagOpenbrace);
        char[] buf = new char[36];
        for (int i = 0; i < 36; i++) {
            buf[i] = (char) stream.read();
        }
        checkTag(HproseTags.TagClosebrace);
        Object o = changeUUIDType(buf, type);
        ref.addElement(o);
        return o;
    }

    private Object changeUUIDType(char[] buf, Class type) throws IOException {
        if (char[].class.equals(type)) {
            return buf;
        }
        String s = new String(buf);
        if (String.class.equals(type)) {
            return s;
        }
        if (type == null ||
            UUID.class.equals(type) ||
            Object.class.equals(type)) {
            return UUID.fromString(s);
        }
        if (StringBuffer.class.equals(type)) {
            return new StringBuffer(s);
        }
        return castError(buf, type);
    }


    private short[] readShortArray(int count) throws IOException {
        short[] a = new short[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = ((Short)unserialize(Short.class)).shortValue();
        }
        return a;
    }

    private int[] readIntegerArray(int count) throws IOException {
        int[] a = new int[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = ((Integer)unserialize(Integer.class)).intValue();
        }
        return a;
    }

    private long[] readLongArray(int count) throws IOException {
        long[] a = new long[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = ((Long)unserialize(Long.class)).longValue();
        }
        return a;
    }

    private boolean[] readBooleanArray(int count) throws IOException {
        boolean[] a = new boolean[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = ((Boolean)unserialize(Boolean.class)).booleanValue();
        }
        return a;
    }

    private float[] readFloatArray(int count) throws IOException {
        float[] a = new float[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = ((Float)unserialize(Float.class)).floatValue();
        }
        return a;
    }

    private double[] readDoubleArray(int count) throws IOException {
        double[] a = new double[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = ((Double)unserialize(Double.class)).doubleValue();
        }
        return a;
    }

    private String[] readStringArray(int count) throws IOException {
        String[] a = new String[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = (String) unserialize(String.class);
        }
        return a;
    }

    private StringBuffer[] readStringBufferArray(int count) throws IOException {
        StringBuffer[] a = new StringBuffer[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = (StringBuffer) unserialize(StringBuffer.class);
        }
        return a;
    }

    private byte[][] readBytesArray(int count) throws IOException {
        byte[][] a = new byte[count][];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = (byte[]) unserialize(byte[].class);
        }
        return a;
    }

    private char[][] readCharsArray(int count) throws IOException {
        char[][] a = new char[count][];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = (char[]) unserialize(char[].class);
        }
        return a;
    }

    private Calendar[] readCalendarArray(int count) throws IOException {
        Calendar[] a = new Calendar[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = (Calendar) unserialize(Calendar.class);
        }
        return a;
    }

    private Date[] readDateArray(int count) throws IOException {
        Date[] a = new Date[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = (Date) unserialize(Date.class);
        }
        return a;
    }

    public void readArray(Class[] types, Object[] a, int count) throws IOException {
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = unserialize(types[i]);
        }
    }

    public Object[] readArray(int count) throws IOException {
        Object[] a = new Object[count];
        ref.addElement(a);
        for (int i = 0; i < count; i++) {
            a[i] = unserialize(Object.class);
        }
        return a;
    }

    private Vector readVector(int count) throws IOException {
        Vector list = new Vector(count);
        ref.addElement(list);
        for (int i = 0; i < count; i++) {
            list.addElement(unserialize(Object.class));
        }
        return list;
    }

    private Stack readStack(int count) throws IOException {
        Stack list = new Stack();
        ref.addElement(list);
        for (int i = 0; i < count; i++) {
            list.addElement(unserialize(Object.class));
        }
        return list;
    }

    private Hashtable readListAsHashtable(int count) throws IOException {
        Hashtable map = new Hashtable(count);
        ref.addElement(map);
        for (int i = 0; i < count; i++) {
            Object key = HproseHelper.valueOf(i);
            Object value = unserialize(Object.class);
            map.put(key, value);
        }
        return map;
    }

    public Object readList() throws IOException {
        return readList(true, null);
    }

    public Object readList(boolean includeTag) throws IOException {
        return readList(includeTag, null);
    }

    public Object readList(Class type) throws IOException {
        return readList(true, type);
    }

    public Object readList(boolean includeTag, Class type) throws IOException {
        if (includeTag) {
            int tag = checkTags((char) HproseTags.TagList + "" +
                                (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
        }
        int count = readInt(HproseTags.TagOpenbrace);
        Object list = null;
        if ((type == null) ||
            Object.class.equals(type) ||
            Object[].class.equals(type)) {
            list = readArray(count);
        }
        else if (int[].class.equals(type)) {
            list = readIntegerArray(count);
        }
        else if (short[].class.equals(type)) {
            list = readShortArray(count);
        }
        else if (long[].class.equals(type)) {
            list = readLongArray(count);
        }
        else if (String[].class.equals(type)) {
            list = readStringArray(count);
        }
        else if (boolean[].class.equals(type)) {
            list = readBooleanArray(count);
        }
        else if (double[].class.equals(type)) {
            list = readDoubleArray(count);
        }
        else if (float[].class.equals(type)) {
            list = readFloatArray(count);
        }
        else if (StringBuffer[].class.equals(type)) {
            list = readStringBufferArray(count);
        }
        else if (byte[][].class.equals(type)) {
            list = readBytesArray(count);
        }
        else if (char[][].class.equals(type)) {
            list = readCharsArray(count);
        }
        else if (Calendar[].class.equals(type)) {
            list = readCalendarArray(count);
        }
        else if (Date[].class.equals(type)) {
            list = readDateArray(count);
        }
        else if (Vector.class.equals(type)) {
            list = readVector(count);
        }
        else if (Stack.class.equals(type)) {
            list = readStack(count);
        }
        else if (Hashtable.class.equals(type)) {
            list = readListAsHashtable(count);
        }
        else {
            castError("List", type);
        }
        checkTag(HproseTags.TagClosebrace);
        return list;
    }

    private Hashtable readHashtable(int count) throws IOException {
        Hashtable map = new Hashtable(count);
        ref.addElement(map);
        for (int i = 0; i < count; i++) {
            Object key = unserialize(Object.class);
            Object value = unserialize(Object.class);
            map.put(key, value);
        }
        return map;
    }

    private Serializable readObject(int count, Class type) throws IOException {
        Serializable obj = (Serializable)HproseHelper.newInstance(type);
        ref.addElement(obj);
        for (int i = 0; i < count; i++) {
            String name = (String)(readString(String.class));
            Object value = unserialize(obj.getPropertyType(name));
            obj.setProperty(name, value);
        }
        return obj;
    }

    public Object readMap() throws IOException {
        return readMap(true, null);
    }

    public Object readMap(boolean includeTag) throws IOException {
        return readMap(includeTag, null);
    }

    public Object readMap(Class type) throws IOException {
        return readMap(true, type);
    }

    public Object readMap(boolean includeTag, Class type) throws IOException {
        if (includeTag) {
            int tag = checkTags((char) HproseTags.TagMap + "" +
                                (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
        }
        int count = readInt(HproseTags.TagOpenbrace);
        Object map = null;
        if ((type == null) ||
            Object.class.equals(type) ||
            Hashtable.class.equals(type)) {
            map = readHashtable(count);
        }
        else if (Serializable.class.isAssignableFrom(type)) {
            map = readObject(count, type);
        }
        else {
            castError("Map", type);
        }
        checkTag(HproseTags.TagClosebrace);
        return map;
    }

    public Object readObject() throws IOException {
        return readObject(true, null);
    }

    public Object readObject(boolean includeTag) throws IOException {
        return readObject(includeTag, null);
    }

    public Object readObject(Class type) throws IOException {
        return readObject(true, type);
    }

    public Object readObject(boolean includeTag, Class type) throws IOException {
        if (includeTag) {
            int tag = checkTags((char) HproseTags.TagObject + "" +
                                (char) HproseTags.TagClass + "" +
                                (char) HproseTags.TagRef);
            if (tag == HproseTags.TagRef) {
                return readRef(type);
            }
            if (tag == HproseTags.TagClass) {
                readClass();
                return readObject(type);
            }
        }
        Object c = classref.elementAt(readInt(HproseTags.TagOpenbrace));
        String[] propertyNames = (String[]) propertyref.get(c);
        int count = propertyNames.length;
        Serializable obj = null;
        if (Class.class.equals(c.getClass())) {
            Class cls = (Class) c;
            if ((type == null) || type.isAssignableFrom(cls)) {
                obj = HproseHelper.newInstance(cls);
            }
            else {
                obj = HproseHelper.newInstance(type);
            }
        }
        else if (type != null) {
            obj = HproseHelper.newInstance(type);
        }
        if (obj == null) {
            Hashtable map = new Hashtable(count);
            ref.addElement(map);
            for (int i = 0; i < count; i++) {
                Object value = unserialize(Object.class);
                if (value == null) {
                    value = HproseHelper.Null;
                }
                map.put(propertyNames[i], value);
            }
            checkTag(HproseTags.TagClosebrace);
            return map;
        }
        else {
            ref.addElement(obj);
            for (int i = 0; i < count; i++) {
                Object value = unserialize(obj.getPropertyType(propertyNames[i]));
                obj.setProperty(propertyNames[i], value);
            }
            checkTag(HproseTags.TagClosebrace);
            return obj;
        }
    }

    private void readClass() throws IOException {
        String className = (String) readString(false, null, false);
        int count = readInt(HproseTags.TagOpenbrace);
        String[] propertyNames = new String[count];
        for (int i = 0; i < count; i++) {
            propertyNames[i] = (String) readString(true);
        }
        checkTag(HproseTags.TagClosebrace);
        Class type = HproseHelper.getClass(className);
        if (type == null) {
            Object key = new Object();
            classref.addElement(key);
            propertyref.put(key, propertyNames);
        }
        else {
            classref.addElement(type);
            propertyref.put(type, propertyNames);
        }
    }

    private Object readRef(Class type) throws IOException {
        Object o = ref.elementAt(readInt(HproseTags.TagSemicolon));
        if (type == null || type.isInstance(o)) {
            return o;
        }
        return castError(o, type);
    }

    private Object castError(String srctype, Class desttype) throws IOException {
        throw new HproseException(srctype + " can't change to " + desttype.getName());
    }

    private Object castError(Object obj, Class type) throws IOException {
        throw new HproseException(obj.getClass().getName() + " can't change to " + type.getName());
    }

    public ByteArrayOutputStream readRaw() throws IOException {
    	ByteArrayOutputStream ostream = new ByteArrayOutputStream();
    	readRaw(ostream);
    	return ostream;
    }

    public void readRaw(OutputStream ostream) throws IOException {
        readRaw(ostream, stream.read());
    }

    private void readRaw(OutputStream ostream, int tag) throws IOException {
        ostream.write(tag);
        switch (tag) {
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
            case HproseTags.TagNull:
            case HproseTags.TagEmpty:
            case HproseTags.TagTrue:
            case HproseTags.TagFalse:
            case HproseTags.TagNaN:
                break;
            case HproseTags.TagInfinity:
                ostream.write(stream.read());
                break;
            case HproseTags.TagInteger:
            case HproseTags.TagLong:
            case HproseTags.TagDouble:
            case HproseTags.TagRef:
                readNumberRaw(ostream);
                break;
            case HproseTags.TagDate:
            case HproseTags.TagTime:
                readDateTimeRaw(ostream);
                break;
            case HproseTags.TagUTF8Char:
                readUTF8CharRaw(ostream);
                break;
            case HproseTags.TagBytes:
                readBytesRaw(ostream);
                break;
            case HproseTags.TagString:
                readStringRaw(ostream);
                break;
            case HproseTags.TagGuid:
                readGuidRaw(ostream);
                break;
            case HproseTags.TagList:
            case HproseTags.TagMap:
            case HproseTags.TagObject:
                readComplexRaw(ostream);
                break;
            case HproseTags.TagClass:
                readComplexRaw(ostream);
                readRaw(ostream);
                break;
            case HproseTags.TagError:
                readRaw(ostream);
                break;
            case -1:
                throw new HproseException("No byte found in stream");
            default:
                throw new HproseException("Unexpected serialize tag '" +
                        (char) tag + "' in stream");
        }
    }

    private void readNumberRaw(OutputStream ostream) throws IOException {
        int tag;
        do {
            tag = stream.read();
            ostream.write(tag);
        } while (tag != HproseTags.TagSemicolon);
    }

    private void readDateTimeRaw(OutputStream ostream) throws IOException {
        int tag;
        do {
            tag = stream.read();
            ostream.write(tag);
        } while (tag != HproseTags.TagSemicolon &&
                 tag != HproseTags.TagUTC);
    }

    private void readUTF8CharRaw(OutputStream ostream) throws IOException {
        int tag = stream.read();
        switch (tag >>> 4) {
            case 0:
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7: {
                // 0xxx xxxx
                ostream.write(tag);
                break;
            }
            case 12:
            case 13: {
                // 110x xxxx   10xx xxxx
                ostream.write(tag);
                ostream.write(stream.read());
                break;
            }
            case 14: {
                // 1110 xxxx  10xx xxxx  10xx xxxx
                ostream.write(tag);
                ostream.write(stream.read());
                ostream.write(stream.read());
                break;
            }
            default:
                throw new HproseException("bad utf-8 encoding at " +
                                          ((tag < 0) ? "end of stream" :
                                              "0x" + Integer.toHexString(tag & 0xff)));
        }
    }

    private void readBytesRaw(OutputStream ostream) throws IOException {
        int len = 0;
        int tag = '0';
        do {
            len *= 10;
            len += tag - '0';
            tag = stream.read();
            ostream.write(tag);
        } while (tag != HproseTags.TagQuote);
        int off = 0;
        byte[] b = new byte[len];
        while (len > 0) {
            int size = stream.read(b, off, len);
            off += size;
            len -= size;
        }
        ostream.write(b);
        ostream.write(stream.read());
    }

    private void readStringRaw(OutputStream ostream) throws IOException {
        int count = 0;
        int tag = '0';
        do {
            count *= 10;
            count += tag - '0';
            tag = stream.read();
            ostream.write(tag);
        } while (tag != HproseTags.TagQuote);
        for (int i = 0; i < count; i++) {
            tag = stream.read();
            switch (tag >>> 4) {
                case 0:
                case 1:
                case 2:
                case 3:
                case 4:
                case 5:
                case 6:
                case 7: {
                    // 0xxx xxxx
                    ostream.write(tag);
                    break;
                }
                case 12:
                case 13: {
                    // 110x xxxx   10xx xxxx
                    ostream.write(tag);
                    ostream.write(stream.read());
                    break;
                }
                case 14: {
                    // 1110 xxxx  10xx xxxx  10xx xxxx
                    ostream.write(tag);
                    ostream.write(stream.read());
                    ostream.write(stream.read());
                    break;
                }
                case 15: {
                    // 1111 0xxx  10xx xxxx  10xx xxxx  10xx xxxx
                    if ((tag & 0xf) <= 4) {
                        ostream.write(tag);
                        ostream.write(stream.read());
                        ostream.write(stream.read());
                        ostream.write(stream.read());
                        break;
                    }
                // no break here!! here need throw exception.
                }
                default:
                    throw new HproseException("bad utf-8 encoding at " +
                                              ((tag < 0) ? "end of stream" :
                                                  "0x" + Integer.toHexString(tag & 0xff)));
            }
        }
        ostream.write(stream.read());
    }

    private void readGuidRaw(OutputStream ostream) throws IOException {
        int len = 38;
        int off = 0;
        byte[] b = new byte[len];
        while (len > 0) {
            int size = stream.read(b, off, len);
            off += size;
            len -= size;
        }
        ostream.write(b);
    }

    private void readComplexRaw(OutputStream ostream) throws IOException {
        int tag;
        do {
            tag = stream.read();
            ostream.write(tag);
        } while (tag != HproseTags.TagOpenbrace);
        while ((tag = stream.read()) != HproseTags.TagClosebrace) {
            readRaw(ostream, tag);
        }
        ostream.write(tag);
    }

    public void reset() {
        ref.removeAllElements();
        classref.removeAllElements();
        propertyref.clear();
    }
}
