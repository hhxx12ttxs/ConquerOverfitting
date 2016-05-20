package net.smstpdu.tpdu;

import java.util.ArrayList;
import java.util.Arrays;

import net.smstpdu.GSMAlphabet;
import net.smstpdu.MessageType;
import net.smstpdu.NumberingPlan;
import net.smstpdu.TBCD;
import net.smstpdu.TPDA_OA_DA;
import net.smstpdu.TypeOfNumber;
import net.smstpdu.tpdcs.TPDCS;
import net.smstpdu.tpdu.deliver.TPSCTS;
import net.smstpdu.tpdu.udh.UDH;
import net.smstpdu.tppid.TPPID;
import net.smstpdu.tppid.TPPID_Interworking;
import net.utils.Utils;

public class SmsDeliver extends TPDU {
	public boolean TP_RP;
	public boolean TP_UDHI;
	public boolean TP_SRI;
	public boolean TP_MMS;
	public TPDA_OA_DA TP_OA;
	public TPPID TP_PID;
	public TPDCS TP_DCS;
	public TPSCTS TP_SCTS;
	public int TP_UDL;
	public byte[] TP_UD;
	public UDH udh;
	
	public SmsDeliver(){
		super();
		type = MessageType.SMS_DELIVER;
	}
	public byte[] encode(){
		ArrayList<Byte> buff = new ArrayList<Byte>();
		byte tmp_byte = 0;
		byte[] tmp_buff = null;

		// TP-UDHI
		tmp_byte += (TP_UDHI ? (byte)0x40 : 0);
		// TP-RP
		tmp_byte += (TP_RP ? (byte)0x80 : 0);
		// TP-MMS
		tmp_byte += (TP_MMS ? (byte)0x04 : 0);
		// TP-SRI
		tmp_byte += (TP_SRI ? (byte)0x20 : 0);
		
		// TP-MTI
		tmp_byte += 0x00;
		buff.add(tmp_byte);

		// TP_OA
		buff.add((byte)TBCD.decode(TP_OA.digits).length());
		tmp_byte = (byte)0x80; // no extension
		tmp_byte += TP_OA.typeOfNumber.getId() + TP_OA.numberingPlan.getId();
		buff.add(tmp_byte);
		Utils.bytesToLst(buff, TP_OA.digits);
		
		// TP-PID
		tmp_byte = (byte)TP_PID.format.getId();
		switch(TP_PID.format){
			case DEFAULT1:
				tmp_byte += TP_PID.interworking.getId();
				if(TP_PID.interworking == TPPID_Interworking.TELEMATIC_INTERWORKING) tmp_byte += TP_PID.device_type1.getId();
				break;
			case DEFAULT2:
				tmp_byte += TP_PID.device_type2.getId();
				break;
		}
		buff.add(tmp_byte);

		// TP-DCS
		tmp_byte = TP_DCS.encode();
		buff.add(tmp_byte);
		
		// TP-Service-Center-Time-Stamp
		Utils.bytesToLst(buff, TP_SCTS.encode());

		// TP-UDL
		if(TP_UDHI){
			// encode udh
			tmp_buff = udh.encode();
			// calculate TP_UDL
			switch(TP_DCS.encoding){
				case DEFAULT:
					TP_UDL = (int)((tmp_buff.length * 8 + GSMAlphabet.getPrePaddingBits(tmp_buff.length)) / 7) + TP_UDL;
					break;
				case _8BIT:
					TP_UDL = TP_UD.length + udh.length + 1;
					break;
				case UCS2:
					TP_UDL = TP_UD.length + udh.length + 1;
					break;
					
			}
		}else{
			switch(TP_DCS.encoding){
				case DEFAULT:
					//TP_UDL = (int)Math.ceil((double)((TP_UD.length) * 8) / 7);
					//TP_UDL = GSMAlphabet.decode(TP_UD).length();
					// has to be calculated before
					break;
				case _8BIT:
					TP_UDL = TP_UD.length;
					break;
				case UCS2:
					TP_UDL = TP_UD.length;
					break;
			}
		}
		//TP_UDL
		buff.add((byte)TP_UDL);
		
		// TP-UDH
		if(TP_UDHI && tmp_buff != null) Utils.bytesToLst(buff, tmp_buff);

		// TP-UD
		Utils.bytesToLst(buff, TP_UD);
		
		// result
		return Utils.list2array(buff);
	}

	public void init(byte[] data) {
		// TP-UDHI
		TP_UDHI = (data[byte_pos] & 0x40) == 0x40;
		// TP-RP
		TP_RP = (data[byte_pos] & 0x80) == 0x80;
		//TP-MMS
		TP_MMS = (data[byte_pos] & 0x04) == 0x04;
		//TP-SRI
		TP_SRI = (data[byte_pos] & 0x20) == 0x20;
		byte_pos++;

		// TP-OA
		TP_OA = new TPDA_OA_DA();
		TP_OA.length = data[byte_pos] & 0xFF;
		byte_pos++;
		TP_OA.typeOfNumber = TypeOfNumber.get(data[byte_pos] & 0x70);
		TP_OA.numberingPlan = NumberingPlan.get(data[byte_pos] & 0x0F);
		byte_pos++;
		if(TP_OA.length > 0){
			// BCD encoded digits, 2 digits per byte
			TP_OA.digits = Arrays.copyOfRange(data, byte_pos, byte_pos + (int)Math.ceil((double)(TP_OA.length) / 2));
			byte_pos += TP_OA.digits.length;
		
		}		

		// TP-PID
		TP_PID = new TPPID();
		TP_PID.init(data[byte_pos]);
		byte_pos++;

		// TP-DCS
		TP_DCS = TPDCS.decode(data[byte_pos]);
		byte_pos++;
		
		// TP-Service-Center-Time-Stamp
		TP_SCTS = TPSCTS.decode(Arrays.copyOfRange(data, byte_pos, byte_pos + 7));
		byte_pos += 7;
		
		// if DCS found and data exists
		if(TP_DCS != null && byte_pos < data.length){
			// TP-UDL
			TP_UDL = data[byte_pos] & 0xFF;
			byte_pos++;

			if(TP_UDL > 0){
				// User Data header
				if(TP_UDHI){
					// security check, some messages have UDHI set but actual
					// UDH is missing
					if((data[byte_pos] & 0xFF) < data.length - byte_pos){
						udh = new UDH();
						udh.length = data[byte_pos] & 0xFF;
						byte_pos++;
						udh.init(Arrays.copyOfRange(data, byte_pos, byte_pos + udh.length));
						byte_pos += udh.length;
					}			
				}
				// TP-UD
				TP_UD = Arrays.copyOfRange(data, byte_pos, data.length);
			}		
		}		
		
		
	}

}

