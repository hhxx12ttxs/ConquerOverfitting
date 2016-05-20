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

public class EncodeMsg {
	int index;

	public int EncodeSmsDeliverTpdu(SmsAddressInfo SCA,
			TPDU_SMS_DELIVER tpdu_deliver, char[] packet, int pid, int dcs) {
		// char tp_dcs; // BYTE
		char[] timeStamp = new char[21];
		char[] scaNumber = new char[21];
		char[] orgNumber = new char[21];
		int scaLength;
		int orgLength;
		int i;

		index = 0;

		if (SCA.diallingNum[0] == '+') {
			SCA.ton = 1;
			SCA.npi = 1;
			for (i = 0; i < SCA.dialnumlen-1; i++) {
				scaNumber[i] = SCA.diallingNum[i + 1];
			}
			scaLength = SCA.dialnumlen - 1;
			scaNumber[scaLength] = '\0';
		} else {
			SCA.ton = 0;
			SCA.npi = 0;
			for (i = 0; i < SCA.dialnumlen; i++) {
				scaNumber[i] = SCA.diallingNum[i];
			}
			scaLength = SCA.dialnumlen;
			scaNumber[scaLength] = '\0';
		}

		SmsUtilEncodeAddrField_sca(packet, scaNumber, scaLength, SCA.ton, SCA.npi);

		tpdu_deliver.msgType = EnumConstants.SMS_TPDU_DELIVER;

		packet[index] = (char) tpdu_deliver.msgType;
		packet[index] |= tpdu_deliver.mms ? 0x04 : 0;
		packet[index] |= tpdu_deliver.sri ? 0x20 : 0;
		packet[index] |= tpdu_deliver.udhi ? 0x40 : 0;
		packet[index] |= tpdu_deliver.rp ? 0x80 : 0;

		index++;
		
		if (tpdu_deliver.orgAddr.diallingNum[0] == '+') {
			tpdu_deliver.orgAddr.ton = 1;
			tpdu_deliver.orgAddr.npi = 1;
			for (i = 0; i < tpdu_deliver.orgAddr.dialnumlen-1; i++) {
				orgNumber[i] = tpdu_deliver.orgAddr.diallingNum[i + 1];
			}
			orgLength = tpdu_deliver.orgAddr.dialnumlen - 1;
			orgNumber[orgLength] = '\0';
		} else {
			tpdu_deliver.orgAddr.ton = 0;
			tpdu_deliver.orgAddr.npi = 0;
			for (i = 0; i < tpdu_deliver.orgAddr.dialnumlen; i++) {
				orgNumber[i] = tpdu_deliver.orgAddr.diallingNum[i];
			}
			orgLength = tpdu_deliver.orgAddr.dialnumlen;
			orgNumber[orgLength] = '\0';
		}

		/* TP-OA */
		SmsUtilEncodeAddrField_dst(packet, orgNumber, orgLength, tpdu_deliver.orgAddr.ton, tpdu_deliver.orgAddr.npi);

		/* TP-PID */
		packet[index++] = (char) pid;

		int tp_dcs = SmsUtilEncodeDCS(tpdu_deliver.dcs);

		packet[index++] = (char) tp_dcs;

		SmsUtilEncodeTimeStamp(tpdu_deliver.scts, timeStamp);

		for (i = 0; i < 7; i++) {
			packet[index++] = timeStamp[i];
		}

		/* TP-UD */
		switch(tpdu_deliver.dcs.alphabetType) {
			case EnumConstants.TAPI_NETTEXT_ALPHABET_DEFAULT:
				/* UDL */
				packet[index++] = (char) tpdu_deliver.userData.data.length;
				SmsUtilPackGSMCode(packet, new String(tpdu_deliver.userData.data).toCharArray(),
						tpdu_deliver.userData.data.length);
				break;
			case EnumConstants.TAPI_NETTEXT_ALPHABET_8BIT:
				packet[index++] = (char) tpdu_deliver.userData.data.length;
				SmsUtilPackGSM8Code(packet, new String(tpdu_deliver.userData.data).toCharArray(),
						tpdu_deliver.userData.data.length);
				break;
			case EnumConstants.TAPI_NETTEXT_ALPHABET_UCS2:
				int udhl 	  	= 0;
				int fillBits 	= 0;
				int tempIndex   = index;

				if (tpdu_deliver.userData.headerCnt > 0)
					index = index + 2;
				else
					index = index + 1;

				// setting HEADER
				int headerLen 	= 6;
				for (i = 0; i < tpdu_deliver.userData.headerCnt; i++)
				{
					packet[index++] = 0x08; // SMS_UDH_CONCAT_16BIT;
					packet[index++] = 0x04; // offset
					packet[index++] = (char)(tpdu_deliver.userData.smsUDH.msgRef >> 8);
					packet[index++] = (char)(tpdu_deliver.userData.smsUDH.msgRef & 0x00FF);
					packet[index++] = tpdu_deliver.userData.smsUDH.totalSeg;
					packet[index++] = tpdu_deliver.userData.smsUDH.seqNum;
					udhl += headerLen;
				}

				if (udhl > 0) {
					packet[tempIndex] 	 = (char)((udhl+1) + fillBits + tpdu_deliver.userData.length);
					packet[tempIndex+ 1] = (char)udhl;
				} else {
					packet[tempIndex] 	= (char)(tpdu_deliver.userData.length);
				}

				for(int ind = 0; ind < tpdu_deliver.userData.length; ind++) {
					packet[index++] = (char)tpdu_deliver.userData.data[ind];
				}
				break;
			//	throw new NullPointerException(Messages.EncodeMsg_0);
			default:
				throw new NullPointerException(Messages.EncodeMsg_1);
		}

		return index;
	}

	private void SmsUtilPackGSMCode(char[] pOut, char[] szData, int in_len) {
		int shift = 0;

		for (int i = 0; i < in_len; index++, i++) {
			/* pack the high bits using the low bits of the next character */
			pOut[index] = (char) (szData[i] >> shift);
			if (i + 1 < in_len) {
				/* pack the high bits using the low bits of the next character */
				pOut[index] |= szData[i + 1] << (7 - shift);
				pOut[index] &= 0xFF;
				shift++;

				if (shift == 7) {
					shift = 0;
					i++;
				}
			}
		}
	}

	private void SmsUtilPackGSM8Code(char[] pOut, char[] szData, int in_len) {
		for(int i = 0; i < in_len; i++) {
			pOut[index++] = szData[i];
		}
	}
	
	private void SmsUtilEncodeTimeStamp(TmDateTime tmDateTime, char[] timeStamp) {
		char[] szBuf = new char[3];
		char[] tmpBuf = new char[20];
		int year;

		for (int i = 0; i < 7; i++) {
			timeStamp[i] = 0x00;
		}

		year = tmDateTime.year - 2000;
		if (year < 0)
			year += 100;

		szBuf = String.format("%02d", year).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[0] = tmpBuf[0];
		szBuf = String.format("%02d", tmDateTime.month).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[1] = tmpBuf[0];
		szBuf = String.format("%02d", tmDateTime.day).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[2] = tmpBuf[0];
		szBuf = String.format("%02d", tmDateTime.hour).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[3] = tmpBuf[0];
		szBuf = String.format("%02d", tmDateTime.minute).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[4] = tmpBuf[0];
		szBuf = String.format("%02d", tmDateTime.second).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[5] = tmpBuf[0];
		szBuf = String.format("%02d", 0x00).toCharArray(); //$NON-NLS-1$
		SmsUtilConvertDigit2BCD(tmpBuf, szBuf, 2);
		timeStamp[6] = tmpBuf[0];
	}

	private char SmsUtilEncodeDCS(TapiNetTextCodingScheme codingScheme) {
		char dcs = 0x00;

		switch (codingScheme.codingGroupType) {
		case EnumConstants.TAPI_NETTEXT_CODGRP_SM_GENERAL_DCS: // bit 7..4 is
																// 00xx
		{
			if (codingScheme.bCompressed)
				dcs |= 0x20; // bit 5 is 1

			if (codingScheme.bMsgClassSet) {
				dcs |= 0x10; // bit 4 is 1

				switch (codingScheme.classType) {
				case EnumConstants.TAPI_NETTEXT_CLASS_0: // bit 1..0 is 00
				{
					dcs |= 0x00;
					break;
				}
				case EnumConstants.TAPI_NETTEXT_CLASS_1: // bit 1..0 is 01
				{
					dcs |= 0x01;
					break;
				}
				case EnumConstants.TAPI_NETTEXT_CLASS_2: // bit 1..0 is 10
				{
					dcs |= 0x02;
					break;
				}
				case EnumConstants.TAPI_NETTEXT_CLASS_3: // bit 1..0 is 11
				{
					dcs |= 0x03;
					break;
				}
				default:
					break;
				}
			}

			switch (codingScheme.alphabetType) {
			case EnumConstants.TAPI_NETTEXT_ALPHABET_DEFAULT: // bit 3..2 is 00
			{
				dcs |= 0x00;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_ALPHABET_8BIT: // bit 3..2 is 01
			{
				dcs |= 0x04;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_ALPHABET_UCS2: // bit 3..2 is 10
			{
				dcs |= 0x08;
				break;
			}
			default: // bit 3..2 is 11
			{
				dcs |= 0x0C;
				break;
			}
			}

			break;
		}
		case EnumConstants.TAPI_NETTEXT_CODGRP_SM_WAITING_DISCARD: // bit 7..4
																	// is 1100
		{
			dcs |= 0xC0;

			break;
		}
		case EnumConstants.TAPI_NETTEXT_CODGRP_SM_WAITING_STORE: // bit 7..4 is
																	// 1101
		{
			dcs |= 0xD0;

			if (codingScheme.bMsgIndActive) // bit 3..2 is 10
				dcs |= 0x08;

			switch (codingScheme.waitingType) {
			case EnumConstants.TAPI_NETTEXT_WAITING_VOICE_MSG: // bit 1..0 is 00
			{
				dcs |= 0x00;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_WAITING_FAX_MSG: // bit 1..0 is 01
			{
				dcs |= 0x01;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_WAITING_EMAIL_MSG: // bit 1..0 is 10
			{
				dcs |= 0x02;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_WAITING_OTHER_MSG: // bit 1..0 is 11
			{
				dcs |= 0x03;
				break;
			}
			default:
				break;
			}

			break;
		}
		case EnumConstants.TAPI_NETTEXT_CODGRP_SM_WAITING_STORE_UCS2: // bit
																		// 7..4
																		// is
																		// 1110
		{
			dcs |= 0xE0;

			if (codingScheme.bMsgIndActive) // bit 3..2 is 10
				dcs |= 0x08;

			switch (codingScheme.waitingType) {
			case EnumConstants.TAPI_NETTEXT_WAITING_VOICE_MSG: // bit 1..0 is 00
			{
				dcs |= 0x00;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_WAITING_FAX_MSG: // bit 1..0 is 01
			{
				dcs |= 0x01;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_WAITING_EMAIL_MSG: // bit 1..0 is 10
			{
				dcs |= 0x02;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_WAITING_OTHER_MSG: // bit 1..0 is 11
			{
				dcs |= 0x03;
				break;
			}
			default:
				break;
			}

			break;
		}
		case EnumConstants.TAPI_NETTEXT_CODGRP_SM_CLASS_CODING: // bit 7..4 is
																// 1111
		{
			dcs |= 0xF0;

			switch (codingScheme.alphabetType) {
			case EnumConstants.TAPI_NETTEXT_ALPHABET_DEFAULT: // bit 2 is 0
			{
				dcs |= 0x00;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_ALPHABET_8BIT: // bit 2 is 1
			{
				dcs |= 0x04;
				break;
			}
			default:
				break;
			}

			switch (codingScheme.classType) {
			case EnumConstants.TAPI_NETTEXT_CLASS_0: // bit 1..0 is 00
			{
				break;
			}
			case EnumConstants.TAPI_NETTEXT_CLASS_1: // bit 1..0 is 01
			{
				dcs |= 0x01;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_CLASS_2: // bit 1..0 is 10
			{
				dcs |= 0x02;
				break;
			}
			case EnumConstants.TAPI_NETTEXT_CLASS_3: // bit 1..0 is 11
			{
				dcs |= 0x03;
				break;
			}
			default:
				break;
			}

			break;
		}
		case EnumConstants.TAPI_NETTEXT_CODGRP_SM_RESERVED: // bit 7..4 is 1111
		{
			dcs = (char) ((codingScheme.codingGroup << 4) & 0xF0);
			dcs |= (codingScheme.code & 0x0F);
			break;
		}
		default:
			break;
		}
		return dcs;
	}
	
	private void SmsUtilEncodeAddrField_sca(char[] pAddrField, char[] diallingNum,
			int dialnumLen, int ton, int npi) {
//		int index = 0;
		
		if (pAddrField == null || diallingNum == null)
			throw new NullPointerException("SmsUtilEncodeAddrField"); //$NON-NLS-1$

		if (ton != EnumConstants.SIM_TON_ALPHA_NUMERIC) {
			if((dialnumLen % 2) != 0) {
				pAddrField[index++] = (char)(dialnumLen / 2 + 1 + 1);
			} else {
				pAddrField[index++] = (char)(dialnumLen / 2 + 1);
			}
		} else {
			pAddrField[index] = (char) (((dialnumLen * 7 + 7) / 8) * 2);
			if (((dialnumLen * 7) % 8) <= 4) {
				pAddrField[index]--;
			}
			index++;
		}

		// SET_TON_NPI
		pAddrField[index] = 0x80;
		pAddrField[index] |= (ton & 0x07) << 4;
		pAddrField[index] |= npi & 0x0F;

		index++;

		if (ton != EnumConstants.SIM_TON_ALPHA_NUMERIC) {
			int i, j, k;
			char[] tmpBuf = new char[dialnumLen];
			SmsUtilConvertDigit2BCD(tmpBuf, diallingNum, dialnumLen);
			j = index;
			
			if((dialnumLen % 2) != 0) {
				index += dialnumLen / 2 + 1;
			}
			else {
				index += dialnumLen / 2;
			}

			for(i = j, k = 0; i < index; i++, k++)
				pAddrField[i] = tmpBuf[k];
		} else {
			SmsUtilPackGSMCode(pAddrField, diallingNum, (int) dialnumLen);
		}
	}
	
	private void SmsUtilEncodeAddrField_dst(char[] packet, char[] diallingNum,
			int dialnumLen, int ton, int npi) {
//		int index = 0;
		
		if (packet == null || diallingNum == null)
			throw new NullPointerException("SmsUtilEncodeAddrField"); //$NON-NLS-1$

		if (ton != EnumConstants.SIM_TON_ALPHA_NUMERIC) {
			packet[index++] = (char) dialnumLen;
		} else {
			packet[index] = (char) (((dialnumLen * 7 + 7) / 8) * 2);
			if (((dialnumLen * 7) % 8) <= 4) {
				packet[index]--;
			}
			index++;
		}

		// SET_TON_NPI
		packet[index] = 0x80;
		packet[index] |= (ton & 0x07) << 4;
		packet[index] |= npi & 0x0F;

		index++;

		if (ton != EnumConstants.SIM_TON_ALPHA_NUMERIC) {
			int i, j, k;
			char[] tmpBuf = new char[dialnumLen];
			for (i = 0; i < dialnumLen; i++)
				tmpBuf[i] = 0x00;

			SmsUtilConvertDigit2BCD(tmpBuf, diallingNum, dialnumLen);
			j = index;

			if ((dialnumLen % 2) != 0)
				index += dialnumLen / 2 + 1;
			else
				index += dialnumLen / 2;

			for (i = j, k = 0; i < index; i++, k++) {
				packet[i] = tmpBuf[k];
			}
		} else {
			SmsUtilPackGSMCode(packet, diallingNum, (int) dialnumLen);
		}
	}

	private void SmsUtilConvertDigit2BCD(char[] pBCD, char[] pDigits,
			long digitLen) {
		int i, j, digit;
		char higher, lower;

		if (pBCD == null || pDigits == null)
			throw new NullPointerException("SmsUtilConvertDigit2BCD"); //$NON-NLS-1$

		for (i = 0, j = 0; i < digitLen; i = i + 2, j++) {
			if (pDigits[i] == '*')
				digit = 0x0A;
			else if (pDigits[i] == '#')
				digit = 0x0B;
			else if (AcToupper(pDigits[i]) == 'P')
				digit = 0x0C;
			else
				digit = (int) (pDigits[i] - '0');

			lower = (char) (digit & 0x0F);

			if (digitLen != i + 1) {
				if (pDigits[i + 1] == '*')
					digit = 0x0A;
				else if (pDigits[i + 1] == '#')
					digit = 0x0B;
				else if (AcToupper(pDigits[i + 1]) == 'P')
					digit = 0x0C;
				else
					digit = (int) (pDigits[i + 1] - '0');

				higher = (char) (digit & 0x0F);
			} else {
				higher = 0xFF;
			}
			pBCD[j] = (char) ((higher << 4) | lower);
			pBCD[j] &= 0xFF;
		}
	}

	private int AcToupper(int ch) {
		return (('a' <= (ch) && (ch) <= 'z') ? ((ch) - ('a' - 'A')) : (ch));
	}
}

