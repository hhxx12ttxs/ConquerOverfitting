package miui.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.Vector;

public class GifDecoder
{
    public static final int MAX_DECODE_SIZE = 1048576;
    protected static final int MAX_STACK_SIZE = 4096;
    public static final int STATUS_DECODE_CANCEL = 3;
    public static final int STATUS_FORMAT_ERROR = 1;
    public static final int STATUS_OK = 0;
    public static final int STATUS_OPEN_ERROR = 2;
    protected int[] act;
    protected int bgColor;
    protected int bgIndex;
    protected byte[] block = new byte[256];
    protected int blockSize = 0;
    private boolean calledOnce = false;
    protected int delay = 0;
    private int[] dest;
    protected int dispose = 0;
    protected Vector<GifFrame> frames;
    protected int[] gct;
    protected boolean gctFlag;
    protected int gctSize;
    private int height;
    protected int ih;
    protected Bitmap image;
    protected BufferedInputStream in;
    protected boolean interlace;
    protected int iw;
    protected int ix;
    protected int iy;
    protected int lastBgColor;
    protected Bitmap lastBitmap;
    protected int lastDispose = 0;
    protected int[] lct;
    protected boolean lctFlag;
    protected int lctSize;
    protected int loopCount = 1;
    protected int lrh;
    protected int lrw;
    protected int lrx;
    protected int lry;
    private boolean mCancel = false;
    private long mDecodeBmSize;
    private boolean mDecodeToTheEnd;
    private int mDecodedFrames;
    private long mMaxDecodeSize = 1048576L;
    private int mStartFrame;
    protected int pixelAspect;
    protected byte[] pixelStack;
    protected byte[] pixels;
    protected short[] prefix;
    protected int status;
    protected byte[] suffix;
    protected int transIndex;
    protected boolean transparency = false;
    private int width;

    public static boolean isGifStream(InputStream paramInputStream)
    {
        boolean bool = false;
        String str;
        if (paramInputStream != null)
            str = "";
        for (int i = 0; ; i++)
        {
            int j;
            if (i < 6)
            {
                j = readOneByte(paramInputStream);
                if (j != -1);
            }
            else
            {
                bool = str.startsWith("GIF");
                return bool;
            }
            str = str + (char)j;
        }
    }

    protected static int readOneByte(InputStream paramInputStream)
    {
        try
        {
            int j = paramInputStream.read();
            i = j;
            return i;
        }
        catch (Exception localException)
        {
            while (true)
                int i = -1;
        }
    }

    private void requestCancel()
    {
    }

    protected void decodeBitmapData()
    {
        int i = this.iw * this.ih;
        if ((this.pixels == null) || (this.pixels.length < i))
            this.pixels = new byte[i];
        if (this.prefix == null)
            this.prefix = new short[4096];
        if (this.suffix == null)
            this.suffix = new byte[4096];
        if (this.pixelStack == null)
            this.pixelStack = new byte[4097];
        int j = read();
        int k = 1 << j;
        int m = k + 1;
        int n = k + 2;
        int i1 = -1;
        int i2 = j + 1;
        int i3 = -1 + (1 << i2);
        for (int i4 = 0; i4 < k; i4++)
        {
            this.prefix[i4] = 0;
            this.suffix[i4] = ((byte)i4);
        }
        int i5 = 0;
        int i6 = 0;
        int i7 = 0;
        int i8 = 0;
        int i9 = 0;
        int i10 = 0;
        int i11 = 0;
        int i12 = 0;
        label212: int i14;
        if (i10 < i)
        {
            if (i12 != 0)
                break label625;
            if (i8 < i2)
                if (i7 == 0)
                {
                    i7 = readBlock();
                    if (i7 > 0);
                }
            int i18;
            do
            {
                int i17;
                do
                {
                    for (int i13 = i11; i13 < i; i13++)
                        this.pixels[i13] = 0;
                    i5 = 0;
                    i9 += ((0xFF & this.block[i5]) << i8);
                    i8 += 8;
                    i5++;
                    i7--;
                    break;
                    i17 = i9 & i3;
                    i9 >>= i2;
                    i8 -= i2;
                    if (i17 > n)
                        break label619;
                }
                while (i17 == m);
                if (i17 == k)
                {
                    i2 = j + 1;
                    i3 = -1 + (1 << i2);
                    n = k + 2;
                    i1 = -1;
                    break;
                }
                if (i1 == -1)
                {
                    byte[] arrayOfByte5 = this.pixelStack;
                    int i21 = i12 + 1;
                    arrayOfByte5[i12] = this.suffix[i17];
                    i1 = i17;
                    i6 = i17;
                    i12 = i21;
                    break;
                }
                i18 = i17;
                int i20;
                if (i17 == n)
                {
                    byte[] arrayOfByte4 = this.pixelStack;
                    i20 = i12 + 1;
                    arrayOfByte4[i12] = ((byte)i6);
                    i17 = i1;
                }
                int i19;
                for (i12 = i20; i17 > k; i12 = i19)
                {
                    byte[] arrayOfByte3 = this.pixelStack;
                    i19 = i12 + 1;
                    arrayOfByte3[i12] = this.suffix[i17];
                    i17 = this.prefix[i17];
                }
                i6 = 0xFF & this.suffix[i17];
            }
            while (n >= 4096);
            byte[] arrayOfByte2 = this.pixelStack;
            i14 = i12 + 1;
            arrayOfByte2[i12] = ((byte)i6);
            this.prefix[n] = ((short)i1);
            this.suffix[n] = ((byte)i6);
            n++;
            if (((n & i3) == 0) && (n < 4096))
            {
                i2++;
                i3 += n;
            }
            i1 = i18;
        }
        while (true)
        {
            int i15 = i14 - 1;
            byte[] arrayOfByte1 = this.pixels;
            int i16 = i11 + 1;
            arrayOfByte1[i11] = this.pixelStack[i15];
            i10++;
            i11 = i16;
            i12 = i15;
            break;
            return;
            label619: break label212;
            label625: i14 = i12;
        }
    }

    protected boolean err()
    {
        if (this.status != 0);
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    public Bitmap getBitmap()
    {
        return getFrame(0);
    }

    public int getDelay(int paramInt)
    {
        this.delay = -1;
        int i = getFrameCount();
        if ((paramInt >= 0) && (paramInt < i))
            this.delay = ((GifFrame)this.frames.elementAt(paramInt)).delay;
        return this.delay;
    }

    public Bitmap getFrame(int paramInt)
    {
        int i = getFrameCount();
        if (i <= 0);
        int j;
        for (Bitmap localBitmap = null; ; localBitmap = ((GifFrame)this.frames.elementAt(j)).image)
        {
            return localBitmap;
            j = paramInt % i;
        }
    }

    public int getFrameCount()
    {
        if (this.frames == null);
        for (int i = 0; ; i = this.frames.size())
            return i;
    }

    public int getHeight()
    {
        return this.height;
    }

    public int getLoopCount()
    {
        return this.loopCount;
    }

    public int getRealFrameCount()
    {
        if (this.mDecodeToTheEnd);
        for (int i = this.mDecodedFrames; ; i = 0)
            return i;
    }

    public int getWidth()
    {
        return this.width;
    }

    protected void init()
    {
        this.status = 0;
        this.frames = new Vector();
        this.gct = null;
        this.lct = null;
    }

    public boolean isDecodeToTheEnd()
    {
        return this.mDecodeToTheEnd;
    }

    protected int read()
    {
        int i = 0;
        try
        {
            int j = this.in.read();
            i = j;
            return i;
        }
        catch (Exception localException)
        {
            while (true)
                this.status = 1;
        }
    }

    public int read(InputStream paramInputStream)
    {
        this.mDecodeToTheEnd = false;
        if (this.calledOnce)
            throw new IllegalStateException("decoder cannot be called more than once");
        this.calledOnce = true;
        init();
        if (paramInputStream != null)
            this.in = new BufferedInputStream(paramInputStream);
        while (true)
        {
            try
            {
                readHeader();
                if ((!this.mCancel) && (!err()))
                {
                    readContents();
                    if (getFrameCount() < 0)
                        this.status = 1;
                }
                if (this.mCancel)
                {
                    recycle();
                    this.status = 3;
                }
                return this.status;
            }
            catch (OutOfMemoryError localOutOfMemoryError)
            {
                this.status = 2;
                recycle();
                continue;
            }
            this.status = 2;
        }
    }

    protected void readBitmap()
    {
        this.ix = readShort();
        this.iy = readShort();
        this.iw = readShort();
        this.ih = readShort();
        int i = read();
        boolean bool1;
        boolean bool2;
        label71: label103: int j;
        if ((i & 0x80) != 0)
        {
            bool1 = true;
            this.lctFlag = bool1;
            this.lctSize = (2 << (i & 0x7));
            if ((i & 0x40) == 0)
                break label159;
            bool2 = true;
            this.interlace = bool2;
            if (!this.lctFlag)
                break label164;
            this.lct = readColorTable(this.lctSize);
            this.act = this.lct;
            j = 0;
            if (this.transparency)
            {
                j = this.act[this.transIndex];
                this.act[this.transIndex] = 0;
            }
            if (this.act == null)
                this.status = 1;
            if (!err())
                break label191;
        }
        while (true)
        {
            return;
            bool1 = false;
            break;
            label159: bool2 = false;
            break label71;
            label164: this.act = this.gct;
            if (this.bgIndex != this.transIndex)
                break label103;
            this.bgColor = 0;
            break label103;
            label191: decodeBitmapData();
            skip();
            if ((!err()) && (!this.mCancel))
            {
                setPixels();
                if (this.mDecodedFrames >= this.mStartFrame)
                    this.frames.addElement(new GifFrame(this.image, this.delay));
                this.mDecodedFrames = (1 + this.mDecodedFrames);
                if (this.transparency)
                    this.act[this.transIndex] = j;
                resetFrame();
            }
        }
    }

    protected int readBlock()
    {
        this.blockSize = read();
        int i = 0;
        if (this.blockSize > 0);
        try
        {
            while (true)
            {
                int j;
                if (i < this.blockSize)
                {
                    j = this.in.read(this.block, i, this.blockSize - i);
                    if (j != -1);
                }
                else
                {
                    if (i < this.blockSize)
                        this.status = 1;
                    return i;
                }
                i += j;
            }
        }
        catch (Exception localException)
        {
            while (true)
                localException.printStackTrace();
        }
    }

    protected int[] readColorTable(int paramInt)
    {
        int i = paramInt * 3;
        int[] arrayOfInt = null;
        byte[] arrayOfByte = new byte[i];
        int j = 0;
        while (true)
        {
            int k;
            int m;
            try
            {
                int i7 = this.in.read(arrayOfByte, 0, arrayOfByte.length);
                j = i7;
                if (j < i)
                {
                    this.status = 1;
                    return arrayOfInt;
                }
            }
            catch (Exception localException)
            {
                localException.printStackTrace();
                continue;
                arrayOfInt = new int[256];
                k = 0;
                m = 0;
            }
            while (m < paramInt)
            {
                int n = k + 1;
                int i1 = 0xFF & arrayOfByte[k];
                int i2 = n + 1;
                int i3 = 0xFF & arrayOfByte[n];
                int i4 = i2 + 1;
                int i5 = 0xFF & arrayOfByte[i2];
                int i6 = m + 1;
                arrayOfInt[m] = (i5 | (0xFF000000 | i1 << 16 | i3 << 8));
                k = i4;
                m = i6;
            }
        }
    }

    protected void readContents()
    {
        this.mDecodedFrames = 0;
        int i = 0;
        while (true)
        {
            if ((i != 0) || (err()) || (this.mCancel))
                return;
            switch (read())
            {
            default:
                this.status = 1;
                break;
            case 44:
                int k = this.frames.size();
                readBitmap();
                if (this.frames.size() > k)
                    this.mDecodeBmSize += this.image.getRowBytes() * this.image.getHeight();
                if (this.mDecodeBmSize > this.mMaxDecodeSize)
                    i = 1;
                break;
            case 33:
                switch (read())
                {
                default:
                    skip();
                    break;
                case 249:
                    readGraphicControlExt();
                    break;
                case 255:
                    readBlock();
                    String str = "";
                    for (int j = 0; j < 11; j++)
                        str = str + (char)this.block[j];
                    if (str.equals("NETSCAPE2.0"))
                        readNetscapeExt();
                    else
                        skip();
                    break;
                case 254:
                    skip();
                    break;
                case 1:
                    skip();
                }
                break;
            case 59:
                i = 1;
                this.mDecodeToTheEnd = true;
            }
        }
    }

    protected void readGraphicControlExt()
    {
        int i = 1;
        read();
        int j = read();
        this.dispose = ((j & 0x1C) >> 2);
        if (this.dispose == 0)
            this.dispose = i;
        if ((j & 0x1) != 0);
        while (true)
        {
            this.transparency = i;
            this.delay = (10 * readShort());
            if (this.delay <= 0)
                this.delay = 100;
            this.transIndex = read();
            read();
            return;
            i = 0;
        }
    }

    protected void readHeader()
    {
        if (this.mCancel);
        while (true)
        {
            return;
            String str = "";
            for (int i = 0; i < 6; i++)
                str = str + (char)read();
            if (!str.startsWith("GIF"))
            {
                this.status = 1;
            }
            else
            {
                readLSD();
                if ((this.gctFlag) && (!err()))
                {
                    this.gct = readColorTable(this.gctSize);
                    this.bgColor = this.gct[this.bgIndex];
                }
            }
        }
    }

    protected void readLSD()
    {
        this.width = readShort();
        this.height = readShort();
        int i = read();
        if ((i & 0x80) != 0);
        for (boolean bool = true; ; bool = false)
        {
            this.gctFlag = bool;
            this.gctSize = (2 << (i & 0x7));
            this.bgIndex = read();
            this.pixelAspect = read();
            return;
        }
    }

    protected void readNetscapeExt()
    {
        do
        {
            readBlock();
            if (this.block[0] == 1)
                this.loopCount = (0xFF & this.block[1] | (0xFF & this.block[2]) << 8);
        }
        while ((this.blockSize > 0) && (!err()));
    }

    protected int readShort()
    {
        return read() | read() << 8;
    }

    public void recycle()
    {
        if (this.frames != null)
        {
            int i = this.frames.size();
            for (int j = 0; j < i; j++)
                ((GifFrame)this.frames.elementAt(j)).recycle();
        }
    }

    public void requestCancelDecode()
    {
        this.mCancel = true;
        requestCancel();
    }

    protected void resetFrame()
    {
        this.lastDispose = this.dispose;
        this.lrx = this.ix;
        this.lry = this.iy;
        this.lrw = this.iw;
        this.lrh = this.ih;
        this.lastBitmap = this.image;
        this.lastBgColor = this.bgColor;
        this.dispose = 0;
        this.transparency = false;
        this.delay = 0;
        this.lct = null;
    }

    public void setMaxDecodeSize(long paramLong)
    {
        this.mMaxDecodeSize = paramLong;
    }

    protected void setPixels()
    {
        if (this.dest == null)
            this.dest = new int[this.width * this.height];
        if (this.lastDispose > 0)
        {
            if (this.lastDispose == 3)
            {
                int i14 = -2 + getFrameCount();
                if (i14 <= 0)
                    break label199;
                Bitmap localBitmap = getFrame(i14 - 1);
                if (!localBitmap.equals(this.lastBitmap))
                {
                    this.lastBitmap = localBitmap;
                    this.lastBitmap.getPixels(this.dest, 0, this.width, 0, 0, this.width, this.height);
                }
            }
            if ((this.lastBitmap != null) && (this.lastDispose == 2))
            {
                int i9 = 0;
                if (!this.transparency)
                    i9 = this.lastBgColor;
                int i10 = this.lry * this.width + this.lrx;
                for (int i11 = 0; ; i11++)
                {
                    if (i11 >= this.lrh)
                        break label237;
                    int i12 = i10 + this.lrw;
                    int i13 = i10;
                    while (true)
                        if (i13 < i12)
                        {
                            this.dest[i13] = i9;
                            i13++;
                            continue;
                            label199: this.lastBitmap = null;
                            this.dest = new int[this.width * this.height];
                            break;
                        }
                    i10 += this.width;
                }
            }
        }
        label237: int i = 1;
        int j = 8;
        int k = 0;
        for (int m = 0; m < this.ih; m++)
        {
            int n = m;
            if (this.interlace)
                if (k >= this.ih)
                {
                    i++;
                    switch (i)
                    {
                    default:
                    case 2:
                    case 3:
                    case 4:
                    }
                }
            while (true)
            {
                n = k;
                k += j;
                int i1 = n + this.iy;
                if (i1 >= this.height)
                    break;
                int i2 = i1 * this.width;
                int i3 = i2 + this.ix;
                int i4 = i3 + this.iw;
                if (i2 + this.width < i4)
                    i4 = i2 + this.width;
                int i6;
                for (int i5 = m * this.iw; i3 < i4; i5 = i6)
                {
                    byte[] arrayOfByte = this.pixels;
                    i6 = i5 + 1;
                    int i7 = 0xFF & arrayOfByte[i5];
                    int i8 = this.act[i7];
                    if (i8 != 0)
                        this.dest[i3] = i8;
                    i3++;
                }
                k = 4;
                continue;
                k = 2;
                j = 4;
                continue;
                k = 1;
                j = 2;
            }
        }
        if ((this.mDecodedFrames <= this.mStartFrame) && (this.image != null) && (!this.image.isRecycled()))
            this.image.recycle();
        this.image = Bitmap.createBitmap(this.dest, this.width, this.height, Bitmap.Config.ARGB_8888);
    }

    public void setStartFrame(int paramInt)
    {
        this.mStartFrame = paramInt;
    }

    protected void skip()
    {
        do
            readBlock();
        while ((this.blockSize > 0) && (!err()));
    }

    private static class GifFrame
    {
        public int delay;
        public Bitmap image;

        public GifFrame(Bitmap paramBitmap, int paramInt)
        {
            this.image = paramBitmap;
            this.delay = paramInt;
        }

        public void recycle()
        {
            if ((this.image != null) && (!this.image.isRecycled()))
                this.image.recycle();
        }
    }
}

/* Location:                     /home/lithium/miui/chameleon/2.11.16/framework2_dex2jar.jar
 * Qualified Name:         miui.util.GifDecoder
 * JD-Core Version:        0.6.2
 */
