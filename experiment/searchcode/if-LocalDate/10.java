package android.media;

import java.io.IOException;
import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TimeZone;

public class ExifInterface
{
    public static final int ORIENTATION_FLIP_HORIZONTAL = 2;
    public static final int ORIENTATION_FLIP_VERTICAL = 4;
    public static final int ORIENTATION_NORMAL = 1;
    public static final int ORIENTATION_ROTATE_180 = 3;
    public static final int ORIENTATION_ROTATE_270 = 8;
    public static final int ORIENTATION_ROTATE_90 = 6;
    public static final int ORIENTATION_TRANSPOSE = 5;
    public static final int ORIENTATION_TRANSVERSE = 7;
    public static final int ORIENTATION_UNDEFINED = 0;
    public static final String TAG_APERTURE = "FNumber";
    public static final String TAG_DATETIME = "DateTime";
    public static final String TAG_EXPOSURE_TIME = "ExposureTime";
    public static final String TAG_FLASH = "Flash";
    public static final String TAG_FOCAL_LENGTH = "FocalLength";
    public static final String TAG_GPS_ALTITUDE = "GPSAltitude";
    public static final String TAG_GPS_ALTITUDE_REF = "GPSAltitudeRef";
    public static final String TAG_GPS_DATESTAMP = "GPSDateStamp";
    public static final String TAG_GPS_LATITUDE = "GPSLatitude";
    public static final String TAG_GPS_LATITUDE_REF = "GPSLatitudeRef";
    public static final String TAG_GPS_LONGITUDE = "GPSLongitude";
    public static final String TAG_GPS_LONGITUDE_REF = "GPSLongitudeRef";
    public static final String TAG_GPS_PROCESSING_METHOD = "GPSProcessingMethod";
    public static final String TAG_GPS_TIMESTAMP = "GPSTimeStamp";
    public static final String TAG_IMAGE_LENGTH = "ImageLength";
    public static final String TAG_IMAGE_WIDTH = "ImageWidth";
    public static final String TAG_ISO = "ISOSpeedRatings";
    public static final String TAG_MAKE = "Make";
    public static final String TAG_MODEL = "Model";
    public static final String TAG_ORIENTATION = "Orientation";
    public static final String TAG_WHITE_BALANCE = "WhiteBalance";
    public static final int WHITEBALANCE_AUTO = 0;
    public static final int WHITEBALANCE_MANUAL = 1;
    private static SimpleDateFormat sFormatter;
    private static final Object sLock = new Object();
    private HashMap<String, String> mAttributes;
    private String mFilename;
    private boolean mHasThumbnail;

    static
    {
        System.loadLibrary("exif_jni");
        sFormatter = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
        sFormatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    public ExifInterface(String paramString)
        throws IOException
    {
        this.mFilename = paramString;
        loadAttributes();
    }

    private native boolean appendThumbnailNative(String paramString1, String paramString2);

    private native void commitChangesNative(String paramString);

    private static float convertRationalLatLonToFloat(String paramString1, String paramString2)
    {
        try
        {
            String[] arrayOfString1 = paramString1.split(",");
            String[] arrayOfString2 = arrayOfString1[0].split("/");
            double d1 = Double.parseDouble(arrayOfString2[0].trim()) / Double.parseDouble(arrayOfString2[1].trim());
            String[] arrayOfString3 = arrayOfString1[1].split("/");
            double d2 = Double.parseDouble(arrayOfString3[0].trim()) / Double.parseDouble(arrayOfString3[1].trim());
            String[] arrayOfString4 = arrayOfString1[2].split("/");
            double d3 = Double.parseDouble(arrayOfString4[0].trim()) / Double.parseDouble(arrayOfString4[1].trim());
            double d4 = d1 + d2 / 60.0D + d3 / 3600.0D;
            if (!paramString2.equals("S"))
            {
                boolean bool = paramString2.equals("W");
                if (!bool)
                    break label159;
            }
            label159: for (float f = (float)-d4; ; f = (float)d4)
                return f;
        }
        catch (NumberFormatException localNumberFormatException)
        {
            throw new IllegalArgumentException();
        }
        catch (ArrayIndexOutOfBoundsException localArrayIndexOutOfBoundsException)
        {
        }
        throw new IllegalArgumentException();
    }

    private native String getAttributesNative(String paramString);

    private native byte[] getThumbnailNative(String paramString);

    private void loadAttributes()
        throws IOException
    {
        this.mAttributes = new HashMap();
        while (true)
        {
            String str2;
            String str3;
            synchronized (sLock)
            {
                String str1 = getAttributesNative(this.mFilename);
                int i = str1.indexOf(' ');
                int j = Integer.parseInt(str1.substring(0, i));
                int k = i + 1;
                int m = 0;
                if (m >= j)
                    break;
                int n = str1.indexOf('=', k);
                str2 = str1.substring(k, n);
                int i1 = n + 1;
                int i2 = str1.indexOf(' ', i1);
                int i3 = Integer.parseInt(str1.substring(i1, i2));
                int i4 = i2 + 1;
                str3 = str1.substring(i4, i4 + i3);
                k = i4 + i3;
                if (str2.equals("hasThumbnail"))
                {
                    this.mHasThumbnail = str3.equalsIgnoreCase("true");
                    m++;
                }
            }
            this.mAttributes.put(str2, str3);
        }
    }

    private native void saveAttributesNative(String paramString1, String paramString2);

    public double getAltitude(double paramDouble)
    {
        int i = -1;
        double d = getAttributeDouble("GPSAltitude", -1.0D);
        int j = getAttributeInt("GPSAltitudeRef", i);
        if ((d >= 0.0D) && (j >= 0))
            if (j != 1)
                break label49;
        while (true)
        {
            paramDouble = d * i;
            return paramDouble;
            label49: i = 1;
        }
    }

    public String getAttribute(String paramString)
    {
        return (String)this.mAttributes.get(paramString);
    }

    public double getAttributeDouble(String paramString, double paramDouble)
    {
        String str = (String)this.mAttributes.get(paramString);
        if (str == null);
        while (true)
        {
            return paramDouble;
            try
            {
                int i = str.indexOf("/");
                if (i != -1)
                {
                    double d1 = Double.parseDouble(str.substring(i + 1));
                    if (d1 != 0.0D)
                    {
                        double d2 = Double.parseDouble(str.substring(0, i));
                        paramDouble = d2 / d1;
                    }
                }
            }
            catch (NumberFormatException localNumberFormatException)
            {
            }
        }
    }

    public int getAttributeInt(String paramString, int paramInt)
    {
        String str = (String)this.mAttributes.get(paramString);
        if (str == null);
        while (true)
        {
            return paramInt;
            try
            {
                int i = Integer.valueOf(str).intValue();
                paramInt = i;
            }
            catch (NumberFormatException localNumberFormatException)
            {
            }
        }
    }

    public long getDateTime()
    {
        long l1 = -1L;
        String str = (String)this.mAttributes.get("DateTime");
        if (str == null);
        while (true)
        {
            return l1;
            ParsePosition localParsePosition = new ParsePosition(0);
            try
            {
                Date localDate = sFormatter.parse(str, localParsePosition);
                if (localDate != null)
                {
                    long l2 = localDate.getTime();
                    l1 = l2;
                }
            }
            catch (IllegalArgumentException localIllegalArgumentException)
            {
            }
        }
    }

    public long getGpsDateTime()
    {
        long l1 = -1L;
        String str1 = (String)this.mAttributes.get("GPSDateStamp");
        String str2 = (String)this.mAttributes.get("GPSTimeStamp");
        if ((str1 == null) || (str2 == null));
        while (true)
        {
            return l1;
            String str3 = str1 + ' ' + str2;
            if (str3 != null)
            {
                ParsePosition localParsePosition = new ParsePosition(0);
                try
                {
                    Date localDate = sFormatter.parse(str3, localParsePosition);
                    if (localDate != null)
                    {
                        long l2 = localDate.getTime();
                        l1 = l2;
                    }
                }
                catch (IllegalArgumentException localIllegalArgumentException)
                {
                }
            }
        }
    }

    public boolean getLatLong(float[] paramArrayOfFloat)
    {
        boolean bool = true;
        String str1 = (String)this.mAttributes.get("GPSLatitude");
        String str2 = (String)this.mAttributes.get("GPSLatitudeRef");
        String str3 = (String)this.mAttributes.get("GPSLongitude");
        String str4 = (String)this.mAttributes.get("GPSLongitudeRef");
        if ((str1 != null) && (str2 != null) && (str3 != null) && (str4 != null));
        while (true)
        {
            try
            {
                paramArrayOfFloat[0] = convertRationalLatLonToFloat(str1, str2);
                paramArrayOfFloat[1] = convertRationalLatLonToFloat(str3, str4);
                return bool;
            }
            catch (IllegalArgumentException localIllegalArgumentException)
            {
            }
            bool = false;
        }
    }

    public byte[] getThumbnail()
    {
        synchronized (sLock)
        {
            byte[] arrayOfByte = getThumbnailNative(this.mFilename);
            return arrayOfByte;
        }
    }

    public boolean hasThumbnail()
    {
        return this.mHasThumbnail;
    }

    public void saveAttributes()
        throws IOException
    {
        StringBuilder localStringBuilder = new StringBuilder();
        int i = this.mAttributes.size();
        if (this.mAttributes.containsKey("hasThumbnail"))
            i--;
        localStringBuilder.append(i + " ");
        Iterator localIterator = this.mAttributes.entrySet().iterator();
        while (localIterator.hasNext())
        {
            Map.Entry localEntry = (Map.Entry)localIterator.next();
            String str2 = (String)localEntry.getKey();
            if (!str2.equals("hasThumbnail"))
            {
                String str3 = (String)localEntry.getValue();
                localStringBuilder.append(str2 + "=");
                localStringBuilder.append(str3.length() + " ");
                localStringBuilder.append(str3);
            }
        }
        String str1 = localStringBuilder.toString();
        synchronized (sLock)
        {
            saveAttributesNative(this.mFilename, str1);
            commitChangesNative(this.mFilename);
            return;
        }
    }

    public void setAttribute(String paramString1, String paramString2)
    {
        this.mAttributes.put(paramString1, paramString2);
    }
}

/* Location:                     /home/lithium/miui/chameleon/2.11.16/framework_dex2jar.jar
 * Qualified Name:         android.media.ExifInterface
 * JD-Core Version:        0.6.2
 */
