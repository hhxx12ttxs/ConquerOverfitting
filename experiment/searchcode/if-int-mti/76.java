package com.android.internal.telephony.gsm;

import android.annotation.MiuiHook;
import android.annotation.MiuiHook.MiuiHookType;
import android.telephony.PhoneNumberUtils;
import android.telephony.SmsMessage.MessageClass;
import android.text.format.Time;
import android.util.Log;
import com.android.internal.telephony.EncodeException;
import com.android.internal.telephony.GsmAlphabet;
import com.android.internal.telephony.IccUtils;
import com.android.internal.telephony.SmsAddress;
import com.android.internal.telephony.SmsHeader;
import com.android.internal.telephony.SmsHeader.PortAddrs;
import com.android.internal.telephony.SmsMessageBase;
import com.android.internal.telephony.SmsMessageBase.SubmitPduBase;
import com.android.internal.telephony.SmsMessageBase.TextEncodingDetails;
import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;

public class SmsMessage extends SmsMessageBase
{
    static final String LOG_TAG = "GSM";
    private boolean automaticDeletion;
    private int dataCodingScheme;
    private long dischargeTimeMillis;
    private boolean forSubmit;
    private boolean isStatusReportMessage = false;
    private SmsMessage.MessageClass messageClass;
    private int mti;
    private int protocolIdentifier;
    private GsmSmsAddress recipientAddress;
    private boolean replyPathPresent = false;
    private int status;

    public static SmsMessageBase.TextEncodingDetails calculateLength(CharSequence paramCharSequence, boolean paramBoolean)
    {
        SmsMessageBase.TextEncodingDetails localTextEncodingDetails = GsmAlphabet.countGsmSeptets(paramCharSequence, paramBoolean);
        int i;
        if (localTextEncodingDetails == null)
        {
            localTextEncodingDetails = new SmsMessageBase.TextEncodingDetails();
            i = 2 * paramCharSequence.length();
            localTextEncodingDetails.codeUnitCount = paramCharSequence.length();
            if (i <= 140)
                break label80;
            localTextEncodingDetails.msgCount = ((i + 133) / 134);
        }
        for (localTextEncodingDetails.codeUnitsRemaining = ((134 * localTextEncodingDetails.msgCount - i) / 2); ; localTextEncodingDetails.codeUnitsRemaining = ((140 - i) / 2))
        {
            localTextEncodingDetails.codeUnitSize = 3;
            return localTextEncodingDetails;
            label80: localTextEncodingDetails.msgCount = 1;
        }
    }

    public static SmsMessage createFromEfRecord(int paramInt, byte[] paramArrayOfByte)
    {
        SmsMessage localSmsMessage;
        try
        {
            localSmsMessage = new SmsMessage();
            localSmsMessage.indexOnIcc = paramInt;
            if ((0x1 & paramArrayOfByte[0]) == 0)
            {
                Log.w("GSM", "SMS parsing failed: Trying to parse a free record");
                localSmsMessage = null;
            }
            else
            {
                localSmsMessage.statusOnIcc = (0x7 & paramArrayOfByte[0]);
                int i = -1 + paramArrayOfByte.length;
                byte[] arrayOfByte = new byte[i];
                System.arraycopy(paramArrayOfByte, 1, arrayOfByte, 0, i);
                localSmsMessage.parsePdu(arrayOfByte);
            }
        }
        catch (RuntimeException localRuntimeException)
        {
            Log.e("GSM", "SMS PDU parsing failed: ", localRuntimeException);
            localSmsMessage = null;
        }
        return localSmsMessage;
    }

    public static SmsMessage createFromPdu(byte[] paramArrayOfByte)
    {
        try
        {
            localSmsMessage = new SmsMessage();
            localSmsMessage.parsePdu(paramArrayOfByte);
            return localSmsMessage;
        }
        catch (RuntimeException localRuntimeException)
        {
            while (true)
            {
                Log.e("GSM", "SMS PDU parsing failed: ", localRuntimeException);
                SmsMessage localSmsMessage = null;
            }
        }
    }

    private static byte[] encodeUCS2(String paramString, byte[] paramArrayOfByte)
        throws UnsupportedEncodingException
    {
        byte[] arrayOfByte1 = paramString.getBytes("utf-16be");
        byte[] arrayOfByte2;
        if (paramArrayOfByte != null)
        {
            arrayOfByte2 = new byte[1 + (paramArrayOfByte.length + arrayOfByte1.length)];
            arrayOfByte2[0] = ((byte)paramArrayOfByte.length);
            System.arraycopy(paramArrayOfByte, 0, arrayOfByte2, 1, paramArrayOfByte.length);
            System.arraycopy(arrayOfByte1, 0, arrayOfByte2, 1 + paramArrayOfByte.length, arrayOfByte1.length);
        }
        while (true)
        {
            byte[] arrayOfByte3 = new byte[1 + arrayOfByte2.length];
            arrayOfByte3[0] = ((byte)(0xFF & arrayOfByte2.length));
            System.arraycopy(arrayOfByte2, 0, arrayOfByte3, 1, arrayOfByte2.length);
            return arrayOfByte3;
            arrayOfByte2 = arrayOfByte1;
        }
    }

    public static SubmitPdu getSubmitPdu(String paramString1, String paramString2, int paramInt, byte[] paramArrayOfByte, boolean paramBoolean)
    {
        SmsHeader.PortAddrs localPortAddrs = new SmsHeader.PortAddrs();
        localPortAddrs.destPort = paramInt;
        localPortAddrs.origPort = 0;
        localPortAddrs.areEightBits = false;
        SmsHeader localSmsHeader = new SmsHeader();
        localSmsHeader.portAddrs = localPortAddrs;
        byte[] arrayOfByte = SmsHeader.toByteArray(localSmsHeader);
        SubmitPdu localSubmitPdu;
        if (1 + (paramArrayOfByte.length + arrayOfByte.length) > 140)
        {
            Log.e("GSM", "SMS data message may only contain " + (-1 + (140 - arrayOfByte.length)) + " bytes");
            localSubmitPdu = null;
        }
        while (true)
        {
            return localSubmitPdu;
            localSubmitPdu = new SubmitPdu();
            ByteArrayOutputStream localByteArrayOutputStream = getSubmitPduHead(paramString1, paramString2, (byte)65, paramBoolean, localSubmitPdu);
            localByteArrayOutputStream.write(4);
            localByteArrayOutputStream.write(1 + (paramArrayOfByte.length + arrayOfByte.length));
            localByteArrayOutputStream.write(arrayOfByte.length);
            localByteArrayOutputStream.write(arrayOfByte, 0, arrayOfByte.length);
            localByteArrayOutputStream.write(paramArrayOfByte, 0, paramArrayOfByte.length);
            localSubmitPdu.encodedMessage = localByteArrayOutputStream.toByteArray();
        }
    }

    public static SubmitPdu getSubmitPdu(String paramString1, String paramString2, String paramString3, boolean paramBoolean)
    {
        return getSubmitPdu(paramString1, paramString2, paramString3, paramBoolean, null);
    }

    public static SubmitPdu getSubmitPdu(String paramString1, String paramString2, String paramString3, boolean paramBoolean, byte[] paramArrayOfByte)
    {
        return getSubmitPdu(paramString1, paramString2, paramString3, paramBoolean, paramArrayOfByte, 0, 0, 0);
    }

    public static SubmitPdu getSubmitPdu(String paramString1, String paramString2, String paramString3, boolean paramBoolean, byte[] paramArrayOfByte, int paramInt1, int paramInt2, int paramInt3)
    {
        SubmitPdu localSubmitPdu;
        if ((paramString3 == null) || (paramString2 == null))
            localSubmitPdu = null;
        label178: label196: ByteArrayOutputStream localByteArrayOutputStream;
        Object localObject;
        while (true)
        {
            return localSubmitPdu;
            if (paramInt1 == 0)
            {
                SmsMessageBase.TextEncodingDetails localTextEncodingDetails = calculateLength(paramString3, false);
                paramInt1 = localTextEncodingDetails.codeUnitSize;
                paramInt2 = localTextEncodingDetails.languageTable;
                paramInt3 = localTextEncodingDetails.languageShiftTable;
                if ((paramInt1 == 1) && ((paramInt2 != 0) || (paramInt3 != 0)))
                {
                    if (paramArrayOfByte == null)
                        break label296;
                    SmsHeader localSmsHeader2 = SmsHeader.fromByteArray(paramArrayOfByte);
                    if ((localSmsHeader2.languageTable != paramInt2) || (localSmsHeader2.languageShiftTable != paramInt3))
                    {
                        Log.w("GSM", "Updating language table in SMS header: " + localSmsHeader2.languageTable + " -> " + paramInt2 + ", " + localSmsHeader2.languageShiftTable + " -> " + paramInt3);
                        localSmsHeader2.languageTable = paramInt2;
                        localSmsHeader2.languageShiftTable = paramInt3;
                        paramArrayOfByte = SmsHeader.toByteArray(localSmsHeader2);
                    }
                }
            }
            localSubmitPdu = new SubmitPdu();
            int i;
            if (paramArrayOfByte != null)
            {
                i = 64;
                localByteArrayOutputStream = getSubmitPduHead(paramString1, paramString2, (byte)(i | 0x1), paramBoolean, localSubmitPdu);
                if (paramInt1 != 1)
                    break label335;
            }
            try
            {
                byte[] arrayOfByte3 = GsmAlphabet.stringToGsm7BitPackedWithHeader(paramString3, paramArrayOfByte, paramInt2, paramInt3);
                localObject = arrayOfByte3;
                while (true)
                {
                    if (paramInt1 != 1)
                        break label436;
                    if ((0xFF & localObject[0]) <= 160)
                        break label406;
                    Log.e("GSM", "Message too long (" + (0xFF & localObject[0]) + " septets)");
                    localSubmitPdu = null;
                    break;
                    label296: SmsHeader localSmsHeader1 = new SmsHeader();
                    localSmsHeader1.languageTable = paramInt2;
                    localSmsHeader1.languageShiftTable = paramInt3;
                    paramArrayOfByte = SmsHeader.toByteArray(localSmsHeader1);
                    break label178;
                    i = 0;
                    break label196;
                    try
                    {
                        label335: byte[] arrayOfByte2 = encodeUCS2(paramString3, paramArrayOfByte);
                        localObject = arrayOfByte2;
                    }
                    catch (UnsupportedEncodingException localUnsupportedEncodingException2)
                    {
                        Log.e("GSM", "Implausible UnsupportedEncodingException ", localUnsupportedEncodingException2);
                        localSubmitPdu = null;
                    }
                }
            }
            catch (EncodeException localEncodeException)
            {
                try
                {
                    byte[] arrayOfByte1 = encodeUCS2(paramString3, paramArrayOfByte);
                    localObject = arrayOfByte1;
                    paramInt1 = 3;
                }
                catch (UnsupportedEncodingException localUnsupportedEncodingException1)
                {
                    Log.e("GSM", "Implausible UnsupportedEncodingException ", localUnsupportedEncodingException1);
                    localSubmitPdu = null;
                }
            }
        }
        label406: localByteArrayOutputStream.write(0);
        while (true)
        {
            localByteArrayOutputStream.write((byte[])localObject, 0, localObject.length);
            localSubmitPdu.encodedMessage = localByteArrayOutputStream.toByteArray();
            break;
            label436: if ((0xFF & localObject[0]) > 140)
            {
                Log.e("GSM", "Message too long (" + (0xFF & localObject[0]) + " bytes)");
                localSubmitPdu = null;
                break;
            }
            localByteArrayOutputStream.write(8);
        }
    }

    private static ByteArrayOutputStream getSubmitPduHead(String paramString1, String paramString2, byte paramByte, boolean paramBoolean, SubmitPdu paramSubmitPdu)
    {
        ByteArrayOutputStream localByteArrayOutputStream = new ByteArrayOutputStream(180);
        byte[] arrayOfByte;
        int i;
        if (paramString1 == null)
        {
            paramSubmitPdu.encodedScAddress = null;
            if (paramBoolean)
                paramByte |= 32;
            localByteArrayOutputStream.write(paramByte);
            localByteArrayOutputStream.write(0);
            arrayOfByte = PhoneNumberUtils.networkPortionToCalledPartyBCD(paramString2);
            i = 2 * (-1 + arrayOfByte.length);
            if ((0xF0 & arrayOfByte[(-1 + arrayOfByte.length)]) != 240)
                break label123;
        }
        label123: for (int j = 1; ; j = 0)
        {
            localByteArrayOutputStream.write(i - j);
            localByteArrayOutputStream.write(arrayOfByte, 0, arrayOfByte.length);
            localByteArrayOutputStream.write(0);
            return localByteArrayOutputStream;
            paramSubmitPdu.encodedScAddress = PhoneNumberUtils.networkPortionToCalledPartyBCDWithLength(paramString1);
            break;
        }
    }

    public static int getTPLayerLengthForPDU(String paramString)
    {
        return -1 + (paramString.length() / 2 - Integer.parseInt(paramString.substring(0, 2), 16));
    }

    public static SmsMessage newFromCDS(String paramString)
    {
        try
        {
            localSmsMessage = new SmsMessage();
            localSmsMessage.parsePdu(IccUtils.hexStringToBytes(paramString));
            return localSmsMessage;
        }
        catch (RuntimeException localRuntimeException)
        {
            while (true)
            {
                Log.e("GSM", "CDS SMS PDU parsing failed: ", localRuntimeException);
                SmsMessage localSmsMessage = null;
            }
        }
    }

    public static SmsMessage newFromCMT(String[] paramArrayOfString)
    {
        try
        {
            localSmsMessage = new SmsMessage();
            localSmsMessage.parsePdu(IccUtils.hexStringToBytes(paramArrayOfString[1]));
            return localSmsMessage;
        }
        catch (RuntimeException localRuntimeException)
        {
            while (true)
            {
                Log.e("GSM", "SMS PDU parsing failed: ", localRuntimeException);
                SmsMessage localSmsMessage = null;
            }
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.CHANGE_CODE)
    private void parsePdu(byte[] paramArrayOfByte)
    {
        this.mPdu = paramArrayOfByte;
        PduParser localPduParser = new PduParser(paramArrayOfByte);
        this.scAddress = localPduParser.getSCAddress();
        if (this.scAddress != null);
        int i = localPduParser.getByte();
        this.mti = (i & 0x3);
        switch (this.mti)
        {
        default:
            throw new RuntimeException("Unsupported message type");
        case 0:
        case 3:
            parseSmsDeliver(localPduParser, i);
        case 1:
        case 2:
        }
        while (true)
        {
            return;
            parseSmsSubmit(localPduParser, i);
            continue;
            parseSmsStatusReport(localPduParser, i);
        }
    }

    private void parseSmsDeliver(PduParser paramPduParser, int paramInt)
    {
        boolean bool1;
        if ((paramInt & 0x80) == 128)
        {
            bool1 = true;
            this.replyPathPresent = bool1;
            this.originatingAddress = paramPduParser.getAddress();
            if (this.originatingAddress != null);
            this.protocolIdentifier = paramPduParser.getByte();
            this.dataCodingScheme = paramPduParser.getByte();
            this.scTimeMillis = paramPduParser.getSCTimestampMillis();
            if ((paramInt & 0x40) != 64)
                break label82;
        }
        label82: for (boolean bool2 = true; ; bool2 = false)
        {
            parseUserData(paramPduParser, bool2);
            return;
            bool1 = false;
            break;
        }
    }

    private void parseSmsStatusReport(PduParser paramPduParser, int paramInt)
    {
        this.isStatusReportMessage = true;
        if ((paramInt & 0x20) == 0);
        int i;
        for (boolean bool1 = true; ; bool1 = false)
        {
            this.forSubmit = bool1;
            this.messageRef = paramPduParser.getByte();
            this.recipientAddress = paramPduParser.getAddress();
            this.scTimeMillis = paramPduParser.getSCTimestampMillis();
            this.dischargeTimeMillis = paramPduParser.getSCTimestampMillis();
            this.status = paramPduParser.getByte();
            if (!paramPduParser.moreDataPresent())
                break label155;
            i = paramPduParser.getByte();
            for (int j = i; (j & 0x80) != 0; j = paramPduParser.getByte());
        }
        if ((i & 0x1) != 0)
            this.protocolIdentifier = paramPduParser.getByte();
        if ((i & 0x2) != 0)
            this.dataCodingScheme = paramPduParser.getByte();
        if ((i & 0x4) != 0)
            if ((paramInt & 0x40) != 64)
                break label156;
        label155: label156: for (boolean bool2 = true; ; bool2 = false)
        {
            parseUserData(paramPduParser, bool2);
            return;
        }
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    private void parseSmsSubmit(PduParser paramPduParser, int paramInt)
    {
        boolean bool1;
        if ((paramInt & 0x80) == 128)
        {
            bool1 = true;
            this.replyPathPresent = bool1;
            this.messageRef = paramPduParser.getByte();
            this.recipientAddress = paramPduParser.getAddress();
            if (this.recipientAddress != null)
                Log.v("GSM", "SMS recipient address: " + this.recipientAddress.address);
            this.protocolIdentifier = paramPduParser.getByte();
            this.dataCodingScheme = paramPduParser.getByte();
            if ((paramInt & 0x40) != 64)
                break label114;
        }
        label114: for (boolean bool2 = true; ; bool2 = false)
        {
            parseUserData(paramPduParser, bool2);
            return;
            bool1 = false;
            break;
        }
    }

    private void parseUserData(PduParser paramPduParser, boolean paramBoolean)
    {
        boolean bool1 = true;
        int i = 0;
        int j = 0;
        boolean bool2 = false;
        boolean bool5;
        boolean bool6;
        label54: label67: label111: int m;
        if ((0x80 & this.dataCodingScheme) == 0)
            if ((0x40 & this.dataCodingScheme) != 0)
            {
                bool5 = bool1;
                this.automaticDeletion = bool5;
                if ((0x20 & this.dataCodingScheme) == 0)
                    break label202;
                bool6 = bool1;
                if ((0x10 & this.dataCodingScheme) == 0)
                    break label208;
                j = bool1;
                if (!bool6)
                    break label214;
                Log.w("GSM", "4 - Unsupported SMS data coding scheme (compression) " + (0xFF & this.dataCodingScheme));
                if (bool2 != bool1)
                    break label630;
                m = paramPduParser.constructUserData(paramBoolean, bool1);
                this.userData = paramPduParser.getUserData();
                this.userDataHeader = paramPduParser.getUserDataHeader();
                switch (bool2)
                {
                default:
                    label172: if (this.messageBody != null)
                        parseMessageBody();
                    if (j == 0)
                        this.messageClass = SmsMessage.MessageClass.UNKNOWN;
                    break;
                case 0:
                case 2:
                case 1:
                case 3:
                case 4:
                }
            }
        while (true)
        {
            return;
            bool5 = false;
            break;
            label202: bool6 = false;
            break label54;
            label208: j = 0;
            break label67;
            label214: int k;
            switch (0x3 & this.dataCodingScheme >> 2)
            {
            default:
                break;
            case 0:
                bool2 = true;
                break;
            case 2:
                k = 3;
                break;
            case 1:
            case 3:
                Log.w("GSM", "1 - Unsupported SMS data coding scheme " + (0xFF & this.dataCodingScheme));
                k = 2;
                break;
                if ((0xF0 & this.dataCodingScheme) == 240)
                {
                    this.automaticDeletion = false;
                    j = 1;
                    if ((0x4 & this.dataCodingScheme) == 0)
                    {
                        k = 1;
                        break;
                    }
                    k = 2;
                    break;
                }
                if (((0xF0 & this.dataCodingScheme) == 192) || ((0xF0 & this.dataCodingScheme) == 208) || ((0xF0 & this.dataCodingScheme) == 224))
                {
                    label408: boolean bool3;
                    if ((0xF0 & this.dataCodingScheme) == 224)
                    {
                        k = 3;
                        if ((0x8 & this.dataCodingScheme) != 8)
                            break label475;
                        bool3 = bool1;
                        label423: if ((0x3 & this.dataCodingScheme) != 0)
                            break label487;
                        this.isMwi = bool1;
                        this.mwiSense = bool3;
                        if ((0xF0 & this.dataCodingScheme) != 192)
                            break label481;
                    }
                    label475: label481: for (boolean bool4 = bool1; ; bool4 = false)
                    {
                        this.mwiDontStore = bool4;
                        break;
                        k = 1;
                        break label408;
                        bool3 = false;
                        break label423;
                    }
                    label487: this.isMwi = false;
                    Log.w("GSM", "MWI for fax, email, or other " + (0xFF & this.dataCodingScheme));
                    break;
                }
                if ((0xC0 & this.dataCodingScheme) == 128)
                {
                    if (this.dataCodingScheme == 132)
                    {
                        k = 4;
                        break;
                    }
                    Log.w("GSM", "5 - Unsupported SMS data coding scheme " + (0xFF & this.dataCodingScheme));
                    break;
                }
                Log.w("GSM", "3 - Unsupported SMS data coding scheme " + (0xFF & this.dataCodingScheme));
                break;
                label630: bool1 = false;
                break label111;
                this.messageBody = null;
                break label172;
                if (paramBoolean);
                for (int n = this.userDataHeader.languageTable; ; n = 0)
                {
                    if (paramBoolean)
                        i = this.userDataHeader.languageShiftTable;
                    this.messageBody = paramPduParser.getUserDataGSM7Bit(m, n, i);
                    break;
                }
                this.messageBody = paramPduParser.getUserDataUCS2(m);
                break label172;
                this.messageBody = paramPduParser.getUserDataKSC5601(m);
                break label172;
                switch (0x3 & this.dataCodingScheme)
                {
                default:
                    break;
                case 0:
                    this.messageClass = SmsMessage.MessageClass.CLASS_0;
                    break;
                case 1:
                    this.messageClass = SmsMessage.MessageClass.CLASS_1;
                    break;
                case 2:
                    this.messageClass = SmsMessage.MessageClass.CLASS_2;
                    break;
                case 3:
                    this.messageClass = SmsMessage.MessageClass.CLASS_3;
                }
                break;
            }
        }
    }

    int getDataCodingScheme()
    {
        return this.dataCodingScheme;
    }

    public SmsMessage.MessageClass getMessageClass()
    {
        return this.messageClass;
    }

    public int getProtocolIdentifier()
    {
        return this.protocolIdentifier;
    }

    @MiuiHook(MiuiHook.MiuiHookType.NEW_METHOD)
    public String getRecipientAddress()
    {
        if (this.recipientAddress != null);
        for (String str = this.recipientAddress.getAddressString(); ; str = null)
        {
            return str;
            Log.v("GSM", "SMS recipient address is null");
        }
    }

    public int getStatus()
    {
        return this.status;
    }

    public boolean isCphsMwiMessage()
    {
        if ((((GsmSmsAddress)this.originatingAddress).isCphsVoiceMessageClear()) || (((GsmSmsAddress)this.originatingAddress).isCphsVoiceMessageSet()));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    public boolean isMWIClearMessage()
    {
        boolean bool1 = true;
        if ((this.isMwi) && (!this.mwiSense))
            return bool1;
        if ((this.originatingAddress != null) && (((GsmSmsAddress)this.originatingAddress).isCphsVoiceMessageClear()));
        for (boolean bool2 = bool1; ; bool2 = false)
        {
            bool1 = bool2;
            break;
        }
    }

    public boolean isMWISetMessage()
    {
        boolean bool1 = true;
        if ((this.isMwi) && (this.mwiSense))
            return bool1;
        if ((this.originatingAddress != null) && (((GsmSmsAddress)this.originatingAddress).isCphsVoiceMessageSet()));
        for (boolean bool2 = bool1; ; bool2 = false)
        {
            bool1 = bool2;
            break;
        }
    }

    public boolean isMwiDontStore()
    {
        boolean bool = true;
        if ((this.isMwi) && (this.mwiDontStore));
        while (true)
        {
            return bool;
            if (isCphsMwiMessage())
            {
                if (!" ".equals(getMessageBody()));
            }
            else
                bool = false;
        }
    }

    public boolean isReplace()
    {
        if (((0xC0 & this.protocolIdentifier) == 64) && ((0x3F & this.protocolIdentifier) > 0) && ((0x3F & this.protocolIdentifier) < 8));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    public boolean isReplyPathPresent()
    {
        return this.replyPathPresent;
    }

    public boolean isStatusReportMessage()
    {
        return this.isStatusReportMessage;
    }

    public boolean isTypeZero()
    {
        if (this.protocolIdentifier == 64);
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    boolean isUsimDataDownload()
    {
        if ((this.messageClass == SmsMessage.MessageClass.CLASS_2) && ((this.protocolIdentifier == 127) || (this.protocolIdentifier == 124)));
        for (boolean bool = true; ; bool = false)
            return bool;
    }

    private static class PduParser
    {
        int cur;
        int mUserDataSeptetPadding;
        int mUserDataSize;
        byte[] pdu;
        byte[] userData;
        SmsHeader userDataHeader;

        PduParser(byte[] paramArrayOfByte)
        {
            this.pdu = paramArrayOfByte;
            this.cur = 0;
            this.mUserDataSeptetPadding = 0;
        }

        int constructUserData(boolean paramBoolean1, boolean paramBoolean2)
        {
            int i = 0;
            int j = this.cur;
            byte[] arrayOfByte1 = this.pdu;
            int k = j + 1;
            int m = 0xFF & arrayOfByte1[j];
            int n = 0;
            int i1 = 0;
            int i2;
            int i9;
            if (paramBoolean1)
            {
                byte[] arrayOfByte2 = this.pdu;
                int i6 = k + 1;
                i1 = 0xFF & arrayOfByte2[k];
                byte[] arrayOfByte3 = new byte[i1];
                System.arraycopy(this.pdu, i6, arrayOfByte3, 0, i1);
                this.userDataHeader = SmsHeader.fromByteArray(arrayOfByte3);
                i2 = i6 + i1;
                int i7 = 8 * (i1 + 1);
                int i8 = i7 / 7;
                if (i7 % 7 > 0)
                {
                    i9 = 1;
                    n = i8 + i9;
                    this.mUserDataSeptetPadding = (n * 7 - i7);
                }
            }
            while (true)
            {
                int i4;
                int i5;
                if (paramBoolean2)
                {
                    i4 = this.pdu.length - i2;
                    this.userData = new byte[i4];
                    System.arraycopy(this.pdu, i2, this.userData, 0, this.userData.length);
                    this.cur = i2;
                    if (!paramBoolean2)
                        break label257;
                    i5 = m - n;
                    if (i5 >= 0)
                        break label251;
                }
                while (true)
                {
                    return i;
                    i9 = 0;
                    break;
                    if (paramBoolean1);
                    for (int i3 = i1 + 1; ; i3 = 0)
                    {
                        i4 = m - i3;
                        if (i4 >= 0)
                            break;
                        i4 = 0;
                        break;
                    }
                    label251: i = i5;
                    continue;
                    label257: i = this.userData.length;
                }
                i2 = k;
            }
        }

        GsmSmsAddress getAddress()
        {
            int i = 2 + (1 + (0xFF & this.pdu[this.cur])) / 2;
            GsmSmsAddress localGsmSmsAddress = new GsmSmsAddress(this.pdu, this.cur, i);
            this.cur = (i + this.cur);
            return localGsmSmsAddress;
        }

        int getByte()
        {
            byte[] arrayOfByte = this.pdu;
            int i = this.cur;
            this.cur = (i + 1);
            return 0xFF & arrayOfByte[i];
        }

        String getSCAddress()
        {
            int i = getByte();
            Object localObject;
            if (i == 0)
                localObject = null;
            while (true)
            {
                this.cur = (i + this.cur);
                return localObject;
                try
                {
                    String str = PhoneNumberUtils.calledPartyBCDToString(this.pdu, this.cur, i);
                    localObject = str;
                }
                catch (RuntimeException localRuntimeException)
                {
                    Log.d("GSM", "invalid SC address: ", localRuntimeException);
                    localObject = null;
                }
            }
        }

        long getSCTimestampMillis()
        {
            byte[] arrayOfByte1 = this.pdu;
            int i = this.cur;
            this.cur = (i + 1);
            int j = IccUtils.gsmBcdByteToInt(arrayOfByte1[i]);
            byte[] arrayOfByte2 = this.pdu;
            int k = this.cur;
            this.cur = (k + 1);
            int m = IccUtils.gsmBcdByteToInt(arrayOfByte2[k]);
            byte[] arrayOfByte3 = this.pdu;
            int n = this.cur;
            this.cur = (n + 1);
            int i1 = IccUtils.gsmBcdByteToInt(arrayOfByte3[n]);
            byte[] arrayOfByte4 = this.pdu;
            int i2 = this.cur;
            this.cur = (i2 + 1);
            int i3 = IccUtils.gsmBcdByteToInt(arrayOfByte4[i2]);
            byte[] arrayOfByte5 = this.pdu;
            int i4 = this.cur;
            this.cur = (i4 + 1);
            int i5 = IccUtils.gsmBcdByteToInt(arrayOfByte5[i4]);
            byte[] arrayOfByte6 = this.pdu;
            int i6 = this.cur;
            this.cur = (i6 + 1);
            int i7 = IccUtils.gsmBcdByteToInt(arrayOfByte6[i6]);
            byte[] arrayOfByte7 = this.pdu;
            int i8 = this.cur;
            this.cur = (i8 + 1);
            int i9 = arrayOfByte7[i8];
            int i10 = IccUtils.gsmBcdByteToInt((byte)(i9 & 0xFFFFFFF7));
            Time localTime;
            if ((i9 & 0x8) == 0)
            {
                localTime = new Time("UTC");
                if (j < 90)
                    break label317;
            }
            label317: for (int i11 = j + 1900; ; i11 = j + 2000)
            {
                localTime.year = i11;
                localTime.month = (m - 1);
                localTime.monthDay = i1;
                localTime.hour = i3;
                localTime.minute = i5;
                localTime.second = i7;
                return localTime.toMillis(true) - 1000 * (60 * (i10 * 15));
                i10 = -i10;
                break;
            }
        }

        byte[] getUserData()
        {
            return this.userData;
        }

        String getUserDataGSM7Bit(int paramInt1, int paramInt2, int paramInt3)
        {
            String str = GsmAlphabet.gsm7BitPackedToString(this.pdu, this.cur, paramInt1, this.mUserDataSeptetPadding, paramInt2, paramInt3);
            this.cur += paramInt1 * 7 / 8;
            return str;
        }

        SmsHeader getUserDataHeader()
        {
            return this.userDataHeader;
        }

        String getUserDataKSC5601(int paramInt)
        {
            try
            {
                str = new String(this.pdu, this.cur, paramInt, "KSC5601");
                this.cur = (paramInt + this.cur);
                return str;
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException)
            {
                while (true)
                {
                    String str = "";
                    Log.e("GSM", "implausible UnsupportedEncodingException", localUnsupportedEncodingException);
                }
            }
        }

        int getUserDataSeptetPadding()
        {
            return this.mUserDataSeptetPadding;
        }

        String getUserDataUCS2(int paramInt)
        {
            try
            {
                str = new String(this.pdu, this.cur, paramInt, "utf-16");
                this.cur = (paramInt + this.cur);
                return str;
            }
            catch (UnsupportedEncodingException localUnsupportedEncodingException)
            {
                while (true)
                {
                    String str = "";
                    Log.e("GSM", "implausible UnsupportedEncodingException", localUnsupportedEncodingException);
                }
            }
        }

        boolean moreDataPresent()
        {
            if (this.pdu.length > this.cur);
            for (boolean bool = true; ; bool = false)
                return bool;
        }
    }

    public static class SubmitPdu extends SmsMessageBase.SubmitPduBase
    {
    }
}

/* Location:                     /home/lithium/miui/chameleon/2.11.16/framework_dex2jar.jar
 * Qualified Name:         com.android.internal.telephony.gsm.SmsMessage
 * JD-Core Version:        0.6.2
 */
