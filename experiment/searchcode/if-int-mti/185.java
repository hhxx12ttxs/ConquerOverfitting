/*
 * org.tizen.telephony.sms
 * 
 * Copyright (C) 2000 - 2011 Samsung Electronics Co., Ltd. All rights reserved.
 *
 * Contact: 
 * Sungmin Ha <sungmin82.ha@samsung.com>
 * YeongKyoon Lee <yeongkyoon.lee@samsung.com>
 * DongKyun Yun <dk77.yun@samsung.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 * - S-Core Co., Ltd
 * 
 */

package org.tizen.telephony.sms.encription;

import java.io.UnsupportedEncodingException;

public class DecodeMsg {
	int ton, npi;
	public static boolean deliveryReport = false;
	public static char[] smsbuffer = new char[4096];
	public static int smsbuffer_index = 0;
	public static boolean relayedsms = false;
	public static int relay_cnt = 0;
	public static int udhl = 0;
	
	public void DecodeSmsSubmitTpdu(TPDU_SMS_SUBMIT tpdu_submit, int pdu_len, char[] pPDU) {
		int scaAddr_len = 0;
		int destAddr_len = 0;
		char[] diallingNum = new char[EnumConstants.TAPI_NETTEXT_ADDRESS_LEN_MAX+1];
		char[] scaAddr = new char[EnumConstants.TAPI_NETTEXT_SCADDRESS_LEN_MAX+1];
		char[] destAddr = new char[EnumConstants.TAPI_NETTEXT_ADDRESS_LEN_MAX+1];
//		char[] inData = new char[EnumConstants.TAPI_NETTEXT_SMDATA_SIZE_MAX+1];

		int position = 0;

		SmsUtilDecodeAddrField_sca(diallingNum, pPDU);

		position += 2;

		scaAddr_len = getSize(diallingNum);

		if ((scaAddr_len % 2) != 0)
			position += scaAddr_len / 2 + 1;
		else
			position += scaAddr_len / 2;

		if (ton == EnumConstants.SIM_TON_INTERNATIONAL) {
			scaAddr[0] = '+';
			for(int i = 0; i < scaAddr_len; i++) {
				scaAddr[i+1] = diallingNum[i];
				scaAddr[scaAddr_len+1] = '\0';
			}
		} else {
			for(int i = 0; i < scaAddr_len; i++) {
				scaAddr[i] = diallingNum[i];
				scaAddr[scaAddr_len] = '\0';
			}
		}
		
		/* TP-MTI, TP-RD, TP-VPF, TP-RP, TP_UDHI, TP-SRR */
		tpdu_submit.msgType = EnumConstants.SMS_TPDU_SUBMIT;
		tpdu_submit.rd = ((pPDU[position] & 0x04) != 0) ? true : false;
		tpdu_submit.vpf = (pPDU[position] & 0x18) >> 3;
		tpdu_submit.srr = ((pPDU[position] & 0x20) != 0) ? true : false;
		tpdu_submit.udhi = ((pPDU[position] & 0x40) != 0) ? true : false;
		tpdu_submit.rp = ((pPDU[position] & 0x80) != 0) ? true : false;
		
		position++;
		
		/* TP-MR */
		tpdu_submit.mr = pPDU[position];
		
		position++;
		
		/* TP-DA */
		for(int i = 0; i < diallingNum.length; i++)
			diallingNum[i] = 0x00;
		
		SmsUtilDecodeAddrField_dst(diallingNum, getCharArray(pPDU, position));
		
		position+=2;
		
		destAddr_len = getSize(diallingNum);
		
		if((destAddr_len %2) != 0)
			position += destAddr_len / 2 + 1;
		else
			position += destAddr_len / 2;
		
		if (ton == EnumConstants.SIM_TON_INTERNATIONAL) {
			destAddr[0] = '+';
			for(int i = 0; i < destAddr_len; i++) {
				destAddr[i+1] = diallingNum[i];
				destAddr[destAddr_len+1] = '\0';
			}
			tpdu_submit.destAddr.dialnumlen = destAddr_len+1;
			for(int i = 0; i < destAddr_len+1; i++)
				tpdu_submit.destAddr.diallingNum[i] = destAddr[i];
		} else {
			for(int i = 0; i < destAddr_len; i++) {
				destAddr[i] = diallingNum[i];
				destAddr[destAddr_len] = '\0';
			}
			tpdu_submit.destAddr.dialnumlen = destAddr_len;
			for(int i = 0; i < destAddr_len; i++)
				tpdu_submit.destAddr.diallingNum[i] = destAddr[i];
		}
		
		tpdu_submit.destAddr.npi = npi;
		tpdu_submit.destAddr.ton = ton;
		
		/* TP-PID */
		tpdu_submit.pId = pPDU[position];
		position++;
		
		/* TP DCS */
		SmsUtilDecodeDCS(tpdu_submit.dcs, pPDU[position]);
		position++;
		
		/* TP VP */
//		tpdu_submit.vp.vpType = EnumConstants.TAPI_NETTEXT_VP_RELATIVE;
		
		switch(tpdu_submit.vp.vpType) {
			case EnumConstants.TAPI_NETTEXT_VP_RELATIVE:
				tpdu_submit.vp.vpValue = pPDU[position];
				tpdu_submit.vp.vpRelativeType = EnumConstants.TAPI_NETTEXT_VP_REL_1D;
				position++;
				break;
			case EnumConstants.TAPI_NETTEXT_VP_ABSOLUTE:
			case EnumConstants.TAPI_NETTEXT_VP_ENHANCED:
				position += 7;
				break;
			default:
				break;
		}
		
		/* TP UDL */
		tpdu_submit.udl = pPDU[position] & 0x00FF;
		
		if(tpdu_submit.udl > EnumConstants.TAPI_NETTEXT_SMDATA_SIZE_MAX)
			tpdu_submit.udl = EnumConstants.TAPI_NETTEXT_SMDATA_SIZE_MAX;
		
		position++;
		
		/* TP UD */
		if(tpdu_submit.udhi)
		{
			udhl = pPDU[position];
			int fillbits = 7 - ((pPDU[position] * 8) % 7);
			position += udhl;
			udhl += fillbits;
			
			if(pPDU[position] == 1)
			{
				relay_cnt = pPDU[position - 1];
				for(int i = 0; i < smsbuffer.length; i++)
					smsbuffer[i] = '\0';
			}
			position++;
			
			relayedsms = true;
		}
		else
		{
			if(relayedsms)
			{
				for(int i = 0; i < smsbuffer.length; i++)
					smsbuffer[i] = '\0';
				
				smsbuffer_index = 0;
				relayedsms = false;
			}
		}
		
		int i = 0;
		tpdu_submit.userData = new char[EnumConstants.TAPI_NETTEXT_SMDATA_SIZE_MAX+1];
		//System.out.println("Position : " + position);
		if(tpdu_submit.dcs.alphabetType == EnumConstants.TAPI_NETTEXT_ALPHABET_DEFAULT) {
			char[] inData = new char[EnumConstants.TAPI_NETTEXT_SMDATA_SIZE_MAX+1];
			for(i = 0; i < tpdu_submit.udl; i++) {
				inData[i] = (char)(pPDU[position + i] & 0xff);
				if(relay_cnt == 1)	// last or single sms
				{
					if(i == tpdu_submit.udl - udhl)
						break;
				}
				else
				{
					if(tpdu_submit.udhi == true)
					{
						if(i == tpdu_submit.udl - udhl)
							break;
					}
					else
					{
						if(i == tpdu_submit.udl)
							break;
					}
				}
			}
			
			inData[i] = '\0';
			
			for(int j = 0; j < i; j++) {
				tpdu_submit.userData[j] = inData[j];	
			}
			
			if(relayedsms)
			{
				String tmp1 = new String(smsbuffer);
				String tmp2 = new String(inData);
				String tmp3 = tmp1.trim() + tmp2.trim();
				smsbuffer = tmp3.toCharArray();
			}
			// SmsUtilUnpackGSMCode(tpdu_submit.userData, inData, tpdu_submit.udl);
		}
		else if(tpdu_submit.dcs.alphabetType == EnumConstants.TAPI_NETTEXT_ALPHABET_UCS2)
		{
			byte[] inData = new byte[EnumConstants.TAPI_NETTEXT_SMDATA_SIZE_MAX+1];
			for(i = 0; i < tpdu_submit.udl; i++) {
				inData[i] = (byte)(pPDU[position + i] & 0xff);
				if(relay_cnt == 1)	// last or single sms
				{
					if(i == tpdu_submit.udl - udhl + 1)
						break;
				}
				else
				{
					if(tpdu_submit.udhi == true)
					{
						if(i == tpdu_submit.udl - udhl)
							break;
					}
					else
					{
						if(i == tpdu_submit.udl)
							break;
					}
				}
			}
			
			try {
				inData[i] = '\0';
				tpdu_submit.userData = ucs2ToUTF16(inData, i/2);	
			} catch (UnsupportedEncodingException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	
		if(tpdu_submit.srr)
		{
		//	System.out.println("SRR: A status report is requested");
			deliveryReport = true;
		}
		else
		{
		//	System.out.println("SRR: A status report is not requested");
			deliveryReport = false;
		}
	}

	private char[] getCharArray(char[] org, int start) {
		int length = org.length - start;
		char[] result = new char[length];
		
		for(int i = 0; i < length; i++) {
			result[i] = org[start + i];
		}
		return result;
	}
	
	private void SmsUtilDecodeDCS(TapiNetTextCodingScheme pCodingScheme, char dcs) {
		if ( dcs < 0x40 ) { // bits 7..4 = 00xx : general data coding indication
			pCodingScheme.codingGroupType = EnumConstants.TAPI_NETTEXT_CODGRP_SM_GENERAL_DCS;

			if ((dcs & 0x20) != 0) // bit 5 = 1 : indicates the text is compressed
				pCodingScheme.bCompressed = true;

			if ((dcs & 0x10) != 0) // bit 4 = 1 : indicates that bits  1 to 0 have a message class meaning
			{
				pCodingScheme.bMsgClassSet = true;

				switch ( dcs & 0x03 ) // bits 1 to 0 : message class
				{
					case 0x00:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_0;
						break;
					case 0x01:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_1;
						break;
					case 0x02:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_2;
						break;
					case 0x03:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_3;
						break;
				}
			}
			else // bit 4 = 0 : indicates that bits 1 to 0 are reserved and have no message class meaning
				pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_NONE;

			switch ( dcs & 0x0C ) // bits 4 to 3 : character set
			{
				case 0x00:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_DEFAULT;
					break;
				case 0x04:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_8BIT;
					break;
				case 0x08:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_UCS2;
					break;
				case 0x0C:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_MAX;
					break;
			}
		}
		else if ( dcs >= 0x40 && dcs < 0x80 ) // bits 7..4 = 01xx : message marked for automatic deletion group. bits 5..0 are coded exactly the same as group 00xx
		{
			pCodingScheme.codingGroupType = EnumConstants.TAPI_NETTEXT_CODGRP_SM_AUTO_DELETION;

			if ((dcs & 0x20) != 0) // bit 5 = 1 : indicates the text is compressed
				pCodingScheme.bCompressed = true;

			if ((dcs & 0x10) != 0) // bit 4 = 1 : indicates that bits  1 to 0 have a message class meaning
			{
				pCodingScheme.bMsgClassSet = true;

				switch ( dcs & 0x03 ) // bits 1 to 0 : message class
				{
					case 0x00:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_0;
						break;
					case 0x01:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_1;
						break;
					case 0x02:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_2;
						break;
					case 0x03:
						pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_3;
						break;
				}
			}
			else // bit 4 = 0 : indicates that bits 1 to 0 are reserved and have no message class meaning
				pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_NONE;

			switch ( dcs & 0x0C ) // bits 4 to 3 : character set
			{
				case 0x00:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_DEFAULT;
					break;
				case 0x04:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_8BIT;
					break;
				case 0x08:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_UCS2;
					break;
				case 0x0C:
					pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_MAX;
					break;
			}
		}
		// bits 7..4 = 1000 ~ 1011 : reserved
		else if (dcs == 0xC0) // bits 7..4 = 1100 : message waiting indication group, discard message
		{
			pCodingScheme.codingGroupType = EnumConstants.TAPI_NETTEXT_CODGRP_SM_WAITING_DISCARD;
		}
		else if (dcs < 0xE0)
		{
			pCodingScheme.codingGroupType = EnumConstants.TAPI_NETTEXT_CODGRP_SM_WAITING_STORE;

			if ((dcs & 0x08) != 0)
				pCodingScheme.bMsgIndActive = true;

			switch (dcs & 0x03)
			{
				case 0x00:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_VOICE_MSG;
					break;
				case 0x01:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_FAX_MSG;
					break;
				case 0x02:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_EMAIL_MSG;
					break;
				case 0x03:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_OTHER_MSG;
					break;
			}
		}
		else if (dcs < 0xF0)
		{
			pCodingScheme.codingGroupType = EnumConstants.TAPI_NETTEXT_CODGRP_SM_WAITING_STORE_UCS2;

			if ((dcs & 0x08) != 0)
				pCodingScheme.bMsgIndActive = true;

			switch (dcs & 0x03)
			{
				case 0x00:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_VOICE_MSG;
					break;
				case 0x01:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_FAX_MSG;
					break;
				case 0x02:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_EMAIL_MSG;
					break;
				case 0x03:
					pCodingScheme.waitingType = EnumConstants.TAPI_NETTEXT_WAITING_OTHER_MSG;
					break;
			}
		}
		else
		{
			pCodingScheme.codingGroupType = EnumConstants.TAPI_NETTEXT_CODGRP_SM_CLASS_CODING;

			if ((dcs & 0x04) != 0)
				pCodingScheme.alphabetType = EnumConstants.TAPI_NETTEXT_ALPHABET_8BIT;

			switch (dcs & 0x03)
			{
				case 0x00:
					pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_0;
					break;
				case 0x01:
					pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_1;
					break;
				case 0x02:
					pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_2;
					break;
				case 0x03:
					pCodingScheme.classType = EnumConstants.TAPI_NETTEXT_CLASS_3;
					break;
			}
		}
	}

	private void SmsUtilDecodeAddrField_sca(char[] diallingNum, char[] pAddrField) {
//		int ton, npi;
		int index = 0;
		int dialnumLen = 0;
		int length = 0;
		
		ton = (pAddrField[index+1] & 0x70) >> 4;
		npi = pAddrField[index+1] & 0x0F;
		
		if(ton != EnumConstants.SIM_TON_ALPHA_NUMERIC) {
			length = pAddrField[index];
			if(length > 1)
				dialnumLen = (pAddrField[index++] - 1) * 2;
		} else
			dialnumLen = (((pAddrField[index++] + 1) / 2) * 8) / 7;

		if(dialnumLen > EnumConstants.SIM_SMSP_ADDRESS_LEN)
			dialnumLen = EnumConstants.SIM_SMSP_ADDRESS_LEN;
		
		// Ignore type of address field
		index++;
		
		if(ton != EnumConstants.SIM_TON_ALPHA_NUMERIC)
			SmsUtilConvertBCD2Digit(diallingNum, getCharArray(pAddrField, index), dialnumLen);
		else
			SmsUtilUnpackGSMCode(diallingNum, getCharArray(pAddrField, index), dialnumLen);
	}
	
	private void SmsUtilDecodeAddrField_dst(char[] diallingNum, char[] pAddrField) {
//		int ton, npi;
		int index = 0;
		int dialnumLen = 0;
		
		ton = (pAddrField[index+1] & 0x70) >> 4;
		npi = pAddrField[index+1] & 0x0F;
		
		if(ton != EnumConstants.SIM_TON_ALPHA_NUMERIC)
			dialnumLen = pAddrField[index++];
		else
			dialnumLen = (((pAddrField[index++] + 1) / 2) * 8) / 7;
		
		if(dialnumLen > EnumConstants.SIM_SMSP_ADDRESS_LEN)
			dialnumLen = EnumConstants.SIM_SMSP_ADDRESS_LEN;
		
		// Ignore type of address field
		index++;
		
		if(ton != EnumConstants.SIM_TON_ALPHA_NUMERIC)
			SmsUtilConvertBCD2Digit(diallingNum, getCharArray(pAddrField, index), dialnumLen);
		else
			SmsUtilUnpackGSMCode(diallingNum, getCharArray(pAddrField, index), dialnumLen);
	}
	
	private void SmsUtilConvertBCD2Digit(char[] pDigits, char[] pBCD, int digitLen) {
		int i, bcdLen;
		char[] c = {0x00, 0x00};
		char higher, lower;
		
		if(pBCD == null || pDigits == null)
			throw new NullPointerException("SmsUtilConvertBCD2Digit"); //$NON-NLS-1$
		
		if(digitLen == 0) {
			pDigits[0] = 0x00;
			return;
		}
		
		if((digitLen % 2) != 0)
			bcdLen = digitLen / 2 + 1;
		else
			bcdLen = digitLen / 2;
		
		for(i = 0; i < bcdLen; i++) {
			lower = (char)(pBCD[i] & 0x0F);
			
			if(lower == 0x0A)
				lower = '*';
			else if(lower == 0x0B)
				lower = '#';
			else if(lower == 0x0C)
				lower = 'p';
			else if(lower == 0x0F)
				lower = 0;
			else {
				AcItoa(lower, c, 16);
				lower = (char)AcToupper(c[0]);
			}
			
			higher = (char)((pBCD[i] >> 4) & 0x0F);
			
			if(higher == 0x0A)
				higher = '*';
			else if(higher == 0x0B)
				higher = '#';
			else if(higher == 0x0C)
				higher = 'p';
			else if(higher == 0x0F) {
				pDigits[getSize(pDigits)] = lower;
				pDigits[bcdLen*2-1] = '\0';
				return;
			}
			else {
				AcItoa(higher, c, 16);
				higher = (char)AcToupper(c[0]);
			}
			pDigits[getSize(pDigits)] = lower;
			pDigits[getSize(pDigits)] = higher;
		}
		pDigits[digitLen] = '\0';
	}
	
	private int getSize(char[] array) {
		int ret = 0;
		String str = new String(array);
		ret = str.indexOf('\0');
		return ret;
	}
	
	private int AcToupper(int ch) {
		return (('a' <= (ch) && (ch) <= 'z') ? ((ch) - ('a' - 'A')) : (ch));
	}
	
	private void AcItoa(int n, char[] str, int b) {
		int i = 0;
		String arr = new String("0123456789ABCDEF"); //$NON-NLS-1$
		do
			str[i++] = arr.charAt(n%b);
		while((n /= b) > 0);
		reverse(str, i);
		str[i] = '\0';
	}
	
	private void reverse(char[] x, int len) {
		int i, j = len-1;
		for(i = 0; i < j; i++) {
			char t = x[i];
			x[i] = x[j];
			x[j--] = t;
		}
	}
	
	private void SmsUtilUnpackGSMCode(char[] szData, char[] pIn, int in_len) {
		int shift = 0;
		int pos = 0;
		
		for(int i = 0; i < in_len; i++, pos++) {
			szData[i] = (char)((pIn[pos] << shift) & 0x7F);
			if(pos != 0) {
				/* except the first byte, a character contains some bits
				 * from the previous byte
				 */
				szData[i] |= pIn[pos-1] >> (8-shift);
			}
			shift++;
			
			if(shift == 7) {
				shift = 0;
				/* a possible extra complete character is available */
				i++;
				szData[i] = (char)(pIn[pos] >> 1);
				/* This is the end of the input, quit */
				if(szData[i] == 0)
					break;
			}
		}
	}
	
	public char[] ucs2ToUTF16(byte[] ucs2Bytes, int length) throws UnsupportedEncodingException{    
		String unicode = new String(ucs2Bytes, "UTF-16");
		char[] str = unicode.toCharArray();
		
		String tmp1 = new String(smsbuffer);
		String tmp2 = unicode.substring(0, length);
		String tmp3 = tmp1.trim() + tmp2.trim();
		smsbuffer = tmp3.toCharArray();

		return str;
	}
}

